# Stage 18: Test Gap Analysis and Priority Map

## ALL Issues Found (Unified — from Stages 1-17)

### CRITICAL (P0) — Crash/Data-Loss/Blocking

| # | Source | Category | Issue | Affects | Effort |
|---|--------|----------|-------|---------|--------|
| 1 | S10 | Auth | Process death → shows Welcome despite persisted tokens (auto-login broken) | All users after Android kills app | 4h |
| 2 | S16 | Accessibility | Welcome screen broken at 720×1280 — sign-in buttons cut off, no scroll | Small phone users can't log in | 2h |
| 3 | S3 | ViewModel | ScanViewModel `onImageCaptured()` has NO `isLoading` guard — double-submit | Duplicate scans, wasted API calls | 1h |
| 4 | S12 | API | Landmark parent/children DTOs always null (key name + type mismatch) | Landmark relationships broken | 2h |
| 5 | S15 | Performance | Chat streaming `.map{}` copies entire message list per chunk (5000 copies for 50msg+100chunks) | ANR-level jank during chat | 3h |
| 6 | S15 | Performance | ScanViewModel bitmap decode on Main thread — ANR risk | Scan screen freezes | 1h |
| 7 | S15 | Performance | Cold start 6.8s (target <1s) | Poor first impression | 8h |
| 8 | S3 | ViewModel | GlobalScope usage in Chat and StoryReader onCleared() | Memory leak, crash after VM cleared | 2h |

### HIGH (P1) — Core Feature Correctness

| # | Source | Category | Issue | Affects | Effort |
|---|--------|----------|-------|---------|--------|
| 9 | S13 | Audio | TTS settings (enabled/speed) in DataStore are decorative — never checked | Settings screen lies to users | 3h |
| 10 | S13 | Audio | MediaPlayer code duplicated 7× across VMs — no shared manager | Maintenance burden, audio focus | 6h |
| 11 | S13 | Audio | No audio focus handling — plays over phone calls | UX violation | 2h |
| 12 | S10 | Auth | FCM token never sent to backend — push targeting impossible | No push notifications | 2h |
| 13 | S10 | Auth | Logout doesn't clear local Room/DataStore data — ghost state | Stale data for next user | 2h |
| 14 | S10 | Auth | Refresh failure doesn't sign out Firebase — ghost auth state | Inconsistent auth | 1h |
| 15 | S12 | API | Write palette missing numbers + determinatives groups | Can't write number glyphs | 1h |
| 16 | S9 | Repository | Three competing error handling patterns (JSON parse vs regex vs raw) | Inconsistent error messages | 4h |
| 17 | S9 | Repository | ZERO retry logic in any repository | Single failure = permanent error | 4h |
| 18 | S3 | ViewModel | 13 of 20 VMs have no `isLoading` guard — double-submit possible | Duplicate API calls | 4h |
| 19 | S4 | Network | Chat SSE runs on Main thread pool — ANR risk | Chat freezes | 2h |
| 20 | S15 | Performance | DotPattern: ~4000 drawCircle per frame | Continuous jank on landing | 3h |
| 21 | S16 | Accessibility | Zero testTags in entire codebase — UI testing impossible | Can't write Compose tests | 8h |
| 22 | S16 | Accessibility | 7 interactive icons with null contentDescription | TalkBack users can't use speak buttons | 2h |
| 23 | S16 | Accessibility | Touch targets 24dp on speak buttons (severe violation) | Can't tap on accessibility devices | 1h |
| 24 | S17 | RTL | Chat values-ar/strings.xml contains English, not Arabic | Arabic users see English in chat | 1h |

### MEDIUM (P2) — UX/Polish/Testing Infrastructure

| # | Source | Category | Issue | Affects | Effort |
|---|--------|----------|-------|---------|--------|
| 25 | S2 | UX | Identify button is tiny 63×63 icon with no label in Explore header | Users can't find landmark identify | 2h |
| 26 | S2 | UX | Scan and Identify screens look identical (same Eye of Horus icon) | Users confused which is which | 3h |
| 27 | S6 | UX | ImageUploadZone has no parameter to customize icon | Root cause of #26 | 1h |
| 28 | S2 | UX | Landing CTAs below fold, cards not clickable | Buttons hidden on scroll | 2h |
| 29 | S7 | Design | headlineSmall (22sp) larger than headlineMedium (20sp) | Typography hierarchy broken | 0.5h |
| 30 | S11 | Testing | No `core/testing` module — missing TestInstallIn, MainDispatcherRule, fakes | Can't write proper tests | 4h |
| 31 | S11 | Testing | No custom HiltTestRunner | Hilt instrumented tests will crash | 1h |
| 32 | S6 | Compose | Zero @Preview functions anywhere — can't verify in Android Studio | Slow dev iteration | 8h |
| 33 | S5 | Database | 6 features have ZERO offline support (Chat, Auth, Feedback, Translate, User, Lessons) | Network-required for most features | 16h |
| 34 | S14 | Security | No certificate pinning on OkHttpClient | MITM possible | 2h |
| 35 | S14 | Security | Backup rules don't exclude encrypted_prefs | Token leak via ADB backup | 1h |
| 36 | S15 | Performance | ShineSweep on every list item (10+ concurrent infinite animations) | Jank on scrolling | 3h |
| 37 | S16 | Accessibility | Zero heading() semantics — TalkBack can't navigate by headings | Poor screen reader nav | 3h |
| 38 | S16 | Accessibility | Zero mergeDescendants on cards — TalkBack reads each child | Noisy screen reader | 3h |
| 39 | S16 | Accessibility | 15 clickable elements without Role or description | Wrong interaction type announced | 3h |
| 40 | S16 | Accessibility | Zero reduced-motion support for 9+ infinite animations | Accessibility users see constant motion | 4h |
| 41 | S17 | RTL | Chat Arabic suggestion chips have UTF-8 mojibake | Garbled Arabic | 1h |
| 42 | S17 | Dark | All UI uses WadjetColors.* directly (~235 uses) — blocks light theme | Future light theme blocked | 6h |

### LOW (P3) — Polish/Nice-to-Have

| # | Source | Category | Issue | Affects | Effort |
|---|--------|----------|-------|---------|--------|
| 43 | S2 | UX | "Built by Mr Robot" on Welcome screen | Unprofessional | 0.5h |
| 44 | S2 | UX | Landing says "Translate" feature but Dictionary only has 3 tabs | Misleading text | 0.5h |
| 45 | S5 | Database | FTS4 instead of FTS5 for search | Minor perf difference | 2h |
| 46 | S8 | Navigation | No deep links configured | Can't link into app | 4h |
| 47 | S9 | Repository | WadjetResult sealed class exists but never used by any repo | Dead code | 2h |
| 48 | S10 | Firebase | FirebaseFirestore provided in DI but never used | Dead DI binding | 0.5h |
| 49 | S14 | Security | ProGuard -renamesourcefileattribute commented out | Real filenames in release stack traces | 0.5h |
| 50 | S15 | Performance | Coil disk cache 5% of disk (3-6GB) — should be fixed 250MB | Excessive storage | 0.5h |

---

## Test Coverage Gap Map

### Modules With Tests (3 of 20 VMs tested)

| Module | Class | Current Tests | Priority |
|--------|-------|--------------|----------|
| core:common | EgyptianPronunciation | 173 tests ✅ | — |
| core:data | AuthRepositoryImpl | 8 tests ✅ | — |
| core:data | ChatRepositoryImpl | 7 tests ✅ | — |
| core:data | DictionaryRepositoryImpl | 6 tests ✅ | — |
| core:data | ExploreRepositoryImpl | 8 tests ✅ | — |
| core:data | FeedbackRepositoryImpl | 3 tests ✅ | — |
| core:data | ScanRepositoryImpl | 8 tests ✅ | — |
| core:data | StoriesRepositoryImpl | 14 tests ✅ | — |
| core:data | TranslateRepositoryImpl | 4 tests ✅ | — |
| core:data | UserRepositoryImpl | 14 tests ✅ | — |
| core:designsystem | ButtonScreenshotTest | 7 tests ✅ | — |
| core:network | AuthInterceptor | 7 tests ✅ | — |
| core:network | RateLimitInterceptor | 4 tests ✅ | — |
| core:network | TokenAuthenticator | 5 tests ✅ | — |
| feature:auth | AuthViewModel | 12 tests ✅ | — |
| feature:dictionary | DictionaryViewModel | 9 tests ✅ | — |
| feature:explore | ExploreViewModel | 9 tests ✅ | — |
| feature:scan | GardinerUnicode | 22 tests ✅ | — |
| core:database | SignDao | 9 tests (instrumented) ✅ | — |
| core:database | LandmarkDao | 8 tests (instrumented) ✅ | — |

### Modules With ZERO Tests (17 VMs + 6 repos + infrastructure)

| Module | Class | Tests Needed | Priority |
|--------|-------|-------------|----------|
| feature:chat | ChatViewModel | SSE streaming, message management, edit, retry, TTS | P0 |
| feature:scan | ScanViewModel | Image capture, double-submit, bitmap thread, progress | P0 |
| feature:stories | StoriesViewModel | Filter, favorite, list management | P1 |
| feature:stories | StoryReaderViewModel | Chapter nav, interactions, glyph learning, TTS | P1 |
| feature:dashboard | DashboardViewModel | Stats, favorites, filtering | P1 |
| feature:settings | SettingsViewModel | TTS prefs, cache clear, profile, logout | P1 |
| feature:landing | LandingViewModel | Load, refresh, double-submit guard | P1 |
| feature:scan | ScanResultViewModel | Load result, TTS, detail display | P1 |
| feature:scan | HistoryViewModel | History list, delete | P2 |
| feature:feedback | FeedbackViewModel | Submit, validation, error | P2 |
| feature:dictionary | SignDetailViewModel | Load sign, TTS, pronunciation | P2 |
| feature:dictionary | LessonViewModel | Load lesson, TTS, progress | P2 |
| feature:dictionary | TranslateViewModel | Translate, language switch | P2 |
| feature:dictionary | WriteViewModel | Palette load, composition | P2 |
| feature:explore | DetailViewModel | Load landmark, TTS | P2 |
| feature:explore | IdentifyViewModel | Image upload, results | P2 |
| app | HieroglyphsHubViewModel | Random page, suggested signs, load | P2 |
| core:network | TokenManager | Token CRUD, encryption, clear | P1 |
| core:firebase | FirebaseAuthManager | Sign-in, sign-out, token | P1 |
| core:common | ConnectivityManagerNetworkMonitor | Online/offline flow | P2 |
| core:data | UserPreferencesDataStore | Prefs read/write | P2 |
| — | Navigation (WadjetNavHost) | Route reachability, auth gating, back stack | P1 |
| — | Screenshot tests (all screens) | Visual regression | P2 |
| — | E2E tests (user journeys) | Full flow validation | P3 |
| — | API contract tests | Backend alignment | P2 |

---

## What Tests to Write (Ordered by Priority)

| Priority | Type | Module | Test Target | Why | Est. Tests |
|----------|------|--------|-------------|-----|------------|
| P0 | Unit | feature:chat | ChatViewModel | Most complex VM, SSE streaming + TTS + edit/retry | 15-20 |
| P0 | Unit | feature:scan | ScanViewModel | Double-submit bug, Main thread bitmap, progress states | 10-12 |
| P0 | Unit | core:network | TokenManager | Token persistence drives entire auth flow | 8-10 |
| P1 | Unit | feature:stories | StoriesViewModel + StoryReaderViewModel | Complex interactions, GlobalScope bug | 15-20 |
| P1 | Unit | feature:dashboard | DashboardViewModel | Stats, favorites filtering | 8-10 |
| P1 | Unit | feature:settings | SettingsViewModel | TTS prefs, logout, cache clear | 8-10 |
| P1 | Unit | feature:landing | LandingViewModel | Refresh double-submit, state loading | 6-8 |
| P1 | Unit | feature:scan | ScanResultViewModel + HistoryViewModel | Result display, history CRUD | 10-12 |
| P1 | Unit | core:firebase | FirebaseAuthManager | Auth flow correctness | 6-8 |
| P1 | Integration | — | Navigation (WadjetNavHost) | Route reachability, auth gating | 10-12 |
| P2 | Unit | feature:dictionary | SignDetailVM, LessonVM, TranslateVM, WriteVM | 4 untested VMs | 20-25 |
| P2 | Unit | feature:explore | DetailVM, IdentifyVM | Landmark detail, image upload | 10-12 |
| P2 | Unit | feature:feedback | FeedbackViewModel | Form submission | 4-6 |
| P2 | Unit | app | HieroglyphsHubViewModel | Random page bug | 6-8 |
| P2 | Screenshot | all modules | Every screen × dark (+ RTL later) | Visual regression baseline | 40-50 |
| P2 | Contract | — | All 37+ endpoints | Backend alignment | 37+ |
| P3 | E2E | — | 10 user journeys | Full flow validation | 10 scripts |
| P3 | Compose UI | — | Interactive flows (login, scan, chat) | Interaction testing | 15-20 |

**Total estimated new tests: ~250-300**
