# PARITY.md — Web (Wadjet-v3-beta) vs Android (Wadjet-Android)

_Source of truth = web app. Filled 2026-07-06 (Phase 1). Status: PRESENT / PARTIAL / MISSING / EXTRA._

| Feature | Web evidence | Android status | Android evidence | Gaps |
|---|---|---|---|---|
| Auth (register/login/Google/logout/refresh) | `app/api/auth.py:116,145,182,212,260` | PRESENT | `feature/auth/*`, `AuthApiService.kt:14-30`, `AuthRepositoryImpl` | Firebase-fronted + backend session — functionally equivalent |
| Email verification | `auth.py:320,340,354` | PARTIAL | `AuthViewModel.kt:132`, `VerifyEmailSheet.kt` (Firebase-based) | Email link has no Android deep link; backend verify endpoints unused |
| Forgot/reset password | `auth.py:371,393` | PARTIAL | `AuthApiService.kt:29`, `ForgotPasswordSheet.kt` | Reset link completes on web only (no deep link) |
| Scan (photo → identify) | `pages.py:54`, `api/scan.py:852` | PRESENT | `ScanScreen.kt`, `ScanResultScreen.kt`, `ScanApiService.kt:14` | Camera capture disabled (upload-only; web is upload-only too) |
| Scan history | `api/user.py:74` | PRESENT | `ScanHistoryScreen.kt` + Room | — |
| Per-glyph TTS / EN↔AR toggle | `scan.html`, `api/audio.py` | PRESENT | `ScanResultScreen.kt:332-359` | Toggle resets on rotation (remember, not rememberSaveable) |
| Dictionary browse/search | `api/dictionary.py:1146,979` | PRESENT | `DictionaryScreen.kt`, `BrowseTab.kt` | — |
| Sign detail | `dictionary.py:1126` | PRESENT | `SignDetailSheet.kt`, `DictionarySignScreen.kt` | — |
| Favorites (glyphs/stories/landmarks) | `api/user.py:90-119` | PRESENT | `toggleGlyphFavorite` etc., `UserApiService.kt:36-43` | — |
| Learn / alphabet | `dictionary.py:1000` | PRESENT | `LearnTab.kt` | — |
| Lessons (5 levels) | `pages.py:190`, `dictionary.py:1018` | PRESENT | `LessonScreen.kt`, `Route.Lesson(level)` | TTS silent-failure bug (H-05) |
| Write (text → hieroglyphs) | `pages.py:82`, `api/write.py:448,550` | PRESENT | `WriteTab.kt`, `WriteApiService.kt:13-17` | — |
| Explore list/search/filter | `pages.py:90`, `api/explore.py:901,605-668` | PRESENT | `ExploreScreen.kt`, `LandmarkApiService.kt:19-40` | — |
| Landmark detail | `explore.py:668,647` | PRESENT | `LandmarkDetailScreen.kt` | — |
| Identify landmark by photo | `api/explore.py` identify | PRESENT | `IdentifyScreen.kt`, `LandmarkApiService.kt:44` | EXIF bug F-01 applies |
| Ask Thoth about landmark | landmark→chat | PRESENT | `Route.ChatLandmark(slug)`, `ChatViewModel.kt:70` | Slug failure silently degrades to generic chat |
| Stories list/filter | `pages.py:145`, `api/stories.py:32` | PRESENT | `StoriesScreen.kt` | Not cached offline (E-02) |
| Story reader (chapters/quiz/images/narration) | `stories.py:52,80,149` | PRESENT | `StoryReaderScreen.kt`, `StoryReaderViewModel` | Arabic narration unreachable (H-01) |
| Thoth chat (send + SSE streaming) | `api/chat.py:63,90` | PRESENT | `ChatScreen.kt`, SSE in `ChatRepositoryImpl.kt` | — |
| Chat clear / history | `chat.py:131` | PRESENT | `ChatApiService.kt:11`, `ChatHistoryStore.kt` | Android-local history (richer than web) |
| Voice input (STT) + message TTS | `api/audio.py:57,126` | PRESENT | `AudioApiService.kt:16-21` | Arabic lang not passed (H-04) |
| Dashboard | `pages.py:202`, `api/user.py:133,140` | PRESENT | `DashboardScreen.kt` | Ignores local scan history offline (E-05c) |
| Settings | `pages.py:212`, `api/user.py:42,60` | PRESENT | `SettingsScreen.kt` | **No language switcher** (below) |
| Feedback submit | `api/feedback.py:58` | PRESENT | `FeedbackScreen.kt`, `FeedbackApiService.kt:11` | — |
| Feedback admin list | `pages.py:222`, `feedback.py:80` | MISSING | — | Admin-only web tool; acceptable out-of-scope |
| **Language toggle EN/AR** | `partials/nav.html:42-45,84` `toggleLang()` | **MISSING (in-app switch)** | full `values-ar` exists everywhere; strings `quick_settings_language/arabic` defined but UNUSED (`feature/settings/.../values/strings.xml:34,36`); no `setApplicationLocales`/LocaleManager anywhere | User cannot switch EN↔AR in-app; follows system locale only |
| Translate API | `api/translate.py:26` | PARTIAL (no UI) | `TranslateApiService.kt` + repo wired in DI, unused | No web page either — dead data-layer wiring |
| Landing / Welcome / Hieroglyphs hub | `pages.py:24,33,110` | PRESENT | `LandingScreen.kt`, `WelcomeScreen.kt`, `HieroglyphsHubScreen.kt` | — |
| SEO (robots/sitemap) | `pages.py:232,247` | N/A | — | Web-only |
| FCM push | (not in web) | EXTRA | `WadjetFirebaseMessaging` | Notifications can't open specific content (no deep links) |

**Summary:** all primary user-facing features ported. Real parity gaps: (1) in-app EN/AR switch missing — the single biggest Arabic issue; (2) no deep links (email verify/reset + FCM routing); (3) admin feedback view not ported (acceptable); (4) unused Translate wiring.
