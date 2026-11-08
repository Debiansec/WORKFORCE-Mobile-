package com.example.data.model

import androidx.room.*

/**
 * 1. Customer Entity (Top of Hierarchy)
 */
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dmeMineCode: String,
    val province: String,
    val address: String,
    val mineManager: String,
    val managerEmail: String,
    val managerPhone: String,
    val appointedHygienist: String,
    val hygienistEmail: String,
    val hygienistPhone: String,
    val medicalPractitioner: String,
    val medicalEmail: String,
    val medicalPhone: String
)

/**
 * 2. Mine Site Entity (Child of Customer)
 */
@Entity(
    tableName = "mine_sites",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["customerId"])]
)
data class MineSiteEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val name: String,
    val siteCode: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double = 0.0,
    val commodityCode: String = "AU",
    val isUnderground: Boolean = false
)

/**
 * 3. Facility Entity (Child of Mine Site)
 */
@Entity(
    tableName = "facilities",
    foreignKeys = [
        ForeignKey(
            entity = MineSiteEntity::class,
            parentColumns = ["id"],
            childColumns = ["siteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["siteId"])]
)
data class FacilityEntity(
    @PrimaryKey val id: String,
    val siteId: String,
    val name: String,
    val type: String, // "Ablution - Male", "Ablution - Female", "Ablution - Unisex", "Plant Workshop", "Smelter"
    val ventilationDistrict: String = "",
    val approxUserCount: Int = 50,
    val disabledFriendly: Boolean = false
)

/**
 * 4. Assessment Entity (Child of Facility, Site, and Customer)
 */
@Entity(
    tableName = "assessments",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MineSiteEntity::class,
            parentColumns = ["id"],
            childColumns = ["siteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FacilityEntity::class,
            parentColumns = ["id"],
            childColumns = ["facilityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["siteId"]),
        Index(value = ["facilityId"])
    ]
)
data class AssessmentEntity(
    @PrimaryKey val id: String,
    val documentNumber: String, // e.g. WFH-FMT-AF-AWI-FMR-26-001
    val customerId: String,
    val siteId: String,
    val facilityId: String,
    val templateType: String, // "ABLUTION_CORE_50", "THERMAL_STRESS_AIRBORNE", "GENERAL_HYGIENE"
    val ablutionType: String = "Men’s change house", // "Men’s change house", "Lady’s change house", "Lady’s office toilet", "Men’s office toilet"
    val checklistTitle: String = "Monthly ablution facilities checklist Male",
    val facilityLocationDescription: String = "", // Description and / or location
    val inspectionDate: Long, // epoch timestamp
    val dateOfFieldVisit: String = "", // Formatted date e.g. "08 Nov 2026"
    val previousSiteVisitDate: String = "15 Oct 2026",
    val clientName: String = "",
    val mineNameAndLocation: String = "",
    val dmeMineCode: String = "",
    val mineManagerName: String = "",
    val mineManagerContact: String = "",
    val hygienistName: String = "Mr N B Letlape",
    val hygienistRole: String = "Appointed Occupational Hygienist",
    val hygienistContact: String = "+27 (0)83 456 2673",
    val medicalPractitionerName: String = "Dr K O Sebopa",
    val medicalPractitionerContact: String = "+27 (0)14 763 9000",
    val assessorName: String = "Mr R R M Taukobong",
    val assessorRole: String = "Occupational Hygiene Assistant",
    val assessmentType: String = "Ablution Facility Hygiene Inspection",
    val status: String, // "DRAFT", "SUBMITTED", "APPROVED", "REJECTED"
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val gpsAccuracyMeters: Float = 0f,
    val previousVisitRemedialSummary: String = "None, compliant baseline",
    val specialHygienistInstructions: String = "Perform full verification according to MHSA regulations",
    val executiveSummary: String = "",
    val fieldOfficerComments: String = "",
    val assessorSignatureSvg: String = "",
    val hygienistSignatureSvg: String = "",
    val signatureTimestamp: Long = 0L,
    val payloadHash: String = "",
    val syncStatus: String = "SYNCED", // "ONLINE", "OFFLINE_DRAFT", "SYNCING", "SYNCED", "SYNC_FAILED"
    val lastModifiedTimestamp: Long = System.currentTimeMillis()
)

/**
 * 5. Assessment Answer Entity (Child of Assessment - Checklist Questions)
 */
@Entity(
    tableName = "assessment_answers",
    foreignKeys = [
        ForeignKey(
            entity = AssessmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assessmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["assessmentId"])]
)
data class AssessmentAnswerEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val assessmentId: String,
    val sectionCode: String, // "A_BASINS", "B_SHOWERS", "C_TOILETS", "D_URINALS", "D_SANITARY", "E_LOCKERS", "F_GENERAL", "E_HAND_DRYING"
    val questionNumber: Int,
    val questionText: String,
    val response: String, // "YES", "NO", "N/A"
    val remarks: String = "",
    val isNonConformance: Boolean = false,
    val photoUri: String = "",
    val requiredRemedialAction: String = "",
    val responsiblePerson: String = "",
    val followUpDate: String = "",
    val isClosureConfirmed: Boolean = false
)

// ==========================================
// TYPE-SAFE HIERARCHY RELATIONS (1-to-Many / 1-to-1)
// ==========================================

/**
 * Facility with all child Assessments
 */
data class FacilityWithAssessments(
    @Embedded val facility: FacilityEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "facilityId"
    )
    val assessments: List<AssessmentEntity>
)

/**
 * Mine Location with all Facilities and their Assessments
 */
data class MineSiteWithFacilities(
    @Embedded val mineSite: MineSiteEntity,
    @Relation(
        entity = FacilityEntity::class,
        parentColumn = "id",
        entityColumn = "siteId"
    )
    val facilities: List<FacilityWithAssessments>
)

/**
 * Customer organization with complete Mine hierarchy
 */
data class CustomerWithMines(
    @Embedded val customer: CustomerEntity,
    @Relation(
        entity = MineSiteEntity::class,
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val mineSites: List<MineSiteWithFacilities>
)

/**
 * Full Assessment Payload with Parent Context and all 50 Answers (Customer -> Mine -> Facility -> Assessment -> Answers)
 */
data class FullAssessmentHierarchy(
    @Embedded val assessment: AssessmentEntity,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: CustomerEntity,
    @Relation(
        parentColumn = "siteId",
        entityColumn = "id"
    )
    val site: MineSiteEntity,
    @Relation(
        parentColumn = "facilityId",
        entityColumn = "id"
    )
    val facility: FacilityEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "assessmentId"
    )
    val answers: List<AssessmentAnswerEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "assessmentId"
    )
    val environmentalReadings: List<EnvironmentalReadingEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "assessmentId"
    )
    val chemicalRegisters: List<ChemicalRegisterEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "assessmentId"
    )
    val remedialActions: List<RemedialActionEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "assessmentId"
    )
    val evidencePhotos: List<EvidencePhotoEntity>
)

// ==========================================
// OTHER SUPPORTING ENTITIES
// ==========================================

@Entity(tableName = "commodities")
data class CommodityEntity(
    @PrimaryKey val code: String,
    val name: String,
    val description: String = ""
)

@Entity(tableName = "activity_areas")
data class ActivityAreaEntity(
    @PrimaryKey val code: String,
    val name: String,
    val isUnderground: Boolean = false
)

@Entity(tableName = "occupations")
data class OccupationEntity(
    @PrimaryKey val code: String,
    val title: String,
    val category: String = ""
)

@Entity(tableName = "pollutants")
data class PollutantEntity(
    @PrimaryKey val code: String,
    val name: String,
    val casNumber: String = "",
    val oelTwa: Double,
    val unit: String,
    val isCarcinogenOrSkin: Boolean = false,
    val targetOrgan: String = ""
)

@Entity(tableName = "environmental_readings")
data class EnvironmentalReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assessmentId: String,
    val shiftPeriod: String, // "Start of shift", "Middle of shift", "End of shift"
    val measurementTime: String = "", // e.g. "06:30", "11:45", "15:15"
    val locationDescription: String = "",
    val wetBulbTemp: Double? = null, // null = not taken; 0.0 = measured zero
    val dryBulbTemp: Double? = null,
    val globeTemp: Double? = null,
    val wbgtIndex: Double? = null,
    val relativeHumidity: Double? = null,
    val windSpeed: Double? = null,
    val generalConditions: String = "", // General environmental conditions
    val equipmentUsed: String = "WBGT Logger & Anemometer",
    val instrumentModel: String = "WBGT Logger model AZ 87786",
    val serialNumber: String = "SN-AZ87786-01",
    val comments: String = "",
    val windChill: Double? = null,
    val luxReading: Double? = null,
    val noiseDba: Double? = null,
    val wbgtMonitorModel: String = "WBGT Logger AZ 87786",
    val anemometerModel: String = "Kestrel Pocket Weather 4000",
    val readingTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chemical_registers")
data class ChemicalRegisterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assessmentId: String,
    val registerType: String,
    val chemicalName: String,
    val category: String,
    val sdsReturned: Boolean,
    val cleaningProcedureObtained: Boolean,
    val cleaningProcedureReturned: Boolean,
    val comments: String
)

@Entity(tableName = "remedial_actions")
data class RemedialActionEntity(
    @PrimaryKey val id: String,
    val actionReference: String,
    val assessmentId: String,
    val customerName: String,
    val mineLocation: String,
    val facilityName: String,
    val nonConformanceDescription: String,
    val requiredCorrectiveAction: String,
    val responsiblePerson: String,
    val priority: String,
    val targetDate: String,
    val status: String,
    val closureEvidenceNote: String = "",
    val verificationComments: String = "",
    val photoEvidenceUri: String = "",
    val closureDate: Long = 0L,
    val syncStatus: String = "SYNCED"
)

@Entity(tableName = "evidence_photos")
data class EvidencePhotoEntity(
    @PrimaryKey val id: String,
    val assessmentId: String,
    val findingReference: String = "",
    val photoUri: String,
    val caption: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val syncStatus: String = "SYNCED"
)

@Entity(tableName = "dmr_exposure_samples")
data class DmrExposureSampleEntity(
    @PrimaryKey val id: String,
    val assessmentId: String = "",
    val mineCode: String,
    val subMineCode: String = "",
    val reportingPeriod: String,
    val commodityCode: String,
    val sampleArea: String,
    val activityAreaCode: String,
    val hegNumber: String,
    val hegDescription: String,
    val occupationCode: String,
    val occupationTitle: String,
    val employeeInitialsSurname: String = "M. T. Khumalo",
    val employeeIdNumber: String = "8504125890082",
    val companyNumber: String = "EMP-94021",
    val pollutantCode: String,
    val sampleConcentrationTwa: Double,
    val analysisPercent: Double,
    val oelLimit: Double,
    val allocatedMedicalDose: Double,
    val calculated90thPercentile: Double,
    val classificationBand: String,
    val pollutantIndex: Double = 0.0,
    val aqiCombinedIndex: Double = 0.0,
    val reasonsForOverExposure: String = "",
    val correctiveMeasures: String = ""
)
