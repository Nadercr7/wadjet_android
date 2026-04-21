# Stage 2: UX Walkthrough

## Screen Inventory (from live emulator)

| # | Screen | Route | Reachable? | Elements | Key Issues |
|---|--------|-------|------------|----------|------------|
| 1 | Welcome | Route.Welcome | YES | WADJET title, feature cards (Scan, Dictionary, Explore), Sign-in Google, Sign up Email, "Already have account? Sign in" link, "Built by Mr Robot" | Feature cards lack content-desc. No resource-ids. |
| 2 | Login Sheet | (BottomSheet) | YES via "Sign in" | Title "Sign In", Email field, Password field, Sign In button, Forgot password?, Google sign-in, "Create one" link | Works correctly. Login succeeded with test account. |
| 3 | Landing/Home | Route.Landing | YES (after auth) | "Welcome back" + WADJET title, Hieroglyphs card (scan/translate/dictionary/write bullets), "Start Scanning" CTA, Landmarks card, Quick Actions (Scan, Dictionary, Write, Explore, Identify, Stories), Thoth chat preview | Top bar has Profile + Settings icons (content-desc). Cards are NOT clickable (clickable=false on TextViews). Quick Action cards work. |
| 4 | HieroglyphsHub | Route.Hieroglyphs | YES via bottom nav | Title, Dictionary info (1023 signs), Suggested Signs (A43A, A44, A45), Tools section (Scan Hieroglyphs, Dictionary, Write in Hieroglyphs) | Good layout. Tool cards are interactive. |
| 5 | Explore | Route.Explore | YES via bottom nav | Search bar, Category chips (All/Coptic/Greco-Roman/Islamic/Modern), City chips (All Cities/Alexandria/Aswan/Beheira...), Featured section, All Landmarks list | Has "Identify from photo" icon in top-right (content-desc="Identify from photo") — THE UX ISSUE |
| 6 | LandmarkDetail | Route.LandmarkDetail | YES via tap landmark | Title, Arabic name, tags (Pharaonic/Giza/Old Kingdom/4th Dynasty), Maps/Chat/Save buttons, Overview/History/Tips/Gallery tabs, description text | Rich content. Back + Share in top bar. |
| 7 | Identify | Route.Identify | YES via Explore header icon | Eye of Horus icon, "Upload a photo of an Egyptian landmark", file size info, "Browse Gallery" button | SMALL access point — only 63×63 icon with content-desc. NO icon label visible. |
| 8 | Stories | Route.Stories | YES via bottom nav | Filter chips (All/Beginner/Intermediate/Advanced), Story cards with glyph, title, difficulty, glyph count, duration, chapter count. "Premium" badges. | Clean layout. Cards well-structured. |
| 9 | Chat (Thoth) | Route.Chat | YES via bottom nav | Thoth welcome message, suggested prompts ("Tell me about the pyramids", "What are hieroglyphs?", "Famous pharaohs"), message input, voice input icon, send icon | Content-descs: Listen, Voice input, Send, Back, Clear chat. Good. |
| 10 | Dictionary | Route.Dictionary | YES via Hub→Dictionary | 4 tabs (Browse/Learn/Write/Translate), Search bar, Category chips, Type chips, Sign grid (glyph + code + description) | Well-structured. Many signs loaded. |
| 11 | Scan | Route.Scan | YES via Hub→Scan Hieroglyphs | Eye of Horus icon, "Tap to select a hieroglyph image", file size info, "Browse Gallery" button, History icon | History access via top-right icon (content-desc="History") |
| 12 | ScanHistory | Route.ScanHistory | YES via Scan→History icon | Empty state: Eye of Horus, "No scans yet", "Scan hieroglyphs to see them here" | Clean empty state. |
| 13 | Dashboard | Route.Dashboard | YES via Profile icon | Username/email, stats grid (Scans Today/Total Scans/Stories Done/Glyphs Learned), Favorites tabs (Landmark/Glyph/Story), empty state for favorites | Well-structured. "E" avatar initial visible (possibly truncated). |
| 14 | Settings (Quick) | (Dialog) | YES via Settings icon | Quick Settings: Enable TTS toggle, Cache (0 MB), Cancel/Full Settings | Smart dialog pattern. |
| 15 | Settings (Full) | Route.Settings | YES via Quick→Full Settings | Profile (name/email), TTS settings (enable/speed), Storage (cache/clear), About (version/built by), Account section | Clean layout. |
| 16 | Feedback | Route.Feedback | via Settings→Send Feedback | Not visited in walkthrough (accessible from Settings) | — |
| 17 | ScanResult | Route.ScanResult | Requires scan | Not tested (needs real image upload) | — |
| 18 | StoryReader | Route.StoryReader | Requires story tap | Not tested in this walkthrough | — |
| 19 | DictionarySign | Route.DictionarySign | Requires sign tap | Not tested in this walkthrough | — |
| 20 | Lesson | Route.Lesson | Requires Learn tab selection | Not tested in this walkthrough | — |

## UX Issues Master List

| # | Screen | Severity | Issue | What It Looks Like Now | Suggested Fix |
|---|--------|----------|-------|------------------------|---------------|
| 1 | Explore | **HIGH** | "Identify from photo" button is a tiny 63×63 icon in the top-right with NO visible label — only content-desc="Identify from photo" | Small camera-like icon at [975,485][1038,548], easy to miss | Add a visible "Identify" label below the icon, increase to 48dp+, or add a prominent card/FAB |
| 2 | Scan + Identify | **HIGH** | Scan screen and Identify screen look almost identical: same Eye of Horus (𓂀) icon, same upload-style layout, same "Browse Gallery" button | Scan: "Tap to select a hieroglyph image" / Identify: "Upload a photo of an Egyptian landmark" — text differs but layout identical | Use different icons: Camera/scanner for Scan, Landmark/building for Identify. Different background colors/themes per screen. |
| 3 | Landing | **MEDIUM** | "Start Scanning" and "Start Exploring" CTA buttons at bottom of cards are not discoverable without scrolling — cards show content but CTAs are below the fold | Cards fill the screen with bullet points, CTA is at the bottom | Either add the CTA at card top or make the entire card tappable |
| 4 | Landing | **MEDIUM** | All TextViews on cards show `clickable=false` — the cards might be clickable at a parent View level, but individual elements give no clickable affordance to accessibility | Screen mapper shows zero clickable elements besides the 3 bottom generic Views | Ensure card containers have proper click semantics propagated |
| 5 | Landing | **LOW** | "Translate — English & Arabic translation" listed as feature bullet on Hieroglyphs card, but the Translate tab was removed (only 3 tabs now: Browse, Learn, Write) | Text says "Translate" but feature no longer exists separately | Update landing card bullets to reflect current 3-tab structure |
| 6 | Dictionary | **LOW** | "Translate" tab still visible as 4th tab in Dictionary screen (bounds [866,172][1025,217]) | Shows Browse/Learn/Write/Translate | Verify if Translate was truly removed per spec 002 or just deprioritized |
| 7 | Welcome | **LOW** | "Built by Mr Robot" shown at bottom of Welcome screen — unprofessional for a production app | Plain text at bottom | Remove or change to "Made with ❤️ by Wadjet Team" or similar |
| 8 | Dashboard | **LOW** | Avatar shows "E" (first letter of... what? Email character "n"? Or "Explorer" role?) — may be truncated | "E" visible at [97,509][135,586], then "Explorer" text next to it | This is the role label "Explorer" — the "E" might be the avatar initial. Verify it shows correct user initial. |
| 9 | All Screens | **MEDIUM** | Zero `resource-id` attributes on any Compose element — all are `android.view.View` with no IDs | UI Automator can't find elements by ID, only by text or content-desc | Add `testTag` modifiers to key interactive elements for testability |
| 10 | All Screens | **MEDIUM** | Most interactive elements lack `content-desc` — only a few icons have it (Profile, Settings, Back, etc.) | Buttons like "Start Scanning", "Browse Gallery" have no content-desc | Add `contentDescription` to all actionable composables |

## Accessibility Issues

| # | Screen | Element | Issue | Fix |
|---|--------|---------|-------|-----|
| 1 | All | All Compose Views | No `resource-id` — UIAutomator cannot identify elements | Add `testTag` to all interactive composables |
| 2 | All | Most buttons/cards | Missing `contentDescription` | Add descriptive content descriptions |
| 3 | Explore | Identify icon | 63×63px (~21dp at 3x) — below 48dp minimum touch target | Increase to 48dp+ |
| 4 | Landing | Feature cards | TextViews inside cards show `clickable=false` — screen readers may skip | Ensure click handler is on semantics-proper node |
| 5 | Explore | Favorite heart icon | Icon at [928,1516][981,1569] = 53x53px (~18dp) | Increase touch target to 48dp |

## Navigation Issues

| # | From | To | Issue |
|---|------|----|-------|
| 1 | Any screen | Back | Back button works correctly via KEYCODE_BACK |
| 2 | Welcome | Login | "Already have account? Sign in" → opens Login bottom sheet |
| 3 | Hub | Dictionary | Tapping card navigates correctly |
| 4 | Hub | Scan | Tapping "Scan Hieroglyphs" card navigates correctly |
| 5 | Explore | Identify | Only accessible via tiny top-right icon — no other path |
| 6 | Landing | Quick Actions | All 6 quick action cards (Scan/Dictionary/Write/Explore/Identify/Stories) work |

## Missing States

| # | Screen | Missing State | Notes |
|---|--------|---------------|-------|
| 1 | Landing | Error state | What if API call to load data fails? |
| 2 | Landing | Offline state | No offline indicator visible |
| 3 | Explore | Error state | Not tested — what if landmarks fail to load? |
| 4 | Dictionary | Empty search | Not tested — what if search returns 0 results? |
| 5 | ScanResult | Not tested | Requires actual image upload |
| 6 | StoryReader | Not tested | Requires tapping a story |
| 7 | Chat | Error during streaming | Not tested |
| 8 | Identify | Upload failure | Not tested |

## Logcat Errors During Walkthrough

| Error | Screen | Severity | Notes |
|-------|--------|----------|-------|
| `GoogleApiManager: Failed to get service from broker. SecurityException: Unknown calling package name 'com.google.android.gms'` | Global (post-login) | LOW | Emulator-specific GMS issue. Not a real device problem. |
| No app crashes detected | All screens | OK | App stable throughout walkthrough |

## Emulator Evidence Captured

Screenshots pulled to `_investigation/screenshots/005-evidence/`:
- stage02-welcome.png
- stage02-landing.png
- stage02-explore.png
- stage02-stories.png
- stage02-chat.png
- stage02-dictionary-browse.png
- stage02-settings-full.png
- stage02-dashboard.png
- stage02-landmark-detail.png
- stage02-identify.png
- stage02-scan.png
- stage02-scan-history.png
# Stage 2: UX Walkthrough

## Emulator Setup
- **Device**: Pixel_8 (API 37, x86_64)
- **Emulator ID**: emulator-5554
- **Animations**: Disabled (all 3 animation scales = 0)
- **App**: com.wadjet.app installed via `gradlew installDebug`

## Screen Inventory (from live emulator)

**NOTE**: App requires authentication. Without a valid account, only the Welcome screen and auth sheets are accessible. Full walkthrough of authenticated screens requires manual login or mock auth setup.

| # | Screen | Route | Reachable (Emulator)? | Element Count | Issues |
|---|--------|-------|-----------------------|---------------|--------|
| 1 | Welcome | Route.Welcome | YES | 39 elements, 3 interactive | No "Skip" / Guest mode. All elements are clickable views with no resource-ids or testTags |
| 2 | Register Sheet | (bottom sheet) | YES | Fields: Display Name, Email, Password, Confirm Password, Create Account btn, Google sign-up | No testTags visible in UI hierarchy |
| 3 | Login Sheet | (bottom sheet) | YES | Fields: Email, Password, Sign In btn, Forgot Password link, Google sign-in | No testTags visible |
| 4 | Forgot Password Sheet | (bottom sheet) | NOT TESTED (requires opening from login) | — | — |
| 5-20 | All auth-gated screens | Various | NOT ACCESSIBLE | — | Requires valid auth token |

## Welcome Screen Analysis

### Elements Found (from UI hierarchy dump):
| Element | Type | Text | Clickable? | Content Description |
|---------|------|------|------------|---------------------|
| Logo image | ImageView | — | No | "Wadjet logo" ✓ |
| Title | TextView | "WADJET" | No | — |
| Subtitle | TextView | "Decode the Secrets\nof Ancient Egypt" | No | — |
| Scan card | View (group) | "Scan" + "Decode hieroglyphs" + hieroglyph unicode | No (not clickable!) | — |
| Dictionary card | View (group) | "Dictionary" + "1,000+ signs" + hieroglyph unicode | No (not clickable!) | — |
| Explore card | View (group) | "Explore" + "260+ landmarks" + hieroglyph unicode | No (not clickable!) | — |
| Google sign-in | View+Button | "Sign in with Google" | YES | — |
| Email sign-up | View+Button | "Sign up with Email" | YES | — |
| Sign in link | View+Button | "Already have an account? Sign in" | YES | — |
| Footer | TextView | "Built by Mr Robot" | No | — |

### Register Sheet Elements:
| Element | Type | Notes |
|---------|------|-------|
| Title | "Create Account" | |
| Display Name field | EditText | Optional |
| Email field | EditText | Required |
| Password field | EditText | Required |
| Confirm Password field | EditText | Required |
| Create Account button | Button | Primary action |
| Divider | "or" | |
| Google sign-up | Button | Alternative |
| Sign in link | "Already have an account?" + "Sign in" | |

### Login Sheet Elements:
| Element | Type | Notes |
|---------|------|-------|
| Title | "Sign In" | |
| Email field | EditText | Required |
| Password field | EditText | Required |
| Sign In button | Button | Primary action |
| Forgot password link | Text button | |
| Divider | "or" | |
| Google sign-in | Button | Alternative |
| Create account link | "Don't have an account?" + "Create one" | |

## UX Issues

| # | Screen | Severity | Issue | What It Looks Like Now | Suggested Fix |
|---|--------|----------|-------|------------------------|---------------|
| 1 | Welcome | MEDIUM | Feature cards (Scan, Dictionary, Explore) are NOT clickable — purely decorative. A user might try to tap them expecting to be taken to those features | Cards show features but no interaction | Either make them clickable (opens login sheet with "sign in to access") or add subtle "Sign in to explore" label |
| 2 | Welcome | LOW | No resource-ids or testTags on ANY Compose elements | UI hierarchy shows only generic android.view.View | Must add testTags and contentDescriptions for testing |
| 3 | Welcome | LOW | "Built by Mr Robot" footer — may confuse users who don't know the reference | Shows in production | Consider changing to "Built with ❤️ by Nader" or removing |
| 4 | All Sheets | LOW | Compose bottom sheets have no testTags — cannot write Compose UI tests for auth flow | No resource-ids at all | Add testTags to all fields and buttons |
| 5 | Welcome | MEDIUM | No guest/skip option — user MUST create account or sign in before seeing any content | Forces auth gate | Consider adding "Browse as Guest" with limited features |

## Accessibility Issues
| # | Screen | Element | Issue | Fix |
|---|--------|---------|-------|-----|
| 1 | Welcome | Logo | Has "Wadjet logo" contentDescription ✓ | OK |
| 2 | Welcome | Feature cards | No contentDescription on card containers | Add descriptive content descriptions |
| 3 | Welcome | All buttons | Buttons have no content-desc (empty string) | The text inside is readable but container should merge descendants |
| 4 | Welcome | Hieroglyph unicode chars | Hieroglyphs (𓀀, 𓊹, 𓋴) shown as standalone TextViews with no content description | Add "Hieroglyph" or specific glyph name |

## Navigation Issues
| # | From | To | Issue |
|---|------|----|-------|
| 1 | Welcome | Login Sheet | Works (tap "Already have an account? Sign in") |
| 2 | Welcome | Register Sheet | Works (tap "Sign up with Email") |
| 3 | Login Sheet | Welcome | Back button works |
| 4 | Register Sheet | Welcome | Back button works |
| 5 | Login → Forgot Password | NOT TESTED | Requires login sheet open |

## Missing States
| # | Screen | Missing State | Notes |
|---|--------|---------------|-------|
| 1 | Welcome | Offline state | No indication if network is unavailable |
| 2 | Register | Success state | Not tested (requires valid registration) |
| 3 | Register | Duplicate email error | Not tested |
| 4 | Login | Invalid credentials error | Not tested |
| 5 | Login | Rate limited error | Not tested |

## Logcat Errors During Walkthrough
- No crashes or FATAL exceptions
- Only emulator Bluetooth noise (`BluetoothPowerStatsCollector: error: 11`)
- GC activity normal (10MB/17MB heap, 38% free)
- No `wadjet` or `com.wadjet.app` error-level logs

## Emulator Limitation Note
Full screen walkthrough requires valid authentication. The remaining 15+ screens (Landing, Scan, Dictionary, Explore, Chat, Stories, Dashboard, Settings, Feedback, etc.) need to be assessed via **code analysis** (Stages 3, 6, 7, 8) rather than live emulator testing. E2E tests should include auth bypass or test account setup.
