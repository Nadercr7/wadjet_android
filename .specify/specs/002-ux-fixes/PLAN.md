# Wadjet Android — UX Fixes Plan (Spec 002)

> **Goal:** Fix 9 UX issues found during hands-on testing. Each issue is verified via deep code investigation.
> **Backend:** `https://nadercr7-wadjet-v2.hf.space` | **Source:** `D:\Personal attachements\Projects\Wadjet-v3-beta`
> **Android:** `D:\Personal attachements\Projects\Wadjet-Android`

---

## Issue 1: Scan Arabic Translation is Literal (Not Natural)

### Root Cause
Translation is **100% server-side**. The Android app is a transparent pass-through — `ScanResponse.translation_ar` is displayed as-is with zero processing. The backend's AI prompt likely translates hieroglyphs → English → Arabic mechanically.

### Files Involved
| File | Role |
|------|------|
| `core/network/model/ScanModels.kt` | DTO: `translation_ar` field |
| `core/data/repository/ScanRepositoryImpl.kt` | 1:1 mapping, no transformation |
| `feature/scan/screen/ScanResultScreen.kt` | Displays text with no RTL treatment |

### Tasks
1. **[BACKEND]** Fix the Arabic translation prompt in the scan pipeline — it should produce natural, contextual Arabic (فصحى مبسطة), not a literal word-for-word translation of the English. The prompt should ask for meaning-first Arabic with Egyptological context.
2. **[ANDROID]** Wrap Arabic text block in `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)` for proper RTL rendering.
3. **[ANDROID]** Set `textDirection = TextDirection.Rtl` and appropriate Arabic-friendly typography.

---

## Issue 2: Some Landmarks Have No Images

### Root Cause
All images are remote URLs from the backend. Some landmarks have `thumbnail: null` and/or `images: []`. There's also an `original_image` API field that the Android DTO **ignores entirely**. When both are null, the detail screen shows NO hero image — not even a placeholder.

### Files Involved
| File | Role |
|------|------|
| `core/network/model/LandmarkModels.kt` | `LandmarkDetailDto` missing `original_image` field |
| `core/data/repository/ExploreRepositoryImpl.kt` | DTO→Domain mapper drops `original_image` |
| `feature/explore/screen/LandmarkDetailScreen.kt` | No hero image when images=[] AND thumbnail=null |
| `feature/explore/screen/ExploreScreen.kt` | `LandmarkCard` falls back to placeholder correctly |

### Tasks
1. **[BACKEND]** Audit all landmarks in the database — every landmark MUST have at least a `thumbnail` URL. Populate missing ones with curated photos (Wikimedia Commons, CC-licensed).
2. **[ANDROID]** Add `original_image` field to `LandmarkDetailDto` (`@SerialName("original_image") val originalImage: String? = null`) and map it to the domain model as a fallback.
3. **[ANDROID]** Fix `LandmarkDetailScreen` hero section: when `images` is empty AND `thumbnail` is null, show a full-size placeholder (`ic_placeholder_landmark`) instead of nothing.
4. **[ANDROID]** Fix children cards in `LandmarkDetailScreen` — currently `child.thumbnail?.let {}` skips the entire image. Always render `AsyncImage` with placeholder/fallback.
5. **[ANDROID]** Add image URL to recommendation cards (they currently show text only).

---

## Issue 3: Dictionary Sound Button on Non-Pronounceable Signs

### Root Cause
`SignDetailSheet.kt` shows the speaker button whenever `speechText` or `reading` is non-blank — with **no check on sign type**. Determinatives (silent classifiers) have `reading` fields populated with descriptions like "classifier seeded man" instead of null. The backend is returning descriptions in the `reading` field for non-phonetic signs.

### Files Involved
| File | Role |
|------|------|
| `feature/dictionary/sheet/SignDetailSheet.kt` | TTS button visibility logic (line ~174) |
| `core/domain/model/Dictionary.kt` | `Sign` has `isPhonetic: Boolean` and `type: String` |
| `core/data/repository/DictionaryRepositoryImpl.kt` | Maps DTO→domain |

### Tasks
1. **[ANDROID]** Fix `SignDetailSheet.kt` TTS button visibility — add type guard:
   ```kotlin
   val canPronounce = sign.isPhonetic || sign.type !in listOf("determinative")
   val ttsText = sign.speechText?.takeIf { it.isNotBlank() }
       ?: sign.reading?.takeIf { it.isNotBlank() && canPronounce }
   ```
2. **[BACKEND]** Fix the sign data: `reading` field should be `null` for all determinatives/classifiers. Descriptions belong in `description` only.
3. **[ANDROID]** Pass `lang = "egy"` or a custom context hint when calling TTS for dictionary signs so the server uses Egyptological pronunciation, not English TTS reading descriptions.

---

## Issue 4: No Pronunciation in Write Feature

### Root Cause
The Write speaker button passes **raw Unicode hieroglyphs** (e.g., `𓉔𓂋𓃭`) to `/api/audio/speak`. No TTS engine can pronounce Unicode hieroglyphs. The API response already contains per-glyph `transliteration` values (e.g., `"anx"`, `"wDA"`) that should be concatenated and sent instead. Also, the TTS `context` is `"pronunciation"` which isn't mapped to any voice on the server.

### Files Involved
| File | Role |
|------|------|
| `feature/dictionary/screen/WriteTab.kt` | Speaker icon onClick sends `result.hieroglyphs` |
| `feature/dictionary/WriteViewModel.kt` | `speakResult()` — needs to build proper TTS text |
| `core/domain/model/Dictionary.kt` | `WriteResult` has `glyphs: List<WriteGlyph>` with `transliteration` per glyph |
| `core/data/repository/DictionaryRepositoryImpl.kt` | `speakPhonetic()` sends `SpeakRequest` |
| `core/network/model/WriteModels.kt` | `SpeakRequest(text, lang, context, voice?, style?)` |

### Tasks
1. **[ANDROID]** Fix Write TTS text: collect all `glyph.transliteration` from `WriteResult.glyphs`, join them with spaces, and send THAT to `/api/audio/speak` instead of the raw Unicode hieroglyphs.
2. **[ANDROID]** Use `context = "dictionary_speak"` (mapped server-side to Charon voice) instead of `"pronunciation"`.
3. **[BACKEND]** Create a dedicated Egyptological pronunciation endpoint or enhance `/api/audio/speak` to accept `context: "hieroglyphic_reading"` — this should use reconstructed Middle Egyptian phonetics with proper vowel insertion (e.g., `anx` → "ankh", `nfr` → "nefer").
4. **[ANDROID]** Add a loading popup (use `WadjetToast.Info`) saying "Generating pronunciation…" when TTS is triggered, since server-side generation takes time.
5. **[QUALITY]** Verify pronunciation accuracy: the TTS should reflect scholarly Egyptological consensus on pronunciation (e.g., `ꜥnḫ` = "ankh", `wḏꜣ` = "wedja", `snb` = "seneb"). Document expected pronunciations.

---

## Issue 5: Remove Translate Tab

### Root Cause
The Translate tab takes **MdC transliteration** (expert Egyptological notation like `anx wDA snb`) and translates it to English/Arabic. This requires scholarly knowledge that 99% of users don't have. Write already converts English → Hieroglyphs. For the reverse direction, Scan already handles it (image → translation). Translate is effectively dead weight.

### Files Involved
| File | Role |
|------|------|
| `feature/dictionary/screen/DictionaryScreen.kt` | 4-tab pager: Browse, Learn, Write, Translate |
| `feature/dictionary/screen/TranslateTab.kt` | Entire tab composable |
| `feature/dictionary/TranslateViewModel.kt` | ViewModel for translate |
| `core/domain/repository/TranslateRepository.kt` | Repository interface |
| `core/data/repository/TranslateRepositoryImpl.kt` | Implementation |
| `core/network/api/TranslateApiService.kt` | Retrofit API |
| `core/network/model/TranslateModels.kt` | DTOs |

### Tasks
1. **[ANDROID]** Remove the "Translate" tab from `DictionaryScreen.kt` — change from 4 tabs to 3 (Browse, Learn, Write).
2. **[ANDROID]** Keep `TranslateTab.kt`, `TranslateViewModel.kt`, and the network/data/domain translate layer in the codebase (don't delete) — just disconnect from the tab. It could be re-added later as a power-user feature behind a setting.
3. **[ANDROID]** Update tab index logic — the indices for initial tab navigation (e.g., Landing → Write tab) need to be adjusted from index 3→2 (Write was tab 2, now it's the last tab at index 2 still, so should be fine after removing Translate from position 3).

---

## Issue 6: Stories Icons Don't Match Visual Identity

### Root Cause
`StoryCard` in `StoriesScreen.kt` renders `story.coverGlyph` (a Unicode hieroglyph) at 36sp with **no `fontFamily` specified**. The system fallback font renders these as ugly tofu boxes or generic glyphs. Meanwhile, the completion screen in `StoryReaderScreen.kt` correctly uses `fontFamily = NotoSansEgyptianHieroglyphs`. It's a single missing prop.

### Files Involved
| File | Role |
|------|------|
| `feature/stories/screen/StoriesScreen.kt` line ~240 | `StoryCard` cover glyph `Text()` — missing fontFamily |
| `feature/stories/screen/StoryReaderScreen.kt` line ~330 | Completion screen — has fontFamily (correct) |
| `core/designsystem/WadjetFonts.kt` | `NotoSansEgyptianHieroglyphs` font defined |

### Tasks
1. **[ANDROID]** Add `fontFamily = NotoSansEgyptianHieroglyphs` to the cover glyph `Text()` in `StoryCard` composable in `StoriesScreen.kt`.
2. **[ANDROID]** Verify the gradient background colors behind the glyph match the app's gold/dark Egyptian palette per difficulty level.
3. **[ANDROID]** Also check `DashboardScreen.kt` `StoryProgressRow` — it shows raw `storyId` as title and no cover glyph.

---

## Issue 7: Stories Images Not Loading

### Root Cause
Story scene images are **generated on-demand** via `POST /api/stories/{id}/chapters/{idx}/image` → Cloudflare Workers AI (FLUX/SDXL). This depends on:
- The AI image generation service being available (Cloudflare uptime)
- The server having `sceneImagePrompt` for each chapter
- HuggingFace Spaces not losing cached images on restart (ephemeral `/static/cache/images/`)

Failures are **silent** — `loadChapterImage()` catches errors and only logs them. The user sees a hieroglyph fallback "𓁟" with no indication that loading failed.

### Files Involved
| File | Role |
|------|------|
| `feature/stories/StoryReaderViewModel.kt` line ~218 | `loadChapterImage()` — silent error catch |
| `feature/stories/screen/StoryReaderScreen.kt` line ~420 | `SceneImage` composable — Coil `AsyncImage` |
| `core/data/repository/StoriesRepositoryImpl.kt` | `generateChapterImage()` API call |
| `core/network/api/StoriesApiService.kt` line ~30 | `POST` endpoint for image generation |

### Tasks
1. **[BACKEND]** Pre-generate all story scene images and store permanent URLs in the story data (not ephemeral cache). Each chapter's `ChapterDto` should include a `scene_image_url` field with a pre-generated/hosted image.
2. **[ANDROID]** Add `sceneImageUrl` field to `ChapterDto` and domain model. Use this as the primary image source, falling back to the generation endpoint only if null.
3. **[ANDROID]** When image generation fails, show a proper error state with retry button instead of silently falling back to a glyph.
4. **[ANDROID]** Add `WadjetToast.Info("Generating scene image…")` when the generation POST is triggered.
5. **[ANDROID]** Cache successfully generated image URLs locally (Room or SharedPreferences) to avoid re-generating on repeat visits.

---

## Issue 8: Missing Popup Notes/Toasts

### Root Cause
A fully-built `WadjetToast` component exists in `core/designsystem/component/WadjetToast.kt` (Success, Error, Info variants with animation) but is used by **ZERO screens**. Meanwhile, `ScanEvent.ShowToast` events are emitted but **never collected** (dead code). Errors in Chat, Stories, and Auth are stored in state but **never shown** to the user. Rate limit handling gives **zero user feedback**.

### Files Involved
| File | Role |
|------|------|
| `core/designsystem/component/WadjetToast.kt` | Built, animated, branded — UNUSED |
| `feature/scan/ScanViewModel.kt` | Emits `ScanEvent.ShowToast` but never collected |
| `app/navigation/WadjetNavGraph.kt` | Never collects `scanViewModel.events` |
| `feature/chat/screen/ChatScreen.kt` | `state.error` set but not displayed |
| `feature/stories/StoryReaderViewModel.kt` | Narration errors silent |
| `core/network/RateLimitInterceptor.kt` | Retries silently, no user feedback |

### Tasks
1. **[ANDROID]** Wire `WadjetToast` into a global toast system — create a `ToastController` (or similar) that any ViewModel can trigger, and place `WadjetToast` in the root `Scaffold` or `WadjetNavGraph`.
2. **[ANDROID]** Collect `ScanEvent.ShowToast` in the scan NavGraph — currently dead code.
3. **[ANDROID]** Add `WadjetToast.Info("Audio will take a moment to generate…")` for ALL TTS triggers:
   - Chat: `speakMessage()` in `ChatViewModel`
   - Stories: narration start in `StoryReaderViewModel`
   - Write: pronunciation in `WriteTab`
   - Scan: already has `TtsButton` spinner (keep this, but add toast too)
4. **[ANDROID]** Show rate limit feedback: `WadjetToast.Error("Please wait X seconds")` when 429 is received.
5. **[ANDROID]** Show chat errors as toast: daily limit reached, send failure, STT failure.
6. **[ANDROID]** Show "Chat cleared" confirmation toast after `clearChat()`.
7. **[ANDROID]** Replace raw `Toast.makeText` in Dictionary (`SignDetailSheet`) with `WadjetToast` for consistency.
8. **[ANDROID]** Add offline indicator banner (planned in PLAN.md but never wired).

---

## Issue 9: Thoth Chat UX Improvements

### Root Cause
The chat is functionally working (streaming, STT, TTS, history) but lacks polish that modern chat apps have. Message editing is **architecturally impossible** (no backend API, no data model support). Many other UX improvements are purely Android-side.

### Files Involved
| File | Role |
|------|------|
| `feature/chat/screen/ChatScreen.kt` | Full UI (625+ lines) |
| `feature/chat/ChatViewModel.kt` | State management, streaming, TTS |
| `feature/chat/ChatHistoryStore.kt` | JSON file-based persistence |
| `core/domain/model/Chat.kt` | `ChatMessage` — minimal fields |
| `core/data/repository/ChatRepositoryImpl.kt` | SSE streaming, audio API |

### Tasks

#### Android-Only (No Backend Changes)
1. **Copy message** — Add long-press context menu on message bubbles with "Copy" action → `ClipboardManager`.
2. **Message timestamps** — Show timestamp (relative: "2m ago", "Yesterday") below each message or on tap.
3. **Typing indicator** — Show "Thoth is typing…" animation between user-sends and first SSE chunk.
4. **Scroll-to-bottom FAB** — Show a floating button when user scrolls up in long conversations.
5. **Retry button** — Show a retry icon on failed messages (ViewModel has `retryLastMessage()` but no UI trigger).
6. **Error display** — Show `state.error` as `WadjetToast.Error` instead of silently consuming it.
7. **TTS loading indicator** — Show spinner on the speaker icon while audio is being generated (Chat has no loading state, unlike Scan's `TtsButton`).
8. **"Chat cleared" toast** — Confirmation after clearing.

#### Requires Backend Changes
9. **Message editing** — Needs: new `PUT /api/chat/message/{id}` endpoint, `editedAt` field in `ChatMessage`, ViewModel `editMessage()` function, UI edit mode with inline text field. **Defer to later phase** — requires significant backend work.

---

## Priority Order

| Priority | Issue | Effort | Scope |
|----------|-------|--------|-------|
| **P0** | #8 Toast system (blocks all feedback) | Medium | Android |
| **P1** | #3 Dictionary sound button guard | Small | Android + Backend |
| **P1** | #6 Stories font fix | Tiny | Android |
| **P1** | #4 Write pronunciation fix | Medium | Android + Backend |
| **P2** | #1 Scan Arabic translation quality | Medium | Backend |
| **P2** | #5 Remove Translate tab | Small | Android |
| **P2** | #7 Stories images | Large | Backend + Android |
| **P2** | #2 Landmarks images | Medium | Backend + Android |
| **P3** | #9 Chat UX improvements | Large | Android (mostly) |

---

## Execution Phases

### Phase A: Foundation (Toast System + Quick Fixes)
- Task 8.1: Wire global `WadjetToast` system
- Task 6.1: Fix story cover glyph fontFamily (1 line)
- Task 3.1: Fix dictionary sound button guard
- Task 5.1-5.3: Remove Translate tab

### Phase B: Audio & Pronunciation
- Task 4.1-4.2: Fix Write TTS text (use transliteration, not Unicode)
- Task 4.3: Backend — enhance pronunciation endpoint
- Task 4.4: Add loading toast for TTS
- Task 3.2: Backend — fix reading field for determinatives
- Task 8.3-8.7: Wire toasts for all TTS triggers, errors, rate limits

### Phase C: Content & Images
- Task 1.1: Backend — fix Arabic translation prompt
- Task 1.2-1.3: Android — RTL text handling for Arabic
- Task 2.1: Backend — populate missing landmark images
- Task 2.2-2.5: Android — add original_image fallback, fix hero/children/recs
- Task 7.1-7.5: Pre-generate story images, add caching

### Phase D: Chat Polish
- Tasks 9.1-9.8: Copy, timestamps, typing indicator, scroll FAB, retry, errors, TTS loading
- Task 9.9: (DEFERRED) Message editing — requires backend API
