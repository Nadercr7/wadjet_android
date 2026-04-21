# Stage 13 — Audio, TTS, and Pronunciation

**Date:** 2025-07-22  
**Auditor:** Automated (Copilot)  
**Scope:** EgyptianPronunciation, AudioApiService, TTS playback in all features, STT, TTS settings

---

## Summary

| Metric | Value |
|---|---|
| **TTS-enabled features** | 7 (Chat, Stories, Dictionary Browse/Lesson/SignDetail, Scan, ScanResult) |
| **Audio playback method** | `MediaPlayer` (no ExoPlayer, no shared manager) |
| **Code duplication** | `playWavBytes()` pattern copy-pasted 7 times |
| **Audio focus handling** | **NONE** — plays over phone calls |
| **TTS settings respected** | **NO** — `ttsEnabled` and `ttsSpeed` in DataStore are decorative |
| **STT** | Chat only, dual-tier: Android SpeechRecognizer + server Groq Whisper fallback |
| **Critical issues** | 3 |
| **Major issues** | 6 |

---

## 1. EgyptianPronunciation Engine

**Location:** `core/common/.../EgyptianPronunciation.kt`

### Algorithm (3 levels)
1. **Whole-word lookup** — `WORD_MAP` with ~70 hand-tuned words (gods, titles, nature, buildings)
2. **Phoneme mapping** — 28 MdC characters → English sound equivalents via `PHONEME_MAP`
3. **Vowel epenthesis** — inserts `'e'` between consecutive consonants (Egyptological convention)

### Coverage
- ~70 whole words in `WORD_MAP`
- 28 phoneme mappings covering standard Egyptian alphabet + `l`
- Constants: `VOICE = "Orus"`, `STYLE` (ancient sage persona)

### Edge Cases
- Blank → empty string
- Already-pronounceable (contains `e`, `o`, `u`) → returned unchanged
- MdC structure markers stripped before conversion
- Well-tested: `EgyptianPronunciationTest.kt` exists ✅

---

## 2. TTS Integration Points

| Feature | ViewModel | Trigger | API Context | Pronunciation Transform | Local TTS Fallback | Issues |
|---|---|---|---|---|---|---|
| **Chat** | `ChatViewModel` | `speakMessage()` per bot msg | `thoth_chat` | None | ✅ Android TTS (en-US / ar) | Temp file in system tmp, not cacheDir |
| **Stories** | `StoryReaderViewModel` | `speakChapter()` — sequential paragraphs | `story_narration` (per-chapter voice/style) | None | ✅ `localTtsText` + `CompletableDeferred` | Uses `GlobalScope` in `onCleared()` |
| **Dictionary Browse** | `DictionaryViewModel` | `speakSign(text)` | `hieroglyph_pronunciation`, voice=Orus | ✅ `EgyptianPronunciation.toSpeech()` | ✅ `localTtsText` on null/error | — |
| **Dictionary Lesson** | `LessonViewModel` | `speakSign(text)` | Same | ✅ Same | ❌ **NO fallback** | Silent failure on null bytes |
| **Dictionary SignDetail** | `SignDetailViewModel` | `speakSign(text)` | Same | ✅ Same | ❌ **NO fallback** — sets error string | Silent failure |
| **Scan Result** | `ScanResultViewModel` | `speak(key, text, lang)` | Dynamic context | ✅ For transliteration | ✅ `localTtsText` + `localTtsLang` | `stopMediaPlayer()` not in `onCleared()` |
| **Scan Live** | `ScanViewModel` | `speak(key, text, lang)` | Same | ✅ Same | ✅ Same | Same issue |

---

## 3. Audio Playback Architecture

### Pattern (duplicated 7 times)
```
API call → ByteArray? → File.createTempFile(".wav") → MediaPlayer → setDataSource → prepare → start
    → onCompletionListener: release + delete temp
    → onErrorListener: release + delete temp (sometimes missing)
```

### No Shared Audio Manager
Every ViewModel duplicates the same `playWavBytes()` logic independently.

### Resource Leak Analysis

| ViewModel | Released in `onCleared()`? | Temp cleanup on error? |
|---|---|---|
| ChatViewModel | ✅ | ✅ `deleteOnExit()` |
| StoryReaderViewModel | ✅ via `stopNarration()` | ✅ |
| DictionaryViewModel | ✅ | On completion only |
| LessonViewModel | ✅ | ❌ **Missing on error** |
| SignDetailViewModel | ⚠️ Partial — no `stop()`, no null-assign | ❌ **Missing on error** |
| ScanResultViewModel | ❌ **`onCleared()` may miss it** | On completion only |
| ScanViewModel | ❌ **Same** | On completion only |

### Audio Focus
**Zero audio focus handling.** No `AudioManager.requestAudioFocus()`, no `OnAudioFocusChangeListener`. MediaPlayer plays over phone calls, navigation, and other media.

---

## 4. STT (Speech-to-Text) Flow

**Location:** ChatScreen only

### Dual-tier Architecture
1. **Primary: Android `SpeechRecognizer`** (on-device Google)
   - `SpeechRecognizer.isRecognitionAvailable(context)` gate
   - `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` with `en-US` hardcoded
   - Results → `ChatViewModel.onSttResult()` → populate input

2. **Fallback: Server STT (Groq Whisper)**
   - Records via `MediaRecorder` → OGG/OPUS → temp file
   - Uploads to `POST /api/audio/stt` (multipart)
   - `chatRepository.transcribe()` → returns text

### Issues
- Hardcoded `en-US` — Arabic users can't use voice input
- Permission denied → mic button does nothing (no re-prompt)
- Only in Chat — no other feature has STT

---

## 5. TTS Settings (BROKEN)

### DataStore Keys
| Key | Type | Default | Stored in |
|---|---|---|---|
| `tts_enabled` | Boolean | `true` | `UserPreferencesDataStore` |
| `tts_speed` | Float | `1.0f` | `UserPreferencesDataStore` |

### Settings UI
- Toggle switch + speed slider in Settings screen ✅

### Actual Usage
**ZERO ViewModels read `ttsEnabled` or `ttsSpeed` from DataStore.** The settings exist in the UI, can be toggled by the user, but have absolutely no effect on audio playback anywhere in the app.

---

## 6. All Issues

### Critical

| # | Issue | Impact |
|---|---|---|
| 1 | **TTS settings decorative** — `ttsEnabled`/`ttsSpeed` from DataStore never consumed by any playback ViewModel | User disables TTS in Settings → audio still plays |
| 2 | **No audio focus handling** — MediaPlayer plays over phone calls, navigation, other media | Android compliance failure |
| 3 | **Massive code duplication** — `playWavBytes()` pattern copy-pasted 7 times across ViewModels | Maintenance burden, inconsistent error handling |

### Major

| # | Issue | Impact |
|---|---|---|
| 4 | `SignDetailViewModel` MediaPlayer not properly released — no `stop()`, no null-assign, no temp file cleanup on error | Resource leak |
| 5 | `ScanResultViewModel`/`ScanViewModel` may not release MediaPlayer on destruction | Resource leak |
| 6 | `LessonViewModel` has no local TTS fallback — server returns null/error → silence | Silent failure |
| 7 | `SignDetailViewModel` has no local TTS fallback — sets error string instead of playing | Silent failure |
| 8 | STT hardcoded to `en-US` — Arabic users can't use voice input | i18n gap |
| 9 | Temp files not cleaned on error in `LessonVM` and `SignDetailVM` | Storage leak |

### Minor

| # | Issue | Impact |
|---|---|---|
| 10 | ChatVM temp files in system tmp (not cacheDir) — survive cache clearing | Inconsistency |
| 11 | No MediaPlayer error listener in SignDetailVM | Silent failure |
| 12 | Local TTS in Chat → no `UtteranceProgressListener` → dismiss callback premature | UI timing |
| 13 | `SpeakRequest.context` defaults to `"dictionary"` but never used | Misleading |
| 14 | No TTS audio caching — same text re-fetched every time | Wasted network + latency |
