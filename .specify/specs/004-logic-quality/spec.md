# Feature Specification: Logic & Quality

**Spec ID**: 004-logic-quality
**Status**: Draft
**Date**: 2026-04-15
**Investigation evidence**: See `_investigation/stage-01` through `stage-12`

---

## User Scenarios & Testing

### User Story 1 — Accurate Hieroglyphic Pronunciation (Priority: P0)

As a user learning hieroglyphs, I need every word pronounced correctly according to Egyptological scholarship, so that I develop accurate knowledge.

**Why P0:** The core value of the app — if pronunciation is wrong, the app is harmful to learners.

**Acceptance Scenarios:**
1. Given the sign "anx", When TTS speaks it, Then it sounds like "ankh" [aːnəx]
2. Given any word in the ~100 WORD_MAP list, When TTS speaks it, Then pronunciation matches scholarly consensus
3. Given an unknown MdC word (not in WORD_MAP), When the fallback path processes it, Then phoneme-by-phoneme conversion produces a recognizable pronunciation
4. Given MdC text containing structural characters (`.`, `:`, `=`, `*`, `-`), When processed, Then structural characters are stripped and do not affect pronunciation
5. Given all 27 PHONEME_MAP entries, When each MdC consonant is converted, Then the TTS text matches the MdC standard

---

### User Story 2 — Complete Scan Results (Priority: P0)

As a user scanning hieroglyphs, I need ALL API response fields displayed, so that I see the complete analysis.

**Why P0:** The scan is the app's flagship feature; missing data makes results incomplete.

**Acceptance Scenarios:**
1. Given a successful scan, When results are displayed, Then all glyphs include `code`, `glyph`, `transliteration`, `description`, `confidence`
2. Given a scan response with `glyph_count`/`num_detections`, When mapped to domain, Then the count is correctly preserved
3. Given a scan response with all timing and AI metadata, When displayed, Then no fields are silently dropped

---

### User Story 3 — Complete Dictionary Detail (Priority: P0)

As a user browsing dictionary signs, I need all Egyptological data displayed, so that I can study each sign thoroughly.

**Why P0:** Scholarly users need `logographicValue`, `determinativeClass`, `exampleUsages`, `relatedSigns`.

**Acceptance Scenarios:**
1. Given a sign detail API response with `logographic_value`, When displayed, Then the logographic value is visible
2. Given a sign detail with `example_usages`, When displayed, Then example words are shown
3. Given a sign detail with `related_signs`, When displayed, Then related signs are navigable
4. Given a sign detail with `determinative_class`, When displayed, Then the class is labeled

---

### User Story 4 — Story Interaction Feedback (Priority: P0)

As a user interacting with stories, I need correction feedback when I answer wrong, so that I learn from mistakes.

**Why P0:** Without `correctAnswer`, `targetGlyph`, `hint`, users get no learning feedback.

**Acceptance Scenarios:**
1. Given a wrong `choose_glyph` answer, When the result is shown, Then the correct glyph is displayed
2. Given a wrong `write_word` answer, When the result is shown, Then the target glyph and hint are displayed
3. Given a `story_decision`, When the result is shown, Then the chosen path (`choiceId`) is tracked
4. Given a correct answer of any type, When the result is shown, Then positive reinforcement is shown with explanation

---

### User Story 5 — Reliable Auth & Session (Priority: P1)

As a user, I need my auth state to be consistent and reactive, so that I never get stuck in a half-logged-in state.

**Why P1:** Split-brain auth (Firebase ✓ but backend ✗) causes all API calls to fail silently.

**Acceptance Scenarios:**
1. Given backend auth fails after Firebase succeeds, When the user tries to use the app, Then an error is surfaced (not silent partial auth)
2. Given the user signs out, When sign-out completes, Then navigation returns to Welcome screen
3. Given the backend token expires, When the user is on any screen, Then auth state reactively updates (not a one-shot check)
4. Given multiple simultaneous 401 responses, When token refresh is triggered, Then only one refresh occurs and others wait without blocking OkHttp threads

---

### User Story 6 — Thread-Safe ViewModel Lifecycle (Priority: P1)

As a user navigating the app, I need ViewModels to properly manage resources, so that I don't experience memory leaks, audio playing after navigation, or lost data.

**Acceptance Scenarios:**
1. Given ScanViewModel holds a MediaPlayer, When the user navigates away, Then MediaPlayer is released in `onCleared()`
2. Given HistoryViewModel collects a Flow, When `refresh()` is called, Then the old collector is cancelled before starting a new one
3. Given StoryReaderViewModel needs to save progress, When the ViewModel is cleared, Then progress is saved using a non-cancellable scope
4. Given ChatHistoryStore reads from disk, When ChatViewModel initializes, Then file I/O happens on a background dispatcher

---

### User Story 7 — Offline Dictionary & Landmarks (Priority: P1)

As a user without internet, I need dictionary and landmark data available offline, so that I can continue studying.

**Acceptance Scenarios:**
1. Given the user has previously loaded dictionary signs, When offline, Then cached signs are returned including search results
2. Given the user searches "ankh" offline, When results are shown, Then FTS results are returned (not the default all-signs list)
3. Given the user views a landmark detail offline, When an IOException occurs, Then cached detail JSON is returned as fallback
4. Given categories/alphabet/lessons have been loaded, When offline, Then cached data is available (not just signs)

---

### User Story 8 — Chat Session Integrity (Priority: P1)

As a user chatting with Thoth, I need conversations to maintain correct session context.

**Acceptance Scenarios:**
1. Given the user loads an old conversation, When they send a new message, Then the `sessionId` matches the loaded conversation
2. Given the user clears chat, When they send a new message, Then a fresh session is created
3. Given the SSE connection drops, When the user retries, Then the partial response is preserved or the message is re-sent
4. Given chat suggestion chips, When tapped, Then the suggestion text is correctly sent (not the stale input)

---

### User Story 9 — Secure Token Management (Priority: P1)

As a user, I need my authentication tokens handled securely without blocking the app.

**Acceptance Scenarios:**
1. Given a token refresh is needed, When the interceptor handles it, Then it does NOT use `runBlocking` on OkHttp threads
2. Given a 429 rate limit response, When the interceptor handles it, Then it does NOT use `Thread.sleep` on OkHttp threads
3. Given a refresh token is sent, When the request is built, Then proper cookie handling is used
4. Given a refresh response, When the access token is extracted, Then proper JSON parsing is used (not regex)

---

### User Story 10 — FTS Search Quality (Priority: P2)

As a user searching the dictionary, I need results ranked by relevance, not alphabetical order.

**Acceptance Scenarios:**
1. Given the user searches "bird", When results are shown, Then the most relevant match appears first
2. Given the user searches with diacritics (ḥ, ḫ, š), When the FTS query runs, Then diacritics are preserved or properly handled
3. Given a single-character search, When debounce completes, Then no API call is made (minimum query length)

---

## Requirements

### Functional Requirements

| ID | Description | Source | Priority |
|----|-------------|--------|----------|
| FR-LQ-001 | Every MdC transliteration must convert to TTS-ready text matching Egyptological pronunciation conventions | Stage 6 | P0 |
| FR-LQ-002 | PHONEME_MAP must cover all 24 MdC consonants plus `j` alias and `l` (late Egyptian) | Stage 6 | P0 |
| FR-LQ-003 | WORD_MAP entries must produce pronunciations verified against academic sources | Stage 6 | P0 |
| FR-LQ-004 | Tokenizer must strip hyphens, digits, and all MdC structural characters | Stage 6 (P-04, P-05) | P0 |
| FR-LQ-005 | All DTO→Domain mappings must preserve every API response field | Stages 3, 8, 10 | P0 |
| FR-LQ-006 | `InteractResponse` must map `correctAnswer`, `targetGlyph`, `gardinerCode`, `hint`, `choiceId` to domain | Stage 8 (S8-01) | P0 |
| FR-LQ-007 | `Sign` domain model must include `logographicValue`, `determinativeClass`, `exampleUsages`, `relatedSigns` | Stage 10 (S10-01) | P0 |
| FR-LQ-008 | SignEntity must cache all sign detail fields for offline display | Stage 10 (S10-07) | P1 |
| FR-LQ-009 | Auth failure on backend after Firebase success must surface as error, not silent partial auth | Stage 9 (S9-03) | P1 |
| FR-LQ-010 | Sign-out must navigate user to Welcome screen | Stage 9 (S9-08) | P1 |
| FR-LQ-011 | Auth state must be a reactive Flow, not a one-shot snapshot | Stage 9 (S9-07) | P1 |
| FR-LQ-012 | ScanViewModel must release MediaPlayer in `onCleared()` | Stage 5 (S5-01) | P1 |
| FR-LQ-013 | HistoryViewModel must cancel previous Flow collector on refresh | Stage 5 (S5-02) | P1 |
| FR-LQ-014 | ChatHistoryStore must perform file I/O off main thread | Stage 5 (S5-03) | P1 |
| FR-LQ-015 | StoryReaderViewModel must save progress in non-cancellable scope | Stage 5 (S5-04) | P1 |
| FR-LQ-016 | Chat `loadConversation()` must update `sessionId` to match loaded conversation | Stage 8 (S8-03) | P1 |
| FR-LQ-017 | Offline dictionary search must use FTS (not return all cached signs) | Stage 10 (S10-04) | P1 |
| FR-LQ-018 | LandmarkDetail offline fallback must catch IOException (not just non-200) | Stage 7 (S7-08) | P1 |
| FR-LQ-019 | Token refresh must not use `runBlocking` on OkHttp interceptor threads | Stage 9 (S9-02) | P1 |
| FR-LQ-020 | Rate limit retry must not use `Thread.sleep` on OkHttp threads | Stage 9 (S9-01) | P1 |
| FR-LQ-021 | Access token parsing must use proper JSON parser, not regex | Stage 9 (S9-10) | P1 |
| FR-LQ-022 | FTS must use FTS5 with BM25 ranking (not FTS4 alphabetical) | Stage 10 (S10-02) | P2 |
| FR-LQ-023 | FTS query must sanitize input to preserve Unicode/diacritics | Stage 10 (S10-03) | P2 |
| FR-LQ-024 | Temp TTS files must be deleted on completion and on error | Stages 7, 8 (S7-02) | P2 |
| FR-LQ-025 | Story progress save must have local fallback (Room) if both Firestore and REST fail | Stage 8 (S8-10) | P2 |
| FR-LQ-026 | Feedback submission must check `response.isSuccessful` before accessing body | Stage 11 (S11-05) | P2 |
| FR-LQ-027 | Error handling must be consistent across all repositories (single pattern) | Stage 11 (S11-07) | P2 |
| FR-LQ-028 | Unpaginated API lists (scan history, favorites, progress, stories) should have pagination | Stage 11 (S11-11) | P2 |
| FR-LQ-029 | EgyptianPronunciation must have comprehensive unit tests (≥95% coverage) | Stage 12 (S12-08) | P0 |
| FR-LQ-030 | All repository implementations must have unit tests | Stage 12 | P2 |

### Non-Functional Requirements

| ID | Description | Source | Priority |
|----|-------------|--------|----------|
| NFR-LQ-001 | TTS audio must not have choppy pauses between syllables — vowel epenthesis must produce natural-sounding words | Stage 6 | P0 |
| NFR-LQ-002 | Pronunciation fallback path must produce ≥3/5 quality for any MdC input | Stage 6 | P1 |
| NFR-LQ-003 | Token refresh must not block OkHttp dispatcher threads (avoid thread starvation) | Stage 9 | P1 |
| NFR-LQ-004 | Temp file accumulation must not exceed 50MB in cache across all features | Stages 7, 8 | P2 |
| NFR-LQ-005 | Dictionary FTS search results must appear within 200ms on cached data | Stage 10 | P2 |
| NFR-LQ-006 | Repository error handling must follow a single consistent pattern across all 9 implementations | Stage 11 | P2 |
| NFR-LQ-007 | Unit test coverage for `core:common` must reach ≥95% | Stage 12 | P0 |
| NFR-LQ-008 | Unit test coverage for `core:data` repositories must reach ≥60% | Stage 12 | P2 |
| NFR-LQ-009 | CI pipeline must run instrumentation tests (Room DAOs) on every PR | Stage 12 | P2 |
| NFR-LQ-010 | `User-Agent` header must use dynamic `BuildConfig.VERSION_NAME` | Stage 11 (S11-18) | P2 |
