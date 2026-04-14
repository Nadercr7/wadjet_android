# Workflow: UX Redesign

## Branch Strategy

```
main (stable)
 └── 003-ux-redesign (long-lived feature branch)
      ├── 003-ux/phase-1-design-system
      ├── 003-ux/phase-2-strings
      ├── 003-ux/phase-3-navigation
      ├── 003-ux/phase-4-interaction
      └── 003-ux/phase-5-polish
```

### Rules
- **Feature branch:** `003-ux-redesign` — created from `main`
- **Phase branches:** One per phase, branched from `003-ux-redesign`
- **Merge direction:** Phase branch → `003-ux-redesign` → `main` (when all phases complete)
- **Never force push** on `003-ux-redesign` or `main`
- **Squash merge** phase branches into `003-ux-redesign`
- **Rebase** `003-ux-redesign` onto `main` before final merge (if main has advanced)

---

## Commit Convention

### Phase-Level Commits (squash merges)
Follow the existing project pattern:
```
Phase N: UX redesign — <description>
```

Examples:
- `Phase 1: UX redesign — design system fixes & critical bugs`
- `Phase 2: UX redesign — string extraction & localization infrastructure`
- `Phase 3: UX redesign — navigation, platform polish & accessibility`
- `Phase 4: UX redesign — interaction & content UX improvements`
- `Phase 5: UX redesign — visual polish, transitions & adaptive layout`

### Granular Commits (within a phase branch)
Use Conventional Commits for individual changes:

```
<type>(<scope>): <description>

[optional body]

Refs: UX-<finding-id>, T<task-id>
```

### Types
| Type | When |
|------|------|
| `fix` | Fixing existing bugs (e.g., hardcoded colors, broken animations) |
| `feat` | New components or capabilities |
| `refactor` | Structural changes with no behavior change (e.g., string extraction) |
| `style` | Visual-only changes (spacing, typography, shape tokens) |
| `test` | Adding or updating tests |
| `chore` | Build config, dependency changes |

### Scopes
| Scope | Module(s) |
|-------|-----------|
| `designsystem` | `core/designsystem` |
| `theme` | Theme.kt, Color.kt, Type.kt, Shape.kt |
| `nav` | Navigation graph, Route.kt, TopLevelDestination.kt |
| `auth` | `feature/auth` |
| `chat` | `feature/chat` |
| `home` | `feature/dashboard` (HomeScreen) |
| `hieroglyphs` | `feature/dictionary` (hub + dictionary screens) |
| `explore` | `feature/explore` |
| `stories` | `feature/stories` |
| `scan` | `feature/scan` |
| `settings` | `feature/settings` |
| `landing` | `feature/landing` |
| `feedback` | `feature/feedback` |
| `strings` | String extraction (any module) |
| `a11y` | Accessibility improvements |

### Examples
```
fix(theme): remove hardcoded color from displayLarge TextStyle

The displayLarge style had `color = WadjetColors.Ivory` baked in,
which bled into all composables using it.

Refs: UX-006, T002

feat(designsystem): add WadjetAsyncImage component

Unified image loading with consistent placeholder, error state,
and crossfade transition.

Refs: UX-038, T019

refactor(strings): extract all hardcoded strings from feature/chat

Moved 23 strings to strings.xml with ar stub entries.

Refs: UX-001, T029
```

---

## Phase Workflow

Each phase follows the same cycle:

```
┌─────────────────────────────────────────────────┐
│ 1. Create phase branch                          │
│    git checkout 003-ux-redesign                 │
│    git pull                                     │
│    git checkout -b 003-ux/phase-N-<name>        │
├─────────────────────────────────────────────────┤
│ 2. Read phase prompt                            │
│    Open phase-prompts.md §Phase N               │
│    Copy-paste prompt block into Copilot          │
├─────────────────────────────────────────────────┤
│ 3. Execute tasks                                │
│    Follow task list in tasks.md                 │
│    Commit after each logical group              │
├─────────────────────────────────────────────────┤
│ 4. Verify                                       │
│    ./gradlew assembleDebug (must pass)          │
│    ./gradlew testDebugUnitTest (must pass)      │
│    Manual spot-check on emulator / device       │
│    Review lint: ./gradlew lintDebug             │
├─────────────────────────────────────────────────┤
│ 5. Merge                                        │
│    git checkout 003-ux-redesign                 │
│    git merge --squash 003-ux/phase-N-<name>     │
│    git commit -m "Phase N: UX redesign — <summary>"│
│    git push                                     │
│    git branch -d 003-ux/phase-N-<name>          │
└─────────────────────────────────────────────────┘
```

---

## Phase Sequencing & Dependencies

```
Phase 1 (Design System)     ──►  Phase 2 (Strings)
                                       │
Phase 3 (Navigation) ◄────────────────┘
         │
Phase 4 (Interaction) ◄───────────────┘
         │
Phase 5 (Polish) ◄────────────────────┘
```

| Phase | Depends On | Can Start After |
|-------|-----------|-----------------|
| Phase 1 | None | Immediately |
| Phase 2 | Phase 1 (theme tokens needed for XML references) | Phase 1 merge |
| Phase 3 | Phase 2 (localized strings in nav labels) | Phase 2 merge |
| Phase 4 | Phase 1 (design system components), Phase 2 (strings) | Phase 2 merge |
| Phase 5 | Phase 3 + 4 (all structural changes done) | Phase 4 merge |

---

## Verification Checklist (per phase)

### Build
- [ ] `./gradlew assembleDebug` — clean pass (0 errors)
- [ ] `./gradlew assembleRelease` — clean pass (0 errors)
- [ ] No new lint warnings introduced: `./gradlew lintDebug`

### Tests
- [ ] `./gradlew testDebugUnitTest` — all pass
- [ ] Screenshot baselines recorded (Phase 1+): `./gradlew recordRoborazziDebug`
- [ ] Screenshot tests pass: `./gradlew verifyRoborazziDebug`

### Manual Verification
- [ ] Launch on emulator (API 26 min, API 35 target)
- [ ] All 5 bottom tabs navigate correctly
- [ ] No visible color regressions (Gold stays #D4AF37)
- [ ] Dark theme remains the only theme
- [ ] Animations play smoothly (no jank visible)
- [ ] Back button behavior correct on all screens
- [ ] TalkBack: navigate through each screen, verify announcements

### Code Quality
- [ ] No hardcoded `Color(0x...)` outside `WadjetColors`
- [ ] No hardcoded strings outside `strings.xml` (Phase 2+)
- [ ] No `RoundedCornerShape(Xdp)` outside `WadjetShapes` (Phase 1+)
- [ ] All new composables have `@Preview` functions
- [ ] No unused imports or dead code introduced

---

## Final Merge to Main

After all 5 phases are merged into `003-ux-redesign`:

```bash
# Update feature branch
git checkout 003-ux-redesign
git pull

# Rebase onto latest main (resolve conflicts if any)
git rebase main

# Final full verification
./gradlew clean assembleDebug assembleRelease
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew verifyRoborazziDebug

# Merge to main
git checkout main
git merge 003-ux-redesign
git push

# Clean up
git branch -d 003-ux-redesign
```

---

## Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| Phase 1 shapes migration breaks layouts | Screenshot tests catch regressions; roll back individual files |
| String extraction misses a screen | Grep for `"[A-Z]` in Kotlin files post-Phase 2 to find stragglers |
| Navigation refactor breaks deep links | Navigation tests in Phase 3; test each route individually |
| Shared element transitions cause jank | Phase 5 is last — if animations underperform, they can be simplified |
| Arabic RTL layout issues | Not in scope for this spec (separate RTL spec); only string extraction + font swap |
| Build time regression from test infra | Roborazzi is Robolectric-based (no emulator); CI budget ~5 min total |

---

## Quick Reference

| Item | Command / Path |
|------|---------------|
| Build debug | `./gradlew assembleDebug` |
| Build release | `./gradlew assembleRelease` |
| Run unit tests | `./gradlew testDebugUnitTest` |
| Run lint | `./gradlew lintDebug` |
| Record screenshots | `./gradlew recordRoborazziDebug` |
| Verify screenshots | `./gradlew verifyRoborazziDebug` |
| Gap analysis | `.specify/specs/003-ux-redesign/gap-analysis.md` |
| Task list | `.specify/specs/003-ux-redesign/tasks.md` |
| Phase prompts | `.specify/specs/003-ux-redesign/phase-prompts.md` |
| Design tokens | `.specify/specs/003-ux-redesign/design-tokens.md` |
| Test strategy | `.specify/specs/003-ux-redesign/testing.md` |
