# STAGE 7 REPORT: Scan & Landmarks Feature Audit

> Generated: 2026-04-15
> Scope: feature/scan/, feature/explore/, core layers (domain, data, network, database)

---

## S7-01 | MAJOR — ScanViewModel never cleans up MediaPlayer on ViewModel destruction

**File:** [feature/scan/src/main/java/com/wadjet/feature/scan/ScanViewModel.kt](feature/scan/src/main/java/com/wadjet/feature/scan/ScanViewModel.kt#L46-L210)
**Description:** `ScanViewModel` holds a `mediaPlayer` reference but has **no `onCleared()` override**. If the user navigates away while TTS audio is playing, the MediaPlayer leaks, holding audio focus and a file descriptor. Compare with `ScanResultViewModel` which correctly overrides `onCleared()` at line 129.
**Evidence:**
```kotlin
// ScanViewModel — no onCleared() anywhere in the class
private var mediaPlayer: MediaPlayer? = null
```
**Impact:** Resource leak, audio continues playing after navigation.

---

## S7-02 | MAJOR — Temp TTS files in ScanViewModel never deleted

**File:** [feature/scan/src/main/java/com/wadjet/feature/scan/ScanViewModel.kt](feature/scan/src/main/java/com/wadjet/feature/scan/ScanViewModel.kt#L93-L108)
**Description:** `playWavBytes()` creates a `File.createTempFile("tts_", ".wav")` in every call but never deletes it — not on completion, not on error. Over time this accumulates orphaned WAV files in `cacheDir`. Compare with `ScanResultViewModel.playWavBytes()` which calls `tmp.delete()` in `setOnCompletionListener`.
**Evidence:**
```kotlin
// ScanViewModel.playWavBytes
val tmp = File.createTempFile("tts_", ".wav", context.cacheDir)
tmp.writeBytes(bytes)
mediaPlayer = MediaPlayer().apply {
    setDataSource(tmp.absolutePath)
    prepare()
    setOnCompletionListener {
        _state.update { s -> s.copy(ttsStates = s.ttsStates + (key to TtsState.IDLE)) }
        stopMediaPlayer()
        // ← no tmp.delete()
    }
    start()
}
```

---

## S7-03 | MEDIUM — Compressed scan images accumulate in cache without cleanup

**File:** [feature/scan/src/main/java/com/wadjet/feature/scan/ScanViewModel.kt](feature/scan/src/main/java/com/wadjet/feature/scan/ScanViewModel.kt#L165-L180)
**Description:** `compressImage()` creates `scan_${timestamp}.jpg` files in `cacheDir` on every scan, and `uriToFile()` creates `picked_${timestamp}.jpg`. Neither is ever deleted after the scan completes. The thumbnail is saved separately; the full-resolution compressed file is orphaned.
**Impact:** Disk usage grows unbounded on devices that scan frequently.

---

## S7-04 | MEDIUM — ScanApi endpoint mismatch with expected contract

**File:** [core/network/src/main/java/com/wadjet/core/network/api/ScanApiService.kt](core/network/src/main/java/com/wadjet/core/network/api/ScanApiService.kt)
**Description:** The API spec requires `/api/scan/image` but ScanApiService maps to `POST /api/scan`. The following expected endpoints are **completely missing** from the Android client:
- `/api/scan/history` — no server-side history fetch (history is local-only via Room)
- `/api/scan/image` — may just be a naming discrepancy with `/api/scan`

This means scan history is purely device-local and will not sync across devices or survive app reinstall.

---

## S7-05 | MEDIUM — HistoryViewModel creates duplicate Flow collectors on refresh

**File:** [feature/scan/src/main/java/com/wadjet/feature/scan/HistoryViewModel.kt](feature/scan/src/main/java/com/wadjet/feature/scan/HistoryViewModel.kt#L33-L48)
**Description:** `loadHistory()` is called from `init` and again from `refresh()`. Each call launches a new coroutine that collects from `getScanHistory()` Flow indefinitely. On refresh, the old collector is **never cancelled**, resulting in duplicate active collectors.
**Evidence:**
```kotlin
private fun loadHistory() {
    viewModelScope.launch {          // ← new coroutine each call
        try {
            scanRepository.getScanHistory().collect { items ->  // ← never-ending collect
                _state.update { it.copy(items = items, isLoading = false, error = null) }
            }
        } catch ...
    }
}

fun refresh() {
    _state.update { it.copy(isLoading = true, error = null) }
    loadHistory()   // ← second collector — old one still alive
}
```
**Fix:** Store the Job and cancel it before re-launching, or use `flatMapLatest`.

---

## S7-06 | MEDIUM — ScanHistoryScreen swipe-to-delete race condition

**File:** [feature/scan/src/main/java/com/wadjet/feature/scan/screen/ScanHistoryScreen.kt](feature/scan/src/main/java/com/wadjet/feature/scan/screen/ScanHistoryScreen.kt#L124-L140)
**Description:** When the user swipes to dismiss, `onDelete` shows a Snackbar and only actually deletes if the user does NOT tap "Undo". However, the `SwipeToDismissBox` visually removes the item immediately via its end-state, but the data list still contains the item. The item will flash back when the Flow re-emits. More critically, the swipe-to-delete triggers `onDelete` which shows a snackbar, but the SwipeToDismissBox state is never reset — swiping the same item again will not work.
**Evidence:**
```kotlin
LaunchedEffect(dismissState.currentValue) {
    if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
        onDelete()  // shows snackbar, conditional delete
        // dismissState never reset to Settled
    }
}
```

---

## S7-07 | MEDIUM — LandmarkApi missing `/api/landmarks/featured` dedicated endpoint

**File:** [core/network/src/main/java/com/wadjet/core/network/api/LandmarkApiService.kt](core/network/src/main/java/com/wadjet/core/network/api/LandmarkApiService.kt)
**Description:** The expected endpoint `/api/landmarks/featured` is not implemented as a separate API call. Instead, featured landmarks must be fetched using `?featured=true` query param on the general `getLandmarks()` endpoint. This works but deviates from the stated API contract. The `ExploreViewModel` never passes `featured=true` — featured landmarks are never specifically requested.

---

## S7-08 | MEDIUM — LandmarkDetail offline cache only works on error, not proactively

**File:** [core/data/src/main/java/com/wadjet/core/data/repository/ExploreRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/ExploreRepositoryImpl.kt#L95-L110)
**Description:** `getLandmarkDetail()` only falls back to cached `detailJson` when the API returns an error response (non-200). If there's an `IOException` (airplane mode), the exception propagates uncaught because only the inner `if (!response.isSuccessful)` has a cache fallback — the `IOException` case is **not wrapped in a try/catch** like `getLandmarks()` is.
**Evidence:**
```kotlin
override suspend fun getLandmarkDetail(slug: String): Result<LandmarkDetail> = suspendRunCatching {
    val response = landmarkApi.getLandmarkDetail(slug)  // ← IOException not caught
    if (response.isSuccessful) {
        ...
    } else {
        // Fallback to cached — but only for non-2xx, NOT for IOException
        val cached = landmarkDao.getBySlug(slug)
        ...
    }
}
```
Compare with `getLandmarks()` which explicitly catches `java.io.IOException` at line 64.

---

## S7-09 | LOW — GardinerUnicode common glyph map has inaccurate code points

**File:** [feature/scan/src/main/java/com/wadjet/feature/scan/util/GardinerUnicode.kt](feature/scan/src/main/java/com/wadjet/feature/scan/util/GardinerUnicode.kt#L24-L89)
**Description:** Several entries in `COMMON_GLYPHS` may have incorrect Unicode code points. The `UNICODE_MAP` lazy fallback uses `Character.getName()` which is correct, but the hardcoded `COMMON_GLYPHS` bypasses this validation. For example, G1 (Vulture) is mapped to `\uD80C\uDD80` (U+13180) but the standard Egyptian Hieroglyph G001 is actually U+13146. Due to how the Gardiner→Unicode mapping works in the Unicode standard (padded to 3 digits), the hardcoded values should be verified against the standard.
**Impact:** Incorrect hieroglyph rendering for the most-used signs.

---

## S7-10 | LOW — ExploreViewModel offline fallback only for search queries, not category/city

**File:** [feature/explore/src/main/java/com/wadjet/feature/explore/ExploreViewModel.kt](feature/explore/src/main/java/com/wadjet/feature/explore/ExploreViewModel.kt#L125-L140)
**Description:** When `loadLandmarks()` fails, the offline fallback (`searchOffline`) is only used when `searchQuery.isNotBlank()`. For category/city browsing, the fallback is `emptyList()` (kept existing). The Room DAO has `getFiltered(category, city)` — this should be used.
**Evidence:**
```kotlin
.onFailure { error ->
    val cached = if (s.searchQuery.isNotBlank()) {
        exploreRepository.searchOffline(s.searchQuery)
    } else {
        emptyList()  // ← category/city browsing gets nothing offline
    }
```

---

## S7-11 | LOW — IdentifyViewModel does not check free-tier scan limits

**File:** [feature/explore/src/main/java/com/wadjet/feature/explore/IdentifyViewModel.kt](feature/explore/src/main/java/com/wadjet/feature/explore/IdentifyViewModel.kt#L37-L50)
**Description:** `ScanViewModel.onImageCaptured()` checks `userRepository.getLimits()` before scanning, but `IdentifyViewModel.onImageCaptured()` does not check any limits. If identify and scan share the same rate limit bucket on the server, the user will get a raw API error instead of a friendly message.

---

## S7-12 | LOW — Landmark entity does not cache `featured` filter data for offline

**File:** [core/database/src/main/java/com/wadjet/core/database/dao/LandmarkDao.kt](core/database/src/main/java/com/wadjet/core/database/dao/LandmarkDao.kt#L16-L28)
**Description:** `getFiltered()` only filters by `category` (type) and `city`. It cannot filter by `featured` or by `era`. The entity stores these fields but the DAO query doesn't use them for filtering.

---

## S7-13 | INFO — ScanApiService endpoint uses `/api/scan` not `/api/scan/image`

**File:** [core/network/src/main/java/com/wadjet/core/network/api/ScanApiService.kt](core/network/src/main/java/com/wadjet/core/network/api/ScanApiService.kt#L13)
**Description:** URL path is `POST /api/scan` but expected contract says `/api/scan/image`. This may be correct (server may accept both) but should be verified. The server's actual endpoint name determines if this is a bug.

---

## S7-14 | INFO — Scan result screen shows error for null error with `?: stringResource`

**File:** [feature/scan/src/main/java/com/wadjet/feature/scan/screen/ScanScreen.kt](feature/scan/src/main/java/com/wadjet/feature/scan/screen/ScanScreen.kt#L115-L125)
**Description:** The error display uses `state.error?.let { error -> ... ErrorState(message = error ?: ...) }`. Since we're inside `let`, `error` is non-null, so the `?:` fallback is dead code. Harmless but misleading.

---

## Summary — Stage 7

| Severity | Count |
|----------|-------|
| CRITICAL | 0     |
| MAJOR    | 2     |
| MEDIUM   | 5     |
| LOW      | 4     |
| INFO     | 2     |
| **Total**| **13**|
