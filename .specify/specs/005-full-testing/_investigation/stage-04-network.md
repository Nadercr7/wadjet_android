# Stage 4 — Network Layer: API Contract & Error Handling

**Date:** 2025-07-22  
**Auditor:** Automated (Copilot)  
**Scope:** 11 API services, 10 DTO model files, auth infrastructure, live API testing

---

## Summary

| Metric | Value |
|---|---|
| **API Service interfaces** | 11 (37+ endpoints total) |
| **DTO model files** | 10 |
| **Interceptors** | 2 (Auth + RateLimit) + TokenAuthenticator |
| **Critical issues** | 3 |
| **High issues** | 4 |
| **Medium issues** | 4 |
| **Missing backend endpoints** | 6 (intentional omissions + real gaps) |
| **Parameter mismatches** | 5+ between contract and Retrofit |

---

## 1. Live API Verification

| Endpoint | Status | Response Shape Matches DTO? | Notes |
|---|---|---|---|
| `GET /api/dictionary?page=1&per_page=2` | 200 | Yes | Fields: `signs[]`, `total`, `page`, `per_page`, `total_pages` |
| `GET /api/dictionary/categories` | 200 | Yes | `{categories: [{code, name, count}]}` |
| `GET /api/dictionary/alphabet` | 200 | Yes | `{signs: [...], count: N}` |
| `GET /api/landmarks?page=1&per_page=2` | 200 | Yes | Includes `subcategory`, `period` — extra fields ignored by `ignoreUnknownKeys` |
| `GET /api/landmarks/categories` | 200 | Yes | `{types: [...], cities: [...]}` — no `category_tree` or `total` (DTO has defaults) |
| `GET /api/stories` | 200 | Yes | `{stories: [{id, title{en,ar}, subtitle{en,ar}, ...}]}` |
| `GET /api/write/palette` | 200 | Yes | `{groups: {uniliteral: [...], ...}}` |
| `GET /api/dictionary/NONEXISTENT` | **404** | Yes | `{"detail":"Not found"}` → parsed as `ErrorResponse` |
| `GET /api/user/profile` (no auth) | **401** | Yes | Returns 401 as expected |

---

## 2. API Service Interfaces (11 total)

### 2.1 AuthApiService (6 endpoints)
| Method | Path | Return | Notes |
|---|---|---|---|
| POST | `api/auth/register` | `AuthResponse` | |
| POST | `api/auth/login` | `AuthResponse` | |
| POST | `api/auth/google` | `AuthResponse` | |
| POST | `api/auth/refresh` | `AuthResponse` | Cookie-based |
| POST | `api/auth/logout` | `Unit` | |
| POST | `api/auth/forgot-password` | `Unit` | |

### 2.2 AudioApiService (2 endpoints)
| Method | Path | Return | Notes |
|---|---|---|---|
| POST | `api/audio/speak` | `ResponseBody` (raw bytes) | |
| POST | `api/audio/stt` | `SttResponse` | Multipart: file + lang |

### 2.3 ChatApiService (1 endpoint + SSE)
| Method | Path | Return | Notes |
|---|---|---|---|
| POST | `api/chat/clear` | `ClearChatResponse` | |
| POST | `api/chat/stream` | (SSE via raw OkHttp) | NOT in Retrofit |

### 2.4 DictionaryApiService (5 endpoints)
| Method | Path | Return | Notes |
|---|---|---|---|
| GET | `api/dictionary` | `DictionaryResponse` | Paginated with filters |
| GET | `api/dictionary/categories` | `CategoriesResponse` | |
| GET | `api/dictionary/alphabet` | `AlphabetResponse` | |
| GET | `api/dictionary/lesson/{level}` | `LessonResponse` | |
| GET | `api/dictionary/{code}` | `SignDetailDto` | |

### 2.5 FeedbackApiService (1 endpoint)
| Method | Path | Return |
|---|---|---|
| POST | `api/feedback` | `FeedbackResponse` |

### 2.6 LandmarkApiService (5 endpoints)
| Method | Path | Return | Notes |
|---|---|---|---|
| GET | `api/landmarks` | `LandmarkListResponse` | Paginated |
| GET | `api/landmarks/categories` | `LandmarkCategoriesResponse` | |
| GET | `api/landmarks/{slug}` | `LandmarkDetailDto` | 37 fields |
| GET | `api/landmarks/{slug}/children` | `LandmarkChildrenResponse` | |
| POST | `api/explore/identify` | `IdentifyResponse` | Multipart |

### 2.7 ScanApiService (1 endpoint)
| Method | Path | Return | Notes |
|---|---|---|---|
| POST | `api/scan` | `ScanResponse` | Multipart: file + mode |

### 2.8 StoriesApiService (4 endpoints)
| Method | Path | Return |
|---|---|---|
| GET | `api/stories` | `StoriesListResponse` |
| GET | `api/stories/{storyId}` | `StoryFullDto` |
| POST | `api/stories/{storyId}/interact` | `InteractResponse` |
| POST | `api/stories/{storyId}/chapters/{index}/image` | `ChapterImageResponse` |

### 2.9 TranslateApiService (1 endpoint)
| Method | Path | Return |
|---|---|---|
| POST | `api/translate` | `TranslateResponse` |

### 2.10 UserApiService (11 endpoints)
| Method | Path | Return |
|---|---|---|
| GET | `api/user/profile` | `UserResponse` |
| PATCH | `api/user/profile` | `UserResponse` |
| PATCH | `api/user/password` | `OkResponse` |
| GET | `api/user/history` | `List<ScanHistoryItemDto>` |
| GET | `api/user/favorites` | `List<FavoriteItemDto>` |
| POST | `api/user/favorites` | `FavoriteItemDto` |
| DELETE | `api/user/favorites/{item_type}/{item_id}` | `OkResponse` |
| GET | `api/user/stats` | `UserStatsResponse` |
| GET | `api/user/progress` | `List<StoryProgressItemDto>` |
| POST | `api/user/progress` | `OkResponse` |
| GET | `api/user/limits` | `UserLimitsResponse` |

### 2.11 WriteApiService (2 endpoints)
| Method | Path | Return |
|---|---|---|
| POST | `api/write` | `WriteResponse` |
| GET | `api/write/palette` | `PaletteResponse` |

**Auth mechanism:** All 11 services rely entirely on `AuthInterceptor` for Bearer token injection. No endpoint uses `@Header("Authorization")`.

---

## 3. Auth Infrastructure

### 3.1 TokenManager
- **Storage:** `EncryptedSharedPreferences` with AES-256-GCM (AndroidX Security Crypto)
- **Keys:** `access_token`, `refresh_token` in `"wadjet_secure_prefs"`
- **Thread safety:** Relies on SharedPreferences apply() — no explicit synchronization

### 3.2 AuthInterceptor
- **Scope guard:** Only applies to requests matching `baseUrl`
- **Auth endpoints:** Login, register, google, refresh, forgot-password get special handling
- **Normal requests:** Adds `Authorization: Bearer <token>` if token exists
- **Refresh endpoint:** Injects `Cookie: wadjet_refresh=<token>`
- **Set-Cookie extraction:** Parses `Set-Cookie` headers for `wadjet_refresh` on auth responses
- **BUG:** `/auth/logout` is NOT in `isAuthEndpoint()` — goes through normal Bearer path

### 3.3 TokenAuthenticator (401 handling)
- **Trigger:** Any 401 response
- **Skip auth endpoints:** Returns null for URLs containing `/auth/`
- **Retry limit:** Max 1 retry
- **Concurrency:** Uses `ReentrantLock` — good for avoiding blocking coroutine dispatchers
- **Race condition handling:** Checks if token changed since failure → skip refresh if already refreshed
- **CRITICAL BUG:** Creates a **new bare OkHttpClient** for refresh call — no interceptors, no User-Agent, no timeouts, no connection pool reuse

### 3.4 RateLimitInterceptor
- **Passive:** Only logs 429 and 503 responses
- **Does NOT retry or delay** — delegates retry to repository/ViewModel layer
- **Reads `Retry-After` header** and logs special message for `/auth/login` lockout

### 3.5 NetworkModule Configuration
- **Interceptor order:** AuthInterceptor → RateLimitInterceptor → HttpLoggingInterceptor → User-Agent
- **Authenticator:** TokenAuthenticator
- **Timeouts:** Connect 30s, Read 60s (SSE-safe), Write 30s
- **JSON:** `ignoreUnknownKeys=true`, `coerceInputValues=true`, `encodeDefaults=true`
- **SSE Chat:** Raw OkHttp EventSource, same shared client, POST with `Accept: text/event-stream`

---

## 4. Error Response Handling

| Status Code | What Backend Sends | Android Handling | Adequate? |
|---|---|---|---|
| 400 | `{"detail":"..."}` | Caught by `Result.isFailure` in repos | Partial — only some repos parse `ErrorResponse` |
| 401 | `{"detail":"Not authenticated"}` | `TokenAuthenticator` auto-refreshes and retries | Yes |
| 403 | `{"detail":"Forbidden"}` | No special handling — falls through to generic error | No |
| 404 | `{"detail":"Not found"}` | Caught by `Result.isFailure` | Partial |
| 429 | `{"detail":"Too many requests"}` | `RateLimitInterceptor` logs only, no retry | No — user sees generic error |
| 500 | `{"detail":"Internal server error"}` | Caught by `Result.isFailure` | Partial |
| Network timeout | N/A | OkHttp throws exception → caught in repos | Yes |
| No internet | N/A | `ConnectivityManagerNetworkMonitor` + exception handling | Varies by feature |
| Malformed JSON | N/A | `ignoreUnknownKeys` + `coerceInputValues` handle minor issues | Mostly — catastrophic malformation would crash |

---

## 5. API Contract vs Implementation Mismatches

### 5.1 Missing Endpoints (in contract, not in Retrofit)

| # | Endpoint | Severity | Notes |
|---|----------|----------|-------|
| 1 | `POST /api/auth/send-verification` | **High** | Email verification flow not implemented |
| 2 | `POST /api/auth/verify-email` | **High** | Email verification flow not implemented |
| 3 | `GET /api/auth/verify-email` | Low | Browser redirect, may be intentionally skipped |
| 4 | `POST /api/auth/reset-password` | **High** | Only forgot-password exists, no actual reset |
| 5 | `GET /api/dictionary/speak` | Low | Audio served via `/api/audio/speak` instead |
| 6 | `GET /api/stories/{id}/chapters/{idx}` | Low | Full story fetched instead of per-chapter |

### 5.2 Parameter & Response Mismatches

| # | Endpoint | Issue | Severity |
|---|----------|-------|----------|
| 1 | `POST /api/scan` | Missing `translate` param — can't disable translation | High |
| 2 | `POST /api/audio/stt` | Path mismatch: Android=`api/audio/stt`, Contract=`api/stt` | **Critical** |
| 3 | `POST /api/user/progress` | `glyphs_learned` type: Android=`List<String>`, Contract=`String(max5000)` | **Critical** |
| 4 | `POST /api/user/progress` | Response: server returns `{id, story_id, chapter_index, completed}`, Android expects `{ok: true}` | High |
| 5 | `GET /api/landmarks` | Missing `subcategory`, `parent`, `include_children` query params | Medium |
| 6 | Scan glyphs | Contract: named `x1,y1,x2,y2` fields; Android DTO: flat `List<Float>` bbox | High |
| 7 | `POST /api/feedback` | Missing `page_url` field from contract | Low |

---

## 6. DTO Analysis Notes

### Naming Consistency
- **ChatModels** use raw snake_case property names (`session_id`) instead of `@SerialName` — inconsistent with all other DTOs
- **SpeakRequest** and **SttResponse** are defined in `WriteModels.kt` instead of a dedicated audio models file

### Nullable Safety
- All DTO fields have sensible defaults for nullable fields
- `LandmarkDetailDto` has 37 fields, nearly all nullable with null default — very defensive
- `ignoreUnknownKeys=true` ensures new server fields don't crash deserialization

### Default Values
- `SignDetailDto` defaults all strings to `""` — prevents NPE but makes it hard to distinguish "empty" from "not sent"
- `DictionaryResponse.perPage` defaults to `30` matching the query default

---

## 7. Critical Issues

| # | Issue | Location | Impact |
|---|---|---|---|
| **C1** | TokenAuthenticator creates bare OkHttpClient for refresh — no interceptors, no auth headers, no User-Agent, default timeouts, no connection pool reuse | TokenAuthenticator.kt ~L72 | Refresh call may behave differently than expected; no User-Agent could cause server issues |
| **C2** | STT path mismatch: Android sends to `api/audio/stt`, contract says `api/stt` | AudioApiService.kt | STT feature may 404 unless server also handles the `/audio/stt` path |
| **C3** | `glyphs_learned` type mismatch: Android sends `List<String>`, server expects plain string | UserModels.kt / SaveProgressRequest | Story progress save may fail silently |

## 8. High Issues

| # | Issue | Location | Impact |
|---|---|---|---|
| **H1** | Missing `translate` param in ScanApiService | ScanApiService.kt | Can't control scan translation behavior |
| **H2** | SaveProgress response shape mismatch | UserApiService.kt | May fail to deserialize (mitigated by ignoreUnknownKeys + coerceInputValues) |
| **H3** | 6 backend endpoints not implemented | Auth (3), Dictionary, Stories, Chat | Email verification, password reset, single chapter fetch not available |
| **H4** | Scan glyph bbox format mismatch | ScanModels.kt | If server sends named fields instead of array, glyph positions would be lost |

## 9. Medium Issues

| # | Issue | Location |
|---|---|---|
| **M1** | Missing landmark query params (subcategory, parent, include_children) | LandmarkApiService.kt |
| **M2** | Logout not in `isAuthEndpoint()` — gets Bearer treatment | AuthInterceptor.kt |
| **M3** | ChatModels use raw snake_case property names | ChatModels.kt |
| **M4** | SpeakRequest/SttResponse in WriteModels.kt (misleading organization) | WriteModels.kt |

---

## 10. SSE Chat Stream Analysis

- **Implementation:** Raw OkHttp + `EventSources.createFactory(okHttpClient)` in ChatRepositoryImpl
- **Body:** POST to `api/chat/stream` with JSON `{message, session_id, landmark?}` and `Accept: text/event-stream`
- **Parsing:** Chunks parsed as `ChatStreamChunk`; `[DONE]` signal closes the flow
- **Fallback:** Falls back to raw text if JSON parsing fails — good resilience
- **Timeout:** Uses shared OkHttpClient with 60s read timeout — adequate for SSE
- **Auth:** Gets Bearer token via AuthInterceptor (same client)
- **Reconnection:** No automatic reconnection on stream failure
- **Error recovery:** Errors emitted via Flow — ViewModel can show error and offer retry

---

## 11. Test Coverage for Network Layer

| Component | Has Tests? | Notes |
|---|---|---|
| AuthInterceptor | **Yes** (3 tests) | Token attachment, auth endpoint detection |
| RateLimitInterceptor | **Yes** (3 tests) | 429/503 logging |
| TokenAuthenticator | **No** | Critical component with no tests |
| TokenManager | **No** | EncryptedSharedPreferences wrapper untested |
| API Services | **No** (unit) | Only integration-tested via repository tests with MockWebServer |
| DTOs | **Partial** | Tested implicitly by repository tests parsing responses |
| SSE Chat | **No** | No test for streaming, parsing, or error handling |

---

## 12. Recommendations

1. **Fix TokenAuthenticator** — reuse shared OkHttpClient (minus auth interceptor to avoid loop) instead of bare client
2. **Verify STT endpoint path** — test `api/audio/stt` against live backend; fix path if needed
3. **Fix `glyphs_learned` serialization** — change to `String` or confirm server handles `List<String>`
4. **Add 429 retry** — implement exponential backoff in RateLimitInterceptor or repository layer
5. **Implement email verification flow** — 3 missing auth endpoints
6. **Add TokenAuthenticator tests** — critical auth component with zero coverage
7. **Add SSE stream tests** — chat is core feature with no network-level tests
8. **Normalize ChatModels** — use `@SerialName` for `session_id` like all other DTOs
