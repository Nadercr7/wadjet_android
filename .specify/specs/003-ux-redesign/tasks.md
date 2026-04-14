# Tasks: UX Redesign

## Format
- `[ID] [P?] [Story] Description` → `[file.kt]`
- [P] = parallelizable (can run concurrently with other [P] tasks in same phase)
- [Story] = US1–US8 (from spec.md)

---

## Phase 1: Design System Fixes & Critical Bugs

### Typography & Theme
- [ ] T001 [US1] Add `headlineSmall` (PlayfairDisplay SemiBold 22sp/28sp) to type scale → `WadjetTypography.kt`
- [ ] T002 [US1] Remove `color = WadjetColors.Ivory` from `displayLarge` TextStyle → `WadjetTypography.kt`
- [ ] T003 [US1] Fix `wadjetTypographyForLang("ar")` to swap display/headline families to Cairo → `WadjetTypography.kt`
- [ ] T004 [US1] Wire `wadjetTypographyForLang()` into `WadjetTheme` using device locale → `WadjetTheme.kt`

### Color Tokens
- [ ] T005 [P] [US1] Add `DifficultyBeginner`, `DifficultyIntermediate`, `DifficultyAdvanced` color tokens → `WadjetColors.kt`
- [ ] T006 [P] [US1] Remove `TextDim` token (merge usages into `TextMuted`) → `WadjetColors.kt` + grep all usages

### Shape Migration
- [ ] T007 [US1] Replace all hardcoded `RoundedCornerShape(12.dp)` with `MaterialTheme.shapes.medium` in design system components → `WadjetButton.kt`, `WadjetCard.kt`, `WadjetTextField.kt`, `WadjetBadge.kt`, `WadjetToast.kt`, `ImageUploadZone.kt`
- [ ] T008 [US1] Replace hardcoded shapes in all screen composables (RoundedCornerShape(16.dp) → shapes.large, 8.dp → shapes.small, 24.dp → shapes.extraLarge) → all feature screen files

### Component Fixes
- [ ] T009 [P] [US1] Fix `WadjetButton` haptic: remove `HapticFeedbackType.LongPress` from click handler → `WadjetButton.kt`
- [ ] T010 [P] [US1] Add `enabled`/`isLoading` to `WadjetDarkButton` and `WadjetTextButton` → `WadjetButton.kt`
- [ ] T011 [P] [US1] Fix `WadjetCard`: remove `collectIsHoveredAsState()` from `WadjetCardGlow`, deduplicate Card blocks → `WadjetCard.kt`
- [ ] T012 [P] [US1] Fix `WadjetTextField`: add `keyboardOptions`/`keyboardActions` params, remove forced `fillMaxWidth` → `WadjetTextField.kt`
- [ ] T013 [US2] Fix `WadjetToast`: replace Unicode icons with Material Icons, add `liveRegion` semantics, add swipe-to-dismiss → `WadjetToast.kt`
- [ ] T014 [P] [US2] Fix `TtsButton`: increase touch target to 48dp, fix `contentDescription` override in PLAYING state → `TtsButton.kt`
- [ ] T015 [P] [US2] Fix `OfflineIndicator`: add `liveRegion` semantics, use typography token → `OfflineIndicator.kt`
- [ ] T016 [P] [US1] Fix `EmptyState`/`ErrorState`: use `HieroglyphStyle` for glyph sizing, add glyph param to ErrorState → `EmptyState.kt`, `ErrorState.kt`
- [ ] T017 [P] [US1] Fix `ImageUploadZone`: remove state-write-during-composition bug → `ImageUploadZone.kt`
- [ ] T018 [P] [US1] Fix `WadjetFullLoader`: render `message` parameter → `WadjetFullLoader.kt`

### New Components
- [ ] T019 [US1] Create `WadjetAsyncImage` component (placeholder, error, crossfade, loading indicator) → `core/designsystem/src/main/java/**/component/WadjetAsyncImage.kt`
- [ ] T020 [US1] Create `WadjetSearchBar` component (gold theme, outlined, search icon) → `core/designsystem/src/main/java/**/component/WadjetSearchBar.kt`

### Animation Fixes
- [ ] T021 [P] [US7] Fix `MeteorShower` stagger: use `initialStartOffset` instead of `delayMillis` → `MeteorShower.kt`
- [ ] T022 [P] [US7] Delete `Modifier.borderBeam()` dead code → `BorderBeam.kt`
- [ ] T023 [P] [US7] Fix `FadeUp` offset: use `{ it / 8 }` proportional → `FadeUp.kt`
- [ ] T024 [P] [US7] Fix `KenBurnsImage` translationX: use `20.dp.toPx()` → `KenBurnsImage.kt`

### Hardcoded Color Replacement
- [ ] T025 [P] [US1] Replace `Color(0xFFFF4444)` → `WadjetColors.Error` → `ScanHistoryScreen.kt`, `ExploreScreen.kt`, `LandmarkDetailScreen.kt`
- [ ] T026 [P] [US1] Replace `Color(0xFF4CAF50)` → `WadjetColors.Success` → `RegisterSheet.kt`
- [ ] T027 [P] [US1] Replace `Color.White` → `WadjetColors.Text` or appropriate token → `ScanHistoryScreen.kt`
- [ ] T028 [P] [US1] Replace hardcoded difficulty gradient hex values → `WadjetColors` difficulty tokens → `StoriesScreen.kt`

---

## Phase 2: String Extraction & Localization

- [ ] T029 [US3] Extract all auth screen strings → `feature/auth/` `strings.xml` + `WelcomeScreen.kt`, `LoginSheet.kt`, `RegisterSheet.kt`, `ForgotPasswordSheet.kt`
- [ ] T030 [P] [US3] Extract all landing screen strings → `feature/landing/` `strings.xml` + `LandingScreen.kt`
- [ ] T031 [P] [US3] Extract all scan screen strings → `feature/scan/` `strings.xml` + `ScanScreen.kt`, `ScanResultScreen.kt`, `ScanHistoryScreen.kt`
- [ ] T032 [P] [US3] Extract all explore screen strings → `feature/explore/` `strings.xml` + `ExploreScreen.kt`, `LandmarkDetailScreen.kt`, `IdentifyScreen.kt`
- [ ] T033 [P] [US3] Extract all dictionary screen strings → `feature/dictionary/` `strings.xml` + all dictionary screens
- [ ] T034 [P] [US3] Extract all chat screen strings → `feature/chat/` `strings.xml` + `ChatScreen.kt`
- [ ] T035 [P] [US3] Extract all stories screen strings → `feature/stories/` `strings.xml` + `StoriesScreen.kt`, `StoryReaderScreen.kt`
- [ ] T036 [P] [US3] Extract all dashboard screen strings → `feature/dashboard/` `strings.xml` + `DashboardScreen.kt`
- [ ] T037 [P] [US3] Extract all settings screen strings → `feature/settings/` `strings.xml` + `SettingsScreen.kt`
- [ ] T038 [P] [US3] Extract all feedback screen strings → `feature/feedback/` `strings.xml` + `FeedbackScreen.kt`
- [ ] T039 [US3] Extract shared component strings (Back, Try Again, No internet, etc.) → `core/designsystem/` `strings.xml` + all component files
- [ ] T040 [US3] Create Arabic stub files (`values-ar/strings.xml`) for each module → all modules
- [ ] T041 [US3] Remove unused `colors.xml` template entries → `app/src/main/res/values/colors.xml`

---

## Phase 3: Navigation, Platform Polish & Accessibility

### Platform & Splash
- [ ] T042 [US5] Add `installSplashScreen()` before `super.onCreate()` with auth gate condition → `MainActivity.kt`
- [ ] T043 [P] [US5] Remove deprecated `statusBarColor`/`navigationBarColor` from themes.xml → `app/src/main/res/values/themes.xml`
- [ ] T044 [P] [US5] Add `android:enableOnBackInvokedCallback="true"` to manifest → `AndroidManifest.xml`
- [ ] T045 [US5] Use `BuildConfig.VERSION_NAME` for version display → `SettingsScreen.kt`

### Navigation
- [ ] T046 [US4] Remove floating avatar from `MainActivity.kt`, add TopAppBar with profile + settings-gear actions on tab screens → `MainActivity.kt`
- [ ] T047 [US4] Add DictionarySign → Write deep link parameter → `Route.kt`, `WadjetNavGraph.kt`, `DictionarySignScreen.kt`
- [ ] T080 [US4] Add per-tab back stack preservation: `saveState = true`, `restoreState = true`, `launchSingleTop = true` on all bottom-tab navigation calls → `MainActivity.kt`
- [ ] T081 [US4] Add quick-settings dialog (TTS toggle, language, clear cache) triggered from TopAppBar gear icon → new `feature/settings/src/main/java/**/SettingsQuickDialog.kt`, `MainActivity.kt`
- [ ] T082 [P] [US4] Add `launchSingleTop = true` to all non-tab `navController.navigate(...)` calls → `WadjetNavGraph.kt`
- [ ] T083 [P] [US4] Add `lifecycleIsResumed()` extension guard on detail-pushing navigation callbacks → `WadjetNavGraph.kt`, new `app/src/main/java/**/navigation/NavUtils.kt`

### Auth Fixes
- [ ] T048 [US4] Deduplicate Google Sign-In into helper function → `WelcomeScreen.kt`
- [ ] T049 [P] [US4] Add Google Sign-In error display on WelcomeScreen → `WelcomeScreen.kt`
- [ ] T050 [P] [US4] Add confirm-password visibility toggle + mismatch validation → `RegisterSheet.kt`
- [ ] T051 [P] [US6] Wire keyboard actions (Next/Done) on auth form fields → `LoginSheet.kt`, `RegisterSheet.kt`

### Chat Insets
- [ ] T052 [US6] Fix ChatScreen IME inset handling: exclude from Scaffold, apply on input bar → `ChatScreen.kt`

### Accessibility
- [ ] T053 [US2] Add `contentDescription` to all interactive elements missing it → `ExploreScreen.kt`, `ScanResultScreen.kt`, `IdentifyScreen.kt`, `StoryReaderScreen.kt`, `FeedbackScreen.kt`
- [ ] T054 [P] [US2] Add `Role.Button` semantics to all `Box.clickable`/`Text.clickable` actions → `FeedbackScreen.kt`, `DashboardScreen.kt`
- [ ] T055 [P] [US2] Add RTL + font-scale preview annotations → `WadjetPreviews.kt`

### Screen-Specific Fixes
- [ ] T056 [P] [US1] Fix LandmarkDetail GalleryTab: replace LazyVerticalGrid with non-lazy grid → `LandmarkDetailScreen.kt`
- [ ] T057 [P] [US1] Fix ScanScreen: remove dead `visible` state + empty LaunchedEffect → `ScanScreen.kt`
- [ ] T058 [P] [US1] Fix ExploreScreen: replace raw TextField with WadjetSearchBar → `ExploreScreen.kt`
- [ ] T059 [P] [US1] Fix StoriesScreen: use WadjetColors difficulty tokens for gradients → `StoriesScreen.kt`
- [ ] T060 [P] [US1] Fix DashboardScreen: real glyph/thumbnail on ScanCards, fix 5f chapter hardcode, fix slug display, load avatar → `DashboardScreen.kt`
- [ ] T061 [P] [US1] Fix FeedbackScreen: replace Box.clickable with WadjetButton → `FeedbackScreen.kt`

---

## Phase 4: Interaction & Content UX Improvements

- [ ] T062 [US4] Add pull-to-refresh and loading/error states to LandingScreen → `LandingScreen.kt`
- [ ] T063 [P] [US4] Fix Write QuickAction icon (replace broken glyph) → `LandingScreen.kt`
- [ ] T064 [P] [US4] Add undo Snackbar to ScanHistory delete → `ScanHistoryScreen.kt`
- [ ] T065 [P] [US4] Standardize StoryReader error handling to toast → `StoryReaderScreen.kt`
- [ ] T066 [P] [US4] Remove dead `localTts` map + fix `formatRelativeTime` in ChatScreen → `ChatScreen.kt`
- [ ] T067 [P] [US4] Delete dead `WadjetBottomBar.kt` in design system (if confirmed dead code) → `WadjetBottomBar.kt`
- [ ] T084 [US4] Enrich HieroglyphsHubScreen with dynamic content: recent scans carousel, learning streak, suggested signs, scan stats → `HieroglyphsHubScreen.kt`, `HieroglyphsHubViewModel.kt` (new)

---

## Phase 5: Visual Polish, Transitions & Adaptive Layout

### Shared Element Transitions
- [ ] T068 [US7] Wrap NavHost in SharedTransitionLayout → `WadjetNavGraph.kt`
- [ ] T069 [US7] Add sharedBounds on ExploreScreen landmark cards + LandmarkDetail hero → `ExploreScreen.kt`, `LandmarkDetailScreen.kt`
- [ ] T070 [US7] Add sharedBounds on StoriesScreen cards + StoryReader header → `StoriesScreen.kt`, `StoryReaderScreen.kt`

### Adaptive Layout
- [ ] T071 [US8] Add `calculateWindowSizeClass()` to MainActivity and pass to app scaffold → `MainActivity.kt`
- [ ] T072 [US8] Replace bottom bar with `NavigationSuiteScaffold` (auto-switches bar/rail/drawer) → `MainActivity.kt`

### Animation Pixel Fixes
- [ ] T073 [P] [US7] Fix ShineSweep: use measured width → `ShineSweep.kt`
- [ ] T074 [P] [US7] Fix GoldGradientText: use measured width → `GoldGradientText.kt`
- [ ] T075 [P] [US7] Fix GoldGradientSweep: use measured width → `GoldGradientSweep.kt`
- [ ] T076 [P] [US7] Fix DotPattern: convert to dp-based spacing → `DotPattern.kt`
- [ ] T077 [P] [US7] Fix ShimmerEffect: use measured width → `ShimmerEffect.kt`
- [ ] T078 [P] [US7] Fix ButtonShimmer: use measured width for sweep range → `ButtonShimmer.kt`
- [ ] T079 [P] [US7] Add label to GoldPulse animation spec for debuggability → `GoldPulse.kt`

---

## Deferred (Enhancement / Future)

These findings are documented but not tasked in the current redesign scope. They are 🔵 Enhancement or 🟡 Minor items with no immediate fix required.

- [ ] D001 [UX-002] ~~Deferred~~ Moved to T084 (UX-002 upgraded to Major)
- [ ] D002 [UX-005] Add global search overlay accessible from Landing TopAppBar → new `SearchScreen.kt`
- [ ] D003 [UX-009] Add 3-screen onboarding carousel for first-time users → new `OnboardingScreen.kt`
- [ ] D004 [UX-020] Document `Dust` and `Warning` color token usage in WadjetColors → `WadjetColors.kt` (comments)
- [ ] D005 [UX-034] Handle landscape mode in ChatScreen (reduce TopAppBar, increase visible area) → `ChatScreen.kt`
- [ ] D006 [UX-036] Add onboarding flow after first login → new `OnboardingScreen.kt` (same as D003)
- [ ] D007 [UX-037] Add one-time tooltip on Identify icon in ExploreScreen → `ExploreScreen.kt`

---

## Task Summary

| Phase | Tasks | Parallelizable |
|-------|-------|---------------|
| 1 | 28 (T001–T028) | 20 |
| 2 | 13 (T029–T041) | 10 |
| 3 | 24 (T042–T061, T080–T083) | 16 |
| 4 | 7 (T062–T067, T084) | 5 |
| 5 | 12 (T068–T079) | 7 |
| **Total** | **84** | **58** |
| Deferred | 6 (D002–D007) | — |
