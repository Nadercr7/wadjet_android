# Wadjet Android — UX Fixes Prompts (Spec 002)

> Copy-paste each phase prompt when starting that phase. Each prompt is self-contained.
> **Read the full plan first:** `.specify/specs/002-ux-fixes/PLAN.md`
> **Backend source:** `D:\Personal attachements\Projects\Wadjet-v3-beta`
> **Android workspace:** `D:\Personal attachements\Projects\Wadjet-Android`

---

## Phase A Prompt: Foundation (Toast System + Quick Fixes)

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Read the plan at ".specify/specs/002-ux-fixes/PLAN.md" for full context on all 9 issues.

TASK: Phase A — Foundation. Wire the toast system and apply quick fixes.

=== TASK 1: Global Toast System ===

A fully-built WadjetToast component exists at core/designsystem/component/WadjetToast.kt
(Success, Error, Info variants with slide-in animation, auto-dismiss). It is used by ZERO screens.

1. Create a ToastController (singleton or Hilt-provided) that any ViewModel can trigger:
   - Should expose a SharedFlow<ToastEvent> where ToastEvent = (message: String, type: Success|Error|Info, duration: Long)
   - ViewModels inject ToastController and call toastController.show("message", ToastType.Info)
2. Place a WadjetToast composable in the root scaffold/NavHost in WadjetNavGraph.kt (or MainScreen)
   that collects from ToastController and displays toasts.
3. Wire ScanEvent.ShowToast: in app/navigation/WadjetNavGraph.kt, the scan composable block
   currently NEVER collects scanViewModel.events. Add a LaunchedEffect to collect and route
   ScanEvent.ShowToast to the ToastController.
4. Replace the raw Toast.makeText in feature/dictionary/sheet/SignDetailSheet.kt with WadjetToast
   via ToastController.

=== TASK 2: Fix Story Cover Glyph Font ===

In feature/stories/screen/StoriesScreen.kt, the StoryCard composable renders story.coverGlyph
with Text() but is MISSING fontFamily. The completion screen in StoryReaderScreen.kt correctly
uses fontFamily = NotoSansEgyptianHieroglyphs (from core/designsystem/WadjetFonts.kt).

Fix: Add fontFamily = com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs to the cover
glyph Text() in StoryCard. Find the exact Text() call around line 240 in StoriesScreen.kt.
Also check DashboardScreen.kt StoryProgressRow for the same issue.

=== TASK 3: Fix Dictionary Sound Button ===

In feature/dictionary/sheet/SignDetailSheet.kt around line 174, the TTS button shows whenever
speechText or reading is non-blank. There's NO check on sign type.

Current:
  val ttsText = sign.speechText?.takeIf { it.isNotBlank() } ?: sign.reading
  if (!ttsText.isNullOrBlank()) { /* show speaker button */ }

Fix: Add a guard for non-pronounceable signs:
  val canPronounce = sign.isPhonetic || sign.type !in listOf("determinative")
  val ttsText = sign.speechText?.takeIf { it.isNotBlank() }
      ?: sign.reading?.takeIf { it.isNotBlank() && canPronounce }
  if (!ttsText.isNullOrBlank()) { /* show speaker button */ }

This ensures determinatives (silent classifiers like "seeded man") never show a speaker button.

=== TASK 4: Remove Translate Tab ===

In feature/dictionary/screen/DictionaryScreen.kt, there's a 4-tab HorizontalPager:
Browse (0), Learn (1), Write (2), Translate (3).

1. Remove the "Translate" tab entry from the tabs list and pager content.
2. Keep all Translate-related files (TranslateTab.kt, TranslateViewModel.kt, TranslateRepository,
   TranslateApiService, TranslateModels) — just disconnect from the UI.
3. The tab count goes from 4 to 3. Verify Write is still at index 2.
4. Check if any navigation passes initialTab=3 and adjust if needed.

IMPORTANT: Do NOT delete any Translate source files. Just remove from the tab pager.

After all changes, verify no compile errors and the app builds successfully.
```

---

## Phase B Prompt: Audio & Pronunciation Fixes

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Read the plan at ".specify/specs/002-ux-fixes/PLAN.md" for full context.
Phase A (toast system, font fix, sound guard, tab removal) is DONE.

TASK: Phase B — Fix pronunciation in Write feature and wire TTS loading toasts.

=== TASK 1: Fix Write TTS (Critical) ===

The Write feature's speaker button sends RAW UNICODE HIEROGLYPHS (e.g., "𓉔𓂋𓃭") to
POST /api/audio/speak. No TTS engine can pronounce Unicode hieroglyphs.

The WriteResult already contains per-glyph transliterations in WriteResult.glyphs[].transliteration.

Files to modify:
- feature/dictionary/screen/WriteTab.kt — the speaker button onClick
- feature/dictionary/WriteViewModel.kt or DictionaryViewModel.kt — speakResult/speakSign function

Fix:
1. When speaker is tapped, collect all glyph.transliteration from result.glyphs
2. Join them with spaces: "anx wDA snb"
3. Send THIS transliteration string to POST /api/audio/speak
4. Use context = "dictionary_speak" instead of "pronunciation"
   (dictionary_speak is mapped to Charon voice on the server)
5. If ALL transliterations are blank/null (unlikely), fall back to the description concatenation

Example:
  result.glyphs = [{transliteration: "anx"}, {transliteration: "wDA"}, {transliteration: "snb"}]
  → TTS text = "anx wDA snb"
  → SpeakRequest(text = "anx wDA snb", lang = "en", context = "dictionary_speak")

=== TASK 2: Wire TTS Loading Toasts ===

Using the ToastController from Phase A, add Info toasts when TTS is triggered:

1. Chat: In ChatViewModel.speakMessage(), before the API call:
   toastController.show("Generating audio…", ToastType.Info)

2. Stories: In StoryReaderViewModel when narration starts:
   toastController.show("Generating narration…", ToastType.Info)

3. Write: In the Write speak function, before calling speakPhonetic():
   toastController.show("Generating pronunciation…", ToastType.Info)

4. Wire error toasts too:
   - Chat daily limit: toastController.show("Daily chat limit reached", ToastType.Error)
   - Chat send failure: toastController.show("Failed to send message", ToastType.Error)
   - Rate limit 429: toastController.show("Please wait a moment…", ToastType.Error)

5. Add "Chat cleared" confirmation:
   In ChatViewModel.clearChat(), after success:
   toastController.show("Chat cleared", ToastType.Success)

After all changes, verify the app builds. Test TTS in Write — it should now speak
transliterations, not hieroglyph Unicode.
```

---

## Phase C Prompt: Content & Images

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
The web backend source is at "D:\Personal attachements\Projects\Wadjet-v3-beta".
Read ".specify/specs/002-ux-fixes/PLAN.md" for full context.
Phase A+B are DONE.

TASK: Phase C — Fix landmarks images, scan Arabic translation, story images.

=== TASK 1: Scan Arabic Translation (Backend) ===

The scan pipeline in the backend generates translation_ar that's too literal.
Look at the backend scan route in the Wadjet-v3-beta project — find where the AI generates
Arabic translations and improve the prompt.

The Arabic prompt should:
- Translate the MEANING, not word-for-word from English
- Use فصحى مبسطة (simplified MSA) — natural, readable Arabic
- Include Egyptological context where relevant
- Not just be Google-Translate-quality output of the English

Also in the Android app:
- In feature/scan/screen/ScanResultScreen.kt, wrap the Arabic text display with:
  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { ... }
- Set textAlign = TextAlign.Start (which will be right-aligned in RTL context)

=== TASK 2: Landmark Images (Backend + Android) ===

BACKEND:
- Audit the landmarks data in the backend.
- Every landmark MUST have at least a thumbnail URL.
- Use Wikimedia Commons CC-licensed images for any missing ones.

ANDROID:
1. In core/network/model/LandmarkModels.kt → LandmarkDetailDto:
   Add: @SerialName("original_image") val originalImage: String? = null
2. Map originalImage through domain model (core/domain/model/Landmark.kt → LandmarkDetail)
3. In ExploreRepositoryImpl.kt → toDomain mapper, map originalImage
4. In feature/explore/screen/LandmarkDetailScreen.kt:
   - When images is empty AND thumbnail is null, show placeholder hero image (ic_placeholder_landmark)
   - Use originalImage as fallback: images → thumbnail → originalImage → placeholder
5. Fix children cards: remove the ?.let{} guard on child.thumbnail. Always render AsyncImage
   with placeholder/fallback drawables.

=== TASK 3: Story Images ===

Scene images are generated on-demand via POST and depend on Cloudflare AI availability.
HuggingFace Spaces ephemeral storage means cached images are lost on restart.

BACKEND:
- Pre-generate scene images for all story chapters.
- Store URLs permanently (not in /static/cache/).
- Add a scene_image_url field to the chapter data in the API response.

ANDROID:
1. Add sceneImageUrl: String? to ChapterDto in core/network/model/StoryModels.kt
2. Map to domain model Chapter
3. In StoryReaderViewModel.loadChapterImage():
   - First try chapter.sceneImageUrl (pre-generated)
   - Only fall back to POST generation if sceneImageUrl is null
4. When generation fails, show a retry button instead of silent failure
5. Use WadjetToast.Info("Generating scene image…") when POST is triggered
6. Cache generated URLs in SharedPreferences keyed by storyId+chapterIndex
```

---

## Phase D Prompt: Chat Polish

```
I'm working on the Wadjet Android app at "D:\Personal attachements\Projects\Wadjet-Android".
Read ".specify/specs/002-ux-fixes/PLAN.md" for full context.
Phase A+B+C are DONE.

TASK: Phase D — Polish the Thoth chat UX. All changes are Android-only (no backend changes).

File: feature/chat/screen/ChatScreen.kt (625+ lines)
File: feature/chat/ChatViewModel.kt
File: core/domain/model/Chat.kt

=== 1. Copy Message ===
Add a long-press handler on message bubbles (both user and assistant).
On long press, show a popup menu with "Copy" option.
Use ClipboardManager to copy message.content to clipboard.
Show WadjetToast.Success("Copied to clipboard") via ToastController.

=== 2. Message Timestamps ===
Show relative timestamps below each message (or on tap to toggle).
Use "Just now", "2m ago", "1h ago", "Yesterday", date format.
Style: small text (12sp), WadjetColors.TextSecondary, below the bubble.

=== 3. Typing Indicator ===
When user sends a message and before the first SSE chunk arrives, show a
"Thoth is typing..." indicator in the message list.
Use the Thoth avatar (ibis hieroglyph) with an animated dots indicator.
The indicator should appear as a temporary assistant message with StreamingDots
animation (already exists in the codebase).

=== 4. Scroll-to-Bottom FAB ===
When user scrolls up in a long conversation, show a small FAB at the bottom-right
to jump back to the latest message. Hide when already at bottom.
Use LazyListState.isScrolledToEnd and a small circular button with a down-arrow icon.
Style: WadjetColors.Gold background, small size (40dp).

=== 5. Retry Button ===
ChatViewModel already has retryLastMessage(). When a message fails to send:
- Show an error icon on the failed message bubble
- Add a small "Retry" text button below the failed message
- On tap, call viewModel.retryLastMessage()

=== 6. Error Display ===
state.error is set in ChatViewModel but NEVER shown to the user
(except the LOCAL_TTS: magic prefix at ChatScreen.kt line ~131).
When state.error is non-null (and not a LOCAL_TTS: prefix), show it via
WadjetToast.Error(state.error) and then clear the error state.

=== 7. TTS Loading State ===
In ChatScreen, when a message's TTS is loading (speakMessage triggered but audio not
yet playing), show a small CircularProgressIndicator on the speaker icon instead of
the static VolumeUp icon. Track loading state per message via speakingMessageId + a
new isLoadingTts boolean in ChatUiState.

=== 8. Chat Cleared Toast ===
After clearChat() succeeds, show WadjetToast.Success("Chat cleared").

IMPORTANT:
- Do NOT implement message editing (Issue 9.9) — it needs backend API work.
- Keep changes focused on the chat module. Don't refactor unrelated code.
- Verify the app builds after all changes.
```
