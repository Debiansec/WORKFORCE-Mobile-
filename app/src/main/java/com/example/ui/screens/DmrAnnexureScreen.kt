package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.components.WfhTopBar
import com.example.ui.theme.*
import com.example.viewmodel.WfhViewModel

@Composable
fun DmrAnnexureScreen(
    viewModel: WfhViewModel,
    onNavigateBack: () -> Unit
) {
    val commodities by viewModel.commodities.collectAsState()
    val activityAreas by viewModel.activityAreas.collectAsState()
    val occupations by viewModel.occupations.collectAsState()
    val pollutants by viewModel.pollutants.collectAsState()
    val dmrSamples by viewModel.dmrExposureSamples.collectAsState()

    var selectedSection by remember { mutableStateOf(0) }
    val sectionTitles = listOf(
        "HEG Form 21.9(2)(a)",
        "Commodity Codes (A1)",
        "Activity Areas (A2)",
        "Occupations (A3)",
        "Pollutant OELs (A4)"
    )

    var searchQuery by remember { mutableStateOf("") }

    // Dialog state for DMPR HEG
    var showHegDialog by remember { mutableStateOf(false) }
    var hegToEdit by remember { mutableStateOf<DmrExposureSampleEntity?>(null) }
    var hegToDelete by remember { mutableStateOf<DmrExposureSampleEntity?>(null) }

    Scaffold(
        topBar = {
            WfhTopBar(
                title = "DMPR Mandatory Standards",
                subtitle = "COP Airborne Pollutants Annexure A & C (Pages 31-63)",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (selectedSection == 0) {
                        IconButton(
                            onClick = {
                                hegToEdit = null
                                showHegDialog = true
                            },
                            modifier = Modifier.testTag("add_heg_top_btn")
                        ) {
                            Icon(Icons.Default.AddChart, contentDescription = "Add HEG Sample", tint = SafetyAmber)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedSection == 0) {
                FloatingActionButton(
                    onClick = {
                        hegToEdit = null
                        showHegDialog = true
                    },
                    containerColor = SafetyRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_add_heg")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AddChart, contentDescription = null)
                        Text("New HEG Sample", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        },
        containerColor = Charcoal900
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                containerColor = Charcoal800,
                contentColor = SafetyAmber,
                edgePadding = 12.dp
            ) {
                sectionTitles.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedSection == idx,
                        onClick = { selectedSection = idx },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedSection == idx) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSection == idx) SafetyAmber else TextSecondary
                            )
                        },
                        modifier = Modifier.testTag("tab_dmr_$idx")
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search codes, descriptions, CAS numbers...", fontSize = 12.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("dmr_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SafetyAmber,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (selectedSection) {
                    0 -> {
                        // DMR Form 21.9(2)(a) HEG Personal Exposure Report (Page 73-77)
                        item {
                            Surface(
                                color = Charcoal800,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("QUARTERLY AIRBORNE POLLUTANTS EXPOSURE REPORT", color = SafetyAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("Regulation 9.2.7 | Form 21.9(2)(a) Single & Additive Effects", color = TextSecondary, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Statistical HEG 90th percentile classification (Band A >= OEL, Band B >= 50% OEL, Band C >= 10% OEL, Band D < 10% OEL).", color = TextMuted, fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = {
                                            hegToEdit = null
                                            showHegDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp).testTag("quick_add_heg_btn")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add HEG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        val filteredSamples = dmrSamples.filter {
                            it.hegNumber.contains(searchQuery, ignoreCase = true) ||
                                    it.hegDescription.contains(searchQuery, ignoreCase = true) ||
                                    it.occupationTitle.contains(searchQuery, ignoreCase = true) ||
                                    it.pollutantCode.contains(searchQuery, ignoreCase = true)
                        }

                        items(filteredSamples) { sample ->
                            DmrSampleCard(
                                sample = sample,
                                onEdit = {
                                    hegToEdit = sample
                                    showHegDialog = true
                                },
                                onDelete = {
                                    hegToDelete = sample
                                }
                            )
                        }
                    }
                    1 -> {
                        // Commodities
                        val filtered = commodities.filter {
                            it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true)
                        }
                        items(filtered) { comm ->
                            CodeCard(code = comm.code, title = comm.name, subtitle = comm.description, badge = "Commodity")
                        }
                    }
                    2 -> {
                        // Activity Areas
                        val filtered = activityAreas.filter {
                            it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true)
                        }
                        items(filtered) { act ->
                            CodeCard(
                                code = act.code,
                                title = act.name,
                                subtitle = if (act.isUnderground) "Underground Ventilation District" else "Surface Activity Area",
                                badge = if (act.isUnderground) "Underground" else "Surface"
                            )
                        }
                    }
                    3 -> {
                        // Occupations
                        val filtered = occupations.filter {
                            it.title.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true)
                        }
                        items(filtered) { occ ->
                            CodeCard(code = occ.code, title = occ.title, subtitle = occ.category, badge = "Occupation")
                        }
                    }
                    4 -> {
                        // Pollutants & OELs
                        val filtered = pollutants.filter {
                            it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) || it.casNumber.contains(searchQuery, ignoreCase = true)
                        }
                        items(filtered) { pol ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = pol.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "Code: ${pol.code} • Target Organ: ${pol.targetOrgan}", color = TextMuted, fontSize = 11.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Surface(color = Charcoal800, shape = RoundedCornerShape(6.dp)) {
                                            Text(
                                                text = "OEL: ${pol.oelTwa} ${pol.unit}",
                                                color = SafetyAmber,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // ==========================================
    // CREATE / EDIT HEG SAMPLE DIALOG
    // ==========================================
    if (showHegDialog) {
        DmrHegEditDialog(
            initialSample = hegToEdit,
            onDismiss = { showHegDialog = false },
            onSave = { savedSample ->
                viewModel.saveDmrSample(savedSample)
                showHegDialog = false
            }
        )
    }

    // ==========================================
    // CONFIRM DELETE HEG SAMPLE DIALOG
    // ==========================================
    hegToDelete?.let { s ->
        AlertDialog(
            onDismissRequest = { hegToDelete = null },
            title = {
                Text("Delete HEG Sample?", color = SafetyRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Are you sure you want to delete ${s.hegNumber} (${s.hegDescription}) for ${s.reportingPeriod}?\nConcentration: ${s.sampleConcentrationTwa} mg/m³ (Band ${s.classificationBand}).",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDmrSample(s)
                        hegToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                    modifier = Modifier.testTag("confirm_delete_heg_btn")
                ) {
                    Text("Delete Sample", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { hegToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

@Composable
fun DmrSampleCard(
    sample: DmrExposureSampleEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .testTag("heg_sample_card_${sample.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${sample.hegNumber}: ${sample.hegDescription}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${sample.occupationTitle} (${sample.occupationCode}) • Period: ${sample.reportingPeriod}",
                        color = SafetyAmber,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatusBadge(status = "Band ${sample.classificationBand}")

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp).testTag("edit_heg_${sample.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit HEG", tint = SafetyAmber, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("delete_heg_${sample.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete HEG", tint = SafetyRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(color = Charcoal800, shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TWA Concen.", color = TextMuted, fontSize = 9.sp)
                        Text("${sample.sampleConcentrationTwa} mg/m³", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(color = Charcoal800, shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("OEL Limit", color = TextMuted, fontSize = 9.sp)
                        Text("${sample.oelLimit} mg/m³", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(color = Charcoal800, shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("90th Percentile", color = TextMuted, fontSize = 9.sp)
                        Text("${sample.calculated90thPercentile}", color = SafetyOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(color = Charcoal800, shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("AQI Combined", color = TextMuted, fontSize = 9.sp)
                        Text("${sample.aqiCombinedIndex}", color = SafetyGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (sample.correctiveMeasures.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "• Corrective Action: ${sample.correctiveMeasures}", color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun DmrHegEditDialog(
    initialSample: DmrExposureSampleEntity?,
    onDismiss: () -> Unit,
    onSave: (DmrExposureSampleEntity) -> Unit
) {
    val isEdit = initialSample != null
    var hegNumber by remember { mutableStateOf(initialSample?.hegNumber ?: "HEG 1.1") }
    var hegDesc by remember { mutableStateOf(initialSample?.hegDescription ?: "Drill Rig & Excavator Operators") }
    var occCode by remember { mutableStateOf(initialSample?.occupationCode ?: "20401") }
    var occTitle by remember { mutableStateOf(initialSample?.occupationTitle ?: "Drill rig operator (jumbo)") }
    var pollutantCode by remember { mutableStateOf(initialSample?.pollutantCode ?: "AIRB-DUST") }
    var period by remember { mutableStateOf(initialSample?.reportingPeriod ?: "2026-Q1") }
    var concentrationText by remember { mutableStateOf(initialSample?.sampleConcentrationTwa?.toString() ?: "1.45") }
    var oelText by remember { mutableStateOf(initialSample?.oelLimit?.toString() ?: "3.00") }
    var percentileText by remember { mutableStateOf(initialSample?.calculated90thPercentile?.toString() ?: "1.62") }
    var correctiveMeasures by remember { mutableStateOf(initialSample?.correctiveMeasures ?: "Verify water suppression nozzles on drill mast.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Modify HEG Exposure Sample" else "Add HEG Exposure Sample (Form 21.9(2)(a))",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quick Afrimat Kuipersbult Presets
                if (!isEdit) {
                    Text("Afrimat Kuipersbult HEG Presets", color = SafetyAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                hegNumber = "HEG 1.1"
                                hegDesc = "Drill & Blast Crew"
                                occCode = "20401"
                                occTitle = "Drill rig operator"
                                pollutantCode = "AIRB-DUST"
                                concentrationText = "1.82"
                                oelText = "3.00"
                                percentileText = "2.10"
                                correctiveMeasures = "Check dust suppression sprayers on drill rigs"
                            },
                            contentPadding = PaddingValues(2.dp),
                            modifier = Modifier.weight(1f).height(28.dp)
                        ) {
                            Text("HEG 1.1 Drilling", fontSize = 9.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                hegNumber = "HEG 2.1"
                                hegDesc = "Loading & Hauling Fleet"
                                occCode = "21602"
                                occTitle = "Articulated dump truck driver"
                                pollutantCode = "AIRB-DUST"
                                concentrationText = "2.40"
                                oelText = "3.00"
                                percentileText = "2.75"
                                correctiveMeasures = "Enforce air conditioning cabin seal inspections"
                            },
                            contentPadding = PaddingValues(2.dp),
                            modifier = Modifier.weight(1f).height(28.dp)
                        ) {
                            Text("HEG 2.1 Haulage", fontSize = 9.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                hegNumber = "HEG 3.1"
                                hegDesc = "Processing & Crushing Plant"
                                occCode = "21701"
                                occTitle = "Crusher operator"
                                pollutantCode = "SILICA-RESP"
                                concentrationText = "0.08"
                                oelText = "0.10"
                                percentileText = "0.09"
                                correctiveMeasures = "Inspect atomised water sprays at primary jaw crusher"
                            },
                            contentPadding = PaddingValues(2.dp),
                            modifier = Modifier.weight(1f).height(28.dp)
                        ) {
                            Text("HEG 3.1 Crusher", fontSize = 9.sp)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hegNumber,
                        onValueChange = { hegNumber = it },
                        label = { Text("HEG Number *") },
                        placeholder = { Text("e.g. HEG 1.1") },
                        modifier = Modifier.weight(1f).testTag("heg_number_input")
                    )
                    OutlinedTextField(
                        value = period,
                        onValueChange = { period = it },
                        label = { Text("Reporting Period") },
                        placeholder = { Text("2026-Q1") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = hegDesc,
                    onValueChange = { hegDesc = it },
                    label = { Text("HEG Description *") },
                    placeholder = { Text("e.g. Front End Loader & Excavator Operators") },
                    modifier = Modifier.fillMaxWidth().testTag("heg_desc_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = occCode,
                        onValueChange = { occCode = it },
                        label = { Text("Occ Code") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = pollutantCode,
                        onValueChange = { pollutantCode = it },
                        label = { Text("Pollutant Code") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = occTitle,
                    onValueChange = { occTitle = it },
                    label = { Text("Occupation Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = concentrationText,
                        onValueChange = { concentrationText = it },
                        label = { Text("TWA Concen (mg/m³)") },
                        modifier = Modifier.weight(1f).testTag("heg_concen_input")
                    )
                    OutlinedTextField(
                        value = oelText,
                        onValueChange = { oelText = it },
                        label = { Text("OEL Limit") },
                        modifier = Modifier.weight(1f).testTag("heg_oel_input")
                    )
                }

                OutlinedTextField(
                    value = percentileText,
                    onValueChange = { percentileText = it },
                    label = { Text("Calculated 90th Percentile") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = correctiveMeasures,
                    onValueChange = { correctiveMeasures = it },
                    label = { Text("Statutory Corrective Action") },
                    placeholder = { Text("e.g. Inspect suppression sprayers, cabin filters") },
                    modifier = Modifier.fillMaxWidth().testTag("heg_action_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val concen = concentrationText.toDoubleOrNull() ?: 1.0
                    val oel = oelText.toDoubleOrNull() ?: 3.0
                    val perc = percentileText.toDoubleOrNull() ?: concen

                    // Statistical Band Classification according to DMPR Annexure C:
                    // Band A: >= OEL
                    // Band B: >= 50% OEL
                    // Band C: >= 10% OEL
                    // Band D: < 10% OEL
                    val band = when {
                        perc >= oel - 1e-6 -> "A"
                        perc >= 0.5 * oel - 1e-6 -> "B"
                        perc >= 0.1 * oel - 1e-6 -> "C"
                        else -> "D"
                    }

                    val sampleId = initialSample?.id ?: "HEG-SMP-${System.currentTimeMillis() % 100000}"

                    onSave(
                        DmrExposureSampleEntity(
                            id = sampleId,
                            assessmentId = initialSample?.assessmentId ?: "DMPR-MCOP-2026",
                            mineCode = "10192",
                            subMineCode = "01",
                            reportingPeriod = period.trim().ifEmpty { "2026-Q1" },
                            commodityCode = "QD",
                            sampleArea = "Quarry Primary Pit & Crushing Area",
                            activityAreaCode = "07",
                            hegNumber = hegNumber.trim().ifEmpty { "HEG 1.1" },
                            hegDescription = hegDesc.trim().ifEmpty { "General Workstation" },
                            occupationCode = occCode.trim().ifEmpty { "20401" },
                            occupationTitle = occTitle.trim().ifEmpty { "Production Operator" },
                            pollutantCode = pollutantCode.trim().ifEmpty { "AIRB-DUST" },
                            sampleConcentrationTwa = concen,
                            analysisPercent = 100.0,
                            oelLimit = oel,
                            allocatedMedicalDose = 1.0,
                            calculated90thPercentile = perc,
                            classificationBand = band,
                            pollutantIndex = if (oel > 0) concen / oel else 0.0,
                            aqiCombinedIndex = if (oel > 0) perc / oel else 0.0,
                            reasonsForOverExposure = if (band == "A") "Heavy haulage traffic during dry wind conditions" else "",
                            correctiveMeasures = correctiveMeasures.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                modifier = Modifier.testTag("confirm_save_heg_btn")
            ) {
                Text(if (isEdit) "Update Sample" else "Save HEG Sample", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = SurfaceCard
    )
}

@Composable
fun CodeCard(code: String, title: String, subtitle: String, badge: String) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                if (subtitle.isNotEmpty()) {
                    Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
                }
            }
            Surface(
                color = Charcoal800,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Text(
                    text = "Code $code",
                    color = SafetyAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
