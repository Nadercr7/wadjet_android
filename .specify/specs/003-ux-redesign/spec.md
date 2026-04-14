# Feature Specification: UX Redesign
**Spec ID**: 003-ux-redesign
**Status**: Draft
**Date**: 2026-04-15

## User Scenarios & Testing

### User Story 1 — Design System Foundation (Priority: P0)
As a developer, I need the design system to produce consistent UI across all screens so that visual quality matches professional apps.

**Why this priority:** 7 critical/major findings in the design system (broken typography, unused shapes, missing components, hardcoded colors) undermine every screen. Fixing the foundation first prevents rework.

**Acceptance Scenarios:**
1. Given a screen uses `MaterialTheme.typography.headlineSmall`, When it renders, Then it uses PlayfairDisplay SemiBold 22sp (not M3 default)
2. Given a screen uses `MaterialTheme.shapes.medium`, When it renders, Then it uses `RoundedCornerShape(12.dp)` from `WadjetShapes`
3. Given the app locale is Arabic, When `WadjetTheme` is applied, Then all text styles (including display/headline) use Cairo font family
4. Given any component, When it renders a color, Then no hardcoded hex values exist — only `WadjetColors` tokens
5. Given the `displayLarge` style, When applied to any Text, Then it does NOT override the container's content color

### User Story 2 — Accessibility & Localization Foundation (Priority: P0)
As a visually impaired user, I need all interactive elements to be properly announced by TalkBack so I can navigate the app independently.

**Why this priority:** 7 accessibility findings including critical toast/offline not announced. Legal compliance risk.

**Acceptance Scenarios:**
1. Given a toast appears, When TalkBack is active, Then the toast message is announced as a polite live region
2. Given any clickable surface, When TalkBack traverses it, Then its role (Button, Link, Tab) and label are announced
3. Given the offline banner shows, When TalkBack is active, Then "No internet connection" is announced
4. Given any button icon, When TalkBack reads it, Then the `contentDescription` is in the current app language
5. Given any TtsButton, When rendered, Then its touch target is ≥48dp

### User Story 3 — String Extraction for Localization (Priority: P0)
As a user switching to Arabic, I need all UI text to come from string resources so the app can be localized.

**Why this priority:** Zero localization infrastructure. Arabic is a planned feature and RTL is architecturally required. Every string must be extracted before Arabic can be added.

**Acceptance Scenarios:**
1. Given any screen composable, When it displays user-facing text, Then the text comes from `stringResource(R.string.x)`
2. Given `strings.xml`, When reviewed, Then it contains all button labels, error messages, placeholders, titles, and UI text
3. Given a `strings.xml (ar)` file exists, When the device locale is Arabic, Then all strings render in Arabic

### User Story 4 — Navigation & Dashboard Access (Priority: P1)
As a user, I need to easily access my Dashboard and Settings without hunting for a tiny floating icon.

**Why this priority:** Dashboard access is non-discoverable (UX-001). Settings requires 3 taps, violating the ≤2 tap rule.

**Acceptance Scenarios:**
1. Given I'm on any bottom-nav screen, When I look at the TopAppBar, Then I see a profile/avatar icon button and a settings gear icon
2. Given I tap the profile icon, When the Dashboard opens, Then it shows in ≤1 tap from any tab
3. Given I'm on Dashboard, When I tap Settings, Then Settings opens (2 taps total from any tab)
4. Given the floating avatar, When I look for it, Then it no longer exists (replaced by TopAppBar action)
5. Given I tap the gear icon in TopAppBar, When the quick-settings dialog opens, Then I can toggle TTS, change language, or clear cache in 1 tap
6. Given I navigate Home → Scan → ScanResult, When I switch to Explore tab and back to Home, Then the Scan → ScanResult stack is preserved (not reset to Landing)
7. Given I double-tap a landmark card rapidly, When the navigation executes, Then only ONE LandmarkDetail screen is pushed (no duplicate)

### User Story 5 — Platform Polish (Priority: P1)
As a user on Android 12+, I need the app splash and system bar behavior to feel native and polished.

**Why this priority:** White splash flash on dark app, deprecated statusBarColor, no predictive back support. These are visible quality issues.

**Acceptance Scenarios:**
1. Given Android 12+ device, When the app launches, Then `installSplashScreen()` shows a dark splash with Wadjet icon — no white flash
2. Given the app is running, When system bars are inspected, Then they use `enableEdgeToEdge()` — no deprecated `statusBarColor`
3. Given Android 14+ device, When the user swipes back, Then predictive back gesture preview is shown
4. Given `SettingsScreen`, When version is displayed, Then it shows `BuildConfig.VERSION_NAME` (not hardcoded)

### User Story 6 — Chat Keyboard UX (Priority: P1)
As a user chatting with Thoth, I need the keyboard to animate smoothly with the input bar and not hide my messages.

**Why this priority:** Chat is a primary feature (own bottom tab). Poor keyboard behavior makes it feel broken.

**Acceptance Scenarios:**
1. Given I'm on ChatScreen, When the keyboard opens, Then the input bar slides up with the keyboard smoothly (no jump)
2. Given I'm typing, When I tap "Done"/"Next", Then the keyboard action works correctly (not ignored)
3. Given messages are visible, When keyboard opens, Then the last visible message stays in view

### User Story 7 — Visual Polish & Transitions (Priority: P2)
As a user exploring landmarks, I want smooth transitions between the list and detail screens so the app feels premium.

**Why this priority:** Polish feature. Competitive with Jetsnack/Airbnb quality. Not blocking but visually impactful.

**Acceptance Scenarios:**
1. Given I tap a landmark card in Explore, When the detail screen opens, Then the card image morphs into the detail hero via shared element transition
2. Given all animations, When tested on device, Then they run at 60fps with no jank
3. Given `MeteorShower` on WelcomeScreen, When it loops, Then meteor stagger persists across all cycles (not just the first)

### User Story 8 — Adaptive Layout (Priority: P2)
As a tablet user, I need the app to use my screen space efficiently — not just stretch the phone layout.

**Why this priority:** Tablet users are a secondary audience but Google Play visibility rewards adaptive apps.

**Acceptance Scenarios:**
1. Given a tablet (>840dp width), When bottom nav is rendered, Then it shows as NavigationRail (side) instead of bottom bar
2. Given an expanded window, When Explore is shown, Then it uses a two-pane layout (list + detail)

### Edge Cases
- **Empty favorites on Dashboard** — Shows `EmptyState` with Egyptian glyph ("Your tomb of treasures awaits")
- **All stories locked** — Shows first 3 free, rest with lock icon and "Premium" label (no CTA)
- **Scan with 0 detected glyphs** — ScanResult shows AI notes + badges but no glyph grid section
- **Chat with no internet** — OfflineIndicator shows, send button disabled, previous messages remain visible
- **Auth token expiry during scan** — Automatic 401 refresh; if refresh fails, navigate to Welcome screen
- **Arabic RTL with English hieroglyph terms** — Mixed-direction text handled via `CompositionLocalProvider(LocalLayoutDirection)`

## Requirements

### Functional Requirements
- **FR-UX-001**: All user-facing strings extracted to `strings.xml` per module
- **FR-UX-002**: `wadjetTypographyForLang()` wired into `WadjetTheme` based on device locale
- **FR-UX-003**: `headlineSmall` added to `WadjetTypography` (PlayfairDisplay SemiBold 22sp)
- **FR-UX-004**: `displayLarge` TextStyle color removed (apply at call site)
- **FR-UX-005**: All components use `MaterialTheme.shapes.*` instead of hardcoded `RoundedCornerShape`
- **FR-UX-006**: All hardcoded `Color(0xFF...)` replaced with `WadjetColors` tokens
- **FR-UX-007**: `WadjetAsyncImage` component created (placeholder, error, crossfade)
- **FR-UX-008**: `WadjetSearchBar` component created (golden design system)
- **FR-UX-009**: Floating avatar replaced with TopAppBar profile action
- **FR-UX-010**: `installSplashScreen()` added to `MainActivity`
- **FR-UX-011**: Predictive back enabled (`enableOnBackInvokedCallback="true"`)
- **FR-UX-012**: Deprecated `statusBarColor`/`navigationBarColor` removed from `themes.xml`
- **FR-UX-013**: Chat IME insets properly handled (Jetchat pattern)
- **FR-UX-014**: Auth form keyboard actions (Next/Done) wired
- **FR-UX-015**: Scan history delete has undo Snackbar
- **FR-UX-016**: Toast supports swipe-to-dismiss
- **FR-UX-017**: Toast announces to TalkBack via `liveRegion`
- **FR-UX-018**: All interactive elements have `contentDescription` and semantic `role`
- **FR-UX-019**: `MeteorShower` stagger uses `initialStartOffset`
- **FR-UX-020**: `Modifier.borderBeam()` dead code deleted
- **FR-UX-021**: `WadjetFullLoader.message` parameter rendered
- **FR-UX-022**: `ImageUploadZone` recomposition bug fixed
- **FR-UX-023**: Version string uses `BuildConfig.VERSION_NAME`
- **FR-UX-024**: Google Sign-In code de-duplicated
- **FR-UX-025**: DictionarySign → Write flow connected
- **FR-UX-026**: Register sheet: confirm-password visibility toggle + mismatch indicator
- **FR-UX-027**: Per-tab back stack preservation with `saveState = true` + `restoreState = true` on all tab navigation
- **FR-UX-028**: Anti-double-navigation guard (`launchSingleTop = true` on all navigations)
- **FR-UX-029**: Quick-settings dialog accessible from TopAppBar gear icon (TTS toggle, language, clear cache)
- **FR-UX-030**: Shared element transitions on Explore→LandmarkDetail and Stories→StoryReader flows

### Non-Functional Requirements
- **NFR-UX-001**: All animations run at ≥60fps on mid-range devices (Pixel 6a equivalent)
- **NFR-UX-002**: Touch targets ≥48dp on all interactive elements
- **NFR-UX-003**: Color contrast passes WCAG AA (4.5:1 normal text, 3:1 large text) for all text on Night background
- **NFR-UX-004**: No `Color(0xFF...)` literals in any composable outside `WadjetColors.kt`
- **NFR-UX-005**: No user-facing string literals in composables (all via `stringResource()`)
- **NFR-UX-006**: Every `AsyncImage` has placeholder + error + fallback drawables
- **NFR-UX-007**: LazyColumn/LazyGrid items use `key` parameters
- **NFR-UX-008**: No nested scrollable containers (LazyColumn inside verticalScroll)
- **NFR-UX-009**: App builds with zero errors after each phase
- **NFR-UX-010**: All existing tests pass after each phase
