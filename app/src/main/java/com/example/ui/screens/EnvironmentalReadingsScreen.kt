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
import com.example.data.model.EnvironmentalReadingEntity
import com.example.ui.components.WfhTopBar
import com.example.ui.theme.*
import com.example.viewmodel.WfhViewModel

@Composable
fun EnvironmentalReadingsScreen(
    viewModel: WfhViewModel,
    onNavigateBack: () -> Unit
) {
    val readings by viewModel.environmentalReadings.collectAsState()

    var showReadingDialog by remember { mutableStateOf(false) }
    var readingToEdit by remember { mutableStateOf<EnvironmentalReadingEntity?>(null) }
    var readingToDelete by remember { mutableStateOf<EnvironmentalReadingEntity?>(null) }

    Scaffold(
        topBar = {
            WfhTopBar(
                title = "Environmental Readings",
                subtitle = "Thermal Stress, Lux, Noise & Weather Registry",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = {
                            readingToEdit = null
                            showReadingDialog = true
                        },
                        modifier = Modifier.testTag("add_reading_top_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Reading", tint = SafetyAmber)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    readingToEdit = null
                    showReadingDialog = true
                },
                containerColor = SafetyRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_reading")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DeviceThermostat, contentDescription = null)
                    Text("Log Reading", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        containerColor = Charcoal900
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal800),
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeviceThermostat, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("STATUTORY INSTRUMENTATION (SABS CALIBRATED)", color = SafetyAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• WBGT Monitor: WBGT Logger model AZ 87786 (Outdoor Index formula: 0.7·WB + 0.2·GT + 0.1·DB)", color = TextSecondary, fontSize = 11.sp)
                        Text("• Anemometer: Kestrel Pocket Weather Tracker model 4000 (Air velocity & wind chill)", color = TextSecondary, fontSize = 11.sp)
                        Text("• Sound Level: Class 1 Integrating Sound Level Meter (OEL: 85 dBA 8-hr TWA)", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOGGED FIELD MEASUREMENTS (${readings.size})",
                        color = SafetyAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time Telemetry & Calibration",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            items(readings) { reading ->
                EnvironmentalReadingCard(
                    reading = reading,
                    onEdit = {
                        readingToEdit = reading
                        showReadingDialog = true
                    },
                    onDelete = {
                        readingToDelete = reading
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // ==========================================
    // CREATE / EDIT READING DIALOG
    // ==========================================
    if (showReadingDialog) {
        ReadingEditDialog(
            initialReading = readingToEdit,
            onDismiss = { showReadingDialog = false },
            onSave = { savedReading ->
                viewModel.saveReading(savedReading)
                showReadingDialog = false
            }
        )
    }

    // ==========================================
    // DELETE CONFIRMATION DIALOG
    // ==========================================
    readingToDelete?.let { r ->
        AlertDialog(
            onDismissRequest = { readingToDelete = null },
            title = {
                Text("Delete Reading?", color = SafetyRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Are you sure you want to delete the reading for '${r.locationDescription}' (${r.shiftPeriod})?\nWBGT: ${r.wbgtIndex?.let { "%.1f°C".format(it) } ?: "N/M"}, Noise: ${r.noiseDba?.let { "%.1f dBA".format(it) } ?: "N/M"}.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReading(r)
                        readingToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                    modifier = Modifier.testTag("confirm_delete_reading_btn")
                ) {
                    Text("Delete Reading", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { readingToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

@Composable
fun EnvironmentalReadingCard(
    reading: EnvironmentalReadingEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .testTag("reading_card_${reading.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reading.locationDescription,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${reading.shiftPeriod} • ${reading.wbgtMonitorModel}",
                        color = SafetyAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp).testTag("edit_reading_${reading.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Reading", tint = SafetyAmber, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("delete_reading_${reading.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Reading", tint = SafetyRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Thermal Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip("Wet Bulb", reading.wetBulbTemp?.let { "%.1f°C".format(it) } ?: "N/M", SafetyBlue, Modifier.weight(1f))
                MetricChip("Dry Bulb", reading.dryBulbTemp?.let { "%.1f°C".format(it) } ?: "N/M", SafetyAmber, Modifier.weight(1f))
                MetricChip("Globe", reading.globeTemp?.let { "%.1f°C".format(it) } ?: "N/M", SafetyOrange, Modifier.weight(1f))
                MetricChip("WBGT", reading.wbgtIndex?.let { "%.1f°C".format(it) } ?: "N/M", SafetyRed, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Additional parameters: Humidity, Wind, Lux, Noise
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip("Humidity", reading.relativeHumidity?.let { "%.0f%%".format(it) } ?: "N/M", TextSecondary, Modifier.weight(1f))
                MetricChip("Wind", reading.windSpeed?.let { "%.1f m/s".format(it) } ?: "N/M", TextSecondary, Modifier.weight(1f))
                MetricChip("Lux", reading.luxReading?.let { "%.0f lx".format(it) } ?: "N/M", TextSecondary, Modifier.weight(1f))
                MetricChip("Noise", reading.noiseDba?.let { "%.1f dBA".format(it) } ?: "N/M", if ((reading.noiseDba ?: 0.0) >= 85.0) SafetyRed else SafetyGreen, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MetricChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Charcoal800,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.border(1.dp, BorderDark, RoundedCornerShape(6.dp))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, color = TextMuted, fontSize = 9.sp)
            Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReadingEditDialog(
    initialReading: EnvironmentalReadingEntity?,
    onDismiss: () -> Unit,
    onSave: (EnvironmentalReadingEntity) -> Unit
) {
    val isEdit = initialReading != null
    var locationDesc by remember { mutableStateOf(initialReading?.locationDescription ?: "") }
    var shiftPeriod by remember { mutableStateOf(initialReading?.shiftPeriod ?: "Start of shift") }
    var wetBulb by remember { mutableStateOf(initialReading?.wetBulbTemp?.toString() ?: "24.5") }
    var dryBulb by remember { mutableStateOf(initialReading?.dryBulbTemp?.toString() ?: "29.8") }
    var globeTemp by remember { mutableStateOf(initialReading?.globeTemp?.toString() ?: "32.1") }
    var humidity by remember { mutableStateOf(initialReading?.relativeHumidity?.toString() ?: "55.0") }
    var windSpeed by remember { mutableStateOf(initialReading?.windSpeed?.toString() ?: "1.4") }
    var lux by remember { mutableStateOf(initialReading?.luxReading?.toString() ?: "210.0") }
    var noise by remember { mutableStateOf(initialReading?.noiseDba?.toString() ?: "83.5") }
    var wbgtModel by remember { mutableStateOf(initialReading?.wbgtMonitorModel ?: "WBGT Logger AZ 87786") }
    var anemometerModel by remember { mutableStateOf(initialReading?.anemometerModel ?: "Kestrel Pocket Weather 4000") }

    val shifts = listOf("Start of shift", "Mid-shift", "End of shift", "Night shift")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Modify Environmental Reading" else "Log Field Measurement",
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
                OutlinedTextField(
                    value = locationDesc,
                    onValueChange = { locationDesc = it },
                    label = { Text("Location / Workplace *") },
                    placeholder = { Text("e.g. Kuipersbult Quarry Primary Crusher") },
                    modifier = Modifier.fillMaxWidth().testTag("reading_loc_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyAmber,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Text("Shift Period", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    shifts.forEach { shift ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (shiftPeriod == shift) SafetyAmber.copy(alpha = 0.2f) else Charcoal800,
                            border = BorderStroke(1.dp, if (shiftPeriod == shift) SafetyAmber else BorderDark),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { shiftPeriod = shift }
                        ) {
                            Text(
                                text = shift,
                                color = if (shiftPeriod == shift) SafetyAmber else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (shiftPeriod == shift) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Text("Thermal Stress Parameters (°C)", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = wetBulb,
                        onValueChange = { wetBulb = it },
                        label = { Text("Wet Bulb (°C)") },
                        modifier = Modifier.weight(1f).testTag("reading_wb_input")
                    )
                    OutlinedTextField(
                        value = dryBulb,
                        onValueChange = { dryBulb = it },
                        label = { Text("Dry Bulb (°C)") },
                        modifier = Modifier.weight(1f).testTag("reading_db_input")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = globeTemp,
                        onValueChange = { globeTemp = it },
                        label = { Text("Globe (°C)") },
                        modifier = Modifier.weight(1f).testTag("reading_globe_input")
                    )
                    OutlinedTextField(
                        value = humidity,
                        onValueChange = { humidity = it },
                        label = { Text("Humidity (%)") },
                        modifier = Modifier.weight(1f).testTag("reading_rh_input")
                    )
                }

                Text("Aero & Physical Environment", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = windSpeed,
                        onValueChange = { windSpeed = it },
                        label = { Text("Wind Speed (m/s)") },
                        modifier = Modifier.weight(1f).testTag("reading_wind_input")
                    )
                    OutlinedTextField(
                        value = lux,
                        onValueChange = { lux = it },
                        label = { Text("Lux (Illum)") },
                        modifier = Modifier.weight(1f).testTag("reading_lux_input")
                    )
                }

                OutlinedTextField(
                    value = noise,
                    onValueChange = { noise = it },
                    label = { Text("Noise Level (dBA - OEL 85 dBA)") },
                    modifier = Modifier.fillMaxWidth().testTag("reading_noise_input")
                )

                Text("Instrument Calibration Models", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = wbgtModel,
                    onValueChange = { wbgtModel = it },
                    label = { Text("WBGT Monitor Serial/Model") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = anemometerModel,
                    onValueChange = { anemometerModel = it },
                    label = { Text("Anemometer Model") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val wb = wetBulb.toDoubleOrNull()
                    val db = dryBulb.toDoubleOrNull()
                    val gt = globeTemp.toDoubleOrNull()
                    // Outdoor WBGT Formula: 0.7*WB + 0.2*GT + 0.1*DB
                    val calculatedWbgt = if (wb != null && gt != null && db != null) {
                        (0.7 * wb + 0.2 * gt + 0.1 * db)
                    } else null

                    val readingId = initialReading?.id ?: 0L
                    val assessId = initialReading?.assessmentId ?: "MANUAL-LOG"

                    onSave(
                        EnvironmentalReadingEntity(
                            id = readingId,
                            assessmentId = assessId,
                            shiftPeriod = shiftPeriod,
                            locationDescription = locationDesc.trim().ifEmpty { "General Mining Surface" },
                            wetBulbTemp = wb,
                            dryBulbTemp = db,
                            globeTemp = gt,
                            wbgtIndex = calculatedWbgt,
                            relativeHumidity = humidity.toDoubleOrNull(),
                            windSpeed = windSpeed.toDoubleOrNull(),
                            luxReading = lux.toDoubleOrNull(),
                            noiseDba = noise.toDoubleOrNull(),
                            wbgtMonitorModel = wbgtModel.trim().ifEmpty { "WBGT Logger AZ 87786" },
                            anemometerModel = anemometerModel.trim().ifEmpty { "Kestrel Pocket Weather 4000" }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                modifier = Modifier.testTag("confirm_save_reading_btn")
            ) {
                Text(if (isEdit) "Update Reading" else "Save Reading", color = Color.White, fontWeight = FontWeight.Bold)
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
