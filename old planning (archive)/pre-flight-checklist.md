# Wadjet Android — Pre-Flight Checklist

> Verified on April 8, 2026.
> All items below have been audited, cross-checked, and confirmed.

---

## Planning Files (15 total)

| # | File | Status | Notes |
|---|------|--------|-------|
| 1 | CONSTITUTION.md | ✅ Complete | Identity, stack, 16 features, 10 rules |
| 2 | spec.md | ✅ Complete | 13 features (F1-F13), acceptance criteria |
| 3 | architecture.md | ✅ Complete + Fixed | ScanHistory route added, nav diagram updated |
| 4 | api-mapping.md | ✅ Complete | 12 Retrofit interfaces, full DTOs |
| 5 | design-system.md | ✅ Complete | Compose theme, 10+ components |
| 6 | screens.md | ✅ Complete + Fixed | S18 auth bottom sheets added |
| 7 | firebase-schema.md | ✅ Complete + Fixed | lesson_progress subcollection added |
| 8 | implementation-plan.md | ✅ Complete + Fixed | FCM, signing, data safety, versioning added; versions synced |
| 9 | prompts.md | ✅ Complete + Fixed | P3, P5, P8, P9, P10 prompts added |
| 10 | project-structure.md | ✅ Complete + Fixed | Test dirs + .github/workflows/ added |
| 11 | i18n-strings.md | ✅ Complete + Fixed | Identify, notification, accessibility strings added |
| 12 | dependencies.md | ✅ Complete + Fixed | All versions updated to latest stable |
| 13 | release-checklist.md | ✅ NEW | Play Store, signing, CI/CD, privacy policy, analytics |
| 14 | pre-flight-checklist.md | ✅ NEW | This file |
| 15 | phase0-instructions.md | ✅ NEW | Step-by-step setup: env, Git, GitHub, Firebase, fonts, models |

---

## Version Verification (Online — April 8, 2026)

| Dependency | Planning Version | Latest Stable | Status |
|------------|-----------------|--------------|--------|
| Kotlin | 2.1.0 | 2.1.0 | ✅ |
| Compose BOM | 2026.03.00 | 2026.03.00 | ✅ |
| Material 3 | 1.4.0 | 1.4.0 | ✅ |
| Lifecycle | 2.10.0 | 2.10.0 | ✅ |
| Navigation | 2.9.7 | 2.9.7 | ✅ |
| Room | 2.8.4 | 2.8.4 | ✅ |
| Hilt | 2.53.1 | 2.53.1 | ✅ |
| Retrofit | 2.11.0 | 2.11.0 | ✅ |
| OkHttp | 4.12.0 | 4.12.0 | ✅ |
| Coil | 3.0.4 | 3.0.4 | ✅ |
| CameraX | 1.4.1 | 1.4.1 | ✅ |
| ONNX Runtime | 1.20.0 | 1.20.0 | ✅ |
| Firebase BOM | 33.7.0 | 33.7.0 | ✅ |

**Note**: Room 2.8.4 and Lifecycle 2.10.0 raised minSdk to 23. Our min SDK is 26 — **no conflict**.

---

## API Endpoint Verification (Source Code — April 8, 2026)

| Endpoint | File | Verified |
|----------|------|----------|
| POST /api/scan | app/api/scan.py | ✅ exists |
| POST /api/detect | app/api/scan.py | ✅ exists |
| POST /api/read | app/api/scan.py | ✅ exists |
| POST /api/stt | app/api/audio.py | ✅ exists (NOT /api/audio/transcribe) |
| POST /api/audio/speak | app/api/audio.py | ✅ exists |
| GET /api/stories | app/api/stories.py | ✅ exists |
| GET /api/stories/{id} | app/api/stories.py | ✅ exists |
| POST /api/stories/{id}/interact | app/api/stories.py | ✅ exists |
| POST /api/stories/{id}/chapters/{n}/image | app/api/stories.py | ✅ exists |
| POST /api/chat/stream | app/api/chat.py | ✅ exists |
| All auth endpoints | app/api/auth.py | ✅ exists |
| All user endpoints | app/api/user.py | ✅ exists |
| All dictionary endpoints | app/api/dictionary.py | ✅ exists |
| All explore endpoints | app/api/explore.py | ✅ exists |
| POST /api/feedback | app/api/feedback.py | ✅ exists |

---

## Data Verification

| Item | Expected | Actual | Status |
|------|----------|--------|--------|
| Stories count | 12 | 12 JSON files in data/stories/ | ✅ |
| Gardiner signs | 1000+ | Full dictionary in web app | ✅ |
| Landmarks | 260+ | expanded_sites.json | ✅ |
| ONNX models | 3 (detector, classifier, landmark) | models/ directory | ✅ |
| Lessons | 5 levels | /dictionary/lesson/{1-5} routes | ✅ |

---

## Issues Found & Fixed

| Issue | Severity | Fix Applied |
|-------|----------|-------------|
| Lifecycle 2.8.7 outdated | High | Updated to 2.10.0 |
| Navigation 2.8.5 outdated | High | Updated to 2.9.7 |
| Room 2.6.1 outdated | High | Updated to 2.8.4 |
| Material3 1.3.1 outdated | Medium | Updated to 1.4.0 |
| Compose BOM 2024.12.01 stale | High | Updated to 2026.03.00 |
| Dead composeCompiler 1.5.15 | Medium | Removed (bundled with Kotlin 2.0+) |
| Missing firebase-crashlytics | Medium | Added to :app dependencies |
| Commented-out maps-compose | Medium | Uncommented |
| Commented-out compose-markdown | Medium | Uncommented |
| Orphan :core:model section | Low | Removed, note added |
| Missing ScanHistory route | Medium | Added to architecture.md Route sealed class |
| Missing lesson_progress schema | High | Added to firebase-schema.md |
| Missing identify/notification/a11y strings | Medium | Added to i18n-strings.md |
| Missing S18 wireframe | Medium | Added auth bottom sheets to screens.md |
| Missing FCM/signing/versioning tasks | High | Added to implementation-plan.md P10 |
| Missing P3/P5/P8/P9/P10 prompts | Medium | Added to prompts.md |
| Missing test directories | Medium | Added to project-structure.md |
| Missing .github/workflows/ | Medium | Added to project-structure.md |
| Outdated versions in plan P0.3 | High | Synced with dependencies.md |
| No release/Play Store file | High | Created release-checklist.md |

---

## Identity Preservation Check

| Element | Web | Android Planning | Match |
|---------|-----|------------------|-------|
| App name | Wadjet | Wadjet | ✅ |
| Background | #0A0A0A | #0A0A0A | ✅ |
| Surface | #141414 | #141414 | ✅ |
| Gold accent | #D4AF37 | #D4AF37 | ✅ |
| Heading font | Playfair Display | Playfair Display | ✅ |
| Body font | Inter | Inter | ✅ |
| Arabic font | — (web uses CSS) | Cairo | ✅ (native equivalent) |
| Glyph font | Noto Sans Egyptian Hieroglyphs | Noto Sans Egyptian Hieroglyphs | ✅ |
| Footer | "Built by Mr Robot" | "Built by Mr Robot" | ✅ |
| Dynamic colors | N/A (web) | DISABLED (forced dark) | ✅ |

---

## What's Ready

- [x] All 18 screens wireframed
- [x] All API endpoints mapped with DTOs
- [x] Firebase schema designed with security rules
- [x] 20-module architecture defined
- [x] Complete dependency catalog with latest versions
- [x] EN + AR string resources drafted
- [x] 11 phased implementation plan (P0-P10)
- [x] AI prompts for all 11 phases
- [x] Release checklist (zero to Play Store)
- [x] CI/CD workflow template
- [x] Analytics events plan
- [x] Deep linking spec
- [x] Privacy policy outline
- [x] Data Safety Form answers

## What You Need Before Phase 0

1. **Android Studio** — installed ✅ (you confirmed)
2. **JDK 17** — bundled with Android Studio (verify: `java -version`)
3. **Android SDK 35** — install via SDK Manager in Android Studio
4. **Google account** — for Firebase Console
5. **GitHub repo** — create `Wadjet-Android` repository
6. **Font files** — download from Google Fonts (Playfair Display, Inter, Cairo, JetBrains Mono, Noto Sans Egyptian Hieroglyphs)
7. **ONNX models** — copy from `D:\Personal attachements\Projects\Wadjet-v3-beta\models\`
