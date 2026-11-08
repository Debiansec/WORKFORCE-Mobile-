package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.WfhDatabase
import com.example.data.model.*
import com.example.data.repository.WfhRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class UserRole(val displayName: String, val canEditDraft: Boolean, val canApprove: Boolean, val canViewDraftReports: Boolean) {
    FIELD_ASSESSOR("Field Assessor", true, false, true),
    OCCUPATIONAL_HYGIENIST("Occupational Hygienist", false, true, true),
    MINE_MANAGEMENT("Mine Management / Client Rep", false, false, false),
    SYSTEM_ADMINISTRATOR("System Administrator", true, true, true)
}

data class DashboardSummary(
    val totalAssessments: Int = 0,
    val inProgressDrafts: Int = 0,
    val pendingReview: Int = 0,
    val approvedAssessments: Int = 0,
    val openRemedialActions: Int = 0,
    val overdueActions: Int = 0,
    val totalMines: Int = 0,
    val syncStatusText: String = "All Synced (Local & Cloud Ready)"
)

class WfhViewModel(application: Application) : AndroidViewModel(application) {
    private val database = WfhDatabase.getDatabase(application)
    private val repository = WfhRepository(database.wfhDao())

    // Current logged-in role & profile
    private val _currentUserRole = MutableStateFlow(UserRole.FIELD_ASSESSOR)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _currentUserName = MutableStateFlow("Mr R R M Taukobong")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    fun switchRole(role: UserRole) {
        _currentUserRole.value = role
        _currentUserName.value = when (role) {
            UserRole.FIELD_ASSESSOR -> "Mr R R M Taukobong (Assessor)"
            UserRole.OCCUPATIONAL_HYGIENIST -> "Mr N B Letlape (Site OH)"
            UserRole.MINE_MANAGEMENT -> "P. J. Steyn (Mine Manager)"
            UserRole.SYSTEM_ADMINISTRATOR -> "GeekForge Admin"
        }
    }

    // Master flows
    val assessments = repository.assessments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customers = repository.customers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val mineSites = repository.mineSites.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val facilities = repository.facilities.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val remedialActions = repository.remedialActions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val commodities = repository.commodities.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activityAreas = repository.activityAreas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val occupations = repository.occupations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pollutants = repository.pollutants.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val environmentalReadings = repository.allReadings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dmrExposureSamples = repository.dmrExposureSamples.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val evidencePhotos = repository.allEvidencePhotos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Operational Dashboard Summary
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        assessments,
        remedialActions,
        mineSites
    ) { assessList, actionsList, sitesList ->
        DashboardSummary(
            totalAssessments = assessList.size,
            inProgressDrafts = assessList.count { it.status == "DRAFT" },
            pendingReview = assessList.count { it.status == "SUBMITTED" },
            approvedAssessments = assessList.count { it.status == "APPROVED" },
            openRemedialActions = actionsList.count { it.status == "OPEN" || it.status == "IN_PROGRESS" },
            overdueActions = actionsList.count { it.status == "OPEN" },
            totalMines = sitesList.size,
            syncStatusText = "Online • SQLite Synced"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    // Assessment creation & answer updates
    fun createNewAssessment(
        customerId: String,
        siteId: String,
        facilityId: String,
        templateType: String,
        ablutionType: String = "Men’s change house",
        locationDescription: String = "",
        documentNumber: String = "",
        dateOfFieldVisit: String = "",
        previousVisitDate: String = "15 Oct 2026",
        previousRemedialSummary: String = "Replace missing soap dispensers in Basin #2; repair cubicle 1 lock latch.",
        specialInstructions: String = "Perform full verification according to MHSA regulations. Focus on ventilation, disinfection records, and water tap closures.",
        lat: Double,
        lng: Double,
        accuracy: Float,
        onCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            val id = repository.createAssessment(
                customerId = customerId,
                siteId = siteId,
                facilityId = facilityId,
                templateType = templateType,
                ablutionType = ablutionType,
                facilityLocationDescription = locationDescription,
                documentNumber = documentNumber,
                dateOfFieldVisit = dateOfFieldVisit,
                previousSiteVisitDate = previousVisitDate,
                previousVisitRemedialSummary = previousRemedialSummary,
                specialHygienistInstructions = specialInstructions,
                assessorName = _currentUserName.value,
                hygienistName = "Mr N B Letlape",
                latitude = lat,
                longitude = lng,
                accuracy = accuracy
            )
            onCreated(id)
        }
    }

    fun updateAssessment(assessment: AssessmentEntity) {
        viewModelScope.launch {
            repository.updateAssessment(assessment)
        }
    }

    fun getAssessmentAnswers(assessmentId: String): Flow<List<AssessmentAnswerEntity>> {
        return repository.getAnswersForAssessment(assessmentId)
    }

    fun updateAnswer(answer: AssessmentAnswerEntity) {
        viewModelScope.launch {
            repository.updateAnswer(answer)
        }
    }

    // Customers CRUD
    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // Mine Sites CRUD
    fun saveMineSite(site: MineSiteEntity) {
        viewModelScope.launch {
            repository.saveMineSite(site)
        }
    }

    fun deleteMineSite(site: MineSiteEntity) {
        viewModelScope.launch {
            repository.deleteMineSite(site)
        }
    }

    // Facilities CRUD
    fun saveFacility(facility: FacilityEntity) {
        viewModelScope.launch {
            repository.saveFacility(facility)
        }
    }

    fun deleteFacility(facility: FacilityEntity) {
        viewModelScope.launch {
            repository.deleteFacility(facility)
        }
    }

    fun getReadingsForAssessment(assessmentId: String): Flow<List<EnvironmentalReadingEntity>> {
        return repository.getReadingsForAssessment(assessmentId)
    }

    fun addReading(reading: EnvironmentalReadingEntity) {
        viewModelScope.launch {
            repository.addEnvironmentalReading(reading)
        }
    }

    fun saveReading(reading: EnvironmentalReadingEntity) {
        viewModelScope.launch {
            if (reading.id == 0L) {
                repository.addEnvironmentalReading(reading)
            } else {
                repository.updateEnvironmentalReading(reading)
            }
        }
    }

    fun deleteReading(reading: EnvironmentalReadingEntity) {
        viewModelScope.launch {
            repository.deleteEnvironmentalReading(reading)
        }
    }

    fun getChemicalsForAssessment(assessmentId: String): Flow<List<ChemicalRegisterEntity>> {
        return repository.getChemicalsForAssessment(assessmentId)
    }

    fun addRemedialAction(action: RemedialActionEntity) {
        viewModelScope.launch {
            repository.addRemedialAction(action)
        }
    }

    fun updateRemedialActionStatus(action: RemedialActionEntity, newStatus: String, notes: String = "") {
        viewModelScope.launch {
            repository.updateRemedialAction(
                action.copy(
                    status = newStatus,
                    verificationComments = if (notes.isNotEmpty()) notes else action.verificationComments,
                    closureDate = if (newStatus == "CLOSED") System.currentTimeMillis() else action.closureDate
                )
            )
        }
    }

    fun addEvidencePhoto(photo: EvidencePhotoEntity) {
        viewModelScope.launch {
            repository.addEvidencePhoto(photo)
        }
    }

    fun addDmrSample(sample: DmrExposureSampleEntity) {
        viewModelScope.launch {
            repository.addDmrExposureSample(sample)
        }
    }

    fun saveDmrSample(sample: DmrExposureSampleEntity) {
        viewModelScope.launch {
            repository.addDmrExposureSample(sample) // REPLACE strategy handles both insert and update
        }
    }

    fun deleteDmrSample(sample: DmrExposureSampleEntity) {
        viewModelScope.launch {
            repository.deleteDmrExposureSample(sample)
        }
    }

    fun signAndSubmit(
        assessmentId: String,
        assessorSig: String,
        hygienistSig: String = "",
        isApprove: Boolean = false,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.signAndSubmitAssessment(assessmentId, assessorSig, hygienistSig, isApprove)
            onComplete()
        }
    }
}
