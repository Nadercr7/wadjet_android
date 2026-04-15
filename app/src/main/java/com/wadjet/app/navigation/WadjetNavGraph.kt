package com.wadjet.app.navigation

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.wadjet.app.screen.HieroglyphsHubScreen
import com.wadjet.app.screen.HieroglyphsHubViewModel
import com.wadjet.feature.auth.screen.WelcomeScreen
import com.wadjet.feature.chat.ChatViewModel
import com.wadjet.feature.chat.screen.ChatScreen
import com.wadjet.feature.dashboard.DashboardViewModel
import com.wadjet.feature.dashboard.screen.DashboardScreen
import com.wadjet.feature.dictionary.screen.DictionaryScreen
import com.wadjet.feature.dictionary.screen.DictionarySignScreen
import com.wadjet.feature.dictionary.screen.LessonScreen
import com.wadjet.feature.dictionary.LessonViewModel
import com.wadjet.feature.explore.DetailViewModel
import com.wadjet.feature.explore.ExploreViewModel
import com.wadjet.feature.explore.IdentifyViewModel
import com.wadjet.feature.explore.screen.ExploreScreen
import com.wadjet.feature.explore.screen.IdentifyScreen
import com.wadjet.feature.explore.screen.LandmarkDetailScreen
import com.wadjet.feature.feedback.FeedbackViewModel
import com.wadjet.feature.feedback.screen.FeedbackScreen
import com.wadjet.feature.landing.LandingViewModel
import com.wadjet.feature.landing.screen.LandingScreen
import com.wadjet.feature.scan.HistoryViewModel
import com.wadjet.feature.scan.ScanResultViewModel
import com.wadjet.feature.scan.ScanViewModel
import com.wadjet.feature.scan.screen.ScanHistoryScreen
import com.wadjet.feature.scan.screen.ScanResultScreen
import com.wadjet.feature.scan.screen.ScanScreen
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.feature.settings.SettingsViewModel
import com.wadjet.feature.settings.screen.SettingsScreen
import com.wadjet.feature.stories.StoriesViewModel
import com.wadjet.feature.stories.StoryReaderViewModel
import com.wadjet.feature.stories.screen.StoriesScreen
import com.wadjet.feature.stories.screen.StoryReaderScreen
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wadjet.core.common.ToastController
import com.wadjet.feature.scan.ScanEvent
import kotlinx.coroutines.launch
import com.wadjet.app.navigation.lifecycleIsResumed
import com.wadjet.core.ui.LocalAnimatedVisibilityScope
import com.wadjet.core.ui.LocalSharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WadjetNavGraph(
    navController: NavHostController,
    startDestination: Route,
    webClientId: String,
    toastController: ToastController,
    modifier: Modifier = Modifier,
) {
    SharedTransitionLayout {
        val sharedTransitionScope = this
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(tween(300, easing = EaseOut)) { it / 4 } +
                fadeIn(tween(300))
        },
        exitTransition = {
            slideOutHorizontally(tween(300, easing = EaseIn)) { -it / 4 } +
                fadeOut(tween(150))
        },
        popEnterTransition = {
            slideInHorizontally(tween(300, easing = EaseOut)) { -it / 4 } +
                fadeIn(tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(tween(300, easing = EaseIn)) { it / 4 } +
                fadeOut(tween(150))
        },
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

        // --- Bottom-nav tab transitions: fadeIn(200ms) + scaleIn(0.96f) ---
        composable<Route.Landing>(
            enterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            exitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
            popEnterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            popExitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
        ) { navEntry ->
            val viewModel: LandingViewModel = hiltViewModel()
            val landingState by viewModel.state.collectAsStateWithLifecycle()
            LandingScreen(
                state = landingState,
                onNavigateToScan = { navController.navigate(Route.Scan) { launchSingleTop = true } },
                onNavigateToExplore = { navController.navigate(Route.Explore) { launchSingleTop = true } },
                onNavigateToDictionary = { navController.navigate(Route.Dictionary()) { launchSingleTop = true } },
                onNavigateToWrite = { navController.navigate(Route.Dictionary(initialTab = 2)) { launchSingleTop = true } },
                onNavigateToIdentify = { navController.navigate(Route.Identify) { launchSingleTop = true } },
                onNavigateToStories = { navController.navigate(Route.Stories) { launchSingleTop = true } },
                onNavigateToChat = { navController.navigate(Route.Chat) { launchSingleTop = true } },
                onNavigateToStoryReader = { storyId -> if (navEntry.lifecycleIsResumed()) navController.navigate(Route.StoryReader(storyId)) { launchSingleTop = true } },
                onRefresh = viewModel::refresh,
            )
        }

        // Hieroglyphs hub
        composable<Route.Hieroglyphs>(
            enterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            exitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
            popEnterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            popExitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
        ) {
            val hubViewModel: HieroglyphsHubViewModel = hiltViewModel()
            val hubState by hubViewModel.state.collectAsStateWithLifecycle()
            HieroglyphsHubScreen(
                state = hubState,
                onNavigateToScan = { navController.navigate(Route.Scan) { launchSingleTop = true } },
                onNavigateToDictionary = { navController.navigate(Route.Dictionary()) { launchSingleTop = true } },
                onNavigateToWrite = { navController.navigate(Route.Dictionary(initialTab = 2)) { launchSingleTop = true } },
            )
        }

        // Scan
        composable<Route.Scan>(
            enterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            exitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
            popEnterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            popExitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
        ) { navEntry ->
            val viewModel: ScanViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            // Collect scan events (toast + navigation)
            androidx.compose.runtime.LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        is ScanEvent.ShowToast -> toastController.info(event.message)
                        is ScanEvent.NavigateToResult -> { /* handled via state */ }
                    }
                }
            }

            val result = state.result
            if (result != null) {
                ScanResultScreen(
                    result = result,
                    ttsStates = state.ttsStates,
                    onSpeak = { key, text, lang -> viewModel.speak(key, text, lang) },
                    onScanAgain = { viewModel.resetScan() },
                    onNavigateToDictionarySign = { code -> if (navEntry.lifecycleIsResumed()) navController.navigate(Route.DictionarySign(code)) { launchSingleTop = true } },
                    onBack = { navController.popBackStack() },
                )
            } else {
                ScanScreen(
                    state = state,
                    onImageCaptured = { viewModel.onImageCaptured(it) },
                    onImageSelected = { viewModel.onImageSelected(it) },
                    onNavigateToHistory = { navController.navigate(Route.ScanHistory) { launchSingleTop = true } },
                    onBack = { navController.popBackStack() },
                )
            }
        }

        composable<Route.ScanHistory> { navEntry ->
            val viewModel: HistoryViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            ScanHistoryScreen(
                state = state,
                onScanTap = { scanId -> if (navEntry.lifecycleIsResumed()) navController.navigate(Route.ScanResult(scanId.toString())) { launchSingleTop = true } },
                onDelete = { viewModel.deleteScan(it) },
                onRefresh = viewModel::refresh,
                onBack = { navController.popBackStack() },
            )
        }

        composable<Route.ScanResult> { navEntry ->
            val viewModel: ScanResultViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val result = state.result
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(WadjetColors.Night),
                        contentAlignment = Alignment.Center,
                    ) {
                        com.wadjet.core.designsystem.component.ShimmerCardList(itemCount = 3)
                    }
                }
                result != null -> {
                    ScanResultScreen(
                        result = result,
                        ttsStates = state.ttsStates,
                        onSpeak = { key, text, lang -> viewModel.speak(key, text, lang) },
                        onScanAgain = { navController.popBackStack() },
                    onNavigateToDictionarySign = { code -> if (navEntry.lifecycleIsResumed()) navController.navigate(Route.DictionarySign(code)) { launchSingleTop = true } },
                        onBack = { navController.popBackStack() },
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(WadjetColors.Night),
                        contentAlignment = Alignment.Center,
                    ) {
                        com.wadjet.core.designsystem.component.EmptyState(
                            glyph = "\uD80C\uDC80",
                            title = "Scan not found",
                            subtitle = state.error ?: "This scan result is no longer available",
                        )
                    }
                }
            }
        }
        composable<Route.Dictionary> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Dictionary>()
            DictionaryScreen(
                onNavigateToLesson = { level -> navController.navigate(Route.Lesson(level)) { launchSingleTop = true } },
                initialTab = route.initialTab,
                prefillGlyph = route.prefillGlyph,
            )
        }

        composable<Route.Lesson> {
            val viewModel: LessonViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            LessonScreen(
                state = state,
                onRetry = viewModel::retry,
                onBack = { navController.popBackStack() },
                onSpeak = viewModel::speakSign,
            )
        }

        composable<Route.DictionarySign> { navEntry ->
            DictionarySignScreen(
                onBack = { navController.popBackStack() },
                onPracticeWriting = { code ->
                    if (navEntry.lifecycleIsResumed()) {
                        navController.navigate(Route.Dictionary(initialTab = 2, prefillGlyph = code)) {
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
        composable<Route.Explore>(
            enterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            exitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
            popEnterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            popExitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
        ) { navEntry ->
            val viewModel: ExploreViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalSharedTransitionScope provides sharedTransitionScope,
                LocalAnimatedVisibilityScope provides this,
            ) {
            ExploreScreen(
                state = state,
                onCategorySelected = viewModel::selectCategory,
                onCitySelected = viewModel::selectCity,
                onSearchChanged = viewModel::updateSearch,
                onLandmarkTap = { slug -> if (navEntry.lifecycleIsResumed()) navController.navigate(Route.LandmarkDetail(slug)) { launchSingleTop = true } },
                onToggleFavorite = viewModel::toggleFavorite,
                onLoadMore = viewModel::loadMore,
                onRefresh = viewModel::refresh,
                onIdentify = { navController.navigate(Route.Identify) { launchSingleTop = true } },
                onBack = { navController.popBackStack() },
            )
            }
        }
        composable<Route.LandmarkDetail> { navEntry ->
            val viewModel: DetailViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalSharedTransitionScope provides sharedTransitionScope,
                LocalAnimatedVisibilityScope provides this,
            ) {
            LandmarkDetailScreen(
                state = state,
                onTabSelected = viewModel::selectTab,
                onToggleFavorite = viewModel::toggleFavorite,
                onRecommendationTap = { slug -> if (navEntry.lifecycleIsResumed()) navController.navigate(Route.LandmarkDetail(slug)) { launchSingleTop = true } },
                onChildTap = { slug -> if (navEntry.lifecycleIsResumed()) navController.navigate(Route.LandmarkDetail(slug)) { launchSingleTop = true } },
                onChatAbout = { slug -> if (navEntry.lifecycleIsResumed()) navController.navigate(Route.ChatLandmark(slug)) { launchSingleTop = true } },
                onBack = { navController.popBackStack() },
            )
            }
        }
        composable<Route.Identify> { navEntry ->
            val viewModel: IdentifyViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            IdentifyScreen(
                state = state,
                onImageCaptured = viewModel::onImageCaptured,
                onImageSelected = viewModel::onImageSelected,
                onViewDetails = { slug ->
                    if (navEntry.lifecycleIsResumed()) navController.navigate(Route.LandmarkDetail(slug)) { launchSingleTop = true }
                },
                onAskThoth = { slug ->
                    if (navEntry.lifecycleIsResumed()) navController.navigate(Route.ChatLandmark(slug)) { launchSingleTop = true }
                },
                onIdentifyAnother = viewModel::reset,
                onRetry = viewModel::reset,
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.Chat>(
            enterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            exitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
            popEnterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            popExitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
        ) {
            val viewModel: ChatViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            ChatScreen(
                state = state,
                onInputChanged = viewModel::updateInput,
                onSend = { viewModel.sendMessage() },
                onSpeak = viewModel::speakMessage,
                onRetry = viewModel::retryLastMessage,
                onStartEdit = viewModel::startEditMessage,
                onCancelEdit = viewModel::cancelEdit,
                onSttResult = viewModel::onSttResult,
                onSetRecording = viewModel::setRecording,
                onTranscribeAudio = viewModel::transcribeAudio,
                onStopStreaming = viewModel::stopStreaming,
                onClearChat = viewModel::clearChat,
                onToggleHistory = viewModel::toggleHistory,
                onLoadConversation = viewModel::loadConversation,
                onClearHistory = viewModel::clearHistory,
                onDismissError = viewModel::dismissError,
                onDismissLocalTts = viewModel::dismissLocalTts,
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.ChatLandmark> {
            val viewModel: ChatViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            ChatScreen(
                state = state,
                onInputChanged = viewModel::updateInput,
                onSend = { viewModel.sendMessage() },
                onSpeak = viewModel::speakMessage,
                onRetry = viewModel::retryLastMessage,
                onStartEdit = viewModel::startEditMessage,
                onCancelEdit = viewModel::cancelEdit,
                onSttResult = viewModel::onSttResult,
                onSetRecording = viewModel::setRecording,
                onTranscribeAudio = viewModel::transcribeAudio,
                onStopStreaming = viewModel::stopStreaming,
                onClearChat = viewModel::clearChat,
                onToggleHistory = viewModel::toggleHistory,
                onLoadConversation = viewModel::loadConversation,
                onClearHistory = viewModel::clearHistory,
                onDismissError = viewModel::dismissError,
                onDismissLocalTts = viewModel::dismissLocalTts,
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.Stories>(
            enterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            exitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
            popEnterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            popExitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
        ) { navEntry ->
            val viewModel: StoriesViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalSharedTransitionScope provides sharedTransitionScope,
                LocalAnimatedVisibilityScope provides this,
            ) {
            StoriesScreen(
                state = state,
                onDifficultySelected = viewModel::selectDifficulty,
                onStoryTap = { storyId -> if (navEntry.lifecycleIsResumed()) navController.navigate(Route.StoryReader(storyId)) { launchSingleTop = true } },
                onToggleFavorite = viewModel::toggleStoryFavorite,
                onRefresh = viewModel::refresh,
                onBack = { navController.popBackStack() },
            )
            }
        }

        composable<Route.StoryReader> {
            val viewModel: StoryReaderViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalSharedTransitionScope provides sharedTransitionScope,
                LocalAnimatedVisibilityScope provides this,
            ) {
            StoryReaderScreen(
                state = state,
                onPrevChapter = viewModel::prevChapter,
                onNextChapter = viewModel::nextChapter,
                onReadAgain = viewModel::restartStory,
                onSubmitAnswer = viewModel::submitAnswer,
                onUpdateWriteInput = viewModel::updateWriteInput,
                onSpeak = viewModel::speakChapter,
                onRetryImage = viewModel::retryChapterImage,
                onDismissError = viewModel::dismissError,
                onDismissLocalTts = viewModel::dismissLocalTts,
                onBack = { navController.popBackStack() },
            )
            }
        }

        composable<Route.Dashboard>(
            enterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            exitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
            popEnterTransition = { fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f) },
            popExitTransition = { fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f) },
        ) {
            val viewModel: DashboardViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            DashboardScreen(
                state = state,
                onFavTabSelected = viewModel::selectFavTab,
                onRemoveFavorite = viewModel::removeFavorite,
                onRefresh = viewModel::refresh,
                onSettings = { navController.navigate(Route.Settings) { launchSingleTop = true } },
                onBack = { navController.popBackStack() },
            )
        }

        composable<Route.Settings> {
            val viewModel: SettingsViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val context = androidx.compose.ui.platform.LocalContext.current
            val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

            if (state.signedOut) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.navigate(Route.Welcome) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            }

            SettingsScreen(
                state = state,
                onStartEditName = viewModel::startEditName,
                onUpdateEditName = viewModel::updateEditName,
                onSaveName = viewModel::saveName,
                onCancelEditName = viewModel::cancelEditName,
                onUpdateCurrentPassword = viewModel::updateCurrentPassword,
                onUpdateNewPassword = viewModel::updateNewPassword,
                onChangePassword = viewModel::changePassword,
                onTtsEnabledChanged = viewModel::setTtsEnabled,
                onTtsSpeedChanged = viewModel::setTtsSpeed,
                onClearCache = {
                    coroutineScope.launch {
                        val imageLoader = coil3.SingletonImageLoader.get(context)
                        imageLoader.diskCache?.clear()
                        imageLoader.memoryCache?.clear()
                        val db = dagger.hilt.EntryPoints.get(
                            context.applicationContext,
                            CacheClearEntryPoint::class.java,
                        ).database()
                        db.clearAllTables()
                        viewModel.setCacheSize(0)
                    }
                },
                onSignOut = viewModel::signOut,
                onFeedback = { navController.navigate(Route.Feedback) { launchSingleTop = true } },
                onDismissMessage = viewModel::dismissMessage,
                onBack = { navController.popBackStack() },
            )
        }

        composable<Route.Feedback> {
            val viewModel: FeedbackViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            FeedbackScreen(
                state = state,
                onCategorySelected = viewModel::selectCategory,
                onMessageChanged = viewModel::updateMessage,
                onNameChanged = viewModel::updateName,
                onEmailChanged = viewModel::updateEmail,
                onSubmit = viewModel::submit,
                onDismissError = viewModel::dismissError,
                onBack = { navController.popBackStack() },
            )
        }
    }
    } // end SharedTransitionLayout
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface CacheClearEntryPoint {
    fun database(): com.wadjet.core.database.WadjetDatabase
}
