# STAGE 12 REPORT: Testing & References Audit

> **Date**: 2026-04-15
> **Scope**: All `src/test/`, `src/androidTest/`, ProGuard rules, hardcoded values, build variants, CI/CD
> **Files read**: 4 unit test files, 2 instrumentation test files, proguard-rules.pro, app/build.gradle.kts, android.yml CI, google-services.json

---

## A. Test File Inventory

### Unit Tests (JVM — `src/test/`)

| # | File | Module | Tests | Description |
|---|------|--------|-------|-------------|
| 1 | `AuthInterceptorTest.kt` | `core:network` | 5 | MockWebServer-based. Tests token attachment, auth endpoint skip, 401→refresh→retry, failed refresh clears tokens, refresh cookie sending |
| 2 | `DictionaryRepositoryImplTest.kt` | `core:data` | 5 | MockK-based. Tests getSigns mapping, Room caching, offline fallback, getSign cache fallback, getCategories mapping, searchOffline FTS |
| 3 | `ExploreViewModelTest.kt` | `feature:explore` | 7 | MockK + Turbine. Tests init load, selectCategory, selectCity, error state, loadMore (pagination), last-page guard, toggleFavorite, refresh, dismissError |
| 4 | `DictionaryViewModelTest.kt` | `feature:dictionary` | 9 | MockK + Turbine. Tests init, selectCategory, selectType, error state, loadMore, last-page guard, dismissError, selectSign, state flow emissions |

### Instrumentation Tests (Android — `src/androidTest/`)

| # | File | Module | Tests | Description |
|---|------|--------|-------|-------------|
| 5 | `SignDaoTest.kt` | `core:database` | 8 | In-memory Room. Tests insert/retrieve, not-found, getAll with pagination, getByCategory, getByType, getByFilter, upsert, count, deleteAll |
| 6 | `LandmarkDaoTest.kt` | `core:database` | 7 | In-memory Room. Tests insert/retrieve by slug, ordering by popularity, getFiltered by category/city/null, search, getCities, deleteAll |

**Total: 6 test files, ~41 test methods**

---

## B. Issues

---

#### S12-01 | CRITICAL — Zero tests for auth flow (AuthRepositoryImpl)
- **Description**: `AuthRepositoryImpl` orchestrates Firebase Auth → backend API → token storage, a triple-handshake sequence. It has:
  - Google sign-in, email sign-in, register, forgot password, sign-out
  - Complex error handling (Firebase failure vs backend failure)
  - Token persistence in EncryptedSharedPreferences
- No unit or integration tests exist for any of this. The only auth-adjacent test is `AuthInterceptorTest` which tests the OkHttp interceptor, not the repository logic.
- **Impact**: Auth bugs (e.g. token not saved, Firebase succeeds but backend fails, race conditions) are completely undetected.

---

#### S12-02 | CRITICAL — Zero tests for Chat (ChatRepositoryImpl, ChatViewModel)
- **Description**: Chat has the most complex networking in the app:
  - OkHttp SSE streaming with `callbackFlow`
  - JSON parsing of SSE chunks with fallback to raw text
  - `clearSession` Retrofit call
  - `speak` (TTS) and `transcribe` (STT) via AudioApiService
- No tests exist. `feature:chat` declares test dependencies (junit, mockk, coroutines-test, turbine) but has 0 test files.
- **Impact**: SSE parsing errors, stream cancellation bugs, and audio API failures are completely untested.

---

#### S12-03 | CRITICAL — Zero tests for Scan (ScanRepositoryImpl, ScanViewModel)
- **Description**: Scan involves:
  - Multipart image upload
  - Complex DTO→domain mapping (17+ fields)
  - Room persistence of scan results as serialized JSON
  - Save/delete lifecycle with file system operations (thumbnail deletion)
- `feature:scan` declares test dependencies but has 0 test files.
- **Impact**: The hieroglyph detection display pipeline (the app's flagship feature) has zero automated test coverage.

---

#### S12-04 | CRITICAL — Zero tests for Stories (StoriesRepositoryImpl, StoryViewModel)
- **Description**: Stories involves:
  - Complex nested DTO mapping (StoryFull → Chapter → Paragraph → GlyphAnnotation + Interaction)
  - 4 sealed interaction types (`ChooseGlyph`, `WriteWord`, `GlyphDiscovery`, `StoryDecision`) with complex field mapping
  - Firebase Firestore real-time listeners for progress
  - Dual-write progress (Firestore + REST API)
  - Chapter image generation
  - Chapter TTS narration
- `feature:stories` declares test dependencies (junit, mockk, coroutines-test, turbine) but has 0 test files.
- **Impact**: The interaction type parsing, progress sync, and dual-write logic are all untested.

---

#### S12-05 | MAJOR — Zero tests for UserRepositoryImpl (Dashboard data)
- **Description**: UserRepositoryImpl handles profile, stats, scan history, favorites, story progress, and usage limits. All fetched without caching and with inconsistent error handling (see S11-06, S11-07). No tests.
- **Impact**: Dashboard could display stale/wrong data with no test safety net.

---

#### S12-06 | MAJOR — Zero tests for TranslateRepositoryImpl
- **Description**: Translation has a specific error-in-body pattern:
  ```kotlin
  if (body.error.isNotBlank()) {
      throw ApiException(body.error)
  }
  ```
  This 200-with-error-body pattern is unique and untested.

---

#### S12-07 | MAJOR — Zero tests for ExploreRepositoryImpl
- **Description**: `ExploreViewModelTest` exists and mocks the repository interface, but the repository implementation itself is untested. It has:
  - Offline fallback with Room
  - In-memory favorites state management
  - Multipart image upload (identifyLandmark)
  - Complex landmark detail → Room JSON serialization
- Only the ViewModel is tested (testing against mock), not the actual repository logic.

---

#### S12-08 | MAJOR — Zero tests for EgyptianPronunciation utility
- **Description**: `EgyptianPronunciation.kt` in `core:common` contains `transliterationToSpeech()` which maps Egyptian transliteration characters (x→kh, S→sh, D→dj, etc.). This is a pure function — ideal for unit testing — but has zero tests.
- **Impact**: Pronunciation mapping bugs (e.g. wrong character substitution) are undetectable. This was manually added in Spec 002 Round 2.

---

#### S12-09 | MEDIUM — No Compose UI tests at all
- **Description**: Zero files found matching `**/src/androidTest/**/*Test.kt` or `**/src/test/**/*UiTest.kt` for any Compose tests. The only androidTest files are Room DAO tests.
- **Impact**: All Compose screens (12+ screens across 10 feature modules) have zero automated UI testing. No screenshot tests, no interaction tests.
- **Listed modules with no UI tests**: auth, chat, dashboard, dictionary, explore, feedback, landing, scan, settings, stories

---

#### S12-10 | MEDIUM — No test doubles (Fakes) — only MockK mocks
- **Description**: All tests use MockK `mockk()` mocks. There are no hand-written Fake implementations of repository interfaces.
- **Impact**: Tests using `mockk(relaxed = true)` may silently pass when they shouldn't — relaxed mocks return default values for unconfigured calls, hiding missing setup. Fakes would provide more explicit, deterministic behavior.
- **Note**: This is a quality concern, not a showstopper. MockK usage is consistent and well-structured.

---

#### S12-11 | MEDIUM — No Hilt test components or test modules
- **Description**: No `@HiltAndroidTest` annotations found. Room DAO tests use manual `Room.inMemoryDatabaseBuilder()` — correct approach for DAO testing. But no integration tests use Hilt test injection.
- **Impact**: DI wiring bugs (wrong bindings, missing scopes) can only be caught at app startup runtime.

---

#### S12-12 | MEDIUM — CI runs only `testDebugUnitTest` — no instrumentation tests
- **File**: [android.yml](.github/workflows/android.yml#L38)
- **Evidence**: `./gradlew testDebugUnitTest` — only runs JVM unit tests. The 2 Room DAO androidTest files are never executed in CI.
- **Impact**: The 15 Room DAO tests (SignDaoTest + LandmarkDaoTest) only run manually on a developer's device. Regressions possible.

---

#### S12-13 | LOW — ProGuard rules are comprehensive but lack EgyptianPronunciation protection
- **File**: [proguard-rules.pro](app/proguard-rules.pro)
- **Analysis**: Rules cover:
  - ✅ kotlinx.serialization — all `@Serializable` classes kept
  - ✅ Retrofit method annotations kept
  - ✅ OkHttp/Okio warnings suppressed
  - ✅ Firebase Crashlytics source lines preserved
  - ✅ Room entities and database kept
  - ✅ ONNX Runtime kept
  - ✅ Hilt/Dagger kept
  - ✅ Google Sign-In / Credential Manager kept
  - ✅ Enums fully preserved
- **Missing**: No explicit rule for `EgyptianPronunciation` — but since it's a Kotlin object (not serialized), R8 should keep it via normal code tracing. No risk here.
- **Verdict**: ProGuard rules are solid. No pronunciation data stripping risk.

---

#### S12-14 | INFO — google-services.json contains Firebase API key (expected, not a vulnerability)
- **File**: `app/google-services.json` line 31
- **Evidence**: `"current_key": "AIzaSyAKSH4Mx7yLXoZ8_lCaIPalnmAhPpCHPsE"`
- **Verdict**: This is a restricted Firebase Web API key — scoped to the Firebase project and restricted by app package/SHA. It's NOT a secret. Google's documentation explicitly states these are safe to include in client apps. ✅

---

#### S12-15 | INFO — Backend URL correctly externalized via BuildConfig
- **File**: [app/build.gradle.kts](app/build.gradle.kts#L38-L44)
- **Evidence**:
  - Debug: Uses `debug.base.url` from `local.properties` with fallback to production
  - Release: Hardcoded `"https://nadercr7-wadjet-v2.hf.space"`
- ✅ No hardcoded URLs in source code. Base URL injected via `@Named("baseUrl")` through Hilt.

---

#### S12-16 | INFO — CI/CD pipeline is well-structured
- **File**: [android.yml](.github/workflows/android.yml)
- **Analysis**:
  - ✅ Lint check before build
  - ✅ Unit tests run on every push/PR
  - ✅ gradle caching (hash-based key)
  - ✅ Release job conditioned on `refs/tags/v*`
  - ✅ Keystore decoded from Base64 secret (not committed)
  - ✅ Test results uploaded as artifact
  - ✅ APK renamed with version tag
  - ✅ Prerelease detection for beta/rc tags
- **Gap**: No instrumentation test step (see S12-12). No code coverage reporting.

---

#### S12-17 | LOW — Build variants: signing config empty-string passwords in fallback
- **File**: [app/build.gradle.kts](app/build.gradle.kts#L29-L32)
- **Evidence**:
  ```kotlin
  storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
  keyAlias = System.getenv("KEY_ALIAS") ?: "wadjet-app"
  keyPassword = System.getenv("KEY_PASSWORD") ?: ""
  ```
  If env vars are not set (local dev), signing config has empty passwords. This won't cause a security issue (release build will simply fail to sign) but could produce confusing error messages.

---

#### S12-18 | MEDIUM — Test infra: DictionaryRepositoryImplTest.searchOffline has wrong mock signature
- **File**: [DictionaryRepositoryImplTest.kt](core/data/src/test/java/com/wadjet/core/data/repository/DictionaryRepositoryImplTest.kt#L108)
- **Evidence**:
  ```kotlin
  coEvery { signDao.search(any(), any()) } returns listOf(cached)
  ```
  But `SignDao.search()` in the actual implementation is called with a single argument (the FTS query string). The mock uses `any(), any()` — this works because MockK `any()` is lenient, but it means the test doesn't validate the correct FTS query format (e.g., `"seated*"` vs `"seated"`). If the DAO method signature changes, the test won't catch it.

---

## C. Test Coverage Heat Map

| Module/Feature | Unit Tests | Integration Tests | UI Tests | Critical? |
|---------------|-----------|------------------|----------|-----------|
| `core:network` AuthInterceptor | ✅ 5 tests | — | — | Yes |
| `core:network` RateLimitInterceptor | ❌ 0 | — | — | Yes |
| `core:data` DictionaryRepositoryImpl | ✅ 5 tests | — | — | Yes |
| `core:data` AuthRepositoryImpl | ❌ 0 | — | — | **CRITICAL** |
| `core:data` ChatRepositoryImpl | ❌ 0 | — | — | **CRITICAL** |
| `core:data` ScanRepositoryImpl | ❌ 0 | — | — | **CRITICAL** |
| `core:data` StoriesRepositoryImpl | ❌ 0 | — | — | **CRITICAL** |
| `core:data` ExploreRepositoryImpl | ❌ 0 | — | — | MAJOR |
| `core:data` UserRepositoryImpl | ❌ 0 | — | — | MAJOR |
| `core:data` TranslateRepositoryImpl | ❌ 0 | — | — | MAJOR |
| `core:data` FeedbackRepositoryImpl | ❌ 0 | — | — | Medium |
| `core:database` SignDao | — | ✅ 8 tests | — | Yes |
| `core:database` LandmarkDao | — | ✅ 7 tests | — | Yes |
| `core:database` ScanResultDao | — | ❌ 0 | — | Yes |
| `core:common` EgyptianPronunciation | ❌ 0 | — | — | MAJOR |
| `core:common` ToastController | ❌ 0 | — | — | Low |
| `core:ml` (on-device ML) | ❌ 0 | — | — | Medium |
| `feature:dictionary` ViewModel | ✅ 9 tests | — | ❌ 0 | — |
| `feature:explore` ViewModel | ✅ 7 tests | — | ❌ 0 | — |
| `feature:chat` ViewModel | ❌ 0 | — | ❌ 0 | **CRITICAL** |
| `feature:scan` ViewModel | ❌ 0 | — | ❌ 0 | **CRITICAL** |
| `feature:stories` ViewModel | ❌ 0 | — | ❌ 0 | **CRITICAL** |
| `feature:auth` ViewModel | ❌ 0 | — | ❌ 0 | MAJOR |
| `feature:dashboard` ViewModel | ❌ 0 | — | ❌ 0 | Medium |
| `feature:settings` ViewModel | ❌ 0 | — | ❌ 0 | Low |
| `feature:feedback` ViewModel | ❌ 0 | — | ❌ 0 | Low |
| `feature:landing` ViewModel | ❌ 0 | — | ❌ 0 | Low |

---

## D. Summary

| Severity | Count |
|----------|-------|
| CRITICAL | 4 (S12-01, S12-02, S12-03, S12-04) |
| MAJOR | 4 (S12-05, S12-06, S12-07, S12-08) |
| MEDIUM | 4 (S12-09, S12-10, S12-11, S12-12, S12-18) |
| LOW | 2 (S12-13, S12-17) |
| INFO | 3 (S12-14, S12-15, S12-16) |

**Test coverage estimate: ~15% of repository/ViewModel logic has any tests. 0% UI test coverage. 6 test files total across 18 modules.**

### Priority Recommendations for Test Addition (ranked by risk × effort)

1. **EgyptianPronunciation unit tests** — pure function, 1 hour, high value
2. **ScanRepositoryImpl unit tests** — flagship feature, MockK-based, 2 hours
3. **ChatRepositoryImpl unit tests** — SSE parsing is fragile, 2 hours
4. **StoriesRepositoryImpl unit tests** — 4 interaction type mappings, 2 hours
5. **AuthRepositoryImpl unit tests** — Firebase+backend dual handshake, 3 hours
6. **RateLimitInterceptor unit tests** — MockWebServer-based (pattern exists in AuthInterceptorTest), 1 hour
