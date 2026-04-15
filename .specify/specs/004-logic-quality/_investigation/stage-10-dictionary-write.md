# Stage 10 — Dictionary & Write Feature Audit

> **Auditor**: Copilot · **Date**: 2026-04-15  
> **Scope**: Dictionary browse/search, FTS, sign detail, offline, write/translate feature, favorites

---

## Files Reviewed

| # | File | Lines |
|---|------|-------|
| 1 | `feature/dictionary/…/DictionaryViewModel.kt` | 1–185 |
| 2 | `feature/dictionary/…/SignDetailViewModel.kt` | 1–105 |
| 3 | `feature/dictionary/…/WriteViewModel.kt` | 1–66 |
| 4 | `feature/dictionary/…/TranslateViewModel.kt` | 1–65 |
| 5 | `feature/dictionary/…/LessonViewModel.kt` | 1–88 |
| 6 | `feature/dictionary/…/screen/DictionaryScreen.kt` | 1–150 |
| 7 | `feature/dictionary/…/screen/BrowseTab.kt` | 1–210 |
| 8 | `feature/dictionary/…/screen/LearnTab.kt` | 1–240 |
| 9 | `feature/dictionary/…/screen/WriteTab.kt` | 1–210 |
| 10 | `feature/dictionary/…/screen/TranslateTab.kt` | 1–200 |
| 11 | `feature/dictionary/…/screen/DictionarySignScreen.kt` | 1–100 |
| 12 | `feature/dictionary/…/screen/LessonScreen.kt` | (referenced) |
| 13 | `feature/dictionary/…/sheet/SignDetailSheet.kt` | 1–200 |
| 14 | `core/domain/…/model/Dictionary.kt` | 1–80 |
| 15 | `core/domain/…/repository/DictionaryRepository.kt` | 1–25 |
| 16 | `core/data/…/repository/DictionaryRepositoryImpl.kt` | 1–260 |
| 17 | `core/network/…/api/DictionaryApiService.kt` | 1–45 |
| 18 | `core/network/…/api/WriteApiService.kt` | 1–16 |
| 19 | `core/network/…/model/DictionaryModels.kt` | 1–115 |
| 20 | `core/network/…/model/WriteModels.kt` | 1–65 |
| 21 | `core/database/…/entity/SignEntity.kt` | 1–25 |
| 22 | `core/database/…/entity/SignFtsEntity.kt` | 1–12 |
| 23 | `core/database/…/dao/SignDao.kt` | 1–42 |
| 24 | `core/database/…/WadjetDatabase.kt` | 1–22 |

---

## Issues

### S10-01 | CRITICAL — Domain model `Sign` drops important fields from API DTO

**Files**: [Dictionary.kt](core/domain/src/main/java/com/wadjet/core/domain/model/Dictionary.kt#L1-L18) · [DictionaryRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/DictionaryRepositoryImpl.kt#L211-L227)

The API DTO `SignDetailDto` includes these fields that are **completely dropped** in the mapping to domain `Sign`:

| API field | DTO field | In domain `Sign`? | In `SignEntity`? |
|-----------|-----------|-------------------|-----------------|
| `logographic_value` | `logographicValue` | ❌ **NO** | ❌ **NO** |
| `determinative_class` | `determinativeClass` | ❌ **NO** | ❌ **NO** |
| `example_usages` | `exampleUsages` | ❌ **NO** | ❌ **NO** |
| `related_signs` | `relatedSigns` | ❌ **NO** | ❌ **NO** |

The DTO correctly deserializes all four fields, but `toDomain()` and `toEntity()` simply don't map them:

```kotlin
private fun SignDetailDto.toDomain() = Sign(
    code = code,
    glyph = unicodeChar,
    // ... no logographicValue, determinativeClass, exampleUsages, relatedSigns
)
```

**Impact**: The sign detail screen cannot show logographic values, determinative classes, example usages, or related signs — all of which are returned by the API and are core Egyptological data.

---

### S10-02 | CRITICAL — FTS uses FTS4 (no ranking, no BM25)

**File**: [SignFtsEntity.kt](core/database/src/main/java/com/wadjet/core/database/entity/SignFtsEntity.kt)

```kotlin
@Fts4(contentEntity = SignEntity::class)
@Entity(tableName = "signs_fts")
data class SignFtsEntity(
    val code: String,
    val glyph: String,
    val transliteration: String,
    val description: String,
    @ColumnInfo(name = "category_name") val categoryName: String,
)
```

**FTS4** is used instead of **FTS5**. FTS4 has no built-in ranking function (`bm25()`) and uses the deprecated `matchinfo()` for relevance scoring, which Room doesn't expose conveniently. The current DAO query doesn't use any ranking at all — results are sorted by `code` (alphabetical), not by relevance:

```kotlin
// SignDao.kt
WHERE signs_fts MATCH :query
ORDER BY signs.code    // ← alphabetical, not relevance-ranked
LIMIT :limit
```

**Impact**: Searching "bird" returns results in Gardiner code order, not by how well they match the query.

---

### S10-03 | MAJOR — FTS search query not sanitized for special FTS operators

**File**: [DictionaryRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/DictionaryRepositoryImpl.kt#L200-L201)

```kotlin
val ftsQuery = query.trim().replace(Regex("""[^\w\s]"""), "").let { "$it*" }
```

The regex strips non-word/non-space chars, then appends `*` for prefix matching. Issues:
1. `\w` in Kotlin regex matches `[a-zA-Z0-9_]` — strips all Unicode characters including Arabic, hieroglyphic transliteration diacritics (ḥ, ḫ, š, etc.)
2. Multi-word queries like `"eye of horus"` become `eye of horus*` — FTS4 treats spaces as implicit AND, so this works, but `of*` will match many unrelated terms
3. Empty query after stripping (all-special-char input) produces `*` which is an invalid FTS4 query and will throw

---

### S10-04 | MAJOR — Offline browse fallback ignores search query

**File**: [DictionaryRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/DictionaryRepositoryImpl.kt#L58-L72)

When the network is unavailable (`IOException` caught), the offline fallback dispatches based on `category` and `type` filters but **completely ignores the search query**:

```kotlin
val cached = when {
    category != null && type != null -> signDao.getByFilter(category, type, limit, offset)
    category != null -> signDao.getByCategory(category, limit, offset)
    type != null -> signDao.getByType(type, limit, offset)
    else -> signDao.getAll(limit, offset)
}
```

If the user was searching "ankh" offline, they'd get the first 30 cached signs (alphabetical) instead of search results. The separate `searchOffline()` method exists but is **never called** from `getSigns()`.

---

### S10-05 | MAJOR — Translate tab exists as screen/ViewModel but is not wired into the UI

**Files**: [TranslateTab.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/TranslateTab.kt) · [TranslateViewModel.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/TranslateViewModel.kt) · [DictionaryScreen.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/DictionaryScreen.kt)

The DictionaryScreen's tab list is:

```kotlin
private val TAB_TITLE_RES = listOf(
    R.string.dictionary_tab_browse,    // 0
    R.string.dictionary_tab_learn,     // 1
    R.string.dictionary_tab_write,     // 2
)
```

`TranslateTab` and `TranslateViewModel` are fully implemented (input fields, API call via `TranslateRepository`, result display) but are **not included in any tab or navigation route**. The code is dead/unreachable.

---

### S10-06 | MAJOR — No offline caching for categories, alphabet, or lessons

**File**: [DictionaryRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/DictionaryRepositoryImpl.kt#L80-L90)

Only `getSigns()` has an offline fallback. These methods have **no caching at all**:

| Method | Offline behavior |
|--------|-----------------|
| `getCategories()` | Throws `ApiException` |
| `getAlphabet()` | Throws `ApiException` |
| `getLesson()` | Throws `ApiException` |
| `getSign()` | Falls back to Room cache ✓ |
| `write()` | Throws `ApiException` |
| `getPalette()` | Throws `ApiException` |

The Learn tab's alphabet grid, lesson list, and the Write tab's palette are completely unavailable offline.

---

### S10-07 | MAJOR — Sign entity doesn't cache enough data for full detail display

**File**: [SignEntity.kt](core/database/src/main/java/com/wadjet/core/database/entity/SignEntity.kt)

Even if `logographicValue`, `determinativeClass`, `exampleUsages`, and `relatedSigns` were mapped to the domain model (S10-01), the Room entity doesn't store them:

```kotlin
@Entity(tableName = "signs")
data class SignEntity(
    @PrimaryKey val code: String,
    val glyph: String,
    val transliteration: String,
    // ... 
    // NO: logographicValue, determinativeClass, exampleUsages, relatedSigns
)
```

The offline sign detail view would always be incomplete even if the domain model were fixed.

---

### S10-08 | MAJOR — MediaPlayer leak in DictionaryViewModel on rapid speak calls

**File**: [DictionaryViewModel.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/DictionaryViewModel.kt#L155-L170)

```kotlin
fun speakSign(text: String) {
    ...
    mediaPlayer?.apply { if (isPlaying) stop(); release() }
    mediaPlayer = MediaPlayer().apply {
        setDataSource(tmp.absolutePath)
        prepare()
        setOnCompletionListener { release(); mediaPlayer = null; tmp.delete() }
        start()
    }
}
```

If `speakSign()` is called rapidly:
1. The old `mediaPlayer` is stopped and released
2. A new one is created
3. But `setOnCompletionListener` captures closure over `mediaPlayer` — if another `speakSign()` call happens before completion, `release()` in the listener releases the **wrong** (already-replaced) instance

The same pattern exists in `SignDetailViewModel` and `LessonViewModel`.

---

### S10-09 | MEDIUM — TTS temp files not cleaned on error path

**File**: [DictionaryViewModel.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/DictionaryViewModel.kt#L157-L167)

```kotlin
val tmp = File.createTempFile("dict_tts_", ".wav")
tmp.writeBytes(bytes)
mediaPlayer?.apply { if (isPlaying) stop(); release() }
mediaPlayer = MediaPlayer().apply {
    setDataSource(tmp.absolutePath)
    prepare()
    setOnCompletionListener { release(); mediaPlayer = null; tmp.delete() }
    start()
}
```

If `MediaPlayer.prepare()` or `start()` throws, the temp file is never deleted. Over time, this accumulates `.wav` files in the app's cache directory.

---

### S10-10 | MEDIUM — Favorites use server-only storage, no local cache

**Files**: [DictionaryViewModel.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/DictionaryViewModel.kt#L71-L78) · [SignDetailViewModel.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/SignDetailViewModel.kt#L52-L56)

Favorites are fetched from `userRepository.getFavorites()` (server API) on every ViewModel init. There's optimistic UI update with rollback on failure, but:
1. No local persistence — favorites are lost on app restart until the server responds
2. Offline → `getFavorites()` fails → `favorites` remains empty → all heart icons show unfavorited
3. `addFavorite()` / `removeFavorite()` fail offline → optimistic update rolls back → user sees flicker

---

### S10-11 | MEDIUM — Browse search debounce is 400ms, but no minimum query length

**File**: [DictionaryViewModel.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/DictionaryViewModel.kt#L124-L129)

```kotlin
fun onSearchQueryChange(query: String) {
    _state.update { it.copy(searchQuery = query) }
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
        delay(400) // debounce
        loadSigns()
    }
}
```

A single-character search (e.g., "a") triggers a full API call after 400ms. This produces large result sets and wastes bandwidth. No minimum query length filter.

---

### S10-12 | MEDIUM — Sign grid uses `GridCells.Fixed(3)` — doesn't adapt to screen width

**File**: [BrowseTab.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/BrowseTab.kt#L155)

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(3),
```

On tablets or landscape, 3 columns creates very wide cells. On small phones, cells may be too narrow for the glyph + description. Should use `GridCells.Adaptive(minSize = ...)`.

---

### S10-13 | MEDIUM — Write feature uses hardcoded "smart" mode

**File**: [WriteViewModel.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/WriteViewModel.kt#L49)

```kotlin
fun convert() {
    ...
    repository.write(s.inputText, "smart")
```

The write API accepts a `mode` parameter, but the UI hardcodes `"smart"`. There's no user option to switch between modes (e.g., "phonetic", "logographic", "gardiner").

---

### S10-14 | MEDIUM — Palette signs not used in Write tab UI

**File**: [WriteTab.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/WriteTab.kt)

The `WriteViewModel` loads `palette` on init and `WriteUiState` includes `palette: List<PaletteSign>`. The `WriteTab` composable receives `onAppendGlyph`, and a `PaletteItem` composable is defined at the bottom of the file. But the palette grid is **not rendered** in the current Write tab UI — only the text input + convert button are shown. The `PaletteItem` composable is dead code.

```kotlin
// WriteTab never renders the palette — only input + convert + result
```

---

### S10-15 | MEDIUM — FTS entity indexes only 5 columns; `reading` and `speechText` are not searchable

**File**: [SignFtsEntity.kt](core/database/src/main/java/com/wadjet/core/database/entity/SignFtsEntity.kt)

```kotlin
data class SignFtsEntity(
    val code: String,
    val glyph: String,
    val transliteration: String,
    val description: String,
    val categoryName: String,
)
```

Missing from FTS index:
- `reading` — users searching by phonetic reading (e.g., "nfr") won't find signs
- `typeName` — can't search by type label
- `speechText` — English pronunciation text not searchable

---

### S10-16 | MEDIUM — `getSign()` offline fallback has no IOException catch

**File**: [DictionaryRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/DictionaryRepositoryImpl.kt#L82-L92)

```kotlin
override suspend fun getSign(code: String, lang: String): Result<Sign> = suspendRunCatching {
    val response = dictionaryApi.getSign(code, lang = lang)
    if (response.isSuccessful) {
        ...
    } else {
        // Fallback to cache
        signDao.getByCode(code)?.toDomain()
            ?: throw ApiException("Sign not found: $code")
    }
}
```

The fallback to `signDao.getByCode()` only triggers when the server returns a non-200 response. If the request **throws** (IOException, no network), `suspendRunCatching` wraps it as a `Result.failure` without trying the cache. Compare with `getSigns()` which explicitly catches `IOException`.

---

### S10-17 | LOW — Lesson navigation hardcodes 5 lessons in UI

**File**: [LearnTab.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/LearnTab.kt#L64-L70)

```kotlin
val lessons = listOf(
    LessonInfo(1, stringResource(R.string.lesson_1_title), ...),
    LessonInfo(2, ...),
    LessonInfo(3, ...),
    LessonInfo(4, ...),
    LessonInfo(5, ...),
)
```

The lesson list is hardcoded to 5 items in the UI. If the backend adds lesson 6+, or if `totalLessons` from the API changes, the UI won't update. The API returns `total_lessons` in `LessonResponse` but it's unused in the learn tab.

---

### S10-18 | LOW — `cachedAt` timestamp in `SignEntity` is never used

**File**: [SignEntity.kt](core/database/src/main/java/com/wadjet/core/database/entity/SignEntity.kt#L22)

```kotlin
@ColumnInfo(name = "cached_at") val cachedAt: Long = System.currentTimeMillis(),
```

The `cachedAt` field is written on every insert but never queried. There's no cache invalidation, TTL check, or "stale data" indicator. Cached signs persist forever until overwritten by a newer API response.

---

### S10-19 | LOW — Error state overloaded for TTS fallback

**File**: [DictionaryViewModel.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/DictionaryViewModel.kt#L166-L167)

```kotlin
_state.update { it.copy(error = "LOCAL_TTS:$text") }
```

The error field is being used as a signal channel for TTS fallback (`"LOCAL_TTS:$text"`). This is a code smell — it overloads the error UI state with a control signal. If `error` is displayed to users, they'd see `"LOCAL_TTS:nfr"` as an error message.

---

### S10-20 | LOW — Database version 4 with `exportSchema = false`

**File**: [WadjetDatabase.kt](core/database/src/main/java/com/wadjet/core/database/WadjetDatabase.kt#L18)

```kotlin
@Database(
    entities = [...],
    version = 4,
    exportSchema = false,
)
```

Schema export is disabled, which means no `*.json` schema files are generated for migration testing. At version 4, there's no visible migration strategy — if entities change, the app likely uses `fallbackToDestructiveMigration()` (all cached data lost on upgrade).

---

### S10-21 | INFO — TranslateTab and TranslateViewModel are fully implemented but orphaned

**Files**: [TranslateTab.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/TranslateTab.kt) · [TranslateViewModel.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/TranslateViewModel.kt)

The translate feature is complete:
- UI with transliteration input, optional Gardiner sequence input, translate button, result card
- ViewModel with `translateRepository.translate()` call
- Result display: English, Arabic, context, grammar notes, Gardiner rendering

But it's unreachable from any navigation route or tab. It was likely planned as a 4th tab but not added to the tab list.

---

### S10-22 | INFO — Sign detail pronunciation display truncated

**File**: [SignDetailSheet.kt](feature/dictionary/src/main/java/com/wadjet/feature/dictionary/sheet/SignDetailSheet.kt)

```kotlin
if (!sign.pronunciationSound.isNullOrBlank()) {
    DetailRow("Pronunciation", "${sign.pronunciationSound} — ${sign.pronunciationExample.orEmpty()}")
}
```

If `pronunciationExample` is null, displays `"sound — "` with a trailing dash-space. Minor cosmetic issue.

---

## Summary

| Severity | Count |
|----------|-------|
| CRITICAL | 2 |
| MAJOR | 7 |
| MEDIUM | 8 |
| LOW | 3 |
| INFO | 2 |
| **Total** | **22** |

**Most impactful issues**: Dropped data fields from API (S10-01), FTS4 without ranking (S10-02), offline search not wired (S10-04), and translate feature fully implemented but orphaned (S10-05/21).
