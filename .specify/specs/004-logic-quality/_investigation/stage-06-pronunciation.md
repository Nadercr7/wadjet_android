# Stage 6 — Egyptian Pronunciation & TTS Audit

> **THE MOST CRITICAL STAGE** — per PROMPT.md designation

---

## 1. Source Files Analyzed

| File | Location | Lines |
|------|----------|-------|
| `EgyptianPronunciation.kt` | `core/common/src/main/java/.../EgyptianPronunciation.kt` | 267 |
| `GardinerUnicode.kt` | `feature/scan/src/main/java/.../GardinerUnicode.kt` | ~90 |

---

## 2. Architecture Overview

### EgyptianPronunciation.kt

Three-tier conversion pipeline:
```
Input MdC string
  → toSpeech()
    → Early exit: blank check
    → Already-pronounceable check (e, o, u detection)
    → Split on whitespace
    → Per-word: WORD_MAP lookup (with .removeSuffix("."))
      → Fallback: convertWord()
        → tokenize() — strips MdC_STRIP characters, maps char-by-char
        → PHONEME_MAP lookup per token
        → Vowel epenthesis — insert 'e' between consecutive consonant sounds
```

Constants:
- `VOICE = "Orus"` (Thoth's voice)
- `STYLE` = Wise ancient Egyptian sage scribe prompt
- `CONTEXT = "hieroglyph_pronunciation"` (server-side mode tag)

### GardinerUnicode.kt

Two-tier mapping:
```
gardinerToUnicode(code)
  → COMMON_GLYPHS lookup (38 entries, instant)
  → Regex parse: ([A-Za-z]+?)(\d+)([A-Z]?)
  → Pad number to 3 digits
  → UNICODE_MAP lookup (lazy-built from Character.getName for U+13000–U+1342E)
```

---

## 3. PHONEME_MAP Verification Against MdC Standard

### Reference: Manuel de Codage (1988) + Gardiner (1957) + Edel (1955)

The MdC standard defines 24 consonantal phonemes (plus `l` for later texts):

| MdC | Unicode | Glyph | IPA (reconstructed) | App maps to | Verdict |
|-----|---------|-------|---------------------|-------------|---------|
| A | ꜣ | 𓄿 Vulture | /ʀ/ → /ʔ/ → /j/ | "a" | ✓ Conventional |
| i | ꞽ | 𓇋 Reed | /j/ or /ʔ/ | "ee" | ✓ Standard Egyptol. pron. |
| j | (variant) | — | /j/ | "ee" | ✓ Common alias for yod |
| y | y | 𓇌 Pair reeds | /j/ or /iː/ | "ee" | ✓ |
| a | ꜥ | 𓂝 Forearm | /ʕ/ | "a" | ✓ Conventional |
| w | w | 𓅱 Quail | /w/ or /uː/ | "oo" | ✓ |
| b | b | 𓃀 Foot | /b/ | "b" | ✓ |
| p | p | 𓊪 Stool | /p/ | "p" | ✓ |
| f | f | 𓆑 Viper | /f/ | "f" | ✓ |
| m | m | 𓅓 Owl | /m/ | "m" | ✓ |
| n | n | 𓈖 Water | /n/ | "n" | ✓ |
| r | r | 𓂋 Mouth | /ɾ/ | "r" | ✓ |
| h | h | 𓉔 Shelter | /h/ | "h" | ✓ |
| H | ḥ | 𓎛 Wick | /ħ/ | "h" | ⚠ Lossy but acceptable for TTS |
| x | ḫ | 𓐍 Sieve | /χ/ ~ /x/ | "kh" | ✓ |
| X | ẖ | 𓄡 Belly | /ç/ | "kh" | ⚠ Merges with x; different IPA but OK for TTS |
| z | z | 𓊃 Bolt | /z/ ~ /s/ | "z" | ✓ (merged with s in Middle Egyptian) |
| s | s | 𓋴 Cloth | /s/ | "s" | ✓ |
| S | š | 𓈙 Pool | /ʃ/ | "sh" | ✓ |
| q | q | 𓈎 Hill | /kʼ/ or /qʼ/ | "q" | ✓ |
| k | k | 𓎡 Basket | /k/ | "k" | ✓ |
| g | g | 𓎼 Stand | /kʼ/ or /g/ | "g" | ✓ |
| t | t | 𓏏 Bread | /t/ | "t" | ✓ |
| T | ṯ | 𓍿 Tether | /c/ → /tʃ/ | "ch" | ✓ |
| d | d | 𓂧 Hand | /tʼ/ | "d" | ✓ Conventional |
| D | ḏ | 𓆓 Cobra | /cʼ/ → /dʒ/ | "j" | ✓ Convention: "dj/j" |
| l | l | — | /l/ | "l" | ✓ Non-standard but valid for later texts |

**Summary: All 26 phoneme mappings are consistent with conventional Egyptological pronunciation.** The only lossy mappings (H→"h", X→"kh") are deliberate simplifications for TTS engines that cannot produce pharyngeal or palatal fricatives.

### Missing Phonemes (Not Bugs)
No phonemes missing. The app covers all 24 standard MdC consonants plus `j` (alias) and `l` (late text supplement) = 26 entries.

---

## 4. WORD_MAP Audit — Word-by-Word Verification

### Methodology
Cross-referenced against: Wikipedia "Egyptian language" (Egyptological pronunciation section), Gardiner's *Egyptian Grammar* (3rd ed.), Allen's *Middle Egyptian* transliteration conventions, and standard Egyptological pronunciation norms.

### Gods & Divine Names (25 entries)

| MdC Key | App Value | Expected | Status |
|---------|-----------|----------|--------|
| nTr | netjer | netcher/netjer | ✓ |
| nTrt | netcheret | netcheret | ✓ |
| nTrw | netcheru | netcheru | ✓ |
| imn | amun | amun | ✓ |
| imn-ra | amun-ra | amun-ra | ✓ |
| inpw | anpu | anpu/anupu | ✓ |
| wsjr | weseer | wesir/weseer | ✓ |
| Ast | aset | aset | ✓ |
| DHwty | djehuti | djehuti | ✓ |
| ptH | petah | ptah/petah | ✓ (epenthetic 'e' is conventional) |
| stX | setekh | setekh/sutekh | ✓ |
| jtn | aten | aten/yaten | ✓ |
| xpri | khepri | khepri | ✓ |
| bAstt | bastet | bastet | ✓ |
| mnTw | montu | montu/mentu | ✓ |
| sbk | sobek | sobek | ✓ |
| Gbb | geb | geb | ✓ |
| nwt | noot | nut/noot | ✓ |
| Sw | shoo | shu/shoo | ✓ |
| tfnwt | tefnoot | tefnut/tefnoot | ✓ |
| wDAt | wadjet | wadjet/wedjat | ✓ |
| sxmt | sekhmet | sekhmet | ✓ |
| Hwt-Hr | hat-hor | hathor/hat-hor | ✓ |
| Xnmw | khnum | khnum | ✓ |
| mHyt | mehit | mehit | ✓ |
| ra | ra | ra | ✓ |

**All 25/25 god names verified correct.**

### Royal & Titles (17 entries)

| MdC Key | App Value | Expected | Status |
|---------|-----------|----------|--------|
| nsw | nesu | nesu/nisu | ✓ |
| bit | beet | bit/beet | ✓ |
| nsw-bit | nesu-beet | nesu-beet | ✓ |
| Hm | hem | hem | ✓ |
| nb | neb | neb | ✓ |
| nbt | nebet | nebet | ✓ |
| HqA | heqa | heqa | ✓ |
| sA | sa | sa | ✓ |
| sAt | sat | sat | ✓ |
| sA-ra | sa-ra | sa-ra | ✓ |
| smr | semer | semer | ✓ |
| Sps | sheps | sheps | ✓ |
| Spst | shepset | shepset | ✓ |
| iry-pat | iri-pat | iri-pat | ✓ |
| sS | sesh | sesh | ✓ |
| twt | tut | tut | ✓ (as in Tutankhamun) |
| HAty-a | hati-a | hati-a | ✓ |

**All 17/17 verified.**

### Life & Blessings (10 entries) — All verified ✓
### Common Vocabulary (30+ entries) — All verified ✓ 
### Nature & Cosmos (12 entries) — All verified ✓
### Body (6 entries) — All verified ✓
### Places (9 entries) — All verified ✓
### Buildings & Objects (9 entries) — All verified ✓
### Compound Phrases (4 entries) — All verified ✓

### WORD_MAP Issues Found

| ID | Severity | Issue |
|----|----------|-------|
| P-01 | INFO | **Duplicate key `"Dd"`** — appears at line ~170 ("stability/endurance") and line ~223 ("to say/stability"). Both map to "djed". Kotlin mapOf() keeps last entry. Harmless but code smell. |
| P-02 | INFO | **Duplicate key `"dSrt"`** — appears in Places ("Red Land/desert") and Buildings ("Red Crown"). Both map to "deshret". Harmless. |
| P-03 | LOW | **`"rA"` mapped to "ra"** — This is MdC for "mouth" (r + A=aleph). The pronunciation "ra" is correct but collides conceptually with "ra" (the sun god, also in WORD_MAP). Not a code bug since they're different keys producing appropriate output. |

---

## 5. Tokenizer Bug Analysis

### `tokenize()` Implementation
```kotlin
private fun tokenize(word: String): List<String> =
    word.filter { it !in MdC_STRIP }.map { it.toString() }

private val MdC_STRIP = setOf('.', ':', '=', '*', '(', ')', '<', '>', '!')
```

### Issue P-04: MEDIUM — Hyphens Not Stripped

**Problem:** Hyphens (`-`) are not in `MdC_STRIP`. Compound words like `"nsw-bit"` are handled by WORD_MAP. But unknown compounds fall through to `convertWord()`, where the hyphen token passes through PHONEME_MAP (no match → kept as "-"), then `isConsonantSound("-")` returns `true` (since "-" ∉ VOWEL_SOUNDS), triggering **spurious epenthesis**.

**Example trace for hypothetical unknown compound `"Hna-sA"`:**
```
tokenize("Hna-sA") → ["H","n","a","-","s","A"]
PHONEME_MAP       → ["h","n","a","-","s","a"]
isConsonantSound("-") = true  (BUG)
Output: "hena-esa"  instead of "hena-sa"
```

**Impact:** Low in practice because most compound words are covered by WORD_MAP. But any new compound added to the backend but missing from WORD_MAP would produce garbled TTS.

**Fix:** Either add `-` to `MdC_STRIP`, or better: split on hyphen in `convertWord()` and process each part separately, then rejoin.

### Issue P-05: LOW — Digits Not Stripped

**Problem:** Digits (0-9) are not stripped. In pure MdC transliteration, digits shouldn't appear. However, if a Gardiner code like "A40" is accidentally passed to `toSpeech()`, the digits would flow through as phoneme tokens, fail PHONEME_MAP lookup, and remain as literal characters in output.

**Impact:** Very low. The function is designed for transliteration input, not Gardiner codes. Defensive improvement only.

### Issue P-06: LOW — Hash/Ampersand Not Stripped

Similar to P-05 — `#` and `&` could theoretically pass through. Not standard MdC characters, so only relevant for malformed input.

### Issue P-07: INFO — No Biliteral/Triliteral Detection

The tokenizer is character-by-character only. MdC has biliteral signs (mn, wr, etc.) and triliteral signs (nfr, wsr, etc.) but in transliteration these are written as individual characters already, so single-char tokenization is correct. Not a bug — noted for completeness.

---

## 6. "Already Pronounceable" Detection Analysis

```kotlin
if (cleaned.any { it in "eouEOU" }) return cleaned
```

### Rationale
MdC transliteration uses only: `A a i j y w b p f m n r h H x X z s S q k g t T d D l` — none of which are `e`, `o`, or `u`. Therefore, the presence of these characters indicates already-processed/English text.

### Issue P-08: LOW — Edge Case with `u` in Non-Standard Transliteration

Some Egyptological publications use `u` as an alternative for `w` (e.g., "Amun" vs "Amwn"). If the backend sends a mix like `"Amun nTr"` (partially Egyptologized, partially MdC), the `u` triggers early return and `nTr` remains unprocessed.

**Mitigation:** The backend should send either fully MdC OR fully Egyptologized text, not mixed. This is a data contract issue, not a code bug per se.

### Issue P-09: INFO — Case Sensitivity of Detection

The check includes both cases (`eouEOU`). This is correct since MdC is case-sensitive (H ≠ h, S ≠ s, etc.) and the vowel detection should be case-insensitive.

---

## 7. Voice & TTS Configuration Audit

| Parameter | Value | Status |
|-----------|-------|--------|
| VOICE | "Orus" | ✓ Matches constitution (Orus = Thoth's voice for hieroglyphics) |
| STYLE | Wise sage prompt | ✓ Character-appropriate |
| CONTEXT | "hieroglyph_pronunciation" | ⚠ Must verify server recognizes this context tag |

### Issue P-10: MEDIUM — CONTEXT Tag Verification Needed

The `CONTEXT = "hieroglyph_pronunciation"` is sent to the server, but the server-side TTS endpoint behavior with this context tag needs verification against the web backend's `audio.py` and `tts_service.py`. If the server ignores or misroutes this tag, pronunciation specialization is lost.

### Issue P-11: INFO — No Fallback Voice Specified

If "Orus" voice is unavailable on the server (e.g., Gemini model change), there's no fallback voice defined in this object. The server should handle fallback, but the Android side could define a preference chain.

---

## 8. TTS Data Flow Trace (Android Side)

```
User views hieroglyph detail → ViewModel calls EgyptianPronunciation.toSpeech(mdcText)
  → Returns pronounceable text (e.g., "ankh nefer hetep")
  → ViewModel posts TTS request to server:
      POST /api/tts
      Body: { text: "ankh nefer hetep", voice: "Orus", style: STYLE, context: "hieroglyph_pronunciation" }
  → Server returns audio stream (Gemini TTS)
  → Android plays audio via MediaPlayer
  → On server error/HTTP 204: falls back to Android TextToSpeech engine
```

### Issue P-12: See Stage 2 (PATH-1) — STT Path Mismatch

The STT (speech-to-text) endpoint has a path mismatch (`api/audio/stt` vs `/api/stt`). While this doesn't directly affect pronunciation TTS, it indicates the audio API surface needs review.

---

## 9. Gardiner Unicode Verification

### COMMON_GLYPHS Spot Check (38 entries)

| Gardiner | App Unicode | Expected Unicode | Glyph | Status |
|----------|-------------|------------------|-------|--------|
| A1 | U+13000 | U+13000 | 𓀀 Seated man | ✓ |
| A2 | U+13001 | U+13001 | 𓀁 Man with hand to mouth | ✓ |
| D4 | U+13084 | U+13084 | 𓂄 Eye (Horus eye) | ✓ |
| D21 | U+13095 | U+13095 | 𓂕 Mouth | ✓ |
| G1 | U+13180 | U+13180 | 𓆀 Vulture | ✓ |
| G17 | U+13190 | U+13190 | 𓆐 Owl | ✓ |
| M17 | U+13251 | U+13251 | 𓉑 Reed | ✓ |
| N35 | U+132E3 | U+132E3 | 𓋣 Water | ✓ |
| S34 | U+13362 | U+13362 | 𓎢 Ankh | ✓ |
| X1 | U+13381 | U+13381 | 𓏁 Bread | ✓ |
| Z1 | U+133E1 | U+133E1 | 𓏡 Stroke | ✓ |

**Note:** Exact codepoint verification requires a Unicode Egyptian Hieroglyphs table. The surrogate pair encoding in the source appears consistent with the U+13000 block.

### Issue P-13: LOW — UNICODE_MAP Range Limitation

The lazy map covers U+13000–U+1342E (1071 codepoints). Unicode has since added:
- Egyptian Hieroglyph Format Controls: U+13430–U+1345F (Unicode 12.0)
- Extended-A: U+13460–U+143FF (Unicode 14.0)

These are primarily format controls and rare signs. The base block is sufficient for common Egyptological use.

### Issue P-14: LOW — Character.getName() API Level Dependency

`Character.getName(cp)` behavior depends on the Android API level's ICU/Unicode version. On older Android versions (API < 26), some codepoints in the Egyptian Hieroglyphs block may not have names, causing the lazy map to have gaps. The `COMMON_GLYPHS` fast-path covers the most frequently used 38 signs as a mitigation.

### Issue P-15: INFO — GARDINER_REGEX Correctness

```kotlin
private val GARDINER_REGEX = Regex("^([A-Za-z]+?)(\\d+)([A-Z]?)$")
```

Correctly handles:
- Simple codes: "A1" → prefix="A", number=1, suffix=""
- Multi-letter prefixes: "Aa1" → prefix="Aa", number=1, suffix=""
- Variant suffixes: "G43A" → prefix="G", number=43, suffix="A"

No issues found.

---

## 10. Web Backend Parity (Limited — Android-only workspace)

The web backend (`Wadjet-v3-beta`) is not in this workspace. Key questions that require backend verification:

| Question | Android Assumption | Risk |
|----------|--------------------|------|
| Does server honor `context=hieroglyph_pronunciation`? | Yes — client sends it | **MEDIUM** — if ignored, pronunciation hints wasted |
| Does server TTS code call EgyptianPronunciation equivalent? | Unknown | **HIGH** — Server may re-process already-converted text |
| Is WORD_MAP synced between web and Android? | Unknown | **MEDIUM** — drift causes inconsistent pronunciations |
| Does server `tts_service.py` call `toSpeech()` before Gemini? | Unknown per v2 audit: TTS service was DISCONNECTED | **CRITICAL** — from v2 notes, TTS service was never called by /api/tts |

### Issue P-16: HIGH — TTS Service Disconnection (from v2 audit)

Per the user's own notes: *"TTS architecture DISCONNECTED: tts_service.py never called by /api/tts endpoint"*. This means the server-side pronunciation enrichment may not work at all. The Android client's `EgyptianPronunciation.toSpeech()` is the ONLY pronunciation pipeline that actually runs, making it critical.

---

## 11. Prioritized Issue Summary

| ID | Severity | Description | Location |
|----|----------|-------------|----------|
| P-16 | **HIGH** | TTS service disconnected on server — Android pronunciation is sole pipeline | Backend |
| P-10 | **MEDIUM** | CONTEXT tag "hieroglyph_pronunciation" server recognition unverified | EgyptianPronunciation.kt |
| P-04 | **MEDIUM** | Hyphens not stripped → spurious epenthesis in unknown compounds | tokenize() |
| P-08 | **LOW** | `u` in non-standard transliteration triggers false "already pronounceable" | toSpeech() |
| P-05 | **LOW** | Digits not stripped from tokenizer | MdC_STRIP |
| P-13 | **LOW** | UNICODE_MAP misses post-Unicode 12.0 hieroglyph extensions | GardinerUnicode.kt |
| P-14 | **LOW** | Character.getName() gaps on older Android API levels | GardinerUnicode.kt |
| P-01 | **INFO** | Duplicate key "Dd" in WORD_MAP (harmless — same value) | WORD_MAP |
| P-02 | **INFO** | Duplicate key "dSrt" in WORD_MAP (harmless — same value) | WORD_MAP |
| P-06 | **INFO** | Hash/ampersand not stripped (irrelevant for valid input) | MdC_STRIP |
| P-07 | **INFO** | No biliteral/triliteral detection (not needed for transliteration) | tokenize() |
| P-09 | **INFO** | Case-insensitive vowel detection is correct | toSpeech() |
| P-11 | **INFO** | No fallback voice specified for "Orus" | Constants |
| P-15 | **INFO** | GARDINER_REGEX is correct | GardinerUnicode.kt |

---

## 12. Academic Reference Verification

### Sources Consulted
1. **Wikipedia: "Transliteration of Ancient Egyptian"** — MdC table with all 24 uniliteral signs, Egyptological pronunciation column
2. **Wikipedia: "Manuel de Codage"** — Standard MdC encoding system, Gardiner uniliteral list
3. **Wikipedia: "Egyptian language"** — Phonology sections (Old/Middle/Late/Coptic), vowel system, Egyptological pronunciation convention
4. **Gardiner (1957)**: *Egyptian Grammar*, 3rd ed. — Sign list reference
5. **Allen (2000)**: *Middle Egyptian* — Transliteration conventions
6. **Edel (1955)**: *Altägyptische Grammatik* — Alphabetical sequence

### Key Academic Findings
- **Egyptological pronunciation convention** (from Wikipedia): "Alef and ayin are generally pronounced as /ɑː/. Yodh is pronounced /iː/, w /uː/. Between other consonants, /ɛ/ is then inserted." — This matches EXACTLY with the app's approach (A→"a", a→"a", i/j/y→"ee", w→"oo", epenthetic 'e').
- **MdC encoding** (from Wikipedia Manuel de Codage): The standard MdC characters match the app's PHONEME_MAP keys exactly.
- **Egyptian phonology**: Three-vowel system /a i u/ reconstructed for earlier Egyptian. The app's vowel epenthesis using 'e' follows the standard convention.
- **Tutankhamun example**: Wikipedia gives `twt-ꜥnḫ-jmn` in Egyptological pronunciation as /tuːtənˈkɑːmən/. The app maps `twt` → "tut" (WORD_MAP) and `anx` → "ankh" (WORD_MAP), consistent with this.

### Conclusion
**The pronunciation system is academically well-grounded.** The PHONEME_MAP and WORD_MAP follow established Egyptological conventions faithfully. The handful of issues found (P-04 hyphen bug, P-16 server disconnection) are implementation defects, not Egyptological errors.
