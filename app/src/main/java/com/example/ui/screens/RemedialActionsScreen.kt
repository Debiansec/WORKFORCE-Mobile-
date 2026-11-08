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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RemedialActionEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.WfhTopBar
import com.example.ui.theme.*
import com.example.viewmodel.WfhViewModel

@Composable
fun RemedialActionsScreen(
    viewModel: WfhViewModel,
    onNavigateBack: () -> Unit
) {
    val remedialActions by viewModel.remedialActions.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()

    var filterStatus by remember { mutableStateOf("ALL") }
    var selectedActionForClosure by remember { mutableStateOf<RemedialActionEntity?>(null) }
    var closureNotes by remember { mutableStateOf("") }

    val filteredList = remember(remedialActions, filterStatus) {
        if (filterStatus == "ALL") remedialActions
        else remedialActions.filter { it.status == filterStatus }
    }

    Scaffold(
        topBar = {
            WfhTopBar(
                title = "Remedial Actions Register",
                subtitle = "Non-Conformance & Statutory Corrective Log",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Charcoal900
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "OPEN", "IN_PROGRESS", "CLOSED").forEach { status ->
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        label = { Text(status, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SafetyRed,
                            selectedLabelColor = Color.White,
                            containerColor = Charcoal800,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filterStatus == status,
                            borderColor = BorderDark,
                            selectedBorderColor = SafetyRed
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { action ->
                    RemedialActionCard(
                        action = action,
                        canManage = currentRole.canApprove || currentRole.canEditDraft,
                        onUpdateStatus = { newStatus ->
                            if (newStatus == "CLOSED") {
                                selectedActionForClosure = action
                            } else {
                                viewModel.updateRemedialActionStatus(action, newStatus)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Modal Dialog for Closing Action with Evidence
    if (selectedActionForClosure != null) {
        AlertDialog(
            onDismissRequest = { selectedActionForClosure = null },
            title = {
                Text(
                    text = "Verify & Close Action (${selectedActionForClosure?.actionReference})",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter verification notes and confirmation that corrective work has been inspected according to MHSA statutory guidelines.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = closureNotes,
                        onValueChange = { closureNotes = it },
                        label = { Text("Verification / Closure Comments", fontSize = 11.sp) },
                        placeholder = { Text("E.g., Valve replaced and tested by maintenance team", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("closure_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SafetyGreen,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedActionForClosure?.let {
                            viewModel.updateRemedialActionStatus(it, "CLOSED", closureNotes)
                        }
                        selectedActionForClosure = null
                        closureNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen),
                    modifier = Modifier.testTag("confirm_close_action_btn")
                ) {
                    Text("Verify & Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedActionForClosure = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

@Composable
fun RemedialActionCard(
    action: RemedialActionEntity,
    canManage: Boolean,
    onUpdateStatus: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .testTag("remedial_action_card_${action.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = action.actionReference,
                    color = SafetyAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                StatusBadge(status = action.status)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${action.customerName} • ${action.facilityName}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Charcoal800,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "NON-CONFORMANCE:",
                        color = SafetyRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = action.nonConformanceDescription,
                        color = Color.White,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "CORRECTIVE ACTION REQUIRED:",
                        color = SafetyAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = action.requiredCorrectiveAction,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Resp: ${action.responsiblePerson}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "Target: ${action.targetDate}",
                    color = SafetyOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (action.verificationComments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Verification: ${action.verificationComments}",
                    color = SafetyGreen,
                    fontSize = 10.sp
                )
            }

            if (canManage && action.status != "CLOSED") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (action.status == "OPEN") {
                        OutlinedButton(
                            onClick = { onUpdateStatus("IN_PROGRESS") },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.height(32.dp).testTag("action_in_progress_${action.id}")
                        ) {
                            Text("Set In Progress", fontSize = 10.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = { onUpdateStatus("CLOSED") },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp).testTag("action_close_${action.id}")
                    ) {
                        Text("Verify & Close", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
