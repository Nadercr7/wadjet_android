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

---

# PHASE 3 — Firebase-done-right + backend integration (2026-07-06)

Supervisor decisions executed: KEEP+perfect Firebase (reverses E-P6), backend now in scope
(additive, branch `feat/firebase-integration` on Wadjet-v3-beta, never deployed by me),
premium gating REMOVED, on-device ML WIRED as offline-only fallback + 16KB fix.

## Workstream results (commit ↔ proof)

| Item | Commits | Runtime proof |
|---|---|---|
| B1 backend `POST /api/auth/firebase` | `f27c55a` + verifier fix | 233 pytest green (11 new); live: garbage→401, real exchange 201/200 |
| B2 backend Pexels proxy | `41c96c2` | live: no-auth 401, authed 200 w/ real photos; app thumbnails resolved through it |
| B3 backend green + boot | `850f9c5` (stale /write test) | full suite 233 passed 34.9s; local uvicorn boot + web pages serve |
| A1 Firebase-primary auth | `17466e2` | fresh sign-in → exchange **201** → Landing; **self-heal**: limits 401 → refresh 401 → auth/firebase 200 → retry 200, no logout; cold-restart persists |
| A2 firestore.rules | `a857fea` | rules+firebase.json+.firebaserc authored; deploy = PENDING-USER-CONSOLE (runbook §3); mirror PERMISSION_DENIED until then (expected) |
| A3 FCM | `7f83256` | POST_NOTIFICATIONS runtime request fired on first sign-in (perm flags flipped; granted after accept); token registrar writes users/{uid}/fcm_tokens (blocked on rules deploy); proper ic_stat_wadjet icon |
| A4 Analytics+Crashlytics | `7f83256` | screen_view/login/sign_up/scan_completed/story_completed wired; Crashlytics user-id + release CrashlyticsTree; dashboard checks = PENDING-USER-CONSOLE (runbook §5–6) |
| A5 runbook | `95d380a` | docs/audit/FIREBASE_RUNBOOK.md (ordered, copy-pasteable) |
| C1 gating removed | `401bb3b` | Stories list scrolled end-to-end on emulator: zero locks/Premium badges |
| C2 on-device ML + A-03 | `fe712d0` | offline scan: **Detected (6)** on 6-glyph image, banner+badge+source=on_device_onnx; online same image → server pipeline unchanged; ELF: all 24 .so p_align=0x4000 (4 ABIs) |
| C3 F-03/F-04 | `8931724` | dead CameraX code gone; no fake stage delays |
| C3 C-03 | `c187ede` | filters/query in SavedStateHandle (3 VMs) |
| C3 minors G-05a/E-05e/L-04d | `706dd46` | arrows out of EN+AR strings; isOnline naming; theme hexes centralized |
| J-01 Android half | `f1d53d0` | BuildConfig keys DELETED; proxy live-exercised by Explore thumbnails |

Full regression during E2E: all 5 tabs toured, 0 FATAL exceptions; Dictionary 1023 signs;
Thoth renders; session survives force-stop.

## DoD scorecard deltas (vs Phase 2 final)

- D-05 dual auth: FAIL→**PASS** (Firebase-primary, live-verified incl. self-heal)
- F-02 unused ML: OPEN→**PASS** (wired offline-only, honest UX)
- A-03 16KB: BLOCKED→**PASS** (empirical ELF proof, all ABIs)
- J-01 keys in APK: BLOCKED→**PASS** (proxy both sides, keys deleted)
- J-02 Firestore rules: NEEDS-DECISION→**PASS (code+rules) / PENDING-USER-CONSOLE (deploy)**
- Premium gating parity: NEEDS-DECISION→**PASS** (removed; matches web)
- N-firebase area: OPEN-by-design→**PASS-pending-console** (FCM permission+token registry, Analytics events, Crashlytics wiring, rules authored; console steps in runbook)
- Google Sign-In E2E: implemented + unit-verified; runtime = **PENDING-USER-CONSOLE** (Play-services credentials on emulator + SHA of the final signing cert)

## Blocked on the user (see FIREBASE_RUNBOOK.md)

1. Deploy `firestore.rules` (§3) — until then progress mirror + FCM token writes stay PERMISSION_DENIED.
2. Deploy backend branch + set `FIREBASE_PROJECT_ID`, `PEXELS_API_KEYS` (§4) — until then production sign-in for THIS app build and thumbnails-via-proxy don't work in prod.
3. Enable Crashlytics + verify Analytics DebugView (§5–6).
4. Optional: bulk-import web users into Firebase (§8); test push (§7).

---

# PHASE 4 — approved enhancements E-P8, E-P9, E-P1 (2026-07-06)

Humanization: COMPLETED in the v1.2.0 uplift (branch `uplift/premium`). No dedicated
humanization skill exists locally (the `25-Humanization` repo is AI-framework libraries,
not a copy skill), so this was a rigorous manual editorial pass: warm in-character "scribe"
voice across empty/error/loading states, Thoth replies, onboarding, and tips on BOTH web and
Android (EN + AR); MSA Arabic standardized (residual colloquialisms removed); em-dash-free
punctuation and a consistent middot separator; `…` ellipses; and H2 `<plurals>` for Arabic
number agreement (chat message-count + relative time, stories glyphs + chapters). Canonical
name واجِت preserved throughout. See docs/audit/UPLIFT_LOG.md (Stage H2/H4).

## Per-enhancement results (commit ↔ proof)

| E-P | What shipped | Commits | Runtime proof |
|---|---|---|---|
| E-P8 App Links | Backend serves `/.well-known/assetlinks.json` from `ANDROID_CERT_SHA256` env (404 while unconfigured); Android autoVerify https intent-filter + typed deep links `/stories`→list, `/stories/{id}`→reader | backend `99fef4b`, Android `b31bd9c` | assetlinks served LIVE by local boot (both fingerprints, correct statement shape — tests too); on emulator with the domain approved: `VIEW https://…/stories/creation-from-nun` opened the in-app READER, `…/stories` opened the list. `pm get-app-links` shows the domain registered; installed-APK signature == the debug fingerprint in the statement. AUTO-verify = PENDING-USER-DEPLOY (runbook §9 has the exact env value incl. release SHA-256 extracted from wadjet-release.jks) |
| E-P9 Push sender | `app/core/push_service.py` (Firestore `users/{uid}/fcm_tokens` → `send_each_for_multicast`, 500-token batches, prunes Unregistered/invalid-argument tokens) + admin-only `POST /api/push/send` (uid or broadcast, story_id/landmark_slug data for the app's deep links) | backend `a941a84` + wiring in `99fef4b` | 7 endpoint tests (401/403/400/503/success/single-target/sdk-failure→503) + 2 unit tests (batch chunking, prune policy) — 11 new tests green. Real delivery = PENDING-USER-CONSOLE (needs firestore.rules deploy + `GOOGLE_APPLICATION_CREDENTIALS`; manual test = runbook §7 Option A) |
| E-P1 Wi-Fi prefetch | `StoryPrefetchWorker` (@HiltWorker, daily, UNMETERED + battery-not-low, ≤3 attempts) pulls every story through the existing Room story_cache; Settings→Offline toggle (default ON, EN+AR); WadjetApplication observes the toggle to schedule/cancel; WorkManager on-demand init via HiltWorkerFactory | Android `d79d6f1` | LIVE: logcat `E-P1 prefetched 12/12 stories into Room cache` (+22 story GETs in backend log); airplane + force-stop + relaunch → NEVER-opened "The Book of Thoth" reader rendered fully offline (Ch 1/6; chapter image intentionally not prefetched → retry placeholder); JobScheduler dump shows `Network type: NOT_METERED&INTERNET` + `batteryNotLow=true` required (metered ⇒ CONNECTIVITY unsatisfied by construction); toggle OFF → `E-P1 prefetch cancelled`, ON → rescheduled + immediate 12/12 re-run |

## Regression

- Android: assembleDebug + :core:data/:feature:settings unit tests green (one build OOM'd from
  host memory pressure — NOT a code failure; passed after killing stale processes; hs_err logs cleaned).
- Backend: full pytest green (see below), local boot serves web unaffected
  (`/welcome` 200, `/` 302→welcome, `/api/health` 200, story page session-gate intact).
- Emulator note: one mid-session Android system_server wobble (System UI ANR) after heavy load;
  OS reboot inside the running 4GB instance recovered it; all checks passed after.

## Pending on the user (added to FIREBASE_RUNBOOK)

- §9: set `ANDROID_CERT_SHA256` (exact value ready — release + debug fingerprints) and deploy →
  App Links auto-verify goes live; drop the debug fingerprint for production-only.
- §7 Option A: service-account (`GOOGLE_APPLICATION_CREDENTIALS`) + rules deploy → real push E2E
  via `POST /api/push/send`.

---

# PHASE 5 — live production verification (2026-07-07)

Supervisor deployed everything (rules, backend→master→HF Space, Firebase env, Crashlytics).
Re-verified the production-URL build on the emulator against live prod. Raw logs:
`docs/audit/PHASE5_LIVE_LOGS.md`. Verify-only; no code changes needed (nothing regressed).

## DoD scorecard — PENDING items resolved

| Item | Phase 3/4 status | Phase 5 verdict | Proof |
|---|---|---|---|
| A1 auth vs PROD | code-verified vs local | **PASS** | `login` event + Landing; POST /api/auth/firebase 200 on prod; session persists cold restart |
| J-02 Firestore progress mirror | PASS(code)/PENDING deploy | **PASS** | rules live: chapter advance wrote progress (Room row completed=1), zero PERMISSION_DENIED |
| A3 FCM token registry | PASS(code)/PENDING deploy | **PASS** | `FCM token registered for user 0nYKZA…`, no write-failure, no PERMISSION_DENIED |
| A3 notification permission | PASS | **PASS** | runtime prompt on first sign-in AND on logged-in launch; granted → token flow |
| A4 Analytics events | PASS(code)/PENDING console | **PASS** | login, screen_view, story_completed, scan_completed all emitted (FA verbose) |
| A4 Crashlytics | PASS(code)/PENDING console | **PASS** (supervisor: 100% crash-free release) | + this session: 0 FATAL across full tour |
| E-P7/J-01 Pexels proxy | PASS(code) | **PASS** | thumbnails resolved via /api/images/pexels-search; 0 key literals in APK |
| E-P8 App Links (assetlinks) | PENDING deploy | **PASS (served) + NEEDS-RELEASE-BUILD (auto-verify)** | prod serves correct statement w/ release cert; debug build correctly not auto-verified; https link opens in-app reader |
| E-P8/B-02 in-app link routing | CLOSED | **PASS** | `https://…/stories/osiris-myth` → in-app StoryReader |
| C2 offline scan fallback | PASS | **PASS** | airplane: on-device ONNX, Detected(6), source on_device_onnx |
| E-P1 offline story read | PASS | **PASS** | airplane: cached Osiris reader rendered |
| Google Sign-In runtime | PENDING-USER-CONSOLE | **NEEDS-PHYSICAL-DEVICE** | Credential Manager flow launches correctly (GetGoogleIdOperation started); no Google account on emulator + release-signing needed to complete — can't be faked |

## Single remaining known-pending item

**E-P9 real FCM push delivery** — the sender + admin endpoint are live (`POST /api/push/send` → 401 unauth on prod), but sending needs `GOOGLE_APPLICATION_CREDENTIALS` (service account) which the supervisor confirms is STILL NOT set. To close: FIREBASE_RUNBOOK §7 Option A — generate a service-account key (Firebase console → Project settings → Service accounts), set `GOOGLE_APPLICATION_CREDENTIALS` to its path on the Space, then `POST /api/push/send` as the admin account delivers to the registered `fcm_tokens` (which we confirmed are now being written).

## Surprises

None that required a fix. Two non-defects worth noting: (1) App Links auto-verify correctly
fails for the debug-signed emulator build because prod assetlinks lists only the release cert
(security model working; a Play build verifies); (2) my first App-Links test used a wrong story
slug (`the-osiris-myth`) — routing was correct, the URL was mine; the real slug `osiris-myth`
opened the reader.

---

# PHASE 6 — final signed release APK (2026-07-07)

Produced the public-download release artifact. Verify-only + version bump; no features.

## Artifact
- Path: `app/build/outputs/apk/release/app-release.apk`
- Size: 135,634,001 bytes (~129.3 MB)
- versionCode **2**, versionName **1.1.0** (commit 35497d9) — installs as an update over 1.0.0
- SHA-256: `bbc13b76dd31bf141760191ed22eecee3de0870fdaeb5dc7735746376a31da61`
- Signer: v2 scheme, **release cert `1C:94:2D:…:B5:FB:D9:02`** (matches prod assetlinks.json)
- BASE_URL = production (https://nadercr7-wadjet-v2.hf.space)
- 16KB alignment: all native .so `p_align=0x4000` across 4 ABIs (ELF-checked on the release APK)

## Build notes
- Cache purged + `--no-build-cache`; R8 minify + resource shrink ON.
- **lintVital workaround (commit 14b8e71)**: `lint { checkReleaseBuilds = false }` — lintVitalAnalyzeRelease
  crashes with "Found class KaCallableMemberCall, but interface was expected" (a Kotlin-analysis-API
  mismatch inside the lint tool, analyzing core:designsystem/BorderBeam.kt; the log itself calls it
  "a bug in lint"). It is NOT R8 and NOT our code; R8/package/sign are unaffected. Debug `./gradlew lint`
  still runs.

## R8 SAFETY — ran the release build end-to-end (installed, not just built). ZERO keep rules needed.
| Path (R8 risk) | Result |
|---|---|
| Launch | clean, no ClassNotFound/NoClassDefFound |
| Email/password sign-in vs PROD | Landing "Welcome back"; POST /api/auth/firebase 200; `login` analytics — kotlinx-serialization DTOs survived R8 |
| 5-tab tour (Explore/Stories/Thoth/Hieroglyphs/Home) | all render (Retrofit + serialization + Pexels proxy) |
| Scan ONLINE (server) | AI Vision, Detected(6), `scan_completed{detection_source=ai_vision}` — multipart + ScanResponse deserialize OK |
| Scan OFFLINE (on-device ONNX) | Detected(6), `scan_completed{detection_source=on_device_onnx}` — **ONNX Runtime native reflection survived R8**, no UnsatisfiedLinkError |
| Story read | "The Golden Age" (Osiris) rendered |
| Chat (Thoth) | SSE response streamed |
| TTS | "Listen" → MediaPlayer active (speak POST → cache → AudioPlaybackManager) |
| **0 FATAL** across the whole session | crash buffer clean |

The existing app/proguard-rules.pro (serialization $$serializer + serializer(), Retrofit, Room,
ONNX ai.onnxruntime.**, Hilt, Crashlytics, Credential Manager/googleid, enums) already covered
everything — no new rule added.

## App Links (release cert now matches prod assetlinks)
```
pm verify-app-links --re-verify → pm get-app-links:
  nadercr7-wadjet-v2.hf.space: verified
```
Cold `VIEW https://nadercr7-wadjet-v2.hf.space/stories/eye-of-ra` → in-app reader "The Wrath of
the Eye". This is the Phase-5 NEEDS-RELEASE-BUILD item now **PASS** on the release artifact.

## Google Sign-In
Flow launches cleanly on the release build (CredentialManager → GoogleIdService,
TYPE_GOOGLE_ID_TOKEN_CREDENTIAL matched, googleid keep-rules intact under R8, no FATAL).
Still cannot COMPLETE on the emulator (no Google account provisioned) → **NEEDS-PHYSICAL-DEVICE**.

## DoD deltas
- App Links auto-verify: NEEDS-RELEASE-BUILD → **PASS** (verified on the release APK).
- Release artifact R8-safe: **PASS** (full E2E, 0 keep rules, 0 FATAL).
- Google Sign-In: **NEEDS-PHYSICAL-DEVICE** (flow proven to launch under R8).
- Still the only pending delivery item: E-P9 push send (service-account env — runbook §7).
