# Stage 2: Network Layer — Logic & Quality Audit Report

**Date:** 2026-04-15  
**Scope:** `core/network/src/main/java/com/wadjet/core/network/`  
**Method:** Zero-hallucination — only what was read from the actual files

---

## Endpoint Coverage Matrix

| # | Contract Endpoint | Method | API Service | Retrofit Function | Status |
|---|---|---|---|---|---|
| 1 | `/api/auth/register` | POST | AuthApiService | `register()` | ✅ |
| 2 | `/api/auth/login` | POST | AuthApiService | `login()` | ✅ |
| 3 | `/api/auth/refresh` | POST | AuthApiService | `refresh()` | ✅ |
| 4 | `/api/auth/logout` | POST | AuthApiService | `logout()` | ✅ |
| 5 | `/api/auth/google` | POST | AuthApiService | `googleAuth()` | ✅ |
| 6 | `/api/auth/send-verification` | POST | — | — | ❌ MISSING |
| 7 | `/api/auth/verify-email` | POST | — | — | ❌ MISSING |
| 8 | `/api/auth/verify-email` | GET | — | — | ❌ MISSING |
| 9 | `/api/auth/forgot-password` | POST | AuthApiService | `forgotPassword()` | ✅ |
| 10 | `/api/auth/reset-password` | POST | — | — | ❌ MISSING |
| 11 | `/api/scan` | POST | ScanApiService | `scan()` | ✅ ⚠️ |
| 12 | `/api/landmarks` | GET | LandmarkApiService | `getLandmarks()` | ✅ |
| 13 | `/api/landmarks/categories` | GET | LandmarkApiService | `getCategories()` | ✅ |
| 14 | `/api/landmarks/{slug}` | GET | LandmarkApiService | `getLandmarkDetail()` | ✅ |
| 15 | `/api/landmarks/{slug}/children` | GET | LandmarkApiService | `getLandmarkChildren()` | ✅ |
| 16 | `/api/explore/identify` | POST | LandmarkApiService | `identifyLandmark()` | ✅ |
| 17 | `/api/dictionary` | GET | DictionaryApiService | `getSigns()` | ✅ |
| 18 | `/api/dictionary/categories` | GET | DictionaryApiService | `getCategories()` | ✅ |
| 19 | `/api/dictionary/alphabet` | GET | DictionaryApiService | `getAlphabet()` | ✅ |
| 20 | `/api/dictionary/lesson/{level}` | GET | DictionaryApiService | `getLesson()` | ✅ |
| 21 | `/api/dictionary/speak` | GET | — | — | ❌ MISSING |
| 22 | `/api/dictionary/{code}` | GET | DictionaryApiService | `getSign()` | ✅ |
| 23 | `/api/write` | POST | WriteApiService | `write()` | ✅ |
| 24 | `/api/write/palette` | GET | WriteApiService | `getPalette()` | ✅ |
| 25 | `/api/translate` | POST | TranslateApiService | `translate()` | ✅ |
| 26 | `/api/stt` | POST | AudioApiService | `stt()` | ❌ PATH MISMATCH |
| 27 | `/api/audio/speak` | POST | AudioApiService | `speak()` | ✅ |
| 28 | `/api/stories` | GET | StoriesApiService | `getStories()` | ✅ |
| 29 | `/api/stories/{id}` | GET | StoriesApiService | `getStory()` | ✅ |
| 30 | `/api/stories/{id}/chapters/{idx}` | GET | — | — | ❌ MISSING |
| 31 | `/api/stories/{id}/interact` | POST | StoriesApiService | `interact()` | ✅ |
| 32 | `/api/stories/{id}/chapters/{idx}/image` | POST | StoriesApiService | `generateChapterImage()` | ✅ |
| 33 | `/api/chat` | POST | — | — | ❌ MISSING |
| 34 | `/api/chat/stream` | POST | — | — | ⚠️ OkHttp SSE only |
| 35 | `/api/chat/clear` | POST | ChatApiService | `clearChat()` | ✅ |
| 36 | `/api/user/profile` | GET | UserApiService | `getProfile()` | ✅ |
| 37 | `/api/user/profile` | PATCH | UserApiService | `updateProfile()` | ✅ |
| 38 | `/api/user/password` | PATCH | UserApiService | `changePassword()` | ✅ |
| 39 | `/api/user/history` | GET | UserApiService | `getScanHistory()` | ✅ |
| 40 | `/api/user/favorites` | GET | UserApiService | `getFavorites()` | ✅ |
| 41 | `/api/user/favorites` | POST | UserApiService | `addFavorite()` | ✅ |
| 42 | `/api/user/favorites/{type}/{id}` | DELETE | UserApiService | `removeFavorite()` | ✅ |
| 43 | `/api/user/stats` | GET | UserApiService | `getStats()` | ✅ |
| 44 | `/api/user/progress` | GET | UserApiService | `getStoryProgress()` | ✅ |
| 45 | `/api/user/progress` | POST | UserApiService | `saveProgress()` | ✅ |
| 46 | `/api/user/limits` | GET | UserApiService | `getLimits()` | ✅ |
| 47 | `/api/feedback` | POST | FeedbackApiService | `submit()` | ✅ |
| 48 | `/api/feedback` | GET | — | — | ❌ MISSING |

**Summary:** 38 implemented via Retrofit, 1 via direct OkHttp SSE, 8 missing entirely, 1 path mismatch.

---

## DTO Field-by-Field Comparison

### AuthModels.kt
- RegisterRequest: email, password, display_name — ✅ complete
- LoginRequest: email, password — ✅ complete
- GoogleAuthRequest: credential — ✅ complete
- ForgotPasswordRequest: email — ✅ complete
- AuthResponse: access_token, token_type, user — ✅ complete
- UserResponse: id, email, display_name, preferred_lang, tier, auth_provider, email_verified, avatar_url, created_at — ✅ all fields match

> ⚠️ With `encodeDefaults = true`, `display_name: null` serializes as `"display_name": null` rather than being omitted. FastAPI/Pydantic accepts this.

### ScanModels.kt — ISSUES FOUND

| Contract Field | DTO Field | Status | Issue |
|---|---|---|---|
| glyphs[] | glyphs | ✅ | |
| glyph_count | numDetections (`num_detections`) | ❌ | Field name mismatch — contract says `glyph_count` |
| gardiner_sequence | gardinerSequence | ✅ | |
| transliteration | transliteration | ✅ | |
| reading_direction | readingDirection | ✅ | |
| layout_mode | layoutMode | ✅ | |
| total_ms | timing.totalMs (nested) | ❌ | Structural mismatch — flat in contract, nested in TimingDto |
| detection_source | detectionSource | ✅ | |
| mode | mode | ✅ | |
| ai_calls_used | — | ❌ | MISSING entirely |
| image_size | — | ❌ | MISSING entirely |
| confidence_summary | confidenceSummary | ✅ | |
| quality_hints | qualityHints | ✅ | |
| ai_reading | aiReading | ✅ | |

Extra fields in DTO not in contract: `translation_en`, `translation_ar`, `ai_unverified`

### LandmarkModels.kt
- IdentifyResponse: name, confidence, slug, source, agreement, description, isKnownLandmark, isEgyptian, top3 — ✅ all fields match contract
- LandmarkSummaryDto, LandmarkDetailDto, LandmarkCategoriesResponse — ✅ well-formed

### DictionaryModels.kt
- SignDetailDto vs SignDict contract: all 17 fields match ✅
- LessonResponse: all 13 fields match ✅
- ExampleWordDto, PracticeWordDto — ✅ complete

### ChatModels.kt — ISSUES
- ChatRequest: defined but **NEVER USED** (dead code — SSE body hand-crafted)
- `session_id` field uses snake_case Kotlin naming instead of camelCase + @SerialName

### StoryModels.kt — ✅ all DTOs well-formed

### UserModels.kt — ✅ all DTOs correct

### WriteModels.kt
- SpeakRequest and SttResponse live here despite being audio DTOs (packaging inconsistency)
- WriteRequest, PaletteResponse — ✅ correct

### TranslateModels.kt — ✅ correct

### FeedbackModels.kt — ✅ correct (name/email default to "" with encodeDefaults)

---

## Auth Interceptor Analysis

**Positive:**
- External URL bypass for non-baseUrl URLs ✅
- Mutex protecting concurrent refresh ✅
- Stale token check (currentToken != failedToken) ✅
- Cookie extraction for wadjet_refresh ✅

**Issues:**
- AUTH-1: Deprecated `RequestBody.create()` (line 95)
- AUTH-2: Regex-based JSON parsing instead of kotlinx.serialization (line 150-152) — brittle
- AUTH-3: `isAuthEndpoint()` uses `contains()` not prefix match (line 154-160)
- AUTH-4: Missing `send-verification`, `verify-email`, `reset-password` in `isAuthEndpoint()`
- AUTH-5: `logout` not in `isAuthEndpoint()` — 401 on logout triggers unexpected refresh
- AUTH-6: Failed refresh re-issues original request (wasted round-trip) instead of returning 401

## Rate Limit Interceptor Analysis

**Positive:**
- Login lockout 429 returned without retry ✅
- Retry-After respected with 30s cap ✅
- Exponential backoff for 503 (1s → 2s → 4s, max 3) ✅

**Issues:**
- RATE-1: 429 on a 503 retry is NOT intercepted — returned raw to caller

## Token Manager Analysis

**Positive:**
- EncryptedSharedPreferences with AES256-GCM ✅
- @Singleton scope ✅
- clearAll() for complete logout ✅

**Issues:**
- TM-1: Deprecated `MasterKeys.getOrCreate()` API
- TM-2: No token expiry tracking (cold-start requires failing request)
- TM-3: `.apply()` async writes — risk token loss on force-kill
- TM-4: `clearAll()` wipes entire prefs file

---

## Missing Endpoints

| Endpoint | Severity | Notes |
|---|---|---|
| POST /api/auth/send-verification | High | Email verification flow broken |
| POST /api/auth/verify-email | High | Email verification flow broken |
| GET /api/auth/verify-email | High | Token-based email verification broken |
| POST /api/auth/reset-password | High | Password reset flow dead-end |
| GET /api/dictionary/speak | Medium | Dictionary audio playback endpoint |
| GET /api/stories/{id}/chapters/{idx} | Medium | Individual chapter fetch |
| POST /api/chat | Low | Non-streaming chat fallback |
| GET /api/feedback | Low | Reading submitted feedback |

## Wrong/Extra Endpoints

- **PATH-1 (HIGH)**: STT path is `api/audio/stt` but contract says `/api/stt` — likely 404 in production
- **PATH-2 (Low)**: ScanApiService uses absolute path `/api/scan` (leading slash), others use relative
- **DTO-DEAD-1**: ChatRequest defined but never used
- **CONV-1**: ChatRequest/ClearChatRequest use snake_case Kotlin field names
- **PKG-1**: SpeakRequest/SttResponse in WriteModels.kt instead of AudioModels

## All Issues (Consolidated)

| ID | Severity | File | Description |
|---|---|---|---|
| PATH-1 | 🔴 Critical | AudioApiService.kt:22 | STT path mismatch: `api/audio/stt` vs contract `/api/stt` |
| MISSING-1 | 🟠 Major | — | POST /api/auth/send-verification not implemented |
| MISSING-2 | 🟠 Major | — | POST /api/auth/verify-email not implemented |
| MISSING-3 | 🟠 Major | — | GET /api/auth/verify-email not implemented |
| MISSING-4 | 🟠 Major | — | POST /api/auth/reset-password not implemented |
| MISSING-5 | 🟠 Major | — | GET /api/dictionary/speak not implemented |
| MISSING-6 | 🟡 Minor | — | GET /api/stories/{id}/chapters/{idx} not implemented |
| SCAN-1 | 🟠 Major | ScanModels.kt | `glyph_count` mapped as `num_detections` — field name mismatch |
| SCAN-2 | 🟡 Minor | ScanModels.kt | `total_ms` structural mismatch (flat vs nested) |
| SCAN-3 | 🟡 Minor | ScanModels.kt | `ai_calls_used` missing from DTO |
| SCAN-4 | 🟡 Minor | ScanModels.kt | `image_size` missing from DTO |
| SSE-3 | 🟡 Minor | NetworkModule.kt:64 | 60s readTimeout terminates SSE streams after silent period |
| RATE-1 | 🟡 Minor | RateLimitInterceptor.kt | 429 on 503-retry not handled |
| AUTH-1 | 🔵 Enhancement | AuthInterceptor.kt:95 | Deprecated RequestBody.create() |
| AUTH-2 | 🔵 Enhancement | AuthInterceptor.kt:150 | Regex JSON parsing — fragile |
| AUTH-5 | 🔵 Enhancement | AuthInterceptor.kt:154 | Logout 401 triggers unexpected refresh |
| AUTH-6 | 🔵 Enhancement | AuthInterceptor.kt:127 | Failed refresh wastes a round-trip |
| TM-1 | 🔵 Enhancement | TokenManager.kt:16 | Deprecated MasterKeys API |
| TM-2 | 🔵 Enhancement | TokenManager.kt | No token expiry tracking |
| DTO-DEAD-1 | 🔵 Enhancement | ChatModels.kt:8 | Dead ChatRequest DTO |
| CONV-1 | 🔵 Enhancement | ChatModels.kt:13 | snake_case Kotlin field names |
| PKG-1 | 🔵 Enhancement | WriteModels.kt | Audio DTOs in wrong models file |
