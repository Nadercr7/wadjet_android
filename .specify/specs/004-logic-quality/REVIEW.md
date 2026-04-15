# Pre-Implementation Review — Logic & Quality Audit

> **USE THIS PROMPT** after the 12 investigation stage files + 9 planning files have been generated,
> but **BEFORE starting Phase 1 implementation**. This is your final gate.

---

## Instructions

You are a **QA reviewer**. You did NOT write the investigation or planning files — you are reviewing someone else's work. Be skeptical. Assume things were missed. Prove everything is correct or flag it.

Read ALL 21 files below, then run every check in this document. Write your findings to:
`.specify/specs/004-logic-quality/REVIEW-RESULTS.md`

Do NOT start any code changes. This is review only.

---

## Step 1: Read everything

### Investigation files (12):
```
.specify/specs/004-logic-quality/_investigation/stage-01-existing-context.md
.specify/specs/004-logic-quality/_investigation/stage-02-network-layer.md
.specify/specs/004-logic-quality/_investigation/stage-03-data-layer.md
.specify/specs/004-logic-quality/_investigation/stage-04-database-offline.md
.specify/specs/004-logic-quality/_investigation/stage-05-viewmodels.md
.specify/specs/004-logic-quality/_investigation/stage-06-pronunciation.md
.specify/specs/004-logic-quality/_investigation/stage-07-scan-landmarks.md
.specify/specs/004-logic-quality/_investigation/stage-08-chat-stories.md
.specify/specs/004-logic-quality/_investigation/stage-09-auth-security.md
.specify/specs/004-logic-quality/_investigation/stage-10-dictionary-write.md
.specify/specs/004-logic-quality/_investigation/stage-11-web-parity.md
.specify/specs/004-logic-quality/_investigation/stage-12-references-testing.md
```

### Planning files (9):
```
.specify/specs/004-logic-quality/gap-analysis.md
.specify/specs/004-logic-quality/spec.md
.specify/specs/004-logic-quality/plan.md
.specify/specs/004-logic-quality/tasks.md
.specify/specs/004-logic-quality/phase-prompts.md
.specify/specs/004-logic-quality/pronunciation-audit.md
.specify/specs/004-logic-quality/api-parity.md
.specify/specs/004-logic-quality/testing.md
.specify/specs/004-logic-quality/workflow.md
```

### Source code (spot-check — read these to verify claims):
```
core/common/src/main/java/com/wadjet/core/common/EgyptianPronunciation.kt
feature/scan/src/main/java/**/util/GardinerUnicode.kt
core/network/src/main/java/**/AuthInterceptor.kt
core/network/src/main/java/**/TokenManager.kt
```

---

## Step 2: File existence & completeness check

### 2a. Do all 21 files exist?
```bash
$base = ".specify/specs/004-logic-quality"
$stages = 1..12 | ForEach-Object { "$base/_investigation/stage-$('{0:D2}' -f $_)-*.md" }
$plans = @("gap-analysis","spec","plan","tasks","phase-prompts","pronunciation-audit","api-parity","testing","workflow") | ForEach-Object { "$base/$_.md" }
($stages + $plans) | ForEach-Object { $found = Get-ChildItem $_ -ErrorAction SilentlyContinue; if ($found) { "✅ $($found.Name)" } else { "❌ MISSING: $_" } }
```
**Every file must exist. If any is missing, STOP and flag it.**

### 2b. Are any files suspiciously short?
```bash
Get-ChildItem ".specify/specs/004-logic-quality" -Recurse -Filter "*.md" |
  Where-Object { $_.Name -ne "PROMPT.md" -and $_.Name -ne "REVIEW.md" } |
  Sort-Object Length |
  Select-Object Name, @{N='Lines';E={(Get-Content $_.FullName).Count}} |
  Format-Table
```
**Any file under 30 lines is suspicious. Any file under 10 lines was likely hallucinated or skipped.**

---

## Step 3: Cross-reference integrity

### 3a. Every gap-analysis finding must trace to a stage file
For each finding `LQ-XXX` in `gap-analysis.md`:
- [ ] Does it cite which stage it came from? (e.g., "from stage-06")
- [ ] Go to that stage file — does the evidence actually exist there?
- [ ] Is the file:line reference real? (Spot-check 10 references by reading the actual source file)

### 3b. Every task must trace to a gap-analysis finding
For each task `T-XXX` in `tasks.md`:
- [ ] Does it reference a finding ID (e.g., `LQ-XXX`)?
- [ ] Does that finding actually exist in `gap-analysis.md`?
- [ ] Is the file path in the task real? (Spot-check 10 paths with `Test-Path`)

### 3c. Every phase-prompt must reference the right tasks
For each phase prompt in `phase-prompts.md`:
- [ ] Does it list task IDs that exist in `tasks.md`?
- [ ] Are the source files listed real?
- [ ] Does the verification section include build + test commands?

### 3d. Spec user stories must cover all critical findings
- [ ] Does every 🔴 Critical finding in gap-analysis have a corresponding user story in `spec.md`?
- [ ] Does every P0 task in `tasks.md` have a user story?

---

## Step 4: Pronunciation audit verification (MOST CRITICAL)

### 4a. Word count check
Count the words in `pronunciation-audit.md`'s "Known Word List" table.
```bash
(Select-String -Path ".specify/specs/004-logic-quality/pronunciation-audit.md" -Pattern "^\| \d" | Measure-Object).Count
```
**The actual WORD_MAP in `EgyptianPronunciation.kt` has ~100 entries. The audit must cover ALL of them — not 70, not 80, ALL.**

### 4b. Spot-check 10 pronunciations against actual source code
Read `EgyptianPronunciation.kt` and pick 10 random WORD_MAP entries. For each:
1. Does the audit table show the correct "App TTS Output" (matching the actual code)?
2. Does the "Scholarly Pronunciation" column cite a real source?
3. Is the Status (✅/❌) consistent with the comparison?

**If ANY of the 10 are wrong, the entire audit is unreliable — flag for redo.**

### 4c. Phoneme mapping completeness
Read the PHONEME_MAP in `EgyptianPronunciation.kt`. Count the entries.
Compare against the phoneme table in `pronunciation-audit.md`.
- [ ] Same number of entries?
- [ ] Same MdC characters?
- [ ] No extra or missing rows?

### 4d. Online source verification
In `stage-12-references-testing.md`:
- [ ] Are Wikipedia URLs cited with specific content (not just "Wikipedia says it's correct")?
- [ ] Are there at least 5 different source URLs cited?
- [ ] Is there any pronunciation marked ✅ WITHOUT a source citation? (That's a red flag)

### 4e. Tokenizer gap coverage
In `stage-06-pronunciation.md`:
- [ ] Is there a section about the tokenizer not stripping digits 0-9?
- [ ] Is there a section about hyphens not being in MdC_STRIP?
- [ ] Is there a fallback stress-test table with at least 20 words?
- [ ] Is there a duplicate WORD_MAP key check?
- [ ] Is there a TTS end-to-end data flow trace (8+ steps)?
- [ ] Is there a DISCONNECT check (tts_service.py not called)?

### 4f. Missing words audit
In `pronunciation-audit.md`:
- [ ] Is there a "MISSING Words That Must Be Added" section?
- [ ] Does it list at least 20 important words not currently in the WORD_MAP?
- [ ] Does it cover: pharaoh names, numbers, common verbs, body parts, monument names?

---

## Step 5: API parity verification

### 5a. Endpoint count
Count endpoints in `api-parity.md`'s coverage table.
The API contract has 48 endpoints. The table should have 48 rows.
```bash
(Select-String -Path ".specify/specs/004-logic-quality/api-parity.md" -Pattern "^\| \d" | Measure-Object).Count
```

### 5b. DTO field-by-field sections
- [ ] Does `api-parity.md` have a field-by-field comparison for ALL 10 DTO files?
- [ ] Not just "they match" — actual field names listed?

### 5c. Cross-check with stage-02
- [ ] Does `stage-02-network-layer.md` have an endpoint coverage matrix?
- [ ] Does `api-parity.md` agree with stage-02's findings?

---

## Step 6: Task & phase sanity

### 6a. Task count vs finding count
- Count 🔴 Critical findings in gap-analysis → each should have at least 1 task
- Count 🟠 Major findings → each should have at least 1 task
- Total tasks should be >= total critical + major findings

### 6b. Phase ordering
- [ ] Is Phase 1 about pronunciation/Egyptological accuracy? (Highest priority)
- [ ] Are phases ordered by priority (P0 → P1 → P2)?
- [ ] Does each phase have a clear goal, file list, and verification?

### 6c. Phase prompt self-containment
Pick Phase 1's prompt from `phase-prompts.md`. Read it as if you know NOTHING else:
- [ ] Can you understand what to do from the prompt alone?
- [ ] Are all file paths included?
- [ ] Are investigation stage files referenced for evidence?
- [ ] Is there a build command?
- [ ] Is there a test command?
- [ ] Is there a manual verification step (e.g., "listen to TTS")?
- [ ] Is there a git commit command?

### 6d. No implementation leakage
- [ ] Do the planning files contain ONLY planning — no actual code changes?
- [ ] Are code changes described (what to change) but NOT executed?

---

## Step 7: Consistency checks

### 7a. Severity consistency
- [ ] Does `gap-analysis.md` use the same severity labels (🔴🟠🟡🔵) as defined in `PROMPT.md`?
- [ ] Are the severity counts in the Statistics table correct? (Manually count and compare)

### 7b. ID uniqueness
- [ ] Are all `LQ-XXX` IDs in gap-analysis unique?
- [ ] Are all `T-XXX` IDs in tasks unique?
- [ ] Are all `US-X` or user story numbers in spec unique?

### 7c. Git history acknowledgment
- [ ] Does `stage-01-existing-context.md` list ALL commits from `git log`?
- [ ] Does `gap-analysis.md` have an "Already Fixed vs. Still Open" section?
- [ ] Are findings from 001-logic-parity (B1-B5, G1-G10) cross-referenced with fix status?

### 7d. Web backend comparison
- [ ] Does `stage-11-web-parity.md` cover ALL features (TTS, scan, landmarks, chat, stories, dictionary, auth)?
- [ ] Does it cite actual Python function names from the backend?
- [ ] Is it comparing logic behavior, not just endpoint existence?

---

## Step 8: Red flag detection

Flag these as **REVIEW FAILURES** — any one means the planning is unreliable:

| # | Red Flag | How to detect | Found? |
|---|----------|--------------|--------|
| 1 | Generic findings without file:line references | Grep for "various files" or "multiple places" without specifics | |
| 2 | Pronunciation marked ✅ without source URL | Scan pronunciation-audit.md | |
| 3 | "No issues found" in any stage file | If a stage found zero issues, it wasn't thorough enough | |
| 4 | Tasks without finding IDs | Every task must trace to evidence | |
| 5 | Missing fallback stress-test | stage-06 must have 20+ words tested through convertWord() | |
| 6 | Stage files under 50 lines | Indicates skipped investigation | |
| 7 | TTS data flow trace missing | The DISCONNECT bug is critical — must be checked | |
| 8 | Gardiner Unicode not spot-checked | At least 50 signs must be verified | |
| 9 | Phase prompts without verification steps | Every phase must have build+test+manual check | |
| 10 | API parity without field-level comparison | "DTOs match" is not enough — fields must be listed | |

---

## Output: REVIEW-RESULTS.md

Write your findings to `.specify/specs/004-logic-quality/REVIEW-RESULTS.md`:

```markdown
# Pre-Implementation Review Results
**Date**: [date]
**Reviewer**: AI QA Agent

## Summary
- Files found: X/21
- Review checks passed: X/Y
- Red flags found: X/10
- **VERDICT**: ✅ READY FOR PHASE 1 / ❌ NEEDS REWORK

## File Existence
[✅/❌ for each of 21 files, with line counts]

## Cross-Reference Integrity
### Finding → Stage traceability
[10 spot-checks with results]
### Task → Finding traceability
[10 spot-checks with results]
### Phase-prompt → Task traceability
[Check results]

## Pronunciation Audit Verification
### Word count: X/~100 WORD_MAP entries covered
### 10-word spot check:
| # | MdC | Code says | Audit says | Match? |
...
### Phoneme count: X in code vs X in audit
### Online sources cited: X unique URLs
### Tokenizer gaps covered: [checklist]
### Missing words section: [X words listed]

## API Parity Verification
### Endpoint count: X/48
### DTO field-level sections: X/10

## Task & Phase Sanity
### Task count vs finding count: [numbers]
### Phase ordering: [correct/wrong]
### Phase 1 self-containment: [pass/fail]

## Red Flags
| # | Red Flag | Status | Evidence |
|---|----------|--------|----------|
| 1 | Generic findings | ✅/❌ | [details] |
...

## Issues Found
### Must Fix Before Phase 1:
1. [issue — what's wrong and where]
...

### Recommendations:
1. [suggestion]
...
```

**If VERDICT is ❌ NEEDS REWORK:**
List exactly which files need to be regenerated and what's missing.
Do NOT proceed to Phase 1 until all issues are resolved.
