# UX Architecture: Wadjet Android

## Current Screen Hierarchy

```
Wadjet App (MainActivity)
├── [Auth Gate] ─ isLoggedIn?
│   ├── NO  → WelcomeScreen
│   │         ├── LoginSheet (ModalBottomSheet)
│   │         ├── RegisterSheet (ModalBottomSheet)
│   │         └── ForgotPasswordSheet (ModalBottomSheet)
│   └── YES → Landing (bottom nav start)
│
├── ══ Bottom Navigation Bar (5 tabs) ══
│   ├── 🏠 Home → LandingScreen
│   │         ├── → Scan (quick action)
│   │         ├── → Explore (quick action)
│   │         ├── → Dictionary (quick action)
│   │         ├── → Write (quick action → Dictionary tab 2)
│   │         ├── → Identify (quick action)
│   │         ├── → Stories (quick action)
│   │         ├── → Chat (quick action)
│   │         └── → StoryReader (continue reading)
│   │
│   ├── 𓂀 Hieroglyphs → HieroglyphsHubScreen
│   │         ├── → Scan
│   │         ├── → Dictionary
│   │         └── → Write (→ Dictionary tab 2)
│   │
│   ├── 🧭 Explore → ExploreScreen
│   │         ├── → LandmarkDetailScreen
│   │         │     ├── → ChatLandmark
│   │         │     ├── → LandmarkDetail (recommendations)
│   │         │     └── → LandmarkDetail (children)
│   │         └── → IdentifyScreen
│   │               ├── → LandmarkDetail
│   │               └── → ChatLandmark
│   │
│   ├── 📖 Stories → StoriesScreen
│   │         └── → StoryReaderScreen
│   │
│   └── 💬 Thoth → ChatScreen
│
├── ══ Floating Avatar (top-right) ══
│   └── → DashboardScreen
│         ├── → SettingsScreen
│         │     └── → FeedbackScreen
│         └── → ScanResult (from recent scans)
│
└── ══ Non-tab Screens ══
    ├── ScanScreen
    │     ├── → ScanResultScreen (inline)
    │     │     ├── → DictionarySignScreen (from glyph sheet)
    │     │     └── Share Intent
    │     └── → ScanHistoryScreen
    │           └── → ScanResult(scanId)
    ├── DictionaryScreen (3 tabs: Browse/Learn/Write)
    │     ├── → LessonScreen (from Learn tab)
    │     └── → DictionarySignScreen (from Browse tab)
    └── DictionarySignScreen
```

## Proposed Screen Hierarchy

```
Wadjet App (MainActivity)
├── [Auth Gate] ─ isLoggedIn?
│   ├── NO  → WelcomeScreen
│   │         ├── LoginSheet (ModalBottomSheet)
│   │         ├── RegisterSheet (ModalBottomSheet)
│   │         └── ForgotPasswordSheet (ModalBottomSheet)
│   └── YES → Landing (bottom nav start)
│
├── ══ Bottom Navigation Bar (5 tabs — unchanged) ══
│   ├── 🏠 Home → LandingScreen
│   │         (same quick actions — all reachable in 1 tap)
│   │
│   ├── 𓂀 Hieroglyphs → HieroglyphsHubScreen
│   │         (add dynamic content: recent signs, learning progress)
│   │
│   ├── 🧭 Explore → ExploreScreen
│   │         (same children)
│   │
│   ├── 📖 Stories → StoriesScreen
│   │         (same children)
│   │
│   └── 💬 Thoth → ChatScreen
│
├── ══ TopAppBar Actions (replaces floating avatar) ══
│   ├── 👤 Profile icon → DashboardScreen (1 tap)
│   └── ⚙️ Settings icon → SettingsScreen (1 tap)
│
└── ══ Cross-Feature Flows (NEW) ══
    └── DictionarySign → "Practice Writing" → Dictionary(tab=2, prefill=glyph)
```

## Navigation Architecture

### Current Bottom Navigation
| Tab | Route | Label | Icon |
|-----|-------|-------|------|
| 1 | Landing | Home | `Icons.Outlined.Home` |
| 2 | Hieroglyphs | Hieroglyphs | `Icons.Outlined.HistoryEdu` |
| 3 | Explore | Explore | `Icons.Outlined.Explore` |
| 4 | Stories | Stories | `Icons.AutoMirrored.Outlined.MenuBook` |
| 5 | Chat | Thoth | `Icons.AutoMirrored.Outlined.Chat` |

### Proposed Bottom Navigation
| Tab | Route | Label | Icon | Change |
|-----|-------|-------|------|--------|
| 1 | Landing | Home | `Icons.Outlined.Home` | No change |
| 2 | Hieroglyphs | Hieroglyphs | `Icons.Outlined.HistoryEdu` | No change |
| 3 | Explore | Explore | `Icons.Outlined.Explore` | No change |
| 4 | Stories | Stories | `Icons.AutoMirrored.Outlined.MenuBook` | No change |
| 5 | Chat | Thoth | `Icons.AutoMirrored.Outlined.Chat` | No change |

**Rationale for keeping 5 tabs:** Wadjet's 5 tabs each represent a genuinely distinct feature domain. Reference apps: NiA uses 3 (ForYou, Saved, Interests); Jetsnack uses 4 (Feed, Search, Cart, Profile); Reply uses 4 (Inbox, Articles, DMs, Groups). Material Design guidelines allow 3-5 tabs. Wadjet is at the upper limit but justified: Home provides cross-feature shortcuts, Hieroglyphs groups 3 related modes, Explore/Stories/Thoth are standalone domains. Adding a 6th Profile tab would overcrowd. Dashboard/Settings access moves to TopAppBar actions.

### Key Navigation Changes

1. **Remove floating avatar** — Replace with TopAppBar profile icon action on all bottom-nav screens. (NiA pattern: `TopAppBar` actions for settings/profile)
2. **Add TopAppBar to bottom-nav screens** — Currently Landing/Hieroglyphs have no TopAppBar. Add one with app branding + profile + settings actions.
3. **Add quick-settings dialog** — Gear icon in TopAppBar opens `SettingsDialog` (AlertDialog) for TTS toggle, language, clear cache. Full SettingsScreen accessible from Dashboard. (NiA `SettingsDialog.kt` pattern)
4. **Add per-tab back stack preservation** — Use `saveState = true` + `restoreState = true` on all tab navigation calls. Each tab preserves its own navigation history when switching between tabs. (NiA `NavigationState.kt` + Jetsnack `JetsnackNavController.kt` pattern)
5. **Add anti-double-navigation guard** — Use `launchSingleTop = true` on all navigations. For detail-pushing callbacks, add `lifecycleIsResumed()` guard. (Jetsnack pattern)
6. **Add cross-feature deep links** — DictionarySign → Write flow, ScanResult → Learn flow.

### Navigation Patterns from Reference Apps

| Pattern | Source App | Source File | Wadjet Application |
|---------|-----------|-------------|-------------------|
| Per-tab independent back stacks | NiA | `core/navigation/NavigationState.kt` | `saveState = true` + `restoreState = true` on tab navigate |
| Settings as dialog | NiA | `feature/settings/impl/.../SettingsDialog.kt` | Quick-settings `AlertDialog` from TopAppBar gear icon |
| Dual NavHost (outer/inner) | Jetsnack | `JetsnackApp.kt` + `Home.kt` | Outer: full-screen routes (hide bottom bar). Inner: tab routes (show bottom bar) |
| Anti-double-navigation | Jetsnack | `JetsnackNavController.kt` | `lifecycleIsResumed()` guard on detail-push callbacks |
| SharedTransitionLayout | Jetsnack, pokedex-compose | `JetsnackApp.kt`, `PokedexNavHost.kt` | Container transform: Explore card → LandmarkDetail hero |
| NavigationSuiteScaffold | NiA | `NiaApp.kt` | Auto-switch bottom bar → rail → drawer by screen size |
| TwoPane / ListDetailSceneStrategy | Reply, NiA, JetNews | `ReplyListContent.kt`, `TopicEntryProvider.kt` | Explore list+detail side-by-side on tablets |
| Type-safe @Serializable routes | NiA, Reply, pokedex-compose | `ForYouNavKey.kt`, `ReplyNavigationActions.kt` | Already implemented in `Route.kt` ✓ |

## Comparison Table

| Aspect | Current | Proposed | Rationale |
|--------|---------|----------|-----------|
| Bottom nav tabs | 5 (Home, Hieroglyphs, Explore, Stories, Thoth) | 5 (same) | Optimal count — each tab is a distinct domain |
| Dashboard access | Floating avatar (non-discoverable) | TopAppBar action icon | Standard pattern (NiA), always visible |
| Settings depth | 3 taps (avatar → Dashboard → Settings) | 1 tap (quick dialog) or 2 taps (full screen) | NiA `SettingsDialog` pattern — within ≤2 tap rule |
| Feedback depth | 4 taps | 3 taps (profile → Dashboard → Settings → Feedback) | Acceptable — rarely accessed |
| Per-tab back stacks | None — tab switch loses in-tab state | `saveState + restoreState` on tab navigation | NiA + Jetsnack pattern — expected tab behavior |
| Double-tap guard | None | `launchSingleTop + lifecycleIsResumed()` | Jetsnack pattern — prevents duplicate screens |
| Bottom bar on detail screens | Conditional route-string check | Dual NavHost (structural) | Jetsnack pattern — self-documenting |
| Max navigation depth | 4 (Landing → Explore → Landmark → Chat) | 3 (same, but Settings reduced) | Improved average |
| TopAppBar on tab screens | None (Landing, Hieroglyphs) | Shared WadjetTopBar (branding + profile + settings) | Consistent chrome |
| Hub screens | HieroglyphsHub (static menu) | Enriched hub (dynamic content) | NiA ForYou pattern — no reference app uses static menus |
| Cross-feature flows | Broken (Scan → Dict → end) | Connected (Scan → Dict → Write) | Learning loop closed |
| Shared element transitions | None | Explore → Detail, Stories → Reader | Jetsnack + pokedex-compose pattern |
| Adaptive nav (tablet) | Bottom bar only | NavigationSuiteScaffold (auto rail/drawer) | NiA pattern — free tablet support |
| List/detail on tablet | Single-pane everywhere | TwoPane on Explore tab | Reply `TwoPane` + NiA `ListDetailSceneStrategy` |

## Navigation Depth Map

| Screen | From Nearest Tab | Taps |
|--------|-----------------|------|
| WelcomeScreen | N/A (auth gate) | 0 (pre-auth) |
| LoginSheet | N/A (auth gate) | 1 (from Welcome) |
| RegisterSheet | N/A (auth gate) | 1 (from Welcome) |
| ForgotPasswordSheet | N/A (auth gate) | 2 (Welcome → Login → Forgot) |
| LandingScreen | Home | 0 |
| HieroglyphsHubScreen | Hieroglyphs | 0 |
| ExploreScreen | Explore | 0 |
| StoriesScreen | Stories | 0 |
| ChatScreen | Thoth | 0 |
| ScanScreen | Home / Hieroglyphs | 1 |
| DictionaryScreen | Home / Hieroglyphs | 1 |
| IdentifyScreen | Explore | 1 |
| StoryReaderScreen | Stories | 1 |
| ScanResultScreen | Home → Scan | 1* (inline, same route) |
| LandmarkDetailScreen | Explore | 1 |
| ScanHistoryScreen | Home → Scan | 2 |
| DictionarySignScreen | Hieroglyphs → Dict | 2 |
| LessonScreen | Hieroglyphs → Dict | 2 |
| DashboardScreen | Any tab (TopAppBar) | **1** (was 1 via avatar) |
| SettingsQuickDialog | Any tab (TopAppBar gear) | **1** (new — NiA pattern) |
| SettingsScreen | Dashboard | **2** (was 3) |
| FeedbackScreen | Settings | **3** (was 4) |
| ChatLandmark | Explore → Detail | 2 |
| ScanResult (from history) | Scan → History | 3 |
| SignDetailSheet | Hieroglyphs → Dict → Sign | 3 (sheet from DictionarySign or ScanResult) |

*ScanResultScreen is rendered inline within the Scan composable route, not a separate navigation destination.

## Screen Grouping Rationale

### Home (Landing)
Central hub — personalised content, quick actions to all features. Functions as the "dashboard lite" for active users. Justifies its own tab because it provides cross-feature navigation shortcuts.

### Hieroglyphs (Hub → Scan/Dictionary/Write)
Groups the three "hieroglyph interaction" modes under one umbrella. The hub screen adds value if it shows learning progress and recent activity. Without dynamic content, it's a dead menu — recommendation is to enrich it.

### Explore (Landmarks + Identify)
Egyptian landmarks are the app's richest content type (100+ items with images, maps, history). Identify is a sub-feature (image → landmark match). Grouping under Explore is logical.

### Stories
Self-contained reading experience with chapters, interactions, and narration. Merging with another tab would dilute it. Standalone tab is correct.

### Thoth (Chat)
AI chat is a standalone experience with its own keyboard/input UX. Separate tab is correct. Also serves as a cross-feature endpoint (any screen can "Ask Thoth" about current context).

### Dashboard/Settings/Feedback (Profile cluster)
Not primary features — accessed infrequently. Accessible via TopAppBar action icon. No dedicated tab needed.

## Reference App Navigation Comparison

| App | Tabs | Max Depth | Settings | Hub Screens | Nav Library | Key Pattern |
|-----|:----:|:---------:|----------|:-----------:|-------------|-------------|
| **NiA** | 3 | 2 | Dialog (1 tap) | None | Navigation 3 | Per-tab back stacks, `NavigationSuiteScaffold`, adaptive list-detail |
| **Jetsnack** | 4 | 3 | N/A | None | NavHost 2.x (strings) | Dual NavHost, `SharedTransitionLayout`, anti-double-nav guard |
| **Reply** | 4 | 2 | N/A | None | NavHost 2.8 (type-safe) | Adaptive (BottomBar/Rail/Drawer), `TwoPane`, state-driven detail |
| **JetNews** | 0 (drawer) | 2 | N/A | None | Navigation 3 | `ListDetailSceneStrategy`, `NavBackStack` per item |
| **Jetchat** | 0 (drawer) | 2 | In drawer | None | Fragment NavComponent | Hybrid Compose+Fragment, drawer nav |
| **Pokedex** | 0 | 2 | N/A | None | Intent-based (Activities) | Flat grid → detail, shared element via TransformationLayout |
| **pokedex-compose** | 0 | 2 | Dialog | None | Navigation 3 | `SharedTransitionLayout`, full object passing, dialog route |
| **architecture-samples** | 0 (drawer) | 2 | N/A | None | NavHost 2.x (strings) | `NavigationActions` class, result-back pattern |
| **Wadjet (current)** | 5 | 4 | 3 taps deep | 1 (static) | NavHost 2.8 (type-safe) | — |
| **Wadjet (proposed)** | 5 | 3 | 1 tap (dialog) | 1 (enriched) | NavHost 2.8 (type-safe) | Per-tab stacks, dual NavHost, `NavigationSuiteScaffold`, `SharedTransitionLayout` |

### Key Takeaways from Reference Apps

1. **Zero reference apps use hub/menu screens.** Every tab destination goes directly to content. Wadjet's HieroglyphsHub must be enriched with dynamic content to justify its existence.
2. **3-4 tabs is the norm.** Wadjet's 5 is at the Material Design limit but defensible given 5 genuinely distinct domains.
3. **Settings is never more than 1-2 taps deep.** NiA uses a dialog (1 tap). Jetsnack puts Profile as a tab (1 tap). No app buries settings 3+ levels.
4. **Per-tab back stack preservation is universal** in all multi-tab apps. This is the most impactful missing feature in Wadjet's navigation.
5. **Shared element transitions are expected** on list→detail flows. Jetsnack and pokedex-compose both implement them.
6. **Max depth is 2-3** across all reference apps. Wadjet's current max of 4 (Settings/Feedback) is an outlier.
