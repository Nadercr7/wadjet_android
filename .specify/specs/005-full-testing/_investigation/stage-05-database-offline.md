# Stage 5 — Database and Offline Resilience

**Date:** 2025-07-22  
**Auditor:** Automated (Copilot)  
**Scope:** Room database (7 entities, 6 DAOs), DataStore, ConnectivityManager, offline behavior per feature

---

## Summary

| Metric | Value |
|---|---|
| **Room DB version** | 7 |
| **Entities** | 7 (signs, signs_fts, scan_results, landmarks, categories, story_progress, favorites) |
| **DAOs** | 6 (Sign, ScanResult, Landmark, Category, StoryProgress, Favorite) |
| **Manual migrations** | 3 (4→5, 5→6, 6→7) |
| **DataStore keys** | Only 2 (tts_enabled, tts_speed) |
| **Features with offline read** | 5 (Dictionary, Explore, Scan History, Stories progress, TTS prefs) |
| **Features with offline write** | 2 (Scan History, Stories progress) |
| **Features with NO offline support** | 6 (Chat, Auth, Feedback, Translate, User profile/stats, Lessons) |

---

## 1. Room Database

### Configuration
- **DB name:** `wadjet.db`
- **exportSchema:** `true`
- **Type converters:** None — complex data stored as JSON strings
- **Migration strategy:** Manual migrations (4→5, 5→6, 6→7) + `fallbackToDestructiveMigration()` safety net
- **Exported schemas:** 5.json, 6.json, 7.json (schemas 1-4 missing)
- **DI:** Hilt `@Singleton` via DatabaseModule.kt

### Entities

| Entity | Table | PK | Columns | Indexes | JSON Blobs |
|---|---|---|---|---|---|
| `SignEntity` | `signs` | `code` (manual) | 19 | None | `example_usages_json`, `related_signs_json` |
| `SignFtsEntity` | `signs_fts` (FTS4) | N/A (virtual) | 7 (content synced from signs) | N/A | None |
| `ScanResultEntity` | `scan_results` | `id` (autoGenerate) | 14 | None | `results_json` |
| `LandmarkEntity` | `landmarks` | `slug` (manual) | 11 | None | `detail_json` |
| `CategoryEntity` | `categories` | `code` (manual) | 4 | None | None |
| `StoryProgressEntity` | `story_progress` | `story_id` (manual) | 6 | None | `glyphs_learned_json` |
| `FavoriteEntity` | `favorites` | `(item_type, item_id)` composite | 3 | None | None |

### DAO Summary

| DAO | Queries | Flow-based | @Transaction | Conflict Strategy |
|---|---|---|---|---|
| `SignDao` | 9 (including FTS search) | None | None | REPLACE |
| `ScanResultDao` | 7 | `getAll()` → `Flow<List>` | None | REPLACE |
| `LandmarkDao` | 9 (including LIKE search) | `getAll()` → `Flow<List>` | None | REPLACE |
| `CategoryDao` | 3 | None | None | REPLACE |
| `StoryProgressDao` | 4 | None | None | REPLACE |
| `FavoriteDao` | 6 | None | None | REPLACE |

---

## 2. DataStore Preferences

| Key | Type | Default | Usage |
|---|---|---|---|
| `tts_enabled` | Boolean | `true` | Toggle TTS across app |
| `tts_speed` | Float | `1.0f` | TTS playback speed |

**Missing preferences:** Language, theme, onboarding state, last-viewed page, etc. are NOT persisted.

---

## 3. ConnectivityManagerNetworkMonitor

- **Interface:** `NetworkMonitor { val isOnline: Flow<Boolean> }`
- **Checks:** `NET_CAPABILITY_INTERNET` AND `NET_CAPABILITY_VALIDATED` (real internet, not captive portal)
- **Flow:** Uses `callbackFlow` with `.conflate()` to skip stale intermediate states
- **Consumed by:** `MainActivity` → `OfflineIndicator` composable (gold banner)
- **NOT injected into any repository or ViewModel** — repos catch `IOException` reactively instead

---

## 4. Offline Support Per Feature

### Features WITH Offline Read

| Feature | Strategy | Cache Source | Fallback Trigger |
|---|---|---|---|
| Dictionary (signs list) | Try API → catch IOException → Room | SignDao (paginated) | `java.io.IOException` |
| Dictionary (sign detail) | Try API → catch IOException → Room | SignDao `getByCode()` | `java.io.IOException` |
| Dictionary (categories) | API → cache to Room → IOException → Room | CategoryDao | `java.io.IOException` |
| Dictionary (FTS search) | Direct Room FTS4 | SignFtsEntity | Always local |
| Explore (landmarks list) | API → cache page 1 → IOException → Room | LandmarkDao (filtered) | `java.io.IOException` |
| Explore (landmark detail) | API → cache JSON → IOException → Room | LandmarkDao `detail_json` | `java.io.IOException` |
| Explorе (favorites read) | API → Room fallback | FavoriteDao | Exception |
| Scan History | Room-only (direct) | ScanResultDao `getAll()` Flow | Always local |
| Stories Progress | Firestore → Room fallback | StoryProgressDao | Firestore error |
| TTS Settings | DataStore (always local) | UserPreferencesDataStore | Always local |

### Features WITHOUT Offline Support

| Feature | Behavior When Offline | Impact |
|---|---|---|
| Chat (Thoth) | SSE stream times out → error shown | **HIGH** — core feature |
| Auth (login/register) | Firebase call fails | MEDIUM (cached session persists) |
| Feedback | API fails → error | LOW |
| Translate | API fails → error | LOW |
| User Profile/Stats | API fails → error | MEDIUM |
| Dictionary Lessons | API fails → error | MEDIUM |
| Dictionary Alphabet | API fails → error | LOW |
| Write in Hieroglyphs | API fails → error | LOW |
| Identify Landmark | API fails → error | LOW |
| Scan (new) | API fails → error | MEDIUM |
| Stories (list) | API fails → error → **empty screen** | **HIGH** |
| Stories (full story) | API fails → error | **HIGH** |
| Favorite toggle | API-first, no offline queue | MEDIUM |

---

## 5. Emulator Offline Test Results

**Note:** Emulator airplane mode does NOT fully block network (known Android emulator issue — host-routed traffic still passes through loopback). Results are from code analysis augmented with partial emulator testing.

### Screenshots Captured
- `offline_welcome.png` — Welcome screen shown (user session expired on restart in airplane mode)
- `offline_landing.png` — Landing screen after login (Firebase cached credentials worked)

### Observed Behaviors
| Screen | Offline Status | Notes |
|---|---|---|
| Welcome | Reachable | No offline indicator here (pre-auth) |
| Login | **Works** (Firebase cached credentials) | Even offline, Firebase SDK can authenticate from cache |
| Landing | Content loaded | Static content |
| Hieroglyphs Hub | Content loaded | API data loaded (emulator still had network via loopback) |
| Explore | Content loaded | Cached landmarks visible with category filters |
| Stories | Content loaded | Stories list present |
| Chat | **Works** | Backend reachable via loopback (not a true offline test) |

---

## 6. Critical Issues

| # | Issue | Severity | Detail |
|---|---|---|---|
| 1 | **`fallbackToDestructiveMigration()` present** | **HIGH** | Schemas 1-4 are missing. Any migration failure silently wipes all cached data. Users on very old versions lose everything. |
| 2 | **FTS content sync is manual only** | **HIGH** | FTS4 `content=signs` requires `INSERT INTO signs_fts(signs_fts) VALUES('rebuild')` after bulk inserts. This rebuild only runs in migrations, NOT after runtime `insertAll()`. **New signs cached from API won't appear in FTS search until a migration runs.** |
| 3 | **`runBlocking` in Firestore listener** | **HIGH** | `StoriesRepositoryImpl` uses `kotlinx.coroutines.runBlocking` inside Firestore `addSnapshotListener` callback to read from Room. Blocks the Firestore callback thread — could cause ANRs. |
| 4 | **No offline write queue** | **MEDIUM** | Favorites add/remove, feedback, translations all fail silently offline. No `WorkManager` queue to retry when connectivity returns. |
| 5 | **No `@Transaction` anywhere** | **MEDIUM** | `insertAll` + `deleteByType` in favorites isn't atomic. A crash mid-write could leave partial state. |
| 6 | **No indexes on `landmarks`** | **LOW** | Queries filter on `type`, `city` and sort by `popularity` but no indexes exist. May slow with larger datasets. |
| 7 | **No stale cache eviction** | **MEDIUM** | `cached_at` column exists on multiple entities but is never compared or used for eviction. Cache grows indefinitely. |
| 8 | **Stories list has NO offline support** | **HIGH** | `getStories()` is API-only. If offline, user sees error/empty screen with no way to read cached stories. Story progress is cached but the story content itself is not. |
| 9 | **NetworkMonitor not injected into repos** | **MEDIUM** | Repositories don't proactively check connectivity. Every offline request attempts a network call first and waits for timeout. |

---

## 7. Recommendations

1. **Remove `fallbackToDestructiveMigration()`** — replace with a migration from version 1 to current, or add all missing schemas
2. **Fix FTS rebuild** — trigger `INSERT INTO signs_fts(signs_fts) VALUES('rebuild')` after every `signDao.insertAll()` call
3. **Replace `runBlocking` with proper coroutine** — use `CoroutineScope(SupervisorJob())` in Firestore listener instead
4. **Add offline write queue** — use WorkManager for favorite toggles and feedback submission
5. **Add `@Transaction` to multi-step operations** — especially favorites sync (delete + insertAll)
6. **Add indexes** — `landmarks(type)`, `landmarks(city)`, `landmarks(popularity)`, `scan_results(created_at)`
7. **Implement stale cache eviction** — e.g., expire landmarks/signs cache after 7 days using `cached_at`
8. **Cache stories list** — add `StoryEntity` table and offline fallback in `StoriesRepositoryImpl`
9. **Inject `NetworkMonitor` into repositories** — check `isOnline` before API calls to fail-fast and serve cache immediately
10. **Expand DataStore** — persist language preference, theme, onboarding state, last-used filters
