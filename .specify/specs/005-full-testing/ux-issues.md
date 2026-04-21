# UX Issues — Complete Inventory

## Critical (user gets confused or can't finish what they started)

| # | Screen | Issue | What It Looks Like Now | Fix |
|---|--------|-------|------------------------|-----|
| 1 | Welcome | Sign-in buttons cut off at 720×1280, no scroll — user **cannot log in** on small phones | Buttons below fold, `scrollable=false` | Wrap in `verticalScroll(rememberScrollState())` inside WelcomeScreen.kt Column |
| 2 | Scan | No `isLoading` guard on `onImageCaptured()` → double-submit on rapid tap | ScanViewModel.kt — missing guard | Add `if (_uiState.value.isLoading) return` at top of `onImageCaptured()` |
| 3 | Identify | Same double-submit bug as Scan | IdentifyViewModel.kt — missing guard | Same fix as Scan |
| 4 | Chat | Streaming `.map{}` copies full message list per chunk → ANR during long chats | ChatViewModel.kt:~L195 — 5,000 list copies for 50msg+100chunks | Replace `.map{}` with index-based update on mutable list |
| 5 | Scan | `BitmapFactory.decodeFile()` on Main thread → ANR risk | ScanViewModel.kt:~L173 | Move to `withContext(Dispatchers.IO)` |
| 6 | Auth | Process death: Welcome shown despite persisted tokens — auto-login broken | WelcomeScreen shows after Android kills/restores app | Splash/start destination must check token existence before routing |
| 7 | Explore | Landmark parent/children DTOs always null (key + type mismatch) | LandmarkDetailDto uses `parent` (object) but backend sends `parent_slug` (string) | Fix DTO key names: `parent_slug: String?`, `children_slugs: List<String>?` |
| 8 | Chat + StoryReader | `GlobalScope.launch(NonCancellable)` in `onCleared()` → memory leak + crash | ChatViewModel.kt, StoryReaderViewModel.kt | Use `viewModelScope` with `NonCancellable` or `ProcessLifecycleOwner` |

## Major (user friction, unclear flow)

| # | Screen | Issue | What It Looks Like Now | Fix |
|---|--------|-------|------------------------|-----|
| 9 | TTS Settings | `ttsEnabled` and `ttsSpeed` DataStore values never read by any playback VM | Toggle/slider work in UI but audio ignores them | All speak functions must check `userPrefs.ttsEnabled` before playing |
| 10 | All Audio | MediaPlayer `playWavBytes()` duplicated 7× across VMs, no shared manager | 7 independent copy-paste implementations | Extract `AudioPlaybackManager` singleton with audio focus, temp file cleanup |
| 11 | All Audio | Zero audio focus handling — plays over phone calls | No `AudioManager.requestAudioFocus()` anywhere | Add focus request/abandon in shared `AudioPlaybackManager` |
| 12 | Explore | "Identify from photo" is a tiny 63×63 icon in top-right header — easy to miss | Small camera icon at top of ExploreScreen, no label | Add visible "Identify" text label below icon, increase to 48dp+, or use FAB |
| 13 | Scan + Identify | Both screens look identical: same Eye of Horus icon, same upload layout | ImageUploadZone has no icon parameter | Add `icon` param to ImageUploadZone; use camera icon for Scan, landmark icon for Identify |
| 14 | Auth | FCM token generated but never sent to backend → push targeting impossible | Token in Logcat but no API call | Call `POST /api/user/fcm-token` after successful login |
| 15 | Auth | Logout doesn't clear Room DB or DataStore → ghost state for next user | Only clears tokens + Firebase sign-out | Add `database.clearAllTables()` + `dataStore.clear()` in logout flow |
| 16 | Auth | Token refresh failure doesn't sign out Firebase → ghost auth state | Firebase still authed after API token expires | Call `firebaseAuth.signOut()` when refresh fails |
| 17 | Dictionary | Write palette missing `numbers` and `determinative` groups from backend | PaletteGroupsDto has 4 fields, backend sends 6 | Add `numbers` and `determinative` fields to `PaletteGroupsDto` |
| 18 | All APIs | ZERO retry logic in any repository — single failure = permanent error | Exception → error state, no retry | Add `retryWithExponentialBackoff()` to network calls |
| 19 | 13 ViewModels | No `isLoading` guard → double-submit on rapid interactions | Users can trigger duplicate API calls on fast taps | Add guard at top of each action function |
| 20 | Chat | SSE streaming runs on Main thread pool → ANR risk | ChatRepositoryImpl network call on default dispatcher | Use `Dispatchers.IO` for SSE connection |
| 21 | Landing | DotPattern draws ~4,000 circles per frame → continuous jank | Infinite animation with individual drawCircle calls | Pre-render dot grid to `ImageBitmap`, draw once per frame |
| 22 | All A11y | Zero `testTag` in entire codebase → Compose UI testing impossible | No `Modifier.testTag()` anywhere | Add testTags to all interactive elements (buttons, fields, cards) |
| 23 | All A11y | 7 interactive icons with `contentDescription = null` | TalkBack announces nothing for speak buttons | Add content descriptions: "Play pronunciation", "Ask Thoth", etc. |
| 24 | All A11y | Touch targets 24dp on Lesson/Learn speak buttons (minimum is 48dp) | LessonScreen.kt:~L212, LearnTab.kt:~L153 | Increase `IconButton` size to 48.dp minimum |
| 25 | Chat Arabic | `values-ar/strings.xml` contains English strings, not Arabic | Tools:ignore MissingTranslation used, UTF-8 mojibake in suggestion chips | Write proper Arabic translations for chat module |

## Minor (polish)

| # | Screen | Issue | What It Looks Like Now | Fix |
|---|--------|-------|------------------------|-----|
| 26 | Welcome | "Built by Mr Robot" footer | Production app shows meme text | Remove or change to "Made with ❤️" |
| 27 | Landing | "Translate" feature bullet on Hieroglyphs card but Dictionary has Browse/Learn/Write/Translate | Text may be stale if Translate was removed | Verify Translate tab status; update bullets |
| 28 | Landing | CTA buttons below fold, cards not clickable | Bullets fill card, button at bottom needs scroll | Make entire card tappable or move CTA to top |
| 29 | Typography | `headlineSmall` (22sp) > `headlineMedium` (20sp) | Hierarchy inverted | Swap values: headlineMedium=22, headlineSmall=20 |
| 30 | Landing | shineSweep animation on every list card → jank on scroll | 10+ concurrent infinite animations | Disable shineSweep on list items, keep on featured/hero only |
| 31 | All A11y | Zero `heading()` semantics → TalkBack can't navigate by headings | No `.semantics { heading() }` | Add heading semantics to all screen titles and section headers |
| 32 | All A11y | Zero `mergeDescendants` on cards → TalkBack reads each child separately | Noisy screen reader experience | Add `semantics(mergeDescendants = true)` on story/landmark/sign cards |
| 33 | All A11y | 15 clickable elements without Role or contentDescription | `.clickable {}` without semantic info | Add `Role.Button` and descriptions to all 15 instances |
| 34 | All A11y | Zero reduced-motion support for 9+ infinite animations | Animations run even with "Remove animations" | Check `LocalReducedMotionEnabled` or `AccessibilityManager` |
| 35 | Security | No certificate pinning on OkHttpClient | MITM possible | Add `CertificatePinner` for `nadercr7-wadjet-v2.hf.space` |
| 36 | Security | Backup rules don't exclude encrypted_prefs | Token leak via ADB backup | Add `encrypted_prefs.xml` to `data_extraction_rules.xml` exclude list |
| 37 | Dashboard | Avatar shows "E" (from "Explorer" role?) — may show wrong initial | Unclear initial letter | Verify it uses first letter of display name |
| 38 | Stories Arabic | Suggestion chips UTF-8 mojibake | Garbled Arabic text | Fix encoding in `values-ar/strings.xml` |
| 39 | Coil | Disk cache 5% of disk (3-6GB) | Excessive storage on modern devices | Set fixed `maxSizeBytes(250L * 1024 * 1024)` |
| 40 | ScanScreen | ~100 lines of dead camera/permission code | `PermissionDeniedContent` never rendered | Remove dead code |
| 41 | StoryReader | Loading state is just `Text("Loading...")` | No spinner or shimmer | Use `WadjetSectionLoader` like other screens |
| 42 | HieroglyphsHub | No error state rendering | Hub silently fails | Add `ErrorState` composable |
| 43 | DictSign | Error shows raw text, no retry button | No retry affordance | Add `ErrorState` with retry button |
| 44 | ProGuard | `-renamesourcefileattribute` commented out | Real filenames in release stack traces | Uncomment the line |
| 45 | Firebase | FirebaseFirestore provided in DI but never used | Dead binding | Remove from `FirebaseModule` |
