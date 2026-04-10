# Phase 6 — Implementation Prompts

> Copy-paste these prompts to execute each sub-phase. Each prompt is self-contained with full context.

---

## Prompt 6.1: Fix Image Loading (CRITICAL)

```
Phase 6.1 — Fix landmark image loading. Images are blank everywhere.

**Root cause**: Coil's `ImageLoader` in `WadjetApplication.kt` has:
1. No base URL prepending — server returns relative paths like `/static/cache/images/...` 
2. No OkHttpClient injection — Coil uses its own default client without auth headers
3. No URL transformation — both relative and absolute (Wikimedia) URLs need handling

**What to do**:
1. In `WadjetApplication.kt`, inject `@Named("baseUrl")` (currently = `BuildConfig.BASE_URL`)
2. Add a Coil `Interceptor` that:
   - If URL starts with `/` → prepend base URL (strip trailing slash from base)
   - If URL starts with `http://` or `https://` → leave as-is
   - Edge case: if URL is empty/null → skip
3. Inject the app's authenticated `OkHttpClient` (from Hilt `NetworkModule`) into Coil's `ImageLoader.Builder.okHttpClient()`
4. This requires making `WadjetApplication` receive the OkHttpClient via Hilt — use `@EntryPoint` since Application can't use constructor injection

**Files to modify**:
- `app/src/main/java/com/wadjet/app/WadjetApplication.kt`
- Possibly `app/src/main/java/com/wadjet/app/di/NetworkModule.kt` (if OkHttpClient needs to be exposed differently)

**Verify**: After fix, all these should show images:
- ExploreScreen landmark cards (thumbnail)
- LandmarkDetailScreen image carousel (images[].url)
- LandmarkDetailScreen hero (thumbnail) 
- IdentifyScreen results
- Story chapter images

Build and test: `.\gradlew assembleDebug`
```

---

## Prompt 6.2: Fix Favorites Unification (CRITICAL)

```
Phase 6.2 — Unify favorites to use REST API only. Remove Firestore favorites.

**Problem**: ExploreScreen writes favorites to Firestore (`users/{uid}/favorites`). DashboardScreen reads favorites from REST API (`GET api/user/favorites`). They never see each other's data.

**What to do**:

1. **ExploreRepositoryImpl.kt** — Remove all Firestore favorites code:
   - Remove `FirebaseFirestore` and `FirebaseAuth` dependencies
   - `toggleFavorite(slug, name, thumbnail, isFavorite)`:
     - If adding: call `userApiService.addFavorite(AddFavoriteRequest(item_type="landmark", item_id=slug))`
     - If removing: call `userApiService.removeFavorite("landmark", slug)`
   - `getFavorites()`: call `userApiService.getFavorites()` → filter by `item_type=="landmark"` → return `Flow<Set<String>>` of slugs
   - Remove `observeFavorites()` Firestore listener

2. **ExploreViewModel.kt** — Update `observeFavorites()`:
   - Instead of collecting Firestore flow, call REST API to get favorites set
   - Refresh favorites after toggle (or optimistically update local set)

3. **DetailViewModel.kt** — Same pattern: use REST for favorite check/toggle

4. **DI Module** — Remove Firestore from Explore module's repository bindings if it was only used for favorites

**API reference**:
- `POST api/user/favorites` body: `{"item_type": "landmark", "item_id": "great_pyramids_of_giza"}`
- `DELETE api/user/favorites/{item_type}/{item_id}`
- `GET api/user/favorites` returns `List<FavoriteItemDto>`

Build and test: `.\gradlew assembleDebug`
```

---

## Prompt 6.3: Fix TTS Crash + TtsButton (HIGH)

```
Phase 6.3 — Fix pronunciation crash on 204 + update TtsButton.

**Problem 1**: `DictionaryRepositoryImpl.speakPhonetic()` calls `audioApi.speak(...)`. When server returns HTTP 204 (no audio available), the response body is empty. The code calls `.body()!!.bytes()` which throws NPE or empty body exception.

**Problem 2**: `TtsButton.kt` still uses `CircularProgressIndicator` (missed in Phase 5 cleanup).

**What to do**:

1. **DictionaryRepositoryImpl.kt** — Fix `speakPhonetic()`:
   ```kotlin
   suspend fun speakPhonetic(text: String): ByteArray? {
       val response = audioApi.speak(SpeakRequest(text=text, lang="en", context="pronunciation"))
       return if (response.isSuccessful && response.code() != 204) {
           response.body()?.bytes()
       } else null
   }
   ```

2. **DictionaryViewModel.kt** — Handle null from `speakPhonetic()`:
   - If null: set an error/signal that triggers local `TextToSpeech` in the UI
   - Pattern: `_state.update { it.copy(localTtsText = text) }` → UI reads this and uses Android TTS

3. **TtsButton.kt** — Replace `CircularProgressIndicator` with a small gold loading indicator:
   ```kotlin
   TtsState.LOADING -> Box(modifier = Modifier.size(16.dp)) {
       CircularProgressIndicator(  // Keep the small CPI here — it's appropriate for a 16dp icon-sized loader
           modifier = Modifier.size(16.dp),
           color = WadjetColors.Gold,
           strokeWidth = 2.dp,
       )
   }
   ```
   Actually, the TtsButton CPI is fine at 16dp inside an icon button — it's contextually appropriate. The Phase 5 audit was for full-screen/section loaders. Skip the TtsButton change unless you want consistency.

4. **Also check**: `ChatViewModel.speakMessage()` and `StoriesViewModel.speakChapter()` — do they handle 204/null properly?

Build and test: `.\gradlew assembleDebug`
```

---

## Prompt 6.4: Fix Scan History Tap (HIGH)

```
Phase 6.4 — Make scan history items tappable to restore full results.

**Problem**: `ScanHistoryScreen` shows past scans but `onScanTap` is `TODO: load cached result` — tapping does nothing.

**What to do**:

1. **HistoryViewModel.kt** — Add `loadScanResult(scanId: Int)`:
   - Call `scanRepository.getScanResultJson(scanId)` which reads from Room
   - Deserialize JSON string back to `ScanResult` using kotlinx.serialization
   - Expose as state: `selectedResult: ScanResult?`

2. **ScanHistoryScreen.kt** — Wire `onScanTap`:
   - On tap: call `viewModel.loadScanResult(scan.id)`
   - Show result in a bottom sheet or navigate to a result view

3. **Option A (Simple)**: Navigate to `Route.Scan` with a savedStateHandle param indicating "restore mode", then ScanViewModel reads the cached result
4. **Option B (Better)**: Navigate to `Route.ScanResult(scanId)` — this route exists but is never used. Wire it in WadjetNavGraph to a ScanResultScreen that loads from Room.

**Recommended**: Option B — use the existing `Route.ScanResult(scanId)` route.

5. **WadjetNavGraph.kt** — Wire `composable<Route.ScanResult>`:
   - Pass `scanId` to a ViewModel that loads from Room
   - Reuse `ScanResultScreen` composable with the loaded result

Build and test: `.\gradlew assembleDebug`
```

---

## Prompt 6.5: Add Standalone Translate API (MEDIUM)

```
Phase 6.5 — Add standalone MdC translation (no image scan required).

**Web backend has**: `POST /api/translate` accepting `{transliteration, gardiner_sequence}` returning `{english, arabic, context, provider}`.

**What to do**:

1. **Create `TranslateApiService.kt`** in `core/network/`:
   ```kotlin
   interface TranslateApiService {
       @POST("api/translate")
       suspend fun translate(@Body request: TranslateRequest): Response<TranslateResponse>
   }
   ```
   DTOs: `TranslateRequest(transliteration: String, gardinerSequence: String? = null)`, `TranslateResponse(transliteration, english, arabic, context, error, provider, latencyMs, fromCache)`

2. **Add to DI** — provide in `NetworkModule.kt`

3. **Create `TranslateRepository`** — simple passthrough with error handling

4. **UI Option**: Add a "Translate" section to the Dictionary WriteTab or as a separate tab
   - Text input for MdC transliteration
   - "Translate" button
   - Results card showing English + Arabic + context

5. **Wire in DictionaryViewModel** or create a small `TranslateViewModel`

Build and test: `.\gradlew assembleDebug`
```

---

## Prompt 6.6: Dynamic Categories (MEDIUM)

```
Phase 6.6 — Fetch landmark categories from server instead of hardcoding.

**Problem**: ExploreViewModel has `CATEGORIES = listOf("All", "Pharaonic", "Islamic", "Coptic", "Greco-Roman", "Museum", "Natural")` hardcoded. The `getCategoriesFromApi()` Retrofit method exists in `LandmarkApiService` but is never called.

**What to do**:

1. **ExploreViewModel.kt**:
   - Replace `CATEGORIES` companion val with a mutable state list
   - In `init`, call `exploreRepository.getCategories()` → extract `types[].name` → prepend "All" → update state
   - Keep hardcoded list as fallback if API fails
   - Also load `cities` from API response instead of from Room only

2. **ExploreRepositoryImpl.kt** — Add `getCategories()`:
   - Call `landmarkApi.getCategories()` → map to domain model
   - Return types + cities

**API response format** (from web backend):
```json
{
  "types": [{"name": "Pharaonic", "count": 120}, {"name": "Islamic", "count": 45}],
  "cities": [{"name": "Cairo", "count": 80}, {"name": "Luxor", "count": 60}],
  "category_tree": [...],
  "total": 265
}
```

Build and test: `.\gradlew assembleDebug`
```

---

## Prompt 6.7: Landing Screen Enhancement (MEDIUM)

```
Phase 6.7 — Enhance Landing screen to match web app's feature grouping.

**Web structure**: Landing has two path cards, each listing sub-features:
- Hieroglyphs: Scan, Translate, Dictionary, Write
- Landmarks: Explore 260+ sites, Identify from photo, AI descriptions

**Current Android Landing**: Two `PathCardRich` cards but they only navigate to Scan/Explore directly. Dictionary, Write, Learn, Identify are not listed.

**What to do**:

1. **LandingScreen.kt** — Update both `PathCardRich` cards:
   - Hieroglyphs card: Change features list to ["Scan & detect hieroglyphs", "Dictionary — 1,000+ signs", "Write in hieroglyphs", "Interactive lessons"]
   - Keep primary button → Scan, add secondary links/buttons for Dictionary and Write
   - Landmarks card: Change features list to ["Explore 260+ landmarks", "Identify from photo", "AI-powered details"]
   - Keep primary button → Explore, add secondary button for Identify

2. **Quick Actions grid**: Change from 2×2 to 2×3 or 3×2:
   - Add: Write (𓏞), Identify (𓉐 or camera icon), Chat (𓅝 — move from separate section)
   - Current: Scan, Dictionary, Explore, Stories
   - New: Scan, Dictionary, Write, Explore, Identify, Stories

3. Ensure navigation callbacks exist for Write (`→ Dictionary?tab=write`) and Identify (`→ Identify screen`)

Build and test: `.\gradlew assembleDebug`
```

---

## Prompt 6.8: Chat Persistence (MEDIUM)

```
Phase 6.8 — Persist chat conversations across navigation.

**Problem**: Chat history only saves on `clearChat()`. If user navigates away (back button), conversation is lost. Also, sessionId is `UUID.randomUUID()` per ViewModel instance = new session every time.

**What to do**:

1. **ChatViewModel.kt** — Save on `onCleared()`:
   ```kotlin
   override fun onCleared() {
       super.onCleared()
       saveCurrentConversation()  // Same logic as clearChat() save
       mediaPlayer?.release()
   }
   ```

2. **Session persistence** — Store last `sessionId` in SharedPreferences:
   - On init: check if stored sessionId exists and is < 1 hour old
   - If yes: reuse it, load conversation from `ChatHistoryStore`
   - If no: create new UUID, store with timestamp
   - On `clearChat()`: generate new session, store it

3. **ChatHistoryStore** — Add `loadLatestConversation()`:
   - Returns the most recent conversation by `createdAt`
   - Used to restore conversation on screen re-entry

4. **STT fallback** — In `transcribeAudio()`, on failure:
   - Instead of just setting `error = "STT_FALLBACK"`, trigger the local `SpeechRecognizer` automatically
   - Or at minimum, show a user-friendly message suggesting they use the mic button (which uses local STT)

Build and test: `.\gradlew assembleDebug`
```

---

## Prompt 6.9: Camera Re-enable (LOW)

```
Phase 6.9 — Re-enable camera capture in ScanScreen.

**Current state**: CameraX code is commented out with `// CAMERA_DISABLED` comments. The `onImageCaptured(File)` API exists in the ViewModel.

**What to do**:

1. Search for all `CAMERA_DISABLED` comments in `ScanScreen.kt`
2. Uncomment the CameraX integration:
   - `PreviewView` composable
   - `ImageCapture` use case
   - `ProcessCameraProvider` binding
   - Capture button
3. Add camera permission request using `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`
4. Toggle between camera and gallery views
5. Test: capture → compress → scan pipeline

**IdentifyScreen** already has camera support — verify it works there too.

Build and test: `.\gradlew assembleDebug`
```

---

## Prompt 6.10: Write Palette + Story Progress + Settings (LOW)

```
Phase 6.10 — Fix remaining low-priority gaps.

**1. Write palette MdC mode fix** (`WriteViewModel.kt`):
- `appendGlyph(glyph)` currently appends the unicode character
- When `selectedMode == "mdc"`, it should append the Gardiner code (e.g., "G1") instead
- The glyph object from palette has both `code` and `unicodeChar` fields

**2. Story progress** — Accept Firestore as mobile source-of-truth for now. No change needed.

**3. Settings cache clear** (`SettingsViewModel.kt` or `SettingsScreen.kt`):
- Find the `/* TODO */` for cache clear
- Implement: clear Coil disk cache + Room data:
  ```kotlin
  val imageLoader = context.imageLoader
  imageLoader.diskCache?.clear()
  imageLoader.memoryCache?.clear()
  database.clearAllTables()
  ```

**4. Verify DTO field mappings** — Spot-check these fields are mapped:
- `LandmarkDetailDto.recommendations` → `LandmarkDetail.recommendations`
- `SignDetailDto.speech` → `Sign.speech`
- `LandmarkDetailDto.wikipediaUrl` → `LandmarkDetail.wikipediaUrl`

Build and test: `.\gradlew assembleDebug`
```

---

## Execution Order

Start with **6.1 + 6.2 + 6.3** together (all critical, independent).
Then **6.4** (scan history) and **6.6** (categories).
Then **6.7** (landing) and **6.8** (chat).
Finally **6.5, 6.9, 6.10** (lower priority).

After each sub-phase: `.\gradlew assembleDebug` to verify build.
After all phases: `.\gradlew testDebugUnitTest` for full test suite.
Final: `git add -A && git commit && git push`.
