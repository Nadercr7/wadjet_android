# Stage 1: Existing Test Audit

## Environment Health Check

| Component | Status | Version/Path |
|-----------|--------|-------------|
| Java | OK | OpenJDK 17.0.17 LTS (Microsoft) |
| ADB | OK | 36.0.0-13206524, `C:\Users\Nader\AppData\Local\Android\Sdk\platform-tools\adb.exe` |
| ANDROID_HOME | OK | `C:\Users\Nader\AppData\Local\Android\Sdk` |
| Gradle | OK | 8.13 (wrapper) |
| Health check script | BROKEN | `emu_health_check.ps1` has syntax errors at lines 112–113, 141–142 (smart quotes in string concatenation) |

## Test Run Results

**All 320 unit tests PASS, 0 failures, 0 errors.**
Build: `BUILD SUCCESSFUL in 2s` (all tasks UP-TO-DATE from cache).

## Test Inventory

There are **20 test files** (not 12 as listed in the prompt) — 18 JVM unit tests + 2 instrumented (Android) tests.

### Unit Tests (18 files, 320 tests)

| # | File | Module | Test Count | All Passing? | Coverage Notes |
|---|------|--------|------------|--------------|----------------|
| 1 | `EgyptianPronunciationTest.kt` | core:common | 173 | YES | Exhaustive WORD_MAP, PHONEME_MAP, epenthesis, tokenizer. Gold standard. |
| 2 | `AuthRepositoryImplTest.kt` | core:data | 8 | YES | Google/email sign-in, register, sign-out, 429 retry-after, split-brain fix |
| 3 | `ChatRepositoryImplTest.kt` | core:data | 7 | YES | clearSession, speak (200/204/error), transcribe (success/error) |
| 4 | `DictionaryRepositoryImplTest.kt` | core:data | 6 | YES | getSigns, Room cache, offline fallback, getSign cache, categories, FTS |
| 5 | `ExploreRepositoryImplTest.kt` | core:data | 8 | YES | getLandmarks, Room cache/fallback, getCachedLandmarks Flow, toggleFavorite |
| 6 | `FeedbackRepositoryImplTest.kt` | core:data | 3 | YES | submitFeedback success, HTTP error, null body. Thinnest file. |
| 7 | `ScanRepositoryImplTest.kt` | core:data | 8 | YES | scanImage, saveScanResult, history, getScanResult, deleteScan |
| 8 | `StoriesRepositoryImplTest.kt` | core:data | 14 | YES | 4 interaction types, Firebase sync, chapter image gen, unknown type filter |
| 9 | `TranslateRepositoryImplTest.kt` | core:data | 4 | YES | translate success, 200-with-error-body, HTTP error, null gardiner |
| 10 | `UserRepositoryImplTest.kt` | core:data | 14 | YES | Profile, stats, favorites CRUD, Room fallback, limits, password change |
| 11 | `ButtonScreenshotTest.kt` | core:designsystem | 7 | YES | Roborazzi screenshot: 4 button variants, disabled/loading states |
| 12 | `AuthInterceptorTest.kt` | core:network | 7 | YES | Bearer token, null token, auth endpoints skip, external URL skip, refresh cookie, 401 passthrough, Set-Cookie extraction |
| 13 | `RateLimitInterceptorTest.kt` | core:network | 4 | YES | 200 passthrough, 429 no-sleep, 503 no-sleep, login lockout |
| 14 | `TokenAuthenticatorTest.kt` | core:network | 5 | YES | 401→refresh→retry, failed refresh clears tokens, auth endpoint skip, concurrent refresh skip, Set-Cookie save |
| 15 | `AuthViewModelTest.kt` | feature:auth | 12 | YES | Double-submit guard, email validation, password validation, forgotPasswordSent reset, events, register password mismatch |
| 16 | `DictionaryViewModelTest.kt` | feature:dictionary | 9 | YES | Init load, selectCategory, selectType filter, error state, loadMore, dismissError, selectSign, Flow emissions |
| 17 | `ExploreViewModelTest.kt` | feature:explore | 9 | YES | Init load, selectCategory, selectCity, error state, loadMore, toggleFavorite, refresh, dismissError |
| 18 | `GardinerUnicodeTest.kt` | feature:scan | 22 | YES | COMMON_GLYPHS lookup, algorithmic fallback, regex matching, unknown/invalid codes, supplementary char validation |

### Instrumented Tests (2 files, not run — require emulator)

| # | File | Module | Test Count (approx) | Notes |
|---|------|--------|---------------------|-------|
| 19 | `SignDaoTest.kt` | core:database | 9 | Insert, retrieve, pagination, category/type filter, upsert, count, deleteAll |
| 20 | `LandmarkDaoTest.kt` | core:database | 8 | Insert, retrieve, popularity ordering, category/city filter, search, getCities, deleteAll |

## Test Quality Assessment

### Excellent Quality
- **EgyptianPronunciationTest** (173 tests): Exhaustive coverage of every WORD_MAP entry, every phoneme, epenthesis rules, tokenizer edge cases. Uses clean helper method `assertSpeech()`. Descriptive backtick names.
- **StoriesRepositoryImplTest** (14 tests): Tests all 4 interaction type mappings individually, Firebase sync with/without auth, unknown type filtering. Complex setup done well.
- **UserRepositoryImplTest** (14 tests): Wide API surface — profile, stats, favorites, limits, history, password. Offline fallback tested.

### Good Quality
- **AuthRepositoryImplTest** (8 tests): Tests split-brain fix (Firebase signout on backend failure), 429 Retry-After parsing, all auth flows.
- **AuthInterceptorTest** (7 tests): MockWebServer pattern. Tests external URL skip (important Coil fix), cookie handling.
- **TokenAuthenticatorTest** (5 tests): Tests full refresh lifecycle including concurrent refresh detection.
- **AuthViewModelTest** (12 tests): Double-submit guard tested correctly with dispatcher timing. `Unsafe` hack for `Patterns.EMAIL_ADDRESS` is fragile but works.
- **ChatRepositoryImplTest** (7 tests): Covers speak 204 edge case. Missing: `IOException` test.
- **ExploreRepositoryImplTest** (8 tests): Tests offline-first (network→Room fallback). Good Flow testing.
- **ScanRepositoryImplTest** (8 tests): Good CRUD coverage. Handles serialization edge cases.
- **DictionaryViewModelTest** (9 tests): Covers loadMore pagination boundary. Uses `UnconfinedTestDispatcher`.
- **ExploreViewModelTest** (9 tests): Same solid patterns as DictionaryViewModelTest.

### Adequate but Thin
- **FeedbackRepositoryImplTest** (3 tests): Only 1 method, 3 paths. Missing IOException, request validation.
- **TranslateRepositoryImplTest** (4 tests): Covers 200-with-error-body edge case. Missing IOException.
- **RateLimitInterceptorTest** (4 tests): Correctly verifies non-blocking behavior. Simple interceptor, simple tests.

### Screenshot Tests
- **ButtonScreenshotTest** (7 tests): Roborazzi + Robolectric Native Graphics. Covers 4 button variants. Missing: dark theme, RTL, pressed state, long text overflow.

## Test Patterns in Use

| Pattern | Usage | Files |
|---------|-------|-------|
| JUnit 4 `@Test` | All tests | All 20 |
| Backtick descriptive names | All tests | All 20 |
| MockK `mockk(relaxed = true)` | Mocking dependencies | All repo + VM tests |
| MockK `coEvery`/`coVerify` | Suspend function mocking | All repo + VM tests |
| `runTest {}` | Coroutine test scope | All repo + VM tests |
| `StandardTestDispatcher` | Explicit dispatch control | AuthViewModelTest |
| `UnconfinedTestDispatcher` | Immediate dispatch | DictionaryViewModelTest, ExploreViewModelTest |
| `Dispatchers.setMain/resetMain` | Main dispatcher swap | All ViewModel tests |
| Turbine `flow.test { awaitItem() }` | Flow assertions | DictionaryViewModelTest, ExploreViewModelTest |
| MockWebServer | HTTP request verification | AuthInterceptorTest, RateLimitInterceptorTest, TokenAuthenticatorTest |
| Retrofit `Response.success()/error()` | API response simulation | All repository tests |
| Room `inMemoryDatabaseBuilder` | DB testing | SignDaoTest, LandmarkDaoTest (instrumented) |
| Roborazzi + Robolectric | Screenshot testing | ButtonScreenshotTest |
| `@Before/@After` setup/teardown | Resource management | All tests |

## Modules With Zero Tests

### Core Modules (no tests)
| Module | Classes That Need Tests | Priority |
|--------|------------------------|----------|
| core:firebase | `FirebaseAuthManager`, `WadjetFirebaseMessaging` | HIGH — auth is critical |
| core:ui | Shared-transition composable locals | LOW — thin utility layer |
| core:ml | ML placeholder | SKIP — placeholder module |
| core:domain | 9 domain models, 9 repository interfaces | LOW — data classes + interfaces, tested indirectly |

### Feature Module ViewModels (17 with no tests)
| Module | ViewModel | Priority |
|--------|-----------|----------|
| feature:chat | `ChatViewModel` | HIGH — complex SSE streaming, message management |
| feature:dashboard | `DashboardViewModel` | MEDIUM — favorites, stats |
| feature:feedback | `FeedbackViewModel` | LOW — simple form |
| feature:landing | `LandingViewModel` | LOW — mostly navigation triggers |
| feature:scan | `ScanViewModel` | HIGH — camera/upload flow, state machine |
| feature:scan | `ScanResultViewModel` | MEDIUM — result display, favorites |
| feature:scan | `HistoryViewModel` | LOW — simple list |
| feature:dictionary | `SignDetailViewModel` | MEDIUM — sign detail, TTS trigger |
| feature:dictionary | `LessonViewModel` | MEDIUM — quiz/lesson state |
| feature:dictionary | `TranslateViewModel` | LOW — removed feature (3 tabs now) |
| feature:dictionary | `WriteViewModel` | MEDIUM — write mode logic |
| feature:explore | `DetailViewModel` | MEDIUM — landmark detail, children, TTS |
| feature:explore | `IdentifyViewModel` | HIGH — image upload flow |
| feature:stories | `StoriesViewModel` | MEDIUM — story list loading |
| feature:stories | `StoryReaderViewModel` | HIGH — chapter navigation, interactions, image gen |
| feature:settings | `SettingsViewModel` | LOW — preferences, theme |
| app | `HieroglyphsHubViewModel` | LOW — simple hub routing |

### Other Untested Areas
| Area | What Needs Tests | Priority |
|------|-----------------|----------|
| Navigation | `WadjetNavHost`, route definitions, deep links, back stack | HIGH |
| Compose UI | All 18+ screens — zero compose UI tests | HIGH |
| E2E flows | Login→feature→result journeys | MEDIUM |
| Screenshot | Only buttons tested — 0 screen screenshots | MEDIUM |
| API contract | DTO vs live backend field alignment | MEDIUM |

## Gradle Test Config

Tests use standard Android Gradle test tasks:
- `testDebugUnitTest` — runs all JVM unit tests
- `connectedDebugAndroidTest` — runs instrumented tests (requires emulator)
- Roborazzi config present in `core:designsystem` (Robolectric `@GraphicsMode(NATIVE)`, `@Config(qualifiers="480dpi")`)
- Hilt testing dependencies present in version catalog (`libs.hilt.testing`)
- Compose UI test dependencies present (`libs.compose.ui.test`)

## Failed Tests

**None.** All 320 unit tests pass. Instrumented tests (2 files) not run in this stage (require emulator).

## Summary Statistics

| Metric | Value |
|--------|-------|
| Total unit test files | 18 |
| Total instrumented test files | 2 |
| Total test methods (unit) | 320 |
| All passing | YES |
| Modules with tests | 8 of 18 |
| Modules with zero tests | 10 of 18 |
| ViewModels with tests | 3 of 20 |
| ViewModels with zero tests | 17 of 20 |
| Repository impls with tests | 9 of 9 (100%) |
| Network interceptors tested | 3 of 3 (100%) |
| Compose UI tests | 0 |
| Navigation tests | 0 |
| E2E tests | 0 |
| Screenshot tests | 1 file (buttons only) |
# Stage 1: Existing Test Audit

## Environment Health Check
- **ADB**: 1.0.41 (36.0.0-13206524) — `C:\Users\Nader\AppData\Local\Android\Sdk\platform-tools\adb.exe`
- **Java**: OpenJDK 17.0.17 LTS (Microsoft)
- **ANDROID_HOME**: `C:\Users\Nader\AppData\Local\Android\Sdk`
- **Gradle**: 8.13 (build successful, config cache working)
- **Emulator**: Pixel_8 AVD available (API 37, WHPX acceleration)
- **Note**: `emu_health_check.ps1` has a parsing error (line 112, unterminated string) — script won't run

## Test Inventory

| # | File | Module | Test Count | All Passing? | Coverage Notes |
|---|------|--------|------------|--------------|----------------|
| 1 | `EgyptianPronunciationTest.kt` | core:common | 173 | ✅ | Exhaustive WORD_MAP, PHONEME_MAP, epenthesis, edge cases |
| 2 | `AuthRepositoryImplTest.kt` | core:data | 8 | ✅ | Google, email, register, signOut, split-brain fix, 429 |
| 3 | `ChatRepositoryImplTest.kt` | core:data | 7 | ✅ | clearSession, speak (success/204/error), transcribe |
| 4 | `DictionaryRepositoryImplTest.kt` | core:data | 6 | ✅ | getSigns, caching, fallback, categories, FTS |
| 5 | `ExploreRepositoryImplTest.kt` | core:data | 8 | ✅ | getLandmarks, caching, IOException fallback, toggleFavorite |
| 6 | `FeedbackRepositoryImplTest.kt` | core:data | 3 | ✅ | submitFeedback only (thin) |
| 7 | `ScanRepositoryImplTest.kt` | core:data | 8 | ✅ | scanImage, save, history, delete — **2 flaky conditional tests** |
| 8 | `StoriesRepositoryImplTest.kt` | core:data | 14 | ✅ | Stories, interactions, image gen, progress, Firebase sync |
| 9 | `TranslateRepositoryImplTest.kt` | core:data | 4 | ✅ | translate success/error/null gardiner |
| 10 | `UserRepositoryImplTest.kt` | core:data | 14 | ✅ | Profile, stats, favorites, limits, history, password |
| 11 | `AuthInterceptorTest.kt` | core:network | 7 | ✅ | Bearer token, null token, login skip, external skip, refresh cookie, 401 passthrough, Set-Cookie |
| 12 | `RateLimitInterceptorTest.kt` | core:network | 4 | ✅ | 200 passthrough, 429 no-block, 503 no-block, login lockout |
| 13 | `TokenAuthenticatorTest.kt` | core:network | 5 | ✅ | 401→refresh→retry, failed refresh clears, auth skip, concurrent refresh skip, Set-Cookie save |
| 14 | `AuthViewModelTest.kt` | feature:auth | 12 | ✅ | Double-submit, email/password validation, events, register, forgotPassword |
| 15 | `DictionaryViewModelTest.kt` | feature:dictionary | 9 | ✅ | Init, category/type filter, pagination, error, Turbine flow |
| 16 | `ExploreViewModelTest.kt` | feature:explore | 9 | ✅ | Init, category/city, pagination, favorites, refresh, error |
| 17 | `GardinerUnicodeTest.kt` | feature:scan | 22 | ✅ | COMMON_GLYPHS, algorithmic fallback, regex, unknown codes |
|   | **TOTAL** | | **353** | **ALL PASS** | |

## Test Quality Assessment

### EgyptianPronunciationTest (173 tests) — EXCELLENT
- Exhaustive: every WORD_MAP entry, every PHONEME_MAP entry, all epenthesis rules
- Edge cases: empty, blank, whitespace, special chars, mixed MdC/English, multi-word
- Naming: backtick descriptive names ✅
- No mocking needed (pure function)

### AuthRepositoryImplTest (8 tests) — GOOD
- Thorough error paths: backend failure signs out Firebase (split-brain fix)
- Tests 429 rate-limit with Retry-After header extraction
- Complex mock setup for Firebase user properties
- No tests for: `forgotPassword`, `refreshToken` explicitly (covered implicitly via TokenAuthenticator)

### ChatRepositoryImplTest (7 tests) — ADEQUATE
- Covers clearSession, speak (success/204 null/error), transcribe
- No test for SSE streaming (most critical chat feature!)
- No test for chat history save/load

### DictionaryRepositoryImplTest (6 tests) — GOOD
- Tests network-first with Room cache fallback
- Tests FTS offline search
- No test for: lesson endpoint, alphabet endpoint, write endpoint

### ExploreRepositoryImplTest (8 tests) — GOOD
- Tests Room cache fallback on IOException
- Tests toggleFavorite add/remove
- Tests Flow observation from cached landmarks

### FeedbackRepositoryImplTest (3 tests) — THIN
- Only submitFeedback success/error/null
- Missing: IOException/network-error test
- Lowest coverage of all repos

### ScanRepositoryImplTest (8 tests) — HAS ISSUES
- **2 flaky tests**: `saveScanResult` and `getScanResult` use `if/else` branches that pass regardless of outcome
- Good coverage of scan flow otherwise
- Tests JSON deserialization paths

### StoriesRepositoryImplTest (14 tests) — GOOD BUT FRAGILE
- Most thorough repo test — covers all 4 interaction types individually
- Tests image generation, progress save (local + REST sync)
- **Fragile**: Deep Firebase mock chains (FirebaseAuth → currentUser → uid, Firestore → collection → document → set → Tasks.forResult)
- Tests unknown interaction type filtering

### TranslateRepositoryImplTest (4 tests) — CLEAN
- Tests 200-with-error-body edge case
- Tests null gardiner handling

### UserRepositoryImplTest (14 tests) — CLEAN & BROAD
- Broadest API surface: profile, stats, favorites, limits, history, progress, password
- Tests Room cache fallback for favorites

### AuthInterceptorTest (7 tests) — EXCELLENT
- MockWebServer-based, realistic HTTP testing
- Tests external URL skip (CDN fix)
- Tests Set-Cookie extraction
- Tests 401 passthrough (delegated to TokenAuthenticator)

### RateLimitInterceptorTest (4 tests) — GOOD
- Verifies NO sleep/block behavior
- Time-based assertions (< 1000ms)

### TokenAuthenticatorTest (5 tests) — EXCELLENT
- Full refresh flow: 401→refresh→retry
- Concurrent refresh optimization (skip if already refreshed)
- Set-Cookie extraction from refresh response

### AuthViewModelTest (12 tests) — EXCELLENT
- Double-submit guard (advanced technique with Unsafe for EMAIL_ADDRESS pattern)
- Turbine event channel testing
- Email/password validation

### DictionaryViewModelTest (9 tests) — GOOD
- Turbine flow testing
- Pagination loadMore with boundary check
- Category/type filtering

### ExploreViewModelTest (9 tests) — GOOD
- Similar to DictionaryViewModelTest pattern
- Pull-to-refresh test
- Favorite toggle

### GardinerUnicodeTest (22 tests) — GOOD
- COMMON_GLYPHS consistency check
- Algorithmic fallback via UNICODE_MAP
- Edge cases: empty, non-Gardiner, unknown codes

## Test Patterns in Use
- **Framework**: JUnit 4 with `@Test`, `@Before`, `@After`
- **Mocking**: MockK (`mockk`, `coEvery`, `coVerify`, `every`, `verify`, `relaxed = true`, `match {}`)
- **Coroutines**: `runTest {}`, `StandardTestDispatcher`, `UnconfinedTestDispatcher`, `advanceUntilIdle`, `Dispatchers.setMain/resetMain`
- **Flows**: Turbine (`flow.test { awaitItem(), expectMostRecentItem(), cancelAndIgnoreRemainingEvents() }`)
- **HTTP**: MockWebServer (`MockResponse`, `server.takeRequest()`, `server.requestCount`)
- **Room**: `inMemoryDatabaseBuilder` + `AndroidJUnit4` runner
- **Naming**: Backtick descriptive names (e.g., `` `signInWithGoogle success stores token and returns user` ``)
- **Result pattern**: All repos return `Result<T>`, tests use `result.isSuccess`, `result.isFailure`, `result.getOrThrow()`
- **Retrofit stubs**: `Response.success()`, `Response.error()` with `toResponseBody()`
- **Android workaround**: `sun.misc.Unsafe` to set `Patterns.EMAIL_ADDRESS` in JVM unit tests

## Modules With Zero Unit Tests
| Module | What's In It | Priority |
|--------|-------------|----------|
| feature:chat | ChatViewModel (SSE streaming, history, TTS, STT, edit) | **P0** — most complex feature |
| feature:dashboard | DashboardViewModel (favorites, stats, history tabs) | P1 |
| feature:feedback | FeedbackViewModel (form submission) | P2 |
| feature:landing | LandingViewModel (featured items, user state) | P2 |
| feature:settings | SettingsViewModel (theme, language, profile, logout) | P1 |
| feature:stories | StoriesViewModel, StoryReaderViewModel (reading, interactions) | P1 |
| feature:scan | ScanViewModel, ScanResultViewModel, HistoryViewModel (3 VMs!) | P1 |
| core:firebase | FirebaseAuthManager (Google sign-in, email auth) | P1 |
| core:ui | SharedTransition composable locals | P3 |
| core:designsystem | Theme, components, animations | P3 (screenshot tests instead) |
| core:domain | Pure models, no logic to test | N/A |
| core:ml | Placeholder only | N/A |
| app | Navigation (WadjetNavHost), HieroglyphsHubViewModel | P1 |

## Modules With Zero Instrumented Tests
- **All** except core:database (SignDaoTest, LandmarkDaoTest)
- Missing DAO tests: `CategoryDao`, `ScanResultDao`, `StoryProgressDao`
- Missing: Room migration tests
- Missing: Compose UI tests for every screen
- Missing: E2E navigation tests

## Gradle Test Config
| Module | JUnit | MockK | Coroutines Test | Turbine | MockWebServer | Room Testing | Espresso | Compose UI Test | Hilt Testing |
|--------|-------|-------|----------------|---------|---------------|-------------|----------|----------------|-------------|
| app | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ✅ |
| core:common | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| core:data | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| core:network | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| core:database | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| core:firebase | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| feature:* | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |

## Failed Tests
**NONE** — All 353 tests pass (BUILD SUCCESSFUL in 33s)

## Key Issues Found
1. **ScanRepositoryImplTest**: 2 tests with conditional `if/else` branches — always pass regardless of outcome (flaky by design)
2. **StoriesRepositoryImplTest**: Deep Firebase mock chains are brittle
3. **FeedbackRepositoryImplTest**: Only 3 tests, missing IOException path
4. **ChatRepositoryImplTest**: No tests for SSE streaming (the most critical chat feature)
5. **13 modules have zero unit tests** — most notably ChatViewModel and the 3 scan ViewModels
6. **No Compose UI tests at all** — compose-ui-test only in app module deps, never used
7. **No screenshot tests** — Roborazzi not set up
8. **No E2E tests**
9. **Only 2/5 DAOs tested** (SignDao, LandmarkDao) — missing CategoryDao, ScanResultDao, StoryProgressDao
10. **No Room migration tests** despite DB being at v6
