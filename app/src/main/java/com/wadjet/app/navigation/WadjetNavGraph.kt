package com.wadjet.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.feature.auth.screen.WelcomeScreen
import com.wadjet.feature.landing.screen.LandingScreen

@Composable
fun WadjetNavGraph(
    navController: NavHostController,
    startDestination: Route,
    webClientId: String,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<Route.Splash> {
            // Splash handled by SplashViewModel in MainActivity — this is a fallback
        }

        composable<Route.Welcome> {
            WelcomeScreen(
                webClientId = webClientId,
                onAuthSuccess = {
                    navController.navigate(Route.Landing) {
                        popUpTo<Route.Welcome> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.Landing> {
            LandingScreen(
                onNavigateToScan = { navController.navigate(Route.Scan) },
                onNavigateToExplore = { navController.navigate(Route.Explore) },
                onNavigateToDictionary = { navController.navigate(Route.Dictionary) },
                onNavigateToStories = { navController.navigate(Route.Stories) },
            )
        }

        // Placeholder destinations — implemented in later phases
        composable<Route.Scan> { PlaceholderScreen("Scan") }
        composable<Route.ScanHistory> { PlaceholderScreen("Scan History") }
        composable<Route.Dictionary> { PlaceholderScreen("Dictionary") }
        composable<Route.Explore> { PlaceholderScreen("Explore") }
        composable<Route.Chat> { PlaceholderScreen("Thoth Chat") }
        composable<Route.Stories> { PlaceholderScreen("Stories") }
        composable<Route.Dashboard> { PlaceholderScreen("Dashboard") }
        composable<Route.Settings> { PlaceholderScreen("Settings") }
        composable<Route.Feedback> { PlaceholderScreen("Feedback") }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name,
            color = WadjetColors.Gold,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
