# STAGE 11 REPORT: Web Parity & API Surface Audit

> **Date**: 2026-04-15
> **Scope**: All Retrofit API services, DTOs, interceptors, repositories, domain models, caching layer
> **Files read**: 11 API services, 10 DTO files, 2 interceptors, 1 token manager, 1 network module, 9 repository impls, 9 domain models

---

## A. API Completeness — Full Retrofit Annotation Census

### All Discovered Annotations

| # | Service | Method | Annotation | Path |
|---|---------|--------|-----------|------|
| 1 | AudioApiService | speak | `@POST` | `api/audio/speak` |
| 2 | AudioApiService | stt | `@POST` (Multipart) | `api/audio/stt` |
| 3 | AuthApiService | register | `@POST` | `api/auth/register` |
| 4 | AuthApiService | login | `@POST` | `api/auth/login` |
| 5 | AuthApiService | googleAuth | `@POST` | `api/auth/google` |
| 6 | AuthApiService | refresh | `@POST` | `api/auth/refresh` |
| 7 | AuthApiService | logout | `@POST` | `api/auth/logout` |
| 8 | AuthApiService | forgotPassword | `@POST` | `api/auth/forgot-password` |
| 9 | ChatApiService | clearChat | `@POST` | `api/chat/clear` |
| 10 | DictionaryApiService | getSigns | `@GET` | `api/dictionary` |
| 11 | DictionaryApiService | getCategories | `@GET` | `api/dictionary/categories` |
| 12 | DictionaryApiService | getAlphabet | `@GET` | `api/dictionary/alphabet` |
| 13 | DictionaryApiService | getLesson | `@GET` | `api/dictionary/lesson/{level}` |
| 14 | DictionaryApiService | getSign | `@GET` | `api/dictionary/{code}` |
| 15 | FeedbackApiService | submit | `@POST` | `api/feedback` |
| 16 | LandmarkApiService | getLandmarks | `@GET` | `api/landmarks` |
| 17 | LandmarkApiService | getCategories | `@GET` | `api/landmarks/categories` |
| 18 | LandmarkApiService | getLandmarkDetail | `@GET` | `api/landmarks/{slug}` |
| 19 | LandmarkApiService | getLandmarkChildren | `@GET` | `api/landmarks/{slug}/children` |
| 20 | LandmarkApiService | identifyLandmark | `@POST` (Multipart) | `api/explore/identify` |
| 21 | ScanApiService | scan | `@POST` (Multipart) | `/api/scan` |
| 22 | StoriesApiService | getStories | `@GET` | `api/stories` |
| 23 | StoriesApiService | getStory | `@GET` | `api/stories/{storyId}` |
| 24 | StoriesApiService | interact | `@POST` | `api/stories/{storyId}/interact` |
| 25 | StoriesApiService | generateChapterImage | `@POST` | `api/stories/{storyId}/chapters/{index}/image` |
| 26 | TranslateApiService | translate | `@POST` | `api/translate` |
| 27 | UserApiService | getProfile | `@GET` | `api/user/profile` |
| 28 | UserApiService | updateProfile | `@PATCH` | `api/user/profile` |
| 29 | UserApiService | changePassword | `@PATCH` | `api/user/password` |
| 30 | UserApiService | getScanHistory | `@GET` | `api/user/history` |
| 31 | UserApiService | getFavorites | `@GET` | `api/user/favorites` |
| 32 | UserApiService | addFavorite | `@POST` | `api/user/favorites` |
| 33 | UserApiService | removeFavorite | `@DELETE` | `api/user/favorites/{item_type}/{item_id}` |
| 34 | UserApiService | getStats | `@GET` | `api/user/stats` |
| 35 | UserApiService | getStoryProgress | `@GET` | `api/user/progress` |
| 36 | UserApiService | saveProgress | `@POST` | `api/user/progress` |
| 37 | UserApiService | getLimits | `@GET` | `api/user/limits` |
| 38 | WriteApiService | write | `@POST` | `api/write` |
| 39 | WriteApiService | getPalette | `@GET` | `api/write/palette` |

**Plus 1 OkHttp SSE (non-Retrofit):**
| 40 | ChatRepositoryImpl | streamChat | OkHttp SSE | `api/chat/stream` |

**Total annotated endpoints: 39 Retrofit + 1 OkHttp SSE = 40 total**

---

## B. Issues

---

#### S11-01 | MEDIUM — ScanApiService path has leading slash, inconsistent with all others
- **File**: [ScanApiService.kt](core/network/src/main/java/com/wadjet/core/network/api/ScanApiService.kt#L12)
- **Evidence**: `@POST("/api/scan")` — all other endpoints use relative path (no leading `/`). With Retrofit's `baseUrl` ending in `/`, a leading slash makes the path absolute (replaces the entire path segment of the base URL). Since the base URL is `https://nadercr7-wadjet-v2.hf.space/`, this still works by coincidence, but it's inconsistent and fragile.
- **Fix**: Change to `@POST("api/scan")`

---

#### S11-02 | MAJOR — DTO → Domain field drops: SignDetailDto loses `exampleUsages`, `relatedSigns`, `logographicValue`, `determinativeClass`
- **File**: [DictionaryRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/DictionaryRepositoryImpl.kt#L169-L185)
- **Evidence**: The `SignDetailDto.toDomain()` mapper drops these fields:
  - `exampleUsages: List<ExampleUsageDto>?` — NOT mapped to Sign domain model
  - `relatedSigns: List<RelatedSignDto>?` — NOT mapped
  - `logographicValue: String?` — NOT mapped
  - `determinativeClass: String?` — NOT mapped
- **Impact**: Single-sign detail view cannot display example usages, related signs, logographic values, or determinative classes even though the API provides them.

---

#### S11-03 | MEDIUM — DTO → Domain field drops: ExampleWordDto.speechText and PracticeWordDto.speechText lost in Lesson mapping
- **File**: [DictionaryRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/DictionaryRepositoryImpl.kt#L112-L130)
- **Evidence**: `ExampleWord` domain model has no `speechText` field; `PracticeWord` likewise. The DTO provides `speechText` for TTS but it's silently discarded.
- **Impact**: Cannot pronounce example/practice words in lessons.

---

#### S11-04 | MEDIUM — DTO → Domain field drops: TranslateResponse.latencyMs lost
- **File**: [TranslateRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/TranslateRepositoryImpl.kt#L22-L32)
- **Evidence**: `TranslationResult` domain model lacks `latencyMs`. The backend reports translation latency but it's discarded.
- **Impact**: Cannot display response time metrics to user. Low user impact, useful for debug/stats.

---

#### S11-05 | MAJOR — FeedbackRepositoryImpl ignores HTTP error status codes
- **File**: [FeedbackRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/FeedbackRepositoryImpl.kt#L17-L24)
- **Evidence**:
  ```kotlin
  val response = feedbackApi.submit(...)
  val body = response.body() ?: throw Exception("Failed to submit feedback")
  body.id
  ```
  If the response is a 4xx/5xx, `response.body()` is null but `response.errorBody()` has the error. The code throws a generic "Failed to submit feedback" instead of parsing the actual error detail. This differs from other repos that check `response.isSuccessful` first.

---

#### S11-06 | LOW — UserRepositoryImpl.removeFavorite silently swallows errors
- **File**: [UserRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/UserRepositoryImpl.kt#L82-L87)
- **Evidence**:
  ```kotlin
  if (!response.isSuccessful) {
      Timber.w("Remove favorite failed: ${response.code()}")
  }
  ```
  Unlike `addFavorite` which throws, `removeFavorite` just logs a warning. The user gets `Result.success(Unit)` even on server failure. Inconsistent error contract.

---

#### S11-07 | MEDIUM — Inconsistent error handling across repositories
- **Description**: Five different error-handling patterns exist:
  1. **Pattern A** (AuthRepo): `response.isSuccessful` check → parse errorBody → throw custom `AuthException`
  2. **Pattern B** (DictionaryRepo, ExploreRepo, ScanRepo): `response.isSuccessful` check → throw `ApiException` with code
  3. **Pattern C** (FeedbackRepo): No `isSuccessful` check → null-check on body
  4. **Pattern D** (UserRepo): Mix of `isSuccessful` check and null-check depending on method
  5. **Pattern E** (StoriesRepo): `response.isSuccessful` check → generic `Exception`
- **Impact**: Inconsistent user-facing error messages. Some errors include API status codes, some include parsed detail text, some are generic.

---

#### S11-08 | MEDIUM — Rate limiter `Thread.sleep()` blocks OkHttp dispatcher threads
- **File**: [RateLimitInterceptor.kt](core/network/src/main/java/com/wadjet/core/network/RateLimitInterceptor.kt#L36)
- **Evidence**:
  ```kotlin
  Thread.sleep(waitMs)   // L36 — 429 retry
  Thread.sleep(backoffMs) // L46 — 503 retry
  ```
  Already flagged in Stage 9 (S9-01). Blocks OkHttp thread pool. Under concurrent requests, can exhaust all dispatcher threads.
- **Note**: Cross-reference with S9-01. Not a new finding but confirmed from API surface perspective.

---

#### S11-09 | LOW — RateLimitInterceptor 429 retry does NOT re-check status after retry
- **File**: [RateLimitInterceptor.kt](core/network/src/main/java/com/wadjet/core/network/RateLimitInterceptor.kt#L22-L38)
- **Evidence**: After sleeping and retrying once for a 429, if the retry ALSO returns 429, it's returned as-is. The retry logic only executes once — no loop. This is intentional to avoid infinite retries, but the 503 path has 3 retries while 429 only has 1. Asymmetric.

---

#### S11-10 | LOW — AuthInterceptor regex-based JSON parsing is fragile
- **File**: [AuthInterceptor.kt](core/network/src/main/java/com/wadjet/core/network/AuthInterceptor.kt#L150-L152)
- **Evidence**:
  ```kotlin
  val regex = """"access_token"\s*:\s*"([^"]+)"""".toRegex()
  return regex.find(json)?.groupValues?.get(1)
  ```
  Comment says "avoids adding JSON dep to interceptor hot path" — but `kotlinx.serialization.json.Json` is already a transitive dependency in the module. If the token contains escaped characters (unlikely but possible with JWT), the regex fails.

---

#### S11-11 | MAJOR — Pagination: Only dictionary and landmarks implement pagination correctly
- **Analysis**:
  - **DictionaryApiService**: ✅ `page`/`per_page` params → `DictionaryResponse` has `total_pages` — correctly paginated by ViewModel `loadMore()`
  - **LandmarkApiService**: ✅ `page`/`per_page` params → `LandmarkListResponse` has `total_pages` — correctly paginated
  - **UserApiService.getScanHistory**: ❌ Returns `List<ScanHistoryItemDto>` with NO pagination — fetches all history at once
  - **UserApiService.getFavorites**: ❌ Returns `List<FavoriteItemDto>` with NO pagination
  - **UserApiService.getStoryProgress**: ❌ Returns `List<StoryProgressItemDto>` with NO pagination
  - **StoriesApiService.getStories**: ❌ Returns all stories — `StoriesListResponse` has `count` but no `page`/`total_pages`
- **Impact**: For users with large scan history or many favorites, these unpaginated lists could cause OOM or slow loads.

---

#### S11-12 | MEDIUM — No offline caching for stories, translate, or user profile
- **Caching analysis**:
  - **Dictionary signs**: ✅ Cached in Room (`SignEntity`) — offline fallback on `IOException`
  - **Landmarks**: ✅ Cached in Room (`LandmarkEntity`) — offline fallback, detail JSON stored
  - **Scan results**: ✅ Stored in Room (`ScanResultEntity`) — local history
  - **Stories**: ❌ No Room caching — `getStories()` and `getStory()` fail completely offline
  - **Story progress**: Partial — Firebase Firestore has offline persistence, but REST API sync has no fallback
  - **User profile/stats**: ❌ No caching — fail offline
  - **Translate results**: ❌ No caching — backend has `from_cache` field suggesting server-side caching, but client has none
  - **Chat**: ✅ `ChatHistoryStore` uses JSON files — but no offline message queue
- **Impact**: Stories, translate, and user features completely unavailable offline even if previously loaded.

---

#### S11-13 | LOW — Cache invalidation strategy is implicit / absent
- **Description**: Room caches for signs and landmarks are append-only:
  - `signDao.insertAll()` uses `@Insert(onConflict = REPLACE)` — old data is only replaced when the same code appears, never purged
  - `landmarkDao.insertAll()` same pattern
  - No TTL, no max-age, no cache-busting mechanism
  - If the backend updates a sign's description, the app shows stale data until the user navigates to that exact page again
- **Impact**: Stale offline data. Not critical since cache is a fallback, but there's no "stale while revalidate" pattern.

---

#### S11-14 | INFO — HttpLoggingInterceptor correctly scoped to DEBUG builds
- **File**: [NetworkModule.kt](core/network/src/main/java/com/wadjet/core/network/di/NetworkModule.kt#L55-L60)
- **Evidence**:
  ```kotlin
  level = if (com.wadjet.core.network.BuildConfig.DEBUG) {
      HttpLoggingInterceptor.Level.BODY
  } else {
      HttpLoggingInterceptor.Level.NONE
  }
  ```
  ✅ Correctly uses `BuildConfig.DEBUG` to gate logging. **BODY** level in debug logs full request/response including auth tokens — acceptable for dev, stripped for release.

---

#### S11-15 | INFO — No Timber debug-level (`.d()`) logging found in network layer
- **Evidence**: Searched all files under `core/network/`. Only `Timber.w()` and `Timber.e()` are used. No `.d()` calls that would pollute production logs.
- ✅ Clean.

---

#### S11-16 | MEDIUM — ExploreRepositoryImpl favorites state is in-memory only and not synchronized
- **File**: [ExploreRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/ExploreRepositoryImpl.kt#L40-L41)
- **Evidence**:
  ```kotlin
  private val _favorites = MutableStateFlow<Set<String>>(emptySet())
  private var favoritesLoaded = false
  ```
  The favorites set is loaded once from API, then mutated locally. If another device modifies favorites, or if the app is killed and restarted, the stale `favoritesLoaded = false` gate triggers a reload — but between loads, favorites can be out of sync.
- **Edge case**: `toggleFavorite` optimistically updates `_favorites` before the API call. If the API call fails for `removeFavorite` (which swallows errors — see S11-06), the local state diverges from server state.

---

#### S11-17 | MAJOR — AuthInterceptor token refresh is not truly thread-safe due to `runBlocking` usage
- **File**: [AuthInterceptor.kt](core/network/src/main/java/com/wadjet/core/network/AuthInterceptor.kt#L79-L113)
- **Evidence**:
  ```kotlin
  val newToken = runBlocking {
      mutex.withLock { ... }
  }
  ```
  `runBlocking` inside an OkHttp interceptor blocks the calling thread. The `Mutex` prevents concurrent refreshes, but if two threads hit 401 simultaneously:
  1. Thread A enters `runBlocking` → acquires mutex → starts refresh
  2. Thread B enters `runBlocking` → waits for mutex → **blocks OkHttp dispatcher thread**
  This can cause thread starvation under high-concurrency 401 storms (e.g. expired token + multiple parallel requests).

---

#### S11-18 | LOW — `User-Agent` header hardcoded to version 1.0.0
- **File**: [NetworkModule.kt](core/network/src/main/java/com/wadjet/core/network/di/NetworkModule.kt#L63-L66)
- **Evidence**: `.header("User-Agent", "Wadjet-Android/1.0.0")` — does not use `BuildConfig.VERSION_NAME`. Will be stale after version bumps.

---

## C. Summary

| Severity | Count |
|----------|-------|
| CRITICAL | 0 |
| MAJOR | 4 (S11-02, S11-05, S11-11, S11-17) |
| MEDIUM | 6 (S11-01, S11-03, S11-07, S11-08, S11-12, S11-16) |
| LOW | 4 (S11-04, S11-06, S11-09, S11-10, S11-13, S11-18) |
| INFO | 2 (S11-14, S11-15) |

**Total Retrofit endpoints: 39. Total OkHttp SSE: 1. Grand total: 40 mapped endpoints.**
