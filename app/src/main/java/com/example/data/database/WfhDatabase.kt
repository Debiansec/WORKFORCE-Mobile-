package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.WfhDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CommodityEntity::class,
        ActivityAreaEntity::class,
        OccupationEntity::class,
        PollutantEntity::class,
        CustomerEntity::class,
        MineSiteEntity::class,
        FacilityEntity::class,
        AssessmentEntity::class,
        AssessmentAnswerEntity::class,
        EnvironmentalReadingEntity::class,
        ChemicalRegisterEntity::class,
        RemedialActionEntity::class,
        EvidencePhotoEntity::class,
        DmrExposureSampleEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class WfhDatabase : RoomDatabase() {
    abstract fun wfhDao(): WfhDao

    companion object {
        @Volatile
        private var INSTANCE: WfhDatabase? = null

        fun getDatabase(context: Context): WfhDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WfhDatabase::class.java,
                    "wfh_occupational_hygiene.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialMasterData(database.wfhDao())
                    }
                }
            }
        }

        suspend fun populateInitialMasterData(dao: WfhDao) {
            // 1. Mandatory Commodity Codes (DMPR Annexure A1, page 31)
            val commodities = listOf(
                CommodityEntity("AU", "Gold", "Precious metal underground & opencast mining"),
                CommodityEntity("PT", "Platinum group metals", "Bushveld complex underground mining"),
                CommodityEntity("CL", "Coal", "Mpumalanga thermal & coking coal operations"),
                CommodityEntity("CR", "Chrome", "Chromite ore extraction"),
                CommodityEntity("FE", "Iron-ore", "Sishen / Kolomela surface open pits"),
                CommodityEntity("DI", "Diamonds", "Kimberlite pipe and alluvial operations"),
                CommodityEntity("CU", "Copper", "Phalaborwa copper operations"),
                CommodityEntity("MN", "Manganese", "Kalahari manganese field"),
                CommodityEntity("Ni", "Nickel", "Bushveld by-product & base metal"),
                CommodityEntity("UR", "Uranium", "Witwatersrand gold reef co-product"),
                CommodityEntity("AS", "Asbestos", "Historical rehabilitation sites"),
                CommodityEntity("LS", "Limestone", "Cement and aggregate production"),
                CommodityEntity("QD", "Quartzite dimension stone", "Surface quarries"),
                CommodityEntity("SA", "Sand", "Alluvial & silica sand deposits")
            )
            dao.insertCommodities(commodities)

            // 2. Mandatory Activity Area Codes (DMPR Annexure A2, page 32)
            val activities = listOf(
                ActivityAreaEntity("01", "Conventional mining (coal)", false),
                ActivityAreaEntity("02", "Continuous miner (coal)", false),
                ActivityAreaEntity("03", "Longwall mining (coal)", false),
                ActivityAreaEntity("05", "Stoping / pillar extraction (coal)", false),
                ActivityAreaEntity("07", "Opencast", false),
                ActivityAreaEntity("08", "Stoping", true),
                ActivityAreaEntity("09", "Development (single shift)", true),
                ActivityAreaEntity("10", "Development (multiblast)", true),
                ActivityAreaEntity("11", "Shaft sinking", true),
                ActivityAreaEntity("13", "Trackless mining", true),
                ActivityAreaEntity("15", "Ground handling (conveyor / loco's)", true),
                ActivityAreaEntity("16", "Shafts and services", true),
                ActivityAreaEntity("17", "Roving underground", true),
                ActivityAreaEntity("18", "Underground workshops", true),
                ActivityAreaEntity("20", "Crushing", false),
                ActivityAreaEntity("21", "Milling / pulverising", false),
                ActivityAreaEntity("22", "Screening / grading", false),
                ActivityAreaEntity("24", "Concentrating", false),
                ActivityAreaEntity("25", "Heat process", false),
                ActivityAreaEntity("26", "Smelting", false),
                ActivityAreaEntity("28", "Refining", false),
                ActivityAreaEntity("31", "Roving surface", false),
                ActivityAreaEntity("32", "Assay / laboratory", false),
                ActivityAreaEntity("33", "Surface workshops", false)
            )
            dao.insertActivityAreas(activities)

            // 3. Mandatory Occupation Codes (DMPR Annexure A3, page 33-54)
            val occupations = listOf(
                OccupationEntity("20799", "Rock breaking worker", "Mining Production"),
                OccupationEntity("20401", "Drill rig operator (jumbo)", "Drilling & Blasting"),
                OccupationEntity("20402", "Driller: hand percussion/jackhammer", "Stoping"),
                OccupationEntity("20305", "General miner", "Mining Operations"),
                OccupationEntity("20302", "Shift boss", "Mining Supervisory"),
                OccupationEntity("20002", "Mine overseer", "Production Management"),
                OccupationEntity("21109", "Driver: haul truck (underground and opencast)", "Trackless Mobile"),
                OccupationEntity("21106", "Load haul dump driver", "Trackless Mobile"),
                OccupationEntity("40456", "Welder", "Engineering Workshops"),
                OccupationEntity("40447", "Plater/boilermaker", "Engineering Maintenance"),
                OccupationEntity("40314", "Electrician", "Engineering Support"),
                OccupationEntity("40477", "Diesel mechanic", "Mechanical Workshop"),
                OccupationEntity("30207", "Crusher attendant", "Mineral Processing"),
                OccupationEntity("30501", "Smelter", "Pyrometallurgy"),
                OccupationEntity("70121", "Occupational hygienist", "Occupational Health"),
                OccupationEntity("70120", "Occupational health physician", "Medical Surveillance"),
                OccupationEntity("80605", "Change house worker", "Hygiene & Sanitation Services"),
                OccupationEntity("80604", "Change house team leader/supervisor", "Hygiene & Sanitation Services")
            )
            dao.insertOccupations(occupations)

            // 4. Mandatory Pollutants & OELs (DMPR Annexure A4, pages 54-63)
            val pollutants = listOf(
                PollutantEntity("522", "Silica, crystalline: Quartz", "14808-60-7", 0.1, "mg/m3", true, "Lung (Silicosis)"),
                PollutantEntity("459", "Particles not otherwise classified (PNOC): Respirable particulate", "", 3.0, "mg/m3", false, "Respiratory Tract"),
                PollutantEntity("458", "Particles not otherwise classified (PNOC): Inhalable particulate", "", 10.0, "mg/m3", false, "Upper Respiratory"),
                PollutantEntity("151", "Coal dust [respirable particulate <5% quartz]", "", 2.0, "mg/m3", false, "Lung (Pneumoconiosis)"),
                PollutantEntity("598", "Welding fumes", "", 5.0, "mg/m3", false, "Respiratory & Fume fever"),
                PollutantEntity("113", "Carbon monoxide", "630-08-0", 30.0, "ppm", false, "Blood (Carboxyhemoglobin)"),
                PollutantEntity("441", "Nitrogen dioxide", "10102-44-0", 3.0, "ppm", false, "Respiratory (Irritation)"),
                PollutantEntity("543", "Sulphur dioxide", "7446-09-5", 2.0, "ppm", false, "Respiratory (Irritation)"),
                PollutantEntity("341", "Hydrogen sulphide", "7783-06-4", 10.0, "ppm", false, "CNS & Respiratory"),
                PollutantEntity("487", "Platinum mine dust respirable particulate", "", 1.5, "mg/m3", false, "Lung & Allergic Sensitization"),
                PollutantEntity("366", "Lead, elemental and inorganic compounds [as Pb]", "7439-92-1", 0.15, "mg/m3", true, "Blood, CNS, Kidney")
            )
            dao.insertPollutants(pollutants)

            // 5. Customers & Mining Companies
            val customers = listOf(
                CustomerEntity(
                    id = "CUST-001",
                    name = "Mapochs Mine (Pty) Ltd",
                    dmeMineCode = "10383",
                    province = "Limpopo Province",
                    address = "Portion 10 of the farm Goudmyn 337KT, Steelpoort",
                    mineManager = "T. Van Der Merwe",
                    managerEmail = "manager@mapochs.co.za",
                    managerPhone = "+27 (0)13 230 4000",
                    appointedHygienist = "Mr N B Letlape",
                    hygienistEmail = "ChiefOH@workforcehealthsa.co.za",
                    hygienistPhone = "+27 (0)83 456 2673",
                    medicalPractitioner = "Dr. K. Dlamini",
                    medicalEmail = "medical@mapochs.co.za",
                    medicalPhone = "+27 (0)13 230 4050"
                ),
                CustomerEntity(
                    id = "CUST-002",
                    name = "Letaba Crushers (Pty) Ltd",
                    dmeMineCode = "14466",
                    province = "Limpopo Province",
                    address = "Farm Tarentaalrant 24LT, Tzaneen",
                    mineManager = "P. J. Steyn",
                    managerEmail = "psteyn@letabacrushers.co.za",
                    managerPhone = "+27 (0)15 307 1200",
                    appointedHygienist = "Mr N B Letlape",
                    hygienistEmail = "ChiefOH@workforcehealthsa.co.za",
                    hygienistPhone = "+27 (0)83 456 2673",
                    medicalPractitioner = "Dr. S. Mokoena",
                    medicalEmail = "clinic@letabacrushers.co.za",
                    medicalPhone = "+27 (0)15 307 1222"
                ),
                CustomerEntity(
                    id = "CUST-003",
                    name = "Gracelica Mining (Pty) Ltd",
                    dmeMineCode = "12870",
                    province = "Limpopo Province",
                    address = "Portion 10 of the farm Goudmyn 337KT, Steelpoort",
                    mineManager = "J. R. Sithole",
                    managerEmail = "sithole@gracelicamining.co.za",
                    managerPhone = "+27 (0)13 231 9000",
                    appointedHygienist = "Mr N B Letlape",
                    hygienistEmail = "ChiefOH@workforcehealthsa.co.za",
                    hygienistPhone = "+27 (0)83 456 2673",
                    medicalPractitioner = "Dr. M. Botha",
                    medicalEmail = "omp@gracelicamining.co.za",
                    medicalPhone = "+27 (0)13 231 9015"
                ),
                CustomerEntity(
                    id = "CUST-004",
                    name = "Afrimat Ltd / Afrimat CM North (Pty) Ltd",
                    dmeMineCode = "10192",
                    province = "Limpopo Province",
                    address = "Kuipersbult 511 LQ, Lephalale, Waterberg District",
                    mineManager = "Peter Lekgeu",
                    managerEmail = "peter.lekgeu@afrimat.co.za",
                    managerPhone = "+27 (0)14 763 2189",
                    appointedHygienist = "Mr N B Letlape",
                    hygienistEmail = "ChiefOH@workforcehealthsa.co.za",
                    hygienistPhone = "+27 (0)83 456 2673",
                    medicalPractitioner = "Dr K O Sebopa",
                    medicalEmail = "sebopa.omp@afrimat.co.za",
                    medicalPhone = "+27 (0)14 763 9000"
                )
            )
            dao.insertCustomers(customers)

            // 6. Mine Sites
            val sites = listOf(
                MineSiteEntity(
                    id = "SITE-MAP-01",
                    customerId = "CUST-001",
                    name = "Mapochs Magnetite Open Cast & Plant",
                    siteCode = "MAP-OC-01",
                    latitude = -24.78912,
                    longitude = 30.14582,
                    elevationMeters = 890.0,
                    commodityCode = "FE",
                    isUnderground = false
                ),
                MineSiteEntity(
                    id = "SITE-LET-01",
                    customerId = "CUST-002",
                    name = "Letaba Quarry & Crushing Plant",
                    siteCode = "LET-Q-01",
                    latitude = -23.82415,
                    longitude = 30.17120,
                    elevationMeters = 720.0,
                    commodityCode = "QD",
                    isUnderground = false
                ),
                MineSiteEntity(
                    id = "SITE-GRA-01",
                    customerId = "CUST-003",
                    name = "Steelpoort Chrome Shaft 2 & Plant",
                    siteCode = "GRA-CR-02",
                    latitude = -24.75030,
                    longitude = 30.19840,
                    elevationMeters = 910.0,
                    commodityCode = "CR",
                    isUnderground = true
                ),
                MineSiteEntity(
                    id = "SITE-AFR-01",
                    customerId = "CUST-004",
                    name = "Kuipersbult Quarry & Crushing Plant",
                    siteCode = "AFR-KUI-01",
                    latitude = -23.70760,
                    longitude = 27.57939,
                    elevationMeters = 860.0,
                    commodityCode = "QD",
                    isUnderground = false
                )
            )
            dao.insertMineSites(sites)

            // 7. Facilities
            val facilities = listOf(
                FacilityEntity("FAC-001", "SITE-MAP-01", "Clinic Men's Ablution", "Ablution - Male", "District North", 45, true),
                FacilityEntity("FAC-002", "SITE-MAP-01", "Main Mine Office Unisex Change House", "Ablution - Unisex", "Admin Surface", 80, true),
                FacilityEntity("FAC-003", "SITE-MAP-01", "Plant Workshop Change House (Men's)", "Ablution - Male", "District Plant", 120, false),
                FacilityEntity("FAC-004", "SITE-MAP-01", "Lady's Change House", "Ablution - Female", "Admin Complex", 35, true),
                FacilityEntity("FAC-005", "SITE-LET-01", "Quarry Workshop & Ablution", "Ablution - Unisex", "Crushing Zone", 40, true),
                FacilityEntity("FAC-006", "SITE-LET-01", "Engineering Plant Workshop", "Plant Workshop", "Mechanical Bay", 25, false),
                FacilityEntity("FAC-007", "SITE-GRA-01", "Underground Shaft 2 Sub-station & Refuge", "Underground Refuge", "Vent District 3", 60, false),
                FacilityEntity("FAC-008", "SITE-AFR-01", "Kuipersbult Plant Ablution (Men's)", "Ablution - Male", "Processing Plant", 55, true),
                FacilityEntity("FAC-009", "SITE-AFR-01", "Admin & Weighbridge Change House", "Ablution - Unisex", "Surface Gate", 30, true),
                FacilityEntity("FAC-010", "SITE-AFR-01", "Quarry Workshop & Mechanical Bay", "Plant Workshop", "Pit Workshop", 25, false)
            )
            dao.insertFacilities(facilities)

            // 8. Pre-populated Assessments
            val sampleAssessment1 = AssessmentEntity(
                id = "ASSESS-2026-001",
                documentNumber = "WFH-FMT-AF-AWI-FMR-26-001",
                customerId = "CUST-001",
                siteId = "SITE-MAP-01",
                facilityId = "FAC-001",
                templateType = "ABLUTION_CORE_50",
                inspectionDate = System.currentTimeMillis() - 86400000L * 2,
                assessorName = "Mr R R M Taukobong",
                assessorRole = "Occupational Hygiene Assistant",
                hygienistName = "Mr N B Letlape",
                hygienistRole = "Appointed Occupational Hygienist",
                status = "DRAFT",
                latitude = -24.78912,
                longitude = 30.14582,
                gpsAccuracyMeters = 3.8f,
                previousVisitRemedialSummary = "None, first visit for 2026.",
                specialHygienistInstructions = "Inspect all 50 mandatory items. Special focus on basin drainage and chemical SDS verification.",
                executiveSummary = "Inspection conducted at Mapochs Clinic Ablution. 47 questions conforming, 3 remedial actions logged.",
                fieldOfficerComments = "Facility is kept clean overall. Soap dispensers in basin 2 require replenishment and door latch requires adjustment.",
                syncStatus = "SYNCED"
            )

            val sampleAssessment2 = AssessmentEntity(
                id = "ASSESS-2026-005",
                documentNumber = "WFH-LET-THE-AWI-FMR-26-005",
                customerId = "CUST-002",
                siteId = "SITE-LET-01",
                facilityId = "FAC-006",
                templateType = "THERMAL_STRESS_AIRBORNE",
                inspectionDate = System.currentTimeMillis() - 86400000L * 5,
                assessorName = "Mr N B Ngobese",
                assessorRole = "Occupational Hygiene Assistant",
                hygienistName = "Mr N B Letlape",
                hygienistRole = "Appointed Occupational Hygienist",
                status = "APPROVED",
                latitude = -23.82415,
                longitude = 30.17120,
                gpsAccuracyMeters = 4.2f,
                previousVisitRemedialSummary = "None.",
                specialHygienistInstructions = "Surface quarry monitoring under high solar radiation. Measure wet bulb, dry bulb, and WBGT indices.",
                executiveSummary = "Thermal stress levels assessed. Workshop area recorded WBGT below 34°C limit; water hydration stations maintained.",
                fieldOfficerComments = "Adequate shade provided. Fan ventilation operational in mechanical repair bay.",
                syncStatus = "SYNCED"
            )

            dao.insertAssessment(sampleAssessment1)
            dao.insertAssessment(sampleAssessment2)

            // 9. Pre-populate 50 Mandatory Ablution Questions for Assessment 1
            val coreQuestions = generate50MandatoryAblutionAnswers("ASSESS-2026-001")
            dao.insertAnswers(coreQuestions)

            // 10. Environmental Readings
            val readings = listOf(
                EnvironmentalReadingEntity(
                    assessmentId = "ASSESS-2026-001",
                    shiftPeriod = "Start of shift",
                    locationDescription = "Outside general environment",
                    wetBulbTemp = 18.5,
                    dryBulbTemp = 24.2,
                    globeTemp = 26.0,
                    wbgtIndex = 20.8,
                    relativeHumidity = 58.0,
                    windSpeed = 1.4,
                    luxReading = 450.0,
                    noiseDba = 68.5
                ),
                EnvironmentalReadingEntity(
                    assessmentId = "ASSESS-2026-001",
                    shiftPeriod = "Middle of shift",
                    locationDescription = "Inside ablution entrance",
                    wetBulbTemp = 20.1,
                    dryBulbTemp = 27.4,
                    globeTemp = 28.5,
                    wbgtIndex = 22.6,
                    relativeHumidity = 52.0,
                    windSpeed = 0.4,
                    luxReading = 220.0,
                    noiseDba = 55.0
                ),
                EnvironmentalReadingEntity(
                    assessmentId = "ASSESS-2026-005",
                    shiftPeriod = "Warmest period",
                    locationDescription = "Plant workshop boilermaking bay",
                    wetBulbTemp = 23.5,
                    dryBulbTemp = 32.8,
                    globeTemp = 35.1,
                    wbgtIndex = 27.0,
                    relativeHumidity = 45.0,
                    windSpeed = 0.8,
                    windChill = null,
                    luxReading = 380.0,
                    noiseDba = 84.2
                )
            )
            dao.insertEnvironmentalReadings(readings)

            // 11. Chemical Register
            val chemicals = listOf(
                ChemicalRegisterEntity(
                    assessmentId = "ASSESS-2026-001",
                    registerType = "DEEP_CLEANING",
                    chemicalName = "Sanichlor Heavy Duty Degreaser",
                    category = "Alkaline Disinfectant",
                    sdsReturned = true,
                    cleaningProcedureObtained = true,
                    cleaningProcedureReturned = true,
                    comments = "Procedure posted on site wall. Certified bi-monthly deep clean."
                ),
                ChemicalRegisterEntity(
                    assessmentId = "ASSESS-2026-001",
                    registerType = "ROUTINE_CLEANING",
                    chemicalName = "Hygiene Pine Gel Sanitizer",
                    category = "Quaternary Ammonium Compound",
                    sdsReturned = true,
                    cleaningProcedureObtained = true,
                    cleaningProcedureReturned = true,
                    comments = "Daily routine floor scrubbing and tile wash."
                )
            )
            dao.insertChemicals(chemicals)

            // 12. Remedial Actions
            val remedialActions = listOf(
                RemedialActionEntity(
                    id = "REM-2026-001",
                    actionReference = "ACT-MAP-01-01",
                    assessmentId = "ASSESS-2026-001",
                    customerName = "Mapochs Mine (Pty) Ltd",
                    mineLocation = "Portion 10, Steelpoort",
                    facilityName = "Clinic Men's Ablution",
                    nonConformanceDescription = "Basin #2 soap dispenser empty and leaking slightly around valve nozzle.",
                    requiredCorrectiveAction = "Replace dispensing valve and replenish with SABS approved liquid antiseptic soap.",
                    responsiblePerson = "K. Sithole (Hygiene Team Lead)",
                    priority = "MEDIUM",
                    targetDate = "2026-07-15",
                    status = "OPEN"
                ),
                RemedialActionEntity(
                    id = "REM-2026-002",
                    actionReference = "ACT-MAP-01-02",
                    assessmentId = "ASSESS-2026-001",
                    customerName = "Mapochs Mine (Pty) Ltd",
                    mineLocation = "Portion 10, Steelpoort",
                    facilityName = "Clinic Men's Ablution",
                    nonConformanceDescription = "Door closer spring broken on toilet cubicle 1, not latching automatically.",
                    requiredCorrectiveAction = "Install new pneumatic door closer for hygiene privacy and compliance with SANS 10400.",
                    responsiblePerson = "M. Fourie (Mine Maintenance)",
                    priority = "HIGH",
                    targetDate = "2026-07-10",
                    status = "IN_PROGRESS"
                )
            )
            dao.insertRemedialActions(remedialActions)

            // 13. DMR Exposure Samples (Annexure C, page 73-77)
            val dmrSamples = listOf(
                DmrExposureSampleEntity(
                    id = "DMR-SMP-001",
                    assessmentId = "ASSESS-2026-001",
                    mineCode = "10383",
                    reportingPeriod = "Q1 Jan-March 2026",
                    commodityCode = "FE",
                    sampleArea = "Opencast",
                    activityAreaCode = "07",
                    hegNumber = "HEG 1.1",
                    hegDescription = "Drilling and Blasting Team",
                    occupationCode = "20404",
                    occupationTitle = "Driller: opencast/large diameter",
                    employeeInitialsSurname = "T. M. Khumalo",
                    employeeIdNumber = "8503145678081",
                    companyNumber = "EMP-8841",
                    pollutantCode = "522",
                    sampleConcentrationTwa = 0.065,
                    analysisPercent = 4.2,
                    oelLimit = 0.1,
                    allocatedMedicalDose = 0.065,
                    calculated90thPercentile = 0.082,
                    classificationBand = "B",
                    pollutantIndex = 0.65,
                    aqiCombinedIndex = 0.72,
                    reasonsForOverExposure = "None, exposure well within compliance limits (<0.1 mg/m3).",
                    correctiveMeasures = "Continued wet drilling dust suppression."
                ),
                DmrExposureSampleEntity(
                    id = "DMR-SMP-002",
                    assessmentId = "ASSESS-2026-005",
                    mineCode = "14466",
                    reportingPeriod = "Q2 Apr-June 2026",
                    commodityCode = "QD",
                    sampleArea = "Surface",
                    activityAreaCode = "20",
                    hegNumber = "HEG 2.1",
                    hegDescription = "Crushing and Screening Plant Operators",
                    occupationCode = "30207",
                    occupationTitle = "Crusher attendant",
                    employeeInitialsSurname = "S. P. Ndlovu",
                    employeeIdNumber = "9107225129087",
                    companyNumber = "LET-302",
                    pollutantCode = "459",
                    sampleConcentrationTwa = 1.25,
                    analysisPercent = 98.0,
                    oelLimit = 3.0,
                    allocatedMedicalDose = 1.25,
                    calculated90thPercentile = 1.85,
                    classificationBand = "B",
                    pollutantIndex = 0.42,
                    aqiCombinedIndex = 0.58,
                    reasonsForOverExposure = "Wind swirl during dry shift transfer conveyor.",
                    correctiveMeasures = "Enclosed chute skirts and atomised water spray nozzles adjusted."
                )
            )
            dao.insertDmrExposureSamples(dmrSamples)
        }

        fun generate50MandatoryAblutionAnswers(assessmentId: String): List<AssessmentAnswerEntity> {
            val list = mutableListOf<AssessmentAnswerEntity>()

            // Section A — Basins: 5 questions (Ablution PDF Page 14 & 16)
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 1, questionText = "Are the basins clean?", response = "YES", remarks = "Cleaned during morning schedule"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 2, questionText = "Are the soap dispensers filled with liquid soap?", response = "NO", remarks = "Basin #2 empty, logged for refill", isNonConformance = true))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 3, questionText = "Do both water taps close properly?", response = "YES", remarks = "No dripping observed"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 4, questionText = "Is there a sign / poster above each basin illustrating the correct way to wash your hands properly?", response = "YES", remarks = "SABS laminated poster present"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 5, questionText = "Are there any signs of molds / yeast / mildew around or inside the basin?", response = "NO", remarks = "All sealant clean"))

            // Section B — Showers: 5 questions (Ablution PDF Page 17 & 20)
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "B_SHOWERS", questionNumber = 6, questionText = "Are all the showers clean?", response = "YES", remarks = "Tiles scrubbed"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "B_SHOWERS", questionNumber = 7, questionText = "Do both water taps close properly?", response = "YES", remarks = "Water shutoff tested"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "B_SHOWERS", questionNumber = 8, questionText = "Are proper shower heads fitted on all the showers?", response = "YES", remarks = "Standard flow heads"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "B_SHOWERS", questionNumber = 9, questionText = "Are the shower curtains / doors in good condition and closes properly providing the necessary privacy?", response = "YES", remarks = "Opaque cubicle doors"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "B_SHOWERS", questionNumber = 10, questionText = "Is there an informative poster explaining bacterial and fungal infections?", response = "YES", remarks = "Informative sign posted at entrance"))

            // Section C — Toilets: 7 questions (Ablution PDF Page 14 & 17)
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 11, questionText = "Are the toilets clean and the floors dry?", response = "YES", remarks = "Dry floor surfaces"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 12, questionText = "Are all toilet water bowls fitted with lids?", response = "YES", remarks = "Fitted on all 4 pans"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 13, questionText = "Are all toilets fitted with sitting seats?", response = "YES", remarks = "Heavy duty plastic seats"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 14, questionText = "Are all toilets flushing properly?", response = "YES", remarks = "Strong dual-flush pressure"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 15, questionText = "Is there enough toilet paper in all the toilets?", response = "YES", remarks = "Twin-roll dispensers full"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 16, questionText = "Does the toilet door close properly?", response = "NO", remarks = "Cubicle 1 latch loose", isNonConformance = true))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 17, questionText = "Are there any signs of molds / yeast / mildew?", response = "NO", remarks = "No discoloration detected"))

            // Section D — Urinals: 5 questions (Ablution PDF Page 14 & 17)
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_URINALS", questionNumber = 18, questionText = "Are all urinals clean?", response = "YES", remarks = "Clean ceramic surface"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_URINALS", questionNumber = 19, questionText = "Do all the urinals flush properly?", response = "YES", remarks = "Auto-sensor flush operational"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_URINALS", questionNumber = 20, questionText = "Are the floors around the urinals clean and dry?", response = "YES", remarks = "Floor drain clear"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_URINALS", questionNumber = 21, questionText = "Are there any bad odours in the urinal area?", response = "NO", remarks = "Deodorizer blocks in place"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_URINALS", questionNumber = 22, questionText = "Are there any signs of moulds / yeast / mildew?", response = "NO", remarks = "Satisfactory condition"))

            // Section E — Lockers: 5 questions (Ablution PDF Page 17 & 21)
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_LOCKERS", questionNumber = 23, questionText = "Are all employees issued with their own lockers?", response = "YES", remarks = "One locker per appointed staff"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_LOCKERS", questionNumber = 24, questionText = "Are all lockers marked with their respective owner's names and / or coy numbers?", response = "YES", remarks = "Clear stencil numbering"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_LOCKERS", questionNumber = 25, questionText = "Do the doors of the lockers close properly?", response = "YES", remarks = "Hinges intact"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_LOCKERS", questionNumber = 26, questionText = "Can the doors of the lockers lock?", response = "YES", remarks = "Padlock hasps functional"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_LOCKERS", questionNumber = 27, questionText = "Is the locker area clean?", response = "YES", remarks = "Passageway swept"))

            // Section F — General: 23 questions (Ablution PDF Page 15, 18, 22)
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 28, questionText = "Is there a sign on the toilet/change house door indicating the gender?", response = "YES", remarks = "Bold pictorial signage"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 29, questionText = "Are there signs / posters above every basin illustrating the correct way to wash hands?", response = "YES", remarks = "Laminated posters"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 30, questionText = "Are there signs / posters at the showers educating employees about fungal infections?", response = "YES", remarks = "Posted at shower entrance"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 31, questionText = "Is there a rubbish bin?", response = "YES", remarks = "Foot-pedal stainless steel bin"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 32, questionText = "Is there a bin liner in the rubbish bin?", response = "YES", remarks = "Heavy duty black liner"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 33, questionText = "Is the facility deeply cleaned / sanitised – state frequency under remarks?", response = "YES", remarks = "Bi-weekly deep chemical wash"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 34, questionText = "Has a certificate been issued by the deep cleaning / sanitising company?", response = "YES", remarks = "Cert #DC-2026-048 on file"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 35, questionText = "Is there a cleaning schedule and register in the toilet – state frequency?", response = "YES", remarks = "Daily 3x shift signoff"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 36, questionText = "Is the register signed off after cleaning and up to date?", response = "YES", remarks = "Signed by morning cleaner"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 37, questionText = "Is there a complaint / compliment book in the facility?", response = "YES", remarks = "Mounted on entrance board"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 38, questionText = "Is the area around the toilet clean and tidy?", response = "YES", remarks = "Outdoor perimeter swept"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 39, questionText = "Is personal hygiene in the toilet up to standard?", response = "YES", remarks = "Satisfactory compliance"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 40, questionText = "Do the facility doors close automatically and properly?", response = "YES", remarks = "Pneumatic arm operational"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 41, questionText = "Are there any bad odours in the facility?", response = "NO", remarks = "No sewage or stagnant odour"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 42, questionText = "Are employees trained on the importance of personal hygiene at work – state frequency?", response = "YES", remarks = "Monthly safety toolbox talks"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 43, questionText = "Has a ventilation assessment been conducted – reference date and report number?", response = "YES", remarks = "Rep #VENT-2026-03, date 15 May 2026"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 44, questionText = "Has an illumination assessment been conducted – reference date and report number?", response = "YES", remarks = "Rep #LUX-2026-11, 240 lux recorded"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 45, questionText = "Review all complaints / compliments in the complaint book and log in remarks?", response = "YES", remarks = "None logged in past 30 days"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 46, questionText = "List the approximate total of employees using the facility in remarks?", response = "YES", remarks = "Approximately 45 daily employees"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 47, questionText = "Provide full details of deep cleaning methods & chemicals with SDS?", response = "YES", remarks = "Sanichlor and Pine Gel SDS verified"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 48, questionText = "Are windows in the facility of the frozen type to protect privacy?", response = "YES", remarks = "Frosted privacy glass"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 49, questionText = "Obtain full list of chemicals used for routine cleaning with SDS?", response = "YES", remarks = "SDS binder accessible at cleaners store"))
            list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 50, questionText = "Are the facilities disabled person friendly?", response = "YES", remarks = "Ramp access, grab rails, widened doorway"))

            return list
        }

        fun generateAnswersForAblutionType(assessmentId: String, ablutionType: String): List<AssessmentAnswerEntity> {
            val list = mutableListOf<AssessmentAnswerEntity>()

            when (ablutionType) {
                "Lady’s change house" -> {
                    // Monthly ablution facilities checklist Female
                    // Basins (5)
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 1, questionText = "Are the basins clean?", response = "YES", remarks = "Cleaned during morning schedule"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 2, questionText = "Are the soap dispensers filled with liquid soap?", response = "YES", remarks = "Filled with approved disinfectant soap"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 3, questionText = "Do both water taps close properly?", response = "YES", remarks = "No dripping observed"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 4, questionText = "Is there a sign / poster above each basin illustrating correct hand washing?", response = "YES", remarks = "SABS laminated poster present"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 5, questionText = "Are there any signs of molds / yeast / mildew around or inside the basin?", response = "NO", remarks = "All sealant clean"))

                    // Showers (5)
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "B_SHOWERS", questionNumber = 6, questionText = "Are all the showers clean?", response = "YES", remarks = "Tiles scrubbed and disinfected"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "B_SHOWERS", questionNumber = 7, questionText = "Do both water taps close properly?", response = "YES", remarks = "Water shutoff tested"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "B_SHOWERS", questionNumber = 8, questionText = "Are proper shower heads fitted on all the showers?", response = "YES", remarks = "Standard flow heads fitted"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "B_SHOWERS", questionNumber = 9, questionText = "Are the shower cubicle doors in good condition providing necessary privacy?", response = "YES", remarks = "Full height cubicle doors with locks"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "B_SHOWERS", questionNumber = 10, questionText = "Is there an informative poster explaining fungal and bacterial infections?", response = "YES", remarks = "Poster at shower entrance"))

                    // Toilets (7)
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 11, questionText = "Are the toilets clean and the floors dry?", response = "YES", remarks = "Dry floor surfaces"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 12, questionText = "Are all toilet water bowls fitted with lids?", response = "YES", remarks = "Fitted on all pans"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 13, questionText = "Are all toilets fitted with sitting seats?", response = "YES", remarks = "Heavy duty seats intact"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 14, questionText = "Are all toilets flushing properly?", response = "YES", remarks = "Dual flush mechanism working"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 15, questionText = "Is there enough toilet paper in all the toilets?", response = "YES", remarks = "Dispensers replenished"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 16, questionText = "Does the toilet cubicle door close and latch properly?", response = "YES", remarks = "Latches and locks functional"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 17, questionText = "Are there any signs of molds / yeast / mildew?", response = "NO", remarks = "Grouting clean"))

                    // Sanitary / Feminine Hygiene Facilities (5)
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_SANITARY", questionNumber = 18, questionText = "Is there a sanitary disposal bin in each female cubicle?", response = "YES", remarks = "Dedicated sanitary bin per cubicle"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_SANITARY", questionNumber = 19, questionText = "Are sanitary bins pedal-operated with close-fitting lids?", response = "YES", remarks = "Pedal lids working, no hands required"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_SANITARY", questionNumber = 20, questionText = "Are sanitary disposal bags / liners provided in dispensers?", response = "YES", remarks = "Dispensers filled"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_SANITARY", questionNumber = 21, questionText = "Is sanitary waste removed and serviced at required frequency by certified waste contractor?", response = "YES", remarks = "Contractor collection log on file"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_SANITARY", questionNumber = 22, questionText = "Are female hygiene educational posters mounted and in good condition?", response = "YES", remarks = "Laminated posters mounted"))

                    // Lockers (5)
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_LOCKERS", questionNumber = 23, questionText = "Are all employees issued with their own lockers?", response = "YES", remarks = "One locker per employee"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_LOCKERS", questionNumber = 24, questionText = "Are all lockers marked with employee name and company number?", response = "YES", remarks = "Clearly stenciled"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_LOCKERS", questionNumber = 25, questionText = "Do the doors of the lockers close properly?", response = "YES", remarks = "Hinges intact"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_LOCKERS", questionNumber = 26, questionText = "Can the doors of the lockers be locked securely?", response = "YES", remarks = "Hasps secure"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_LOCKERS", questionNumber = 27, questionText = "Is the locker area clean and dry?", response = "YES", remarks = "Aisles clear and swept"))

                    // General (23)
                    val generalQuestions = generate50MandatoryAblutionAnswers(assessmentId).filter { it.sectionCode == "F_GENERAL" }
                    list.addAll(generalQuestions)
                }
                "Lady’s office toilet" -> {
                    // Monthly toilet and basin checklist Male / Female
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 1, questionText = "Are the basins clean and free of stains?", response = "YES", remarks = "Morning clean completed"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 2, questionText = "Is the liquid soap dispenser filled and operating?", response = "YES", remarks = "Full antibacterial soap"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 3, questionText = "Do hot and cold taps close tightly with no dripping?", response = "YES", remarks = "Valves tested"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 4, questionText = "Is there a proper hand washing instructional sign above the basin?", response = "YES", remarks = "Framed notice in place"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 5, questionText = "Are there any signs of mold, mildew or lime scale?", response = "NO", remarks = "No mold present"))

                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 6, questionText = "Are the toilets clean and the floor dry?", response = "YES", remarks = "Clean dry floor"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 7, questionText = "Is the toilet bowl fitted with an intact lid and seat?", response = "YES", remarks = "Lid and seat intact"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 8, questionText = "Does the toilet flush properly with adequate water pressure?", response = "YES", remarks = "Flushing normal"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 9, questionText = "Is adequate 2-ply toilet paper supplied in dispensers?", response = "YES", remarks = "Rolls full"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 10, questionText = "Does the cubicle door close, latch and lock smoothly for privacy?", response = "YES", remarks = "Lock mechanism functional"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 11, questionText = "Are walls and partitions free of graffiti and damage?", response = "YES", remarks = "Clean painted walls"))

                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_SANITARY", questionNumber = 12, questionText = "Is a sanitary disposal pedal bin provided inside the cubicle?", response = "YES", remarks = "Sanitary bin present"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_SANITARY", questionNumber = 13, questionText = "Are plastic sanitary bag liners provided and bin clean?", response = "YES", remarks = "Bag dispenser filled"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_SANITARY", questionNumber = 14, questionText = "Is the sanitary bin serviced and emptied regularly?", response = "YES", remarks = "Service label verified"))

                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_HAND_DRYING", questionNumber = 15, questionText = "Is paper towel dispenser stocked or warm air hand dryer operational?", response = "YES", remarks = "Automated warm air dryer operational"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_HAND_DRYING", questionNumber = 16, questionText = "Is a lined waste bin provided for paper towels?", response = "YES", remarks = "Stainless steel waste bin provided"))

                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 17, questionText = "Is natural or mechanical ventilation adequate with no foul odours?", response = "YES", remarks = "Extractor fan operating"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 18, questionText = "Is lighting bright and all lamps functioning?", response = "YES", remarks = "LED lighting > 200 lux"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 19, questionText = "Is there a daily cleaning sign-off schedule displayed and signed?", response = "YES", remarks = "Daily log on back of door"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 20, questionText = "Is the facility accessible for disabled persons?", response = "YES", remarks = "Wide entrance doorway, grab bar installed"))
                }
                "Men’s office toilet" -> {
                    // Monthly toilet and basin checklist Male / Female
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 1, questionText = "Are the basins clean and free of stains?", response = "YES", remarks = "Morning clean completed"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 2, questionText = "Is the liquid soap dispenser filled and operating?", response = "YES", remarks = "Full antibacterial soap"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 3, questionText = "Do hot and cold taps close tightly with no dripping?", response = "YES", remarks = "Valves tested"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 4, questionText = "Is there a proper hand washing instructional sign above the basin?", response = "YES", remarks = "Framed notice in place"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "A_BASINS", questionNumber = 5, questionText = "Are there any signs of mold, mildew or lime scale?", response = "NO", remarks = "No mold present"))

                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 6, questionText = "Are the toilets clean and the floor dry?", response = "YES", remarks = "Clean dry floor"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 7, questionText = "Is the toilet bowl fitted with an intact lid and seat?", response = "YES", remarks = "Lid and seat intact"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 8, questionText = "Does the toilet flush properly with adequate water pressure?", response = "YES", remarks = "Flushing normal"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 9, questionText = "Is adequate 2-ply toilet paper supplied in dispensers?", response = "YES", remarks = "Rolls full"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "C_TOILETS", questionNumber = 10, questionText = "Does the cubicle door close, latch and lock smoothly for privacy?", response = "YES", remarks = "Lock mechanism functional"))

                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_URINALS", questionNumber = 11, questionText = "Are all urinals clean with no stains or crust buildup?", response = "YES", remarks = "Ceramic clean"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_URINALS", questionNumber = 12, questionText = "Do the urinals flush properly with auto/manual flusher?", response = "YES", remarks = "Auto flush cycle operating"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_URINALS", questionNumber = 13, questionText = "Are deodorizer blocks present with no bad odours?", response = "YES", remarks = "Deodorizer blocks in place"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "D_URINALS", questionNumber = 14, questionText = "Are floors around urinals dry and free of splash / pooling?", response = "YES", remarks = "Floor drain clear and dry"))

                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_HAND_DRYING", questionNumber = 15, questionText = "Is paper towel dispenser stocked or warm air hand dryer operational?", response = "YES", remarks = "Paper towel dispenser full"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "E_HAND_DRYING", questionNumber = 16, questionText = "Is a lined waste bin provided for paper towels?", response = "YES", remarks = "Bin in corner with liner"))

                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 17, questionText = "Is natural or mechanical ventilation adequate with no foul odours?", response = "YES", remarks = "Ventilation window and fan"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 18, questionText = "Is lighting bright and all lamps functioning?", response = "YES", remarks = "Illumination > 200 lux"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 19, questionText = "Is there a daily cleaning sign-off schedule displayed and signed?", response = "YES", remarks = "Daily log on back of door"))
                    list.add(AssessmentAnswerEntity(assessmentId = assessmentId, sectionCode = "F_GENERAL", questionNumber = 20, questionText = "Is the facility accessible for disabled persons?", response = "YES", remarks = "Level threshold, grab rails"))
                }
                else -> {
                    // Default Men's Change House 50-point checklist
                    list.addAll(generate50MandatoryAblutionAnswers(assessmentId))
                }
            }

            return list
        }

        fun generateInitialShiftReadings(assessmentId: String, location: String): List<EnvironmentalReadingEntity> {
            return listOf(
                EnvironmentalReadingEntity(
                    assessmentId = assessmentId,
                    shiftPeriod = "Start of shift",
                    measurementTime = "06:30",
                    locationDescription = location.ifEmpty { "Ablution Facility Main Corridor" },
                    wetBulbTemp = null, // null distinguishes not taken from 0.0
                    dryBulbTemp = null,
                    globeTemp = null,
                    wbgtIndex = null,
                    relativeHumidity = null,
                    windSpeed = null,
                    generalConditions = "Cool morning, overcast sky, calm air",
                    equipmentUsed = "WBGT Logger AZ 87786 & Kestrel 4000",
                    instrumentModel = "WBGT Logger model AZ 87786",
                    serialNumber = "SN-AZ87786-01",
                    comments = "Baseline shift calibration verified"
                ),
                EnvironmentalReadingEntity(
                    assessmentId = assessmentId,
                    shiftPeriod = "Middle of shift",
                    measurementTime = "11:45",
                    locationDescription = location.ifEmpty { "Ablution Facility Main Corridor" },
                    wetBulbTemp = null,
                    dryBulbTemp = null,
                    globeTemp = null,
                    wbgtIndex = null,
                    relativeHumidity = null,
                    windSpeed = null,
                    generalConditions = "Midday solar heat, light breeze",
                    equipmentUsed = "WBGT Logger AZ 87786 & Kestrel 4000",
                    instrumentModel = "WBGT Logger model AZ 87786",
                    serialNumber = "SN-AZ87786-01",
                    comments = "Peak shift operational verification"
                ),
                EnvironmentalReadingEntity(
                    assessmentId = assessmentId,
                    shiftPeriod = "End of shift",
                    measurementTime = "15:15",
                    locationDescription = location.ifEmpty { "Ablution Facility Main Corridor" },
                    wetBulbTemp = null,
                    dryBulbTemp = null,
                    globeTemp = null,
                    wbgtIndex = null,
                    relativeHumidity = null,
                    windSpeed = null,
                    generalConditions = "Late afternoon cooling, light haze",
                    equipmentUsed = "WBGT Logger AZ 87786 & Kestrel 4000",
                    instrumentModel = "WBGT Logger model AZ 87786",
                    serialNumber = "SN-AZ87786-01",
                    comments = "End of shift verification"
                )
            )
        }
    }
}
