# Wadjet Android — Implementation Plan

> Phased approach: foundation → features → polish → release.
> Each phase is independently shippable.

---

## Phase Overview

| Phase | Name | Description | Estimated Effort |
|-------|------|-------------|-----------------|
| P0 | Project Setup | Android Studio project, Gradle, Hilt, Firebase | Foundation |
| P1 | Design System | Theme, components, fonts, icons | Foundation |
| P2 | Auth & Navigation | Firebase Auth, navigation graph, bottom nav | Core |
| P3 | Dictionary | Browse + Search + Sign Detail + Lessons | Feature |
| P4 | Scanner | Camera, gallery, API scan, results display | Feature |
| P5 | Landmarks | Browse, detail, identify, map integration | Feature |
| P6 | Chat | Streaming SSE, TTS, STT, markdown rendering | Feature |
| P7 | Stories | List, reader, interactions, images, narration | Feature |
| P8 | Dashboard & Settings | Stats, history, favorites, profile, prefs | Feature |
| P9 | Offline & Polish | Room caching, offline mode, animations, RTL | Polish |
| P10 | Testing & Release | Tests, ProGuard, Play Store prep | Release |

---

## P0: Project Setup

### Tasks

**P0.1 — Create Android Studio Project**
- New project: Empty Compose Activity
- Package: `com.wadjet.app`
- Min SDK 26, Target SDK 35
- Kotlin 2.1.0, Compose BOM 2026.03.00
- Gradle Kotlin DSL

**P0.2 — Configure Multi-Module Gradle**
- Create module structure per `architecture.md`:
  - `:app`, `:core:designsystem`, `:core:domain`, `:core:data`, `:core:network`, `:core:database`, `:core:firebase`, `:core:ml`, `:core:common`, `:core:ui`
  - Feature modules: `:feature:auth`, `:feature:landing`, `:feature:scan`, `:feature:dictionary`, `:feature:explore`, `:feature:chat`, `:feature:stories`, `:feature:dashboard`, `:feature:settings`, `:feature:feedback`
- Version catalog (`libs.versions.toml`) for all dependencies
- Common build logic in `buildSrc` or convention plugins

**P0.3 — Add Dependencies**
```toml
# libs.versions.toml (key dependencies — see dependencies.md for complete catalog)
[versions]
kotlin = "2.1.0"
compose-bom = "2026.03.00"
hilt = "2.53.1"
retrofit = "2.11.0"
okhttp = "4.12.0"
room = "2.8.4"
coil = "3.0.4"
firebase-bom = "33.7.0"
onnxruntime = "1.20.0"
camerax = "1.4.1"
navigation = "2.9.7"
lifecycle = "2.10.0"
kotlinx-serialization = "1.7.3"
datastore = "1.1.1"
```

**P0.4 — Firebase Setup**
- Create Firebase project `wadjet-android`
- Add Android app with package name
- Download `google-services.json`
- Enable Auth (Google + Email), Firestore, Storage, Analytics, Crashlytics
- Deploy security rules from `firebase-schema.md`

**P0.5 — Configure Build Variants**
```kotlin
// build.gradle.kts (app)
buildTypes {
    debug {
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000\"")  // Emulator → localhost
    }
    release {
        buildConfigField("String", "BASE_URL", "\"https://nadercr7-wadjet-v2.hf.space\"")
        isMinifyEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

### Deliverable
- Project compiles and runs on emulator
- Blank screen with Wadjet theme applied
- Firebase connected (Analytics events arriving)
- All modules created with empty placeholder code

---

## P1: Design System

### Tasks

**P1.1 — Colors & Theme**
- Implement `WadjetColors.kt` (all hex values from `design-system.md`)
- Implement `WadjetTheme.kt` (Material 3 dark scheme override)
- System bar colors (Night status bar, Surface nav bar)

**P1.2 — Typography**
- Add font files to `res/font/`:
  - Playfair Display (semibold, bold)
  - Inter (regular, medium, semibold)
  - JetBrains Mono (regular)
  - Noto Sans Egyptian Hieroglyphs
  - Cairo (regular, medium, semibold, bold)
- Implement `WadjetTypography` + Arabic variant
- Custom text styles: `HieroglyphStyle`, `GardinerCodeStyle`, `GoldGradientText`

**P1.3 — Component Library**
- `WadjetButton` (primary gold)
- `WadjetGhostButton` (outlined)
- `WadjetCard` + `WadjetCardGlow`
- `WadjetTextField`
- `WadjetBadge` (4 variants)
- `WadjetTopBar`
- `WadjetBottomBar`
- `ShimmerEffect`
- `LoadingOverlay`
- `ErrorState` (empty state with retry)
- `WadjetToast` (snackbar replacement)

**P1.4 — Animations**
- Gold pulse modifier
- Fade up animated visibility
- Ken Burns image composable
- Shimmer loading effect
- Page transition animations

**P1.5 — Icons**
- Import Lucide icons (SVG → ImageVector) or add `compose-icons` dependency
- Create icon utilities

### Deliverable
- Design system showcase screen (dev-only) showing all components
- Screenshot matches web app aesthetic

---

## P2: Auth & Navigation

### Tasks

**P2.1 — Token Manager**
- `TokenManager` class with EncryptedSharedPreferences
- Store/retrieve: Firebase token, Wadjet access token, Wadjet refresh token
- Thread-safe refresh mechanism

**P2.2 — Firebase Auth Repository**
- Google Sign-In implementation (Credential Manager API)
- Email/Password register + login
- Auto token refresh
- Sign out + account deletion

**P2.3 — Wadjet Backend Auth Sync**
- On Google sign-in → `POST /api/auth/google`
- On email register → `POST /api/auth/register`
- On email login → `POST /api/auth/login`
- Token refresh interceptor
- Logout sync

**P2.4 — Navigation Setup**
- `NavHost` with all routes from `architecture.md`
- Bottom navigation bar (5 tabs)
- Auth state observer → redirect to Welcome when logged out
- Deep link support (future)

**P2.5 — Welcome Screen (S02)**
- Wadjet branding and feature preview
- Google Sign-In button
- Email sign-up/sign-in bottom sheets

**P2.6 — Splash Screen (S01)**
- Android 12+ Splash Screen API
- Wadjet eye logo animation
- Auth state check → navigate

**P2.7 — Landing Screen (S03)**
- Dual-path cards (Hieroglyphs + Landmarks)
- Quick action chips
- Welcome greeting with user name

### Deliverable
- Complete auth flow: Welcome → Google Sign-In → Landing
- Email registration and login working
- Bottom navigation between 5 tabs (placeholder content)
- Token refresh on 401

---

## P3: Dictionary

### Tasks

**P3.1 — Dictionary API Service**
- Retrofit interface: `DictionaryApiService`
- Data models: `SignDetail`, `DictionaryResponse`, `LessonResponse`

**P3.2 — Dictionary Repository**
- Fetch signs with pagination, category, type, search filters
- Room cache for offline access
- Stale-while-revalidate pattern

**P3.3 — Room DAOs**
- `SignDao` with FTS search
- Insert/query/clear operations
- Category and type filtering

**P3.4 — Browse Tab UI**
- Category chips (horizontal scroll)
- Type filter chips
- Search bar
- Sign grid (LazyVerticalGrid)
- Pagination (load more on scroll)

**P3.5 — Sign Detail Bottom Sheet (S07)**
- Large glyph display
- Gardiner code, category, type badge
- Transliteration, meaning, pronunciation
- TTS pronunciation button
- Fun fact, examples
- Favorite, copy, share actions

**P3.6 — Learn Tab (Lessons)**
- 5 lesson cards with progress
- Lesson screen (S08) with teaching + exercises
- Score tracking

**P3.7 — Write Tab**
- Text input + mode selector
- API call: `POST /api/write`
- Hieroglyph output display
- Glyph palette (grid of tappable glyphs)
- Copy, share actions

### Deliverable
- Full dictionary with browse, search, filter
- All 5 lessons playable
- Write tool functional (all 3 modes)
- Offline dictionary access

---

## P4: Scanner

### Tasks

**P4.1 — CameraX Setup**
- Camera permission request
- CameraX preview composable
- Image capture to file
- Gallery picker (PhotoPicker API)

**P4.2 — Scan API Service**
- Retrofit interface: `ScanApiService`
- Multipart file upload
- Image compression before upload (max 1024px, 85% JPEG)

**P4.3 — Scan Pipeline UI**
- Animated step indicators (4 steps)
- Progress bar
- Loading state with shimmer on captured image

**P4.4 — Scan Results Screen (S05)**
- Annotated image display (base64 → Bitmap, zoomable)
- Detected glyphs grid (code, glyph, confidence)
- Transliteration display (monospace, gold)
- Translation display (EN/AR toggle)
- Timing info
- Save to history, share, scan again actions

**P4.5 — Scan History**
- List of past scans (thumbnail, date, stats)
- Tap to view full results
- Swipe to delete
- Firestore sync for cloud backup

### Deliverable
- Camera scan working on real device
- Gallery upload working
- Full results display with all data
- History persisted

---

## P5: Landmarks

### Tasks

**P5.1 — Landmark API Service**
- Retrofit interface: `LandmarkApiService`
- All data models

**P5.2 — Landmark Repository**
- Paginated list with filters
- Detail fetch with Room cache
- Category tree

**P5.3 — Explore Screen (S09)**
- Landmark grid/list
- Category chips + city dropdown
- Search bar
- Pull-to-refresh

**P5.4 — Landmark Detail Screen (S10)**
- Hero image carousel (HorizontalPager)
- Collapsing toolbar with parallax
- Tabs: Overview, History, Tips, Gallery
- Recommendations carousel
- Action buttons: Maps, Chat, Favorite, Share

**P5.5 — Identify Landmark (S11)**
- Camera/gallery → upload → results
- Top-3 matches display
- Navigate to detail on tap

**P5.6 — Google Maps Integration**
- Maps intent for directions
- Optional: Map view with gold markers

**P5.7 — Favorites**
- Add/remove favorite (Firestore)
- Heart icon toggle with animation
- Favorites list in Dashboard

### Deliverable
- Browse 260+ landmarks with filtering
- Rich detail pages with images
- Landmark identification from photos
- Favorites sync across devices

---

## P6: Chat

### Tasks

**P6.1 — SSE Stream Parser**
- OkHttp SSE client for `POST /api/chat/stream`
- Parse `data: {"text": "..."}` chunks
- Handle `data: [DONE]` termination
- Error handling and retry

**P6.2 — Chat Repository**
- Message history management
- Session ID generation (UUID)
- Landmark context injection

**P6.3 — Chat Screen (S12)**
- Message list (LazyColumn, reverse)
- User messages (right, gold bg)
- Bot messages (left, surface bg, streaming animation)
- Typing indicator / blinking cursor during stream
- Markdown rendering in bot messages

**P6.4 — TTS Integration**
- Play button on bot messages
- Server TTS: `POST /api/audio/speak` → MediaPlayer
- Fallback: Android TextToSpeech
- Voice selection by context (Orus for chat)

**P6.5 — STT Integration**
- Mic button in input bar
- Android SpeechRecognizer
- Transcribed text auto-populates input

**P6.6 — Landmark Chat**
- From landmark detail → "Chat about this"
- Pre-inject landmark slug into chat request

### Deliverable
- Streaming chat with word-by-word display
- TTS on bot responses
- Voice input via STT
- Landmark-context chat

---

## P7: Stories

### Tasks

**P7.1 — Stories API Service + Repository**
- Fetch story list and individual stories
- Room cache for offline reading
- Interaction API calls

**P7.2 — Story List Screen (S13)**
- Story cards with cover glyph, title, difficulty badge
- Progress indicators per story
- Difficulty filter chips
- Free tier lock indicators

**P7.3 — Story Reader Screen (S14)**
- Chapter progress bar
- Scene image with Ken Burns animation
- Paragraph display with inline glyph annotations
- Tappable glyph words → tooltip/popup

**P7.4 — Interactions**
- `choose_glyph`: Multiple choice grid
- `write_word`: Text input for Gardiner code
- `glyph_discovery`: Info reveal card
- `story_decision`: Branching choice buttons
- Feedback display (correct/incorrect, explanation)

**P7.5 — Story Images**
- Request AI generation: `POST /api/stories/{id}/chapters/{n}/image`
- Display with Ken Burns
- Coil disk cache

**P7.6 — Story Narration**
- Chapter TTS: server Aoede voice
- Play/pause/progress controls
- Auto-advance option

**P7.7 — Progress Tracking**
- Chapter completion, score, glyphs learned
- Firestore sync
- Resume from last chapter

### Deliverable
- All 12 stories readable
- 4 interaction types working
- AI scene images generated
- TTS narration
- Progress persisted and synced

---

## P8: Dashboard & Settings

### Tasks

**P8.1 — Dashboard Screen (S15)**
- Stats cards (scans today, total, stories, glyphs)
- Recent scans horizontal carousel
- Favorites tabs (landmarks, glyphs, stories)
- Story progress list

**P8.2 — Settings Screen (S16)**
- Profile section (name editable, email read-only)
- Language selector (EN/AR)
- Password change (email users only)
- TTS settings (enable, speed)
- Storage management (clear cache)
- About section
- Sign out + delete account

**P8.3 — Feedback Screen (S17)**
- Category chips
- Message textarea
- Optional name/email
- Submit → API

### Deliverable
- Complete user dashboard
- All settings functional
- Language switch applies immediately
- Feedback submission

---

## P9: Offline & Polish

### Tasks

**P9.1 — Room Database Complete**
- All entities and DAOs from `firebase-schema.md`
- Migration strategy
- Cache TTL enforcement

**P9.2 — Offline Mode**
- Detect network state (ConnectivityManager)
- Graceful degradation:
  - Dictionary: fully offline
  - Landmarks: cached list, cached visited details
  - Stories: cached content, interactions work
  - Scan: shows "Requires internet" (unless on-device ONNX)
  - Chat: shows "Requires internet"
- Offline indicator in UI

**P9.3 — Arabic RTL**
- Test ALL screens in RTL mode
- Fix any layout issues
- Ensure hieroglyph sections stay LTR within RTL container
- Cairo font for Arabic body text

**P9.4 — Animations Polish**
- Smooth page transitions
- Card press feedback
- Pull-to-refresh animation
- Loading shimmer placeholders on all data screens
- Micro-interactions (favorite heart, send button)

**P9.5 — Performance**
- Image loading optimization (Coil memory/disk cache limits)
- Lazy loading on lists
- Reduce recomposition (stability annotations)
- Startup performance (baseline profiles)

**P9.6 — Accessibility**
- Content descriptions on all images
- Touch targets ≥ 44dp
- Screen reader navigation
- Sufficient color contrast (gold on night passes WCAG AA)

### Deliverable
- Full offline experience for cached data
- RTL Arabic layout
- Smooth animations everywhere
- Accessibility compliant

---

## P10: Testing & Release

### Tasks

**P10.1 — Unit Tests**
- ViewModel tests with Turbine
- Repository tests with MockK
- Token refresh logic tests

**P10.2 — Integration Tests**
- Room DAO tests
- Retrofit API tests with MockWebServer
- Firebase Auth mock tests

**P10.3 — UI Tests**
- Compose testing for each screen
- User flow tests (auth → scan → results)
- RTL layout tests

**P10.4 — ProGuard / R8**
- Keep rules for Retrofit models
- Keep rules for Room entities
- Keep rules for Firebase models
- Test release build on real device

**P10.5 — FCM Push Notifications**
- `WadjetFirebaseMessaging.kt` (already in project structure)
- Handle notification types: story_update, new_landmark, scan_tip
- Create notification channel "Wadjet Updates"
- Deep link from notification tap → relevant screen
- FCM token registration with backend (future)

**P10.6 — CI/CD**
- GitHub Actions workflow (see `release-checklist.md` for YAML):
  - Build → Unit Tests → Lint → Assemble Release
- GitHub Secrets for keystore, passwords, key alias
- Artifact upload (debug APK, release AAB)

**P10.7 — App Signing & Versioning**
- Generate release keystore (`keytool -genkeypair`)
- Configure signing in `build.gradle.kts`
- Enroll in Google Play App Signing
- Versioning policy: `versionCode` increments every upload, `versionName` follows semver
- Back up keystore to encrypted storage (NEVER commit to Git)

**P10.8 — Play Store Submission (LAST STEP)**
- App icon (Wadjet eye, gold on dark)
- Feature graphic (gold & black branding)
- Screenshots (all major screens, EN + AR)
- Store listing text (EN + AR)
- Privacy policy hosted at public URL
- Content rating questionnaire
- Data Safety Form (see `release-checklist.md`)
- Upload AAB to internal testing → test → promote to production

### Deliverable
- Test suite passing
- CI pipeline configured
- Signed release AAB
- Play Store listing live

---

## Dependency Order

```
P0 (Setup)
 └→ P1 (Design System)
     └→ P2 (Auth & Nav)
         ├→ P3 (Dictionary)  ←─ can start immediately after P2
         ├→ P4 (Scanner)     ←─ can start after P2 (parallel with P3)
         ├→ P5 (Landmarks)   ←─ can start after P2 (parallel with P3/P4)
         ├→ P6 (Chat)        ←─ can start after P2
         └→ P7 (Stories)     ←─ can start after P2
              └→ P8 (Dashboard) ←─ needs P3-P7 data models
                   └→ P9 (Polish) ←─ needs all features
                        └→ P10 (Release) ←─ needs everything
```

Phases P3–P7 are **independently buildable** and can be done in any order. P4 (Scanner) is recommended first as it's the core feature.
