# UX Gap Analysis: Wadjet Android

## Summary

The Wadjet Android app has a strong **Egyptian identity** (dark+gold, hieroglyphic visuals, themed empty states) and a solid module architecture. However, the UI/UX has **69 findings** across 13 analysis dimensions. The top 5 critical issues are:

1. **Zero string localization** — every user-facing string is hardcoded in composables; no `strings.xml` infrastructure exists for English or Arabic
2. **Arabic typography never activates** — `wadjetTypographyForLang()` is defined but never wired into `WadjetTheme`; PlayfairDisplay headings will render as tofu for Arabic users
3. **Chat keyboard behavior** — IME insets not properly excluded from Scaffold; keyboard pushes content instead of animating with the input bar (contrast: Jetchat pattern)
4. **Nested scrolling conflict** — `LazyVerticalGrid` inside `verticalScroll` on LandmarkDetail GalleryTab causes measurement issues
5. **Design system shapes unused** — `WadjetShapes` is defined but every component hardcodes `RoundedCornerShape(12.dp)` directly, defeating token-based theming

## Methodology

- Read every screen composable (25 screens + 4 bottom sheets), all 18 design system components, all 10 animations, 5 theme files, resource/build files
- Cross-referenced with Now in Android (navigation, scaffold, design system), Jetsnack (transitions, bottom nav), Jetchat (chat keyboard handling, bubbles), Reply (adaptive layouts), Pokedex Compose (detail screen patterns)
- Evaluated against 12 analysis dimensions (A–L) defined in the audit prompt

---

## Findings

### A. Information Architecture & Screen Hierarchy

#### UX-001 | 🟠 Major | Dashboard access via floating avatar is non-discoverable
- **Current state:** `MainActivity.kt` — a 40dp `IconButton` with `Icons.Default.AccountCircle` floats in top-right corner on bottom-nav screens. No tooltip, no label, no animation drawing attention.
- **Problem:** New users have no indication this icon leads to Dashboard/Profile. It overlaps content. Standard pattern is a dedicated Profile tab in the bottom nav.
- **Recommendation:** Replace the floating avatar with a 6th bottom nav item (Profile/Dashboard), or add it as an action in a top app bar. Reference: NiA uses `TopAppBar` actions for settings gear; Jetsnack uses a dedicated "Profile" bottom tab.
- **Reference:** `compose-samples/Jetsnack/` `HomeSections.PROFILE` bottom tab

#### UX-002 | 🟠 Major | HieroglyphsHub intermediate screen is an anti-pattern
- **Current state:** `HieroglyphsHubScreen.kt` — Hieroglyphs bottom tab opens a hub with 3 cards (Scan, Dictionary, Write) plus a static explainer section.
- **Problem:** Users must tap Hieroglyphs tab → then tap Scan/Dictionary/Write. This is 2 taps to reach core features. The hub is a pure menu with zero dynamic content. **No reference app uses hub/menu screens as tab destinations** — NiA (3 tabs, all go directly to content), Jetsnack (4 tabs, Feed goes to scrollable product grid), Reply (4 tabs, Inbox goes to email list), both Pokedex apps (flat list → detail, no intermediate screen), architecture-samples (2 destinations, no hub). Hub screens are an anti-pattern in modern navigation.
- **Recommendation:** Enrich the hub with dynamic content: recent scans carousel, learning streak card, suggested signs, scan count stats — transforming it from a dead menu into a personalized "Hieroglyphs home" (like NiA's ForYou feed). This justifies the screen's existence and the extra tap.
- **Reference:** NiA `ForYouScreen` — personalized feed as tab content, no intermediate menu. Jetsnack `Feed.kt` — dynamic category strips + featured items.

#### UX-003 | 🟡 Minor | Dictionary has 3 nested tabs (Browse/Learn/Write) — Write feels misplaced
- **Current state:** `DictionaryScreen.kt` — 3 tabs inside the Dictionary screen. Write tab accesses the AI transliteration feature (English → hieroglyphs).
- **Problem:** "Write in Hieroglyphs" is a creative/productive feature — conceptually different from "browse a dictionary" or "take a lesson." Users looking for the write feature may not think to look under Dictionary.
- **Recommendation:** Keep it as a Dictionary tab for now but ensure the tab label is "Write" not "Translate" (already done in spec 002). No change needed.
- **Reference:** N/A (no change recommended).

#### UX-004 | 🟠 Major | Settings and Feedback are buried too deep (3-4 taps)
- **Current state:** `DashboardScreen.kt` settings icon → `SettingsScreen.kt` → "Send Feedback" link → `FeedbackScreen.kt`
- **Problem:** Settings requires: floating avatar → Dashboard → Settings = 3 taps. Feedback = 4 taps. Both exceed the ≤2 tap rule. NiA solves this by showing Settings as a **dialog** triggered from a TopAppBar gear icon — 1 tap from any screen. Jetsnack puts Profile as a bottom tab (1 tap). No reference app buries settings 3+ levels deep.
- **Recommendation:** (1) Move Dashboard to TopAppBar action icon (UX-001 fix). (2) Add a gear icon in TopAppBar that opens a quick-settings dialog for common settings (TTS toggle, language, clear cache) — NiA pattern from `SettingsDialog.kt`. Full SettingsScreen remains accessible from Dashboard for detailed settings. This gives settings access in 1 tap (quick dialog) or 2 taps (full screen).
- **Reference:** NiA `feature/settings/impl/.../SettingsDialog.kt` — Settings as AlertDialog from TopAppBar gear icon.

#### UX-005 | 🔵 Enhancement | No global search
- **Current state:** Search exists only in ExploreScreen (landmarks) and DictionaryScreen (browse tab). No unified search across all content.
- **Problem:** Users can't quickly search for a landmark name from the Landing screen or find a specific hieroglyph without navigating to the Dictionary first.
- **Recommendation:** Add a search bar or search icon to the Landing TopAppBar that opens a unified search overlay (landmarks + signs + stories). Reference: NiA has a search icon in TopAppBar.
- **Reference:** `nowinandroid/` `SearchScreen.kt`

---

### B. User Flow Analysis

#### UX-006 | 🟠 Major | No cross-feature flow from Scan Result → Dictionary → Practice Writing
- **Current state:** `ScanResultScreen.kt` → `GlyphDetailSheet` → "View in Dictionary" navigates to `DictionarySignScreen`. But from there, no path to practice writing that glyph exists.
- **Problem:** The user flow scan → identify glyph → learn about it → practice writing it is broken. The Write tab doesn't accept a pre-filled glyph.
- **Recommendation:** Add a "Practice Writing" button on `DictionarySignScreen` that navigates to `Dictionary(initialTab = 2)` with the sign pre-filled. Enable the Write tab to accept an initial input parameter.
- **Reference:** Jetsnack connects Snack detail → Cart → Checkout as a continuous flow; NiA connects Topic → Article → Author across features.

#### UX-007 | 🟡 Minor | Landing screen lacks loading and error states
- **Current state:** `LandingScreen.kt` — when `state.userName`, `recentScan`, or `inProgressStory` fail to load, screen shows defaults with no indication of loading or failure.
- **Problem:** User sees stale or empty personalized sections without understanding why.
- **Recommendation:** Add a shimmer skeleton for the greeting + continue sections while data loads. Show an inline retry if loading fails.
- **Reference:** NiA's `ForYouScreen` uses `NiaLoadingWheel` centered during initial load.

#### UX-008 | 🟡 Minor | Write QuickAction on Landing has broken glyph icon
- **Current state:** `LandingScreen.kt` — Write QuickAction uses `"✏"` (Unicode replacement character/emoji) as icon text.
- **Problem:** On some devices this renders as `?` (tofu) or an emoji. Inconsistent with the Egyptian iconography rule.
- **Recommendation:** Replace with a proper hieroglyph glyph (e.g., `𓏞` scribe's palette) or a Material icon `Icons.Outlined.Draw`.
- **Reference:** NiA uses only Material Icons (never Unicode emoji/glyph substitutes).

#### UX-009 | 🔵 Enhancement | No onboarding for first-time users
- **Current state:** After auth, users land on `LandingScreen` with no guidance on what to do first.
- **Problem:** Feature discoverability relies on the user exploring. The 6 quick actions ("Scan", "Explore", "Dictionary", "Write", "Identify", "Stories") may overwhelm a new user.
- **Recommendation:** Add a one-time onboarding flow (3–4 swipe cards) after first login highlighting the key features. Alternatively, add contextual tooltips on first visit to each section. Reference: Airbnb-style progressive disclosure.
- **Reference:** NiA `ForYouScreen` onboarding section with `HorizontalPager`; Jetsnack first-run featured snacks.

---

### C. Visual Consistency & Design System

#### UX-010 | 🔴 Critical | All strings hardcoded — zero localization infrastructure
- **Current state:** `app/src/main/res/values/strings.xml` contains only `<string name="app_name">Wadjet</string>`. Every button label, error message, placeholder, title, and UI text is a string literal in Kotlin composables.
- **Problem:** Arabic localization is impossible without string resources. Material lint (`StringNotInResources`) would flag hundreds of violations. Updating text requires code changes instead of resource edits.
- **Recommendation:** Extract all user-facing strings to `strings.xml` per module. Create `strings.xml (ar)` stubs. Reference: NiA uses `stringResource(R.string.x)` throughout.
- **Reference:** `nowinandroid/` — every visible string uses `R.string.*`

#### UX-011 | 🔴 Critical | Arabic typography never activates
- **Current state:** `WadjetTypography.kt` defines `wadjetTypographyForLang(lang)` that swaps body/title/label to Cairo for Arabic. But `WadjetTheme.kt` always passes `WadjetTypography` — the locale-aware version is never called.
- **Problem:** When Arabic locale is added, headings will use PlayfairDisplay (no Arabic glyphs → tofu boxes), and body text will use Inter instead of Cairo.
- **Recommendation:** Wire `wadjetTypographyForLang()` into `WadjetTheme` using `ConfigurationCompat.getLocales(resources.configuration)[0]?.language`. Also swap display/headline families to Cairo for Arabic (currently only body/title/label are swapped).
- **Reference:** NiA wires typography via `NiaTheme()`; Compose M3 guidelines show locale-aware typography in `ProvideTextStyle`.

#### UX-012 | 🔴 Critical | `displayLarge` has hardcoded `color = WadjetColors.Ivory` in TextStyle
- **Current state:** `WadjetTypography.kt` — the `displayLarge` style has `color = WadjetColors.Ivory` inside the `TextStyle`. All other styles omit color.
- **Problem:** In Material 3, TextStyle color overrides `LocalContentColor` everywhere it's used. Every `displayLarge` usage ignores its container's content color. This causes unpredictable color behavior.
- **Recommendation:** Remove `color` from the `TextStyle` definition. Apply `WadjetColors.Ivory` at the call site using `color` parameter or `CompositionLocalProvider(LocalContentColor provides WadjetColors.Ivory)`.
- **Reference:** NiA and all M3 samples never set color inside Typography TextStyles.

#### UX-013 | 🟠 Major | Hardcoded colors throughout screens
- **Current state:** Multiple screens use `Color(0xFFFF4444)` (red), `Color(0xFF4CAF50)` (green), `Color.White`, and gradient hex values directly instead of `WadjetColors` tokens.
- **Problem:** If the design system palette changes, these screens won't update. Visual consistency depends on developer discipline.
- **Affected files:** `ScanHistoryScreen.kt` (0xFFFF4444, Color.White), `ExploreScreen.kt` (0xFFFF4444), `LandmarkDetailScreen.kt` (0xFFFF4444), `RegisterSheet.kt` (0xFF4CAF50 ×2), `StoriesScreen.kt` (5 gradient hex values for difficulty levels)
- **Recommendation:** Replace all hardcoded colors with `WadjetColors` tokens. Add `WadjetColors.DifficultyBeginner`, `DifficultyIntermediate`, `DifficultyAdvanced` for story gradients.
- **Reference:** NiA centralizes all colors in `NiaColors.kt` — zero hardcoded hex in composables.

#### UX-014 | 🟠 Major | Design system shapes defined but never used
- **Current state:** `WadjetShapes.kt` defines `small=8dp, medium=12dp, large=16dp, extraLarge=24dp`. No component or screen uses `MaterialTheme.shapes.*`.
- **Problem:** Every component hardcodes `RoundedCornerShape(12.dp)`. Changing corner radii requires editing 40+ files instead of one token file.
- **Recommendation:** Migrate all components to use `MaterialTheme.shapes.medium` (12dp), `MaterialTheme.shapes.large` (16dp) etc. Start with design system components.
- **Reference:** NiA components use `MaterialTheme.shapes` tokens.

#### UX-015 | 🟠 Major | Missing `headlineSmall` in typography scale
- **Current state:** Typography defines `headlineMedium` (20sp) and `titleLarge` (18sp) but no `headlineSmall` (M3 default: 24sp).
- **Problem:** Many screens use `MaterialTheme.typography.headlineSmall` (e.g., `LandmarkDetailScreen`, `ScanResultScreen`, `StoryReaderScreen`, `IdentifyScreen`). Without a definition, M3 falls back to a default style that doesn't use the Wadjet font families — visual inconsistency.
- **Recommendation:** Add `headlineSmall` to `WadjetTypography` — suggest `PlayfairDisplay SemiBold, 22sp, 28sp` to bridge the gap.
- **Reference:** M3 Typography guidelines define all 15 type scale slots; NiA defines the complete set in `NiaTypography`.

#### UX-016 | 🟠 Major | 18 components insufficient — missing critical patterns
- **Current state:** Design system has: WadjetButton (4 variants), WadjetCard (2), WadjetTextField, WadjetTopBar, WadjetBottomBar, WadjetBadge, WadjetToast, TtsButton, StreamingDots, ShimmerEffect, ShimmerPlaceholders (3), LoadingOverlay, EmptyState, ErrorState, OfflineIndicator, ImageUploadZone, WadjetSectionLoader, WadjetFullLoader.
- **Problem:** Missing components that every professional app needs:
  - **WadjetAsyncImage** (unified image loading with placeholder/error/crossfade — currently each screen manages Coil individually)
  - **WadjetSearchBar** (ExploreScreen uses raw `TextField`, inconsistent with WadjetTextField)
  - **WadjetFilterChipRow** (duplicate chip logic in Explore, Stories, Feedback)
  - **WadjetSnackbar** (currently half the screens use Toast, half use Snackbar — no shared style)
  - **WadjetTab/TabRow** wrapper (tab styling duplicated across Dictionary, LandmarkDetail, Dashboard)
  - **WadjetIconButton** variants (consistent sizing for top bar actions)
- **Recommendation:** Add these 6 components. Priority: WadjetAsyncImage (used by 10+ screens), WadjetSearchBar, WadjetSnackbar.
- **Reference:** NiA has `DynamicAsyncImage`, `NiaTopAppBar`, `NiaNavigationBar`, `NiaFilterChip`, `NiaTab`.

#### UX-017 | 🟡 Minor | `WadjetBottomBar` has feature route strings in design system module
- **Current state:** `WadjetBottomBar.kt` in `core/designsystem` contains `bottomNavItems` with hardcoded route strings (`"landing"`, `"scan"`, etc.).
- **Problem:** `core/designsystem` should not know about feature routes. This breaks module boundaries.
- **Recommendation:** Move the route list to `app/navigation/` (which already manages `TopLevelDestination`). The design system should only provide the visual `NavigationBar` wrapper. Note: `MainActivity.kt` already has its own `WadjetBottomBar` — the design system version appears to be dead code.
- **Reference:** NiA `NiaNavigationBar` in design system contains zero route references — routing is in `app/navigation/`.

#### UX-018 | 🟡 Minor | Inconsistent spacing between auth sheets
- **Current state:** `LoginSheet.kt` uses `spacedBy(16.dp)`, `RegisterSheet.kt` uses `spacedBy(12.dp)`.
- **Problem:** Sheets that share the same visual context (ModalBottomSheet for auth) should have identical internal spacing.
- **Recommendation:** Standardize to `spacedBy(16.dp)` for both sheets.
- **Reference:** M3 Bottom Sheet guidelines use consistent 16dp internal spacing.

#### UX-019 | 🟡 Minor | `colors.xml` contains unused template defaults
- **Current state:** `app/src/main/res/values/colors.xml` has purple_200/500/700, teal_200/700, black, white — Android project template leftovers.
- **Problem:** Dead code clutter. No composable references these XML colors.
- **Recommendation:** Remove all entries from `colors.xml` (the file can stay empty or be deleted).
- **Reference:** NiA has no `colors.xml` entries — all colors are in Compose `Color.kt`.

#### UX-020 | 🟡 Minor | `Dust` and `Warning` color tokens unused
- **Current state:** `WadjetColors.kt` defines `Dust = #8B7355` and `Warning = #F59E0B`. Neither is used by any design system component.
- **Problem:** Dead tokens add confusion. `Warning` is used in `ScanResultScreen.kt` but accessed directly — it should be documented.
- **Recommendation:** `Warning` is used in screens, so document its usage. For `Dust`, it IS used in `LandingScreen.kt` footer text and `ScanHistoryScreen.kt` date text — document or rename to clarify purpose.
- **Reference:** NiA color tokens all have KDoc usage comments; unused tokens are removed.

#### UX-021 | 🟡 Minor | `TextDim` and `TextMuted` nearly indistinguishable
- **Current state:** `TextDim = #7E7E7E`, `TextMuted = #8A8A8A` — differ by 12 luminance units.
- **Problem:** On screens, these look identical. Two near-identical tokens cause confusion about which to use.
- **Recommendation:** Remove `TextDim` and use only `TextMuted`. If a dimmer text level is needed, increase the gap (e.g., `#606060`).
- **Reference:** M3 defines exactly 3 on-surface content colors (primary, secondary, tertiary) — not 2 near-duplicates.

---

### D. Interaction Design

#### UX-022 | 🟠 Major | No swipe-to-dismiss on Toast
- **Current state:** `WadjetToast.kt` — toast auto-dismisses after `durationMs` but cannot be swiped away.
- **Problem:** Toasts cover content. Users can't dismiss them early. On small screens, a 3-second toast blocking the bottom is frustrating.
- **Recommendation:** Add `SwipeToDismissBox` or `Modifier.swipeable` to the toast composable.
- **Reference:** M3 `SwipeToDismissBox` API; Jetsnack dismissible cart items.

#### UX-023 | 🟠 Major | Scan history delete has no undo
- **Current state:** `ScanHistoryScreen.kt` — `SwipeToDismissBox` fires `onDelete()` immediately on swipe complete. No confirmation dialog, no undo snackbar.
- **Problem:** Accidental swipe permanently deletes a scan record. Destructive actions should be reversible.
- **Recommendation:** Show a "Scan deleted" snackbar with "Undo" action. Delay actual deletion until snackbar timeout. Reference: Gmail's swipe-to-archive with undo.
- **Reference:** M3 `SwipeToDismissBox` + `Snackbar` undo pattern; Reply sample uses Snackbar undo for archive.

#### UX-024 | 🟡 Minor | WadjetButton uses wrong haptic type
- **Current state:** `WadjetButton.kt` — triggers `HapticFeedbackType.LongPress` on every tap.
- **Problem:** `LongPress` haptic is a heavy vibration intended for long-press context menus. Button taps should use a lighter feedback or none (the system provides tap feedback automatically).
- **Recommendation:** Remove explicit haptic from button tap handler. If haptic is desired, use `HapticFeedbackType.TextHandleMove` (lighter).
- **Reference:** M3 Button implementation provides no explicit haptic — system tap feedback suffices.

#### UX-025 | 🟡 Minor | `WadjetCardGlow` hover animation doesn't work on touch devices
- **Current state:** `WadjetCard.kt` — `WadjetCardGlow` uses `collectIsHoveredAsState()` for border glow + `collectIsPressedAsState()`.
- **Problem:** Hover events don't fire on touch screens (only mouse/stylus). The glow effect is invisible to 99% of users on phones.
- **Recommendation:** Replace hover detection with press detection only. Or add a periodic `shineSweep` animation as the visual enhancement.
- **Reference:** Jetsnack card animations use press-based `Modifier.clickable` interactions, not hover.

#### UX-026 | 🟡 Minor | No pull-to-refresh on Landing screen
- **Current state:** `LandingScreen.kt` — `LazyColumn` with no `PullToRefreshBox`. Stories, Explore, Dashboard all have pull-to-refresh.
- **Problem:** If personalized data (recent scan, in-progress story) is stale, user has no way to refresh without leaving and returning.
- **Recommendation:** Wrap `LazyColumn` in `PullToRefreshBox` and add an `onRefresh` callback.
- **Reference:** NiA `ForYouScreen` uses `PullToRefreshBox` on its primary `LazyColumn`.

---

### E. Accessibility

#### UX-027 | 🔴 Critical | Toast messages not announced to TalkBack
- **Current state:** `WadjetToast.kt` — visual toast with no `semantics` annotation or `LiveRegion` modifier.
- **Problem:** Blind or low-vision users using TalkBack never hear toast messages (success, error, info). They miss critical feedback.
- **Recommendation:** Add `Modifier.semantics { liveRegion = LiveRegionMode.Polite }` to the toast root. Or use `Snackbar` (which has built-in accessibility announcements).
- **Reference:** M3 `Snackbar` implementation uses `LiveRegion.Polite` by default; NiA uses Snackbar for all transient messages.

#### UX-028 | 🟠 Major | Multiple tappable surfaces lack semantic roles
- **Current state:** `FeedbackScreen.kt` submit is `Box.clickable` (no `Role.Button`). `DashboardScreen.kt` "Remove" favorite is `Text.clickable`. Multiple card `Surface.clickable` have no role.
- **Problem:** Screen readers announce these as "double-tap to activate" without identifying what they are. Users can't distinguish buttons from links from cards.
- **Recommendation:** Add `Modifier.semantics { role = Role.Button }` to all clickable actions. Better: use `WadjetButton` or `IconButton` composables which include roles.
- **Reference:** NiA uses `IconButton` and `TextButton` for all clickable actions — semantic roles are provided automatically.

#### UX-029 | 🟠 Major | No content descriptions on interactive elements
- **Current state:** Found across multiple screens:
  - ExploreScreen `SearchBar TextField` — no label or description
  - ExploreScreen `LandmarkCard` clickable surface — no role
  - ScanResultScreen EN/AR language toggle — no description
  - IdentifyScreen warning banners — icon `contentDescription = null`
  - StoryReaderScreen glyph option boxes — no description
- **Problem:** Screen reader users can't navigate these interactive elements meaningfully.
- **Recommendation:** Add `contentDescription` to all interactive elements. Add `label` to TextField.
- **Reference:** NiA provides `contentDescription` on every `Icon` and `Image`; M3 accessibility guidelines require labels on all `TextField` components.

#### UX-030 | 🟡 Minor | TtsButton touch target below 48dp minimum
- **Current state:** `TtsButton.kt` — `IconButton` with `Modifier.size(32.dp)`.
- **Problem:** While `IconButton` internally provides a 48dp touch ripple, the bounded `size(32.dp)` clips it. WCAG/Material guidelines require ≥48dp touch targets.
- **Recommendation:** Remove `Modifier.size(32.dp)` or increase to `Modifier.size(48.dp)` with `padding` around the icon.
- **Reference:** M3 accessibility: minimum 48dp touch target; NiA `IconButton` uses default 48dp.

#### UX-031 | 🟡 Minor | OfflineIndicator not announced to TalkBack
- **Current state:** `OfflineIndicator.kt` — visual banner with no `Role.Status` or `liveRegion` semantics.
- **Problem:** Network status changes are invisible to screen reader users.
- **Recommendation:** Add `Modifier.semantics { liveRegion = LiveRegionMode.Polite; contentDescription = "No internet connection" }`.
- **Reference:** NiA `OfflineBanner` uses `semantics` with `liveRegion`.

#### UX-032 | 🟡 Minor | All hardcoded `contentDescription = "Back"` are English-only
- **Current state:** `WadjetTopBar.kt` and every screen with a back icon use `contentDescription = "Back"`.
- **Problem:** When Arabic locale is added, screen readers will still announce "Back" in English.
- **Recommendation:** Use `stringResource(R.string.navigate_back)` — but this requires UX-010 (string extraction) first.
- **Reference:** NiA uses `stringResource(R.string.core_ui_back)` for all back button descriptions.

---

### F. Responsive & Adaptive Layout

#### UX-033 | 🟠 Major | No WindowSizeClass or adaptive layout support
- **Current state:** `MainActivity.kt` — no `calculateWindowSizeClass()` usage. Every screen uses phone-only layouts.
- **Problem:** On tablets (10"+), screens will have large empty margins or stretched content. On foldables, no dual-pane support.
- **Recommendation:** Add `calculateWindowSizeClass()` in `MainActivity` and pass to Scaffold. Use `NavigationSuiteScaffold` (auto-switches bottom bar to rail/drawer). For Explore list/detail — add `TwoPane` layout on expanded width.
- **Reference:** NiA `NiaNavigationSuiteScaffold`, Reply `TwoPane` with `HorizontalTwoPaneStrategy`.

#### UX-034 | 🟡 Minor | No landscape mode consideration
- **Current state:** No screen handles landscape orientation. `WadjetPreviews.kt` has a landscape preview but no composable adapts to it.
- **Problem:** In landscape, vertical lists with top padding waste screen estate. Chat keyboard covers most of the screen.
- **Recommendation:** For Phase 1, handle landscape chat by reducing TopAppBar height and increasing list visible area. Full adaptive layouts in a later phase.
- **Reference:** Jetchat handles landscape by conditionally hiding the channel header.

#### UX-035 | 🟡 Minor | WadjetPreviews missing RTL and font scale variants
- **Current state:** `WadjetPreviews.kt` — has Phone, Landscape, Tablet, Dark previews. No RTL or large text preview.
- **Problem:** Arabic layout issues and text truncation at large font scales won't be caught during development.
- **Recommendation:** Add `@Preview(locale = "ar")` and `@Preview(fontScale = 1.5f)` to the annotation.
- **Reference:** NiA `ThemePreviews` includes dark/light + font scale variations.

---

### G. Onboarding & Discoverability

#### UX-036 | 🔵 Enhancement | No onboarding flow
- **Current state:** After auth, users land directly on `LandingScreen`. No explanation of features.
- **Problem:** New users may not discover all 6 features (Scan, Explore, Dictionary, Write, Identify, Stories) or understand the Hieroglyphs hub.
- **Recommendation:** Add an optional 3-screen onboarding carousel after first login: 1) "Scan hieroglyphs from photos," 2) "Explore 100 landmarks," 3) "Chat with Thoth, the god of wisdom." Implement with `HorizontalPager` + page indicator.
- **Reference:** NiA `ForYouScreen` uses `HorizontalPager` for onboarding topic selection.

#### UX-037 | 🔵 Enhancement | No tooltips or contextual help
- **Current state:** No tooltips, coachmarks, or "What's this?" affordances anywhere.
- **Problem:** Features like "Identify" (landmark recognition from photos) are non-obvious.
- **Recommendation:** Add one-time tooltip on the Identify icon in ExploreScreen (`"Upload a photo to identify any Egyptian landmark"`). Use `TooltipBox` (M3 component).
- **Reference:** M3 `TooltipBox` API; Jetsnack uses tooltip on filter chips.

---

### H. Micro-interactions & Polish

#### UX-038 | 🟠 Major | `ImageUploadZone` has infinite recomposition bug
- **Current state:** `ImageUploadZone.kt` — writes to `MutableState` during composition: `var localUri by remember { mutableStateOf(selectedImageUri) }; if (selectedImageUri != localUri) { localUri = selectedImageUri }`.
- **Problem:** Writing state during composition can cause an infinite recomposition loop under certain conditions (e.g., when the composition is invalidated by another source).
- **Recommendation:** Remove `localUri` entirely and use `selectedImageUri` directly as a controlled component. Or move the sync to `LaunchedEffect(selectedImageUri) { localUri = selectedImageUri }`.
- **Reference:** Compose side-effects documentation: "Never write to state during composition — use `LaunchedEffect` or event callbacks."

#### UX-039 | 🟠 Major | `MeteorShower` stagger collapses after first animation cycle
- **Current state:** `MeteorShower.kt` — uses `delayMillis` inside `tween()` within `infiniteRepeatable()`. The delay only applies to the first iteration.
- **Problem:** After the first cycle, all 5 meteors sync up and animate simultaneously — losing the staggered effect that makes it look natural.
- **Recommendation:** Use `infiniteRepeatable(tween(durationMs), initialStartOffset = StartOffset(index * period))` for persistent stagger.
- **Reference:** Compose animation docs: `initialStartOffset` is the correct API for persistent stagger in `infiniteRepeatable`.

#### UX-040 | 🟠 Major | No shared element transitions between list and detail
- **Current state:** `WadjetNavGraph.kt` — all transitions are `slideIn`/`fadeIn` combinations. No shared element animations.
- **Problem:** Tapping a landmark card or story → detail screen has no visual connection. The card disappears and a new screen slides in. Professional apps (Jetsnack, Pokedex Compose) use shared bounds transitions.
- **Recommendation:** Wrap root `NavHost` in `SharedTransitionLayout`. Add `sharedBounds(key = "landmark-$slug")` on explore cards and landmark detail header. Use `spatialExpressiveSpring()` for bounds, `nonSpatialExpressiveSpring()` for fades.
- **Reference:** `compose-samples/Jetsnack/` `SnackDetail.kt` shared element pattern

#### UX-041 | 🟡 Minor | `WadjetFullLoader` ignores `message` parameter
- **Current state:** `WadjetFullLoader.kt` — accepts `message: String?` but never renders it.
- **Problem:** Callers passing a message get silently ignored. API contract is broken.
- **Recommendation:** Add `if (message != null) Text(message, style = bodyMedium, color = TextMuted)` below the shimmer bar.
- **Reference:** NiA `NiaLoadingWheel` renders an optional message below the spinner.

#### UX-042 | 🟡 Minor | `FadeUp` uses pixel-based offset instead of proportion
- **Current state:** `FadeUp.kt` — `initialOffsetY = { 40 }` (40 physical pixels).
- **Problem:** On high-density screens (3x), this is only ~13dp. On low-density screens (1x), it's 40dp. Inconsistent visual intensity.
- **Recommendation:** Use `initialOffsetY = { it / 8 }` (proportion of component height) for density-independent behavior.
- **Reference:** Compose `AnimatedVisibility` uses proportional offsets: `slideInVertically { it }` for full-height.

#### UX-043 | 🟡 Minor | `ShineSweep`, `GoldGradientText`, `GoldGradientSweep` use pixel-based animations
- **Current state:** Multiple animations use hardcoded pixel values (`1000f`, `500px`, `2000f`).
- **Problem:** On wide tablets or high-DPI screens, the sweep doesn't cover the full width. On small screens, it overshoots.
- **Recommendation:** Use measured composable width from `onSizeChanged` or `drawBehind { size.width }` to compute sweep range dynamically.
- **Reference:** NiA animations use `drawBehind { size }` for density-independent sweep; Jetsnack card animations measure width.

#### UX-044 | 🟡 Minor | `KenBurnsImage` translationX uses raw pixels
- **Current state:** `KenBurnsImage.kt` — `translationX` animates to `20f` (raw pixels).
- **Problem:** On 3x density, 20px = ~7dp — barely visible pan effect.
- **Recommendation:** Use `20.dp.toPx()` or `with(LocalDensity.current) { 20.dp.toPx() }`.
- **Reference:** Jetsnack `KenBurnsImage` equivalent uses `LocalDensity.current` for all translation values.

#### UX-045 | 🟡 Minor | Keyboard IME not handled in auth forms
- **Current state:** `LoginSheet.kt`, `RegisterSheet.kt` — `TextField`s use default keyboard options. No `imeAction = ImeAction.Next` on email → password, no `ImeAction.Done` on last field.
- **Problem:** Users can't use the "Next"/"Done" keyboard action to navigate between fields or submit the form.
- **Recommendation:** Wire `KeyboardOptions(imeAction = ImeAction.Next)` on email fields, `ImeAction.Done` on last password field with `KeyboardActions(onDone = { onSignIn(...) })`.
- **Reference:** Jetchat input bar uses `ImeAction.Send` with `KeyboardActions(onSend = ...)`; same pattern applies to auth forms.

---

### I. Auth & Onboarding UX

#### UX-046 | 🟠 Major | Google Sign-In code duplicated 3 times
- **Current state:** `WelcomeScreen.kt` — the `credentialManager.getCredential` block is copy-pasted verbatim 3 times (main screen, LoginSheet callback, RegisterSheet callback).
- **Problem:** Any bug fix or change must be applied 3 times. This is a maintenance hazard.
- **Recommendation:** Extract to a `suspend fun performGoogleSignIn(context, credentialManager, webClientId): String` helper function.
- **Reference:** NiA extracts shared credential logic into repository/utility classes; architecture-samples deduplicate auth code.

#### UX-047 | 🟡 Minor | Google Sign-In errors not shown on WelcomeScreen
- **Current state:** `WelcomeScreen.kt` — `state.error` is passed to sheets (LoginSheet, RegisterSheet) but the main WelcomeScreen never reads or displays `state.error`. Google Sign-In errors (which happen at the WelcomeScreen level) are lost.
- **Problem:** If Google Sign-In fails outside a sheet, the user gets no feedback.
- **Recommendation:** Add an error display (inline text or toast) on `WelcomeScreen` for `state.error`.
- **Reference:** NiA shows inline error messages below action buttons.

#### UX-048 | 🟡 Minor | RegisterSheet: no confirm-password mismatch indicator
- **Current state:** `RegisterSheet.kt` — no visual feedback when password and confirm-password don't match until form submission.
- **Problem:** Users type the entire form, submit, and only then learn passwords don't match.
- **Recommendation:** Add inline validation: show error border + "Passwords don't match" when `confirmPassword.isNotEmpty() && password != confirmPassword`.
- **Reference:** M3 `TextField` supports `isError` state with `supportingText` for inline validation.

#### UX-049 | 🟡 Minor | RegisterSheet: confirm-password has no visibility toggle
- **Current state:** Only the main password field has an eye toggle. Confirm-password is always masked.
- **Problem:** Users can't verify what they typed in the confirm field.
- **Recommendation:** Add the same `IconButton` eye toggle to the confirm-password field.
- **Reference:** M3 password field pattern: both password fields should have visibility toggle.

---

### J. Content-Specific UX

#### UX-050 | 🔴 Critical | LandmarkDetail `GalleryTab` has nested scrolling conflict
- **Current state:** `LandmarkDetailScreen.kt` — `DetailContent` uses `verticalScroll`. `GalleryTab` inside uses `LazyVerticalGrid(height = 400.dp)`.
- **Problem:** `LazyVerticalGrid` inside a `verticalScroll` creates nested scrolling conflict. Items beyond 400dp are clipped. Compose will log warnings about "placing a Lazy layout inside a scrollable container."
- **Recommendation:** Replace `LazyVerticalGrid(height = 400.dp)` with a non-lazy `Column` of `Row` pairs (for 2 columns) since gallery images are typically <20. Or use `Modifier.heightIn(max = 400.dp)` with `nestedScroll`.
- **Reference:** Jetsnack detail uses a non-lazy scrollable column for similar content.

#### UX-051 | 🟠 Major | Chat insets not properly handled for keyboard
- **Current state:** `ChatScreen.kt` — uses `Scaffold` but doesn't exclude IME/navBar insets. The input bar might get hidden behind or pushed incorrectly when keyboard opens.
- **Problem:** When the keyboard opens, the input bar should animate up with it smoothly. Without proper inset exclusion, the content either jumps or the input bar is hidden.
- **Recommendation:** Apply the Jetchat pattern: `Scaffold(contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars).exclude(WindowInsets.ime))`, then add `Modifier.navigationBarsPadding().imePadding()` on the `ChatInputBar`.
- **Reference:** `compose-samples/Jetchat/` `Conversation.kt`

#### UX-052 | 🟠 Major | Chat and StoryReader handle errors differently (Toast vs Snackbar)
- **Current state:** `ChatScreen.kt` uses `ToastController` for errors. `StoryReaderScreen.kt` uses `Snackbar`. Other screens use inline `ErrorState`.
- **Problem:** Inconsistent error display patterns confuse users and make the app feel unpolished.
- **Recommendation:** Standardize on `ToastController` for ephemeral errors and inline `ErrorState` for persistent/recoverable errors. Reserve `Snackbar` for actions-with-undo only.
- **Reference:** M3 guidelines: Snackbar for undo actions, Banner/Toast for informational; NiA uses Snackbar only for actionable messages.

#### UX-053 | 🟡 Minor | Dashboard showing raw IDs as favorite names
- **Current state:** `DashboardScreen.kt` `FavoriteRow` — `favorite.itemId` (slug like "valley-of-the-kings") shown to user.
- **Problem:** Slugs are not human-readable names. Users see "valley-of-the-kings" instead of "Valley of the Kings."
- **Recommendation:** Store the human-readable name alongside the slug in the favorites data model.
- **Reference:** NiA uses display names (not slugs) for all user-facing content identifiers.

#### UX-054 | 🟡 Minor | Dashboard hardcodes 5 chapters per story
- **Current state:** `DashboardScreen.kt` `StoryProgressRow` — `(chapterIndex + 1).toFloat() / 5f`.
- **Problem:** Not all stories have 5 chapters. Progress shows >100% or incorrect values for variable-length stories.
- **Recommendation:** Use `story.totalChapters` from the data model instead of hardcoded `5f`.
- **Reference:** Pokedex Compose detail screens dynamically compute progress from model data.

#### UX-055 | 🟡 Minor | Dashboard ScanCard shows hardcoded glyph for all scans
- **Current state:** `DashboardScreen.kt` `ScanCard` — always shows `"𓀀"` regardless of actual scan content.
- **Problem:** All recent scan cards look identical.
- **Recommendation:** Show the scan's first detected glyph or thumbnail image.
- **Reference:** NiA `RecentSearchCard` shows dynamic content (topic icon + title), not a hardcoded placeholder.

#### UX-056 | 🟡 Minor | Dashboard avatar never loads image
- **Current state:** `DashboardScreen.kt` `UserHeader` and `SettingsScreen.kt` — `avatarUrl` parameter exists but `AsyncImage` is never used. Always shows initials.
- **Problem:** Users with Google profile photos never see them.
- **Recommendation:** Add `AsyncImage(avatarUrl)` with initials as fallback.
- **Reference:** NiA `DynamicAsyncImage` with `placeholder` and `error` drawables as fallback.

---

### K. Android Platform Polish

#### UX-057 | 🟠 Major | `themes.xml` uses deprecated `statusBarColor`
- **Current state:** `themes.xml` — `<item name="android:statusBarColor">#FF0A0A0A</item>`.
- **Problem:** `statusBarColor` has no effect on API 35 (target SDK). Modern apps use `enableEdgeToEdge()`.
- **Recommendation:** Remove `statusBarColor` and `navigationBarColor` from `themes.xml`. Rely on `enableEdgeToEdge()` in `MainActivity.onCreate()`.
- **Reference:** NiA `MainActivity.kt` uses `enableEdgeToEdge()` with no XML status bar color overrides.

#### UX-058 | 🟠 Major | No Android 12+ Splash Screen API usage
- **Current state:** No `installSplashScreen()` call in `MainActivity.kt`. The Route.Splash composable exists but is a fallback empty screen.
- **Problem:** On Android 12+, the system shows a default splash (white background with app icon). Without `installSplashScreen()`, there's a jarring white flash before the dark theme loads.
- **Recommendation:** Add `installSplashScreen()` before `super.onCreate()`. Use `splashScreen.setKeepOnScreenCondition { !authReady }` to gate on auth state.
- **Reference:** NiA `MainActivity.kt` — `installSplashScreen()` with condition

#### UX-059 | 🟡 Minor | No predictive back gesture support
- **Current state:** No `android:enableOnBackInvokedCallback="true"` in manifest. No custom `BackHandler` with predictive back animations.
- **Problem:** On Android 14+, the system can show a preview of the previous screen during back gesture. Without opt-in, this feature is disabled.
- **Recommendation:** Add `android:enableOnBackInvokedCallback="true"` to `<application>` in `AndroidManifest.xml`. Compose Navigation 2.8+ supports predictive back automatically when this flag is set.
- **Reference:** NiA `AndroidManifest.xml` includes `enableOnBackInvokedCallback="true"`.

#### UX-060 | 🟡 Minor | Version string hardcoded in Settings
- **Current state:** `SettingsScreen.kt` — `"Wadjet v1.0.0-beta"` as string literal.
- **Problem:** Version never updates with builds.
- **Recommendation:** Use `BuildConfig.VERSION_NAME` and display `"Wadjet v${BuildConfig.VERSION_NAME}"`.
- **Reference:** NiA `SettingsDialog` uses `BuildConfig.VERSION_NAME` for version display.

---

### L. Performance & Smoothness (UI Layer)

#### UX-061 | 🟠 Major | `Modifier.borderBeam()` is broken dead code
- **Current state:** `BorderBeam.kt` — `Modifier.borderBeam()` extension is defined but its `drawWithContent` block exits without drawing anything. Comment admits it "can't hold infinite transition state" in a modifier.
- **Problem:** Dead code. Any callsite that uses the modifier instead of the composable wrapper gets no visual effect.
- **Recommendation:** Delete the `Modifier.borderBeam()` function. Keep only the `@Composable BorderBeam(...)` wrapper.
- **Reference:** Compose custom drawing guidelines: modifier-based animations needing `Composition` scope should use composable wrappers, not `Modifier.composed {}`.

#### UX-062 | 🟡 Minor | `ShimmerEffect` pixel-hardcoded sweep range
- **Current state:** `ShimmerEffect.kt` — sweep range `-1000f to 1000f`, 500px gradient window.
- **Problem:** On tablets, the shimmer barely covers the composable before restarting. On small phones, it overshoots.
- **Recommendation:** Use `drawBehind { size.width }` to compute the sweep range relative to the actual composable width.
- **Reference:** NiA `loadingWheel` animation computes sweep range from measured size.

#### UX-063 | 🟡 Minor | `ButtonShimmer` not density-aware
- **Current state:** `ButtonShimmer.kt` — shimmer animation runs across the button surface.
- **Problem:** Uses pixel-based sweep similar to `ShineSweep`. On high-DPI tablets the sweep range may not cover the full button width proportionally.
- **Recommendation:** Use measured composable width from `drawBehind { size.width }` to compute sweep range dynamically, same pattern as UX-062.
- **Reference:** NiA animation patterns use measured dimensions.

#### UX-064 | 🟡 Minor | `DotPattern` uses pixel-based dot spacing
- **Current state:** `DotPattern.kt` — `dotRadius = 1.5f` and `spacing = 24f` are raw pixel values.
- **Problem:** On high-DPI screens (`xxhdpi`), dots are tiny and tightly spaced. On `mdpi`, they're too large and spread apart. Density-independent values are required.
- **Recommendation:** Convert to dp-based values: `with(LocalDensity.current) { 1.dp.toPx() }` for radius, `8.dp.toPx()` for spacing.
- **Reference:** Jetsnack `AnimatedBorder` uses `LocalDensity.current` for all pixel computations.

#### UX-065 | 🟡 Minor | `GoldPulse` animation has no obvious issues
- **Current state:** `GoldPulse.kt` — pulsing gold glow animation used on featured elements.
- **Problem:** No critical bugs found. Animation is density-independent (uses `alpha` and `scale` — inherently proportional). Minor: could benefit from `label` parameter on `animateFloatAsState` for Compose tooling inspection.
- **Recommendation:** Add `label = "goldPulse"` to animation spec for debuggability. Low priority.
- **Reference:** Compose animation debugging guidelines recommend labeling all animation specs.

---

### M. Navigation Architecture Patterns (NEW — from deep-dive)

#### UX-066 | 🟠 Major | No per-tab back stack preservation
- **Current state:** `WadjetNavGraph.kt` — single flat `NavHost` with all 23 routes. Tab switches use `navController.navigate(Route.Landing) { popUpTo(...) }` but do not use `saveState = true` / `restoreState = true`.
- **Problem:** When a user navigates Home → Scan → ScanResult, then switches to Explore tab and back to Home — the Scan → ScanResult stack is lost. User starts at Landing again. Every reference app with bottom tabs preserves per-tab state: NiA uses `NavigationState` with `subStacks: Map<NavKey, NavBackStack<NavKey>>` for independent per-tab back stacks. Jetsnack uses `saveState = true` + `restoreState = true` on tab navigate. Reply uses the same pattern. This is the **#1 expected behavior** for bottom-tab navigation.
- **Recommendation:** Add `saveState = true` and `restoreState = true` to all bottom-tab navigation calls in `MainActivity.kt`. Ensure `launchSingleTop = true` is set. This is a one-line fix per tab navigate call.
- **Reference:** NiA `core/navigation/Navigator.kt` — per-tab independent `NavBackStack`. Jetsnack `JetsnackNavController.kt` — `popUpTo(startDestination) { saveState = true } + restoreState = true`.

#### UX-067 | 🟡 Minor | No anti-double-navigation guard
- **Current state:** `WadjetNavGraph.kt` — navigation callbacks like `onLandmarkTap = { slug -> navController.navigate(Route.LandmarkDetail(slug)) }` have no guard against rapid double-taps.
- **Problem:** Fast double-tapping a card or button can push the destination twice onto the back stack, causing duplicate screens. Jetsnack solves this with a `lifecycleIsResumed()` guard: navigation only proceeds if the current `NavBackStackEntry.lifecycle.currentState == Lifecycle.State.RESUMED`.
- **Recommendation:** Add a `NavBackStackEntry.lifecycleIsResumed()` extension. Wrap all `navController.navigate(...)` calls in detail-pushing callbacks with this guard. Alternatively, use `launchSingleTop = true` on all non-tab navigations (simpler, covers most cases).
- **Reference:** Jetsnack `JetsnackNavController.kt` lines 43-45 — `fun navigateToSnackDetail(..., from: NavBackStackEntry) { if (from.lifecycleIsResumed()) navController.navigate(...) }`.

#### UX-068 | 🟠 Major | Bottom bar visible on detail screens
- **Current state:** `MainActivity.kt` — `showBottomBar` is set based on `currentRoute in topLevelRoutes`. Detail screens (LandmarkDetail, StoryReader, ScanHistory, DictionarySign, Lesson, Identify, ChatLandmark) hide the bottom bar. **However**, the current implementation relies on checking the route string, which is fragile.
- **Problem:** Jetsnack uses a fundamentally better pattern: a **dual NavHost** (outer NavHost for full-screen destinations that hide bottom bar, inner NavHost for tab destinations that show it). This makes bottom-bar visibility structural rather than conditional. The current string-matching approach breaks if a new route is added and forgotten in the visibility check.
- **Recommendation:** Refactor to dual NavHost pattern: outer NavHost contains (1) `home` nested graph with all tab destinations + bottom bar, and (2) full-screen routes (LandmarkDetail, StoryReader, etc.) without bottom bar. This is cleaner and self-documenting.
- **Reference:** Jetsnack `JetsnackApp.kt` — outer NavHost (home + snack_detail), inner NavHost (feed/search/cart/profile). Bottom bar is inside `MainContainer` (inner), not in root.

#### UX-069 | 🟠 Major | No shared element transitions on list→detail navigation
- **Current state:** `WadjetNavGraph.kt` — transitions between screens use `slideInHorizontally + fadeIn` for all routes. No shared element animations exist.
- **Problem:** Professional apps use container transforms (shared element transitions) for list→detail navigation to maintain visual continuity. Jetsnack wraps its entire `NavHost` in `SharedTransitionLayout` and uses `sharedBounds` on snack cards → snack detail hero image. pokedex-compose wraps `NavDisplay` in `SharedTransitionLayout` for Pokemon card → detail transform. NiA uses `ListDetailSceneStrategy` for adaptive shared transitions. Wadjet's Explore→LandmarkDetail and Stories→StoryReader flows would benefit most.
- **Recommendation:** Wrap `NavHost` in `SharedTransitionLayout`. Add `sharedBounds` modifiers on Explore card image → LandmarkDetail hero image, and Stories card → StoryReader header. Use `CompositionLocalProvider` to pass `SharedTransitionScope` and `AnimatedVisibilityScope` to feature screens. (Already partially planned in T068-T070, but now informed by deeper reference analysis.)
- **Reference:** Jetsnack `JetsnackApp.kt` `SharedTransitionLayout { NavHost(...) }`. pokedex-compose `PokedexNavHost.kt` `SharedTransitionLayout { NavDisplay(...) }`.

---

## Statistics

| Severity | Count |
|----------|-------|
| 🔴 Critical | 5 |
| 🟠 Major | 25 |
| 🟡 Minor | 35 |
| 🔵 Enhancement | 4 |
| **Total** | **69** |
