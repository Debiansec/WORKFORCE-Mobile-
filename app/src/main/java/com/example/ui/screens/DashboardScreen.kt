package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AssessmentEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.UserRole
import com.example.viewmodel.WfhViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: WfhViewModel,
    onStartNewAssessment: () -> Unit,
    onViewAssessment: (String) -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToMines: () -> Unit,
    onNavigateToRemedial: () -> Unit,
    onNavigateToReadings: () -> Unit,
    onNavigateToDmrAnnexure: () -> Unit
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val assessments by viewModel.assessments.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()

    var showRoleMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            WfhTopBar(
                title = "WORKFORCE HEALTH SA",
                subtitle = "Occupational Hygiene & Mine Health Platform",
                actions = {
                    // Role switcher chip
                    Box {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Charcoal700,
                            modifier = Modifier
                                .clickable { showRoleMenu = true }
                                .border(1.dp, SafetyAmber.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .testTag("role_switcher_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(SafetyGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentRole.displayName.split(" ").first(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch Role",
                                    tint = SafetyAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false },
                            modifier = Modifier.background(SurfaceCard)
                        ) {
                            UserRole.values().forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = role.displayName,
                                                color = if (role == currentRole) SafetyAmber else Color.White,
                                                fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = if (role.canApprove) "Approver / Statutory review" else if (role.canEditDraft) "Field capture & signatures" else "Client read-only",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.switchRole(role)
                                        showRoleMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        containerColor = Charcoal900
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Industrial Hero Banner
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal800),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.wfh_banner),
                            contentDescription = "Mining Operations Header",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Charcoal900.copy(alpha = 0.85f),
                                            Charcoal900
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.wfh_logo),
                                    contentDescription = "WFH Logo",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Charcoal900)
                                        .border(1.dp, SafetyAmber, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "WORKFORCE HEALTH SA",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Appointed: $currentUserName",
                                        color = SafetyAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Operational Status Bar
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Charcoal800,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(SafetyGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = summary.syncStatusText,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "DMPR / MHSA Reg 9.2",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // 3. Operational KPIs (Section 4.1)
            item {
                Text(
                    text = "OPERATIONAL OVERVIEW",
                    color = SafetyAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Assessments",
                        value = "${summary.totalAssessments}",
                        icon = { Icon(Icons.Default.Assignment, "Total", tint = Color.White) },
                        accentColor = SafetyBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "In Progress Drafts",
                        value = "${summary.inProgressDrafts}",
                        icon = { Icon(Icons.Default.EditNote, "Drafts", tint = SafetyAmber) },
                        accentColor = SafetyAmber,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Pending Review",
                        value = "${summary.pendingReview}",
                        icon = { Icon(Icons.Default.PendingActions, "Pending", tint = SafetyOrange) },
                        accentColor = SafetyOrange,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Approved / Final",
                        value = "${summary.approvedAssessments}",
                        icon = { Icon(Icons.Default.CheckCircle, "Approved", tint = SafetyGreen) },
                        accentColor = SafetyGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Remedial Actions",
                        value = "${summary.openRemedialActions}",
                        icon = { Icon(Icons.Default.Warning, "Actions", tint = SafetyRed) },
                        accentColor = SafetyRed,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToRemedial
                    )
                    StatCard(
                        title = "Mine Facilities",
                        value = "${summary.totalMines}",
                        icon = { Icon(Icons.Default.LocationCity, "Mines", tint = SafetyAmber) },
                        accentColor = SafetyAmber,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToMines
                    )
                }
            }

            // 4. Quick Actions (Section 4.1)
            item {
                Text(
                    text = "FIELD ACTIONS",
                    color = SafetyAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Primary Start New Assessment Button
                Button(
                    onClick = onStartNewAssessment,
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_new_assessment_button")
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "START NEW FIELD ASSESSMENT",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Navigation Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToCustomers,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quick_action_customers")
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp), tint = SafetyAmber)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clients", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToMines,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quick_action_mines")
                    ) {
                        Icon(Icons.Default.Terrain, contentDescription = null, modifier = Modifier.size(18.dp), tint = SafetyAmber)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mine Sites", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToReadings,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quick_action_readings")
                    ) {
                        Icon(Icons.Default.Thermostat, contentDescription = null, modifier = Modifier.size(18.dp), tint = SafetyAmber)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Readings", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToDmrAnnexure,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quick_action_dmr_annexure")
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp), tint = SafetyAmber)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DMPR HEGs", fontSize = 12.sp)
                    }
                }
            }

            // 5. Recent Inspections List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT SITE ASSESSMENTS",
                        color = SafetyAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "${assessments.size} registered",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            items(assessments) { assessment ->
                AssessmentCard(
                    assessment = assessment,
                    onClick = { onViewAssessment(assessment.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AssessmentCard(
    assessment: AssessmentEntity,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("assessment_card_${assessment.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assessment.documentNumber,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                StatusBadge(status = assessment.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (assessment.templateType == "ABLUTION_CORE_50") "Ablution Facilities Assessment (50 Core Questions)"
                else "Thermal Stress & Airborne Pollutant Monitoring",
                color = SafetyAmber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateFormat.format(Date(assessment.inspectionDate)),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = assessment.assessorName.split(" ").take(2).joinToString(" "),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            if (assessment.latitude != 0.0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "GPS",
                        tint = SafetyGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GPS: %.4f, %.4f (±%.1fm)".format(assessment.latitude, assessment.longitude, assessment.gpsAccuracyMeters),
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
