# Stage 3: Data Layer

**Date:** 2026-04-15
**Scope:** `core/data/`, `core/domain/`

## DTO → Domain Mapping Audit

### 1. Auth — UserResponse → User
- All fields mapped EXCEPT `createdAt` → **DROPPED**
- `currentUser` Flow uses FirebaseUser, not backend data — emits hardcoded `preferredLang="en"`, `tier="free"`

### 2. Chat — No DTO mapping; raw string chunks → ViewModel builds ChatMessage. OK.

### 3. Dictionary — SignDetailDto → Sign
- **4 fields DROPPED**: `logographicValue`, `determinativeClass`, `exampleUsages`, `relatedSigns`
- `ExampleWordDto.speechText` and `PracticeWordDto.speechText` also DROPPED

### 4. Feedback — All fields mapped. OK.

### 5. Landmark — All fields mapped including coordinates. OK.

### 6. Scan — ScanResponse → ScanResult
- `aiReading.provider` → **DROPPED**
- All other fields mapped with null-safe defaults

### 7. Stories — StorySummaryDto/StoryFullDto → domain
- `ChapterDto.sceneImagePrompt` intentionally not mapped
- **5 InteractResponse fields DROPPED**: `correctAnswer`, `targetGlyph`, `gardinerCode`, `hint`, `choiceId`
- UI cannot show correct answer or hint after wrong interaction

### 8. Translate — All fields mapped. `latencyMs` dropped intentionally.

### 9. User — Stats/History OK. 
- `StoryProgressItemDto.glyphsLearned: Int` (read) vs `SaveProgressRequest.glyphsLearned: List<String>` (write) — type inconsistency

## Repository Error Handling

| Repository | Return Type | Pattern | Issues |
|---|---|---|---|
| AuthRepositoryImpl | Result<T> | suspendRunCatching | signOut() no result; backend failures silently logged |
| ChatRepositoryImpl | Result<T>/Flow<String> | suspendRunCatching + SSE onFailure | ✅ Correct |
| DictionaryRepositoryImpl | Result<T> | suspendRunCatching + IOException fallback | ✅ Correct |
| ExploreRepositoryImpl | Result<T>/Flow<T> | suspendRunCatching + IOException fallback | ✅ Correct |
| FeedbackRepositoryImpl | Result<Int> | suspendRunCatching | Does NOT check response.isSuccessful |
| ScanRepositoryImpl | Result<T>/Flow<T> | suspendRunCatching | ✅ Correct |
| StoriesRepositoryImpl | Result<T>/Flow<T> | suspendRunCatching | saveProgress() failures silently swallowed |
| TranslateRepositoryImpl | Result<T> | suspendRunCatching | ✅ Correct |
| UserRepositoryImpl | Result<T> | suspendRunCatching | removeFavorite() HTTP failure always returns success |

**Note:** WadjetResult<T> not used in any repository — all use kotlin.Result<T>

## Caching Strategy

| Repository | Cache | Offline Fallback |
|---|---|---|
| Dictionary signs | Room `signs` | ✅ IOException fallback |
| Dictionary categories/lesson/alphabet | None | ❌ |
| Landmark list | Room `landmarks` (page 1 only) | ✅ IOException fallback |
| Landmark detail | Room `detail_json` | ✅ on prior visit |
| Scan results | Room `scan_results` | ✅ Always local |
| Story progress | Firestore (offline persistence) | ✅ Firestore cache |
| Chat/User/Translate/Feedback | None | ❌ |

## Missing Repository Methods
- No offline fallback for categories, lessons, alphabet, palette in Dictionary
- No offline fallback for stories list/detail
- No pagination for ScanRepository.getScanHistory()
- No local persistence for UserRepository profile/stats

## Issues Found

| # | Severity | Description | File |
|---|---|---|---|
| S3-01 | 🟠 Major | `currentUser` Flow uses FirebaseUser with hardcoded defaults; backend data not reflected | AuthRepositoryImpl.kt |
| S3-02 | 🔵 Enhancement | `UserResponse.createdAt` dropped | AuthModels.kt |
| S3-03 | 🟠 Major | 4 Sign DTO fields dropped: logographicValue, determinativeClass, exampleUsages, relatedSigns | DictionaryRepositoryImpl.kt |
| S3-04 | 🟡 Minor | ExampleWord/PracticeWord speechText dropped — can't speak lesson words | DictionaryModels.kt |
| S3-05 | 🟡 Minor | AiReadingDto.provider dropped — AI attribution inaccessible | ScanRepositoryImpl.kt |
| S3-06 | 🟠 Major | 5 InteractResponse fields dropped (correctAnswer, hint, targetGlyph, etc.) — UI can't show feedback on wrong answer | StoriesRepositoryImpl.kt |
| S3-07 | 🟡 Minor | glyphsLearned type inconsistency between read (Int) and write (List<String>) | UserModels.kt |
| S3-08 | 🟡 Minor | FeedbackRepositoryImpl doesn't check response.isSuccessful | FeedbackRepositoryImpl.kt |
| S3-09 | 🟡 Minor | removeFavorite() HTTP failure silently returns success | UserRepositoryImpl.kt |
| S3-10 | 🟡 Minor | saveProgress() failures silently swallowed | StoriesRepositoryImpl.kt |
| S3-11 | 🔵 Enhancement | signOut() has no Result wrapper | AuthRepositoryImpl.kt |

---

# Stage 4: Database & Offline

## Room Schema Audit

| Entity | Table | PK | Indexes | Issues |
|---|---|---|---|---|
| SignEntity | signs | code (String) | NONE | No index on category, type |
| SignFtsEntity | signs_fts | FTS rowid | FTS4 | Should be FTS5 |
| ScanResultEntity | scan_results | id (autoGenerate) | NONE | No index on created_at or firestore_id |
| LandmarkEntity | landmarks | slug (String) | NONE | No index on city, type |

**Database version: 4**, `exportSchema = false`, `fallbackToDestructiveMigration()`

## FTS Configuration
- Uses **FTS4** (not FTS5)
- Default `simple` tokenizer (ASCII-only)
- Indexed columns: code, glyph, transliteration, description, categoryName
- FTS sanitizer strips non-ASCII characters via `[^\w\s]` regex — breaks non-ASCII transliteration

## DAO Query Audit

| DAO | Method | Performance Issue |
|---|---|---|
| SignDao.getByFilter | Full scan | No index on category/type |
| SignDao.getByCategory | Full scan | No index |
| SignDao.search | Non-canonical FTS JOIN | Should use rowid |
| ScanResultDao.getAll | Full scan | No index on created_at |
| ScanResultDao.getByFirestoreId | Full scan | No index |
| LandmarkDao.getFiltered | Full scan | No index on city/type |
| LandmarkDao.search | LIKE %...% | No FTS, always full scan |

## DataStore Preferences

| Key | Type | Default | Notes |
|---|---|---|---|
| tts_enabled | Boolean | true | ✅ |
| tts_speed | Float | 1.0f | ✅ |

**Missing**: preferred_lang, onboarding_completed, last_sync_timestamp

## Network Monitor
- Implementation correct (initial state, onLost re-check, validated capability)
- Cold callbackFlow per collector — should be shareIn()
- **Not used by any repository** — offline detection via IOException catch only

## Offline Feature Matrix

| Feature | Offline | Mechanism |
|---|---|---|
| Dictionary browse | ✅ Partial | Room fallback |
| Dictionary search | ✅ Always | Room FTS |
| Dictionary categories/lesson/alphabet | ❌ | Network only |
| Dictionary write | ❌ | Network only |
| Landmark browse | ✅ Partial | Room fallback (page 1) |
| Landmark detail | ✅ | Room cached JSON |
| Landmark identify | ❌ | Network only |
| Scan history | ✅ Always | Room |
| Scan new | ❌ | Network only |
| Stories | ❌ | Network only |
| Chat | ❌ | SSE fails immediately |
| Auth state | ✅ | Firebase SDK |
| User profile/stats | ❌ | Network only |

## Issues Found

| # | Severity | Description | File |
|---|---|---|---|
| S4-01 | 🟠 Major | exportSchema=false — no migration audit trail | WadjetDatabase.kt |
| S4-02 | 🟠 Major | fallbackToDestructiveMigration() — all cache lost on version bump | DatabaseModule.kt |
| S4-03 | 🟡 Minor | FTS4 instead of FTS5 — older tokenizer | SignFtsEntity.kt |
| S4-04 | 🟡 Minor | FTS sanitizer destroys non-ASCII chars | DictionaryRepositoryImpl.kt |
| S4-05 | 🟡 Minor | Non-canonical FTS JOIN pattern | SignDao.kt |
| S4-06 | 🟡 Minor | No index on signs.category/type | SignEntity.kt |
| S4-07 | 🟡 Minor | No index on scan_results.created_at | ScanResultEntity.kt |
| S4-08 | 🔵 Enhancement | No index on scan_results.firestore_id | ScanResultEntity.kt |
| S4-09 | 🟡 Minor | No index on landmarks.city/type | LandmarkEntity.kt |
| S4-10 | 🟡 Minor | Landmark search uses LIKE %...% — always full scan | LandmarkDao.kt |
| S4-11 | 🟡 Minor | preferred_lang not in DataStore — lang flash on cold start | UserPreferencesDataStore.kt |
| S4-12 | 🔵 Enhancement | NetworkMonitor cold callbackFlow per collector | ConnectivityManagerNetworkMonitor.kt |
| S4-13 | 🟡 Minor | NetworkMonitor unused by repositories; IOException-only detection | All RepositoryImpl files |
| S4-14 | 🟠 Major | 9 features have zero offline fallback | Multiple |
