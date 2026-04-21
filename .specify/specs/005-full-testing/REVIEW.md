# QA Gate - 005-full-testing Review

> Run this prompt AFTER all 18 investigation stages are complete and all 9 planning files exist.
> This is a quality gate. It checks that everything is complete, accurate, and actionable.
> You don't write tests here. You verify the plan.

---

## Step 1: Verify All 18 Investigation Files Exist

Check that every stage file exists and has real content (not empty/placeholder):

```
.specify/specs/005-full-testing/_investigation/stage-01-existing-tests.md
.specify/specs/005-full-testing/_investigation/stage-02-ux-walkthrough.md
.specify/specs/005-full-testing/_investigation/stage-03-viewmodels.md
.specify/specs/005-full-testing/_investigation/stage-04-network.md
.specify/specs/005-full-testing/_investigation/stage-05-database-offline.md
.specify/specs/005-full-testing/_investigation/stage-06-compose-screens.md
.specify/specs/005-full-testing/_investigation/stage-07-designsystem.md
.specify/specs/005-full-testing/_investigation/stage-08-navigation.md
.specify/specs/005-full-testing/_investigation/stage-09-repositories.md
.specify/specs/005-full-testing/_investigation/stage-10-firebase-auth.md
.specify/specs/005-full-testing/_investigation/stage-11-best-practices.md
.specify/specs/005-full-testing/_investigation/stage-12-backend-api.md
.specify/specs/005-full-testing/_investigation/stage-13-audio-pronunciation.md
.specify/specs/005-full-testing/_investigation/stage-14-security-edge.md
.specify/specs/005-full-testing/_investigation/stage-15-performance.md
.specify/specs/005-full-testing/_investigation/stage-16-accessibility.md
.specify/specs/005-full-testing/_investigation/stage-17-darkmode-rtl.md
.specify/specs/005-full-testing/_investigation/stage-18-gap-analysis.md
```

For each file: exists? More than 50 lines? Has real markdown tables and real findings?

---

## Step 2: Verify All 9 Planning Files Exist

```
.specify/specs/005-full-testing/test-plan.md
.specify/specs/005-full-testing/ux-issues.md
.specify/specs/005-full-testing/unit-tests.md
.specify/specs/005-full-testing/screenshot-tests.md
.specify/specs/005-full-testing/e2e-tests.md
.specify/specs/005-full-testing/api-contract-tests.md
.specify/specs/005-full-testing/phase-prompts.md
.specify/specs/005-full-testing/workflow.md
.specify/specs/005-full-testing/testing.md
```

Also verify 006-fixes folder exists:
```
.specify/specs/006-fixes/PROMPT.md
.specify/specs/006-fixes/REVIEW.md
```

---

## Step 3: Cross-Reference Coverage

### 3a: Every screen tested
For all 18 screens + sub-screens (LoginSheet, RegisterSheet, 4 Dictionary tabs, etc.):
- [ ] Mentioned in `ux-issues.md` (even if "no issues found")
- [ ] Has unit tests in `unit-tests.md` (for the ViewModel)
- [ ] Has screenshot entry in `screenshot-tests.md`
- [ ] Shows up in at least one E2E journey in `e2e-tests.md`

### 3b: Every ViewModel has tests
All 20 ViewModels must appear in `unit-tests.md` with at least:
- Success path test
- Error path test
- Loading state test
- Edge case test

### 3c: Every API endpoint covered
All 37+ endpoints must appear in:
- `api-contract-tests.md` (schema check)
- At least one test in `unit-tests.md` (repository test with MockWebServer)

### 3d: Every module accounted for
All 20 Gradle modules show up somewhere in the testing plan.

### 3e: Emulator scripts actually used
Verify that the investigation used ALL 9 Python scripts + emu_health_check:
- [ ] screen_mapper.py (used in Stage 2, 6, 8, 15, 16, 17)
- [ ] navigator.py (used in Stage 2, 8, 10, 13, 15, 16, 17)
- [ ] gesture.py (used in Stage 2, 5, 8, 15)
- [ ] keyboard.py (used in Stage 2, 8, 14)
- [ ] app_launcher.py (used in Stage 2, 5, 10, 14)
- [ ] emulator_manage.py (used in Stage 2)
- [ ] build_and_test.py (used in Stage 1, 2)
- [ ] log_monitor.py (used in Stage 2, 4, 5, 10, 13, 14, 15)
- [ ] emu_health_check.ps1 (used in Stage 1)
If any script is missing, that's a red flag.

---

## Step 4: Verify UX Issues Are Actionable

For every issue in `ux-issues.md`:
- [ ] Has exact file:line reference
- [ ] Has concrete fix description (specific, not vague like "make it better")
- [ ] Has severity rating
- [ ] Will flow into 006-fixes/ through the handoff protocol

---

## Step 5: Verify Phase Prompts Work Standalone

Read each prompt in `phase-prompts.md`:
- [ ] Can be copy-pasted and run without extra context
- [ ] References exact file paths
- [ ] Has clear inputs and expected outputs
- [ ] Doesn't repeat work from other phases

---

## Step 6: Red Flags

Flag any of these if you find them:
1. Any stage file under 30 lines (too thin, probably incomplete)
2. Any "TODO" or "TBD" still sitting in the planning files
3. Test cases without concrete assertions
4. UX issues without file:line references
5. Screens missing from the test matrix
6. ViewModels missing from unit-tests.md
7. References to files that don't exist (check with `Test-Path`)
8. Phase prompts that depend on output from earlier phases (should be self-contained)
9. Duplicate issues between ux-issues.md and 006-fixes/ (should cross-reference, not copy)
10. Made-up test framework features (double-check that APIs actually exist)
11. Missing stages (must be exactly 18)
12. Performance, accessibility, RTL, or dark mode not covered at all

---

## Step 7: Output

Write your review to `.specify/specs/005-full-testing/REVIEW-RESULTS.md`:

```markdown
# 005-full-testing - QA Review Results

## Investigation Files: [X/18 complete]
| Stage | File | Lines | Status | Issues |

## Planning Files: [X/9 complete]
| File | Lines | Status | Issues |

## Coverage Gaps
[Anything missing]

## Red Flags Found
[Numbered list]

## Emulator Script Usage
[Which scripts were used, which were missed]

## Verdict: PASS / FAIL / PASS WITH NOTES
[Overall assessment]

## Required Fixes Before Proceeding
[Numbered list of what needs fixing, if any]
```
