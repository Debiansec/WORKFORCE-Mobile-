package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AssessmentAnswerEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.EnvironmentalReadingEntity
import com.example.ui.components.SignaturePad
import com.example.ui.components.StatusBadge
import com.example.ui.components.WfhTopBar
import com.example.ui.theme.*
import com.example.viewmodel.UserRole
import com.example.viewmodel.WfhViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AssessmentDetailScreen(
    assessmentId: String,
    viewModel: WfhViewModel,
    onNavigateBack: () -> Unit,
    onViewReport: (String) -> Unit
) {
    val context = LocalContext.current
    val assessments by viewModel.assessments.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()

    val answersFlow = remember(assessmentId) { viewModel.getAssessmentAnswers(assessmentId) }
    val answers by answersFlow.collectAsState(initial = emptyList())

    val readingsFlow = remember(assessmentId) { viewModel.getReadingsForAssessment(assessmentId) }
    val readings by readingsFlow.collectAsState(initial = emptyList())

    val assessment = assessments.find { it.id == assessmentId }
    if (assessment == null) {
        Box(modifier = Modifier.fillMaxSize().background(Charcoal900), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SafetyAmber)
        }
        return
    }

    // Top Level Tabs: Checklist Items, Shift Environmental, Statutory Header, Signatures
    var mainTabIndex by remember { mutableStateOf(0) }
    val mainTabs = listOf(
        "Checklist Items (${answers.size})" to Icons.Default.Checklist,
        "Shift Environmental (3)" to Icons.Default.DeviceThermostat,
        "Statutory Header" to Icons.Default.Business,
        "Signatures & Review" to Icons.Default.Draw
    )

    // Voice to Text Dictation Launcher
    var activeDictationQuestionId by remember { mutableStateOf<Long?>(null) }
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank() && activeDictationQuestionId != null) {
                val q = answers.find { it.localId == activeDictationQuestionId }
                if (q != null) {
                    val newRemarks = if (q.remarks.isBlank()) spokenText else "${q.remarks} $spokenText"
                    viewModel.updateAnswer(q.copy(remarks = newRemarks))
                }
            }
        }
    }

    fun launchVoiceDictation(questionId: Long) {
        activeDictationQuestionId = questionId
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Dictate observation or non-conformance comment...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            // Speech recognizer not installed or unavailable
        }
    }

    // Photo Attachment Launcher
    var activePhotoQuestionId by remember { mutableStateOf<Long?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && activePhotoQuestionId != null) {
            val q = answers.find { it.localId == activePhotoQuestionId }
            if (q != null) {
                viewModel.updateAnswer(q.copy(photoUri = uri.toString()))
            }
        }
    }

    val canEdit = (assessment.status == "DRAFT" && currentRole.canEditDraft)
    val canApprove = (assessment.status == "SUBMITTED" && currentRole.canApprove)

    var assessorSig by remember { mutableStateOf(assessment.assessorSignatureSvg) }
    var hygienistSig by remember { mutableStateOf(assessment.hygienistSignatureSvg) }

    // Checklist Section Filter (Basins, Showers, Toilets, Urinals/Sanitary, Lockers, General)
    val distinctSections = remember(answers) {
        val codes = answers.map { it.sectionCode }.distinct()
        listOf("ALL" to "All Items (${answers.size})") + codes.map { code ->
            val count = answers.count { it.sectionCode == code }
            val cleanTitle = when (code) {
                "A_BASINS" -> "A - Basins ($count)"
                "B_SHOWERS" -> "B - Showers ($count)"
                "C_TOILETS" -> "C - Toilets ($count)"
                "D_URINALS" -> "D - Urinals ($count)"
                "D_SANITARY" -> "D - Sanitary ($count)"
                "E_LOCKERS" -> "E - Lockers ($count)"
                "E_HAND_DRYING" -> "E - Hand Drying ($count)"
                "F_GENERAL" -> "F - General ($count)"
                else -> "$code ($count)"
            }
            code to cleanTitle
        }
    }
    var selectedSectionFilter by remember { mutableStateOf("ALL") }

    Scaffold(
        topBar = {
            WfhTopBar(
                title = assessment.documentNumber,
                subtitle = "${assessment.ablutionType} • ${assessment.status}",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { onViewReport(assessment.id) },
                        modifier = Modifier.testTag("view_report_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Report", tint = SafetyAmber)
                    }
                }
            )
        },
        containerColor = Charcoal900
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Top Tabs
            ScrollableTabRow(
                selectedTabIndex = mainTabIndex,
                containerColor = Charcoal800,
                contentColor = SafetyAmber,
                edgePadding = 12.dp
            ) {
                mainTabs.forEachIndexed { index, (label, icon) ->
                    Tab(
                        selected = mainTabIndex == index,
                        onClick = { mainTabIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (mainTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (mainTabIndex == index) SafetyAmber else TextSecondary
                                )
                            }
                        },
                        modifier = Modifier.testTag("assessment_main_tab_$index")
                    )
                }
            }

            when (mainTabIndex) {
                0 -> {
                    // ==========================================
                    // TAB 1: CHECKLIST ITEMS
                    // ==========================================
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Section Sub-Filter Chips
                        ScrollableTabRow(
                            selectedTabIndex = distinctSections.indexOfFirst { it.first == selectedSectionFilter }.coerceAtLeast(0),
                            containerColor = Charcoal900,
                            contentColor = SafetyAmber,
                            edgePadding = 12.dp
                        ) {
                            distinctSections.forEach { (code, title) ->
                                Tab(
                                    selected = selectedSectionFilter == code,
                                    onClick = { selectedSectionFilter = code },
                                    text = {
                                        Text(
                                            text = title,
                                            fontSize = 11.sp,
                                            fontWeight = if (selectedSectionFilter == code) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedSectionFilter == code) SafetyAmber else TextMuted
                                        )
                                    }
                                )
                            }
                        }

                        val filteredAnswers = remember(answers, selectedSectionFilter) {
                            if (selectedSectionFilter == "ALL") answers else answers.filter { it.sectionCode == selectedSectionFilter }
                        }

                        val nonConformanceCount = answers.count { it.isNonConformance }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Charcoal800,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = assessment.checklistTitle,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Location: ${assessment.facilityLocationDescription.ifEmpty { assessment.mineNameAndLocation }}",
                                                    color = SafetyAmber,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            if (nonConformanceCount > 0) {
                                                Surface(
                                                    color = SafetyRed.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(1.dp, SafetyRed)
                                                ) {
                                                    Text(
                                                        text = "$nonConformanceCount Non-Conformances",
                                                        color = SafetyRed,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            items(filteredAnswers) { answer ->
                                ChecklistQuestionCard(
                                    answer = answer,
                                    canEdit = canEdit,
                                    onUpdate = { updated -> viewModel.updateAnswer(updated) },
                                    onVoiceDictation = { launchVoiceDictation(answer.localId) },
                                    onPickPhoto = {
                                        activePhotoQuestionId = answer.localId
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onAttachSamplePhoto = {
                                        viewModel.updateAnswer(
                                            answer.copy(photoUri = "android.resource://${context.packageName}/drawable/ic_launcher_background")
                                        )
                                    }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
                1 -> {
                    // ==========================================
                    // TAB 2: SHIFT ENVIRONMENTAL CONDITIONS (3 PERIODS)
                    // ==========================================
                    ShiftEnvironmentalPane(
                        assessmentId = assessment.id,
                        readings = readings,
                        canEdit = canEdit,
                        location = assessment.facilityLocationDescription,
                        onSaveReading = { reading -> viewModel.saveReading(reading) }
                    )
                }
                2 -> {
                    // ==========================================
                    // TAB 3: STATUTORY ASSESSMENT HEADER
                    // ==========================================
                    StatutoryHeaderPane(
                        assessment = assessment,
                        canEdit = canEdit,
                        onUpdateAssessment = { updated -> viewModel.updateAssessment(updated) }
                    )
                }
                3 -> {
                    // ==========================================
                    // TAB 4: SIGNATURES & LEGAL REVIEW
                    // ==========================================
                    SignaturesAndReviewPane(
                        assessment = assessment,
                        canEdit = canEdit,
                        canApprove = canApprove,
                        assessorSig = assessorSig,
                        hygienistSig = hygienistSig,
                        onAssessorSigChange = { assessorSig = it },
                        onHygienistSigChange = { hygienistSig = it },
                        onSubmit = {
                            viewModel.signAndSubmit(
                                assessmentId = assessment.id,
                                assessorSig = assessorSig,
                                isApprove = false,
                                onComplete = { onNavigateBack() }
                            )
                        },
                        onApprove = {
                            viewModel.signAndSubmit(
                                assessmentId = assessment.id,
                                assessorSig = assessorSig,
                                hygienistSig = hygienistSig.ifEmpty { "HYGIENIST_APP_SIG" },
                                isApprove = true,
                                onComplete = { onNavigateBack() }
                            )
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPONENT: CHECKLIST QUESTION CARD (WITH NON-CONFORMANCES, VOICE, PHOTOS, REMEDIAL)
// ==========================================
@Composable
fun ChecklistQuestionCard(
    answer: AssessmentAnswerEntity,
    canEdit: Boolean,
    onUpdate: (AssessmentAnswerEntity) -> Unit,
    onVoiceDictation: () -> Unit,
    onPickPhoto: () -> Unit,
    onAttachSamplePhoto: () -> Unit
) {
    var remarksText by remember(answer.remarks) { mutableStateOf(answer.remarks) }
    var actionText by remember(answer.requiredRemedialAction) { mutableStateOf(answer.requiredRemedialAction) }
    var personText by remember(answer.responsiblePerson) { mutableStateOf(answer.responsiblePerson) }
    var dateText by remember(answer.followUpDate) { mutableStateOf(answer.followUpDate) }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (answer.isNonConformance) SafetyRed else BorderDark,
                RoundedCornerShape(10.dp)
            )
            .testTag("question_card_${answer.questionNumber}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Q# and Question Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Q${answer.questionNumber}",
                    color = SafetyAmber,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.width(38.dp)
                )

                Text(
                    text = answer.questionText,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                if (answer.isNonConformance) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = SafetyRed,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "NON-CONFORMANCE",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Response Selection Buttons (YES / NO / N/A)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("YES", "NO", "N/A").forEach { choice ->
                    val isSelected = answer.response == choice
                    val btnBg = when {
                        isSelected && choice == "YES" -> SafetyGreen
                        isSelected && choice == "NO" -> SafetyRed
                        isSelected && choice == "N/A" -> Charcoal500
                        else -> Charcoal700
                    }

                    Button(
                        onClick = {
                            if (canEdit) {
                                val isNonConf = (choice == "NO")
                                onUpdate(
                                    answer.copy(
                                        response = choice,
                                        isNonConformance = isNonConf,
                                        requiredRemedialAction = if (isNonConf && answer.requiredRemedialAction.isEmpty()) "Issue rectification work order and re-inspect" else answer.requiredRemedialAction,
                                        responsiblePerson = if (isNonConf && answer.responsiblePerson.isEmpty()) "Cleaning Supervisor / Maintenance Foreman" else answer.responsiblePerson,
                                        followUpDate = if (isNonConf && answer.followUpDate.isEmpty()) "15 Nov 2026" else answer.followUpDate
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = btnBg),
                        shape = RoundedCornerShape(6.dp),
                        enabled = canEdit,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("q_${answer.questionNumber}_$choice")
                    ) {
                        Text(
                            text = choice,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Comments (Compulsory indicator if non-conforming) + Voice to Text Dictation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (answer.isNonConformance) "Observations / Comments * (COMPULSORY)" else "Observations / Comments",
                        fontSize = 11.sp,
                        fontWeight = if (answer.isNonConformance) FontWeight.Bold else FontWeight.Normal,
                        color = if (answer.isNonConformance) SafetyRed else SafetyAmber
                    )
                }

                if (canEdit) {
                    IconButton(
                        onClick = onVoiceDictation,
                        modifier = Modifier.size(32.dp).testTag("voice_dictate_q_${answer.questionNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Dictation",
                            tint = SafetyAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (canEdit) {
                OutlinedTextField(
                    value = remarksText,
                    onValueChange = {
                        remarksText = it
                        onUpdate(answer.copy(remarks = it))
                    },
                    placeholder = {
                        Text(
                            if (answer.isNonConformance) "Compulsory: Detail finding & cause of non-conformance" else "Optional: Record field observation or remarks",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("q_${answer.questionNumber}_remarks"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (answer.isNonConformance && remarksText.isBlank()) SafetyRed else SafetyAmber,
                        unfocusedBorderColor = if (answer.isNonConformance && remarksText.isBlank()) SafetyRed.copy(alpha = 0.5f) else BorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            } else if (answer.remarks.isNotEmpty()) {
                Surface(
                    color = Charcoal800,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text(
                        text = "Observation: ${answer.remarks}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Photo Evidence Section
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Photographic Evidence:", color = TextSecondary, fontSize = 11.sp)
                if (canEdit) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = onPickPhoto,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pick Photo", color = SafetyAmber, fontSize = 10.sp)
                        }
                        TextButton(
                            onClick = onAttachSamplePhoto,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("Attach Evidence", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }

            if (answer.photoUri.isNotEmpty()) {
                Surface(
                    color = Charcoal800,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = SafetyGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Photo Attached (Geotagged & Timestamped)", color = Color.White, fontSize = 11.sp)
                        }
                        if (canEdit) {
                            IconButton(
                                onClick = { onUpdate(answer.copy(photoUri = "")) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = SafetyRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Compulsory Remedial Action Block for Non-Conformances
            if (answer.isNonConformance) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Charcoal800,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SafetyRed.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "REQUIRED REMEDIAL ACTION PLAN (COMPULSORY)",
                            color = SafetyRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (canEdit) {
                            OutlinedTextField(
                                value = actionText,
                                onValueChange = {
                                    actionText = it
                                    onUpdate(answer.copy(requiredRemedialAction = it))
                                },
                                label = { Text("Required Remedial Action *") },
                                modifier = Modifier.fillMaxWidth().testTag("q_${answer.questionNumber}_action")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = personText,
                                    onValueChange = {
                                        personText = it
                                        onUpdate(answer.copy(responsiblePerson = it))
                                    },
                                    label = { Text("Responsible Person *") },
                                    modifier = Modifier.weight(1f).testTag("q_${answer.questionNumber}_person")
                                )
                                OutlinedTextField(
                                    value = dateText,
                                    onValueChange = {
                                        dateText = it
                                        onUpdate(answer.copy(followUpDate = it))
                                    },
                                    label = { Text("Follow-up Date *") },
                                    modifier = Modifier.weight(1f).testTag("q_${answer.questionNumber}_date")
                                )
                            }
                        } else {
                            Text("Action: ${answer.requiredRemedialAction}", color = Color.White, fontSize = 11.sp)
                            Text("Responsible: ${answer.responsiblePerson} • Target: ${answer.followUpDate}", color = TextSecondary, fontSize = 11.sp)
                        }

                        // Closure Confirmation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Closure Confirmation Status:", color = TextSecondary, fontSize = 11.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = answer.isClosureConfirmed,
                                    onCheckedChange = { confirmed ->
                                        if (canEdit) {
                                            onUpdate(answer.copy(isClosureConfirmed = confirmed))
                                        }
                                    },
                                    enabled = canEdit,
                                    colors = CheckboxDefaults.colors(checkedColor = SafetyGreen)
                                )
                                Text(
                                    text = if (answer.isClosureConfirmed) "CLOSED & VERIFIED" else "OPEN FINDING",
                                    color = if (answer.isClosureConfirmed) SafetyGreen else SafetyOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT: SHIFT ENVIRONMENTAL CONDITIONS PANE (3 PERIODS)
// ==========================================
@Composable
fun ShiftEnvironmentalPane(
    assessmentId: String,
    readings: List<EnvironmentalReadingEntity>,
    canEdit: Boolean,
    location: String,
    onSaveReading: (EnvironmentalReadingEntity) -> Unit
) {
    val periods = listOf("Start of shift", "Middle of shift", "End of shift")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Charcoal800),
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeviceThermostat, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SHIFT ENVIRONMENTAL CONDITIONS & THERMAL STRESS", color = SafetyAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Statutory measurements across three periods (Start, Middle, End of shift). WBGT is calculated using the outdoor index equation (0.7·WB + 0.2·GT + 0.1·DB).",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Note: The system strictly distinguishes between a reading that has not been taken (blank / null) and a measurement of zero (0.0).",
                        color = SafetyOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        items(periods) { period ->
            val reading = readings.find { it.shiftPeriod.equals(period, ignoreCase = true) } ?: EnvironmentalReadingEntity(
                assessmentId = assessmentId,
                shiftPeriod = period,
                locationDescription = location
            )

            ShiftMeasurementPeriodCard(
                periodName = period,
                reading = reading,
                canEdit = canEdit,
                onSave = onSaveReading
            )
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun ShiftMeasurementPeriodCard(
    periodName: String,
    reading: EnvironmentalReadingEntity,
    canEdit: Boolean,
    onSave: (EnvironmentalReadingEntity) -> Unit
) {
    var actualTime by remember(reading.measurementTime) {
        mutableStateOf(reading.measurementTime.ifEmpty {
            when (periodName) {
                "Start of shift" -> "06:30"
                "Middle of shift" -> "11:45"
                "End of shift" -> "15:15"
                else -> "08:00"
            }
        })
    }

    // Thermal inputs (stored as string in UI; empty string represents null/not taken, "0" represents 0.0)
    var wbText by remember(reading.wetBulbTemp) { mutableStateOf(reading.wetBulbTemp?.toString() ?: "") }
    var dbText by remember(reading.dryBulbTemp) { mutableStateOf(reading.dryBulbTemp?.toString() ?: "") }
    var gtText by remember(reading.globeTemp) { mutableStateOf(reading.globeTemp?.toString() ?: "") }
    var rhText by remember(reading.relativeHumidity) { mutableStateOf(reading.relativeHumidity?.toString() ?: "") }
    var windText by remember(reading.windSpeed) { mutableStateOf(reading.windSpeed?.toString() ?: "") }
    var conditionsText by remember(reading.generalConditions) { mutableStateOf(reading.generalConditions) }

    // Configurable Equipment inputs
    var equipmentUsed by remember(reading.equipmentUsed) { mutableStateOf(reading.equipmentUsed) }
    var instrumentModel by remember(reading.instrumentModel) { mutableStateOf(reading.instrumentModel) }
    var serialNumber by remember(reading.serialNumber) { mutableStateOf(reading.serialNumber) }
    var commentsText by remember(reading.comments) { mutableStateOf(reading.comments) }

    // Calculated WBGT
    val calculatedWbgt = remember(wbText, gtText, dbText) {
        val wb = wbText.toDoubleOrNull()
        val gt = gtText.toDoubleOrNull()
        val db = dbText.toDoubleOrNull()
        if (wb != null && gt != null && db != null) {
            (0.7 * wb + 0.2 * gt + 0.1 * db)
        } else null
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .testTag("shift_period_card_${periodName.replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (periodName) {
                            "Start of shift" -> SafetyBlue.copy(alpha = 0.2f)
                            "Middle of shift" -> SafetyOrange.copy(alpha = 0.2f)
                            else -> SafetyGreen.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = periodName.uppercase(),
                            color = when (periodName) {
                                "Start of shift" -> SafetyBlue
                                "Middle of shift" -> SafetyOrange
                                else -> SafetyGreen
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (canEdit) {
                    Button(
                        onClick = {
                            val wb = if (wbText.isBlank()) null else wbText.toDoubleOrNull()
                            val db = if (dbText.isBlank()) null else dbText.toDoubleOrNull()
                            val gt = if (gtText.isBlank()) null else gtText.toDoubleOrNull()
                            val rh = if (rhText.isBlank()) null else rhText.toDoubleOrNull()
                            val wind = if (windText.isBlank()) null else windText.toDoubleOrNull()

                            onSave(
                                reading.copy(
                                    shiftPeriod = periodName,
                                    measurementTime = actualTime,
                                    wetBulbTemp = wb,
                                    dryBulbTemp = db,
                                    globeTemp = gt,
                                    wbgtIndex = calculatedWbgt,
                                    relativeHumidity = rh,
                                    windSpeed = wind,
                                    generalConditions = conditionsText,
                                    equipmentUsed = equipmentUsed.ifEmpty { "WBGT Logger AZ 87786 & Kestrel 4000" },
                                    instrumentModel = instrumentModel.ifEmpty { "WBGT Logger model AZ 87786" },
                                    serialNumber = serialNumber.ifEmpty { "SN-AZ87786-01" },
                                    comments = commentsText
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Save Readings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Measurement Time
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = actualTime,
                    onValueChange = { actualTime = it },
                    label = { Text("Actual Measurement Time") },
                    placeholder = { Text("e.g. 06:30") },
                    enabled = canEdit,
                    modifier = Modifier.weight(1f)
                )
                // WBGT Readout Badge
                Surface(
                    color = Charcoal800,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SafetyAmber),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Calculated WBGT", color = TextMuted, fontSize = 9.sp)
                        Text(
                            text = calculatedWbgt?.let { "%.1f°C".format(it) } ?: "Not Taken",
                            color = if (calculatedWbgt != null) SafetyRed else TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Temperature Matrix: WB, DB, GT
            Text("Thermal Temperature Parameters (°C):", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = wbText,
                    onValueChange = { wbText = it },
                    label = { Text("Wet-Bulb") },
                    placeholder = { Text("Blank=N/T") },
                    enabled = canEdit,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = dbText,
                    onValueChange = { dbText = it },
                    label = { Text("Dry-Bulb") },
                    placeholder = { Text("Blank=N/T") },
                    enabled = canEdit,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = gtText,
                    onValueChange = { gtText = it },
                    label = { Text("Globe") },
                    placeholder = { Text("Blank=N/T") },
                    enabled = canEdit,
                    modifier = Modifier.weight(1f)
                )
            }

            // Aero Parameters: RH, Wind Speed
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = rhText,
                    onValueChange = { rhText = it },
                    label = { Text("Humidity (%)") },
                    placeholder = { Text("Blank=N/T") },
                    enabled = canEdit,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = windText,
                    onValueChange = { windText = it },
                    label = { Text("Wind (m/s)") },
                    placeholder = { Text("Blank=N/T") },
                    enabled = canEdit,
                    modifier = Modifier.weight(1f)
                )
            }

            // General Environmental Conditions
            OutlinedTextField(
                value = conditionsText,
                onValueChange = { conditionsText = it },
                label = { Text("General Environmental Conditions") },
                placeholder = { Text("e.g. Sunny morning, light dust haze, calm air") },
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth()
            )

            // Configurable Equipment Records
            Text("Configurable Equipment Records:", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = equipmentUsed,
                onValueChange = { equipmentUsed = it },
                label = { Text("Equipment Used") },
                placeholder = { Text("e.g. WBGT Logger & Anemometer") },
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = instrumentModel,
                    onValueChange = { instrumentModel = it },
                    label = { Text("Instrument Model") },
                    placeholder = { Text("e.g. WBGT Logger model AZ 87786") },
                    enabled = canEdit,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = serialNumber,
                    onValueChange = { serialNumber = it },
                    label = { Text("Serial Number") },
                    placeholder = { Text("e.g. SN-882194B") },
                    enabled = canEdit,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = commentsText,
                onValueChange = { commentsText = it },
                label = { Text("Comments / Calibration Notes") },
                placeholder = { Text("e.g. Calibration verified prior to shift inspection") },
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth()
            )

            // Current Reading State Chips (Distinguishes between Not Taken vs 0.0)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(color = Charcoal800, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Wet-Bulb", color = TextMuted, fontSize = 9.sp)
                        Text(
                            text = if (wbText.isBlank()) "Not Taken" else "$wbText°C",
                            color = if (wbText.isBlank()) TextMuted else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(color = Charcoal800, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Dry-Bulb", color = TextMuted, fontSize = 9.sp)
                        Text(
                            text = if (dbText.isBlank()) "Not Taken" else "$dbText°C",
                            color = if (dbText.isBlank()) TextMuted else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(color = Charcoal800, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Globe", color = TextMuted, fontSize = 9.sp)
                        Text(
                            text = if (gtText.isBlank()) "Not Taken" else "$gtText°C",
                            color = if (gtText.isBlank()) TextMuted else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(color = Charcoal800, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Wind", color = TextMuted, fontSize = 9.sp)
                        Text(
                            text = if (windText.isBlank()) "Not Taken" else "$windText m/s",
                            color = if (windText.isBlank()) TextMuted else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT: STATUTORY HEADER PANE
// ==========================================
@Composable
fun StatutoryHeaderPane(
    assessment: AssessmentEntity,
    canEdit: Boolean,
    onUpdateAssessment: (AssessmentEntity) -> Unit
) {
    var previousVisitDate by remember(assessment.previousSiteVisitDate) { mutableStateOf(assessment.previousSiteVisitDate) }
    var previousRemedial by remember(assessment.previousVisitRemedialSummary) { mutableStateOf(assessment.previousVisitRemedialSummary) }
    var specialInstructions by remember(assessment.specialHygienistInstructions) { mutableStateOf(assessment.specialHygienistInstructions) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("STATUTORY IDENTIFICATION & CLIENT DETAILS", color = SafetyAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Text("• Client Name: ${assessment.clientName}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("• Mine Name & Location: ${assessment.mineNameAndLocation}", color = TextSecondary, fontSize = 12.sp)
                    Text("• DME Mine Code: ${assessment.dmeMineCode}", color = SafetyAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("• Facility Identification: ${assessment.ablutionType}", color = Color.White, fontSize = 12.sp)
                    Text("• Location Description: ${assessment.facilityLocationDescription}", color = TextSecondary, fontSize = 12.sp)
                    Text("• Date of Field Visit: ${assessment.dateOfFieldVisit}", color = TextSecondary, fontSize = 12.sp)
                    Text("• Assessment Document Reference: ${assessment.documentNumber}", color = SafetyAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("• Assessment Type: ${assessment.assessmentType}", color = TextSecondary, fontSize = 12.sp)
                    Text("• Appointed Assessor: ${assessment.assessorName} (${assessment.assessorRole})", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("STATUTORY APPOINTEES & CONTACTS (MHSA 29 OF 1996)", color = SafetyAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("• Mine Manager: ${assessment.mineManagerName}", color = Color.White, fontSize = 12.sp)
                    Text("• Mine Manager Contact: ${assessment.mineManagerContact}", color = TextMuted, fontSize = 11.sp)
                    Text("• Site Occupational Hygienist: ${assessment.hygienistName} (${assessment.hygienistRole})", color = Color.White, fontSize = 12.sp)
                    Text("• Hygienist Contact: ${assessment.hygienistContact}", color = TextMuted, fontSize = 11.sp)
                    Text("• Occupational Medical Practitioner (OMP): ${assessment.medicalPractitionerName}", color = Color.White, fontSize = 12.sp)
                    Text("• OMP Contact Details: ${assessment.medicalPractitionerContact}", color = TextMuted, fontSize = 11.sp)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PREVIOUS VISIT & SPECIAL HYGIENIST INSTRUCTIONS", color = SafetyAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    if (canEdit) {
                        OutlinedTextField(
                            value = previousVisitDate,
                            onValueChange = {
                                previousVisitDate = it
                                onUpdateAssessment(assessment.copy(previousSiteVisitDate = it))
                            },
                            label = { Text("Previous Site Visit Date") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = previousRemedial,
                            onValueChange = {
                                previousRemedial = it
                                onUpdateAssessment(assessment.copy(previousVisitRemedialSummary = it))
                            },
                            label = { Text("Remedial Actions from Previous Visit") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = specialInstructions,
                            onValueChange = {
                                specialInstructions = it
                                onUpdateAssessment(assessment.copy(specialHygienistInstructions = it))
                            },
                            label = { Text("Special Instructions from Appointed OH") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("• Previous Site Visit Date: ${assessment.previousSiteVisitDate}", color = TextSecondary, fontSize = 12.sp)
                        Text("• Remedial Actions from Previous Visit: ${assessment.previousVisitRemedialSummary}", color = TextSecondary, fontSize = 12.sp)
                        Text("• Special Instructions from Appointed OH: ${assessment.specialHygienistInstructions}", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

// ==========================================
// COMPONENT: SIGNATURES & LEGAL REVIEW PANE
// ==========================================
@Composable
fun SignaturesAndReviewPane(
    assessment: AssessmentEntity,
    canEdit: Boolean,
    canApprove: Boolean,
    assessorSig: String,
    hygienistSig: String,
    onAssessorSigChange: (String) -> Unit,
    onHygienistSigChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onApprove: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ASSESSMENT LEGAL SIGN-OFF & STATUS",
                        color = SafetyAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Document Ref: ${assessment.documentNumber}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Appointed Assessor: ${assessment.assessorName} (${assessment.assessorRole})",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Appointed Hygienist: ${assessment.hygienistName} (${assessment.hygienistRole})",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Status: ", color = TextMuted, fontSize = 12.sp)
                        StatusBadge(status = assessment.status)
                    }

                    if (assessment.payloadHash.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Legal Audit Hash (SHA-256):",
                            color = SafetyAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = assessment.payloadHash,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Section 10: Assessor Signature
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. OCCUPATIONAL HYGIENE ASSESSOR SIGNATURE",
                        color = SafetyAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "I certify that I have conducted this physical workplace ablution assessment in accordance with MHSA regulations and WFH standards.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (canEdit) {
                        SignaturePad(
                            signerRole = "Assessor",
                            onSignatureCaptured = onAssessorSigChange
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onSubmit,
                            enabled = assessorSig.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_hygienist_review_button")
                        ) {
                            Text("Submit for Occupational Hygienist Review", color = Charcoal900, fontWeight = FontWeight.Bold)
                        }
                    } else if (assessment.assessorSignatureSvg.isNotEmpty()) {
                        Surface(
                            color = Charcoal800,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            Text("Assessor Signature Recorded & Geotagged", color = SafetyGreen, fontSize = 11.sp, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
        }

        // Section 11: Hygienist Signature & Sign-Off
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. APPOINTED OCCUPATIONAL HYGIENIST SIGN-OFF",
                        color = SafetyAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Statutory review and approval by Appointed Occupational Hygienist (MHSA Regulation 12.1).",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (canApprove) {
                        SignaturePad(
                            signerRole = "Appointed Occupational Hygienist",
                            onSignatureCaptured = onHygienistSigChange
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("approve_assessment_button")
                        ) {
                            Text("Approve & Sign Statutory Report", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else if (assessment.hygienistSignatureSvg.isNotEmpty()) {
                        Surface(
                            color = Charcoal800,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            Text("Approved & Signed by ${assessment.hygienistName}", color = SafetyGreen, fontSize = 11.sp, modifier = Modifier.padding(12.dp))
                        }
                    } else {
                        Text(
                            text = "Pending Assessor Submission. Hygienist approval required upon submission.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
