# Stage 1: Existing Context

## Previously Identified Bugs (from 001-logic-parity)

| ID | Title | Status | Evidence |
|----|-------|--------|----------|
| B1 | IdentifyResponse DTO has WRONG field — missing `source`, `agreement`, `description`, `isKnownLandmark`, `isEgyptian` | **FIXED** | Commit `dd007c6` "Simplify identify results and remove provider branding" |
| B2 | IdentifyMatchDto missing `source` field | **FIXED** | Commit `dd007c6` |
| B3 | IdentifyResult domain model too simple — missing ensemble fields | **FIXED** | Commit `dd007c6` |
| B4 | Landmark thumbnails blank — no placeholder/error/fallback on AsyncImage | **FIXED** | Commit `2092c9f` "Phase 1: UX redesign — design system fixes & critical bugs" |
| B5 | Navigate "Write" from Landing → lands on wrong tab | **FIXED** | Commit `559ad6b` "Phase 3: UX redesign — navigation, platform polish & accessibility" |

## Previously Identified Gaps (from 001-logic-parity)

| ID | Title | Status | Evidence |
|----|-------|--------|----------|
| G1 | No Dictionary/Hieroglyphs in bottom nav | **FIXED** | Commit `559ad6b` Phase 3 UX redesign — navigation restructure |
| G2 | No Thoth/Chat in bottom nav | **FIXED** | Commit `559ad6b` |
| G3 | Landing "Write" navigates to Dictionary tab 0 | **FIXED** | Commit `559ad6b` |
| G4 | LandingScreen has no featured landmarks | **UNKNOWN** | No specific commit mentions featured landmarks on landing |
| G5 | Identify result only shows match list — missing confidence badge, source, agreement | **FIXED** | Commit `dd007c6` (simplified identify) + `cdc1a98` Phase 4 UX |
| G6 | Story covers are minimalist | **FIXED** | Commit `ad6caae` Phase 6 logic parity + `cdc1a98` Phase 4 UX |
| G7 | Camera disabled everywhere | **STILL OPEN** | Intentionally deprioritized; only image upload |
| G8 | Scan mode selector missing | **RESOLVED BY DESIGN** | Smart defaults — always auto |
| G9 | Scan glyphs not tappable | **FIXED** | Commit `cdc1a98` Phase 4 UX redesign |
| G10 | Chat `ChatLandmark` slug extraction potentially broken | **LIKELY FIXED** | Commit `559ad6b` Phase 3 navigation polish |

## Previously Identified Minor Issues

| ID | Title | Status |
|----|-------|--------|
| M1 | Coil `BaseUrlInterceptor` only handles paths starting with `/` | **FIXED** | Commit `2092c9f` Phase 1 |
| M2 | `ScanResultSerializable.toDomain()` always returns `confidenceSummary = null` | **LIKELY FIXED** | Commit `ad6caae` Phase 6 |
| M3 | `ContinueScanCard` AsyncImage no placeholder/error | **FIXED** | Commit `2092c9f` |
| M4 | Chat `LOCAL_TTS:` fallback only speaks English | **FIXED** | Commit `674057f` Phase 7-8 |
| M5 | Story TTS: `speakChapter()` doesn't pass voice/style | **ALREADY DONE** (was correct at 001 audit time) |
| M6 | No pull-to-refresh on ExploreScreen, StoriesScreen | **FIXED** | Commit `30cfc2d` Phase 5 cross-cutting quality |
| M7 | Identify: no "Identify Another" button | **FIXED** | Commit `cdc1a98` Phase 4 UX |
| M8 | Error messages generic | **FIXED** | Commit `30cfc2d` Phase 5 + `674057f` Phase 7-8 |

## Previously Identified Additional Gaps

| ID | Title | Status |
|----|-------|--------|
| A1 | Rate limit handling — No 429 handler | **FIXED** | RateLimitInterceptor exists — commit `ad6caae` + earlier |
| A2 | Email verification | **STILL MISSING** | No evidence of implementation |
| A3 | Password reset (in-app) | **STILL MISSING** | No evidence |
| A4 | Free tier usage display | **UNKNOWN** | DTO exists, unclear if wired |
| A5 | Story progress → REST API | **FIXED** | Commit `674057f` Phase 7-8 — progress sync |
| A6 | Scan history → REST API | **PARTIAL** | Room-only save still likely |
| A7 | Glyph/Story favorites UI | **FIXED** | Commit `674057f` Phase 7-8 — favorites |
| A8 | Language toggle (AR) | **PARTIAL** | Commit `6f25306` Phase 2 UX — string extraction & localization infrastructure |
| A9 | Accessibility | **IMPROVED** | Commit `30cfc2d` Phase 5 + `559ad6b` Phase 3 |
| A10 | Write mode selector (remove) | **FIXED** | Smart defaults enforced |
| A11 | Scan mode selector | **RESOLVED BY DESIGN** |
| A12 | Deep links | **STILL MISSING** |

## Git History Summary (all 42 commits, newest first)

| Commit | Description |
|--------|-------------|
| `289e1c7` | Fix pronunciation + add TTS everywhere in Learn + sort stories by difficulty |
| `2450726` | Replace generic difficulty colors with Egyptian pigment palette |
| `9f3f2c5` | Refine TTS voice style: wise sage instead of theatrical priest |
| `35ab68a` | Improve Egyptian pronunciation: academic verification, fix SignDetailSheet TTS gap |
| `3c6adaa` | Add comprehensive Egyptian pronunciation system with ancient voice character |
| `dd007c6` | Simplify identify results and remove provider branding |
| `5490824` | Phase 5: UX redesign — visual polish, transitions & adaptive layout |
| `cdc1a98` | Phase 4: UX redesign — interaction & content UX improvements |
| `559ad6b` | Phase 3: UX redesign — navigation, platform polish & accessibility |
| `6f25306` | Phase 2: UX redesign — string extraction & localization infrastructure |
| `2092c9f` | Phase 1: UX redesign — design system fixes & critical bugs |
| `e563e96` | fix: chat crash — duplicate LazyColumn keys from restored conversations |
| `8c6b44f` | fix: UX round 2 — chat edit, write pronunciation, image auth scope |
| `744db1c` | feat: UX fixes spec 002 — all 4 phases (A/B/C/D) |
| `674057f` | Phase 7-8: Chat polish, favorites, progress sync, TTS persistence, error/empty states, haptics, accessibility |
| `ad6caae` | Phase 6: Logic parity — image loading, favorites, TTS, scan history, translate, categories, landing, chat persistence, write MdC, cache clear |
| `30cfc2d` | Phase 5: Cross-cutting quality — loading/error/empty states, transitions, accessibility, haptics, pull-to-refresh |
| `a603e1c` | Phase 4.8 quality fixes: stories interactions, Groq Whisper STT, past conversations, alphabet tab |
| `193962c` | Phase 4.8: Feature quality verification — logic parity with web app |
| `3936e85` | Phase 4: Feature screen UI redesign (4.1-4.7) |
| `685e156` | Phase 3: Landing & Welcome redesign |
| `729d4b8` | Phase 2: Camera → Image Upload — create ImageUploadZone, refactor ScanScreen/IdentifyScreen |
| `059dadc` | Phase 1: Identity alignment — fix Dust color, replace emojis with hieroglyphs, add animations |
| `4d16a77` | Fix: debug BASE_URL uses production backend |
| `fc3e429` | Fix: cleartext HTTP for emulator, surface Google Sign-In errors |
| `1c2117e` | Adjust for APK distribution (no Play Store) |
| `1991e5b` | P10: Testing, FCM notifications, CI/CD, signing, ProGuard |
| `c68b841` | P9: Offline support, performance, animations, accessibility polish |
| `3a48cc3` | P8: Dashboard, Settings, Feedback |
| `a8400b3` | P7: Stories — list, reader, 4 interaction types, TTS narration, Firestore progress |
| `15d1b4e` | P6: Chat — SSE streaming, TTS, STT, markdown, landmark context |
| `e2e609d` | Quality: suspendRunCatching, ProGuard rules, dispatcher injection |
| `8c96f23` | Fix: Welcome screen feature cards clipping |
| `59f2052` | P5: Landmarks — Explore, detail, identify, favorites, offline cache |
| `1d07e8b` | Quality: NiA patterns + critical bug fixes |
| `b83e9f9` | P4: Scanner — CameraX preview, scan pipeline, results display, history |
| `6f449c1` | Branding: Wadjet launcher icons, adaptive/monochrome, in-app logo |
| `940fd4f` | P3: Dictionary — browse, learn, write tabs, Room FTS cache, sign detail sheet |
| `c08c878` | P2: Enable Google Sign-In |
| `be9e852` | P2: Auth & navigation — Firebase, backend sync, NavGraph, auth sheets |
| `a7ba308` | P1: Design system — theme, colors, typography, components, animations |
| `eb98c3c` | P0: Project setup — 20 modules, Gradle, Firebase |
| `18f4d3f` | P0.0: Download 11 fonts, remove Firebase Storage |
| `627af45` | P0.0: ONNX models, font dir, .gitignore security rules |
| `884e289` | P0.0: Initial Android Studio project + planning docs |

## What's Confirmed Fixed (commit evidence)

- B1-B3 (Identify DTOs) → fixed in `dd007c6`
- B4 (AsyncImage placeholders) → fixed in `2092c9f`
- B5 (Write navigation tab) → fixed in `559ad6b`
- G1-G3 (Navigation restructure) → fixed in `559ad6b`
- G5 (Identify result detail) → fixed in `dd007c6` + `cdc1a98`
- G9 (Tappable glyphs) → fixed in `cdc1a98`
- M1-M3 (Image loading) → fixed in `2092c9f`
- M4 (LOCAL_TTS language) → fixed in `674057f`
- M6-M8 (Pull-to-refresh, error messages) → fixed in `30cfc2d` + `674057f`
- A1 (Rate limit) → RateLimitInterceptor exists
- A7 (Favorites) → fixed in `674057f`
- A10 (Smart defaults) → fixed per design

## What's Still Open

1. **G4** — Featured landmarks on landing — no evidence of implementation
2. **G7** — Camera disabled — intentional, not blocking
3. **A2** — Email verification — not implemented
4. **A3** — Password reset (in-app) — not implemented
5. **A4** — Free tier usage display — unknown if wired
6. **A6** — Scan history REST sync — still Room-only
7. **A8** — Arabic language toggle — infrastructure built (Phase 2 UX), but likely incomplete
8. **A12** — Deep links — not implemented

## Key Pronunciation Commits (relevant to Stage 6)

1. `3c6adaa` — "Add comprehensive Egyptian pronunciation system with ancient voice character"
2. `35ab68a` — "Improve Egyptian pronunciation: academic verification, fix SignDetailSheet TTS gap"
3. `9f3f2c5` — "Refine TTS voice style: wise sage instead of theatrical priest"
4. `289e1c7` — "Fix pronunciation + add TTS everywhere in Learn + sort stories by difficulty"

These 4 commits represent significant work on EgyptianPronunciation.kt — Stage 6 will audit the current state.

## API Contract Summary (48 endpoints)

| # | Method | Path | Category |
|---|--------|------|----------|
| 1 | POST | /api/auth/register | Auth |
| 2 | POST | /api/auth/login | Auth |
| 3 | POST | /api/auth/refresh | Auth |
| 4 | POST | /api/auth/logout | Auth |
| 5 | POST | /api/auth/google | Auth |
| 6 | POST | /api/auth/send-verification | Auth |
| 7 | POST | /api/auth/verify-email | Auth |
| 8 | GET | /api/auth/verify-email | Auth |
| 9 | POST | /api/auth/forgot-password | Auth |
| 10 | POST | /api/auth/reset-password | Auth |
| 11 | POST | /api/scan | Scan |
| 12 | GET | /api/landmarks | Explore |
| 13 | GET | /api/landmarks/categories | Explore |
| 14 | GET | /api/landmarks/{slug} | Explore |
| 15 | GET | /api/landmarks/{slug}/children | Explore |
| 16 | POST | /api/explore/identify | Explore |
| 17 | GET | /api/dictionary | Dictionary |
| 18 | GET | /api/dictionary/categories | Dictionary |
| 19 | GET | /api/dictionary/alphabet | Dictionary |
| 20 | GET | /api/dictionary/lesson/{level} | Dictionary |
| 21 | GET | /api/dictionary/speak | Dictionary |
| 22 | GET | /api/dictionary/{code} | Dictionary |
| 23 | POST | /api/write | Write |
| 24 | GET | /api/write/palette | Write |
| 25 | POST | /api/translate | Translate |
| 26 | POST | /api/stt | Audio |
| 27 | POST | /api/audio/speak | Audio |
| 28 | GET | /api/stories | Stories |
| 29 | GET | /api/stories/{id} | Stories |
| 30 | GET | /api/stories/{id}/chapters/{idx} | Stories |
| 31 | POST | /api/stories/{id}/interact | Stories |
| 32 | POST | /api/stories/{id}/chapters/{idx}/image | Stories |
| 33 | POST | /api/chat | Chat |
| 34 | POST | /api/chat/stream | Chat |
| 35 | POST | /api/chat/clear | Chat |
| 36 | GET | /api/user/profile | User |
| 37 | PATCH | /api/user/profile | User |
| 38 | PATCH | /api/user/password | User |
| 39 | GET | /api/user/history | User |
| 40 | GET | /api/user/favorites | User |
| 41 | POST | /api/user/favorites | User |
| 42 | DELETE | /api/user/favorites/{type}/{id} | User |
| 43 | GET | /api/user/stats | User |
| 44 | GET | /api/user/progress | User |
| 45 | POST | /api/user/progress | User |
| 46 | GET | /api/user/limits | User |
| 47 | POST | /api/feedback | Feedback |
| 48 | GET | /api/feedback | Feedback (Admin) |
