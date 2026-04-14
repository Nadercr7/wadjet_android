# Phase Prompts: UX Redesign

---

## Phase 1: Design System Fixes & Critical Bugs

### Prompt:

[COPY-PASTE BLOCK START]

Read these files first:
- `.specify/specs/003-ux-redesign/plan.md` (Phase 1 section)
- `.specify/specs/003-ux-redesign/tasks.md` (Phase 1 tasks T001–T028)
- `.specify/specs/003-ux-redesign/gap-analysis.md` (findings UX-010 through UX-025, UX-038–044, UX-061–062)
- `.specify/specs/003-ux-redesign/design-tokens.md` (all sections)
- `.specify/memory/constitution.md`

Source files to read:
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/WadjetTypography.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/WadjetTheme.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/WadjetColors.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/WadjetShapes.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/component/WadjetButton.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/component/WadjetCard.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/component/WadjetTextField.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/component/WadjetToast.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/component/TtsButton.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/component/OfflineIndicator.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/component/EmptyState.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/component/ErrorState.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/component/ImageUploadZone.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/component/WadjetFullLoader.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/animation/BorderBeam.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/animation/MeteorShower.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/animation/FadeUp.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/animation/KenBurnsImage.kt`
- `feature/auth/src/main/java/com/wadjet/feature/auth/sheet/RegisterSheet.kt`
- `feature/scan/src/main/java/com/wadjet/feature/scan/screen/ScanHistoryScreen.kt`
- `feature/explore/src/main/java/com/wadjet/feature/explore/screen/ExploreScreen.kt`
- `feature/explore/src/main/java/com/wadjet/feature/explore/screen/LandmarkDetailScreen.kt`
- `feature/stories/src/main/java/com/wadjet/feature/stories/screen/StoriesScreen.kt`

Tasks:

**Typography & Theme (T001–T004):**
1. [T001] In `WadjetTypography.kt`, add `headlineSmall = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp)` to the Typography object.
2. [T002] In `WadjetTypography.kt`, remove `color = WadjetColors.Ivory` from the `displayLarge` TextStyle definition. Apply Ivory at call sites instead.
3. [T003] In `WadjetTypography.kt`, update `wadjetTypographyForLang("ar")` to also swap `displayLarge`, `displayMedium`, `displaySmall`, `headlineLarge`, `headlineMedium`, `headlineSmall` to Cairo Bold/SemiBold.
4. [T004] In `WadjetTheme.kt`, get the current locale and call `wadjetTypographyForLang(locale)` instead of the static `WadjetTypography`. Use `ConfigurationCompat.getLocales(LocalContext.current.resources.configuration)[0]?.language ?: "en"`.

**Color Tokens (T005–T006):**
5. [T005] In `WadjetColors.kt`, add: `val DifficultyBeginner = Color(0xFF8B6914)`, `val DifficultyIntermediate = Color(0xFF4A90D9)`, `val DifficultyAdvanced = Color(0xFF9B59B6)`. Add darker variants too: `DifficultyBeginnerDark = Color(0xFF5A4410)`, etc.
6. [T006] In `WadjetColors.kt`, remove `TextDim`. Grep for `TextDim` in the entire codebase and replace with `TextMuted`.

**Shape Migration (T007–T008):**
7. [T007] In ALL design system component files: replace every `RoundedCornerShape(12.dp)` with `MaterialTheme.shapes.medium`, `RoundedCornerShape(16.dp)` → `MaterialTheme.shapes.large`, `RoundedCornerShape(8.dp)` → `MaterialTheme.shapes.small`, `RoundedCornerShape(24.dp)` → `MaterialTheme.shapes.extraLarge`.
8. [T008] Repeat [T007] across all feature screen composables. Start with screens that have the most hardcoded shapes.

**Component Fixes (T009–T018):**
9. [T009] In `WadjetButton.kt`: remove the `hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)` line from the primary button click modifier.
10. [T010] Add `enabled: Boolean = true` and `isLoading: Boolean = false` parameters to `WadjetDarkButton` and `WadjetTextButton`, matching the existing `WadjetButton` signature.
11. [T011] In `WadjetCard.kt`: (a) merge the two `Card(...)` blocks in `WadjetCard` into one with conditional onClick. (b) In `WadjetCardGlow`, remove `collectIsHoveredAsState()`, keep only `collectIsPressedAsState()` for the border color animation.
12. [T012] In `WadjetTextField.kt`: (a) add `keyboardOptions: KeyboardOptions = KeyboardOptions.Default` and `keyboardActions: KeyboardActions = KeyboardActions.Default` parameters, forwarding them to the inner `OutlinedTextField`. (b) Change `modifier.fillMaxWidth()` to just `modifier` (let callers control width).
13. [T013] In `WadjetToast.kt`: (a) replace Unicode icons `"✓" "✗" "ⓘ"` with `Icon(Icons.Filled.CheckCircle/Error/Info)`. (b) Add `Modifier.semantics { liveRegion = LiveRegionMode.Polite }` to the toast root composable. (c) Wrap in `SwipeToDismissBox` or add `Modifier.swipeable` for dismiss gesture.
14. [T014] In `TtsButton.kt`: (a) change `Modifier.size(32.dp)` to `Modifier.size(48.dp)` with inner icon at `18.dp`. (b) Fix PLAYING state to use the passed `contentDescription` instead of hardcoded `"Stop"`.
15. [T015] In `OfflineIndicator.kt`: (a) add `Modifier.semantics { liveRegion = LiveRegionMode.Polite }`. (b) Replace `fontSize = 12.sp, fontWeight = SemiBold` with `MaterialTheme.typography.labelSmall`.
16. [T016] In `EmptyState.kt` and `ErrorState.kt`: (a) replace `fontSize = 56.sp` with a size derived from `HieroglyphStyle`. (b) Add `glyph: String = "𓂀"` parameter to `ErrorState`.
17. [T017] In `ImageUploadZone.kt`: remove the `var localUri` + mid-composition state write. Either use `selectedImageUri` directly or sync via `LaunchedEffect(selectedImageUri) { localUri = selectedImageUri }`.
18. [T018] In `WadjetFullLoader.kt`: add `if (message != null) { Spacer(Modifier.height(12.dp)); Text(message, style = MaterialTheme.typography.bodyMedium, color = WadjetColors.TextMuted) }` after the shimmer bar.

**New Components (T019–T020):**
19. [T019] Create `WadjetAsyncImage.kt` in `core/designsystem/component/`. Wraps Coil `AsyncImage` with: `CircularProgressIndicator` (Gold, 24dp) during loading, placeholder glyph on error, `crossfade(300)`. Signature: `fun WadjetAsyncImage(url: String?, contentDescription: String?, modifier: Modifier, placeholder: @Composable (() -> Unit)? = null)`.
20. [T020] Create `WadjetSearchBar.kt` in `core/designsystem/component/`. Styled `TextField` matching the app theme with search icon, gold focus indicator, clear button when text is non-empty. Signature: `fun WadjetSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier, placeholder: String)`.

**Animation Fixes (T021–T024):**
21. [T021] In `MeteorShower.kt`: change the `delayMillis` in `tween()` to use `initialStartOffset = StartOffset(index * (durationMs / meteorCount))` on `infiniteRepeatable`.
22. [T022] In `BorderBeam.kt`: delete the entire `fun Modifier.borderBeam(...)` function. Keep only the `@Composable fun BorderBeam(...)` wrapper.
23. [T023] In `FadeUp.kt`: change `initialOffsetY = { 40 }` to `initialOffsetY = { it / 8 }`.
24. [T024] In `KenBurnsImage.kt`: change `translationX` target from `20f` to `with(LocalDensity.current) { 20.dp.toPx() }`.

**Hardcoded Color Replacement (T025–T028):**
25. [T025] In `ScanHistoryScreen.kt`, `ExploreScreen.kt`, `LandmarkDetailScreen.kt`: replace every `Color(0xFFFF4444)` with `WadjetColors.Error`.
26. [T026] In `RegisterSheet.kt`: replace both `Color(0xFF4CAF50)` with `WadjetColors.Success`.
27. [T027] In `ScanHistoryScreen.kt`: replace `Color.White` with `WadjetColors.Text`.
28. [T028] In `StoriesScreen.kt`: replace all hardcoded gradient hex values with `WadjetColors.DifficultyBeginner/Intermediate/Advanced` and their dark variants.

Verification:
1. Build: `.\gradlew.bat assembleDebug` — must succeed with zero errors
2. Run tests: `.\gradlew.bat testDebugUnitTest` — all existing tests must pass
3. Manual check: Open WelcomeScreen, LandingScreen, ExploreScreen, ChatScreen, StoriesScreen on device — verify Egyptian theme is intact, colors match, no tofu/broken glyphs
4. Verify MeteorShower stagger persists across animation cycles
5. Verify toast appears and auto-dismisses (and can be swiped)
6. Lint: no new warnings from hardcoded colors

Commit & push:
```
git add -A
git commit -m "Phase 1: UX redesign — design system fixes & critical bugs"
git push origin 003-ux-redesign
```

[COPY-PASTE BLOCK END]

---

## Phase 2: String Extraction & Localization

### Prompt:

[COPY-PASTE BLOCK START]

Read these files first:
- `.specify/specs/003-ux-redesign/plan.md` (Phase 2 section)
- `.specify/specs/003-ux-redesign/tasks.md` (Phase 2 tasks T029–T041)
- `.specify/specs/003-ux-redesign/gap-analysis.md` (finding UX-010)
- `.specify/memory/constitution.md`

For each feature module, do the following:
1. Read ALL composable files in the module
2. Find every hardcoded user-facing string (button labels, error messages, placeholders, titles, descriptions, Egyptian-themed text)
3. Create `src/main/res/values/strings.xml` in that module with all strings
4. Replace each string literal with `stringResource(R.string.x)` in the composable
5. Create `src/main/res/values-ar/strings.xml` stub file with the same keys and English values (Arabic translation to be done later)

Tasks:

1. [T029] Auth module: `WelcomeScreen.kt`, `LoginSheet.kt`, `RegisterSheet.kt`, `ForgotPasswordSheet.kt`
   - Strings: "Sign in with Google", "Sign up with Email", "Already have an account? Sign in", "Decode the Secrets of Ancient Egypt", "Built by Mr Robot", "Sign In", "Email", "Password", "Forgot password?", "Don't have an account?", "Create one", "Signing in…", "Register", "Display Name (optional)", "Confirm Password", "Creating account…", "8+ characters", "Uppercase", "Lowercase", "Number/symbol", "Send Reset Link", "Sending…", "Check your inbox", "Open Email App", "← Back to Sign In", and all other visible text
2. [T030] Landing module: `LandingScreen.kt` — "Good morning/afternoon/evening", feature titles, descriptions, "Quick Actions", "Continue where you left off", "Ask Thoth anything…", footer text
3. [T031] Scan module: `ScanScreen.kt`, `ScanResultScreen.kt`, `ScanHistoryScreen.kt` — "Scan Hieroglyphs", "Analyzing Inscription", step descriptions, "Results", "Share", "Scan Again", "Scan History", "Scan not found", etc.
4. [T032] Explore module: `ExploreScreen.kt`, `LandmarkDetailScreen.kt`, `IdentifyScreen.kt` — "Featured", "All Landmarks", search placeholder, "No results for…", tab labels, action button labels, "Ask Thoth", "View Details", "Identify Another"
5. [T033] Dictionary module: all dictionary screens — tab labels, lesson text, sign detail labels
6. [T034] Chat module: `ChatScreen.kt` — "Thoth", "Thoth is thinking…", "Past conversations", "Clear chat", suggestion chips, "Send", "Voice input", "Stop generating", etc.
7. [T035] Stories module: `StoriesScreen.kt`, `StoryReaderScreen.kt` — "Stories", difficulty labels, "Premium", "Story Complete", "Read Again", chapter nav labels
8. [T036] Dashboard module: `DashboardScreen.kt` — "Dashboard", stat labels, "Recent Scans", "Favorites", "Story Progress", "Remove"
9. [T037] Settings module: `SettingsScreen.kt` — "Settings", section headers, field labels, "Sign Out", "Clear Cache", "Send Feedback", version prefix "Wadjet v"
10. [T038] Feedback module: `FeedbackScreen.kt` — "Send Feedback", "Category", "Message", "Optional", "Submit Feedback", "Thank you!"
11. [T039] Design system shared strings: `WadjetTopBar.kt` ("Back"), `ErrorState.kt` ("Try Again"), `OfflineIndicator.kt` ("No internet connection"), `EmptyState.kt` default texts
12. [T040] Create `values-ar/strings.xml` stubs for all modules with English values as placeholders
13. [T041] Delete all entries from `app/src/main/res/values/colors.xml` (unused template colors)

Important rules:
- Egyptian-themed text must preserve the mystical tone in the string resource (the key name should be descriptive, e.g., `scan_error_unreadable = "The ancient scribes couldn't read this image"`)
- Do NOT change the visible text — only move it from Kotlin to XML
- Add `tools:ignore="MissingTranslation"` to `values-ar/strings.xml` root element
- Use proper string formatting for dynamic values (`%1$s`, `%1$d`)

Verification:
1. Build: `.\gradlew.bat assembleDebug` — must succeed with zero errors
2. Run tests: `.\gradlew.bat testDebugUnitTest` — all existing tests must pass
3. Manual check: Run the app and visually verify ALL screens display the same text as before (no missing or broken strings)
4. Grep: `grep -r "\"[A-Z]" feature/*/src/main/java/ --include="*.kt"` — should return zero hardcoded English strings in composables (excluding imports and constants)
5. Lint: no new warnings

Commit & push:
```
git add -A
git commit -m "Phase 2: UX redesign — string extraction & localization infrastructure"
git push origin 003-ux-redesign
```

[COPY-PASTE BLOCK END]

---

## Phase 3: Navigation, Platform Polish & Accessibility

### Prompt:

[COPY-PASTE BLOCK START]

Read these files first:
- `.specify/specs/003-ux-redesign/plan.md` (Phase 3 section)
- `.specify/specs/003-ux-redesign/tasks.md` (Phase 3 tasks T042–T061, T080–T083)
- `.specify/specs/003-ux-redesign/gap-analysis.md` (findings UX-001, UX-027–032, UX-046–060, UX-066–UX-069)
- `.specify/specs/003-ux-redesign/architecture.md` (proposed navigation changes)
- `.specify/memory/constitution.md`

Source files to read:
- `app/src/main/java/com/wadjet/app/MainActivity.kt`
- `app/src/main/java/com/wadjet/app/navigation/WadjetNavGraph.kt`
- `app/src/main/java/com/wadjet/app/navigation/Route.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/themes.xml`
- `feature/auth/src/main/java/com/wadjet/feature/auth/screen/WelcomeScreen.kt`
- `feature/auth/src/main/java/com/wadjet/feature/auth/sheet/LoginSheet.kt`
- `feature/auth/src/main/java/com/wadjet/feature/auth/sheet/RegisterSheet.kt`
- `feature/chat/src/main/java/com/wadjet/feature/chat/screen/ChatScreen.kt`
- `feature/scan/src/main/java/com/wadjet/feature/scan/screen/ScanScreen.kt`
- `feature/scan/src/main/java/com/wadjet/feature/scan/screen/ScanHistoryScreen.kt`
- `feature/explore/src/main/java/com/wadjet/feature/explore/screen/ExploreScreen.kt`
- `feature/explore/src/main/java/com/wadjet/feature/explore/screen/LandmarkDetailScreen.kt`
- `feature/explore/src/main/java/com/wadjet/feature/explore/screen/IdentifyScreen.kt`
- `feature/stories/src/main/java/com/wadjet/feature/stories/screen/StoriesScreen.kt`
- `feature/dashboard/src/main/java/com/wadjet/feature/dashboard/screen/DashboardScreen.kt`
- `feature/settings/src/main/java/com/wadjet/feature/settings/screen/SettingsScreen.kt`
- `feature/feedback/src/main/java/com/wadjet/feature/feedback/screen/FeedbackScreen.kt`
- `feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/DictionarySignScreen.kt`
- `core/ui/src/main/java/com/wadjet/core/ui/WadjetPreviews.kt`

Tasks:

**Platform & Splash (T042–T045):**
1. [T042] In `MainActivity.kt`: add `implementation("androidx.core:core-splashscreen:1.0.1")` to app build.gradle.kts, then add `installSplashScreen()` before `super.onCreate()`. Optionally add `splashScreen.setKeepOnScreenCondition { !isAuthReady }`.
2. [T043] In `themes.xml`: remove the `<item name="android:statusBarColor">` and `<item name="android:navigationBarColor">` lines. Keep `<item name="android:windowBackground">#FF0A0A0A</item>`.
3. [T044] In `AndroidManifest.xml`: add `android:enableOnBackInvokedCallback="true"` to the `<application>` element.
4. [T045] In `SettingsScreen.kt`: replace `"Wadjet v1.0.0-beta"` with `"Wadjet v${BuildConfig.VERSION_NAME}"`. Add import for `com.wadjet.app.BuildConfig` (or use the feature module's BuildConfig if available).

**Navigation (T046–T047):**
5. [T046] In `MainActivity.kt`: remove the floating `IconButton` (avatar). Add a `TopAppBar` to the Scaffold that shows on all bottom-nav screens, with: (a) "Wadjet" branding text or logo on the left, (b) profile `IconButton(Icons.Default.AccountCircle)` on the right → `navController.navigate(Route.Dashboard)`, (c) settings gear `IconButton(Icons.Default.Settings)` → opens `SettingsQuickDialog`. The TopAppBar should be inside the Scaffold `topBar` slot, visible when `showBottomBar == true`.
6. [T047] In `Route.kt`: add an optional `prefillGlyph: String? = null` param to `Route.Dictionary`. In `WadjetNavGraph.kt`: pass `prefillGlyph` to `DictionaryScreen`. In `DictionarySignScreen.kt`: add a "Practice Writing" button that navigates to `Route.Dictionary(initialTab = 2, prefillGlyph = code)`.

**Per-Tab Back Stacks & Navigation Guards (T080–T083):**
21. [T080] In `MainActivity.kt`: on ALL bottom-tab navigation calls (the `TopLevelDestination` click handler), add `popUpTo(navController.graph.findStartDestination().id) { saveState = true }`, `launchSingleTop = true`, and `restoreState = true`. This preserves each tab's back stack when switching tabs. Reference: NiA `NavigationState.kt`, Jetsnack `JetsnackNavController.kt`.
22. [T081] Create `feature/settings/src/main/java/**/SettingsQuickDialog.kt`: an `AlertDialog` with toggles for TTS enabled, language selector (English/Arabic), and "Clear Cache" button. Wire it from the `MainActivity.kt` TopAppBar gear icon. Reference: NiA `SettingsDialog.kt`.
23. [T082] In `WadjetNavGraph.kt`: add `launchSingleTop = true` to ALL `navController.navigate(...)` calls for non-tab routes (LandmarkDetail, ScanResult, StoryReader, DictionarySign, Lesson, Dashboard, Settings, Feedback, etc.) to prevent duplicate screens on double-tap.
24. [T083] Create `app/src/main/java/**/navigation/NavUtils.kt` with `fun NavBackStackEntry.lifecycleIsResumed(): Boolean = lifecycle.currentState == Lifecycle.State.RESUMED`. Use this guard in detail-pushing callbacks (e.g., `onLandmarkTap`, `onStoryTap`, `onSignClick`) where high-interaction items are tappable. Reference: Jetsnack `JetsnackNavController.kt`.

**Auth Fixes (T048–T051):**
7. [T048] In `WelcomeScreen.kt`: extract the `credentialManager.getCredential(...)` block into a private `suspend fun performGoogleSignIn(context, credentialManager, webClientId, onSuccess, onError)`. Call it from all 3 locations.
8. [T049] On `WelcomeScreen`: if `state.error != null` and no sheet is visible, show the error inline (e.g., a `Text` with `WadjetColors.Error` below the auth buttons).
9. [T050] In `RegisterSheet.kt`: (a) add a password visibility toggle to the confirm-password field (same eye icon pattern as main password). (b) When `confirmPassword.isNotEmpty() && password != confirmPassword`, show red error border and "Passwords don't match" text.
10. [T051] In `LoginSheet.kt` and `RegisterSheet.kt`: add `KeyboardOptions(imeAction = ImeAction.Next)` on email fields, `ImeAction.Done` on last field with `KeyboardActions(onDone = { onSignIn/onRegister() })`.

**Chat Insets (T052):**
11. [T052] In `ChatScreen.kt`: change the `Scaffold` to `Scaffold(contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars).exclude(WindowInsets.ime))`. On the `ChatInputBar` composable, add `Modifier.navigationBarsPadding().imePadding()`.

**Accessibility (T053–T055):**
12. [T053] Add `contentDescription` to all interactive elements found missing in the gap analysis: ExploreScreen SearchBar, ScanResultScreen language toggle, IdentifyScreen warning icons, StoryReaderScreen glyph options, FeedbackScreen submit.
13. [T054] Add `Modifier.semantics { role = Role.Button }` to: FeedbackScreen submit Box, DashboardScreen "Remove" Text.clickable.
14. [T055] In `WadjetPreviews.kt`: add `@Preview(name = "RTL", locale = "ar", device = "spec:width=411dp,height=891dp")` and `@Preview(name = "Large Text", fontScale = 1.5f, device = "spec:width=411dp,height=891dp")`.

**Screen-Specific Fixes (T056–T061):**
15. [T056] In `LandmarkDetailScreen.kt`: replace the `GalleryTab` `LazyVerticalGrid(height=400.dp)` with a non-lazy approach: either `Column` of `Row`s (for 2-col grid) with `wrapContentHeight`, or `FlowRow` with equal-width items.
16. [T057] In `ScanScreen.kt`: remove the unused `val (visible, _) = remember { mutableStateOf(false) }` and the empty `LaunchedEffect(Unit) { delay(100) }`.
17. [T058] In `ExploreScreen.kt`: replace the raw `TextField` SearchBar with `WadjetSearchBar` (created in Phase 1).
18. [T059] `StoriesScreen.kt` difficulty gradients should now use WadjetColors difficulty tokens (if not done in T028).
19. [T060] In `DashboardScreen.kt`: (a) replace hardcoded `"𓀀"` glyph with actual scan data. (b) Replace `/ 5f` with the story's actual total chapters. (c) Replace `favorite.itemId` with `favorite.displayName` (if available) or format the slug. (d) Add `AsyncImage(avatarUrl)` with initials fallback.
20. [T061] In `FeedbackScreen.kt`: replace the `Box(clickable)` submit with `WadjetButton(text = "Submit Feedback", onClick = onSubmit, isLoading = state.isSubmitting)`.

Verification:
1. Build: `.\gradlew.bat assembleDebug` — must succeed with zero errors
2. Run tests: `.\gradlew.bat testDebugUnitTest` — all existing tests must pass
3. Manual check on device:
   - App launch: no white splash flash (dark splash with icon)
   - TopAppBar visible on all tab screens with profile icon + gear icon
   - Profile icon → Dashboard opens (1 tap)
   - Gear icon → Quick-settings dialog opens (1 tap)
   - Tab switching preserves back stack: Home → Scan → switch to Explore → switch back to Home → Scan is still visible (NOT reset to Landing)
   - Double-tap a landmark card fast → only ONE LandmarkDetail screen pushed
   - Chat keyboard: input bar slides up smoothly when keyboard opens
   - Back gesture: predictive back preview works on Android 14+
   - TalkBack: navigate through all screens — verify announcements
4. Lint: no new warnings

Commit & push:
```
git add -A
git commit -m "Phase 3: UX redesign — navigation, platform polish & accessibility"
git push origin 003-ux-redesign
```

[COPY-PASTE BLOCK END]

---

## Phase 4: Interaction & Content UX Improvements

### Prompt:

[COPY-PASTE BLOCK START]

Read these files first:
- `.specify/specs/003-ux-redesign/plan.md` (Phase 4 section)
- `.specify/specs/003-ux-redesign/tasks.md` (Phase 4 tasks T062–T067, T084)
- `.specify/specs/003-ux-redesign/gap-analysis.md` (findings UX-002, UX-007, UX-008, UX-023, UX-052)

Source files to read:
- `feature/landing/src/main/java/com/wadjet/feature/landing/screen/LandingScreen.kt`
- `feature/scan/src/main/java/com/wadjet/feature/scan/screen/ScanHistoryScreen.kt`
- `feature/stories/src/main/java/com/wadjet/feature/stories/screen/StoryReaderScreen.kt`
- `feature/chat/src/main/java/com/wadjet/feature/chat/screen/ChatScreen.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/component/WadjetBottomBar.kt`
- `app/src/main/java/com/wadjet/app/screen/HieroglyphsHubScreen.kt`

Tasks:

1. [T062] In `LandingScreen.kt`: (a) wrap `LazyColumn` in `PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = onRefresh)`. (b) Add shimmer loading state when `state.isLoading`: show `ShimmerCardList` items instead of content. (c) Add error inline when `state.error != null`.
2. [T063] In `LandingScreen.kt` QuickAction for "Write": replace the broken glyph icon with `Icons.Outlined.Draw` or a proper Egyptian hieroglyph like `"𓏞"`.
3. [T064] In `ScanHistoryScreen.kt`: change `SwipeToDismissBox` `LaunchedEffect(dismissState.currentValue)` to NOT call `onDelete` directly. Instead, show a `Snackbar` with "Scan deleted" + "Undo" action. Only call `onDelete` when snackbar times out without undo. Use `SnackbarHostState` + `LaunchedEffect`.
4. [T065] In `StoryReaderScreen.kt`: replace `Snackbar` error handling with `ToastController` (consistent with all other screens). Remove `snackbarHostState` and `SnackbarHost`. Use the same toast pattern as `ChatScreen`.
5. [T066] In `ChatScreen.kt`: (a) remove the `localTts` map (dead code). (b) Wrap `formatRelativeTime` calls in `remember(timestamp)` to avoid recomputation on every composition.
6. [T067] Verify if `core/designsystem/component/WadjetBottomBar.kt` is dead code (not imported anywhere). If confirmed dead, delete it. The actual bottom bar is defined inline in `MainActivity.kt`.
7. [T084] Enrich `HieroglyphsHubScreen.kt` with dynamic content, transforming it from a static menu into a personalized "Hieroglyphs home": (a) Add a `HieroglyphsHubViewModel` that loads recent scans, learning streak, and suggested signs from existing repositories. (b) Add a "Recent Scans" horizontal carousel showing the last 3 scans with thumbnails. (c) Add a learning streak card ("5-day streak! 🔥") or progress indicator (X/1071 signs learned). (d) Add a "Suggested Signs" section showing 3 random signs the user hasn't seen. (e) Keep the existing 3 feature cards (Scan, Dictionary, Write) but move them below the dynamic content. Reference: NiA `ForYouScreen` — personalized feed as tab content.
4. [T065] In `StoryReaderScreen.kt`: replace `Snackbar` error handling with `ToastController` (consistent with all other screens). Remove `snackbarHostState` and `SnackbarHost`. Use the same toast pattern as `ChatScreen`.
5. [T066] In `ChatScreen.kt`: (a) remove the `localTts` map (dead code). (b) Wrap `formatRelativeTime` calls in `remember(timestamp)` to avoid recomputation on every composition.
6. [T067] Verify if `core/designsystem/component/WadjetBottomBar.kt` is dead code (not imported anywhere). If confirmed dead, delete it. The actual bottom bar is defined inline in `MainActivity.kt`.

Verification:
1. Build: `.\gradlew.bat assembleDebug` — must succeed with zero errors
2. Run tests: `.\gradlew.bat testDebugUnitTest` — all pass
3. Manual check:
   - Pull-to-refresh on Landing screen works
   - Swipe-to-delete a scan history item → "Undo" snackbar appears → tap Undo → item restored
   - Story reader errors show as toast (not snackbar)
   - Chat doesn't crash on load
   - Hieroglyphs tab shows dynamic content (recent scans, streak, suggested signs) above the 3 feature cards
4. Lint: no new warnings

Commit & push:
```
git add -A
git commit -m "Phase 4: UX redesign — interaction & content UX improvements"
git push origin 003-ux-redesign
```

[COPY-PASTE BLOCK END]

---

## Phase 5: Visual Polish, Transitions & Adaptive Layout

### Prompt:

[COPY-PASTE BLOCK START]

Read these files first:
- `.specify/specs/003-ux-redesign/plan.md` (Phase 5 section)
- `.specify/specs/003-ux-redesign/tasks.md` (Phase 5 tasks T068–T079)
- `.specify/specs/003-ux-redesign/gap-analysis.md` (findings UX-033, UX-040, UX-043, UX-063–UX-065)

Source files to read:
- `app/src/main/java/com/wadjet/app/navigation/WadjetNavGraph.kt`
- `app/src/main/java/com/wadjet/app/MainActivity.kt`
- `feature/explore/src/main/java/com/wadjet/feature/explore/screen/ExploreScreen.kt`
- `feature/explore/src/main/java/com/wadjet/feature/explore/screen/LandmarkDetailScreen.kt`
- `feature/stories/src/main/java/com/wadjet/feature/stories/screen/StoriesScreen.kt`
- `feature/stories/src/main/java/com/wadjet/feature/stories/screen/StoryReaderScreen.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/animation/ShineSweep.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/animation/GoldGradientText.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/animation/GoldGradientSweep.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/animation/DotPattern.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/component/ShimmerEffect.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/animation/ButtonShimmer.kt`
- `core/designsystem/src/main/java/com/wadjet/core/designsystem/animation/GoldPulse.kt`

Also read these reference files for shared element transition pattern:
- `D:\Personal attachements\Repos\23-Android-Kotlin\compose-samples\Jetsnack\app\src\main\java\com\example\jetsnack\ui\snackdetail\SnackDetail.kt`
- `D:\Personal attachements\Repos\23-Android-Kotlin\compose-samples\Jetsnack\app\src\main\java\com\example\jetsnack\ui\JetsnackApp.kt`

Tasks:

**Shared Element Transitions (T068–T070):**
1. [T068] In `WadjetNavGraph.kt`: wrap the `NavHost` in `SharedTransitionLayout`. Pass `this@SharedTransitionLayout` and each composable's `AnimatedVisibilityScope` (from the composable builder receiver) to screens via `CompositionLocalProvider`. Define `val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope> { error("...") }` and `val LocalAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope> { error("...") }` in a new file `core/ui/src/main/java/.../SharedTransitionLocals.kt`.
2. [T069] In `ExploreScreen.kt` `LandmarkCard`: add `Modifier.sharedBounds(rememberSharedContentState("landmark-${landmark.slug}"), LocalAnimatedVisibilityScope.current)` on the card image. In `LandmarkDetailScreen.kt`: add the same `sharedBounds` key on the hero image/carousel. Use `spatialExpressiveSpring()` for boundsTransform.
3. [T070] In `StoriesScreen.kt` `StoryCard`: add `sharedBounds("story-${story.id}")` on the glyph cover. In `StoryReaderScreen.kt`: match with `sharedBounds("story-${storyId}")` on the scene image or chapter header.

**Adaptive Layout (T071–T072):**
4. [T071] In `MainActivity.kt`: add `val windowSizeClass = calculateWindowSizeClass(this)` in `onCreate` (requires `implementation("androidx.compose.material3:material3-window-size-class")` in build.gradle.kts). Pass `windowSizeClass` to `WadjetApp`.
5. [T072] In `MainActivity.kt` `WadjetApp`: replace the manual `NavigationBar` + `Scaffold` pattern with `NavigationSuiteScaffold` (M3 adaptive). This automatically uses `NavigationBar` on phones, `NavigationRail` on medium screens, and `NavigationDrawer` on expanded. Preserve the existing tab items and selection logic.

**Animation Pixel Fixes (T073–T079):**
6. [T073] In `ShineSweep.kt`: replace the hardcoded offset range with `drawWithContent { val sweep = size.width + size.height; ... }` using the measured `size`.
7. [T074] In `GoldGradientText.kt`: replace `targetValue = 1000f` and `500` window with the measured text width from `onGloballyPositioned { coordinates -> }` or `SubcomposeLayout`.
8. [T075] In `GoldGradientSweep.kt`: replace `targetValue = 2000f` with measured `size.width + size.height` and window of `size.width * 0.3f`.
9. [T076] In `DotPattern.kt`: convert `dotRadius = 1.5f` and `spacing = 24f` to dp-based values using `with(LocalDensity.current) { 1.dp.toPx() }` and `with(LocalDensity.current) { 8.dp.toPx() }`.
10. [T077] In `ShimmerEffect.kt`: replace `-1000f to 1000f` with `-size.width to size.width * 2` using the measured width from `drawBehind { }`.
11. [T078] In `ButtonShimmer.kt`: apply the same measured-width pattern as T077 — replace pixel-hardcoded sweep range with composable width from `drawBehind { size.width }`.
12. [T079] In `GoldPulse.kt`: add `label = "goldPulse"` to `animateFloatAsState` for Compose animation tooling inspection.

Verification:
1. Build: `.\gradlew.bat assembleDebug` — must succeed with zero errors
2. Run tests: `.\gradlew.bat testDebugUnitTest` — all pass
3. Manual check on phone:
   - Tap a landmark card in Explore → card image morphs into detail hero (shared element)
   - Tap a story card → story glyph morphs into reader header
   - Animations run smoothly at 60fps (no jank on transitions)
   - Bottom bar still works correctly on phone
4. Manual check on tablet (emulator or real device):
   - Navigation bar appears as a side rail (NavigationRail)
   - Content area is wider with no excessive stretching
5. Verify shimmer/gradient animations scale correctly on both phone and tablet widths

Commit & push:
```
git add -A
git commit -m "Phase 5: UX redesign — visual polish, transitions & adaptive layout"
git push origin 003-ux-redesign
```

[COPY-PASTE BLOCK END]
