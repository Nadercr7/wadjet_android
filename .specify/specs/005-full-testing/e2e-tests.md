# E2E Tests — Emulator + Agent Scripts

## Setup

```powershell
$SCRIPTS = "D:\Personal attachements\Repos\23-Android-Kotlin\awesome-android-agent-skills\.github\skills\testing_and_automation\android-emulator-skill\scripts"

# 1. Boot emulator
python "$SCRIPTS\emulator_manage.py" --boot Pixel_8

# 2. Build and install
cd "D:\Personal attachements\Projects\Wadjet-Android"
./gradlew installDebug

# 3. Launch app
python "$SCRIPTS\app_launcher.py" --launch com.wadjet.app

# 4. Verify screen
python "$SCRIPTS\screen_mapper.py" --json
```

---

## User Journeys

### Journey 1: New User Onboarding

| Step | Action | Script Command | Expected Result | How to Verify |
|------|--------|---------------|-----------------|---------------|
| 1 | Launch app cold | `python $SCRIPTS/app_launcher.py --launch com.wadjet.app` | Welcome screen | `screen_mapper.py --json` → text "WADJET" + "Sign up with Email" |
| 2 | Tap "Sign up with Email" | `python $SCRIPTS/navigator.py --find-text "Sign up with Email" --tap` | Register sheet opens | `screen_mapper.py --json` → text "Create Account" |
| 3 | Enter display name | `python $SCRIPTS/navigator.py --find-text "Display Name" --tap` then `python $SCRIPTS/keyboard.py --type "Test User"` | Name filled | `screen_mapper.py --json` → EditText contains "Test User" |
| 4 | Enter email | `python $SCRIPTS/navigator.py --find-text "Email" --tap` then `python $SCRIPTS/keyboard.py --type "testuser@example.com"` | Email filled | EditText contains email |
| 5 | Enter password | `python $SCRIPTS/navigator.py --find-text "Password" --tap` then `python $SCRIPTS/keyboard.py --type "Test1234!"` | Password filled | EditText filled (obscured) |
| 6 | Enter confirm password | `python $SCRIPTS/navigator.py --find-text "Confirm" --tap` then `python $SCRIPTS/keyboard.py --type "Test1234!"` | Confirm filled | EditText filled |
| 7 | Tap "Create Account" | `python $SCRIPTS/navigator.py --find-text "Create Account" --tap` | Success → Landing screen | `screen_mapper.py --json` → text "Welcome back" |

### Journey 2: Scan Hieroglyph

| Step | Action | Script Command | Expected Result | How to Verify |
|------|--------|---------------|-----------------|---------------|
| 1 | From Landing, tap "Scan" quick action | `python $SCRIPTS/navigator.py --find-text "Scan" --tap` | Scan screen | text "Tap to select" |
| 2 | Tap "Browse Gallery" | `python $SCRIPTS/navigator.py --find-text "Browse Gallery" --tap` | System picker opens | Activity changed |
| 3 | Push test image + select | `adb push test_hieroglyph.png /sdcard/Download/` then select via picker | Image selected, analysis starts | `ScanProgressOverlay` visible |
| 4 | Wait for analysis | Poll `screen_mapper.py --json` every 2s | Scan result screen | text "Scan Result" or glyph codes visible |
| 5 | Tap speak button | `python $SCRIPTS/navigator.py --find-content-desc "Listen" --tap` | Audio plays | `log_monitor.py --filter "MediaPlayer"` |
| 6 | Back to scan | `adb shell input keyevent KEYCODE_BACK` | Scan screen | text "Tap to select" |
| 7 | Check history | `python $SCRIPTS/navigator.py --find-content-desc "History" --tap` | History shows new scan | Scan item visible |

### Journey 3: Browse Dictionary then Play Pronunciation

| Step | Action | Script Command | Expected Result | How to Verify |
|------|--------|---------------|-----------------|---------------|
| 1 | Tap "Hieroglyphs" bottom nav | `python $SCRIPTS/navigator.py --find-text "Hieroglyphs" --tap` | HieroglyphsHub | text "1023 signs" |
| 2 | Tap "Dictionary" tool card | `python $SCRIPTS/navigator.py --find-text "Dictionary" --tap` | Dictionary Browse tab | Sign grid visible |
| 3 | Tap a category chip | `python $SCRIPTS/navigator.py --find-text "A - Man" --tap` | Filtered to category A | Grid updates |
| 4 | Tap a sign (e.g., A1) | `python $SCRIPTS/navigator.py --find-text "A1" --tap` | SignDetail screen | Sign code + description |
| 5 | Tap speak button | `python $SCRIPTS/navigator.py --find-content-desc "Play pronunciation" --tap` | Audio plays | MediaPlayer log or TTS log |
| 6 | Back to dictionary | `adb shell input keyevent KEYCODE_BACK` | Dictionary grid | Sign grid visible |

### Journey 4: Explore Landmarks then Identify

| Step | Action | Script Command | Expected Result | How to Verify |
|------|--------|---------------|-----------------|---------------|
| 1 | Tap "Explore" bottom nav | `python $SCRIPTS/navigator.py --find-text "Explore" --tap` | Explore list | Landmark cards visible |
| 2 | Tap category chip "Pharaonic" | `python $SCRIPTS/navigator.py --find-text "Pharaonic" --tap` | Filtered landmarks | Only pharaonic sites |
| 3 | Tap a landmark card | `python $SCRIPTS/navigator.py --find-text "Great Pyramid" --tap` | LandmarkDetail | Title, description, tabs |
| 4 | Swipe to History tab | `python $SCRIPTS/gesture.py --swipe left` | History tab content | "History" tab selected |
| 5 | Back to Explore | `adb shell input keyevent KEYCODE_BACK` | Explore list | |
| 6 | Tap Identify icon | `python $SCRIPTS/navigator.py --find-content-desc "Identify from photo" --tap` | Identify screen | text "Upload a photo" |
| 7 | Select test image | Same gallery flow as Journey 2 | Identification starts | Loading indicator |
| 8 | Wait for result | Poll `screen_mapper.py` | Result shown or "No match" | Text content |

### Journey 5: Chat with Thoth AI

| Step | Action | Script Command | Expected Result | How to Verify |
|------|--------|---------------|-----------------|---------------|
| 1 | Tap "Thoth" bottom nav | `python $SCRIPTS/navigator.py --find-text "Thoth" --tap` | Chat screen | Welcome message + suggestions |
| 2 | Tap suggestion chip | `python $SCRIPTS/navigator.py --find-text "Tell me about the pyramids" --tap` | Message sent, streaming starts | StreamingDots visible |
| 3 | Wait for response | Poll `screen_mapper.py` for bot message | Bot reply rendered | Long text visible |
| 4 | Type custom message | `python $SCRIPTS/navigator.py --find-type EditText --tap` then `python $SCRIPTS/keyboard.py --type "Who was Cleopatra?"` | Input filled | EditText has text |
| 5 | Tap send | `python $SCRIPTS/navigator.py --find-content-desc "Send" --tap` | Message sent, streaming | New user + bot messages |
| 6 | Tap speak on bot message | `python $SCRIPTS/navigator.py --find-content-desc "Listen" --tap` | Audio plays | MediaPlayer log |

### Journey 6: Read a Story

| Step | Action | Script Command | Expected Result | How to Verify |
|------|--------|---------------|-----------------|---------------|
| 1 | Tap "Stories" (from Landing quick action or bottom nav) | `python $SCRIPTS/navigator.py --find-text "Stories" --tap` | Stories list | Story cards visible |
| 2 | Tap first story card | `python $SCRIPTS/navigator.py --find-text "Beginner" --tap` (filter) then tap first card | StoryReader | Chapter text |
| 3 | Read chapter, scroll down | `python $SCRIPTS/gesture.py --swipe up` | More content | |
| 4 | Tap next chapter | `python $SCRIPTS/navigator.py --find-text "Next" --tap` | Chapter 2 | Chapter updated |
| 5 | Complete an interaction (quiz) | Tap answer option | Feedback banner | Correct/incorrect |
| 6 | Back to stories | `adb shell input keyevent KEYCODE_BACK` | Stories list | |

### Journey 7: Dashboard and Favorites

| Step | Action | Script Command | Expected Result | How to Verify |
|------|--------|---------------|-----------------|---------------|
| 1 | Tap profile icon (Landing top bar) | `python $SCRIPTS/navigator.py --find-content-desc "Profile" --tap` | Dashboard | Stats grid visible |
| 2 | Check stats | `screen_mapper.py --json` | "Scans Today", "Total Scans", etc. | Stats values visible |
| 3 | Tap "Landmark" favorites tab | `python $SCRIPTS/navigator.py --find-text "Landmark" --tap` | Landmark favorites | List or empty state |
| 4 | Pull to refresh | `python $SCRIPTS/gesture.py --swipe down --from-top` | Data refreshes | Stats update |

### Journey 8: Settings and Theme

| Step | Action | Script Command | Expected Result | How to Verify |
|------|--------|---------------|-----------------|---------------|
| 1 | Tap settings icon | `python $SCRIPTS/navigator.py --find-content-desc "Settings" --tap` | Quick Settings dialog | TTS toggle, cache info |
| 2 | Toggle TTS off | `python $SCRIPTS/navigator.py --find-text "Enable TTS" --tap` | Toggle changes | Switch state flipped |
| 3 | Tap "Full Settings" | `python $SCRIPTS/navigator.py --find-text "Full Settings" --tap` | Settings screen | Profile, TTS, Storage sections |
| 4 | Tap "Clear Cache" | `python $SCRIPTS/navigator.py --find-text "Clear" --tap` | Cache cleared | "0 MB" or "0 B" |
| 5 | Back | `adb shell input keyevent KEYCODE_BACK` | Previous screen | |

### Journey 9: Offline Mode

| Step | Action | Script Command | Expected Result | How to Verify |
|------|--------|---------------|-----------------|---------------|
| 1 | Enable airplane mode | `adb shell settings put global airplane_mode_on 1; adb shell am broadcast -a android.intent.action.AIRPLANE_MODE` | No network | |
| 2 | Open Dictionary | `python $SCRIPTS/navigator.py --find-text "Hieroglyphs" --tap` then tap Dictionary | Cached signs show | Grid visible from Room cache |
| 3 | Try Chat | `python $SCRIPTS/navigator.py --find-text "Thoth" --tap` then type + send | Error message | Error toast or error state |
| 4 | Try Explore | `python $SCRIPTS/navigator.py --find-text "Explore" --tap` | Cached landmarks show | At least some cards from Room |
| 5 | Disable airplane mode | `adb shell settings put global airplane_mode_on 0; adb shell am broadcast -a android.intent.action.AIRPLANE_MODE` | Network restored | |
| 6 | Refresh | Pull down or tap retry | Fresh data loads | Updated content |

### Journey 10: Auth — Login, Logout, Re-login

| Step | Action | Script Command | Expected Result | How to Verify |
|------|--------|---------------|-----------------|---------------|
| 1 | Launch fresh (clear data first) | `adb shell pm clear com.wadjet.app` then launch | Welcome screen | "WADJET" title |
| 2 | Tap "Sign in" link | `python $SCRIPTS/navigator.py --find-text "Sign in" --tap` | Login sheet | "Sign In" title |
| 3 | Enter credentials | Type email + password | Fields filled | |
| 4 | Tap "Sign In" | `python $SCRIPTS/navigator.py --find-text "Sign In" --tap` | Landing screen | "Welcome back" |
| 5 | Go to Settings → Sign Out | Navigate to full settings → tap "Sign out" | Welcome screen | "WADJET" title |
| 6 | Verify local data cleared | Check Room DB + DataStore | Tables empty | `adb shell run-as com.wadjet.app cat databases/wadjet.db` |
| 7 | Re-login | Same login flow | Landing screen | User data fresh |

---

## Error Journeys

### Error 1: No Internet During Scan

| Step | Action | Expected | Verify |
|------|--------|----------|--------|
| 1 | Airplane ON | No network | |
| 2 | Select image in Scan | Error message shown | Error state in screen_mapper |
| 3 | Airplane OFF | Network restored | |
| 4 | Retry | Scan succeeds | Result screen |

### Error 2: Backend Down

| Step | Action | Expected | Verify |
|------|--------|----------|--------|
| 1 | Block backend via iptables or hosts | Connection timeout | |
| 2 | Try any API feature | Error state with retry | ErrorState composable visible |
| 3 | Unblock | Retry works | |

### Error 3: Invalid Image Upload

| Step | Action | Expected | Verify |
|------|--------|----------|--------|
| 1 | Push non-image file (txt/pdf) | Error or graceful handling | No crash |
| 2 | Push oversized image (>10MB) | Error about file size | Error message |

### Error 4: Expired Token Mid-Session

| Step | Action | Expected | Verify |
|------|--------|----------|--------|
| 1 | Login normally | Authed | |
| 2 | Manually clear/corrupt access token | Token invalid | |
| 3 | Make API call | TokenAuthenticator triggers refresh | 401 → refresh → retry (transparent) |
| 4 | If refresh fails | User logged out to Welcome | Welcome screen |
