# Stage 11 — NiA and Best Practices Reference

**Date:** 2025-07-22  
**Auditor:** Automated (Copilot)  
**Scope:** Now in Android testing patterns, android-testing SKILL.md, Wadjet current test infra gaps

---

## Summary

| Metric | Value |
|---|---|
| **NiA patterns applicable** | 9/9 (all directly applicable) |
| **Test infra gaps in Wadjet** | 5 critical missing pieces |
| **Dependencies present** | 13/17 — missing `core/testing` module, `@TestInstallIn`, custom `TestRunner`, `MainDispatcherRule` |
| **Roborazzi coverage** | 1 module only (`core/designsystem`) — needs expansion to all feature modules |

---

## 1. NiA Testing Patterns

| Pattern | How NiA Does It | Applicable to Wadjet? | Notes |
|---|---|---|---|
| **Roborazzi Screenshot Tests** | Dedicated `core/screenshot-testing` module with `captureMultiTheme()` (6 permutations: light/dark × dynamic-color × android-theme). JVM via Robolectric with `@GraphicsMode(NATIVE)`, `@Config(qualifiers = "480dpi")` | ✅ **Already partially done** — `ScreenshotHelper.kt` + `ButtonScreenshotTest.kt` in `core/designsystem` | Wadjet's helper is simpler (single-theme). Add dark-mode permutations. NiA also does accessibility checks via `checkRoboAccessibility` |
| **`@TestInstallIn` Module Replacement** | `core/data-test` module with `TestDataModule` using `@TestInstallIn(replaces = [DataModule])` to swap real repos for fakes globally | ✅ **Critical gap** — Wadjet has no `@TestInstallIn` modules | Create `core/testing` module with test DI modules |
| **Fake Repositories (two layers)** | Layer 1 (`core/testing`): `MutableSharedFlow`-based fakes for VM tests. Layer 2 (`core/data-test`): backed by demo data source for integration tests | ✅ Wadjet uses MockK — consider adding flow-based fakes for cleaner tests | `TestNewsRepository` with `sendNewsResources()` pattern maps to Wadjet repos |
| **`MainDispatcherRule`** | `TestWatcher` calling `Dispatchers.setMain(UnconfinedTestDispatcher())` | ✅ **Wadjet does manually** in `@Before/@After` in every VM test | Single shared rule replaces boilerplate in ~10 test files |
| **ViewModel Unit Tests** | Construct VM directly with fake repos (no Hilt). `backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.stateFlow.collect() }` | ✅ Already follows this pattern (with `StandardTestDispatcher` + `advanceUntilIdle()`) | Both approaches valid |
| **Repository Unit Tests** | Real repo implementations with test doubles (`TestDao`, `TestNetworkDataSource`, `InMemoryDataStore`). No mocking framework | ✅ Partially done — Wadjet uses MockK. Could add hand-written test doubles for complex collaborators | Hand-written test doubles > MockK for DAOs |
| **Navigation Integration Test** | `@HiltAndroidTest` with `createAndroidComposeRule<MainActivity>()`. `HiltAndroidRule` (order=0), permission rule (1), compose rule (2). Real repos swapped via `@TestInstallIn` | ✅ **Wadjet needs this** — requires custom `HiltTestRunner`, `@TestInstallIn`, `ui-test-hilt-manifest` | Highest-priority integration test to add |
| **Custom Test Runner** | `NiaTestRunner extends AndroidJUnitRunner`, overrides `newApplication()` → `HiltTestApplication` | ✅ **Missing in Wadjet** — current runner is default `AndroidJUnitRunner` | Hilt instrumented tests crash without this |
| **Test Data Fixtures** | `core/testing/data/` with shared test data objects across all modules | ✅ Good pattern — avoid duplicating test data | Create shared `SignTestData`, `LandmarkTestData`, `UserTestData`, `ChatMessageTestData` |

---

## 2. Roborazzi Setup for Wadjet

### Already Configured (in `core/designsystem` only)
- `roborazzi` plugin v1.59.0 in `libs.versions.toml` ✅
- Plugin applied in `core/designsystem/build.gradle.kts` ✅
- `roborazzi`, `roborazzi-compose`, `roborazzi-rule` test deps ✅
- `isIncludeAndroidResources = true` in testOptions ✅
- `ScreenshotHelper.kt` with `captureScreenshot()` ✅
- `ButtonScreenshotTest.kt` working ✅

### Missing / Needed
1. **Dark mode permutations** — only captures one theme state. Add `captureMultiTheme()`
2. **Feature module expansion** — add Roborazzi plugin to feature modules for screen-level screenshots
3. **Multi-device captures** — NiA captures phone/foldable/tablet. Wadjet only captures one config
4. **CI integration** — `recordRoborazziDebug` / `verifyRoborazziDebug` tasks
5. **Accessibility screenshot checks** — `checkRoboAccessibility` pattern from NiA

### Adding Roborazzi to a Feature Module
```kotlin
// feature/chat/build.gradle.kts
plugins {
    alias(libs.plugins.roborazzi)
}
android {
    testOptions { unitTests { isIncludeAndroidResources = true } }
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

---

## 3. Recommended Test Structure for Wadjet

```
core/
  testing/                          ← NEW shared module
    src/main/kotlin/com/wadjet/core/testing/
      WadjetTestRunner.kt           ← HiltTestApplication runner
      util/MainDispatcherRule.kt    ← Shared dispatcher rule
      data/                         ← Shared test fixtures
        SignTestData.kt
        LandmarkTestData.kt
        UserTestData.kt
        ChatMessageTestData.kt
      repository/                   ← Flow-based test repos
        TestAuthRepository.kt
        TestChatRepository.kt
        TestDictionaryRepository.kt
      di/TestDispatcherModule.kt    ← @TestInstallIn for dispatchers
```

### Naming Conventions
| Test Type | Suffix | Location | Needs Hilt? |
|---|---|---|---|
| ViewModel unit | `*ViewModelTest` | `src/test/` | No — construct directly |
| Repository unit | `*RepositoryImplTest` | `src/test/` | No — MockK or hand-written fakes |
| DAO integration | `*DaoTest` | `src/androidTest/` | Optional (in-memory Room) |
| Screenshot | `*ScreenshotTest` | `src/test/` | No — Robolectric + Compose rule |
| Compose UI | `*ScreenTest` | `src/androidTest/` | Yes — `@HiltAndroidTest` |
| Navigation E2E | `NavigationTest` | `app/src/androidTest/` | Yes — full app with `@TestInstallIn` |

---

## 4. Testing SSE Streams (ChatRepository)

Wadjet's `ChatRepositoryImpl` uses OkHttp SSE. MockWebServer with SSE responses is the recommended approach:

```kotlin
class ChatRepositorySSETest {
    private val server = MockWebServer()

    @Test
    fun `sendMessage emits streamed tokens`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/event-stream")
                .setBody("data: {\"token\": \"Hello\"}\n\ndata: {\"token\": \" World\"}\n\ndata: [DONE]\n\n")
                .throttleBody(50, 100, TimeUnit.MILLISECONDS)
        )
        // ... construct repo with server.url, assert flow emissions
    }
}
```

**Required**: Add `okhttp-mockwebserver` to `core/data/build.gradle.kts` (currently only in `core/network`).

---

## 5. Testing Hilt ViewModels

ViewModel tests should **NOT** use Hilt. Construct directly with fakes:

```kotlin
class ChatViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()
    private val chatRepository = TestChatRepository()
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setup() {
        viewModel = ChatViewModel(chatRepository)
    }

    @Test
    fun `initial state is empty`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collect()
        }
        assertIs<ChatUiState.Empty>(viewModel.uiState.value)
    }
}
```

Use `@HiltAndroidTest` **only** for full-screen instrumented UI tests.

---

## 6. Key Dependencies — Present vs Missing

| Dependency | Version | In TOML? | Used? | Status |
|---|---|---|---|---|
| `junit` | 4.13.2 | ✅ | ✅ all modules | Ready |
| `mockk` | 1.13.13 | ✅ | ✅ `core/data`, `core/network`, features | Ready |
| `coroutines-test` | 1.9.0 | ✅ | ✅ `core/data`, `app` | Ready |
| `turbine` | 1.2.0 | ✅ | ✅ `core/data`, `app` | Ready |
| `robolectric` | 4.16 | ✅ | ✅ `core/designsystem` only | Expand to features |
| `roborazzi` | 1.59.0 | ✅ | ✅ `core/designsystem` only | Expand to features |
| `compose-ui-test` | BOM | ✅ | ✅ `app`, `core/designsystem` | Ready |
| `hilt-testing` | 2.53.1 | ✅ | ✅ `app` androidTest only | Ready |
| `okhttp-mockwebserver` | 4.12.0 | ✅ | ✅ `core/network` only | ⚠️ Add to `core/data` for SSE |
| `room-testing` | 2.7.1 | ✅ | ❌ Not used | ⚠️ Add to `core/database` |
| `espresso-core` | 3.6.1 | ✅ | ✅ `app` androidTest | Ready |
| Custom `TestRunner` | — | — | ❌ | ❌ **Missing** |
| `core/testing` module | — | — | ❌ | ❌ **Missing** |
| `@TestInstallIn` modules | — | — | ❌ | ❌ **Missing** |
| `MainDispatcherRule` | — | — | ❌ | ❌ **Missing** |

---

## 7. Priority Actions

1. **Create `core/testing` module** — `MainDispatcherRule`, test fixtures, `WadjetTestRunner`, flow-based fake repos
2. **Add `@TestInstallIn` modules** — `TestDataModule`, `TestNetworkModule` for Hilt instrumented tests
3. **Add `mockwebserver` to `core/data`** — enables SSE testing for `ChatRepositoryImpl`
4. **Add `room-testing` to `core/database`** — enables migration tests (critical given `fallbackToDestructiveMigration`)
5. **Expand Roborazzi** — apply plugin to feature modules, add multi-theme capture
