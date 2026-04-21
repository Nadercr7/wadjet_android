# Phase 02: Visual Issues - Findings

## Source
Stages 6, 7, 15, 17 from 005-full-testing

## Issues Found
| # | ID | Category | Severity | File:Line | Description | Root Cause |
|---|-----|----------|----------|-----------|-------------|------------|
| 1 | FIX-020 | UX | CRITICAL | feature/auth/WelcomeScreen.kt | Sign-in buttons cut off at 720×1280, no scroll | Column without verticalScroll |
| 2 | FIX-021 | Design | MEDIUM | core/designsystem/WadjetTypography.kt | headlineSmall (22sp) > headlineMedium (20sp) | Size values swapped |
| 3 | FIX-022 | UX | HIGH | core/designsystem/ImageUploadZone.kt | No icon parameter — Scan/Identify indistinguishable | Hardcoded Eye of Horus |
| 4 | FIX-023 | UX | HIGH | feature/explore/ExploreScreen.kt | Identify button tiny 63×63 with no label | Small Icon, no text |
| 5 | FIX-024 | UX | MEDIUM | feature/landing/LandingScreen.kt | DotPattern ~4000 drawCircle per frame | Individual circles not pre-rendered |
| 6 | FIX-025 | UX | MEDIUM | core/designsystem/ShineSweep.kt | shineSweep on every list card — 10+ concurrent infinite anims | Applied to all cards |
| 7 | FIX-026 | i18n | HIGH | feature/chat/values-ar/strings.xml | English strings instead of Arabic + UTF-8 mojibake | Bad translation file |
| 8 | FIX-027 | UX | LOW | feature/auth/WelcomeScreen.kt | "Built by Mr Robot" footer | Unprofessional text |
| 9 | FIX-028 | UX | LOW | feature/landing/LandingScreen.kt | "Translate" bullet but only 3 tabs | Stale text |
| 10 | FIX-029 | UX | MEDIUM | feature/stories/StoryReaderScreen.kt | Loading state is just `Text("Loading...")` | No proper loader |

## Fix Priority
| Priority | Issue IDs | Reason |
|----------|-----------|--------|
| P0 | FIX-020 | Users can't log in on small phones |
| P1 | FIX-022, FIX-023, FIX-026 | Core UX confusion, i18n broken |
| P2 | FIX-021, FIX-024, FIX-025, FIX-027, FIX-028, FIX-029 | Visual polish |
