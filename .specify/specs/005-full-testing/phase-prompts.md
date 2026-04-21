# Phase Prompts — Testing Implementation

> Copy-paste these prompts to execute each testing phase. Each prompt is self-contained.

---

## Phase 1 Prompt: Test Infrastructure + Core P0 Unit Tests

```
## Task: Create test infrastructure and write P0 unit tests

### Step 1: Create `core/testing` module

1. Create `core/testing/build.gradle.kts` with dependencies: JUnit 4, MockK, Turbine, coroutines-test, Hilt testing
2. Register in `settings.gradle.kts`: `include(":core:testing")`
3. Create these files:
   - `WadjetTestRunner.kt` — extends AndroidJUnitRunner, returns HiltTestApplication
   - `util/MainDispatcherRule.kt` — TestWatcher that calls `Dispatchers.setMain(UnconfinedTestDispatcher())`
   - `data/SignTestData.kt` — shared test fixtures for signs
   - `data/LandmarkTestData.kt` — shared landmarks
   - `data/UserTestData.kt` — shared user profile/stats
   - `data/ChatMessageTestData.kt` — shared chat messages
   - `data/StoryTestData.kt` — shared stories

4. Update `app/build.gradle.kts` testRunner to `com.wadjet.core.testing.WadjetTestRunner`

### Step 2: Write ChatViewModel tests (P0)

Open `.specify/specs/005-full-testing/unit-tests.md` — tests #15-29.
Write `feature/chat/src/test/java/com/wadjet/feature/chat/ChatViewModelTest.kt`.
Match existing patterns from `AuthViewModelTest.kt`: JUnit 4, MockK, backtick names, StandardTestDispatcher.
Use MainDispatcherRule from core/testing.

Key test cases:
- Initial state: empty messages, suggestions present
- sendMessage: adds user msg, blocks during streaming, empty text no-op
- Streaming: response appends bot message, error sets error state
- editMessage/retryMessage
- clearChat/speakMessage
- GlobalScope leak verification

### Step 3: Write ScanViewModel tests (P0)

Tests #30-36 from unit-tests.md.
Write `feature/scan/src/test/java/com/wadjet/feature/scan/ScanViewModelTest.kt`.

Key: Test double-submit guard (the CRITICAL bug), bitmap thread, progress states.

### Step 4: Write TokenManager tests (P0)

Tests #1-8 from unit-tests.md.
Write `core/network/src/test/java/com/wadjet/core/network/TokenManagerTest.kt`.

Use Robolectric for EncryptedSharedPreferences or mock the storage layer.

### Step 5: Run all tests
```bash
./gradlew test
```
All 320 existing + new tests must pass. Write failures to `006-fixes/phase-01-unit-bugs.md`.
```

---

## Phase 2 Prompt: P1 Unit Tests (Feature ViewModels)

```
## Task: Write P1 unit tests for all untested feature ViewModels

Open `.specify/specs/005-full-testing/unit-tests.md` for exact test cases.
Use MainDispatcherRule from `core/testing`.
Match patterns from existing tests (AuthViewModelTest, DictionaryViewModelTest).

### Files to create:

1. `feature/stories/src/test/.../StoriesViewModelTest.kt` — tests #45-49
2. `feature/stories/src/test/.../StoryReaderViewModelTest.kt` — tests #50-58
3. `feature/dashboard/src/test/.../DashboardViewModelTest.kt` — tests #59-66
4. `feature/settings/src/test/.../SettingsViewModelTest.kt` — tests #67-76
5. `feature/landing/src/test/.../LandingViewModelTest.kt` — tests #77-82
6. `feature/scan/src/test/.../ScanResultViewModelTest.kt` — tests #37-40
7. `feature/scan/src/test/.../HistoryViewModelTest.kt` — tests #41-44
8. `core/firebase/src/test/.../FirebaseAuthManagerTest.kt` — tests #9-14

### Add `core:testing` dependency to each feature module:
```kotlin
testImplementation(project(":core:testing"))
```

### Run all tests:
```bash
./gradlew test
```
Write any failures to `006-fixes/phase-02-unit-bugs.md`.
```

---

## Phase 3 Prompt: Roborazzi Screenshot Setup + Golden Baselines

```
## Task: Expand Roborazzi to all feature modules and capture golden baselines

Open `.specify/specs/005-full-testing/screenshot-tests.md` for the full matrix.

### Step 1: Add Roborazzi to each feature module

For each of these modules, add to `build.gradle.kts`:
- feature:auth, feature:landing, feature:scan, feature:dictionary, feature:explore
- feature:chat, feature:stories, feature:dashboard, feature:settings, feature:feedback, app

Add `roborazzi` plugin + dependencies (see screenshot-tests.md Setup Steps).
Copy `robolectric.properties` to each module's `src/test/resources/`.

### Step 2: Create screenshot test files

For each screen in the matrix (38 entries), create a test file:
- `WelcomeScreenshotTest.kt`, `LoginSheetScreenshotTest.kt`, etc.
- Each test renders the composable with fake data (from core/testing fixtures)
- Capture dark mode + each state variant (loading, error, success, empty)

### Step 3: Record baselines
```bash
./gradlew recordRoborazziDebug
```

### Step 4: Verify
```bash
./gradlew verifyRoborazziDebug
```

Commit golden images to `src/test/snapshots/`.
Write visual issues to `006-fixes/phase-03-visual-issues.md`.
```

---

## Phase 4 Prompt: Compose UI Tests (Interactive Flows)

```
## Task: Write Compose UI tests for critical interactive flows

### Prerequisites
- `core/testing` module with HiltTestRunner (from Phase 1)
- testTags added to interactive elements (from 006-fixes if already done)

### Tests to write:

1. `feature/auth/src/androidTest/.../LoginFlowTest.kt`
   - Launch WelcomeScreen → tap Sign In → fill email/password → tap Sign In → verify navigation
   - Test validation: invalid email shows error, empty password shows error

2. `feature/chat/src/androidTest/.../ChatInteractionTest.kt`
   - Type message → tap send → verify message appears in list
   - Tap suggestion chip → verify it populates input

3. `feature/scan/src/androidTest/.../ScanUploadTest.kt`
   - Verify upload zone is visible
   - Verify history icon navigates to history

4. `feature/dictionary/src/androidTest/.../DictionaryNavigationTest.kt`
   - Switch between Browse/Learn/Write/Translate tabs
   - Tap sign → verify detail screen opens

5. `app/src/androidTest/.../NavigationIntegrationTest.kt` (from NiA pattern)
   - `@HiltAndroidTest` with `createAndroidComposeRule<MainActivity>()`
   - Verify all 5 bottom nav destinations reachable
   - Verify auth gating: unauthenticated → Welcome, authenticated → Landing
   - Verify back stack: Landing → Explore → Detail → Back → Explore

### Run:
```bash
./gradlew connectedDebugAndroidTest
```
Write failures to `006-fixes/phase-04-interaction-bugs.md`.
```

---

## Phase 5 Prompt: E2E Emulator Tests

```
## Task: Run E2E user journeys on emulator using Python agent scripts

Open `.specify/specs/005-full-testing/e2e-tests.md` for all 10 journeys + 4 error journeys.

### Setup
$SCRIPTS = "D:\Personal attachements\Repos\23-Android-Kotlin\awesome-android-agent-skills\.github\skills\testing_and_automation\android-emulator-skill\scripts"

1. Boot emulator: `python "$SCRIPTS\emulator_manage.py" --boot Pixel_8`
2. Install app: `./gradlew installDebug`
3. Launch: `python "$SCRIPTS\app_launcher.py" --launch com.wadjet.app`

### Execute each journey sequentially:
- Journey 1: New User Onboarding
- Journey 2: Scan Hieroglyph (needs test image pushed to device)
- Journey 3: Browse Dictionary + Pronunciation
- Journey 4: Explore Landmarks + Identify
- Journey 5: Chat with Thoth
- Journey 6: Read a Story
- Journey 7: Dashboard + Favorites
- Journey 8: Settings
- Journey 9: Offline Mode (airplane mode toggle)
- Journey 10: Auth cycle (login/logout/re-login)
- Error Journeys 1-4

### For each journey:
1. Execute all steps using the script commands in the table
2. Verify each step using `screen_mapper.py --json`
3. Capture screenshot at each step: `adb exec-out screencap -p > journey_N_step_M.png`
4. Record pass/fail per step

Write all failures to `006-fixes/phase-05-e2e-failures.md`.
```

---

## Phase 6 Prompt: API Contract Tests

```
## Task: Run API contract tests against live backend

Open `.specify/specs/005-full-testing/api-contract-tests.md`.

### Step 1: Run Python test script
```bash
pip install httpx
python .specify/specs/005-full-testing/api_contract_tests.py
```
(Copy the Python script from api-contract-tests.md to a .py file first)

### Step 2: Verify DTO mismatches

For each FAIL in the script output:
1. Open the Android DTO file
2. Compare field names and types with backend response
3. Document exact discrepancy

### Known mismatches to verify:
- Landmark parent: Android `parent` (object) vs backend `parent_slug` (string)
- Landmark children: Android `children` (List<object>) vs backend `children_slugs` (List<string>)
- Write palette: Android missing `numbers` and `determinative` groups
- Error parsing: 3 different patterns (should be unified)

Write all mismatches to `006-fixes/phase-06-api-mismatches.md`.
```

---

## Phase 7 Prompt: CI/CD Integration

```
## Task: Integrate all tests into CI/CD pipeline

### Step 1: Create/update `.github/workflows/android.yml`

```yaml
name: Android CI
on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Unit tests
        run: ./gradlew test
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: '**/build/reports/tests/'

  screenshot-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Screenshot verification
        run: ./gradlew verifyRoborazziDebug
      - name: Upload diffs on failure
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: screenshot-diffs
          path: '**/build/outputs/roborazzi/'

  api-contract:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: '3.12'
      - run: pip install httpx
      - name: API contract tests
        run: python scripts/api_contract_tests.py
```

### Step 2: Add PR gate
- Unit tests must pass before merge
- Screenshot tests must pass before merge
- API contract tests are informational (don't block merge)

Write CI issues to `006-fixes/phase-07-ci-issues.md`.
```
