# Testing Strategy: UX Redesign

## Current State

| Metric | Value |
|--------|-------|
| Total test files | 6 |
| Unit tests | 6 (ExampleUnitTest per module) |
| UI / Compose tests | **0** |
| Screenshot tests | **0** |
| Navigation tests | **0** |
| Accessibility tests | **0** |
| Snapshot / screenshot infra | NOT configured |
| CI pipeline | NOT configured |

All 6 existing tests are default Android Studio template `ExampleUnitTest.kt` files — they assert `2 + 2 == 4` and provide zero coverage.

---

## Proposed Test Infrastructure

### Dependencies to Add

```toml
# gradle/libs.versions.toml additions
[versions]
roborazzi = "1.32.2"

[libraries]
# Compose testing
compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }

# Screenshot testing (Roborazzi — no emulator needed)
roborazzi-compose = { module = "io.github.takahirom.roborazzi:roborazzi-compose", version.ref = "roborazzi" }
roborazzi-junit = { module = "io.github.takahirom.roborazzi:roborazzi-junit-rule", version.ref = "roborazzi" }

# Accessibility checks
accessibility-test-framework = { module = "com.google.android.apps.common.testing.accessibility.framework:accessibility-test-framework", version = "4.1.1" }
```

### Test Runner Configuration

```kotlin
// app/build.gradle.kts additions
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true // Required for Robolectric
        }
    }
}

dependencies {
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit)
    testImplementation(libs.accessibility.test.framework)
    debugImplementation(libs.compose.ui.test.manifest)
}
```

---

## Screenshot Tests

One screenshot test per screen/sheet to catch visual regressions. Uses Roborazzi (Robolectric-based, no emulator).

### Test Files to Create

| Test File | Screen(s) | Priority |
|-----------|-----------|----------|
| `LandingScreenTest.kt` | LandingScreen (Home tab) | P1 |
| `HieroglyphsHubScreenTest.kt` | HieroglyphsHubScreen (3 tabs) | P1 |
| `ExploreScreenTest.kt` | ExploreScreen (3 tabs) | P1 |
| `StoriesScreenTest.kt` | StoriesScreen | P1 |
| `ChatScreenTest.kt` | ChatScreen (Thoth tab) | P1 |
| `DashboardScreenTest.kt` | DashboardScreen | P2 |
| `SettingsScreenTest.kt` | SettingsScreen | P2 |
| `DictionaryLookupScreenTest.kt` | DictionaryLookupScreen | P2 |
| `DictionarySignScreenTest.kt` | DictionarySignScreen | P2 |
| `ScanResultScreenTest.kt` | ScanResultScreen | P2 |
| `LandmarkDetailScreenTest.kt` | LandmarkDetailScreen | P2 |
| `StoryReaderScreenTest.kt` | StoryReaderScreen | P2 |
| `AuthSheetsTest.kt` | LoginSheet, RegisterSheet, ForgotSheet, ProfileSheet | P2 |
| `DesignSystemComponentsTest.kt` | WadjetButton, WadjetBottomSheet, WadjetToast, WadjetLoadingOverlay, WadjetNavBar | P1 |

### Example Screenshot Test

```kotlin
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LandingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val roborazziRule = RoborazziRule(
        composeRule = composeTestRule,
        captureRoot = composeTestRule.onRoot(),
        options = RoborazziRule.Options(
            captureType = RoborazziRule.CaptureType.LastImage()
        )
    )

    @Test
    fun landingScreen_default() {
        composeTestRule.setContent {
            WadjetTheme {
                LandingScreen(
                    uiState = LandingUiState.Success(/* test data */),
                    onNavigate = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun landingScreen_loading() {
        composeTestRule.setContent {
            WadjetTheme {
                LandingScreen(
                    uiState = LandingUiState.Loading,
                    onNavigate = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun landingScreen_error() {
        composeTestRule.setContent {
            WadjetTheme {
                LandingScreen(
                    uiState = LandingUiState.Error("Network unavailable"),
                    onNavigate = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
```

---

## Navigation Tests

Verify that all routes resolve and transitions work correctly.

| Test | Description | Priority |
|------|-------------|----------|
| `bottomNav_allTabs` | Tap each of 5 bottom nav items, verify correct screen shown | P0 |
| `bottomNav_preservesState` | Navigate away and back, verify scroll position/state retained | P1 |
| `hieroglyphsHub_tabNavigation` | Verify 3 tab switches within hub (Dictionary, Write, Gallery) | P1 |
| `exploreHub_tabNavigation` | Verify 3 tab switches within hub (Landmarks, Temples, Museums) | P1 |
| `dictionaryLookup_deepLink` | Tap dictionary result → DictionaryLookupScreen with correct args | P1 |
| `scan_toResult` | Navigate ScanScreen → ScanResultScreen with scan data | P1 |
| `story_toReader` | Tap story card → StoryReaderScreen with correct story slug | P1 |
| `explore_toLandmarkDetail` | Tap landmark card → LandmarkDetailScreen with correct ID | P1 |
| `settings_logout_toLogin` | Logout from settings → back to login sheet | P2 |
| `auth_registerSuccess_toDashboard` | Complete registration → navigate to main app | P2 |

### Example Navigation Test

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomNav_allTabs_showCorrectScreen() {
        // Home is default
        composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()

        // Tap Hieroglyphs
        composeTestRule.onNodeWithContentDescription("Hieroglyphs").performClick()
        composeTestRule.onNodeWithTag("hieroglyphs_hub").assertIsDisplayed()

        // Tap Explore
        composeTestRule.onNodeWithContentDescription("Explore").performClick()
        composeTestRule.onNodeWithTag("explore_screen").assertIsDisplayed()

        // Tap Stories
        composeTestRule.onNodeWithContentDescription("Stories").performClick()
        composeTestRule.onNodeWithTag("stories_screen").assertIsDisplayed()

        // Tap Thoth
        composeTestRule.onNodeWithContentDescription("Thoth").performClick()
        composeTestRule.onNodeWithTag("thoth_screen").assertIsDisplayed()
    }
}
```

---

## Accessibility Tests

### Semantic Checks (automated via `AccessibilityChecks`)

| Test | Description | Priority |
|------|-------------|----------|
| `allScreens_contentDescriptions` | Verify all interactive elements have contentDescription or text | P0 |
| `allScreens_touchTargets` | Verify minimum 48dp touch targets on all clickable elements | P0 |
| `allScreens_contrastRatio` | Automated WCAG AA contrast check via ATF | P0 |
| `bottomNav_focusOrder` | Verify TalkBack traversal order is logical | P1 |
| `thothChat_announcements` | Verify chat messages get announced as they arrive | P1 |
| `toasts_talkbackAnnouncement` | Verify toast messages are announced (currently BROKEN — UX-027) | P0 |
| `storyReader_roleDescriptions` | Verify heading roles on chapter titles | P2 |
| `dictionaryLookup_hieroglyphAlt` | Verify hieroglyphs have alt text (category + transliteration) | P1 |

### Accessibility Check Setup

```kotlin
// Enable automated accessibility checks for all Compose tests
@Before
fun enableAccessibilityChecks() {
    AccessibilityChecks.enable()
        .setRunChecksFromRootView(true)
        .setSuppressingResultMatcher(
            // Suppress known false positives if any
            matchesCheck(SpeakableTextPresentCheck::class.java)
        )
}
```

---

## Interaction Tests

| Test | Description | Priority |
|------|-------------|----------|
| `thothChat_sendMessage` | Type message, tap send, verify message appears in list | P1 |
| `thothChat_keyboardInsets` | Open keyboard, verify input stays above keyboard | P0 |
| `dictionarySign_drawing` | Touch down on canvas, drag, verify stroke recorded | P2 |
| `storyReader_chapterScroll` | Scroll to chapter 2, verify chapter header visible | P2 |
| `exploreSearch_filterResults` | Type query, verify filtered results shown | P1 |
| `hieroglyphGallery_categoryFilter` | Tap category chip, verify grid filters | P1 |
| `settings_themeToggle` | (Future) Toggle theme, verify recomposition | P3 |
| `auth_loginValidation` | Submit empty email, verify error shown | P1 |
| `auth_registerValidation` | Submit mismatched passwords, verify error | P1 |
| `scanResult_identifyAction` | Tap identify on scan result, verify loading + result | P2 |

---

## Test Data Strategy

### Fakes / Test Doubles

| Interface | Fake | Notes |
|-----------|------|-------|
| `HieroglyphRepository` | `FakeHieroglyphRepository` | Returns hardcoded list of 10 hieroglyphs |
| `StoryRepository` | `FakeStoryRepository` | Returns 3 stories (beginner, intermediate, advanced) |
| `AuthRepository` | `FakeAuthRepository` | Simulates login/register/logout |
| `ExploreRepository` | `FakeExploreRepository` | Returns 5 landmarks, 3 temples, 2 museums |
| `ChatRepository` | `FakeChatRepository` | Echoes messages with delay |
| `ScanRepository` | `FakeScanRepository` | Returns hardcoded scan result |

### Hilt Test Modules

Each fake binds via a `@TestInstallIn` module replacing the production module:

```kotlin
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object FakeRepositoryModule {
    @Provides @Singleton
    fun provideHieroglyphRepo(): HieroglyphRepository = FakeHieroglyphRepository()
    // ... etc
}
```

---

## Test Coverage Targets

| Category | Current | Phase 1 Target | Phase 5 Target |
|----------|---------|----------------|----------------|
| Screenshot tests | 0 | 6 (5 tabs + design system) | 15 (all screens) |
| Navigation tests | 0 | 2 (bottom nav + hub tabs) | 10 (all flows) |
| Accessibility tests | 0 | 4 (touch targets, contrast, content desc, toast) | 8 (all) |
| Interaction tests | 0 | 0 | 10 (all key interactions) |
| **Total** | **0** | **12** | **43** |

---

## CI Integration (Future)

Recommended pipeline (when CI is set up):

```yaml
# .github/workflows/ui-tests.yml
name: UI Tests
on: [pull_request]
jobs:
  screenshot-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - name: Run screenshot tests
        run: ./gradlew verifyRoborazziDebug
      - name: Upload diff images on failure
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: screenshot-diffs
          path: '**/build/outputs/roborazzi/**/*_compare.png'

  accessibility-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - name: Run accessibility tests
        run: ./gradlew testDebugUnitTest --tests "*Accessibility*"
```

### Screenshot Baseline Workflow

1. **Record baselines:** `./gradlew recordRoborazziDebug` (generates PNGs in `src/test/resources/`)
2. **Verify on PR:** `./gradlew verifyRoborazziDebug` (compares against baselines)
3. **Update after intentional changes:** `./gradlew recordRoborazziDebug` then commit new PNGs
