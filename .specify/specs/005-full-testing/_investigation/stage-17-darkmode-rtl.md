# Stage 17: Dark Mode and RTL (Arabic) Full Pass

## Executive Summary

The app is **dark-only by design** — no light color scheme exists. Dark mode "just works" because it's the only mode. For RTL/Arabic: layout patterns are clean and Cairo font is fully wired, but there are translation gaps in the chat module and the system locale couldn't be changed via ADB without reboot, so live RTL verification was limited to code analysis.

---

## Dark Mode Audit

### Theme Architecture

| File | What It Contains |
|------|-----------------|
| WadjetColors.kt | 21 colors in a centralized `object` |
| WadjetTheme.kt | **Dark-only** — uses `darkColorScheme()` exclusively |
| WadjetTypography.kt | 15 text styles, Arabic swap via `wadjetTypographyForLang()` |
| WadjetFonts.kt | 5 font families (PlayfairDisplay, Inter, JetBrainsMono, NotoSansEgyptianHieroglyphs, Cairo) |

**NO `lightColorScheme` defined. NO `isSystemInDarkTheme()` call.** The app is forced dark-mode-only.

### Color Usage Pattern

**All feature code uses `WadjetColors.*` directly** instead of `MaterialTheme.colorScheme.*`:

| Module | ~WadjetColors.* uses |
|--------|----------------------|
| feature/chat/ | ~55 |
| feature/stories/ | ~50 |
| feature/scan/ | ~20 |
| feature/explore/ | ~15 |
| feature/settings/ | ~10 |
| feature/auth/ | ~5 |
| core/designsystem/component/ | ~50 |
| app/ | ~30 |
| **TOTAL** | **~235** |

**Impact**: Since the app is dark-only, this works fine today. But if a light theme is ever added, all ~235 `WadjetColors.*` references would need to change to `MaterialTheme.colorScheme.*`.

### Hardcoded Colors in UI Code

| Pattern | Count | Status |
|---------|-------|--------|
| `Color(0x...)` in UI files | **0** | CLEAN |
| `Color.White/Black/Gray/Red/...` | **0** | CLEAN |
| `Color.Transparent` | 12 (across animations, scan, explore) | ACCEPTABLE (theme-agnostic) |

**ZERO hardcoded color violations in UI code** — all colors come from `WadjetColors.*`.

### Dark Mode Emulator Test

| Screen | Renders? | Notes |
|--------|----------|-------|
| Welcome | ✅ | All text and buttons visible. Same as normal (dark-only). |
| System "night yes" | ✅ | No visual change — app is already dark. |
| System "night no" | ✅ | No visual change — app ignores system night mode. |

**The app does not respond to Android system dark/light preference.** It's always dark.

### Compose Previews

| Finding | Status |
|---------|--------|
| `@Preview` functions in codebase | **ZERO** |
| `@WadjetPreviews` annotation | Defined in WadjetPreviews.kt (Phone, Landscape, Tablet, Dark, RTL, Large Text) |
| Usage of `@WadjetPreviews` | **Never used anywhere** |

No screen or component has any preview function — cannot visually verify anything in Android Studio.

### Dark Mode Issues Summary

| # | Issue | Severity | Impact |
|---|-------|----------|--------|
| 1 | No light theme — dark-only app | INFO | Intentional design decision, but doesn't follow system preference |
| 2 | All UI uses `WadjetColors.*` directly (~235 uses) | P2 | Blocks future light theme addition; ~235 lines to update |
| 3 | `WadjetDarkColorScheme` mapping is essentially unused | P3 | The `darkColorScheme()` call maps colors but feature code bypasses it |
| 4 | Zero `@Preview` functions | P2 | Cannot verify dark/light in Android Studio |
| 5 | `WadjetTypography.kt` L105/L112 hardcode color in HieroglyphStyle/GardinerCodeStyle | P3 | Minor — these are glyph-specific semantic colors |

---

## RTL (Arabic) Audit

### Android Manifest

✅ `android:supportsRtl="true"` is set in AndroidManifest.xml.

### Arabic String Resources

| Module | `values-ar/strings.xml` Exists? | Quality |
|--------|------|---------|
| app | ✅ | Arabic translations present |
| feature/auth | ✅ | Needs full verification |
| feature/chat | ✅ | **BROKEN — English strings, not Arabic** |
| feature/dashboard | ✅ | Needs verification |
| feature/dictionary | ✅ | Needs verification |
| feature/explore | ✅ | Needs verification |
| feature/feedback | ✅ | Needs verification |
| feature/landing | ✅ | Needs verification |
| feature/scan | ✅ | Needs verification |
| feature/settings | ✅ | Needs verification |
| feature/stories | ✅ | Needs verification |
| core/designsystem | ✅ | Needs verification |

**Critical issue**: `feature/chat/src/main/res/values-ar/strings.xml` contains **English strings** with `tools:ignore="MissingTranslation"`. Arabic suggestion chips (`chat_suggestion_pyramids_ar`, etc.) have UTF-8 mojibake (garbled Arabic).

### Arabic Typography

| Check | Status |
|-------|--------|
| Cairo font bundled (4 weights) | ✅ YES |
| `wadjetTypographyForLang("ar")` | ✅ Swaps ALL 15 text styles to Cairo |
| Wired into WadjetTheme | ✅ Via `ConfigurationCompat.getLocales()` |
| Display/Headline styles swapped | ✅ YES |
| Body/Title/Label styles swapped | ✅ YES |

**Arabic typography: FULLY IMPLEMENTED** ✅

### RTL Layout Patterns

| Pattern | Found | Status |
|---------|-------|--------|
| `TextAlign.Left` / `TextAlign.Right` | **0** | ✅ CLEAN |
| `padding(left=)` / `padding(right=)` | **0** | ✅ CLEAN |
| `Modifier.offset(x=)` | **0** | ✅ CLEAN |
| `Arrangement.Start` / `Arrangement.End` | 3 (ChatScreen) | ✅ RTL-aware |
| `Alignment.Start` / `Alignment.End` | 3 (LoginSheet, ForgotPasswordSheet, ChatScreen) | ✅ RTL-aware |
| Forced `LayoutDirection.Ltr` | **0** | ✅ CLEAN |
| Forced `LayoutDirection.Rtl` | 1 (ScanResultScreen L361 — intentional for Arabic text block) | ✅ INTENTIONAL |

**No RTL-breaking layout patterns found.** ✅

### Icon Auto-Mirroring

**AutoMirrored (correct):**
- `Icons.AutoMirrored.Filled.ArrowBack` — StoryReader, Stories, Settings, LandmarkDetail, Explore
- `Icons.AutoMirrored.Filled.ArrowForward` — StoryReader
- `Icons.AutoMirrored.Filled.VolumeUp` — StoryReader
- `Icons.AutoMirrored.Filled.Chat` — LandmarkDetail

**Non-mirrored (symmetric — correct):**
- Check, Close, Stop, Replay, Favorite, Lock, Map, Share, Edit, History, FileUpload, etc.

**Icon mirroring: FULLY CORRECT** ✅

### Canvas / drawBehind RTL Safety

| Location | Usage | RTL Risk |
|----------|-------|----------|
| ScanScreen.kt L204 | Camera overlay Canvas | LOW — absolute positioning |
| ScanScreen.kt L391 | drawBehind | LOW — visual effect |
| WadjetToast.kt L136 | drawBehind | LOW — edge glow |
| ShimmerEffect.kt L33 | drawBehind | LOW — symmetric shimmer |
| Animations (ButtonShimmer, GoldGradientSweep, ShineSweep) | drawWithContent | LOW — sweep effects |

### Live RTL Testing

**BLOCKED**: Could not change system locale via `adb shell settings put system system_locales ar-EG` — requires full emulator reboot to take effect. The `setprop persist.sys.locale ar-EG; setprop ctl.restart zygote` approach also didn't propagate without reboot.

**Mitigation**: Code-level audit shows all layout patterns are RTL-safe. The `@WadjetPreviews` annotation includes an RTL variant but is never used — **adding Compose previews would be the best way to verify RTL rendering visually.**

---

## RTL Issues Summary

| # | Issue | Severity | Impact |
|---|-------|----------|--------|
| 1 | Chat `values-ar/strings.xml` contains **English text, not Arabic** | P1 | Arabic users see English in chat UI |
| 2 | Chat Arabic suggestion chips have UTF-8 mojibake | P2 | Garbled Arabic in chat suggestions |
| 3 | `@WadjetPreviews` (includes RTL variant) is never used | P2 | Can't verify RTL in Android Studio |
| 4 | No live RTL testing possible via ADB (needs reboot) | INFO | Code analysis shows clean patterns |

---

## Combined Action Items

### P1 — High Priority
| # | Issue | Effort |
|---|-------|--------|
| 1 | Fix `feature/chat/values-ar/strings.xml` — translate to actual Arabic | ~1 hour |
| 2 | Fix chat Arabic suggestion chips UTF-8 encoding | ~30 min |

### P2 — Medium Priority
| # | Issue | Effort |
|---|-------|--------|
| 3 | Add `@WadjetPreviews` to all 22 screens | ~2-3 hours |
| 4 | Migrate ~235 `WadjetColors.*` to `MaterialTheme.colorScheme.*` (if adding light theme) | ~4-6 hours |

### P3 — Low Priority
| # | Issue | Effort |
|---|-------|--------|
| 5 | Add `isSystemInDarkTheme()` support with light color scheme | ~4 hours |
| 6 | Remove unused `WadjetDarkColorScheme` mapping or wire feature code through it | ~4-6 hours |
