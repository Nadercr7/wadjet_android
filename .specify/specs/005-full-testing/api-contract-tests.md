# API Contract Tests

## Test per Endpoint

| # | Method | Endpoint | Request | Expected Status | Expected Fields | Android DTO | Match? |
|---|--------|----------|---------|-----------------|-----------------|-------------|--------|
| 1 | GET | `/api/dictionary?page=1&per_page=50` | — | 200 | signs[], total, page, per_page, total_pages | DictionaryResponse | ✅ |
| 2 | GET | `/api/dictionary/categories` | — | 200 | categories[{code, name, count}], total_signs | CategoriesResponse | ✅ (missing total_signs) |
| 3 | GET | `/api/dictionary/alphabet` | — | 200 | signs[], count | AlphabetResponse | ✅ |
| 4 | GET | `/api/dictionary/{code}` | code=A1 | 200 | 15 sign fields | SignDetailDto | ✅ |
| 5 | GET | `/api/dictionary/{code}` | code=NONEXISTENT | 404 | detail | ErrorResponse | ✅ |
| 6 | GET | `/api/dictionary/lesson/{level}` | level=beginner | 200 | lesson data | LessonResponse | ✅ |
| 7 | GET | `/api/landmarks?page=1&per_page=24` | — | 200 | landmarks[], total | LandmarkListResponse | ⚠️ DTO has page/perPage/totalPages but backend doesn't send them |
| 8 | GET | `/api/landmarks/categories` | — | 200 | types[], cities[] | LandmarkCategoriesResponse | ✅ |
| 9 | GET | `/api/landmarks/{slug}` | slug=great-pyramid | 200 | 20+ fields | LandmarkDetailDto | ❌ `parent` vs `parent_slug`, `children` vs `children_slugs` |
| 10 | GET | `/api/landmarks/{slug}/children` | slug=giza | 200 | children[] | LandmarkChildrenResponse | ✅ |
| 11 | GET | `/api/stories` | — | 200 | stories[], count=12 | StoriesListResponse | ✅ |
| 12 | GET | `/api/stories/{storyId}` | valid id | 200 | Full story + chapters | StoryFullDto | ✅ |
| 13 | POST | `/api/stories/{id}/interact` | type, data | 200 | InteractResponse | InteractResponse | ✅ |
| 14 | POST | `/api/stories/{id}/chapters/{idx}/image` | — | 200 | image_url | ChapterImageResponse | ✅ |
| 15 | POST | `/api/chat/stream` | message, session_slug | 200 (SSE) | data: chunks | Raw OkHttp SSE | ✅ |
| 16 | POST | `/api/chat/clear` | session_slug | 200 | status | ClearChatResponse | ✅ |
| 17 | POST | `/api/audio/speak` | text, voice, context | 200 | WAV bytes | ResponseBody | ✅ |
| 18 | POST | `/api/audio/speak` | empty text | 204 | No content | ResponseBody | ✅ (handled) |
| 19 | POST | `/api/audio/stt` | file (multipart) | 200 | text | SttResponse | ✅ |
| 20 | POST | `/api/scan` | file + mode (multipart) | 200 | scan result | ScanResponse | ✅ |
| 21 | POST | `/api/explore/identify` | file (multipart) | 200 | results[] | IdentifyResponse | ✅ |
| 22 | POST | `/api/translate` | text, direction | 200 | translation | TranslateResponse | ✅ |
| 23 | GET | `/api/write/palette` | — | 200 | groups (6) | PaletteGroupsDto | ❌ Missing `numbers`, `determinative` |
| 24 | POST | `/api/write/compose` | signs[] | 200 | composed text | ComposeResponse | ✅ |
| 25 | POST | `/api/auth/register` | name, email, password | 200 | access_token, user | AuthResponse | ✅ |
| 26 | POST | `/api/auth/login` | email, password | 200 | access_token, user | AuthResponse | ✅ |
| 27 | POST | `/api/auth/google` | id_token | 200 | access_token, user | AuthResponse | ✅ |
| 28 | POST | `/api/auth/refresh` | cookie | 200 | access_token | AuthResponse | ✅ |
| 29 | POST | `/api/auth/logout` | — | 200 | — | Unit | ✅ |
| 30 | POST | `/api/auth/forgot-password` | email | 200 | — | Unit | ✅ |
| 31 | GET | `/api/user/profile` | Bearer token | 200 | user object | UserResponse | ✅ |
| 32 | PATCH | `/api/user/profile` | name | 200 | updated user | UserResponse | ✅ |
| 33 | PATCH | `/api/user/password` | old, new | 200 | ok | OkResponse | ✅ |
| 34 | GET | `/api/user/history` | Bearer token | 200 | history[] | List<ScanHistoryItemDto> | ✅ |
| 35 | GET | `/api/user/favorites` | Bearer token | 200 | favorites[] | List<FavoriteItemDto> | ✅ |
| 36 | POST | `/api/user/favorites` | item_type, item_id | 200 | favorite | FavoriteItemDto | ✅ |
| 37 | DELETE | `/api/user/favorites/{type}/{id}` | — | 200 | ok | OkResponse | ✅ |
| 38 | GET | `/api/user/stats` | Bearer token | 200 | stats | UserStatsResponse | ✅ |
| 39 | GET | `/api/user/progress` | Bearer token | 200 | progress[] | List<StoryProgressItemDto> | ✅ |
| 40 | POST | `/api/user/progress` | story_id, chapter | 200 | ok | OkResponse | ✅ |
| 41 | GET | `/api/user/limits` | Bearer token | 200 | limits | UserLimitsResponse | ✅ |
| 42 | POST | `/api/feedback` | category, message, email | 200 | ok | FeedbackResponse | ✅ |
| 43 | GET | `/api/user/profile` (no auth) | — | 401 | detail | ErrorResponse | ✅ |

## Error Contract

| Status | Backend Error Body | Android Parsing | Consistent? |
|--------|-------------------|-----------------|-------------|
| 400 | `{"detail": "Validation error message"}` | Pattern A: JSON parse `detail` | ✅ Auth/Scan repos |
| 401 | `{"detail": "Not authenticated"}` | Triggers TokenAuthenticator refresh | ✅ |
| 404 | `{"detail": "Sign not found: ..."}` | Pattern B: regex `"detail":"(.+?)"` | ⚠️ Fragile regex in Dictionary/Explore |
| 429 | `{"detail": "Rate limited"}` + `Retry-After` header | Auth repo parses header; others don't | ❌ Only AuthRepo handles 429 |
| 500 | `{"detail": "Internal server error"}` | Pattern C: raw exception string | ❌ Feedback/Settings show raw error |

### Three Error Parsing Patterns (SHOULD BE UNIFIED)

| Pattern | Used By | How It Works | Risk |
|---------|---------|-------------|------|
| A (JSON) | AuthRepo, ScanRepo, UserRepo | `errorBody().string()` → JSON parse → `detail` | ✅ Correct |
| B (Regex) | DictionaryRepo, ExploreRepo | Regex `"detail":"(.+?)"` on exception message | ⚠️ Breaks if message format changes |
| C (Raw) | FeedbackRepo, SettingsVM | `e.message` passed directly to UI | ❌ Shows "HTTP 500 Internal Server Error" to user |

## Test Script (Python httpx)

```python
#!/usr/bin/env python3
"""API Contract Test Suite for Wadjet Backend.
Run: python api_contract_tests.py
Requires: pip install httpx
"""
import httpx
import json
import sys

BASE = "https://nadercr7-wadjet-v2.hf.space"
PASS = 0
FAIL = 0

def check(name: str, response: httpx.Response, expected_status: int, required_fields: list[str] = None):
    global PASS, FAIL
    ok = response.status_code == expected_status
    if ok and required_fields:
        data = response.json()
        for field in required_fields:
            if field not in data:
                ok = False
                break
    status = "PASS" if ok else "FAIL"
    if ok:
        PASS += 1
    else:
        FAIL += 1
    print(f"  [{status}] {name} — {response.status_code} ({response.elapsed.total_seconds():.2f}s)")
    if not ok and required_fields:
        data = response.json()
        missing = [f for f in required_fields if f not in data]
        if missing:
            print(f"         Missing fields: {missing}")

client = httpx.Client(base_url=BASE, timeout=30.0)

# === PUBLIC ENDPOINTS (no auth) ===
print("\n=== Dictionary ===")
check("GET /api/dictionary", client.get("/api/dictionary", params={"page": 1, "per_page": 2}), 200, ["signs", "total", "page"])
check("GET /api/dictionary/categories", client.get("/api/dictionary/categories"), 200, ["categories"])
check("GET /api/dictionary/alphabet", client.get("/api/dictionary/alphabet"), 200, ["signs"])
check("GET /api/dictionary/A1", client.get("/api/dictionary/A1"), 200, ["code", "transliteration", "unicode_char"])
check("GET /api/dictionary/NONEXISTENT (404)", client.get("/api/dictionary/NONEXISTENT"), 404, ["detail"])

print("\n=== Landmarks ===")
check("GET /api/landmarks", client.get("/api/landmarks", params={"page": 1, "per_page": 2}), 200, ["landmarks", "total"])
check("GET /api/landmarks/categories", client.get("/api/landmarks/categories"), 200, ["types", "cities"])

print("\n=== Stories ===")
check("GET /api/stories", client.get("/api/stories"), 200, ["stories"])

print("\n=== Write ===")
r = client.get("/api/write/palette")
check("GET /api/write/palette", r, 200, ["groups"])
if r.status_code == 200:
    groups = r.json().get("groups", {})
    expected_groups = ["uniliteral", "biliteral", "triliteral", "logogram", "numbers", "determinative"]
    missing_groups = [g for g in expected_groups if g not in groups]
    if missing_groups:
        print(f"  [INFO] Backend palette groups: {list(groups.keys())}")
        print(f"  [INFO] Android DTO missing: {missing_groups}")

print("\n=== Auth Guard ===")
check("GET /api/user/profile (no auth → 401)", client.get("/api/user/profile"), 401, ["detail"])

# === AUTH ENDPOINTS (need credentials) ===
print("\n=== Auth (requires test account) ===")
login_resp = client.post("/api/auth/login", json={"email": "test@test.com", "password": "test1234"})
if login_resp.status_code == 200:
    token = login_resp.json().get("access_token")
    auth_headers = {"Authorization": f"Bearer {token}"}
    check("POST /api/auth/login", login_resp, 200, ["access_token", "user"])

    print("\n=== User (authed) ===")
    check("GET /api/user/profile", client.get("/api/user/profile", headers=auth_headers), 200, ["user"])
    check("GET /api/user/stats", client.get("/api/user/stats", headers=auth_headers), 200)
    check("GET /api/user/favorites", client.get("/api/user/favorites", headers=auth_headers), 200)
    check("GET /api/user/history", client.get("/api/user/history", headers=auth_headers), 200)
    check("GET /api/user/limits", client.get("/api/user/limits", headers=auth_headers), 200)
    check("GET /api/user/progress", client.get("/api/user/progress", headers=auth_headers), 200)
else:
    print(f"  [SKIP] Login failed ({login_resp.status_code}) — skipping authed endpoints")

# === LANDMARK DETAIL (check parent/children mismatch) ===
print("\n=== Landmark Detail (DTO mismatch check) ===")
r = client.get("/api/landmarks")
if r.status_code == 200:
    landmarks = r.json().get("landmarks", [])
    if landmarks:
        slug = landmarks[0].get("slug", "great-pyramid")
        detail = client.get(f"/api/landmarks/{slug}")
        check(f"GET /api/landmarks/{slug}", detail, 200, ["slug", "name"])
        if detail.status_code == 200:
            data = detail.json()
            has_parent_slug = "parent_slug" in data
            has_parent = "parent" in data
            has_children_slugs = "children_slugs" in data
            has_children = "children" in data
            print(f"  [INFO] Backend sends: parent_slug={has_parent_slug}, children_slugs={has_children_slugs}")
            print(f"  [INFO] Android expects: parent={has_parent}, children={has_children}")
            if has_parent_slug and not has_parent:
                print(f"  [FAIL] DTO MISMATCH: Android expects 'parent' (object) but backend sends 'parent_slug' (string)")
                FAIL += 1

print(f"\n{'='*50}")
print(f"Results: {PASS} passed, {FAIL} failed, {PASS+FAIL} total")
sys.exit(1 if FAIL > 0 else 0)
```
