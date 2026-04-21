# Master Fix Plan

## All Issues (Unified from all phases)
| # | ID | Phase | Category | Severity | File | Description | Fix Effort | Status |
|---|-----|-------|----------|----------|------|-------------|------------|--------|
| 1 | FIX-001 | 01 | ViewModel | CRITICAL | ScanViewModel.kt | Missing isLoading guard on onImageCaptured | 15min | ✅ DONE |
| 2 | FIX-002 | 01 | ViewModel | CRITICAL | IdentifyViewModel.kt | Missing isLoading guard on onImageCaptured | 15min | ✅ DONE |
| 3 | FIX-003 | 01 | ViewModel | CRITICAL | ChatViewModel.kt | GlobalScope in onCleared() | 30min | ✅ DONE |
| 4 | FIX-004 | 01 | ViewModel | CRITICAL | StoryReaderViewModel.kt | GlobalScope in onCleared() | 30min | ✅ DONE |
| 5 | FIX-005 | 01 | ViewModel | CRITICAL | ChatViewModel.kt:~L195 | Streaming .map{} copies entire list per chunk | 1h | ✅ DONE |
| 6 | FIX-006 | 01 | ViewModel | CRITICAL | ScanViewModel.kt:~L173 | Bitmap decode on Main thread | 20min | ✅ DONE |
| 7 | FIX-007 | 01 | Repository | CRITICAL | StoriesRepositoryImpl.kt | runBlocking in Firestore listener | 30min | ✅ DONE |
| 8 | FIX-008 | 01 | Repository | CRITICAL | ExploreRepositoryImpl.kt | favoritesLoaded race condition | 20min | ✅ DONE |
| 9 | FIX-009 | 01 | Auth | CRITICAL | TokenAuthenticator.kt | Bare OkHttpClient for refresh | 30min | ✅ DONE |
| 10 | FIX-010 | 01 | Auth | CRITICAL | MainActivity.kt | Process death session restoration broken | 1h | ✅ DONE |
| 11 | FIX-011 | 01 | ViewModel | HIGH | DictionaryViewModel.kt | speakSign no loading guard | 15min | ✅ DONE |
| 12 | FIX-012 | 01 | ViewModel | HIGH | SignDetailViewModel.kt | toggleFavorite race condition | 20min | ✅ DONE |
| 13 | FIX-013 | 01 | ViewModel | HIGH | ExploreViewModel.kt | loadLandmarks no job cancellation | 20min | ✅ DONE |
| 14 | FIX-014 | 01 | ViewModel | HIGH | DictionaryViewModel.kt | loadSigns no job cancellation | 20min | ✅ DONE |
| 15 | FIX-015 | 01 | ViewModel | HIGH | StoriesViewModel.kt | toggleStoryFavorite no guard | 15min | ✅ DONE |
| 16 | FIX-016 | 01 | ViewModel | HIGH | DashboardViewModel.kt | refresh no cancellation | 20min | ✅ DONE |
| 17 | FIX-017 | 01 | ViewModel | HIGH | SettingsViewModel.kt | saveName/signOut missing guards | 15min | ✅ DONE |
| 18 | FIX-018 | 01 | Auth | HIGH | AuthRepositoryImpl.kt | Logout doesn't clear local data | 30min | ✅ DONE |
| 19 | FIX-019 | 01 | Auth | HIGH | TokenAuthenticator.kt | Refresh failure no Firebase signout | 15min | ✅ DONE |
| 20 | FIX-020 | 02 | UX | CRITICAL | WelcomeScreen.kt | Welcome not scrollable — can't login on small phones | 20min | ✅ DONE |
| 21 | FIX-021 | 02 | Design | MEDIUM | WadjetTypography.kt | headlineSmall > headlineMedium sizes swapped | 5min | ✅ DONE |
| 22 | FIX-022 | 02 | UX | HIGH | ImageUploadZone.kt | No icon parameter — Scan/Identify identical | 30min | ✅ DONE |
| 23 | FIX-023 | 02 | UX | HIGH | ExploreScreen.kt | Identify button tiny, no label | 20min | ✅ DONE |
| 24 | FIX-024 | 02 | Perf | MEDIUM | DotPattern.kt | ~4000 drawCircle per frame | 1h | ✅ DONE |
| 25 | FIX-025 | 02 | Perf | MEDIUM | ShineSweep.kt | shineSweep on all list cards | 30min | ✅ DONE |
| 26 | FIX-026 | 02 | i18n | HIGH | chat/values-ar/strings.xml | English strings instead of Arabic | 30min | ✅ DONE |
| 27 | FIX-027 | 02 | UX | LOW | WelcomeScreen.kt | "Built by Mr Robot" | 5min | ✅ DONE |
| 28 | FIX-028 | 02 | UX | LOW | LandingScreen.kt | "Translate" bullet stale | 10min | ✅ DONE |
| 29 | FIX-029 | 02 | UX | MEDIUM | StoryReaderScreen.kt | Loading just Text("Loading...") | 15min | ✅ DONE |
| 30 | FIX-030 | 03 | API | CRITICAL | LandmarkModels.kt | parent/children DTOs always null | 30min | ✅ DONE |
| 31 | FIX-031 | 03 | API | HIGH | WriteModels.kt | Palette missing numbers + determinatives | 15min | ✅ DONE |
| 32 | FIX-032 | 03 | Network | MEDIUM | AuthInterceptor.kt | Logout not in isAuthEndpoint | 10min | ✅ DONE |
| 33 | FIX-033 | 03 | Audio | HIGH | SettingsRepo + VMs | TTS settings decorative | 2h | ✅ DONE |
| 34 | FIX-034 | 03 | Audio | HIGH | 7 ViewModels | MediaPlayer 7× duplication, no audio focus | 3h | ✅ DONE |
| 35 | FIX-035 | 03 | Security | MEDIUM | backup_rules.xml | Encrypted prefs not excluded from backup | 10min | ✅ DONE |
| 36 | FIX-036 | 03 | Security | LOW | proguard-rules.pro | renamesourcefileattribute commented out | 5min | ✅ DONE |
| 37 | FIX-037 | 03 | Perf | LOW | WadjetApplication.kt | Coil disk cache unbounded | 5min | ✅ DONE |
| 38 | FIX-038 | 04 | A11y | HIGH | LessonScreen.kt | Speak button touch targets 24dp | 15min | ✅ DONE |
| 39 | FIX-039 | 04 | A11y | HIGH | LearnTab.kt | Speak button touch targets 24dp | 15min | ✅ DONE |
| 40 | FIX-040 | 04 | A11y | HIGH | 7 locations | Interactive icons null contentDescription | 30min | ✅ DONE |
| 41 | FIX-041 | 04 | A11y | MEDIUM | All screens | Zero heading semantics | 1h | ✅ DONE |
| 42 | FIX-042 | 04 | A11y | MEDIUM | All cards | Zero mergeDescendants | 1h | ✅ DONE |
| 43 | FIX-043 | 04 | A11y | MEDIUM | Animations | Zero reduced motion | 1h | ✅ DONE |
| 44 | FIX-044 | 04 | Perf | MEDIUM | compiler config | Missing stability entries | 15min | ✅ DONE |
| 45 | FIX-045 | 04 | Perf | LOW | Lazy lists | Missing keys | 30min | ✅ DONE |

## Fix Execution Order
1. **Fix Phase A** (Critical bugs + Security): FIX-001 to FIX-010, FIX-020, FIX-030, FIX-035
2. **Fix Phase B** (Core UX + High bugs): FIX-011 to FIX-019, FIX-022, FIX-023, FIX-026, FIX-031, FIX-033
3. **Fix Phase C** (Visual + Accessibility): FIX-021, FIX-024, FIX-025, FIX-027 to FIX-029, FIX-038 to FIX-043
4. **Fix Phase D** (Performance + Polish): FIX-032, FIX-034, FIX-036, FIX-037, FIX-044, FIX-045

## Dependency Map
- FIX-022 (ImageUploadZone icon param) must come before FIX-023 (Identify button update)
- FIX-033 (TTS settings wiring) should come before FIX-034 (audio manager extraction)
- FIX-010 (session restoration) may impact FIX-018 (logout clearing)
