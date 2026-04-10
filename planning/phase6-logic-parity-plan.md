# Phase 6: Logic Parity & Feature Completeness Plan

> **Goal**: Make the Android app work at the same quality as the web app — same APIs, same fallbacks, same smart logic, same UX grouping, same data flow.

---

## Executive Summary of Gaps

After deep analysis of both codebases, **24 critical gaps** were identified across 8 categories. The Android app currently acts as a thin API client that passes raw server responses to UI with minimal processing, missing the web app's sophisticated multi-model ensemble, fallback chains, data enrichment, and UX structure.

---

## GAP CATALOGUE

### Category A: Broken Image Loading (CRITICAL — User-Visible)

| # | Gap | Web Behavior | Android Behavior | Impact |
|---|-----|-------------|-----------------|--------|
| A1 | **Landmark thumbnails blank** | Server returns relative URLs (`/static/...` or Wikimedia URLs), Jinja2 renders them properly | Coil receives raw `thumbnail` string — no base URL prepended for relative paths, no auth headers on image requests | **All landmark images invisible** |
| A2 | **Landmark detail images blank** | Image carousel renders `images[].url`, wiki original images work | Same Coil issue — `AsyncImage(model = images[page].url)` gets relative URLs | **Detail page image carousel empty** |
| A3 | **Identify result not persisting** | After identify, user sees landmark detail with images | Identify fetches detail inline but doesn't cache to Room; navigating to detail re-fetches; if offline → error | Detail lost on navigation |
| A4 | **Coil has no OkHttp client** | N/A (server-rendered) | Coil creates its own OkHttp → no auth interceptor, no logging, no base URL handling | Auth-protected images fail silently |

**Fix**: Inject `@Named("baseUrl")` and authenticated `OkHttpClient` into Coil `ImageLoader`. Add interceptor to prepend base URL to relative paths. Handle both `/static/...` (relative) and `https://...` (absolute Wikimedia) URLs.

---

### Category B: Navigation & Feature Grouping (UX Mismatch)

| # | Gap | Web Structure | Android Structure | Impact |
|---|-----|--------------|-------------------|--------|
| B1 | **No Hieroglyphs hub** | `/hieroglyphs` page with 3 cards: Scan, Dictionary, Write | Path card on Landing goes directly to Scan. No hub page. | Write/Dictionary not discoverable from path |
| B2 | **No Landmarks hub** | `/landmarks` page with 2 cards: Explore, Identify | Path card on Landing goes directly to Explore. Identify buried in top bar | Identify not discoverable |
| B3 | **Write not accessible** | Write is standalone card in Hieroglyphs hub + tab in Dictionary | Write is a tab inside Dictionary only — no landing presence | Users don't know Write exists |
| B4 | **Learn/Lessons not accessible** | Lessons linked from Dictionary page, visible in nav | Lessons only accessible from Learn tab inside Dictionary | Hidden feature |
| B5 | **Chat not in bottom nav** | Chat always accessible from top nav | Chat only accessible from Landing card or landmark detail deeplink | Hard to reach |

**Fix**: Option 1 (Minimal): Add hub-style sections to Landing screen that mirror the web's dual-path grouping. Option 2 (Full): Add Hieroglyphs and Landmarks hub screens matching web. Recommended: Option 1 — enhance Landing with proper feature grouping.

---

### Category C: Scan Pipeline Gaps

| # | Gap | Web Behavior | Android Behavior | Impact |
|---|-----|-------------|-----------------|--------|
| C1 | **No `translate` param sent** | `POST /api/scan` accepts `translate: bool = True` from form | Android sends only `file` + `mode` parts — no `translate` field | Translation still works (server defaults to True), but user can't toggle it off |
| C2 | **No standalone translate** | `POST /api/translate` available for transliterating MdC text without re-scanning | No `TranslateApiService` exists | Users can't translate text without scanning an image |
| C3 | **Scan history tap is TODO** | Scan history shows past results | `onScanTap` is TODO — tapping a history item does nothing | History is view-only, useless |
| C4 | **Camera disabled** | Web uses `getUserMedia()` for camera capture | Camera code is commented out (`// CAMERA_DISABLED`) | Users must use gallery only |
| C5 | **No quality hints display** | Server returns `quality_hints` (blur, dark, overexposed) | `ScanResult` model has `qualityHints: List<String>?` field but it IS displayed in `ScanResultScreen` in a hints section. Actually OK. | ✅ No gap |
| C6 | **No AI unverified warning UX** | Shows yellow banner when scan is `ai_unverified` | `ScanResultScreen` does show an AI Unverified banner | ✅ No gap |

**Fix**: Add `translate` form part. Create `TranslateApiService` + repository + UI (can be a section in Dictionary). Implement scan history tap to restore cached results. Re-enable camera with CameraX.

---

### Category D: Explore/Landmark Gaps

| # | Gap | Web Behavior | Android Behavior | Impact |
|---|-----|-------------|-----------------|--------|
| D1 | **Categories hardcoded** | `GET /api/landmarks/categories` returns dynamic types/cities/category_tree | `CATEGORIES` list is hardcoded in ViewModel; `getCategoriesFromApi()` exists but is never called | New categories added to backend won't appear |
| D2 | **No subcategory tree** | Web shows nested category tree (Temples > Luxor Temples, etc.) | Flat category list only | Less organized browsing |
| D3 | **No recommendation engine** | Detail page shows 5 recommendations (tag-based scoring: type, era, city, geo-proximity) | `LandmarkDetail` model has `recommendations` field but check if server returns them | Missing or incomplete recs |
| D4 | **Identify shows wrong results** | Ensemble: ONNX + Gemini + Groq + Grok + Cloudflare with formal merge | Android sends image → gets single result. The ensemble/merge happens server-side — this is correct. BUT the user reported wrong results (pyramids → White Desert) | May be a backend issue or image quality/compression issue. Check if Android over-compresses images before upload |
| D5 | **No slug resolution/fuzzy matching** | Web has 50+ slug aliases + fuzzy Jaccard matching for mismatches | Android sends raw slug from identify result — if slug doesn't match `expanded_sites.json`, detail fetch fails | 404 on valid landmarks |
| D6 | **AI enrichment missing client-side** | Non-curated sites get AI-generated highlights/tips from backend | Android fetches detail which should include enrichment — verify DTO maps all enriched fields | May be missing fields |
| D7 | **Favorites dual-storage conflict** | Web: `POST /api/user/favorites` (REST backend) | ExploreScreen → Firestore; DashboardScreen → REST API `/api/user/favorites` | Favorites saved in Firestore never appear in Dashboard (which reads from REST) |

**Fix**: Fetch categories from API. Map subcategory tree. Verify recommendations are returned and displayed. Check image compression quality for identify. Unify favorites to use REST API only (remove Firestore favorites). Verify all enriched fields mapped in DTOs.

---

### Category E: Dictionary & Write Gaps

| # | Gap | Web Behavior | Android Behavior | Impact |
|---|-----|-------------|-----------------|--------|
| E1 | **No pronunciation crash on 204** | TTS fallback: Gemini→Groq→Browser SpeechSynthesis. 204 = use browser TTS | `speakPhonetic()` throws on empty body when server returns 204 | **App crash on pronunciation** |
| E2 | **Write preview has no debounce protection** | Smart mode calls AI → Gemini→Groq→Grok with 3-retry | 500ms debounce on `onInputChange` fires API call which can overlap | Multiple concurrent write calls |
| E3 | **Palette glyph insertion** | Web: click glyph from palette → inserts MdC code into input | Android: `appendGlyph(glyph)` appends unicode char, not MdC code | MdC mode gets unicode chars instead of codes |
| E4 | **No speech map for pronunciation** | Backend has 80+ hand-curated `_SPEECH_MAP` entries (hotep, nefer, ankh) | If the `speech` field is in the sign detail DTO, it should come from server | Verify `speech` field is mapped |
| E5 | **Alphabet not integrated with Browse** | Web: dictionary page has alphabet tab showing 26 uniliteral signs with TTS | Android: `AlphabetUiState` loaded separately, displayed in LearnTab | Different UX location but functional |

**Fix**: Handle 204 in `speakPhonetic()` (return null, trigger local TTS). Fix palette to insert MdC codes in MdC mode. Verify speech field mapping.

---

### Category F: Stories Gaps

| # | Gap | Web Behavior | Android Behavior | Impact |
|---|-----|-------------|-----------------|--------|
| F1 | **Progress stored in Firestore, not REST** | Web: progress tracked server-side in SQLite via REST | Android: Firestore `users/{uid}/story_progress` — completely separate from backend | Progress doesn't sync between web and mobile |
| F2 | **No offline story cache** | Web: service worker caches story data | Android: stories fetched from API every time, no Room cache | No offline reading |
| F3 | **Image generation on every chapter load** | Web: generates once, caches server-side by hash | Android: `loadChapterImage()` called on every `goToChapter()` — server caches but network round-trip on each chapter | Unnecessary API calls |

**Fix**: Add `POST api/user/progress` REST sync for story progress (or accept Firestore as source-of-truth for mobile). Cache stories in Room. Cache chapter image URLs locally after first fetch.

---

### Category G: Chat Gaps

| # | Gap | Web Behavior | Android Behavior | Impact |
|---|-----|-------------|-----------------|--------|
| G1 | **Chat history lost on back-nav** | Web: in-memory `_SessionStore` (ephemeral by design, 1hr TTL) | Android: `ChatHistoryStore` saves to JSON files only on `clearChat()` — not on `onCleared()` (back-navigation) | **Conversation lost** when user navigates away |
| G2 | **Session not persisted across app restarts** | Web: session ID in-memory, 1hr TTL | Android: `UUID.randomUUID()` per ViewModel = new session every screen visit | Previous conversation context lost |
| G3 | **No landmark context auto-message** | Web: landmark name → enriches system prompt with attraction info | Android: `ChatLandmark(slug)` auto-sends "Tell me about this landmark" but landmark context enrichment depends on server | Verify server receives landmark param |
| G4 | **STT only saves to input field** | Web: fills chat input after transcription | Android: `onSttResult(text)` fills input — but `transcribeAudio()` on failure sets `error = "STT_FALLBACK"` instead of falling back to local SpeechRecognizer result | STT error doesn't gracefully degrade |

**Fix**: Save chat on `onCleared()`. Persist session ID in preferences. Verify landmark param sent in SSE request body. Handle STT fallback properly.

---

### Category H: Auth & Cross-Cutting Gaps

| # | Gap | Web Behavior | Android Behavior | Impact |
|---|-----|-------------|-----------------|--------|
| H1 | **TtsButton has CircularProgressIndicator** | N/A (Phase 5 missed this one) | `TtsButton.kt` still uses `CircularProgressIndicator` for LOADING state | Inconsistent with branded loaders |
| H2 | **No offline indicator UX** | Web: N/A (always online) | `OfflineIndicator` composable exists in nav graph but unclear if it actually detects connectivity | Verify functionality |
| H3 | **Settings cache clear is TODO** | Web: no equivalent | Settings screen has `/* TODO */` for clear cache action | Broken settings option |
| H4 | **Dashboard favorites inconsistency** | Web: reads from REST backend | Android Dashboard reads favorites from `UserApiService`; Explore writes to Firestore | **Favorites appear in Explore but not Dashboard** |

---

## IMPLEMENTATION PHASES

### Phase 6.1: Image Loading Fix (CRITICAL — Day 1)
**Priority: P0 — This breaks the entire Explore experience**

1. Inject `@Named("baseUrl")` into `WadjetApplication`
2. Create Coil `Interceptor` that prepends base URL to relative paths
3. Inject authenticated `OkHttpClient` into Coil `ImageLoader`
4. Handle both relative (`/static/...`) and absolute (`https://upload.wikimedia.org/...`) URLs
5. Verify: landmark thumbnails, detail images, identify results, story images

**Files**: `WadjetApplication.kt`, possibly new `CoilModule.kt`
**Test**: Browse landmarks → images visible; identify → result images visible

---

### Phase 6.2: Favorites Unification (CRITICAL — Day 1)
**Priority: P0 — Favorites silently break between screens**

1. Remove Firestore favorites from `ExploreRepositoryImpl`
2. Use `UserApiService.addFavorite()` / `removeFavorite()` everywhere
3. Read favorites from `UserApiService.getFavorites()` in both Explore and Dashboard
4. Update `ExploreViewModel.observeFavorites()` to poll from REST API or use a shared in-memory cache

**Files**: `ExploreRepositoryImpl.kt`, `ExploreViewModel.kt`, `DetailViewModel.kt`
**Test**: Favorite a landmark in Explore → appears in Dashboard. Unfavorite in Dashboard → gone from Explore.

---

### Phase 6.3: Pronunciation / TTS Crash Fix (HIGH)
**Priority: P1 — Crashes the app**

1. Fix `DictionaryRepositoryImpl.speakPhonetic()` to handle 204 (return null)
2. Handle null in `DictionaryViewModel.speakSign()` → trigger local TTS fallback
3. Replace `CircularProgressIndicator` in `TtsButton.kt` with `WadjetSectionLoader` or a small gold indicator

**Files**: `DictionaryRepositoryImpl.kt`, `DictionaryViewModel.kt`, `TtsButton.kt`
**Test**: Tap pronunciation on any sign → audio plays or local TTS fallback

---

### Phase 6.4: Scan History Restore (HIGH)

1. Implement `onScanTap(scanId)` in `ScanHistoryScreen`
2. Read `ScanResultEntity.resultsJson` from Room
3. Deserialize back to `ScanResult`
4. Navigate to `ScanResultScreen` or show result in-place

**Files**: `HistoryViewModel.kt`, `ScanHistoryScreen.kt`, `WadjetNavGraph.kt`
**Test**: Scan an image → go to history → tap → see full results

---

### Phase 6.5: Translate API (MEDIUM)

1. Create `TranslateApiService` with `POST api/translate`
2. Add `TranslateRepository` interface + impl
3. Add translate section to Dictionary (or as a new quick-action)
4. Allow users to translate MdC/transliteration text without scanning

**Files**: New `TranslateApiService.kt`, `TranslateRepository.kt`, UI in dictionary or standalone
**Test**: Enter MdC text → get English/Arabic translation

---

### Phase 6.6: Dynamic Categories (MEDIUM)

1. Call `GET /api/landmarks/categories` in `ExploreViewModel.init`
2. Replace hardcoded `CATEGORIES` with server response types
3. Add city filter from server `cities` list
4. Optionally display subcategory tree

**Files**: `ExploreViewModel.kt`
**Test**: Categories match server data; new categories auto-appear

---

### Phase 6.7: Landing Screen Enhancement (MEDIUM)

1. Update Landing path cards to show all sub-features:
   - Hieroglyphs card: Scan, Dictionary, Write, Learn
   - Landmarks card: Explore, Identify
2. Add Write quick-action tile to the 2×2 grid (currently only Scan, Dictionary, Explore, Stories)
3. Ensure each sub-feature navigates to the right place

**Files**: `LandingScreen.kt`
**Test**: All features reachable from Landing in ≤ 2 taps

---

### Phase 6.8: Chat Persistence (MEDIUM)

1. Save conversation on `onCleared()` (not just `clearChat()`)
2. Persist `sessionId` in `EncryptedSharedPreferences` (reuse across screen visits within 1hr)
3. Restore last conversation when re-opening Chat
4. Handle STT fallback gracefully (fall to local `SpeechRecognizer` on server STT failure)

**Files**: `ChatViewModel.kt`, `ChatHistoryStore.kt`, possibly `ChatRepositoryImpl.kt`
**Test**: Send message → navigate away → come back → conversation still visible

---

### Phase 6.9: Story Progress Sync (LOW)

1. Option A: Keep Firestore for mobile (accept separate progress)
2. Option B: Sync Firestore→REST on story completion (fire-and-forget `POST api/user/progress`)
3. Cache stories in Room for offline reading

**Files**: `StoriesRepositoryImpl.kt`, possibly new Room entity
**Test**: Complete chapter → progress saved; offline → stories still readable

---

### Phase 6.10: Camera Re-enable (LOW)

1. Uncomment CameraX code in `ScanScreen.kt`
2. Test camera capture → scan pipeline
3. Also enable camera in `IdentifyScreen` (already partially implemented)

**Files**: `ScanScreen.kt`, `ScanViewModel.kt`
**Test**: Take photo with camera → scan works

---

### Phase 6.11: Palette & Write Fixes (LOW)

1. Fix `appendGlyph()` to insert MdC code (not unicode) when in MdC mode
2. Verify write preview debounce doesn't cause concurrent API calls (add `Job` cancellation)

**Files**: `WriteViewModel.kt`
**Test**: Select palette glyph in MdC mode → correct code inserted

---

### Phase 6.12: Settings & Misc (LOW)

1. Implement cache clear in Settings
2. Verify offline indicator works
3. Verify all DTO fields mapped (recommendations, enrichment, wikipedia, speech)

**Files**: `SettingsViewModel.kt`, various DTOs
**Test**: Clear cache works; offline banner shows when disconnected

---

## DEPENDENCY ORDER

```
6.1 (Images) ─── can start immediately, blocks nothing
6.2 (Favorites) ── can start immediately, blocks nothing  
6.3 (TTS crash) ── can start immediately, blocks nothing
  │
  ├── 6.4 (Scan History) ── after 6.1 (images need to work for history thumbnails)
  ├── 6.5 (Translate) ── independent
  ├── 6.6 (Categories) ── independent
  ├── 6.7 (Landing) ── after B features are confirmed working
  ├── 6.8 (Chat) ── independent
  │
  ├── 6.9 (Story Progress) ── independent  
  ├── 6.10 (Camera) ── independent
  ├── 6.11 (Write) ── independent
  └── 6.12 (Settings) ── last, depends on nothing
```

**Parallelizable**: 6.1 + 6.2 + 6.3 can all be done in the same session.

---

## ESTIMATED SCOPE

| Phase | Files Changed | New Files | Complexity |
|-------|--------------|-----------|-----------|
| 6.1 | 1-2 | 0-1 | Medium (Coil interceptor) |
| 6.2 | 3-4 | 0 | Medium (remove Firestore, wire REST) |
| 6.3 | 3 | 0 | Low (null handling + fallback) |
| 6.4 | 3 | 0 | Low-Medium (deserialize + navigate) |
| 6.5 | 3-4 | 2-3 | Medium (new API + UI) |
| 6.6 | 1-2 | 0 | Low (replace hardcoded list) |
| 6.7 | 1 | 0 | Low (UI layout changes) |
| 6.8 | 2-3 | 0 | Medium (persistence + session) |
| 6.9 | 2-3 | 0-1 | Medium (sync logic) |
| 6.10 | 2 | 0 | Medium (CameraX) |
| 6.11 | 1 | 0 | Low |
| 6.12 | 2-3 | 0 | Low |
| **Total** | ~25-30 | 2-4 | |

---

## SUCCESS CRITERIA

- [ ] All landmark images load correctly (thumbnails + detail + identify results)
- [ ] Favorites sync between Explore and Dashboard
- [ ] Pronunciation doesn't crash on 204
- [ ] Scan history items are tappable and show full results
- [ ] Categories fetched from server dynamically
- [ ] All features reachable from Landing in ≤ 2 taps
- [ ] Chat persists across navigation
- [ ] MdC palette inserts correct codes
- [ ] Translate works without scanning
- [ ] Build passes with zero errors
