# Tasks: Logic & Quality

## Format
`[ID] [Priority] [Story] Description` → `file.kt` (from stage-XX finding)

---

## Phase 1: Egyptological Accuracy

- [ ] T001 P0 [US1] Add hyphen `-` to MdC_STRIP set in tokenizer → `EgyptianPronunciation.kt` (from stage-06 P-04)
- [ ] T002 P0 [US1] Add digits `0-9` to MdC_STRIP set → `EgyptianPronunciation.kt` (from stage-06 P-05)
- [ ] T003 P0 [US1] Add `#` and `&` to MdC_STRIP set → `EgyptianPronunciation.kt` (from stage-06 P-06)
- [ ] T004 P0 [US1] Improve "already pronounceable" detection — check for mixed MdC+English input (e.g. "Amun nTr") → `EgyptianPronunciation.kt` (from stage-06 P-08)
- [ ] T005 P0 [US1] Remove duplicate WORD_MAP key `"Dd"` (line ~223 is the duplicate) → `EgyptianPronunciation.kt` (from stage-06 P-01)
- [ ] T006 P0 [US1] Remove duplicate WORD_MAP key `"dSrt"` → `EgyptianPronunciation.kt` (from stage-06 P-02)
- [ ] T007 P0 [US1] Verify COMMON_GLYPHS Unicode code points against Unicode standard (e.g. G1 U+13146 vs U+13180) → `GardinerUnicode.kt` (from stage-07 S7-09)
- [ ] T008 P0 [US1] Write comprehensive EgyptianPronunciation unit tests — all ~100 WORD_MAP entries → NEW `EgyptianPronunciationTest.kt` (from stage-12 S12-08)
- [ ] T009 P0 [US1] Write phoneme mapping tests — all 27 PHONEME_MAP entries → `EgyptianPronunciationTest.kt` (from stage-12 S12-08)
- [ ] T010 P0 [US1] Write vowel epenthesis tests — consonant clusters produce natural output → `EgyptianPronunciationTest.kt` (from stage-12 S12-08)
- [ ] T011 P0 [US1] Write GardinerUnicode tests — COMMON_GLYPHS + fallback + invalid codes → NEW `GardinerUnicodeTest.kt` (from stage-12 S12-08)

---

## Phase 2: API Contract Alignment

### Domain Model Fixes
- [ ] T020 P0 [US3] Add `logographicValue`, `determinativeClass`, `exampleUsages`, `relatedSigns` to `Sign` domain model → `Dictionary.kt` (from stage-10 S10-01)
- [ ] T021 P0 [US3] Add same 4 fields to `SignEntity` for offline caching → `SignEntity.kt` (from stage-10 S10-07)
- [ ] T022 P0 [US3] Update `SignDetailDto.toDomain()` mapper to include all 4 fields → `DictionaryRepositoryImpl.kt` (from stage-10 S10-01)
- [ ] T023 P0 [US3] Update `SignDetailDto.toEntity()` mapper to include all 4 fields → `DictionaryRepositoryImpl.kt` (from stage-10 S10-01)
- [ ] T024 P0 [US4] Add `correctAnswer`, `targetGlyph`, `gardinerCode`, `hint`, `choiceId` to `InteractionResult` domain model → `Story.kt` (from stage-08 S8-01)
- [ ] T025 P0 [US4] Update `InteractResponse` mapper to include all 5 fields → `StoriesRepositoryImpl.kt` (from stage-08 S8-01)
- [ ] T026 P0 [US3] Add `speechText` to `ExampleWord` and `PracticeWord` domain models → `Dictionary.kt` (from stage-11 S11-03)
- [ ] T027 P0 [US3] Update lesson mapper to carry `speechText` through → `DictionaryRepositoryImpl.kt` (from stage-11 S11-03)

### Response Handling Fixes
- [ ] T028 P1 [US2] Fix ScanApiService leading slash: `@POST("/api/scan")` → `@POST("api/scan")` → `ScanApiService.kt` (from stage-11 S11-01)
- [ ] T029 P1 [US2] Fix `FeedbackRepositoryImpl` to check `response.isSuccessful` before accessing body → `FeedbackRepositoryImpl.kt` (from stage-11 S11-05)
- [ ] T030 P1 [US2] Fix `UserRepositoryImpl.removeFavorite()` to throw on failure (consistent with addFavorite) → `UserRepositoryImpl.kt` (from stage-11 S11-06)
- [ ] T031 P0 [US2] Verify `glyph_count`/`num_detections` field mapping in ScanModels → `ScanModels.kt` (from stage-02)

### Room Migration
- [ ] T032 P1 [US3] Bump Room database version 4 → 5, write migration for new SignEntity columns → `WadjetDatabase.kt` (from stage-10 S10-20)
- [ ] T033 P1 [US3] Enable `exportSchema = true` for migration testing → `WadjetDatabase.kt` (from stage-10 S10-20)
- [ ] T034 P1 [US3] Update SignDao queries to return new fields → `SignDao.kt`
- [ ] T035 P1 [US3] Update FTS entity to index `reading` and `typeName` columns → `SignFtsEntity.kt` (from stage-10 S10-15)

---

## Phase 3: ViewModel Safety & Lifecycle

### Critical Lifecycle Fixes
- [ ] T040 P0 [US6] Add `onCleared()` to ScanViewModel — release MediaPlayer, delete temp files → `ScanViewModel.kt` (from stage-05 S5-01, stage-07 S7-01)
- [ ] T041 P0 [US6] Fix HistoryViewModel — store Job reference, cancel before re-launch on refresh → `HistoryViewModel.kt` (from stage-05 S5-02, stage-07 S7-05)
- [ ] T042 P0 [US6] Move ChatHistoryStore file I/O to `Dispatchers.IO` → `ChatHistoryStore.kt` + `ChatViewModel.kt` (from stage-05 S5-03)
- [ ] T043 P0 [US6] Fix StoryReaderViewModel.onCleared() — use `NonCancellable` for saveProgress → `StoryReaderViewModel.kt` (from stage-05 S5-04)

### Major ViewModel Fixes
- [ ] T044 P1 [US6] Fix ChatViewModel.retryLastMessage() — check isStreaming, cancel old streamJob before new → `ChatViewModel.kt` (from stage-05 S5-05)
- [ ] T045 P1 [US6] Fix StoryReaderViewModel restoreProgress vs loadStory race → `StoryReaderViewModel.kt` (from stage-05 S5-06)
- [ ] T046 P1 [US6] Fix StoryReaderViewModel speakAndWait inner launch — make cancellation-aware → `StoryReaderViewModel.kt` (from stage-05 S5-07)
- [ ] T047 P1 [US6] Fix DashboardViewModel — surface errors instead of silent swallowing → `DashboardViewModel.kt` (from stage-05 S5-08)
- [ ] T048 P1 [US6] Replace LOCAL_TTS error-as-signal anti-pattern with dedicated TTS state → `ChatViewModel.kt`, `ScanViewModel.kt`, `ScanResultViewModel.kt`, `DictionaryViewModel.kt` (from stage-05 S5-09)
- [ ] T049 P1 [US6] Fix DictionaryViewModel.speakSign() MediaPlayer closure capture bug → `DictionaryViewModel.kt` (from stage-10 S10-08)
- [ ] T050 P1 [US6] Fix StoryReaderViewModel — replace MutableSet in data class with immutable Set → `StoryReaderViewModel.kt` (from stage-05 S5-15, stage-08 S8-15)

### Minor ViewModel Fixes
- [ ] T051 P2 [US6] Add double-submit guards (if isLoading return) to Auth, Translate, Write, Feedback, Settings VMs → multiple files (from stage-05 S5-10)
- [ ] T052 P2 [US6] Fix ExploreViewModel stale page snapshot in loadMore() → `ExploreViewModel.kt` (from stage-05 S5-11)
- [ ] T053 P2 [US6] Fix SettingsViewModel signOut — check result, don't keep passwords in state → `SettingsViewModel.kt` (from stage-05 S5-12)
- [ ] T054 P2 [US6] Fix SignDetailViewModel TTS failure — surface error → `SignDetailViewModel.kt` (from stage-05 S5-13)
- [ ] T055 P2 [US6] Fix IdentifyViewModel — delete temp file on failure → `IdentifyViewModel.kt` (from stage-05 S5-14)
- [ ] T056 P2 [US6] Parallelize sequential network calls in Dashboard/Landing VMs → `DashboardViewModel.kt`, `LandingViewModel.kt` (from stage-05 S5-16)

### Temp File Cleanup
- [ ] T057 P2 [US6] Add temp TTS file deletion in ScanViewModel (completion + error paths) → `ScanViewModel.kt` (from stage-07 S7-02)
- [ ] T058 P2 [US6] Add temp file deletion on error path in DictionaryViewModel → `DictionaryViewModel.kt` (from stage-10 S10-09)

---

## Phase 4: Auth & Security

### Critical Auth Fixes
- [ ] T060 P1 [US5] Fix split-brain auth: if backend fails after Firebase success → sign out Firebase + surface error → `AuthRepositoryImpl.kt` (from stage-09 S9-03)
- [ ] T061 P1 [US9] Replace `runBlocking` in AuthInterceptor with OkHttp `Authenticator` interface → `AuthInterceptor.kt` (from stage-09 S9-02)
- [ ] T062 P1 [US9] Replace `Thread.sleep` in RateLimitInterceptor — return error response, retry at VM layer → `RateLimitInterceptor.kt` (from stage-09 S9-01)

### Major Auth Fixes
- [ ] T063 P1 [US5] Replace regex JSON parsing with kotlinx.serialization in AuthInterceptor → `AuthInterceptor.kt` (from stage-09 S9-10)
- [ ] T064 P1 [US5] Make auth state reactive — `currentUser` as Flow, observe in MainActivity → `AuthRepositoryImpl.kt`, `MainActivity.kt` (from stage-09 S9-07)
- [ ] T065 P1 [US5] Add navigation reset on sign-out — emit event → navigate to Welcome → `AuthRepositoryImpl.kt`, `WadjetNavGraph.kt` (from stage-09 S9-08)
- [ ] T066 P1 [US5] Fix forgotPasswordSent never resets — clear on sheet dismiss → `AuthViewModel.kt` (from stage-09 S9-05)
- [ ] T067 P1 [US5] Upgrade `MasterKeys` to `MasterKey.Builder` → `TokenManager.kt` (from stage-09 S9-16)

### Medium Security Fixes
- [ ] T068 P2 [US9] Improve email validation — use `android.util.Patterns.EMAIL_ADDRESS` → `AuthViewModel.kt` (from stage-09 S9-13)
- [ ] T069 P2 [US9] Add Google Sign-In nonce for replay protection → `WelcomeScreen.kt` (from stage-09 S9-19)
- [ ] T070 P2 [US5] Don't send password to both Firebase AND backend (use Firebase ID token for backend) → `AuthRepositoryImpl.kt` (from stage-09 S9-14)
- [ ] T071 P2 [US9] Surface lockout duration on 429 login → `AuthViewModel.kt` (from stage-09 S9-11)

### Auth Tests
- [ ] T072 P1 [US5] Write AuthRepositoryImpl tests (Firebase+backend dual handshake) → NEW `AuthRepositoryImplTest.kt` (from stage-12 S12-01)
- [ ] T073 P1 [US9] Write RateLimitInterceptor tests (429, 503, login path) → NEW `RateLimitInterceptorTest.kt` (from stage-12)
- [ ] T074 P1 [US9] Fix AuthInterceptorTest — add concurrent 401 race condition test → `AuthInterceptorTest.kt`
- [ ] T075 P2 [US5] Write auth ViewModel tests → NEW `AuthViewModelTest.kt`

---

## Phase 5: Offline & Data Layer

### FTS Upgrade
- [ ] T080 P2 [US10] Upgrade FTS4 → FTS5 in SignFtsEntity → `SignFtsEntity.kt` (from stage-10 S10-02)
- [ ] T081 P2 [US10] Add BM25 ranking to FTS search query → `SignDao.kt` (from stage-10 S10-02)
- [ ] T082 P2 [US10] Fix FTS query sanitization — preserve Unicode, handle diacritics → `DictionaryRepositoryImpl.kt` (from stage-10 S10-03)
- [ ] T083 P2 [US10] Add minimum query length (2 chars) before FTS search → `DictionaryViewModel.kt` (from stage-10 S10-11)

### Offline Fallback Fixes
- [ ] T084 P1 [US7] Fix offline search: use `searchOffline()` in getSigns() IOException fallback → `DictionaryRepositoryImpl.kt` (from stage-10 S10-04)
- [ ] T085 P1 [US7] Fix getSign() — add IOException catch with Room fallback → `DictionaryRepositoryImpl.kt` (from stage-10 S10-16)
- [ ] T086 P1 [US7] Fix getLandmarkDetail() — add IOException catch with Room fallback → `ExploreRepositoryImpl.kt` (from stage-07 S7-08)
- [ ] T087 P1 [US7] Fix ExploreViewModel offline fallback — use getFiltered() for category/city, not emptyList() → `ExploreViewModel.kt` (from stage-07 S7-10)

### Caching Improvements
- [ ] T088 P2 [US7] Cache categories, alphabet responses in Room for offline → `DictionaryRepositoryImpl.kt` (from stage-10 S10-06)
- [ ] T089 P2 [US7] Add local story progress fallback (Room) if both Firestore and REST fail → `StoriesRepositoryImpl.kt` (from stage-08 S8-10)
- [ ] T090 P2 [US7] Cache favorites locally for offline display → `DictionaryViewModel.kt`, `ExploreRepositoryImpl.kt` (from stage-10 S10-10)
- [ ] T091 P2 [US7] Add cache TTL or staleness indicator for signed data → `SignEntity.kt` (from stage-10 S10-18)

### Room Migration for FTS5
- [ ] T092 P2 [US10] Write Room migration for FTS4→FTS5 (drop and recreate FTS table) → `WadjetDatabase.kt`

---

## Phase 6: Feature Completeness & Polish

### Chat Session Fixes
- [ ] T100 P1 [US8] Fix loadConversation() — update sessionId to match loaded conversation → `ChatViewModel.kt` (from stage-08 S8-03)
- [ ] T101 P1 [US8] Fix clearChat() — generate new sessionId after clearing → `ChatViewModel.kt` (from stage-08 S8-13)
- [ ] T102 P2 [US8] Fix suggestion chip race — pass suggestion text directly to sendMessage() → `ChatScreen.kt`, `ChatViewModel.kt` (from stage-08 S8-08)
- [ ] T103 P2 [US8] Fix SSE baseUrl — ensure trailing slash → `ChatRepositoryImpl.kt` (from stage-08 S8-02)

### Dead Code Decision
- [ ] T104 P2 — Decide: wire TranslateTab into DictionaryScreen OR remove TranslateViewModel + TranslateTab + TranslateRepository → `DictionaryScreen.kt` (from stage-10 S10-05)
- [ ] T105 P2 — Decide: wire PaletteItem into WriteTab OR remove dead code → `WriteTab.kt` (from stage-10 S10-14)

### Error Handling Consistency
- [ ] T106 P2 Standardize error handling to single pattern across all 9 repositories → multiple files (from stage-11 S11-07)
- [ ] T107 P2 Fix StoryReaderVM restoreProgress — cancel collector when no longer needed → `StoryReaderViewModel.kt` (from stage-08 S8-07)
- [ ] T108 P2 Fix ScanHistoryScreen swipe-to-delete — reset dismiss state → `ScanHistoryScreen.kt` (from stage-07 S7-06)

### Minor Polish
- [ ] T109 P2 Update User-Agent header to use BuildConfig.VERSION_NAME → `NetworkModule.kt` (from stage-11 S11-18)
- [ ] T110 P2 Fix FREE_STORY_LIMIT — use story ID/flag, not list position → `StoriesScreen.kt` (from stage-08 S8-14)
- [ ] T111 P2 Fix narration timing estimate — use TTS completion callback instead of heuristic → `StoryReaderViewModel.kt` (from stage-08 S8-06)
- [ ] T112 P2 Fix BrowseTab grid — use GridCells.Adaptive instead of Fixed(3) → `BrowseTab.kt` (from stage-10 S10-12)
- [ ] T113 P2 Fix Write mode hardcoding — expose mode selection or make server-driven → `WriteViewModel.kt` (from stage-10 S10-13)
- [ ] T114 P2 Fix IdentifyViewModel — check scan limits before identify → `IdentifyViewModel.kt` (from stage-07 S7-11)
- [ ] T115 P2 Add compressed scan image cleanup after upload → `ScanViewModel.kt` (from stage-07 S7-03)
- [ ] T116 P2 Add ChatHistoryStore encryption or use EncryptedFile → `ChatHistoryStore.kt` (from stage-08 S8-04)
- [ ] T117 P2 Fix StoriesViewModel.loadFavorites — handle failure, show empty state → `StoriesViewModel.kt` (from stage-08 S8-16)
- [ ] T118 P2 Fix lesson hardcoding — drive lesson count from API `total_lessons` → `LearnTab.kt` (from stage-10 S10-17)

---

## Phase 7: Testing

- [ ] T120 P1 Write ScanRepositoryImpl tests → NEW `ScanRepositoryImplTest.kt` (from stage-12 S12-03)
- [ ] T121 P1 Write ChatRepositoryImpl tests (SSE parsing) → NEW `ChatRepositoryImplTest.kt` (from stage-12 S12-02)
- [ ] T122 P1 Write StoriesRepositoryImpl tests (4 interaction types) → NEW `StoriesRepositoryImplTest.kt` (from stage-12 S12-04)
- [ ] T123 P1 Write UserRepositoryImpl tests → NEW `UserRepositoryImplTest.kt` (from stage-12 S12-05)
- [ ] T124 P1 Write TranslateRepositoryImpl tests (200-with-error-body) → NEW `TranslateRepositoryImplTest.kt` (from stage-12 S12-06)
- [ ] T125 P1 Write FeedbackRepositoryImpl tests → NEW `FeedbackRepositoryImplTest.kt`
- [ ] T126 P2 Write ExploreRepositoryImpl tests (offline + identify) → NEW `ExploreRepositoryImplTest.kt` (from stage-12 S12-07)
- [ ] T127 P2 Write ScanResultDao instrumentation tests → NEW `ScanResultDaoTest.kt`
- [ ] T128 P2 Fix DictionaryRepositoryImplTest.searchOffline mock arity → `DictionaryRepositoryImplTest.kt` (from stage-12 S12-18)
- [ ] T129 P2 Add Turbine dependency to feature:chat, feature:scan, feature:stories, feature:auth → `build.gradle.kts` files
- [ ] T130 P2 Add ChatViewModel tests → NEW `ChatViewModelTest.kt`
- [ ] T131 P2 Add ScanViewModel tests → NEW `ScanViewModelTest.kt`
- [ ] T132 P2 Add StoriesViewModel tests → NEW `StoriesViewModelTest.kt`
- [ ] T133 P2 Add AuthViewModel tests → NEW `AuthViewModelTest.kt`
- [ ] T134 P2 Add CI instrumentation test step → `.github/workflows/android.yml` (from stage-12 S12-12)
- [ ] T135 P2 Add code coverage reporting (JaCoCo/Kover) → root `build.gradle.kts`

---

## Summary

| Phase | Task Range | Count | Priority Mix |
|-------|-----------|-------|-------------|
| 1. Pronunciation | T001–T011 | 11 | 11 P0 |
| 2. API Contract | T020–T035 | 16 | 8 P0, 8 P1 |
| 3. ViewModel Safety | T040–T058 | 19 | 4 P0, 7 P1, 8 P2 |
| 4. Auth & Security | T060–T075 | 16 | 3 P1-critical, 8 P1, 5 P2 |
| 5. Offline & Data | T080–T092 | 13 | 4 P1, 9 P2 |
| 6. Feature Polish | T100–T118 | 19 | 2 P1, 17 P2 |
| 7. Testing | T120–T135 | 16 | 6 P1, 10 P2 |
| **Total** | | **110** | **19 P0, 32 P1, 59 P2** |
