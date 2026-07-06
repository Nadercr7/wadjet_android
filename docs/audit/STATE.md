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

## Phase 2 (2026-07-06)

- A-02 REMEDIATED: purged `~/.gradle/caches/build-cache-1`; plain `assembleDebug` now **BUILD SUCCESSFUL** with no flags. Plain `installDebug` green.
- Emulator: existing AVD **Pixel_8** (API 36.1, x86_64) booted; app installed and launched — cold start ~10.3s (debug, emulator), no crash.
- Test account: `wadjetaudit11803@web-library.net` (mail.tm disposable; password in session only). Full auth E2E verified: register → backend+Firebase account → verification email received → oobCode applied → gate passes → Landing.

### Runtime screen-tour baseline (all rendered, none crashed)

| Screen | Status | Notes |
|---|---|---|
| Welcome | OK | black/gold correct |
| Register sheet | OK | password-strength meter live |
| VerifyEmail sheet | OK | hard gate; honest error on unverified |
| Landing (Home) | OK | single top bar |
| Hieroglyphs hub | OK | live counts (1023 signs), suggested signs from API |
| Explore | OK | live landmarks+images; **L-02 double bar + back arrow visible** |
| Stories | OK | live list, premium locks; L-02 double bar |
| Thoth chat | OK | greeting, suggestions; L-02 double bar |
| Dashboard | OK | stats zeros, favorites empty-state |
| Quick Settings dialog | OK | TTS+cache only — **G-01 no language row confirmed** |
| Settings (full) | OK | Profile/TTS/Storage/About/SignOut; no Language row |
| Feedback | OK | categories + form |
| Dictionary Browse | OK | glyph font renders; categories/filters live |
| Sign detail sheet (A1) | OK | fun fact, fav/copy/share |
| Dictionary Write | OK | "sun"→N5 ra ☉ + breakdown via POST /api/write |
| Story reader (Creation from Nun) | OK | chapter image, narration btn, glyph annotation |
| Learn tab | OK (rendered before Write) | |
| NOT YET TOURED | Scan flow, ScanResult, ScanHistory, Lesson, LandmarkDetail, Identify, ChatLandmark, chat send/stream | will be exercised during their fixes |
- Compiler warnings (clean build): ~14 deprecation warnings across chat/dictionary/database (ChatHistoryStore ×4, LessonScreen ×3, DictionaryScreen ×2, ClickableText, MarkdownText, `stabilityConfigurationFile`, `fallbackToDestructiveMigration`, RequestBody.create). No errors.
- `gradlew.bat installDebug` + launch: NOT POSSIBLE (no device/emulator attached) → all runtime checks UNVERIFIED.

### Batch 1 — blockers (all committed & verified on emulator)

- D-01 (619803f) STT path; C-01 (ba9da31) sign-out race; G-01 (a4aaa6d) EN/AR runtime switcher incl. RTL + Cairo + persistence; H-01 (44d2189) Arabic narration (lang=ar → 200 audio/wav, played).
- G-01 side-effect (white API-31 system splash from AppCompat parent) fixed: values-v31 `windowSplashScreenBackground` Night — verified dark splash screenshot.

### Batch 2 — contract fixes (all committed & verified live on emulator + real backend)

| Fix | Commit | Runtime proof |
|---|---|---|
| D-03 num_detections | 349af0b | scan returned `"num_detections":6`, UI "Detected (6)" |
| D-04 logout cookie | 9229f58 | `POST /api/auth/logout` sent `Cookie: wadjet_refresh=…` → 200 + server `Max-Age=0` revoke |
| D-08 refresh race logout | 42ba655 | offline cold-start/browsing kept session; no spurious logout |
| D-06 totalPages | eccbc53 | total:164/per_page:24 → scroll fired `page=2` → 200 |
| E-01 downgrade-only fallback | b19b629 | real v7→v8 upgrade: user_version 8, v7 progress row survived |
| E-02 story offline cache | 320cea4 | airplane+force-stop+relaunch: list in server order + reader offline |
| D-02 glyphs_learned string | a360268 | progress POST body `"glyphs_learned":"[]"` → 200 (was 422) |
| F-01 EXIF upright | 94e71fe | EXIF-6 portrait file uploaded upright (server `image_size 600×250`, 6 glyphs) |

- Migration test rig: worktree `D:\audit-wt-v7` builds the pre-Batch-2 commit (DB v7) — reusable for future schema bumps; remove at engagement end.
- Emulator: AVD RAM raised 2048→4096 MB (`-memory 4096`, cold boot) after lowmemorykiller killed the app as TOP at 2 GB. UI driving via scratchpad `ui.py` (uiautomator dump → tap).
- New live findings this batch: J-02 update (Firestore story_progress write PERMISSION_DENIED for own uid → mirror is dead), B-03 (verify gate on verified accounts), G-06 (reader shows EN text in Arabic).

## Notes / known repo facts

- 10 feature modules + 9 core modules (see settings.gradle.kts), ~207 Kotlin files.
- `values-ar` string resources exist in every module (per supervisor ground truth) — Arabic issues are wiring/RTL, not missing translations.
- 8× `hs_err_pid*.log` + `replay_pid25100.log` in repo root = historical JVM crashes during builds. `gradle.properties` already contains likely mitigations (`-Xmx2048m`, `kotlin.compiler.execution.strategy=in-process`, `org.gradle.workers.max=2`).
- `wadjet-release.jks`, `signing.properties`, `local.properties` sit in repo root but are **NOT tracked by git** (verified via `git ls-files`).
- Untracked-but-present marketing files: `wadjet-android-deck*.html`, `DECK-PLAN.md`, `build-deck.ps1`, `logo-base64.txt`, `script-ar.md`.
