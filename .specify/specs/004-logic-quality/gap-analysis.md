# Logic & Quality Gap Analysis: Wadjet Android

**Date**: 2026-04-15
**Evidence**: 12 investigation stage files in `_investigation/`
**Methodology**: Zero-hallucination staged investigation — every finding traced to source file + line

---

## Summary

**Overall logic health: MODERATE** — Core features work but have significant data drops, lifecycle bugs, and a fragile auth layer.

**Top 5 Critical Findings:**
1. **InteractResponse drops 5 fields** (S8-01) — story interaction feedback completely broken for wrong answers
2. **Sign domain model drops 4 fields** (S10-01) — core Egyptological data invisible to users
3. **Split-brain Firebase+backend auth** (S9-03) — users can be half-logged-in with all API calls failing
4. **ScanViewModel MediaPlayer leak** (S5-01/S7-01) — audio continues after navigation, file descriptor leak
5. **Zero test coverage for 4 critical repositories** (S12-01–04) — Auth, Chat, Scan, Stories all untested

| Severity | Count |
|----------|-------|
| 🔴 Critical | 14 |
| 🟠 Major | 28 |
| 🟡 Minor | 47 |
| 🔵 Enhancement | 15 |
| **Total** | **104** |

---

## Already Fixed vs. Still Open (from 001-logic-parity)

### Bugs — All Fixed ✓
| ID | Description | Status | Commit Evidence |
|----|-------------|--------|-----------------|
| B1 | Wrong scan field mapping | ✅ FIXED | Scan model refactor commits |
| B2 | Landmark image base URL | ✅ FIXED | Image loading commits |
| B3 | Auth token race condition | ✅ FIXED | AuthInterceptor mutex added |
| B4 | Missing error states | ✅ FIXED | VM error handling commits |
| B5 | Chat SSE parsing | ✅ FIXED | SSE chunk parser commits |

### Gaps — Mixed Status
| ID | Description | Status |
|----|-------------|--------|
| G1 | Complete DTO field mapping | ❌ STILL OPEN (4 fields in Signs, 5 in Stories) |
| G2 | Offline caching | ⚠️ PARTIAL (signs + landmarks done; stories/translate/profile missing) |
| G3 | Error handling consistency | ❌ STILL OPEN (5 different patterns across repos) |
| G4 | Featured landmarks endpoint | ❌ STILL OPEN (never passes `featured=true`) |
| G5 | Pagination | ⚠️ PARTIAL (dictionary + landmarks done; history/favorites/stories not) |
| G6 | Test coverage | ❌ STILL OPEN (~15% coverage) |
| G7 | Camera (CameraX disabled) | ❌ STILL OPEN (camera feature disabled) |
| G8 | ProGuard rules | ✅ FIXED (comprehensive rules) |
| G9 | Build variants | ✅ FIXED (debug + release properly configured) |
| G10 | CI pipeline | ⚠️ PARTIAL (unit tests only, no instrumentation) |

### Additional Gaps Still Open
| ID | Description | Status |
|----|-------------|--------|
| A2 | Email verification enforcement | ❌ STILL OPEN (emailVerified never checked) |
| A3 | In-app password reset (confirm flow) | ❌ STILL OPEN (relies on Firebase WebView) |
| A4 | Free tier wiring | ❌ STILL OPEN (hardcoded limits) |
| A6 | Scan history REST sync | ❌ STILL OPEN (local-only Room) |
| A8 | Arabic partial | ❌ STILL OPEN |
| A12 | Deep links | ❌ STILL OPEN (no intent filters) |

---

## Findings

### A. Egyptological Accuracy (from Stages 6, 12)

#### LQ-001 | 🟡 Minor | Tokenizer does not strip hyphens — spurious epenthesis on unknown compounds
- **Current state:** `MdC_STRIP` in `EgyptianPronunciation.kt` contains `.`, `:`, `=`, `*`, `(`, `)`, `<`, `>`, `!` but NOT `-`
- **Problem:** Unknown compound words (not in WORD_MAP) containing `-` produce spurious vowel epenthesis because `isConsonantSound("-")` returns true
- **Expected:** Hyphens should be stripped or words split on hyphen and each part processed separately
- **Fix:** Add `-` to `MdC_STRIP` or split on `-` in `convertWord()`
- **Files:** `core/common/.../EgyptianPronunciation.kt`
- **Source:** Stage 6, P-04

#### LQ-002 | 🟡 Minor | Tokenizer does not strip digits — Gardiner codes passed to TTS produce garbage
- **Current state:** Digits 0-9 not in `MdC_STRIP`
- **Problem:** If "A40" (determinative code) is accidentally passed to `toSpeech()`, digits flow through as phoneme tokens
- **Fix:** Add `'0'..'9'` to `MdC_STRIP`
- **Files:** `core/common/.../EgyptianPronunciation.kt`
- **Source:** Stage 6, P-05

#### LQ-003 | 🔵 Enhancement | "Already pronounceable" detection has edge case with `u`
- **Current state:** `cleaned.any { it in "eouEOU" }` triggers early return
- **Problem:** Mixed input like "Amun nTr" contains `u` → early return → "nTr" never converted
- **Fix:** Per-word detection or require majority of characters to be vowels
- **Files:** `core/common/.../EgyptianPronunciation.kt`
- **Source:** Stage 6, P-08

#### LQ-004 | 🔵 Enhancement | Duplicate WORD_MAP keys `"Dd"` and `"dSrt"`
- **Current state:** Both keys appear twice in `mapOf()`. Kotlin keeps last entry. Both instances map to the same value so harmless.
- **Fix:** Remove duplicate entries for clarity
- **Files:** `core/common/.../EgyptianPronunciation.kt`
- **Source:** Stage 6, P-01/P-02

#### LQ-005 | 🟡 Minor | GardinerUnicode COMMON_GLYPHS may have incorrect code points
- **Current state:** G1 (Vulture) mapped to `\uD80C\uDD80` (U+13180) but standard Egyptian Hieroglyph G001 is U+13146
- **Problem:** Incorrect hieroglyph rendering for commonly-used signs
- **Fix:** Verify all 38 COMMON_GLYPHS entries against Unicode Egyptian Hieroglyphs block standard
- **Files:** `feature/scan/.../util/GardinerUnicode.kt`
- **Source:** Stage 7, S7-09

#### LQ-006 | 🟡 Minor | CONTEXT tag "hieroglyph_pronunciation" unverified against server
- **Current state:** Sent to server but server-side behavior unknown
- **Fix:** Verify against `audio.py` / `tts_service.py` on backend
- **Files:** `core/common/.../EgyptianPronunciation.kt`
- **Source:** Stage 6, P-10

---

### B. API Contract Fidelity (from Stages 2, 11)

#### LQ-010 | 🔴 Critical | Sign domain model drops 4 API fields (logographicValue, determinativeClass, exampleUsages, relatedSigns)
- **Current state:** `SignDetailDto` has all 4 fields; `toDomain()` and `toEntity()` drop them
- **Problem:** Core Egyptological data invisible to users
- **Fix:** Add fields to `Sign` domain model, `SignEntity`, update mappers
- **Files:** `Dictionary.kt`, `SignEntity.kt`, `DictionaryRepositoryImpl.kt`
- **Source:** Stage 10, S10-01; Stage 11, S11-02

#### LQ-011 | 🔴 Critical | InteractResponse drops 5 fields (correctAnswer, targetGlyph, gardinerCode, hint, choiceId)
- **Current state:** DTO deserializes all 5; mapper drops them all
- **Problem:** Wrong answer feedback impossible — users can't learn from mistakes
- **Fix:** Add fields to `InteractionResult` domain model, update mapper
- **Files:** `Story.kt`, `StoriesRepositoryImpl.kt`
- **Source:** Stage 8, S8-01

#### LQ-012 | 🟡 Minor | ScanApiService has leading slash inconsistency
- **Current state:** `@POST("/api/scan")` — all other endpoints use relative paths
- **Fix:** Change to `@POST("api/scan")`
- **Files:** `ScanApiService.kt`
- **Source:** Stage 11, S11-01

#### LQ-013 | 🟠 Major | FeedbackRepositoryImpl ignores HTTP error status codes
- **Current state:** Doesn't check `response.isSuccessful` — null body throws generic error
- **Fix:** Add `response.isSuccessful` check, parse error body
- **Files:** `FeedbackRepositoryImpl.kt`
- **Source:** Stage 11, S11-05

#### LQ-014 | 🟡 Minor | UserRepositoryImpl.removeFavorite silently swallows errors
- **Current state:** Logs warning but returns `Result.success(Unit)` on server failure
- **Fix:** Throw exception consistent with `addFavorite()`
- **Files:** `UserRepositoryImpl.kt`
- **Source:** Stage 11, S11-06

#### LQ-015 | 🟡 Minor | ExampleWord and PracticeWord drop speechText field
- **Current state:** DTO has `speechText`; domain model doesn't
- **Problem:** Cannot pronounce example/practice words in lessons
- **Fix:** Add `speechText` to domain models and mappers
- **Files:** `Dictionary.kt`, `DictionaryRepositoryImpl.kt`
- **Source:** Stage 11, S11-03

#### LQ-016 | 🟠 Major | 5 different error handling patterns across repositories
- **Current state:** AuthRepo (custom AuthException), DictionaryRepo (ApiException), FeedbackRepo (null check), UserRepo (mixed), StoriesRepo (generic Exception)
- **Fix:** Standardize to single pattern
- **Files:** All 9 repository implementations
- **Source:** Stage 11, S11-07

#### LQ-017 | 🟠 Major | 4 API lists have no pagination (scan history, favorites, progress, stories)
- **Current state:** Return all items in single response
- **Impact:** OOM risk for users with large histories
- **Fix:** Add pagination support
- **Files:** `UserApiService.kt`, `StoriesApiService.kt`
- **Source:** Stage 11, S11-11

---

### C. Data Layer & Mapping (from Stage 3)

#### LQ-020 | 🟠 Major | Auth drops `createdAt` field from backend response
- **Source:** Stage 3, S3-01

#### LQ-021 | 🔵 Enhancement | TranslateResponse drops `latencyMs` field
- **Source:** Stage 11, S11-04

---

### D. Database & Offline (from Stages 4, 7, 10)

#### LQ-030 | 🔴 Critical | FTS4 has no relevance ranking — search results alphabetical, not by relevance
- **Current state:** `@Fts4`, `ORDER BY signs.code` — no BM25 or matchinfo ranking
- **Fix:** Upgrade to `@Fts5`, add `bm25()` ranking
- **Files:** `SignFtsEntity.kt`, `SignDao.kt`
- **Source:** Stage 10, S10-02

#### LQ-031 | 🟠 Major | Offline browse fallback ignores search query
- **Current state:** IOException → returns all cached signs alphabetically, ignores user's search term
- **Fix:** Call `searchOffline()` when search query is present
- **Files:** `DictionaryRepositoryImpl.kt`
- **Source:** Stage 10, S10-04

#### LQ-032 | 🟠 Major | No offline caching for categories, alphabet, lessons
- **Fix:** Cache in Room
- **Files:** `DictionaryRepositoryImpl.kt`, new entities
- **Source:** Stage 10, S10-06

#### LQ-033 | 🟠 Major | SignEntity doesn't cache enough fields for full detail display
- **Fix:** Add 4 new columns (logographicValue, determinativeClass, exampleUsages, relatedSigns)
- **Files:** `SignEntity.kt`
- **Source:** Stage 10, S10-07

#### LQ-034 | 🟡 Minor | LandmarkDetail offline fallback doesn't catch IOException
- **Current state:** Only falls back on non-200; IOException propagates uncaught
- **Fix:** Add IOException catch with Room fallback
- **Files:** `ExploreRepositoryImpl.kt`
- **Source:** Stage 7, S7-08

#### LQ-035 | 🟡 Minor | getSign() offline fallback has no IOException catch
- **Fix:** Add IOException catch, try Room cache first
- **Files:** `DictionaryRepositoryImpl.kt`
- **Source:** Stage 10, S10-16

#### LQ-036 | 🟡 Minor | FTS query sanitization strips Unicode characters including diacritics
- **Current state:** `\w` matches only `[a-zA-Z0-9_]` — strips ḥ, ḫ, š, etc.
- **Fix:** Use a Unicode-aware regex or whitelist MdC characters
- **Files:** `DictionaryRepositoryImpl.kt`
- **Source:** Stage 10, S10-03

#### LQ-037 | 🟡 Minor | Database version 4 with exportSchema=false — no migration testing
- **Fix:** Enable `exportSchema = true`, write migration for v4→v5
- **Files:** `WadjetDatabase.kt`
- **Source:** Stage 10, S10-20

#### LQ-038 | 🟡 Minor | No offline caching for stories, translate, or user profile
- **Source:** Stage 11, S11-12

#### LQ-039 | 🔵 Enhancement | cachedAt timestamp never used — no TTL or staleness check
- **Source:** Stage 10, S10-18

#### LQ-040 | 🔵 Enhancement | FTS entity only indexes 5 columns — reading and speechText not searchable
- **Source:** Stage 10, S10-15

#### LQ-041 | 🟡 Minor | ExploreViewModel offline fallback only for search, not category/city browsing
- **Source:** Stage 7, S7-10

#### LQ-042 | 🔵 Enhancement | Landmark entity cannot filter by `featured` or `era`
- **Source:** Stage 7, S7-12

---

### E. ViewModel Logic (from Stage 5)

#### LQ-050 | 🔴 Critical | ScanViewModel: No onCleared() — MediaPlayer leaked
- **Fix:** Override `onCleared()`, release MediaPlayer, delete temp files
- **Files:** `ScanViewModel.kt`
- **Source:** Stage 5, S5-01; Stage 7, S7-01

#### LQ-051 | 🔴 Critical | HistoryViewModel: refresh() spawns duplicate perpetual Flow collectors
- **Fix:** Store Job, cancel before re-launch; or use `flatMapLatest`
- **Files:** `HistoryViewModel.kt`
- **Source:** Stage 5, S5-02; Stage 7, S7-05

#### LQ-052 | 🔴 Critical | ChatHistoryStore: Blocking file I/O on main thread
- **Fix:** Wrap in `withContext(Dispatchers.IO)`
- **Files:** `ChatHistoryStore.kt`, `ChatViewModel.kt`
- **Source:** Stage 5, S5-03

#### LQ-053 | 🔴 Critical | StoryReaderViewModel: saveCurrentProgress() in onCleared() cancelled immediately
- **Fix:** Use `withContext(NonCancellable)` or `applicationScope`
- **Files:** `StoryReaderViewModel.kt`
- **Source:** Stage 5, S5-04

#### LQ-054 | 🟠 Major | ChatViewModel: retryLastMessage() no isStreaming guard — streamJob overwritten
- **Source:** Stage 5, S5-05

#### LQ-055 | 🟠 Major | StoryReaderViewModel: restoreProgress() vs loadStory() race condition
- **Source:** Stage 5, S5-06

#### LQ-056 | 🟠 Major | StoryReaderViewModel: speakAndWait bypasses narration cancellation
- **Source:** Stage 5, S5-07

#### LQ-057 | 🟠 Major | DashboardViewModel: All 4 failures silent — error never shown
- **Source:** Stage 5, S5-08

#### LQ-058 | 🟠 Major | LOCAL_TTS error-as-signal anti-pattern in 4 VMs
- **Source:** Stage 5, S5-09

#### LQ-059 | 🟡 Minor | No double-submit guard in Auth/Translate/Write/Feedback/Settings
- **Source:** Stage 5, S5-10

#### LQ-060 | 🟡 Minor | ExploreViewModel stale page snapshot in loadMore()
- **Source:** Stage 5, S5-11

#### LQ-061 | 🟡 Minor | SettingsViewModel: signOut ignores result, passwords in state
- **Source:** Stage 5, S5-12

#### LQ-062 | 🟡 Minor | StoryReaderViewModel: MutableSet in data class breaks StateFlow equality
- **Source:** Stage 5, S5-15; Stage 8, S8-15

#### LQ-063 | 🟠 Major | DictionaryViewModel MediaPlayer closure capture bug on rapid speak
- **Source:** Stage 10, S10-08

---

### F. TTS & Audio Pipeline (from Stages 6, 7, 8)

#### LQ-070 | 🟠 Major | Temp TTS files in ScanViewModel never deleted
- **Files:** `ScanViewModel.kt`
- **Source:** Stage 7, S7-02

#### LQ-071 | 🟡 Minor | Compressed scan images accumulate in cache without cleanup
- **Source:** Stage 7, S7-03

#### LQ-072 | 🟡 Minor | TTS temp files not cleaned on error path in DictionaryViewModel
- **Source:** Stage 10, S10-09

#### LQ-073 | 🟡 Minor | Story narration timing estimate crude and unreliable (local TTS fallback)
- **Source:** Stage 8, S8-06

#### LQ-074 | 🟡 Minor | StoryReaderViewModel restoreProgress creates duplicate collector
- **Source:** Stage 8, S8-07

#### LQ-075 | 🔵 Enhancement | SignDetailViewModel: TTS failure completely silent
- **Source:** Stage 5, S5-13

---

### G. Scan & Landmarks (from Stage 7)

#### LQ-080 | 🟡 Minor | ScanApi endpoint mismatch: `/api/scan` vs expected `/api/scan/image`
- **Source:** Stage 7, S7-04/S7-13

#### LQ-081 | 🟡 Minor | LandmarkApi missing `/api/landmarks/featured` — ExploreViewModel never requests featured
- **Source:** Stage 7, S7-07

#### LQ-082 | 🟡 Minor | ScanHistoryScreen swipe-to-delete race condition — dismiss state never reset
- **Source:** Stage 7, S7-06

#### LQ-083 | 🟡 Minor | IdentifyViewModel does not check free-tier scan limits
- **Source:** Stage 7, S7-11

---

### H. Chat & Stories (from Stage 8)

#### LQ-090 | 🟠 Major | SSE stream bypasses Retrofit interceptors (auth, rate limit)
- **Source:** Stage 8, S8-02

#### LQ-091 | 🟠 Major | ChatViewModel session mismatch after loading old conversation
- **Fix:** Update `sessionId` when loading conversation
- **Source:** Stage 8, S8-03

#### LQ-092 | 🟡 Minor | Chat suggestion chips: race between onInputChanged and onSend
- **Source:** Stage 8, S8-08

#### LQ-093 | 🟡 Minor | Stories have zero offline support
- **Source:** Stage 8, S8-09

#### LQ-094 | 🟠 Major | StoriesRepositoryImpl.saveProgress silently fails both paths
- **Source:** Stage 8, S8-10

#### LQ-095 | 🟡 Minor | Chat SSE has no retry/reconnect mechanism
- **Source:** Stage 8, S8-05

#### LQ-096 | 🟡 Minor | clearChat reuses old sessionId (val initialized in init)
- **Source:** Stage 8, S8-13

#### LQ-097 | 🔵 Enhancement | Chat history stored in plain JSON (no encryption)
- **Source:** Stage 8, S8-04

#### LQ-098 | 🔵 Enhancement | Chat has no offline indicator
- **Source:** Stage 8, S8-17

#### LQ-099 | 🔵 Enhancement | Chat history uses JSONObject, not kotlinx.serialization
- **Source:** Stage 8, S8-18

#### LQ-100 | 🟡 Minor | FREE_STORY_LIMIT hardcoded, based on list position not story ID
- **Source:** Stage 8, S8-14

#### LQ-101 | 🟡 Minor | StoriesViewModel.loadFavorites ignores failure
- **Source:** Stage 8, S8-16

---

### I. Auth & Security (from Stage 9)

#### LQ-110 | 🔴 Critical | Dual auth creates split-brain state (Firebase ✓ + backend ✗)
- **Fix:** If backend fails, sign out Firebase, surface error
- **Files:** `AuthRepositoryImpl.kt`
- **Source:** Stage 9, S9-03

#### LQ-111 | 🔴 Critical | `runBlocking` in AuthInterceptor blocks OkHttp threads → thread starvation
- **Fix:** Use OkHttp Authenticator interface
- **Files:** `AuthInterceptor.kt`
- **Source:** Stage 9, S9-02; Stage 11, S11-17

#### LQ-112 | 🔴 Critical | `Thread.sleep` in RateLimitInterceptor blocks OkHttp dispatcher threads
- **Fix:** Return error, retry at VM layer with coroutine delay
- **Files:** `RateLimitInterceptor.kt`
- **Source:** Stage 9, S9-01; Stage 11, S11-08

#### LQ-113 | 🟠 Major | Missing API endpoints: send-verification, verify-email, reset-password-confirm
- **Source:** Stage 9, S9-04

#### LQ-114 | 🟠 Major | forgotPasswordSent never resets — shows stale success state
- **Source:** Stage 9, S9-05

#### LQ-115 | 🟠 Major | No email verification enforcement — unverified users have full access
- **Source:** Stage 9, S9-06

#### LQ-116 | 🟠 Major | isLoggedIn is one-shot non-reactive snapshot
- **Fix:** Make reactive with `StateFlow`/`callbackFlow`
- **Source:** Stage 9, S9-07

#### LQ-117 | 🟠 Major | Sign-out has no navigation reset — user stays on current screen
- **Source:** Stage 9, S9-08

#### LQ-118 | 🟠 Major | Refresh token handling: no cookie jar, no Secure/HttpOnly enforcement
- **Source:** Stage 9, S9-09

#### LQ-119 | 🟠 Major | JSON parsing via regex in auth interceptor — fragile, breakable
- **Fix:** Use kotlinx.serialization
- **Source:** Stage 9, S9-10

#### LQ-120 | 🟡 Minor | No client-side brute-force protection on login
- **Source:** Stage 9, S9-11

#### LQ-121 | 🟡 Minor | Password validation allows weak passwords (no special chars)
- **Source:** Stage 9, S9-12

#### LQ-122 | 🟡 Minor | Email validation regex overly permissive
- **Source:** Stage 9, S9-13

#### LQ-123 | 🟡 Minor | Password sent to both Firebase AND backend (should use ID token)
- **Source:** Stage 9, S9-14

#### LQ-124 | 🟡 Minor | Deprecated MasterKeys API for EncryptedSharedPreferences
- **Source:** Stage 9, S9-16

#### LQ-125 | 🟡 Minor | Login sends password before validation completes
- **Source:** Stage 9, S9-17

#### LQ-126 | 🔵 Enhancement | No PKCE/nonce for Google Sign-In
- **Source:** Stage 9, S9-19

#### LQ-127 | 🔵 Enhancement | Auth state uses two separate sources of truth
- **Source:** Stage 9, S9-21

---

### J. Dictionary, Translate & Write (from Stage 10)

#### LQ-130 | 🟡 Minor | Favorites use server-only storage — lost offline
- **Source:** Stage 10, S10-10

#### LQ-131 | 🟡 Minor | Browse search debounce 400ms but no minimum query length
- **Source:** Stage 10, S10-11

#### LQ-132 | 🟡 Minor | Sign grid uses GridCells.Fixed(3) — doesn't adapt
- **Source:** Stage 10, S10-12

#### LQ-133 | 🟡 Minor | Write feature hardcodes "smart" mode
- **Source:** Stage 10, S10-13

#### LQ-134 | 🔵 Enhancement | Palette signs loaded but not rendered in Write tab
- **Source:** Stage 10, S10-14

#### LQ-135 | 🔵 Enhancement | Lesson navigation hardcodes 5 lessons
- **Source:** Stage 10, S10-17

#### LQ-136 | 🔵 Enhancement | Translate tab fully implemented but unreachable (dead code)
- **Source:** Stage 10, S10-05

#### LQ-137 | 🟡 Minor | Error state overloaded for TTS fallback in DictionaryViewModel
- **Source:** Stage 10, S10-19

---

### K. Testing Gaps (from Stage 12)

#### LQ-140 | 🔴 Critical | Zero tests for AuthRepositoryImpl — dual auth handshake untested
- **Source:** Stage 12, S12-01

#### LQ-141 | 🔴 Critical | Zero tests for ChatRepositoryImpl — SSE parsing untested
- **Source:** Stage 12, S12-02

#### LQ-142 | 🔴 Critical | Zero tests for ScanRepositoryImpl — flagship feature untested
- **Source:** Stage 12, S12-03

#### LQ-143 | 🔴 Critical | Zero tests for StoriesRepositoryImpl — 4 interaction types untested
- **Source:** Stage 12, S12-04

#### LQ-144 | 🟠 Major | Zero tests for EgyptianPronunciation — pure function ideal for testing
- **Source:** Stage 12, S12-08

#### LQ-145 | 🟠 Major | Zero tests for UserRepositoryImpl
- **Source:** Stage 12, S12-05

#### LQ-146 | 🟠 Major | Zero tests for TranslateRepositoryImpl
- **Source:** Stage 12, S12-06

#### LQ-147 | 🟠 Major | Zero tests for ExploreRepositoryImpl
- **Source:** Stage 12, S12-07

#### LQ-148 | 🟡 Minor | Zero Compose UI tests across 10 feature modules
- **Source:** Stage 12, S12-09

#### LQ-149 | 🟡 Minor | CI runs only unit tests — no instrumentation tests
- **Source:** Stage 12, S12-12

#### LQ-150 | 🟡 Minor | No test doubles (Fakes) — only MockK mocks with relaxed defaults
- **Source:** Stage 12, S12-10

#### LQ-151 | 🟡 Minor | No Hilt test components
- **Source:** Stage 12, S12-11

#### LQ-152 | 🟡 Minor | DictionaryRepositoryImplTest searchOffline mock uses wrong arity
- **Source:** Stage 12, S12-18

---

## Statistics

| Severity | Count |
|----------|-------|
| 🔴 Critical | 14 |
| 🟠 Major | 28 |
| 🟡 Minor | 47 |
| 🔵 Enhancement | 15 |
| **Total** | **104** |

### By Category
| Category | Critical | Major | Minor | Enhancement | Total |
|----------|----------|-------|-------|-------------|-------|
| A. Egyptological Accuracy | 0 | 0 | 4 | 2 | 6 |
| B. API Contract | 2 | 3 | 3 | 0 | 8 |
| C. Data Layer | 0 | 1 | 0 | 1 | 2 |
| D. Database & Offline | 1 | 3 | 6 | 3 | 13 |
| E. ViewModel Logic | 4 | 6 | 4 | 0 | 14 |
| F. TTS & Audio | 0 | 1 | 4 | 1 | 6 |
| G. Scan & Landmarks | 0 | 0 | 4 | 0 | 4 |
| H. Chat & Stories | 0 | 3 | 5 | 4 | 12 |
| I. Auth & Security | 3 | 7 | 6 | 2 | 18 |
| J. Dictionary & Write | 0 | 0 | 5 | 3 | 8 |
| K. Testing | 4 | 4 | 5 | 0 | 13 |
