# Stage 12 — Backend API Smoke Test

**Date:** 2025-07-22  
**Auditor:** Automated (Copilot)  
**Scope:** All public API endpoints, response schemas, timing, DTO contract comparison

---

## Summary

| Metric | Value |
|---|---|
| **Endpoints tested** | 9 (6 public + 3 error) |
| **All public endpoints responding** | ✅ (200 OK, 141-172ms) |
| **Error handling** | ✅ 404/401 return `{"detail":"..."}` |
| **Critical DTO mismatches** | 2 (landmark parent/children always null) |
| **High DTO mismatches** | 2 (palette missing numbers + determinatives) |
| **JSON config** | `ignoreUnknownKeys = true`, `coerceInputValues = true` — extra backend fields don't crash |

---

## 1. Endpoint Responses

| # | Endpoint | Status | Time (ms) | Key Fields | Notes |
|---|---|---|---|---|---|
| 1 | `/api/dictionary` | 200 | 148 | signs(50), total=1023, page=1, per_page=50, total_pages=21 | Paginated, 50/page |
| 2 | `/api/dictionary/categories` | 200 | 141 | categories(26), total_signs=1023 | All Gardiner categories A-Z + Aa |
| 3 | `/api/dictionary/alphabet` | 200 | ~150 | alphabet object | Uniliteral sign list |
| 4 | `/api/landmarks` | 200 | 172 | landmarks(24), total=164 | Paginated, 24/page |
| 5 | `/api/landmarks/categories` | 200 | ~150 | categories | Landmark types/subcategories |
| 6 | `/api/stories` | 200 | 141 | stories array, count=12 | 12 stories total |
| 7 | `/api/write/palette` | 200 | ~150 | groups: uniliteral, biliteral, triliteral, numbers, determinative, logogram | 6 sign groups |
| 8 | `/api/dictionary/NONEXISTENT` | 404 | ~140 | `{"detail":"Sign not found: NONEXISTENT"}` | Correct error format |
| 9 | `/api/user/profile` | 401 | ~140 | `{"detail":"Not authenticated"}` | Correct auth guard |

Backend is fast and healthy. No cold-start observed during testing.

---

## 2. Schema Mismatches (DTO vs Backend)

### CRITICAL

| # | Endpoint | DTO Field | Backend Field | Issue |
|---|---|---|---|---|
| 1 | `/api/landmarks/{slug}` | `parent: LandmarkParentDto?` (expects `"parent"` key with object) | `parent_slug: String` (sends `"parent_slug"` key with flat string) | **DTO always null** — key name mismatch (`parent` vs `parent_slug`) AND type mismatch (object vs string) |
| 2 | `/api/landmarks/{slug}` | `children: List<LandmarkChildDto>?` (expects `"children"` with objects) | `children_slugs: List<String>` (sends `"children_slugs"` with flat strings) | **DTO always null** — same dual mismatch |

### HIGH

| # | Endpoint | DTO Field | Backend Field | Issue |
|---|---|---|---|---|
| 3 | `/api/write/palette` | `PaletteGroupsDto` has: uniliteral, biliteral, triliteral, logogram | Backend also sends: `numbers`, `determinative` | **Two entire palette groups silently dropped** — users can never write number or determinative signs |

### MEDIUM

| # | Endpoint | DTO Field | Backend Field | Issue |
|---|---|---|---|---|
| 4 | `/api/dictionary` | `DictionaryResponse` missing `count` field | Backend sends `"count": 50` (items per page) | Minor — total/page enough for pagination |
| 5 | `/api/landmarks` | `LandmarkListResponse` has `page`, `perPage`, `totalPages` | Backend doesn't send these — not paginated same way | DTO fabricates values (1, 24, 1) |
| 6 | `/api/landmarks/{slug}` | `LandmarkDetailDto` has 13 extra fields | Not in backend schema | `highlights`, `visitingTips`, `historicalSignificance`, `dynasty`, `notablePharaohs`, `notableTombs`, `notableFeatures`, `keyArtifacts`, `architecturalFeatures`, `originalImage`, `wikipediaExtract`, `wikipediaUrl`, `recommendations` — all always null |

### LOW

| # | Endpoint | DTO Field | Backend Field | Issue |
|---|---|---|---|---|
| 7 | `/api/dictionary/categories` | Missing `total_signs` | Backend sends it | Not used by UI |
| 8 | `/api/landmarks` | Missing `image_count` | Backend sends per landmark | Not critical |
| 9 | `/api/landmarks` | Missing `source` | Backend sends data source attribution | Copyright concern |

---

## 3. Error Response Format

All backend errors follow a consistent pattern:
```json
{"detail": "<error message>"}
```

Android parsing:
- **Pattern A** (Auth/Scan/User): Parses `errorBody()` as JSON → extracts `detail` — ✅ correct
- **Pattern B** (Dictionary/Explore): Regex `"detail":"(.+?)"` on exception message — **fragile but works**
- **Pattern C** (Feedback/Settings): No parsing — raw exception message shown to user
- **Rate limiting**: Returns 429 with `Retry-After` header. Auth repo handles this. Other repos don't check.

---

## 4. Sign DTO Field Mapping

Backend sign fields → DTO coverage:

| Backend Field | In `SignDto`? | In `SignDetailDto`? | Notes |
|---|---|---|---|
| `code` | ✅ | ✅ | — |
| `transliteration` | ✅ | ✅ | — |
| `type` | ✅ | ✅ | — |
| `type_name` | ✅ | ✅ | `@SerialName("type_name")` |
| `description` | ✅ | ✅ | — |
| `category` | ✅ | ✅ | — |
| `category_name` | ✅ | ✅ | `@SerialName("category_name")` |
| `reading` | ✅ | ✅ | — |
| `logographic_value` | ✅ | ✅ | `@SerialName("logographic_value")` |
| `determinative_class` | ✅ | ✅ | `@SerialName("determinative_class")` |
| `unicode_char` | ✅ | ✅ | `@SerialName("unicode_char")` |
| `is_phonetic` | ✅ | ✅ | `@SerialName("is_phonetic")` |
| `pronunciation` | ✅ | ✅ | nullable ✅ |
| `fun_fact` | ✅ | ✅ | `@SerialName("fun_fact")`, nullable ✅ |
| `speech_text` | ✅ | ✅ | `@SerialName("speech_text")`, nullable ✅ |

Sign DTO mapping is **clean** — all 15 fields accounted for.

---

## 5. Landmark DTO Field Mapping

| Backend Field | In `LandmarkSummaryDto`? | In `LandmarkDetailDto`? | Notes |
|---|---|---|---|
| `slug` | ✅ | ✅ | — |
| `name` | ✅ | ✅ | — |
| `name_ar` | ✅ | ✅ | — |
| `city` | ✅ | ✅ | — |
| `type` | ✅ | ✅ | — |
| `subcategory` | — | ✅ | — |
| `era` | — | ✅ | — |
| `period` | — | ✅ | — |
| `popularity` | — | ✅ | — |
| `description` | ✅ | ✅ | — |
| `coordinates` | — | ✅ | — |
| `maps_url` | — | ✅ | — |
| `thumbnail` | ✅ | ✅ | — |
| `image_count` | — | — | ❌ Ignored |
| `tags` | ✅ | ✅ | — |
| `related_sites` | — | ✅ | — |
| `parent_slug` | — | ❌ `parent` (wrong key) | **CRITICAL** |
| `children_slugs` | — | ❌ `children` (wrong key) | **CRITICAL** |
| `featured` | ✅ | ✅ | — |
| `images` | — | ✅ | — |
| `sections` | — | ✅ | — |
| `source` | — | — | ❌ Ignored |

---

## 6. Backend Health

- Server responsive: ✅ (all endpoints return in <200ms)
- No cold-start observed (HuggingFace Spaces running)
- No 500 errors encountered
- CORS headers present
- Security headers: HSTS, CSP, X-Frame-Options, X-Content-Type-Options ✅
