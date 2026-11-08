package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.CustomerEntity
import com.example.data.model.FacilityEntity
import com.example.data.model.MineSiteEntity
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.WfhViewModel

sealed class Screen {
    object Dashboard : Screen()
    object CustomersAndMines : Screen()
    object RemedialActions : Screen()
    object EnvironmentalReadings : Screen()
    object DmrAnnexure : Screen()
    data class NewAssessmentWizard(
        val customer: CustomerEntity? = null,
        val site: MineSiteEntity? = null,
        val facility: FacilityEntity? = null
    ) : Screen()
    data class AssessmentDetail(val assessmentId: String) : Screen()
    data class ReportPreview(val assessmentId: String) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WFHTheme {
                MainAppNavHost()
            }
        }
    }
}

@Composable
fun MainAppNavHost(viewModel: WfhViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    // BackHandler for sub-screens
    if (currentScreen !is Screen.Dashboard) {
        BackHandler {
            currentScreen = Screen.Dashboard
        }
    }

    val isTopLevelScreen = currentScreen is Screen.Dashboard ||
            currentScreen is Screen.CustomersAndMines ||
            currentScreen is Screen.RemedialActions ||
            currentScreen is Screen.EnvironmentalReadings ||
            currentScreen is Screen.DmrAnnexure

    Scaffold(
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar(
                    containerColor = Charcoal800,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .testTag("wfh_bottom_navigation")
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Dashboard,
                        onClick = { currentScreen = Screen.Dashboard },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Charcoal900,
                            selectedTextColor = SafetyAmber,
                            indicatorColor = SafetyAmber,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.CustomersAndMines,
                        onClick = { currentScreen = Screen.CustomersAndMines },
                        icon = { Icon(Icons.Default.Terrain, contentDescription = "Sites") },
                        label = { Text("Mines", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Charcoal900,
                            selectedTextColor = SafetyAmber,
                            indicatorColor = SafetyAmber,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_mines")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.RemedialActions,
                        onClick = { currentScreen = Screen.RemedialActions },
                        icon = { Icon(Icons.Default.Warning, contentDescription = "Remedial") },
                        label = { Text("Remedial", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Charcoal900,
                            selectedTextColor = SafetyAmber,
                            indicatorColor = SafetyAmber,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_remedial")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.EnvironmentalReadings,
                        onClick = { currentScreen = Screen.EnvironmentalReadings },
                        icon = { Icon(Icons.Default.DeviceThermostat, contentDescription = "Readings") },
                        label = { Text("Readings", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Charcoal900,
                            selectedTextColor = SafetyAmber,
                            indicatorColor = SafetyAmber,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_readings")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.DmrAnnexure,
                        onClick = { currentScreen = Screen.DmrAnnexure },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "DMPR") },
                        label = { Text("DMPR", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Charcoal900,
                            selectedTextColor = SafetyAmber,
                            indicatorColor = SafetyAmber,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_dmpr")
                    )
                }
            }
        },
        containerColor = Charcoal900
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onStartNewAssessment = { currentScreen = Screen.NewAssessmentWizard() },
                        onViewAssessment = { assessmentId ->
                            currentScreen = Screen.AssessmentDetail(assessmentId)
                        },
                        onNavigateToCustomers = { currentScreen = Screen.CustomersAndMines },
                        onNavigateToMines = { currentScreen = Screen.CustomersAndMines },
                        onNavigateToRemedial = { currentScreen = Screen.RemedialActions },
                        onNavigateToReadings = { currentScreen = Screen.EnvironmentalReadings },
                        onNavigateToDmrAnnexure = { currentScreen = Screen.DmrAnnexure }
                    )
                }
                is Screen.CustomersAndMines -> {
                    CustomersAndMinesScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.Dashboard },
                        onStartAssessmentForFacility = { cust, site, fac ->
                            currentScreen = Screen.NewAssessmentWizard(cust, site, fac)
                        }
                    )
                }
                is Screen.RemedialActions -> {
                    RemedialActionsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.Dashboard }
                    )
                }
                is Screen.EnvironmentalReadings -> {
                    EnvironmentalReadingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.Dashboard }
                    )
                }
                is Screen.DmrAnnexure -> {
                    DmrAnnexureScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.Dashboard }
                    )
                }
                is Screen.NewAssessmentWizard -> {
                    NewAssessmentWizardScreen(
                        viewModel = viewModel,
                        preselectedCustomer = screen.customer,
                        preselectedSite = screen.site,
                        preselectedFacility = screen.facility,
                        onNavigateBack = { currentScreen = Screen.Dashboard },
                        onAssessmentCreated = { newId ->
                            currentScreen = Screen.AssessmentDetail(newId)
                        }
                    )
                }
                is Screen.AssessmentDetail -> {
                    AssessmentDetailScreen(
                        assessmentId = screen.assessmentId,
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.Dashboard },
                        onViewReport = { id ->
                            currentScreen = Screen.ReportPreview(id)
                        }
                    )
                }
                is Screen.ReportPreview -> {
                    ReportPreviewScreen(
                        assessmentId = screen.assessmentId,
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentScreen = Screen.AssessmentDetail(screen.assessmentId)
                        }
                    )
                }
            }
        }
    }
}
