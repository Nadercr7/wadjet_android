# Stage 3 — ViewModel Audit

**Date:** 2025-07-22
**Auditor:** Automated (Copilot)
**Scope:** All 20 ViewModels across 10 feature modules

---

## Summary

| Metric | Value |
|---|---|
| **Total ViewModels** | 20 |
| **With tests** | 3 (Auth, Dictionary, Explore) |
| **Without tests** | 17 |
| **Critical bugs** | 8 cross-cutting patterns |
| **Using SavedStateHandle** | 5 (ScanResult, SignDetail, Lesson, Detail, StoryReader) + partial (Chat for slug) |
| **Using GlobalScope** | 2 (Chat, StoryReader) — anti-pattern |
| **With proper isLoading guard** | 7 of 20 |
| **Duplicated TTS/MediaPlayer code** | 6 ViewModels |
| **Silently swallowing failures** | 10 ViewModels |

---

## Per-ViewModel Findings

### 1. HieroglyphsHubViewModel
**Path:** `feature/dashboard/src/main/java/com/wadjet/feature/dashboard/HieroglyphsHubViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| `isLoading` only cleared in 3rd coroutine; first two silently swallow failures | BUG |
| Random page (1..30) can exceed actual page total — 404/empty response | BUG |
| No tests | GAP |

### 2. AuthViewModel
**Path:** `feature/auth/src/main/java/com/wadjet/feature/auth/AuthViewModel.kt`
**Tests:** Yes

| Issue | Severity |
|---|---|
| Validation runs before `isLoading` check — minor ordering issue | MINOR |
| Has double-submit guard, email/password validation, proper error handling | GOOD |

### 3. LandingViewModel
**Path:** `feature/landing/src/main/java/com/wadjet/feature/landing/LandingViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| `refresh()` has no double-submit guard | BUG |
| No job cancellation for concurrent loads | BUG |

### 4. ScanViewModel
**Path:** `feature/scan/src/main/java/com/wadjet/feature/scan/ScanViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| `onImageCaptured()` has NO `isLoading` guard — missing double-submit protection | CRITICAL |
| Compressed file leak possible (temp files not always cleaned) | BUG |
| `delay(400)` code smell | MINOR |

### 5. ScanResultViewModel
**Path:** `feature/scan/src/main/java/com/wadjet/feature/scan/ScanResultViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| Uses SavedStateHandle for scanId | GOOD |
| MediaPlayer released in onCleared | GOOD |
| No retry mechanism for failed load | MINOR |
| Duplicated TTS/MediaPlayer code with ScanViewModel | MINOR |

### 6. HistoryViewModel
**Path:** `feature/scan/src/main/java/com/wadjet/feature/scan/HistoryViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| `collectorJob?.cancel()` before new load — good | GOOD |
| `deleteScan()` has no `isLoading` guard — user can spam delete | BUG |
| Delete failure only logged, not shown to user | BUG |
| No confirmation dialog before delete | MINOR |

### 7. DictionaryViewModel
**Path:** `feature/dictionary/src/main/java/com/wadjet/feature/dictionary/DictionaryViewModel.kt`
**Tests:** Yes

| Issue | Severity |
|---|---|
| `loadSigns(page=1)` can be called simultaneously from `selectCategory`, `selectType`, `onSearchQueryChange`, and init — no job cancellation, stale results overwrite | CRITICAL |
| `speakSign` has no loading guard — rapid taps create overlapping MediaPlayer instances | BUG |
| Temp files from `speakSign` created in system temp dir, not cleaned in `onCleared()` | BUG |
| `loadCategories` failure silently swallowed — user sees no categories | BUG |
| `loadFavorites` failure silently ignored | MINOR |
| No `SavedStateHandle` — search, filters, pagination lost on process death | ISSUE |

### 8. SignDetailViewModel
**Path:** `feature/dictionary/src/main/java/com/wadjet/feature/dictionary/SignDetailViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| `toggleFavorite` race condition — rapid double-tap sends conflicting API calls, optimistic update flips twice | CRITICAL |
| `onCleared` calls `mediaPlayer?.release()` without `.stop()` first — may crash if playing | BUG |
| `speakSign` no loading guard — overlapping players | BUG |
| No retry for failed load | MINOR |

### 9. LessonViewModel
**Path:** `feature/dictionary/src/main/java/com/wadjet/feature/dictionary/LessonViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| `speakSign` has no guard — rapid taps create overlapping MediaPlayers | BUG |
| `retry()` no `isLoading` guard — concurrent loads possible | BUG |
| TTS failure silently logged, not shown to user | MINOR |
| Uses SavedStateHandle for `level` | GOOD |

### 10. TranslateViewModel
**Path:** `feature/dictionary/src/main/java/com/wadjet/feature/dictionary/TranslateViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| **Clean.** Has `isLoading` guard, checks `isBlank()` | GOOD |
| No SavedStateHandle for input/result | MINOR |

### 11. WriteViewModel
**Path:** `feature/dictionary/src/main/java/com/wadjet/feature/dictionary/WriteViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| Has `isLoading` guard and `isBlank()` check | GOOD |
| `loadPalette()` failure silently ignored — empty palette, no error | BUG |
| `isPreviewLoading` declared but never used | MINOR |
| No SavedStateHandle | MINOR |

### 12. ExploreViewModel
**Path:** `feature/explore/src/main/java/com/wadjet/feature/explore/ExploreViewModel.kt`
**Tests:** Yes

| Issue | Severity |
|---|---|
| `loadLandmarks()` can be called from `selectCategory`, `selectCity`, `updateSearch`, `refresh`, and `init` without cancelling previous job — stale results overwrite | CRITICAL |
| `toggleFavorite` failure only logged, not shown to user + no optimistic UI update | BUG |
| `loadCategories` failure partially handled but categories stays at `FALLBACK_CATEGORIES` | ISSUE |
| No SavedStateHandle — filters, search, page all lost on process death | ISSUE |

### 13. DetailViewModel
**Path:** `feature/explore/src/main/java/com/wadjet/feature/explore/DetailViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| `retry()` has no `isLoading` guard — concurrent loads | BUG |
| `toggleFavorite` failure silently logged, no optimistic rollback | BUG |
| Uses SavedStateHandle for slug via toRoute | GOOD |

### 14. IdentifyViewModel
**Path:** `feature/explore/src/main/java/com/wadjet/feature/explore/IdentifyViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| `onImageCaptured` has NO `isLoading` guard — same critical bug as ScanViewModel | CRITICAL |
| Compressed temp files not cleaned up on success path — only cleaned on failure | BUG |
| No `onCleared` cleanup for temp files | BUG |
| Same duplicate code as ScanViewModel for `compressImage`/`uriToFile` | ISSUE |

### 15. ChatViewModel
**Path:** `feature/chat/src/main/java/com/wadjet/feature/chat/ChatViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| `GlobalScope.launch(NonCancellable)` in `onCleared()` — anti-pattern, outlives ViewModel | CRITICAL |
| `speakMessage` has no guard against calling while loading TTS (only checks `isSpeaking`, not `isLoadingTts`) | BUG |
| `clearChat` calls `chatRepository.clearSession()` fire-and-forget — orphaned server data on failure | BUG |
| Chat messages not persisted to SavedStateHandle — entire conversation lost on process death | ISSUE |
| `tempFile.deleteOnExit()` — JVM may not call this on Android process kill | MINOR |

### 16. StoriesViewModel
**Path:** `feature/stories/src/main/java/com/wadjet/feature/stories/StoriesViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| `toggleStoryFavorite` has no guard — rapid taps cause toggle chaos | CRITICAL |
| `refresh()` calls `loadStories()` without cancelling previous — concurrent loads | BUG |

### 17. StoryReaderViewModel
**Path:** `feature/stories/src/main/java/com/wadjet/feature/stories/StoryReaderViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| `GlobalScope.launch(NonCancellable)` in `onCleared()` — same anti-pattern as ChatViewModel | CRITICAL |
| `loadChapterImage()` can be called concurrently from `goToChapter()` and `restoreProgress()` | BUG |
| Inner coroutine in `speakAndWait` survives outer cancellation | BUG |
| `submitAnswer` has no `isLoading` guard | BUG |
| SharedPreferences for image cache — inconsistent with DataStore elsewhere | MINOR |

### 18. DashboardViewModel
**Path:** `feature/dashboard/src/main/java/com/wadjet/feature/dashboard/DashboardViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| `refresh()` launches `loadDashboard()` without cancelling previous — concurrent async blocks interleave state | BUG |
| Single `error` field overwritten by 4 parallel requests — loses error context | BUG |

### 19. SettingsViewModel
**Path:** `feature/settings/src/main/java/com/wadjet/feature/settings/SettingsViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| `saveName()` missing `isSaving` guard — double-submit possible | BUG |
| `signOut()` has no loading guard — multiple rapid taps invoke signOut multiple times | BUG |
| Password validation only checks length (no uppercase/lowercase/digit like AuthViewModel) — inconsistent | ISSUE |

### 20. FeedbackViewModel
**Path:** `feature/feedback/src/main/java/com/wadjet/feature/feedback/FeedbackViewModel.kt`
**Tests:** None

| Issue | Severity |
|---|---|
| **Clean.** Has `isSubmitting` guard, proper validation (category required, 10-1000 chars) | GOOD |
| No email format validation | MINOR |
| After `isSuccess=true`, no way to reset form | MINOR |

---

## Cross-Cutting Critical Bugs

| # | VMs Affected | Issue |
|---|---|---|
| 1 | Scan, Identify | **Missing `isLoading` guard on image capture** — rapid taps launch concurrent scan/upload |
| 2 | Chat, StoryReader | **`GlobalScope` in `onCleared()`** — anti-pattern, outlives ViewModel lifecycle |
| 3 | Explore, Dictionary, Landing, Dashboard, Stories | **No job cancellation on refresh/filter change** — stale results can overwrite fresh |
| 4 | SignDetail, Stories, Dictionary | **Favorite toggle race condition** — rapid double-tap causes conflicting API calls |
| 5 | Identify | **Temp files leaked on success path** — compressed images never deleted after identify |
| 6 | Settings | **`saveName()` missing loading guard** — double-submit |
| 7 | StoryReader | **Inner coroutine in `speakAndWait` survives outer cancellation** |
| 8 | Dashboard | **Single `error` field overwritten by parallel requests** — loses error context |

---

## Pattern Analysis

| Pattern | Count | ViewModels |
|---|---|---|
| `MutableStateFlow` + `data class` | 20/20 | All |
| Proper `isLoading` guard on actions | 7/20 | Auth, Translate, Write, Feedback, Chat, Settings(partial), History |
| `SavedStateHandle` used | 5/20 | ScanResult, SignDetail, Lesson, Detail, StoryReader + Chat(slug) |
| `onCleared` cleanup | 8/20 | Scan, ScanResult, Dictionary, SignDetail, Lesson, Chat, StoryReader, Settings |
| Silent failure swallowing | 10/20 | HieroglyphsHub, Dictionary, Write, Explore, Detail, History, Lesson, SignDetail, Stories, Landing |
| Duplicated TTS/MediaPlayer code | 6 VMs | Scan, ScanResult, Dictionary, SignDetail, Lesson, Chat |
| `GlobalScope` anti-pattern | 2 | Chat, StoryReader |

---

## Recommendations

1. **Extract TTS utility** — 6 ViewModels copy-paste the same MediaPlayer/TTS pattern. Extract to `core:common` or `core:ui`.
2. **Add `isLoading` guards** — All action-triggering methods should check loading state before launching coroutines.
3. **Replace `GlobalScope`** — Use `ProcessLifecycleOwner.lifecycleScope` or `WorkManager` for work that must survive `onCleared()`.
4. **Job cancellation pattern** — All refresh/reload/filter methods should cancel previous job before launching new one (follow `HistoryViewModel` pattern).
5. **Favorite toggle debounce** — SignDetail, Stories, Dictionary need either a debounce or a `Mutex` on toggle operations.
6. **SavedStateHandle adoption** — 15 VMs lose all user state on process death. At minimum, preserve search queries, form inputs, and filter selections.
7. **Temp file cleanup** — IdentifyViewModel and DictionaryViewModel leak temp files. Add `onCleared()` cleanup.
8. **Extract `compressImage`/`uriToFile`** — Duplicated between ScanViewModel and IdentifyViewModel. Move to `core:common`.
