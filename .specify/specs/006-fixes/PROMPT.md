# Fix Plan - Wadjet Android

> This is the TREATMENT folder. It receives findings from `005-full-testing/` and organizes
> them into prioritized fix phases with copy-paste prompts.
>
> Don't start this until at least one testing phase from 005 has completed.

---

## What You Are

A senior Android engineer implementing fixes for bugs, UX issues, and quality problems found during full testing. For every fix:
1. Understand the root cause (from the phase findings file)
2. Pick the best solution (not a quick hack)
3. Implement it
4. Make sure nothing else breaks
5. Run relevant tests to confirm

---

## About the App

Same as `005-full-testing/PROMPT.md`. Read that if you need context.

**Package**: `com.wadjet.app`
**Backend**: `https://nadercr7-wadjet-v2.hf.space`
**Architecture**: Multi-module (app + 10 core + 10 feature)
**Stack**: Kotlin, Compose, Hilt, Retrofit, Room, Firebase

---

## Emulator and Script Tools

Use these for verifying fixes visually on the actual device:

**Scripts**: `D:\Personal attachements\Repos\23-Android-Kotlin\awesome-android-agent-skills\.github\skills\testing_and_automation\android-emulator-skill\scripts\`
(shorthand: `$SCRIPTS`)

Key commands for fix verification:
```bash
# Build + install after a fix
python $SCRIPTS/build_and_test.py --task installDebug

# Verify the screen looks right
python $SCRIPTS/screen_mapper.py --json
python $SCRIPTS/navigator.py --find-text "element" --tap

# Check for crashes
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 5 --json

# Run unit tests (to check for regressions)
python $SCRIPTS/build_and_test.py --task testDebugUnitTest --json
```

---

## Folder Structure

```
.specify/specs/006-fixes/
  PROMPT.md                    <- This file (master fix prompt)
  REVIEW.md                    <- QA gate for fix quality
  master-fix-plan.md           <- Generated: all issues unified + prioritized
  phase-prompts.md             <- Generated: copy-paste prompts per fix phase
  workflow.md                  <- Generated: fix execution order + verification
  phase-01-unit-bugs.md        <- From 005 Phase 1-2 (unit test failures)
  phase-02-visual-issues.md    <- From 005 Phase 3 (screenshot diffs)
  phase-03-interaction-bugs.md <- From 005 Phase 4 (UI test failures)
  phase-04-e2e-failures.md     <- From 005 Phase 5 (emulator E2E failures)
  phase-05-api-mismatches.md   <- From 005 Phase 6 (API contract mismatches)
  phase-06-ux-improvements.md  <- From 005 investigation (UX walkthrough)
  phase-07-pre-existing.md     <- From 005 Stage 1 (already-failing tests)
```

---

## How Phase Files Get Created

After each testing phase in 005, the executor writes a phase file HERE with this structure:

```markdown
# Phase XX: [Name] - Findings

## Source
[Which 005 testing phase produced these findings]

## Issues Found
| # | ID | Category | Severity | File:Line | Description | Root Cause |
|---|-----|----------|----------|-----------|-------------|------------|

## Detailed Issue Analysis

### Issue FIX-001: [Title]
**File**: `feature/explore/src/.../IdentifyScreen.kt:42`
**Severity**: HIGH
**Category**: UX
**What happens**: [exact current broken behavior]
**Root cause**: [why]
**Best fix**: [optimal solution, not a hack]
**Alternative fixes**: [other options, why they're worse]
**Affected tests**: [which tests catch this]
**Verification**: [how to confirm the fix works]

### Issue FIX-002: [Title]
...

## Fix Priority
| Priority | Issue IDs | Reason |
|----------|-----------|--------|
| P0 (crash/data loss) | FIX-001, FIX-003 | App crashes or loses data |
| P1 (can't complete task) | FIX-002, FIX-005 | User gets blocked |
| P2 (friction) | FIX-004 | User CAN do it but it's confusing |
| P3 (polish) | FIX-006 | Cosmetic |
```

---

## Planning Files (Generated After All Phase Files Exist)

### `master-fix-plan.md` - Unified Fix Plan
```markdown
# Master Fix Plan

## All Issues (Unified from all phases)
| # | ID | Phase | Category | Severity | File | Description | Fix Effort | Status |
|---|-----|-------|----------|----------|------|-------------|------------|--------|

## Fix Execution Order
Group by dependency, not just severity:
1. Infrastructure (if test infra itself has issues)
2. Security (tokens, cleartext, permissions)
3. Crashes (P0 - make the app stop crashing)
4. Data correctness (wrong data shown)
5. UX blockers (user can't finish a task)
6. UX friction (user CAN finish but struggles)
7. Visual (dark mode, RTL, accessibility)
8. Performance (jank, memory, recomposition)

## Dependency Map
[Which fixes need other fixes done first]

## Fix Phases
| Fix Phase | Issues | Scope |
|-----------|--------|-------|
| Fix Phase A | FIX-001 to FIX-010 | Critical + Security |
| Fix Phase B | FIX-011 to FIX-025 | Core UX |
| Fix Phase C | FIX-026 to FIX-040 | Visual + Accessibility |
| Fix Phase D | FIX-041+ | Performance + Polish |
```

### `phase-prompts.md` - Fix Execution Prompts
```markdown
# Fix Phase Prompts

## Fix Phase A Prompt: Critical + Security
Read these files for context:
- .specify/specs/006-fixes/master-fix-plan.md
- .specify/specs/006-fixes/phase-XX-*.md (relevant phases)

Implement fixes for: [FIX-001, FIX-003, ...]

For each fix:
1. Read the issue details from the phase file
2. Read the affected source file
3. Implement the best fix described
4. Run related tests: python $SCRIPTS/build_and_test.py --task :module:testDebug --json
5. If tests fail because the fix changed expected behavior, update the test too
6. After ALL fixes, run full suite: python $SCRIPTS/build_and_test.py --task testDebugUnitTest --json
7. Verify visually on emulator:
   python $SCRIPTS/build_and_test.py --task installDebug
   python $SCRIPTS/app_launcher.py --launch com.wadjet.app
   python $SCRIPTS/screen_mapper.py --json

After finishing, write a summary to:
`.specify/specs/006-fixes/fix-results-phase-A.md`

# Fix Phase A Results

## Fixes Applied
| # | ID | File Changed | Lines Changed | Test Result |

## New Issues (regressions)
[Any new problems from the fixes]

## Still Open
[Issues that couldn't be fixed, with reason]

---

## Fix Phase B Prompt: Core UX
[Same structure]

## Fix Phase C Prompt: Visual + Accessibility
[Same structure]

## Fix Phase D Prompt: Performance + Polish
[Same structure]
```

### `workflow.md` - Fix Workflow
```markdown
# Fix Workflow

## Before You Start
- [ ] At least some 005 testing phases are done
- [ ] Phase files written in 006-fixes/
- [ ] master-fix-plan.md generated
- [ ] phase-prompts.md generated
- [ ] Git branch created: `fix/005-findings`

## Execution Order
1. Create fix branch from develop
2. Run Fix Phase A (critical + security)
3. Run full test suite - check for regressions
   python $SCRIPTS/build_and_test.py --task testDebugUnitTest --json
4. Commit: "fix: phase A - critical + security fixes"
5. Run Fix Phase B (core UX)
6. Run full test suite
7. Commit: "fix: phase B - core UX fixes"
8. Run Fix Phase C (visual + accessibility)
9. Run screenshot tests - update golden baselines
10. Commit: "fix: phase C - visual + accessibility"
11. Run Fix Phase D (performance + polish)
12. Verify on emulator:
    python $SCRIPTS/emulator_manage.py --boot Pixel_8
    python $SCRIPTS/build_and_test.py --task installDebug
    python $SCRIPTS/app_launcher.py --launch com.wadjet.app
    [walk through all screens with navigator.py]
13. Commit: "fix: phase D - performance + polish"
14. Final full test suite - ALL GREEN
15. PR to develop

## After Each Fix Phase
1. Run tests
2. Write fix-results-phase-X.md
3. Update master-fix-plan.md status column
4. If new issues appear, add them to master plan

## Final Verification
After ALL fix phases:
- [ ] python $SCRIPTS/build_and_test.py --task testDebugUnitTest --json -> 0 failures
- [ ] Roborazzi screenshots match golden baselines
- [ ] E2E emulator journeys pass (all 10+)
- [ ] API contract tests pass
- [ ] python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 30 --json -> clean
- [ ] Dark mode full pass (adb shell cmd uimode night yes)
- [ ] RTL Arabic full pass
- [ ] TalkBack basic pass
- [ ] Large font 1.5x full pass (adb shell settings put system font_scale 1.5)
```

---

## Known Pre-Existing UX Issues (seeds for phase-06)

### Issue FIX-UX-001: Landmark Upload Button Hidden
**Screen**: IdentifyScreen (Route.Identify)
**Severity**: HIGH
**Description**: The upload button in landmarks/identify is placed somewhere unclear, looks unimportant, small and easy to miss.
**Root cause**: To be determined during investigation (exact Icon/size/placement)
**Best fix**: To be determined (likely: larger button, prominent placement, clear label)

### Issue FIX-UX-002: Same Upload Icon for Different Features
**Screen**: ScanScreen + IdentifyScreen
**Severity**: HIGH
**Description**: Uploading for hieroglyph scanning and landmark identification use the same icon/indicator. Users can't tell which one they're doing.
**Root cause**: To be determined (probably same Icon composable reused)
**Best fix**: Different icons + labels:
- Hieroglyph scan: camera/scan icon + "Scan Hieroglyph" label
- Landmark identify: map-pin/image icon + "Identify Landmark" label

---

# RULES

1. Every fix must reference the original issue ID (FIX-001, etc.)
2. Every fix must have a test that proves it works. If no test exists, write one.
3. No quick hacks. Find the root cause, fix it properly.
4. Don't break other things. Run full test suite after each phase.
5. Match existing code style, patterns, naming, architecture.
6. UX fixes need visual verification on the emulator using screen_mapper.py and navigator.py.
7. Update status in master-fix-plan.md after each fix.
8. If a fix is too risky, flag it. Don't ship it without discussing.
