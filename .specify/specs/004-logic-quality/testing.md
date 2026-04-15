# Testing Plan: Logic & Quality

**Source**: Stage 12 investigation (`_investigation/stage-12-testing-references.md`)
**Date**: 2026-04-15

---

## Current Coverage

| # | File | Module | Tests | Type |
|---|------|--------|-------|------|
| 1 | `AuthInterceptorTest.kt` | `core:network` | 5 | Unit (MockWebServer) |
| 2 | `DictionaryRepositoryImplTest.kt` | `core:data` | 5 | Unit (MockK) |
| 3 | `ExploreViewModelTest.kt` | `feature:explore` | 7 | Unit (MockK + Turbine) |
| 4 | `DictionaryViewModelTest.kt` | `feature:dictionary` | 9 | Unit (MockK + Turbine) |
| 5 | `SignDaoTest.kt` | `core:database` | 8 | Instrumentation (Room in-memory) |
| 6 | `LandmarkDaoTest.kt` | `core:database` | 7 | Instrumentation (Room in-memory) |

**Total: 6 test files, ~41 test methods, ~15% logic coverage, 0% UI coverage**

### Existing Test Quality Assessment
- `AuthInterceptorTest` — Good: covers token attach, skip auth endpoints, 401→refresh→retry, failed refresh clears tokens. **Gap:** no test for concurrent 401 race condition.
- `DictionaryRepositoryImplTest` — Good: covers mapping, caching, offline fallback. **Gap:** `searchOffline` mock uses wrong arity (`any(), any()` vs single arg).
- `ExploreViewModelTest` — Good: covers init, filter, pagination, error, favorites. **Gap:** no offline fallback test.
- `DictionaryViewModelTest` — Good: covers init, filter, pagination. **Gap:** no TTS/speak test, no search debounce test.
- `SignDaoTest` / `LandmarkDaoTest` — Good: comprehensive CRUD + filter tests. **Gap:** no FTS search ranking test.

---

## Critical Test Gaps (from S12-01 through S12-08)

| Gap | Module | Risk | Effort |
|-----|--------|------|--------|
| AuthRepositoryImpl | `core:data` | 🔴 CRITICAL — dual auth handshake untested | 3h |
| ChatRepositoryImpl | `core:data` | 🔴 CRITICAL — SSE parsing untested | 2h |
| ScanRepositoryImpl | `core:data` | 🔴 CRITICAL — flagship feature untested | 2h |
| StoriesRepositoryImpl | `core:data` | 🔴 CRITICAL — 4 interaction types untested | 2h |
| EgyptianPronunciation | `core:common` | 🟠 MAJOR — pure function, ideal for tests | 1h |
| UserRepositoryImpl | `core:data` | 🟠 MAJOR — dashboard data untested | 1h |
| TranslateRepositoryImpl | `core:data` | 🟠 MAJOR — 200-with-error-body pattern | 1h |
| ExploreRepositoryImpl | `core:data` | 🟠 MAJOR — offline + identify untested | 2h |

---

## Test Plan by Priority

### Priority 1: Pronunciation Tests (MUST HAVE — Phase 1 deliverable)

```
core/common/src/test/java/com/wadjet/core/common/EgyptianPronunciationTest.kt
```

- [ ] **UT-001** `toSpeech()` — every WORD_MAP entry produces correct output (parametrized test, ~100 cases)
- [ ] **UT-002** `toSpeech()` — blank/empty input returns empty string
- [ ] **UT-003** `toSpeech()` — "already pronounceable" detection (text with e/o/u returns as-is)
- [ ] **UT-004** `toSpeech()` — multi-word input splits and processes each word
- [ ] **UT-005** Phoneme mapping — every MdC consonant maps to correct TTS text (26 cases)
- [ ] **UT-006** Vowel epenthesis — consonant clusters get 'e' insertion (`nfr` → `nefer`)
- [ ] **UT-007** Vowel epenthesis — no insertion between vowel sounds (`wADt` → correct)
- [ ] **UT-008** Tokenizer — MdC_STRIP characters removed (`.`, `:`, `=`, `*`, etc.)
- [ ] **UT-009** Tokenizer — hyphens in unknown words (after P-04 fix)
- [ ] **UT-010** Tokenizer — digits in input (after P-05 fix)
- [ ] **UT-011** Fallback path — unknown words produce reasonable pronunciation

```
feature/scan/src/test/java/com/wadjet/feature/scan/util/GardinerUnicodeTest.kt
```

- [ ] **UT-012** `gardinerToUnicode()` — COMMON_GLYPHS entries return correct Unicode
- [ ] **UT-013** `gardinerToUnicode()` — codes not in COMMON_GLYPHS fall back to UNICODE_MAP
- [ ] **UT-014** `gardinerToUnicode()` — invalid codes return null
- [ ] **UT-015** Regex parsing — parses "A1", "G43A", "Aa2" correctly

### Priority 2: API Response Parsing Tests (Phase 2 deliverable)

```
core/data/src/test/java/com/wadjet/core/data/repository/ScanRepositoryImplTest.kt
```

- [ ] **UT-020** `scan()` — multipart upload constructs correct request
- [ ] **UT-021** `scan()` — response with all fields parses correctly to domain
- [ ] **UT-022** `scan()` — `glyph_count`/`num_detections` field mapping correct
- [ ] **UT-023** `scan()` — saves result to Room with serialized JSON
- [ ] **UT-024** `getScanHistory()` — returns Flow from Room, ordered by timestamp

```
core/data/src/test/java/com/wadjet/core/data/repository/ChatRepositoryImplTest.kt
```

- [ ] **UT-030** SSE stream — parses `data: {"token": "hello"}` chunks correctly
- [ ] **UT-031** SSE stream — handles `data: [DONE]` termination
- [ ] **UT-032** SSE stream — handles malformed JSON gracefully (falls back to raw text)
- [ ] **UT-033** SSE stream — `onFailure` emits error and closes flow
- [ ] **UT-034** `clearSession()` — calls correct API endpoint

```
core/data/src/test/java/com/wadjet/core/data/repository/StoriesRepositoryImplTest.kt
```

- [ ] **UT-040** `getStories()` — maps full response to domain list
- [ ] **UT-041** `getStory()` — maps chapters, paragraphs, annotations, interactions
- [ ] **UT-042** `interact()` — `InteractResponse` maps ALL fields including `correctAnswer`, `targetGlyph`, `gardinerCode`, `hint`, `choiceId` (after LQ fix)
- [ ] **UT-043** `saveProgress()` — writes to both Firestore and REST API
- [ ] **UT-044** `saveProgress()` — handles Firestore failure gracefully
- [ ] **UT-045** `saveProgress()` — handles no-auth (null uid) gracefully

### Priority 3: Repository & ViewModel Tests (Phase 3+ deliverable)

```
core/data/src/test/java/com/wadjet/core/data/repository/AuthRepositoryImplTest.kt
```

- [ ] **UT-050** `signInWithEmail()` — Firebase success + backend success → token saved
- [ ] **UT-051** `signInWithEmail()` — Firebase success + backend failure → error surfaced (after split-brain fix)
- [ ] **UT-052** `signInWithGoogle()` — idToken exchange flow
- [ ] **UT-053** `register()` — success flow with both Firebase and backend
- [ ] **UT-054** `signOut()` — clears tokens, signs out Firebase, calls backend logout
- [ ] **UT-055** `currentUser` flow — emits null when token expires

```
core/network/src/test/java/com/wadjet/core/network/RateLimitInterceptorTest.kt
```

- [ ] **UT-060** 429 response — retries once after delay
- [ ] **UT-061** 503 response — retries up to 3 times with backoff
- [ ] **UT-062** Login path (contains `/auth/login`) — returns 429 without retry
- [ ] **UT-063** Non-429/503 response — passes through unchanged

```
core/data/src/test/java/com/wadjet/core/data/repository/UserRepositoryImplTest.kt
```

- [ ] **UT-070** `getProfile()` — maps response to domain User
- [ ] **UT-071** `getFavorites()` — returns list of FavoriteItem
- [ ] **UT-072** `removeFavorite()` — handles server failure (after consistency fix)
- [ ] **UT-073** `getLimits()` — maps usage limits response

```
core/data/src/test/java/com/wadjet/core/data/repository/TranslateRepositoryImplTest.kt
```

- [ ] **UT-080** `translate()` — success response maps to TranslationResult
- [ ] **UT-081** `translate()` — 200-with-error-body throws ApiException

```
core/data/src/test/java/com/wadjet/core/data/repository/FeedbackRepositoryImplTest.kt
```

- [ ] **UT-090** `submit()` — success returns feedback ID
- [ ] **UT-091** `submit()` — 4xx error properly detected (after isSuccessful check fix)

### Priority 4: Integration & UI Tests (Phase 7 deliverable)

```
core/database/src/androidTest/java/.../ScanResultDaoTest.kt
```

- [ ] **IT-001** ScanResultDao — insert, retrieve, delete, ordering

```
app/src/androidTest/java/.../NavigationTest.kt (optional)
```

- [ ] **IT-010** App launch → Welcome screen when not logged in
- [ ] **IT-011** App launch → Landing screen when logged in

### CI Pipeline Updates

- [ ] **CI-001** Add `connectedDebugAndroidTest` step to `android.yml` (for Room DAO tests)
- [ ] **CI-002** Add code coverage reporting (JaCoCo or Kover)
- [ ] **CI-003** Set minimum coverage threshold for `core:common` (pronunciation) = 90%

---

## Test Infrastructure Needed

### Fixtures
- `ScanResponseFixture.json` — full scan response with all fields
- `StoryFullFixture.json` — complete story with chapters, interactions
- `InteractResponseFixture.json` — all 4 interaction type responses

### Fake Implementations
- `FakeTokenManager` — in-memory token storage for auth tests
- `FakeFirebaseAuthManager` — mock Firebase auth for repository tests

### Libraries Already Available
- JUnit 4 (all modules)
- MockK (all modules)
- kotlinx-coroutines-test (all feature modules)
- Turbine (feature:explore, feature:dictionary — add to others)
- MockWebServer (core:network)
- Room in-memory DB (core:database)

### Libraries to Add
- Turbine → `feature:chat`, `feature:scan`, `feature:stories`, `feature:auth` (for Flow testing)

---

## Coverage Targets

| Module | Current | Target | Priority |
|--------|---------|--------|----------|
| `core:common` (pronunciation) | 0% | 95% | P0 |
| `core:data` (repositories) | ~10% | 60% | P1 |
| `core:network` (interceptors) | ~30% | 70% | P1 |
| `core:database` (DAOs) | ~50% | 80% | P2 |
| `feature:*` (ViewModels) | ~15% | 40% | P2 |
| UI tests (Compose) | 0% | 10% | P3 (stretch) |
