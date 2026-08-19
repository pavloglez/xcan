package com.jpdgbv.xcan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jpdgbv.xcan.core.ui.theme.CharcoalSurface
import com.jpdgbv.xcan.core.ui.theme.ElectricBlue
import com.jpdgbv.xcan.core.ui.theme.LightGrayText
import com.jpdgbv.xcan.core.ui.theme.XCanTheme
import com.jpdgbv.xcan.feature.config.ConfigRoute
import com.jpdgbv.xcan.feature.dashboard.DashboardRoute
import com.jpdgbv.xcan.feature.maintenance.MaintenanceRoute
import com.jpdgbv.xcan.feature.diagnostics.DiagnosticsRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XCanTheme {
                val navController = rememberNavController()

                val items = listOf(
                    Triple("dashboard", "Dashboard", Icons.Filled.Home),
                    Triple("diagnostics", "Diagnostics", Icons.Filled.Warning),
                    Triple("maintenance", "Maintenance", Icons.Filled.Build),
                    Triple("config", "Config", Icons.Filled.Settings)
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(containerColor = CharcoalSurface) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination

                            items.forEach { (route, label, icon) ->
                                NavigationBarItem(
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label) },
                                    selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CharcoalSurface,
                                        selectedTextColor = ElectricBlue,
                                        indicatorColor = ElectricBlue,
                                        unselectedIconColor = LightGrayText,
                                        unselectedTextColor = LightGrayText
                                    ),
                                    onClick = {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("dashboard") { DashboardRoute() }
                        composable("diagnostics") { DiagnosticsRoute() }
                        composable("maintenance") { MaintenanceRoute() }
                        composable("config") { ConfigRoute() }
                    }
                }
            }
        }
    }
}