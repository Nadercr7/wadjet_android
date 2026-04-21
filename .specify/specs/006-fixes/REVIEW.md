# QA Gate - 006-fixes Review

> Run this AFTER all fix phases are complete.
> Verifies every fix was properly implemented, tested, and documented.

---

## Step 1: Verify All Phase Files Exist

```
.specify/specs/006-fixes/phase-01-unit-bugs.md
.specify/specs/006-fixes/phase-02-visual-issues.md
.specify/specs/006-fixes/phase-03-interaction-bugs.md
.specify/specs/006-fixes/phase-04-e2e-failures.md
.specify/specs/006-fixes/phase-05-api-mismatches.md
.specify/specs/006-fixes/phase-06-ux-improvements.md
.specify/specs/006-fixes/phase-07-pre-existing.md
.specify/specs/006-fixes/master-fix-plan.md
.specify/specs/006-fixes/phase-prompts.md
.specify/specs/006-fixes/workflow.md
```

---

## Step 2: Verify Fix Results

For each `fix-results-phase-X.md`:
- [ ] Every issue from master-fix-plan.md is accounted for (fixed, deferred, or explained)
- [ ] Every fix has a file:line reference showing what changed
- [ ] Every fix has a test result (pass/fail)
- [ ] No regressions introduced

---

## Step 3: Run Full Test Suite

```bash
cd "D:\Personal attachements\Projects\Wadjet-Android"
python $SCRIPTS/build_and_test.py --task testDebugUnitTest --json
python $SCRIPTS/build_and_test.py --task connectedDebugAndroidTest --json  # if emulator running
```

All tests must pass.

---

## Step 4: Emulator Verification

Boot emulator and walk through all 18 screens:
```bash
python $SCRIPTS/emulator_manage.py --boot Pixel_8
python $SCRIPTS/build_and_test.py --task installDebug
python $SCRIPTS/app_launcher.py --launch com.wadjet.app
```

For each screen:
```bash
python $SCRIPTS/screen_mapper.py --json
python $SCRIPTS/log_monitor.py --package com.wadjet.app --priority E --duration 3 --json
```

Check:
- [ ] No crashes on any screen
- [ ] UX issues from phase-06 are visibly fixed
- [ ] Upload buttons are distinct (hieroglyph scan vs landmark identify)
- [ ] Dark mode works (adb shell cmd uimode night yes, then walk through)
- [ ] Logcat clean (no errors/warnings)

---

## Step 5: Output

Write to `.specify/specs/006-fixes/REVIEW-RESULTS.md`:

```markdown
# 006-fixes - QA Review Results

## Phase Files: [X/7 complete]
## Fix Results: [X fix phases executed]
## Test Suite: PASS / FAIL
## Emulator Walkthrough: PASS / FAIL
## Regressions Found: [list]
## Verdict: PASS / FAIL / PASS WITH NOTES
```
