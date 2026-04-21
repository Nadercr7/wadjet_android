# Full Testing and UX Quality Audit - Wadjet Android

> **ZERO HALLUCINATION PROTOCOL**: This audit uses staged investigation.
> Each stage writes its findings to a file BEFORE the next stage starts.
> The final planning phase reads ALL findings files to produce output.
> No memory loss. No guessing. No skipping.

---

## What You Are

A senior Android QA architect and automation engineer who knows:
- Jetpack Compose UI testing (semantics, accessibility, interaction)
- Android Emulator automation (ADB, UIAutomator, Python agent scripts)
- Screenshot regression testing (Roborazzi, Compose Preview)
- Unit, integration and E2E testing
- How a real user thinks and what confuses them
- API contract testing (Retrofit vs backend)
- Offline behavior, edge cases, error handling, security
- RTL layouts (Arabic), dark mode, accessibility, performance

Your job: investigate every screen, button, flow, API call, error state and edge case in the Wadjet app. Produce a complete test plan covering 100% of the app, plus document every UX problem you find.

---

## The App

Wadjet is an Egyptian archaeology Android app (Pure Jetpack Compose, zero XML layouts) backed by a FastAPI server on HuggingFace Spaces.

**Package**: `com.wadjet.app`
**Backend**: `https://nadercr7-wadjet-v2.hf.space`
**Min SDK**: 26 | **Target SDK**: 35 | **Compile SDK**: 35

### Architecture
```
app (main + navigation + DI)
  core/designsystem    # Theme, colors, typography, 15+ reusable components, 10 animations
  core/domain          # Domain models (9) + repository interfaces (9)
  core/data            # Repository implementations (9) + UserPreferencesDataStore
  core/network         # 11 Retrofit services (37+ endpoints) + interceptors + TokenManager
  core/database        # Room DB v6 (6 tables, 5 DAOs, FTS5 search)
  core/firebase        # Firebase Auth, Messaging, Crashlytics, Analytics, Google Sign-In
  core/ml              # ML placeholder
  core/common          # EgyptianPronunciation, NetworkMonitor, utils
  core/ui              # Shared-transition composable locals
  feature/auth         # Welcome, Login, Register, Forgot Password
  feature/landing      # Home/landing page
  feature/scan         # Hieroglyph scanning + results + history
  feature/dictionary   # Browse, Learn, Translate, Write (4 tabs)
  feature/explore      # Landmarks + identify (image upload)
  feature/chat         # Thoth AI chat (SSE streaming)
  feature/stories      # Stories list + reader (chapters + interactions)
  feature/dashboard    # User dashboard, favorites, stats
  feature/settings     # Settings, profile
  feature/feedback     # Feedback form
```

### 20 Routes (Navigation Graph)
| Route | Params |
|---|---|
| `Route.Splash` | none |
| `Route.Welcome` | none |
| `Route.Landing` | none |
| `Route.Hieroglyphs` | none |
| `Route.Scan` | none |
| `Route.ScanResult` | `scanId: String` |
| `Route.ScanHistory` | none |
| `Route.Dictionary` | `initialTab: Int = 0`, `prefillGlyph: String? = null` |
| `Route.DictionarySign` | `code: String` |
| `Route.Lesson` | `level: Int` |
| `Route.Explore` | none |
| `Route.LandmarkDetail` | `slug: String` |
| `Route.Identify` | none |
| `Route.Chat` | none |
| `Route.ChatLandmark` | `slug: String` |
| `Route.Stories` | none |
| `Route.StoryReader` | `storyId: String` |
| `Route.Dashboard` | none |
| `Route.Settings` | none |
| `Route.Feedback` | none |

**Bottom nav tabs**: HOME, HIEROGLYPHS, EXPLORE, STORIES, THOTH

### 37+ API Endpoints
**Auth (6)**: register, login, google, refresh, logout, forgot-password
**Scan (1)**: multipart file upload into AI detection
**Dictionary (5)**: list, categories, alphabet, lesson/{level}, {code}
**Landmarks (5)**: list, categories, {slug}, {slug}/children, identify (multipart)
**Chat (2)**: stream (SSE via OkHttp), clear
**Stories (4)**: list, {storyId}, interact, chapter image
**User (11)**: profile (GET/PATCH), password, history, favorites (CRUD), stats, progress (GET/POST), limits
**Feedback (1)**: submit
**Translate (1)**: translate
**Audio (2)**: speak (TTS), stt (speech-to-text)
**Write (2)**: write, palette

### 20 ViewModels
Auth, Landing, HieroglyphsHub, Scan, ScanResult, History, Dictionary, SignDetail, Lesson, Translate, Write, Explore, Detail, Identify, Chat, Stories, StoryReader, Dashboard, Settings, Feedback

### Room DB (6 tables, 5 DAOs)
Signs (+ FTS5), ScanResults, Landmarks, Categories, StoryProgress

### Existing Tests (12 files)
| File | Type |
|---|---|
| `EgyptianPronunciationTest.kt` | Unit (core:common) |
| `AuthRepositoryImplTest.kt` | Unit (core:data) |
| `DictionaryRepositoryImplTest.kt` | Unit (core:data) |
| `AuthInterceptorTest.kt` | Unit (core:network) |
| `RateLimitInterceptorTest.kt` | Unit (core:network) |
| `TokenAuthenticatorTest.kt` | Unit (core:network) |
| `AuthViewModelTest.kt` | Unit (feature:auth) |
| `DictionaryViewModelTest.kt` | Unit (feature:dictionary) |
| `ExploreViewModelTest.kt` | Unit (feature:explore) |
| `GardinerUnicodeTest.kt` | Unit (feature:scan) |
| `SignDaoTest.kt` | Instrumented (core:database) |
| `LandmarkDaoTest.kt` | Instrumented (core:database) |

**ZERO tests for**: chat, dashboard, feedback, landing, settings, stories, core:firebase, core:ui, core:designsystem, navigation, E2E

### Testing Stack Already In Place
| Tool | Catalog Key | What For |
|---|---|---|
| JUnit 4 | `libs.junit` | Test runner |
| MockK 1.13.13 | `libs.mockk` | Mocking |
| Turbine 1.2.0 | `libs.turbine` | Flow testing |
| Coroutines Test | `libs.kotlinx.coroutines.test` | Dispatcher control |
| MockWebServer | `libs.okhttp.mockwebserver` | HTTP testing |
| Room Testing | `libs.room.testing` | Migration testing |
| Compose UI Test | `libs.compose.ui.test` | Compose semantics testing |
| Hilt Testing | `libs.hilt.testing` | DI in tests |
| Espresso | `libs.espresso.core` | UI assertions |

### Emulator Agent Scripts (9 Python scripts)
Location: `D:\Personal attachements\Repos\23-Android-Kotlin\awesome-android-agent-skills\.github\skills\testing_and_automation\android-emulator-skill\scripts\`

Shorthand used in this prompt: `$SCRIPTS` = that full path.

| Script | What It Does | Key Flags |
|---|---|---|
| `screen_mapper.py` | Dump UI hierarchy, list all interactive elements | `--verbose`, `--json` |
| `navigator.py` | Find elements by text/id/class, tap, enter text | `--find-text`, `--find-id`, `--find-type`, `--tap`, `--enter-text`, `--json` |
| `gesture.py` | Swipe, scroll, custom gestures | `--swipe up/down/left/right`, `--scroll`, `--duration`, `--json` |
| `keyboard.py` | Key events and hardware buttons | `--key BACK/HOME/ENTER/TAB`, `--text "string"`, `--json` |
| `app_launcher.py` | Launch, kill, install, uninstall apps | `--launch`, `--terminate`, `--install`, `--uninstall`, `--list`, `--json` |
| `emulator_manage.py` | Boot, shutdown, list AVDs | `--list`, `--boot`, `--shutdown`, `--json` |
| `build_and_test.py` | Gradle build, run tests, parse results | `--task assembleDebug/installDebug/testDebug/connectedCheck`, `--clean`, `--json` |
| `log_monitor.py` | Logcat with smart filtering | `--package`, `--tag`, `--priority E/W/I`, `--duration`, `--json` |
| `emu_health_check.ps1` | Check ADB, emulator, Java, Gradle, ANDROID_HOME | (no flags, just run it) |

**How to use them**:
```bash
python $SCRIPTS/screen_mapper.py --json               # see what's on screen (structured)
python $SCRIPTS/navigator.py --find-text "Login" --tap # find and tap element
python $SCRIPTS/navigator.py --find-id "btn_scan" --tap
python $SCRIPTS/navigator.py --find-type EditText --enter-text "user@test.com"
python $SCRIPTS/gesture.py --swipe up                  # scroll down
python $SCRIPTS/gesture.py --swipe left                # next page/tab
python $SCRIPTS/keyboard.py --key BACK                 # press back button
python $SCRIPTS/keyboard.py --text "hello world"       # type text
python $SCRIPTS/app_launcher.py --launch com.wadjet.app
python $SCRIPTS/app_launcher.py --terminate com.wadjet.app
python $SCRIPTS/app_launcher.py --install app/build/outputs/apk/debug/app-debug.apk
python $SCRIPTS/emulator_manage.py --boot Pixel_8
python $SCRIPTS/emulator_manage.py --shutdown
python $SCRIPTS/build_and_test.py --task assembleDebug --json
python $SCRIPTS/build_and_test.py --task installDebug
python $SCRIPTS/build_and_test.py --task testDebugUnitTest --json  # run + parse results
python $SCRIPTS/build_and_test.py --task connectedDebugAndroidTest --json
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 10 --json
python $SCRIPTS/log_monitor.py --package com.wadjet.app --tag "Choreographer" --duration 30
```

### Emulator Setup
- AVD: `Pixel_8` (API 37, x86_64, Google Play)
- Location: `D:\AndroidAVD\Pixel_8.avd`
- HW Acceleration: WHPX 10.0.26200 (working)
- ANDROID_HOME: `C:\Users\Nader\AppData\Local\Android\Sdk`
- ADB, emulator, cmdline-tools all in PATH

### Known User-Reported UX Issues
1. **Landmarks upload button**: placed somewhere unclear, looks unimportant, doesn't stand out
2. **Same icon for different uploads**: hieroglyph scan upload and landmark identify upload show the same indicator. They should look different so users know which one they're using.

### Previous Specs (for context, don't repeat their work)
- `001-logic-parity/` - Logic gap analysis (some fixed, some open)
- `002-ux-fixes/` - Quick UX fixes
- `003-ux-redesign/` - Full UX redesign planning
- `004-logic-quality/` - Logic and quality audit with Egyptology verification

### Repos Available Locally
```
D:\Personal attachements\Repos\23-Android-Kotlin\
  awesome-android-agent-skills/    # Emulator scripts + testing SKILL.md
  nowinandroid/                     # Google's reference app (testing gold standard)
  compose-samples/                  # Official Compose samples
  architecture-samples/             # Google architecture samples
  Jepack-Compose-Starter/           # Compose starter patterns
  AwesomeUI/                        # UI component gallery
  android-showcase/                 # Clean arch showcase
  kotlin-coroutines-and-flow/       # Coroutine testing patterns
  awesome-kotlin/                   # Kotlin ecosystem reference
  Jepack-Compose-Movie/             # Movie app (testing example)
  android-developer-roadmap/        # Roadmap reference
  compose-multiplatform-core/       # KMP Compose reference
D:\Personal attachements\Repos\03-Anthropic\
  claude-cookbooks/                 # AI testing patterns
```

### Online references to search when needed:
- https://github.com/android/nowinandroid - NiA testing (Roborazzi, Hilt, Screenshot)
- https://developer.android.com/develop/ui/compose/testing - Compose testing docs
- https://developer.android.com/training/testing - Android testing fundamentals
- https://github.com/nicbell/roborazzi-compose-preview-test - Roborazzi + Compose Preview
- https://mockk.io - MockK documentation
- https://cashapp.github.io/turbine/ - Turbine Flow testing
- https://maestro.mobile.dev - Maestro E2E testing (alternative)

---

# INVESTIGATION PROTOCOL

> **How this works:**
> 1. Do Stage 1, write findings to `_investigation/stage-01-*.md`
> 2. Do Stage 2, write findings to `_investigation/stage-02-*.md`
> 3. Repeat for all 18 stages
> 4. After ALL stages are done, read ALL `_investigation/stage-*.md` files
> 5. Produce the final planning files from the complete data
>
> **NEVER skip writing a stage file. NEVER jump to the next stage without writing.**
> **NEVER start the final planning phase without re-reading ALL stage files.**

Write all stage files to: `.specify/specs/005-full-testing/_investigation/`

---

## Stage 1: Existing Test Audit (What We Already Have)
**Output: `_investigation/stage-01-existing-tests.md`**

### First, check the environment:
```bash
powershell $SCRIPTS/../emu_health_check.ps1
```

### Read ALL 12 existing test files:
```
core/common/src/test/java/com/wadjet/core/common/EgyptianPronunciationTest.kt
core/data/src/test/java/com/wadjet/core/data/repository/AuthRepositoryImplTest.kt
core/data/src/test/java/com/wadjet/core/data/repository/DictionaryRepositoryImplTest.kt
core/network/src/test/java/com/wadjet/core/network/AuthInterceptorTest.kt
core/network/src/test/java/com/wadjet/core/network/RateLimitInterceptorTest.kt
core/network/src/test/java/com/wadjet/core/network/TokenAuthenticatorTest.kt
feature/auth/src/test/java/com/wadjet/feature/auth/AuthViewModelTest.kt
feature/dictionary/src/test/java/com/wadjet/feature/dictionary/DictionaryViewModelTest.kt
feature/explore/src/test/java/com/wadjet/feature/explore/ExploreViewModelTest.kt
feature/scan/src/test/java/com/wadjet/feature/scan/GardinerUnicodeTest.kt
core/database/src/androidTest/java/com/wadjet/core/database/dao/SignDaoTest.kt
core/database/src/androidTest/java/com/wadjet/core/database/dao/LandmarkDaoTest.kt
```

### Run existing tests using the build script:
```bash
cd "D:\Personal attachements\Projects\Wadjet-Android"
python $SCRIPTS/build_and_test.py --task testDebugUnitTest --json
```

### Write to stage file:
```markdown
# Stage 1: Existing Test Audit

## Environment Health Check
[Paste emu_health_check output]

## Test Inventory
| # | File | Module | Test Count | All Passing? | Coverage Notes |
|---|------|--------|------------|--------------|----------------|

## Test Quality Assessment
For each file:
- Are assertions thorough or surface-level?
- Edge cases covered?
- Error paths tested?
- Naming quality? (descriptive backtick names vs generic)

## Test Patterns in Use
- Framework: JUnit 4 with @Test, @Before, @After
- Mocking: MockK (mockk, coEvery, coVerify, relaxed)
- Coroutines: runTest {}, StandardTestDispatcher, advanceUntilIdle
- Flows: Turbine (flow.test { awaitItem() })
- HTTP: MockWebServer
- Room: inMemoryDatabaseBuilder + AndroidJUnit4
- Naming: Backtick descriptive names

## Modules With Zero Tests
[List every module/class that has no test coverage]

## Gradle Test Config
[What's in each module's build.gradle.kts for testing?]

## Failed Tests
[Any failures from the build_and_test.py run?]
```

---

## Stage 2: Screen-by-Screen UX Walkthrough (Emulator)
**Output: `_investigation/stage-02-ux-walkthrough.md`**

This stage uses the actual emulator to SEE and INTERACT with the app like a real user.

### Setup:
```bash
# 1. Boot emulator
python $SCRIPTS/emulator_manage.py --boot Pixel_8

# 2. Disable animations for reliable, faster testing
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

# 3. Wait for boot, then do a CLEAN build and install
cd "D:\Personal attachements\Projects\Wadjet-Android"
python $SCRIPTS/build_and_test.py --task installDebug --clean

# 4. Launch the app
python $SCRIPTS/app_launcher.py --launch com.wadjet.app

# 5. Verify it's running (check for crashes immediately)
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 5 --json

# 6. Take a screenshot of the initial screen as evidence
adb shell screencap /sdcard/005-evidence/stage02-initial.png
adb pull /sdcard/005-evidence/ .specify/specs/005-full-testing/_investigation/screenshots/
```

### Evidence capture (do this for EVERY screen you visit):
```bash
# After mapping each screen, take a screenshot for the record
adb shell mkdir -p /sdcard/005-evidence
adb shell screencap /sdcard/005-evidence/stage02-[screen-name].png
# Pull all at the end of the stage:
adb pull /sdcard/005-evidence/ .specify/specs/005-full-testing/_investigation/screenshots/
```

### For EVERY screen (all 18 + sub-screens), do this sequence:
```bash
# Navigate to the screen
python $SCRIPTS/navigator.py --find-text "<tab or button text>" --tap

# Wait a second for content to load, then map the screen
python $SCRIPTS/screen_mapper.py --json

# Check for errors in logcat
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 3 --json

# If the screen has scrollable content, scroll down
python $SCRIPTS/gesture.py --swipe up --json
python $SCRIPTS/screen_mapper.py --json  # map again after scroll

# If the screen has text inputs, test typing
python $SCRIPTS/navigator.py --find-type EditText --enter-text "test input"
python $SCRIPTS/keyboard.py --key ENTER

# Press back to test navigation
python $SCRIPTS/keyboard.py --key BACK
```

### For each screen, document:
```markdown
### Screen: [Name]
**Route**: Route.XXX
**How to reach**: [step-by-step navigation]

#### Elements Found (from screen_mapper --json):
| Element | Type | Text | Resource ID | Clickable? | Content Description |

#### UX Issues:
- [ ] [Be specific: "Upload button is a 24dp icon with no label in the top-right, easy to miss"]

#### Interaction Test:
- [ ] Tapped every button - expected result?
- [ ] Scroll works?
- [ ] Back navigation works?
- [ ] Loading state visible?
- [ ] Error state reachable?
- [ ] Empty state reachable?

#### Accessibility:
- [ ] Content descriptions on all images/icons?
- [ ] Touch targets >= 48dp?
- [ ] Contrast good enough?
```

### Critical screens to test extra carefully:
1. **Welcome** - Login/Register flow (bottom sheets)
2. **Landing** - All cards, CTAs, navigation
3. **HieroglyphsHub** - Scan CTA, Dictionary CTA, Write CTA
4. **Scan then ScanResult** - Upload image, see results
5. **Dictionary** - All 4 tabs (Browse, Learn, Translate, Write)
6. **DictionarySign** - Sign detail, pronunciation play button
7. **Explore then LandmarkDetail** - List, filter, detail
8. **Identify** - Upload image (THE CONFUSING BUTTON - document exactly what's wrong)
9. **Chat** - Send message, receive stream, try STT
10. **Stories then StoryReader** - List, chapters, interactions
11. **Dashboard** - Favorites, stats, history
12. **Settings** - Theme, language, profile, logout
13. **Feedback** - Form submission

### Also test these interactions:
```bash
# Pull-to-refresh (if any screen has it)
python $SCRIPTS/gesture.py --swipe down --duration 500

# Swipe between tabs (Dictionary has 4)
python $SCRIPTS/gesture.py --swipe left
python $SCRIPTS/gesture.py --swipe right

# Long press (if any elements support it)
# Type special characters
python $SCRIPTS/keyboard.py --text "ح ع ق"  # Arabic chars
python $SCRIPTS/keyboard.py --text "𓀀𓁐"      # Hieroglyphs (if supported)

# Test with empty inputs
python $SCRIPTS/navigator.py --find-type EditText --enter-text ""
python $SCRIPTS/keyboard.py --key ENTER

# Test with very long input
python $SCRIPTS/navigator.py --find-type EditText --enter-text "This is a very long test input that should stress test the text field boundaries and wrapping behavior in the UI to see if it clips or overflows or handles it properly"
```

### Write to stage file:
```markdown
# Stage 2: UX Walkthrough

## Screen Inventory (from live emulator)
| # | Screen | Route | Reachable? | Element Count | Issues |
|---|--------|-------|------------|---------------|--------|

## UX Issues Master List
| # | Screen | Severity | Issue | What It Looks Like Now | Suggested Fix |
|---|--------|----------|-------|------------------------|---------------|
| 1 | Identify | HIGH | Upload button hidden/unclear | [exact position, size from mapper] | [concrete fix] |
| 2 | Identify+Scan | HIGH | Same icon for different uploads | [describe both] | [different icons] |
...

## Accessibility Issues
| # | Screen | Element | Issue | Fix |
...

## Navigation Issues
| # | From | To | Issue |
...

## Missing States
| # | Screen | Missing State | Notes |
|---|--------|---------------|-------|
| | | Empty state | |
| | | Error state | |
| | | Loading state | |
| | | Offline state | |

## Logcat Errors During Walkthrough
[Every error message, with which screen it happened on]
```

---

## Stage 3: All 20 ViewModels - State and Logic Audit
**Output: `_investigation/stage-03-viewmodels.md`**

### Read ALL 20 ViewModel files:
```
app/src/main/java/com/wadjet/app/screen/HieroglyphsHubViewModel.kt
feature/auth/src/main/java/com/wadjet/feature/auth/AuthViewModel.kt
feature/landing/src/main/java/com/wadjet/feature/landing/LandingViewModel.kt
feature/scan/src/main/java/com/wadjet/feature/scan/ScanViewModel.kt
feature/scan/src/main/java/com/wadjet/feature/scan/ScanResultViewModel.kt
feature/scan/src/main/java/com/wadjet/feature/scan/HistoryViewModel.kt
feature/dictionary/src/main/java/com/wadjet/feature/dictionary/DictionaryViewModel.kt
feature/dictionary/src/main/java/com/wadjet/feature/dictionary/SignDetailViewModel.kt
feature/dictionary/src/main/java/com/wadjet/feature/dictionary/LessonViewModel.kt
feature/dictionary/src/main/java/com/wadjet/feature/dictionary/TranslateViewModel.kt
feature/dictionary/src/main/java/com/wadjet/feature/dictionary/WriteViewModel.kt
feature/explore/src/main/java/com/wadjet/feature/explore/ExploreViewModel.kt
feature/explore/src/main/java/com/wadjet/feature/explore/DetailViewModel.kt
feature/explore/src/main/java/com/wadjet/feature/explore/IdentifyViewModel.kt
feature/chat/src/main/java/com/wadjet/feature/chat/ChatViewModel.kt
feature/stories/src/main/java/com/wadjet/feature/stories/StoriesViewModel.kt
feature/stories/src/main/java/com/wadjet/feature/stories/StoryReaderViewModel.kt
feature/dashboard/src/main/java/com/wadjet/feature/dashboard/DashboardViewModel.kt
feature/settings/src/main/java/com/wadjet/feature/settings/SettingsViewModel.kt
feature/feedback/src/main/java/com/wadjet/feature/feedback/FeedbackViewModel.kt
```

### For each ViewModel, check:
1. **State management**: StateFlow? MutableState? Is initial state correct?
2. **Error handling**: Catches exceptions? Shows error to user? Can the user retry?
3. **Loading states**: Shows a loading indicator? Handles slow network?
4. **Cancellation**: viewModelScope.launch - is work cancelled properly?
5. **Double-submit protection**: Can user spam click a button? Is there a guard?
6. **Input validation**: Are inputs validated before making API calls?
7. **Race conditions**: Multiple rapid calls - safe?
8. **Memory leaks**: Any long-lived references? Uncleared subscriptions?

### Write to stage file:
```markdown
# Stage 3: ViewModel Audit

## ViewModel Matrix
| # | ViewModel | Has Test? | State Type | Error Handling | Loading | Double-Submit Guard | Issues |
|---|-----------|-----------|------------|----------------|---------|---------------------|--------|

## Detailed Findings Per ViewModel
### AuthViewModel
- State: [describe]
- Issues: [list]
- Test coverage: [what's tested, what's missing]

[Repeat for all 20]

## Cross-Cutting Issues
[Patterns that show up across multiple ViewModels]

## Test Requirements (what unit tests need to be written)
| # | ViewModel | Test Case | Priority |
```

---

## Stage 4: Network Layer - API Contract and Error Handling
**Output: `_investigation/stage-04-network.md`**

### Read ALL 11 API services + DTOs + interceptors:
```
core/network/src/main/java/com/wadjet/core/network/api/*.kt         (11 files)
core/network/src/main/java/com/wadjet/core/network/model/*.kt       (10 files)
core/network/src/main/java/com/wadjet/core/network/AuthInterceptor.kt
core/network/src/main/java/com/wadjet/core/network/RateLimitInterceptor.kt
core/network/src/main/java/com/wadjet/core/network/TokenManager.kt
core/network/src/main/java/com/wadjet/core/network/TokenAuthenticator.kt
core/network/src/main/java/com/wadjet/core/network/di/NetworkModule.kt
```

### Also read the API contract from 001:
```
.specify/specs/001-logic-parity/contracts/api-contract.md
```

### Test each endpoint against the live backend:
```bash
# All GET endpoints (no auth needed for most)
curl -s https://nadercr7-wadjet-v2.hf.space/api/dictionary | python -m json.tool | head -30
curl -s https://nadercr7-wadjet-v2.hf.space/api/dictionary/categories | python -m json.tool
curl -s https://nadercr7-wadjet-v2.hf.space/api/dictionary/alphabet | python -m json.tool | head -20
curl -s https://nadercr7-wadjet-v2.hf.space/api/landmarks | python -m json.tool | head -30
curl -s https://nadercr7-wadjet-v2.hf.space/api/landmarks/categories | python -m json.tool
curl -s https://nadercr7-wadjet-v2.hf.space/api/stories | python -m json.tool | head -30
curl -s https://nadercr7-wadjet-v2.hf.space/api/write/palette | python -m json.tool

# Error responses
curl -s -w "\n%{http_code}" https://nadercr7-wadjet-v2.hf.space/api/dictionary/NONEXISTENT
curl -s -w "\n%{http_code}" https://nadercr7-wadjet-v2.hf.space/api/user/profile  # should be 401
```

### Write to stage file:
```markdown
# Stage 4: Network Layer

## Live API vs DTO Comparison
| # | Endpoint | Backend Response Fields | DTO Fields | Match? | Missing/Extra |
|---|----------|------------------------|------------|--------|---------------|

## Error Response Handling
| # | Status Code | What Backend Sends | Does Android Handle It? | How? |
|---|-------------|-------------------|------------------------|------|
| | 400 | | | |
| | 401 | | | |
| | 403 | | | |
| | 404 | | | |
| | 429 | | | |
| | 500 | | | |
| | Network timeout | | | |
| | No internet | | | |
| | Malformed JSON | | | |

## Auth Flow
- Token storage: [where, how]
- Refresh flow: [automatic? manual?]
- Race condition: [concurrent requests during refresh?]
- Logout: [clears everything?]

## SSE Chat Stream
- Connection handling
- Error recovery
- Reconnection logic
- Message parsing

## Issues Found
```

---

## Stage 5: Database and Offline Resilience
**Output: `_investigation/stage-05-database-offline.md`**

### Read ALL database files:
```
core/database/src/main/java/com/wadjet/core/database/WadjetDatabase.kt
core/database/src/main/java/com/wadjet/core/database/dao/*.kt          (5 files)
core/database/src/main/java/com/wadjet/core/database/entity/*.kt       (6 files)
core/database/src/main/java/com/wadjet/core/database/di/DatabaseModule.kt
core/common/src/main/java/com/wadjet/core/common/ConnectivityManagerNetworkMonitor.kt
core/data/src/main/java/com/wadjet/core/data/datastore/UserPreferencesDataStore.kt
```

### Test SLOW network on emulator (before airplane mode):
```bash
# Simulate a slow 2G connection (high latency, low bandwidth)
adb emu network delay 1500
adb emu network speed gsm

# Navigate key screens and check: do loading indicators show? Does the app feel stuck?
python $SCRIPTS/navigator.py --find-text "Explore" --tap
python $SCRIPTS/screen_mapper.py --json  # check for loading spinner or progress bar
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority W --duration 10 --json

# Try Chat on slow network (SSE streaming should struggle)
python $SCRIPTS/navigator.py --find-text "Thoth" --tap
python $SCRIPTS/screen_mapper.py --json

# Reset to normal network
adb emu network delay 0
adb emu network speed full
```

### Test offline mode on emulator:
```bash
# Turn on airplane mode
adb shell settings put global airplane_mode_on 1
adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true

# Navigate every screen and document what works vs what breaks
python $SCRIPTS/app_launcher.py --launch com.wadjet.app
python $SCRIPTS/screen_mapper.py --json
# [navigate to each tab using navigator.py]
python $SCRIPTS/navigator.py --find-text "Hieroglyphs" --tap
python $SCRIPTS/screen_mapper.py --json
python $SCRIPTS/navigator.py --find-text "Explore" --tap
python $SCRIPTS/screen_mapper.py --json
python $SCRIPTS/navigator.py --find-text "Stories" --tap
python $SCRIPTS/screen_mapper.py --json
python $SCRIPTS/navigator.py --find-text "Thoth" --tap
python $SCRIPTS/screen_mapper.py --json

# Check for crashes
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 5 --json

# Turn airplane mode off
adb shell settings put global airplane_mode_on 0
adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false
```

### Write to stage file:
```markdown
# Stage 5: Database and Offline

## Room Schema
| Entity | Table | PK | Indexes | Issues |
...

## Offline Behavior Per Feature
| Feature | Has Cache? | Degrades Gracefully? | Error Shown? | Issues |
|---------|-----------|---------------------|-------------|--------|
| Dictionary | Room signs | ??? | ??? | |
| Landmarks | Room landmarks | ??? | ??? | |
| Scan History | Room scan_results | ??? | ??? | |
| Chat | No cache | ??? | ??? | |
| Stories | StoryProgress only | ??? | ??? | |
| Auth | Token in memory | ??? | ??? | |

## Slow Network (2G) Behavior
| Feature | Shows Loading? | Timeout Handling? | Retry Option? | Issues |
|---------|---------------|------------------|---------------|--------|
| Dictionary | ??? | ??? | ??? | |
| Landmarks | ??? | ??? | ??? | |
| Chat (SSE) | ??? | ??? | ??? | |
| Scan | ??? | ??? | ??? | |
| Stories | ??? | ??? | ??? | |

## Migration Safety
[Are all migrations tested? Any destructive fallback?]

## FTS5 Search Testing
[Does local search work? Arabic characters? Special chars?]

## DataStore
| Preference | Type | Tested? | Issues |
```

---

## Stage 6: UI Composition - Every Screen's Compose Code
**Output: `_investigation/stage-06-compose-screens.md`**

### Read ALL 18 screen composables:
```
feature/auth/src/main/java/com/wadjet/feature/auth/screen/WelcomeScreen.kt
feature/landing/src/main/java/com/wadjet/feature/landing/screen/LandingScreen.kt
app/src/main/java/com/wadjet/app/screen/HieroglyphsHubScreen.kt
feature/scan/src/main/java/com/wadjet/feature/scan/screen/ScanScreen.kt
feature/scan/src/main/java/com/wadjet/feature/scan/screen/ScanResultScreen.kt
feature/scan/src/main/java/com/wadjet/feature/scan/screen/ScanHistoryScreen.kt
feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/DictionaryScreen.kt
feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/DictionarySignScreen.kt
feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/LessonScreen.kt
feature/explore/src/main/java/com/wadjet/feature/explore/screen/ExploreScreen.kt
feature/explore/src/main/java/com/wadjet/feature/explore/screen/LandmarkDetailScreen.kt
feature/explore/src/main/java/com/wadjet/feature/explore/screen/IdentifyScreen.kt
feature/chat/src/main/java/com/wadjet/feature/chat/screen/ChatScreen.kt
feature/stories/src/main/java/com/wadjet/feature/stories/screen/StoriesScreen.kt
feature/stories/src/main/java/com/wadjet/feature/stories/screen/StoryReaderScreen.kt
feature/dashboard/src/main/java/com/wadjet/feature/dashboard/screen/DashboardScreen.kt
feature/settings/src/main/java/com/wadjet/feature/settings/screen/SettingsScreen.kt
feature/feedback/src/main/java/com/wadjet/feature/feedback/screen/FeedbackScreen.kt
```

### Also read sub-screens, sheets and tabs:
```
feature/auth/src/main/java/com/wadjet/feature/auth/screen/LoginSheet.kt
feature/auth/src/main/java/com/wadjet/feature/auth/screen/RegisterSheet.kt
feature/auth/src/main/java/com/wadjet/feature/auth/screen/ForgotPasswordSheet.kt
feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/BrowseTab.kt
feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/LearnTab.kt
feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/TranslateTab.kt
feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/WriteTab.kt
feature/dictionary/src/main/java/com/wadjet/feature/dictionary/screen/SignDetailSheet.kt
feature/settings/src/main/java/com/wadjet/feature/settings/screen/SettingsQuickDialog.kt
```

### For each screen, check:
1. **Semantics**: Are `contentDescription`, `testTag`, `semantics` blocks there? (needed for testing)
2. **Accessibility**: Touch targets, contrast, screen readers
3. **State rendering**: Does it handle loading/error/empty/success?
4. **Testability**: Can we write `onNodeWithTag("xxx")` assertions? Or no tags at all?
5. **UX issues**: Confusing icons, hidden buttons, unclear text
6. **Recomposition safety**: Remember/derivedStateOf used correctly? Performance traps?

### Write to stage file:
```markdown
# Stage 6: Compose Screens Audit

## Semantics and Testability
| # | Screen | testTags | contentDescriptions | Testable? | Issues |
|---|--------|----------|---------------------|-----------|--------|

## State Handling
| # | Screen | Loading | Error | Empty | Offline | Issues |
|---|--------|---------|-------|-------|---------|--------|

## UX Issues From Code
| # | Screen | Issue | Code Location | Severity |
|---|--------|-------|---------------|----------|
| 1 | IdentifyScreen | Upload button: [exact composable, size, placement] | file:line | HIGH |
| 2 | ScanScreen + IdentifyScreen | Same Icon() used for different uploads | file:line | HIGH |
...

## Missing Semantics (must add for testing)
| # | Screen | Element | Needed testTag/description |
```

---

## Stage 7: Design System and Reusable Components
**Output: `_investigation/stage-07-designsystem.md`**

### Read the design system:
```
core/designsystem/src/main/java/com/wadjet/core/designsystem/theme/Theme.kt
core/designsystem/src/main/java/com/wadjet/core/designsystem/theme/Color.kt
core/designsystem/src/main/java/com/wadjet/core/designsystem/theme/Type.kt
core/designsystem/src/main/java/com/wadjet/core/designsystem/component/*.kt  (all components)
core/designsystem/src/main/java/com/wadjet/core/designsystem/animation/*.kt  (all animations)
```

### Check:
1. Theme consistency: Same colors/typography used everywhere?
2. Dark mode: All colors defined for both light and dark?
3. Component API: Reusable and well-parameterized?
4. Animations: Performance safe? No infinite recompositions?
5. Previews: Present? Cover different states?

### Write to stage file:
```markdown
# Stage 7: Design System

## Component Inventory
| # | Component | Used Where | Has Preview? | testTag? | Issues |

## Theme Audit
| Attribute | Light | Dark | Consistent? |

## Animation Performance
| Animation | Type | Safe? | Issues |

## Upload Icons (THE CONFUSION ISSUE)
| Feature | Icon Used | Size | Location | What It Should Be |
| Scan | ??? | ?? | ?? | Camera/scan icon |
| Identify | ??? | ?? | ?? | Landmark/image icon |
```

---

## Stage 8: Navigation - Every Path and Edge Case
**Output: `_investigation/stage-08-navigation.md`**

### Read navigation files:
```
app/src/main/java/com/wadjet/app/navigation/Route.kt
app/src/main/java/com/wadjet/app/navigation/WadjetNavHost.kt
app/src/main/java/com/wadjet/app/navigation/TopLevelDestination.kt
app/src/main/java/com/wadjet/app/MainActivity.kt
app/src/main/java/com/wadjet/app/WadjetApp.kt
```

### Test on emulator:
```bash
# Navigate to every screen via bottom nav
python $SCRIPTS/navigator.py --find-text "Home" --tap
python $SCRIPTS/navigator.py --find-text "Hieroglyphs" --tap
python $SCRIPTS/navigator.py --find-text "Explore" --tap
python $SCRIPTS/navigator.py --find-text "Stories" --tap
python $SCRIPTS/navigator.py --find-text "Thoth" --tap

# Navigate to every screen via actions (cards, buttons)
# [use navigator.py to tap specific actions on each screen]

# Press back on every screen
python $SCRIPTS/keyboard.py --key BACK

# Rotate device on every screen
adb shell settings put system accelerometer_rotation 0
adb shell settings put system user_rotation 1  # landscape
python $SCRIPTS/screen_mapper.py --json  # check if layout adapts
adb shell settings put system user_rotation 0  # back to portrait

# Kill and relaunch (cold restart)
python $SCRIPTS/app_launcher.py --terminate com.wadjet.app
python $SCRIPTS/app_launcher.py --launch com.wadjet.app
# Check which screen shows up

# REAL process death test (different from force-stop!):
# 1. Navigate to a screen with state (e.g. Dictionary with search, or Chat with messages)
python $SCRIPTS/navigator.py --find-text "Hieroglyphs" --tap
python $SCRIPTS/screen_mapper.py --json
# 2. Press Home (put app in background, don't kill it)
python $SCRIPTS/keyboard.py --key HOME
# 3. Kill the process while it's in background (simulates Android killing it for memory)
adb shell am kill com.wadjet.app
# 4. Reopen from recents (this triggers SavedStateHandle restoration)
adb shell am start -n com.wadjet.app/.MainActivity
python $SCRIPTS/screen_mapper.py --json
# 5. Check: did it restore the screen? Did it lose scroll position? Did it crash?
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 3 --json

# Test deep links if any
adb shell am start -a android.intent.action.VIEW -d "wadjet://dictionary/A1" com.wadjet.app
```

### Write to stage file:
```markdown
# Stage 8: Navigation

## Route Reachability Matrix
| Route | Via Bottom Nav? | Via Action? | Deep Link? | Back Behavior | Issues |

## Back Stack Issues
| # | Scenario | Expected | Actual | Issue |

## State Preservation (rotation/process death)
| Screen | Preserves State on Rotation? | On Process Death (am kill)? | On Don't-Keep-Activities? | Issues |

## Auth-Gated Routes
| Route | Requires Auth? | Redirects to Login? | Tested? |
```

---

## Stage 9: Data Layer - Repositories and Error Patterns
**Output: `_investigation/stage-09-repositories.md`**

### Read ALL 9 repository interfaces + 9 implementations:
```
core/domain/src/main/java/com/wadjet/core/domain/repository/*.kt     (9 interfaces)
core/data/src/main/java/com/wadjet/core/data/repository/*.kt         (9 implementations)
```

### Also read error handling utilities:
```
core/common/src/main/java/com/wadjet/core/common/WadjetResult.kt
core/common/src/main/java/com/wadjet/core/common/SuspendRunCatching.kt
```

### Check:
1. Does every repository method handle errors consistently?
2. What's the caching strategy per repo?
3. Are there retry mechanisms?
4. Thread safety of concurrent calls?

### Write to stage file:
```markdown
# Stage 9: Repositories

## Error Handling Consistency
| Repository | Method | Uses WadjetResult? | Catches All? | Returns Error State? |

## Cache Strategy
| Repository | Network-first? | Cache-first? | Offline fallback? |

## Issues Found
```

---

## Stage 10: Firebase and Auth Flow
**Output: `_investigation/stage-10-firebase-auth.md`**

### Read ALL Firebase files:
```
core/firebase/src/main/java/com/wadjet/core/firebase/FirebaseAuthManager.kt
core/firebase/src/main/java/com/wadjet/core/firebase/WadjetFirebaseMessaging.kt
core/firebase/src/main/java/com/wadjet/core/firebase/di/FirebaseModule.kt
core/network/src/main/java/com/wadjet/core/network/TokenManager.kt
core/network/src/main/java/com/wadjet/core/network/TokenAuthenticator.kt
core/network/src/main/java/com/wadjet/core/network/AuthInterceptor.kt
```

### Test auth flows on emulator:
```bash
# Fresh install test
python $SCRIPTS/app_launcher.py --terminate com.wadjet.app
adb shell pm clear com.wadjet.app  # clear all data
python $SCRIPTS/app_launcher.py --launch com.wadjet.app
python $SCRIPTS/screen_mapper.py --json  # should show Welcome

# Try to register
python $SCRIPTS/navigator.py --find-text "Register" --tap
python $SCRIPTS/screen_mapper.py --json  # map the register sheet

# Try to login
python $SCRIPTS/keyboard.py --key BACK
python $SCRIPTS/navigator.py --find-text "Login" --tap
python $SCRIPTS/screen_mapper.py --json  # map the login sheet

# Test logout
# [navigate to Settings, find logout button]
python $SCRIPTS/navigator.py --find-text "Settings" --tap
python $SCRIPTS/navigator.py --find-text "Logout" --tap
python $SCRIPTS/screen_mapper.py --json  # should go back to Welcome

# Check logcat for token/auth issues
python $SCRIPTS/log_monitor.py --package com.wadjet.app --tag "Auth" --duration 10 --json
python $SCRIPTS/log_monitor.py --package com.wadjet.app --tag "Token" --duration 10 --json
```

### Write to stage file:
```markdown
# Stage 10: Firebase and Auth

## Auth Flow Matrix
| Flow | Works? | Error Handling | Notes |

## Token Lifecycle
| Event | Expected | Actual | Issues |
| Fresh login | Token stored | | |
| Token expired | Auto-refresh | | |
| Refresh failed | Redirect to login | | |
| Concurrent refresh | Queue/lock | | |
| Logout | Clear all | | |
| Process death | Token persisted | | |

## Firebase Services
| Service | Configured? | Tested? | Issues |

## Security Issues
[Token storage security, cleartext in logs, etc.]
```

---

## Stage 11: NiA and Best Practices Reference
**Output: `_investigation/stage-11-best-practices.md`**

### Read testing patterns from Now in Android:
```
D:\Personal attachements\Repos\23-Android-Kotlin\nowinandroid\
```
Look at:
- How NiA sets up Roborazzi screenshot tests
- How NiA structures Hilt test modules
- How NiA does navigation testing
- Compose UI test patterns

### Also read the android-testing SKILL.md:
```
D:\Personal attachements\Repos\23-Android-Kotlin\awesome-android-agent-skills\.github\skills\testing_and_automation\android-testing\SKILL.md
```

### Search online if needed:
- Roborazzi setup for multi-module projects
- Compose Preview Screenshot Testing library
- Testing SSE/streaming in Android
- Testing Hilt-injected ViewModels

### Write to stage file:
```markdown
# Stage 11: Best Practices Reference

## NiA Testing Patterns
| Pattern | How NiA Does It | Works for Wadjet? | Notes |

## Roborazzi Setup Steps
[Exact steps to add Roborazzi to Wadjet's multi-module project]

## Recommended Test Structure
[Where files go, naming rules, config]

## Testing SSE Streams
[How to test ChatRepository's SSE stream]

## Testing Hilt ViewModels
[Pattern for injecting fakes]
```

---

## Stage 12: Backend API Smoke Test
**Output: `_investigation/stage-12-backend-api.md`**

### Hit every public endpoint:
```bash
# Dictionary endpoints
curl -s https://nadercr7-wadjet-v2.hf.space/api/dictionary | python -m json.tool | head -20
curl -s https://nadercr7-wadjet-v2.hf.space/api/dictionary/categories | python -m json.tool
curl -s https://nadercr7-wadjet-v2.hf.space/api/dictionary/alphabet | python -m json.tool | head -20
curl -s "https://nadercr7-wadjet-v2.hf.space/api/dictionary?q=ankh" | python -m json.tool

# Landmark endpoints
curl -s https://nadercr7-wadjet-v2.hf.space/api/landmarks | python -m json.tool | head -30
curl -s https://nadercr7-wadjet-v2.hf.space/api/landmarks/categories | python -m json.tool

# Stories
curl -s https://nadercr7-wadjet-v2.hf.space/api/stories | python -m json.tool | head -30

# Write
curl -s https://nadercr7-wadjet-v2.hf.space/api/write/palette | python -m json.tool

# Error responses
curl -s -o /dev/null -w "%{http_code}" https://nadercr7-wadjet-v2.hf.space/api/dictionary/NONEXISTENT_CODE
curl -s -o /dev/null -w "%{http_code}" https://nadercr7-wadjet-v2.hf.space/api/user/profile
curl -s -o /dev/null -w "%{http_code}" https://nadercr7-wadjet-v2.hf.space/api/nonexistent

# Response time test
curl -s -o /dev/null -w "%{time_total}" https://nadercr7-wadjet-v2.hf.space/api/dictionary
curl -s -o /dev/null -w "%{time_total}" https://nadercr7-wadjet-v2.hf.space/api/landmarks
curl -s -o /dev/null -w "%{time_total}" https://nadercr7-wadjet-v2.hf.space/api/stories
```

### For each endpoint record:
1. Actual JSON structure vs Android DTO fields
2. Response time
3. Error response format
4. Fields the Android app doesn't know about

### Write to stage file:
```markdown
# Stage 12: Backend API Smoke Test

## Endpoint Responses
| # | Endpoint | Status | Time (s) | Key Fields | Matches DTO? | Issues |

## Error Response Format
[What does the backend return for errors? Does Android parse it correctly?]

## Schema Mismatches
| Endpoint | Backend Field | DTO Field | Issue |

## Backend Health
- Server responsive?
- Cold start time?
- Any 500 errors?
```

---

## Stage 13: Pronunciation, TTS, Audio Pipeline
**Output: `_investigation/stage-13-audio-pronunciation.md`**

### Read:
```
core/common/src/main/java/com/wadjet/core/common/EgyptianPronunciation.kt
core/network/src/main/java/com/wadjet/core/network/api/AudioApiService.kt
core/network/src/main/java/com/wadjet/core/network/model/AudioModels.kt
```

### Find where TTS is triggered:
Search for `speak`, `audio`, `pronunciation`, `tts` across all feature modules.

### Test on emulator:
```bash
# Go to Dictionary, pick a sign, hit play
python $SCRIPTS/navigator.py --find-text "Hieroglyphs" --tap
# [navigate to a dictionary sign]
python $SCRIPTS/navigator.py --find-text "Play" --tap  # or whatever the button says
python $SCRIPTS/log_monitor.py --package com.wadjet.app --tag "Audio" --duration 5 --json

# Check volume is up
adb shell media volume --set 15 --stream 3

# Test STT (speech-to-text) in Chat
python $SCRIPTS/navigator.py --find-text "Thoth" --tap
# [look for microphone button]
python $SCRIPTS/screen_mapper.py --json

# Test with no internet (TTS should fail gracefully)
adb shell settings put global airplane_mode_on 1
adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true
# [try playing pronunciation]
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 5 --json
adb shell settings put global airplane_mode_on 0
adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false
```

### Write to stage file:
```markdown
# Stage 13: Audio and Pronunciation

## TTS Integration Points
| Feature | Trigger | API Call | Response Handling | Audio Playback | Issues |

## Pronunciation Engine
[EgyptianPronunciation.kt analysis]
(Reference 004-logic-quality for what was already found. Note what's fixed vs still open.)

## Audio Playback Architecture
[How audio is played: MediaPlayer? ExoPlayer? Raw AudioTrack?]

## STT (Speech-to-Text) Flow
[How STT works: Groq Whisper? Recording? Permissions?]

## Issues Found
```

---

## Stage 14: Security and Edge Cases
**Output: `_investigation/stage-14-security-edge.md`**

### Read:
```
app/src/main/AndroidManifest.xml
app/proguard-rules.pro
core/network/src/main/java/com/wadjet/core/network/di/NetworkModule.kt
```

### Check:
1. **Token storage**: Hardcoded secrets? Tokens in plain SharedPreferences?
2. **Network security**: Certificate pinning? Cleartext traffic allowed?
3. **Input validation**: SQL injection via Room? XSS via WebView?
4. **Deep link security**: Can malicious deep links crash the app?
5. **Permissions**: Camera, storage, microphone requested properly?
6. **ProGuard/R8**: Rules correct? Release builds crash-free?
7. **Memory**: Large images handled? OOM possible?
8. **Config changes**: Rotation, split-screen, back gesture

### Test edge cases on emulator:
```bash
# Rotate device
adb shell settings put system accelerometer_rotation 0
adb shell settings put system user_rotation 1  # landscape
python $SCRIPTS/screen_mapper.py --json
adb shell settings put system user_rotation 0  # back to portrait

# Rapid button tapping (test double-submit)
python $SCRIPTS/navigator.py --find-text "Login" --tap
python $SCRIPTS/navigator.py --find-text "Login" --tap
python $SCRIPTS/navigator.py --find-text "Login" --tap

# Large image upload test
# (push a large test image to the device)
# adb push test_large_10mb.jpg /sdcard/Download/

# Check for cleartext traffic in logcat
python $SCRIPTS/log_monitor.py --package com.wadjet.app --tag "OkHttp" --duration 10 --json

# Monkey test (random UI interactions to find crashes)
adb shell monkey -p com.wadjet.app --throttle 200 -v 500
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 5 --json

# Test intent injection
adb shell am start -a android.intent.action.VIEW -d "wadjet://../../etc/passwd" com.wadjet.app 2>&1
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 3 --json

# Memory check
adb shell dumpsys meminfo com.wadjet.app | head -30

# Activity state check (see current back stack, tasks, activity state)
adb shell dumpsys activity activities | Select-String -Pattern "com.wadjet" -Context 2,2

# Split-screen / multi-window test
adb shell am start -n com.wadjet.app/.MainActivity --windowingMode 3  # freeform/split
python $SCRIPTS/screen_mapper.py --json
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 3 --json

# Don't-keep-activities developer option (tests state save/restore on every navigation)
adb shell settings put global always_finish_activities 1
python $SCRIPTS/navigator.py --find-text "Hieroglyphs" --tap
python $SCRIPTS/navigator.py --find-text "Home" --tap
python $SCRIPTS/keyboard.py --key BACK
python $SCRIPTS/screen_mapper.py --json  # did it restore correctly?
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 3 --json
adb shell settings put global always_finish_activities 0  # reset

# Record a video of the monkey test (visual evidence of crashes)
adb shell screenrecord /sdcard/005-evidence/monkey-test.mp4 &
adb shell monkey -p com.wadjet.app --throttle 200 -v 300
# Stop recording after monkey finishes (Ctrl+C or timeout)
adb pull /sdcard/005-evidence/monkey-test.mp4 .specify/specs/005-full-testing/_investigation/
```

### Write to stage file:
```markdown
# Stage 14: Security and Edge Cases

## Security Audit
| Area | Status | Issue | Severity |
|------|--------|-------|----------|
| Token storage | | | |
| Certificate pinning | | | |
| Cleartext traffic | | | |
| ProGuard rules | | | |
| Permissions | | | |
| Input sanitization | | | |
| Deep link validation | | | |

## Edge Cases
| # | Scenario | Expected | Actual | Issue |
|---|----------|----------|--------|-------|
| | Rapid back presses | | | |
| | Rotate during API call | | | |
| | Kill app during upload | | | |
| | Very long text input | | | |
| | No storage permission | | | |
| | API returns HTML instead of JSON | | | |
| | Monkey test results | | | |
| | Malicious deep link | | | |
| | Split-screen mode | | | |
| | Don't-keep-activities (state restore) | | | |
| | Process death via `am kill` (background) | | | |
| | Slow 2G network (from Stage 5) | | | |

## Memory Usage
[dumpsys meminfo results, OOM risks]

## Activity State (dumpsys output)
[Back stack, task info, any leaked activities]
```

---

## Stage 15: Performance and Recomposition Profiling
**Output: `_investigation/stage-15-performance.md`**

### Test on emulator:
```bash
# Turn animations BACK ON for performance measurement (we disabled them in Stage 2)
adb shell settings put global window_animation_scale 1
adb shell settings put global transition_animation_scale 1
adb shell settings put global animator_duration_scale 1

# Enable GPU overdraw visualization
adb shell setprop debug.hwui.overdraw show

# Take a baseline memory snapshot before testing
adb shell dumpsys meminfo com.wadjet.app | Select-String "TOTAL"

# Start a screen recording for performance evidence
adb shell screenrecord /sdcard/005-evidence/perf-test.mp4 &

# Monitor dropped frames during scroll
python $SCRIPTS/log_monitor.py --package com.wadjet.app --tag "Choreographer" --duration 30 --json

# Scroll every scrollable screen rapidly
python $SCRIPTS/navigator.py --find-text "Explore" --tap
for ($i = 0; $i -lt 10; $i++) { python $SCRIPTS/gesture.py --swipe up }
python $SCRIPTS/navigator.py --find-text "Hieroglyphs" --tap
for ($i = 0; $i -lt 10; $i++) { python $SCRIPTS/gesture.py --swipe up }

# Rapid tab switching stress test
for ($i = 0; $i -lt 20; $i++) {
    python $SCRIPTS/navigator.py --find-text "Home" --tap
    python $SCRIPTS/navigator.py --find-text "Hieroglyphs" --tap
    python $SCRIPTS/navigator.py --find-text "Explore" --tap
    python $SCRIPTS/navigator.py --find-text "Stories" --tap
    python $SCRIPTS/navigator.py --find-text "Thoth" --tap
}

# Check for dropped frames after stress test
python $SCRIPTS/log_monitor.py --package com.wadjet.app --tag "Choreographer" --duration 5 --json

# Memory usage before and after stress
adb shell dumpsys meminfo com.wadjet.app | Select-String "TOTAL"

# Disable overdraw
adb shell setprop debug.hwui.overdraw false

# Stop screen recording and pull the video
# (screenrecord auto-stops after 3 min, or pull what we have)
adb pull /sdcard/005-evidence/perf-test.mp4 .specify/specs/005-full-testing/_investigation/

# Disable animations again for remaining stages (cleaner testing)
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
```

### For Compose recomposition, check the code for:
1. @Stable / @Immutable annotations on state classes (missing = unnecessary recompositions)
2. `remember` and `derivedStateOf` usage (or lack of it)
3. Lambda stability (passing lambdas that recreate every recomposition)
4. Large lists without `key` parameter in LazyColumn items

### Write to stage file:
```markdown
# Stage 15: Performance

## Scroll Performance Per Screen
| Screen | Scrollable? | Jank? | Dropped Frames | Issue |

## Recomposition Safety
| State Class | @Stable? | @Immutable? | Risk |

## Image Loading
| Screen | Image Count | Placeholder? | Transition Smooth? | Large Image Handling |

## Memory Profile
| Scenario | Memory (MB) | GC Count | OOM Risk |

## Tab Switching Stress
[Results of 20-cycle rapid switching. Crashes? Leaks? ANRs?]

## GPU Overdraw
[Screens with too many overdraw layers]
```

---

## Stage 16: Accessibility - TalkBack, Font Sizes, Display Sizes
**Output: `_investigation/stage-16-accessibility.md`**

### Test on emulator:
```bash
# Enable TalkBack
adb shell settings put secure enabled_accessibility_services com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService
adb shell settings put secure accessibility_enabled 1

# Navigate every screen with TalkBack on
# For each screen, use screen_mapper to check what contentDescription is set
python $SCRIPTS/screen_mapper.py --verbose --json

# Change font to largest (1.5x)
adb shell settings put system font_scale 1.5

# Go through all screens, check for clipping/overflow
python $SCRIPTS/navigator.py --find-text "Home" --tap
python $SCRIPTS/screen_mapper.py --json
python $SCRIPTS/navigator.py --find-text "Hieroglyphs" --tap
python $SCRIPTS/screen_mapper.py --json
python $SCRIPTS/navigator.py --find-text "Explore" --tap
python $SCRIPTS/screen_mapper.py --json
# [repeat for all tabs]

# Reset font
adb shell settings put system font_scale 1.0

# Disable TalkBack
adb shell settings put secure accessibility_enabled 0

# Small phone simulation
adb shell wm size 720x1280
python $SCRIPTS/screen_mapper.py --json
# [check all screens]
adb shell wm size reset

# Large density
adb shell wm density 500
python $SCRIPTS/screen_mapper.py --json
adb shell wm density reset
```

### For each screen document:
1. **TalkBack reading order**: Logical? Skips important stuff? Reads decorative images?
2. **Content descriptions**: On all actionable elements? Useful (not just "button")?
3. **Large font**: Text clipped? Overlapping? Buttons still tappable?
4. **Small display**: Layout shrinks OK? Nothing cut off?
5. **Touch targets**: All interactive elements >= 48dp x 48dp?

### Write to stage file:
```markdown
# Stage 16: Accessibility

## TalkBack Audit Per Screen
| Screen | Reading Order OK? | Missing Descriptions | Reads Decorative Stuff? | Issues |

## Content Descriptions Inventory
| Screen | Element | Has Description? | Description Quality | Fix Needed |

## Font Scale 1.5x
| Screen | Text Clipped? | Layout Broken? | Buttons Tappable? | Issues |

## Small Display (720x1280)
| Screen | Layout OK? | Anything Cut Off? | Issues |

## Large Density (500dpi)
| Screen | Layout OK? | Issues |

## Touch Target Audit
| Screen | Element | Size | Meets 48dp? | Fix Needed |
```

---

## Stage 17: Dark Mode and RTL (Arabic) Full Pass
**Output: `_investigation/stage-17-darkmode-rtl.md`**

### Test on emulator:
```bash
# Switch to dark mode
adb shell cmd uimode night yes

# Go through ALL screens in dark mode
python $SCRIPTS/app_launcher.py --launch com.wadjet.app
python $SCRIPTS/screen_mapper.py --json
python $SCRIPTS/navigator.py --find-text "Hieroglyphs" --tap
python $SCRIPTS/screen_mapper.py --json
python $SCRIPTS/navigator.py --find-text "Explore" --tap
python $SCRIPTS/screen_mapper.py --json
python $SCRIPTS/navigator.py --find-text "Stories" --tap
python $SCRIPTS/screen_mapper.py --json
python $SCRIPTS/navigator.py --find-text "Thoth" --tap
python $SCRIPTS/screen_mapper.py --json
# [navigate to sub-screens too]
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 3 --json

# Switch back to light
adb shell cmd uimode night no

# Switch to Arabic locale for RTL testing
# Go to Settings > System > Languages > Add Arabic
# Or try:
adb shell "setprop persist.sys.locale ar-EG; setprop ctl.restart zygote" 2>/dev/null
# If that doesn't work, set language via the emulator Settings app manually

# Navigate ALL screens in Arabic RTL
python $SCRIPTS/screen_mapper.py --json
# Check: layout mirrors? Text aligned right? Icons flip where needed?
# Back arrow becomes forward arrow? Swipe directions right?

# Reset to English
adb shell "setprop persist.sys.locale en-US; setprop ctl.restart zygote" 2>/dev/null
```

### For dark mode check:
1. Text readable against background?
2. Icons/images visible? Not washed out?
3. Borders/dividers visible?
4. Status bar + nav bar styled right?
5. Any hardcoded colors (not using theme)?

### For RTL check:
1. Layout mirrored correctly?
2. Text alignment correct?
3. Icons that should flip (arrows, progress) DO flip?
4. Icons that should NOT flip (play button, search) DON'T flip?
5. Padding/margins mirror correctly?
6. Scrolling direction correct?

### Write to stage file:
```markdown
# Stage 17: Dark Mode and RTL

## Dark Mode Audit
| Screen | Background OK? | Text Readable? | Icons Visible? | Hardcoded Colors? | Issues |

## RTL Arabic Audit
| Screen | Layout Mirrors? | Text Aligns Right? | Icons Flip Right? | Padding OK? | Issues |

## Hardcoded Colors Found
| File | Line | Color | Should Be |

## RTL Layout Failures
| Screen | Element | Issue | Fix |
```

---

## Stage 18: Test Gap Analysis and Priority Map
**Output: `_investigation/stage-18-gap-analysis.md`**

### Re-read ALL previous stage files:
```
_investigation/stage-01-existing-tests.md
_investigation/stage-02-ux-walkthrough.md
_investigation/stage-03-viewmodels.md
_investigation/stage-04-network.md
_investigation/stage-05-database-offline.md
_investigation/stage-06-compose-screens.md
_investigation/stage-07-designsystem.md
_investigation/stage-08-navigation.md
_investigation/stage-09-repositories.md
_investigation/stage-10-firebase-auth.md
_investigation/stage-11-best-practices.md
_investigation/stage-12-backend-api.md
_investigation/stage-13-audio-pronunciation.md
_investigation/stage-14-security-edge.md
_investigation/stage-15-performance.md
_investigation/stage-16-accessibility.md
_investigation/stage-17-darkmode-rtl.md
```

### Compile everything:
```markdown
# Stage 18: Gap Analysis and Priority Map

## ALL Issues Found (Unified)
| # | Source Stage | Category | Issue | Severity | Affects | Effort |
|---|-------------|----------|-------|----------|---------|--------|

## Test Coverage Gap Map
| Module | Class | Current Tests | Tests Needed | Priority |
|--------|-------|--------------|-------------|----------|

## UX Issues Priority
| # | Issue | Severity | User Impact | Fix Effort | Priority Score |

## What Tests to Write (Ordered by Priority)
| Priority | Type | Module | Test | Why |
|----------|------|--------|------|-----|
| P0 | Unit | ... | ... | Crash/data-loss prevention |
| P1 | Unit | ... | ... | Core feature correctness |
| P2 | Integration | ... | ... | Feature interaction |
| P3 | Screenshot | ... | ... | Visual regression |
| P4 | E2E | ... | ... | End-to-end user flow |
```

---

# PLANNING OUTPUT

> **ONLY start this after ALL 18 stage files are written and re-read.**
>
> The fix/treatment plan is NOT in this folder. After testing finds problems,
> those go to a SEPARATE folder: `.specify/specs/006-fixes/`. This folder (005) is
> only for testing investigation + test writing. 006 is for fixing what tests find.

After re-reading all `_investigation/stage-*.md` files, produce these planning files in `.specify/specs/005-full-testing/`:

---

## Planning File 1: `test-plan.md` - Master Test Plan
```markdown
# Wadjet Android - Master Test Plan

## Testing Layers
| Layer | Tool | Scope | Count |
|-------|------|-------|-------|
| Unit | JUnit + MockK + Turbine | ViewModels, Repos, Utils | ??? |
| Integration | MockWebServer + Room | API to Repo to DB | ??? |
| Screenshot | Roborazzi | Every screen x light/dark | ??? |
| Compose UI | compose-ui-test | Interactive flows | ??? |
| E2E | Emulator + Agent Scripts | Full user journeys | ??? |
| API Contract | curl/httpx | Backend alignment | ??? |

## Test Matrix Per Feature
| Feature | Unit | Integration | Screenshot | UI | E2E | Priority |
|---------|------|-------------|------------|-----|-----|----------|
[Every feature row]

## Phases
### Phase 1: Foundation (must have)
[What tests to write first]

### Phase 2: Coverage (should have)
[Expand coverage]

### Phase 3: Automation (nice to have)
[CI/CD, Roborazzi baselines, E2E scripts]
```

---

## Planning File 2: `ux-issues.md` - All UX Problems Found
```markdown
# UX Issues - Complete Inventory

## Critical (user gets confused or can't finish what they started)
| # | Screen | Issue | What It Looks Like Now | Fix |

## Major (user friction, unclear flow)
| # | Screen | Issue | What It Looks Like Now | Fix |

## Minor (polish)
| # | Screen | Issue | What It Looks Like Now | Fix |
```

---

## Planning File 3: `unit-tests.md` - Every Unit Test to Write
```markdown
# Unit Tests Specification

## Module: core:common
| # | Test Class | Test Case | Assertion | Priority |

## Module: core:network
| # | Test Class | Test Case | Assertion | Priority |

## Module: core:data
| # | Test Class | Test Case | Assertion | Priority |

## Module: core:database
| # | Test Class | Test Case | Assertion | Priority |

## Module: core:firebase
| # | Test Class | Test Case | Assertion | Priority |

## Module: feature:auth
| # | Test Class | Test Case | Assertion | Priority |

[... for ALL modules with test cases for every ViewModel, Repository, etc.]
```

---

## Planning File 4: `screenshot-tests.md` - Roborazzi Setup + Every Screenshot
```markdown
# Screenshot Tests - Roborazzi

## Setup Steps
[Exact Gradle config changes for multi-module Roborazzi]

## Screenshot Matrix
| # | Screen | Composable | Light? | Dark? | Arabic RTL? | States |
|---|--------|------------|--------|-------|-------------|--------|
[Every screen x every variant]

## Golden Baseline Process
[How to record, compare, update]
```

---

## Planning File 5: `e2e-tests.md` - Emulator E2E Test Scripts
```markdown
# E2E Tests - Emulator + Agent Scripts

## Setup
[How to boot emulator, build APK, install, launch]
# Full commands using build_and_test.py, emulator_manage.py, app_launcher.py

## User Journeys
### Journey 1: New User Onboarding
| Step | Action | Script Command | Expected Result | How to Verify |
|------|--------|---------------|-----------------|---------------|
| 1 | Launch app | python $SCRIPTS/app_launcher.py --launch com.wadjet.app | Welcome screen | screen_mapper.py --json |
| 2 | Tap Register | python $SCRIPTS/navigator.py --find-text "Register" --tap | Register sheet shows | screen_mapper.py --json |
| 3 | Type email | python $SCRIPTS/navigator.py --find-type EditText --enter-text "test@test.com" | Email filled | screen_mapper.py --json |
...

### Journey 2: Scan Hieroglyph
| Step | Action | Script Command | Expected Result | How to Verify |
...

### Journey 3: Browse Dictionary then Play Pronunciation
...

### Journey 4: Explore Landmarks then Identify
...

### Journey 5: Chat with Thoth AI
...

### Journey 6: Read a Story
...

### Journey 7: Manage Favorites
...

### Journey 8: Settings and Language Switch
...

### Journey 9: Offline Mode
| Step | Action | Script Command | Expected Result | How to Verify |
| 1 | Enable airplane | adb shell settings put global airplane_mode_on 1 | | |
| 2 | Open Dictionary | navigator.py --find-text "Hieroglyphs" --tap | Cached data shows | screen_mapper |
| 3 | Try Chat | navigator.py --find-text "Thoth" --tap | Error message | screen_mapper |
...

### Journey 10: Auth (Login/Register/Logout)
...

## Error Journeys
### Error 1: No internet during scan
### Error 2: Backend down
### Error 3: Invalid image upload
### Error 4: Expired token mid-session
```

---

## Planning File 6: `api-contract-tests.md` - Backend Contract Tests
```markdown
# API Contract Tests

## Test per Endpoint
| # | Method | Endpoint | Request | Expected Status | Expected Fields | Android DTO | Match? |

## Error Contract
| Status | Backend Error Body | Android Parsing |

## Test Script (Python httpx)
[Ready-to-run Python script that tests all endpoints]
```

---

## Planning File 7: `phase-prompts.md` - Execution Prompts
```markdown
# Phase Prompts - Testing Implementation

## Phase 1 Prompt: Setup + Unit Tests (core modules)
[Copy-paste prompt that writes all core module unit tests]

## Phase 2 Prompt: Unit Tests (feature modules)
[Copy-paste prompt for all feature module unit tests]

## Phase 3 Prompt: Roborazzi Screenshot Setup + Golden Baselines
[Copy-paste prompt]

## Phase 4 Prompt: Compose UI Tests
[Copy-paste prompt]

## Phase 5 Prompt: E2E Emulator Tests
[Copy-paste prompt]

## Phase 6 Prompt: API Contract Tests
[Copy-paste prompt]

## Phase 7 Prompt: CI/CD Integration
[Copy-paste prompt for updating .github/workflows/android.yml]
```

---

## Planning File 8: `workflow.md` - Execution Workflow
```markdown
# Testing Workflow

## Order of Operations
1. Run existing tests - see what already fails
2. Stage investigation (18 stages) - full understanding
3. Phase 1-2: Write unit tests - find bugs
4. Write findings to 006-fixes/phase-01-unit-bugs.md
5. Phase 3: Roborazzi setup + screenshots - find visual issues
6. Write findings to 006-fixes/phase-02-visual-issues.md
7. Phase 4: Compose UI tests - find interaction bugs
8. Write findings to 006-fixes/phase-03-interaction-bugs.md
9. Phase 5: E2E emulator tests - find flow-level bugs
10. Write findings to 006-fixes/phase-04-e2e-failures.md
11. Phase 6: API contract tests - find backend mismatches
12. Write findings to 006-fixes/phase-05-api-mismatches.md
13. Phase 7: CI/CD integration
14. Switch to 006-fixes/ - execute fix phases in priority order
15. Re-run all tests - everything green

## Handoff Protocol (005 to 006)
After EACH testing phase:
1. Run the tests
2. Collect all failures and issues
3. Write a dedicated file in 006-fixes/ for that phase
4. Each file has: problems, root cause, best solution, fix prompts
5. User picks when to switch from testing (005) to fixing (006)

## Done Checklist
- [ ] All 18 investigation stages complete
- [ ] All 9 planning files in 005-full-testing/ written
- [ ] All test phases run (Phase 1-7)
- [ ] All findings written to 006-fixes/ phase files
- [ ] 006-fixes/ master plan + phase prompts generated
- [ ] All fixes done
- [ ] All tests re-run - green
- [ ] CI/CD runs tests on every PR
```

---

## Planning File 9: `testing.md` - Test Infrastructure Setup
```markdown
# Test Infrastructure

## Dependencies to Add
[Exact changes to libs.versions.toml and module build.gradle.kts files]

## Test Utilities to Create
[Shared test fixtures, fake repositories, mock factories]

## Roborazzi Configuration
[Exact Gradle plugin setup for multi-module]

## Emulator Script Integration
[How to run the Python scripts from Gradle or standalone]

## CI/CD Test Jobs
[Updated android.yml with test stages]
```

---

# RULES

1. **Every issue needs a file:line reference.** Not "the button is bad" but "IdentifyScreen.kt:42 has a 24dp icon with no label."
2. **Every test case needs exact assertions.** Not "verify it works" but "assertEquals(expected, actual)."
3. **Every UX fix needs a concrete description.** Not "make it better" but "change Icon(Icons.Default.Upload) to Icon(Icons.Default.CameraAlt), increase from 24.dp to 40.dp, add text label 'Scan Hieroglyph' below."
4. **Match existing patterns.** JUnit 4, MockK, Turbine, backtick test names, same style as the existing 12 tests.
5. **Don't redo 003-ux-redesign or 004-logic-quality work.** Reference them.
6. **The emulator is real.** Boot it, install the app, navigate, document. Don't imagine screens.
7. **Use every emulator script.** screen_mapper, navigator, gesture, keyboard, app_launcher, emulator_manage, build_and_test, log_monitor. All of them. With --json when you need structured data.
8. **All test findings go to `006-fixes/`.** This folder (005) is tests only. 006 is fixes.
