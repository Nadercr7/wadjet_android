# Wadjet Android — Logic Parity & Quality Plan

> **Goal:** Make the Android app work at the SAME quality as the web app (`Wadjet-v3-beta`), with identical logic, fallbacks, API usage, and UX grouping. Then build APK.
>
> **Design Principle: SMART DEFAULTS** — The user never chooses modes. The app always picks the smartest, most realistic option automatically:
> - **Scan**: Always sends `mode=auto` (backend runs parallel AI+ONNX, 4-level fallback chain). No mode selector UI.
> - **Write**: Always sends `mode=smart` (AI translation from English → MdC → hieroglyphs, with phrase shortcuts). No mode selector UI.
> - **TTS**: Server picks best provider (Gemini→Groq→204/local). No voice picker.
> - **Identify**: Already auto (ONNX + Gemini parallel → Grok tiebreak). No change needed.
>
> **Reference web app:** `D:\Personal attachements\Projects\Wadjet-v3-beta`
> **Android workspace:** `D:\Personal attachements\Projects\Wadjet-Android`

---

## Deep Analysis Summary (Code-Level Verified)

### What Works ✅
- Auth flow (Firebase + backend JWT, token refresh with mutex lock in AuthInterceptor)
- Scan pipeline sends `mode` param (auto/ai/onnx) correctly → `POST /api/scan`
- ScanResultScreen displays: annotated image (base64), glyph flow row, transliteration, translations, timing, confidence summary, AI notes, quality hints
- Chat/Thoth SSE streaming via raw OkHttp EventSource (not Retrofit), ChatViewModel handles token-by-token updates, error recovery, conversation persistence via ChatHistoryStore
- Dictionary has 4 tabs: Browse (FTS + API), Learn (alphabet + lessons), Write (3 modes), Translate — ALL wired to ViewModels + API
- WriteTab is FULLY IMPLEMENTED with: alpha/smart/mdc mode chips, live preview (500ms debounce), glyph palette, copy button, share button, glyph breakdown — calls `POST /api/write` and `GET /api/write/palette`
- Stories list + StoryReaderScreen with all 4 interaction types, scene image generation, TTS narration
- Landmark browsing with Room cache fallback + offline search
- Scan history saved to Room with thumbnails
- TTS: server (`POST /api/audio/speak`) → 204 → `LOCAL_TTS:` signal → Android TextToSpeech engine
- STT: Server Groq Whisper (`POST /api/audio/stt`) + local SpeechRecognizer fallback
- Markdown rendering in chat via `compose-markdowntext` library
- Encrypted token storage (EncryptedSharedPreferences + AES256-GCM)
- Coil 3 with BaseUrlInterceptor prepending base URL to `/`-prefixed paths
- Landmark detail: image carousel, highlights, visiting tips, historical significance, Wikipedia URL, recommendations section, children, Google Maps, "Ask Thoth" button

### Critical Issues Found 🔴 (Verified in Code)

| # | Issue | Exact Code Location | Impact |
|---|-------|---------------------|--------|
| **B1** | **Identify DTO has WRONG field `landmark: LandmarkDetailDto?`** — Web's `POST /api/explore/identify` returns: `{slug, name, confidence, source, agreement, description, is_known_landmark, is_egyptian, top3[]}`. Android `IdentifyResponse` has `landmark` (NEVER sent) and MISSING: `source`, `agreement`, `description`, `is_known_landmark`, `is_egyptian`. Result: `detail` is always null, no source/agreement info shown. **THIS is why Pyramids → White Desert**: without `is_known_landmark` and `description` fields, the app can't show what the ensemble actually identified. | `core/network/model/LandmarkModels.kt` → `IdentifyResponse` | **BROKEN** |
| **B2** | **Identify `IdentifyMatchDto` missing fields** — Web returns `top3` as `[{name, slug, confidence, source}]`. Android DTO has only `{slug, name, confidence}` — missing `source` per match. | `core/network/model/LandmarkModels.kt` → `IdentifyMatchDto` | **BROKEN** |
| **B3** | **Identify domain model `IdentifyResult` too simple** — Only has `topMatch`, `matches`, `detail`. Missing: `source`, `agreement`, `description`, `isKnownLandmark`, `isEgyptian`. The `detail` field maps from the non-existent `landmark` DTO field → always null. | `core/domain/model/Landmark.kt` → `IdentifyResult` | **BROKEN** |
| **B4** | **Landmark thumbnails blank** — `LandmarkCard` and `ContinueScanCard` pass nullable values to `AsyncImage` with NO `placeholder`, NO `error`, NO `fallback`. Null thumbnail = completely invisible card. | All `AsyncImage` usages in explore, landing, stories | **VISUAL BUG** |
| **B5** | **Navigate "Write" from Landing → goes to Dictionary** — `onNavigateToWrite = { navController.navigate(Route.Dictionary) }` — This is actually OK because WriteTab is tab 2 inside Dictionary! But it lands on BrowseTab (tab 0), not WriteTab. | `WadjetNavGraph.kt` line routing Landing | **WRONG TAB** |

### Major Gaps 🟠 (Verified in Code)

| # | Issue | Code Evidence | Impact |
|---|-------|--------------|--------|
| **G1** | **Navigation: No Dictionary/Hieroglyphs in bottom nav** — Bottom nav: Home, Scan, Explore, Stories, Profile. Dictionary only reachable via Landing quick actions. Web has "Hieroglyphs" dropdown always visible. | `TopLevelDestination.kt` — 5 tabs, no Dictionary/Hieroglyphs | UX gap |
| **G2** | **Navigation: No Thoth/Chat in bottom nav** — Chat only reachable from Landing ThothChatEntry card. Web has "Thoth" in main nav. | `TopLevelDestination.kt` — PROFILE instead of THOTH | UX gap |
| **G3** | **Landing page: "Write" navigates to Dictionary tab 0** — Should navigate to Dictionary directly on Write tab (tab 2). Need to pass `initialTab=2` parameter. | `WadjetNavGraph.kt`: `onNavigateToWrite = { navController.navigate(Route.Dictionary) }` | Wrong destination |
| **G4** | **LandingScreen: No featured landmarks** — Web shows featured landmarks carousel. LandingViewModel doesn't load landmarks at all. | `LandingViewModel.kt` — no ExploreRepository dependency | Missing feature |
| **G5** | **Identify result screen: Only shows matches list** — Web shows: confidence badge, source indicator (ONNX/Gemini/Groq), agreement badge (consensus/uncertain), description text, is_egyptian warning, "View Details" button. Android only shows MatchCard with name + confidence. | `IdentifyScreen.kt` → `IdentifyResults` composable | Quality gap |
| **G6** | **Story covers: unicode glyph only** — No cover images. Web likely has no dedicated cover image either (uses coverGlyph), but the Android visual is minimal (just a glyph in a plain card). Should be more visually rich. | `StoriesScreen.kt` → story card | Visual gap |
| **G7** | **Camera disabled everywhere** — Both ScanScreen and IdentifyScreen have CameraX code fully commented out (`CAMERA_DISABLED`). Only image upload works. | Both feature/scan and feature/explore | Feature gap |
| **G8** | **Scan mode selector missing in UI** — ScanViewModel accepts `mode` parameter but ScanScreen has NO UI to select between auto/ai/onnx. Always sends "auto". | `ScanScreen.kt` — no mode chips | Missing UI |
| **G9** | **ScanResultScreen: glyphs not tappable** — GlyphChip shows code + unicode but no tap detail sheet. Web shows detailed info per glyph. | `ScanResultScreen.kt` → `GlyphChip` composable | UX gap |
| **G10** | **Chat: `ChatLandmark` route gets slug but `ChatViewModel` reads it wrong** — `savedStateHandle.get<String>("slug")` but the Route is defined as `data class ChatLandmark(val slug: String)` — needs `toRoute<Route.ChatLandmark>().slug` | `ChatViewModel.kt` init block | Potentially broken |

### Minor Issues 🟡

| # | Issue |
|---|-------|
| **M1** | Coil BaseUrlInterceptor only handles paths starting with `/`. Relative paths without leading `/` silently fail. |
| **M2** | `ScanResultSerializable.toDomain()` always returns `confidenceSummary = null` — loses data on Room reload. |
| **M3** | `ContinueScanCard` AsyncImage loads `scan.thumbnailPath` (local file) — no placeholder/error. |
| **M4** | Chat `LOCAL_TTS:` fallback only speaks English (`Locale.US`). Should detect language from message. |
| **M5** | Story TTS: `StoryReaderViewModel.speakChapter()` doesn't pass `voice`/`style` from ChapterDto to speak request. |
| **M6** | No pull-to-refresh on ExploreScreen, StoriesScreen (web uses HTMX fetch for fresh data). |
| **M7** | Identify: no "Identify Another" button after result. Must use back button + re-upload. |
| **M8** | Error messages are generic: "Identify failed", "Scan failed" — web has contextual messages. |

### Additional Gaps Discovered (Full Audit) 🟠

| # | Category | Web Has | Android Status | Impact |
|---|----------|---------|----------------|--------|
| **A1** | **Rate limit handling** | Per-endpoint limits + 429 responses + account lockout | **MISSING** — No 429 handler in AuthInterceptor/OkHttp, no retry-after, no cooldown UI | User gets cryptic error |
| **A2** | **Email verification** | `send-verification`, `verify-email` endpoints + branded Resend emails | **MISSING** — No endpoints, no UI | Unverified accounts |
| **A3** | **Password reset (in-app)** | `reset-password` endpoint (token + new password) | **MISSING** — Forgot-password sends email, but no in-app reset-password screen with token input | User must use web link |
| **A4** | **Free tier usage display** | `GET /api/user/limits` → tier, limits, usage | **PARTIAL** — DTO + endpoint exist, NEVER CALLED in any ViewModel. No UI shows "8/10 scans today" | User doesn't know limits |
| **A5** | **Story progress → REST API** | `POST /api/user/progress` → server-side persistence | **MISSING** — Progress goes to Firestore only, no REST `POST progress` | Server has no progress data |
| **A6** | **Scan history → REST API** | Server records scans; `GET /api/user/history` | **PARTIAL** — Room-only save. Dashboard pulls from API (which has data if server records scans independently) | Dashboard may show stale |
| **A7** | **Glyph/Story favorites UI** | Favorite landmarks, glyphs, AND stories | **PARTIAL** — Only landmark favoriting UI. Glyph/story favorite buttons don't exist on their screens | Incomplete favorite system |
| **A8** | **Language toggle** | Cookie-based EN/AR toggle, `?lang=` param, full `ar.json` i18n | **MISSING** — No Arabic strings.xml, no language toggle in Settings, all UI strings hardcoded in Composables | No Arabic UI |
| **A9** | **Accessibility** | N/A (web handles via HTML semantics) | **PARTIAL** — Some `contentDescription` tags (scan, stories), many null/missing (dashboard icons, most images) | Screen reader gaps |
| **A10** | **Write: user picks mode** | Web lets user pick but smart is default | **SMART DEFAULT** → Remove mode selector. Always use `smart` mode. User types English, gets best hieroglyphs. |
| **A11** | **Scan: user picks mode** | Web lets user pick but auto is default | **SMART DEFAULT** → Remove mode selector. Always use `auto` mode. Backend handles optimal pipeline. |
| **A12** | **Deep links** | Web URL routing (all pages addressable) | **MISSING** — No `wadjet://` scheme, no intent-filters in AndroidManifest | Can't link to specific content |

---

## Phase Plan

### Phase 0: API Contract Alignment + Critical Fixes
> Fix all DTO mismatches so the app actually receives and processes ALL data the web API returns.

**Tasks:**

**0.1 — Fix `IdentifyResponse` DTO** (`core/network/model/LandmarkModels.kt`)
```kotlin
// CURRENT (WRONG):
data class IdentifyResponse(
    val slug: String? = null,
    val name: String? = null,
    val confidence: Float = 0f,
    val top3: List<IdentifyMatchDto> = emptyList(),
    val landmark: LandmarkDetailDto? = null,  // ← NEVER returned by web
)

// CORRECT (matches web's /api/explore/identify response):
data class IdentifyResponse(
    val slug: String? = null,
    val name: String? = null,
    val confidence: Float = 0f,
    val source: String? = null,           // "onnx", "gemini", "grok", "ensemble"
    val agreement: String? = null,        // "full", "partial", "tiebreak", "single", "best_confidence"
    val description: String? = null,      // AI-generated description
    @SerialName("is_known_landmark") val isKnownLandmark: Boolean = false,
    @SerialName("is_egyptian") val isEgyptian: Boolean = true,
    val top3: List<IdentifyMatchDto> = emptyList(),
)
```

**0.2 — Fix `IdentifyMatchDto`** (`core/network/model/LandmarkModels.kt`)
```kotlin
// ADD source field:
data class IdentifyMatchDto(
    val slug: String,
    val name: String = "",
    val confidence: Float = 0f,
    val source: String? = null,  // ADD
)
```

**0.3 — Fix `IdentifyResult` domain model** (`core/domain/model/Landmark.kt`)
```kotlin
// CURRENT:
data class IdentifyResult(
    val topMatch: IdentifyMatch?,
    val matches: List<IdentifyMatch>,
    val detail: LandmarkDetail?,  // always null
)

// CORRECT:
data class IdentifyResult(
    val topMatch: IdentifyMatch?,
    val matches: List<IdentifyMatch>,
    val source: String?,
    val agreement: String?,
    val description: String?,
    val isKnownLandmark: Boolean,
    val isEgyptian: Boolean,
)
```

**0.4 — Fix `IdentifyMatch` domain model** (`core/domain/model/Landmark.kt`)
```kotlin
data class IdentifyMatch(
    val slug: String,
    val name: String,
    val confidence: Float,
    val source: String?,  // ADD
)
```

**0.5 — Fix mapping in `ExploreRepositoryImpl.identifyLandmark()`** (`core/data/repository/ExploreRepositoryImpl.kt`)
```kotlin
// Update the mapping to use new fields:
IdentifyResult(
    topMatch = if (topSlug != null && topName != null) {
        IdentifyMatch(topSlug, topName, body.confidence, body.source)
    } else null,
    matches = body.top3.map { IdentifyMatch(it.slug, it.name, it.confidence, it.source) },
    source = body.source,
    agreement = body.agreement,
    description = body.description,
    isKnownLandmark = body.isKnownLandmark,
    isEgyptian = body.isEgyptian,
)
```

**0.6 — Fix `ScanResultSerializable.toDomain()` losing `confidenceSummary`** (`core/data/repository/ScanRepositoryImpl.kt`)
- Add `confidenceSummary` field to `ScanResultSerializable`
- Map it in both `toSerializable()` and `toDomain()`

**0.7 — Fix chat `ChatLandmark` slug extraction** (`feature/chat/ChatViewModel.kt`)
- Current: `savedStateHandle.get<String>("slug")` — may not work with type-safe nav
- Should use: `savedStateHandle.toRoute<Route.ChatLandmark>().slug` (or verify it works)

**0.8 — Fix story TTS voice/style passthrough**
- Current: `SpeakRequest(text, lang, context="story_narration")` — missing voice and style
- Fix: Pass `chapter.ttsVoice` and `chapter.ttsStyle` from `ChapterDto` to speak request

**0.9 — Add 429 rate-limit handler** (`core/network/AuthInterceptor.kt` or new `RateLimitInterceptor.kt`)
- On HTTP 429: parse `Retry-After` header, show "Please wait X seconds" Snackbar, auto-retry after delay
- On account lockout (login 429): show "Account locked for 15 minutes" in login screen
- Add exponential backoff for transient failures (503, timeout)

**0.10 — Enforce smart defaults** (no user mode selection)
- `WriteViewModel.kt`: Remove mode selection state. Hardcode `mode = "smart"` in `write()` call.
- `WriteTab.kt`: Remove FilterChip mode selector row entirely. Just show text input + convert.
- `ScanViewModel.kt`: Already sends `mode = "auto"`. Confirm no UI exposes mode choice.
- Keep alpha/mdc accessible ONLY through a "Power user" long-press on the input label (hidden, not prominent)

**0.11 — Wire free tier limits**
- In `ScanViewModel.submitScan()`: call `userRepository.getLimits()` before scan. If `usage.scans >= limits.scans_per_day`, show "Daily scan limit reached (10/10)" error instead of scanning.
- Same for `ChatViewModel.sendMessage()`: check chat_messages_per_day
- Show current usage as subtle badge on Home: "3/10 scans today"

**Verification:** Build project, no compile errors. Deploy to emulator, test identify with a landmark photo. Write screen has no mode picker. Scan auto-sends auto.

---

### Phase 1: Image Loading & Placeholder System
> Every image in the app shows content or a themed placeholder. Zero blank spaces.

**Tasks:**

**1.1 — Create Egyptian-themed placeholder drawables** in `core/designsystem/src/main/res/drawable/`:
- `ic_placeholder_landmark.xml` — pyramid/temple silhouette vector
- `ic_placeholder_glyph.xml` — eye of horus silhouette
- `ic_placeholder_story.xml` — scroll/papyrus silhouette
- `ic_placeholder_error.xml` — broken-image icon

**1.2 — Fix ALL AsyncImage calls** — grep for `AsyncImage` across the entire project and add:
- `placeholder = painterResource(R.drawable.ic_placeholder_*)` (type-specific)
- `error = painterResource(R.drawable.ic_placeholder_error)` 
- `fallback = painterResource(R.drawable.ic_placeholder_*)` (for null model)

Specific locations to fix:
- `ExploreScreen` → `LandmarkCard` thumbnail
- `LandmarkDetailScreen` → image carousel + thumbnail
- `IdentifyScreen` → match result thumbnails
- `StoriesScreen` → story card cover
- `LandingScreen` → `ContinueScanCard` thumbnail, `ContinueStoryCard`
- `StoryReaderScreen` → scene image

**1.3 — Fix BaseUrlInterceptor** (`WadjetApplication.kt`) — handle relative paths without `/`:
```kotlin
if (data is String && !data.startsWith("http") && !data.startsWith("file:")) {
    val path = if (data.startsWith("/")) data else "/$data"
    val newRequest = chain.request.newBuilder().data("$baseUrl$path").build()
    return chain.withRequest(newRequest).proceed()
}
```

**1.4 — Story cover visual enhancement** — For StoriesScreen cards, use coverGlyph as large centered hieroglyph on a gradient background per difficulty:
- Beginner → gold/amber gradient
- Intermediate → blue/teal gradient  
- Advanced → purple/indigo gradient

**Verification:** Browse all landmarks — every card shows an image or placeholder. Story list shows themed covers. Zero blank image spaces.

---

### Phase 2: Identify Feature Logic Parity
> Make identify show the FULL ensemble result matching web app quality.

**Tasks:**

**2.1 — Rewrite `IdentifyResults` composable** in `IdentifyScreen.kt`:
Show:
- Top match with LARGE confidence badge (green ≥80%, yellow ≥50%, red <50%)
- Source chip: `Ensemble`, `Gemini`, `ONNX`, `Grok` (from `result.source`)
- Agreement badge: `Verified ✓` (full), `Consensus` (partial), `Uncertain` (tiebreak/best_confidence), `Single Source` (single)
- If `isEgyptian == false`: amber warning banner *"This doesn't appear to be an Egyptian landmark"*
- If `isKnownLandmark == false`: info banner *"Not in our known landmarks database"*
- Description text card (from `result.description`)
- Top-3 alternatives list with confidence progress bars + source per match
- "View Full Details" button → `LandmarkDetail(slug)` 
- "Ask Thoth about this" button → `ChatLandmark(slug)`

**2.2 — Auto-fetch landmark detail after identify**
When top match ≥60% confidence AND isKnownLandmark:
- Call `exploreRepository.getLandmarkDetail(slug)` 
- Show inline preview: thumbnail + highlights + era

**2.3 — Add "Identify Another" button** — Reset state to upload mode without back navigation

**2.4 — Update `IdentifyUiState`** — Add fields for detail preview + loading state

**Verification:** Upload pyramids photo → returns "Great Pyramids of Giza" with ensemble source, agreement badge, description. NOT "White Desert".

---

### Phase 3: Navigation Restructure
> Match the web app's information architecture. Make all features easily accessible.

**Tasks:**

**3.1 — Restructure bottom nav to 5 tabs:**

| Tab | Label | Route | Icon |
|-----|-------|-------|------|
| 1 | Home | Landing | Home |
| 2 | Hieroglyphs | **NEW** HieroglyphsHub | Egyptian-eye custom icon or `AutoStories` |
| 3 | Explore | Explore | Explore |
| 4 | Stories | Stories | MenuBook |
| 5 | Thoth | Chat | Chat/Forum |

Dashboard/Profile → accessible from **user avatar icon** in top app bar.

**3.2 — Create `Route.Hieroglyphs` and `HieroglyphsHubScreen`:**
Three feature cards matching web's `/hieroglyphs` page:
- **Scan** — "Detect & translate hieroglyphs from photos" → Route.Scan
- **Dictionary** — "Browse 1,000+ Gardiner signs" → Route.Dictionary
- **Write** — "Type English, get hieroglyphs" → Route.Dictionary (with initialTab=2 param)
  (Smart default: no mode selection. User types English text, AI converts to best hieroglyphs.)

Add a "How Scanning Works" explainer section: 1. Upload photo → 2. AI detects glyphs → 3. Translate to English/Arabic

**3.3 — Fix "Write" navigation from Landing** — Pass `initialTab=2` query arg to Dictionary so it opens on the Write tab:
- Add `initialTab` param to `Route.Dictionary`: `data class Dictionary(val initialTab: Int = 0)`
- In DictionaryScreen, read `initialTab` from SavedStateHandle and set pager initial page

**3.4 — Add user avatar to top app bar** — Small avatar/icon in top-right → navigates to Dashboard

**3.5 — Add Dashboard to routes accessible from top bar** (not bottom nav)

**Verification:** Every feature reachable within 2 taps. Bottom nav matches web info architecture. "Write" quick action opens WriteTab.

---

### Phase 4: Scan Feature Quality
> Match web's scan result quality. Smart default = always auto mode (no user choice).

**Tasks:**

**4.1 — Verify scan always sends `mode=auto`** (smart default, no mode picker UI)

**4.2 — Enhance ScanResultScreen metadata display:**
- Pipeline source indicator: "Detected by: AI Vision (Gemini)" or "ONNX + AI Verified"
- Confidence summary card with color-coded bars
- Quality hints section (if any — blurry, dark, small)
- AI notes section (collapsible) — from `aiReading.notes`
- AI verified/unverified badge

**4.3 — Make annotated image zoomable:**
Use `Modifier.pointerInput` with `detectTransformGestures` for pinch-to-zoom + pan.

**4.4 — Make detected glyphs tappable:**
Tap glyph chip → bottom sheet showing:
- Gardiner code + Unicode character (large)
- Transliteration + meaning
- Confidence (detection + classification)
- "View in Dictionary" button → `DictionarySign(code)`

**4.5 — Add "Scan Another" button** without going back

**4.6 — TTS for full transliteration** — "Read aloud" button for the entire sequence

**Verification:** Scan hieroglyphs → see mode used, confidence, AI notes. Tap glyphs for details. TTS works.

---

### Phase 5: Landmark Detail & Explore Enhancements
> Full detail parity with web landmark pages.

**Tasks:**

**5.1 — Verify LandmarkDetailScreen renders ALL sections:**
Check each section exists and works:
- ✅ Image carousel with Ken Burns
- ✅ Highlights  
- ✅ Historical significance
- ✅ Visiting tips
- ✅ Sections (from expanded_sites)
- ✅ Wikipedia URL (external link)
- ✅ Recommendations → verify tappable → navigate to detail
- ✅ Children sub-sites
- ✅ Google Maps button (coordinate intent)
- ✅ "Ask Thoth" → ChatLandmark
- ❓ Tags as chips — verify rendered
- ❓ Dynasty/era/notable pharaohs — verify from LandmarkDetailDto

**5.2 — Add featured landmarks carousel to ExploreScreen top**
- Call `getLandmarks(featured=true)` 
- Horizontal scrollable row at top of screen

**5.3 — Category filters as horizontal scrollable chip row**
- Types + cities as filter chips with counts

**5.4 — Search with debounce** (already in ExploreViewModel — verify working)

**5.5 — Pull-to-refresh** on ExploreScreen

**Verification:** Every landmark detail section renders. Featured landmarks appear at top. Filters work.

---

### Phase 6: Stories & TTS Improvements
> Stories and audio at full quality.

**Tasks:**

**6.1 — Enhance story cards visually:**
- Large coverGlyph on difficulty-themed gradient background
- Difficulty badge with color
- Chapter count + estimated time
- Glyph count taught

**6.2 — Fix TTS voice/style passthrough:**
- `StoryReaderViewModel.speakChapter()` must pass `chapter.ttsVoice` and `chapter.ttsStyle` to `SpeakRequest`
- Context should be `"story_narration"`

**6.3 — Verify scene image generation:**
- `POST /api/stories/{id}/chapters/{idx}/image` → receives `{image_url, status}`
- Show shimmer loading while generating
- Display with Ken Burns animation via AsyncImage

**6.4 — Auto-narration mode:**
- Sequential paragraph TTS (play next on `MediaPlayer.OnCompletionListener`)
- Highlight current paragraph being narrated

**6.5 — Verify all 4 interaction types:**
- `choose_glyph` — options shown, correct answer highlighted
- `write_word` — text input validated
- `glyph_discovery` — auto-pass with reveal
- `story_decision` — all valid, show outcome

**6.6 — Fix `LOCAL_TTS:` language detection** in ChatScreen:
- Currently hardcoded `Locale.US`
- Should detect Arabic text → use `Locale("ar")` for Arabic content

**Verification:** Full story playthrough. TTS with correct voices. Scene images load. All interactions work.

---

### Phase 7: Chat & Polish
> Chat quality + favorites parity + loading/error/empty states + overall app polish.

**Tasks:**

**7.1 — Verify ChatLandmark context works:**
- Fix or verify slug extraction in ChatViewModel init
- Confirm landmark context sent to SSE endpoint
- Show landmark name in chat header when in landmark mode

**7.2 — Verify markdown renders correctly in chat:**
- Bold, italic, bullets, numbered lists, tables, blockquotes, code
- Hieroglyph unicode characters render with NotoSansEgyptianHieroglyphs font

**7.3 — Verify SSE streaming robustness:**
- Mid-stream errors → show partial message + error indicator
- Network timeout → graceful recovery

**7.4 — Add glyph + story favorite buttons:**
- Dictionary: Add ♡ toggle on sign detail sheet → `POST /api/user/favorites` type=glyph
- Stories: Add ♡ toggle on story card → `POST /api/user/favorites` type=story
- Landmarks already have favorite toggle — verify it works

**7.5 — Wire story progress to REST API:**
- Add `POST /api/user/progress` call in `StoriesRepositoryImpl.saveProgress()`
- Keep Firestore as secondary sync, but REST API is primary
- Progress should sync: chapter_index, glyphs_learned, score, completed

**7.6 — Add loading states everywhere:**
- Shimmer skeletons for: landmark grid, dictionary list, story list
- Already have: scan processing animation, chat streaming dots

**7.7 — Error states — contextual per feature:**
- Scan: "Couldn't read the hieroglyphs. Try a clearer photo."
- Identify: "Couldn't identify the landmark. Try a different angle."
- Network: "No connection. Some features available offline."
- Rate limited: "You've reached your daily limit. Try again tomorrow."

**7.8 — Empty states:**
- No scan history: eye icon + "Your first scan awaits"
- No favorites: heart + "Tap ♡ on landmarks you love"
- No search results: magnifier + "No matches found"

**7.9 — Pull-to-refresh** on: ExploreScreen, StoriesScreen, DashboardScreen

**7.10 — Haptic feedback** on: button taps, scan complete, quiz answer correct

**7.11 — Offline banner** when no connectivity (use existing `NetworkMonitor`)

**7.12 — Accessibility pass:**
- Add `contentDescription` to ALL images, icons, and interactive elements
- Ensure TalkBack can navigate all screens
- Priority: scan result images, landmark cards, story cards, dashboard icons

**Verification:** All screens have loading/error/empty states. Favorites work for landmarks + glyphs + stories. Haptics work.

---

### Phase 8: Testing & Validation
> Comprehensive testing of EVERY feature before APK build.

**Checklist:**

**Auth:**
- [ ] Register new account
- [ ] Login with email/password
- [ ] Google Sign-In
- [ ] Token refresh (stay logged in 30+ min)
- [ ] Logout and re-login
- [ ] Forgot password → email received

**Scan (Smart Default = auto, no mode picker):**
- [ ] Upload hieroglyph image → correct Gardiner codes
- [ ] Verify NO mode selector visible — always auto
- [ ] Full metadata displayed (pipeline, confidence, AI notes, timing)
- [ ] Glyphs tappable → detail sheet
- [ ] TTS plays transliteration
- [ ] Scan saved to history
- [ ] View scan history → tap to revisit
- [ ] Daily limit reached → shows limit message

**Identify:**
- [ ] Upload pyramids photo → "Great Pyramids of Giza" (NOT White Desert)
- [ ] Upload Sphinx → "Great Sphinx of Giza"
- [ ] Upload non-Egyptian → "not Egyptian" indicator
- [ ] Shows: confidence, source, agreement, description
- [ ] "View Details" → landmark detail
- [ ] "Identify Another" works
- [ ] "Ask Thoth" → contextual chat

**Dictionary (all 4 tabs):**
- [ ] Browse: all categories, search, sign detail sheet
- [ ] Learn: alphabet, lessons 1-5
- [ ] Translate: transliteration → EN + AR

**Write (Smart Default = smart, no mode picker):**
- [ ] Type English text → get best hieroglyphs (smart mode auto)
- [ ] Verify NO mode selector visible
- [ ] Glyph palette works
- [ ] Copy result
- [ ] TTS for result

**Explore:**
- [ ] All landmark cards show images/placeholders
- [ ] Category/city filters work
- [ ] Search by name
- [ ] Detail: all sections visible
- [ ] Image carousel works
- [ ] Recommendations tappable
- [ ] Google Maps opens
- [ ] "Ask Thoth" opens chat
- [ ] Favorite toggle ♡ works

**Stories:**
- [ ] All listed with themed covers
- [ ] Read through chapters
- [ ] Scene images load (AI generated)
- [ ] All 4 interaction types work
- [ ] TTS narration works
- [ ] Bilingual text visible
- [ ] Favorite toggle ♡ works
- [ ] Progress saves to server

**Chat:**
- [ ] SSE streaming works
- [ ] Markdown renders
- [ ] Voice input (STT) works
- [ ] Voice output (TTS) works
- [ ] Landmark chat context works
- [ ] Clear chat works
- [ ] History persistence works

**Dashboard:**
- [ ] Profile info correct
- [ ] Stats + scan history + favorites (all 3 types)
- [ ] Story progress

**Favorites:**
- [ ] Favorite a landmark → appears in dashboard
- [ ] Favorite a glyph → appears in dashboard
- [ ] Favorite a story → appears in dashboard
- [ ] Unfavorite all 3 types works

**Rate Limits:**
- [ ] Scan limit → shows "limit reached" message
- [ ] Chat limit → shows "limit reached" message
- [ ] 429 from server → shows friendly retry message

**Offline:**
- [ ] Dictionary FTS search works offline
- [ ] Cached landmarks load
- [ ] Offline banner shows
- [ ] Proper error messages elsewhere

---

### Phase 9: APK Build
> Build release APK after all tests pass.

**Tasks:**
1. Version bump (`versionCode`, `versionName`) in `app/build.gradle.kts`
2. Verify `proguard-rules.pro` keeps: Retrofit interfaces, `@Serializable` DTOs, Room entities, Firebase, Hilt, Coil interceptors
3. Check signing config exists
4. Build: `./gradlew assembleRelease`
5. Build bundle: `./gradlew bundleRelease`
6. Test release APK on physical device
7. Final smoke test: install → register → scan → identify → browse → chat → story → dashboard

**Output:** `app/build/outputs/apk/release/app-release.apk`

---

## File Reference Map

### Key files to modify per phase:

| Phase | Primary Files |
|-------|---------------|
| P0 | `core/network/model/LandmarkModels.kt`, `core/domain/model/Landmark.kt`, `core/data/repository/ExploreRepositoryImpl.kt`, `core/network/AuthInterceptor.kt` (429 handler), `feature/dictionary/WriteViewModel.kt` (smart default), `feature/dictionary/screen/WriteTab.kt` (remove mode chips) |
| P1 | `core/designsystem/` (add placeholder drawables), all `AsyncImage` call sites, `WadjetApplication.kt` |
| P2 | `feature/explore/screen/IdentifyScreen.kt`, `feature/explore/IdentifyViewModel.kt` |
| P3 | `app/navigation/TopLevelDestination.kt`, `app/navigation/WadjetNavHost.kt`, `app/navigation/Route.kt`, new HieroglyphsHubScreen |
| P4 | `feature/scan/screen/ScanResultScreen.kt` (enhance metadata) |
| P5 | `feature/explore/screen/LandmarkDetailScreen.kt`, `feature/explore/screen/ExploreScreen.kt` |
| P6 | `feature/stories/screen/StoriesScreen.kt`, `feature/stories/screen/StoryReaderScreen.kt`, TTS service |
| P7 | `feature/chat/ChatViewModel.kt`, all screens (loading/error/empty states), `feature/explore/` + `feature/stories/` + `feature/dictionary/` (favorite buttons) |
| P8 | Test on device |
| P9 | `app/build.gradle.kts`, `proguard-rules.pro`, signing config |

---

## Priority Order

1. **Phase 0** → Phase 1 → Phase 2 (critical data + visual fixes)
2. **Phase 3** (navigation restructure)
3. **Phase 4** → Phase 5 → Phase 6 (feature quality)
4. **Phase 7** (chat + polish + favorites + progress sync)
5. **Phase 8** → **Phase 9** (test & build)

Total: 10 phases (0-9), each a focused session.
