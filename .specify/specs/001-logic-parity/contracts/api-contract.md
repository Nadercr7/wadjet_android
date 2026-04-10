# API Contract Reference

> Complete catalog of all 48 Wadjet v3 backend endpoints.
> Base URL: `https://nadercr7-wadjet-v2.hf.space`
> All `Content-Type: application/json` unless noted. Image uploads use `multipart/form-data`.

---

## Auth — `/api/auth`

| # | Method | Path | Rate | Auth | Request | Response |
|---|--------|------|------|------|---------|----------|
| 1 | POST | `/api/auth/register` | 5/min | No | `{email: EmailStr, password: str(8-128, 1upper+1lower+1digit), display_name?: str(max100)}` | **201** `{access_token, token_type:"bearer", user: UserResponse}` + cookies |
| 2 | POST | `/api/auth/login` | 10/min | No | `{email: EmailStr, password: str(max128)}` | **200** `{access_token, token_type:"bearer", user: UserResponse}` + cookies. **429** after 10 fails (15-min lockout) |
| 3 | POST | `/api/auth/refresh` | 10/min | Cookie | reads `wadjet_refresh` cookie | **200** `{access_token, token_type:"bearer"}` — rotates refresh |
| 4 | POST | `/api/auth/logout` | 10/min | Cookie | reads `wadjet_refresh` cookie | **200** `{detail:"Logged out"}` — clears cookies |
| 5 | POST | `/api/auth/google` | 10/min | No | `{credential: str(1-4096)}` — Google ID token | **200/201** `{access_token, token_type:"bearer", user: UserResponse}` |
| 6 | POST | `/api/auth/send-verification` | 3/min | Bearer | (none) | **200** `{detail:"Verification email sent"}` or `"Email already verified"` |
| 7 | POST | `/api/auth/verify-email` | 10/min | No | `{token: str(1-200)}` | **200** `{detail:"Email verified successfully"}` |
| 8 | GET | `/api/auth/verify-email` | — | No | `?token=str` | **302** redirect |
| 9 | POST | `/api/auth/forgot-password` | 3/min | No | `{email: EmailStr}` | **200** `{detail:"If an account exists, a reset email has been sent"}` (anti-enumeration) |
| 10 | POST | `/api/auth/reset-password` | 5/min | No | `{token: str(1-200), new_password: str(8-128)}` | **200** `{detail:"Password reset successfully"}` — invalidates all refresh tokens |

### UserResponse Schema
```json
{
  "id": "str", "email": "str", "display_name": "str|null",
  "preferred_lang": "str", "tier": "str",
  "auth_provider": "email|google", "email_verified": true,
  "avatar_url": "str|null", "created_at": "datetime"
}
```

---

## Scan — `/api/scan`

| # | Method | Path | Rate | Auth | Request | Response |
|---|--------|------|------|------|---------|----------|
| 11 | POST | `/api/scan` | 30/min | Optional | **Multipart:** `file` (JPEG/PNG/WebP/HEIC, max 10MB), `translate` (bool, default true), `mode` ("auto"\|"ai"\|"onnx", default "auto") | **200** ScanResponse |

### ScanResponse Schema
```json
{
  "glyphs": [{"x1":f,"y1":f,"x2":f,"y2":f,"confidence":f,"class_confidence":f,"gardiner_code":"str","low_confidence":bool}],
  "glyph_count": 0,
  "gardiner_sequence": "str",
  "transliteration": "str",
  "reading_direction": "str",
  "layout_mode": "str",
  "total_ms": 0.0,
  "detection_source": "ai_vision (gemini)|onnx|onnx_fallback|...",
  "mode": "auto|ai|onnx",
  "ai_calls_used": 0,
  "image_size": {"width":0,"height":0},
  "confidence_summary": {"avg":f,"min":f,"max":f,"low_count":0},
  "quality_hints": ["str"],
  "ai_reading": {"notes":"str","...":"..."}
}
```

---

## Explore — `/api/landmarks` + `/api/explore`

| # | Method | Path | Rate | Auth | Request | Response |
|---|--------|------|------|------|---------|----------|
| 12 | GET | `/api/landmarks` | 60/min | No | `?category&subcategory&city&search&parent&include_children(bool)&page(≥1)&per_page(1-100)` | `{landmarks:[LandmarkSummary],count,total,page,has_more}` |
| 13 | GET | `/api/landmarks/categories` | 60/min | No | — | `{types:[{name,count}],cities:[{name,count}],category_tree:[{name,count,subcategories}],total}` |
| 14 | GET | `/api/landmarks/{slug}` | 60/min | No | — | LandmarkDetail |
| 15 | GET | `/api/landmarks/{slug}/children` | 60/min | No | — | `{parent_slug,children:[LandmarkSummary],count}` |
| 16 | POST | `/api/explore/identify` | 20/min | No | **Multipart:** `file` (JPEG/PNG/WebP, max 10MB) | IdentifyResponse |

### IdentifyResponse Schema (CRITICAL — Android DTO was wrong)
```json
{
  "name": "str", "confidence": 0.95, "slug": "str",
  "source": "onnx|gemini|grok|ensemble",
  "agreement": "full|partial|tiebreak|single|best_confidence",
  "description": "AI-generated description",
  "is_known_landmark": true, "is_egyptian": true,
  "top3": [{"slug":"str","name":"str","confidence":0.0}]
}
```

### LandmarkSummary Fields
`slug, name, name_ar, city, type, subcategory, era, period, popularity, description, coordinates:[lat,lng]|null, maps_url, thumbnail, image_count, tags:[], related_sites:[], parent_slug|null, children_slugs:[], featured:bool, images:[{url,caption}], sections:[], source`

### LandmarkDetail (adds to Summary)
`dynasty, highlights, visiting_tips, historical_significance, notable_pharaohs, notable_tombs, notable_features, key_artifacts, architectural_features, wikipedia_extract, wikipedia_url, original_image, children:[{slug,name,name_ar,description,thumbnail,featured,tags,subcategory}], parent:{slug,name,name_ar}, recommendations:[{slug,name,score:f,reasons:[str]}]`

---

## Dictionary — `/api/dictionary`

| # | Method | Path | Rate | Auth | Request | Response |
|---|--------|------|------|------|---------|----------|
| 17 | GET | `/api/dictionary` | — | No | `?category(A-Z,Aa)&search&type(uni/bi/tri/logo/det/num/abbr)&page&per_page(1-200)&lang(en|ar)` | `{signs:[SignDict],count,total,page,per_page,total_pages}` |
| 18 | GET | `/api/dictionary/categories` | — | No | `?lang` | `{categories:[{code,name,count}],total_signs}` |
| 19 | GET | `/api/dictionary/alphabet` | — | No | `?lang` | `{signs:[SignDict],count}` (25 uniliteral) |
| 20 | GET | `/api/dictionary/lesson/{level}` | — | No | level 1-5, `?lang` | LessonResponse |
| 21 | GET | `/api/dictionary/speak` | 30/min | No | `?text(1-100)` | **audio/wav** binary |
| 22 | GET | `/api/dictionary/{code}` | — | No | `?lang` | SignDict + `{example_usages, related_signs}` |

### SignDict Fields
`code, transliteration, type, type_name, description, category, category_name, reading, logographic_value|null, determinative_class|null, unicode_char, is_phonetic:bool, pronunciation:{sound,example}|null, fun_fact|null, speech_text|null`

### LessonResponse
```json
{
  "level":1, "title":"str", "subtitle":"str", "description":"str",
  "intro_paragraphs":["str"], "tip":"str",
  "prev_lesson":{"level":0,"title":"str"}|null,
  "next_lesson":{"level":2,"title":"str"}|null,
  "total_lessons":5,
  "signs":[SignDict], "count":0,
  "example_words":[{"hieroglyphs":"str","codes":[],"transliteration":"str","translation":"str","highlight_codes":[],"speech_text":null}],
  "practice_words":[{"hieroglyphs":"str","transliteration":"str","translation":"str","hint":"str","speech_text":null}]
}
```

---

## Write — `/api/write`

| # | Method | Path | Rate | Auth | Request | Response |
|---|--------|------|------|------|---------|----------|
| 23 | POST | `/api/write` | 30/min | No | `{text:str(1-500), mode:"alpha"|"mdc"|"smart"}` | `{glyphs:[GlyphItem], hieroglyphs:str, input:str, mode:str, provider:str}` |
| 24 | GET | `/api/write/palette` | 60/min | No | — | `{groups:{uniliteral:[PaletteSign],biliteral:[],triliteral:[],logogram:[]}}` |

### GlyphItem
`{type:"glyph"|"separator"|"unknown", code:str, transliteration:str, unicode_char:str, description:str, verified:bool}`

### PaletteSign
`{code, transliteration, unicode_char, description, phonetic_value}`

**Provider values:** `"local"` (alpha), `"mdc_detect"`, `"shortcut"`, `"alpha_fallback"`, or AI name (gemini/groq/grok)

---

## Translate — `/api/translate`

| # | Method | Path | Rate | Auth | Request | Response |
|---|--------|------|------|------|---------|----------|
| 25 | POST | `/api/translate` | 30/min | No | `{transliteration:str(max2000), gardiner_sequence:str(max2000,default"")}` | `{transliteration, english, arabic, context, error, provider, latency_ms, from_cache:bool}` |

---

## Audio — `/api/audio` + `/api/stt`

| # | Method | Path | Rate | Auth | Request | Response |
|---|--------|------|------|------|---------|----------|
| 26 | POST | `/api/stt` | 10/min | No | **Multipart:** `file` (wav/mp3/webm/m4a/ogg, max 25MB), `lang(default"en")` | `{text, language}` |
| 27 | POST | `/api/audio/speak` | 20/min | No | `{text:str(1-5000), lang:str(max5,default"en"), context:str(a-z_,1-30,default"default")}` | **audio/wav** binary OR **204** (signal for local TTS) |

### TTS Context → Voice Mapping (server-side)
| Context | Gemini Voice | Fallback |
|---------|-------------|----------|
| `thoth_chat` | Orus | Groq PlayAI |
| `story_narration` | Aoede | Groq PlayAI |
| `default` | Charon | Groq PlayAI |
| `dictionary_speak` | Charon | Groq PlayAI |

---

## Stories — `/api/stories`

| # | Method | Path | Rate | Auth | Request | Response |
|---|--------|------|------|------|---------|----------|
| 28 | GET | `/api/stories` | 60/min | No | — | `{stories:[StoryMeta],count}` |
| 29 | GET | `/api/stories/{id}` | 60/min | No | — | Full story with chapters |
| 30 | GET | `/api/stories/{id}/chapters/{idx}` | 60/min | No | — | `{chapter,story_id,total_chapters}` |
| 31 | POST | `/api/stories/{id}/interact` | 120/min | No | `{chapter_index:int, interaction_index:int, answer:str(1-200)}` | Varies (see below) |
| 32 | POST | `/api/stories/{id}/chapters/{idx}/image` | 10/min | No | — | `{image_url:str|null, status:"ok"|"no_prompt"|"generation_failed"}` |

### Interaction Response Variants
- **choose_glyph**: `{correct:bool, type, explanation:{en,ar}, correct_answer:str}`
- **write_word**: `{correct:bool, type, target_glyph|null, gardiner_code|null, hint:{en,ar}|null}`
- **glyph_discovery**: `{correct:true, type}`
- **story_decision**: `{correct:true, type, choice_id, outcome:{en,ar}}`

---

## Chat — `/api/chat`

| # | Method | Path | Rate | Auth | Request | Response |
|---|--------|------|------|------|---------|----------|
| 33 | POST | `/api/chat` | 30/min | No | `{message:str(1-2000), session_id:str(1-128), landmark:str|null(max200)}` | `{reply, sources}` |
| 34 | POST | `/api/chat/stream` | 30/min | No | Same as above | **SSE:** `data:{"text":"chunk"}` ... `data:[DONE]` |
| 35 | POST | `/api/chat/clear` | 10/min | Optional | `{session_id:str(1-128)}` | `{status:"cleared"}` |

---

## User — `/api/user`

| # | Method | Path | Rate | Auth | Request | Response |
|---|--------|------|------|------|---------|----------|
| 36 | GET | `/api/user/profile` | — | Bearer | — | UserResponse |
| 37 | PATCH | `/api/user/profile` | — | Bearer | `{display_name?:str(max100), preferred_lang?:"en"|"ar"}` | UserResponse |
| 38 | PATCH | `/api/user/password` | — | Bearer | `{current_password:str(max128), new_password:str(8-128)}` | `{ok:true}` |
| 39 | GET | `/api/user/history` | — | Bearer | — | `[{id, results_json, confidence_avg, glyph_count, created_at}]` |
| 40 | GET | `/api/user/favorites` | — | Bearer | — | `[{id, item_type, item_id, created_at}]` |
| 41 | POST | `/api/user/favorites` | — | Bearer | `{item_type:"landmark"|"glyph"|"story", item_id:str(1-200)}` | **201** `{id, item_type, item_id}` |
| 42 | DELETE | `/api/user/favorites/{type}/{id}` | — | Bearer | — | `{ok:true}` |
| 43 | GET | `/api/user/stats` | — | Bearer | — | Aggregated stats |
| 44 | GET | `/api/user/progress` | — | Bearer | — | `[{id, story_id, chapter_index, glyphs_learned, score, completed, updated_at}]` |
| 45 | POST | `/api/user/progress` | — | Bearer | `{story_id:str(1-50), chapter_index:int, glyphs_learned:str(max5000), score:int, completed:bool}` | `{id, story_id, chapter_index, completed}` |
| 46 | GET | `/api/user/limits` | — | Bearer | — | `{tier, limits:{scans_per_day:10, chat_messages_per_day:20, stories_accessible:3}, usage:{scans_today:int}}` |

---

## Feedback — `/api/feedback`

| # | Method | Path | Rate | Auth | Request | Response |
|---|--------|------|------|------|---------|----------|
| 47 | POST | `/api/feedback` | 5/min | No | `{category:"bug"|"suggestion"|"praise"|"other", message:str(10-1000), page_url?:str(max200), name?:str(max100), email?:str(max200)}` | **201** `{ok:true, id:int}` |
| 48 | GET | `/api/feedback` | — | Admin | `?limit(max200)&offset` | `[{id, category, message, page_url, name, email, user_agent, created_at}]` |

---

## Rate Limiter Architecture
- **Engine**: slowapi with per-IP key via `X-Forwarded-For` (configurable `TRUSTED_PROXY_DEPTH`)
- **Login lockout**: 10 failed attempts → 15-min lockout per email (in-memory)
- **Image gen**: max 3 concurrent (semaphore)
- **429 response**: Standard HTTP 429 with `Retry-After` header

## Android Auth Flow (differs from web cookies)
The web uses httpOnly cookies for refresh tokens. The Android app:
1. Stores tokens in `EncryptedSharedPreferences` (not cookies)
2. Sends `Authorization: Bearer <access_token>` header
3. For refresh: sends refresh token in request body (not cookie)
4. `AuthInterceptor` handles 401 → mutex-locked refresh → retry
