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

### Batch 3 — Arabic + audio (all committed & verified on emulator)

| Fix | Commit | Runtime proof |
|---|---|---|
| G-06 reader Arabic content | 877e999 | Arabic titles/paragraphs/annotations/interactions rendered in reader |
| H-02 pronunciation preset | 3a82fe1 | speak body `context:"pronunciation"` -> 200, played |
| H-03 drop voice/style | b5bdc77 | live speak bodies = text/lang/context only |
| H-04 lang threading + voice checks | 0602adf, 78d1bb2 | `GET /api/landmarks?...&lang=ar` in Arabic (was en) |
| H-06 single player + audio focus | 58fbdfc | narration + dictionary both via manager |
| H-05 local TTS fallback | 0f7f494 | airplane mode + uncached sign -> device voice played |
| H-07 TTS disk cache 30MB LRU | 2de725f | replays on 2 paths: audio plays, 0 network requests |
| G-03 five missing ar keys | afc387c | parity script: 0 missing keys |
| G-02 real Arabic, 10 modules | 68806bf | full Arabic tour: Home/nav/Stories/Dictionary/Explore/QuickSettings |
| G-04 VM strings -> resources | f3df668, 9a2fdaf | build + auth tests green; StringResolver locale-aware |

- New finding G-07 (minor): value-constants chips (difficulty/sign types/feedback categories) still EN — value<->label mapping needed (Batch 5 candidate).
- Arabic narration end-to-end re-verified after all audio refactors: Arabic paragraph -> lang=ar -> 200 audio/wav -> played; replay served from client cache.

### Batch 4 — UI/perf/security (committed & verified on emulator)

| Fix | Commit | Proof |
|---|---|---|
| L-01 dark system bars | a2026bf | explicit SystemBarStyle.dark both bars |
| I-02 prepareAsync | 0b8f99f | playback works via async path |
| I-03 ApplicationScope persistence | 22de1c0 | build green, persistence intact |
| I-04 off-main base64 decode | 8a9945c | produceState + Dispatchers.Default |
| I-01 painter-based list images | c966a49 | story covers render in lists |
| E-03 Room indices (v9) | 8578885 | real v8->v9: 5 indices, data survived |
| L-02 single top bar, no root back arrows | f8f22d7 | screenshot: one Stories bar |
| K-01 48dp touch targets | 705ad91 | 6 controls enlarged |
| B-01 wadjet:// deep links + FCM intents | 529f168 | am start VIEW opened reader (cold) + landmark (onNewIntent) |
| E-04 bundled seed data | 8b327fe | pm clear + airplane: seeded 1023 signs/26 cats/164 landmarks |

- J-01 BLOCKED: backend has no Pexels surface; proxy needs a server endpoint (backend read-only). Follow-up E-P7.
- D-05 STOPPED per guardrail: clean fix = drop Firebase Auth for backend-only auth (incl. /auth/google) — full plan in ENHANCEMENTS E-P6.

### Batch 5 — minors + hygiene (committed; build + full unit-test suite green)

- L-03 (baa507d) chapter-progress resource; B-03 (8a8fb15) verify-gate reload; G-07 (4725a5e) localized chip labels with stable API values; C-02 (2eb8179) rememberSaveable toggles; C-04 (d0e17d2) dead Splash route removed; J-03 (8bd0285) Room DB + prefs excluded from backup/transfer; A-01 (825df3f) heap 3g + crash dumps gitignored; G-05/L-04/I-05/E-05 minors (dcf4f02, shared commit for four minor-bundle ids); K-02 (6f36433) contrast; test adaptations (bd8146e).
- A-03 (16 KB alignment) stays OPEN, blocked on the F-02 ONNX deferral (supervisor decision) — must be addressed before Play API-35+ targeting.

## Final status (2026-07-06, end of Phase 2)

### Regression
- Full `assembleDebug` green; FULL unit-test suite green (`testDebugUnitTest` all modules).
- Emulator regression tour post-Batch-5: Home / Hieroglyphs / Explore / Stories / Thoth / reader / dictionary all render, 0 FATAL exceptions in logcat.
- 49 commits on `audit/fable-2026-07-06` since baseline d0322bb (44 fix/feat/test/chore + 5 docs).

### DoD scorecard

| Area | Status | Proof |
|---|---|---|
| A build | PASS (A-02 cache purged; A-01 heap+hygiene) | plain assembleDebug + full tests green |
| B parity | PASS w/ notes | contract fixes D-01..D-08 verified live; B-01 deep links verified; B-03 fixed; premium-gating parity = NEEDS-DECISION (web has none) |
| C nav/state | PASS | C-01 race fixed+verified; C-02 partial (savers noted); C-04a fixed |
| D network | PASS | every D-fix proven against the live backend (bodies + status codes in FIXLOG) |
| E db/offline | PASS | E-01 (real v7→v8 test), E-02 (offline list+reader), E-03 (v9 indices, real upgrade), E-04 (offline seed 1023 signs), E-05a |
| F ml/scan | PASS w/ deferral | F-01 EXIF verified server-side; F-02 ONNX deferred by supervisor (drives A-03) |
| G i18n | PASS | G-01..G-07: runtime switcher, full Arabic content, RTL, reader Arabic, chips localized — all toured in Arabic on emulator |
| H audio | PASS | H-01..H-07 verified live: Arabic narration, correct presets, fallback to device TTS (offline test), single player + focus, 0-network replays via disk cache |
| I perf | PASS w/ debt | I-01..I-04 fixed; I-05 partials, rest logged as debt |
| J security | PASS w/ blockers noted | J-03 fixed; J-01 blocked on backend endpoint (E-P7); J-02 Firestore rules = server-side (evidence logged) |
| K a11y | PASS | K-01 48dp targets; K-02 contrast |
| L ui | PASS | L-01..L-04 fixed (L-04d cosmetic open); dark splash regression fixed |
| M offline | PASS | seeded fresh-install offline + cached stories offline + TTS cache + local TTS fallback |
| N firebase | OPEN by design | dual-auth unification (D-05) STOPPED per guardrail — full plan E-P6; Firestore mirror dead (J-02 evidence) |
| O debt | LOGGED | remaining debt itemized in E-05/I-05/L-04/C-04 bundles |

### Blocked / needs-decision (supervisor)
1. D-05 auth unification — plan E-P6 (drop Firebase Auth, backend-only incl. /auth/google).
2. J-01 Pexels proxy — needs backend endpoint (E-P7).
3. Premium story gating — Android positional lock vs web having NO gating (NEEDS-DECISION in FIXLOG E-02 note).
4. F-02 ONNX (deferred by supervisor) → keeps A-03 16 KB alignment open.
5. J-02 Firestore rules / dead mirror — server-side confirmation then E-P4.

## Notes / known repo facts

- 10 feature modules + 9 core modules (see settings.gradle.kts), ~207 Kotlin files.
- `values-ar` string resources exist in every module (per supervisor ground truth) — Arabic issues are wiring/RTL, not missing translations.
- 8× `hs_err_pid*.log` + `replay_pid25100.log` in repo root = historical JVM crashes during builds. `gradle.properties` already contains likely mitigations (`-Xmx2048m`, `kotlin.compiler.execution.strategy=in-process`, `org.gradle.workers.max=2`).
- `wadjet-release.jks`, `signing.properties`, `local.properties` sit in repo root but are **NOT tracked by git** (verified via `git ls-files`).
- Untracked-but-present marketing files: `wadjet-android-deck*.html`, `DECK-PLAN.md`, `build-deck.ps1`, `logo-base64.txt`, `script-ar.md`.
