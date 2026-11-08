package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssessmentAnswerEntity
import com.example.data.model.AssessmentEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.WfhTopBar
import com.example.ui.theme.*
import com.example.viewmodel.UserRole
import com.example.viewmodel.WfhViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportPreviewScreen(
    assessmentId: String,
    viewModel: WfhViewModel,
    onNavigateBack: () -> Unit
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

    // Role-based access control rule: Client representatives cannot access draft reports (Section 13)
    if (assessment.status == "DRAFT" && currentRole == UserRole.MINE_MANAGEMENT) {
        Scaffold(
            topBar = {
                WfhTopBar(
                    title = "Access Restricted",
                    canNavigateBack = true,
                    onNavigateBack = onNavigateBack
                )
            },
            containerColor = Charcoal900
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier.fillMaxWidth().border(1.dp, SafetyRed, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = SafetyRed, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Draft Report Restricted", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Client Representatives cannot access unapproved draft assessment reports pursuant to MHSA Statutory Assessment Protocol.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
        return
    }

    val isDraft = assessment.status == "DRAFT" || assessment.status == "SUBMITTED"

    Scaffold(
        topBar = {
            WfhTopBar(
                title = "Statutory Report Preview",
                subtitle = "${assessment.documentNumber} • ${if (isDraft) "DRAFT" else "APPROVED"}",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "WORKFORCE HEALTH SA\nOccupational Hygiene Report: ${assessment.documentNumber}\nClient: ${assessment.clientName}\nFacility: ${assessment.ablutionType}\nStatus: ${assessment.status}\nLegal Hash: ${assessment.payloadHash}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Statutory Report"))
                        },
                        modifier = Modifier.testTag("share_report_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = SafetyAmber)
                    }
                }
            )
        },
        containerColor = Charcoal900
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(6.dp)) }

                // Report Header Card
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Charcoal800),
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "WORKFORCE HEALTH SA",
                                    color = SafetyAmber,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                StatusBadge(status = assessment.status)
                            }

                            Text(
                                text = "STATUTORY OCCUPATIONAL HYGIENE INSPECTION REPORT",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Worksheet: ${assessment.checklistTitle}",
                                color = SafetyAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Statutory Header Fields
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                ReportMetaRow("Client Name", assessment.clientName)
                                ReportMetaRow("Mine Name & Location", assessment.mineNameAndLocation)
                                ReportMetaRow("DME Mine Code", assessment.dmeMineCode)
                                ReportMetaRow("Date of Field Visit", assessment.dateOfFieldVisit)
                                ReportMetaRow("Facility / Workplace", "${assessment.ablutionType} - ${assessment.facilityLocationDescription}")
                                ReportMetaRow("Document Reference", assessment.documentNumber)
                                ReportMetaRow("Assessment Type", assessment.assessmentType)
                                ReportMetaRow("Appointed Assessor", "${assessment.assessorName} (${assessment.assessorRole})")
                                ReportMetaRow("Appointed Hygienist", "${assessment.hygienistName} (${assessment.hygienistRole})")
                                ReportMetaRow("Hygienist Contact", assessment.hygienistContact)
                                ReportMetaRow("Mine Manager", "${assessment.mineManagerName} (${assessment.mineManagerContact})")
                                ReportMetaRow("Occupational Medical (OMP)", "${assessment.medicalPractitionerName} (${assessment.medicalPractitionerContact})")
                                ReportMetaRow("Previous Site Visit Date", assessment.previousSiteVisitDate)
                            }
                        }
                    }
                }

                // Section A: Previous Visit Remedial Action
                item {
                    ReportSectionHeader(number = "A", title = "Remedial action required from previous site visit")
                    ReportTextCard(assessment.previousVisitRemedialSummary)
                }

                // Section B: Special Instructions
                item {
                    ReportSectionHeader(number = "B", title = "Special / additional instruction/s by appointed Occupational Hygienist")
                    ReportTextCard(assessment.specialHygienistInstructions)
                }

                // Section 2: Shift Environmental Conditions
                item {
                    ReportSectionHeader(number = "2", title = "Environmental conditions during shift (3 Measurement Periods)")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        readings.forEach { reading ->
                            Surface(
                                color = Charcoal800,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${reading.shiftPeriod.uppercase()} (${reading.measurementTime})",
                                            color = SafetyAmber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = reading.locationDescription,
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Thermal readout
                                    val wbStr = reading.wetBulbTemp?.let { "%.1f°C".format(it) } ?: "Not Taken"
                                    val dbStr = reading.dryBulbTemp?.let { "%.1f°C".format(it) } ?: "Not Taken"
                                    val gtStr = reading.globeTemp?.let { "%.1f°C".format(it) } ?: "Not Taken"
                                    val wbgtStr = reading.wbgtIndex?.let { "%.1f°C".format(it) } ?: "Not Taken"

                                    Text(
                                        text = "Wet Bulb: $wbStr | Dry Bulb: $dbStr | Globe: $gtStr | WBGT Index: $wbgtStr",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    val rhStr = reading.relativeHumidity?.let { "%.0f%%".format(it) } ?: "Not Taken"
                                    val windStr = reading.windSpeed?.let { "%.1f m/s".format(it) } ?: "Not Taken"

                                    Text(
                                        text = "Humidity: $rhStr | Wind Speed: $windStr | Conditions: ${reading.generalConditions.ifEmpty { "Standard shift conditions" }}",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "Equipment: ${reading.equipmentUsed} • Model: ${reading.instrumentModel} • S/N: ${reading.serialNumber}",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 8: Results & Checklist Summary
                item {
                    ReportSectionHeader(number = "8", title = "Checklist Observations & Compliance Summary")
                    val conforming = answers.count { it.response == "YES" }
                    val nonConforming = answers.count { it.isNonConformance }
                    val na = answers.count { it.response == "N/A" }

                    Surface(
                        color = Charcoal800,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Conforming", color = TextMuted, fontSize = 10.sp)
                                Text("$conforming", color = SafetyGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Non-Conforming", color = TextMuted, fontSize = 10.sp)
                                Text("$nonConforming", color = SafetyRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("N/A", color = TextMuted, fontSize = 10.sp)
                                Text("$na", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Section 8b: Detailed Non-Conformances & Remedial Actions
                val nonConformingAnswers = answers.filter { it.isNonConformance }
                if (nonConformingAnswers.isNotEmpty()) {
                    item {
                        ReportSectionHeader(number = "8B", title = "Compulsory Non-Conformance Findings & Remedial Action Plans")
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            nonConformingAnswers.forEach { item ->
                                Surface(
                                    color = Charcoal800,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, SafetyRed),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Q${item.questionNumber}: ${item.questionText}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(text = "Finding / Observation: ${item.remarks}", color = SafetyRed, fontSize = 11.sp)
                                        Text(text = "Required Action: ${item.requiredRemedialAction}", color = SafetyAmber, fontSize = 11.sp)
                                        Text(
                                            text = "Responsible: ${item.responsiblePerson} • Target: ${item.followUpDate} • Status: ${if (item.isClosureConfirmed) "CLOSED & VERIFIED" else "OPEN"}",
                                            color = if (item.isClosureConfirmed) SafetyGreen else SafetyOrange,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 10 & 11: Acknowledgement & Signatures
                item {
                    ReportSectionHeader(number = "10 & 11", title = "Acknowledgement & Statutory Signatures")
                    Surface(
                        color = Charcoal800,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "1. Assessor: ${assessment.assessorName} (${assessment.assessorRole})",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (assessment.assessorSignatureSvg.isNotEmpty()) "Digital Signature Recorded • Geotagged" else "Signature Pending",
                                color = if (assessment.assessorSignatureSvg.isNotEmpty()) SafetyGreen else TextMuted,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "2. Appointed Occupational Hygienist: ${assessment.hygienistName}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (assessment.status == "APPROVED") "Approved & Counter-Signed" else "Pending Review & Approval",
                                color = if (assessment.status == "APPROVED") SafetyGreen else SafetyOrange,
                                fontSize = 11.sp
                            )

                            if (assessment.payloadHash.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tamper-Evident Legal Hash (SHA-256):",
                                    color = SafetyAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = assessment.payloadHash,
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            // Watermark for draft reports
            if (isDraft) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DRAFT - FOR REVIEW ONLY",
                        color = Color.Red.copy(alpha = 0.08f),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.rotate(-35f)
                    )
                }
            }
        }
    }
}

@Composable
fun ReportMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
        Text(text = value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.4f))
    }
}

@Composable
fun ReportSectionHeader(number: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Surface(
            color = SafetyAmber,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = number, color = Charcoal900, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ReportTextCard(text: String) {
    Surface(
        color = Charcoal800,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(8.dp))
    ) {
        Text(
            text = text.ifEmpty { "None recorded." },
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}
