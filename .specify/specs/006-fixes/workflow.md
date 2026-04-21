# Fix Workflow

## Before You Start
- [x] At least some 005 testing phases are done
- [x] Phase files written in 006-fixes/
- [x] master-fix-plan.md generated
- [x] phase-prompts.md generated
- [x] workflow.md generated
- [ ] Git branch created: `fix/005-findings`

## Execution Order
1. Create fix branch from develop
2. Run Fix Phase A (critical + security)
3. Run full test suite
4. Commit: "fix: phase A - critical + security fixes"
5. Run Fix Phase B (core UX)
6. Run full test suite
7. Commit: "fix: phase B - core UX fixes"
8. Run Fix Phase C (visual + accessibility)
9. Commit: "fix: phase C - visual + accessibility"
10. Run Fix Phase D (performance + polish)
11. Final full test suite - ALL GREEN
12. Commit: "fix: phase D - performance + polish"

## After Each Fix Phase
1. Run tests
2. Write fix-results-phase-X.md
3. Update master-fix-plan.md status column
4. If new issues appear, add them to master plan
