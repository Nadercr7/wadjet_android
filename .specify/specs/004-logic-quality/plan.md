# Implementation Plan: Logic & Quality

**Spec**: 004-logic-quality
**Date**: 2026-04-15
**Evidence base**: 12 investigation stage files in `_investigation/`

---

## Summary

- **Total findings**: ~130 issues across 12 investigation stages
- **Critical**: 14 | **Major**: 30+ | **Medium**: 40+ | **Low/Info**: 40+
- **Phases**: 7 implementation phases
- **Key risk**: Auth split-brain state (S9-03), pronunciation tokenizer gaps (P-04/P-05), dropped DTO fields (S8-01, S10-01)

## Technical Context

**Architecture**: app → feature/* → core/domain → core/data → core/network + core/database
**DI**: Hilt with `@Singleton` for network + database, `@ViewModelScoped` for features
**Serialization**: kotlinx.serialization (DTOs) + Room type converters (entities)
**Networking**: Retrofit 2 + OkHttp 4, AuthInterceptor + RateLimitInterceptor
**Auth**: Firebase Auth + custom JWT backend (dual auth system)
**Local storage**: Room 2.6+ (version 4, exportSchema=false, destructive migration fallback)

---

## Phases

### Phase 1: Egyptological Accuracy — Pronunciation & Gardiner (P0)

**Goal:** Fix ALL pronunciation-related issues, harden the tokenizer, add unit tests
**Priority:** P0 — this is the app's core value proposition
**Dependencies:** None
**Effort:** Medium

**Files affected:**
- `core/common/src/main/java/.../EgyptianPronunciation.kt`
- `feature/scan/src/main/java/.../util/GardinerUnicode.kt`
- NEW: `core/common/src/test/java/.../EgyptianPronunciationTest.kt`
- NEW: `feature/scan/src/test/java/.../util/GardinerUnicodeTest.kt`

**Tasks:** T001–T011 (see `tasks.md`)

**Verification:**
1. `./gradlew :core:common:testDebugUnitTest` — all pronunciation tests pass
2. Manual listen test: TTS for "anx", "nfr", "mAat", "xpr", "wADt", "DHwty", "Hwt-Hr"
3. Compare with web app for same words

**Key fixes:**
- P-04: Add `-` to MdC_STRIP or split on hyphen in fallback
- P-05: Add digits to MdC_STRIP
- P-08: Improve "already pronounceable" detection
- S12-08: Write comprehensive unit tests (100+ test cases)

---

### Phase 2: API Contract Alignment — DTOs & Domain Models (P0)

**Goal:** Fix ALL DTO→Domain mapping drops, ensure every API field reaches the UI
**Priority:** P0 — missing data degrades core features
**Dependencies:** None (parallel with Phase 1)
**Effort:** Large

**Files affected:**
- `core/domain/src/main/java/.../model/Dictionary.kt` — add 4 fields
- `core/domain/src/main/java/.../model/Story.kt` — add 5 InteractionResult fields
- `core/data/src/main/java/.../repository/DictionaryRepositoryImpl.kt` — fix mapper
- `core/data/src/main/java/.../repository/StoriesRepositoryImpl.kt` — fix mapper
- `core/database/src/main/java/.../entity/SignEntity.kt` — add cached fields
- `core/database/src/main/java/.../dao/SignDao.kt` — extend queries
- `core/network/src/main/java/.../api/ScanApiService.kt` — fix leading slash
- `core/data/src/main/java/.../repository/FeedbackRepositoryImpl.kt` — add isSuccessful check
- `core/data/src/main/java/.../repository/UserRepositoryImpl.kt` — fix removeFavorite error handling

**Tasks:** T020–T035 (see `tasks.md`)

**Verification:**
1. JSON parsing tests against actual server responses
2. Sign detail screen shows all 4 new fields
3. Story interaction shows correction feedback on wrong answers
4. Room migration (version 4 → 5) handles new columns

**Key fixes:**
- S10-01: Add `logographicValue`, `determinativeClass`, `exampleUsages`, `relatedSigns` to Sign domain + entity
- S8-01: Add `correctAnswer`, `targetGlyph`, `gardinerCode`, `hint`, `choiceId` to InteractionResult
- S11-02: Fix all DTO→Domain field drops (Dictionary, Stories, Lessons)
- S11-05: Fix FeedbackRepo to check `response.isSuccessful`

---

### Phase 3: ViewModel Safety & Lifecycle (P0)

**Goal:** Fix all resource leaks, thread safety issues, and lifecycle bugs
**Priority:** P0 — crashes, leaks, and data loss
**Dependencies:** Phase 2 (domain model changes)
**Effort:** Medium

**Files affected:**
- `feature/scan/src/main/java/.../ScanViewModel.kt` — add onCleared()
- `feature/scan/src/main/java/.../HistoryViewModel.kt` — fix duplicate collectors
- `feature/chat/src/main/java/.../ChatViewModel.kt` — fix streamJob cancel, suggestion race
- `feature/chat/src/main/java/.../ChatHistoryStore.kt` — move I/O off main thread
- `feature/stories/src/main/java/.../StoryReaderViewModel.kt` — fix saveProgress, MutableSet, restore race
- `feature/dictionary/src/main/java/.../DictionaryViewModel.kt` — fix MediaPlayer closure bug
- Multiple VMs — add double-submit guards, fix LOCAL_TTS anti-pattern

**Tasks:** T040–T058 (see `tasks.md`)

**Verification:**
1. StrictMode enabled — no disk reads on main thread
2. Navigate away during TTS playback — no leaked audio
3. Refresh scan history — no duplicate data
4. Story progress saved on back press

**Key fixes:**
- S5-01: Add `onCleared()` to ScanViewModel (MediaPlayer release)
- S5-02: Store Job reference, cancel before re-launch in HistoryViewModel
- S5-03: Wrap ChatHistoryStore I/O in `withContext(Dispatchers.IO)`
- S5-04: Use `NonCancellable` for saveProgress in StoryReaderViewModel.onCleared()

---

### Phase 4: Auth & Security (P1)

**Goal:** Fix split-brain auth, thread-blocking interceptors, and security issues
**Priority:** P1 — security and reliability
**Dependencies:** Phase 3 (some shared interceptor patterns)
**Effort:** Large (highest risk phase)

**Files affected:**
- `core/data/src/main/java/.../repository/AuthRepositoryImpl.kt` — fix split-brain
- `core/network/src/main/java/.../AuthInterceptor.kt` — replace runBlocking + regex
- `core/network/src/main/java/.../RateLimitInterceptor.kt` — replace Thread.sleep
- `core/network/src/main/java/.../TokenManager.kt` — upgrade MasterKeys API
- `feature/auth/src/main/java/.../AuthViewModel.kt` — fix forgotPasswordSent, validation
- `app/src/main/java/.../MainActivity.kt` — reactive auth state
- `app/src/main/java/.../navigation/WadjetNavGraph.kt` — sign-out navigation

**Tasks:** T060–T075 (see `tasks.md`)

**Verification:**
1. Login with Google → both Firebase + backend succeed → app works
2. Login with email when backend is down → error shown, not partial auth
3. Sign out → navigates to Welcome
4. Token expired → auto-refresh without blocking UI
5. 429 response → graceful retry without thread starvation

**Key fixes:**
- S9-03: If backend auth fails, sign out from Firebase and surface error
- S9-02: Replace `runBlocking` with OkHttp `Authenticator` interface
- S9-01: Replace `Thread.sleep` with coroutine-based retry at repo/VM layer
- S9-08: Emit auth-state-changed event on sign-out → navigate to Welcome
- S9-10: Replace regex JSON parsing with kotlinx.serialization

---

### Phase 5: Offline & Data Layer (P1)

**Goal:** Improve offline experience, fix caching gaps, upgrade FTS
**Priority:** P1 — core offline experience
**Dependencies:** Phase 2 (entity changes)
**Effort:** Medium

**Files affected:**
- `core/database/src/main/java/.../entity/SignFtsEntity.kt` — upgrade to FTS5
- `core/database/src/main/java/.../dao/SignDao.kt` — add BM25 ranking, extend offline queries
- `core/data/src/main/java/.../repository/DictionaryRepositoryImpl.kt` — fix offline search, IOException handling
- `core/data/src/main/java/.../repository/ExploreRepositoryImpl.kt` — fix IOException in getLandmarkDetail
- `core/database/src/main/java/.../WadjetDatabase.kt` — version bump, migration
- `core/data/src/main/java/.../repository/StoriesRepositoryImpl.kt` — add local progress fallback

**Tasks:** T080–T092 (see `tasks.md`)

**Verification:**
1. Airplane mode → dictionary search returns FTS results (not empty)
2. Airplane mode → landmark detail shows cached data
3. Search "bird" → results ranked by relevance, not alphabetical
4. Room database migrates cleanly from v4 to v5

**Key fixes:**
- S10-02: Upgrade FTS4 → FTS5, add BM25 ranking
- S10-04: Use searchOffline() in getSigns() IOException fallback
- S7-08: Add IOException catch to getLandmarkDetail()
- S10-06: Cache categories, alphabet, lessons in Room

---

### Phase 6: Feature Completeness & Polish (P2)

**Goal:** Wire up orphaned features, fix edge cases, improve error handling consistency
**Priority:** P2 — quality polish
**Dependencies:** Phases 2–5
**Effort:** Medium

**Files affected:**
- Chat session management (S8-03, S8-13)
- Translate tab wiring (S10-05) — OR remove dead code
- Palette rendering in Write tab (S10-14) — OR remove dead code
- Temp file cleanup across all VMs
- Error handling consistency (single pattern across repos)
- `User-Agent` header (S11-18)
- Pagination for unpaginated lists (S11-11)
- Various minor fixes (forgotPasswordSent reset, email validation, etc.)

**Tasks:** T100–T118 (see `tasks.md`)

**Verification:**
1. Load old conversation → new messages use correct session
2. Clear chat → fresh session
3. Error messages are user-friendly across all features
4. Temp files cleaned up after TTS playback

---

### Phase 7: Testing (P2)

**Goal:** Add comprehensive test coverage for all critical paths
**Priority:** P2 — but blocks final merge confidence
**Dependencies:** All previous phases
**Effort:** Large

**Files affected:** (all NEW test files)
- `core/common/src/test/.../EgyptianPronunciationTest.kt` (created in Phase 1)
- `core/data/src/test/.../ScanRepositoryImplTest.kt`
- `core/data/src/test/.../ChatRepositoryImplTest.kt`
- `core/data/src/test/.../StoriesRepositoryImplTest.kt`
- `core/data/src/test/.../AuthRepositoryImplTest.kt`
- `core/data/src/test/.../UserRepositoryImplTest.kt`
- `core/data/src/test/.../TranslateRepositoryImplTest.kt`
- `core/network/src/test/.../RateLimitInterceptorTest.kt`
- `.github/workflows/android.yml` — add instrumentation test step

**Tasks:** T120–T135 (see `tasks.md`)

**Verification:**
1. `./gradlew testDebugUnitTest` — all pass
2. `./gradlew connectedDebugAndroidTest` — Room tests pass
3. Coverage report shows ≥95% for `core:common`, ≥60% for `core:data`

---

## Complexity Tracking

| Phase | Tasks | Complexity | Key Risk |
|-------|-------|-----------|----------|
| 1. Pronunciation | 11 | Medium | Tokenizer edge cases |
| 2. API Contract | 16 | Large | Room migration, UI changes for new fields |
| 3. ViewModel Safety | 19 | Medium | Lifecycle subtle bugs |
| 4. Auth & Security | 16 | Large (highest risk) | Breaking auth flow |
| 5. Offline & Data | 13 | Medium | FTS5 migration, Room schema |
| 6. Feature Polish | 19 | Medium | Many small scattered changes |
| 7. Testing | 16 | Large | High volume of test files |
| **Total** | **~110** | — | — |

## Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| Auth changes break login | Test on real device with Google Sign-In before commit |
| Room migration fails | Export schema, write migration test, keep fallbackToDestructiveMigration as safety net |
| FTS5 upgrade drops search | Test with existing search queries before and after |
| Pronunciation changes sound wrong | Manual listen test for all ~100 words |
| DTO changes break parsing | Use actual server response JSON as test fixtures |
