# Logic & Quality Audit — Wadjet Android

> **ZERO HALLUCINATION PROTOCOL**: This audit uses a staged investigation approach.
> Each stage produces a written findings file BEFORE moving to the next stage.
> The final planning phase reads ALL findings files to produce the output.
> This eliminates memory loss, hallucination, and confusion from context overflow.

## Your Role

You are a **senior Android engineer, Egyptologist consultant, and QA architect** specializing in mobile app logic, data flow, API integration, audio/TTS quality, AI output quality, and end-to-end feature correctness. You will perform a deep, exhaustive audit of the Wadjet Android app's **logic, data handling, and output quality** with a special emphasis on **Egyptological accuracy** — every hieroglyphic pronunciation, transliteration, Gardiner code mapping, and archaeological fact must be verified against real academic sources.

**Scope: LOGIC & QUALITY ONLY.** Do not change UI layout, design system, colors, spacing, navigation structure, or screen visual design. Your concern is: does every feature work correctly, produce high-quality output, handle errors gracefully, and match the web app's behavior?

**Accuracy standard: 100%.** An Egyptologist using this app should find ZERO pronunciation errors, ZERO wrong transliterations, ZERO incorrect Gardiner mappings. Every hieroglyphic word must sound like how scholars reconstruct ancient Egyptian speech.

---

## About the App

Wadjet is an Egyptian archaeology Android app backed by a FastAPI server on HuggingFace Spaces.

**Backend:** `https://nadercr7-wadjet-v2.hf.space` — 48 endpoints (auth, scan, dictionary, write, translate, explore/landmarks, chat, stories, audio/TTS, user, feedback)

**Key features & their logic:**
- **Hieroglyph Scanning** — image upload → AI (Gemini/ONNX) detection → glyphs + transliteration + translation
- **TTS / Audio** — server-side Gemini TTS (Orus/Aoede/Charon voices) with Groq fallback → hieroglyphic pronunciation must sound like authentic ancient Egyptian, not robotic or choppy
- **Hieroglyphic Pronunciation** — `EgyptianPronunciation.kt` converts MdC transliteration → TTS-ready text with ~70 known words + phoneme mapping + vowel epenthesis
- **Landmark Identification** — ONNX + Gemini parallel ensemble → Grok tiebreak → identify Egyptian sites
- **Dictionary** — FTS5 local search + API search, 1000+ signs, Gardiner codes
- **Write** — English → hieroglyphs (smart mode), MdC rendering, phrase shortcuts
- **Chat** — SSE streaming with Thoth AI, markdown rendering, STT (Groq Whisper)
- **Stories** — chapters with 4 interaction types, AI-generated scene images, TTS narration
- **Auth** — Firebase Auth + custom JWT, token refresh, Google Sign-In
- **Offline** — Room DB cache for dictionary, landmarks, scan history

**Stack:** Kotlin · Hilt · Retrofit 2 + OkHttp 4 · kotlinx.serialization · Room 2.6+ · CameraX (disabled) · Coil 3 · Firebase (Auth/Firestore/Messaging/Crashlytics)

---

# INVESTIGATION PROTOCOL

> **How this works:**
> 1. Complete Stage 1 → write findings to `_investigation/stage-01-*.md`
> 2. Complete Stage 2 → write findings to `_investigation/stage-02-*.md`
> 3. ... repeat for all 12 stages ...
> 4. After ALL stages done → read ALL `_investigation/stage-*.md` files
> 5. Produce the 9 final planning files from the complete investigation data
>
> **NEVER skip writing a stage file. NEVER proceed to the next stage without writing.**
> **NEVER start the final planning phase without re-reading ALL stage files.**

Create all stage files inside: `.specify/specs/004-logic-quality/_investigation/`

---

## Stage 1: Existing Specs & Git History
**Output → `_investigation/stage-01-existing-context.md`**

### Read these files:
```
.specify/memory/constitution.md
.specify/specs/001-logic-parity/gap-analysis.md
.specify/specs/001-logic-parity/architecture.md
.specify/specs/001-logic-parity/tasks.md
.specify/specs/001-logic-parity/smart-defaults.md
.specify/specs/001-logic-parity/contracts/api-contract.md
.specify/specs/002-ux-fixes/PLAN.md
```

### Run:
```bash
git log --oneline --all
```

### Write to stage file:
```markdown
# Stage 1: Existing Context

## Previously Identified Bugs (from 001-logic-parity)
[List every bug with ID, status: FIXED / STILL OPEN / UNKNOWN]

## Previously Identified Gaps
[List every gap with status]

## Git History Summary
[Every commit, what it delivered]

## What's Confirmed Fixed (commit evidence)
[Bug X → fixed in commit Y]

## What's Still Open
[Bugs/gaps with no fix commit]

## API Contract Summary
[48 endpoints — list them all with method + path]
```
---

## Stage 2: Network Layer — API Services & DTOs
**Output → `_investigation/stage-02-network-layer.md`**

### Read ALL of these files:

**API services (11):**
- `core/network/src/main/java/**/api/AudioApiService.kt`
- `core/network/src/main/java/**/api/AuthApiService.kt`
- `core/network/src/main/java/**/api/ChatApiService.kt`
- `core/network/src/main/java/**/api/DictionaryApiService.kt`
- `core/network/src/main/java/**/api/FeedbackApiService.kt`
- `core/network/src/main/java/**/api/LandmarkApiService.kt`
- `core/network/src/main/java/**/api/ScanApiService.kt`
- `core/network/src/main/java/**/api/StoriesApiService.kt`
- `core/network/src/main/java/**/api/TranslateApiService.kt`
- `core/network/src/main/java/**/api/UserApiService.kt`
- `core/network/src/main/java/**/api/WriteApiService.kt`

**Network DTOs (10):**
- `core/network/src/main/java/**/model/AuthModels.kt`
- `core/network/src/main/java/**/model/ChatModels.kt`
- `core/network/src/main/java/**/model/DictionaryModels.kt`
- `core/network/src/main/java/**/model/FeedbackModels.kt`
- `core/network/src/main/java/**/model/LandmarkModels.kt`
- `core/network/src/main/java/**/model/ScanModels.kt`
- `core/network/src/main/java/**/model/StoryModels.kt`
- `core/network/src/main/java/**/model/TranslateModels.kt`
- `core/network/src/main/java/**/model/UserModels.kt`
- `core/network/src/main/java/**/model/WriteModels.kt`

**Network infrastructure:**
- `core/network/src/main/java/**/AuthInterceptor.kt`
- `core/network/src/main/java/**/RateLimitInterceptor.kt`
- `core/network/src/main/java/**/TokenManager.kt`
- `core/network/src/main/java/**/di/NetworkModule.kt`

### Cross-reference against `api-contract.md` (from Stage 1):
For EVERY endpoint in the contract, check:
1. Is there a matching Retrofit function in the right `*ApiService.kt`?
2. Does the request body DTO match the contract's request schema?
3. Does the response DTO match the contract's response schema field-by-field?
4. Are optional/nullable fields handled correctly?
5. Are error responses (400, 401, 403, 404, 429, 500) handled?

### Write to stage file:
```markdown
# Stage 2: Network Layer

## Endpoint Coverage Matrix
| # | Method | Endpoint | API Service | Request DTO | Response DTO | Match? | Issues |
|---|--------|----------|-------------|-------------|--------------|--------|--------|

## DTO Field-by-Field Comparison
### ScanModels.kt vs /api/scan contract
| Field | Contract | DTO | Match? |
...

### LandmarkModels.kt vs /api/explore/identify contract
| Field | Contract | DTO | Match? |
...

[Repeat for ALL 10 DTO files]

## Auth Interceptor Analysis
[Token refresh logic, race condition risk, retry behavior]

## Rate Limit Interceptor Analysis
[429 handling, Retry-After header, backoff strategy]

## Token Manager Analysis
[Storage, refresh, expiry detection, concurrent access]

## Missing Endpoints
[Endpoints in contract not implemented in Android]

## Wrong/Extra Endpoints
[Endpoints in Android not matching contract]

## Issues Found
[Every issue with file:line reference]
```

---

## Stage 3: Data Layer — Repositories & Domain Models
**Output → `_investigation/stage-03-data-layer.md`**

### Read ALL of these files:

**Repository interfaces (9):**
- `core/domain/src/main/java/**/repository/AuthRepository.kt`
- `core/domain/src/main/java/**/repository/ChatRepository.kt`
- `core/domain/src/main/java/**/repository/DictionaryRepository.kt`
- `core/domain/src/main/java/**/repository/ExploreRepository.kt`
- `core/domain/src/main/java/**/repository/FeedbackRepository.kt`
- `core/domain/src/main/java/**/repository/ScanRepository.kt`
- `core/domain/src/main/java/**/repository/StoriesRepository.kt`
- `core/domain/src/main/java/**/repository/TranslateRepository.kt`
- `core/domain/src/main/java/**/repository/UserRepository.kt`

**Repository implementations (9):**
- `core/data/src/main/java/**/repository/AuthRepositoryImpl.kt`
- `core/data/src/main/java/**/repository/ChatRepositoryImpl.kt`
- `core/data/src/main/java/**/repository/DictionaryRepositoryImpl.kt`
- `core/data/src/main/java/**/repository/ExploreRepositoryImpl.kt`
- `core/data/src/main/java/**/repository/FeedbackRepositoryImpl.kt`
- `core/data/src/main/java/**/repository/ScanRepositoryImpl.kt`
- `core/data/src/main/java/**/repository/StoriesRepositoryImpl.kt`
- `core/data/src/main/java/**/repository/TranslateRepositoryImpl.kt`
- `core/data/src/main/java/**/repository/UserRepositoryImpl.kt`

**Domain models (9):**
- `core/domain/src/main/java/**/model/Chat.kt`
- `core/domain/src/main/java/**/model/Dashboard.kt`
- `core/domain/src/main/java/**/model/Dictionary.kt`
- `core/domain/src/main/java/**/model/Feedback.kt`
- `core/domain/src/main/java/**/model/Landmark.kt`
- `core/domain/src/main/java/**/model/Scan.kt`
- `core/domain/src/main/java/**/model/Story.kt`
- `core/domain/src/main/java/**/model/Translate.kt`
- `core/domain/src/main/java/**/model/User.kt`

### Analyze:
1. DTO → Domain mapping: Are all fields mapped? Any data lost in conversion?
2. Error handling: Using `WadjetResult`? Or raw try/catch?
3. Caching strategy: When is Room used vs. network-only?
4. Offline behavior: What happens when network is unavailable?

### Write to stage file:
```markdown
# Stage 3: Data Layer

## DTO → Domain Mapping Audit
### AuthModels → User domain
| DTO Field | Domain Field | Mapped? | Notes |
...

[Repeat for ALL 9 domain models]

## Repository Error Handling
| Repository | Uses WadjetResult? | Error Strategy | Issues |
...

## Caching Strategy
| Repository | Room Cache? | Network-first? | Offline fallback? | Issues |
...

## Missing Repository Methods
[Methods in the interface but not implemented, or methods that should exist but don't]

## Issues Found
[Every issue with file:line reference]
```

---

## Stage 4: Database & Offline Layer
**Output → `_investigation/stage-04-database-offline.md`**

### Read ALL of these files:
- `core/database/src/main/java/**/WadjetDatabase.kt`
- `core/database/src/main/java/**/LandmarkDao.kt`
- `core/database/src/main/java/**/ScanResultDao.kt`
- `core/database/src/main/java/**/SignDao.kt`
- `core/database/src/main/java/**/DatabaseModule.kt`
- `core/database/src/main/java/**/LandmarkEntity.kt`
- `core/database/src/main/java/**/ScanResultEntity.kt`
- `core/database/src/main/java/**/SignEntity.kt`
- `core/database/src/main/java/**/SignFtsEntity.kt`
- `core/data/src/main/java/**/di/RepositoryModule.kt`
- `core/common/src/main/java/**/ConnectivityManagerNetworkMonitor.kt`
- `core/common/src/main/java/**/NetworkMonitor.kt`
- `core/common/src/main/java/**/SuspendRunCatching.kt`
- `core/common/src/main/java/**/WadjetResult.kt`
- `core/data/src/main/java/**/datastore/UserPreferencesDataStore.kt`

### Analyze:
1. Room schema: Are entities complete? Match what's needed?
2. FTS5: Configured correctly for dictionary search? Arabic support?
3. Migrations: Any schema changes without migrations?
4. DAOs: All needed queries present? Performance (indexes)?
5. DataStore: All preferences properly typed and persisted?
6. NetworkMonitor: Correctly detects offline? Tested?
7. Offline degradation: What works offline? What silently fails?

### Write to stage file:
```markdown
# Stage 4: Database & Offline

## Room Schema Audit
| Entity | Fields | Indexes | Issues |
...

## FTS5 Configuration
[SignFtsEntity setup, tokenizer, language support]

## DAO Query Audit
| DAO | Method | Query | Performance Notes |
...

## DataStore Preferences
| Key | Type | Default | Used Where | Issues |
...

## Network Monitor
[Implementation details, edge cases]

## Offline Feature Matrix
| Feature | Offline Behavior | Expected | Issues |
...

## Issues Found
```

---

## Stage 5: ViewModels & Feature Logic
**Output → `_investigation/stage-05-viewmodels.md`**

### Read ALL 20 ViewModels:
- `app/src/main/java/com/wadjet/app/screen/HieroglyphsHubViewModel.kt`
- `feature/auth/src/main/java/**/AuthViewModel.kt`
- `feature/chat/src/main/java/**/ChatViewModel.kt`
- `feature/chat/src/main/java/**/ChatHistoryStore.kt`
- `feature/dashboard/src/main/java/**/DashboardViewModel.kt`
- `feature/dictionary/src/main/java/**/DictionaryViewModel.kt`
- `feature/dictionary/src/main/java/**/LessonViewModel.kt`
- `feature/dictionary/src/main/java/**/SignDetailViewModel.kt`
- `feature/dictionary/src/main/java/**/TranslateViewModel.kt`
- `feature/dictionary/src/main/java/**/WriteViewModel.kt`
- `feature/explore/src/main/java/**/DetailViewModel.kt`
- `feature/explore/src/main/java/**/ExploreViewModel.kt`
- `feature/explore/src/main/java/**/IdentifyViewModel.kt`
- `feature/feedback/src/main/java/**/FeedbackViewModel.kt`
- `feature/landing/src/main/java/**/LandingViewModel.kt`
- `feature/scan/src/main/java/**/HistoryViewModel.kt`
- `feature/scan/src/main/java/**/ScanResultViewModel.kt`
- `feature/scan/src/main/java/**/ScanViewModel.kt`
- `feature/settings/src/main/java/**/SettingsViewModel.kt`
- `feature/stories/src/main/java/**/StoriesViewModel.kt`
- `feature/stories/src/main/java/**/StoryReaderViewModel.kt`

### For each ViewModel analyze:
1. State management: UiState sealed class? Loading/Success/Error?
2. Error handling: Exceptions caught? User-facing messages?
3. Coroutine scoping: viewModelScope used everywhere? Cancellation?
4. Race conditions: Multiple concurrent calls possible?
5. Memory leaks: Holding references to Context, Activity, Views?
6. Data flow: StateFlow/SharedFlow correct? Replay/buffer config?

### Write to stage file:
```markdown
# Stage 5: ViewModel Logic

## ViewModel-by-ViewModel Audit

### AuthViewModel
- State management: [details]
- Error handling: [details]
- Coroutine safety: [details]
- Issues: [list]

### ChatViewModel
...

[Repeat for ALL 20 ViewModels]

## Cross-Cutting Issues
[Patterns that appear in multiple ViewModels]

## Issues Found
[Every issue with file:line reference]
```

---

## Stage 6: TTS & Egyptian Pronunciation — CRITICAL
**Output → `_investigation/stage-06-pronunciation.md`**

> **THIS IS THE MOST IMPORTANT STAGE.** An Egyptologist should find ZERO errors.

### Read these files:
- `core/common/src/main/java/**/EgyptianPronunciation.kt` — **READ EVERY LINE**
- `core/network/src/main/java/**/api/AudioApiService.kt`
- Any ViewModel that calls audio/TTS functions
- `feature/scan/src/main/java/**/util/GardinerUnicode.kt` — **READ EVERY LINE**

### Read the web backend TTS:
- `D:\Personal attachements\Projects\Wadjet-v3-beta\routes\audio.py`
- `D:\Personal attachements\Projects\Wadjet-v3-beta\services\tts_service.py`

### Verify EVERY pronunciation against online academic sources:

**Online references to consult (fetch these pages):**
- https://en.wikipedia.org/wiki/Egyptian_hieroglyphs — hieroglyph inventory, transliteration
- https://en.wikipedia.org/wiki/Manuel_de_Codage — MdC standard transliteration system
- https://en.wikipedia.org/wiki/Egyptian_language — phonology, reconstructed pronunciation
- https://en.wikipedia.org/wiki/Transliteration_of_Ancient_Egyptian — uniliteral signs, phonetic values
- https://en.wiktionary.org/wiki/Appendix:Egyptian_hieroglyphs — sign list with phonetic values
- https://en.wikipedia.org/wiki/Gardiner%27s_sign_list — complete Gardiner classification
- https://mjn.host.cs.st-andrews.ac.uk/egyptian/texts/corpus/pdf/unilit.pdf — academic uniliteral guide (if accessible)
- https://www.brown.edu/Departments/Egyptology/ — Brown Egyptology resources
- Search for: "ancient Egyptian pronunciation reconstruction" for scholarly consensus

### For the ~70 known words in EgyptianPronunciation.kt:
1. Look up each word in MdC → verify the spelling is correct
2. Look up the commonly accepted pronunciation → verify the TTS output matches
3. Check if vowel epenthesis produces natural-sounding results (e.g., "anx" → "ankh" not "ah-neh-kh")
4. Verify transliteration conventions match the Manuel de Codage standard

### For the phoneme mapping:
1. Every MdC consonant → verify the target sound (e.g., "x" → "kh", "X" → "ch", "S" → "sh")
2. Check for missing consonants that MdC defines but the app doesn't map
3. Verify the vowel insertion rules (ancient Egyptian writing omitted vowels, but TTS needs them)
4. Compare with scholarly reconstructions of Egyptian phonology

### For the tokenizer logic (CRITICAL — this is where unknown words break):
The tokenizer is character-by-character. This means:
1. **Numbers in MdC** — Gardiner codes like `A1`, `B2`, `D21` appear in MdC text as determinatives. The MdC_STRIP set does NOT include digits `0-9`. So `nTrA40` (god + determinative A40) would tokenize `4` and `0` as sounds. **Verify this is handled.**
2. **Hyphens** — MdC uses `-` for horizontal juxtaposition. The strip set does NOT include `-`. Compound words in the WORD_MAP use hyphens (e.g., `"imn-ra"`) but unknown compounds would fail. **Verify fallback handles hyphens.**
3. **Biliterals and triliterals** — MdC has ~80 biliterals (`mn`, `wn`, `wr`, `Hm`, `nw`...) and ~50 triliterals (`nfr`, `anx`, `Htp`...). The tokenizer is single-character only — it can't detect multi-character phonemes. Words in the WORD_MAP are handled, but UNKNOWN biliteral/triliteral sequences go through character-by-character fallback. **Audit how bad this is for common words NOT in the word map.**
4. **Determinative Gardiner codes in text** — If MdC text includes determinative codes like `Z1` (stroke), `A1` (seated man), `N5` (sun disk), these should be SILENTLY DROPPED, not pronounced. **Verify they are handled.**
5. **Plural markers** — MdC uses `.w` or `w` suffix for plurals, `.t` for feminine endings. The word map covers some (nTrw, nfrw) but the fallback doesn't have grammatical awareness. **Document the quality of fallback for common grammatical forms.**
6. **Duplicate keys in WORD_MAP** — Kotlin's `mapOf` silently overwrites duplicate keys (last wins). Check if any MdC key appears twice with DIFFERENT pronunciations. Specifically check `"Dd"` and `"dSrt"` which may be duplicated.
7. **The "already pronounceable" detection** — `toSpeech()` checks if text contains `e`, `o`, or `u` and returns as-is. This is a heuristic. **Verify:** Does any legitimate MdC transliteration contain these letters? (Answer: `u` rarely appears but could in some conventions. `o` is not standard MdC. `e` is not standard MdC.) Are there false positives where English text is passed and correctly detected?
8. **The `removeSuffix(".")` logic** — used for word-final dots. **Verify this doesn't strip dots that are part of MdC notation (like `.t` feminine ending or `.w` plural).**

### For Gardiner Unicode mapping:
1. Every Gardiner code (A1, B1, C1...) → verify it maps to the correct Unicode codepoint
2. Check the Unicode range U+13000–U+1342F (Egyptian Hieroglyphs block)
3. Verify no signs are missing or wrong
4. Check if the mapping covers all commonly used signs
5. **Check for the Extended block** U+13430–U+1345F (hieroglyph format controls) — needed for proper rendering?
6. **Verify bidirectional rendering** — hieroglyphs can be written left-to-right or right-to-left. Does the app handle text direction?

### For the complete word list (EXHAUSTIVE AUDIT):
The WORD_MAP has ~100 words. For EVERY SINGLE ONE:
1. Is the MdC key correctly spelled per MdC convention?
2. Is the pronunciation the commonly accepted Egyptological reconstruction?
3. Is the English output optimized for TTS (easy for TTS to say correctly)?
4. Is there a more common pronunciation variant that should be preferred?
5. Cross-reference with: Wikipedia article for that word if it exists, Thesaurus Linguae Aegyptiae entries, standard textbooks (Allen's "Middle Egyptian", Gardiner's "Egyptian Grammar")

### For MISSING words (the word map should be expanded):
1. Are there common Egyptian words that an Egyptology app MUST include? (e.g., `sSAt` [daughter of], `wHm` [to repeat], `Xnm` [to join])
2. Are all pharaoh names covered? (e.g., `mn-xpr-ra` [Thutmose III], `nb-mAat-ra` [Amenhotep III])
3. Are all major monument names covered?
4. Are numbers (waw, senoo, khemet...) covered?
5. Document every important missing word that should be added

### Voice configuration:
1. Orus voice — used for hieroglyph pronunciation. Is the style prompt correct?
2. Aoede voice — used for stories. Appropriate?
3. Charon voice — used for landing. Appropriate?
4. Does the TTS prompt tell the voice to speak SLOWLY and CLEARLY for ancient words?
5. **Is the STYLE and CONTEXT actually sent to the server?** Trace the complete path: `EgyptianPronunciation.VOICE` + `.STYLE` + `.CONTEXT` → ViewModel → Repository → API Service → HTTP request body. Verify EACH step passes these values correctly.
6. **Does the server actually USE these values?** Check `audio.py` and `tts_service.py` — do they read voice, style, and context parameters?

### TTS end-to-end data flow audit:
Trace the COMPLETE path of a pronunciation request:
1. User sees hieroglyph sign → taps "listen" button
2. Which ViewModel method is called?
3. What text is sent? Raw MdC or converted via `toSpeech()`?
4. Is `toSpeech()` called on the client BEFORE sending to server, or does the server do its own conversion?
5. What does the HTTP request body look like? (text, voice, style, context fields)
6. What does the server return? (audio format, encoding, content-type)
7. How is the audio played? (MediaPlayer, ExoPlayer, custom?)
8. Is the audio cached? Where? For how long?
9. What happens on error? (network fail, server 500, empty response)
10. **Is there a DISCONNECT?** (from user memory: "TTS architecture DISCONNECTED: tts_service.py never called by /api/tts endpoint") — verify if this is STILL true or was fixed

### Write to stage file:
```markdown
# Stage 6: TTS & Egyptian Pronunciation

## EgyptianPronunciation.kt — Full Code Review
[Line-by-line analysis of the conversion logic]

## Tokenizer Analysis
### MdC Structural Characters
| Character | In MdC_STRIP? | Should be? | Impact if missing |
[Numbers 0-9, hyphens, hash, ampersand, etc.]

### Determinative Handling
[How are Gardiner determinative codes handled in MdC text?]

### Biliteral/Triliteral Fallback Quality
| Word | Not in WORD_MAP | Fallback produces | Correct pronunciation | Quality |
[Test at least 20 common words through the fallback path]

## Known Word List — Academic Verification
| # | MdC Input | App Output | Correct Pronunciation | Source | Status |
|---|-----------|------------|----------------------|--------|--------|
| 1 | anx | [what app produces] | ankh [aːnəx] | Wikipedia: Ankh | ✅/❌ |
| 2 | nfr | [what app produces] | nefer [naːfar] | Allen, Middle Egyptian | ✅/❌ |
...
[ALL ~70 words]

## Phoneme Mapping — Academic Verification
| MdC Char | App maps to | Correct IPA | Source | Status |
|----------|-----------|-------------|--------|--------|
| A | [app value] | /ʔ/ (glottal stop) | MdC standard | ✅/❌ |
| a | [app value] | /ʕ/ (pharyngeal) | MdC standard | ✅/❌ |
| i | [app value] | /j/ or /iː/ | MdC standard | ✅/❌ |
...
[ALL consonants in MdC]

## Missing Phonemes
[MdC consonants not in the mapping]

## Vowel Epenthesis Rules
[Current rules, correctness, naturalness — test with example words]

## Duplicate Key Check
[Any WORD_MAP keys that appear twice — list with both values]

## "Already Pronounceable" Detection Audit
[Test cases: MdC text that might contain e/o/u, English text, mixed text]

## WORD_MAP Completeness — Missing Important Words
| # | MdC | English | Why Important | Should Add? |
[List every important Egyptian word NOT in the map — minimum 30]

## Fallback Path Stress Test
Run these 20+ words through convertWord() (NOT in WORD_MAP):
| # | MdC Input | Fallback Output | Correct Pronunciation | Quality (1-5) | Notes |
| 1 | wHm | [fallback produces?] | wehem (to repeat) | ? | ... |
| 2 | sSAt | [fallback produces?] | seshat | ? | ... |
| 3 | nTrA40 | [fallback produces?] | should be "netjer" (drop A40) | ? | ... |
| 4 | Xnm | [fallback produces?] | khenem | ? | ... |
...
[At least 20 common words — test the fallback path quality]

## Gardiner Unicode Mapping — Verification
| Gardiner Code | App Unicode | Correct Unicode | Status |
|--------------|-------------|-----------------|--------|
...
[Sample of critical signs + any wrong ones]

## Gardiner Extended Block (U+13430–U+1345F)
[Format controls for quadrat rendering — needed?]

## Voice Configuration Audit
| Context | Voice | Style Prompt | Assessment | Recommendations |
|---------|-------|-------------|-----------|-----------------|
...

## TTS Data Flow: Complete Trace
1. User taps "listen" on sign X
2. ViewModel method: [which one?]
3. toSpeech() called? → [yes/no, where?]
4. API request body: [exact fields]
5. Server receives: [what does audio.py/tts_service.py do?]
6. Server response: [format, content-type]
7. Client plays audio: [player used, caching]
8. Error handling: [what happens on failure?]
9. **DISCONNECT CHECK**: Is tts_service.py actually called by the endpoint? (Known historic bug)

## Words That Would Sound Wrong
[Specific words + their fallback output + what they should sound like]

## Comparison with Web Backend
[How web app handles pronunciation vs. Android — field by field]

## Issues Found
[Every issue — this is the highest priority section]
```

---

## Stage 7: Scan & Landmark Logic
**Output → `_investigation/stage-07-scan-landmarks.md`**

### Read these files:
- `feature/scan/src/main/java/**/ScanViewModel.kt`
- `feature/scan/src/main/java/**/ScanResultViewModel.kt`
- `feature/scan/src/main/java/**/HistoryViewModel.kt`
- `feature/scan/src/main/java/**/util/GardinerUnicode.kt`
- `feature/scan/src/main/java/**/util/ImageUtil.kt`
- `core/network/src/main/java/**/api/ScanApiService.kt`
- `core/network/src/main/java/**/model/ScanModels.kt`
- `core/data/src/main/java/**/repository/ScanRepositoryImpl.kt`
- `core/domain/src/main/java/**/model/Scan.kt`
- `feature/explore/src/main/java/**/IdentifyViewModel.kt`
- `feature/explore/src/main/java/**/DetailViewModel.kt`
- `feature/explore/src/main/java/**/ExploreViewModel.kt`
- `core/network/src/main/java/**/api/LandmarkApiService.kt`
- `core/network/src/main/java/**/model/LandmarkModels.kt`
- `core/data/src/main/java/**/repository/ExploreRepositoryImpl.kt`
- `core/domain/src/main/java/**/model/Landmark.kt`
- `core/database/src/main/java/**/LandmarkDao.kt`
- `core/database/src/main/java/**/LandmarkEntity.kt`

### Read web backend for comparison:
- `D:\Personal attachements\Projects\Wadjet-v3-beta\routes\scan.py`
- `D:\Personal attachements\Projects\Wadjet-v3-beta\routes\landmarks.py`
- `D:\Personal attachements\Projects\Wadjet-v3-beta\services\landmark_pipeline.py`

### Analyze:
1. Scan upload: image compression, size limits, HEIC support, base64 vs multipart
2. Scan response: ALL fields captured? confidence, quality hints, AI reading?
3. Glyph rendering: Gardiner → Unicode correct? Display issues?
4. Landmark identify: ensemble logic (ONNX + Gemini + Grok tiebreak)
5. Landmark detail: all sections populated? Images loading?
6. Landmark images: correct base URL? Placeholder on failure? (B4 from 001-spec)
7. Scan history: saved locally? Thumbnails? Restorable?
8. "Ask Thoth" from landmark: context correctly passed to chat?

### Write to stage file:
```markdown
# Stage 7: Scan & Landmark Logic

## Scan Pipeline Analysis
[Upload → request → response → parse → display — every step]

## Scan Response Field Coverage
| API Field | DTO field | Domain field | Displayed? | Issues |
...

## Image Handling
[Compression, size limits, formats, upload method]

## Gardiner → Unicode Rendering
[Correctness of GardinerUnicode.kt, missing signs]

## Landmark Identify Flow
[Request → ensemble → response → display — every step]

## Landmark Detail Completeness
| Section | API field | Displayed? | Issues |
...

## Image Loading Issues
[AsyncImage usage, base URLs, placeholders, error states]

## Scan History Persistence
[Room storage, thumbnails, restoration]

## Web Backend Comparison
| Feature | Web Backend | Android | Match? | Issues |
...

## Issues Found
```

---

## Stage 8: Chat, Stories & Interactive Features
**Output → `_investigation/stage-08-chat-stories.md`**

### Read these files:
- `feature/chat/src/main/java/**/ChatViewModel.kt`
- `feature/chat/src/main/java/**/ChatHistoryStore.kt`
- `core/network/src/main/java/**/api/ChatApiService.kt`
- `core/network/src/main/java/**/model/ChatModels.kt`
- `core/data/src/main/java/**/repository/ChatRepositoryImpl.kt`
- `core/domain/src/main/java/**/model/Chat.kt`
- `feature/stories/src/main/java/**/StoriesViewModel.kt`
- `feature/stories/src/main/java/**/StoryReaderViewModel.kt`
- `core/network/src/main/java/**/api/StoriesApiService.kt`
- `core/network/src/main/java/**/model/StoryModels.kt`
- `core/data/src/main/java/**/repository/StoriesRepositoryImpl.kt`
- `core/domain/src/main/java/**/model/Story.kt`

### Read web backend:
- `D:\Personal attachements\Projects\Wadjet-v3-beta\routes\chat.py` or `services\thoth_chat.py`

### Analyze:
1. SSE streaming: OkHttp EventSource implementation, memory management, disconnect handling
2. Markdown rendering: library used, all markdown features supported?
3. Chat history: local persistence, scroll position, conversation limits
4. STT: Groq Whisper integration, permissions, locale handling
5. Context window: does it grow unbounded? Truncation strategy?
6. Stories: chapter loading, 4 interaction types all working?
7. Story images: AI-generated scene images loading correctly?
8. Story TTS: Aoede voice narration, chapter-by-chapter, play/pause/stop
9. Story progress: Firestore persistence? Restored on reopen?

### Write to stage file:
```markdown
# Stage 8: Chat & Stories

## Chat SSE Streaming
[Implementation details, memory, disconnect, reconnect]

## Markdown Rendering
[Library, supported features, edge cases]

## Chat History
[Persistence, limits, conversation management]

## STT Integration
[Groq Whisper, permissions, error handling]

## Context Management
[Token counting, truncation, context strategy]

## Stories Feature
### Chapter Loading
### Interaction Types (4)
### Scene Images
### TTS Narration
### Progress Persistence

## Web Backend Comparison
| Feature | Web | Android | Match? |
...

## Issues Found
```

---

## Stage 9: Auth, Security & Firebase
**Output → `_investigation/stage-09-auth-security.md`**

### Read these files:
- `feature/auth/src/main/java/**/AuthViewModel.kt`
- `core/network/src/main/java/**/AuthInterceptor.kt`
- `core/network/src/main/java/**/TokenManager.kt`
- `core/network/src/main/java/**/RateLimitInterceptor.kt`
- `core/network/src/main/java/**/api/AuthApiService.kt`
- `core/network/src/main/java/**/model/AuthModels.kt`
- `core/data/src/main/java/**/repository/AuthRepositoryImpl.kt`
- `core/domain/src/main/java/**/repository/AuthRepository.kt`
- `core/firebase/src/main/java/**/FirebaseAuthManager.kt`
- `core/firebase/src/main/java/**/WadjetFirebaseMessaging.kt`
- `core/firebase/src/main/java/**/FirebaseModule.kt`
- `feature/settings/src/main/java/**/SettingsViewModel.kt`
- `app/build.gradle.kts` — ProGuard rules, build config

### Analyze:
1. Token refresh race condition: Multiple requests hit 401 → all try to refresh → what happens?
2. EncryptedSharedPreferences: Tokens stored securely?
3. Firebase Auth + JWT: Both layers synchronized?
4. Google Sign-In: Flow correct? Token exchange?
5. Logout: Clears everything (local tokens, cookies, Firebase)?
6. Password reset: End-to-end flow?
7. Rate limiting: Retry-After respected? Backoff?
8. ProGuard: kotlinx.serialization rules? Retrofit keep rules?
9. Secrets: Any API keys, tokens, or secrets in source code?
10. HTTPS: Certificate pinning? TLS version?

### Write to stage file:
```markdown
# Stage 9: Auth & Security

## Token Refresh Flow
[Sequence diagram, race condition analysis]

## Token Storage Security
[EncryptedSharedPreferences implementation]

## Firebase ↔ JWT Synchronization
[How both auth layers interact]

## Google Sign-In Flow
[Step by step]

## Logout Completeness
[What gets cleared, what might be missed]

## Rate Limiting
[Implementation, edge cases]

## ProGuard/R8 Rules
[Keep rules for serialization, Retrofit]

## Security Scan
[Hardcoded secrets, insecure storage, certificate handling]

## Issues Found
```

---

## Stage 10: Dictionary, Translate & Write Logic
**Output → `_investigation/stage-10-dictionary-write.md`**

### Read these files:
- `feature/dictionary/src/main/java/**/DictionaryViewModel.kt`
- `feature/dictionary/src/main/java/**/LessonViewModel.kt`
- `feature/dictionary/src/main/java/**/SignDetailViewModel.kt`
- `feature/dictionary/src/main/java/**/TranslateViewModel.kt`
- `feature/dictionary/src/main/java/**/WriteViewModel.kt`
- `core/network/src/main/java/**/api/DictionaryApiService.kt`
- `core/network/src/main/java/**/api/TranslateApiService.kt`
- `core/network/src/main/java/**/api/WriteApiService.kt`
- `core/network/src/main/java/**/model/DictionaryModels.kt`
- `core/network/src/main/java/**/model/TranslateModels.kt`
- `core/network/src/main/java/**/model/WriteModels.kt`
- `core/data/src/main/java/**/repository/DictionaryRepositoryImpl.kt`
- `core/data/src/main/java/**/repository/TranslateRepositoryImpl.kt`
- `core/domain/src/main/java/**/model/Dictionary.kt`
- `core/domain/src/main/java/**/model/Translate.kt`
- `core/database/src/main/java/**/SignDao.kt`
- `core/database/src/main/java/**/SignEntity.kt`
- `core/database/src/main/java/**/SignFtsEntity.kt`

### Analyze:
1. FTS5 offline search: accuracy, Gardiner code lookup, Arabic text?
2. Sign detail: all fields populated (unicode, transliteration, description, category)?
3. Lesson progression: logic correct? Completion tracking?
4. Write mode: `mode=smart` always sent? Response rendering?
5. Phrase shortcuts: work correctly?
6. MdC rendering: correct conversion from text to hieroglyphs?
7. Translate: bidirectional? Quality of results?

### Write to stage file:
```markdown
# Stage 10: Dictionary, Translate & Write

## FTS5 Search Analysis
[Query format, tokenizer, accuracy, edge cases]

## Sign Detail Completeness
[All fields, Gardiner mapping, Unicode display]

## Lesson System Logic
[Progression, completion, tracking]

## Write Feature
[Smart mode, MdC rendering, phrase shortcuts]

## Translate Feature
[Input/output flow, quality]

## Issues Found
```

---

## Stage 11: Web Backend Parity & Cross-Comparison
**Output → `_investigation/stage-11-web-parity.md`**

### Read web backend files:
```
D:\Personal attachements\Projects\Wadjet-v3-beta\routes\audio.py
D:\Personal attachements\Projects\Wadjet-v3-beta\routes\scan.py
D:\Personal attachements\Projects\Wadjet-v3-beta\routes\landmarks.py
D:\Personal attachements\Projects\Wadjet-v3-beta\routes\chat.py
D:\Personal attachements\Projects\Wadjet-v3-beta\services\tts_service.py
D:\Personal attachements\Projects\Wadjet-v3-beta\services\thoth_chat.py
D:\Personal attachements\Projects\Wadjet-v3-beta\services\landmark_pipeline.py
D:\Personal attachements\Projects\Wadjet-v3-beta\services\generate_landmark_details.py
D:\Personal attachements\Projects\Wadjet-v3-beta\services\pregenerate_story_images.py
```

Also check if `D:\Personal attachements\Projects\Wadjet-Analysis\` has useful analysis data.

### Compare EVERY feature:
For each feature, the web backend is the **source of truth**. Document every difference.

### Write to stage file:
```markdown
# Stage 11: Web Backend Parity

## Feature-by-Feature Comparison

### TTS / Audio
| Aspect | Web Backend | Android | Match? | Issue |
...

### Scan
| Aspect | Web Backend | Android | Match? | Issue |
...

### Landmarks / Identify
...

### Chat / Thoth
...

### Stories
...

### Dictionary / Write / Translate
...

### Auth
...

## Summary: Parity Gaps
| # | Feature | Gap Description | Severity |
...
```

---

## Stage 12: Online Reference Verification & Testing Assessment
**Output → `_investigation/stage-12-references-testing.md`**

### Fetch and verify against online sources:

**Core Egyptological references (MUST fetch):**
- https://en.wikipedia.org/wiki/Egyptian_hieroglyphs — hieroglyph inventory
- https://en.wikipedia.org/wiki/Manuel_de_Codage — MdC standard (the transliteration system the app uses)
- https://en.wikipedia.org/wiki/Egyptian_language — phonology, reconstructed pronunciation
- https://en.wikipedia.org/wiki/Transliteration_of_Ancient_Egyptian — uniliteral signs table
- https://en.wikipedia.org/wiki/Gardiner%27s_sign_list — complete Gardiner classification with unicode
- https://en.wiktionary.org/wiki/Appendix:Egyptian_hieroglyphs — sign list with values

**Individual word/concept verification (fetch each to verify pronunciation):**
- https://en.wikipedia.org/wiki/Ankh — "anx" pronunciation
- https://en.wikipedia.org/wiki/Scarab_(artifact) — "xpr/xpri" kheper/khepri
- https://en.wikipedia.org/wiki/Ma%27at — "mAat" pronunciation
- https://en.wikipedia.org/wiki/Wadjet — "wADt" app namesake
- https://en.wikipedia.org/wiki/Djed — "Dd" djed pillar
- https://en.wikipedia.org/wiki/Was-sceptre — "wAs" was scepter
- https://en.wikipedia.org/wiki/Ka_(Egyptian_soul) — "kA" ka spirit
- https://en.wikipedia.org/wiki/Ba_(Egyptian_soul) — "bA" ba soul
- https://en.wikipedia.org/wiki/Akh — "Ax" akh spirit
- https://en.wikipedia.org/wiki/Sekhem_(scepter) — "sxm" sekhem
- https://en.wikipedia.org/wiki/Duat — "dwAt" underworld
- https://en.wikipedia.org/wiki/Kemet — "kmt" black land (Egypt)
- https://en.wikipedia.org/wiki/Deshret — "dSrt" red land / red crown
- https://en.wikipedia.org/wiki/Nefer — "nfr" nefer/beautiful
- https://en.wikipedia.org/wiki/Htp_(hieroglyph) — "Htp" hetep
- https://en.wikipedia.org/wiki/Hathor — "Hwt-Hr" hat-hor pronunciation
- https://en.wikipedia.org/wiki/Thoth — "DHwty" djehuti pronunciation
- https://en.wikipedia.org/wiki/Anubis — "inpw" anpu pronunciation
- https://en.wikipedia.org/wiki/Osiris — "wsjr" wesir pronunciation
- https://en.wikipedia.org/wiki/Isis — "Ast" aset pronunciation
- https://en.wikipedia.org/wiki/Ptah — "ptH" petah pronunciation
- https://en.wikipedia.org/wiki/Sekhmet — "sxmt" sekhmet pronunciation
- https://en.wikipedia.org/wiki/Khnum — "Xnmw" khnum pronunciation
- https://en.wikipedia.org/wiki/Bastet — "bAstt" bastet pronunciation
- https://en.wikipedia.org/wiki/Sobek — "sbk" sobek pronunciation
- https://en.wikipedia.org/wiki/Geb — "Gbb" geb pronunciation
- https://en.wikipedia.org/wiki/Nut_(goddess) — "nwt" noot pronunciation
- https://en.wikipedia.org/wiki/Shu_(Egyptian_god) — "Sw" shoo pronunciation
- https://en.wikipedia.org/wiki/Montu — "mnTw" montu pronunciation

**Unicode verification:**
- https://en.wikipedia.org/wiki/Egyptian_Hieroglyphs_(Unicode_block) — U+13000–U+1342F code chart
- https://unicode.org/charts/PDF/U13000.pdf — official Unicode chart (if accessible)

**Academic pronunciation consensus:**
- Search for: "ancient Egyptian phonology reconstruction"
- Search for: "conventional pronunciation of ancient Egyptian"
- Search for: "Egyptological pronunciation middle Egyptian"
- Search for: "MdC transliteration guide PDF"
- Search for: "Allen Middle Egyptian grammar pronunciation"

Use the fetched data to:
1. Verify EVERY pronunciation claim from Stage 6 — word by word
2. Verify Gardiner code → Unicode mappings
3. Document the scholarly consensus on Egyptian phonology (especially vowel reconstruction)
4. Verify god names, place names, and common vocabulary pronunciations
5. Check if the app's IPA values match academic sources

### Read existing tests:
- `core/data/src/test/**/DictionaryRepositoryImplTest.kt`
- `core/database/src/androidTest/**/LandmarkDaoTest.kt`
- `core/database/src/androidTest/**/SignDaoTest.kt`
- `core/network/src/test/**/AuthInterceptorTest.kt`
- `feature/dictionary/src/test/**/DictionaryViewModelTest.kt`
- `feature/explore/src/test/**/ExploreViewModelTest.kt`

### Browse reference repos for test patterns:
- `D:\Personal attachements\Repos\23-Android-Kotlin\nowinandroid\` — testing strategies
- `D:\Personal attachements\Repos\23-Android-Kotlin\architecture-samples\` — test patterns
- `D:\Personal attachements\Repos\23-Android-Kotlin\compose-samples\` — Jetchat, Jetcaster tests

### Write to stage file:
```markdown
# Stage 12: Online References & Testing

## Pronunciation Verification from Academic Sources
| Word | App Pronunciation | Academic Source | Scholarly Pronunciation | Match? |
...

## MdC Standard Verification
| Symbol | App Interpretation | MdC Standard | Source URL | Match? |
...

## Gardiner Code Verification (spot check)
| Code | App Description | Gardiner's List | Source | Match? |
...

## Scholarly Consensus on Egyptian Phonology
[Summary of how scholars reconstruct pronunciation — vowel insertion rules, consonant values]

## Existing Test Coverage
| Test File | What It Tests | Coverage | Quality |
...

## Missing Test Coverage
| Category | What Needs Testing | Priority | Suggested Test |
...

## Reference Repo Best Practices
[Testing patterns from NiA, Architecture Samples worth adopting]

## Issues Found
```

---

# FINAL PHASE: Planning Output

> **CRITICAL: Before starting this phase, RE-READ ALL 12 stage files:**
> ```
> .specify/specs/004-logic-quality/_investigation/stage-01-existing-context.md
> .specify/specs/004-logic-quality/_investigation/stage-02-network-layer.md
> .specify/specs/004-logic-quality/_investigation/stage-03-data-layer.md
> .specify/specs/004-logic-quality/_investigation/stage-04-database-offline.md
> .specify/specs/004-logic-quality/_investigation/stage-05-viewmodels.md
> .specify/specs/004-logic-quality/_investigation/stage-06-pronunciation.md
> .specify/specs/004-logic-quality/_investigation/stage-07-scan-landmarks.md
> .specify/specs/004-logic-quality/_investigation/stage-08-chat-stories.md
> .specify/specs/004-logic-quality/_investigation/stage-09-auth-security.md
> .specify/specs/004-logic-quality/_investigation/stage-10-dictionary-write.md
> .specify/specs/004-logic-quality/_investigation/stage-11-web-parity.md
> .specify/specs/004-logic-quality/_investigation/stage-12-references-testing.md
> ```
> **Do NOT skip this step. READ THEM ALL before writing any planning file.**

Create all output files inside: `.specify/specs/004-logic-quality/`

[The 12 stage files remain in `_investigation/` as permanent evidence.]

---

### File 1: `gap-analysis.md`

Consolidate ALL issues from ALL 12 stage files into one master document.

```markdown
# Logic & Quality Gap Analysis: Wadjet Android

## Summary
[Executive summary — overall logic health, top 5 critical findings, total count by severity]

## Already Fixed vs. Still Open
[Cross-reference 001-logic-parity findings (B1-B5, G1-G10) against git log — confirmed status]

## Findings

### A. Egyptological Accuracy (from Stage 6, 12)

#### LQ-001 | 🔴 Critical | [Title]
- **Current state:** [What exists — exact file + line/function]
- **Problem:** [What's wrong — with academic source proving it's wrong]
- **Expected:** [Correct pronunciation/value — with source URL]
- **Fix:** [Specific code change]
- **Files:** [Exact files to modify]

...

### B. API Contract Fidelity (from Stage 2, 11)
...

### C. Data Layer & Mapping (from Stage 3)
...

### D. Database & Offline (from Stage 4)
...

### E. ViewModel Logic (from Stage 5)
...

### F. TTS & Audio Pipeline (from Stage 6, 7)
...

### G. Scan & Landmarks (from Stage 7)
...

### H. Chat & Stories (from Stage 8)
...

### I. Auth & Security (from Stage 9)
...

### J. Dictionary, Translate & Write (from Stage 10)
...

### K. Testing Gaps (from Stage 12)
...

## Statistics
| Severity | Count |
|----------|-------|
| 🔴 Critical | X |
| 🟠 Major | X |
| 🟡 Minor | X |
| 🔵 Enhancement | X |
| **Total** | **X** |
```

Severity guide:
- 🔴 Critical = Feature broken, wrong output, wrong pronunciation, data loss, security issue
- 🟠 Major = Feature works but wrong/incomplete, quality noticeably bad, pronunciation unnatural
- 🟡 Minor = Edge case not handled, quality could be better
- 🔵 Enhancement = Optimization, better caching, test coverage

### File 2: `spec.md`

```markdown
# Feature Specification: Logic & Quality
**Spec ID**: 004-logic-quality
**Status**: Draft
**Date**: [date]
**Investigation evidence**: See `_investigation/stage-01` through `stage-12`

## User Scenarios & Testing

### User Story 1 — Accurate Hieroglyphic Pronunciation (Priority: P0)
As a user learning hieroglyphs, I need every word pronounced correctly according to Egyptological scholarship, so that I develop accurate knowledge.
**Why P0:** The core value of the app — if pronunciation is wrong, the app is harmful.
**Acceptance Scenarios:**
1. Given the sign "anx", When TTS speaks it, Then it sounds like "ankh" [aːnəx]
2. Given any word in the ~70 known list, When TTS speaks it, Then pronunciation matches scholarly consensus
...

[Continue with more user stories — TTS quality, scan accuracy, landmark identification, chat reliability, offline behavior, auth security]

## Requirements
### Functional Requirements
- **FR-LQ-001**: Every MdC transliteration must convert to TTS-ready text matching Egyptological pronunciation
- **FR-LQ-002**: [requirement]
...

### Non-Functional Requirements
- **NFR-LQ-001**: TTS audio must not have choppy pauses between syllables
- **NFR-LQ-002**: [requirement]
...
```

### File 3: `plan.md`

```markdown
# Implementation Plan: Logic & Quality
**Spec**: 004-logic-quality
**Date**: [date]
**Evidence base**: 12 investigation stage files in `_investigation/`

## Summary
[Total findings, total tasks, estimated phases]

## Technical Context
[Architecture overview from Stage 3-5, key constraints]

## Phases

### Phase 1: Egyptological Accuracy — Pronunciation & Gardiner
**Goal:** Fix ALL pronunciation errors, complete Gardiner Unicode mapping
**Priority:** P0 — this is the app's core value
**Dependencies:** None
**Files affected:**
- `core/common/.../EgyptianPronunciation.kt`
- `feature/scan/.../GardinerUnicode.kt`
**Tasks:** [reference task IDs]
**Verification:** Listen test every word, compare with academic sources

### Phase 2: API Contract Alignment — DTOs & Response Handling
**Goal:** Fix ALL DTO mismatches, handle all response fields
**Dependencies:** None (parallel with Phase 1)
...

### Phase 3: [Name] — [Goal]
...

[Continue for all phases — order by priority: pronunciation, then API accuracy, then features, then testing]

## Complexity Tracking
| Phase | Tasks | Estimated Complexity | Key Risk |
...
```

### File 4: `tasks.md`

```markdown
# Tasks: Logic & Quality

## Format
- `[ID] [P?] [Story] Description` → `[file.kt]` (from stage-XX finding LQ-YYY)

## Phase 1: Egyptological Accuracy
- [ ] T001 P0 [US1] Fix pronunciation of "anx" → `EgyptianPronunciation.kt` (from stage-06 LQ-001)
- [ ] T002 P0 [US1] Add missing MdC consonant "X" → `EgyptianPronunciation.kt` (from stage-06 LQ-002)
...

## Phase 2: API Contract Alignment
- [ ] T020 P0 [US3] Fix IdentifyResponse DTO fields → `LandmarkModels.kt` (from stage-02 LQ-015)
...

[Every task traces back to a stage finding and a gap-analysis ID]
```

### File 5: `phase-prompts.md`

Self-contained copy-paste prompts for each phase. Each prompt must:
1. List the investigation stage files relevant to the phase
2. List exact source files to read
3. List the spec files for context
4. Give task-by-task instructions with exact code changes
5. Include build + test verification
6. Include manual quality verification (listen to TTS, compare scan output)
7. End with git commit + push

```markdown
# Phase Prompts: Logic & Quality

## Phase 1: Egyptological Accuracy

### Prompt:
[COPY-PASTE BLOCK START]

Read these investigation files for evidence:
- `.specify/specs/004-logic-quality/_investigation/stage-06-pronunciation.md`
- `.specify/specs/004-logic-quality/_investigation/stage-12-references-testing.md`

Read these planning files:
- `.specify/specs/004-logic-quality/plan.md` (Phase 1 section)
- `.specify/specs/004-logic-quality/tasks.md` (Phase 1 section)
- `.specify/specs/004-logic-quality/pronunciation-audit.md`

Read these source files:
- `core/common/src/main/java/**/EgyptianPronunciation.kt`
- `feature/scan/src/main/java/**/util/GardinerUnicode.kt`

Tasks:
1. [T001] Fix pronunciation of "anx" — change line X from "..." to "..."
2. [T002] ...
...

Verification:
1. `./gradlew assembleDebug` — zero errors
2. `./gradlew testDebugUnitTest` — all pass
3. NEW TESTS: Run EgyptianPronunciationTest — all 70+ words pass
4. Manual listen test: Request TTS for words "anx", "nfr", "mAat", "xpr" — verify pronunciation
5. Compare with web app: play the same words on web → should sound identical

Commit:
git add -A
git commit -m "Phase 1: Logic quality — Egyptological accuracy fixes"
git push origin 004-logic-quality

[COPY-PASTE BLOCK END]

## Phase 2: [Name]
...
```

### File 6: `pronunciation-audit.md`

This file is the **most critical** — it must contain word-by-word academic verification.

```markdown
# Egyptian Pronunciation Audit

## Methodology
Sources consulted:
1. Wikipedia: Egyptian hieroglyphs, Manuel de Codage, Egyptian language, Transliteration of Ancient Egyptian
2. Gardiner's Sign List (Wikipedia)
3. [Any additional sources fetched in Stage 12]

## Current Implementation Review
[Line-by-line analysis of EgyptianPronunciation.kt from Stage 6]

## Tokenizer Bug Analysis
### MdC Structural Characters Not Stripped
| Character | In MdC_STRIP? | Used in MdC for | Impact on pronunciation |
| 0-9 | ❌ NO | Gardiner codes (A1, B2) | Would be pronounced as sounds |
| - | ❌ NO | Horizontal join | Works in WORD_MAP but fails in fallback |
| # | ❌ NO | Damaged text | Would be pronounced |
| & | ❌ NO | Special blocks | Would be pronounced |

### Determinative Code Handling
[What happens when MdC text contains A40, Z1, N5, etc.?]

### Biliteral/Triliteral Fallback Quality
[Character-by-character tokenizer can't detect multi-char phonemes for unknown words]
| MdC Word | Not in WORD_MAP | Fallback Produces | Correct | Quality 1-5 |
| wHm | ✗ | [simulated output] | wehem | ? |
| sSAt | ✗ | [simulated output] | seshat | ? |
...
[Minimum 20 common words tested through fallback]

### Duplicate WORD_MAP Keys
[Any key appearing twice — which value wins?]

### "Already Pronounceable" Detection Edge Cases
[Test: MdC with 'u', English names, mixed text]

## Known Word List — Academic Verification
| # | MdC Input | App TTS Output | Scholarly Pronunciation | IPA | Source | Status | Fix |
|---|-----------|---------------|------------------------|-----|--------|--------|-----|
| 1 | anx | [current] | ankh | [ˈʕanax] | Wikipedia: Ankh | ✅/❌ | [if ❌, what to change] |
| 2 | nfr | [current] | nefer | [ˈnaːfar] | Allen, Middle Egyptian | ✅/❌ | ... |
| 3 | mAat | [current] | maat | [muˈʕat] | Wikipedia: Maat | ✅/❌ | ... |
| 4 | xpr | [current] | kheper | [ˈxapar] | Wikipedia: Scarab | ✅/❌ | ... |
| 5 | wADt | [current] | wadjet | [ˈwaːɟat] | Wikipedia: Wadjet | ✅/❌ | ... |
...
[ALL ~100+ words in the WORD_MAP — NONE skipped]

## MISSING Words That Must Be Added
| # | MdC | Pronunciation | Category | Why Essential |
| 1 | wHm | wehem | Common verb (to repeat) | Very common in texts |
| 2 | ... | ... | ... | ... |
...
[All important missing words — gods, pharaohs, monuments, common verbs, numbers]

## Phoneme Mapping — MdC Standard Verification
| MdC | Name | App Maps To | Correct IPA | Correct TTS Text | Source | Status |
|-----|------|-----------|-------------|------------------|--------|--------|
| A | aleph/glottal | [current] | /ʔ/ | "a" (glottal stop) | MdC standard | ✅/❌ |
| i | reed/yod | [current] | /j/ or /iː/ | "ee" | MdC standard | ✅/❌ |
| y | double reed | [current] | /j/ | "y" | MdC standard | ✅/❌ |
| a | arm/ayin | [current] | /ʕ/ | "aa" (pharyngeal) | MdC standard | ✅/❌ |
| w | quail chick | [current] | /w/ | "w" | MdC standard | ✅/❌ |
| b | foot | [current] | /b/ | "b" | MdC standard | ✅/❌ |
| p | stool | [current] | /p/ | "p" | MdC standard | ✅/❌ |
| f | horned viper | [current] | /f/ | "f" | MdC standard | ✅/❌ |
| m | owl | [current] | /m/ | "m" | MdC standard | ✅/❌ |
| n | water | [current] | /n/ | "n" | MdC standard | ✅/❌ |
| r | mouth | [current] | /ɾ/ | "r" | MdC standard | ✅/❌ |
| h | shelter | [current] | /h/ | "h" | MdC standard | ✅/❌ |
| H | wick | [current] | /ħ/ | "hh" (pharyngeal) | MdC standard | ✅/❌ |
| x | placenta | [current] | /x/ | "kh" | MdC standard | ✅/❌ |
| X | belly+tail | [current] | /ç/ | "ch" (palatal) | MdC standard | ✅/❌ |
| s | folded cloth | [current] | /s/ | "s" | MdC standard | ✅/❌ |
| S | pool | [current] | /ʃ/ | "sh" | MdC standard | ✅/❌ |
| q | hill slope | [current] | /q/ | "q" (uvular) | MdC standard | ✅/❌ |
| k | basket | [current] | /k/ | "k" | MdC standard | ✅/❌ |
| g | jar stand | [current] | /ɡ/ | "g" | MdC standard | ✅/❌ |
| t | bread | [current] | /t/ | "t" | MdC standard | ✅/❌ |
| T | tethering rope | [current] | /tʃ/ | "tch" | MdC standard | ✅/❌ |
| d | hand | [current] | /d/ | "d" | MdC standard | ✅/❌ |
| D | snake | [current] | /dʒ/ | "dj" | MdC standard | ✅/❌ |
| z | bolt | [current] | /z~s/ | "z" or "s" | MdC standard | ✅/❌ |
| l | (late Egyptian) | [current] | /l/ | "l" | Late Egyptian | ✅/❌ |

## Missing Phonemes
[Any MdC consonants not in the app's mapping]

## Vowel Epenthesis Rules
[Current rules, correctness per scholarly convention (Egyptologists typically insert "e" between consonants)]
[Test: 3+ consonant clusters — does "e" insertion produce natural results?]

## Gardiner Unicode Mapping — Spot Verification
| Code | Category | App Unicode | Correct Unicode | Verified | Notes |
|------|----------|-------------|-----------------|----------|-------|
| A1 | Man and his activities | [current] | U+13000 | ✅/❌ | ... |
| A2 | Man with hand to mouth | [current] | U+13001 | ✅/❌ | ... |
...
[At least 50 critical signs checked — covering ALL categories A through Aa]

## Voice Configuration Audit
| Context | Voice | Style Prompt | Assessment | Needs Change? |
|---------|-------|-------------|-----------|---------------|
| Hieroglyph signs | Orus | [current prompt text] | [quality assessment] | ... |
| Story narration | Aoede | [current prompt text] | [quality assessment] | ... |
| Landing/general | Charon | [current prompt text] | [quality assessment] | ... |

## TTS Data Flow: End-to-End Trace
| Step | Component | What happens | Data at this point | Issues |
|------|-----------|-------------|-------------------|--------|
| 1 | UI: tap listen | onClick handler | MdC text: "anx" | ... |
| 2 | ViewModel | calls toSpeech()? | "ankh" or "anx"? | ... |
| 3 | Repository | audio request | HTTP body fields | ... |
| 4 | API Service | Retrofit call | URL + params | ... |
| 5 | Server | audio.py | Processing | ... |
| 6 | Server | tts_service.py | TTS generation | **DISCONNECT?** |
| 7 | Response | audio bytes | Format, encoding | ... |
| 8 | Client | playback | Player type, caching | ... |

## TTS Architecture DISCONNECT Check
[From user memory: "tts_service.py never called by /api/tts endpoint" — is this STILL true?]

## Recommendations
### Priority 1: Critical Bugs (pronunciation wrong)
...
### Priority 2: Missing Handling (tokenizer gaps)
...
### Priority 3: Completeness (missing words, missing phonemes)
...
### Priority 4: Quality (naturalness, TTS optimization)
...
```

### File 7: `api-parity.md`

```markdown
# API Parity Audit
**Source**: Stage 2 (network layer) + Stage 11 (web parity)

## Endpoint Coverage
| # | Method | Endpoint | API Service | DTO | Repo | ViewModel | Status | Notes |
|---|--------|----------|-------------|-----|------|-----------|--------|-------|
| 1 | POST | /api/auth/register | AuthApiService | AuthModels | AuthRepoImpl | AuthVM | ✅/❌/⚠️ | ... |
| 2 | POST | /api/auth/login | ... | ... | ... | ... | ... | ... |
...
[ALL 48 endpoints]

## DTO vs. Actual Response — Field-by-Field
### ScanModels.kt vs /api/scan response
| Field | In API Contract | In DTO | In Domain | Displayed | Issue |
...

### LandmarkModels.kt vs /api/explore/identify response
...

[ALL 10 DTO files compared]

## Missing Endpoints
[Endpoints in api-contract.md not implemented]

## Response Handling Issues
[Mismatched field names, missing nullable, wrong types]

## Request Format Issues
[Wrong parameters, missing headers, encoding issues]
```

### File 8: `testing.md`

```markdown
# Testing Plan: Logic & Quality
**Source**: Stage 12 (testing assessment)

## Current Coverage
- 6 test files, ~X tests total
- [Assessment of each existing test]

## Priority 1: Pronunciation Tests (MUST HAVE)
- [ ] UT-001 EgyptianPronunciation.toSpeech() — every known word produces correct output
- [ ] UT-002 EgyptianPronunciation phoneme mapping — every MdC consonant maps correctly
- [ ] UT-003 EgyptianPronunciation vowel epenthesis — natural-sounding output
- [ ] UT-004 GardinerUnicode mapping — every code maps to correct Unicode

## Priority 2: API Response Parsing Tests
- [ ] UT-010 ScanResponse parsing — all fields including confidence, quality hints
- [ ] UT-011 IdentifyResponse parsing — ensemble fields, tiebreak data
- [ ] UT-012 ChatResponse SSE parsing — streaming tokens
- [ ] UT-013 StoryResponse parsing — chapters, interactions, images
...

## Priority 3: Repository & ViewModel Tests
- [ ] UT-020 DictionaryRepository — FTS5 search accuracy
- [ ] UT-021 ScanRepository — upload + parse flow
- [ ] UT-022 AuthRepository — token refresh race condition
...

## Priority 4: Integration Tests
- [ ] IT-001 Scan end-to-end — upload image → get result → display
- [ ] IT-002 TTS end-to-end — text → API → audio → playback
...

## Test Infrastructure Needed
[Test fixtures, mock server, fake repositories]
```

### File 9: `workflow.md`

```markdown
# Git Workflow: Logic & Quality

## Branch Strategy
- Feature branch: `004-logic-quality`
- Base: `main`
- Create: `git checkout -b 004-logic-quality`

## Commit Convention
`Phase X: Logic quality — [description]`

## Phase Workflow
1. Read investigation stage files relevant to the phase
2. Read spec + tasks for the phase
3. Implement changes
4. Build: `./gradlew assembleDebug` → zero errors
5. Test: `./gradlew testDebugUnitTest` → all pass
6. **Pronunciation verify** (if phase affects TTS): Listen to words, compare with web app
7. **Output verify** (if phase affects scan/landmark): Compare results with web app
8. Commit + push

## Verification Before Each Push
- [ ] `./gradlew assembleDebug` clean
- [ ] `./gradlew testDebugUnitTest` all pass
- [ ] No regressions in other features
- [ ] If TTS changes: manual listen test
- [ ] If scan changes: compare output with web app
- [ ] If auth changes: login/logout/refresh test

## Verification Before Final Merge
- [ ] ALL phases complete
- [ ] ALL tests pass (existing + new)
- [ ] TTS: Every word in pronunciation-audit.md sounds correct
- [ ] Scan: Results match web app quality
- [ ] Landmarks: All images load, identification correct
- [ ] Chat: Streaming works, history persists
- [ ] Stories: All interactions work, TTS narration plays
- [ ] Auth: Login/logout/refresh/Google Sign-In all work
- [ ] Offline: Dictionary search works offline, scan history accessible
- [ ] No hardcoded secrets
- [ ] ProGuard rules don't break serialization
```

---

## Constraints
- **Logic only** — do NOT change screen layouts, navigation, design system, colors, fonts
- **Web parity** — the web app is the source of truth for how features should behave
- **Egyptological accuracy** — every pronunciation, transliteration, and Gardiner mapping must be verified against academic sources
- **Smart defaults** — system picks best AI provider, no user selectors
- **Fallback chains** — Gemini → Groq → Browser (TTS) | FLUX → SDXL → placeholders (images)
- **Offline first** — Room cache must work without network
- **Security** — EncryptedSharedPreferences, JWT rotation, no secrets in code
- **No breaking changes** — maintain existing API contracts

## Quality Standard
For every feature, ask:
1. "Does this produce the same quality output as the web app?"
2. "Would an Egyptologist be satisfied with the pronunciation?"
3. "Does this handle every error gracefully?"
4. "Is this data correct and complete?"
5. "Is there an online academic source confirming this is correct?"

## Final Quality Checklist
Before finalizing planning files, verify against investigation evidence:
- [ ] Every ViewModel (20) analyzed — evidence in `stage-05`
- [ ] Every Repository (9+9) reviewed — evidence in `stage-03`
- [ ] Every API Service (11) checked — evidence in `stage-02`
- [ ] Every DTO (10 files) field-by-field compared — evidence in `stage-02`
- [ ] Every domain model (9) verified — evidence in `stage-03`
- [ ] EgyptianPronunciation.kt audited word-by-word — evidence in `stage-06`
- [ ] GardinerUnicode.kt verified — evidence in `stage-06`
- [ ] Online academic sources fetched and cited — evidence in `stage-12`
- [ ] Auth + Token flow reviewed — evidence in `stage-09`
- [ ] All 6 existing tests assessed — evidence in `stage-12`
- [ ] Web backend compared feature-by-feature — evidence in `stage-11`
- [ ] git log cross-referenced — evidence in `stage-01`
- [ ] Every finding has file:line level fix detail
- [ ] Every task traces to a stage finding
- [ ] Phase prompts include manual pronunciation listen tests
- [ ] Total output: 12 investigation files + 9 planning files = 21 files
