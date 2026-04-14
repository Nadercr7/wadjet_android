# Design Token Changes: UX Redesign

## New/Modified Colors

| Token | Hex | Usage | Change |
|-------|-----|-------|--------|
| `DifficultyBeginner` | `#D4AF37` (Gold) | Story card beginner gradient start | NEW |
| `DifficultyBeginnerDark` | `#8B6914` | Story card beginner gradient end | NEW (was hardcoded) |
| `DifficultyIntermediate` | `#4A90D9` | Story card intermediate gradient start | NEW (was hardcoded) |
| `DifficultyIntermediateDark` | `#1A3A6B` | Story card intermediate gradient end | NEW (was hardcoded) |
| `DifficultyAdvanced` | `#9B59B6` | Story card advanced gradient start | NEW (was hardcoded) |
| `DifficultyAdvancedDark` | `#4A1A6B` | Story card advanced gradient end | NEW (was hardcoded) |
| `TextDim` | REMOVED | Was `#7E7E7E`, merged into `TextMuted` | REMOVED |

### Unchanged Tokens (reference)
| Token | Hex | Notes |
|-------|-----|-------|
| `Gold` | `#D4AF37` | Primary accent — no change |
| `GoldLight` | `#E5C76B` | Gradient highlight — no change |
| `Night` | `#0A0A0A` | Background — no change |
| `Surface` | `#141414` | Card/sheet bg — no change |
| `SurfaceAlt` | `#1E1E1E` | Secondary container — no change |
| `Text` | `#F0F0F0` | Primary text — no change |
| `TextMuted` | `#8A8A8A` | Secondary text — absorbs TextDim usages |
| `Success` | `#4CAF50` | Replaces hardcoded `Color(0xFF4CAF50)` in RegisterSheet |
| `Error` | `#EF4444` | Replaces hardcoded `Color(0xFFFF4444)` in 4 screens |
| `Warning` | `#F59E0B` | Used in ScanResultScreen — document, don't remove |
| `Dust` | `#8B7355` | Used in footer text — document, don't remove |

## New/Modified Typography

| Style | Current | Proposed | Change |
|-------|---------|----------|--------|
| `displayLarge` | PlayfairDisplay Bold 36sp, **color=Ivory** | PlayfairDisplay Bold 36sp, **no color** | MODIFIED — remove inline color |
| `headlineSmall` | Missing (M3 default) | PlayfairDisplay SemiBold 22sp / 28sp | NEW |
| Arabic display/headline | Not swapped (remain PlayfairDisplay) | Swapped to Cairo Bold/SemiBold | MODIFIED in `wadjetTypographyForLang` |

### Full Arabic Typography Mapping
When `locale == "ar"`, `wadjetTypographyForLang("ar")` applies:

| Style | English Font | Arabic Font |
|-------|-------------|-------------|
| `displayLarge` | PlayfairDisplay Bold | **Cairo Bold** |
| `displayMedium` | PlayfairDisplay Bold | **Cairo Bold** |
| `displaySmall` | PlayfairDisplay SemiBold | **Cairo SemiBold** |
| `headlineLarge` | PlayfairDisplay SemiBold | **Cairo SemiBold** |
| `headlineMedium` | PlayfairDisplay SemiBold | **Cairo SemiBold** |
| `headlineSmall` (new) | PlayfairDisplay SemiBold | **Cairo SemiBold** |
| `titleLarge` | Inter SemiBold | Cairo SemiBold (already) |
| `titleMedium` | Inter SemiBold | Cairo SemiBold (already) |
| `titleSmall` | Inter Medium | Cairo Medium (already) |
| `bodyLarge` | Inter Normal | Cairo Normal (already) |
| `bodyMedium` | Inter Normal | Cairo Normal (already) |
| `bodySmall` | Inter Normal | Cairo Normal (already) |
| `labelLarge` | Inter Medium | Cairo Medium (already) |
| `labelMedium` | Inter Medium | Cairo Medium (already) |
| `labelSmall` | Inter Medium | Cairo Medium (already) |
| `HieroglyphStyle` | NotoSansEgyptianHieroglyphs | No change (glyphs are language-independent) |
| `GardinerCodeStyle` | JetBrainsMono | No change (codes are Latin) |

## New Components Needed

### WadjetAsyncImage
- **Purpose:** Unified image loading with consistent placeholder, error, and crossfade behavior across all screens.
- **Signature:** `@Composable fun WadjetAsyncImage(url: String?, contentDescription: String?, modifier: Modifier = Modifier, placeholder: @Composable (() -> Unit)? = null)`
- **Behavior:**
  - Loading: Shows `CircularProgressIndicator(color = WadjetColors.Gold, size = 24.dp, strokeWidth = 2.dp)` centered
  - Error: Shows placeholder composable if provided, else a default Egyptian glyph (`𓂀`) on `WadjetColors.SurfaceAlt` background
  - Success: `crossfade(300)` transition
  - Uses Coil `AsyncImage` internally with `ImageRequest.Builder.crossfade(300)`
- **Usage:** ExploreScreen landmark images, LandmarkDetail hero, DashboardScreen scan thumbnails, DashboardScreen/SettingsScreen avatar, StoryReader scene images

### WadjetSearchBar
- **Purpose:** Consistent search input matching the golden design system. Currently ExploreScreen uses a raw `TextField`.
- **Signature:** `@Composable fun WadjetSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier, placeholder: String = "Search...")`
- **Appearance:**
  - `OutlinedTextField` with `RoundedCornerShape(MaterialTheme.shapes.medium)` (12dp)
  - Leading icon: `Icons.Outlined.Search` in `WadjetColors.TextMuted`
  - Trailing icon: `Icons.Outlined.Clear` in `WadjetColors.TextMuted` when `query.isNotEmpty()`
  - Colors: `focusedBorderColor = WadjetColors.Gold`, `unfocusedBorderColor = WadjetColors.Border`, `cursorColor = WadjetColors.Gold`, `containerColor = WadjetColors.Surface`
  - `singleLine = true`
  - `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)`
- **Usage:** ExploreScreen, potentially DictionaryScreen browse search

## Spacing & Layout Tokens

| Current | Standard | Notes |
|---------|----------|-------|
| `spacedBy(12.dp)` (RegisterSheet) | `spacedBy(16.dp)` | Standardize all auth sheets to 16dp |
| `padding(horizontal = 24.dp, bottom = 32.dp)` | Keep | Standard for all bottom sheets |
| `padding(horizontal = 16.dp)` | Keep | Standard horizontal margin for screen content |
| `padding(horizontal = 20.dp)` (LandingScreen) | `padding(horizontal = 16.dp)` | Standardize to 16dp, or document 20dp as Landing-specific exception |
| `spacedBy(24.dp)` (LazyColumn) | Keep | Standard vertical spacing between major sections |
| `spacedBy(12.dp)` (LazyColumn items) | Keep | Standard vertical spacing between list items |

## Shape Token Reference

After migration, all shapes should reference `MaterialTheme.shapes`:

| Radius | Token | Used For |
|--------|-------|----------|
| 8dp | `MaterialTheme.shapes.small` | Small chips, badges, inner elements |
| 12dp | `MaterialTheme.shapes.medium` | Cards, buttons, text fields, toasts, modals |
| 16dp | `MaterialTheme.shapes.large` | Featured cards, path cards, hero sections |
| 24dp | `MaterialTheme.shapes.extraLarge` | Bottom sheets, result panels, identify overlay |
| 50% / Circular | `CircleShape` (direct) | Avatars, dots, circular buttons |

## Icon System Notes

- Primary icon source: Material Icons (via `material-icons-extended`)
- Egyptian glyphs: `NotoSansEgyptianHieroglyphs` font (for display) — NOT icons
- Custom icons: None currently — `WadjetAsyncImage` error placeholder uses hieroglyph glyph text
- Recommendation: Consider creating `WadjetIcons` object (like NiA's `NiaIcons`) to centralize all icon references and enable easy swapping

## WCAG Contrast Check

| Text Color | Background | Contrast Ratio | WCAG AA |
|-----------|------------|----------------|---------|
| `Text` (#F0F0F0) | `Night` (#0A0A0A) | **18.3:1** | Pass |
| `Gold` (#D4AF37) | `Night` (#0A0A0A) | **7.5:1** | Pass |
| `TextMuted` (#8A8A8A) | `Night` (#0A0A0A) | **5.4:1** | Pass (normal) |
| `Sand` (#C4A265) | `Night` (#0A0A0A) | **7.1:1** | Pass |
| `Dust` (#8B7355) | `Night` (#0A0A0A) | **4.0:1** | Pass (large), Borderline (normal) |
| `Gold` (#D4AF37) | `Surface` (#141414) | **6.5:1** | Pass |
| `Night` (#0A0A0A) | `Gold` (#D4AF37) | **7.5:1** | Pass (button text) |
| `TextMuted` (#8A8A8A) | `Surface` (#141414) | **4.7:1** | Pass |
| `Dust` (#8B7355) | `Surface` (#141414) | **3.5:1** | Pass (large only) |

**Action items:**
- `Dust` on `Night` at 4.0:1 barely passes AA for normal text (4.5:1 required). Consider lightening to `#9A8262` for body text usage, or restrict to large text / decorative use only.
