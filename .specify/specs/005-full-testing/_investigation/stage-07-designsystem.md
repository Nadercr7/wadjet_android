# Stage 7 — Design System and Reusable Components

**Date:** 2025-07-22  
**Auditor:** Automated (Copilot)  
**Scope:** Theme (colors, typography, shapes), 19 components, 10 animations, core/ui

---

## Summary

| Metric | Value |
|---|---|
| **Theme** | Dark-only (no light scheme), custom `WadjetColors` object (24 colors) |
| **Typography** | 15 Material3 styles + 2 special (Hieroglyph, Gardiner) + Arabic (Cairo) variant |
| **Components** | 19 files |
| **Animations** | 10 files (8 infinite, 2 one-shot) |
| **@Preview anywhere** | ZERO |
| **Reduced motion support** | ZERO animations respect it |
| **Dynamic color / Material You** | Not supported |

---

## 1. Theme

### Color Scheme
| Aspect | Finding |
|---|---|
| Dark scheme | Fully defined — 20+ Material3 color slots mapped |
| Light scheme | **NOT DEFINED** — app is dark-only |
| Dynamic color | **Not supported** — no `dynamicDarkColorScheme()` |
| Custom colors | `WadjetColors` object with 24 constants (6 groups: Gold, Surfaces, Borders, Text, Semantic, Difficulty) |

**Issue:** Components mix `WadjetColors.X` direct references and `MaterialTheme.colorScheme` inconsistently. LoginSheet/RegisterSheet/ForgotPasswordSheet use `MaterialTheme.colorScheme.error` while everything else uses `WadjetColors.Error`.

### Typography

| Style | Font | Weight | Size | Notes |
|---|---|---|---|---|
| displayLarge | PlayfairDisplay | Bold | 36sp | |
| displayMedium | PlayfairDisplay | Bold | 30sp | |
| displaySmall | PlayfairDisplay | SemiBold | 24sp | |
| headlineLarge | PlayfairDisplay | SemiBold | 22sp | |
| headlineMedium | PlayfairDisplay | SemiBold | 20sp | |
| headlineSmall | PlayfairDisplay | SemiBold | **22sp** | **BUG: Larger than headlineMedium** |
| titleLarge | Inter | SemiBold | 18sp | |
| titleMedium | Inter | SemiBold | 16sp | |
| titleSmall | Inter | Medium | 14sp | |
| bodyLarge | Inter | Normal | 16sp | |
| bodyMedium | Inter | Normal | 14sp | |
| bodySmall | Inter | Normal | 12sp | |
| labelLarge | Inter | Medium | 14sp | |
| labelMedium | Inter | Medium | 12sp | |
| labelSmall | Inter | Medium | 10sp | |
| HieroglyphStyle | NotoSansEgyptianHieroglyphs | Normal | 32sp | Special |
| GardinerCodeStyle | JetBrainsMono | Normal | 14sp | Special |

**Arabic:** `wadjetTypographyForLang("ar")` swaps all Inter/PlayfairDisplay → Cairo (4 weights). Controlled at theme level via locale.

**Fonts:** PlayfairDisplay (2 weights), Inter (3 weights), JetBrainsMono (1), NotoSansEgyptianHieroglyphs (1), Cairo (4 weights).

### Shapes
- `small` = 8dp, `medium` = 12dp, `large` = 16dp, `extraLarge` = 24dp
- `extraSmall` not set (defaults to Material3's 4dp)

---

## 2. Component Inventory (19 files)

| # | Component | Purpose | @Preview | testTag | contentDesc | Issues |
|---|---|---|---|---|---|---|
| 1 | `WadjetButton` (4 variants) | Primary/Ghost/Dark/Text buttons | None | None | Button text as label | No icon slot; loading replaces text silently |
| 2 | `WadjetCard`/`WadjetCardGlow` | Card containers | None | None | Via Material Card | Glow variant has press animation |
| 3 | `WadjetTextField` | Text input fields | None | None | From OutlinedTextField | No supportingText/error message param |
| 4 | `WadjetTopBar` | Screen top bars | None | None | Back arrow has desc | Uses ExperimentalMaterial3Api |
| 5 | `WadjetSearchBar` | Search inputs | None | None | Clear: yes, Search: **null** | **Hardcoded "Search..." placeholder** |
| 6 | `WadjetBadge` | Tags/labels (4 variants) | None | None | None (text readable) | Pill shape not from WadjetShapes |
| 7 | `WadjetAsyncImage` | Remote image loading | None | None | Passed through | Uses SubcomposeAsyncImage (Coil 3) |
| 8 | `WadjetToast` | Toast notifications (3 types) | None | None | **Good:** liveRegion Polite | **Hardcoded "Success"/"Error"/"Info"** strings |
| 9 | `TtsButton` | TTS play/stop/loading | None | None | String resource | Same desc for all 3 states — unhelpful |
| 10 | `StreamingDots` | Chat typing indicator | None | None | **None** — invisible to screen reader | No reducedMotion |
| 11 | `ShimmerEffect` | Loading shimmer | None | None | N/A | `drawBehind` — safe, no recomp. No reducedMotion |
| 12 | `ShimmerPlaceholders` (3) | Skeleton screens (Card/Grid/Detail) | None | None | None | Compose ShimmerEffect blocks |
| 13 | `OfflineIndicator` | Network status banner | None | None | **Good:** liveRegion Polite | One-shot expand/shrink |
| 14 | `LoadingOverlay` | Fullscreen loader | None | None | **None** | No accessibility announcement |
| 15 | `ErrorState` | Error display with retry | None | None | Glyph has desc | Good reusable |
| 16 | `EmptyState` | Empty content state | None | None | Glyph semantics = title | Good reusable |
| 17 | `WadjetFullLoader` | Full-screen branded loader | None | None | None | No reducedMotion |
| 18 | `WadjetSectionLoader` | Section-level loader | None | None | Logo desc = **null** | No reducedMotion |
| 19 | **`ImageUploadZone`** | Image picker + upload | None | None | Selected image has desc | **See deep dive below** |

---

## 3. ImageUploadZone Deep Dive

### API
```
Parameters: onImageSelected, modifier, selectedImageUri?, title, subtitle,
            analyzeButtonText, isAnalyzing, onAnalyze?
```

### Icon
- Empty state: Unicode hieroglyph 𓂀 (Eye of Horus) at 48sp in gold
- **NOT a material icon** — it's a text character from NotoSansEgyptianHieroglyphs font
- No `contentDescription` on this specific text element
- **No parameter to customize the icon** — both Scan and Identify show the exact same visual

### ScanScreen vs IdentifyScreen Usage

| Parameter | ScanScreen | IdentifyScreen |
|---|---|---|
| `title` | `R.string.scan_upload_title` | `R.string.identify_upload_title` |
| `subtitle` | `R.string.scan_upload_subtitle` | `R.string.identify_upload_subtitle` |
| `analyzeButtonText` | `R.string.scan_analyze_button` | `R.string.identify_analyze_button` |
| `onAnalyze` | **null** | **null** |
| Selected image URI | Not passed | Not passed |

**Both pass `onAnalyze = null`** — the analyze button never renders. Image selection fires `onImageSelected` directly via photo picker. Both show identical visual: Eye of Horus on dark background.

### Hardcoded Defaults
```kotlin
title: String = "Tap to select an image"       // English only!
subtitle: String = "Supports JPG, PNG up to 10MB" // English only!
analyzeButtonText: String = "Analyze"            // English only!
```

---

## 4. Animation Inventory

| # | Animation | Infinite? | Duration | Method | reducedMotion | Notes |
|---|---|---|---|---|---|---|
| 1 | `BorderBeam` | Yes | 4000ms | `graphicsLayer` | No | Rotating gold beam |
| 2 | `ButtonShimmer` | Yes | 2000ms | `drawWithContent` | No | Horizontal shine sweep |
| 3 | `DotPattern` | Yes | 3000ms | Canvas | No | **Canvas draws many circles/frame — potential perf issue** |
| 4 | `FadeUp` | One-shot | 600ms | composition | No | Fade + translate |
| 5 | `GoldGradientSweep` | Yes | 4000ms | `drawWithContent` | No | Angular gradient |
| 6 | `GoldGradientText` | Yes | 3000ms | `ShaderBrush` | No | Horizontal sweep on text |
| 7 | `GoldPulse` | Yes | 2000ms | `graphicsLayer` | No | Alpha/scale pulse on gold elements |
| 8 | `KenBurnsImage` | Yes | 20000ms | `graphicsLayer` | No | Slow zoom+pan on images |
| 9 | `MeteorShower` | Yes | 3000ms+stagger | Canvas | No | Falling gold particles (count configurable) |
| 10 | `ShineSweep` | Yes | 3000ms | `drawWithContent` | No | Highlight sweep |

**Key finding:** All 10 animations use `rememberInfiniteTransition` targeting draw phases (graphicsLayer/drawWithContent/Canvas) — **no infinite recompositions**. Safe for performance.

**Systemic accessibility gap:** **None of the 10 animations respect the `reducedMotion` system setting.** Users who need reduced motion still see all infinite animations.

---

## 5. core/ui

| File | Purpose | Issues |
|---|---|---|
| `WadjetPreviews.kt` | Multi-preview annotation (Phone, Landscape, Tablet, Dark, RTL Arabic, Large Text) | **Never used** — no @Preview exists anywhere |
| `SharedTransitionLocals.kt` | CompositionLocals for shared element transitions | Working — used by Explore/Stories |
| `Placeholder.kt` | Empty file (only package declaration) | Dead file |

---

## 6. Critical Design System Issues

| # | Issue | Severity | Impact |
|---|---|---|---|
| 1 | **No light theme** — dark-only | HIGH | Users in bright environments have no option |
| 2 | **headlineSmall (22sp) > headlineMedium (20sp)** — size hierarchy inverted | MEDIUM | Incorrect visual hierarchy |
| 3 | **ImageUploadZone has no icon parameter** — Scan and Identify are indistinguishable | HIGH | User confusion |
| 4 | **WadjetSearchBar has hardcoded "Search..." default** | MEDIUM | i18n violation |
| 5 | **WadjetToast has hardcoded "Success"/"Error"/"Info"** | MEDIUM | i18n violation |
| 6 | **ImageUploadZone has hardcoded English defaults** | MEDIUM | i18n violation |
| 7 | **Zero @Preview in entire codebase** | HIGH | Impossible to iterate on design in Android Studio |
| 8 | **Zero animation reducedMotion support** | HIGH | Accessibility violation for 10 animations |
| 9 | **LoadingOverlay / StreamingDots have no a11y** | MEDIUM | Invisible to screen readers |
| 10 | **TtsButton: same desc for 3 states** | LOW | Screen reader can't convey state changes |

---

## 7. Recommendations

1. **Add icon parameter to `ImageUploadZone`** — allow Scan vs Identify to use different icons
2. **Define light theme** — even if dark is primary, users need options
3. **Fix headlineSmall size** — should be 18sp (below headlineMedium 20sp)
4. **Move all hardcoded strings to string resources** — SearchBar, Toast, ImageUploadZone defaults
5. **Add reducedMotion checks** — `val reduceMotion = LocalReducedMotion.current` for all infinite animations
6. **Add @Preview to every component** — use the existing `WadjetPreviews` annotation
7. **Add testTags to all components** — at minimum buttons, inputs, cards, lists
8. **Add liveRegion/contentDescription to LoadingOverlay and StreamingDots**
9. **Make TtsButton contentDescription state-aware** — different for idle/loading/playing
10. **Delete Placeholder.kt** (empty file) and dead ScanScreen permission code
