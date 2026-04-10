# Wadjet Android — UX Overhaul Plan

> Complete redesign to match Wadjet v3 Beta web app identity.
> Reference: `D:\Personal attachements\Projects\Wadjet-v3-beta\`

---

## Executive Summary

The Android app has solid architecture (Compose, Hilt, Room, Retrofit, ONNX) and correct color tokens, typography, and base components. However, it deviates from the web app's identity in several critical ways:

1. **Emojis instead of hieroglyphs** — 6 screens use standard emojis (🏛📷📖🧭📜) where the web app exclusively uses Unicode Egyptian Hieroglyphs
2. **Camera-first scan/identify** — Uses CameraX live preview; user wants image-upload-only (no camera)
3. **Weak loading states** — Generic `CircularProgressIndicator` instead of branded Wadjet loader (logo + shimmer)
4. **Underdeveloped landing page** — 2 plain cards + 4 emoji icons vs web's rich dual-path hero layout
5. **Missing animations** — Only 4 of 20+ web animations ported to Compose
6. **No chat history panel** — Web has past conversations sidebar; Android has none
7. **Minor color mismatch** — Dust `#A89070` vs web's `#8B7355`

Additionally, the app **silently drops** several API fields (AI notes, quality hints, confidence summary, sources) and has **zero TTS on scan results**, **no chat history**, and **incomplete story interactions**.

This plan addresses every gap across 6 phases, ending with APK release.

---

## Analysis: Emoji → Hieroglyph Mapping

The web app uses ZERO emojis. All decorative glyphs are Unicode Egyptian Hieroglyphs rendered in Noto Sans Egyptian Hieroglyphs font. The Android app must follow the same rule.

| Current (Android) | Replace With | Unicode | Meaning |
|---|---|---|---|
| `🏛` (Explore/Landmarks) | `𓉐` | U+13250 | House/Building |
| `📷` (Scan quick-action) | `𓂀` | U+13080 | Eye of Horus |
| `📖` (Dictionary quick-action) | `𓊹` | U+132B9 | Cartouche/Scroll |
| `🧭` (Explore quick-action) | `𓇯` | U+131EF | Sky/Horizon |
| `📜` (Stories quick-action + placeholder) | `𓁟` | U+1305F | Thoth Head |
| `🏛` (WelcomeScreen feature card) | `𓉐` | U+13250 | House/Building |

**Rule**: Every decorative element must use a glyph from the Noto Sans Egyptian Hieroglyphs font. Standard emojis are banned from all UI surfaces.

---

## Analysis: Camera → Image Upload

User requirement: **No camera for now — image upload only** for both Scan and Landmark Identify.

| Screen | Current | Target |
|---|---|---|
| `ScanScreen` | CameraX live preview + gallery picker | Image upload zone (gold dashed border, tap to pick from gallery) |
| `IdentifyScreen` | CameraX live preview + gallery picker | Image upload zone (same pattern as ScanScreen) |
| `ExploreScreen` FAB | Camera icon → IdentifyScreen | Upload icon → IdentifyScreen (image upload) |

CameraX dependency, permission requests, and camera preview code will be **commented out** (not deleted) so camera can be restored later.

---

## Phase 1: Identity Alignment (Design System + Emojis)

**Goal**: Fix every identity deviation so the app *feels* like the web app.

### 1.1 Fix Dust Color
- **File**: `WadjetColors.kt`
- **Change**: `Dust = Color(0xFFA89070)` → `Dust = Color(0xFF8B7355)`

### 1.2 Replace All Emojis with Hieroglyphs
- **File**: `WelcomeScreen.kt`
  - Feature card `"🏛"` → `"𓉐"` with `WadjetFonts.Hieroglyph` fontFamily
- **File**: `LandingScreen.kt`
  - Path card `"🏛"` → `"𓉐"`
  - Quick-action cards: `"📷"` → `"𓂀"`, `"📖"` → `"𓊹"`, `"🧭"` → `"𓇯"`, `"📜"` → `"𓁟"`
  - All rendered with `WadjetFonts.Hieroglyph` fontFamily at appropriate sizes
- **File**: `StoryReaderScreen.kt`
  - Placeholder `"📜"` → `"𓁟"`
- **Audit**: grep entire codebase for remaining emoji usage and replace

### 1.3 Add Missing Animation Components
Port these web CSS animations to Compose:

| Web Animation | Compose Target | File to Create |
|---|---|---|
| `border-beam` | `BorderBeam` modifier — animated Arc stroke orbiting around a card border | `animation/BorderBeam.kt` |
| `meteor` | `MeteorShower` composable — diagonal falling gold streaks over a surface | `animation/MeteorShower.kt` |
| `dot-pattern` | `DotPattern` modifier/composable — repeating gold dot grid at low opacity | `animation/DotPattern.kt` |
| `btn-shimmer` | `ButtonShimmer` modifier — perimeter light sweep on gold buttons | `animation/ButtonShimmer.kt` |
| `shine` | `ShineSweep` modifier — diagonal shine line sweeping across a surface | `animation/ShineSweep.kt` |
| `gradient-sweep` | Already have `GoldGradientText` — extend to `Modifier.goldGradientSweep()` for backgrounds | `animation/GoldGradientSweep.kt` |

### 1.4 Branded Loading Components
Create branded loading states matching the web app:

| Component | Description | File |
|---|---|---|
| `WadjetFullLoader` | Full-screen: Wadjet logo (pulsing scale 0.95→1.05) + "WADJET" gold gradient text + thin shimmer progress bar below | `component/WadjetFullLoader.kt` |
| `WadjetSectionLoader` | Inline: small logo (32dp, pulsing alpha) + descriptive text ("Loading dictionary...") | `component/WadjetSectionLoader.kt` |
| `WadjetButtonLoader` | Inside-button spinner: small gold `CircularProgressIndicator(16dp, strokeWidth=2dp)` replacing button text | Update `WadjetButton.kt` |
| `StreamingDots` | 3 gold dots with staggered scale animation (for chat streaming) | `component/StreamingDots.kt` |
| `WadjetShimmer` | Gold-tinted shimmer (not gray) — brush colors: `Surface → GoldMuted.copy(0.15f) → Surface` | Update `ShimmerEffect.kt` |

### 1.5 Toast System
Replace default Material Snackbar with Wadjet-branded toast:
- Fixed bottom-center (above bottom nav)
- Variants: success (green left-border), error (red left-border), info (gold left-border)
- Surface background, icon + message, auto-dismiss 3s with fade animation
- **File**: `component/WadjetToast.kt`

**Estimated screens touched**: 8 files
**Risk**: Low — purely visual, no logic changes

---

## Phase 2: Camera → Image Upload Conversion

**Goal**: Replace CameraX camera with clean image upload zones.

### 2.1 Create Reusable Upload Component
**File**: `component/ImageUploadZone.kt`

```
┌─────────────────────────────────────┐
│                                     │
│     ╭─ gold dashed border ─╮        │
│     │                      │        │
│     │    𓂀  (48sp gold)    │        │
│     │                      │        │
│     │  "Tap to select an   │        │
│     │   image from gallery" │        │
│     │                      │        │
│     │  [Browse Gallery]     │        │
│     │   (WadjetGhostButton)│        │
│     │                      │        │
│     ╰──────────────────────╯        │
│                                     │
│  Supports: JPG, PNG up to 10MB      │
│  (TextMuted, bodySmall)             │
└─────────────────────────────────────┘
```

- Gold dashed border (`PathEffect.dashPathEffect`) on `Night` background
- After image selected: show preview thumbnail with "Change" overlay and "Analyze" gold button
- Uses `PickVisualMedia` ActivityResultContract (already exists in ScanScreen)

### 2.2 Refactor ScanScreen
- **Remove**: CameraX `PreviewView`, camera permission request, capture button, gold bracket overlay Canvas
- **Add**: `ImageUploadZone` centered on screen, top bar with back+history icons
- **Keep**: Gallery picker contract (repurpose for the upload zone), `ScanProgressOverlay`, error handling
- **Comment out** (not delete): All CameraX code in a `// CAMERA_DISABLED` block for future restoration

### 2.3 Refactor IdentifyScreen
- **Remove**: CameraX preview, capture button
- **Add**: `ImageUploadZone` with text "Upload a photo of an Egyptian landmark"
- **Keep**: Loading overlay, `IdentifyResults` bottom panel, retry

### 2.4 Update ExploreScreen FAB
- **Change**: Camera icon → `ImageVector` upload icon (or Lucide-style upload)
- **Change**: Content description from "Identify landmark" to "Identify from photo"

### 2.5 Remove CameraX Permission
- **File**: `AndroidManifest.xml` — comment out `<uses-permission android:name="android.permission.CAMERA"/>`
- **File**: `ScanScreen.kt` — remove `rememberPermissionState(Manifest.permission.CAMERA)`
- **File**: `IdentifyScreen.kt` — same

**Estimated screens touched**: 3 screens + 1 component + manifest
**Risk**: Medium — CameraX removal is significant but isolated; gallery picker already works

---

## Phase 3: Screen Redesign — Landing & Welcome

**Goal**: Transform the two entry screens to match web app's rich, immersive landing experience.

### 3.1 Redesign LandingScreen (Home Hub)

Current: 2 plain cards + 4 emoji quick-action cards
Target: Rich dual-path layout matching web's landing page

```
┌──────────────────────────────────────┐
│  ┌─ Greeting Section ─────────────┐  │
│  │ "Welcome back, [Name]"          │  │
│  │  𓁹 WADJET (gold gradient text) │  │
│  │  "Decode the Secrets of         │  │
│  │   Ancient Egypt"                │  │
│  └─────────────────────────────────┘  │
│                                       │
│  ┌─ Hieroglyphs Path Card ────────┐  │
│  │ ╔══════════════════════════════╗│  │
│  │ ║  DotPattern background      ║│  │
│  │ ║                              ║│  │
│  │ ║  𓂀  "Hieroglyphs"           ║│  │
│  │ ║  "Decode Ancient Egypt"      ║│  │
│  │ ║                              ║│  │
│  │ ║  • Scan & translate glyphs   ║│  │
│  │ ║  • 1,000+ Gardiner signs     ║│  │
│  │ ║  • Write in hieroglyphs      ║│  │
│  │ ║                              ║│  │
│  │ ║  [Start Scanning] (btn-gold) ║│  │
│  │ ╚══════════════════════════════╝│  │
│  └─────────────────────────────────┘  │
│                                       │
│  ┌─ Landmarks Path Card ──────────┐  │
│  │ ╔══════════════════════════════╗│  │
│  │ ║  DotPattern background      ║│  │
│  │ ║                              ║│  │
│  │ ║  𓉐  "Landmarks"             ║│  │
│  │ ║  "Explore Sites & Monuments" ║│  │
│  │ ║                              ║│  │
│  │ ║  • 260+ heritage sites       ║│  │
│  │ ║  • Identify from photos      ║│  │
│  │ ║  • Expert AI guide           ║│  │
│  │ ║                              ║│  │
│  │ ║  [Start Exploring] (btn-gold)║│  │
│  │ ╚══════════════════════════════╝│  │
│  └─────────────────────────────────┘  │
│                                       │
│  ┌─ Quick Actions (4-col grid) ───┐  │
│  │  𓂀 Scan  │  𓊹 Dictionary      │  │
│  │  𓇯 Explore│  𓁟 Stories         │  │
│  │  (gold icon, Surface card each) │  │
│  └─────────────────────────────────┘  │
│                                       │
│  ┌─ Continue Section (if data) ───┐  │
│  │  Recent scan thumbnail          │  │
│  │  Story in progress              │  │
│  │  (horizontal scroll)            │  │
│  └─────────────────────────────────┘  │
│                                       │
│  ┌─ Thoth Quick Chat ─────────────┐  │
│  │  𓅝 "Ask Thoth anything..."     │  │
│  │  (tappable → navigates to Chat) │  │
│  └─────────────────────────────────┘  │
└──────────────────────────────────────┘
```

**New elements**:
- Greeting with user name (from `UserApiService.getProfile()`)
- Animated gold gradient "WADJET" title
- Path cards with `DotPattern` background, feature bullet lists, `BorderBeam` animation
- Quick actions using hieroglyphs (not emojis) in small `Surface` cards
- "Continue" section showing recent scan / in-progress story (data from dashboard endpoints)
- Thoth chat entry point

**Requires**: `LandingViewModel` (new) to load user name + recent activity

### 3.2 Polish WelcomeScreen

- Replace `🏛` with `𓉐` in feature cards
- Add `MeteorShower` or `DotPattern` background subtlety
- Add `WadjetFullLoader` for post-login transition
- Animate feature cards with staggered `FadeUp`
- Ensure Google Sign-In button has proper loading state (spinner inside button)

**Estimated screens touched**: 2 screens + 1 new ViewModel
**Risk**: Medium — LandingScreen redesign is significant; WelcomeScreen is polish

---

## Phase 4: Screen Redesign — Feature Screens

**Goal**: Polish all feature screens to match web app quality.

### 4.1 Scan Flow Redesign

#### ScanScreen (after Phase 2 upload conversion)
- Add `FadeUp` animation on the `ImageUploadZone`
- After image selected, show preview with `ShineSweep` modifier on the image border
- "Analyze" button with `ButtonShimmer` effect

#### ScanProgressOverlay
Redesign to match web's 4-step pipeline UI:

```
┌─────────────────────────────────────┐
│                                     │
│   [Image preview, dimmed 50%]       │
│                                     │
│   ┌─ Steps Card (Surface) ────────┐ │
│   │  ✓ Detecting glyphs       ███ │ │
│   │  ◉ Classifying signs      ██░ │ │
│   │  ○ Transliterating        ░░░ │ │
│   │  ○ Translating            ░░░ │ │
│   │                               │ │
│   │  "Classifying 12 detected     │ │
│   │   signs against Gardiner..."  │ │
│   │                               │ │
│   │  ───── shimmer bar ─────      │ │
│   └───────────────────────────────┘ │
└─────────────────────────────────────┘
```

- Each step: icon (✓ done, ◉ active with `GoldPulse`, ○ pending) + label + mini progress bar
- Active step has descriptive subtitle text
- Gold shimmer progress bar at bottom
- `animateContentSize` on step transitions

#### ScanResultScreen
- Add TTS button per section (transliteration, EN translation, AR translation) using server `/api/audio/speak` → local fallback
- Add `WadjetBadge` for confidence percentage
- Detected glyphs: change from `LazyRow` of chips to wrapped `FlowRow` grid showing unicode + Gardiner code + name per glyph
- Gold dividers between sections

### 4.2 Dictionary Polish

- Browse tab: Gold-tinted shimmer while loading signs
- Sign detail `ModalBottomSheet`: add TTS pronunciation button, add `FadeUp` on open
- Learn tab: stagger lesson card animations with `FadeUp` + delay per index
- Write tab: add gold cursor/selection highlighting, real-time preview with `animateContentSize`

### 4.3 Explore Polish

#### ExploreScreen
- `LandmarkCard`: add `ShineSweep` on load, `WadjetCardGlow` on press
- FAB: change to upload icon (Phase 2), add `GoldPulse` modifier
- Empty state: branded illustration or large hieroglyph + descriptive text

#### LandmarkDetailScreen
- Hero image: `KenBurnsImage` (already exists — verify it's applied)
- Add `FadeUp` stagger on badges and sections
- Gallery tab: add shimmer placeholders while images load
- Recommendation cards at bottom: `BorderBeam` on featured recommendations

#### IdentifyScreen (after Phase 2 upload conversion)
- Match `ScanScreen` upload zone pattern
- Results panel: `FadeUp` stagger per result card
- Confidence badge with color gradient (green > 80%, gold > 50%, red < 50%)

### 4.4 Chat Screen Enhancement

- Add `StreamingDots` (3 pulsing gold dots) for streaming state — currently no visible streaming indicator beyond disabled input
- Add past conversations drawer/panel:
  ```
  ┌─ Drawer (Surface bg) ──────┐
  │  Past Conversations         │
  │  ─────────────────────────  │
  │  "Great Pyramid questions"  │
  │  "Hieroglyph meanings"      │
  │  "Luxor Temple history"     │
  │  ─────────────────────────  │
  │  [New Chat] (WadjetButton)  │
  └─────────────────────────────┘
  ```
  - Requires: new endpoint or local Room storage for conversation titles
  - Simplification: store conversations locally in Room, derive title from first user message
- Chat input bar: add `BorderBeam` subtle animation when streaming
- Quick suggestion chips above input (first message only):
  ```
  "Tell me about the pyramids"  "What are hieroglyphs?"  "Famous pharaohs"
  ```

### 4.5 Stories Polish

#### StoriesScreen
- Story cards: add `ShineSweep` on the cover glyph area
- Add `FadeUp` stagger on card list
- Premium lock: keep existing gate (Android-specific, web doesn't have it)
- Difficulty badges: match web's color coding (green/yellow/red)

#### StoryReaderScreen
- Scene image placeholder: `"𓁟"` instead of `"📜"`
- Add floating narration FAB (bottom-right, above bottom nav):
  ```
  ┌──────────┐
  │  𓅝  ▶   │  ← Gold circle FAB, Thoth glyph + play/pause icon
  └──────────┘
  ```
  - 3 states: play (▶) → loading (spinner) → pause (⏸)
  - Matches web's `narration_button.html` pattern
- Paragraph annotations: gold underline on annotated words, tooltip popup on tap
- Interaction blocks: add `BorderBeam` on active question cards

### 4.6 Dashboard Polish

- Stats cards: add `CountUpAnimation` (animate from 0 to value on appear, 800ms)
- User header: `GoldGradientText` for username
- Recent scans: `WadjetCardGlow` on each horizontal card
- Add collapsible sections (matching web's expandable "Recent Scans" section)
- Empty state: branded illustration, gold accent, call-to-action button

### 4.7 Settings Polish
- Section headers: gold left-border accent (4dp gold strip + text)
- TTS slider: gold track + gold thumb (already correct with Gold thumbColor)
- Sign out: confirmation dialog with gold outline
- Minimal changes — SettingsScreen already well-structured

### 4.8 Feature Quality Verification (Logic Parity with Web App)

The Android app calls the same backend API, but **drops or ignores** several response fields the web app renders. This sub-phase ensures every feature works at the same depth and quality as the web app.

#### 4.8.1 Scan — Full Data Rendering

**Problem**: ScanResultScreen ignores 6+ API fields and has zero TTS.

| Fix | Detail |
|---|---|
| Add `ai_reading` to `ScanResponse` model | `@SerializedName("ai_reading") val aiReading: AiReading?` with `data class AiReading(val notes: String?)` |
| Add `ai_unverified` to model | `val aiUnverified: Boolean = false` |
| Add `quality_hints` to model | `val qualityHints: List<String>? = null` |
| Add `confidence_summary` to model | `val confidenceSummary: ConfidenceSummary?` with `data class ConfidenceSummary(val avg: Float, val min: Float, val max: Float, val lowCount: Int)` |
| Render `aiReading.notes` | Gold-bordered card below translation: "AI Notes" heading + notes text |
| Render `aiUnverified` | Warning banner (amber) when true: "AI verification unavailable for this scan" |
| Render `qualityHints` | On 0 detections: show list of actionable tips ("Image too dark", "Try closer crop", etc.) instead of empty state |
| Render `readingDirection` | Badge next to transliteration heading (e.g. "→ Left to Right") |
| Render `layoutMode` | Badge next to reading direction (e.g. "AI Detected") |
| Render `gardinerSequence` | Below transliteration: "Gardiner: G1-D21-N5" in `labelMedium` `TextMuted` |
| Add TTS: transliteration | Speaker button → `AudioApiService.speak(text, lang="en", context="scan_pronunciation")` → local TTS fallback |
| Add TTS: translation EN | Speaker button → server TTS → local fallback |
| Add TTS: translation AR | Speaker button → server TTS with `lang="ar"` → local fallback |
| Confidence color coding | Per-glyph: green (#4CAF50) > 70%, gold (#D4AF37) 40-70%, red (#EF4444) < 40% |
| Add `confidenceSummary` display | Summary card: "Average: 87% / Min: 62% / 2 low-confidence" |

#### 4.8.2 Write — Complete Output Display

**Problem**: WriteTab doesn't render `mdc` field or per-glyph `transliteration`/`phonetic_value`.

| Fix | Detail |
|---|---|
| Render `mdc` return value | When mode is "mdc" or when API returns mdc string: show "MdC: Aa1-G43-X1" in monospace below output |
| Render per-glyph `transliteration` | Add to glyph breakdown row: `glyph + code + transliteration + phonetic + meaning` |
| Render per-glyph `phoneticValue` | Show phonetic value in `Sand` color between transliteration and meaning |
| Realtime preview | Debounced API call (500ms) as user types — show preview hieroglyphs live below input. Add `isPreviewLoading` state with `StreamingDots` indicator |

#### 4.8.3 Explore/Identify — Display All Result Data

**Problem**: IdentifyScreen drops `landmark` field, doesn't show confidence percentage.

| Fix | Detail |
|---|---|
| Show confidence % | In each `MatchCard`: add percentage text (e.g. "92%") with color coding (green/gold/red) |
| Render `landmark` inline | When top match confidence > 80%: expand the top result to show landmark preview (hero image + name + type) directly on identify results screen, without requiring navigation |
| Arabic name display | Verify `LandmarkCard` in ExploreScreen shows `nameAr` (Sand color, below English name) |
| Featured badge | Show gold "Featured" pill on `LandmarkCard` when `landmark.featured == true` |

#### 4.8.4 Stories — All 4 Interaction Types at Full Quality

**Problem**: Interaction feedback and completion screen are incomplete.

| Fix | Detail |
|---|---|
| `glyph_discovery` | Verify: tap renders large Unicode glyph → "Glyph Learned" gold badge → shows `meaning` + `transliteration` text. Add animation: scale from 0→1 + gold pulse on discover |
| `choose_glyph` | Verify: 2×2 option grid shows Unicode + Gardiner code per option. After answer: show `explanation` text (bilingual) in green/red panel. Disable options after submission |
| `write_word` | Verify: hint text shown from `interaction.prompt`. After submit: correct shows Unicode glyph + green; incorrect shows red + "try again". Input retains text on retry |
| `story_decision` | Verify: full-width buttons with `choice.text`. After answer: show `choice.outcome` in gold panel with fade-in animation |
| Pass `tts_voice`/`tts_style` to server TTS | In `speakChapter()`: include `voice` and `style` params in `/api/audio/speak` request when `chapter.ttsVoice`/`chapter.ttsStyle` are non-null |
| Completion screen | After last chapter: full-screen celebration — large `story.coverGlyph`, "Story Complete" gold gradient text, score card, glyphs learned list, "Back to Stories" + "Read Again" buttons |
| Render `glyphsTaught` on story cards | Add "𓂀 ×5 glyphs" badge on `StoriesScreen` story cards |

#### 4.8.5 TTS — Full Fallback Chain Everywhere

**Problem**: ScanResultScreen has zero TTS. Story narration ignores voice/style.

| Fix | Detail |
|---|---|
| ScanResultScreen TTS | Add `TtsButton` composable per section (transliteration, EN, AR). Pattern: tap → POST `/api/audio/speak` → play WAV via `MediaPlayer` → on 204/error, fallback to Android `TextToSpeech` |
| Dictionary sign TTS | Currently uses `phoneticValue` — also try `speech` field from API (richer pronunciation) |
| Chat STT upgrade | Replace Android `SpeechRecognizer` with Groq Whisper: record audio via `MediaRecorder` → POST `/api/stt` → get text → insert into input. Fall back to Android SDK if server fails |
| Story narration voice/style | Forward `chapter.ttsVoice` and `chapter.ttsStyle` as params in the `/api/audio/speak` POST body |

#### 4.8.6 Chat — Full Feature Parity

**Problem**: No conversation history, fixed English greeting, `sources` dropped, no STT quality.

| Fix | Detail |
|---|---|
| Map `sources` field | Add `sources: List<String>? = null` to `ChatStreamChunk` or final response model. When present: render "Sources" collapsible section below assistant message |
| Localized conversation starters | Replace fixed English greeting with localized starters from app locale (en: "Tell me about the pyramids", "What are hieroglyphs?", "Famous pharaohs" / ar: Arabic equivalents) |
| Past conversations (Room) | `ChatConversation` entity: `id`, `title` (first user message truncated to 50 chars), `createdAt`, `messageCount`. `ChatMessage` entity: `conversationId`, `role`, `text`, `timestamp`. Load/save via Room DAO |
| Landmark context display | Replace raw slug display ("Discussing: karnak-temple") with human-readable name from `LandmarkDetailScreen` cache or API lookup |
| Stream stop button | Add "Stop" icon button visible during streaming — cancels `streamJob` coroutine |
| Character counter | Show "42/2000" counter below input field when text length > 0 |

#### 4.8.7 Dictionary — Complete Sign Detail

**Problem**: `speech` field unused, no Arabic localization param.

| Fix | Detail |
|---|---|
| Use `speech` field for TTS | In `SignDetailSheet`: prefer `sign.speech` over `sign.phoneticValue` for `onSpeak()` — richer pronunciation data |
| Pass `lang` param | All `DictionaryApiService` calls should include `lang` parameter from app locale (affects `category_name`, type labels) |
| Verify lesson exercises | Confirm `LessonViewModel` correctly validates answers against `exercise.correctAnswer`, shows hint on wrong, tracks score |
| Verify alphabet tab | Confirm `LearnTab` renders the 25 uniliteral signs from `/api/dictionary/alphabet` in teaching order |

**Estimated screens touched**: 10+ screens, 5+ API models
**Risk**: Medium — model changes require testing against live API; UI changes are additive

---

**Estimated screens touched (Phase 4 total)**: 15+ screens
**Risk**: High — many screens with both UI and logic changes, but changes are additive (not restructuring architecture)

---

## Phase 5: Cross-Cutting Quality

**Goal**: Ensure consistent quality, accessibility, and polish across the entire app.

### 5.1 Loading State Audit
Apply branded loading states everywhere:

| Screen | Current | Target |
|---|---|---|
| App startup | Splash → Welcome | `WadjetFullLoader` (logo pulse + shimmer) before content |
| DictionaryScreen | `ShimmerCardList` | Gold-tinted `WadjetShimmer` |
| ExploreScreen | `ShimmerCardList` | Gold-tinted `WadjetShimmer` |
| StoriesScreen | implicit | `WadjetSectionLoader("Loading stories...")` |
| DashboardScreen | `PullToRefreshBox` | Gold pull-to-refresh indicator color |
| LandmarkDetailScreen | `ShimmerDetail` | Gold-tinted `WadjetShimmer` |
| ChatScreen | no visible streaming UI | `StreamingDots` in assistant bubble |
| Buttons (all) | no loading | `WadjetButtonLoader` inside-button spinner |

### 5.2 Error State Audit
Verify all screens use `ErrorState` composable consistently:
- Gold retry button (not default Material)
- Hieroglyph icon instead of generic error icon
- Descriptive error message (not raw API error)

### 5.3 Empty State Audit

| Screen | Empty Message | Icon/Glyph |
|---|---|---|
| ScanHistory | "No scans yet" + "Scan hieroglyphs to start" | `𓂀` |
| Explore (no results) | "No landmarks found" + "Try different filters" | `𓉐` |
| Stories | "Stories are loading..." | `𓁟` |
| Dashboard (no activity) | "Start your journey" + "Scan, explore, or read" | `𓁹` |
| Chat (empty) | "Ask Thoth anything about ancient Egypt" | `𓅝` |
| Favorites (empty) | "No favorites yet" + "Heart landmarks to save them" | `𓊹` |

### 5.4 Page Transition Animations
Upgrade from basic slide+fade to screen-appropriate transitions:

| Transition | Animation |
|---|---|
| Forward navigation | `slideInHorizontally(end) + fadeIn` (existing, keep) |
| Back navigation | `slideOutHorizontally(end) + fadeOut` (existing, keep) |
| Bottom nav tab switch | `fadeIn(200ms) + scaleIn(0.96f)` (replace slide) |
| Modal/sheet open | `slideInVertically(bottom) + fadeIn` (existing, keep) |
| Scan → ScanResult | `fadeIn(400ms) + expandIn` (special transition) |

### 5.5 Accessibility Pass
- All hieroglyph glyphs need `contentDescription` / `semantics` labels
- Upload zones: clear accessibility labels for screen readers
- Contrast check: Gold on Night (passes WCAG AA for large text; for small text, use Ivory)
- Touch targets: minimum 48dp on all interactive elements
- TalkBack testing on key flows (scan, explore, chat)

### 5.6 Haptic Feedback
Add subtle haptics matching premium app feel:
- Button press: `HapticFeedbackType.LongPress` on gold buttons
- Scan complete: `performHapticFeedback(CONFIRM)` on scan result arrival
- Favorite toggle: `performHapticFeedback(TOGGLE_ON/OFF)`
- Story interaction correct: `performHapticFeedback(CONFIRM)`

### 5.7 Pull-to-Refresh Branding
Customize `PullToRefreshBox` indicator:
- Gold indicator color
- Wadjet logo in the refresh circle (custom `PullToRefreshContainer`)

**Estimated files touched**: 15-20 files
**Risk**: Low-Medium — mostly additive polish, no structural changes

---

## Phase 6: Testing & APK Release

**Goal**: Verify everything works, then build release APK.

### 6.1 Manual Testing Checklist

#### Auth Flow
- [ ] Register with email → lands on Landing
- [ ] Login with email → lands on Landing
- [ ] Google Sign-In → lands on Landing
- [ ] Forgot password → sends reset email
- [ ] Logout → returns to Welcome
- [ ] Token refresh works (stay logged in > 1 hour)

#### Scan Flow (Image Upload)
- [ ] Tap upload zone → gallery picker opens
- [ ] Select image → preview shows with "Analyze" button
- [ ] Tap "Change" → picks new image
- [ ] Tap "Analyze" → progress overlay with 4 steps
- [ ] Result screen shows: annotated image, glyphs, transliteration, EN/AR translation
- [ ] TTS works on transliteration + translation
- [ ] Share + copy work
- [ ] "Scan Again" clears and returns to upload
- [ ] Scan History shows previous scans
- [ ] Swipe-to-delete removes scan from history

#### Dictionary Flow
- [ ] Browse tab: search filters signs correctly
- [ ] Category chips filter correctly
- [ ] Type chips (logogram/phonogram/determinative) filter correctly
- [ ] Tap sign → detail bottom sheet with all info
- [ ] TTS on sign pronunciation
- [ ] Pagination loads more signs
- [ ] Learn tab: 5 lesson cards visible
- [ ] Tap lesson → exercises load and work (MCQ + reveal)
- [ ] Write tab: type transliteration → hieroglyphs appear
- [ ] Palette picker changes output mode

#### Explore Flow (Image Upload)
- [ ] Landmark list loads with images
- [ ] Search filters landmarks
- [ ] Category chips filter
- [ ] City dropdown filters
- [ ] Infinite scroll loads more
- [ ] Pull-to-refresh works
- [ ] Tap landmark → detail screen with hero, tabs, content
- [ ] Maps button opens Google Maps
- [ ] Chat button opens Thoth with landmark context
- [ ] Favorite toggle works (heart icon)
- [ ] Identify FAB → upload zone → select image → results appear
- [ ] Tap result → landmark detail

#### Chat Flow
- [ ] Messages send and stream response
- [ ] StreamingDots visible during streaming
- [ ] Markdown renders correctly in responses
- [ ] Thoth 𓅝 avatar shows on assistant messages
- [ ] TTS on message works (server → local fallback)
- [ ] Voice input (STT) works
- [ ] Clear chat works
- [ ] Landmark-context chat shows subtitle
- [ ] Quick suggestion chips work (first message)

#### Stories Flow
- [ ] Story list loads with cover glyphs (NOT emojis)
- [ ] Difficulty filter works
- [ ] Tap story → reader opens
- [ ] Ken Burns hero image animates
- [ ] Paragraphs render with annotations
- [ ] Interactions work (MCQ, write-in, glyph discovery)
- [ ] Chapter navigation (prev/next)
- [ ] Progress bar updates
- [ ] Floating narration FAB plays/pauses audio
- [ ] Stories 4+ show premium lock

#### Dashboard Flow
- [ ] Stats load with count-up animation
- [ ] Recent scans show in horizontal scroll
- [ ] Favorites show with tab filtering
- [ ] Story progress shows
- [ ] Pull-to-refresh reloads

#### Settings Flow
- [ ] Profile name edit works
- [ ] Password change works (email auth)
- [ ] Password section hidden for Google auth
- [ ] TTS toggle works
- [ ] TTS speed slider works
- [ ] Clear cache works
- [ ] Sign out with confirmation dialog

#### Cross-Cutting
- [ ] No emojis anywhere in the app (visual audit)
- [ ] All loading states show branded Wadjet loader (not generic spinner)
- [ ] All error states show retry button
- [ ] All empty states show hieroglyph icon + descriptive text
- [ ] Bottom nav tabs all navigate correctly
- [ ] Back navigation works everywhere
- [ ] Toast messages display correctly (success, error, info)
- [ ] Offline indicator shows when no connection
- [ ] Images load with shimmer placeholder (Coil)
- [ ] No hardcoded strings (all in strings.xml or constants)
- [ ] No crashes on rotation (if not locked)

### 6.2 Automated Test Updates
- Update existing unit tests for refactored screens (ScanViewModel, IdentifyViewModel)
- Add UI tests for new components (ImageUploadZone, WadjetFullLoader, StreamingDots)
- Verify all existing tests still pass after changes

### 6.3 Build Release APK
```bash
# Clean build
./gradlew clean

# Run all tests
./gradlew testDebugUnitTest

# Build release APK
./gradlew assembleRelease

# APK location: app/build/outputs/apk/release/app-release.apk
```

### 6.4 Create GitHub Release
```bash
# Tag the release
git tag -a v1.1.0 -m "UX Overhaul — Match web app identity"
git push origin v1.1.0

# CI/CD will automatically:
# - Build release APK
# - Rename to wadjet-v1.1.0.apk
# - Publish GitHub Release with APK attached
```

### 6.5 Post-Release
- [ ] Download APK from GitHub Releases
- [ ] Install on real device
- [ ] Run through full testing checklist on real device
- [ ] Share APK link

**Risk**: Low — testing and release are established workflows

---

## Phase Dependency Graph

```
Phase 1 (Identity)
    │
    ├──→ Phase 2 (Camera → Upload)
    │         │
    │         └──→ Phase 4.1-4.7 (Screen Redesign — UI/animations)
    │
    └──→ Phase 3 (Landing & Welcome)
              │
              └──→ Phase 4.8 (Feature Quality — logic/data parity)
                        │
                        └──→ Phase 5 (Cross-Cutting Quality)
                                  │
                                  └──→ Phase 6 (Testing & APK)
```

**Phases 1 and 2** can be done in parallel.
**Phases 3 and 4** can partially overlap (landing redesign is independent from feature screen polish).
**Phase 5** must follow phases 3 and 4 (audit requires completed screens).
**Phase 6** is strictly last.

---

## File Impact Summary

### New Files to Create (14)
| File | Phase |
|---|---|
| `animation/BorderBeam.kt` | P1 |
| `animation/MeteorShower.kt` | P1 |
| `animation/DotPattern.kt` | P1 |
| `animation/ButtonShimmer.kt` | P1 |
| `animation/ShineSweep.kt` | P1 |
| `animation/GoldGradientSweep.kt` | P1 |
| `component/WadjetFullLoader.kt` | P1 |
| `component/WadjetSectionLoader.kt` | P1 |
| `component/StreamingDots.kt` | P1 |
| `component/WadjetToast.kt` | P1 |
| `component/ImageUploadZone.kt` | P2 |
| `component/TtsButton.kt` | P4.8 |
| `db/ChatConversationEntity.kt` | P4.8 |
| `StoryCompletionScreen.kt` | P4.8 |

### Existing Files to Modify (20+)
| File | Phase | Change Type |
|---|---|---|
| `WadjetColors.kt` | P1 | Fix Dust color |
| `ShimmerEffect.kt` | P1 | Gold-tinted shimmer |
| `WadjetButton.kt` | P1 | Add loading spinner variant |
| `WelcomeScreen.kt` | P1, P3 | Emoji→hieroglyph, add animations |
| `LandingScreen.kt` | P1, P3 | Full redesign |
| `ScanScreen.kt` | P2, P4 | Remove camera, add upload zone, progress redesign |
| `IdentifyScreen.kt` | P2, P4 | Remove camera, add upload zone |
| `ExploreScreen.kt` | P2, P4 | Change FAB icon, add animations |
| `ScanResultScreen.kt` | P4 | Add TTS, improve glyph display |
| `DictionaryScreen.kt` | P4 | Animation polish |
| `LandmarkDetailScreen.kt` | P4 | Animation polish |
| `ChatScreen.kt` | P4 | StreamingDots, suggestions, conversations |
| `StoriesScreen.kt` | P4 | Animation polish |
| `StoryReaderScreen.kt` | P4 | Emoji fix, floating narration FAB |
| `DashboardScreen.kt` | P4 | Count-up animation, expand/collapse |
| `ErrorState.kt` | P5 | Hieroglyph icon |
| `AndroidManifest.xml` | P2 | Comment out camera permission |
| `WadjetNavGraph.kt` | P3 | Add LandingViewModel injection |
| Various empty-state strings | P5 | Hieroglyph icons + messages |
| `ScanResponse` model | P4.8 | Add 5 missing fields (aiReading, aiUnverified, qualityHints, confidenceSummary, imageSize) |
| `ScanResultScreen.kt` | P4.8 | Render all new fields + 3 TTS buttons + confidence colors |
| `WriteTab.kt` | P4.8 | Render mdc, per-glyph transliteration/phonetic, realtime preview |
| `IdentifyScreen.kt` | P4.8 | Confidence %, inline landmark preview |
| `StoryReaderScreen.kt` | P4.8 | Verify all 4 interaction types, pass tts_voice/style, completion screen |
| `ChatViewModel.kt` | P4.8 | Map sources, Room conversations, Groq STT, stream stop |
| `ChatScreen.kt` | P4.8 | Past conversations drawer, localized starters, char counter |
| `SignDetailSheet.kt` | P4.8 | Use speech field for TTS, pass lang param |
| `AudioApiService` calls | P4.8 | Add voice/style/context params to speak() |
| Test files | P6 | Update for refactored screens |

---

## Appendix A: Web App Reference Files

When implementing, reference these web app files for exact layouts and behavior:

| Feature | Web File | Path |
|---|---|---|
| Colors & Animations | `input.css` | `app/static/css/input.css` |
| Base layout | `base.html` | `app/templates/base.html` |
| Landing page | `landing.html` | `app/templates/landing.html` |
| Scan page | `scan.html` | `app/templates/scan.html` |
| Dictionary | `dictionary.html` | `app/templates/dictionary.html` |
| Explore | `explore.html` | `app/templates/explore.html` |
| Chat | `chat.html` | `app/templates/chat.html` |
| Stories | `stories.html` | `app/templates/stories.html` |
| Story reader | `story_reader.html` | `app/templates/story_reader.html` |
| Dashboard | `dashboard.html` | `app/templates/dashboard.html` |
| Settings | `settings.html` | `app/templates/settings.html` |
| TTS helper | `tts.js` | `app/static/js/tts.js` |
| Narration button | `narration_button.html` | `app/templates/partials/narration_button.html` |

All paths relative to `D:\Personal attachements\Projects\Wadjet-v3-beta\`.

---

## Appendix B: Hieroglyph Quick Reference

Frequently used hieroglyphs for decorative UI elements:

| Glyph | Unicode | Code Point | Meaning | Use For |
|---|---|---|---|---|
| 𓁹 | U+13079 | Wedjat Eye | Protection | App logo context, general decoration |
| 𓂀 | U+13080 | Eye of Horus | Sight/Vision | Scan feature |
| 𓊹 | U+132B9 | Cartouche | Royal name enclosure | Dictionary feature |
| 𓉐 | U+13250 | House | Building/Dwelling | Landmarks/Explore feature |
| 𓁟 | U+1305F | Thoth Head | Knowledge/Wisdom | Stories, Chat avatar alt |
| 𓅝 | U+1315D | Ibis | Thoth's sacred bird | Chat avatar (Thoth) |
| 𓇯 | U+131EF | Sky/Horizon | Sky/Heaven | Explore quick-action |
| 𓀀 | U+13000 | Seated Man | Person | User/Profile placeholder |
| 𓃀 | U+130C0 | Leg | Movement/Journey | Navigation/Progress |
| 𓆣 | U+131A3 | Scarab | Transformation | Loading/Progress states |
