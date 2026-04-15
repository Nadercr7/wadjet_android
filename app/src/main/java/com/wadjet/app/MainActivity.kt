package com.wadjet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wadjet.app.navigation.Route
import com.wadjet.app.navigation.TopLevelDestination
import com.wadjet.app.navigation.WadjetNavGraph
import com.wadjet.core.common.network.NetworkMonitor
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.WadjetTheme
import com.wadjet.core.designsystem.component.OfflineIndicator
import com.wadjet.core.domain.repository.AuthRepository
import com.wadjet.core.common.ToastController
import com.wadjet.core.common.ToastType
import com.wadjet.core.designsystem.component.ToastState
import com.wadjet.core.designsystem.component.ToastVariant
import com.wadjet.core.designsystem.component.WadjetToast
import com.wadjet.core.designsystem.R as DesignR
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject @Named("webClientId") lateinit var webClientId: String
    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var toastController: ToastController

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            WadjetTheme {
                WadjetApp(
                    isLoggedIn = authRepository.isLoggedIn,
                    webClientId = webClientId,
                    networkMonitor = networkMonitor,
                    toastController = toastController,
                    widthSizeClass = windowSizeClass.widthSizeClass,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WadjetApp(
    isLoggedIn: Boolean,
    webClientId: String,
    networkMonitor: NetworkMonitor,
    toastController: ToastController,
    widthSizeClass: WindowWidthSizeClass,
) {
    var currentToast by remember { mutableStateOf<ToastState?>(null) }
    LaunchedEffect(Unit) {
        toastController.events.collect { event ->
            currentToast = ToastState(
                message = event.message,
                variant = when (event.type) {
                    ToastType.Success -> ToastVariant.Success
                    ToastType.Error -> ToastVariant.Error
                    ToastType.Info -> ToastVariant.Info
                },
                durationMs = event.durationMs,
            )
        }
    }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isOffline by networkMonitor.isOnline.collectAsStateWithLifecycle(initialValue = true)
    var showQuickSettings by remember { mutableStateOf(false) }

    // Determine start destination based on auth state
    val startDestination: Route = if (isLoggedIn) Route.Landing else Route.Welcome

    // Show navigation only on top-level destinations
    val showNav = TopLevelDestination.entries.any { dest ->
        currentDestination?.hasRoute(dest.route::class) == true
    }

    // Adapt layout based on window size class
    val layoutType = if (!showNav) {
        NavigationSuiteType.None
    } else when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> NavigationSuiteType.NavigationBar
        WindowWidthSizeClass.Medium -> NavigationSuiteType.NavigationRail
        else -> NavigationSuiteType.NavigationDrawer
    }

    // Quick-settings dialog
    if (showQuickSettings) {
        com.wadjet.feature.settings.screen.SettingsQuickDialog(
            onDismiss = { showQuickSettings = false },
            onOpenFullSettings = {
                showQuickSettings = false
                navController.navigate(Route.Settings) { launchSingleTop = true }
            },
        )
    }

    // Pre-compute composable colors outside the builder DSL
    val navBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = WadjetColors.Gold,
        selectedTextColor = WadjetColors.Gold,
        unselectedIconColor = WadjetColors.TextMuted,
        unselectedTextColor = WadjetColors.TextMuted,
        indicatorColor = WadjetColors.Gold.copy(alpha = 0.12f),
    )
    val navRailItemColors = NavigationRailItemDefaults.colors(
        selectedIconColor = WadjetColors.Gold,
        selectedTextColor = WadjetColors.Gold,
        unselectedIconColor = WadjetColors.TextMuted,
        unselectedTextColor = WadjetColors.TextMuted,
        indicatorColor = WadjetColors.Gold.copy(alpha = 0.12f),
    )
    val navItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = navBarItemColors,
        navigationRailItemColors = navRailItemColors,
    )

    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { dest ->
                val selected = currentDestination?.hasRoute(dest.route::class) == true
                item(
                    selected = selected,
                    onClick = {
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = stringResource(dest.labelRes),
                        )
                    },
                    label = { Text(stringResource(dest.labelRes)) },
                    colors = navItemColors,
                )
            }
        },
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = WadjetColors.Surface,
            navigationBarContentColor = WadjetColors.TextMuted,
            navigationRailContainerColor = WadjetColors.Surface,
            navigationRailContentColor = WadjetColors.TextMuted,
        ),
        containerColor = WadjetColors.Night,
    ) {
        Scaffold(
            containerColor = WadjetColors.Night,
            topBar = {
                if (showNav) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(DesignR.string.app_name_display),
                                style = MaterialTheme.typography.titleLarge,
                                color = WadjetColors.Gold,
                            )
                        },
                        actions = {
                            IconButton(onClick = {
                                navController.navigate(Route.Dashboard) { launchSingleTop = true }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = stringResource(R.string.top_bar_profile),
                                    tint = WadjetColors.Gold,
                                )
                            }
                            IconButton(onClick = { showQuickSettings = true }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.top_bar_settings),
                                    tint = WadjetColors.TextMuted,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = WadjetColors.Surface,
                        ),
                    )
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Column {
                    OfflineIndicator(isOffline = !isOffline)

                    WadjetNavGraph(
                        navController = navController,
                        startDestination = startDestination,
                        webClientId = webClientId,
                        toastController = toastController,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Global toast overlay
                WadjetToast(
                    toast = currentToast,
                    onDismiss = { currentToast = null },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}