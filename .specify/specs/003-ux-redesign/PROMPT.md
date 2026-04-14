# UI/UX Architecture Audit & Redesign — Wadjet Android

## Your Role

You are a **senior UI/UX architect** specializing in mobile app design systems, information architecture, and Jetpack Compose. You will perform a deep, exhaustive audit of the Wadjet Android app's UI/UX — then produce a complete set of planning files using the spec-kit format defined below.

**Scope: UI/UX ONLY.** Do not touch, analyze, or recommend changes to: business logic, ViewModels internals, API calls, repositories, backend contracts, or data layer. Your concern is what the user sees, touches, and experiences.

---

## About the App

Wadjet is an Egyptian archaeology Android app. Features include:
- Hieroglyph scanning (image upload → AI recognition)
- Hieroglyph dictionary (browse, learn, write, translate)
- Landmark exploration (Egyptian sites with details, images, maps)
- AI chat with "Thoth" (streaming, markdown, STT)
- Egyptian mythology stories (chapters, narration, images)
- User dashboard, settings, feedback

**Stack:** Kotlin · Jetpack Compose (no XML, no Fragments) · Material 3 · Hilt · Compose Navigation · Dark-only theme (Egyptian dark & gold aesthetic)

---

## Phase 1: Deep Investigation

### Step 1 — Read the project's constitution
```
.specify/memory/constitution.md
```
This contains the governing design principles. Respect all constraints defined there.

### Step 2 — Read EVERY screen composable in the app
Read each file **in full** — every composable, layout, spacing, padding, color, typography, scroll behavior, interaction pattern, transition, and state handling:

**App shell & navigation:**
- `app/src/main/java/com/wadjet/app/MainActivity.kt`
- `app/src/main/java/com/wadjet/app/navigation/Route.kt`
- `app/src/main/java/com/wadjet/app/navigation/TopLevelDestination.kt`
- `app/src/main/java/com/wadjet/app/navigation/WadjetNavGraph.kt`
- `app/src/main/java/com/wadjet/app/screen/HieroglyphsHubScreen.kt`

**All feature screens:**
- `feature/auth/` → `WelcomeScreen.kt`, `sheet/LoginSheet.kt`, `sheet/RegisterSheet.kt`, `sheet/ForgotPasswordSheet.kt`
- `feature/landing/` → `LandingScreen.kt`
- `feature/scan/` → `ScanScreen.kt`, `ScanResultScreen.kt`, `ScanHistoryScreen.kt`
- `feature/explore/` → `ExploreScreen.kt`, `LandmarkDetailScreen.kt`, `IdentifyScreen.kt`
- `feature/dictionary/` → `DictionaryScreen.kt`, `BrowseTab.kt`, `LearnTab.kt`, `WriteTab.kt`, `TranslateTab.kt`, `DictionarySignScreen.kt`, `LessonScreen.kt`, `SignDetailSheet.kt`
- `feature/chat/` → `ChatScreen.kt`
- `feature/stories/` → `StoriesScreen.kt`, `StoryReaderScreen.kt`
- `feature/dashboard/` → `DashboardScreen.kt`
- `feature/settings/` → `SettingsScreen.kt`
- `feature/feedback/` → `FeedbackScreen.kt`

**Entire design system:**
- `core/designsystem/src/main/java/**/component/*.kt` (all 18 shared components)
- `core/designsystem/src/main/java/**/WadjetTheme.kt`
- `core/designsystem/src/main/java/**/WadjetColors.kt`
- `core/designsystem/src/main/java/**/WadjetTypography.kt`
- `core/designsystem/src/main/java/**/WadjetShapes.kt`
- `core/designsystem/src/main/java/**/WadjetFonts.kt`
- `core/designsystem/src/main/java/**/animation/*.kt`

**Animation system** (10 custom animations — audit each for quality/usage):
- `BorderBeam.kt`, `ButtonShimmer.kt`, `DotPattern.kt`, `FadeUp.kt`, `GoldGradientSweep.kt`, `GoldGradientText.kt`, `GoldPulse.kt`, `KenBurnsImage.kt`, `MeteorShower.kt`, `ShineSweep.kt`

**Font resources** (5 families — evaluate hierarchy/consistency):
- `core/designsystem/src/main/res/font/` → Cairo (Bold/Medium/Regular/SemiBold), Inter (Medium/Regular/SemiBold), JetBrains Mono (Regular), Noto Sans Egyptian Hieroglyphs, Playfair Display (Bold/SemiBold)

**Shared UI & previews:**
- `core/ui/src/main/java/**/WadjetPreviews.kt`

**Resource files** (XML remnants that affect UI):
- `app/src/main/res/values/strings.xml` — all user-facing text
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/drawable/ic_launcher_background.xml`
- Mipmap directories (app icon assets)

**Build config** (check Compose BOM / Material 3 version):
- `build.gradle.kts` (root)
- `app/build.gradle.kts`
- `core/designsystem/build.gradle.kts`
- `gradle/libs.versions.toml`

### Step 3 — Study reference repos for best practices
Browse these repos in `D:\Personal attachements\Repos\` — extract UI/UX patterns, navigation architecture, design system conventions, and Material 3 best practices:

| Repo | Path | What to look for |
|------|------|-----------------|
| **Now in Android** | `23-Android-Kotlin/nowinandroid/` | Google's official reference — navigation, adaptive layouts, design system, NiaApp scaffold |
| **Compose Samples** | `23-Android-Kotlin/compose-samples/` | **Jetsnack** (bottom nav, cart, catalog), **Jetchat** (chat UI, keyboard handling), **JetNews** (article reader, list/detail), **Jetcaster** (media, dark theme), **JetLagged** (dashboard, charts), **Reply** (adaptive layouts, list/detail pane) — study screen hierarchy, transitions, component reuse |
| **AwesomeUI** | `23-Android-Kotlin/AwesomeUI/` | Copy-paste Compose UI snippets, card patterns, list patterns |
| **Pokedex** | `23-Android-Kotlin/Pokedex/` | Clean MVVM UI structure, detail screen patterns |
| **Pokedex Compose** | `23-Android-Kotlin/pokedex-compose/` | Compose-specific detail/list UI patterns |
| **Compose Tutorials** | `23-Android-Kotlin/Jetpack-Compose-Tutorials/` | Component examples, layout techniques |
| **Agent Skills** | `23-Android-Kotlin/awesome-android-agent-skills/` | SKILL.md files for AI-assisted Android development |
| **Compose Apps List** | `23-Android-Kotlin/awesome-jetpack-compose-android-apps/` | Open-source Compose apps for inspiration |
| **Compose Resources** | `23-Android-Kotlin/awesome-jetpack-compose-learning-resources/` | Learning resources, tutorial links, article references |
| **Awesome Kotlin** | `23-Android-Kotlin/awesome-kotlin/` | Kotlin ecosystem — UI libraries, tools, frameworks |
| **Awesome Android UI** | `23-Android-Kotlin/awesome-android-ui/` | Android UI library catalogue — find specific component libraries for gaps |
| **Architecture Samples** | `23-Android-Kotlin/architecture-samples/` | Google architecture blueprints — UI layer patterns |
| **Jetpack Compose Awesome** | `23-Android-Kotlin/jetpack-compose-awesome/` | Curated list of Compose resources, libraries, tools — discover UI libraries we could use |

**Focus areas:** How do professional apps structure navigation? Handle information density? Use tabs vs. sub-screens vs. bottom sheets? Structure dark themes with accent colors? Handle onboarding, empty states, adaptive layouts? How do they handle auth flows (sheets vs. full-screen)? Chat UI patterns (keyboard, bubbles, streaming)? Reader/immersive screens (stories, articles)? Image-heavy list/grid layouts? Search patterns? Detail screen back-navigation? Bottom sheet vs. dialog decisions? Splash/loading transitions?

---

## Phase 2: Comprehensive Analysis

Analyze **every single aspect** of the UI/UX. Cover ALL dimensions below — and anything else you discover, even if not listed:

### A. Information Architecture & Screen Hierarchy
- Is the current 5-tab bottom navigation (Home | Hieroglyphs | Explore | Stories | Thoth) optimal? Should tabs be merged/split/reordered?
- Does the hub-within-a-tab pattern (HieroglyphsHub → Scan/Dictionary/Write) make sense, or should these be promoted to top-level?
- Is nesting 4 tabs inside Dictionary (Browse/Learn/Write/Translate) good UX or overwhelming? Should Write be its own feature screen?
- Navigation depth analysis: how many taps from each bottom tab to reach every screen? Is anything >2 taps deep?
- Should Dashboard/Settings/Feedback be organized differently (e.g., Profile tab instead of avatar icon)?
- Splash → Welcome → Landing flow — is the auth gate smooth?
- Overall sitemap — is the screen tree logical for a casual Egyptian history enthusiast?

### B. User Flow Analysis
- Map every user journey: first launch → auth → landing → each feature
- Identify dead ends, confusing back-stack, or missing transitions
- Analyze quick actions / shortcuts on Landing — right choices?
- Cross-feature flows: scan → result → learn sign → practice writing it
- Feature discoverability — can new users find everything in one session?

### C. Visual Consistency & Design System
- Is dark+gold Egyptian theme applied consistently on ALL screens?
- Spacing, padding, typography, corner radii — consistent everywhere?
- Do all cards, buttons, text fields follow the design system components?
- Are there screens that feel "different" from the rest?
- Is the component library (18 components) sufficient? What's missing? (e.g., app bars variants, search bars, filter chips, image carousels, progress indicators, snackbars, tab bars)
- Empty states, error states, loading states — evaluated per screen (use EmptyState.kt, ErrorState.kt, ShimmerEffect.kt audit)
- **Typography system**: Are the 5 font families (Cairo, Inter, Playfair Display, JetBrains Mono, Noto Sans Egyptian Hieroglyphs) used consistently? Is the hierarchy clear (headings, body, labels, hieroglyph display)? Compare with NiA and Jetsnack typography systems.
- **Color system**: Are WadjetColors tokens comprehensive enough? Are there hardcoded colors in screen files?
- **Icon system**: Consistent icon style? Material icons vs. custom? Any missing icons?

### D. Interaction Design
- Bottom sheet usage — appropriate everywhere?
- Screen transitions — smooth and meaningful?
- Pull-to-refresh, swipe gestures, long-press — where should they exist?
- Tap targets — all ≥48dp?
- User feedback on actions — loading/success/failure always communicated?

### E. Accessibility
- Content descriptions on images/icons
- Touch target sizing
- Color contrast in dark theme
- Screen reader navigation order
- Focus management in forms and search
- RTL readiness (Arabic support is planned)

### F. Responsive & Adaptive Layout
- Behavior on different screen sizes (phone, tablet, foldable)
- LazyColumn/LazyGrid usage
- Landscape mode handling
- WindowSizeClass integration

### G. Onboarding & Discoverability
- Is onboarding needed? What would it cover?
- Tooltips, hints, coachmarks — where?
- Can users understand each feature's purpose without help?

### H. Micro-interactions & Polish
- Animation presence and quality (enter/exit, shared element, state change)
- Haptic feedback on key actions (scan complete, story interaction, glyph tap)
- Skeleton/shimmer loading (are ShimmerPlaceholders used everywhere they should be?)
- Collapsing toolbars, sticky headers, scroll behaviors (LazyColumn + TopAppBar integration)
- Keyboard handling in search/chat/forms (WindowInsets, ime padding, auto-focus)
- Gesture navigation compatibility (edge-to-edge, system bars)
- Image loading experience (placeholders, crossfade, error fallback — Coil configuration)
- Pull-to-refresh on content lists
- Scroll-to-top behavior

### I. Auth & Onboarding UX
- Welcome screen → Login/Register sheet flow — is it smooth?
- Google Sign-In placement and prominence
- Forgot password flow
- Error handling in auth forms (validation, server errors)
- First-time user experience after successful auth
- Session expiry / token refresh — does the user notice?

### J. Content-Specific UX
- **Scan flow**: Upload → waiting → result — is progress clear? Is the result screen overwhelming or well-structured?
- **Chat UX**: Message bubbles, streaming indicator, markdown rendering, STT button placement, keyboard behavior
- **Story Reader**: Chapter navigation, TTS controls, interaction types (4 types), scene images, immersive reading experience
- **Dictionary**: Search experience, sign detail information density, lesson progression, write preview quality
- **Explore/Landmarks**: Card grid layout, detail screen sections (carousel, maps, tips, recommendations), "Ask Thoth" integration
- **Dashboard**: Stats presentation, history, favorites — is it useful or just a data dump?

### K. Android Platform Polish
- **Edge-to-edge display**: Is `enableEdgeToEdge()` properly implemented? Are system bars handled correctly on all screens? Compare with NiA's implementation.
- **Predictive back gesture** (Android 14+): Does the app support it? Are back handlers properly set up?
- **Android 12+ Splash Screen API**: Is it used, or does the app still show a custom splash? Is the transition smooth?
- **System bar colors**: Do status bar / navigation bar colors match the dark theme on all screens?
- **Window insets**: Are `WindowInsets.ime`, `WindowInsets.systemBars`, `WindowInsets.navigationBars` handled correctly — especially on ChatScreen (keyboard), forms, and bottom sheets?
- **Material 3 version**: Is the app using the latest M3 components, or are there deprecated usages?
- **Permission request UI**: RECORD_AUDIO permission — is the rationale dialog well-designed? POST_NOTIFICATIONS — is timing appropriate?
- **Configuration changes**: Does the app handle rotation, split-screen, picture-in-picture gracefully?
- **Text scaling**: Does the UI handle system font size changes (large text accessibility)?
- **Dark theme integration**: Does the app respect `isSystemInDarkTheme()` correctly (it's forced dark, but system integration matters)?

### L. Performance & Smoothness (UI layer only)
- **Recomposition**: Are there unnecessary recompositions visible in any screen? (Check for unstable parameters, missing `remember`, lambda allocations in LazyColumn items)
- **Lazy list performance**: Are `key` parameters used in all LazyColumn/LazyGrid items? `contentType`?
- **Image performance**: Coil memory/disk cache configuration, image sizing, crossfade timing
- **Animation performance**: Are animations running at 60fps? Any jank on transitions?
- **Compose stability**: Are data classes used for UI state? Are collections wrapped with `@Immutable`/`@Stable`?
- **Startup**: Is the initial compose frame fast? Any heavy composition on first frame?

---

## Phase 3: Output — Create Planning Files

Create all output files inside: `.specify/specs/003-ux-redesign/`

### File 1: `gap-analysis.md`

```markdown
# UX Gap Analysis: Wadjet Android

## Summary
[Executive summary — overall UX health, top 5 critical findings]

## Methodology
[What was analyzed, what repos were referenced]

## Findings

### A. Information Architecture & Screen Hierarchy

#### UX-001 | 🔴 Critical | [Title]
- **Current state:** [What exists — reference exact file + composable]
- **Problem:** [Why it's bad for users]
- **Recommendation:** [Specific fix, referencing patterns from repos]
- **Reference:** [Which repo/sample demonstrates the better pattern]

#### UX-002 | 🟠 Major | [Title]
...

### B. User Flow Analysis
...

### C–H. [Continue for all dimensions]

## Statistics
| Severity | Count |
|----------|-------|
| 🔴 Critical | X |
| 🟠 Major | X |
| 🟡 Minor | X |
| 🔵 Enhancement | X |
```

Severity guide:
- 🔴 Critical = Users get lost, can't complete tasks, or leave the app
- 🟠 Major = Significant friction, confusing flow, visual inconsistency
- 🟡 Minor = Small polish issues, missing feedback, minor inconsistency
- 🔵 Enhancement = Nice-to-have improvements, delight features

### File 2: `architecture.md`

```markdown
# UX Architecture: Wadjet Android

## Current Screen Hierarchy
[ASCII sitemap of current state]

## Proposed Screen Hierarchy
[ASCII sitemap of proposed state]

## Navigation Architecture
### Current Bottom Navigation
[Current tabs and their contents]

### Proposed Bottom Navigation
[Proposed tabs with rationale for each change]

## Comparison Table
| Aspect | Current | Proposed | Rationale |
|--------|---------|----------|-----------|
| Bottom nav tabs | ... | ... | ... |
| Max navigation depth | ... | ... | ... |
| ... | ... | ... | ... |

## Navigation Depth Map
[Table: every screen → number of taps from nearest bottom tab]

## Screen Grouping Rationale
[Why screens are organized the way they are]
```

### File 3: `spec.md`

```markdown
# Feature Specification: UX Redesign
**Spec ID**: 003-ux-redesign
**Status**: Draft
**Date**: [date]

## User Scenarios & Testing

### User Story 1 — [Title] (Priority: P0)
[Description]
**Why this priority:** ...
**Acceptance Scenarios:**
1. Given [state], When [action], Then [outcome]
2. ...

### User Story 2 — [Title] (Priority: P1)
...

### Edge Cases
...

## Requirements

### Functional Requirements
- **FR-UX-001**: [requirement]
- **FR-UX-002**: [requirement]
...

### Non-Functional Requirements
- **NFR-UX-001**: [requirement]
...
```

Priority guide: P0 = must-have (broken UX), P1 = should-have (major friction), P2 = nice-to-have (polish), P3 = future (aspirational)

### File 4: `plan.md`

```markdown
# Implementation Plan: UX Redesign
**Spec**: 003-ux-redesign
**Date**: [date]

## Summary
[One paragraph overview]

## Technical Context
- Language: Kotlin 2.0+
- UI: Jetpack Compose + Material 3
- Navigation: Compose Navigation 2.8+ (type-safe routes)
- Theme: Dark-only, Egyptian gold accent
- Constraints: No XML, no Fragments, no light theme

## Phases

### Phase 1: [Name] — [Goal]
**Dependencies:** None
**Files affected:**
- `path/to/file.kt` — [what changes]
...

### Phase 2: [Name] — [Goal]
**Dependencies:** Phase 1
...

## Complexity Tracking
| Phase | Files | New Components | Risk |
|-------|-------|---------------|------|
| 1 | X | Y | Low/Med/High |
...
```

### File 5: `tasks.md`

```markdown
# Tasks: UX Redesign

## Format
- `[ID] [P?] [Story] Description` → `[file.kt]`
- [P] = parallelizable
- [Story] = US1, US2, etc.

## Phase 1: [Name]
- [ ] T001 [US1] Description → `path/to/file.kt`
- [ ] T002 [P] [US1] Description → `path/to/file.kt`
...

## Phase 2: [Name]
- [ ] T010 [US2] Description → `path/to/file.kt`
...
```

### File 6: `phase-prompts.md`

Self-contained copy-paste prompts for each phase. Each prompt must:
1. List exact source files to read first
2. Give task-by-task instructions
3. Reference the spec/plan for context
4. Include build + test verification
5. End with git commit + push

```markdown
# Phase Prompts: UX Redesign

## Phase 1: [Name]

### Prompt:
[COPY-PASTE BLOCK START]

Read these files first:
- `.specify/specs/003-ux-redesign/plan.md` (Phase 1 section)
- `.specify/specs/003-ux-redesign/tasks.md` (Phase 1 tasks)
- [list all source files to read]

Tasks:
1. [T001] ...
2. [T002] ...
...

Verification:
1. Build: `./gradlew assembleDebug` — must succeed with zero errors
2. Run tests: `./gradlew testDebugUnitTest` — all existing tests must pass
3. Manual check: [specific things to verify on device]
4. Lint: no new warnings

Commit & push:
```
git add -A
git commit -m "Phase 1: UX redesign — [description]"
git push origin 003-ux-redesign
```

[COPY-PASTE BLOCK END]

## Phase 2: [Name]
...
```

### File 7: `design-tokens.md` (only if needed)

```markdown
# Design Token Changes: UX Redesign

## New/Modified Colors
| Token | Current | Proposed | Usage |
|-------|---------|----------|-------|
...

## New/Modified Typography
...

## New Components Needed
### ComponentName
- **Purpose:** ...
- **Signature:** `@Composable fun ComponentName(...)`
- **Usage:** [which screens]
...

## Spacing & Layout Tokens
...
```

### File 8: `testing.md`

```markdown
# UI Testing Plan: UX Redesign

## Current State
- 0 UI/screenshot tests exist
- Only 6 tests total (data/network/ViewModel layer)

## Testing Strategy

### Screenshot Tests (Compose Preview Testing)
For each screen that changes, define golden screenshots:
- [ ] ST-001 `ScreenName` — default state
- [ ] ST-002 `ScreenName` — loading state
- [ ] ST-003 `ScreenName` — error state
- [ ] ST-004 `ScreenName` — empty state
...

### Navigation Tests
- [ ] NT-001 Bottom tab switching — all 5 tabs reachable
- [ ] NT-002 Deep navigation — scan → result → sign detail → dictionary
- [ ] NT-003 Back stack — correct behavior on system back from every screen
...

### Accessibility Tests
- [ ] AT-001 All images have contentDescription
- [ ] AT-002 All tap targets ≥ 48dp
- [ ] AT-003 Color contrast passes WCAG AA on all text
- [ ] AT-004 TalkBack navigation order logical on every screen
...

### Interaction Tests
- [ ] IT-001 Auth flow: welcome → login → landing
- [ ] IT-002 Scan flow: upload → result → back
...

## Test Infrastructure
- Framework: [Compose Testing / Roborazzi / Paparazzi / manual]
- CI integration: [how tests run]
```

### File 9: `workflow.md`

```markdown
# Git Workflow: UX Redesign

## Branch Strategy
- Base branch: `main` (current HEAD: `674057f`)
- Feature branch: `003-ux-redesign`
- Sub-branches per phase (optional): `003-ux-redesign/phase-1`

## Commit Convention
Follow existing pattern: `Phase X.Y: [description]`
Examples:
- `Phase 1: UX redesign — navigation restructuring`
- `Phase 2: UX redesign — design system tokens & components`

## Phase Workflow
For each phase:
1. Create/switch to branch
2. Implement all tasks in the phase
3. Build & verify — zero compile errors
4. Run existing tests — all pass
5. Run new UI tests (if added)
6. Commit with descriptive message
7. Push to remote
8. Optional: tag milestone phases (e.g., `v3.1-ux-phase1`)

## Verification Before Push
- [ ] `./gradlew assembleDebug` — builds clean
- [ ] `./gradlew testDebugUnitTest` — existing tests pass
- [ ] Manual smoke test on device/emulator
- [ ] No hardcoded strings added (all in strings.xml or composable parameters)
- [ ] No new lint warnings

## Final Merge
- Squash or merge into `main`
- Tag: `v3.1-ux-redesign`
- Push: `git push origin main --tags`
```

---

## Constraints to Respect
- **Dark-only** — no light mode. Egyptian dark & gold aesthetic.
- **Compose only** — no XML layouts, no Fragments, no Views.
- **Smart defaults** — the system picks the best option, no user selectors for AI modes/voices.
- **≤2 taps** — any feature reachable within 2 taps from a bottom tab.
- **Camera disabled** — scan uses image upload, not camera.
- **Arabic RTL** — not implemented yet but architecture must not block it.
- **UI/UX only** — do NOT modify or recommend changes to business logic, APIs, or data layer.
- **Edge-to-edge** — all screens must properly handle system bars and window insets.
- **Minimum API 26** (Android 8.0) — respect API level constraints.
- **Material 3** — use latest M3 components; no Material 2 or AppCompat UI components.
- **Egyptian identity in ALL states** — empty, error, and loading states must use Egyptian-themed visuals (pyramid silhouettes, Eye of Horus, papyrus scrolls) and Egyptian-flavored copy ("The ancient scribes couldn't read this image"). Never use generic Material placeholder text or icons.
- **No emojis in app UI** — no emoji characters in any user-facing text, component, or string resource. Use Egyptian iconography instead.
- **Brand tone** — mystical, knowledgeable, slightly playful — like an ancient Egyptian guide speaking to a modern explorer. All user-facing text (errors, empty states, tooltips, onboarding) must follow this voice.

## Professional App Standard
The end result should match the quality of professional apps like:
- **Google's Now in Android** (navigation, adaptive layouts, design system)
- **Google's Jetsnack/Reply** (visual polish, transitions, component quality)
- **Medium, Spotify, Airbnb** (onboarding, discoverability, micro-interactions)

Ask yourself for every screen: "Would this pass a design review at a top-tier app company?"

## Quality Checklist
Before finalizing, verify:
- [ ] Every screen composable in the app has been read and analyzed (all 25+ screens + 4 sheets)
- [ ] Every design system component (18 files) has been reviewed
- [ ] Every animation (10 files) has been audited
- [ ] Font system (5 families) evaluated for hierarchy and consistency
- [ ] Resource files (strings.xml, colors.xml, themes.xml) checked
- [ ] Build config checked for Compose BOM / M3 versions
- [ ] Every finding has a concrete, actionable recommendation (not "improve this")
- [ ] Every recommendation references a pattern from at least one reference repo
- [ ] Navigation depth from every bottom tab to every screen is documented in a table
- [ ] All tasks specify exact files to modify and composables to change
- [ ] Zero logic/backend changes — purely UI/UX
- [ ] Phase prompts are self-contained and copy-pasteable
- [ ] Each phase prompt ends with build + test verification commands
- [ ] Output files are in `.specify/specs/003-ux-redesign/`
- [ ] Auth flow (Welcome → sheets) analyzed
- [ ] All 12 analysis dimensions (A–L) covered with findings
- [ ] Cross-feature user flows mapped (scan → learn → write → chat)
- [ ] Comparison with NiA, Jetsnack, Jetchat, Reply patterns documented
- [ ] Testing plan covers screenshot, navigation, accessibility, and interaction tests
- [ ] Git workflow includes branch strategy, commit convention, and verification steps
- [ ] Android platform polish reviewed (edge-to-edge, predictive back, splash, insets, permissions)
- [ ] Performance audit covers recomposition, lazy lists, image loading, animations
- [ ] Every phase prompt includes commit + push instructions at the end
- [ ] Total output: 9 files (gap-analysis, architecture, spec, plan, tasks, phase-prompts, design-tokens, testing, workflow)
