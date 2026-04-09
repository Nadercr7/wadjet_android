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
import androidx.navigation.toRoute
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.feature.auth.screen.WelcomeScreen
import com.wadjet.feature.dictionary.screen.DictionaryScreen
import com.wadjet.feature.dictionary.screen.LessonScreen
import com.wadjet.feature.dictionary.LessonViewModel
import com.wadjet.feature.landing.screen.LandingScreen
import com.wadjet.feature.scan.HistoryViewModel
import com.wadjet.feature.scan.ScanViewModel
import com.wadjet.feature.scan.screen.ScanHistoryScreen
import com.wadjet.feature.scan.screen.ScanResultScreen
import com.wadjet.feature.scan.screen.ScanScreen
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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

        // Scan
        composable<Route.Scan> {
            val viewModel: ScanViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            if (state.result != null) {
                ScanResultScreen(
                    result = state.result!!,
                    onScanAgain = { viewModel.resetScan() },
                    onBack = { navController.popBackStack() },
                )
            } else {
                ScanScreen(
                    state = state,
                    onImageCaptured = { viewModel.onImageCaptured(it) },
                    onImageSelected = { viewModel.onImageSelected(it) },
                    onNavigateToHistory = { navController.navigate(Route.ScanHistory) },
                    onBack = { navController.popBackStack() },
                )
            }
        }

        composable<Route.ScanHistory> {
            val viewModel: HistoryViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            ScanHistoryScreen(
                state = state,
                onScanTap = { /* TODO: load cached result */ },
                onDelete = { viewModel.deleteScan(it) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.Dictionary> {
            DictionaryScreen(
                onNavigateToLesson = { level -> navController.navigate(Route.Lesson(level)) },
            )
        }

        composable<Route.Lesson> {
            val viewModel: LessonViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            LessonScreen(
                state = state,
                onSelectAnswer = viewModel::selectAnswer,
                onRevealAnswer = viewModel::revealAnswer,
                onNextExercise = viewModel::nextExercise,
                onRetry = viewModel::retry,
                onBack = { navController.popBackStack() },
            )
        }
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
