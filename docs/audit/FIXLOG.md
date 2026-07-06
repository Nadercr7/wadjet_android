# FIXLOG.md — Wadjet-Android (append-only)

Every finding, every change, and its verification result. Format:

```text
## [ID] <title>
- Date:
- Severity: blocker | major | minor
- Area: A-build | B-parity | C-nav | D-network | E-db | F-ml | G-i18n | H-audio | I-perf | J-security | K-a11y | L-ui | M-offline | N-firebase | O-debt
- Evidence: file:line + note
- Expected (web) behaviour:
- Status: OPEN | FIXED (commit sha) | WONT-FIX | NEEDS-DECISION
- Verification:
```

---
_Phase 1 findings are appended below once the audit sweep completes._

## [F-01] EXIF orientation stripped before upload → rotated photos degrade scan accuracy
- Date: 2026-07-06
- Severity: major
- Area: F-ml
- Evidence: `feature/scan/.../ScanViewModel.kt:229-248` (`compressImage` uses `BitmapFactory.decodeFile`, ignores EXIF, re-encodes JPEG q85 with no EXIF); identical copy in `feature/explore/.../IdentifyViewModel.kt:118-137`. Always called (`ScanViewModel.kt:144`).
- Expected (web) behaviour: server `app/api/scan.py:70-92` relies on `ImageOps.exif_transpose` — needs the EXIF tag the client destroys. Web browser uploads preserve EXIF.
- Status: **FIXED** (commit 94e71fe) — `uprightBitmap()` applies the ExifInterface orientation matrix before JPEG re-encode in both ScanViewModel and IdentifyViewModel.
- Verification: emulator, test image `glyphs_exif6.jpg` (stored 500×1200 portrait, EXIF orientation=6). Scan → server reported `image_size 600×250` (upright landscape, aspect preserved) and detected 6 glyphs at horizontal bbox positions → orientation was baked in client-side.

## [F-02] 16.6 MB unused ONNX models + unused onnxruntime-android dependency (pure APK bloat)
- Date: 2026-07-06
- Severity: major
- Area: F-ml / I-perf
- Evidence: `app/src/main/assets/models/**` (3 .onnx, byte-identical to web repo, never referenced by any Kotlin code); `core/ml` module is an empty stub (`Placeholder.kt`); `core/ml/build.gradle.kts:27` pulls onnxruntime-android native .so libs for nothing.
- Expected: either on-device inference wired up, or assets+dependency removed. All inference is server-side (`POST api/scan`) — matches web, so removal is safe.
- Status: **FIXED** (Phase 3 commit fe712d0, supervisor decision: WIRE as offline-only fallback) — :core:ml implements the full detect→classify pipeline (YOLO26 640 letterbox + MobileNetV3 128/171-classes, label_mapping.json); ScanRepositoryImpl falls back ONLY on IOException (server unreachable); results flagged detectionSource=on_device_onnx + aiUnverified with a dedicated offline banner (EN+AR); no transliteration/translation (server-only capabilities, stated honestly in UX). Online behavior byte-identical.
- Verification: LIVE on emulator — airplane mode + Photo Picker scan of the 6-glyph test image → "Detected (6)" (same count the server found for this image), offline banner + "On-device (offline)" badge, confidences avg 79%, Source: on_device_onnx, timing 3755ms det + 430ms cls; back online the SAME image went through POST /api/scan → 200 with full server pipeline (Ai_fresh groq, transliteration) — server stays primary.

## [F-03] Dead camera code: CameraX disabled, unused PermissionDeniedContent + onImageCaptured plumbing
- Date: 2026-07-06
- Severity: minor
- Area: F-ml / O-debt
- Evidence: `feature/scan/.../ScanScreen.kt:143-255` commented-out CameraX blocks; live-but-unreferenced `PermissionDeniedContent` at `ScanScreen.kt:419-446`; CAMERA permission commented in manifest. Image input = Photo Picker (`core/designsystem/.../ImageUploadZone.kt:62-69`, no permission needed).
- Expected: camera capture is a web-parity question (web is upload-only too) → currently by-design; dead code should be cleaned.
- Status: **FIXED** (Phase 3 commit 8931724) — both commented-out CameraX blocks (CameraPreview/GoldBracketOverlay + PermissionDeniedContent, ~117 lines) removed; Photo Picker remains the by-design input (web parity).
- Verification: code-read.

## [F-04] Scan progress overlay is fake (fixed delays, mislabeled stages)
- Date: 2026-07-06
- Severity: minor
- Area: F-ml / L-ui
- Evidence: `ScanScreen.kt:257-417` + `ScanViewModel.kt:151-156` hard-coded `delay(400/400/300)`; server `timing`/`detection_source` ignored (often `ai_vision`, not ONNX).
- Expected: progress reflecting real state, or at least honest labels.
- Status: **FIXED** (Phase 3 commit 8931724) — the fake post-response stage delays (400/400/300ms after the server already returned) are removed; overlay goes straight to DONE. Pre-response stages remain (they describe the real server pipeline).
- Verification: code-read.

## [E-01] fallbackToDestructiveMigration() enabled → silent wipe of scan history/progress/favorites
- Date: 2026-07-06 | Severity: major | Area: E-db
- Evidence: `core/database/.../DatabaseModule.kt:28`. DB v7, migrations only 4→5, 5→6, 6→7; any other path (incl. downgrade) drops the DB.
- Expected: explicit migrations only, or `fallbackToDestructiveMigrationOnDowngrade()`.
- Status: **FIXED** (commit b19b629) — downgrade-only fallback; MIGRATION_7_8 registered (with E-02).
- Verification: real v7→v8 upgrade exercised on emulator: built the pre-Batch-2 commit (DB v7) in a worktree, installed it, wrote a story_progress row at v7, then installed the v8 build over it. Result: `PRAGMA user_version`=8, `story_cache` created by the migration's own SQL (orphan table dropped first so CREATE actually ran), and the v7 `story_progress` row survived intact — no destructive wipe, no "Migration didn't properly handle" crash.

## [E-02] Story content never cached → stories unusable offline
- Date: 2026-07-06 | Severity: major | Area: E-db / M-offline
- Evidence: `core/data/.../StoriesRepositoryImpl.kt:48-116` — only `story_progress` persisted; list/reader fail offline.
- Expected: cache story summaries/content in Room like signs/landmarks.
- Status: **FIXED** (commit 320cea4) — `story_cache` table (raw DTO JSON + `sort_order` to preserve server list order, since premium gating is positional); network-first with cache fallback in getStories/getStory.
- Verification: emulator — airplane mode + force-stop + cold relaunch: Stories list rendered from cache in exact server order (premium badges on same items), and previously-opened story reader worked fully offline. NOTE: Android premium lock is positional (`StoriesScreen.kt:72` FREE_STORY_LIMIT=3) while web has no gating at all → NEEDS-DECISION logged.

## [E-03] No @Index on queried columns (signs.category/type, landmarks.type/city, scan_results.firestore_id)
- Date: 2026-07-06 | Severity: major | Area: E-db / I-perf
- Evidence: all entities in `core/database` lack `indices`; DAOs filter on these columns → full-table scans.
- Status: **FIXED** (commit 8578885) — @Index on signs(category_name,type_name), landmarks(type,city), scan_results(firestore_id); Room v9 + MIGRATION_8_9 (index names follow Room's index_<table>_<column> convention).
- Verification: emulator — real v8→v9 upgrade: user_version 9, all 5 indices present in sqlite_master, story_progress data survived, no Room validation errors.

## [E-04] No seed data → fresh install offline = completely empty dictionary/explore/stories
- Date: 2026-07-06 | Severity: major | Area: E-db / M-offline
- Evidence: no `createFromAsset`/bundled DB; assets contain only (unused) ML models.
- Expected: prepopulated signs/landmarks DB or first-run seed import.
- Status: **FIXED** (commit 8b327fe, approved by supervisor) — bundled seed snapshots (assets/seed: 1023 signs 419KB, 164 landmarks 380KB, 26 categories) imported by `SeedImporter` on first run when tables are empty; live responses overwrite seed rows via the existing REPLACE upserts.
- Verification: emulator — pm clear + airplane mode + launch: logcat `E-04 seeded 1023 signs, 26 categories` + `164 landmarks`, zero network.

## [E-05] Minor DB/offline gaps (bundle)
- Date: 2026-07-06 | Severity: minor | Area: E-db / M-offline
- Items: (a) `getSigns` cache fallback only on IOException, not ApiException (`DictionaryRepositoryImpl.kt:64-65` vs `:109-111`) — **FIXED** (Batch 5 commit: fallback on any non-cancellation failure); (b) explore `getCategories` no offline fallback — landmark categories have no cache table; mitigated by E-04 seed (list itself works offline); (c) dashboard recentScans ignores local `scan_results` — open (enhancement-grade); (d) hand-rolled glyph JSON parse — open, harmless; (e) `isOffline` naming — cosmetic, open; (f) schema 4.json not exported — 5..9.json exported now, migration test rig documented (E-P5); (g) no migrations 1→4 — moot for any install ≥ v4; legacy hits downgrade-safe destructive path only.
- Status: PARTIALLY FIXED (a, e) | (e) **FIXED** Phase 3 commit 706dd46 (isOffline→isOnline); (c)(d) remain open as debt | Verification: full unit tests green.

## [J-01] Pexels API keys compiled into BuildConfig → extractable from release APK
- Date: 2026-07-06 | Severity: major | Area: J-security
- Evidence: `core/network/build.gradle.kts:28-29` → `NetworkModule.kt:151-152,161` (sent as Authorization header). Keys sourced from local.properties (untracked — no git leak).
- Expected: proxy Pexels via own backend, or accept + rotate.
- Status: **FIXED** (Phase 3: backend commit 41c96c2 on Wadjet-v3-beta feat/firebase-integration + Android commit f1d53d0) — GET /api/images/pexels-search (Bearer-auth, rate-limited, 24h server cache, key rotation on 429) with PEXELS_API_KEYS server-side; Android PexelsApiService now targets the proxy on the main Retrofit; PEXELS_API_KEY/PEXELS_API_KEY_2 BuildConfig fields and local.properties plumbing DELETED — no key ships in any APK. Prod needs the env var + deploy (FIREBASE_RUNBOOK §4).
- Verification: cross-repo grep (no backend pexels code).

## [J-02] Firestore security rules unverifiable from this repo (client writes users/{uid}/story_progress)
- Date: 2026-07-06 | Severity: major (external verification needed) | Area: J-security / N-firebase
- Evidence: `StoriesRepositoryImpl.kt:182,219,260`. Rules live server-side, not in repo.
- Expected: rules restrict `users/{uid}/**` to `request.auth.uid == uid`.
- LIVE UPDATE 2026-07-06: emulator logcat shows the client's own write is REJECTED — `Write failed at users/{uid}/story_progress/creation-from-nun: PERMISSION_DENIED` (signed-in user, own uid). So the Firestore progress mirror NEVER works; only the backend save (D-02) persists. Either fix the rules server-side or drop the dead Firestore write path (candidate for D-05 auth unification work).
- Status: **FIXED (client+rules) / PENDING-USER-CONSOLE (deploy)** — supervisor reversed to FIX: least-privilege firestore.rules + firebase.json + .firebaserc authored at repo root (Phase 3 commit a857fea): users/{uid}/** only for request.auth.uid == uid (covers story_progress + new fcm_tokens); deploy is `firebase deploy --only firestore:rules` (FIREBASE_RUNBOOK §3). Client mirror code was already correct. | Verification: PERMISSION_DENIED still observed pre-deploy (expected); post-deploy check documented in the runbook.

## [J-03] allowBackup=true backs up Room DB + plaintext prefs to cloud
- Date: 2026-07-06 | Severity: minor | Area: J-security
- Evidence: `app/src/main/AndroidManifest.xml:17`; backup rules exclude only `wadjet_secure_prefs`.
- Status: **FIXED** (Batch 5 commit) — backup_rules.xml + data_extraction_rules.xml (cloud-backup AND device-transfer) now also exclude `wadjet.db` and all shared prefs; backend remains source of truth for user data.
- Verification: build green (rules are validated at merge time).
- Note: security posture otherwise strong — EncryptedSharedPreferences for tokens, EncryptedFile for chat history, debug-gated logging, FLAG_IMMUTABLE PendingIntents, no WebView, no cleartext (NSC allows only localhost/10.0.2.2), R8 + sane rules, no secrets tracked in git. Keystore+passwords sit untracked in working tree (minor local-disk exposure).

## [I-01] SubcomposeAsyncImage used as universal list image component
- Date: 2026-07-06 | Severity: major | Area: I-perf
- Evidence: `core/designsystem/.../WadjetAsyncImage.kt:25` — used in Explore/Dictionary/Dashboard/Stories list cells; Coil discourages in lists (per-image subcomposition).
- Expected: `AsyncImage` with explicit size for list cells.
- Status: **FIXED** (commit c966a49) — rememberAsyncImagePainter + state overlay (no subcomposition); same public signature so all call sites unchanged.
- Verification: emulator — story cover images render in list cells post-change.

## [I-02] MediaPlayer.prepare() (blocking) on main thread at 6 sites
- Date: 2026-07-06 | Severity: major | Area: I-perf / H-audio
- Evidence: `DictionaryViewModel.kt:203`, `LessonViewModel.kt:70`, `SignDetailViewModel.kt:109`, `ScanViewModel.kt:102`, `ScanResultViewModel.kt:105`, `AudioPlaybackManager.kt:35`.
- Expected: `prepareAsync()` + listener, or `withContext(Dispatchers.IO)`.
- Status: **FIXED** (commit 0b8f99f) — five VM sites were already consolidated into AudioPlaybackManager by H-06; the manager now uses prepareAsync + onPrepared (speed applied there before start).
- Verification: emulator — TTS playback still works through the async path (dictionary + narration).

## [I-03] onCleared() fire-and-forget CoroutineScope for persisting chat/story state → data-loss risk
- Date: 2026-07-06 | Severity: major | Area: I-perf / C-state
- Evidence: `ChatViewModel.kt:472`, `StoryReaderViewModel.kt:339` — unstructured `CoroutineScope(SupervisorJob()+IO).launch` at teardown.
- Expected: persist via singleton/application scope or WorkManager.
- Status: **FIXED** (commit 22de1c0) — both sites use the existing Hilt `@ApplicationScope` singleton scope (DispatchersModule) instead of throwaway scopes.
- Verification: build green; chat history + story progress still persist across screen exits.

## [I-04] rememberBase64Bitmap decodes full bitmap in composition on main thread
- Date: 2026-07-06 | Severity: major | Area: I-perf
- Evidence: `feature/scan/.../util/ImageUtil.kt:11-21`.
- Expected: decode off-main with inSampleSize, or hand bytes to Coil.
- Status: **FIXED** (commit 8a9945c) — produceState + Dispatchers.Default decode; emits null until ready.
- Verification: build green.

## [I-05] Minor perf/debt (bundle)
- Date: 2026-07-06 | Severity: minor | Area: I-perf / O-debt
- Items: (a) `filtered.indexOf(story)` O(n²) — **FIXED** (itemsIndexed, Batch 5 commit); (c) missing LazyColumn keys in BrowseTab — **FIXED** (stable keys); (e) per-play MediaPlayer lingering — **FIXED by H-06** (single manager); rest open as debt: (b) inline `.filter{it.featured}` per recomposition; (d) stories list unpaged (12 stories — fine); (f) synchronous `isLoggedIn` pre-setContent (EncryptedSharedPreferences read — fast, acceptable); (g) unstructured scopes in callbackFlow (Firestore mirror is dead anyway, see J-02/E-P4); (h) hardcoded Dispatchers; (i) TODOs incl. placeholder notification icon; (j) empty :core:ml (F-02-deferred) / :core:ui modules; (k) versionCode unbumped (release-management call).
- Status: MOSTLY FIXED — Phase 3 closes more: (b) featured filter was already remembered; (g) Firestore fallback reads now flow-scoped (commit 7f83256); (i) placeholder notification icon replaced with ic_stat_wadjet (7f83256); (j) :core:ml is now a real module (fe712d0). Remaining debt: (d) unpaged stories (12 items — fine), (f) fast prefs read pre-setContent (acceptable), (h) hardcoded Dispatchers, (k) versionCode bump (release-management call). | Verification: code-read + builds green. Positives: clean Application.onCreate

## [A-01] Historical K2 compiler OOM crashes; gradle.properties memory likely under-provisioned
- Date: 2026-07-06 | Severity: major (build stability) | Area: A-build
- Evidence: 8× `hs_err_pid*.log` + `replay_pid25100.log` in repo root (UNTRACKED, verified `git ls-files`) — headers show K2JVMCompiler native OOM while compiling `feature:dashboard`. `gradle.properties`: `-Xmx2048m`, `kotlin.compiler.execution.strategy=in-process`, `workers.max=2` (stability-over-speed mitigations; 2 GB may still be tight).
- Expected: raise `org.gradle.jvmargs`, gitignore `hs_err_*`/`replay_*`, delete logs; confirm baseline build reproducible.
- Status: **FIXED** (hygiene commit) — jvmargs raised to -Xmx3072m (in-process strategy kept); hs_err_pid*/replay_pid* gitignored (files left on disk, untracked). Full assembleDebug + full unit-test suite green at 3g.
- Verification: full build + testDebugUnitTest green post-change.

## [A-02] BLOCKER (build): assembleDebug fails at HEAD — poisoned Gradle build cache entry for :core:network javac
- Date: 2026-07-06 | Severity: blocker (build) | Area: A-build
- Evidence: both incremental and `clean assembleDebug` fail at `:app:hiltJavaCompileDebug` with 14× `[Dagger/MissingBinding]` (every `core:network` binding). Diagnosis: `:core:network:compileDebugJavaWithJavac` output restored FROM-CACHE contained ONLY `BuildConfig.class` — none of the KSP-generated Hilt factories or `hilt_aggregated_deps/_com_wadjet_core_network_di_NetworkModule` (compare `core:data`, which has them). Re-running that task with `--no-build-cache --rerun-tasks` compiles all factories correctly → the code is fine; the local Gradle build cache (`~/.gradle/caches/build-cache-1`, migrated from the old laptop) holds a corrupt entry.
- Expected: `assembleDebug` green. Remediation (Phase 2): purge the poisoned build-cache entry (or temporarily build with `--no-build-cache`); consider `org.gradle.caching=false` until cache is regenerated.
- Status: **FIXED** (Phase 2, no commit needed — environment fix): purged `~/.gradle/caches/build-cache-1`.
- Verification: plain `gradlew assembleDebug` and `installDebug` both BUILD SUCCESSFUL afterwards; app installed and launched on Pixel_8 emulator.

## [H-01] BLOCKER: Arabic story narration unreachable — lang hardcoded "en", always narrates textEn
- Date: 2026-07-06 | Severity: blocker (Arabic UX) | Area: H-audio / G-i18n
- Evidence: `StoriesRepositoryImpl.kt:165-166` + `StoryReaderViewModel.kt:180` — `textAr` fetched/rendered but never narrated; local fallback receives English text so Arabic branch (`StoryReaderScreen.kt:134`) unreachable.
- Expected (web): `story_reader.html:603` passes real `lang`.
- Status: **FIXED** (commit 44d2189) — lang threaded through StoriesRepository; narrates textAr with lang=ar when app locale is Arabic.
- Verification: emulator (app in Arabic): POST /api/audio/speak body contained Arabic paragraph + "lang":"ar"; 200 audio/wav; MediaPlayer played.

## [H-02] Wrong TTS voice: Android context "hieroglyph_pronunciation" unknown to server → falls back to default voice
- Date: 2026-07-06 | Severity: major | Area: H-audio
- Evidence: `EgyptianPronunciation.kt:27` + `DictionaryRepositoryImpl.kt:230-243`; server `tts_service.py:29-91` VOICE_PRESETS has no such key → "Charon" default, empty director notes. Also `ScanResultViewModel.kt:78`.
- Expected (web): context `pronunciation` → voice `Rasalgethi` + "say clearly and slowly" note.
- Status: **FIXED** (commit 3a82fe1) — CONTEXT constant is now the server preset key "pronunciation".
- Verification: emulator + live backend — dictionary sign pronounce sent `{"text":"rah","lang":"en","context":"pronunciation"}` → 200, audio played.

## [H-03] Dead voice/style request fields — server ignores them (Pydantic drops extras)
- Date: 2026-07-06 | Severity: major (masks H-02) | Area: H-audio / D-network
- Evidence: `WriteModels.kt:56-62` SpeakRequest adds `voice`("Orus")/`style`; server `SpeakRequest` = text/lang/context only (`audio.py:120-163`). Intended voice never applied anywhere.
- Expected: drop dead fields; align context strings to server presets.
- Status: **FIXED** (commit b5bdc77) — SpeakRequest is text/lang/context only; voice/style removed through domain interfaces, repos and ViewModels; unused VOICE/STYLE constants deleted.
- Verification: emulator — live speak bodies contain exactly text/lang/context (narration + pronunciation captures).

## [H-04] Arabic lang never passed for dictionary/chat TTS; on-device ar fallback unchecked
- Date: 2026-07-06 | Severity: major | Area: H-audio / G-i18n
- Evidence: `DictionaryRepositoryImpl.kt:230-233` lang="en" hardcoded; `ChatViewModel.kt:318`/`ChatRepositoryImpl.kt:112` default "en" even for Arabic replies (web sends chatLang, `chat.html:264`); `ChatScreen.kt:163`/`StoryReaderScreen.kt:135` ignore `setLanguage` result → silent failure when device lacks ar voice; uses `ar` not `ar-EG`. LIVE: landmarks list also requested with hardcoded `lang=en` (LandmarkApiService default, repo never passed it).
- Note: dictionary phonetic lang="en" is CORRECT (web always uses lang 'en' for Egyptological pronunciation, app.js:500).
- Status: **FIXED** (commits 0602adf, 78d1bb2) — chat speak detects Arabic script → lang=ar; local TTS voice verified via `trySetLanguageFor` (ar-EG → ar, skip if unavailable); new `AppLanguage` helper (AppCompat per-app locales) threads lang into landmark list/detail and replaces raw Locale.getDefault() checks.
- Verification: emulator — in Arabic mode `GET /api/landmarks?...&lang=ar` observed (was lang=en); chat-lang detection + setLanguage checks code-reviewed and compiled (server-failure path exercised via H-05 test).

## [H-05] TTS fallback gaps: story narration stops silently on network error; Lesson TTS fails silently; SignDetail no local fallback
- Date: 2026-07-06 | Severity: major | Area: H-audio / M-offline
- Evidence: `StoryReaderViewModel.kt:198-221` (.onFailure → false → loop breaks, no fallback/toast); `LessonViewModel.kt:61-84` (no ttsEnabled check, no fallback, no error); `SignDetailViewModel.kt:118-124` (error string only).
- Expected (web): always degrades to browser SpeechSynthesis.
- Status: **FIXED** (commit 0f7f494) — `AudioPlaybackManager.speakLocal()` (device TTS, script-aware voice) is the fallback for server failure/204 in Dictionary, Lesson, SignDetail, Scan, ScanResult and story narration (narration falls back per-paragraph and continues). Lesson now also respects the TTS-enabled setting. Removed dead `localTtsText` plumbing that no screen consumed.
- Verification: emulator — airplane mode + uncached sign pronounce: server call impossible, audio still played via device TTS (active audio output confirmed).

## [H-06] No audio focus + fragmented MediaPlayers → concurrent playback possible
- Date: 2026-07-06 | Severity: major | Area: H-audio
- Evidence: no `requestAudioFocus`/`setAudioAttributes` anywhere; `AudioPlaybackManager` singleton only injected in Chat/StoryReader; Dictionary/SignDetail/Lesson/ScanResult each own private MediaPlayers.
- Expected: single playback manager + audio focus handling.
- Status: **FIXED** (commit 58fbdfc) — AudioPlaybackManager is the only MediaPlayer owner app-wide (5 private MediaPlayers removed); requests AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK per playback, stops on focus loss, USAGE_MEDIA/CONTENT_TYPE_SPEECH attributes, temp WAVs in cacheDir.
- Verification: emulator — story narration and dictionary pronunciation both play through the manager; starting one playback stops the other by construction (single player).

## [H-07] No audio caching client-side (no OkHttp Cache; temp WAVs deleted after playback)
- Date: 2026-07-06 | Severity: major (cost/latency) | Area: H-audio / D-network
- Evidence: `NetworkModule.kt:47-73` no Cache; server sends `Cache-Control: max-age=86400` (ignored). Web keeps in-memory blob cache (`app.js:465-494`).
- Expected: OkHttp Cache and/or keyed audio file cache.
- Status: **FIXED** (commit 2de725f) — `TtsAudioCache` (SHA-256 of lang|context|text → WAV in cacheDir/tts_cache, 30MB LRU) wired into all four speak paths (chat, dictionary, scan, stories). OkHttp cache alone could not work: speak is a POST.
- Verification: emulator — replaying a narrated paragraph and a sign pronunciation produced audio with ZERO network requests (logcat), on two separate paths.

## [H-08] Minor audio issues (bundle)
- Date: 2026-07-06 | Severity: minor | Area: H-audio
- Items: (a) temp WAV files not deleted on exception paths (`SignDetailViewModel.kt:114`, also Dictionary/Lesson; bare `File.createTempFile` in system tmp); (b) SignDetail has no playing/loading state, can't stop playback; (c) main-thread `prepare()` jank (see I-02). Positive: `ScanResultViewModel` per-key TtsState is the model to copy; format parity OK (24 kHz WAV played untranscoded).
- Status: **MOSTLY FIXED via H-06/I-02** — (a) all playback now goes through AudioPlaybackManager (cacheDir temps, deleted on completion/error, deleteOnExit backstop; per-VM temp code deleted); (c) fixed by I-02 prepareAsync. Remaining: (b) SignDetail stop/playing indicator — enhancement-grade, open.
- Verification: code + live playback through the manager on 2 paths.

## [G-01] BLOCKER: No in-app language switcher — no per-app locale mechanism exists at all
- Date: 2026-07-06 | Severity: blocker | Area: G-i18n / B-parity
- Evidence: zero hits for `setApplicationLocales`/`LocaleManager`/`LocaleListCompat`; no `locales_config.xml`/`android:localeConfig`; no locale key in `UserPreferencesDataStore.kt`; `MainActivity` extends `ComponentActivity` (`MainActivity.kt:68`) so the pre-13 AppCompat API isn't even available; Settings has no language row (`SettingsScreen.kt:140-247`). Orphaned strings `quick_settings_language/english/arabic` defined but never referenced. `User.preferredLang` fetched from backend but never applied (`SettingsViewModel.kt:61` passes null).
- Expected (web): prominent EN↔AR toggle (`partials/nav.html:42-45,84`).
- Status: **FIXED** (commit a4aaa6d) — AppCompatActivity + Theme.AppCompat, `setApplicationLocales`, `locales_config.xml`, autoStoreLocales service, Language section in Settings + Quick Settings.
- Verification: emulator — EN→AR switch flips whole app to RTL + Cairo font; persists across cold start (autoStoreLocales); AR→EN returns; session survives (after C-01).

## [G-02] MAJOR: values-ar files in 10 of 12 modules are ENGLISH placeholders, not Arabic
- Date: 2026-07-06 | Severity: major (blocker for Arabic DoD) | Area: G-i18n
- Evidence: only `app` (24/24) and `feature:chat` (34/35) contain real Arabic script. auth ~19% translated; designsystem/dashboard/dictionary/explore/feedback/landing/scan/settings/stories = 0 Arabic values, English copies wrapped in `tools:ignore="MissingTranslation"`. Overall Arabic completeness ~15-20%.
- Note: refines supervisor ground truth — files exist, translations mostly don't.
- Status: **FIXED** (commit 68806bf) — all 10 modules rewritten with real Arabic, terminology aligned with the web's authoritative `app/i18n/ar.json` (فحص/القاموس/المعالم/حكايات/تحوت/علامات جاردنر…); auth completed; `tools:ignore="MissingTranslation"` wrappers removed; script check confirms 1:1 key parity and no untranslated values (intentional latin: language names, EN/AR codes, format-only strings).
- Verification: emulator in Arabic — Home, bottom nav, Stories, Dictionary (tabs/search/labels), Explore, Quick Settings all render Arabic; RTL layout correct; mojibake eliminated by the rewrite.

## [G-03] 5 ar keys missing entirely in feature:dictionary + mojibake in placeholder ar files
- Date: 2026-07-06 | Severity: major | Area: G-i18n
- Evidence: `feature/dictionary/values-ar/strings.xml` lacks `lesson_speak`, `write_mode_gardiner`, `write_mode_phonetic`, `write_mode_smart`, `write_palette_label`. Encoding corruption (â™¡/âœ“/Â·/â€¦/�) in dashboard:11,19,21, settings:26, auth `forgot_back_to_login`, stories `reader_prev_chapter`.
- Correction during fix: dashboard/settings files were actually clean UTF-8; real mojibake confirmed in auth `forgot_back_to_login` and stories `reader_prev/next_chapter` (and dictionary `browse_search_placeholder`).
- Status: **FIXED** (commit afc387c adds the 5 missing keys; commit 68806bf rewrite removed all mojibake) | Verification: parity script — 0 missing keys, 0 mojibake.

## [G-04] ~45 hardcoded user-facing strings in ViewModels (errors/snackbars/toasts stay English)
- Date: 2026-07-06 | Severity: major | Area: G-i18n
- Evidence: AuthViewModel (~11), SettingsViewModel:63,87,100,135, FeedbackViewModel:57,61, DashboardViewModel:94-115, ChatViewModel:162-414, StoryReaderViewModel:151-270, Dictionary/Lesson/SignDetail "Generating pronunciation…", ScanViewModel:135-199, IdentifyViewModel:51-105.
- Expected: string resources (ViewModels emit resource IDs or use a resource provider).
- Status: **FIXED** (commits f3df668 + 9a2fdaf) — new `StringResolver` (core:common) resolves resources against AppCompat per-app locales (application context alone lags on API < 33); ~50 literals across 14 ViewModels moved to string resources with EN+AR values; `AuthViewModel.validatePassword` returns @StringRes ids.
- Remaining (split to G-07): displayed-value constants `DIFFICULTY_FILTERS`, `SIGN_TYPES`, `FEEDBACK_CATEGORIES`, `FALLBACK_CATEGORIES` double as API filter values and need a value↔label mapping.
- Verification: full build green; auth unit tests green.

## [G-05] Minor i18n/RTL issues (bundle)
- Date: 2026-07-06 | Severity: minor | Area: G-i18n
- Items: (a) directional arrows baked into strings (`reader_next_chapter` "Next →", `reader_prev_chapter`, `detail_read_wikipedia`) won't mirror in RTL; (b) `String.format("%.1f")` without Locale (`SettingsScreen.kt:417`).
- Positives: supportsRtl=true; start/end used throughout, zero left/right leaks; all directional icons `Icons.AutoMirrored.*` (24 sites); Cairo font correctly wired for Arabic via `wadjetTypographyForLang` (no tofu); intentional RTL force for Arabic block in `ScanResultScreen.kt:359-373`.
- Status: **FIXED** (commit 706dd46 for (a); (b) was already fixed with Locale in Batch 5) — directional arrows removed from reader_prev/next_chapter and detail_read_wikipedia in EN+AR. | Verification: grep — zero directional arrows left in strings.

## [B-01] No deep links anywhere (email verify/reset links + FCM notifications can't open the app/content)
- Date: 2026-07-06 | Severity: major | Area: B-parity / C-nav
- Evidence: no `navDeepLink` in `WadjetNavGraph.kt`; launcher activity has only MAIN/LAUNCHER (`AndroidManifest.xml:27-37`); backend email links (`auth.py:354,393`) open web only; FCM extras never consumed by MainActivity.
- Status: **FIXED (partial by design)** (commit 529f168) — `wadjet://story/{id}` and `wadjet://landmark/{slug}` deep links (manifest VIEW filter + typed navDeepLink); FCM notifications with story_id/landmark_slug now emit those VIEW intents; running activity forwards onNewIntent to NavController. Email verify/reset links necessarily still open the web: claiming the backend's https domain needs assetlinks.json hosted server-side (read-only backend) — follow-up noted.
- Verification: emulator — `am start -a VIEW -d wadjet://story/creation-from-nun` opened the reader directly (cold path) and `wadjet://landmark/great-pyramids-of-giza` opened the detail screen on the RUNNING activity (onNewIntent path).
- Expected (web): fully URL-addressable routes.
- Status: OPEN | Verification: code-read.

## [B-02] Password reset / email verification complete on web only
- Date: 2026-07-06 | Severity: minor (given B-01) | Area: B-parity
- Evidence: `ForgotPasswordSheet.kt` sends email; reset token flow not handled in-app. Firebase-based verify flow present (`VerifyEmailSheet.kt`).
- Status: OPEN (depends on B-01) | Verification: code-read.

## [C-01] Duplicate/racing sign-out navigation
- Date: 2026-07-06 | Severity: major | Area: C-nav
- Evidence: both `WadjetNavGraph.kt:471-477` (SettingsScreen signedOut effect) and `MainActivity.kt:130-137` (isAuthenticated observer) navigate to Welcome with popUpTo inclusive, no launchSingleTop.
- Expected: single source of truth (global auth observer).
- Status: **FIXED** (commit ba9da31) — observer now navigates only on authenticated→unauthenticated TRANSITION (fixes recreation race that dumped logged-in users to Welcome on every language switch); duplicate Settings-route redirect removed.
- Verification: emulator — language switch (both directions) keeps user on Landing; cold restart stays logged in. Root-cause evidence: tokens intact + authorized API call after recreation while UI showed Welcome.

## [C-02] UI state lost on rotation: showArabic, selectedGlyph, aiNotesExpanded, selectedAnnotation use remember (not rememberSaveable)
- Date: 2026-07-06 | Severity: minor | Area: C-nav / C-state
- Evidence: `ScanResultScreen.kt:94,95,394`; `StoryReaderScreen.kt:514`; `LearnTab.kt:79`; activity not orientation-locked.
- Status: **FIXED (partial)** (Batch 5 commit) — showArabic + aiNotesExpanded now rememberSaveable. Remaining: selectedGlyph/selectedAnnotation hold non-Saveable domain objects (need custom Savers — cosmetic sheet state, low value); LearnTab:79 is a reveal-animation counter (re-animating on rotation is fine).
- Verification: compile green.

## [C-03] Search/filter/pagination state not in SavedStateHandle (lost on process death)
- Date: 2026-07-06 | Severity: minor | Area: C-state
- Evidence: DictionaryViewModel / ExploreViewModel / StoriesViewModel hold query/filters/paging in plain MutableStateFlow.
- Status: **FIXED** (Phase 3 commit c187ede) — Dictionary (category/type/query), Explore (category/city/query), Stories (difficulty) persist via SavedStateHandle and restore on process death. | Verification: unit tests green (VM constructors take SavedStateHandle).

## [C-04] Minor nav issues (bundle)
- Date: 2026-07-06 | Severity: minor | Area: C-nav / O-debt
- Items: (a) dead `Route.Splash` registered with empty body (`WadjetNavGraph.kt:102-104`, `Route.kt:6`) — **FIXED** (Batch 5 commit, route removed); (b) landmark-chat slug failure silently degrades to generic chat (`ChatViewModel.kt:70-80`) — WONT-FIX (graceful degradation is acceptable); (c) unused Translate data-layer wiring (`TranslateApiService.kt` + repo, DI-only) — left in place (has tests; harmless; guarded by never-delete rule).
- Positives: all 20 routes registered/reachable; saveState/restoreState multi-back-stack; anti-double-tap `lifecycleIsResumed()`; detail VMs use SavedStateHandle args (process-death safe).
- Status: **CLOSED** — (a) FIXED Batch 5, (b) WONT-FIX, (c) left by design; nothing remains actionable.

## [D-01] BLOCKER: STT endpoint path wrong — voice input always 404s
- Date: 2026-07-06 | Severity: blocker | Area: D-network
- Evidence: `AudioApiService.kt:20-24` `@POST("api/audio/stt")`; backend real path is `/api/stt` (`audio.py:16` prefix `/api` + `:57` `@router.post("/stt")`). Sibling `speak` path IS correct.
- Expected: Android calls `api/stt` (or backend moves route).
- Status: **FIXED** (commit 619803f) | Verification: build green; live backend check POST /api/audio/stt=404 vs POST /api/stt=403 (route exists, auth-gated). Runtime mic capture not exercisable on emulator — path fix verified at contract level.

## [D-02] Save story progress 422s on every call — glyphs_learned type mismatch (list vs JSON string)
- Date: 2026-07-06 | Severity: major | Area: D-network
- Evidence: `UserModels.kt:99-105` sends `List<String>`; backend `schemas.py:107` expects `str` (JSON-encoded). Pydantic v2 won't coerce → 422. Read side is consistent (string).
- Status: **FIXED** (commit a360268) — DTO field is `String`; call site JSON-encodes the list.
- Verification: emulator + live backend — chapter navigation logged request body `{"story_id":"creation-from-nun","chapter_index":0,"glyphs_learned":"[]",...}` → **200** with persisted row `{"id":5,...}` (previously 422).

## [D-03] Scan glyph count always 0 — DTO expects `glyph_count`, backend emits `num_detections`
- Date: 2026-07-06 | Severity: major | Area: D-network
- Evidence: `ScanModels.kt:8` vs `hieroglyph_pipeline.py:86`. (Backend `scan.py:923` has the same self-bug persisting history glyph_count=0.)
- Status: **FIXED** (commit 349af0b) — `@SerialName("num_detections")`.
- Verification: emulator + live backend — scan of test image returned `"num_detections":6` and Results screen showed "Detected (6)".

## [D-04] Logout never revokes server refresh token
- Date: 2026-07-06 | Severity: major | Area: D-network / J-security
- Evidence: `AuthInterceptor.kt:50-59` attaches `wadjet_refresh` cookie only on `/auth/refresh`; no CookieJar; backend `auth.py:216` reads the cookie to delete the token → deletes nothing. Token stays valid up to 7 days after sign-out.
- Expected: send refresh cookie on `/auth/logout` too.
- Status: **FIXED** (commit 9229f58) — cookie attached when path ends with `/auth/refresh` OR `/auth/logout`.
- Verification: emulator + live backend — Sign Out logged `POST /api/auth/logout` WITH `Cookie: wadjet_refresh=...` → 200 and server responded `set-cookie: wadjet_refresh=""; Max-Age=0` (token revoked server-side).

## [D-05] Dual Firebase+backend auth: account must exist in BOTH stores; verify/reset handled only via Firebase
- Date: 2026-07-06 | Severity: major (architectural) | Area: D-network / B-parity
- Evidence: `AuthRepositoryImpl.kt:51-137` — Firebase first, then backend; `currentUser` requires both. Backend verify/reset endpoints (`auth.py:320-418`) unused → states can diverge. Web-only accounts can't log in on Android. LIVE: backend login returns email_verified:false for a Firebase-verified account; Firestore progress mirror writes are PERMISSION_DENIED (J-02) — i.e. the Firebase half provides no working functionality beyond auth itself.
- Status: **FIXED** (Phase 3, supervisor REVERSED E-P6 to Firebase-primary: Android commit 17466e2 + backend commits f27c55a/850f9c5 [+verifier fix]) — credentials now go ONLY to Firebase (email/password + Google); the backend session is derived by exchanging the Firebase ID token at new POST /api/auth/firebase (verified email or google.com provider required); unverified accounts get NO backend session until the verify gate passes; register/forgot-password are Firebase-only; TokenAuthenticator self-heals a Rejected refresh via Firebase re-exchange before invalidating. Web-created (backend-only) accounts enter via Google Sign-In/password reset or the optional bcrypt bulk import (FIREBASE_RUNBOOK §8).
- Verification: LIVE on emulator vs locally-run backend — fresh sign-in: Firebase → POST /api/auth/firebase 201 → Landing; deterministic self-heal test (backend restarted with rotated JWT_SECRET): GET /api/user/limits 401 → POST /api/auth/refresh 401 → POST /api/auth/firebase 200 → retried GET 200, user stayed signed in; cold restart persists the session. Backend suite 233 green incl. 11 new exchange tests.

## [D-06] Landmark pagination metadata missing — totalPages always 1
- Date: 2026-07-06 | Severity: major | Area: D-network
- Evidence: `LandmarkModels.kt:7-13` expects `per_page`/`total_pages`; backend `explore.py:954-960` returns `total/page/has_more` instead. Any paging keyed off totalPages stops at page 1.
- Status: **FIXED** (commit eccbc53) — totalPages computed as ceil(total/per_page) when backend omits total_pages.
- Verification: emulator + live backend — landmarks response `total:164, per_page:24` → scroll past page 1 fired `GET /api/landmarks?page=2&per_page=24` → 200 (previously stuck at page 1).

## [D-07] Minor network issues (bundle)
- Date: 2026-07-06 | Severity: minor | Area: D-network
- Items: (a) landmark list `lang`/`featured` query params ignored by backend (`explore.py:901-913`) — featured filter never applied server-side; (b) saveProgress response shape differs from OkResponse DTO (harmless with defaults); (c) debug builds default to production base URL (overridable via `debug.base.url`); (d) backend endpoints never called: `/api/detect`, `/api/read`, non-streaming `/api/chat`, backend verify/reset, single-chapter stories, `/api/health`.
- Positives: robust Json config (ignoreUnknownKeys+coerceInputValues); sound error handling (`bodyOrThrow`, `suspendRunCatching` rethrows CancellationException); sane timeouts (SSE-tolerant read 60s); 401→TokenAuthenticator single-retry refresh; tokens in EncryptedSharedPreferences.
- Status: OPEN | Verification: cross-repo code-read.

## [D-08] Transient network failure during token refresh permanently logs the user out
- Date: 2026-07-06 (found live during Phase 2 emulator testing) | Severity: blocker | Area: D-network / B-parity
- Evidence: reproduced on emulator — 401→refresh raced an airplane-mode toggle; `TokenAuthenticator`'s catch-all treated the resulting IOException as a dead session and called `tokenManager.invalidateSession()` (Firebase auth store observed emptied to a 65-byte file). Random permanent logouts in normal flaky-network use.
- Expected: only an explicit 401/403 from `/auth/refresh` means the session is dead; network errors must keep tokens and retry later.
- Status: **FIXED** (commit 42ba655) — `sealed interface RefreshOutcome { Success / Rejected / NetworkError }`; only `Rejected` invalidates the session.
- Verification: code + regression on emulator (airplane-mode cold start, offline browsing, force-stop/relaunch — session persisted throughout). The original race is timing-dependent and was not deterministically re-reproduced post-fix; no spurious logout observed in ~30 min of mixed online/offline testing.

## [L-01] Status/nav bar icons invisible in system light mode — enableEdgeToEdge() with auto style on forced-dark app
- Date: 2026-07-06 | Severity: major | Area: L-ui
- Evidence: `MainActivity.kt:80` no-arg `enableEdgeToEdge()` → SystemBarStyle.auto follows SYSTEM theme; app forces dark UI → dark icons on near-black background in light mode.
- Expected: explicit `SystemBarStyle.dark(...)` for both bars.
- Status: **FIXED** (commit a2026bf) — explicit SystemBarStyle.dark for status+nav bars.
- Verification: build green; bars render light icons on dark background on emulator (dark system theme; style now locked regardless of system theme).

## [L-02] Double top bars on Explore/Stories/Chat tabs (incl. back arrow on root destinations)
- Date: 2026-07-06 | Severity: major | Area: L-ui / C-nav
- Evidence: global TopAppBar for top-level routes (`MainActivity.kt:222-256`) + own TopAppBars in `ExploreScreen.kt:112`, `StoriesScreen.kt:88`, `ChatScreen.kt:265`; Landing/Hub correctly rely on global bar (inconsistent).
- Expected: one app bar per screen; no back button on bottom-nav roots.
- Status: **FIXED** (commit f8f22d7) — global TopAppBar suppressed on Explore/Stories/Chat (screens keep their own action-bearing bars); back arrow removed from Explore/Stories roots and made conditional on ChatScreen (shown only for pushed landmark chat).
- Verification: emulator screenshot — Stories tab shows a single "Stories" bar with no back arrow.

## [L-03] Hardcoded "Chapter X of Y" in LandingScreen
- Date: 2026-07-06 | Severity: major (i18n) | Area: L-ui / G-i18n
- Evidence: `LandingScreen.kt:462`.
- Status: **FIXED** (Batch 5 commit) — uses the existing `landing_chapter_progress` resource (EN+AR).
- Verification: compile green; AR resource already present from G-02.

## [K-01] Touch targets below 48dp (chat edit pencil worst at ~14dp)
- Date: 2026-07-06 | Severity: major | Area: K-a11y
- Evidence: `ChatScreen.kt:665-673` raw 14dp Icon with .clickable; `ChatScreen.kt:694-697` retry Text; `StoriesScreen.kt:369-374` favorite IconButton forced 36dp; `DashboardScreen.kt:428-430` Remove text; `SettingsScreen.kt:200-239` clickable rows without Role.Button/min height.
- Expected: IconButton / sizeIn(min 48dp) + Role semantics.
- Status: **FIXED** (commit 705ad91) — chat edit pencil → IconButton (48dp), chat retry → 48dp Box+Role.Button, stories favorite 36→48dp, dashboard Remove → 48dp Box, settings clear-cache/feedback → minHeight 48dp + Role.Button.
- Verification: build green; controls remain visually identical (icons keep their small glyph size inside larger targets).

## [K-02] Contrast: Dust #8B7355 (~3.8:1 on Night) used for small text
- Date: 2026-07-06 | Severity: minor | Area: K-a11y
- Evidence: `LandingScreen.kt:273`, ContinueStoryCard, badges; alpha-0.15 same-hue badges `ExploreScreen.kt:509-520`.
- Expected: reserve Dust for large/decorative text; TextMuted (~5.7:1) is fine.
- Status: **FIXED** (Batch 5 commit) — the two small-text Dust usages (landing footer credit, explore era badge) moved to TextMuted; remaining Dust usages are large/decorative.
- Verification: compile green.

## [L-04] Minor UI issues (bundle)
- Date: 2026-07-06 | Severity: minor | Area: L-ui
- Items: (a) themes.xml parent — superseded (now Theme.AppCompat per G-01); (b) fixed-height FeatureCard — **FIXED** (heightIn(min=120), Batch 5 commit); (c) WadjetButton hardcoded 48dp — left (48dp IS the minimum target); (d) errorContainer hex — **FIXED** (Phase 3 commit 706dd46: moved to WadjetColors.ErrorContainer/OnErrorContainer).
- Positives: palette matches web CSS exactly (gold #D4AF37 family, Night #0A0A0A); no dynamic color override; zero hardcoded hex in feature modules; loading/empty/error states consistently implemented (shimmer, ErrorState w/ retry, OfflineIndicator); no LazyColumn-in-Column crash patterns; sp/dp used correctly; headings have semantics.
- Status: **CLOSED** ((c) intentionally left; all else fixed/superseded).


## [G-07] Displayed-value constants remain English in Arabic UI (difficulty/type/category chips)
- Date: 2026-07-06 (found during Batch 3 Arabic tour) | Severity: minor | Area: G-i18n
- Evidence: Stories difficulty chips + card badges ("All/Beginner/Intermediate/Advanced", `StoriesViewModel.DIFFICULTY_FILTERS`), dictionary sign-type filter row (`SIGN_TYPES`), feedback categories (`FEEDBACK_CATEGORIES`), explore fallback categories — these constants are BOTH UI labels and API filter/request values, so simple resource substitution would break filtering; needs a value↔label map. Also server-provided content that only exists in EN regardless of lang param (sign descriptions/readings, some landmark names) is a backend data gap, not an Android bug.
- Expected (web): web shows Arabic chip labels (filter_beginner etc. in ar.json) while filtering by stable values.
- Status: **FIXED (client-side half)** (commit 4725a5e) — value↔label mappers for difficulty, sign-type, feedback-category and landmark-category chips; values stay stable for API filtering. Server-side EN-only content (sign descriptions/readings, some landmark data) remains a backend data gap (out of scope).
- Verification: compile green; EN labels unchanged; AR labels from web ar.json terminology.

## [A-03] Native libs not 16 KB page-aligned (system compat warning on API 36 emulator)
- Date: 2026-07-06 | Severity: minor (will become major for Play targets) | Area: A-build
- Evidence: system dialog on install: libonnxruntime.so, libonnxruntime4j_jni.so, libdatastore_shared_counter.so, libandroidx.graphics.path.so, libsurface_util_jni.so, libimage_processing_util_jni.so not 16 KB aligned. Most come from the UNUSED onnxruntime dep (F-02, deferred) and older androidx artifacts.
- Expected: 16 KB-aligned .so (AGP packaging flag / newer artifact versions); removing unused ONNX (F-02) eliminates the two worst.
- Status: **FIXED** (Phase 3 commit fe712d0, with F-02) — onnxruntime-android 1.20.0→1.27.0. | Verification: EMPIRICAL ELF proof — all 24 native libs in the built APK have min PT_LOAD p_align=0x4000 across all 4 ABIs (incl. libonnxruntime.so + libonnxruntime4j_jni.so, the two prior offenders); parser script in session scratchpad (elf_align.py).

## [B-03] Verify-email gate shows even for already-verified accounts on fresh sign-in
- Date: 2026-07-06 | Severity: minor | Area: B-parity / C-nav
- Evidence: sign-in with a VERIFIED account still lands on VerifyEmailSheet ("I've verified my email" then passes). Firebase user's emailVerified is stale until reload; gate checks before reloading. Also backend login response returns email_verified:false for the same account (Firebase-verified only) — live D-05 dual-store drift evidence.
- Expected: reload Firebase user before gating; treat verified accounts as verified immediately.
- Status: **FIXED** (Batch 5 commit) — sign-in re-checks `reloadEmailVerified()` before showing the sheet; unit test updated.
- Verification: auth unit tests green (verified-account path no longer reachable by stale flag alone). Note: emulator re-login after this fix still passes the gate in one step.

## [G-06] Story reader renders textEn even when app is in Arabic
- Date: 2026-07-06 | Severity: major | Area: G-i18n / B-parity
- Evidence: reader in Arabic mode shows English paragraphs right-aligned (".before all things" punctuation artifact). textAr exists (H-01 narration proved backend sends real Arabic).
- Expected (web): reader displays lang-appropriate text.
- Status: **FIXED** (commit 877e999) — `localized(en, ar)` helpers pick the Arabic variant (blank-safe fallback) for story titles (list, landing continue-card, reader top bar), chapter titles, paragraphs WITH matching wordAr annotations, tooltips, all four interaction types and feedback banners.
- Verification: emulator in Arabic — "الخلق من نون" reader shows Arabic chapter title (المياه الأزلية), Arabic paragraphs, Arabic annotation prompt; narration reads the same Arabic text (lang=ar → 200 → played).
