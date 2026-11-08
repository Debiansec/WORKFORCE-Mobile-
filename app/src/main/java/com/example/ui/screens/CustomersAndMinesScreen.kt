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
import com.example.data.model.CustomerEntity
import com.example.data.model.FacilityEntity
import com.example.data.model.MineSiteEntity
import com.example.ui.components.WfhTopBar
import com.example.ui.theme.*
import com.example.viewmodel.WfhViewModel
import java.util.UUID

@Composable
fun CustomersAndMinesScreen(
    viewModel: WfhViewModel,
    onNavigateBack: () -> Unit,
    onStartAssessmentForFacility: (customer: CustomerEntity, site: MineSiteEntity, facility: FacilityEntity) -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val mineSites by viewModel.mineSites.collectAsState()
    val facilities by viewModel.facilities.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0 = Customers, 1 = Mines & Facilities
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }

    // Dialog States
    var showCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    var showSiteDialog by remember { mutableStateOf(false) }
    var siteToEdit by remember { mutableStateOf<MineSiteEntity?>(null) }
    var siteToDelete by remember { mutableStateOf<MineSiteEntity?>(null) }
    var presetCustomerIdForNewSite by remember { mutableStateOf<String?>(null) }

    var showFacilityDialog by remember { mutableStateOf(false) }
    var siteForNewFacility by remember { mutableStateOf<MineSiteEntity?>(null) }
    var facilityToDelete by remember { mutableStateOf<FacilityEntity?>(null) }

    Scaffold(
        topBar = {
            WfhTopBar(
                title = "Clients & Mine Locations",
                subtitle = "Mining Portfolios, Sites & Facilities Registry",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (selectedTabIndex == 0) {
                        IconButton(
                            onClick = {
                                customerToEdit = null
                                showCustomerDialog = true
                            },
                            modifier = Modifier.testTag("add_client_top_btn")
                        ) {
                            Icon(Icons.Default.AddBusiness, contentDescription = "Add Client", tint = SafetyAmber)
                        }
                    } else {
                        IconButton(
                            onClick = {
                                siteToEdit = null
                                presetCustomerIdForNewSite = customers.firstOrNull()?.id
                                showSiteDialog = true
                            },
                            modifier = Modifier.testTag("add_mine_site_top_btn")
                        ) {
                            Icon(Icons.Default.AddLocationAlt, contentDescription = "Add Mine Site", tint = SafetyAmber)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTabIndex == 0) {
                        customerToEdit = null
                        showCustomerDialog = true
                    } else {
                        siteToEdit = null
                        presetCustomerIdForNewSite = customers.firstOrNull()?.id
                        showSiteDialog = true
                    }
                },
                containerColor = SafetyRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_client_or_site")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (selectedTabIndex == 0) Icons.Default.AddBusiness else Icons.Default.AddLocationAlt,
                        contentDescription = null
                    )
                    Text(
                        text = if (selectedTabIndex == 0) "New Client" else "New Mine Site",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
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
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Charcoal800,
                contentColor = SafetyAmber
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "Assigned Clients (${customers.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 0) SafetyAmber else TextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_clients")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "Mines & Facilities (${mineSites.size} Sites, ${facilities.size} Fac)",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 1) SafetyAmber else TextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_mines_facilities")
                )
            }

            if (selectedTabIndex == 0) {
                // Customer Directory
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Surface(
                            color = Charcoal800,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("STATUTORY CLIENT REGISTRATION", color = SafetyAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Mine Health & Safety Act (29 of 1996) Appointed Representatives", color = TextSecondary, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        customerToEdit = null
                                        showCustomerDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp).testTag("quick_add_client_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Client", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    items(customers) { customer ->
                        val customerSites = mineSites.filter { it.customerId == customer.id }
                        CustomerCard(
                            customer = customer,
                            siteCount = customerSites.size,
                            isSelected = selectedCustomer?.id == customer.id,
                            onClick = {
                                selectedCustomer = if (selectedCustomer?.id == customer.id) null else customer
                            },
                            onEdit = {
                                customerToEdit = customer
                                showCustomerDialog = true
                            },
                            onDelete = {
                                customerToDelete = customer
                            },
                            onAddSite = {
                                siteToEdit = null
                                presetCustomerIdForNewSite = customer.id
                                showSiteDialog = true
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            } else {
                // Mine Sites and Facilities
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Surface(
                            color = Charcoal800,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("MINE SITES & WORKPLACES", color = SafetyAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Surface & Underground Inspection Locations & Workstations", color = TextSecondary, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        siteToEdit = null
                                        presetCustomerIdForNewSite = customers.firstOrNull()?.id
                                        showSiteDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp).testTag("quick_add_site_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Site", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    items(mineSites) { site ->
                        val siteFacilities = facilities.filter { it.siteId == site.id }
                        val client = customers.find { it.id == site.customerId }
                        MineSiteCard(
                            site = site,
                            clientName = client?.name ?: "Client (${site.customerId})",
                            facilities = siteFacilities,
                            onFacilitySelect = { facility ->
                                if (client != null) {
                                    onStartAssessmentForFacility(client, site, facility)
                                }
                            },
                            onEditSite = {
                                siteToEdit = site
                                showSiteDialog = true
                            },
                            onDeleteSite = {
                                siteToDelete = site
                            },
                            onAddFacility = {
                                siteForNewFacility = site
                                showFacilityDialog = true
                            },
                            onDeleteFacility = { facility ->
                                facilityToDelete = facility
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOG 1: CREATE / EDIT CUSTOMER
    // ==========================================
    if (showCustomerDialog) {
        CustomerEditDialog(
            initialCustomer = customerToEdit,
            onDismiss = { showCustomerDialog = false },
            onSave = { savedCustomer ->
                viewModel.saveCustomer(savedCustomer)
                showCustomerDialog = false
            }
        )
    }

    // ==========================================
    // DIALOG 2: CONFIRM DELETE CUSTOMER
    // ==========================================
    customerToDelete?.let { cust ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = {
                Text("Delete Mining Client?", color = SafetyRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Are you sure you want to permanently delete '${cust.name}' (DME Code: ${cust.dmeMineCode})?\n\nThis will also cascade delete all affiliated mine sites, facilities, and inspection data.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(cust)
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                    modifier = Modifier.testTag("confirm_delete_client_btn")
                ) {
                    Text("Delete Client", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // ==========================================
    // DIALOG 3: CREATE / EDIT MINE SITE
    // ==========================================
    if (showSiteDialog) {
        MineSiteEditDialog(
            initialSite = siteToEdit,
            customers = customers,
            preselectedCustomerId = presetCustomerIdForNewSite ?: customers.firstOrNull()?.id ?: "",
            onDismiss = { showSiteDialog = false },
            onSave = { savedSite ->
                viewModel.saveMineSite(savedSite)
                showSiteDialog = false
            }
        )
    }

    // ==========================================
    // DIALOG 4: CONFIRM DELETE MINE SITE
    // ==========================================
    siteToDelete?.let { site ->
        AlertDialog(
            onDismissRequest = { siteToDelete = null },
            title = {
                Text("Delete Mine Site?", color = SafetyRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Are you sure you want to delete '${site.name}' (Code: ${site.siteCode})?\n\nAll associated facilities and workplace assessment records will be deleted.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMineSite(site)
                        siteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                    modifier = Modifier.testTag("confirm_delete_site_btn")
                ) {
                    Text("Delete Site", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { siteToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // ==========================================
    // DIALOG 5: CREATE NEW FACILITY
    // ==========================================
    if (showFacilityDialog && siteForNewFacility != null) {
        FacilityEditDialog(
            site = siteForNewFacility!!,
            onDismiss = {
                showFacilityDialog = false
                siteForNewFacility = null
            },
            onSave = { newFacility ->
                viewModel.saveFacility(newFacility)
                showFacilityDialog = false
                siteForNewFacility = null
            }
        )
    }

    // ==========================================
    // DIALOG 6: CONFIRM DELETE FACILITY
    // ==========================================
    facilityToDelete?.let { fac ->
        AlertDialog(
            onDismissRequest = { facilityToDelete = null },
            title = {
                Text("Delete Facility?", color = SafetyRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Are you sure you want to remove '${fac.name}' (${fac.type})?",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFacility(fac)
                        facilityToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { facilityToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

// ==========================================
// CUSTOMER CARD WITH EDIT & DELETE
// ==========================================
@Composable
fun CustomerCard(
    customer: CustomerEntity,
    siteCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddSite: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isSelected) SafetyAmber else BorderDark, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("customer_card_${customer.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "DME Code: ${customer.dmeMineCode} • ${customer.province}",
                        color = SafetyAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Charcoal700
                    ) {
                        Text(
                            text = "$siteCount Sites",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp).testTag("edit_client_${customer.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Client", tint = SafetyAmber, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("delete_client_${customer.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Client", tint = SafetyRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = customer.address, color = TextMuted, fontSize = 11.sp)
            }

            AnimatedVisibility(visible = isSelected) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(Charcoal800, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(text = "STATUTORY CONTACTS (MHSA 29 of 1996)", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Mine Manager: ${customer.mineManager} (${customer.managerEmail} / ${customer.managerPhone})", color = TextSecondary, fontSize = 11.sp)
                    Text(text = "• Appointed Hygienist: ${customer.appointedHygienist} (${customer.hygienistEmail} / ${customer.hygienistPhone})", color = TextSecondary, fontSize = 11.sp)
                    Text(text = "• Occupational Medical Practitioner (OMP): ${customer.medicalPractitioner} (${customer.medicalEmail} / ${customer.medicalPhone})", color = TextSecondary, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onAddSite,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyAmber),
                            border = BorderStroke(1.dp, SafetyAmber),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Mine Site for this Client", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// MINE SITE CARD WITH EDIT & DELETE
// ==========================================
@Composable
fun MineSiteCard(
    site: MineSiteEntity,
    clientName: String,
    facilities: List<FacilityEntity>,
    onFacilitySelect: (FacilityEntity) -> Unit,
    onEditSite: () -> Unit,
    onDeleteSite: () -> Unit,
    onAddFacility: () -> Unit,
    onDeleteFacility: (FacilityEntity) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .testTag("mine_site_card_${site.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = site.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$clientName • Site Code: ${site.siteCode}",
                        color = SafetyAmber,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (site.isUnderground) SafetyOrange.copy(alpha = 0.2f) else SafetyBlue.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (site.isUnderground) "Underground" else "Surface",
                            color = if (site.isUnderground) SafetyOrange else SafetyBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onEditSite,
                        modifier = Modifier.size(36.dp).testTag("edit_site_${site.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Site", tint = SafetyAmber, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onDeleteSite,
                        modifier = Modifier.size(36.dp).testTag("delete_site_${site.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Site", tint = SafetyRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GpsFixed, contentDescription = "GPS", tint = SafetyGreen, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "GPS: %.5f, %.5f (Elev: %.0fm) • Commodity: %s".format(site.latitude, site.longitude, site.elevationMeters, site.commodityCode),
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FACILITIES & WORKSTATIONS (${facilities.size})",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = onAddFacility,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("+ Facility", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            facilities.forEach { facility ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Charcoal800,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = facility.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${facility.type} • Cap: ~${facility.approxUserCount} workers • ${facility.ventilationDistrict}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { onFacilitySelect(facility) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp).testTag("inspect_facility_${facility.id}")
                            ) {
                                Text("Inspect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { onDeleteFacility(facility) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete facility", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// EDIT / CREATE CUSTOMER DIALOG
// ==========================================
@Composable
fun CustomerEditDialog(
    initialCustomer: CustomerEntity?,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit
) {
    val isEdit = initialCustomer != null
    var name by remember { mutableStateOf(initialCustomer?.name ?: "") }
    var dmeCode by remember { mutableStateOf(initialCustomer?.dmeMineCode ?: "") }
    var province by remember { mutableStateOf(initialCustomer?.province ?: "Limpopo Province") }
    var address by remember { mutableStateOf(initialCustomer?.address ?: "") }
    var mineManager by remember { mutableStateOf(initialCustomer?.mineManager ?: "") }
    var managerEmail by remember { mutableStateOf(initialCustomer?.managerEmail ?: "") }
    var managerPhone by remember { mutableStateOf(initialCustomer?.managerPhone ?: "") }
    var appointedHygienist by remember { mutableStateOf(initialCustomer?.appointedHygienist ?: "Mr N B Letlape") }
    var hygienistEmail by remember { mutableStateOf(initialCustomer?.hygienistEmail ?: "ChiefOH@workforcehealthsa.co.za") }
    var hygienistPhone by remember { mutableStateOf(initialCustomer?.hygienistPhone ?: "+27 (0)83 456 2673") }
    var medicalPractitioner by remember { mutableStateOf(initialCustomer?.medicalPractitioner ?: "Dr K O Sebopa") }
    var medicalEmail by remember { mutableStateOf(initialCustomer?.medicalEmail ?: "omp@workforcehealthsa.co.za") }
    var medicalPhone by remember { mutableStateOf(initialCustomer?.medicalPhone ?: "+27 (0)11 888 1234") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Edit Mining Client Details" else "Register New Mining Client",
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
                // Quick Preset for Afrimat
                if (!isEdit) {
                    OutlinedButton(
                        onClick = {
                            name = "Afrimat Ltd / Afrimat CM North"
                            dmeCode = "10192"
                            province = "Limpopo Province"
                            address = "Kuipersbult 511 LQ, Lephalale, Limpopo"
                            mineManager = "Peter Lekgeu"
                            managerEmail = "peter.lekgeu@afrimat.co.za"
                            managerPhone = "+27 (0)14 763 2189"
                            appointedHygienist = "Mr N B Letlape"
                            hygienistEmail = "ChiefOH@workforcehealthsa.co.za"
                            hygienistPhone = "+27 (0)83 456 2673"
                            medicalPractitioner = "Dr K O Sebopa"
                            medicalEmail = "sebopa.omp@afrimat.co.za"
                            medicalPhone = "+27 (0)14 763 9000"
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyAmber),
                        border = BorderStroke(1.dp, SafetyAmber),
                        modifier = Modifier.fillMaxWidth().testTag("preset_afrimat_btn")
                    ) {
                        Text("Pre-fill Afrimat Kuipersbult Details", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Client Company Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("client_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyAmber,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dmeCode,
                        onValueChange = { dmeCode = it },
                        label = { Text("DME Mine Code *") },
                        modifier = Modifier.weight(1f).testTag("client_dme_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SafetyAmber,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = province,
                        onValueChange = { province = it },
                        label = { Text("Province") },
                        modifier = Modifier.weight(1f).testTag("client_province_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SafetyAmber,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Physical Address / Farm Portion") },
                    modifier = Modifier.fillMaxWidth().testTag("client_address_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyAmber,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Text("Statutory Representatives (MHSA)", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = mineManager,
                    onValueChange = { mineManager = it },
                    label = { Text("Mine Manager (2.6.1 Appointee)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = managerEmail,
                        onValueChange = { managerEmail = it },
                        label = { Text("Manager Email") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = managerPhone,
                        onValueChange = { managerPhone = it },
                        label = { Text("Manager Phone") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = appointedHygienist,
                    onValueChange = { appointedHygienist = it },
                    label = { Text("Appointed Occupational Hygienist (12.1)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = medicalPractitioner,
                    onValueChange = { medicalPractitioner = it },
                    label = { Text("Occupational Medical Practitioner (OMP 13.1)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val id = initialCustomer?.id ?: "CUST-${System.currentTimeMillis() % 100000}"
                        onSave(
                            CustomerEntity(
                                id = id,
                                name = name.trim(),
                                dmeMineCode = dmeCode.trim().ifEmpty { "10000" },
                                province = province.trim().ifEmpty { "Limpopo" },
                                address = address.trim().ifEmpty { "South Africa" },
                                mineManager = mineManager.trim().ifEmpty { "Appointed Mine Manager" },
                                managerEmail = managerEmail.trim().ifEmpty { "manager@mine.co.za" },
                                managerPhone = managerPhone.trim().ifEmpty { "+27 (0)11 000 0000" },
                                appointedHygienist = appointedHygienist.trim().ifEmpty { "Mr N B Letlape" },
                                hygienistEmail = hygienistEmail.trim().ifEmpty { "ChiefOH@workforcehealthsa.co.za" },
                                hygienistPhone = hygienistPhone.trim().ifEmpty { "+27 (0)83 456 2673" },
                                medicalPractitioner = medicalPractitioner.trim().ifEmpty { "Dr K O Sebopa" },
                                medicalEmail = medicalEmail.trim().ifEmpty { "omp@workforcehealthsa.co.za" },
                                medicalPhone = medicalPhone.trim().ifEmpty { "+27 (0)11 888 1234" }
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                modifier = Modifier.testTag("save_client_btn")
            ) {
                Text(if (isEdit) "Update Client" else "Save Client", color = Color.White, fontWeight = FontWeight.Bold)
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

// ==========================================
// EDIT / CREATE MINE SITE DIALOG
// ==========================================
@Composable
fun MineSiteEditDialog(
    initialSite: MineSiteEntity?,
    customers: List<CustomerEntity>,
    preselectedCustomerId: String,
    onDismiss: () -> Unit,
    onSave: (MineSiteEntity) -> Unit
) {
    val isEdit = initialSite != null
    var selectedCustId by remember { mutableStateOf(initialSite?.customerId ?: preselectedCustomerId) }
    var name by remember { mutableStateOf(initialSite?.name ?: "") }
    var siteCode by remember { mutableStateOf(initialSite?.siteCode ?: "") }
    var latText by remember { mutableStateOf(initialSite?.latitude?.toString() ?: "-23.70760") }
    var lngText by remember { mutableStateOf(initialSite?.longitude?.toString() ?: "27.57939") }
    var elevationText by remember { mutableStateOf(initialSite?.elevationMeters?.toString() ?: "850.0") }
    var commodityCode by remember { mutableStateOf(initialSite?.commodityCode ?: "QD") }
    var isUnderground by remember { mutableStateOf(initialSite?.isUnderground ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Edit Mine Site / Quarry" else "Add New Mine Site",
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
                Text("Parent Client Organization", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                // Client Selector Chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    customers.forEach { cust ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedCustId == cust.id) SafetyAmber.copy(alpha = 0.2f) else Charcoal800,
                            border = BorderStroke(1.dp, if (selectedCustId == cust.id) SafetyAmber else BorderDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCustId = cust.id }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCustId == cust.id,
                                    onClick = { selectedCustId = cust.id },
                                    colors = RadioButtonDefaults.colors(selectedColor = SafetyAmber)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = cust.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Site / Shaft / Quarry Name *") },
                    placeholder = { Text("e.g. Kuipersbult Quarry & Crushing Plant") },
                    modifier = Modifier.fillMaxWidth().testTag("site_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyAmber,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = siteCode,
                        onValueChange = { siteCode = it },
                        label = { Text("Site Code *") },
                        placeholder = { Text("e.g. KUI-Q-01") },
                        modifier = Modifier.weight(1f).testTag("site_code_input")
                    )
                    OutlinedTextField(
                        value = commodityCode,
                        onValueChange = { commodityCode = it },
                        label = { Text("Commodity Code") },
                        placeholder = { Text("AU, QD, CL, CR") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Latitude (°S)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text("Longitude (°E)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = elevationText,
                    onValueChange = { elevationText = it },
                    label = { Text("Elevation (Meters above sea level)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Underground Operation", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = isUnderground,
                        onCheckedChange = { isUnderground = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SafetyOrange, checkedTrackColor = SafetyOrange.copy(alpha = 0.5f))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedCustId.isNotBlank()) {
                        val id = initialSite?.id ?: "SITE-${System.currentTimeMillis() % 100000}"
                        onSave(
                            MineSiteEntity(
                                id = id,
                                customerId = selectedCustId,
                                name = name.trim(),
                                siteCode = siteCode.trim().ifEmpty { "SITE-${id.takeLast(4)}" },
                                latitude = latText.toDoubleOrNull() ?: -23.70760,
                                longitude = lngText.toDoubleOrNull() ?: 27.57939,
                                elevationMeters = elevationText.toDoubleOrNull() ?: 850.0,
                                commodityCode = commodityCode.trim().ifEmpty { "QD" },
                                isUnderground = isUnderground
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                modifier = Modifier.testTag("save_site_btn")
            ) {
                Text(if (isEdit) "Update Site" else "Save Mine Site", color = Color.White, fontWeight = FontWeight.Bold)
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

// ==========================================
// CREATE FACILITY DIALOG
// ==========================================
@Composable
fun FacilityEditDialog(
    site: MineSiteEntity,
    onDismiss: () -> Unit,
    onSave: (FacilityEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Ablution - Male") }
    var ventDistrict by remember { mutableStateOf("Main Surface") }
    var capacityText by remember { mutableStateOf("50") }
    var disabledFriendly by remember { mutableStateOf(true) }

    val facilityTypes = listOf(
        "Ablution - Male",
        "Ablution - Female",
        "Ablution - Unisex",
        "Plant Workshop",
        "Smelter",
        "Underground Refuge Bay",
        "Change House & Lockers"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Facility to ${site.name}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Facility / Workplace Name *") },
                    placeholder = { Text("e.g. Primary Crushing Ablution") },
                    modifier = Modifier.fillMaxWidth().testTag("facility_name_input")
                )

                Text("Facility Type", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    facilityTypes.forEach { ft ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { type = ft }
                        ) {
                            RadioButton(
                                selected = type == ft,
                                onClick = { type = ft },
                                colors = RadioButtonDefaults.colors(selectedColor = SafetyAmber)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(ft, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = ventDistrict,
                    onValueChange = { ventDistrict = it },
                    label = { Text("Ventilation District / Section") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = capacityText,
                    onValueChange = { capacityText = it },
                    label = { Text("Approximate Worker Capacity") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val facId = "FAC-${System.currentTimeMillis() % 100000}"
                        onSave(
                            FacilityEntity(
                                id = facId,
                                siteId = site.id,
                                name = name.trim(),
                                type = type,
                                ventilationDistrict = ventDistrict.trim().ifEmpty { "Surface Zone" },
                                approxUserCount = capacityText.toIntOrNull() ?: 50,
                                disabledFriendly = disabledFriendly
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyRed)
            ) {
                Text("Save Facility", color = Color.White, fontWeight = FontWeight.Bold)
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
