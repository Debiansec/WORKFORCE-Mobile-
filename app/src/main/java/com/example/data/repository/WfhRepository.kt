package com.example.data.repository

import com.example.data.dao.WfhDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class WfhRepository(private val dao: WfhDao) {

    // Commodities & Activities
    val commodities: Flow<List<CommodityEntity>> = dao.getAllCommodities()
    val activityAreas: Flow<List<ActivityAreaEntity>> = dao.getAllActivityAreas()
    val occupations: Flow<List<OccupationEntity>> = dao.getAllOccupations()
    val pollutants: Flow<List<PollutantEntity>> = dao.getAllPollutants()

    // Customers & Sites
    val customers: Flow<List<CustomerEntity>> = dao.getAllCustomers()
    suspend fun getCustomerById(id: String) = dao.getCustomerById(id)
    suspend fun saveCustomer(customer: CustomerEntity) = dao.insertCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = dao.deleteCustomer(customer)

    val mineSites: Flow<List<MineSiteEntity>> = dao.getAllMineSites()
    fun getMineSitesByCustomer(customerId: String) = dao.getMineSitesByCustomer(customerId)
    suspend fun saveMineSite(site: MineSiteEntity) = dao.insertMineSite(site)
    suspend fun deleteMineSite(site: MineSiteEntity) = dao.deleteMineSite(site)

    val facilities: Flow<List<FacilityEntity>> = dao.getAllFacilities()
    fun getFacilitiesBySite(siteId: String) = dao.getFacilitiesBySite(siteId)
    suspend fun saveFacility(facility: FacilityEntity) = dao.insertFacility(facility)
    suspend fun deleteFacility(facility: FacilityEntity) = dao.deleteFacility(facility)

    // Assessments
    val assessments: Flow<List<AssessmentEntity>> = dao.getAllAssessments()
    suspend fun getAssessmentById(id: String) = dao.getAssessmentById(id)
    fun getAssessmentByIdFlow(id: String) = dao.getAssessmentByIdFlow(id)

    suspend fun createAssessment(
        customerId: String,
        siteId: String,
        facilityId: String,
        templateType: String,
        ablutionType: String = "Men’s change house",
        facilityLocationDescription: String = "",
        documentNumber: String = "",
        dateOfFieldVisit: String = "",
        previousSiteVisitDate: String = "15 Oct 2026",
        previousVisitRemedialSummary: String = "Replace missing soap dispensers in Basin #2; repair cubicle 1 lock latch.",
        specialHygienistInstructions: String = "Perform full verification according to MHSA regulations. Focus on ventilation, disinfection records, and water tap closures.",
        assessorName: String = "Mr R R M Taukobong",
        hygienistName: String = "Mr N B Letlape",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        accuracy: Float = 0f
    ): String {
        val newId = "ASSESS-${System.currentTimeMillis() % 1000000}"
        val docNum = documentNumber.ifEmpty { "WFH-AWI-${System.currentTimeMillis() % 10000}" }

        val customer = dao.getCustomerById(customerId)
        val site = dao.getMineSiteById(siteId)
        val facility = dao.getFacilityById(facilityId)

        val checklistTitle = when (ablutionType) {
            "Men’s change house" -> "Monthly ablution facilities checklist Male"
            "Lady’s change house" -> "Monthly ablution facilities checklist Female"
            "Lady’s office toilet" -> "Monthly toilet and basin checklist Male / Female"
            "Men’s office toilet" -> "Monthly toilet and basin checklist Male / Female"
            else -> "Monthly ablution facilities checklist Male"
        }

        val locationDesc = facilityLocationDescription.ifEmpty {
            "${facility?.name ?: "Ablution Facility"}, ${site?.name ?: ""}"
        }

        val entity = AssessmentEntity(
            id = newId,
            documentNumber = docNum,
            customerId = customerId,
            siteId = siteId,
            facilityId = facilityId,
            templateType = templateType,
            ablutionType = ablutionType,
            checklistTitle = checklistTitle,
            facilityLocationDescription = locationDesc,
            inspectionDate = System.currentTimeMillis(),
            dateOfFieldVisit = dateOfFieldVisit.ifEmpty {
                val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                sdf.format(java.util.Date())
            },
            previousSiteVisitDate = previousSiteVisitDate,
            clientName = customer?.name ?: "Mining Client",
            mineNameAndLocation = "${site?.name ?: "Mine Site"} (${customer?.province ?: "South Africa"})",
            dmeMineCode = customer?.dmeMineCode ?: "10192",
            mineManagerName = customer?.mineManager ?: "Appointed Mine Manager",
            mineManagerContact = "${customer?.managerEmail ?: ""} / ${customer?.managerPhone ?: ""}",
            hygienistName = hygienistName,
            hygienistRole = "Appointed Occupational Hygienist",
            hygienistContact = "${customer?.hygienistEmail ?: "ChiefOH@workforcehealthsa.co.za"} / ${customer?.hygienistPhone ?: "+27 (0)83 456 2673"}",
            medicalPractitionerName = customer?.medicalPractitioner ?: "Dr K O Sebopa",
            medicalPractitionerContact = "${customer?.medicalEmail ?: ""} / ${customer?.medicalPhone ?: ""}",
            assessorName = assessorName,
            assessorRole = "Occupational Hygiene Assistant",
            assessmentType = "Monthly Statutory Hygiene Inspection",
            status = "DRAFT",
            latitude = latitude,
            longitude = longitude,
            gpsAccuracyMeters = accuracy,
            previousVisitRemedialSummary = previousVisitRemedialSummary,
            specialHygienistInstructions = specialHygienistInstructions,
            syncStatus = "OFFLINE_DRAFT"
        )
        dao.insertAssessment(entity)

        // Pre-fill tailored checklist items for selected Ablution type
        val tailoredQuestions = com.example.data.database.WfhDatabase.generateAnswersForAblutionType(newId, ablutionType)
        dao.insertAnswers(tailoredQuestions)

        // Pre-populate 3 shift environmental condition worksheets (Start of shift, Middle of shift, End of shift)
        val initialReadings = com.example.data.database.WfhDatabase.generateInitialShiftReadings(newId, locationDesc)
        dao.insertEnvironmentalReadings(initialReadings)

        return newId
    }

    suspend fun updateAssessment(assessment: AssessmentEntity) {
        dao.updateAssessment(assessment)
    }

    // Answers
    fun getAnswersForAssessment(assessmentId: String): Flow<List<AssessmentAnswerEntity>> =
        dao.getAnswersForAssessment(assessmentId)

    suspend fun updateAnswer(answer: AssessmentAnswerEntity) {
        dao.updateAnswer(answer)
    }

    // Environmental Readings
    fun getReadingsForAssessment(assessmentId: String): Flow<List<EnvironmentalReadingEntity>> =
        dao.getReadingsForAssessment(assessmentId)

    val allReadings: Flow<List<EnvironmentalReadingEntity>> = dao.getAllReadings()

    suspend fun addEnvironmentalReading(reading: EnvironmentalReadingEntity) {
        dao.insertEnvironmentalReading(reading)
    }

    suspend fun updateEnvironmentalReading(reading: EnvironmentalReadingEntity) {
        dao.updateEnvironmentalReading(reading)
    }

    suspend fun deleteEnvironmentalReading(reading: EnvironmentalReadingEntity) {
        dao.deleteEnvironmentalReading(reading)
    }

    // Chemical Registers
    fun getChemicalsForAssessment(assessmentId: String): Flow<List<ChemicalRegisterEntity>> =
        dao.getChemicalsForAssessment(assessmentId)

    suspend fun insertChemicals(chemicals: List<ChemicalRegisterEntity>) {
        dao.insertChemicals(chemicals)
    }

    // Remedial Actions
    val remedialActions: Flow<List<RemedialActionEntity>> = dao.getAllRemedialActions()
    fun getRemedialActionsByAssessment(assessmentId: String) = dao.getRemedialActionsByAssessment(assessmentId)

    suspend fun addRemedialAction(action: RemedialActionEntity) {
        dao.insertRemedialAction(action)
    }

    suspend fun updateRemedialAction(action: RemedialActionEntity) {
        dao.updateRemedialAction(action)
    }

    suspend fun deleteRemedialAction(action: RemedialActionEntity) {
        dao.deleteRemedialAction(action)
    }

    // Photographic Evidence
    fun getEvidencePhotos(assessmentId: String): Flow<List<EvidencePhotoEntity>> =
        dao.getEvidencePhotosForAssessment(assessmentId)

    val allEvidencePhotos: Flow<List<EvidencePhotoEntity>> = dao.getAllEvidencePhotos()

    suspend fun addEvidencePhoto(photo: EvidencePhotoEntity) {
        dao.insertEvidencePhoto(photo)
    }

    // DMR Personal Exposure Samples & Statistics
    val dmrExposureSamples: Flow<List<DmrExposureSampleEntity>> = dao.getAllDmrExposureSamples()

    suspend fun addDmrExposureSample(sample: DmrExposureSampleEntity) {
        dao.insertDmrExposureSample(sample)
    }

    suspend fun updateDmrExposureSample(sample: DmrExposureSampleEntity) {
        dao.updateDmrExposureSample(sample)
    }

    suspend fun deleteDmrExposureSample(sample: DmrExposureSampleEntity) {
        dao.deleteDmrExposureSample(sample)
    }

    // Signatures & Legal Record Immutability (Section 10)
    suspend fun signAndSubmitAssessment(
        assessmentId: String,
        assessorSignature: String,
        hygienistSignature: String = "",
        isApprove: Boolean = false
    ) {
        val current = dao.getAssessmentById(assessmentId) ?: return
        val timestamp = System.currentTimeMillis()
        val payloadContent = "${current.id}|${current.customerId}|${current.inspectionDate}|$assessorSignature|$hygienistSignature|$timestamp"
        val hash = sha256(payloadContent)

        val updated = current.copy(
            status = if (isApprove) "APPROVED" else "SUBMITTED",
            assessorSignatureSvg = if (assessorSignature.isNotEmpty()) assessorSignature else current.assessorSignatureSvg,
            hygienistSignatureSvg = if (hygienistSignature.isNotEmpty()) hygienistSignature else current.hygienistSignatureSvg,
            signatureTimestamp = timestamp,
            payloadHash = hash,
            lastModifiedTimestamp = timestamp,
            syncStatus = "SYNCED"
        )
        dao.updateAssessment(updated)
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
