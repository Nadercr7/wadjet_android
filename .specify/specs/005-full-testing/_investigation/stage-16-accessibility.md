# Stage 16: Accessibility — TalkBack, Font Sizes, Display Sizes

## Executive Summary

The Wadjet app has **critical accessibility gaps**. There are zero test tags, zero heading semantics, zero mergeDescendants, and only 1 semantic Role annotation in the entire codebase. TalkBack usability is very poor. The Welcome screen is **completely broken at 720×1280** — sign-in buttons are cut off with no scroll.

---

## Code-Level Accessibility Audit

### Content Description Coverage

| Metric | Count |
|--------|-------|
| Total `Icon(` / `IconButton(` / `Image(` audited | ~144 |
| With `contentDescription` | ~133 |
| With `null` contentDescription (interactive) | **7 critical** |
| With `null` (decorative, acceptable) | 4 |

**Critical Missing Content Descriptions (interactive icons):**

| # | File | Line | Element | Issue |
|---|------|------|---------|-------|
| 1 | LessonScreen.kt | ~216 | `IconButton` → `Icon(VolumeUp)` in SignRow | `contentDescription = null` on speak button |
| 2 | LessonScreen.kt | ~249 | `IconButton` → `Icon(VolumeUp)` in ExampleWordItem | `contentDescription = null` |
| 3 | LessonScreen.kt | ~281 | `IconButton` → `Icon(VolumeUp)` in PracticeWordItem | `contentDescription = null` |
| 4 | LearnTab.kt | ~157 | `IconButton` → `Icon(VolumeUp)` in sign list | `contentDescription = null` |
| 5 | StoryReaderScreen.kt | ~912 | `Icon(Check/Close/ArrowForward)` in FeedbackBanner | Conveys correct/incorrect state with no description |
| 6 | DashboardScreen.kt | ~403 | `Icon(FavoriteBorder)` in FavoriteRow | Inside interactive row |
| 7 | IdentifyScreen.kt | ~280 | `Icon(Chat)` in "Ask Thoth" button | Inside clickable |

### Touch Target Violations (< 48dp minimum)

| # | File | Line | Element | Size | Severity |
|---|------|------|---------|------|----------|
| 1 | LessonScreen.kt | ~212 | `IconButton(onSpeak)` SignRow | **24.dp** | SEVERE |
| 2 | LearnTab.kt | ~153 | `IconButton(onSpeak)` sign grid | **24.dp** | SEVERE |
| 3 | LessonScreen.kt | ~245 | `IconButton(onSpeak)` ExampleWordItem | **32.dp** | MODERATE |
| 4 | LessonScreen.kt | ~277 | `IconButton(onSpeak)` PracticeWordItem | **32.dp** | MODERATE |
| 5 | StoriesScreen.kt | ~372 | `IconButton(onToggleFavorite)` story card | **36.dp** | MILD |
| 6 | ChatScreen.kt | ~449 | `SmallFloatingActionButton` scroll-to-bottom | **40.dp** | MILD |
| 7 | LandingScreen.kt | ~371 | `Surface` quick action tile | **44.dp** | BORDERLINE |
| 8 | LandingScreen.kt | ~479 | `Surface` Thoth chat icon | **44.dp** | BORDERLINE |

### Test Tag Coverage

**ZERO `testTag` in the entire codebase.** All 22 screens and all 19 design system components have no test tags. This makes automated Compose UI testing impossible via `onNodeWithTag()`.

### Heading Semantics

**ZERO `.semantics { heading() }` annotations.** TalkBack users cannot navigate between sections using heading gestures. Should have headings on:
- All screen titles in TopAppBar
- Section headers ("Lessons", "Favorites", "Recent Scans", "Categories", etc.)
- Tab labels in DictionaryScreen
- "AI Notes" in ScanResultScreen
- Story chapter titles

### mergeDescendants

**ZERO `Modifier.semantics(mergeDescendants = true)`** on any card or list item. TalkBack reads each child text node separately, creating noisy experience. Should be on:
- Story cards (StoriesScreen)
- Landmark cards (ExploreScreen)
- Chat message bubbles (ChatScreen)
- Scan history items (ScanHistoryScreen)
- Sign grid items (LearnTab/BrowseTab)
- FavoriteRow (Dashboard)
- HieroglyphsHub grid items
- WadjetCard (design system component)

### State Description

**ZERO switches** have `stateDescription`. Found 2 Switch composables:
- SettingsScreen.kt ~L384: TTS enable/disable
- SettingsQuickDialog.kt ~L59: TTS quick toggle

### Role Annotations

**Only 1 instance** of semantic `Role`:
- DashboardScreen.kt ~L428: `.semantics { role = Role.Button }` on "Remove" text

**Missing on:** all 15 `.clickable {}` instances, WriteTab mode selectors, HieroglyphsHub filter chips.

### Clickable Without Semantics (15 instances)

| # | File | Element |
|---|------|---------|
| 1 | ChatScreen.kt ~L327 | "Clear history" text |
| 2 | ChatScreen.kt ~L337 | Conversation history item |
| 3 | ChatScreen.kt ~L672 | Edit message icon |
| 4 | ChatScreen.kt ~L696 | Retry error text |
| 5 | ChatScreen.kt ~L822 | Cancel edit text |
| 6 | StoryReaderScreen.kt ~L603 | Dismiss annotation overlay |
| 7 | LandmarkDetailScreen.kt ~L596 | Related landmark card |
| 8 | LandmarkDetailScreen.kt ~L654 | Child landmark card |
| 9 | IdentifyScreen.kt ~L271 | "Ask Thoth" button area |
| 10 | WriteTab.kt ~L95 | Mode selector tabs |
| 11 | SettingsScreen.kt ~L225 | "Send feedback" text |
| 12 | SettingsScreen.kt ~L239 | "Sign out" text |
| 13 | LearnTab.kt ~L119 | Sign grid item |
| 14 | ScanResultScreen.kt ~L409 | AI notes expand/collapse |
| 15 | ImageUploadZone.kt ~L85,147 | Upload zone tap areas |

### Live Regions

**Present (2):**
- WadjetToast.kt: `liveRegion = LiveRegionMode.Polite`
- OfflineIndicator.kt: `liveRegion = LiveRegionMode.Polite`

**Missing (should have):**
- Chat streaming messages (new messages appear dynamically)
- Loading/error banners in scan, stories, explore
- StreamingDots composable
- FeedbackBanner in StoryReaderScreen
- ScanResultScreen glyphs loading

### Custom Actions

**ZERO** `customActions` in semantics. Complex interaction surfaces that need them:
- Chat message long-press actions (edit, copy, retry)
- Story card (favorite + navigate = 2 actions on one item)
- Sign detail sheet (speak, copy, share, favorite = 4 actions)

### Reduced Motion

**ZERO animations check `AccessibilityManager.isEnabled`** or `LocalReducedMotionEnabled`. All 9+ infinite animations (DotPattern, ShineSweep, StreamingDots, GlyphShimmer, etc.) continue even when user has "Remove animations" enabled in Android accessibility settings.

---

## Emulator-Based Accessibility Tests

### TalkBack Audit

With TalkBack enabled on the Welcome screen, the UI dump shows **only 1 content description**: `"Wadjet logo"`. None of the 3 feature cards (Scan, Dictionary, Explore), sign-in buttons, or the tagline have content descriptions readable by TalkBack. The screen is essentially **silent** to a blind user except for text nodes.

**TalkBack reading order on Welcome:**
1. "Wadjet logo" (image) — ✅ has content-desc
2. "WADJET" (text) — read as text
3. "Decode the Secrets of Ancient Egypt" (text) — read as text
4. Scan card icon — ❌ no description (hieroglyph Unicode character)
5. "Scan" (text)
6. "Decode hieroglyphs" (text)
7. Dictionary card icon — ❌ no description
8. "Dictionary" / "1,000+ signs" (text)
9. Explore card icon — ❌ no description
10. "Sign in with Google" — read as text
11. "Sign up with Email" — read as text
12. "Already have an account? Sign in" — read as text
13. "Built by Mr Robot" — read as text

**Key issues:** The hieroglyph unicode icons (𓁀, 𓃭, 𓂀) render as empty/unknown for TalkBack. They need `contentDescription` to say "Scan icon", "Dictionary icon", "Explore icon".

### Font Scale 1.5x Testing

| Screen | Text Visible? | Layout OK? | Bottom Nav Visible? | Issues |
|--------|--------------|-----------|--------------------|---------| 
| Welcome | ✅ All text shown | ✅ | N/A (no nav) | None found |
| Landing (Home) | ✅ "Welcome back", cards, bullets | ✅ | ✅ All 5 tabs | None found |
| Hieroglyphs | ✅ Title, description, signs, tools | ✅ | ✅ All 5 tabs | None found |
| Explore | ✅ Search, filters, landmarks | ✅ | ✅ 4 visible (Thoth cut?) | "Thoth" may be slightly clipped in bottom nav |
| Stories | ✅ Filter chips, story cards | ✅ | ✅ 3 visible | Bottom nav shows "Home, Hieroglyphs" only — other tabs exist but text cut |
| Thoth (Chat) | ✅ Intro message, suggestions, input | ✅ | ✅ All 5 tabs | None found |

**Bottom nav at 1.5x:** The tab labels get clipped/truncated as the font grows. On narrower views (after navigation), not all 5 tab labels are fully visible in the UI dump. Icons should remain tappable even if labels clip. **This is a MODERATE issue** — Material guidelines recommend shorter labels or icon-only bottom nav at large font.

### Small Display (720×1280)

**CRITICAL ISSUE: Welcome screen is BROKEN at 720×1280.**

Content visible:
- ✅ WADJET title
- ✅ "Decode the Secrets of Ancient Egypt"
- ✅ Scan card
- ✅ Dictionary card  
- ✅ Explore card (partial)
- ❌ "Sign in with Google" — **MISSING / below fold**
- ❌ "Sign up with Email" — **MISSING / below fold**
- ❌ "Already have an account? Sign in" — **MISSING / below fold**
- ❌ "Built by Mr Robot" — **MISSING / below fold**

**The view is NOT scrollable** (`scrollable="false"` on all nodes). A user on a small phone **cannot log in** because the sign-in buttons are cut off with no way to scroll down.

**Root cause:** WelcomeScreen likely uses a fixed `Column` without `verticalScroll` modifier, and the hero area + cards consume the entire viewport at small sizes.

**Fix:** Add `Modifier.verticalScroll(rememberScrollState())` to the root Column, or use LazyColumn.

### Large Density (500dpi)

| Metric | Result |
|--------|--------|
| All text visible? | ✅ Yes |
| Sign-in buttons visible? | ✅ Yes |
| Layout broken? | No |
| Bottom nav clipping? | Not tested (Welcome screen shown) |

500dpi works correctly — all Welcome screen elements including sign-in are visible. The higher density (larger DPI = larger virtual pixels) actually reduces the amount of content needed per screen area, so content fits better than at small resolution.

### Configuration Change State Loss

Both `wm size` and `wm density` changes trigger activity recreation. Both caused **session loss** — the user was logged out and returned to the Welcome screen. This suggests one of:
1. Token is stored in memory-only (not persisted to DataStore/Prefs during config change)  
2. Activity recreation doesn't properly restore auth state from persistent storage

**This is likely a test artifact** (adb wm changes are more aggressive than real device config changes), but worth verifying process death handling does restore auth state.

---

## Summary of All Accessibility Issues (Prioritized)

### P0 — Critical
| # | Issue | Impact |
|---|-------|--------|
| 1 | Welcome screen broken at 720×1280 — no scroll, sign-in buttons cut off | Users **cannot log in** on small phones |
| 2 | Touch targets 24dp on speak buttons (LessonScreen, LearnTab) | Cannot tap on accessibility-aware devices |
| 3 | Zero testTags in entire codebase | Cannot write any Compose UI tests |

### P1 — High
| # | Issue | Impact |
|---|-------|--------|
| 4 | 7 interactive icons with null contentDescription | TalkBack users can't understand speak/action buttons |
| 5 | Zero heading() semantics on any screen | TalkBack navigation by headings impossible |
| 6 | Zero mergeDescendants on cards/list items | TalkBack reads every child separately (noisy) |
| 7 | 15 clickable elements without Role or description | TalkBack announces wrong interaction type |
| 8 | Hieroglyph unicode icons have no contentDescription | TalkBack reads nothing for decorative hieroglyphs used as icons |
| 9 | Bottom nav labels clip at 1.5x font | Tab names truncated |

### P2 — Medium
| # | Issue | Impact |
|---|-------|--------|
| 10 | Zero reduced-motion support for 9+ infinite animations | Accessibility users see constant motion |
| 11 | 2 Switch composables without stateDescription | Screen reader doesn't announce toggle state |
| 12 | Missing liveRegion on dynamic content (chat, loading) | Screen reader misses updates |
| 13 | Touch targets 32-40dp on several IconButtons | Below 48dp minimum |

### P3 — Low
| # | Issue | Impact |
|---|-------|--------|
| 14 | Only 1 Role annotation in entire codebase | Most clickables don't declare their role |
| 15 | Zero customActions for multi-action items | Complex items hard to use with TalkBack |
| 16 | 16KB alignment warning on API 37 emulator | Not a user-facing issue but should fix for future |
