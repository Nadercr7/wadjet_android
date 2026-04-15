# Stage 5: ViewModel Logic — Audit Report

**Date:** 2026-04-15
**Scope:** All 21 ViewModels + ChatHistoryStore

## ViewModel-by-ViewModel Audit

### HieroglyphsHubViewModel
- `isLoading=false` gated on only 1 of 3 parallel coroutines — premature "ready" signal
- Coroutines 1 and 2 have no failure handling — errors silently dropped

### AuthViewModel
- No double-submit guard on signInWithEmail, register, forgotPassword, signInWithGoogle
- Rapid taps can launch parallel auth requests → double navigation

### ChatViewModel
- `retryLastMessage()` has no `isStreaming` check — fires while stream active
- `streamJob` overwritten without cancel → ghost coroutine leak
- `error` field dual-purposed as event signal ("LOCAL_TTS:...") — survives config change
- `SpeechRecognizer` imported but never instantiated

### ChatHistoryStore
- **CRITICAL**: All file I/O on main thread — no dispatcher switch
- Called synchronously in ChatViewModel.init → blocks main thread
- No synchronization for concurrent read/write
- `createdAt` is actually last-modified time

### DashboardViewModel
- All 4 onFailure blocks only log to Timber — error field NEVER set, failures invisible
- No duplicate refresh() guard
- 4 sequential network calls (should be parallel)

### DictionaryViewModel
- `error = "LOCAL_TTS:$text"` — signal-in-error-field anti-pattern
- No Job tracking for loadSigns — concurrent filter changes cause interleaved results

### LessonViewModel
- speakSign() failure: only Timber.e() — no user feedback

### SignDetailViewModel
- TTS failure: `catch (_: Exception) { }` — completely silent
- Empty code path: savedStateHandle returns "" → fires useless network request
- MediaPlayer.release() without isPlaying check → potential IllegalStateException

### TranslateViewModel
- No isLoading guard → double-submit possible

### WriteViewModel
- loadPalette() failure silently swallowed
- convert() has no double-submit guard

### DetailViewModel (Explore)
- toggleFavorite() fire-and-forget — no optimistic update/rollback, no user feedback

### ExploreViewModel
- **Stale state snapshot in loadMore()**: page captured before launch, used inside coroutine
- loadLandmarks() called from 4 sources with no Job tracking — responses interleave

### IdentifyViewModel
- Compressed JPEG temp file never deleted on failure
- Double-invocation window before isLoading=true state update

### FeedbackViewModel
- No isSubmitting entry guard — double-submit possible
- Email field not validated before submission

### LandingViewModel
- refresh() no cancellation guard
- 4 sequential network calls (should be parallel)
- authRepository.currentUser.firstOrNull() can suspend indefinitely

### HistoryViewModel
- **CRITICAL**: refresh() spawns additional perpetual flow collectors — N refreshes = N active collectors writing to state simultaneously

### ScanResultViewModel
- `error = "LOCAL_TTS:..."` signal anti-pattern
- Concurrent TTS: speak() doesn't cancel in-flight request — two concurrent HTTP calls, MediaPlayer conflict

### ScanViewModel
- **CRITICAL**: No `onCleared()` — MediaPlayer leaked on ViewModel destruction
- `error = "LOCAL_TTS:..."` signal anti-pattern
- Race condition in onImageCaptured (isLoading set inside coroutine body)

### SettingsViewModel
- Passwords in state (security risk if logged)
- signOut() ignores result — navigates away even on failure
- No changePassword() double-submit guard

### StoriesViewModel
- loadStories()/refresh() no cancellation guard — minor
- toggleStoryFavorite uses optimistic update correctly ✓

### StoryReaderViewModel
- **CRITICAL**: saveCurrentProgress() in onCleared() launched on viewModelScope → cancelled immediately → progress lost
- MutableSet inside data class — breaks StateFlow change detection
- restoreProgress() vs loadStory() race — progress skipped if emitted before story loads
- speakAndWait spawns inner viewModelScope.launch → TTS requests not cancelled when narration stops
- File.createTempFile without dir arg → system temp dir, deleteOnExit() unreliable on Android

## Cross-Cutting Issues

| # | Issue | Affected VMs |
|---|-------|-------------|
| CC-1 | `error` field dual-purpose as one-shot event signal ("LOCAL_TTS:...") | Chat, Scan, ScanResult, Dictionary |
| CC-2 | No double-submit guard (missing `if (isLoading) return`) | Auth, Translate, Write, Feedback, Settings |
| CC-3 | Blocking main-thread file I/O | ChatViewModel/ChatHistoryStore |
| CC-4 | Duplicate flow collectors on refresh | HistoryViewModel |
| CC-5 | Missing onCleared() for MediaPlayer | ScanViewModel |
| CC-6 | retryLastMessage replaces streamJob without cancel | ChatViewModel |
| CC-7 | Stale state snapshot in loadMore() | ExploreViewModel |
| CC-8 | Silent error swallowing — error field never populated | DashboardViewModel |
| CC-9 | saveCurrentProgress() in onCleared() will be cancelled | StoryReaderViewModel |
| CC-10 | MutableSet inside data class — breaks StateFlow | StoryReaderViewModel |
| CC-11 | Sequential network calls where parallel are safe | Dashboard, Landing |

## Issues Found (Consolidated)

| # | Severity | Description | File |
|---|---|---|---|
| S5-01 | 🔴 Critical | ScanViewModel: No onCleared() — MediaPlayer leaked | ScanViewModel.kt |
| S5-02 | 🔴 Critical | HistoryViewModel: refresh() spawns duplicate perpetual flow collectors | HistoryViewModel.kt |
| S5-03 | 🔴 Critical | ChatHistoryStore: Blocking file I/O on main thread in ChatViewModel.init | ChatHistoryStore.kt / ChatViewModel.kt |
| S5-04 | 🔴 Critical | StoryReaderViewModel: saveCurrentProgress() in onCleared() cancelled immediately — progress lost | StoryReaderViewModel.kt |
| S5-05 | 🟠 Major | ChatViewModel: retryLastMessage() no isStreaming guard; streamJob overwritten without cancel | ChatViewModel.kt |
| S5-06 | 🟠 Major | StoryReaderViewModel: restoreProgress() vs loadStory() race — progress skipped | StoryReaderViewModel.kt |
| S5-07 | 🟠 Major | StoryReaderViewModel: speakAndWait inner launch bypasses narration cancellation | StoryReaderViewModel.kt |
| S5-08 | 🟠 Major | DashboardViewModel: All 4 failures silent — error never shown | DashboardViewModel.kt |
| S5-09 | 🟠 Major | LOCAL_TTS error-as-signal anti-pattern in 4 VMs | Chat/Scan/ScanResult/DictionaryVM |
| S5-10 | 🟡 Minor | Auth/Translate/Write/Feedback/Settings: No double-submit guard | Multiple |
| S5-11 | 🟡 Minor | ExploreViewModel: Stale page snapshot in loadMore() | ExploreViewModel.kt |
| S5-12 | 🟡 Minor | SettingsViewModel: signOut() ignores result, passwords in state | SettingsViewModel.kt |
| S5-13 | 🟡 Minor | SignDetailViewModel: TTS failure completely silent | SignDetailViewModel.kt |
| S5-14 | 🟡 Minor | IdentifyViewModel: Temp file not deleted on failure | IdentifyViewModel.kt |
| S5-15 | 🟡 Minor | StoryReaderViewModel: MutableSet in data class | StoryReaderViewModel.kt |
| S5-16 | 🟡 Minor | Dashboard/Landing: Sequential network calls | DashboardVM/LandingVM |
| S5-17 | 🔵 Enhancement | HieroglyphsHubVM: 2/3 parallel loads have no failure handling | HieroglyphsHubViewModel.kt |
| S5-18 | 🔵 Enhancement | StoryReaderVM: Temp files use system dir, deleteOnExit unreliable | StoryReaderViewModel.kt |
