# Implementation Plan — 10 Phases

> Structured task plan for achieving full logic parity between Android and web apps.
> Each task references specific files, gap IDs, and has clear verification criteria.
> Phases are sequential — each depends on the previous.

---

## Phase 0: API Contract Alignment + Critical Fixes
> **Goal**: Fix all DTO mismatches. Enforce smart defaults. Add rate-limit handling. Wire free-tier limits.
> **Fixes**: B1, B2, B3, G10, M2, M5, A1, A4, A10

| Task | Title | Files | Gap IDs | Verification |
|------|-------|-------|---------|-------------|
| 0.1 | Fix `IdentifyResponse` DTO — remove `landmark`, add `source`, `agreement`, `description`, `isKnownLandmark`, `isEgyptian` | `core/network/model/LandmarkModels.kt` | B1 | Compiles, JSON response deserializes |
| 0.2 | Fix `IdentifyMatchDto` — add `source: String?` | `core/network/model/LandmarkModels.kt` | B2 | Compiles |
| 0.3 | Fix `IdentifyResult` domain model — add ensemble fields, remove `detail` | `core/domain/model/Landmark.kt` | B3 | Compiles |
| 0.4 | Fix `IdentifyMatch` — add `source: String?` | `core/domain/model/Landmark.kt` | B2 | Compiles |
| 0.5 | Fix mapping in `ExploreRepositoryImpl.identifyLandmark()` | `core/data/repository/ExploreRepositoryImpl.kt` | B1-B3 | Identify call returns correct fields |
| 0.6 | Fix `ScanResultSerializable.toDomain()` — preserve `confidenceSummary` | `core/data/repository/ScanRepositoryImpl.kt` | M2 | Scan history reload shows confidence |
| 0.7 | Fix chat slug extraction — verify `savedStateHandle` works with type-safe nav | `feature/chat/ChatViewModel.kt` | G10 | ChatLandmark receives slug |
| ~~0.8~~ | ~~Fix story TTS~~ — **ALREADY DONE** in current code. `speakChapter()` passes `voice = chapter.ttsVoice, style = chapter.ttsStyle`. **SKIP.** | — | ~~M5~~ | — |
| 0.9 | Add 429 rate-limit handler — parse `Retry-After`, show cooldown, exponential backoff | `core/network/AuthInterceptor.kt` or new `RateLimitInterceptor.kt` | A1 | 429 → user-friendly message |
| 0.10 | Enforce smart defaults — remove Write mode selector, hardcode `smart` | `WriteViewModel.kt`, `WriteTab.kt` | A10 | No mode chips visible, write sends smart |
| 0.11 | Wire free tier limits — check before scan/chat, show usage badge | `ScanViewModel.kt`, `ChatViewModel.kt`, `LandingScreen.kt` | A4 | "3/10 scans today" visible |
| 0.12 | Add `RECORD_AUDIO` permission to AndroidManifest.xml | `app/src/main/AndroidManifest.xml` | N1 | STT works |
| 0.13 | Add `composable<Route.DictionarySign>` handler in WadjetNavGraph | `WadjetNavGraph.kt` | N2 | Sign detail navigation works |
| 0.14 | Add `chatMessagesToday` to `UsageDto` | `core/network/model/UserModels.kt` | N4 | Chat limit enforceable |

**Exit Criteria**: Project compiles. Identify returns full ensemble data. Write has no mode picker. Rate limit handled. STT permission declared. DictionarySign route exists.

---

## Phase 1: Image Loading & Placeholder System
> **Goal**: Every image shows content or a themed placeholder. Zero blank spaces.
> **Fixes**: B4, M1, M3

| Task | Title | Files | Gap IDs |
|------|-------|-------|---------|
| 1.1 | Create 4 Egyptian-themed vector placeholder drawables | `core/designsystem/src/main/res/drawable/` | B4 |
| 1.2 | Fix ALL `AsyncImage` calls — add placeholder/error/fallback | Every file with `AsyncImage` | B4, M3 |
| 1.3 | Fix `BaseUrlInterceptor` — handle relative paths without leading `/` | `WadjetApplication.kt` | M1 |
| 1.4 | Story cover visual — coverGlyph on difficulty-gradient background | `StoriesScreen.kt` | G6 |

**Exit Criteria**: Browse all landmarks, stories — every card shows image or placeholder. Zero blank spaces.

---

## Phase 2: Identify Feature Logic Parity
> **Goal**: Identify shows FULL ensemble result matching web app quality.
> **Fixes**: G5, M7
> **Depends on**: Phase 0 (DTOs fixed)

| Task | Title | Files | Gap IDs |
|------|-------|-------|---------|
| 2.1 | Rewrite `IdentifyResults` composable — confidence badge, source chip, agreement badge, warnings, description, top-3 with progress bars, action buttons | `IdentifyScreen.kt` | G5 |
| 2.2 | Auto-fetch landmark detail when top match ≥60% + isKnownLandmark | `IdentifyViewModel.kt` | G5 |
| 2.3 | Add "Identify Another" button — reset state to upload | `IdentifyScreen.kt` | M7 |
| 2.4 | Update `IdentifyUiState` — add detail preview + loading state | `IdentifyViewModel.kt` | G5 |

**Exit Criteria**: Upload pyramids → "Great Pyramids of Giza" with ensemble source, agreement, description. NOT "White Desert".

---

## Phase 3: Navigation Restructure
> **Goal**: All features reachable in ≤2 taps. Match web info architecture.
> **Fixes**: G1, G2, G3, B5

| Task | Title | Files | Gap IDs |
|------|-------|-------|---------|
| 3.1 | Restructure bottom nav: Home \| Hieroglyphs \| Explore \| Stories \| Thoth | `TopLevelDestination.kt` | G1, G2 |
| 3.2 | Create `HieroglyphsHubScreen` — Scan + Dictionary + Write cards + explainer | New screen file | G1 |
| 3.3 | Fix "Write" navigation — add `initialTab` param to `Route.Dictionary` | `Route.kt`, `DictionaryScreen.kt`, `WadjetNavGraph.kt` | G3, B5 |
| 3.4 | Add user avatar to top app bar → navigates to Dashboard | `WadjetApp.kt` | G2 |
| 3.5 | Update `WadjetNavHost` with new routes | `WadjetNavHost.kt` | G1, G2 |

**Exit Criteria**: Every feature reachable in ≤2 taps. "Write" opens on Write tab. Thoth in bottom nav.

---

## Phase 4: Scan Feature Quality
> **Goal**: Match web's scan result detail. Smart default = always auto.
> **Fixes**: G9

| Task | Title | Files | Gap IDs |
|------|-------|-------|---------|
| 4.1 | Verify scan always sends `mode=auto` (no UI picker) | `ScanViewModel.kt` | A11 |
| 4.2 | Enhance `ScanResultScreen` — pipeline source, confidence card, quality hints, AI notes | `ScanResultScreen.kt` | — |
| 4.3 | Make annotated image zoomable (pinch + pan) | `ScanResultScreen.kt` | — |
| 4.4 | Make glyphs tappable → bottom sheet with Gardiner code, meaning, confidence, "View in Dictionary" | `ScanResultScreen.kt` | G9 |
| 4.5 | Add "Scan Another" button | `ScanResultScreen.kt` | — |
| 4.6 | TTS "Read aloud" for full transliteration | `ScanResultScreen.kt` | — |

**Exit Criteria**: Scan → see pipeline, confidence, AI notes. Tap glyphs → detail sheet. TTS works.

---

## Phase 5: Landmark Detail & Explore
> **Goal**: Full landmark detail parity with web.
> **Fixes**: G4

| Task | Title | Files | Gap IDs |
|------|-------|-------|---------|
| 5.1 | Verify `LandmarkDetailScreen` renders ALL sections (carousel, highlights, tips, dynasty, pharaohs, recommendations, children, maps, Wikipedia, "Ask Thoth") | `LandmarkDetailScreen.kt` | — |
| 5.2 | Add featured landmarks carousel to `ExploreScreen` top | `ExploreScreen.kt`, `ExploreViewModel.kt` | G4 |
| 5.3 | Category + city filters as scrollable chip rows | `ExploreScreen.kt` | — |
| 5.4 | Verify search with debounce | `ExploreViewModel.kt` | — |
| 5.5 | Pull-to-refresh on `ExploreScreen` | `ExploreScreen.kt` | M6 |

**Exit Criteria**: Every section renders. Featured carousel visible. Filters and search work.

---

## Phase 6: Stories & TTS
> **Goal**: Stories and audio at full quality.
> **Fixes**: G6, M4

| Task | Title | Files | Gap IDs |
|------|-------|-------|---------|
| 6.1 | Enhance story cards — coverGlyph on gradient, difficulty badge, chapter count | `StoriesScreen.kt` | G6 |
| 6.2 | Verify TTS voice/style passthrough (fixed in Phase 0) | `StoryReaderViewModel.kt` | — |
| 6.3 | Verify scene image generation + shimmer + Ken Burns | `StoryReaderScreen.kt` | — |
| 6.4 | Auto-narration mode — sequential paragraph TTS, highlight current | `StoryReaderScreen.kt` | — |
| 6.5 | Verify all 4 interaction types work | `StoryReaderScreen.kt` | — |
| 6.6 | Fix `LOCAL_TTS:` language detection — Arabic → `Locale("ar")` | `ChatScreen.kt` | M4 |

**Exit Criteria**: Full story playthrough. TTS with voices. Scene images load. All interactions work.

---

## Phase 7: Chat & Polish
> **Goal**: Chat parity + favorites + progress sync + loading/error/empty states.
> **Fixes**: M6, M7, M8, A5, A7, A9

| Task | Title | Files | Gap IDs |
|------|-------|-------|---------|
| 7.1 | Verify ChatLandmark context (slug, header) | `ChatViewModel.kt`, `ChatScreen.kt` | — |
| 7.2 | Verify markdown rendering | `ChatScreen.kt` | — |
| 7.3 | SSE robustness — mid-stream errors, timeout recovery | `ChatViewModel.kt` | — |
| 7.4 | Add glyph + story favorite buttons | Dictionary sign sheet, `StoriesScreen.kt` | A7 |
| 7.5 | Wire story progress to REST API (`POST /api/user/progress`) | `StoriesRepositoryImpl.kt` | A5 |
| 7.6 | Shimmer loading skeletons | Explore, Dictionary, Stories screens | — |
| 7.7 | Contextual error messages per feature | All screens | M8 |
| 7.8 | Empty states with themed illustrations | All list screens | — |
| 7.9 | Pull-to-refresh on Explore, Stories, Dashboard | Multiple screens | M6 |
| 7.10 | Haptic feedback on key actions | Multiple screens | — |
| 7.11 | Offline banner via `NetworkMonitor` | `WadjetApp.kt` | — |
| 7.12 | Accessibility pass — `contentDescription` on all images/icons | All screens | A9 |
| 7.13 | Persist TTS settings to DataStore (enabled, speed) | `SettingsViewModel.kt` | N3 | Settings survive restart |

**Exit Criteria**: All screens have loading/error/empty states. Favorites for all 3 types. Haptics. Accessibility. TTS settings persist.

---

## Phase 8: Testing & Validation
> **Goal**: Comprehensive test of EVERY feature before APK build.

### Test Matrix
| Area | Tests | Smart Default Check |
|------|-------|-------------------|
| Auth | Register, login, Google, refresh (30+ min), logout | — |
| Scan | Upload → codes, metadata, tappable glyphs, TTS, history, daily limit | **No mode selector visible** |
| Identify | Pyramids→Giza, Sphinx→Sphinx, non-Egyptian→warning, ensemble metadata | — |
| Dictionary | Browse, search, 5 lessons, alphabet | — |
| Write | Type English → hieroglyphs, palette, copy, TTS | **No mode selector visible** |
| Explore | Cards with images, filters, search, detail, maps, recommendations, "Ask Thoth", favorites | — |
| Stories | Covers, chapters, images, interactions, TTS, bilingual, favorites, progress | — |
| Chat | SSE, markdown, STT, TTS, landmark context, clear, history | — |
| Dashboard | Profile, stats, history, favorites (3 types), progress | — |
| Rate limits | Scan limit message, chat limit message, 429 friendly retry | — |
| Offline | Dictionary FTS, cached landmarks, banner, error messages | — |

**Exit Criteria**: All tests pass. No crashes. No blank images. Smart defaults enforced.

---

## Phase 9: APK Build
> **Goal**: Production-ready release APK.

| Task | Title |
|------|-------|
| 9.1 | Version bump in `app/build.gradle.kts` |
| 9.2 | Verify ProGuard keeps: `@Serializable` DTOs, Retrofit, Room, Firebase, Hilt, Coil |
| 9.3 | Check signing config |
| 9.4 | `./gradlew assembleRelease` |
| 9.5 | `./gradlew bundleRelease` |
| 9.6 | Test release APK on physical device |
| 9.7 | Smoke test: install → register → scan → identify → browse → chat → story → dashboard |
| 9.8 | Enable Room schema export + proper migrations (replace destructive fallback) | `WadjetDatabase.kt`, `DatabaseModule.kt` | N5 |
| 9.9 | Configure backup rules to exclude encrypted tokens | `data_extraction_rules.xml`, `backup_rules.xml` | N7 |

**Output**: `app/build/outputs/apk/release/app-release.apk`

---

## Dependency Graph

```
Phase 0 (DTOs + Smart Defaults + Rate Limits)
    │
    ├── Phase 1 (Image Placeholders)
    │       │
    │       └── Phase 5 (Explore/Landmark Detail)
    │
    ├── Phase 2 (Identify UI) ─── requires Phase 0 DTOs
    │
    ├── Phase 3 (Navigation) ─── independent of 1/2
    │
    ├── Phase 4 (Scan Quality) ─── after Phase 1 placeholders
    │
    └── Phase 6 (Stories + TTS) ─── after Phase 0 TTS fix
            │
            └── Phase 7 (Chat + Polish) ─── after all features done
                    │
                    └── Phase 8 (Testing) ─── after all phases
                            │
                            └── Phase 9 (APK Build)
```

## Effort Estimates
| Phase | Complexity | Key Risk |
|-------|-----------|----------|
| 0 | Medium | DTO changes cascade through layers |
| 1 | Low | Grep-and-fix pattern |
| 2 | Medium | New UI components |
| 3 | Medium | Navigation state management |
| 4 | Medium | Zoomable image, bottom sheets |
| 5 | Low | Mostly verification |
| 6 | Medium | TTS sequential playback |
| 7 | High | Many small changes across all screens |
| 8 | Low | Testing, no new code |
| 9 | Low | Build config |
