# Stage 8 — Navigation Graph Audit

**Date:** 2025-07-22  
**Auditor:** Automated (Copilot)  
**Scope:** Route definitions, NavHost graph, bottom nav, auth gating, transitions, process death

---

## Summary

| Metric | Value |
|---|---|
| **Total routes** | 20 (11 objects, 9 data classes with args) |
| **Bottom nav tabs** | 5 (Home, Hieroglyphs, Explore, Stories, Thoth) |
| **Deep links** | **NONE** |
| **Shared element transitions** | 4 routes (Explore, LandmarkDetail, Stories, StoryReader) |
| **Type-safe navigation** | Full — Kotlin Serialization + compose-navigation |
| **Auth gating** | Global reactive observer only — no per-route guards |

---

## 1. All Routes

| Route | Type | Parameters | Screen |
|---|---|---|---|
| `Splash` | data object | — | Empty (splash via `installSplashScreen()`) |
| `Welcome` | data object | — | WelcomeScreen |
| `Landing` | data object | — | LandingScreen |
| `Hieroglyphs` | data object | — | HieroglyphsHubScreen |
| `Scan` | data object | — | ScanScreen |
| `ScanResult` | data class | `scanId: String` | ScanResultScreen |
| `ScanHistory` | data object | — | ScanHistoryScreen |
| `Dictionary` | data class | `initialTab: Int = 0`, `prefillGlyph: String? = null` | DictionaryScreen |
| `DictionarySign` | data class | `code: String` | DictionarySignScreen |
| `Lesson` | data class | `level: Int` | LessonScreen |
| `Explore` | data object | — | ExploreScreen |
| `LandmarkDetail` | data class | `slug: String` | LandmarkDetailScreen |
| `Identify` | data object | — | IdentifyScreen |
| `Chat` | data object | — | ChatScreen |
| `ChatLandmark` | data class | `slug: String` | ChatScreen (same composable) |
| `Stories` | data object | — | StoriesScreen |
| `StoryReader` | data class | `storyId: String` | StoryReaderScreen |
| `Dashboard` | data object | — | DashboardScreen |
| `Settings` | data object | — | SettingsScreen |
| `Feedback` | data object | — | FeedbackScreen |

---

## 2. Bottom Navigation

| Tab | Route | Icon | Responsive |
|---|---|---|---|
| HOME | `Route.Landing` | `Icons.Outlined.Home` | Bar/Rail/Drawer |
| HIEROGLYPHS | `Route.Hieroglyphs` | `Icons.Outlined.HistoryEdu` | Bar/Rail/Drawer |
| EXPLORE | `Route.Explore` | `Icons.Outlined.Explore` | Bar/Rail/Drawer |
| STORIES | `Route.Stories` | `Icons.AutoMirrored.Outlined.MenuBook` | Bar/Rail/Drawer |
| THOTH | `Route.Chat` | `Icons.AutoMirrored.Outlined.Chat` | Bar/Rail/Drawer |

Adapts via `NavigationSuiteScaffold`: NavigationBar (compact), NavigationRail (medium), NavigationDrawer (expanded). Hidden on non-top-level routes.

Tab switching uses standard NiA pattern: `popUpTo(startDest) { saveState = true }`, `launchSingleTop = true`, `restoreState = true`.

---

## 3. Auth Gating

- **Start destination**: `if (initialLoggedIn) Route.Landing else Route.Welcome`
- **Reactive sign-out**: `LaunchedEffect(isAuthenticated)` watches `authRepository.currentUser`. When false → navigates to Welcome with full backstack clear
- **Per-route guards**: **NONE** — no individual route checks auth. Relies entirely on global observer
- **Risk**: Brief flash of protected screens possible during race condition between auth state change and navigation

---

## 4. Back Stack Behavior

| Scenario | Behavior |
|---|---|
| Tab switching | `popUpTo(start)` + `saveState` + `restoreState` (NiA pattern) |
| Detail screens | Normal push (back stack accumulates) |
| Login → Landing | `popUpTo<Welcome> { inclusive = true }` |
| Sign out | `popUpTo(graph.id) { inclusive = true }` (nuclear clear) |
| Anti-double-tap | `lifecycleIsResumed()` guard on most detail navigations |

---

## 5. Shared Element Transitions

Only 4 routes have shared transition support:
- `Route.Explore` → `ExploreScreen`
- `Route.LandmarkDetail` → `LandmarkDetailScreen`
- `Route.Stories` → `StoriesScreen`
- `Route.StoryReader` → `StoryReaderScreen`

---

## 6. Issues Found

| # | Severity | Issue |
|---|---|---|
| 1 | **Medium** | `Route.Splash` is declared but never navigated to — dead code |
| 2 | **Medium** | `Route.Scan` embeds `ScanResultScreen` inline AND separate `Route.ScanResult` exists — two paths to same screen with different state management, fragile for process death |
| 3 | **Medium** | No deep links at all — notifications, web links, share URLs cannot open specific screens |
| 4 | **Low** | `Route.ChatLandmark` duplicates all `ChatScreen` wiring (~15 callback params) |
| 5 | **Low** | `lifecycleIsResumed()` guard inconsistent — used on some navigations but not others |
| 6 | **Low** | `CacheClearEntryPoint` Hilt definition inside nav graph file — business logic leak |
| 7 | **Low** | Settings `onClearCache` runs business logic in navigation layer |
| 8 | **Low** | Dashboard/Settings have custom fade+scale transitions but manually duplicated |

---

## 7. Process Death Analysis

| Screen | Preserves State? | Via | Notes |
|---|---|---|---|
| Tab position | Yes | NavHost auto-save | Restores current tab |
| ScanResult (via route) | Yes | `SavedStateHandle(scanId)` | Works for history playback |
| ScanResult (inline) | **No** | ViewModel state lost | Returns to scan camera |
| LandmarkDetail | Yes | `SavedStateHandle(slug)` | Reloads from network/cache |
| StoryReader | Partial | `SavedStateHandle(storyId)` | Chapter progress from SharedPrefs, not SavedStateHandle |
| Dictionary filters | **No** | Not in SavedStateHandle | Search, category, type all lost |
| Chat messages | **No** | No persistence | Entire conversation lost |
