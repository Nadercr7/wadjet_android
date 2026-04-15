# Phase Prompts: Logic & Quality

> **Usage**: Copy the entire prompt for the relevant phase into a new AI chat session.
> Each prompt is **self-contained** — includes all context needed to execute that phase.

---

## Phase 1: Egyptological Accuracy — Pronunciation & Gardiner

```markdown
# Task: Execute Logic & Quality Phase 1 — Pronunciation & Gardiner

## Context
You are working on the Wadjet Android app (Egyptian archaeology). This phase fixes all pronunciation-related issues, hardens the tokenizer, and adds comprehensive unit tests.

## READ FIRST (Investigation Data)
Read these files to understand the exact issues:
1. `.specify/specs/004-logic-quality/_investigation/stage-06-pronunciation.md` — P-01 through P-16
2. `.specify/specs/004-logic-quality/_investigation/stage-07-scan-landmarks.md` — S7-09 (Gardiner Unicode)
3. `.specify/specs/004-logic-quality/_investigation/stage-12-testing-references.md` — S12-08 (zero tests)
4. `.specify/specs/004-logic-quality/pronunciation-audit.md` — full word map verification, missing words, tokenizer bugs

## READ (Specs & Plan)
5. `.specify/specs/004-logic-quality/spec.md` — US1 requirements
6. `.specify/specs/004-logic-quality/plan.md` — Phase 1 section
7. `.specify/specs/004-logic-quality/tasks.md` — T001–T011

## SOURCE FILES TO MODIFY
8. `core/common/src/main/java/com/wadjet/core/common/EgyptianPronunciation.kt` (267 lines)
9. `feature/scan/src/main/java/com/wadjet/feature/scan/util/GardinerUnicode.kt` (~90 lines)

## TASKS (execute in order)

### T001: Add hyphen to MdC_STRIP
- In `EgyptianPronunciation.kt`, add `'-'` to the `MdC_STRIP` set
- Bug: hyphen `-` in MdC input treated as consonant, triggers epenthesis

### T002: Add digits to MdC_STRIP
- Add `'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'` to `MdC_STRIP`
- Bug: Gardiner code digits (e.g., "A40") flow through as phoneme tokens

### T003: Add # and & to MdC_STRIP
- Add `'#', '&'` to `MdC_STRIP`
- MdC damaged text / special block markers

### T004: Improve "already pronounceable" detection
- Current check: returns as-is if input contains 'e', 'o', or 'u'
- Issue: per-word check would be better — some words in a phrase may be MdC while others are English
- Consider: process each space-separated token independently (WORD_MAP lookup first, then character check)

### T005: Remove duplicate "Dd" WORD_MAP entry
- Line ~223 duplicates line ~170; both → "djed". Remove the later entry.

### T006: Remove duplicate "dSrt" WORD_MAP entry
- Line ~259 duplicates line ~241; both → "deshret". Remove the later entry.

### T007: Verify Gardiner COMMON_GLYPHS Unicode code points
- In `GardinerUnicode.kt`, check all 38 COMMON_GLYPHS entries against the Unicode Egyptian Hieroglyphs block (U+13000–U+1342E)
- Particularly verify G1 (Vulture/aleph) — pronunciation-audit.md flagged U+13171 vs expected U+13146
- Use Character.getName() or the Unicode chart to confirm

### T008: Write comprehensive EgyptianPronunciation unit tests
- Create `core/common/src/test/java/com/wadjet/core/common/EgyptianPronunciationTest.kt`
- Test EVERY WORD_MAP entry (all ~100+ entries): verify `toSpeech("anx")` returns "ankh", etc.
- Test compound phrases: "anx-wDA-snb" → "ankh-wedja-seneb"
- Test unknown words: fallback tokenizer produces reasonable output
- Test edge cases: empty string, blank, null-like

### T009: Write phoneme mapping tests
- In the same test file, test all 27 PHONEME_MAP entries individually
- Use single-character inputs through `convertWord()` or via words not in WORD_MAP

### T010: Write vowel epenthesis tests
- Test: three-consonant cluster produces 'e' between each pair
- Test: vowel sounds (a, ee, oo) do NOT trigger extra epenthesis
- Test: "nfr" fallback → "nefer", "Htp" fallback → "hetep"

### T011: Write GardinerUnicode tests
- Create `feature/scan/src/test/java/com/wadjet/feature/scan/util/GardinerUnicodeTest.kt`
- Test COMMON_GLYPHS lookup for known codes (A1, D21, N5, S34, X1)
- Test fallback for unknown codes
- Test GARDINER_REGEX pattern matching
- Test UNICODE_MAP lazy initialization

## VERIFY
1. Run: `./gradlew :core:common:testDebugUnitTest` — ALL tests pass
2. Run: `./gradlew :feature:scan:testDebugUnitTest` — ALL tests pass
3. No regressions in existing tests

## GIT
Branch: `004-logic-quality`
Commit: `fix(pronunciation): harden tokenizer, add comprehensive tests [T001-T011]`
```

---

## Phase 2: API Contract Alignment — DTOs & Domain Models

```markdown
# Task: Execute Logic & Quality Phase 2 — API Contract Alignment

## Context
You are working on the Wadjet Android app. This phase fixes ALL DTO→Domain mapping drops across Dictionary, Stories, Scan, and Lessons. Every API field must reach the UI layer.

## READ FIRST (Investigation Data)
1. `.specify/specs/004-logic-quality/_investigation/stage-02-network-layer.md` — full endpoint census, DTO field comparison
2. `.specify/specs/004-logic-quality/_investigation/stage-08-chat-stories.md` — S8-01 (InteractResponse drops 5 fields)
3. `.specify/specs/004-logic-quality/_investigation/stage-10-dictionary-write.md` — S10-01 (Sign drops 4 fields)
4. `.specify/specs/004-logic-quality/_investigation/stage-11-web-parity-api-surface.md` — S11-01 through S11-07
5. `.specify/specs/004-logic-quality/api-parity.md` — full field drop summary

## READ (Specs & Plan)
6. `.specify/specs/004-logic-quality/spec.md` — US2, US3, US4 requirements
7. `.specify/specs/004-logic-quality/tasks.md` — T020–T035

## SOURCE FILES TO MODIFY (read all before editing)
- `core/domain/src/main/java/com/wadjet/core/domain/model/Dictionary.kt`
- `core/domain/src/main/java/com/wadjet/core/domain/model/Story.kt`
- `core/data/src/main/java/com/wadjet/core/data/repository/DictionaryRepositoryImpl.kt`
- `core/data/src/main/java/com/wadjet/core/data/repository/StoriesRepositoryImpl.kt`
- `core/data/src/main/java/com/wadjet/core/data/repository/FeedbackRepositoryImpl.kt`
- `core/data/src/main/java/com/wadjet/core/data/repository/UserRepositoryImpl.kt`
- `core/database/src/main/java/com/wadjet/core/database/entity/SignEntity.kt`
- `core/database/src/main/java/com/wadjet/core/database/dao/SignDao.kt`
- `core/database/src/main/java/com/wadjet/core/database/WadjetDatabase.kt`
- `core/network/src/main/java/com/wadjet/core/network/api/ScanApiService.kt`
- `core/network/src/main/java/com/wadjet/core/network/model/ScanModels.kt`

## TASKS (execute in order)

### T020: Add 4 fields to Sign domain model
In `Dictionary.kt`, add to the `Sign` data class:
- `logographicValue: String? = null`
- `determinativeClass: String? = null`
- `exampleUsages: List<ExampleUsage> = emptyList()`
- `relatedSigns: List<RelatedSign> = emptyList()`
Create `ExampleUsage` and `RelatedSign` data classes if not already present.

### T021: Add same 4 fields to SignEntity
In `SignEntity.kt`, add columns + Room type converters as needed.

### T022-T023: Update SignDetailDto mappers
In `DictionaryRepositoryImpl.kt`, update `toDomain()` and `toEntity()` to map all 4 new fields.

### T024: Add 5 fields to InteractionResult domain model
In `Story.kt`, add to `InteractionResult`:
- `correctAnswer: String? = null`
- `targetGlyph: String? = null`
- `gardinerCode: String? = null`
- `hint: String? = null`
- `choiceId: String? = null`

### T025: Update InteractResponse mapper
In `StoriesRepositoryImpl.kt`, update the mapper to carry through all 5 fields.

### T026-T027: Add speechText to lesson word models
In `Dictionary.kt`, add `speechText: String? = null` to `ExampleWord` and `PracticeWord`.
Update lesson mapper in `DictionaryRepositoryImpl.kt`.

### T028: Fix ScanApiService leading slash
Change `@POST("/api/scan")` to `@POST("api/scan")`.

### T029: Fix FeedbackRepositoryImpl error handling
Add `if (!response.isSuccessful)` check before accessing `response.body()`.

### T030: Fix UserRepositoryImpl.removeFavorite
Make it throw on failure instead of silently logging a warning.

### T031: Verify ScanModels field mapping
Check if `glyph_count` from backend maps to `num_detections` — rename @SerialName if needed.

### T032-T033: Room migration v4→v5
Bump version, write Migration(4, 5) adding new columns to sign_details table.
Enable `exportSchema = true`.

### T034: Update SignDao queries for new fields

### T035: Update FTS entity to index new searchable columns

## VERIFY
1. `./gradlew :core:data:testDebugUnitTest` — passes
2. `./gradlew :core:database:testDebugUnitTest` — passes
3. `./gradlew :core:network:testDebugUnitTest` — passes
4. Build succeeds: `./gradlew assembleDebug`

## GIT
Commit: `fix(data): align DTO→Domain mappings, add missing fields [T020-T035]`
```

---

## Phase 3: ViewModel Safety & Lifecycle

```markdown
# Task: Execute Logic & Quality Phase 3 — ViewModel Safety & Lifecycle

## Context
Wadjet Android app. Fix all resource leaks, thread safety issues, and lifecycle bugs in ViewModels.

## READ FIRST
1. `.specify/specs/004-logic-quality/_investigation/stage-05-viewmodels.md` — S5-01 through S5-18, CC-1 through CC-11
2. `.specify/specs/004-logic-quality/_investigation/stage-07-scan-landmarks.md` — S7-01, S7-02, S7-05
3. `.specify/specs/004-logic-quality/_investigation/stage-08-chat-stories.md` — S8-03 through S8-16
4. `.specify/specs/004-logic-quality/_investigation/stage-10-dictionary-write.md` — S10-08, S10-09

## READ (Specs)
5. `.specify/specs/004-logic-quality/spec.md` — US6 requirements
6. `.specify/specs/004-logic-quality/tasks.md` — T040–T058

## SOURCE FILES TO MODIFY
- `feature/scan/src/main/java/.../ScanViewModel.kt`
- `feature/scan/src/main/java/.../HistoryViewModel.kt`
- `feature/chat/src/main/java/.../ChatViewModel.kt`
- `feature/chat/src/main/java/.../ChatHistoryStore.kt`
- `feature/stories/src/main/java/.../StoryReaderViewModel.kt`
- `feature/dictionary/src/main/java/.../DictionaryViewModel.kt`
- `feature/dashboard/src/main/java/.../DashboardViewModel.kt`
- `feature/explore/src/main/java/.../ExploreViewModel.kt`
- `feature/settings/src/main/java/.../SettingsViewModel.kt`
- `feature/explore/src/main/java/.../IdentifyViewModel.kt`

## CRITICAL TASKS (P0) — DO FIRST

### T040: ScanViewModel onCleared()
Add `override fun onCleared()` that:
- Releases `MediaPlayer` instance if playing/prepared
- Deletes any temp files in `cacheDir`

### T041: HistoryViewModel duplicate collector fix
Store the `viewModelScope.launch` Job reference. Before launching a new collection, cancel the previous Job. Pattern: `private var collectorJob: Job? = null` → `collectorJob?.cancel(); collectorJob = viewModelScope.launch { ... }`

### T042: ChatHistoryStore main-thread I/O
Wrap ALL file operations (readText, writeText, listFiles, delete) in `withContext(Dispatchers.IO) { ... }`.

### T043: StoryReaderViewModel saveProgress in onCleared
Change saveProgress call in onCleared() to use `NonCancellable`:
```kotlin
override fun onCleared() {
    viewModelScope.launch(NonCancellable + Dispatchers.IO) {
        storiesRepository.saveProgress(storyId, currentChapter, completedChoices)
    }
}
```

## MAJOR TASKS (P1) — DO SECOND
T044–T050: See tasks.md for details. Key patterns:
- Check `isStreaming` before retry (T044)
- Add Job tracking to loadStory/restoreProgress (T045)
- Make speakAndWait cancellation-aware (T046)
- Surface DashboardVM errors (T047)
- Replace LOCAL_TTS anti-pattern with sealed class (T048)
- Fix MediaPlayer closure capture (T049)
- Use immutable Set in data class (T050)

## MINOR TASKS (P2) — DO LAST
T051–T058: Double-submit guards, stale page snapshot, settings signOut, temp file cleanup.

## VERIFY
1. Enable StrictMode in debug: `StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())`
2. Navigate away during TTS playback → no leaked MediaPlayer
3. Refresh scan history rapidly → no duplicates
4. Story back press → progress saved
5. `./gradlew assembleDebug` — compiles

## GIT
Commit: `fix(viewmodels): lifecycle safety, leak fixes, thread correctness [T040-T058]`
```

---

## Phase 4: Auth & Security

```markdown
# Task: Execute Logic & Quality Phase 4 — Auth & Security

## Context
Wadjet Android app. Fix split-brain auth state, thread-blocking interceptors, and security issues. THIS IS THE HIGHEST RISK PHASE — test thoroughly.

## READ FIRST
1. `.specify/specs/004-logic-quality/_investigation/stage-09-auth-security.md` — S9-01 through S9-21 (21 issues)
2. `.specify/specs/004-logic-quality/_investigation/stage-11-web-parity-api-surface.md` — S11-08, S11-10, S11-17

## READ (Specs)
3. `.specify/specs/004-logic-quality/spec.md` — US5, US9 requirements
4. `.specify/specs/004-logic-quality/tasks.md` — T060–T075

## SOURCE FILES TO MODIFY
- `core/data/src/main/java/.../repository/AuthRepositoryImpl.kt`
- `core/network/src/main/java/.../AuthInterceptor.kt`
- `core/network/src/main/java/.../RateLimitInterceptor.kt`
- `core/network/src/main/java/.../TokenManager.kt`
- `feature/auth/src/main/java/.../AuthViewModel.kt`
- `app/src/main/java/.../MainActivity.kt`
- `app/src/main/java/.../navigation/WadjetNavGraph.kt`

## CRITICAL TASKS

### T060: Fix split-brain auth (S9-03)
In `AuthRepositoryImpl`, the current flow:
1. Firebase Auth success
2. Backend auth fails
3. Firebase remains signed in → split-brain

Fix: If backend auth fails after Firebase success, immediately call `FirebaseAuth.getInstance().signOut()` and throw with a descriptive error.

### T061: Replace runBlocking in AuthInterceptor (S9-02)
Replace the `runBlocking { mutex.withLock { ... } }` pattern with OkHttp's `Authenticator` interface:
```kotlin
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApi: Lazy<AuthApiService>
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // Synchronize with lock, refresh token, return new request
    }
}
```
Register as `.authenticator(tokenAuthenticator)` on OkHttpClient.

### T062: Replace Thread.sleep in RateLimitInterceptor (S9-01)
Remove `Thread.sleep()` calls. Return the 429/503 response to the caller. Handle retry at the repository/ViewModel layer using coroutine `delay()`.

## MAJOR TASKS
T063: Replace regex JSON parsing with `kotlinx.serialization.json.Json.decodeFromString()`
T064: Make `currentUser` a `StateFlow<User?>` — observe in MainActivity
T065: On sign-out, emit navigation event → reset to Welcome screen
T066: Reset `forgotPasswordSent` when sheet is dismissed
T067: Upgrade `MasterKeys.getOrCreate()` → `MasterKey.Builder(context)`

## MEDIUM/TESTS
T068–T075: Email validation, nonce, auth tests (see tasks.md)

## VERIFY
1. Login with Google → both Firebase + backend succeed
2. Backend down → Firebase login fails, error shown
3. Sign out → navigates to Welcome
4. Token expired → auto-refresh without blocking
5. 429 → retry without thread starvation
6. `./gradlew :core:data:testDebugUnitTest :core:network:testDebugUnitTest` — pass
7. `./gradlew assembleDebug` — compiles

## GIT
Commit: `fix(auth): resolve split-brain, replace blocking interceptors [T060-T075]`
```

---

## Phase 5: Offline & Data Layer

```markdown
# Task: Execute Logic & Quality Phase 5 — Offline & Data Layer

## Context
Wadjet Android app. Improve offline experience — FTS upgrade, caching gaps, offline search.

## READ FIRST
1. `.specify/specs/004-logic-quality/_investigation/stage-10-dictionary-write.md` — S10-02 through S10-18
2. `.specify/specs/004-logic-quality/_investigation/stage-07-scan-landmarks.md` — S7-08, S7-10
3. `.specify/specs/004-logic-quality/_investigation/stage-08-chat-stories.md` — S8-10

## READ (Specs)
4. `.specify/specs/004-logic-quality/spec.md` — US7, US10 requirements
5. `.specify/specs/004-logic-quality/tasks.md` — T080–T092

## SOURCE FILES TO MODIFY
- `core/database/src/main/java/.../entity/SignFtsEntity.kt`
- `core/database/src/main/java/.../dao/SignDao.kt`
- `core/database/src/main/java/.../WadjetDatabase.kt`
- `core/data/src/main/java/.../repository/DictionaryRepositoryImpl.kt`
- `core/data/src/main/java/.../repository/ExploreRepositoryImpl.kt`
- `core/data/src/main/java/.../repository/StoriesRepositoryImpl.kt`

## TASKS

### FTS Upgrade (T080-T083)
1. Change `@Fts4` to `@Fts3` or use FTS5 (Room 2.6+ supports FTS5 via `@Fts4` with `tokenizer = "unicode61"`)
2. Add BM25 ranking: `ORDER BY bm25(sign_fts)` in search query
3. Fix query sanitization to preserve Unicode characters and handle diacritics
4. Add minimum query length check (2 chars) in ViewModel

### Offline Fallback Fixes (T084-T087)
1. In `DictionaryRepositoryImpl.getSigns()`: when IOException occurs, call `searchOffline()` instead of returning empty
2. In `DictionaryRepositoryImpl.getSign()`: add IOException catch → Room fallback
3. In `ExploreRepositoryImpl.getLandmarkDetail()`: add IOException catch → Room fallback
4. In `ExploreViewModel`: use `getFiltered()` for offline category/city browsing

### Caching Improvements (T088-T091)
1. Cache categories/alphabet responses in Room
2. Add local story progress fallback in Room
3. Cache favorites locally for offline display
4. Add `lastSyncedAt` field to SignEntity for staleness tracking

### Room Migration (T092)
Write migration for FTS4→FTS5 (requires DROP + CREATE of FTS table).

## VERIFY
1. Airplane mode → dictionary search returns results (not empty)
2. Airplane mode → landmark detail shows cached data
3. Search "bird" → results ranked by relevance
4. `./gradlew :core:database:testDebugUnitTest` — passes
5. `./gradlew assembleDebug` — compiles

## GIT
Commit: `fix(offline): upgrade FTS, add offline fallbacks, improve caching [T080-T092]`
```

---

## Phase 6: Feature Completeness & Polish

```markdown
# Task: Execute Logic & Quality Phase 6 — Feature Polish

## Context
Wadjet Android app. Wire up orphaned features, fix edge cases, consistency.

## READ FIRST
1. `.specify/specs/004-logic-quality/_investigation/stage-08-chat-stories.md` — S8-02, S8-03, S8-08, S8-13, S8-14
2. `.specify/specs/004-logic-quality/_investigation/stage-10-dictionary-write.md` — S10-05, S10-12, S10-13, S10-14
3. `.specify/specs/004-logic-quality/_investigation/stage-11-web-parity-api-surface.md` — S11-07, S11-18

## READ (Specs)
4. `.specify/specs/004-logic-quality/spec.md` — US8 requirements
5. `.specify/specs/004-logic-quality/tasks.md` — T100–T118

## TASKS (19 tasks, mostly P2)

### Priority P1 (do first)
- T100: Fix loadConversation() — update sessionId to match loaded conversation
- T101: Fix clearChat() — generate new sessionId after clearing

### Priority P2 (do second)
Tasks T102–T118: See tasks.md for full details. Key items:
- Chat suggestion race fix (T102)
- SSE baseUrl trailing slash (T103)
- Dead code decisions: TranslateTab (T104), PaletteItem (T105)
- Standardize error handling pattern (T106)
- User-Agent version (T109)
- Story free limit fix (T110)
- Grid responsive layout (T112)

## VERIFY
1. Chat: load old conversation → send message → correct session
2. `./gradlew assembleDebug` — compiles
3. No regressions

## GIT
Commit: `fix(features): chat session, dead code cleanup, error consistency [T100-T118]`
```

---

## Phase 7: Testing

```markdown
# Task: Execute Logic & Quality Phase 7 — Testing

## Context
Wadjet Android app. Add comprehensive test coverage. Current: 6 test files, ~41 tests, ~15% coverage.

## READ FIRST
1. `.specify/specs/004-logic-quality/_investigation/stage-12-testing-references.md` — S12-01 through S12-18
2. `.specify/specs/004-logic-quality/testing.md` — full test plan and coverage targets

## READ (Specs)
3. `.specify/specs/004-logic-quality/tasks.md` — T120–T135

## EXISTING TEST FILES (read for patterns)
- `core/data/src/test/.../DictionaryRepositoryImplTest.kt`
- `core/data/src/test/.../ExploreRepositoryImplTest.kt`
- `core/network/src/test/.../AuthInterceptorTest.kt`
- `core/network/src/test/.../NetworkModuleTest.kt`
- `core/network/src/test/.../RateLimitInterceptorTest.kt`
- `core/database/src/test/.../SignDaoTest.kt`

## TASKS

### Priority P1 — Critical zero-test repos
- T120: `ScanRepositoryImplTest.kt` — test scan, history, temp file handling
- T121: `ChatRepositoryImplTest.kt` — test SSE parsing (data:, [DONE], error), clearChat
- T122: `StoriesRepositoryImplTest.kt` — test 4 interaction types, image gen, progress
- T123: `UserRepositoryImplTest.kt` — test favorites (add/remove), profile, stats

### Priority P1 — Additional repos
- T124: `TranslateRepositoryImplTest.kt` — test 200-with-error-body, cache indicator
- T125: `FeedbackRepositoryImplTest.kt` — test success + error paths

### Priority P2 — Extended coverage
- T126–T128: ExploreRepo tests, ScanResultDao instrumentation, fix existing test mocks
- T129: Add Turbine dependency for Flow testing
- T130–T133: ViewModel tests (Chat, Scan, Stories, Auth)
- T134: CI instrumentation test step
- T135: Code coverage reporting (JaCoCo/Kover)

## TEST PATTERNS
Use MockK for mocking, Turbine for Flow testing, JUnit 5 assertions.
Follow existing test patterns in `AuthInterceptorTest.kt` for OkHttp tests.
Follow `DictionaryRepositoryImplTest.kt` for repository test structure.

## VERIFY
1. `./gradlew testDebugUnitTest` — ALL tests pass across all modules
2. Coverage report shows >40% line coverage (target)
3. Zero test failures in CI

## GIT
Commit: `test: add comprehensive test coverage [T120-T135]`
```
