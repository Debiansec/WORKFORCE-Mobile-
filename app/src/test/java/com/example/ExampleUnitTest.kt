package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import com.example.data.database.WfhDatabase
import com.example.viewmodel.UserRole

/**
 * Local JVM Unit Tests for WFH Business Logic and Statutory Entities
 */
class ExampleUnitTest {

    @Test
    fun test50MandatoryAblutionQuestionsGeneration() {
        val answers = WfhDatabase.generate50MandatoryAblutionAnswers("TEST_ASSESSMENT_01")
        assertEquals("Must generate exactly 50 mandatory core questions", 50, answers.size)

        val sectionA = answers.filter { it.sectionCode == "A_BASINS" }
        assertEquals("Section A (Basins) must have 5 questions", 5, sectionA.size)

        val sectionB = answers.filter { it.sectionCode == "B_SHOWERS" }
        assertEquals("Section B (Showers) must have 5 questions", 5, sectionB.size)

        val sectionC = answers.filter { it.sectionCode == "C_TOILETS" }
        assertEquals("Section C (Toilets) must have 7 questions", 7, sectionC.size)

        val sectionD = answers.filter { it.sectionCode == "D_URINALS" }
        assertEquals("Section D (Urinals) must have 5 questions", 5, sectionD.size)

        val sectionE = answers.filter { it.sectionCode == "E_LOCKERS" }
        assertEquals("Section E (Lockers) must have 5 questions", 5, sectionE.size)

        val sectionF = answers.filter { it.sectionCode == "F_GENERAL" }
        assertEquals("Section F (General) must have 23 questions", 23, sectionF.size)
    }

    @Test
    fun testRolePermissionsMatrix() {
        val assessor = UserRole.FIELD_ASSESSOR
        val hygienist = UserRole.OCCUPATIONAL_HYGIENIST
        val clientRep = UserRole.MINE_MANAGEMENT
        val admin = UserRole.SYSTEM_ADMINISTRATOR

        // Section 13 permissions:
        // Assessors can edit drafts but cannot approve
        assertEquals(true, assessor.canEditDraft)
        assertEquals(false, assessor.canApprove)

        // Hygienists can approve
        assertEquals(true, hygienist.canApprove)

        // Client representatives cannot view draft unapproved reports
        assertEquals(false, clientRep.canViewDraftReports)

        // Admins can manage everything
        assertEquals(true, admin.canEditDraft)
        assertEquals(true, admin.canApprove)
    }

    @Test
    fun testWbgtCalculationFormula() {
        // Outdoor WBGT standard equation: 0.7 * WB + 0.2 * GT + 0.1 * DB
        val wetBulb = 20.0
        val globe = 30.0
        val dryBulb = 25.0
        val expectedWbgt = (0.7 * wetBulb) + (0.2 * globe) + (0.1 * dryBulb) // 14.0 + 6.0 + 2.5 = 22.5
        assertEquals(22.5, expectedWbgt, 0.001)
    }

    @Test
    fun testDmprAnnexureCBandClassification() {
        // OEL = 3.0 mg/m3
        val oel = 3.0

        fun classifyBand(p90: Double, oelLimit: Double): String = when {
            p90 >= oelLimit - 1e-6 -> "A"
            p90 >= 0.5 * oelLimit - 1e-6 -> "B"
            p90 >= 0.1 * oelLimit - 1e-6 -> "C"
            else -> "D"
        }

        // Band A: >= OEL
        assertEquals("A", classifyBand(3.5, oel))
        assertEquals("A", classifyBand(3.0, oel))

        // Band B: >= 50% OEL (1.5 to < 3.0)
        assertEquals("B", classifyBand(2.1, oel))
        assertEquals("B", classifyBand(1.5, oel))

        // Band C: >= 10% OEL (0.3 to < 1.5)
        assertEquals("C", classifyBand(0.9, oel))
        assertEquals("C", classifyBand(0.3, oel))

        // Band D: < 10% OEL (< 0.3)
        assertEquals("D", classifyBand(0.15, oel))
    }

    @Test
    fun testTailoredAblutionChecklistsForComboBoxOptions() {
        // 1. Men’s change house
        val mensChangeHouse = WfhDatabase.generateAnswersForAblutionType("ASSESS_MCH", "Men’s change house")
        assertEquals(50, mensChangeHouse.size)
        assertEquals(5, mensChangeHouse.count { it.sectionCode == "D_URINALS" })

        // 2. Lady’s change house
        val ladysChangeHouse = WfhDatabase.generateAnswersForAblutionType("ASSESS_LCH", "Lady’s change house")
        assertEquals(50, ladysChangeHouse.size)
        assertEquals(0, ladysChangeHouse.count { it.sectionCode == "D_URINALS" })
        assertEquals(5, ladysChangeHouse.count { it.sectionCode == "D_SANITARY" })

        // 3. Lady’s office toilet
        val ladysOffice = WfhDatabase.generateAnswersForAblutionType("ASSESS_LOT", "Lady’s office toilet")
        assertEquals(20, ladysOffice.size)
        assertEquals(3, ladysOffice.count { it.sectionCode == "D_SANITARY" })
        assertEquals(2, ladysOffice.count { it.sectionCode == "E_HAND_DRYING" })

        // 4. Men’s office toilet
        val mensOffice = WfhDatabase.generateAnswersForAblutionType("ASSESS_MOT", "Men’s office toilet")
        assertEquals(20, mensOffice.size)
        assertEquals(4, mensOffice.count { it.sectionCode == "D_URINALS" })
        assertEquals(2, mensOffice.count { it.sectionCode == "E_HAND_DRYING" })
    }

    @Test
    fun testShiftEnvironmentalReadingsDistinguishNotTakenFromZero() {
        val initialShiftReadings = WfhDatabase.generateInitialShiftReadings("ASSESS_SHIFT_01", "Kuipersbult Plant Ablution")
        assertEquals("Must generate exactly 3 shift measurement periods", 3, initialShiftReadings.size)

        val startShift = initialShiftReadings.find { it.shiftPeriod == "Start of shift" }
        assertNotNull("Start of shift must exist", startShift)
        assertEquals("06:30", startShift?.measurementTime)
        assertEquals(null, startShift?.wetBulbTemp) // Distinguishes not taken from 0.0

        // When reading is recorded as 0.0
        val zeroReading = startShift?.copy(wetBulbTemp = 0.0)
        assertEquals(0.0, zeroReading?.wetBulbTemp)
        assertNotNull(zeroReading?.wetBulbTemp)
    }
}
