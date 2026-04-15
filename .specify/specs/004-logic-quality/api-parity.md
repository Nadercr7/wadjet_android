# API Parity Report

**Date**: 2026-04-15
**Sources**: Stage 2 (Network Layer), Stage 11 (Web Parity & API Surface)
**Methodology**: Zero-hallucination — every finding traced to actual Retrofit annotations, DTO definitions, and repository implementations read from source.

---

## Endpoint Census

### Total: 48 contract endpoints → 40 implemented (39 Retrofit + 1 OkHttp SSE), 8 missing

| # | Endpoint | Method | Service | Retrofit Function | Status | Notes |
|---|----------|--------|---------|-------------------|--------|-------|
| 1 | `/api/auth/register` | POST | AuthApiService | `register()` | ✅ | |
| 2 | `/api/auth/login` | POST | AuthApiService | `login()` | ✅ | |
| 3 | `/api/auth/refresh` | POST | AuthApiService | `refresh()` | ✅ | |
| 4 | `/api/auth/logout` | POST | AuthApiService | `logout()` | ✅ | |
| 5 | `/api/auth/google` | POST | AuthApiService | `googleAuth()` | ✅ | |
| 6 | `/api/auth/send-verification` | POST | — | — | ❌ MISSING | Email verification flow broken |
| 7 | `/api/auth/verify-email` | POST | — | — | ❌ MISSING | Email verification flow broken |
| 8 | `/api/auth/verify-email` | GET | — | — | ❌ MISSING | Token-based verification broken |
| 9 | `/api/auth/forgot-password` | POST | AuthApiService | `forgotPassword()` | ✅ | |
| 10 | `/api/auth/reset-password` | POST | — | — | ❌ MISSING | Password reset dead-end |
| 11 | `/api/scan` | POST | ScanApiService | `scan()` | ✅ ⚠️ | Leading `/` in path |
| 12 | `/api/landmarks` | GET | LandmarkApiService | `getLandmarks()` | ✅ | Paginated |
| 13 | `/api/landmarks/categories` | GET | LandmarkApiService | `getCategories()` | ✅ | |
| 14 | `/api/landmarks/{slug}` | GET | LandmarkApiService | `getLandmarkDetail()` | ✅ | |
| 15 | `/api/landmarks/{slug}/children` | GET | LandmarkApiService | `getLandmarkChildren()` | ✅ | |
| 16 | `/api/explore/identify` | POST | LandmarkApiService | `identifyLandmark()` | ✅ | Multipart |
| 17 | `/api/dictionary` | GET | DictionaryApiService | `getSigns()` | ✅ | Paginated |
| 18 | `/api/dictionary/categories` | GET | DictionaryApiService | `getCategories()` | ✅ | |
| 19 | `/api/dictionary/alphabet` | GET | DictionaryApiService | `getAlphabet()` | ✅ | |
| 20 | `/api/dictionary/lesson/{level}` | GET | DictionaryApiService | `getLesson()` | ✅ | |
| 21 | `/api/dictionary/speak` | GET | — | — | ❌ MISSING | Dictionary audio playback |
| 22 | `/api/dictionary/{code}` | GET | DictionaryApiService | `getSign()` | ✅ | |
| 23 | `/api/write` | POST | WriteApiService | `write()` | ✅ | |
| 24 | `/api/write/palette` | GET | WriteApiService | `getPalette()` | ✅ | |
| 25 | `/api/translate` | POST | TranslateApiService | `translate()` | ✅ | |
| 26 | `/api/stt` | POST | AudioApiService | `stt()` | ⚠️ PATH | App uses `api/audio/stt` — likely 404 |
| 27 | `/api/audio/speak` | POST | AudioApiService | `speak()` | ✅ | |
| 28 | `/api/stories` | GET | StoriesApiService | `getStories()` | ✅ | Not paginated |
| 29 | `/api/stories/{id}` | GET | StoriesApiService | `getStory()` | ✅ | |
| 30 | `/api/stories/{id}/chapters/{idx}` | GET | — | — | ❌ MISSING | Individual chapter fetch |
| 31 | `/api/stories/{id}/interact` | POST | StoriesApiService | `interact()` | ✅ | |
| 32 | `/api/stories/{id}/chapters/{idx}/image` | POST | StoriesApiService | `generateChapterImage()` | ✅ | |
| 33 | `/api/chat` | POST | — | — | ❌ MISSING | Non-streaming chat fallback |
| 34 | `/api/chat/stream` | POST | ChatRepositoryImpl | OkHttp SSE | ✅ | Direct OkHttp, not Retrofit |
| 35 | `/api/chat/clear` | POST | ChatApiService | `clearChat()` | ✅ | |
| 36 | `/api/user/profile` | GET | UserApiService | `getProfile()` | ✅ | |
| 37 | `/api/user/profile` | PATCH | UserApiService | `updateProfile()` | ✅ | |
| 38 | `/api/user/password` | PATCH | UserApiService | `changePassword()` | ✅ | |
| 39 | `/api/user/history` | GET | UserApiService | `getScanHistory()` | ✅ | Not paginated |
| 40 | `/api/user/favorites` | GET | UserApiService | `getFavorites()` | ✅ | Not paginated |
| 41 | `/api/user/favorites` | POST | UserApiService | `addFavorite()` | ✅ | |
| 42 | `/api/user/favorites/{type}/{id}` | DELETE | UserApiService | `removeFavorite()` | ✅ | |
| 43 | `/api/user/stats` | GET | UserApiService | `getStats()` | ✅ | |
| 44 | `/api/user/progress` | GET | UserApiService | `getStoryProgress()` | ✅ | Not paginated |
| 45 | `/api/user/progress` | POST | UserApiService | `saveProgress()` | ✅ | |
| 46 | `/api/user/limits` | GET | UserApiService | `getLimits()` | ✅ | |
| 47 | `/api/feedback` | POST | FeedbackApiService | `submit()` | ✅ | |
| 48 | `/api/feedback` | GET | — | — | ❌ MISSING | Read submitted feedback |

---

## Missing Endpoints — Severity & Impact

| Endpoint | Severity | User Impact | Feature Affected |
|----------|----------|------------|-----------------|
| POST `/api/auth/send-verification` | 🔴 Critical | Cannot verify email — security gap | Auth |
| POST `/api/auth/verify-email` | 🔴 Critical | Cannot verify email | Auth |
| GET `/api/auth/verify-email` | 🔴 Critical | Token-based verify broken | Auth |
| POST `/api/auth/reset-password` | 🟠 Major | Password reset flow dead-end | Auth |
| GET `/api/dictionary/speak` | 🟡 Medium | Dictionary audio via dedicated endpoint unavailable | Dictionary |
| GET `/api/stories/{id}/chapters/{idx}` | 🟡 Medium | Individual chapter fetch not supported | Stories |
| POST `/api/chat` | 🔵 Low | Non-streaming chat fallback unavailable | Chat |
| GET `/api/feedback` | 🔵 Low | Cannot read submitted feedback | Feedback |

---

## Path Mismatches

| Issue | Contract Path | App Path | Impact |
|-------|--------------|----------|--------|
| PATH-1 🔴 | `/api/stt` | `api/audio/stt` | Likely 404 in production — STT broken |
| PATH-2 ⚠️ | `api/scan` (relative) | `/api/scan` (absolute, leading `/`) | Works by coincidence — fragile |

---

## DTO Field-by-Field Comparison

### Auth DTOs ✅ Complete

| DTO | Contract Fields | App Fields | Drops | Status |
|-----|----------------|-----------|-------|--------|
| RegisterRequest | email, password, display_name | email, password, display_name | None | ✅ |
| LoginRequest | email, password | email, password | None | ✅ |
| GoogleAuthRequest | credential | credential | None | ✅ |
| ForgotPasswordRequest | email | email | None | ✅ |
| AuthResponse | access_token, token_type, user | access_token, token_type, user | None | ✅ |
| UserResponse | id, email, display_name, preferred_lang, tier, auth_provider, email_verified, avatar_url, created_at | All 9 fields mapped | None | ✅ |

### Scan DTOs ⚠️ Field Mismatches

| Contract Field | DTO Field | Status | Issue |
|---------------|----------|--------|-------|
| glyphs[] | glyphs | ✅ | |
| **glyph_count** | numDetections (`num_detections`) | ❌ **NAME MISMATCH** | Contract says `glyph_count`, DTO uses `num_detections` |
| gardiner_sequence | gardinerSequence | ✅ | |
| transliteration | transliteration | ✅ | |
| reading_direction | readingDirection | ✅ | |
| layout_mode | layoutMode | ✅ | |
| **total_ms** | timing.totalMs (nested) | ❌ **STRUCTURAL** | Flat in contract, nested `TimingDto` in DTO |
| detection_source | detectionSource | ✅ | |
| mode | mode | ✅ | |
| **ai_calls_used** | — | ❌ **MISSING** | Field not in DTO at all |
| **image_size** | — | ❌ **MISSING** | Field not in DTO at all |
| confidence_summary | confidenceSummary | ✅ | |
| quality_hints | qualityHints | ✅ | |
| ai_reading | aiReading | ✅ | |

**Extra DTO fields** (not in contract): `translation_en`, `translation_ar`, `ai_unverified`

### Dictionary DTOs ⚠️ Field Drops at Repository Layer

| Layer | Issue | Fields Dropped |
|-------|-------|---------------|
| DTO → Domain | SignDetailDto.toDomain() | `exampleUsages`, `relatedSigns`, `logographicValue`, `determinativeClass` |
| DTO → Domain | ExampleWordDto in Lesson | `speechText` (TTS pronunciation text) |
| DTO → Domain | PracticeWordDto in Lesson | `speechText` (TTS pronunciation text) |

**Impact**: Sign detail view cannot show example usages, related signs, logographic values, or determinative classes. Lesson words cannot be pronounced via TTS.

### Stories DTOs ⚠️ InteractResponse Field Drops

| Contract Field | In InteractResponseDto? | In Domain Model? | Status |
|---------------|------------------------|-------------------|--------|
| text | ✅ | ✅ | ✅ |
| choices | ✅ | ✅ | ✅ |
| **scene_mood** | ✅ DTO has it | ❌ Domain drops it | ⚠️ Lost |
| **background_suggestion** | ✅ DTO has it | ❌ Domain drops it | ⚠️ Lost |
| **sound_effect** | ✅ DTO has it | ❌ Domain drops it | ⚠️ Lost |
| **character_emotion** | ✅ DTO has it | ❌ Domain drops it | ⚠️ Lost |
| **narrator_tone** | ✅ DTO has it | ❌ Domain drops it | ⚠️ Lost |

**Impact**: 5 atmospheric/UX fields from story interactions are parsed but never reach the UI layer. Story immersion reduced.

### Translate DTOs ⚠️ Minor Drop

| Contract Field | In DTO? | In Domain? | Status |
|---------------|---------|-----------|--------|
| text | ✅ | ✅ | ✅ |
| from_lang | ✅ | ✅ | ✅ |
| to_lang | ✅ | ✅ | ✅ |
| transliteration | ✅ | ✅ | ✅ |
| from_cache | ✅ | ✅ | ✅ |
| **latency_ms** | ✅ | ❌ Domain drops | ⚠️ Lost — debug/stats only |

### Chat DTOs ⚠️ Dead Code

| Issue | Description |
|-------|------------|
| ChatRequest defined but NEVER USED | SSE body is hand-crafted as JSON string in ChatRepositoryImpl |
| `session_id` uses snake_case Kotlin name | Convention inconsistency (all other DTOs use camelCase + @SerialName) |

### User, Write, Feedback DTOs ✅ Complete

All field mappings verified correct. No drops.

---

## Pagination Status

| Endpoint | Paginated? | Impact |
|----------|-----------|--------|
| GET `/api/dictionary` | ✅ `page`/`per_page` + `total_pages` | Correct |
| GET `/api/landmarks` | ✅ `page`/`per_page` + `total_pages` | Correct |
| GET `/api/stories` | ❌ Returns all stories | OOM risk with many stories |
| GET `/api/user/history` | ❌ Returns all history | OOM risk with heavy scan usage |
| GET `/api/user/favorites` | ❌ Returns all favorites | OOM risk |
| GET `/api/user/progress` | ❌ Returns all progress | OOM risk |

---

## Offline Caching Parity

| Feature | Room Cache? | Offline Fallback? | Notes |
|---------|------------|------------------|-------|
| Dictionary signs | ✅ SignEntity | ✅ Falls back to Room on IOException | FTS4 search included |
| Landmarks | ✅ LandmarkEntity | ✅ Falls back to Room on IOException | Detail JSON stored |
| Scan history | ✅ ScanResultEntity | ✅ Local history always available | |
| Stories | ❌ None | ❌ Fails completely offline | |
| Story progress | Partial (Firebase offline) | ❌ REST API sync has no fallback | |
| User profile/stats | ❌ None | ❌ Fails offline | |
| Translate | ❌ None | ❌ Fails offline | Server has `from_cache` but client doesn't cache |
| Chat history | ✅ JSON files (ChatHistoryStore) | ✅ Old messages available offline | No offline send queue |
| Chat streaming | ❌ None | ❌ Requires network | |

---

## Error Handling Patterns

Five different patterns found across repositories:

| Pattern | Repos Using It | Description | Consistency |
|---------|---------------|-------------|------------|
| A | AuthRepo | `isSuccessful` → parse errorBody → throw `AuthException` | Best |
| B | DictionaryRepo, ExploreRepo, ScanRepo | `isSuccessful` → throw `ApiException(code)` | Good |
| C | FeedbackRepo | No `isSuccessful` check → null-check body | ⚠️ Poor |
| D | UserRepo | Mix: `isSuccessful` for some, null-check for others | ⚠️ Inconsistent |
| E | StoriesRepo | `isSuccessful` → generic `Exception` | Functional |

**Recommendation**: Standardize on Pattern B with typed `ApiException` for all repositories.

---

## Interceptor Issues

### AuthInterceptor
| ID | Severity | Description |
|----|----------|------------|
| AUTH-1 | Enhancement | Deprecated `RequestBody.create()` |
| AUTH-2 | Medium | Regex-based JSON parsing instead of kotlinx.serialization |
| AUTH-3 | Low | `isAuthEndpoint()` uses `contains()` not prefix match |
| AUTH-4 | Major | Missing auth endpoints in `isAuthEndpoint()` list |
| AUTH-5 | Medium | Logout 401 triggers unexpected token refresh |
| AUTH-6 | Low | Failed refresh re-issues original request (wasted round-trip) |

### RateLimitInterceptor
| ID | Severity | Description |
|----|----------|------------|
| RATE-1 | Medium | 429 on a 503 retry returned raw |
| RATE-2 | Major | `Thread.sleep()` blocks OkHttp dispatcher threads |
| RATE-3 | Low | 429 retry is single-attempt; 503 gets 3 retries (asymmetric) |

### TokenManager
| ID | Severity | Description |
|----|----------|------------|
| TM-1 | Enhancement | Deprecated `MasterKeys.getOrCreate()` API |
| TM-2 | Medium | No token expiry tracking — cold start requires failing request |
| TM-3 | Low | `.apply()` async writes — risk token loss on force-kill |

---

## Consolidated Field Drop Summary

| Feature | Fields Dropped | Where | Severity |
|---------|---------------|-------|----------|
| Dictionary sign detail | exampleUsages, relatedSigns, logographicValue, determinativeClass | DictionaryRepositoryImpl.toDomain() | 🟠 Major |
| Dictionary lesson words | speechText (both ExampleWord and PracticeWord) | DictionaryRepositoryImpl.toDomain() | 🟡 Medium |
| Stories interact response | scene_mood, background_suggestion, sound_effect, character_emotion, narrator_tone | StoriesRepositoryImpl.toDomain() | 🟠 Major |
| Translate response | latencyMs | TranslateRepositoryImpl.toDomain() | 🔵 Low |
| Auth response | createdAt available but not used in all flows | UserResponse mapping | 🔵 Info |
| Scan response | glyph_count (name mismatch), ai_calls_used, image_size (missing) | ScanModels.kt | 🟠 Major |
