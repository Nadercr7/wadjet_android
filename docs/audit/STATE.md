# STATE.md — Wadjet-Android Audit Baseline

_Living document. Updated by the audit agent. Started: 2026-07-06._

## Baseline

| Item | Value |
|---|---|
| Audit branch | `audit/fable-2026-07-06` |
| Branched from | `main` @ `d0322bb166f150ba3eccacb43b720649a9771b6d` ("chore: backup work-in-progress before laptop migration", 2026-07-06 09:20 +0300) |
| Repo clean at start | YES (both Wadjet-Android and Wadjet-v3-beta) |
| Reference repo | `D:\Personal attachements\Projects\Wadjet-v3-beta` @ `59563f6d` — READ-ONLY |

## Environment

| Item | Value |
|---|---|
| OS | Windows 11 Pro 10.0.26200 |
| JDK | OpenJDK 17.0.17 (Microsoft build) |
| Gradle wrapper | present (`gradlew.bat`) |
| AGP / Kotlin / KSP | 8.7.3 / 2.1.0 / 2.1.0-1.0.29 |
| Compose BOM | 2026.03.00 (compiler via kotlin-compose plugin) |
| Key libs | Hilt 2.53.1, Retrofit 2.11 + kotlinx-serialization, Room 2.7.1, CameraX 1.4.1, ONNX Runtime 1.20.0, Firebase BOM 33.7.0, Coil 3.0.4 |
| Device/emulator | **NONE ATTACHED** (`adb devices` empty) → all runtime checks are UNVERIFIED in Phase 1 |
| Backend/API keys availability | Not verified — runtime API/TTS/chat checks UNVERIFIED |

## Baseline build (BEFORE any change)

- Attempt 1 — `gradlew.bat assembleDebug` (incremental, caches inherited from old laptop): **FAILED** —
  `:app:compileDebugJavaWithJavac` → 14× `[Dagger/MissingBinding]` (every `core:network` binding: all ApiServices, OkHttpClient, Json).
  Analysis: `NetworkModule` is correctly annotated (`@Module @InstallIn(SingletonComponent)`), hilt+ksp applied, `:app` depends on `:core:network`; HEAD commit only added deck/marketing files (no code). → prime suspect: **stale build/KSP caches from the laptop migration**. Log: scratchpad `baseline-build.log`.
- Attempt 2 — `gradlew.bat clean assembleDebug`: **FAILED identically** (3m20s, "108 from cache") → not stale module outputs; the local **Gradle build cache** itself is poisoned.
- Diagnosis proven: `:core:network:compileDebugJavaWithJavac --no-build-cache --rerun-tasks` → SUCCESS; `NetworkModule_Provide*Factory.class` + `hilt_aggregated_deps` now on disk (the FROM-CACHE output had contained only `BuildConfig.class`).
- Attempt 3 — `gradlew.bat assembleDebug --no-build-cache`: **BUILD SUCCESSFUL** (1m13s, 640 tasks). APK: `app/build/outputs/apk/debug/app-debug.apk` (119.5 MB — large; includes 16.6 MB unused ONNX models + ONNX Runtime .so, see FIXLOG F-02).
- **Baseline verdict: code at HEAD compiles; the default build path is broken by a corrupt `~/.gradle/caches/build-cache-1` entry (machine migration artifact). Until purged, builds need `--no-build-cache`.** (FIXLOG A-02)
- Compiler warnings (clean build): ~14 deprecation warnings across chat/dictionary/database (ChatHistoryStore ×4, LessonScreen ×3, DictionaryScreen ×2, ClickableText, MarkdownText, `stabilityConfigurationFile`, `fallbackToDestructiveMigration`, RequestBody.create). No errors.
- `gradlew.bat installDebug` + launch: NOT POSSIBLE (no device/emulator attached) → all runtime checks UNVERIFIED.

## Notes / known repo facts

- 10 feature modules + 9 core modules (see settings.gradle.kts), ~207 Kotlin files.
- `values-ar` string resources exist in every module (per supervisor ground truth) — Arabic issues are wiring/RTL, not missing translations.
- 8× `hs_err_pid*.log` + `replay_pid25100.log` in repo root = historical JVM crashes during builds. `gradle.properties` already contains likely mitigations (`-Xmx2048m`, `kotlin.compiler.execution.strategy=in-process`, `org.gradle.workers.max=2`).
- `wadjet-release.jks`, `signing.properties`, `local.properties` sit in repo root but are **NOT tracked by git** (verified via `git ls-files`).
- Untracked-but-present marketing files: `wadjet-android-deck*.html`, `DECK-PLAN.md`, `build-deck.ps1`, `logo-base64.txt`, `script-ar.md`.
