# Testing Workflow

## Order of Operations

| Step | Action | Output | Done? |
|------|--------|--------|-------|
| 1 | Run existing 320 tests — verify baseline | All pass ✅ | ✅ Stage 1 |
| 2 | 18-stage investigation — full understanding | 18 stage files in `_investigation/` | ✅ Stages 1-18 |
| 3 | Write 9 planning files | This file + 8 others | ✅ |
| 4 | **Phase 1**: Test infra + P0 unit tests (Chat, Scan, TokenManager) | ~45 new tests + `core/testing` module | ☐ |
| 5 | Write Phase 1 findings → `006-fixes/phase-01-unit-bugs.md` | Bugs found by new tests | ☐ |
| 6 | **Phase 2**: P1 unit tests (Stories, Dashboard, Settings, Landing, etc.) | ~80 new tests | ☐ |
| 7 | Write Phase 2 findings → `006-fixes/phase-02-unit-bugs.md` | More bugs found | ☐ |
| 8 | **Phase 3**: Roborazzi screenshots — all screens × dark × states | ~100 golden images | ☐ |
| 9 | Write Phase 3 findings → `006-fixes/phase-03-visual-issues.md` | Visual regressions/issues | ☐ |
| 10 | **Phase 4**: Compose UI tests — interactive flows | ~20 instrumented tests | ☐ |
| 11 | Write Phase 4 findings → `006-fixes/phase-04-interaction-bugs.md` | Interaction bugs | ☐ |
| 12 | **Phase 5**: E2E emulator tests — 10 user journeys | 10 test scripts run | ☐ |
| 13 | Write Phase 5 findings → `006-fixes/phase-05-e2e-failures.md` | Flow-level failures | ☐ |
| 14 | **Phase 6**: API contract tests — Python httpx | All 43 endpoints tested | ☐ |
| 15 | Write Phase 6 findings → `006-fixes/phase-06-api-mismatches.md` | Backend mismatches | ☐ |
| 16 | **Phase 7**: CI/CD integration | GitHub Actions workflow | ☐ |
| 17 | Switch to `006-fixes/` — execute fix phases in priority order | Fixes applied | ☐ |
| 18 | Re-run ALL tests — everything green | Full green suite | ☐ |

## Handoff Protocol (005 → 006)

After **EACH** testing phase:

1. Run the tests
2. Collect all failures and issues
3. Write a dedicated file in `.specify/specs/006-fixes/` for that phase:
   - `phase-01-unit-bugs.md`
   - `phase-02-unit-bugs.md`
   - `phase-03-visual-issues.md`
   - `phase-04-interaction-bugs.md`
   - `phase-05-e2e-failures.md`
   - `phase-06-api-mismatches.md`
   - `phase-07-ci-issues.md`
4. Each file contains: Problem, Root Cause, Best Solution, Fix Prompt
5. User picks when to switch from testing (005) to fixing (006)

### File Format for 006-fixes/

```markdown
# Phase N: [Category] — Test Findings

## Issue 1: [Title]
- **Test that found it**: `ModuleTest.kt` → `test case name`
- **Root cause**: file:line — description
- **Impact**: What breaks
- **Fix**: Exact code change
- **Priority**: P0/P1/P2

## Issue 2: ...
```

## Done Checklist

- [x] All 18 investigation stages complete
- [x] All 9 planning files in 005-full-testing/ written
- [ ] Phase 1: Test infra + P0 units — run and findings written
- [ ] Phase 2: P1 units — run and findings written
- [ ] Phase 3: Screenshots — baselines captured and findings written
- [ ] Phase 4: Compose UI tests — run and findings written
- [ ] Phase 5: E2E emulator tests — run and findings written
- [ ] Phase 6: API contract tests — run and findings written
- [ ] Phase 7: CI/CD — workflow file committed
- [ ] All 006-fixes/ phase files generated
- [ ] 006-fixes/ master plan + phase prompts generated
- [ ] All fixes applied
- [ ] All tests re-run — green
- [ ] CI/CD runs tests on every PR

## Key Rules

1. **This folder (005) is tests only.** No code fixes here.
2. **006-fixes/ is for fixes.** All problems found go there.
3. **Don't skip phases.** Unit tests before screenshots before E2E.
4. **Run existing tests first** after any infrastructure change to catch regressions.
5. **Match existing patterns.** JUnit 4, MockK, Turbine, backtick names.
