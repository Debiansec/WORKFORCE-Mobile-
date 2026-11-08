package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.CustomerEntity
import com.example.data.model.FacilityEntity
import com.example.data.model.MineSiteEntity
import com.example.ui.components.WfhTopBar
import com.example.ui.theme.*
import com.example.viewmodel.WfhViewModel
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

data class AblutionOption(
    val id: String,
    val label: String,
    val checklistName: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val ablutionOptionsList = listOf(
    AblutionOption(
        id = "MENS_CHANGE_HOUSE",
        label = "Men’s change house",
        checklistName = "Monthly ablution facilities checklist Male",
        description = "50-point core statutory inspection covering Basins, Showers, Toilets, Urinals, Lockers & General.",
        icon = Icons.Default.Male
    ),
    AblutionOption(
        id = "LADYS_CHANGE_HOUSE",
        label = "Lady’s change house",
        checklistName = "Monthly ablution facilities checklist Female",
        description = "50-point statutory checklist covering Basins, Showers, Toilets, Sanitary facilities, Lockers & General.",
        icon = Icons.Default.Female
    ),
    AblutionOption(
        id = "LADYS_OFFICE_TOILET",
        label = "Lady’s office toilet",
        checklistName = "Monthly toilet and basin checklist Male / Female",
        description = "20-point targeted office checklist covering Basins, Toilets, Sanitary Disposal & Hygiene.",
        icon = Icons.Default.Wc
    ),
    AblutionOption(
        id = "MENS_OFFICE_TOILET",
        label = "Men’s office toilet",
        checklistName = "Monthly toilet and basin checklist Male / Female",
        description = "20-point targeted office checklist covering Basins, Toilets, Urinals & Hand Drying.",
        icon = Icons.Default.Wc
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun NewAssessmentWizardScreen(
    viewModel: WfhViewModel,
    preselectedCustomer: CustomerEntity? = null,
    preselectedSite: MineSiteEntity? = null,
    preselectedFacility: FacilityEntity? = null,
    onNavigateBack: () -> Unit,
    onAssessmentCreated: (String) -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()
    val mineSites by viewModel.mineSites.collectAsState()
    val facilities by viewModel.facilities.collectAsState()
    val currentAssessorName by viewModel.currentUserName.collectAsState()

    var selectedCustomer by remember { mutableStateOf(preselectedCustomer ?: customers.firstOrNull()) }
    var selectedSite by remember { mutableStateOf(preselectedSite ?: mineSites.firstOrNull { it.customerId == selectedCustomer?.id }) }
    var selectedFacility by remember { mutableStateOf(preselectedFacility ?: facilities.firstOrNull { it.siteId == selectedSite?.id }) }

    // Ablution Selection from Combo Box
    var selectedAblutionOption by remember { mutableStateOf(ablutionOptionsList[0]) }
    var isAblutionDropdownExpanded by remember { mutableStateOf(false) }

    // Description and / or location text
    var locationDescription by remember {
        mutableStateOf(preselectedFacility?.name ?: "Kuipersbult Plant Main Change House & Ablution")
    }

    // Header & Metadata fields
    val todayFormatted = remember {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        sdf.format(Date())
    }
    var dateOfVisit by remember { mutableStateOf(todayFormatted) }
    var docReference by remember { mutableStateOf("WFH-FMT-AF-AWI-FMR-26-001") }
    var previousVisitDate by remember { mutableStateOf("15 Oct 2026") }
    var previousRemedialSummary by remember {
        mutableStateOf("Replace missing soap dispensers in Basin #2; adjust cubicle 1 latch; service sanitary waste bin.")
    }
    var specialInstructions by remember {
        mutableStateOf("Perform full verification according to MHSA regulations. Focus on ventilation, disinfection records, hot water, and tap closures.")
    }

    var showAdvancedHeader by remember { mutableStateOf(true) }

    // GPS State
    var currentLat by remember { mutableStateOf(selectedSite?.latitude ?: -23.7076) }
    var currentLng by remember { mutableStateOf(selectedSite?.longitude ?: 27.5794) }
    var currentAccuracy by remember { mutableStateOf(4.2f) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fineGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = fineGranted || coarseGranted
        if (hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    currentLat = loc.latitude
                    currentLng = loc.longitude
                    currentAccuracy = loc.accuracy
                }
            }
        }
    }

    LaunchedEffect(selectedCustomer) {
        if (selectedSite?.customerId != selectedCustomer?.id) {
            selectedSite = mineSites.firstOrNull { it.customerId == selectedCustomer?.id }
            selectedFacility = facilities.firstOrNull { it.siteId == selectedSite?.id }
        }
    }

    LaunchedEffect(selectedSite) {
        if (selectedFacility?.siteId != selectedSite?.id) {
            selectedFacility = facilities.firstOrNull { it.siteId == selectedSite?.id }
        }
        selectedSite?.let {
            currentLat = it.latitude
            currentLng = it.longitude
        }
        selectedFacility?.let {
            locationDescription = "${it.name} (${selectedSite?.name ?: ""})"
        }
    }

    Scaffold(
        topBar = {
            WfhTopBar(
                title = "Start Ablution Assessment",
                subtitle = "Monthly Statutory Hygiene Inspection Setup",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // STEP 1: Select Facility Type & Combo Box
            WizardStepHeader(step = 1, title = "Select Ablution Facility Type & Checklist")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Worksheet & Facility Combo Box *",
                        color = SafetyAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Exposed Dropdown Combo Box
                    ExposedDropdownMenuBox(
                        expanded = isAblutionDropdownExpanded,
                        onExpandedChange = { isAblutionDropdownExpanded = !isAblutionDropdownExpanded },
                        modifier = Modifier.fillMaxWidth().testTag("ablution_combo_box")
                    ) {
                        OutlinedTextField(
                            value = selectedAblutionOption.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select from Combo Box") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAblutionDropdownExpanded) },
                            leadingIcon = {
                                Icon(
                                    imageVector = selectedAblutionOption.icon,
                                    contentDescription = null,
                                    tint = SafetyAmber
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SafetyAmber,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = isAblutionDropdownExpanded,
                            onDismissRequest = { isAblutionDropdownExpanded = false },
                            modifier = Modifier.background(Charcoal800)
                        ) {
                            ablutionOptionsList.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = option.icon,
                                                    contentDescription = null,
                                                    tint = if (selectedAblutionOption.id == option.id) SafetyAmber else TextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = option.label,
                                                    color = Color.White,
                                                    fontWeight = if (selectedAblutionOption.id == option.id) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            Text(
                                                text = "↳ ${option.checklistName}",
                                                color = SafetyAmber,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedAblutionOption = option
                                        isAblutionDropdownExpanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Active Checklist Notification Badge
                    Surface(
                        color = Charcoal800,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SafetyAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "APPLICABLE STATUTORY WORKSHEET:",
                                color = SafetyAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = selectedAblutionOption.checklistName,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedAblutionOption.description,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Description and / or location field
                    OutlinedTextField(
                        value = locationDescription,
                        onValueChange = { locationDescription = it },
                        label = { Text("Description and / or Location *") },
                        placeholder = { Text("e.g. Primary Crusher Men's Change House, Block B") },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("location_description_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SafetyAmber,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }

            // STEP 2: Client & Mine Selection
            WizardStepHeader(step = 2, title = "Select Client & Mine Location")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                customers.forEach { customer ->
                    val isSelected = selectedCustomer?.id == customer.id
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) Charcoal700 else SurfaceCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isSelected) SafetyAmber else BorderDark, RoundedCornerShape(8.dp))
                            .clickable { selectedCustomer = customer }
                            .testTag("wizard_customer_${customer.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedCustomer = customer },
                                colors = RadioButtonDefaults.colors(selectedColor = SafetyAmber)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(customer.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("DME Mine Code: ${customer.dmeMineCode} • ${customer.province}", color = SafetyAmber, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Mine Sites
            val availableSites = mineSites.filter { it.customerId == selectedCustomer?.id }
            Text("Mine Sites / Shafts for Selected Client:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                availableSites.forEach { site ->
                    val isSelected = selectedSite?.id == site.id
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) Charcoal700 else SurfaceCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isSelected) SafetyAmber else BorderDark, RoundedCornerShape(8.dp))
                            .clickable { selectedSite = site }
                            .testTag("wizard_site_${site.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedSite = site },
                                colors = RadioButtonDefaults.colors(selectedColor = SafetyAmber)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(site.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Site Code: ${site.siteCode} • GPS: %.4f, %.4f".format(site.latitude, site.longitude), color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // STEP 3: Mandatory Statutory Assessment Header Fields
            WizardStepHeader(step = 3, title = "Assessment Identification & Statutory Header")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STATUTORY APPOINTEES & CODES",
                            color = SafetyAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { showAdvancedHeader = !showAdvancedHeader },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (showAdvancedHeader) "Collapse ▲" else "Expand ▼", color = SafetyAmber, fontSize = 11.sp)
                        }
                    }

                    // Always-visible primary fields
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = docReference,
                            onValueChange = { docReference = it },
                            label = { Text("Assessment Document Ref *") },
                            modifier = Modifier.weight(1.2f).testTag("doc_ref_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SafetyAmber,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = dateOfVisit,
                            onValueChange = { dateOfVisit = it },
                            label = { Text("Date of Visit *") },
                            modifier = Modifier.weight(1f).testTag("date_of_visit_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SafetyAmber,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    AnimatedVisibility(visible = showAdvancedHeader) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Client & Mine Readout
                            Surface(
                                color = Charcoal800,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("• Client Name: ${selectedCustomer?.name ?: "Pending"}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("• Mine Name & Location: ${selectedSite?.name ?: "Pending"} (${selectedCustomer?.address ?: ""})", color = TextSecondary, fontSize = 11.sp)
                                    Text("• DME Mine Code: ${selectedCustomer?.dmeMineCode ?: "10192"}", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("• Assessor's Name: $currentAssessorName", color = TextSecondary, fontSize = 11.sp)
                                    Text("• Assessment Type: Monthly Statutory Hygiene Inspection", color = TextSecondary, fontSize = 11.sp)
                                }
                            }

                            // Appointees
                            Surface(
                                color = Charcoal800,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("• Mine Manager: ${selectedCustomer?.mineManager ?: "Peter Lekgeu"} (${selectedCustomer?.managerPhone ?: "+27 (0)14 763 2189"})", color = TextSecondary, fontSize = 11.sp)
                                    Text("• Site Occupational Hygienist: ${selectedCustomer?.appointedHygienist ?: "Mr N B Letlape"} (${selectedCustomer?.hygienistPhone ?: "+27 (0)83 456 2673"})", color = TextSecondary, fontSize = 11.sp)
                                    Text("• Occupational Medical Practitioner (OMP): ${selectedCustomer?.medicalPractitioner ?: "Dr K O Sebopa"} (${selectedCustomer?.medicalPhone ?: "+27 (0)14 763 9000"})", color = TextSecondary, fontSize = 11.sp)
                                }
                            }

                            // Previous visit date and remedial actions
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = previousVisitDate,
                                    onValueChange = { previousVisitDate = it },
                                    label = { Text("Previous Site Visit Date") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = previousRemedialSummary,
                                onValueChange = { previousRemedialSummary = it },
                                label = { Text("Remedial Actions from Previous Visit") },
                                modifier = Modifier.fillMaxWidth().testTag("previous_remedial_input")
                            )

                            OutlinedTextField(
                                value = specialInstructions,
                                onValueChange = { specialInstructions = it },
                                label = { Text("Special Instructions from Appointed OH") },
                                modifier = Modifier.fillMaxWidth().testTag("special_instructions_input")
                            )
                        }
                    }
                }
            }

            // STEP 4: GPS Geotag Verification
            WizardStepHeader(step = 4, title = "Verify GPS Inspection Coordinates")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = SafetyGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GPS: %.5f, %.5f".format(currentLat, currentLng), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal700),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("refresh_gps_btn")
                        ) {
                            Text("Refresh GPS", fontSize = 10.sp, color = SafetyAmber)
                        }
                    }
                    Text(text = "Accuracy: ±%.1f meters (Tamper-evident legal audit trail)".format(currentAccuracy), color = TextMuted, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action: Start Assessment
            Button(
                onClick = {
                    if (selectedCustomer != null && selectedSite != null) {
                        val facId = selectedFacility?.id ?: "FAC-GEN-01"
                        viewModel.createNewAssessment(
                            customerId = selectedCustomer!!.id,
                            siteId = selectedSite!!.id,
                            facilityId = facId,
                            templateType = "ABLUTION_CORE_50",
                            ablutionType = selectedAblutionOption.label,
                            locationDescription = locationDescription,
                            documentNumber = docReference,
                            dateOfFieldVisit = dateOfVisit,
                            previousVisitDate = previousVisitDate,
                            previousRemedialSummary = previousRemedialSummary,
                            specialInstructions = specialInstructions,
                            lat = currentLat,
                            lng = currentLng,
                            accuracy = currentAccuracy,
                            onCreated = { newAssessmentId ->
                                onAssessmentCreated(newAssessmentId)
                            }
                        )
                    }
                },
                enabled = selectedCustomer != null && selectedSite != null && locationDescription.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_create_assessment_btn")
            ) {
                Text(
                    text = "START ${selectedAblutionOption.label.uppercase()} ASSESSMENT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun WizardStepHeader(step: Int, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 6.dp)
    ) {
        Surface(
            color = SafetyAmber,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "$step", color = Charcoal900, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
