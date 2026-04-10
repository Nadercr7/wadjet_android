# Phase Prompts — Copy-Paste Ready

> Each prompt is self-contained. Copy-paste when starting that phase.
> References: constitution.md, architecture.md, api-contract.md, gap-analysis.md, tasks.md
>
> **DESIGN PRINCIPLE — SMART DEFAULTS:**
> The user NEVER chooses modes. Scan=auto, Write=smart, TTS=server-picks, Identify=auto.

---

## Phase 0: API Contract Alignment + Critical Fixes

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Web backend at "D:\Personal attachements\Projects\Wadjet-v3-beta".
Spec files at .specify/specs/001-logic-parity/ (read constitution.md, api-contract.md, gap-analysis.md, tasks.md).

TASK: Fix all DTO mismatches, enforce smart defaults, add rate-limit handling, wire free-tier limits.
This fixes: B1, B2, B3, G10, M2, M5, A1, A4, A10.

CRITICAL BUG (root cause of "pyramids → White Desert"):
File: core/network/model/LandmarkModels.kt → IdentifyResponse
- Remove: `landmark: LandmarkDetailDto? = null` (web never returns this)
- Add: source (String?), agreement (String?), description (String?),
  @SerialName("is_known_landmark") isKnownLandmark (Boolean),
  @SerialName("is_egyptian") isEgyptian (Boolean)
- Add `source: String?` to IdentifyMatchDto

Fix chain (all layers):
1. IdentifyResult domain model (core/domain/model/Landmark.kt) — add fields, remove `detail`
2. IdentifyMatch — add `source: String?`
3. ExploreRepositoryImpl.identifyLandmark() — map new fields
4. ScanResultSerializable.toDomain() — preserve confidenceSummary
5. ChatViewModel slug extraction — verify savedStateHandle with type-safe nav
6. StoryReaderViewModel.speakChapter() — ALREADY FIXED (passes voice+style). VERIFY only, don't change.
7. Cross-check ALL other DTOs against .specify/specs/001-logic-parity/contracts/api-contract.md

SMART DEFAULTS:
8. WriteViewModel.kt: Remove _selectedMode state. Hardcode mode = "smart"
9. WriteTab.kt: Remove FilterChip mode selector row entirely
10. Verify ScanViewModel sends mode = "auto" with no UI picker

429 RATE LIMITS:
11. Add RateLimitInterceptor: parse Retry-After, show "Please wait X seconds",
    login lockout: "Account locked for 15 minutes", exponential backoff (1s/2s/4s)

FREE TIER:
12. Wire GET /api/user/limits (DTO already exists):
    ScanViewModel: check usage.scans >= limits before scanning
    ChatViewModel: check chat_messages_per_day before sending
    Show "3/10 scans today" badge on Home

NEWLY DISCOVERED FIXES:
13. Add RECORD_AUDIO permission to app/src/main/AndroidManifest.xml (ChatScreen STT needs it)
14. Add composable<Route.DictionarySign> handler in WadjetNavGraph.kt (route defined but no screen handler)
15. Add chatMessagesToday field to UsageDto in core/network/model/UserModels.kt

16. Build — zero compile errors.
```

---

## Phase 1: Image Loading & Placeholder System

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Read .specify/specs/001-logic-parity/tasks.md Phase 1.

TASK: Fix all image loading. Every image shows content or themed placeholder. Zero blank spaces.
Fixes: B4, M1, M3.

1. Create 4 vector drawables in core/designsystem/src/main/res/drawable/:
   ic_placeholder_landmark.xml — pyramid silhouette
   ic_placeholder_glyph.xml — Eye of Horus
   ic_placeholder_story.xml — papyrus scroll
   ic_placeholder_error.xml — broken image icon

2. grep "AsyncImage" across ALL files. Fix EVERY one with:
   placeholder = painterResource(R.drawable.ic_placeholder_*)
   error = painterResource(R.drawable.ic_placeholder_error)
   fallback = painterResource(R.drawable.ic_placeholder_*)

3. Fix BaseUrlInterceptor — handle paths without leading "/":
   if (!data.startsWith("http") && !data.startsWith("file:"))
     prepend "/" if missing, then prepend baseUrl

4. Story covers: coverGlyph on gradient (Beginner=gold, Intermediate=blue, Advanced=purple)

Vector XML only (no PNGs). Follow existing designsystem patterns.
```

---

## Phase 2: Identify Feature Logic Parity

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Web backend: "D:\Personal attachements\Projects\Wadjet-v3-beta".
Read .specify/specs/001-logic-parity/tasks.md Phase 2.
PREREQUISITE: Phase 0 done (DTOs fixed).

TASK: Identify shows FULL ensemble result matching web. Fixes: G5, M7.

1. Read web: app/api/explore.py, app/core/ensemble.py
2. Read: feature/explore/screen/IdentifyScreen.kt, IdentifyViewModel.kt
3. Rewrite IdentifyResults composable:
   a. Top match — LARGE confidence badge (green ≥80%, yellow ≥50%, red <50%)
   b. Source chip: "Ensemble" / "Gemini" / "ONNX" / "Grok"
   c. Agreement badge: "Verified ✓" (full) / "Consensus" (partial) / "Uncertain" (tiebreak) / "Single Source"
   d. isEgyptian==false → amber warning
   e. isKnownLandmark==false → info banner
   f. Description text card
   g. Top-3 with confidence progress bars + source per match
   h. "View Full Details" → LandmarkDetail(slug)
   i. "Ask Thoth" → ChatLandmark(slug)
4. Auto-fetch detail when top match ≥60% + isKnownLandmark
5. "Identify Another" button (reset to upload)
6. Build — no errors

Use Material 3, WadjetTheme colors.
```

---

## Phase 3: Navigation Restructure

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Web backend: "D:\Personal attachements\Projects\Wadjet-v3-beta".
Read .specify/specs/001-logic-parity/tasks.md Phase 3.

TASK: Match web's navigation structure. Fixes: G1, G2, G3, B5.

CURRENT bottom nav: Home | Scan | Explore | Stories | Profile
NEW bottom nav: Home | Hieroglyphs | Explore | Stories | Thoth
Dashboard → user avatar in top app bar

1. Read: TopLevelDestination.kt, Route.kt, WadjetNavHost.kt
2. Read web: app/templates/partials/nav.html
3. Update TopLevelDestination to 5 new tabs
4. Create Route.Hieroglyphs + HieroglyphsHubScreen:
   - Scan card: "Detect & translate hieroglyphs" → Route.Scan
   - Dictionary card: "Browse 1,000+ signs" → Route.Dictionary
   - Write card: "Type English, get hieroglyphs" → Route.Dictionary(initialTab=2)
   - "How Scanning Works" explainer
5. Add initialTab param to Route.Dictionary. DictionaryScreen reads it.
6. Dashboard accessible from avatar (not bottom nav)
7. Build — no errors

WriteTab mode selector already removed in Phase 0. Phase 3 only fixes navigation.
```

---

## Phase 4: Scan Feature Quality

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Read .specify/specs/001-logic-parity/tasks.md Phase 4 + api-contract.md Scan section.

TASK: Scan result quality matching web. Smart default = always auto. Fixes: G9.

1. Read web: app/api/scan.py (especially _scan_auto_mode)
2. Read: ScanScreen.kt, ScanResultScreen.kt, ScanViewModel.kt
3. VERIFY scan sends mode="auto" — NO mode picker
4. Enhance ScanResultScreen:
   - Pipeline source: "Detected by: AI Vision (Gemini)" / "ONNX + AI Verified"
   - Confidence summary with color bars
   - Quality hints + AI notes (collapsible)
5. Zoomable annotated image (detectTransformGestures: pinch + pan)
6. Tappable glyphs → bottom sheet: Gardiner code, unicode, meaning, confidence, "View in Dictionary"
7. TTS "Read aloud" for transliteration
8. "Scan Another" button
9. Build — no errors
```

---

## Phase 5: Landmark Detail & Explore

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Read .specify/specs/001-logic-parity/tasks.md Phase 5 + api-contract.md Explore section.

TASK: Full landmark detail parity. Fixes: G4.

1. Read web: app/api/explore.py (get_landmark), app/core/recommendation_engine.py
2. Read: LandmarkDetailScreen.kt, DetailViewModel.kt, LandmarkModels.kt
3. Verify ALL detail sections render (carousel, highlights, tips, dynasty, pharaohs, Wikipedia, maps, recommendations, children, "Ask Thoth")
4. Featured landmarks carousel at top of ExploreScreen
5. Category + city filter chips (scrollable)
6. Verify search debounce
7. Pull-to-refresh
8. Build — no errors
```

---

## Phase 6: Stories & TTS

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Read .specify/specs/001-logic-parity/tasks.md Phase 6 + api-contract.md Stories/Audio sections.

TASK: Stories and audio at full quality. Fixes: G6, M4.

1. Read web: app/core/tts_service.py, app/api/audio.py, app/core/image_service.py
2. Read: StoriesScreen.kt, StoryReaderScreen.kt, viewmodels
3. Story cards: coverGlyph on gradient, difficulty badge, chapter count
4. Verify TTS voice/style (fixed Phase 0)
5. Scene images: shimmer → display with Ken Burns
6. Auto-narration: sequential paragraph TTS, highlight current
7. Verify 4 interaction types: choose_glyph, write_word, glyph_discovery, story_decision
8. Fix LOCAL_TTS language: detect Arabic → Locale("ar") instead of Locale.US
9. Build — no errors
```

---

## Phase 7: Chat & Polish

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Read .specify/specs/001-logic-parity/tasks.md Phase 7.

TASK: Chat quality + favorites + progress sync + polish. Fixes: M6, M8, A5, A7, A9.

CHAT: Verify slug, markdown, SSE robustness, landmark header
FAVORITES: Add ♡ toggle on glyph detail + story cards (POST /api/user/favorites type=glyph|story)
PROGRESS: Wire POST /api/user/progress in StoriesRepositoryImpl
POLISH:
- Shimmer loading (explore, dictionary, stories)
- Contextual errors ("Couldn't read hieroglyphs", "Check connection", "Daily limit reached")
- Empty states ("Your first scan awaits", "Tap ♡ on landmarks", "No matches found")
- Pull-to-refresh (Explore, Stories, Dashboard)
- Haptic feedback (button taps, scan complete, quiz correct)
- Offline banner via NetworkMonitor
- Accessibility: contentDescription on ALL images, icons, interactive elements
- Persist TTS settings (enabled, speed) to DataStore — currently in-memory only, resets on restart
Build — no errors.
```

---

## Phase 8: Testing & Validation

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Read .specify/specs/001-logic-parity/tasks.md Phase 8 for the test matrix.

TASK: Test EVERY feature. Fix anything broken.

SMART DEFAULT CHECKS:
- Scan: NO mode selector visible, sends auto
- Write: NO mode selector visible, sends smart
- TTS: No voice picker

KEY VERIFICATION:
- Pyramids → "Great Pyramids of Giza" (NOT White Desert)
- Sphinx → "Great Sphinx"
- Non-Egyptian → warning indicator
- Every image has placeholder, never blank
- Rate limit → friendly message
- Offline → dictionary FTS works, banner shows

Run all test scenarios from the Phase 8 checklist.
Fix any issues found.
```

---

## Phase 9: APK Build

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".

TASK: Build release APK.

1. Version bump in app/build.gradle.kts
2. Verify proguard-rules.pro keeps: @Serializable DTOs, Retrofit, Room, Firebase, Hilt, Coil, OkHttp
3. Check signing config
4. ./gradlew assembleRelease
5. ./gradlew bundleRelease
6. Test release APK on device
7. Smoke test: install → register → scan → identify → browse → chat → story → dashboard
8. Enable Room schema export + proper migrations (replace fallbackToDestructiveMigration)
9. Configure backup_rules.xml to exclude EncryptedSharedPreferences tokens

Do NOT create a keystore via command — I'll do that manually if needed.
```
