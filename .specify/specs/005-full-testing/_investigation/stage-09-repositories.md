# Stage 9 — Repository Layer Audit

**Date:** 2025-07-22  
**Auditor:** Automated (Copilot)  
**Scope:** All repository interfaces, implementations, error handling, caching, thread safety

---

## Summary

| Metric | Value |
|---|---|
| **Total repositories** | 9 |
| **Return pattern** | `Result<T>` via `suspendRunCatching` (mostly) |
| **Retry logic** | **NONE** in any repository |
| **WadjetResult sealed class** | Exists but **NEVER USED** by any repo |
| **Offline-capable repos** | 3 (Dictionary, Explore, Stories-progress) |
| **Critical thread safety issues** | 2 |

---

## 1. All Repositories

| Repository | Interface | Implementation | Error Pattern | Offline Support |
|---|---|---|---|---|
| **AuthRepository** | `AuthRepository` | `DefaultAuthRepository` | `suspendRunCatching` → `Result<T>` | None (network-only) |
| **DictionaryRepository** | `DictionaryRepository` | `DefaultDictionaryRepository` | `suspendRunCatching` → `Result<T>` | **YES** — Room cache (signs, categories, detail) |
| **ExploreRepository** | `ExploreRepository` | `DefaultExploreRepository` | `suspendRunCatching` → `Result<T>` | **YES** — Room cache (landmarks, favorites) |
| **ScanRepository** | `ScanRepository` | `DefaultScanRepository` | `suspendRunCatching` → `Result<T>` | Local scan history only |
| **ChatRepository** | `ChatRepository` | `DefaultChatRepository` | Manual try/catch, manual JSON | None (SSE streaming) |
| **StoriesRepository** | `StoriesRepository` | `DefaultStoriesRepository` | `suspendRunCatching` → `Result<T>` | **Partial** — Firestore progress only |
| **FeedbackRepository** | `FeedbackRepository` | `DefaultFeedbackRepository` | `suspendRunCatching` → `Result<T>` | None |
| **UserRepository** | `UserRepository` | `DefaultUserRepository` | `suspendRunCatching` → `Result<T>` | None |
| **SettingsRepository** | `SettingsRepository` | `DefaultSettingsRepository` | `suspendRunCatching` → `Result<T>` | DataStore (tts prefs only) |

---

## 2. Error Handling Patterns

Three competing patterns across the codebase:

### Pattern A — JSON body parsing
```kotlin
val body = response.errorBody()?.string()
val json = JSONObject(body)
throw ApiException(json.optString("detail", "Unknown error"))
```
**Used by:** Auth, Scan, User — most thorough but verbose

### Pattern B — Regex extraction
```kotlin
val msg = e.message?.let { Regex("\"detail\":\"(.+?)\"").find(it)?.groupValues?.get(1) }
```
**Used by:** Dictionary, Explore — fragile, breaks if API changes quotes/escaping

### Pattern C — Raw exception
```kotlin
suspendRunCatching { api.doThing() }
```
**Used by:** Feedback, Settings — no error message extraction at all

### `WadjetResult` Sealed Class (DEAD CODE)
```kotlin
sealed class WadjetResult<out T> {
    data class Success<T>(val data: T) : WadjetResult<T>()
    data class Error(val message: String, val code: Int? = null) : WadjetResult<Nothing>()
    data object Loading : WadjetResult<Nothing>()
}
```
Defined in `core/common` but **never referenced** by any repository or ViewModel. All repos use Kotlin `Result<T>` instead.

---

## 3. Caching Strategy

| Repository | Cache Layer | Invalidation | TTL |
|---|---|---|---|
| **Dictionary** | Room (signs, categories, detail) | Never auto-invalidated, manual clear in Settings | None |
| **Explore** | Room (landmarks, favorites) | Favorites: local-only toggle. Landmarks: overwritten on fetch | None |
| **Stories** | Firestore (progress docs) | Real-time listener | None |
| **Scan** | Room (scan history) | Local-only, never synced | None |
| **Settings** | DataStore (2 keys) | On write | None |
| **Auth** | EncryptedSharedPreferences (tokens) | On login/logout/refresh | None |
| **Chat, Feedback, User** | None | — | — |

**No repo has stale cache eviction or max-age checking.**

---

## 4. Thread Safety Issues

### CRITICAL — StoriesRepository: `runBlocking` in Firestore Listener

```kotlin
firestore.collection("users/$uid/story_progress")
    .addSnapshotListener { snapshot, _ ->
        runBlocking {
            snapshot?.documents?.forEach { doc ->
                storyProgressDao.upsert(doc.toStoryProgress())
            }
        }
    }
```
- Firestore listener runs on main thread
- `runBlocking` blocks it while writing to Room
- **Risk:** ANR on large updates

### CRITICAL — ExploreRepository: `favoritesLoaded` Race Condition

```kotlin
private var favoritesLoaded = false  // NOT volatile, NOT synchronized

suspend fun getLandmarks(): Result<List<Landmark>> {
    if (!favoritesLoaded) {
        loadFavorites()  // sets favoritesLoaded = true
    }
    ...
}
```
- Multiple coroutines can enter `if (!favoritesLoaded)` simultaneously
- `loadFavorites()` runs multiple times on first access
- Results in duplicate Room writes and wasted network calls

---

## 5. Inconsistent Return Types

Most methods return `Result<T>`, but several break the pattern:

| Repository | Method | Returns |
|---|---|---|
| `ExploreRepository` | `toggleFavorite()` | `Unit` (no Result wrapper) |
| `ExploreRepository` | `isFavorite()` | `Boolean` (no Result wrapper) |
| `ChatRepository` | `sendMessage()` | `Flow<String>` (raw SSE tokens) |
| `StoriesRepository` | `observeProgress()` | `Flow<List<StoryProgress>>` (bare Flow) |

---

## 6. ChatRepository — Manual JSON Building

```kotlin
val jsonBody = """{"message":"${message.replace("\"", "\\\"")}","landmark":"${landmark ?: ""}"}"""
```
- Hand-rolled JSON string with manual escaping
- Fragile: doesn't handle newlines, backslashes, unicode
- Should use kotlinx.serialization or JSONObject

---

## 7. Missing Retry Logic

**Zero repositories implement retry for transient failures.** All network calls are single-attempt:
- No exponential backoff
- No retry on 5xx
- No retry on IOException
- Token refresh is the only "retry" (handled by OkHttp Authenticator, not repo)

---

## 8. Priority Issues

| # | Severity | Issue | Location |
|---|---|---|---|
| 1 | **Critical** | `runBlocking` in Firestore SnapshotListener blocks main thread → ANR risk | `StoriesRepository` |
| 2 | **Critical** | `favoritesLoaded` var not synchronized — race condition on first access | `ExploreRepository` |
| 3 | **High** | No retry mechanism in any repository — single-attempt for all network calls | All repositories |
| 4 | **Medium** | `WadjetResult` sealed class exists but never used — dead code | `core/common` |
| 5 | **Medium** | 3 different error parsing patterns across repos — inconsistent user messages | Auth/Scan vs Dict/Explore vs Feedback |
| 6 | **Medium** | Some methods return bare types instead of `Result<T>` — caller must handle exceptions | Various |
| 7 | **Low** | ChatRepository builds JSON manually — fragile escaping | `ChatRepository` |
| 8 | **Low** | No `@Volatile` on any mutable state across all repos | All |
