# Smart Defaults Design

> **Core Principle**: The user NEVER chooses technical modes. The app always picks the smartest, most realistic option automatically. Zero friction, zero configuration.

---

## 1. Scan — Always `mode=auto`

### What the Backend Does in Auto Mode
```
POST /api/scan  { file, mode: "auto" }

Step 1: AI Reader (Gemini → Groq → Grok) + ONNX (YOLOv8s → MobileNetV3) — RUN IN PARALLEL
Step 2: If AI succeeds → merge results with ONNX bounding boxes
Step 3: If AI fails → fresh AI call (Gemini → Groq → Grok)
Step 4: If still fails → sequence verify (Gemini + Grok cross-check)
Step 5: Last resort → raw AI reading (best effort)
```
Auto mode is strictly superior: it uses **all** available methods and picks the best result. No user should ever choose "ai only" or "onnx only" — those bypass the ensemble.

### Android Implementation
| What | Current State | Fix |
|------|--------------|-----|
| `ScanViewModel.submitScan()` | Sends `mode` param (currently "auto") | ✅ **Keep** — always "auto" |
| `ScanScreen` UI | No mode picker visible | ✅ **Already correct** |
| Mode selector UI | Missing (never built) | ✅ **Don't build one** |

### No Changes Needed — scan already defaults to auto.

---

## 2. Write — Always `mode=smart`

### What the Backend Does in Smart Mode
```
POST /api/write  { text: "hello world", mode: "smart" }

Step 1: Check 90+ phrase shortcuts (common Egyptian words/phrases → pre-verified MdC)
Step 2: If no shortcut → AI translate English → MdC (Gemini → Groq → Grok)
Step 3: Validate MdC codes against Gardiner sign list
Step 4: Return glyphs[] with verified codes + unicode
```
Smart mode is strictly superior: it combines shortcut precision with AI intelligence. Alpha mode (simple letter→glyph) and MdC mode (requires expert knowledge) are inferior for normal users.

### Android Implementation
| What | Current State | Fix Required |
|------|--------------|-------------|
| `WriteViewModel` | Has `_selectedMode` StateFlow with "alpha"/"smart"/"mdc" | **CHANGE**: Remove mode state. Hardcode `mode = "smart"` in `write()` |
| `WriteTab.kt` | FilterChip row for alpha/smart/mdc selection | **CHANGE**: Remove FilterChip row entirely |
| Glyph palette | Works for all modes (inserts Gardiner codes) | ✅ **Keep** — palette remains useful for manual insertion |
| Live preview | 500ms debounce → calls `/api/write` → shows preview | ✅ **Keep** — continue using smart mode |

### Code Changes (Phase 0, Task 0.10)

**`feature/dictionary/viewmodel/WriteViewModel.kt`:**
```kotlin
// REMOVE:
private val _selectedMode = MutableStateFlow("smart")
val selectedMode: StateFlow<String> = _selectedMode.asStateFlow()
fun setMode(mode: String) { _selectedMode.value = mode }

// In write() function, CHANGE:
// FROM: mode = _selectedMode.value
// TO:   mode = "smart"
```

**`feature/dictionary/screen/WriteTab.kt`:**
```kotlin
// REMOVE the entire FilterChip row:
// Row(modifier = Modifier.fillMaxWidth(), ...) {
//     listOf("alpha", "smart", "mdc").forEach { mode ->
//         FilterChip(selected = selectedMode == mode, ...)
//     }
// }
```

### Power User Escape Hatch (Optional, Low Priority)
Long-press on the text input label → hidden dialog to switch to alpha/mdc. NOT a visible UI element.

---

## 3. TTS — Server Picks Provider

### What the Backend Does
```
POST /api/audio/speak  { text, lang, context }

Step 1: Select Gemini voice based on context:
  - thoth_chat → Orus
  - story_narration → Aoede
  - default → Charon
Step 2: Try Gemini 2.5 Flash TTS (rotate through 17 API keys)
Step 3: If Gemini fails → try Groq PlayAI
Step 4: If all fail → HTTP 204 (no content)
```
The server always picks the best voice for the context. No user voice picker needed.

### Android Implementation
| What | Current State | Fix Required |
|------|--------------|-------------|
| TTS request | Sends `{text, lang, context}` | ✅ **Correct** |
| Voice picker | None built | ✅ **Don't build one** |
| 204 handling | Falls back to `LOCAL_TTS:` → Android TextToSpeech | ✅ **Correct** |
| Context values | Uses "default" everywhere | **FIX**: Use correct context per screen |

### Correct Context Values per Screen
| Screen | Context to Send |
|--------|----------------|
| Chat (Thoth) | `"thoth_chat"` |
| Story Reader | `"story_narration"` |
| Dictionary speak | `"dictionary_speak"` |
| Scan read-aloud | `"default"` |
| Everything else | `"default"` |

### Additional Fix: Story TTS Voice/Style — ALREADY DONE ✅
Story chapters include `ttsVoice` and `ttsStyle` fields. Verified on 2026-04-10: `StoryReaderViewModel.speakChapter()` already passes `voice = chapter.ttsVoice, style = chapter.ttsStyle`. No change needed.

---

## 4. Identify — Already Auto

### What the Backend Does
```
POST /api/explore/identify  { file }

Step 1: ONNX EfficientNet-B0 classifier + Gemini Vision — RUN IN PARALLEL
Step 2: merge_landmark() ensemble:
  - Case 1: Both agree → 1.15× confidence boost → "full" agreement
  - Case 2: Partial match → higher confidence wins → "partial" agreement
  - Case 3: Disagreement → Grok tiebreak (majority vote) → "tiebreak" agreement
  - Case 4: Only one source → highest confidence → "best_confidence" or "single"
Step 3: Slug resolve + fuzzy match → full landmark data
Step 4: Return {slug, name, confidence, source, agreement, description, is_known_landmark, is_egyptian, top3}
```
There's only one mode — the full ensemble. No configuration needed.

### Android Implementation
✅ **No changes needed** — identify has always been a single endpoint with no mode parameter.

---

## Summary: What to Remove vs Keep

### Remove (Phase 0)
- ❌ `WriteViewModel._selectedMode` state + `setMode()` function
- ❌ `WriteTab` FilterChip row (mode selector)

### Keep As-Is
- ✅ Scan sends `mode=auto` (already correct)
- ✅ TTS sends context (no voice picker)
- ✅ Identify (no modes)
- ✅ Glyph palette in Write (useful for manual insertion, not a mode)
- ✅ Live preview in Write (will use smart mode)

### Don't Build
- ❌ No scan mode selector
- ❌ No write mode selector
- ❌ No TTS voice picker
- ❌ No provider indicator ("Powered by Gemini") — unnecessary technical detail

### Design Philosophy
The app should feel like magic. You upload a photo → you get perfect results. You type English → you get hieroglyphs. You tap speak → you hear the best available voice. The user's cognitive load is zero.
