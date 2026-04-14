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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject @Named("webClientId") lateinit var webClientId: String
    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var toastController: ToastController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WadjetTheme {
                WadjetApp(
                    isLoggedIn = authRepository.isLoggedIn,
                    webClientId = webClientId,
                    networkMonitor = networkMonitor,
                    toastController = toastController,
                )
            }
        }
    }
}

@Composable
private fun WadjetApp(
    isLoggedIn: Boolean,
    webClientId: String,
    networkMonitor: NetworkMonitor,
    toastController: ToastController,
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

    // Determine start destination based on auth state
    val startDestination: Route = if (isLoggedIn) Route.Landing else Route.Welcome

    // Show bottom bar only on top-level destinations
    val showBottomBar = TopLevelDestination.entries.any { dest ->
        currentDestination?.hasRoute(dest.route::class) == true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = WadjetColors.Night,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                WadjetBottomBar(
                    destinations = TopLevelDestination.entries,
                    currentDestination = currentDestination,
                    onNavigate = { dest ->
                        navController.navigate(dest.route) {
                            // Pop up to the start destination to avoid back stack buildup
                            popUpTo(startDestination) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
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

            // Floating avatar for Dashboard access on bottom-nav screens
            if (showBottomBar) {
                IconButton(
                    onClick = { navController.navigate(Route.Dashboard) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp)
                        .zIndex(1f)
                        .size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = WadjetColors.Surface.copy(alpha = 0.85f),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Dashboard",
                        tint = WadjetColors.Gold,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WadjetBottomBar(
    destinations: List<TopLevelDestination>,
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    NavigationBar(
        containerColor = WadjetColors.Surface,
        contentColor = WadjetColors.TextMuted,
    ) {
        destinations.forEach { dest ->
            val selected = currentDestination?.hasRoute(dest.route::class) == true
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(dest) },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label,
                    )
                },
                label = { Text(dest.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = WadjetColors.Gold,
                    selectedTextColor = WadjetColors.Gold,
                    unselectedIconColor = WadjetColors.TextMuted,
                    unselectedTextColor = WadjetColors.TextMuted,
                    indicatorColor = WadjetColors.Gold.copy(alpha = 0.12f),
                ),
            )
        }
    }
}