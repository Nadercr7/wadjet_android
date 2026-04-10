# Phase Prompts — Copy & Send Before Each Phase

---

## Phase 1 Prompt

```
Implement Phase 1 (Identity Alignment) from planning/ux-overhaul-plan.md in the Wadjet Android project at D:\Personal attachements\Projects\Wadjet-Android\

What to do:
1. Fix Dust color in WadjetColors.kt (#A89070 → #8B7355)
2. Replace ALL emojis with Unicode Egyptian Hieroglyphs across every screen (🏛→𓉐, 📷→𓂀, 📖→𓊹, 🧭→𓇯, 📜→𓁟) — render them with WadjetFonts.Hieroglyph fontFamily. Grep the entire codebase to catch every emoji.
3. Create 6 new animation composables in core/designsystem/animation/:
   - BorderBeam.kt — animated arc stroke orbiting card border
   - MeteorShower.kt — diagonal falling gold streaks
   - DotPattern.kt — repeating gold dot grid background
   - ButtonShimmer.kt — perimeter light sweep on buttons
   - ShineSweep.kt — diagonal shine line sweep
   - GoldGradientSweep.kt — animated gradient on backgrounds
   Reference: extract CSS @keyframes from web app input.css (D:\Personal attachements\Projects\Wadjet-v3-beta\app\static\css\input.css) and from D:\Personal attachements\Repos\21-Frontend-UI\ (magicui, animate-ui, react-bits for CSS animation patterns — convert to Compose, don't import React)
4. Create 4 new branded components in core/designsystem/component/:
   - WadjetFullLoader.kt — full-screen: pulsing logo + gold gradient "WADJET" text + shimmer bar
   - WadjetSectionLoader.kt — inline: small pulsing logo + text
   - StreamingDots.kt — 3 gold dots with staggered scale animation
   - WadjetToast.kt — fixed bottom toast with success/error/info variants
5. Update ShimmerEffect.kt to use gold-tinted colors (Surface → GoldMuted 15% → Surface)
6. Update WadjetButton.kt to support loading spinner state (small gold CircularProgressIndicator replacing text)

Build with: $env:GRADLE_USER_HOME = "D:\.gradle"; .\gradlew.bat assembleDebug --console=plain
Verify: zero emojis in grep, all new files compile, existing tests pass.
```

---

## Phase 2 Prompt

```
Implement Phase 2 (Camera → Image Upload) from planning/ux-overhaul-plan.md in the Wadjet Android project at D:\Personal attachements\Projects\Wadjet-Android\

What to do:
1. Create ImageUploadZone.kt in core/designsystem/component/ — reusable composable:
   - Gold dashed border (PathEffect.dashPathEffect) on Night background
   - 𓂀 hieroglyph icon (48sp, gold, Noto font)
   - "Tap to select an image" text + "Browse Gallery" WadjetGhostButton
   - After selection: show image preview with "Change" overlay + "Analyze" WadjetButton
   - Uses PickVisualMedia ActivityResultContract
   - Accepts: onImageSelected(Uri), customizable title/subtitle text
2. Refactor ScanScreen.kt:
   - Remove CameraX PreviewView, camera permission, capture button, gold bracket overlay
   - Comment out camera code (don't delete) with // CAMERA_DISABLED markers
   - Replace with ImageUploadZone centered on screen
   - Keep: gallery picker contract (wire to upload zone), ScanProgressOverlay, error handling, top bar with back+history
3. Refactor IdentifyScreen.kt:
   - Same approach: remove CameraX, add ImageUploadZone with text "Upload a photo of an Egyptian landmark"
   - Keep: loading overlay, IdentifyResults panel, retry
4. Update ExploreScreen.kt FAB: camera icon → upload icon, update content description
5. Update AndroidManifest.xml: comment out CAMERA permission

Build & verify same command. Test: gallery picker still works, scan flow still processes images, identify flow still works.
```

---

## Phase 3 Prompt

```
Implement Phase 3 (Landing & Welcome Redesign) from planning/ux-overhaul-plan.md in the Wadjet Android project at D:\Personal attachements\Projects\Wadjet-Android\

Reference web landing page: D:\Personal attachements\Projects\Wadjet-v3-beta\app\templates\landing.html

What to do:
1. Create LandingViewModel.kt — loads user profile name + recent scan + in-progress story from existing API services
2. Redesign LandingScreen.kt to match web app's rich layout:
   - Greeting section: "Welcome back, [Name]" + animated gold gradient "WADJET" title (GoldGradientText) + tagline
   - Hieroglyphs path card: DotPattern bg, 𓂀 icon, "Hieroglyphs" heading, 3 bullet features, "Start Scanning" btn-gold with BorderBeam
   - Landmarks path card: DotPattern bg, 𓉐 icon, "Landmarks" heading, 3 bullet features, "Start Exploring" btn-gold with BorderBeam
   - Quick actions 2×2 grid: 𓂀 Scan, 𓊹 Dictionary, 𓇯 Explore, 𓁟 Stories — Surface cards with hieroglyph icons
   - Continue section (conditional): recent scan thumbnail + in-progress story card (horizontal scroll)
   - Thoth quick chat entry: 𓅝 "Ask Thoth anything..." tappable → Chat screen
   - All sections animated with FadeUp stagger
3. Polish WelcomeScreen.kt:
   - Ensure all emojis replaced (Phase 1 should've done this — verify)
   - Add MeteorShower or DotPattern as subtle background
   - WadjetFullLoader for post-login transition
   - Staggered FadeUp on feature cards
   - Google Sign-In button loading spinner state

Build & verify. The landing page should feel rich and immersive like the web app.
```

---

## Phase 4 Prompt (UI/Animation — 4.1 through 4.7)

```
Implement Phase 4 sub-phases 4.1 through 4.7 (Feature Screen UI Redesign) from planning/ux-overhaul-plan.md in the Wadjet Android project at D:\Personal attachements\Projects\Wadjet-Android\
NOTE: Phase 4.8 (Feature Quality / Logic Parity) is a SEPARATE prompt. This prompt is UI/animation only.

Reference all web templates: D:\Personal attachements\Projects\Wadjet-v3-beta\app\templates\

This is the largest phase. Work through these sub-phases in order:

4.1 Scan Flow:
- ScanScreen: FadeUp on upload zone, ShineSweep on image preview border, ButtonShimmer on Analyze button
- ScanProgressOverlay: redesign to 4-step pipeline UI (✓ done / ◉ active with GoldPulse / ○ pending), each step with label + mini progress, descriptive subtitle, gold shimmer bar
- ScanResultScreen: add TTS button per section (transliteration, EN, AR), WadjetBadge for confidence, FlowRow glyph grid (unicode + code + name), gold dividers
- Reference: scan.html from web app

4.2 Dictionary: gold-tinted shimmer loading, TTS in sign detail sheet, FadeUp stagger on lesson cards, animateContentSize in write tab

4.3 Explore:
- LandmarkCard: ShineSweep on load, WadjetCardGlow on press
- FAB: GoldPulse modifier
- Empty state: branded hieroglyph + text
- LandmarkDetail: verify KenBurnsImage on hero, FadeUp on badges, shimmer gallery placeholders
- IdentifyScreen: match scan upload pattern, FadeUp stagger results, confidence color badges

4.4 Chat (reference chat.html):
- Add StreamingDots in assistant bubble during streaming
- Quick suggestion chips above input (first message only): "Tell me about the pyramids", "What are hieroglyphs?", "Famous pharaohs"
- Chat input bar: BorderBeam when streaming
- Past conversations: store in Room (derive title from first user message), drawer panel

4.5 Stories: ShineSweep on cover glyph, FadeUp stagger cards, difficulty badge colors (green/yellow/red)
- StoryReader: 𓁟 not 📜 placeholder, floating narration FAB (𓅝 + play/pause, 3 states), gold underline on annotated words

4.6 Dashboard: CountUpAnimation on stats (0→value, 800ms), GoldGradientText username, WadjetCardGlow on scan cards, collapsible sections

4.7 Settings: gold left-border section headers, minimal changes

Build after each sub-phase to catch errors early.
```

---

## Phase 4.8 Prompt

```
Implement Phase 4.8 (Feature Quality Verification — Logic Parity) from planning/ux-overhaul-plan.md in the Wadjet Android project at D:\Personal attachements\Projects\Wadjet-Android\

Reference web app: D:\Personal attachements\Projects\Wadjet-v3-beta\
Read the web templates + API files to understand full behavior.

This phase ensures every feature works at the SAME depth and quality as the web app. The Android app calls the same backend but silently drops several API response fields.

4.8.1 SCAN — Add missing model fields + render them:
- Add to ScanResponse: aiReading (notes), aiUnverified (bool), qualityHints (list), confidenceSummary (avg/min/max/lowCount)
- ScanResultScreen: render AI Notes card, ai_unverified warning banner, quality hints on 0 detections
- Render readingDirection + layoutMode as badges (already in model, just not displayed)
- Render gardinerSequence below transliteration ("Gardiner: G1-D21-N5")
- Add 3 TTS buttons (transliteration, EN translation, AR translation) using /api/audio/speak → Android TTS fallback
- Color-code per-glyph confidence: green >70%, gold 40-70%, red <40%
- Show confidenceSummary as a summary card
- Reference: scan.html from web app, app/api/scan.py for response shape

4.8.2 WRITE — Complete output:
- Render mdc return value in monospace when present
- Render per-glyph transliteration + phoneticValue in breakdown row (currently only shows code + meaning)
- Add debounced realtime preview (500ms delay) as user types — StreamingDots while loading
- Reference: write tab in dictionary.html, app/api/write.py

4.8.3 EXPLORE/IDENTIFY — Full results:
- Show confidence percentage in IdentifyScreen MatchCard (with green/gold/red color)
- When top match >80%: expand inline landmark preview (hero image + name + type)
- Verify Arabic name shown on LandmarkCards, featured badge on featured landmarks
- Reference: explore.html, app/api/explore.py

4.8.4 STORIES — All 4 interaction types verified:
- glyph_discovery: large Unicode + "Glyph Learned" gold badge + meaning + transliteration + scale animation
- choose_glyph: 2×2 option grid, post-answer explanation text, disabled options after answer
- write_word: hint from prompt, correct shows glyph green, incorrect shows red + retry
- story_decision: full-width buttons, outcome text in gold panel after answer
- Forward tts_voice/tts_style to /api/audio/speak for chapter narration
- Add completion screen after last chapter: coverGlyph, "Story Complete" gold gradient, score, glyphs learned, buttons
- Show glyphsTaught badge on story cards in StoriesScreen
- Reference: story_reader.html, app/api/stories.py

4.8.5 TTS — Everywhere:
- Create reusable TtsButton composable (3 states: speaker → spinner → stop)
- Add TTS to ScanResultScreen (3 sections)
- Dictionary: use speech field (not just phoneticValue) for sign pronunciation
- Chat STT: replace Android SpeechRecognizer with Groq Whisper (/api/stt) + local fallback
- Story narration: pass voice/style params

4.8.6 CHAT — Full parity:
- Map sources field from API response (render as collapsible "Sources" below message)
- Localized conversation starters (not fixed English greeting)
- Past conversations via Room: ChatConversation + ChatMessage entities, drawer panel
- Landmark context: show human-readable name (not raw slug)
- Add stream stop button (cancel streamJob)
- Add character counter (N/2000)

4.8.7 DICTIONARY — Complete:
- Use speech field for TTS (prefer over phoneticValue)
- Pass lang param to all DictionaryApiService calls
- Verify lesson exercises validate correctly

Build after each sub-section. Test against live API (production backend).
```

---

## Phase 5 Prompt

```
Implement Phase 5 (Cross-Cutting Quality) from planning/ux-overhaul-plan.md in the Wadjet Android project at D:\Personal attachements\Projects\Wadjet-Android\

What to do:
1. Loading state audit: replace every remaining CircularProgressIndicator with branded Wadjet loaders (WadjetFullLoader, WadjetSectionLoader, WadjetButtonLoader, gold-tinted shimmer). Check ALL screens.
2. Error state audit: every ErrorState must use gold retry button + hieroglyph icon (not generic) + descriptive message
3. Empty state audit: every empty state gets hieroglyph icon + descriptive text + call-to-action (see plan for per-screen mapping)
4. Page transitions: bottom nav tab switch → fadeIn(200ms) + scaleIn(0.96f) instead of slide. Scan→ScanResult → fadeIn(400ms) + expandIn
5. Accessibility: contentDescription on all hieroglyphs, 48dp minimum touch targets, contrast check (use Ivory for small text on dark)
6. Haptic feedback: LongPress on gold buttons, CONFIRM on scan complete, TOGGLE on favorite, CONFIRM on correct story answer
7. Pull-to-refresh: gold indicator color on all PullToRefreshBox screens

Do a final visual audit: grep for any remaining emojis, any remaining default gray CircularProgressIndicator, any missing loading/error/empty states.
Build & run all tests.
```

---

## Phase 6 Prompt

```
Implement Phase 6 (Testing & APK Release) from planning/ux-overhaul-plan.md in the Wadjet Android project at D:\Personal attachements\Projects\Wadjet-Android\

What to do:
1. Update unit tests for refactored screens (ScanViewModel without camera, IdentifyViewModel without camera, new LandingViewModel)
2. Add UI tests for new components (ImageUploadZone, WadjetFullLoader, StreamingDots)
3. Run full test suite: $env:GRADLE_USER_HOME = "D:\.gradle"; .\gradlew.bat testDebugUnitTest --console=plain
4. Fix any test failures
5. Build release APK: .\gradlew.bat assembleRelease --console=plain
6. Verify APK exists at app/build/outputs/apk/release/
7. Git commit all changes, tag v1.1.0, push
8. Verify CI/CD creates GitHub Release with APK attached

The APK is the final deliverable. Everything must compile and pass tests before release.
```
