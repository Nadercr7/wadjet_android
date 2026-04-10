# Wadjet Android — Architecture Reference

> Complete technical architecture of the Android app and its relationship to the web backend.
> All information verified by reading actual source code on 2026-04-10.

## Module Dependency Graph

```
┌─────────────────────────────────────────────────────┐
│                    app (navigation)                  │
│  TopLevelDestination · WadjetNavHost · Route · DI    │
└────────────┬────────────────────────────┬────────────┘
             │ depends on                  │
┌────────────▼────────────┐  ┌────────────▼────────────┐
│      feature/*          │  │      feature/*           │
│  auth · landing · scan  │  │  chat · stories · dash   │
│  explore · dictionary   │  │  settings · feedback     │
│  (UI + ViewModels)      │  │  (UI + ViewModels)       │
└────────────┬────────────┘  └────────────┬────────────┘
             │ depends on                  │
┌────────────▼─────────────────────────────▼───────────┐
│              core/domain                              │
│  Models: Landmark, ScanResult, Story, User, etc.     │
│  Interfaces: ExploreRepository, ScanRepository, etc. │
└────────────┬─────────────────────────────────────────┘
             │ depends on
┌────────────▼─────────────────────────────────────────┐
│              core/data                                │
│  Repository implementations (network + cache logic)  │
│  DTO → Domain mapping                                │
└──┬─────────┬──────────┬──────────┬───────────────────┘
   │         │          │          │
┌──▼──┐  ┌──▼───┐  ┌───▼──┐  ┌───▼──────────┐
│net  │  │db    │  │fire  │  │common        │
│work │  │base  │  │base  │  │NetworkMonitor│
└─────┘  └──────┘  └──────┘  └──────────────┘

core/designsystem — Theme, colors, shared composables (used by all features)
core/ui — Shared UI components
core/ml — (exists but currently unused on client — ONNX runs server-side)
```

## Module Inventory (18 modules)

| Module | Path | Purpose |
|--------|------|---------|
| `app` | `app/` | Entry point, navigation, Hilt setup, Firebase init |
| `feature:auth` | `feature/auth/` | Login, register, Google Sign-In, forgot password |
| `feature:landing` | `feature/landing/` | Home screen with quick actions, resume cards |
| `feature:scan` | `feature/scan/` | Hieroglyph scanning (upload, result, history) |
| `feature:explore` | `feature/explore/` | Landmark browsing, detail, identify |
| `feature:dictionary` | `feature/dictionary/` | Browse, Learn, Write, Translate (4 tabs) |
| `feature:chat` | `feature/chat/` | Thoth AI chatbot (SSE streaming) |
| `feature:stories` | `feature/stories/` | Story list, reader, interactions |
| `feature:dashboard` | `feature/dashboard/` | User profile, stats, history, favorites |
| `feature:settings` | `feature/settings/` | Name, password, TTS toggle, cache |
| `feature:feedback` | `feature/feedback/` | Bug/suggestion submission |
| `core:network` | `core/network/` | Retrofit services (11), DTOs, AuthInterceptor |
| `core:database` | `core/database/` | Room DB (v3), 4 tables, DAOs, FTS5 |
| `core:domain` | `core/domain/` | Domain models, repository interfaces |
| `core:data` | `core/data/` | Repository implementations |
| `core:designsystem` | `core/designsystem/` | WadjetTheme, colors, typography |
| `core:common` | `core/common/` | NetworkMonitor, utilities |
| `core:firebase` | `core/firebase/` | FirebaseAuth, Firestore, Messaging |
| `core:ui` | `core/ui/` | Shared composable components |
| `core:ml` | `core/ml/` | (Placeholder — ONNX runs server-side) |

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Kotlin | 2.0+ |
| UI | Jetpack Compose | BOM latest |
| DI | Hilt | 2.51+ |
| Navigation | Compose Navigation | 2.8+ (type-safe routes) |
| Network | Retrofit 2 + OkHttp 4 | latest |
| JSON | kotlinx.serialization | 1.6+ |
| Images | Coil 3 | 3.0.4 |
| Database | Room | 2.6+ |
| Auth | Firebase Auth + custom JWT backend | |
| Cloud | Firebase Firestore + Messaging + Crashlytics + Analytics | |
| Markdown | compose-markdowntext | |
| Camera | CameraX | (currently disabled) |
| Encrypted Storage | EncryptedSharedPreferences | AES256-GCM |

## Backend Architecture (Web App)

**Server**: FastAPI (Python) on Hugging Face Spaces
**URL**: `https://nadercr7-wadjet-v2.hf.space`

### AI Pipeline Architecture

```
                    ┌──────────────┐
                    │  User Upload │
                    └──────┬───────┘
                           │
              ┌────────────▼────────────┐
              │    POST /api/scan       │
              │    mode=auto (default)  │
              └─────┬──────────┬────────┘
                    │          │
         ┌──────────▼──┐  ┌───▼──────────┐
         │ AI Reader   │  │  ONNX        │
         │ (Gemini→    │  │  YOLOv8s     │  ← parallel
         │  Groq→Grok) │  │  detect →    │
         │             │  │  MobileNetV3  │
         └──────┬──────┘  │  classify    │
                │         └───┬──────────┘
                │             │
         ┌──────▼─────────────▼──────┐
         │    Merge & Verify         │
         │  AI success → merge with  │
         │    ONNX bboxes            │
         │  AI fail → fresh AI read  │
         │    (Gemini→Groq→Grok)     │
         │  Still fail → sequence    │
         │    verify (Gemini+Grok)   │
         │  Last resort → AI read    │
         └────────────┬──────────────┘
                      │
              ┌───────▼────────┐
              │ Transliterate  │
              │ + Translate    │
              │ (RAG: FAISS +  │
              │  Gemini embed) │
              └───────┬────────┘
                      │
              ┌───────▼────────┐
              │ JSON Response  │
              │ glyphs[]       │
              │ annotated_img  │
              │ translations   │
              │ confidence     │
              │ ai_reading     │
              └────────────────┘
```

### Landmark Identify Pipeline

```
              ┌──────────────┐
              │  User Photo  │
              └──────┬───────┘
                     │
        ┌────────────▼────────────┐
        │ POST /api/explore/identify│
        └─────┬──────────┬────────┘
              │          │
    ┌─────────▼───┐  ┌──▼──────────┐
    │ ONNX        │  │ Gemini      │
    │ EfficientB0 │  │ Vision      │  ← parallel
    │ classifier  │  │             │
    └──────┬──────┘  └──────┬──────┘
           │                │
    ┌──────▼────────────────▼──────┐
    │     merge_landmark()         │
    │ Case 1: Agree → 1.15x boost │
    │ Case 2: Partial match        │
    │ Case 3: Grok tiebreak        │
    │ Case 4: Highest confidence   │
    └──────────────┬───────────────┘
                   │
    ┌──────────────▼───────────────┐
    │ Slug resolve + fuzzy match   │
    │ → full landmark data         │
    └──────────────┬───────────────┘
                   │
    ┌──────────────▼───────────────┐
    │ {slug, name, confidence,     │
    │  source, agreement,          │
    │  description,                │
    │  is_known_landmark,          │
    │  is_egyptian, top3[]}        │
    └──────────────────────────────┘
```

### TTS Fallback Chain

```
Client requests audio
        │
        ▼
POST /api/audio/speak
  {text, lang, context, voice?, style?}
        │
  ┌─────▼─────────────┐
  │ Gemini 2.5 Flash   │ ← 10 voice presets per context
  │ TTS (17 key rotate)│   Orus=Thoth, Aoede=stories,
  └──┬────────────┬────┘   Charon=default, etc.
     │ success    │ fail
     ▼            ▼
  audio/wav    ┌──────────────┐
  response     │ Groq PlayAI  │
               └──┬───────┬───┘
                  │       │ fail
                  ▼       ▼
              audio    HTTP 204
              response (no content)
                         │
                    ┌────▼────────────────┐
                    │ Android client sees  │
                    │ 204 → falls back to  │
                    │ LOCAL_TTS: signal →  │
                    │ Android TextToSpeech │
                    └─────────────────────┘
```

## Room Database Schema (v3)

| Table | Purpose | Key Columns |
|-------|---------|-------------|
| `signs` | Gardiner sign list (1000+) | gardiner_code (PK), unicode, category, type, phonetic_value, transliteration, meaning |
| `signs_fts` | FTS5 virtual table for offline search | phonetic_value, transliteration, meaning |
| `scan_results` | Scan history with results | id (PK), timestamp, thumbnailPath, resultJson, mode |
| `landmarks` | Cached landmark data | slug (PK), name, category, city, thumbnail, detailJson, lastUpdated |

## Navigation Architecture

### Current Bottom Nav (5 tabs)
```
HOME(Landing) | SCAN | EXPLORE | STORIES | PROFILE(Dashboard)
```

### Target Bottom Nav (per web parity)
```
HOME(Landing) | HIEROGLYPHS(Hub) | EXPLORE | STORIES | THOTH(Chat)
Dashboard → user avatar in top app bar
```

### Route Graph
```
Landing ──┬── Scan ──── ScanResult
          ├── Dictionary (4 tabs: Browse, Learn, Write, Translate)
          ├── DictionarySign(code)
          ├── Explore ──┬── LandmarkDetail(slug)
          │             └── Identify ── IdentifyResult
          ├── Stories ──── StoryReader(id)
          ├── Chat
          ├── ChatLandmark(slug)
          ├── Dashboard
          ├── Settings
          └── Feedback
```

## Network Services (11 Retrofit interfaces)

| Service | File | Key Endpoints |
|---------|------|---------------|
| `AuthApiService` | `core/network/api/` | register, login, google, refresh, logout, forgot-password |
| `ScanApiService` | | POST /api/scan (multipart: image + mode) |
| `ExploreApiService` | | GET /api/landmarks, GET /api/landmarks/{slug}, POST /api/explore/identify |
| `DictionaryApiService` | | GET /api/dictionary, categories, alphabet, lesson/{n}, speak |
| `WriteApiService` | | POST /api/write, GET /api/write/palette |
| `TranslateApiService` | | POST /api/translate |
| `ChatApiService` | | (SSE handled by raw OkHttp, not Retrofit) |
| `StoriesApiService` | | GET /api/stories, GET /api/stories/{id}, POST interact, POST chapters/{n}/image |
| `AudioApiService` | | POST /api/audio/speak, POST /api/stt |
| `UserApiService` | | GET/PATCH profile, GET/PATCH password, history, favorites, stats, progress, limits |
| `FeedbackApiService` | | POST /api/feedback |

## Auth Flow

```
Register/Login/Google
        │
        ▼
Backend returns: { access_token (30min JWT), refresh_token (7d) }
        │
  ┌─────▼──────────────┐
  │ Store encrypted in  │
  │ EncryptedSharedPrefs│
  │ (AES256-GCM)        │
  └─────┬──────────────┘
        │
  On every request: AuthInterceptor adds Bearer token
        │
  On 401: mutex-locked token refresh → retry original request
        │
  On refresh fail: clear tokens → redirect to Login
```
