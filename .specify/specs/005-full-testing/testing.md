# Test Infrastructure

## Dependencies to Add

### `gradle/libs.versions.toml` — Already Present (verify)

```toml
[versions]
roborazzi = "1.59.0"     # ✅ exists
mockk = "1.13.16"        # ✅ exists
turbine = "1.2.0"        # ✅ exists
robolectric = "4.14.1"   # ✅ exists

[libraries]
roborazzi = { ... }       # ✅ exists
roborazzi-compose = { ... } # ✅ exists
roborazzi-rule = { ... }  # ✅ exists

[plugins]
roborazzi = { ... }       # ✅ exists
```

No new version catalog entries needed — all testing libs are already declared.

### New Module: `core/testing/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.wadjet.core.testing"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Exported to consumers
    api(libs.junit4)
    api(libs.mockk)
    api(libs.turbine)
    api(libs.kotlinx.coroutines.test)
    api(libs.hilt.android.testing)
    ksp(libs.hilt.compiler)

    // Internal
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
}
```

### Register in `settings.gradle.kts`

Add: `include(":core:testing")`

### Feature Module Changes

Each feature module's `build.gradle.kts` needs:

```kotlin
dependencies {
    testImplementation(project(":core:testing"))
}
```

### Roborazzi in Feature Modules

Each feature module that needs screenshot tests:

```kotlin
plugins {
    alias(libs.plugins.roborazzi)
}

android {
    testOptions {
        unitTests { isIncludeAndroidResources = true }
    }
}

dependencies {
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.rule)
    testImplementation(libs.compose.ui.test)
    testImplementation(libs.compose.ui.test.manifest)
}
```

### App Module `testInstrumentationRunner`

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        testInstrumentationRunner = "com.wadjet.core.testing.WadjetTestRunner"
    }
}
```

---

## Test Utilities to Create

### `core/testing/src/main/kotlin/com/wadjet/core/testing/`

#### `WadjetTestRunner.kt`
```kotlin
package com.wadjet.core.testing

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class WadjetTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
```

#### `util/MainDispatcherRule.kt`
```kotlin
package com.wadjet.core.testing.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

#### `data/SignTestData.kt`
```kotlin
package com.wadjet.core.testing.data

object SignTestData {
    fun sign(code: String = "A1", description: String = "seated man") = /* SignDto(...) */
    fun signDetail(code: String = "A1") = /* SignDetailDto(...) */
    fun signList(count: Int = 10) = (1..count).map { sign("A$it") }
}
```

#### `data/LandmarkTestData.kt`
```kotlin
package com.wadjet.core.testing.data

object LandmarkTestData {
    fun landmark(slug: String = "great-pyramid") = /* LandmarkSummaryDto(...) */
    fun landmarkDetail(slug: String = "great-pyramid") = /* LandmarkDetailDto(...) */
    fun landmarkList(count: Int = 5) = (1..count).map { landmark("landmark-$it") }
}
```

#### `data/UserTestData.kt`
```kotlin
package com.wadjet.core.testing.data

object UserTestData {
    fun user(name: String = "Test User", email: String = "test@test.com") = /* UserDto(...) */
    fun stats() = /* UserStatsResponse(...) */
}
```

#### `data/ChatMessageTestData.kt`
```kotlin
package com.wadjet.core.testing.data

object ChatMessageTestData {
    fun userMessage(text: String = "Hello") = /* ChatMessage(role=user, text=text) */
    fun botMessage(text: String = "Hi there") = /* ChatMessage(role=bot, text=text) */
    fun conversation(turns: Int = 3) = (1..turns).flatMap {
        listOf(userMessage("Question $it"), botMessage("Answer $it"))
    }
}
```

#### `data/StoryTestData.kt`
```kotlin
package com.wadjet.core.testing.data

object StoryTestData {
    fun story(id: String = "story-1") = /* StoryDto(...) */
    fun storyList() = listOf(story("story-1"), story("story-2"), story("story-3"))
}
```

---

## Roborazzi Configuration

### Already Configured (core:designsystem)
- Plugin: `roborazzi` v1.59.0
- Helper: `ScreenshotHelper.kt` with `captureScreenshot()` function
- Existing test: `ButtonScreenshotTest.kt` (7 tests, all passing)
- Config: `robolectric.properties` with `graphics.mode=NATIVE`, `sdk=34`, `qualifiers=480dpi`

### Expansion Needed

1. **Copy `robolectric.properties`** to every feature module's `src/test/resources/`
2. **Add Roborazzi plugin** to 11 modules (see screenshot-tests.md)
3. **Enhance `ScreenshotHelper`** to support multi-state captures
4. **Golden images stored in** `src/test/snapshots/images/` per module

### Record/Verify Commands
```bash
# Record golden baselines (first time)
./gradlew recordRoborazziDebug

# Verify against baselines (CI)
./gradlew verifyRoborazziDebug

# Compare and generate diff report
./gradlew compareRoborazziDebug
```

---

## Emulator Script Integration

### Scripts Location
```
$SCRIPTS = "D:\Personal attachements\Repos\23-Android-Kotlin\awesome-android-agent-skills\.github\skills\testing_and_automation\android-emulator-skill\scripts"
```

### Available Scripts

| Script | Purpose | Key Flags |
|--------|---------|-----------|
| `emulator_manage.py` | Boot/kill emulators | `--boot`, `--kill`, `--list` |
| `app_launcher.py` | Install/launch/clear app | `--launch`, `--clear`, `--install` |
| `screen_mapper.py` | Dump UI hierarchy | `--json`, `--text`, `--screenshot` |
| `navigator.py` | Find and tap elements | `--find-text`, `--find-content-desc`, `--find-type`, `--tap`, `--enter-text` |
| `gesture.py` | Swipe/scroll/pinch | `--swipe`, `--scroll`, `--pinch` |
| `keyboard.py` | Type text, press keys | `--type`, `--key` |
| `build_and_test.py` | Build + test orchestration | `--build`, `--test`, `--install` |
| `log_monitor.py` | Filter logcat | `--filter`, `--level`, `--tag` |

### Running E2E from Gradle (optional)

```kotlin
// app/build.gradle.kts
tasks.register("e2eTests") {
    group = "verification"
    description = "Run E2E emulator tests"
    doLast {
        exec {
            commandLine("python", "$SCRIPTS/build_and_test.py", "--e2e")
        }
    }
}
```

---

## CI/CD Test Jobs

### `.github/workflows/android.yml`

```yaml
name: Wadjet Android CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - uses: gradle/actions/setup-gradle@v4
      - name: Run unit tests
        run: ./gradlew test --no-daemon
      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: unit-test-results
          path: '**/build/reports/tests/'

  screenshot-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - uses: gradle/actions/setup-gradle@v4
      - name: Verify screenshots
        run: ./gradlew verifyRoborazziDebug --no-daemon
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
        continue-on-error: true  # Informational, doesn't block merge
```

### PR Gate Rules
- **Required**: `unit-tests` must pass
- **Required**: `screenshot-tests` must pass
- **Informational**: `api-contract` reports but doesn't block
