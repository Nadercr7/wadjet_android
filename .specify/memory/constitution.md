# Wadjet Android Constitution

> Governing principles for the Wadjet Android app — Egyptian civilization explorer.
> This document is the **source of truth** for all development decisions.

## Core Principles

### 1. Smart Defaults — Zero User Friction
The user **NEVER** chooses technical modes. The app always picks the smartest, most realistic option automatically:
- **Scan**: Always `mode=auto` — backend runs parallel AI+ONNX with 4-level fallback chain
- **Write**: Always `mode=smart` — AI translates English → MdC → hieroglyphs with 90+ phrase shortcuts
- **TTS**: Server picks best provider (Gemini → Groq PlayAI → HTTP 204 → Android TextToSpeech)
- **Identify**: Already auto — ONNX + Gemini parallel → Grok tiebreak → `merge_landmark()` ensemble
- **No voice pickers, no mode selectors, no provider choosers.** The app is smart for the user.

### 2. Web Parity — Same Quality, Same Logic
Every feature must produce **identical results** to the web app (`Wadjet-v3-beta`):
- Same API endpoints, same request/response contracts
- Same fallback chains and error recovery
- Same UI information density (all metadata visible)
- Same feature accessibility (every feature reachable in ≤2 taps)

### 3. Offline First — Egyptian Knowledge Always Available
- Dictionary: Full Gardiner sign list in Room DB with FTS5 search — works offline
- Landmarks: Room cache with offline browsing
- Scan history: Saved locally with thumbnails
- Network-dependent features degrade gracefully with contextual error messages

### 4. Clean Architecture — Strict Module Boundaries
```
app (navigation + DI wiring)
  ↓ depends on
feature/* (UI + ViewModels — Compose + StateFlow)
  ↓ depends on
core/domain (models + repository interfaces)
  ↓ depends on
core/data (repository implementations)
  ↓ depends on
core/network (Retrofit services + DTOs) + core/database (Room entities + DAOs)
core/designsystem (theme + shared composables)
core/common (utilities + network monitor)
core/firebase (auth + Firestore + messaging)
```
- Features NEVER depend on other features
- Domain NEVER depends on framework classes (Android, Retrofit, Room)
- DTOs live in `core/network`, domain models in `core/domain`, mapping in `core/data`

### 5. Egyptian Design Language — Dark & Gold
- Dark-only theme (Black & Gold brand) — no light mode
- NotoSansEgyptianHieroglyphs font for glyph rendering
- Egyptian-themed placeholders (pyramid silhouettes, Eye of Horus, papyrus scrolls)
- Contextual error messages with Egyptian flavor ("The ancient scribes couldn't read this image")

### 6. Security — Production-Grade from Day One
- Encrypted token storage (EncryptedSharedPreferences + AES256-GCM)
- JWT access (30min) + rotating refresh token (7 days)
- Rate limit awareness (429 handler with retry-after)
- No secrets in code, no hardcoded API keys (BuildConfig only)

## Development Rules

### Code Style
- Kotlin with Jetpack Compose (no XML layouts, no Fragments)
- kotlinx.serialization for JSON (not Gson)
- Hilt for DI (not manual, not Koin)
- Coil 3 for image loading (not Glide, not Picasso)
- StateFlow for UI state (not LiveData)
- Type-safe navigation (Compose Navigation 2.8+)

### Testing Contract
- Every DTO must match the web API response exactly — cross-checked against actual endpoint code
- Every AsyncImage must have placeholder + error + fallback drawables
- Every ViewModel must handle: loading, success, error, empty states
- Every network call must handle: success, 401 (auto-refresh), 429 (rate limit), timeout, no network

### What NOT to Do
- ❌ Don't add mode selection UI (scan modes, write modes, TTS voice pickers)
- ❌ Don't add light theme
- ❌ Don't use Fragments or XML layouts
- ❌ Don't hardcode strings in Composables for user-facing text (use string resources)
- ❌ Don't create new modules unless absolutely necessary (prefer adding to existing feature modules)
- ❌ Don't add features not in the web app (keep parity, don't invent)

## Governance

This constitution was created from a deep code-level analysis of both the Android app and the web backend on 2026-04-10. It may be amended when:
1. The web app adds fundamental new features
2. A core design decision proves wrong after real user testing
3. Android platform requirements change (e.g., new Compose APIs)

Version: 1.0 | Ratified: 2026-04-10 | Last Amended: 2026-04-10
