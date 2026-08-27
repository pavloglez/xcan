package com.pavloglez.xcan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pavloglez.xcan.core.data.CarRepository
import com.pavloglez.xcan.core.ui.LocalHazeState
import com.pavloglez.xcan.core.ui.components.LocalActiveCarName
import com.pavloglez.xcan.core.ui.glassmorphism
import com.pavloglez.xcan.core.ui.theme.CharcoalSurface
import com.pavloglez.xcan.core.ui.theme.DeepCharcoal
import com.pavloglez.xcan.core.ui.theme.ElectricBlue
import com.pavloglez.xcan.core.ui.theme.LightGrayText
import com.pavloglez.xcan.core.ui.theme.XCanDuration
import com.pavloglez.xcan.core.ui.theme.XCanEasing
import com.pavloglez.xcan.core.ui.theme.XCanTheme
import com.pavloglez.xcan.feature.config.ConfigRoute as ConfigScreen
import com.pavloglez.xcan.feature.dashboard.DashboardRoute as DashboardScreen
import com.pavloglez.xcan.feature.diagnostics.DiagnosticsRoute as DiagnosticsScreen
import com.pavloglez.xcan.feature.maintenance.MaintenanceRoute as MaintenanceScreen
import com.pavloglez.xcan.feature.logging.LogSessionDetailRoute as LogSessionDetailScreen
import com.pavloglez.xcan.feature.logging.LogSessionsRoute as LogSessionsScreen
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeState
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var carRepository: CarRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XCanTheme {
                val navController = rememberNavController()
                val hazeState = remember { HazeState() }
                
                val activeCar by carRepository.getActiveCar().collectAsStateWithLifecycle(initialValue = null)

                CompositionLocalProvider(
                    LocalHazeState provides hazeState,
                    LocalActiveCarName provides activeCar?.name
                ) {
                    val topLevelRoutes = listOf(
                        Triple(DiagnosticsRoute::class, "Diagnostics", Icons.Filled.Warning),
                        Triple(MaintenanceRoute::class, "Maintenance", Icons.Filled.Build),
                        Triple(DashboardRoute::class, "Dashboard", Icons.Filled.Home),
                        Triple(LogSessionsRoute::class, "Logs", Icons.Filled.History),
                        Triple(ConfigRoute::class, "Config", Icons.Filled.Settings)
                    )

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = CharcoalSurface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Main content area - we only apply top padding so content flows under the bottom bar
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .padding(top = innerPadding.calculateTopPadding())
                            ) {
                                NavHost(
                                    navController = navController,
                                    startDestination = DashboardRoute,
                                    modifier = Modifier.fillMaxSize(),
                                    enterTransition = {
                                        fadeIn(tween(XCanDuration.Standard, easing = XCanEasing.EaseOut)) +
                                                scaleIn(initialScale = 0.95f, animationSpec = tween(XCanDuration.Standard, easing = XCanEasing.EaseOut))
                                    },
                                    exitTransition = {
                                        fadeOut(tween(XCanDuration.Standard, easing = XCanEasing.EaseOut))
                                    },
                                    popEnterTransition = {
                                        fadeIn(tween(XCanDuration.Standard, easing = XCanEasing.EaseOut)) +
                                                scaleIn(initialScale = 0.95f, animationSpec = tween(XCanDuration.Standard, easing = XCanEasing.EaseOut))
                                    },
                                    popExitTransition = {
                                        fadeOut(tween(XCanDuration.Standard, easing = XCanEasing.EaseOut))
                                    }
                                ) {
                                    composable<DiagnosticsRoute> { DiagnosticsScreen() }
                                    composable<MaintenanceRoute> { MaintenanceScreen() }
                                    composable<DashboardRoute> { DashboardScreen() }
                                    composable<ConfigRoute> { ConfigScreen() }
                                    composable<LogSessionsRoute> {
                                        LogSessionsScreen(
                                            onSessionClick = { sessionId ->
                                                navController.navigate(LogSessionDetailRoute(sessionId = sessionId))
                                            }
                                        )
                                    }
                                    composable<LogSessionDetailRoute> { backStack ->
                                        val route = backStack.toRoute<LogSessionDetailRoute>()
                                        LogSessionDetailScreen(sessionId = route.sessionId)
                                    }
                                }
                            }

                            // Floating Navigation Bar
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            // Hide bottom bar on details screens
                            val isDetail = currentDestination?.route?.contains("LogSessionDetailRoute") == true
                            
                            if (!isDetail) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                                        .padding(bottom = innerPadding.calculateBottomPadding())
                                        .fillMaxWidth()
                                        .glassmorphism(hazeState, shape = RoundedCornerShape(32.dp), backgroundColor = DeepCharcoal.copy(alpha = 0.5f))
                                    
                                ) {
                                    NavigationBar(
                                        containerColor = Color.Transparent,
                                        windowInsets = androidx.compose.foundation.layout.WindowInsets(0.dp),
                                        modifier = Modifier.fillMaxWidth().height(68.dp)//.clip(RoundedCornerShape(32.dp))
                                    ) {
                                        topLevelRoutes.forEach { (route, label, icon) ->
                                            NavigationBarItem(
                                                icon = {
                                                    val isSelected = currentDestination?.hierarchy?.any { it.route?.contains(route.simpleName ?: "") == true } == true
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = label,
                                                        modifier = androidx.compose.ui.Modifier.drawBehind {
                                                            if (isSelected) {
                                                                drawCircle(
                                                                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                                                        colors = listOf(ElectricBlue.copy(alpha = 0.5f), Color.Transparent),
                                                                        radius = size.width * 2f
                                                                    ),
                                                                    radius = size.width * 2f
                                                                )
                                                            }
                                                        }
                                                    )
                                                },
                                                label = { 
                                                    Text(
                                                        text = label, 
                                                        maxLines = 1, 
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                        fontSize = 10.sp
                                                    ) 
                                                },
                                                selected = currentDestination?.hierarchy?.any { it.route?.contains(route.simpleName ?: "") == true } == true,
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                                    selectedTextColor = ElectricBlue,
                                                    indicatorColor = Color.Transparent,
                                                    unselectedIconColor = LightGrayText,
                                                    unselectedTextColor = LightGrayText
                                                ),
                                                onClick = {
                                                    val instance = when (route) {
                                                        DiagnosticsRoute::class -> DiagnosticsRoute
                                                        MaintenanceRoute::class -> MaintenanceRoute
                                                        DashboardRoute::class -> DashboardRoute
                                                        LogSessionsRoute::class -> LogSessionsRoute
                                                        ConfigRoute::class -> ConfigRoute
                                                        else -> DashboardRoute
                                                    }
                                                    navController.navigate(instance) {
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
                            }
                        }
                    }
                }
            }
        }
    }
}
