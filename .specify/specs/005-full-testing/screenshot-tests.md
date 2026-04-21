# Screenshot Tests — Roborazzi

## Setup Steps

### 1. Add Roborazzi to feature modules

Each feature module needs in `build.gradle.kts`:

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

### 2. Modules to add plugin to (not yet configured)

| Module | Config Status |
|--------|-------------|
| core:designsystem | ✅ Already configured |
| feature:auth | ❌ Add |
| feature:landing | ❌ Add |
| feature:scan | ❌ Add |
| feature:dictionary | ❌ Add |
| feature:explore | ❌ Add |
| feature:chat | ❌ Add |
| feature:stories | ❌ Add |
| feature:dashboard | ❌ Add |
| feature:settings | ❌ Add |
| feature:feedback | ❌ Add |
| app | ❌ Add (for HieroglyphsHub) |

### 3. Screenshot Helper Enhancement

Current `ScreenshotHelper.kt` in `core/designsystem` captures single-theme only. Enhance to:

```kotlin
fun captureMultiTheme(
    name: String,
    content: @Composable () -> Unit
) {
    // Dark (current theme — only mode)
    captureScreenshot("${name}_dark", content)
    // If light theme added later:
    // captureScreenshot("${name}_light", isLight = true, content)
}
```

### 4. Roborazzi Config (robolectric.properties)

Already in `core/designsystem/src/test/resources/robolectric.properties`:
```
graphics.mode=NATIVE
sdk=34
qualifiers=480dpi
```

Copy to all feature modules' `src/test/resources/`.

---

## Screenshot Matrix

### Auth Screens

| # | Screen | Composable | Dark? | States |
|---|--------|------------|-------|--------|
| 1 | Welcome | WelcomeScreen | ✅ | Default, Loading (Google sign-in) |
| 2 | Login Sheet | LoginSheet | ✅ | Empty, Filled, Error (invalid email), Loading |
| 3 | Register Sheet | RegisterSheet | ✅ | Empty, Filled, Error (password mismatch), Loading |
| 4 | Forgot Password | ForgotPasswordSheet | ✅ | Empty, Success |

### Landing

| # | Screen | Composable | Dark? | States |
|---|--------|------------|-------|--------|
| 5 | Landing/Home | LandingScreen | ✅ | Loading (shimmer), Success, Error |

### Scan

| # | Screen | Composable | Dark? | States |
|---|--------|------------|-------|--------|
| 6 | Scan Upload | ScanScreen | ✅ | Empty (no image), Image Selected, Loading (4-step progress), Error |
| 7 | Scan Result | ScanResultScreen | ✅ | Success (with glyphs), Error |
| 8 | Scan History | ScanHistoryScreen | ✅ | Empty, With Items, Error |

### Dictionary

| # | Screen | Composable | Dark? | States |
|---|--------|------------|-------|--------|
| 9 | Browse Tab | BrowseTab | ✅ | Loading, Success (grid), Empty Search |
| 10 | Learn Tab | LearnTab | ✅ | Loading, Success (lesson list) |
| 11 | Write Tab | WriteTab | ✅ | Palette loaded, Composition result |
| 12 | Translate Tab | TranslateTab | ✅ | Empty, Result |
| 13 | Sign Detail | DictionarySignScreen | ✅ | Loading, Success, Error |
| 14 | Lesson | LessonScreen | ✅ | Loading, Success (with practice items) |

### Explore

| # | Screen | Composable | Dark? | States |
|---|--------|------------|-------|--------|
| 15 | Explore List | ExploreScreen | ✅ | Loading (shimmer), Success, Empty, Error |
| 16 | Landmark Detail | LandmarkDetailScreen | ✅ | Loading (shimmer), Success (with tabs), Error |
| 17 | Identify Upload | IdentifyScreen | ✅ | Empty, Image Selected, Loading, Results, Error, No Match |

### Chat

| # | Screen | Composable | Dark? | States |
|---|--------|------------|-------|--------|
| 18 | Chat Empty | ChatScreen | ✅ | Empty (suggestions visible) |
| 19 | Chat Active | ChatScreen | ✅ | With messages (user + bot), Streaming (dots) |
| 20 | Chat Error | ChatScreen | ✅ | Error toast, Retry button |

### Stories

| # | Screen | Composable | Dark? | States |
|---|--------|------------|-------|--------|
| 21 | Stories List | StoriesScreen | ✅ | Loading (shimmer), Success (with cards), Empty, Error |
| 22 | Story Reader | StoryReaderScreen | ✅ | Loading, Chapter (text + glyphs), Interaction (quiz), Completion |

### Dashboard

| # | Screen | Composable | Dark? | States |
|---|--------|------------|-------|--------|
| 23 | Dashboard | DashboardScreen | ✅ | Loading, Success (stats + favorites), Empty Favorites, Error |

### Settings

| # | Screen | Composable | Dark? | States |
|---|--------|------------|-------|--------|
| 24 | Settings Full | SettingsScreen | ✅ | Default, Change Password dialog |
| 25 | Settings Quick | SettingsQuickDialog | ✅ | Default |

### Feedback

| # | Screen | Composable | Dark? | States |
|---|--------|------------|-------|--------|
| 26 | Feedback | FeedbackScreen | ✅ | Empty, Filled, Submitting, Success, Error |

### HieroglyphsHub

| # | Screen | Composable | Dark? | States |
|---|--------|------------|-------|--------|
| 27 | Hub | HieroglyphsHubScreen | ✅ | Loading, Success |

### Design System Components (expand existing)

| # | Component | Dark? | States |
|---|-----------|-------|--------|
| 28 | WadjetCard | ✅ | Default, Clickable, Shimmer |
| 29 | WadjetSearchBar | ✅ | Empty, With text, Focused |
| 30 | WadjetChip | ✅ | Selected, Unselected |
| 31 | EmptyState | ✅ | With icon + text |
| 32 | ErrorState | ✅ | With retry button |
| 33 | WadjetSectionLoader | ✅ | Default spinner |
| 34 | ShimmerCardList | ✅ | Default shimmer |
| 35 | OfflineIndicator | ✅ | Default banner |
| 36 | ImageUploadZone | ✅ | Empty, Hover/Focus |
| 37 | WadjetToast | ✅ | Error, Success |
| 38 | ScanProgressOverlay | ✅ | Each of 4 steps |

**Total: 38 screenshot test files × 2-4 state variants = ~100 golden images**

---

## Golden Baseline Process

### Record (first time / after intentional visual changes)
```bash
./gradlew recordRoborazziDebug
```
This generates golden images in `src/test/snapshots/`.

### Verify (CI / PR checks)
```bash
./gradlew verifyRoborazziDebug
```
Compares current renders against golden images. Fails if any pixel diff exceeds threshold.

### Update (after intentional visual changes)
```bash
./gradlew recordRoborazziDebug
git add src/test/snapshots/
git commit -m "chore: update screenshot baselines"
```

### CI Integration
Add to GitHub Actions:
```yaml
- name: Screenshot tests
  run: ./gradlew verifyRoborazziDebug
- name: Upload diffs on failure
  if: failure()
  uses: actions/upload-artifact@v4
  with:
    name: screenshot-diffs
    path: '**/build/outputs/roborazzi/'
```
