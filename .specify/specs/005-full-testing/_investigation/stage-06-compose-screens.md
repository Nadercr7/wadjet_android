# Stage 6 — Compose Screens Audit

**Date:** 2025-07-22  
**Auditor:** Automated (Copilot)  
**Scope:** 26 Compose screen files (18 main screens + 8 sub-screens/sheets/tabs)

---

## Summary

| Metric | Value |
|---|---|
| **Screen files audited** | 26 |
| **testTag values found** | **ZERO** across entire codebase |
| **@Preview functions found** | **ZERO** across entire codebase |
| **Accessibility violations** | 11+ (null contentDescription, tiny touch targets) |
| **Hardcoded strings** | 4 instances |
| **Screens with proper state handling** | ~15/18 (Loading + Error + Empty + Success) |

---

## Global Findings

### P0: ZERO testTags
Not a single `Modifier.testTag(...)` exists in any screen. Automated UI testing via Compose test rules is **impossible** without first adding testTags to all interactive elements.

### P0: ZERO @Preview Functions
No screen has any `@Preview` composable. Design iteration in Android Studio preview pane is impossible. The `WadjetPreviews` multi-preview annotation exists in `core/ui` but is never used.

### Color System
All screens use `WadjetColors.*` (custom theme object) rather than `MaterialTheme.colorScheme.*`. This is intentional (dark Egyptian theme) but breaks Material You dynamic color. **Exceptions:** LoginSheet, RegisterSheet, ForgotPasswordSheet use `MaterialTheme.colorScheme.error` for error text — **inconsistent** with all other screens using `WadjetColors.Error`.

---

## ScanScreen vs IdentifyScreen — THE CONFUSION ISSUE

| Aspect | ScanScreen | IdentifyScreen |
|---|---|---|
| **Title text** | `R.string.scan_title` | `R.string.identify_title` |
| **Upload component** | `ImageUploadZone` (shared) | `ImageUploadZone` (shared) |
| **Upload icon** | Eye of Horus (𓂀) 48sp gold | Eye of Horus (𓂀) 48sp gold |
| **Upload title** | `R.string.scan_upload_title` | `R.string.identify_upload_title` |
| **Upload subtitle** | `R.string.scan_upload_subtitle` | `R.string.identify_upload_subtitle` |
| **Background** | `WadjetColors.Night` | `WadjetColors.Night` |
| **TopBar actions** | History icon | None |
| **Loading state** | `ScanProgressOverlay` (4-step pipeline) | Simple `WadjetSectionLoader` |
| **Result** | Navigates to ScanResultScreen | Inline results bottom sheet |

**Root cause:** `ImageUploadZone` has no parameter to customize the icon. Both screens show the **exact same visual** (Eye of Horus on dark background). The only differences are small title text changes. A user cannot visually distinguish them.

**Explore toolbar button:** Uses `Icons.Default.FileUpload` to reach Identify — a generic upload icon, not a landmark/identify icon.

---

## Per-Screen State Handling

| # | Screen | Loading | Error | Empty | Issues |
|---|---|---|---|---|---|
| 1 | WelcomeScreen | `isLoading` disables buttons | Inline error text | N/A | Google loading is local state, not in VM |
| 2 | LandingScreen | `ShimmerCardList` | `ErrorState` | N/A | Hardcoded chapter text English |
| 3 | HieroglyphsHubScreen | `isLoading` flag | **NONE** | Sections hidden | **No error state at all** |
| 4 | ScanScreen | `ScanProgressOverlay` (animated) | `ErrorState` | N/A | Dead PermissionDenied code |
| 5 | ScanResultScreen | None (renders directly) | None | N/A | No explicit loading/error |
| 6 | ScanHistoryScreen | `ShimmerCardList` | `ErrorState` + retry | `EmptyState` | Swipe-to-delete fires before undo |
| 7 | DictionaryScreen | Delegates to tabs | Delegates to tabs | Delegates | Translate tab still present |
| 8 | DictionarySignScreen | `CircularProgressIndicator` | Text with error | N/A | **No retry button on error** |
| 9 | LessonScreen | `WadjetSectionLoader` | `ErrorState` + retry | N/A | Nested LazyVerticalGrid in LazyColumn |
| 10 | ExploreScreen | `ShimmerCardList` | `ErrorState` overlay | `EmptyState` | Good state handling |
| 11 | LandmarkDetailScreen | `ShimmerDetail` | `ErrorState` | N/A | Good state handling |
| 12 | IdentifyScreen | `WadjetSectionLoader` | `ErrorState` | No-match state | Good state handling |
| 13 | ChatScreen | `StreamingDots` | Toast (auto-dismiss) | `EmptyState` | Error via toast only |
| 14 | StoriesScreen | `ShimmerCardList` in LazyColumn | `ErrorState` | `EmptyState` | Unused `animateColorAsState` |
| 15 | StoryReaderScreen | Plain `Text("Loading...")` | Toast | Completion screen | **Loading is just plain text** |
| 16 | DashboardScreen | PullToRefresh | `ErrorState` in LazyColumn | `EmptyState` (favs) | TODO placeholders |
| 17 | SettingsScreen | `isChangingPassword` flag | Snackbar | N/A | No inline error display |
| 18 | FeedbackScreen | `isSubmitting` on button | Inline text | N/A | Success state has no back |

---

## Accessibility Violations

### contentDescription = null on Interactive Icons

| Screen | Element | Violation |
|---|---|---|
| HieroglyphsHubScreen | All 3 HubCard tool icons (CameraAlt, MenuBook, Edit) | `contentDescription = null` |
| LessonScreen | ALL VolumeUp TTS icons | `contentDescription = null` |
| LearnTab | ALL VolumeUp TTS icons | `contentDescription = null` |
| ChatScreen | Ask Thoth icon | `contentDescription = null` |
| DashboardScreen | FavoriteBorder icon | `contentDescription = null` |
| ForgotPasswordSheet | Hieroglyph "𓂋" | No contentDescription |

### Touch Targets Below 48dp

| Screen | Element | Actual Size | Minimum |
|---|---|---|---|
| LessonScreen | TTS button | **24dp** (icon 14dp) | 48dp |
| LearnTab | TTS button | **24dp** (icon 16dp) | 48dp |
| ChatScreen | Edit message icon | **14dp** | 48dp |
| ChatScreen | History item icon | **16dp** | 48dp |
| LandingScreen | QuickAction icon | **44dp** | 48dp |
| HieroglyphsHubScreen | ExplainerStep number | **22dp** | 48dp |

---

## Hardcoded English Strings

| File | String | Notes |
|---|---|---|
| ScanScreen.kt | "Camera Permission Required", "Grant Permission" | Dead code (PermissionDeniedContent unused) |
| LandingScreen.kt | "Chapter ${chapter + 1} of ${story.chapterCount}" | In ContinueStoryCard |
| DashboardScreen.kt | Hardcoded glyph "𓀀" in ScanCard | Has TODO comment |
| DashboardScreen.kt | StoryProgressRow divisor `5f` | Has TODO comment |

---

## UX Issues from Code

| # | Screen | Issue | Severity |
|---|---|---|---|
| 1 | Scan + Identify | Both use identical `ImageUploadZone` — visually indistinguishable | **HIGH** |
| 2 | ExploreScreen | `FileUpload` icon for Identify action — confusing metaphor | **HIGH** |
| 3 | DictionaryScreen | Still has 4 tabs including Translate (spec says remove) | **MEDIUM** |
| 4 | HieroglyphsHubScreen | No error state rendering at all | **HIGH** |
| 5 | DictionarySignScreen | Error shows raw text, no retry button | **MEDIUM** |
| 6 | StoryReaderScreen | Loading state is just `Text("Loading...")` | **MEDIUM** |
| 7 | ScanHistoryScreen | Swipe delete fires before undo confirmation | **MEDIUM** |
| 8 | FeedbackScreen | After success, no way to navigate back or reset form | **LOW** |
| 9 | ScanScreen | ~100 lines of dead camera/permission code | **LOW** |
| 10 | StoriesScreen | Unused `animateColorAsState` / dead code for `bgColor` | **LOW** |

---

## Recomposition Concerns

| Screen | Issue | Impact |
|---|---|---|
| LandingScreen | `visibleSections` incremented per section → many recomps | LOW — one-time animation |
| StoriesScreen | `visibleCount` incremented per item → recomps | LOW — one-time stagger |
| ScanResultScreen | `showArabic` toggle recomps entire translation section | LOW |
| LandingScreen | LazyColumn `item {}` blocks without keys | MEDIUM — may cause index-based issues |
| LessonScreen | Nested `LazyVerticalGrid` inside `LazyColumn` | MEDIUM — known anti-pattern |
