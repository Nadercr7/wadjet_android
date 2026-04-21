# Phase 01: Unit/ViewModel Bugs - Findings

## Source
Stages 3, 9, 10, 13 from 005-full-testing

## Issues Found
| # | ID | Category | Severity | File:Line | Description | Root Cause |
|---|-----|----------|----------|-----------|-------------|------------|
| 1 | FIX-001 | ViewModel | CRITICAL | feature/scan/ScanViewModel.kt | `onImageCaptured()` no `isLoading` guard — double-submit | Missing guard check |
| 2 | FIX-002 | ViewModel | CRITICAL | feature/explore/IdentifyViewModel.kt | `onImageCaptured()` no `isLoading` guard | Same pattern as FIX-001 |
| 3 | FIX-003 | ViewModel | CRITICAL | feature/chat/ChatViewModel.kt | `GlobalScope.launch(NonCancellable)` in onCleared() | Anti-pattern outlives VM |
| 4 | FIX-004 | ViewModel | CRITICAL | feature/stories/StoryReaderViewModel.kt | `GlobalScope.launch(NonCancellable)` in onCleared() | Same anti-pattern |
| 5 | FIX-005 | ViewModel | CRITICAL | feature/chat/ChatViewModel.kt:~L195 | Streaming `.map{}` copies entire message list per chunk | O(n*m) list copies |
| 6 | FIX-006 | ViewModel | CRITICAL | feature/scan/ScanViewModel.kt:~L173 | Bitmap decode on Main thread — ANR risk | Missing Dispatchers.IO |
| 7 | FIX-007 | Repository | CRITICAL | core/data/StoriesRepositoryImpl.kt | `runBlocking` in Firestore listener → ANR | Blocks callback thread |
| 8 | FIX-008 | Repository | CRITICAL | core/data/ExploreRepositoryImpl.kt | `favoritesLoaded` not synchronized — race condition | Bare boolean, no volatile/mutex |
| 9 | FIX-009 | Auth | CRITICAL | TokenAuthenticator.kt:~L78 | Bare OkHttpClient for refresh — no interceptors/timeouts | Creates new unshared client |
| 10 | FIX-010 | Auth | CRITICAL | MainActivity.kt:~L80 | Process death → Welcome despite persisted tokens | Firebase init timing race |
| 11 | FIX-011 | ViewModel | HIGH | feature/dictionary/DictionaryViewModel.kt | `speakSign` no loading guard — overlapping MediaPlayer | Missing guard |
| 12 | FIX-012 | ViewModel | HIGH | feature/dictionary/SignDetailViewModel.kt | `toggleFavorite` race condition — rapid double-tap | No debounce/mutex |
| 13 | FIX-013 | ViewModel | HIGH | feature/explore/ExploreViewModel.kt | `loadLandmarks()` concurrent calls, no job cancellation | Stale results overwrite |
| 14 | FIX-014 | ViewModel | HIGH | feature/dictionary/DictionaryViewModel.kt | `loadSigns()` concurrent calls, no job cancellation | Stale results overwrite |
| 15 | FIX-015 | ViewModel | HIGH | feature/stories/StoriesViewModel.kt | `toggleStoryFavorite` no guard — toggle chaos | Missing guard |
| 16 | FIX-016 | ViewModel | HIGH | feature/dashboard/DashboardViewModel.kt | `refresh()` no cancellation, error field overwritten | Concurrent interleave |
| 17 | FIX-017 | ViewModel | HIGH | feature/settings/SettingsViewModel.kt | `saveName()` and `signOut()` missing loading guards | Double-submit |
| 18 | FIX-018 | Auth | HIGH | AuthRepositoryImpl.kt | Logout doesn't clear Room DB/DataStore | Ghost state for next user |
| 19 | FIX-019 | Auth | HIGH | TokenAuthenticator.kt | Refresh failure doesn't sign out Firebase | Ghost auth state |

## Fix Priority
| Priority | Issue IDs | Reason |
|----------|-----------|--------|
| P0 | FIX-001 to FIX-010 | Crashes, ANR, data corruption, auth broken |
| P1 | FIX-011 to FIX-019 | Core feature correctness, double-submits, race conditions |
