# STAGE 8 REPORT: Chat & Stories Feature Audit

> Generated: 2026-04-15
> Scope: feature/chat/, feature/stories/, core layers (domain, data, network)

---

## S8-01 | CRITICAL — InteractResponse drops fields that were never mapped to domain

**File:** [core/data/src/main/java/com/wadjet/core/data/repository/StoriesRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/StoriesRepositoryImpl.kt#L119-L132)
**Description:** The `InteractResponse` DTO contains `correctAnswer`, `targetGlyph`, `gardinerCode`, `hint`, and `choiceId` fields, but the mapper to `InteractionResult` domain model drops **all five**. The domain `InteractionResult` only has `correct`, `type`, `explanationEn/Ar`, `outcomeEn/Ar`. This means:
- On wrong `choose_glyph` answers, the app cannot show which glyph was correct
- On wrong `write_word` answers, the app cannot show the target glyph or hint as corrective feedback
- On `story_decision`, the chosen path ID (`choiceId`) is lost

**Evidence — DTO has fields:**
```kotlin
// InteractResponse (network model)
@SerialName("correct_answer") val correctAnswer: String? = null,
@SerialName("target_glyph") val targetGlyph: String? = null,
@SerialName("gardiner_code") val gardinerCode: String? = null,
val hint: BilingualText? = null,
@SerialName("choice_id") val choiceId: String? = null,
```
**Evidence — Mapper drops them:**
```kotlin
InteractionResult(
    correct = r.correct,
    type = r.type,
    explanationEn = r.explanation?.en,
    explanationAr = r.explanation?.ar,
    outcomeEn = r.outcome?.en,
    outcomeAr = r.outcome?.ar,
    // correctAnswer, targetGlyph, gardinerCode, hint, choiceId — ALL DROPPED
)
```
**Impact:** Users who answer incorrectly see no correction feedback for write_word and choose_glyph types.

---

## S8-02 | MAJOR — SSE stream uses POST but OkHttp EventSource expects GET-like semantics

**File:** [core/data/src/main/java/com/wadjet/core/data/repository/ChatRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/ChatRepositoryImpl.kt#L37-L85)
**Description:** The SSE implementation manually builds a JSON body and does a `POST` via OkHttp `EventSources.createFactory()`. While this works with many servers, the SSE specification (and some intermediate proxies/CDNs) expects GET requests. The implementation bypasses Retrofit entirely and manually constructs the request, losing:
- Auth interceptor headers (the OkHttpClient may or may not have the AuthInterceptor depending on DI scope)
- Rate limit interceptor
- Retry logic

Moreover, `baseUrl` is injected as a named string — if it doesn't end with `/`, the URL `${baseUrl}api/chat/stream` could be malformed (e.g., `https://example.comapi/chat/stream`).

**Evidence:**
```kotlin
val request = Request.Builder()
    .url("${baseUrl}api/chat/stream")  // ← manual URL construction, no trailing slash check
    .post(requestBody.toRequestBody("application/json".toMediaType()))
    .header("Accept", "text/event-stream")
    .build()
// Uses raw okHttpClient, not the Retrofit-configured one necessarily
val factory = EventSources.createFactory(okHttpClient)
```

---

## S8-03 | MAJOR — ChatViewModel session mismatch after loading old conversation

**File:** [feature/chat/src/main/java/com/wadjet/feature/chat/ChatViewModel.kt](feature/chat/src/main/java/com/wadjet/feature/chat/ChatViewModel.kt#L430-L434)
**Description:** When the user loads a past conversation via `loadConversation(conversationId)`, only the message list is updated. The `sessionId` remains the **current** session ID, not the loaded conversation's session ID. Any new message sent will go to the wrong server-side session — the server has no context of the displayed conversation history.
**Evidence:**
```kotlin
fun loadConversation(conversationId: String) {
    val messages = chatHistoryStore.loadConversation(conversationId) ?: return
    _state.update {
        it.copy(messages = messages, showHistory = false)
        // ← sessionId is NOT updated to conversationId
    }
}
```
**Impact:** After loading an old conversation, the next message sent will create a reply in a different server-side session context, producing incoherent responses.

---

## S8-04 | MAJOR — Chat history stored in plain JSON files with no encryption

**File:** [feature/chat/src/main/java/com/wadjet/feature/chat/ChatHistoryStore.kt](feature/chat/src/main/java/com/wadjet/feature/chat/ChatHistoryStore.kt)
**Description:** Entire chat conversations (user messages + AI responses) are stored as plain JSON files in `context.filesDir/chat_history/`. While `filesDir` is app-private, on rooted devices or via adb backup this data is trivially readable. Chat messages may contain personal information or topics the user considers sensitive.
**Impact:** Privacy concern — chat history is unencrypted on disk.

---

## S8-05 | MEDIUM — Chat SSE stream has no retry/reconnect mechanism

**File:** [core/data/src/main/java/com/wadjet/core/data/repository/ChatRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/ChatRepositoryImpl.kt#L65-L80)
**Description:** If the SSE connection drops mid-stream (e.g., network blip), `onFailure` is called and the flow closes with an exception. There is no automatic reconnection. The 60-second timeout in `ChatViewModel` handles complete stalls, but transient network interruptions will lose partial responses with no recovery.
**Evidence:**
```kotlin
override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
    val msg = t?.message ?: response?.message ?: "SSE stream failed"
    Timber.e(t, "Chat SSE failure: $msg")
    close(Exception(msg))  // ← terminal, no retry
}
```

---

## S8-06 | MEDIUM — Story narration timing estimate is crude and unreliable

**File:** [feature/stories/src/main/java/com/wadjet/feature/stories/StoryReaderViewModel.kt](feature/stories/src/main/java/com/wadjet/feature/stories/StoryReaderViewModel.kt#L169-L178)
**Description:** When server TTS returns 204 (use local fallback), the narration pauses for an estimated duration: `text.split(" ").size * 80L + 500L` ms. This crude heuristic doesn't account for actual local TTS playback duration. The local TTS (`TextToSpeech.speak()`) is triggered via the error state mechanism in StoryReaderScreen, which is asynchronous. The narration will advance to the next paragraph before local TTS finishes speaking.
**Evidence:**
```kotlin
// In speakAndWait():
} else {
    _state.update { it.copy(error = "LOCAL_TTS:$text") }
    val estimatedMs = text.split(" ").size * 80L + 500L
    kotlinx.coroutines.delay(estimatedMs)
    if (cont.isActive) cont.resume(true)
}
```
**Impact:** Narration paragraphs overlap when using local TTS fallback.

---

## S8-07 | MEDIUM — StoryReaderViewModel.restoreProgress creates duplicate collector

**File:** [feature/stories/src/main/java/com/wadjet/feature/stories/StoryReaderViewModel.kt](feature/stories/src/main/java/com/wadjet/feature/stories/StoryReaderViewModel.kt#L227-L241)
**Description:** `restoreProgress()` calls `storiesRepository.getStoryProgress(storyId).collect {}` which is an indefinite Firestore snapshot listener. The guard `if (progress != null && _state.value.story != null && _state.value.currentChapter == 0)` only applies the restore on first load, but the collector stays alive for the ViewModel's lifetime, processing every Firestore update. If the user navigates chapters, `currentChapter != 0` and all updates are silently swallowed — wasted network/battery.

---

## S8-08 | MEDIUM — Chat suggestion chips send message before input is updated

**File:** [feature/chat/src/main/java/com/wadjet/feature/chat/screen/ChatScreen.kt](feature/chat/src/main/java/com/wadjet/feature/chat/screen/ChatScreen.kt#L373-L385)
**Description:** The suggestion chip onClick calls `onInputChanged(suggestion); onSend()` synchronously. Since `onInputChanged` updates StateFlow which is collected asynchronously, calling `onSend()` immediately after may read the old (empty) `inputText` from state. `sendMessage()` falls back to `_state.value.inputText` which races.
**Evidence:**
```kotlin
FilterChip(
    selected = false,
    onClick = { onInputChanged(suggestion); onSend() },
    // onSend reads inputText from state, which may not be updated yet
```
However, `sendMessage(text: String = _state.value.inputText)` — since `onSend` is `{ viewModel.sendMessage() }` without passing the text directly, the default parameter reads from the potentially-stale state.

---

## S8-09 | MEDIUM — Stories have no offline support at all

**File:** [core/data/src/main/java/com/wadjet/core/data/repository/StoriesRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/StoriesRepositoryImpl.kt)
**Description:** Unlike Explore (which caches landmarks in Room), the Stories feature has **zero offline caching**. Both `getStories()` and `getStory()` fail immediately on network errors. Story progress is synced to Firestore, but the actual story content is ephemeral. If the user starts a story and loses connectivity mid-chapter, they cannot continue reading or interacting.

---

## S8-10 | MEDIUM — StoriesRepositoryImpl.saveProgress silently fails both paths

**File:** [core/data/src/main/java/com/wadjet/core/data/repository/StoriesRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/StoriesRepositoryImpl.kt#L180-L210)
**Description:** `saveProgress()` persists to Firestore AND REST API. Both failures are caught and logged. If both fail (e.g., no auth/no network), progress is silently lost. No local queue or Room fallback exists. The user may complete a story and lose all progress on next app restart.
**Evidence:**
```kotlin
override suspend fun saveProgress(progress: StoryProgress) {
    val uid = firebaseAuth.currentUser?.uid ?: return  // ← silent bail if not logged in
    try {
        firestore...set(data).await()
    } catch (e: Exception) {
        Timber.e(e, "Failed to save story progress to Firestore")  // ← swallowed
    }
    try {
        userApi.saveProgress(...)
    } catch (e: Exception) {
        Timber.w(e, "Failed to sync progress to REST API")  // ← swallowed
    }
}
```

---

## S8-11 | MEDIUM — GlyphDiscovery interaction is auto-submitted but logic is unclear

**File:** [feature/stories/src/main/java/com/wadjet/feature/stories/StoryReaderViewModel.kt](feature/stories/src/main/java/com/wadjet/feature/stories/StoryReaderViewModel.kt#L102-L109)
**Description:** `submitAnswer()` handles all interaction types uniformly — it sends the answer to the server. For `GlyphDiscovery`, there's no "answer" per se (it's a reveal-and-learn interaction). The glyph is always added to `glyphsLearned` regardless of `result.correct`, which is correct behavior, but the server call is unnecessary overhead for discovery interactions that are always "correct".

---

## S8-12 | LOW — Chat `playWavBytes` writes temp file without `deleteOnExit` in some paths

**File:** [feature/chat/src/main/java/com/wadjet/feature/chat/ChatViewModel.kt](feature/chat/src/main/java/com/wadjet/feature/chat/ChatViewModel.kt#L282-L305)
**Description:** `playWavBytes()` creates a temp file with `File.createTempFile("tts_", ".wav")` and calls `tempFile.deleteOnExit()`. This is fine, but `deleteOnExit()` only works when the JVM shuts down cleanly (which Android doesn't guarantee). The `setOnCompletionListener` correctly deletes the file, but if the MediaPlayer errors silently or the app is killed, files accumulate.

---

## S8-13 | LOW — ChatViewModel.clearChat creates new greeting but reuses old sessionId

**File:** [feature/chat/src/main/java/com/wadjet/feature/chat/ChatViewModel.kt](feature/chat/src/main/java/com/wadjet/feature/chat/ChatViewModel.kt#L407-L427)
**Description:** `clearChat()` calls `chatRepository.clearSession(sessionId)` to clear the server session, then clears the local stored session ID. However, the ViewModel's `sessionId` property is a `val` initialized in `init` — it cannot change. Any messages sent after clearing will still use the old `sessionId`, which was just cleared on the server. The server may treat this as a new empty session (which works), but it's semantically confusing and creates a zombie session reference.

---

## S8-14 | LOW — Story `FREE_STORY_LIMIT` is hardcoded in UI, not server-driven

**File:** [feature/stories/src/main/java/com/wadjet/feature/stories/screen/StoriesScreen.kt](feature/stories/src/main/java/com/wadjet/feature/stories/screen/StoriesScreen.kt#L81)
**Description:** `FREE_STORY_LIMIT = 3` is a hardcoded constant in the screen file. This means the limit cannot be adjusted server-side. The locking logic compares against the **original index** in `state.stories` (not the filtered list), meaning a different story may be locked depending on server ordering.
**Evidence:**
```kotlin
private const val FREE_STORY_LIMIT = 3
// ...
val index = state.stories.indexOf(story)  // index in unfiltered list
val isLocked = index >= FREE_STORY_LIMIT  // locked based on position, not ID
```
**Impact:** If the server reorders stories, different stories get locked/unlocked unpredictably.

---

## S8-15 | LOW — StoryReaderViewModel uses `MutableSet` in data class (broken equals/hashCode)

**File:** [feature/stories/src/main/java/com/wadjet/feature/stories/StoryReaderViewModel.kt](feature/stories/src/main/java/com/wadjet/feature/stories/StoryReaderViewModel.kt#L32-L45)
**Description:** `ReaderUiState` is a `data class` with a `val glyphsLearned: MutableSet<String> = mutableSetOf()` property. Kotlin data class `equals()` and `hashCode()` use structural equality on all properties, but `MutableSet` equality depends on its contents which change over time. Since Compose relies on `StateFlow` equality checks to determine recomposition, mutations to `glyphsLearned` may not trigger recomposition.
**Evidence:**
```kotlin
data class ReaderUiState(
    ...
    val glyphsLearned: MutableSet<String> = mutableSetOf(),  // ← mutable in data class
```
The code often does `state.glyphsLearned.toMutableSet()` then creates a new state copy, which partially mitigates this, but the field type itself is a code smell.

---

## S8-16 | LOW — StoriesViewModel.loadFavorites does not handle failure

**File:** [feature/stories/src/main/java/com/wadjet/feature/stories/StoriesViewModel.kt](feature/stories/src/main/java/com/wadjet/feature/stories/StoriesViewModel.kt#L88-L93)
**Description:** `loadFavorites()` calls `userRepository.getFavorites()` and only handles `onSuccess`. If the call fails (network error, auth issue), the `favorites` set stays empty and the user sees no favorite indicators. No error is shown and no retry is offered.

---

## S8-17 | INFO — Chat has no offline indicator or graceful degradation

**File:** [feature/chat/src/main/java/com/wadjet/feature/chat/ChatViewModel.kt](feature/chat/src/main/java/com/wadjet/feature/chat/ChatViewModel.kt)
**Description:** Chat is 100% server-dependent (SSE streaming). There is no offline detection, no airplane mode warning, and no queue for messages. If the user types and sends while offline, the SSE connection will fail with an IOException and they'll see a generic error after timeout (up to 60 seconds). A connectivity check before sending would improve UX.

---

## S8-18 | INFO — Chat history file format uses JSONObject/JSONArray, not kotlinx.serialization

**File:** [feature/chat/src/main/java/com/wadjet/feature/chat/ChatHistoryStore.kt](feature/chat/src/main/java/com/wadjet/feature/chat/ChatHistoryStore.kt)
**Description:** The rest of the codebase uses `kotlinx.serialization.json.Json` for JSON handling, but `ChatHistoryStore` uses `org.json.JSONObject`/`JSONArray`. This inconsistency is not a bug but creates two JSON parsing code paths to maintain.

---

## Summary — Stage 8

| Severity | Count |
|----------|-------|
| CRITICAL | 1     |
| MAJOR    | 3     |
| MEDIUM   | 6     |
| LOW      | 5     |
| INFO     | 3     |
| **Total**| **18**|
