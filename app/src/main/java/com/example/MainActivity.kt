package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.GlassTheme
import com.example.ui.components.GradientBackground
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Retrieve repository and set up VM
            val app = application as ExpenseTrackerApplication
            val factory = ExpenseViewModelFactory(app, app.repository)
            val viewModel: ExpenseViewModel = viewModel(factory = factory)

            val isDarkThemePref by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkThemePref) {
                GradientBackground(
                    isDark = isDarkThemePref,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

                    Scaffold(
                        containerColor = Color.Transparent,
                        bottomBar = {
                            // Only show the floating bottom bar on primary tabs
                            val primaryTabs = listOf("dashboard", "manager", "analytics", "calendar", "settings")
                            if (currentRoute in primaryTabs) {
                                FloatingBottomBar(
                                    currentRoute = currentRoute,
                                    onTabSelected = { route ->
                                        if (currentRoute != route) {
                                            navController.navigate(route) {
                                                popUpTo("dashboard") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToQuickEntry = { navController.navigate("quick_entry") },
                                    onNavigateToManager = { navController.navigate("manager") },
                                    onNavigateToAnalytics = { navController.navigate("analytics") }
                                )
                            }

                            composable("quick_entry") {
                                QuickEntryScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.navigateUp() }
                                )
                            }

                            composable("manager") {
                                ExpenseManagerScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.navigate("dashboard") }
                                )
                            }

                            composable("analytics") {
                                AnalyticsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.navigate("dashboard") }
                                )
                            }

                            composable("budget") {
                                BudgetScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.navigate("settings") }
                                )
                            }

                            composable("calendar") {
                                CalendarScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.navigate("dashboard") }
                                )
                            }

                            composable("settings") {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.navigate("dashboard") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class BottomBarItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun FloatingBottomBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit
) {
    val items = listOf(
        BottomBarItem("dashboard", "Home", Icons.Default.Home),
        BottomBarItem("manager", "Manager", Icons.Default.ListAlt),
        BottomBarItem("analytics", "Insight", Icons.Default.InsertChartOutlined),
        BottomBarItem("calendar", "Calendar", Icons.Default.CalendarToday),
        BottomBarItem("settings", "Settings", Icons.Default.Settings)
    )

    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating glassmorphic tab container
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(
                    if (isDark) Color(0x1AFFFFFF) else Color(0xE6FFFFFF)
                )
                .border(
                    width = 1.dp,
                    brush = GlassTheme.glassBorderColor(isDark),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .fillMaxWidth(0.95f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isActive = currentRoute == item.route
                val accentColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (isActive) Color(0x226366F1) else Color.Transparent
                        )
                        .clickable { onTabSelected(item.route) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (isActive) accentColor else GlassTheme.glassSubTextColor(isDark),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        color = if (isActive) accentColor else GlassTheme.glassSubTextColor(isDark),
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
