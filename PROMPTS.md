# Wadjet Android — Phase Prompts

> Copy-paste each prompt when starting that phase. Each prompt is self-contained with full context.
> **Web app reference:** `D:\Personal attachements\Projects\Wadjet-v3-beta`
> **Android workspace:** `D:\Personal attachements\Projects\Wadjet-Android`
> **PLAN file:** `D:\Personal attachements\Projects\Wadjet-Android\PLAN.md`
>
> **DESIGN PRINCIPLE — SMART DEFAULTS:**
> The user NEVER chooses modes. The app always picks the best option automatically:
> - Scan: Always `mode=auto` (backend optimal pipeline). No mode selector.
> - Write: Always `mode=smart` (AI English→hieroglyphs). No mode selector.
> - TTS: Server picks best provider. No voice picker.
> - Identify: Already auto (ensemble). No change.

---

## Phase 0 Prompt: API Contract Alignment + Critical Fixes

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
The web backend is at "D:\Personal attachements\Projects\Wadjet-v3-beta".
Read PLAN.md Phase 0 for the full task list with exact code snippets.

TASK: Fix all DTO mismatches between the Android app and the web API.

CRITICAL BUG — IdentifyResponse DTO (root cause of "pyramids → White Desert"):
File: core/network/model/LandmarkModels.kt

CURRENT (WRONG):
  IdentifyResponse has `landmark: LandmarkDetailDto? = null` — web NEVER returns this field.
  MISSING fields: source, agreement, description, is_known_landmark, is_egyptian
  IdentifyMatchDto is missing `source` field.

FIX TO:
  Remove `landmark` field. Add: source (String?), agreement (String?), description (String?),
  isKnownLandmark (Boolean, @SerialName("is_known_landmark")),
  isEgyptian (Boolean, @SerialName("is_egyptian")).
  Add `source: String?` to IdentifyMatchDto.

Then fix the chain:
1. Domain model `IdentifyResult` in core/domain/model/Landmark.kt — add same fields, remove `detail: LandmarkDetail?`
2. Domain model `IdentifyMatch` — add `source: String?`
3. Mapping in core/data/repository/ExploreRepositoryImpl.kt `identifyLandmark()` — map new fields
4. Fix ScanResultSerializable.toDomain() in ScanRepositoryImpl.kt — it currently drops `confidenceSummary`
5. Fix chat landmark slug extraction in feature/chat/ChatViewModel.kt — verify `savedStateHandle.get<String>("slug")` works with type-safe nav
6. Fix story TTS: pass chapter.ttsVoice and chapter.ttsStyle to SpeakRequest in story reader
7. Cross-check ALL other DTOs (ScanModels verified correct, but double-check StoryModels, ChatModels, WriteModels)

SMART DEFAULTS (critical design decision):
8. WriteViewModel.kt: Remove mode selection state. Hardcode mode = "smart" in write() call.
9. WriteTab.kt: Remove the FilterChip mode selector row entirely. User types English, gets hieroglyphs.
10. Verify ScanViewModel always sends mode = "auto" with no UI to change it.

429 RATE LIMIT HANDLING:
11. Add a RateLimitInterceptor (or add to AuthInterceptor) that handles HTTP 429:
    - Parse Retry-After header, show "Please wait X seconds" Snackbar
    - On login lockout 429: show "Account locked for 15 minutes"
    - Add exponential backoff for 503/timeout (3 retries, 1s/2s/4s)

FREE TIER LIMITS:
12. Wire GET /api/user/limits in relevant ViewModels:
    - ScanViewModel.submitScan(): Check usage.scans >= limits.scans_per_day before scanning
    - ChatViewModel.sendMessage(): Check chat_messages_per_day before sending
    - Show subtle usage badge on Home screen: "3/10 scans today"
    - The DTO + endpoint already exist (UserLimitsResponse) — just wire them to ViewModels

13. Build the project — zero compile errors

Read the web endpoint at D:\Personal attachements\Projects\Wadjet-v3-beta\app\api\explore.py to verify exact response format.
Don't add features beyond what's listed — fix data contracts + smart defaults + rate limits.
```

---

## Phase 1 Prompt: Image Loading & Placeholder System

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Read PLAN.md Phase 1 for the full task list.

TASK: Fix all image loading issues. Every image shows content or a themed placeholder. Zero blank spaces.

Steps:
1. Create 4 Egyptian-themed vector placeholder drawables in core/designsystem/src/main/res/drawable/:
   - ic_placeholder_landmark.xml — pyramid/temple silhouette
   - ic_placeholder_glyph.xml — eye of horus silhouette
   - ic_placeholder_story.xml — scroll/papyrus silhouette
   - ic_placeholder_error.xml — broken-image icon

2. Search ALL files for "AsyncImage" — fix EVERY one with:
   - placeholder = painterResource(R.drawable.ic_placeholder_*)
   - error = painterResource(R.drawable.ic_placeholder_error)
   - fallback = painterResource(R.drawable.ic_placeholder_*)
   Locations: ExploreScreen LandmarkCard, LandmarkDetailScreen carousel+thumbnails,
   IdentifyScreen match results, StoriesScreen story cards, LandingScreen continue cards,
   StoryReaderScreen scene images.

3. Fix BaseUrlInterceptor in WadjetApplication.kt — handle relative paths without leading "/":
   if path doesn't start with "http" or "file:", prepend "/" if missing, then prepend baseUrl

4. Story cover visual: use coverGlyph large on gradient per difficulty:
   Beginner → gold/amber, Intermediate → blue/teal, Advanced → purple/indigo

Use vector drawables only (XML), no PNGs. Follow existing designsystem patterns.
```

---

## Phase 2 Prompt: Identify Feature Logic Parity

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Reference web app: "D:\Personal attachements\Projects\Wadjet-v3-beta"
Read PLAN.md Phase 2 for the full task list.

TASK: Make the landmark identify feature show FULL ensemble results matching web quality.

PREREQUISITE: Phase 0 must be done (DTOs fixed with source, agreement, description, etc.)

Steps:
1. Read web identify implementation: D:\Personal attachements\Projects\Wadjet-v3-beta\app\api\explore.py
2. Read web ensemble logic: D:\Personal attachements\Projects\Wadjet-v3-beta\app\core\ensemble.py
3. Read current feature/explore/screen/IdentifyScreen.kt and IdentifyViewModel.kt
4. Rewrite IdentifyResults composable to show:
   a. Top match with LARGE confidence badge (green ≥80%, yellow ≥50%, red <50%)
   b. Source chip: "Ensemble", "Gemini", "ONNX", "Grok" (from result.source)
   c. Agreement badge: "Verified ✓" (full), "Consensus" (partial), "Uncertain" (tiebreak), "Single Source" (single)
   d. If isEgyptian == false → amber warning banner "This doesn't appear to be an Egyptian landmark"
   e. If isKnownLandmark == false → info banner "Not in our known landmarks database"
   f. Description text card
   g. Top-3 alternatives with confidence progress bars + source per match
   h. "View Full Details" → LandmarkDetail(slug)
   i. "Ask Thoth about this" → ChatLandmark(slug)
5. Auto-fetch landmark detail when top match ≥60% confidence AND isKnownLandmark
6. Add "Identify Another" button — reset state to upload mode
7. Build and verify no errors

Follow Material 3 design. Use existing WadjetTheme colors.
```

---

## Phase 3 Prompt: Navigation Restructure

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Reference web app: "D:\Personal attachements\Projects\Wadjet-v3-beta"
Read PLAN.md Phase 3 for the full task list.

TASK: Restructure navigation to match web app's information architecture.

CURRENT Android bottom nav: Home | Scan | Explore | Stories | Profile
WEB nav structure: Hieroglyphs (Scan+Dictionary+Write) | Explore | Stories | Thoth | [User menu]

NEW bottom nav (5 tabs):
1. Home → Route.Landing (Home icon)
2. Hieroglyphs → NEW Route.Hieroglyphs (Egyptian eye icon)
3. Explore → Route.Explore (Compass icon)
4. Stories → Route.Stories (Book icon)
5. Thoth → Route.Chat (Chat icon)

Dashboard/Profile → user avatar icon in top app bar

Steps:
1. Read: app/navigation/TopLevelDestination.kt, Route.kt, WadjetNavHost.kt
2. Read web: D:\Personal attachements\Projects\Wadjet-v3-beta\app\templates\partials\nav.html
3. Update TopLevelDestination.kt to the 5 tabs above
4. Create Route.Hieroglyphs
5. Create HieroglyphsHubScreen — 3 feature cards:
   - Scan: "Detect & translate hieroglyphs from photos" → Route.Scan
   - Dictionary: "Browse 1,000+ Gardiner signs" → Route.Dictionary
   - Write: "Type English, get hieroglyphs" → Route.Dictionary(initialTab=2)
     (Smart default: no mode selection. User types English, AI converts automatically.)
   - "How Scanning Works" explainer section (Upload → Detect → Translate)
6. Fix "Write" navigation: Add initialTab param to Route.Dictionary. DictionaryScreen reads it to set pager initial page.
7. Add Route.Dashboard accessible from avatar in top app bar (not bottom nav)
8. Update WadjetNavHost.kt with new routes
9. Add user avatar to top app bar in WadjetApp.kt
10. Build and verify

IMPORTANT: WriteTab is ALREADY IMPLEMENTED but has a mode selector (alpha/smart/mdc chips).
Phase 0 removes the mode selector and hardcodes smart mode. Phase 3 just fixes navigation.
```

---

## Phase 4 Prompt: Scan Feature Quality

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Reference web app: "D:\Personal attachements\Projects\Wadjet-v3-beta"
Read PLAN.md Phase 4 for the full task list.

TASK: Match web's scan result quality. SMART DEFAULT = always auto mode, NO mode picker UI.

Steps:
1. Read web scan: D:\Personal attachements\Projects\Wadjet-v3-beta\app\api\scan.py (especially _scan_auto_mode)
2. Read Android: ScanScreen.kt, ScanResultScreen.kt, ScanViewModel.kt, ScanResultViewModel.kt
3. VERIFY scan always sends mode="auto" — no mode picker in UI (smart default)
4. Enhance ScanResultScreen metadata:
   a. Pipeline source: "Detected by: AI Vision (Gemini)" or "ONNX + AI Verified"
   b. Confidence summary card with color-coded bars
   c. Quality hints section (blurry, dark, small)
   d. AI notes section (collapsible) from aiReading.notes
   e. AI verified/unverified badge
5. Make annotated image zoomable (detectTransformGestures: pinch + pan)
6. Make detected glyphs tappable → bottom sheet:
   Gardiner code, unicode, transliteration, meaning, confidence, "View in Dictionary"
7. TTS "Read aloud" for full transliteration
8. "Scan Another" button without back navigation
9. Build and verify
```

---

## Phase 5 Prompt: Landmark Detail & Explore

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Reference web app: "D:\Personal attachements\Projects\Wadjet-v3-beta"
Read PLAN.md Phase 5 for the full task list.

TASK: Full landmark detail parity with web.

Steps:
1. Read web: D:\Personal attachements\Projects\Wadjet-v3-beta\app\api\explore.py (get_landmark)
2. Read web: D:\Personal attachements\Projects\Wadjet-v3-beta\app\core\recommendation_engine.py
3. Read Android: LandmarkDetailScreen.kt, DetailViewModel.kt, LandmarkModels.kt
4. Verify LandmarkDetailScreen renders ALL sections:
   - Image carousel with Ken Burns, highlights, historical significance, visiting tips
   - Dynasty/era/notable pharaohs, Wikipedia link, Google Maps button
   - Recommendations as "Similar Sites" horizontal scrollable LandmarkCards
   - Children sub-sites (tappable), tags as chips, "Ask Thoth" FAB
5. Add featured landmarks carousel at top of ExploreScreen
6. Category + city filters as scrollable chip rows
7. Search with debounce (verify existing implementation works)
8. Pull-to-refresh on ExploreScreen
9. Build and verify
```

---

## Phase 6 Prompt: Stories & TTS

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Reference web app: "D:\Personal attachements\Projects\Wadjet-v3-beta"
Read PLAN.md Phase 6 for the full task list.

TASK: Stories and audio at full quality.

Steps:
1. Read web: D:\Personal attachements\Projects\Wadjet-v3-beta\app\core\tts_service.py (10 voice presets)
2. Read web: D:\Personal attachements\Projects\Wadjet-v3-beta\app\api\audio.py
3. Read web: D:\Personal attachements\Projects\Wadjet-v3-beta\app\core\image_service.py
4. Read Android: StoriesScreen.kt, StoryReaderScreen.kt, StoriesViewModel.kt, StoryReaderViewModel.kt
5. Story cards: coverGlyph on difficulty-themed gradient, difficulty badge, chapter count
6. Fix TTS voice/style passthrough:
   - StoryReaderViewModel.speakChapter() must pass chapter.ttsVoice + chapter.ttsStyle to SpeakRequest
   - Context = "story_narration"
7. Scene image generation: POST /api/stories/{id}/chapters/{idx}/image → shimmer loading → display
8. Auto-narration mode: sequential paragraph TTS, highlight current paragraph
9. Verify all 4 interaction types: choose_glyph, write_word, glyph_discovery, story_decision
10. Fix LOCAL_TTS language: detect Arabic text → Locale("ar") instead of hardcoded Locale.US
11. Build and verify
```

---

## Phase 7 Prompt: Chat & Polish

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Reference web app: "D:\Personal attachements\Projects\Wadjet-v3-beta"
Read PLAN.md Phase 7 for the full task list.

TASK: Chat quality + favorites parity + story progress sync + loading/error/empty states + polish.

CHAT:
1. Read web: D:\Personal attachements\Projects\Wadjet-v3-beta\app\core\thoth_chat.py
2. Read Android: ChatScreen.kt, ChatViewModel.kt
3. Fix/verify ChatLandmark slug extraction from savedStateHandle
4. Verify markdown renders: bold, italic, bullets, tables, blockquotes, hieroglyph unicode
5. SSE robustness: mid-stream errors → partial message + error indicator
6. Landmark name in chat header for ChatLandmark mode

FAVORITES PARITY (web supports favorite landmarks, glyphs, AND stories):
7. Add ♡ toggle button on Dictionary sign detail sheet → POST /api/user/favorites type=glyph
8. Add ♡ toggle button on Story cards → POST /api/user/favorites type=story
9. Verify landmark ♡ toggle already works in ExploreScreen
10. All 3 types should appear in Dashboard favorites tab

STORY PROGRESS SYNC:
11. Add POST /api/user/progress call in StoriesRepositoryImpl.saveProgress()
    Keep Firestore as secondary sync, but REST API is primary
    Sync: chapter_index, glyphs_learned, score, completed

POLISH:
12. Loading states — shimmer for: landmark grid, dictionary list, story list
13. Error states — contextual: "Couldn't read hieroglyphs", "Couldn't identify", "Check connection", "Daily limit reached"
14. Empty states: "Your first scan awaits", "Tap ♡ on landmarks", "No matches found"
15. Pull-to-refresh: ExploreScreen, StoriesScreen, DashboardScreen
16. Haptic feedback: button taps, scan complete, quiz answer correct
17. Offline banner via NetworkMonitor
18. Accessibility: add contentDescription to ALL images, icons, interactive elements
19. Build and verify
```

---

## Phase 8 Prompt: Testing & Validation

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Read PLAN.md Phase 8 for the complete test checklist.

TASK: Comprehensive testing of EVERY feature before APK build.

Run the app and test systematically:

AUTH: Register, login, Google Sign-In, token refresh (30+ min), logout/re-login

SCAN: Upload hieroglyph image, all 3 modes, full metadata, tappable glyphs, TTS, history

IDENTIFY:
- Upload pyramids → "Great Pyramids of Giza" NOT "White Desert"
- Upload Sphinx → "Great Sphinx"
- Upload non-Egyptian → "not Egyptian" indicator
- Shows: confidence, source, agreement, description
- "View Details" + "Ask Thoth" buttons work

DICTIONARY: Browse, search, sign detail, all 5 lessons, TTS

WRITE: Alpha mode, MdC mode, smart mode, palette, copy, TTS

EXPLORE: All cards show images, filters, search, detail all sections, maps, recommendations, "Ask Thoth"

STORIES: Covers, chapters, scene images, all 4 interactions, TTS narration, bilingual

CHAT: SSE streaming, markdown, voice in/out, landmark context, clear, history

DASHBOARD: Profile, stats, scan history, favorites, story progress

OFFLINE: Dictionary FTS, cached landmarks, offline banner, error messages

Fix any issues found.
```

---

## Phase 9 Prompt: APK Build

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Read PLAN.md Phase 9 for the full task list.

TASK: Build release APK after all tests pass.

Steps:
1. Read app/build.gradle.kts — check versionCode and versionName
2. Bump version if needed
3. Verify proguard-rules.pro keeps:
   - @Serializable DTOs (kotlinx.serialization)
   - Retrofit interfaces
   - Room entities + DAOs
   - Firebase, Hilt, Coil interceptors, OkHttp
4. Check signing config for release
5. If no keystore exists, tell me — I'll create one manually
6. Build: ./gradlew assembleRelease
7. Build bundle: ./gradlew bundleRelease
8. Report APK size
9. Install release APK on device → final smoke test:
   install → register → scan → identify → browse → chat → story → dashboard

Do NOT create a keystore via command.
Output: app/build/outputs/apk/release/app-release.apk
```
