package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WfhDao {

    // ==========================================
    // 1. CUSTOMERS
    // ==========================================
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Transaction
    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun getCustomerWithMines(customerId: String): Flow<CustomerWithMines?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: String)

    // ==========================================
    // 2. MINE SITES & LOCATIONS
    // ==========================================
    @Query("SELECT * FROM mine_sites WHERE customerId = :customerId ORDER BY name ASC")
    fun getMineSitesByCustomer(customerId: String): Flow<List<MineSiteEntity>>

    @Query("SELECT * FROM mine_sites ORDER BY name ASC")
    fun getAllMineSites(): Flow<List<MineSiteEntity>>

    @Query("SELECT * FROM mine_sites WHERE id = :siteId LIMIT 1")
    suspend fun getMineSiteById(siteId: String): MineSiteEntity?

    @Transaction
    @Query("SELECT * FROM mine_sites WHERE id = :siteId")
    fun getMineSiteWithFacilities(siteId: String): Flow<MineSiteWithFacilities?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMineSites(sites: List<MineSiteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMineSite(site: MineSiteEntity)

    @Update
    suspend fun updateMineSite(site: MineSiteEntity)

    @Delete
    suspend fun deleteMineSite(site: MineSiteEntity)

    @Query("DELETE FROM mine_sites WHERE id = :id")
    suspend fun deleteMineSiteById(id: String)

    // ==========================================
    // 3. FACILITIES
    // ==========================================
    @Query("SELECT * FROM facilities WHERE siteId = :siteId ORDER BY name ASC")
    fun getFacilitiesBySite(siteId: String): Flow<List<FacilityEntity>>

    @Query("SELECT * FROM facilities ORDER BY name ASC")
    fun getAllFacilities(): Flow<List<FacilityEntity>>

    @Query("SELECT * FROM facilities WHERE id = :id LIMIT 1")
    suspend fun getFacilityById(id: String): FacilityEntity?

    @Transaction
    @Query("SELECT * FROM facilities WHERE id = :facilityId")
    fun getFacilityWithAssessments(facilityId: String): Flow<FacilityWithAssessments?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacilities(facilities: List<FacilityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacility(facility: FacilityEntity)

    @Update
    suspend fun updateFacility(facility: FacilityEntity)

    @Delete
    suspend fun deleteFacility(facility: FacilityEntity)

    @Query("DELETE FROM facilities WHERE id = :id")
    suspend fun deleteFacilityById(id: String)

    // ==========================================
    // 4. ASSESSMENTS
    // ==========================================
    @Query("SELECT * FROM assessments ORDER BY inspectionDate DESC")
    fun getAllAssessments(): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM assessments WHERE id = :id LIMIT 1")
    suspend fun getAssessmentById(id: String): AssessmentEntity?

    @Query("SELECT * FROM assessments WHERE id = :id LIMIT 1")
    fun getAssessmentByIdFlow(id: String): Flow<AssessmentEntity?>

    @Query("SELECT * FROM assessments WHERE facilityId = :facilityId ORDER BY inspectionDate DESC")
    fun getAssessmentsByFacility(facilityId: String): Flow<List<AssessmentEntity>>

    @Transaction
    @Query("SELECT * FROM assessments WHERE id = :assessmentId")
    fun getFullAssessmentHierarchy(assessmentId: String): Flow<FullAssessmentHierarchy?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: AssessmentEntity)

    @Update
    suspend fun updateAssessment(assessment: AssessmentEntity)

    @Delete
    suspend fun deleteAssessment(assessment: AssessmentEntity)

    // ==========================================
    // 5. 50-POINT ABLUTION ASSESSMENT ANSWERS
    // ==========================================
    @Query("SELECT * FROM assessment_answers WHERE assessmentId = :assessmentId ORDER BY questionNumber ASC")
    fun getAnswersForAssessment(assessmentId: String): Flow<List<AssessmentAnswerEntity>>

    @Query("SELECT * FROM assessment_answers WHERE assessmentId = :assessmentId AND sectionCode = :sectionCode ORDER BY questionNumber ASC")
    fun getAnswersBySection(assessmentId: String, sectionCode: String): Flow<List<AssessmentAnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<AssessmentAnswerEntity>)

    @Update
    suspend fun updateAnswer(answer: AssessmentAnswerEntity)

    @Query("DELETE FROM assessment_answers WHERE assessmentId = :assessmentId")
    suspend fun deleteAnswersByAssessment(assessmentId: String)

    // ==========================================
    // 6. SUPPORTING REGISTERS & ENVIRONMENTAL
    // ==========================================
    @Query("SELECT * FROM environmental_readings WHERE assessmentId = :assessmentId ORDER BY id ASC")
    fun getReadingsForAssessment(assessmentId: String): Flow<List<EnvironmentalReadingEntity>>

    @Query("SELECT * FROM environmental_readings ORDER BY readingTimestamp DESC")
    fun getAllReadings(): Flow<List<EnvironmentalReadingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnvironmentalReading(reading: EnvironmentalReadingEntity)

    @Update
    suspend fun updateEnvironmentalReading(reading: EnvironmentalReadingEntity)

    @Delete
    suspend fun deleteEnvironmentalReading(reading: EnvironmentalReadingEntity)

    @Query("DELETE FROM environmental_readings WHERE id = :id")
    suspend fun deleteEnvironmentalReadingById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnvironmentalReadings(readings: List<EnvironmentalReadingEntity>)

    @Query("SELECT * FROM chemical_registers WHERE assessmentId = :assessmentId ORDER BY id ASC")
    fun getChemicalsForAssessment(assessmentId: String): Flow<List<ChemicalRegisterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChemicals(chemicals: List<ChemicalRegisterEntity>)

    @Query("SELECT * FROM remedial_actions ORDER BY targetDate ASC")
    fun getAllRemedialActions(): Flow<List<RemedialActionEntity>>

    @Query("SELECT * FROM remedial_actions WHERE assessmentId = :assessmentId")
    fun getRemedialActionsByAssessment(assessmentId: String): Flow<List<RemedialActionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemedialAction(action: RemedialActionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemedialActions(actions: List<RemedialActionEntity>)

    @Update
    suspend fun updateRemedialAction(action: RemedialActionEntity)

    @Delete
    suspend fun deleteRemedialAction(action: RemedialActionEntity)

    @Query("SELECT * FROM evidence_photos WHERE assessmentId = :assessmentId ORDER BY timestamp DESC")
    fun getEvidencePhotosForAssessment(assessmentId: String): Flow<List<EvidencePhotoEntity>>

    @Query("SELECT * FROM evidence_photos ORDER BY timestamp DESC")
    fun getAllEvidencePhotos(): Flow<List<EvidencePhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidencePhoto(photo: EvidencePhotoEntity)

    // Statutory Tables
    @Query("SELECT * FROM commodities ORDER BY code ASC")
    fun getAllCommodities(): Flow<List<CommodityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommodities(items: List<CommodityEntity>)

    @Query("SELECT * FROM activity_areas ORDER BY code ASC")
    fun getAllActivityAreas(): Flow<List<ActivityAreaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityAreas(items: List<ActivityAreaEntity>)

    @Query("SELECT * FROM occupations ORDER BY title ASC")
    fun getAllOccupations(): Flow<List<OccupationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccupations(items: List<OccupationEntity>)

    @Query("SELECT * FROM pollutants ORDER BY name ASC")
    fun getAllPollutants(): Flow<List<PollutantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPollutants(items: List<PollutantEntity>)

    @Query("SELECT * FROM dmr_exposure_samples ORDER BY reportingPeriod DESC")
    fun getAllDmrExposureSamples(): Flow<List<DmrExposureSampleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDmrExposureSamples(samples: List<DmrExposureSampleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDmrExposureSample(sample: DmrExposureSampleEntity)

    @Update
    suspend fun updateDmrExposureSample(sample: DmrExposureSampleEntity)

    @Delete
    suspend fun deleteDmrExposureSample(sample: DmrExposureSampleEntity)

    @Query("DELETE FROM dmr_exposure_samples WHERE id = :id")
    suspend fun deleteDmrExposureSampleById(id: String)
}
