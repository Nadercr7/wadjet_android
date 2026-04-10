# Gap Analysis — Android vs Web Parity

> Every discrepancy between the Android app and the web backend, categorized by severity.
> All findings verified by reading actual source code on 2026-04-10.

---

## Severity Legend
- 🔴 **Critical (B)** — Feature broken, produces wrong results
- 🟠 **Major (G)** — Feature missing or significantly different from web
- 🟡 **Minor (M)** — Quality/polish issue, not blocking
- 🔵 **Additional (A)** — Discovered in exhaustive audit, web-exclusive features

---

## 🔴 Critical Bugs (B1–B5)

### B1 — IdentifyResponse DTO has WRONG field
| | Detail |
|--|--------|
| **File** | `core/network/model/LandmarkModels.kt` → `IdentifyResponse` |
| **Problem** | Has `landmark: LandmarkDetailDto? = null` — web NEVER returns this field. MISSING: `source`, `agreement`, `description`, `is_known_landmark`, `is_egyptian` |
| **Impact** | **Pyramids photo → "White Desert"** because without `isKnownLandmark` and `description`, the app can't show what the ensemble identified |
| **Fix Phase** | Phase 0, Task 0.1 |

### B2 — IdentifyMatchDto missing `source` field
| | Detail |
|--|--------|
| **File** | `core/network/model/LandmarkModels.kt` → `IdentifyMatchDto` |
| **Problem** | Web returns `top3` as `[{name, slug, confidence, source}]`. Android DTO has only `{slug, name, confidence}` |
| **Impact** | Can't show which AI model identified each match |
| **Fix Phase** | Phase 0, Task 0.2 |

### B3 — IdentifyResult domain model too simple
| | Detail |
|--|--------|
| **File** | `core/domain/model/Landmark.kt` → `IdentifyResult` |
| **Problem** | Only `topMatch`, `matches`, `detail`. Missing: `source`, `agreement`, `description`, `isKnownLandmark`, `isEgyptian`. The `detail` field maps from non-existent `landmark` DTO field → always null |
| **Impact** | UI can't display any ensemble metadata |
| **Fix Phase** | Phase 0, Task 0.3 |

### B4 — Landmark thumbnails blank
| | Detail |
|--|--------|
| **File** | All `AsyncImage` usages in explore, landing, stories |
| **Problem** | `LandmarkCard` and `ContinueScanCard` pass nullable values with NO `placeholder`, NO `error`, NO `fallback` |
| **Impact** | Null thumbnail = completely invisible card |
| **Fix Phase** | Phase 1, Tasks 1.1–1.2 |

### B5 — Navigate "Write" from Landing → lands on wrong tab
| | Detail |
|--|--------|
| **File** | `WadjetNavGraph.kt` |
| **Problem** | `onNavigateToWrite = { navController.navigate(Route.Dictionary) }` — lands on BrowseTab (tab 0), not WriteTab (tab 2) |
| **Impact** | User expects to write, sees browse instead |
| **Fix Phase** | Phase 3, Task 3.3 |

---

## 🟠 Major Gaps (G1–G10)

### G1 — No Dictionary/Hieroglyphs in bottom nav
| | Detail |
|--|--------|
| **File** | `TopLevelDestination.kt` — 5 tabs: Home, Scan, Explore, Stories, Profile |
| **Web** | "Hieroglyphs" dropdown always visible in main nav |
| **Impact** | Dictionary only reachable via Landing quick actions |
| **Fix Phase** | Phase 3, Task 3.1 |

### G2 — No Thoth/Chat in bottom nav
| | Detail |
|--|--------|
| **File** | `TopLevelDestination.kt` — PROFILE instead of THOTH |
| **Web** | "Thoth" in main nav bar |
| **Impact** | Chat only reachable from Landing ThothChatEntry card |
| **Fix Phase** | Phase 3, Task 3.1 |

### G3 — Landing "Write" navigates to Dictionary tab 0
| | Detail |
|--|--------|
| **File** | `WadjetNavGraph.kt` |
| **Problem** | No `initialTab` parameter passed to Dictionary route |
| **Fix Phase** | Phase 3, Task 3.3 |

### G4 — LandingScreen has no featured landmarks
| | Detail |
|--|--------|
| **File** | `LandingViewModel.kt` — no ExploreRepository dependency |
| **Web** | Featured landmarks carousel on home page |
| **Fix Phase** | Phase 5, Task 5.2 (or Phase 3 if added to landing) |

### G5 — Identify result only shows match list
| | Detail |
|--|--------|
| **File** | `IdentifyScreen.kt` → `IdentifyResults` composable |
| **Web** | Confidence badge, source indicator, agreement badge, description, is_egyptian warning, "View Details" button |
| **Android** | Only MatchCard with name + confidence |
| **Fix Phase** | Phase 2, Task 2.1 |

### G6 — Story covers are minimalist
| | Detail |
|--|--------|
| **File** | `StoriesScreen.kt` → story card |
| **Problem** | Only a unicode glyph in a plain card. No visual richness |
| **Fix Phase** | Phase 6, Task 6.1 |

### G7 — Camera disabled everywhere
| | Detail |
|--|--------|
| **File** | `feature/scan` and `feature/explore` |
| **Problem** | CameraX code fully commented out (`CAMERA_DISABLED`). Only image upload works |
| **Fix Phase** | Not scheduled (optional future enhancement) |

### G8 — Scan mode selector missing
| | Detail |
|--|--------|
| **Note** | **INTENTIONAL per Smart Defaults design**. No mode selector needed — always auto |
| **Status** | ✅ Resolved by design decision |

### G9 — Scan glyphs not tappable
| | Detail |
|--|--------|
| **File** | `ScanResultScreen.kt` → `GlyphChip` composable |
| **Web** | Tap glyph → detailed info sheet |
| **Fix Phase** | Phase 4, Task 4.4 |

### G10 — Chat `ChatLandmark` slug extraction potentially broken
| | Detail |
|--|--------|
| **File** | `ChatViewModel.kt` init block |
| **Problem** | `savedStateHandle.get<String>("slug")` may not work with type-safe nav — needs `toRoute<Route.ChatLandmark>().slug` |
| **Fix Phase** | Phase 0, Task 0.7 |

---

## 🟡 Minor Issues (M1–M8)

| # | Issue | Fix Phase |
|---|-------|-----------|
| **M1** | Coil `BaseUrlInterceptor` only handles paths starting with `/`. Relative paths without `/` silently fail. | Phase 1, Task 1.3 |
| **M2** | `ScanResultSerializable.toDomain()` always returns `confidenceSummary = null` — loses data on Room reload. | Phase 0, Task 0.6 |
| **M3** | `ContinueScanCard` AsyncImage loads `scan.thumbnailPath` (local file) — no placeholder/error. | Phase 1, Task 1.2 |
| **M4** | Chat `LOCAL_TTS:` fallback only speaks English (`Locale.US`). Should detect language. | Phase 6, Task 6.10 |
| **M5** | Story TTS: `speakChapter()` doesn't pass `voice`/`style` from `ChapterDto` to speak request. | Phase 0, Task 0.8 |
| **M6** | No pull-to-refresh on ExploreScreen, StoriesScreen. | Phase 7, Task 7.9 |
| **M7** | Identify: no "Identify Another" button. Must back-navigate. | Phase 2, Task 2.3 |
| **M8** | Error messages generic: "Identify failed", "Scan failed" — web has contextual messages. | Phase 7, Task 7.7 |

---

## 🔵 Additional Gaps (A1–A12) — Exhaustive Audit

| # | Category | Web Has | Android Status | Fix Phase |
|---|----------|---------|----------------|-----------|
| **A1** | Rate limit handling | Per-endpoint limits + 429 + account lockout | **MISSING** — No 429 handler, no retry-after, no cooldown UI | Phase 0, Task 0.9 |
| **A2** | Email verification | `send-verification`, `verify-email` + branded Resend emails | **MISSING** — No endpoints, no UI | Post-MVP |
| **A3** | Password reset (in-app) | `reset-password` endpoint (token + new password) | **MISSING** — Only forgot-password email, no in-app reset screen | Post-MVP |
| **A4** | Free tier usage display | `GET /api/user/limits` → tier, limits, usage | **PARTIAL** — DTO + endpoint exist, NEVER CALLED. No usage UI | Phase 0, Task 0.11 |
| **A5** | Story progress → REST API | `POST /api/user/progress` → server persistence | **MISSING** — Progress goes to Firestore only, no REST call | Phase 7, Task 7.5 |
| **A6** | Scan history → REST API | Server records scans; `GET /api/user/history` | **PARTIAL** — Room-only save. Dashboard pulls from API | Low priority |
| **A7** | Glyph/Story favorites UI | Favorite landmarks, glyphs, AND stories | **PARTIAL** — Only landmark favoriting. No glyph/story favorite buttons | Phase 7, Tasks 7.4 |
| **A8** | Language toggle (AR) | Cookie-based EN/AR, full `ar.json` i18n | **MISSING** — No Arabic strings.xml, all UI hardcoded | Post-MVP (Phase 6 in web plan) |
| **A9** | Accessibility | N/A (HTML semantics) | **PARTIAL** — Some `contentDescription`, many null/missing | Phase 7, Task 7.12 |
| **A10** | Write mode selector | Web lets user pick, smart is default | **SMART DEFAULT** → Remove selector, always smart | Phase 0, Task 0.10 |
| **A11** | Scan mode selector | Web lets user pick, auto is default | **SMART DEFAULT** → Already auto, no selector built | ✅ Done by design |
| **A12** | Deep links | Web URL routing (all pages addressable) | **MISSING** — No `wadjet://` scheme, no intent-filters | Post-MVP |

---

## Android Feature Audit (22 capabilities)

| # | Feature | Status | Notes |
|---|---------|--------|-------|
| 1 | Auth (register/login/Google) | ✅ PRESENT | Firebase + backend JWT, EncryptedSharedPrefs |
| 2 | Token refresh + mutex | ✅ PRESENT | AuthInterceptor handles 401 |
| 3 | Scan (upload + result) | ✅ PRESENT | mode=auto, full result display |
| 4 | Scan history (Room) | ✅ PRESENT | Local with thumbnails |
| 5 | Identify landmarks | ⚠️ PARTIAL | DTOs wrong (B1-B3), minimal result UI (G5) |
| 6 | Landmark browsing + detail | ✅ PRESENT | Room cache, full detail screen |
| 7 | Dictionary (Browse/Learn/Write/Translate) | ✅ PRESENT | All 4 tabs functional |
| 8 | Write hieroglyphs | ✅ PRESENT | All 3 modes + palette. **Needs**: remove mode selector |
| 9 | Chat/Thoth SSE | ✅ PRESENT | Raw OkHttp EventSource, conversation persistence |
| 10 | Stories + interactions | ✅ PRESENT | All interaction types work |
| 11 | Story scene images | ✅ PRESENT | API call exists |
| 12 | TTS (server + local fallback) | ✅ PRESENT | 204 → LOCAL_TTS: → Android TextToSpeech |
| 13 | STT (server + local) | ✅ PRESENT | Groq Whisper + SpeechRecognizer |
| 14 | Dashboard/Profile | ✅ PRESENT | Stats, history, favorites |
| 15 | Settings | ✅ PRESENT | Name, password, TTS toggle, cache |
| 16 | Feedback | ✅ PRESENT | Bug/suggestion form |
| 17 | Image placeholders | ❌ MISSING | No placeholder/error/fallback on AsyncImage (B4) |
| 18 | Rate limit handling | ❌ MISSING | No 429 handler (A1) |
| 19 | Free tier limits | ❌ MISSING | DTO exists but never called (A4) |
| 20 | Glyph/Story favorites | ❌ MISSING | Only landmark favorites (A7) |
| 21 | Story progress REST sync | ❌ MISSING | Firestore only (A5) |
| 22 | Arabic language | ❌ MISSING | No strings.xml, no toggle (A8) |

---

## Priority Matrix

### Must Fix Before Release (Phases 0–4)
B1, B2, B3, B4, B5, G1, G2, G3, G5, G9, G10, M1, M2, M5, A1, A4, A10

### Should Fix Before Release (Phases 5–7)
G4, G6, M3, M4, M6, M7, M8, A5, A7, A9

### Post-MVP (Nice to Have)
A2, A3, A6, A8, A12, G7 (camera)

---

## 🔴 Newly Discovered Issues (N1–N8)

> Found during final pre-implementation audit on 2026-04-10.

| # | Severity | Issue | Location | Impact | Fix Phase |
|---|----------|-------|----------|--------|----------|
| **N1** | **HIGH** | `RECORD_AUDIO` permission not declared in AndroidManifest.xml — ChatScreen requests it at runtime but manifest lacks it | `app/src/main/AndroidManifest.xml` | STT/voice input will silently fail or crash | Phase 0 (1 line) |
| **N2** | **HIGH** | `Route.DictionarySign(code)` defined in Route.kt but has NO `composable<>` handler in WadjetNavGraph — navigation crashes | `WadjetNavGraph.kt` | Plan task 4.4 "View in Dictionary" button crashes | Phase 0 (add composable) |
| **N3** | **MEDIUM** | TTS settings (enabled, speed) not persisted — `setTtsEnabled()` / `setTtsSpeed()` only update in-memory StateFlow, no DataStore write | `SettingsViewModel.kt` | Settings revert on app restart | Phase 7 |
| **N4** | **MEDIUM** | `UsageDto` only has `scansToday` — missing `chatMessagesToday` for chat limit enforcement | `core/network/model/UserModels.kt` | Task 0.11 chat limit check incomplete | Phase 0 |
| **N5** | **MEDIUM** | Room `exportSchema=false` + `fallbackToDestructiveMigration()` — any schema change wipes all local data | `WadjetDatabase.kt`, `DatabaseModule.kt` | Data loss on future updates | Phase 9 |
| **N6** | **LOW** | `security-crypto:1.1.0-alpha06` is alpha library in production | `libs.versions.toml` | Potential instability | Note only |
| **N7** | **LOW** | Backup rules are empty stubs — encrypted tokens could backup/restore incorrectly | `data_extraction_rules.xml`, `backup_rules.xml` | Auth failures after restore | Phase 9 |
| **N8** | **LOW** | `strings.xml` only has `app_name` — 200+ strings hardcoded in Composables | All feature modules | Blocks future i18n (A8) | Post-MVP |

---

## Corrections from Audit

| # | Original Finding | Actual Status |
|---|-----------------|---------------|
| **M5** | "speakChapter() doesn't pass voice/style" | **ALREADY FIXED** — code passes `voice = chapter.ttsVoice, style = chapter.ttsStyle`. Task 0.8 can be skipped. |
| **G8** | "Scan mode selector missing" | **Intentional** — Smart Defaults design. Resolved. |
