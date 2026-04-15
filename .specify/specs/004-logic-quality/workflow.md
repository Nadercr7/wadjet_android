# Git Workflow: Logic & Quality

## Branch Strategy
- Feature branch: `004-logic-quality`
- Base: `main`
- Create: `git checkout -b 004-logic-quality`

## Commit Convention
`Phase X: Logic quality — [description]`

Examples:
- `Phase 1: Logic quality — Egyptological accuracy fixes`
- `Phase 2: Logic quality — API contract alignment`
- `Phase 3: Logic quality — ViewModel safety & lifecycle fixes`

## Phase Workflow

For each phase:

1. **Read investigation stage files** relevant to the phase (listed in `phase-prompts.md`)
2. **Read spec + tasks** for the phase (`spec.md`, `tasks.md`)
3. **Implement changes** — follow task list in order
4. **Build**: `./gradlew assembleDebug` → zero errors
5. **Test**: `./gradlew testDebugUnitTest` → all pass
6. **Pronunciation verify** (if phase affects TTS): Listen to words, compare with web app
7. **Output verify** (if phase affects scan/landmark): Compare results with web app
8. **Commit + push**

## Phase Execution Order

| Phase | Name | Priority | Can Parallel? |
|-------|------|----------|---------------|
| 1 | Egyptological Accuracy | P0 | Independent |
| 2 | API Contract Alignment | P0 | Parallel with Phase 1 |
| 3 | ViewModel Safety & Lifecycle | P0 | After Phase 2 |
| 4 | Auth & Security | P1 | After Phase 3 |
| 5 | Offline & Data Layer | P1 | After Phase 2 |
| 6 | Feature Completeness | P2 | After Phase 3 |
| 7 | Testing | P2 | After all other phases |

## Verification Before Each Push

- [ ] `./gradlew assembleDebug` clean (zero errors, zero warnings in changed files)
- [ ] `./gradlew testDebugUnitTest` all pass
- [ ] No regressions in other features (smoke test main flows)
- [ ] If TTS changes: manual listen test on key words (anx, nfr, mAat, xpr, wADt)
- [ ] If scan changes: compare output with web app for same image
- [ ] If auth changes: login/logout/refresh/Google Sign-In test
- [ ] If DTO changes: verify JSON parsing against actual server response
- [ ] If Room changes: verify migration or destructive fallback documented

## Verification Before Final Merge

- [ ] ALL phases (1–7) complete
- [ ] ALL tests pass (existing + new from Phase 7)
- [ ] **TTS**: Every word in `pronunciation-audit.md` sounds correct
- [ ] **Scan**: Results match web app quality — glyphs, confidence, transliteration all present
- [ ] **Landmarks**: All images load, identification correct, details complete
- [ ] **Chat**: SSE streaming works, history persists, session ID correct after load
- [ ] **Stories**: All 4 interaction types work, TTS narration plays, progress saves
- [ ] **Auth**: Login/logout/refresh/Google Sign-In all work; sign-out navigates to Welcome
- [ ] **Offline**: Dictionary search works offline, scan history accessible, landmarks cached
- [ ] **Dictionary**: All 4 dropped API fields now displayed, FTS ranking works
- [ ] No hardcoded secrets in source (grep for API keys, passwords)
- [ ] ProGuard rules don't break kotlinx.serialization or Retrofit annotations
- [ ] `User-Agent` header uses `BuildConfig.VERSION_NAME`
- [ ] CI pipeline runs instrumentation tests (if added)

## Rollback Strategy

If a phase introduces regressions:
1. `git stash` current work
2. `git log --oneline` to find last good commit
3. `git revert <commit>` for the problematic phase
4. Fix the issue in isolation
5. Re-apply with `git stash pop`

## File Organization

```
.specify/specs/004-logic-quality/
├── _investigation/           # 12 stage files (permanent evidence, DO NOT EDIT)
│   ├── stage-01-existing-context.md
│   ├── stage-02-network-layer.md
│   ├── ...
│   └── stage-12-testing-references.md
├── gap-analysis.md           # Master issue list (LQ-001 through LQ-XXX)
├── spec.md                   # User stories & requirements
├── plan.md                   # Phased implementation plan
├── tasks.md                  # Task list traced to findings
├── phase-prompts.md          # Copy-paste prompts per phase
├── pronunciation-audit.md    # Word-by-word academic verification
├── api-parity.md             # 48-endpoint comparison
├── testing.md                # Test plan
└── workflow.md               # This file
```
