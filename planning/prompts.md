# Wadjet Android — AI Prompts

> Ready-to-use prompts for AI coding assistants, **in phase order P0 → P10**.
> Use the Master Context prompt first in every new session, then the phase prompt.

---

## Master Context Prompt (Use FIRST in every session)

```
You are building the Wadjet Android app — a native Kotlin/Jetpack Compose Android app
that mirrors the Wadjet v3 web app (an AI-powered Egyptian heritage platform).

Key facts:
- Package: com.wadjet.app
- Min SDK 26, Target SDK 35
- Kotlin 2.1.0, Jetpack Compose with Material 3
- Architecture: MVVM + Clean Architecture + Hilt DI
- Backend: Wadjet API at https://nadercr7-wadjet-v2.hf.space
- User data: Firebase Auth + Cloud Firestore
- Local cache: Room 2.8.4 database
- ML: ONNX Runtime Android 1.20.0 (hieroglyph + landmark models)
- Network: Retrofit 2.11.0 + OkHttp 4.12.0
- Image loading: Coil 3.0.4
- Design: Black & Gold theme (NEVER use Material You dynamic colors)

Brand colors (NON-NEGOTIABLE):
- Night: #0A0A0A (background)
- Surface: #141414 (cards)
- Gold: #D4AF37 (primary accent)
- GoldLight: #E5C76B (hover)
- GoldDark: #B8962E (active)
- Text: #F0F0F0
- TextMuted: #8A8A8A

Fonts: Playfair Display (headings), Inter (body), Noto Sans Egyptian Hieroglyphs (glyphs)

The project repo is at D:\Personal attachements\Projects\Wadjet-Android\
Read the planning/ folder for full architecture, API mapping, design system, screens,
Firebase schema, dependencies, and release checklist.
```

---

## P0: Project Setup

```
Modify the existing Wadjet Android project skeleton.
The project was ALREADY created by Android Studio — :app module, default libs.versions.toml,
and build.gradle.kts files already exist. DO NOT recreate from scratch. MODIFY them in place.

Reference: planning/implementation-plan.md P0, planning/project-structure.md,
           planning/dependencies.md (COMPLETE version catalog)

IMPORTANT existing state:
- Android Studio created a default single-module project
- compileSdk is wrong (36) → must be 35
- Java target is wrong (11) → must be 17
- libs.versions.toml has default Android Studio versions → REPLACE entirely
- google-services.json is already in app/

1. Create 19 NEW library modules alongside the existing :app module:
   :core:designsystem, :core:domain, :core:data, :core:network,
   :core:database, :core:firebase, :core:ml, :core:common, :core:ui,
   :feature:auth, :feature:landing, :feature:scan, :feature:dictionary,
   :feature:explore, :feature:chat, :feature:stories, :feature:dashboard,
   :feature:settings, :feature:feedback

   Each needs a build.gradle.kts with android-library + kotlin-android + compose plugins,
   namespace (e.g. "com.wadjet.core.designsystem"), compileSdk = 35, minSdk = 26,
   Java 17 target, and module-specific dependencies from planning/dependencies.md.

2. REPLACE libs.versions.toml — copy EXACTLY from planning/dependencies.md:
   Kotlin 2.1.0, Compose BOM 2026.03.00, Hilt 2.53.1, Retrofit 2.11.0,
   OkHttp 4.12.0, Room 2.8.4, Coil 3.0.4, Firebase BOM 33.7.0,
   ONNX Runtime 1.20.0, CameraX 1.4.1, Navigation 2.9.7,
   Lifecycle 2.10.0, Kotlinx Serialization 1.7.3, DataStore 1.1.1,
   JUnit 4, MockK, Turbine

3. REPLACE root build.gradle.kts — add ALL plugin aliases from planning/dependencies.md
   [plugins] section with apply false.

4. REPLACE app/build.gradle.kts — see planning/phase0-instructions.md Step 7:
   - compileSdk = 35, minSdk = 26, targetSdk = 35, Java 17
   - Hilt, KSP, google-services, firebase-crashlytics plugins
   - BuildConfig with BASE_URL: debug = "http://10.0.2.2:8000",
     release = "https://nadercr7-wadjet-v2.hf.space"
   - implementation(project(":core:*")) and implementation(project(":feature:*")) for ALL modules
   - ProGuard enabled for release

5. Base Hilt setup: WadjetApplication.kt with @HiltAndroidApp

6. Update settings.gradle.kts: include all 20 modules

7. Each library module: minimal placeholder (empty class/interface) so project compiles

8. Git repo already initialized — planning/ folder is in the project root.
   Do NOT overwrite or move planning/ files.
   .gitignore should exclude: google-services.json, *.jks, local.properties

Commit as: "P0: Project setup — 20 modules, Gradle, Firebase"
```

---

## P1: Design System

```
Implement the Wadjet design system in :core:designsystem module.

Reference: planning/design-system.md (COMPLETE spec with exact values)

Create these files:
1. WadjetColors.kt — All color values (Gold, Night, Surface, etc.)
2. WadjetTheme.kt — Material 3 darkColorScheme override, WadjetTheme composable
3. WadjetTypography.kt — Type scale with Playfair Display, Inter, Cairo
4. WadjetShapes.kt — Rounded corner shapes
5. WadjetFonts.kt — Font families (Playfair, Inter, JetBrains Mono, Noto Egyptian, Cairo)
6. Components:
   - WadjetButton.kt (gold primary + ghost outline + dark variants)
   - WadjetCard.kt (standard + glow variant)
   - WadjetTextField.kt (outlined with gold focus)
   - WadjetBadge.kt (4 variants: Gold, Muted, Success, Error)
   - WadjetTopBar.kt
   - WadjetBottomBar.kt (5 tabs: Home, Scan, Explore, Stories, Profile)
   - ShimmerEffect.kt
   - ErrorState.kt (empty state with icon, message, retry button)
   - LoadingOverlay.kt
7. Animations:
   - GoldPulse modifier
   - FadeUp AnimatedVisibility wrapper
   - KenBurnsImage composable
   - GoldGradientText composable

CRITICAL: Never use dynamicDarkColorScheme(). Always force WadjetDarkColorScheme.
The app is BLACK AND GOLD only. No system theme, no Material You.

Font files needed in res/font/:
playfair_display_semibold.ttf, playfair_display_bold.ttf,
inter_regular.ttf, inter_medium.ttf, inter_semibold.ttf,
jetbrains_mono_regular.ttf, noto_sans_egyptian_hieroglyphs.ttf,
cairo_regular.ttf, cairo_medium.ttf, cairo_semibold.ttf, cairo_bold.ttf
```

---

## P2: Auth & Navigation

```
Implement authentication and navigation for Wadjet Android.

Reference: planning/architecture.md, planning/api-mapping.md, planning/firebase-schema.md,
           planning/screens.md (S01, S02, S03, S18)

Auth flow:
1. Firebase Auth with Google Sign-In (Credential Manager API) + Email/Password
2. On Firebase auth success → also authenticate with Wadjet backend:
   - Google: POST /api/auth/google { credential: google_id_token }
   - Email register: POST /api/auth/register { email, password, display_name? }
   - Email login: POST /api/auth/login { email, password }
3. Store both Firebase token + Wadjet access_token in EncryptedSharedPreferences
4. Auth interceptor: add Bearer token to all API requests, refresh on 401

Implementation:
1. TokenManager (EncryptedSharedPreferences) for secure token storage
2. AuthRepository: Firebase Auth + Wadjet backend sync
3. AuthInterceptor (OkHttp): auto-add token, mutex-locked refresh on 401
4. NavGraph with type-safe routes (see Route sealed class in architecture.md)
5. Bottom navigation: Home, Scan, Explore, Stories, Profile
6. Auth state observer: navigate to Welcome when logged out, Landing when logged in
7. Screens: Splash (S01), Welcome (S02), Landing (S03)
8. Auth bottom sheets (S18): Login, Register, Forgot Password

Security requirements:
- EncryptedSharedPreferences for ALL tokens (not plain SharedPreferences)
- Password validation: 8+ chars, 1 uppercase, 1 lowercase, 1 digit
- Account lockout after 10 failed logins
- No open redirect in any navigation
```

---

## P3: Dictionary

```
Implement the dictionary feature for Wadjet Android.

Reference: planning/api-mapping.md (DictionaryApiService), planning/screens.md (S06, S07, S08)

Dictionary Screen (S06): 3-tab container (Browse, Learn, Write)

Browse Tab:
1. GET /api/dictionary?category=A&type=uniliteral&page=1&per_page=30
2. Category chips: A-Z letters + "All" (horizontal scroll)
3. Type filter chips: Uniliteral, Biliteral, Triliteral, Logogram
4. Search bar → GET /api/dictionary?q=vulture
5. Sign grid (LazyVerticalGrid, 3 columns): Noto glyph, Gardiner code, name
6. Tap sign → Sign Detail Bottom Sheet (S07)
7. Pagination: load more on scroll end
8. Room cache for offline access (SignEntity + SignDao with FTS)

Sign Detail Bottom Sheet (S07):
1. Large glyph (80dp, Noto font, gold)
2. Code + category + type badge
3. Transliteration, phonetic, meaning
4. TTS pronunciation: POST /api/audio/speak { text: phonetic, lang: "en" }
5. Fun fact + examples sections
6. Actions: Favorite (Firestore), Copy (clipboard), Share

Learn Tab (Lessons):
1. 5 lesson cards (levels 1-5) rendered locally; each opens GET /api/dictionary/lesson/{level}
2. Lesson Screen (S08): teaching section + exercise section
3. Exercise types: multiple-choice glyph grid, matching
4. Score tracking + glyphs mastered counter
5. Progress synced to Firestore lesson_progress subcollection

Write Tab:
1. Text input + mode selector (Alphabetic, Smart, MdC)
2. POST /api/write { text, mode }
3. Display hieroglyph output (large, Noto font)
4. Glyph palette: scrollable grid of tappable signs
5. Copy + share rendered hieroglyphs
```

---

## P4: Scanner

```
Implement the hieroglyph scanner for Wadjet Android.

Reference: planning/api-mapping.md (ScanApiService), planning/screens.md (S04, S05)

Camera:
1. CameraX preview (full screen with gold corner bracket overlay)
2. Capture button (large gold FAB)
3. Gallery picker (PhotoPicker API)
4. Camera permission handling with rationale

Scan pipeline:
1. Image captured → compress to max 1024px, 85% JPEG quality
2. POST /api/scan (multipart: file + mode="auto")
3. Animated progress steps: Detecting → Classifying → Transliterating → Translating
4. Parse response → ScanResult domain model

Results display (S05):
1. Annotated image (decode base64 JPEG → Bitmap → zoomable Image)
2. Detected glyphs grid: Unicode glyph (Noto font), Gardiner code, confidence bar
3. Transliteration (JetBrains Mono, gold)
4. Translation toggle EN↔AR
5. Timing stats
6. Actions: Save history (Firestore), Share (image + text), Scan again

History:
1. Save scan summary to Firestore users/{uid}/scan_history
2. Save full results JSON to Room (too large for Firestore)
3. History list: thumbnail, date, glyph count, confidence
4. Tap to view, swipe to delete
```

---

## P5: Landmarks

```
Implement the landmark explorer for Wadjet Android.

Reference: planning/api-mapping.md (LandmarkApiService), planning/screens.md (S09, S10, S11)

Explore Screen (S09):
1. GET /api/explore?category=pharaonic&city=Luxor&page=1&per_page=20
2. Category chips: All, Pharaonic, Islamic, Coptic, Greco-Roman, Museum, Natural
3. City dropdown filter
4. Search bar → GET /api/explore?q=pyramid
5. Landmark card list: image, name, city badge, category badge, favorite heart
6. Pull-to-refresh + load more pagination
7. Room cache for offline browsing

Landmark Detail (S10):
1. GET /api/explore/{slug} → full landmark data
2. Collapsing toolbar with hero image carousel (HorizontalPager)
3. Name (EN + AR), category/city badges
4. Action row: Maps (Intent to Google Maps), Chat (navigate to S12 with slug),
   Favorite (toggle Firestore), Share (share link/text)
5. Tabs: Overview, History, Tips, Gallery
6. Recommendations row (horizontal scroll of related landmarks)
7. Image gallery: grid of landmark images, tap to fullscreen viewer

Identify Landmark (S11):
1. Reuse CameraX from scanner (same CameraPreview composable)
2. Capture → compress → POST /api/explore/identify (multipart)
3. Show top-3 matches: image, name, confidence %, city
4. Tap match → navigate to Landmark Detail (S10)

Map Integration:
- "Get Directions" button → Intent to Google Maps with lat/lng
- Optional: Compose Google Maps view with gold markers (Maps Compose SDK)

Favorites:
- Heart toggle on cards and detail → Firestore users/{uid}/favorites
- Favorites list visible in Dashboard (S15)
```

---

## P6: Chat

```
Implement the Thoth AI chatbot for Wadjet Android.

Reference: planning/api-mapping.md (ChatApiService), planning/screens.md (S12)

SSE Streaming:
1. POST /api/chat/stream { message, session_id, landmark? }
2. Response: text/event-stream
3. Parse: each "data: {\"text\": \"chunk\"}" line adds to message
4. "data: [DONE]" terminates stream
5. Use OkHttp directly (NOT Retrofit) for SSE
6. Emit chunks via Kotlin Flow → collect in ViewModel → update UI state

Chat UI:
1. Message list (LazyColumn, reverseLayout)
2. User messages: right-aligned, Gold background, Night text
3. Bot messages: left-aligned, Surface background, with ibis avatar
4. Streaming: show blinking cursor (▌) at end of bot message while streaming
5. Markdown rendering in bot messages (bold, lists, code blocks)
6. Message input bar: TextField + mic button + send button

TTS:
1. Play button on each bot message
2. POST /api/audio/speak { text, lang: "en", context: "thoth_chat" }
3. Response 200 → WAV blob → play with MediaPlayer
4. Response 204 → use Android TextToSpeech with Orus-like voice

STT:
1. Mic button → Android SpeechRecognizer
2. Start listening → show recording indicator
3. Result → populate input field

Landmark context:
1. Navigate from Landmark Detail → Chat with slug parameter
2. Include landmark slug in chat request
3. Thoth discusses that specific landmark
```

---

## P7: Stories

```
Implement interactive stories for Wadjet Android.

Reference: planning/api-mapping.md (StoriesApiService), planning/screens.md (S13, S14)

Story List (S13):
1. GET /api/stories → grid of story cards
2. Each card: cover_glyph (large emoji), title (bilingual), difficulty badge, chapter count
3. Progress bar from Firestore story_progress
4. Difficulty filter chips: All, Beginner, Intermediate, Advanced
5. Free tier: first 3 stories accessible, rest show lock icon

Story Reader (S14):
1. GET /api/stories/{id} → full story JSON with chapters
2. Chapter progress bar (gold, top)
3. Scene image generation: POST /api/stories/{id}/chapters/{n}/image
   - Display with KenBurnsImage composable
   - Coil disk cache (don't re-generate)
4. Chapter title (Playfair, gold)
5. Paragraphs with inline glyph annotations:
   - Words with glyph_annotations → styled with gold underline
   - Tap annotated word → tooltip: glyph (large, Noto font), code, meaning, transliteration
6. Interactions (4 types):
   - choose_glyph: 2x2 grid of glyph buttons, one correct
   - write_word: TextField for Gardiner code input
   - glyph_discovery: Info card, tap to reveal, always "correct"
   - story_decision: 2-3 text buttons, each with different outcome text
   - POST /api/stories/{id}/interact { chapter_index, interaction_index, answer }
   - Show correct/incorrect feedback with explanation
7. Chapter navigation: Previous / Next buttons
8. Narration: TTS play button → POST /api/audio/speak { text, context: "story_narration" }
9. Score + glyphs learned counter at bottom
10. Progress saved to Firestore on chapter completion

BilingualText handling:
- Story JSON uses { en: "...", ar: "..." } for all text
- Use user's preferred_lang to select display text
```

---

## P8: Dashboard & Settings

```
Implement dashboard and settings for Wadjet Android.

Reference: planning/api-mapping.md (UserApiService), planning/screens.md (S15, S16, S17)
Reference: planning/firebase-schema.md for Firestore data paths

Dashboard Screen (S15):
1. GET /api/user/profile → user info (name, email, join date)
2. GET /api/user/stats → scan count, story progress, glyphs learned
3. Stat cards grid (2x2): Scans Today, Total Scans, Stories Done, Glyphs Learned
4. Recent Scans: horizontal scroll of thumbnails from Firestore scan_history
5. Favorites: sub-tabs (Landmarks, Glyphs, Stories) from Firestore favorites
6. Story Progress: list of started stories with progress bars

Settings Screen (S16):
1. Profile section: editable display name, read-only email, provider badge
   PATCH /api/user/profile { display_name }
2. Language selector (EN/AR): immediately applies locale + RTL
3. Password change (visible only for email auth users):
   PATCH /api/user/password { current_password, new_password }
4. TTS settings: enable toggle + speed slider (0.5x to 2.0x)
5. Storage: show cache size, "Clear Cache" button
6. About: Version, "Built by Mr Robot", Send Feedback link
7. Sign Out: Firebase signOut + clear tokens + navigate to Welcome
8. Delete Account: confirmation dialog → Firebase delete + DELETE /api/user/account

Feedback Screen (S17):
1. Category chips: Bug, Suggestion, Praise, Other
2. Message textarea (max 1000 chars, char counter)
3. Optional name + email fields
4. POST /api/feedback { category, message, name?, email? }
5. Success state: "Thank you" with animation
```

---

## P9: Offline & Polish

```
Implement offline support and polish for Wadjet Android.

Reference: planning/implementation-plan.md P9 tasks

Offline Mode:
1. NetworkMonitor: observe ConnectivityManager for network state (StateFlow)
2. OfflineIndicator: show gold bar at top "You're offline — showing cached data"
3. Graceful degradation per feature:
   - Dictionary: fully offline from Room (signs, lessons)
   - Landmarks: cached list + visited detail pages
   - Stories: cached story content, interactions queue for later sync
   - Scan: "Requires internet" screen with camera preview disabled
   - Chat: "Requires internet" with past messages still visible
4. Cache TTL: 24h for lists, 7d for detail pages, refresh on next online

Room Completeness:
1. All entities: SignEntity, LandmarkEntity, StoryEntity, ScanResultEntity
2. FTS search for dictionary (Room FTS4)
3. Data migration strategy (fallback to destructive migration in v1)

Arabic RTL:
1. Test every screen with ForceRTL developer setting
2. Hieroglyph text must stay LTR in CompositionLocalLayoutDirection override
3. Arabic body text uses Cairo font (via WadjetTypography.arabic variant)
4. Bidirectional text in mixed EN+AR content

Animation Polish:
1. Page transitions: fadeIn + slideInHorizontally
2. Card click: scale(0.98) press feedback
3. Shared element transitions for image → detail screen
4. Loading: shimmer placeholders on all data screens (ShimmerEffect composable)
5. Micro: heart pulse on favorite, send button scale on tap

Performance:
1. Baseline profiles: BaselineProfileRule for startup + critical paths
2. Coil: maxMemoryCacheSize(25%), diskCacheSize(100MB)
3. Stability annotations on data classes (@Stable, @Immutable)
4. derivedStateOf / remember with key for expensive computations

Accessibility:
1. contentDescription on all Image/Icon composables
2. Touch targets ≥ 44dp (Modifier.minimumInteractiveComponentSize)
3. semantics { heading() } on section titles
4. Announce screen changes with AccessibilityEvent
```

---

## P10: Testing & Release

```
Implement testing and prepare release for Wadjet Android.

Reference: planning/implementation-plan.md P10, planning/release-checklist.md

Unit Tests (src/test/):
1. ViewModels: Turbine for Flow testing, MockK for repositories
2. Repositories: MockK for API services, verify mapping logic
3. Token refresh: simulate 401 → refresh → retry (Mutex verification)
4. All tests use JUnit 5 + kotlinx.coroutines.test

Integration Tests (src/androidTest/):
1. Room DAO tests: in-memory database, verify queries
2. Retrofit: MockWebServer for each API service
3. Full auth flow: register → login → token refresh → logout

UI Tests (src/androidTest/):
1. ComposeTestRule for each screen
2. Critical flows: Auth → Scan → Results → Save (end-to-end)
3. RTL layout: verify no clipping or overlap in Arabic mode
4. Accessibility: check contentDescription, touch targets

ProGuard / R8 (release build):
1. Keep rules: Retrofit models, Room entities, Firebase models, Kotlinx Serialization
2. Verify release build on real device (R8 can break reflection)
3. Test all features after minification

FCM Push Notifications:
1. WadjetFirebaseMessaging.kt handles incoming messages
2. Notification types: story_update, new_landmark, scan_tip
3. Create notification channel "Wadjet Updates"
4. Deep link from notification tap → relevant screen

CI/CD:
1. .github/workflows/android.yml (from release-checklist.md)
2. GitHub Secrets: KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
3. Build → Lint → Test → Bundle Release → Upload AAB artifact

App Signing:
1. Generate release keystore (see release-checklist.md)
2. Configure signing in build.gradle.kts
3. Enroll in Play App Signing

Play Store (LAST — only after everything above works):
1. Adaptive icon: gold Eye of Wadjet on #0A0A0A
2. Feature graphic: 1024x500, black+gold branding
3. 8 screenshots per locale (EN + AR) — see release-checklist.md
4. Store listing text from release-checklist.md
5. Privacy policy hosted at public URL
6. Content rating + Data Safety Form
7. Upload AAB to internal testing track → test → promote to production
```

---

## Phase-Start Checklist (use before EACH phase)

```
I'm starting Phase P[N] of the Wadjet Android app.

Before writing code:
1. Read planning/CONSTITUTION.md — project rules and identity
2. Read planning/implementation-plan.md — Phase P[N] tasks
3. Read planning/architecture.md — relevant module structure
4. Read planning/api-mapping.md — API endpoints used in this phase
5. Read planning/design-system.md — components needed
6. Read planning/screens.md — UI wireframes for screens in this phase
7. Read planning/dependencies.md — correct dependency versions

Current state:
- Phases completed: P0..P[N-1]
- Working modules: [list]
- Firebase connected: Yes/No

Tasks for this phase:
[List from implementation-plan.md]

Output the complete implementation for each task, including:
- Kotlin source files with full code
- Gradle dependency additions (if any)
- Resource files (layouts, strings, drawables)
- Test stubs
```

---

## Debugging Prompts

### API Connection Issues
```
The Wadjet Android app can't connect to the backend API.
Base URL: https://nadercr7-wadjet-v2.hf.space
Error: [paste error]

Check:
1. Is the URL correct in BuildConfig?
2. Is the auth interceptor adding the Bearer token?
3. Is CSRF causing issues? (Mobile should skip CSRF)
4. Is the request format matching the API spec in planning/api-mapping.md?
5. Check OkHttp logs for the actual request/response
```

### Firebase Issues
```
Firebase Auth/Firestore issue in Wadjet Android.
Error: [paste error]

Check:
1. Is google-services.json present in app/ directory?
2. Are SHA fingerprints registered in Firebase Console?
3. Are Firestore security rules deployed (planning/firebase-schema.md)?
4. Is the Firestore path correct (users/{uid}/subcollection)?
5. Is the user authenticated before Firestore access?
```

### Design System Issues
```
The UI doesn't match the Wadjet web design.
Screen: [which screen]
Issue: [what's wrong]

Requirements:
- Background MUST be #0A0A0A (Night)
- Cards MUST be #141414 (Surface) with #2A2A2A border
- Primary accent MUST be #D4AF37 (Gold)
- Headings: Playfair Display
- Body: Inter
- No Material You dynamic colors
- Reference: planning/design-system.md
```
