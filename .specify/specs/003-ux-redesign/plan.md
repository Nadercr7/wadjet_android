# Implementation Plan: UX Redesign
**Spec**: 003-ux-redesign
**Date**: 2026-04-15

## Summary

The UX redesign addresses 69 findings across 13 dimensions, organized into 5 sequential phases. Phase 1 fixes the design system foundation and critical bugs. Phase 2 extracts all strings for localization. Phase 3 improves navigation architecture (per-tab back stacks, quick-settings dialog, anti-double-nav guards), platform polish, and accessibility. Phase 4 enhances interaction design, content-specific UX, and enriches the Hieroglyphs hub. Phase 5 adds visual polish, transitions, and adaptive layout. Each phase builds on the previous — phases must execute in order.

## Technical Context
- Language: Kotlin 2.1.0
- UI: Jetpack Compose (BOM 2026.03.00) + Material 3
- Navigation: Compose Navigation 2.9.7 (type-safe routes)
- Theme: Dark-only, Egyptian gold accent
- Constraints: No XML layouts, no Fragments, no light theme, ≤API 26 minimum

---

## Phase 1: Design System Fixes & Critical Bugs
**Goal:** Fix the foundation so all subsequent work builds on solid ground.
**Dependencies:** None

**Files affected:**
- `core/designsystem/src/main/java/**/WadjetTypography.kt` — add `headlineSmall`, remove `displayLarge` color, fix Arabic typography swap
- `core/designsystem/src/main/java/**/WadjetTheme.kt` — wire `wadjetTypographyForLang()` to locale
- `core/designsystem/src/main/java/**/WadjetShapes.kt` — no changes (already correct)
- `core/designsystem/src/main/java/**/WadjetColors.kt` — add `DifficultyBeginner`, `DifficultyIntermediate`, `DifficultyAdvanced` tokens; remove `TextDim`
- `core/designsystem/src/main/java/**/component/WadjetButton.kt` — fix haptic type, add `enabled`/`isLoading` to DarkButton/TextButton
- `core/designsystem/src/main/java/**/component/WadjetCard.kt` — remove hover-only animation, deduplicate Card blocks
- `core/designsystem/src/main/java/**/component/WadjetTextField.kt` — add `keyboardOptions`, `keyboardActions` params, remove forced `fillMaxWidth`
- `core/designsystem/src/main/java/**/component/WadjetToast.kt` — add `liveRegion` semantics, replace Unicode icons with Material Icons, add swipe-to-dismiss
- `core/designsystem/src/main/java/**/component/WadjetTopBar.kt` — remove `@OptIn(ExperimentalMaterial3Api::class)` if stable
- `core/designsystem/src/main/java/**/component/EmptyState.kt` — use `HieroglyphStyle` for glyph sizing
- `core/designsystem/src/main/java/**/component/ErrorState.kt` — add glyph parameter, use `HieroglyphStyle`
- `core/designsystem/src/main/java/**/component/TtsButton.kt` — fix touch target to ≥48dp, fix contentDescription override
- `core/designsystem/src/main/java/**/component/OfflineIndicator.kt` — add `liveRegion` semantics
- `core/designsystem/src/main/java/**/component/ImageUploadZone.kt` — fix recomposition bug
- `core/designsystem/src/main/java/**/component/WadjetFullLoader.kt` — render `message` parameter
- `core/designsystem/src/main/java/**/animation/BorderBeam.kt` — delete `Modifier.borderBeam()` dead code
- `core/designsystem/src/main/java/**/animation/MeteorShower.kt` — fix stagger with `initialStartOffset`
- `core/designsystem/src/main/java/**/animation/FadeUp.kt` — use proportional offset
- `core/designsystem/src/main/java/**/animation/KenBurnsImage.kt` — use dp-based translation
- ALL screen files — replace `MaterialTheme.shapes.X` calls (currently `RoundedCornerShape(12.dp)` hardcoded) — can be done incrementally
- ALL screen files — replace hardcoded `Color(0xFF...)` with `WadjetColors` tokens

**New files:**
- `core/designsystem/src/main/java/**/component/WadjetAsyncImage.kt` — shared image component
- `core/designsystem/src/main/java/**/component/WadjetSearchBar.kt` — search bar component

---

## Phase 2: String Extraction & Localization Infrastructure
**Goal:** Extract all hardcoded strings to resources. Create Arabic stub files.
**Dependencies:** Phase 1

**Files affected:**
- `app/src/main/res/values/strings.xml` — add all app-level strings
- `feature/auth/src/main/res/values/strings.xml` — create, add auth strings
- `feature/landing/src/main/res/values/strings.xml` — create, add landing strings
- `feature/scan/src/main/res/values/strings.xml` — create, add scan strings
- `feature/explore/src/main/res/values/strings.xml` — create, add explore strings
- `feature/dictionary/src/main/res/values/strings.xml` — create, add dictionary strings
- `feature/chat/src/main/res/values/strings.xml` — create, add chat strings
- `feature/stories/src/main/res/values/strings.xml` — create, add stories strings
- `feature/dashboard/src/main/res/values/strings.xml` — create, add dashboard strings
- `feature/settings/src/main/res/values/strings.xml` — create, add settings strings
- `feature/feedback/src/main/res/values/strings.xml` — create, add feedback strings
- `core/designsystem/src/main/res/values/strings.xml` — create, add shared component strings (Back, Try Again, etc.)
- ALL screen composables — replace string literals with `stringResource(R.string.x)`
- ALL component composables — replace hardcoded strings with `stringResource(R.string.x)`

**New files:**
- `*/src/main/res/values-ar/strings.xml` — Arabic stub files (per module)

---

## Phase 3: Navigation, Platform Polish & Accessibility
**Goal:** Fix navigation architecture (per-tab back stacks, anti-double-nav, quick-settings dialog), add splash screen API, predictive back, and accessibility.
**Dependencies:** Phase 2

**Files affected:**
- `app/src/main/java/**/MainActivity.kt` — add `installSplashScreen()`, reactive `enableEdgeToEdge()`, remove floating avatar, add TopAppBar with profile + gear icons, add per-tab back stack preservation (`saveState`/`restoreState`)
- `app/src/main/java/**/navigation/WadjetNavGraph.kt` — add DictionarySign → Write deep link, add `launchSingleTop = true` to all non-tab navigate calls
- `app/src/main/res/values/themes.xml` — remove deprecated `statusBarColor`/`navigationBarColor`
- `app/src/main/AndroidManifest.xml` — add `android:enableOnBackInvokedCallback="true"`
- `feature/auth/src/main/java/**/WelcomeScreen.kt` — deduplicate Google Sign-In, show errors
- `feature/auth/src/main/java/**/RegisterSheet.kt` — add confirm password toggle + mismatch indicator, fix spacing, replace hardcoded green
- `feature/auth/src/main/java/**/LoginSheet.kt` — add keyboard actions (Next/Done)
- `feature/auth/src/main/java/**/ForgotPasswordSheet.kt` — handle missing email app error
- `feature/chat/src/main/java/**/ChatScreen.kt` — fix IME inset handling (Jetchat pattern), fix dead `localTts` map
- `feature/scan/src/main/java/**/ScanScreen.kt` — remove dead `visible` state, remove empty `LaunchedEffect`
- `feature/scan/src/main/java/**/ScanHistoryScreen.kt` — add undo snackbar on delete, replace hardcoded colors
- `feature/explore/src/main/java/**/ExploreScreen.kt` — replace raw TextField with WadjetSearchBar, add contentDescription, replace hardcoded colors
- `feature/explore/src/main/java/**/LandmarkDetailScreen.kt` — fix GalleryTab nested scroll, replace hardcoded colors
- `feature/explore/src/main/java/**/IdentifyScreen.kt` — add warning banner contentDescriptions
- `feature/stories/src/main/java/**/StoriesScreen.kt` — replace hardcoded gradient colors with tokens
- `feature/dashboard/src/main/java/**/DashboardScreen.kt` — fix slug display, fix hardcoded 5 chapters, show scan thumbnails, add avatar image loading
- `feature/settings/src/main/java/**/SettingsScreen.kt` — use BuildConfig.VERSION_NAME, add avatar image
- `feature/feedback/src/main/java/**/FeedbackScreen.kt` — replace Box.clickable submit with WadjetButton
- `app/src/main/res/values/colors.xml` — remove unused template colors
- `core/ui/src/main/java/**/WadjetPreviews.kt` — add RTL and font-scale preview annotations

**New files:**
- `feature/settings/src/main/java/**/SettingsQuickDialog.kt` — quick-settings dialog (TTS, language, cache) triggered from TopAppBar gear icon (NiA `SettingsDialog.kt` pattern)
- `app/src/main/java/**/navigation/NavUtils.kt` — `NavBackStackEntry.lifecycleIsResumed()` extension (Jetsnack anti-double-nav pattern)

---

## Phase 4: Interaction & Content UX Improvements
**Goal:** Polish interactions, error handling, content-specific UX, and enrich the Hieroglyphs hub.
**Dependencies:** Phase 3

**Files affected:**
- `feature/landing/src/main/java/**/LandingScreen.kt` — add loading/error states, add pull-to-refresh, fix Write QuickAction icon
- `feature/scan/src/main/java/**/ScanResultScreen.kt` — add EN/AR toggle contentDescription
- `feature/stories/src/main/java/**/StoryReaderScreen.kt` — standardize error handling to toast (not snackbar), add glyph option contentDescriptions
- `feature/chat/src/main/java/**/ChatScreen.kt` — remove dead `localTts` code, fix `formatRelativeTime` remember
- `feature/dictionary/src/main/java/**/DictionarySignScreen.kt` — add "Practice Writing" navigation
- `app/src/main/java/**/navigation/WadjetNavGraph.kt` — wire DictionarySign → Dictionary(tab=2, prefill) route
- `app/src/main/java/**/screen/HieroglyphsHubScreen.kt` — enrich with dynamic content (recent scans, learning streak, suggested signs)
- `core/designsystem/src/main/java/**/component/WadjetBottomBar.kt` — delete if dead code; route list already in `TopLevelDestination.kt`

**New files:**
- `app/src/main/java/**/screen/HieroglyphsHubViewModel.kt` — ViewModel for dynamic hub content (recent scans, streak, suggestions)

---

## Phase 5: Visual Polish, Transitions & Adaptive Layout
**Goal:** Add shared element transitions, adaptive layouts, and animation fixes.
**Dependencies:** Phase 4

**Files affected:**
- `app/src/main/java/**/navigation/WadjetNavGraph.kt` — wrap NavHost in `SharedTransitionLayout`, add `sharedBounds` keys for landmark/story transitions
- `app/src/main/java/**/MainActivity.kt` — integrate `NavigationSuiteScaffold` using `WindowSizeClass`
- `feature/explore/src/main/java/**/ExploreScreen.kt` — add `sharedBounds` on landmark cards
- `feature/explore/src/main/java/**/LandmarkDetailScreen.kt` — add `sharedBounds` on hero image
- `feature/stories/src/main/java/**/StoriesScreen.kt` — add `sharedBounds` on story cards
- `feature/stories/src/main/java/**/StoryReaderScreen.kt` — add `sharedBounds` on chapter header
- `core/designsystem/src/main/java/**/animation/ShineSweep.kt` — use measured width instead of absolute pixels
- `core/designsystem/src/main/java/**/animation/GoldGradientText.kt` — use measured width
- `core/designsystem/src/main/java/**/animation/GoldGradientSweep.kt` — use measured width
- `core/designsystem/src/main/java/**/animation/DotPattern.kt` — convert to dp-based spacing
- `core/designsystem/src/main/java/**/component/ShimmerEffect.kt` — use measured width
- `core/designsystem/src/main/java/**/animation/ButtonShimmer.kt` — use measured width for sweep
- `core/designsystem/src/main/java/**/animation/GoldPulse.kt` — add animation label for debuggability

---

## Complexity Tracking

| Phase | Files Modified | New Files | New Components | Risk |
|-------|---------------|-----------|---------------|------|
| 1 | ~25 | 2 | WadjetAsyncImage, WadjetSearchBar | Med — touches design system core |
| 2 | ~30 | ~12 | None | Low — mechanical string extraction |
| 3 | ~20 | 2 (SettingsQuickDialog, NavUtils) | SettingsQuickDialog | Med-High — nav architecture + splash + insets |
| 4 | ~9 | 1 (HieroglyphsHubViewModel) | None | Med — hub enrichment adds ViewModel |
| 5 | ~14 | 0 | None | High — shared transitions + adaptive layout |
