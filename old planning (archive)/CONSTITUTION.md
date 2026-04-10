# Wadjet Android — Project Constitution

> This is the single source of truth for the Wadjet Android app.
> Read this file FIRST before making any changes. No exceptions.

---

## Project Identity

**Wadjet Android** is the native Android companion to the Wadjet v3 web app — an AI-powered Egyptian heritage platform. The Android app MUST deliver the same features, the same identity, and the same experience as the web version, adapted for mobile-native patterns.

**One-liner**: Scan hieroglyphs, translate inscriptions, explore landmarks, read stories, learn from Thoth — natively on Android.

**Developer**: Nader (Mr Robot)
**Package name**: `com.wadjet.app`
**Min SDK**: 26 (Android 8.0 Oreo) — covers 95%+ of active devices
**Target SDK**: 35 (Android 15)

---

## Architecture Decision: Standalone vs API Client

### Decision: HYBRID — Firebase + Wadjet API

The Android app uses **Firebase** as its primary backend for:
- **Authentication** (Firebase Auth — Google, Email/Password)
- **Database** (Cloud Firestore — user data, favorites, progress, history)
- **Storage** (Firebase Storage — cached images, audio)
- **Analytics** (Firebase Analytics + Crashlytics)
- **Push notifications** (Firebase Cloud Messaging)

The app calls the **existing Wadjet v3 API** for:
- **AI features**: Scan, Translate, Chat (streaming), Stories interaction, TTS, STT
- **Data**: Landmarks, Dictionary, Stories content
- **Image generation**: Story scene images (Cloudflare FLUX/SDXL via backend)

**Rationale**: AI inference runs server-side (ONNX models + multi-provider AI rotation). Replicating that on-device is impractical. Firebase handles user-facing CRUD operations with offline sync, while the Wadjet API handles all AI/ML workloads.

---

## Tech Stack (LOCKED — No Substitutions)

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Kotlin | 2.1.0 |
| UI Framework | Jetpack Compose | Material 3 |
| Architecture | MVVM + Clean Architecture | — |
| DI | Hilt (Dagger) | 2.53.1 |
| Navigation | Compose Navigation | Type-safe |
| Networking | Retrofit + OkHttp | — |
| Serialization | Kotlinx Serialization | — |
| Image Loading | Coil 3 | Compose-native |
| Auth | Firebase Auth | — |
| Database | Cloud Firestore | — |
| Local Cache | Room (offline cache) | — |
| ML (on-device) | ONNX Runtime Android | 1.20.0 |
| Camera | CameraX | — |
| TTS | Server TTS → Android TTS fallback | — |
| STT | SpeechRecognizer | — |
| Maps | Google Maps Compose | — |
| Analytics | Firebase Analytics + Crashlytics | — |
| Async | Kotlin Coroutines + Flow | — |
| Build | Gradle (Kotlin DSL) | 8.x |

---

## Design System — Black & Gold (NON-NEGOTIABLE)

The Android app MUST use the **exact same** Black & Gold design system as the web app. This is the Wadjet brand identity. No Material You dynamic colors. No system theme override.

### Colors (Exact Hex Values)
| Token | Value | Usage |
|-------|-------|-------|
| `Night` | `#0A0A0A` | App background, status bar, nav bar |
| `Surface` | `#141414` | Card backgrounds, bottom sheets |
| `SurfaceAlt` | `#1E1E1E` | Elevated surface |
| `SurfaceHover` | `#252525` | Pressed/selected states |
| `Gold` | `#D4AF37` | Primary accent, CTAs, FABs |
| `GoldLight` | `#E5C76B` | Hover/pressed states |
| `GoldDark` | `#B8962E` | Active/focused states |
| `GoldMuted` | `#A08520` | Disabled gold |
| `Border` | `#2A2A2A` | Card borders, dividers |
| `BorderLight` | `#3A3A3A` | Elevated borders |
| `Text` | `#F0F0F0` | Primary text |
| `TextMuted` | `#8A8A8A` | Secondary text |
| `Ivory` | `#F5F0E8` | Emphasized text on dark |
| `Sand` | `#C4A265` | Muted accent text |
| `Dust` | `#A89070` | Disabled/tertiary text |
| `Success` | `#4CAF50` | Success states |
| `Error` | `#EF4444` | Error states |
| `Warning` | `#F59E0B` | Warning states |

### Typography
| Style | Font | Weight | Usage |
|-------|------|--------|-------|
| Display | Playfair Display | 700 | Hero text, feature titles |
| Headline | Playfair Display | 600 | Section headers, screen titles |
| Title | Inter | 600 | Card titles, list headers |
| Body | Inter | 400 | Body text, descriptions |
| Label | Inter | 500 | Buttons, chips, labels |
| Mono | JetBrains Mono | 400 | Gardiner codes, MdC |
| Hieroglyph | Noto Sans Egyptian Hieroglyphs | 400 | Hieroglyph Unicode display |

### Icons
- **Lucide** icon set (same as web) — use `lucide-android` or inline SVG via Compose
- Gold-tinted icons on dark backgrounds

---

## Feature Parity Requirements

The Android app MUST implement ALL features from the web app:

### Hieroglyphs Path
1. **Scan** — Camera capture + gallery upload → ONNX detection → classification → transliteration → translation
2. **Dictionary** — Browse 1,000+ Gardiner signs, search, filter by category/type, sign detail view
3. **Lessons** — 5 progressive hieroglyph lessons with interactive exercises
4. **Write** — Text → hieroglyphs (alpha/MdC/smart modes), glyph palette

### Landmarks Path
5. **Explore** — Browse 260+ landmarks, filter by category/city, search, detail views with images/sections
6. **Identify** — Camera/gallery → ONNX landmark classifier → top-3 results → detail view
7. **Recommendations** — Tag/era/proximity-based landmark recommendations

### Shared Features
8. **Thoth Chat** — Streaming AI chatbot with context, SSE parsing, STT input, TTS output
9. **Stories** — 12 interactive stories, 4 interaction types, AI-generated scene images, TTS narration
10. **TTS** — Server TTS (Gemini voices) → Android TTS fallback, context-based voice selection
11. **STT** — Device SpeechRecognizer for chat input
12. **Auth** — Google Sign-In + Email/Password, token management
13. **Dashboard** — User stats, scan history, favorites, story progress
14. **Settings** — Profile, language, password (email users only), about
15. **Bilingual** — Full EN + AR support, RTL layout for Arabic
16. **Feedback** — In-app feedback form

---

## Non-Negotiable Rules

1. **Black & Gold ONLY** — Never use Material You dynamic colors, system themes, or any non-brand colors
2. **Same API** — Use the existing Wadjet v3 API for all AI/ML operations
3. **Offline-first for data** — Landmarks, dictionary, stories content cached locally via Room
4. **Firebase for user data** — Auth, favorites, progress, history synced via Firestore
5. **ONNX on-device** — Hieroglyph detection + classification + landmark identification run locally
6. **Arabic RTL** — Full RTL layout support when user selects Arabic
7. **"Built by Mr Robot"** — Footer/about attribution. Never change.
8. **No ads** — This is a portfolio/educational project
9. **Min 44dp touch targets** — All interactive elements per Material guidelines
10. **Secure token storage** — EncryptedSharedPreferences for all tokens/secrets

---

## Backend Relationship

| Operation | Source | Why |
|-----------|--------|-----|
| Auth (Google) | Firebase Auth | Native SDK, no cookie management |
| Auth (Email) | Firebase Auth | Better mobile UX than custom JWT |
| User profile | Firestore | Offline sync, real-time updates |
| Favorites | Firestore | Instant sync across devices |
| Scan history | Firestore + Room | Cloud backup + fast local access |
| Story progress | Firestore | Cross-device continuity |
| Hieroglyph scan | Wadjet API `/api/scan` | Server-side ONNX + AI verification |
| Translation | Wadjet API `/api/translate` | RAG + multi-provider AI |
| Dictionary data | Wadjet API `/api/dictionary` → Room cache | Static data, cache aggressively |
| Landmark data | Wadjet API `/api/landmarks` → Room cache | Static data, cache aggressively |
| Landmark identify | Wadjet API `/api/explore/identify` | Server-side ONNX model |
| Chat | Wadjet API `/api/chat/stream` | SSE streaming, server-side LLM |
| Stories content | Wadjet API `/api/stories` → Room cache | Static data, cache aggressively |
| Story interaction | Wadjet API `/api/stories/{id}/interact` | Server-side validation |
| Story images | Wadjet API `/api/stories/{id}/chapters/{n}/image` | Cloudflare AI generation |
| TTS | Wadjet API `/api/audio/speak` → Android TTS | Server voices → device fallback |
| STT | Android SpeechRecognizer | No server dependency |
| Feedback | Wadjet API `/api/feedback` | Direct server storage |

---

## Quality Gates

Before any release:
- [ ] All screens match web app design (Black & Gold)
- [ ] AR/RTL layout tested on all screens
- [ ] Offline mode tested (airplane mode)
- [ ] Camera permissions handled gracefully
- [ ] All API error states have UI feedback
- [ ] Firebase Auth flow complete (Google + Email)
- [ ] ONNX models load and run correctly
- [ ] TTS plays on real device
- [ ] Memory profiled — no leaks on navigation
- [ ] ProGuard/R8 rules verified — no runtime crashes
- [ ] Min SDK 26 tested on emulator
- [ ] Target SDK 35 tested on physical device
