# Wadjet Android — Screen Inventory

> Every screen in the app, its layout, components, and navigation flow.
> Maps 1:1 to web templates.

---

## Screen Map

```
S01  Splash                    (2s, auto-navigate)
S02  Welcome                   (first launch / logged out)
S03  Landing                   (dual-path hub, home tab)
S04  Scan                      (camera + gallery + results)
S05  Scan Results              (detection results view)
S06  Dictionary                (browse + learn + write tabs)
S07  Sign Detail               (bottom sheet)
S08  Lesson                    (interactive lesson)
S09  Explore                   (landmark browser)
S10  Landmark Detail           (full landmark page)
S11  Identify                  (landmark from photo)
S12  Chat                      (Thoth AI chatbot)
S13  Stories                   (story listing)
S14  Story Reader              (interactive reader)
S15  Dashboard                 (user stats + history)
S16  Settings                  (profile + preferences)
S17  Feedback                  (feedback form)
S18  Auth Modals               (login/register/forgot - bottom sheets)
```

---

## S01: Splash Screen

**Duration**: 2 seconds, then navigate based on auth state.

**Layout**:
```
┌──────────────────────┐
│                      │
│                      │
│                      │
│    ◉ Eye of Wadjet   │  (gold, pulse animation)
│    logo (120dp)      │
│                      │
│    W A D J E T       │  (gold gradient sweep text)
│    ─────────         │  (shimmer loading bar)
│                      │
│                      │
└──────────────────────┘
Background: Night (#0A0A0A)
```

**Navigation**:
- Firebase `currentUser` != null → S03 (Landing)
- Firebase `currentUser` == null → S02 (Welcome)

---

## S02: Welcome Screen

**Web equivalent**: `welcome.html`

**Layout**:
```
┌──────────────────────┐
│                      │
│  ◉ Wadjet            │  (logo + name, centered)
│                      │
│  "Decode the Secrets │  (Playfair, gold gradient)
│   of Ancient Egypt"  │
│                      │
│  [feature previews]  │  (horizontal scroll cards)
│  Scan  Dict  Explore │
│                      │
│  ┌──────────────────┐│
│  │ Sign in w Google │ │  (Google white button)
│  └──────────────────┘│
│  ┌──────────────────┐│
│  │ Sign up w Email  │ │  (WadjetGhostButton)
│  └──────────────────┘│
│                      │
│  Already have account?│
│  [Sign in]           │
│                      │
│  Built by Mr Robot   │
└──────────────────────┘
```

---

## S03: Landing (Home Tab)

**Web equivalent**: `landing.html`

**Layout**:
```
┌──────────────────────┐
│ ≡  Wadjet      🔔 👤│  (TopBar: logo, notif, avatar)
├──────────────────────┤
│                      │
│  Welcome back, Nader │  (Playfair, gold)
│                      │
│ ┌──────────────────┐ │
│ │ 𓂀 Hieroglyphs   │ │  (large card, parallax tilt)
│ │ Decode Ancient   │ │
│ │ Egypt            │ │
│ │ [Start Scanning] │ │  (WadjetButton)
│ └──────────────────┘ │
│                      │
│ ┌──────────────────┐ │
│ │ 🏛 Landmarks     │ │  (large card, parallax tilt)
│ │ Explore Sites    │ │
│ │ & Monuments      │ │
│ │ [Start Exploring]│ │  (WadjetButton)
│ └──────────────────┘ │
│                      │
│ ── Quick Actions ──  │
│ [📷Scan] [📖Dict]   │  (4 action chips)
│ [🧭Explore] [📜More]│
│                      │
├──────────────────────┤
│ 🏠  📷  🧭  📜  👤 │  (BottomNav)
└──────────────────────┘
```

---

## S04: Scan Screen

**Web equivalent**: `scan.html`

**Layout**:
```
┌──────────────────────┐
│ ← Scan        🖼 📜 │  (back, gallery, history)
├──────────────────────┤
│                      │
│ ┌──────────────────┐ │
│ │                  │ │
│ │   CameraX        │ │  (live preview, gold frame)
│ │   Preview        │ │
│ │                  │ │
│ │   ┌─ ─ ─ ─ ─┐   │ │  (gold corner brackets)
│ │   │ viewfinder│  │ │
│ │   └─ ─ ─ ─ ─┘   │ │
│ │                  │ │
│ └──────────────────┘ │
│                      │
│  Point camera at     │
│  hieroglyphs         │
│                      │
│       ◉ Capture      │  (large gold FAB)
│                      │
├──────────────────────┤
│ 🏠  📷  🧭  📜  👤 │
└──────────────────────┘
```

**Loading state (pipeline running)**:
```
┌──────────────────────┐
│ ← Scanning...        │
├──────────────────────┤
│                      │
│  [captured image]    │  (dimmed, shimmer overlay)
│                      │
│  ● Detecting glyphs  │  (step 1, active, gold pulse)
│  ○ Classifying signs │  (step 2, pending, muted)
│  ○ Transliterating   │  (step 3, pending)
│  ○ Translating       │  (step 4, pending)
│                      │
│  ═══════━━━━━━━━━━━  │  (progress bar, gold)
│                      │
└──────────────────────┘
```

---

## S05: Scan Results

**Web equivalent**: `scan.html` results section

**Layout**:
```
┌──────────────────────┐
│ ← Results    📤 💾  │  (back, share, save)
├──────────────────────┤
│                      │
│ [annotated image]    │  (bounding boxes on photo)
│  (zoomable/pinchable)│
│                      │
│ ── Detected (5) ──── │
│ ┌─────┬─────┬─────┐ │
│ │ 𓄿  │ 𓂋  │ 𓇋  │ │  (glyph chips, scrollable)
│ │ G1  │ D21 │ M17 │ │
│ │ 92% │ 87% │ 95% │ │
│ └─────┴─────┴─────┘ │
│                      │
│ ── Transliteration ──│
│  A r i              │  (monospace, gold)
│                      │
│ ── Translation ──── │
│  EN: "The great..." │
│  AR: "العظيم..."     │
│                      │
│ ── Timing ────────── │
│  Detection: 120ms    │
│  Total: 970ms        │
│                      │
│ [🔄 Scan Again]     │  (WadjetButton)
│                      │
└──────────────────────┘
```

---

## S05b: Scan History

**Route**: `ScanHistory` (accessible from Dashboard S15 or Scan screen)

**Layout**:
```
┌──────────────────────┐
│ ← Scan History       │
├──────────────────────┤
│                      │
│ ┌──────────────────┐ │
│ │ 🖼 [thumb] 3 glyphs│  (card per scan)
│ │ "A r i" · 92%    │ │
│ │ Apr 8, 2026      │ │
│ └──────────────────┘ │
│ ┌──────────────────┐ │
│ │ 🖼 [thumb] 1 glyph │
│ │ "n f r" · 88%    │ │
│ │ Apr 7, 2026      │ │
│ └──────────────────┘ │
│                      │
│ (Empty state: ghost  │
│  illustration +      │
│  "No scans yet")     │
│                      │
└──────────────────────┘
```

**State**: Firestore `scan_history` subcollection, sorted by `scanned_at` descending. Tap opens S05 with cached results.

---

## S06: Dictionary Screen

**Web equivalent**: `dictionary.html`

**Layout**:
```
┌──────────────────────┐
│ ← Dictionary         │
├──────────────────────┤
│ [Browse] [Learn] [Write]│ (tab bar, gold indicator)
├──────────────────────┤
│                      │
│ [🔍 Search signs...] │  (search bar)
│                      │
│ [A][B][C]...[Z][All] │  (category chips, scroll)
│ [Uni][Bi][Tri][Logo] │  (type filter chips)
│                      │
│ ┌─────┐ ┌─────┐     │
│ │  𓄿  │ │  𓇋  │     │  (sign grid, 3 columns)
│ │ G1   │ │ M17  │     │
│ │Vulture││Reed  │     │
│ └─────┘ └─────┘     │
│ ┌─────┐ ┌─────┐     │
│ │  𓂋  │ │  𓊃  │     │
│ │ D21  │ │ S29  │     │
│ │Mouth │ │Cloth │     │
│ └─────┘ └─────┘     │
│        ...           │
│                      │
├──────────────────────┤
│ 🏠  📷  🧭  📜  👤 │
└──────────────────────┘
```

---

## S07: Sign Detail (Bottom Sheet)

**Web equivalent**: sign detail modal in dictionary

**Layout**:
```
┌──────────────────────┐
│  ──── handle ────    │
│                      │
│     𓄿                │  (80dp, gold, hieroglyph font)
│                      │
│  G1 · Birds          │  (code + category, sand)
│  [Uniliteral]        │  (type badge)
│                      │
│  Transliteration: A  │
│  Phonetic: A         │
│  Meaning: Egyptian   │
│  vulture             │
│                      │
│  🔊 "ah"            │  (pronunciation, tappable)
│  Like the pause in   │
│  "uh-oh"            │
│                      │
│  ── Fun Fact ──      │
│  "The vulture was..."│
│                      │
│  ── Examples ──      │
│  "Used in royal..."  │
│                      │
│  [❤ Favorite] [📋 Copy] [📤 Share]│
└──────────────────────┘
```

---

## S08: Lesson Screen

**Web equivalent**: `lesson_page.html`

**Layout**:
```
┌──────────────────────┐
│ ← Lesson 1: Alphabet │
├──────────────────────┤
│ ▓▓▓▓▓▓▓░░░░░ 60%    │  (progress bar, gold)
│                      │
│  ── Teaching ──      │
│                      │
│     𓄿                │  (large sign display)
│  "A" - Egyptian      │
│  Vulture             │
│  🔊 Play sound      │
│                      │
│  "This sign represents│
│   the 'A' sound..."  │
│                      │
│  ── Exercise ──      │
│                      │
│  Which glyph is "A"? │
│                      │
│  ┌────┐ ┌────┐      │
│  │ 𓄿  │ │ 𓇋  │      │  (multiple choice grid)
│  └────┘ └────┘      │
│  ┌────┐ ┌────┐      │
│  │ 𓂋  │ │ 𓊃  │      │
│  └────┘ └────┘      │
│                      │
│  [← Prev] [Next →]  │
│                      │
│  Score: 8/10  ⭐⭐⭐ │
└──────────────────────┘
```

---

## S09: Explore Screen

**Web equivalent**: `explore.html`

**Layout**:
```
┌──────────────────────┐
│ ← Explore      📷 🗺│  (back, identify, map)
├──────────────────────┤
│ [🔍 Search landmarks]│
│                      │
│ [All][Pharaonic]     │  (category chips)
│ [Islamic][Museum]    │
│ [City ▼]             │  (city dropdown)
│                      │
│ ┌──────────────────┐ │
│ │ [image]          │ │  (landmark card)
│ │ Great Pyramids   │ │
│ │ Giza · Pharaonic │ │
│ │ Old Kingdom      │ │
│ │              ❤   │ │
│ └──────────────────┘ │
│ ┌──────────────────┐ │
│ │ [image]          │ │
│ │ Karnak Temple    │ │
│ │ Luxor · Pharaonic│ │
│ └──────────────────┘ │
│        ...           │
│                      │
│  ── Load More ──     │  (pagination)
│                      │
├──────────────────────┤
│ 🏠  📷  🧭  📜  👤 │
└──────────────────────┘
```

---

## S10: Landmark Detail

**Web equivalent**: explore.html detail modal → full screen

**Layout**:
```
┌──────────────────────┐
│ ← Great Pyramids  📤│  (collapsing toolbar)
├──────────────────────┤
│ [           hero    ]│  (image carousel, swipeable)
│ [         images    ]│
│  • • ○ •            │  (page indicator)
│                      │
│  Great Pyramids of   │  (Playfair, gold)
│  Giza               │
│  أهرامات الجيزة      │  (AR name, sand)
│                      │
│  [Pharaonic] [Giza]  │  (badges)
│  [Old Kingdom]       │
│                      │
│  [📍 Maps] [💬 Chat] │  (action row)
│  [❤ Save] [📤 Share]│
│                      │
│  [Overview][History] │  (tabs)
│  [Tips][Gallery]     │
│                      │
│  "The Great Pyramid  │  (body text)
│   of Giza is the..." │
│                      │
│  ── Recommendations ─│
│  ┌────┐ ┌────┐      │  (horizontal scroll)
│  │Sphinx│ │Saqqara│   │
│  └────┘ └────┘      │
│                      │
└──────────────────────┘
```

---

## S11: Identify Landmark

**Layout**: Reuses S04 camera UI with different branding:
- Title: "Identify Landmark"
- After capture → Loading → Results showing top-3 matches
- Tap match → Navigate to S10

---

## S12: Chat Screen

**Web equivalent**: `chat.html`

**Layout**:
```
┌──────────────────────┐
│ ← Thoth        🗑   │  (back, clear chat)
├──────────────────────┤
│                      │
│  ┌──────────────┐    │
│  │ I am Thoth,  │  🦅│  (bot message, left-aligned)
│  │ keeper of    │    │
│  │ wisdom...    │    │
│  │         🔊  │    │  (TTS button)
│  └──────────────┘    │
│                      │
│        ┌──────────┐  │
│        │ Tell me  │  │  (user message, right-aligned)
│        │ about    │  │   gold bg, night text
│        │ Osiris   │  │
│        └──────────┘  │
│                      │
│  ┌──────────────┐    │
│  │ Ah, Osiris!  │  🦅│  (streaming, typing anim)
│  │ The great    │    │
│  │ king of the  │    │
│  │ underworld...│    │
│  │ ▌            │    │  (blinking cursor while streaming)
│  └──────────────┘    │
│                      │
├──────────────────────┤
│ [Message...]  🎤  📤│  (input bar, mic, send)
└──────────────────────┘
```

---

## S13: Stories Screen

**Web equivalent**: `stories.html`

**Layout**:
```
┌──────────────────────┐
│ ← Stories            │
├──────────────────────┤
│ [All][Beginner]      │  (difficulty filter chips)
│ [Intermediate][Adv]  │
│                      │
│ ┌──────────────────┐ │
│ │ 𓊨  The Myth of  │ │  (story card)
│ │    Osiris        │ │
│ │ Beginner · 15min │ │
│ │ ▓▓▓▓▓░░░ 60%    │ │  (progress bar)
│ │ 5 chapters       │ │
│ └──────────────────┘ │
│ ┌──────────────────┐ │
│ │ 𓁹  The Eye of Ra│ │
│ │ Intermediate     │ │
│ │ ░░░░░░░░ 0%     │ │
│ │ 🔒 Premium      │ │  (locked for free tier)
│ └──────────────────┘ │
│        ...           │
│                      │
├──────────────────────┤
│ 🏠  📷  🧭  📜  👤 │
└──────────────────────┘
```

---

## S14: Story Reader

**Web equivalent**: `story_reader.html`

**Layout**:
```
┌──────────────────────┐
│ ← Osiris  Ch 2/5  🔊│  (back, chapter indicator, narrate)
│ ▓▓▓▓▓▓▓▓▓░░ 80%     │  (chapter progress)
├──────────────────────┤
│                      │
│  [scene image with   │  (Ken Burns animation)
│   Ken Burns effect]  │
│                      │
│  Chapter 2:          │  (Playfair, gold)
│  "The Journey to     │
│   the Underworld"    │
│                      │
│  "Osiris traveled    │  (body text with inline
│   through the 𓊨      │   glyph annotations)
│   (throne) of the    │
│   dead..."           │
│                      │
│  ── Interaction ──   │
│                      │
│  Which glyph means   │
│  "throne"?           │
│                      │
│  ┌────┐ ┌────┐      │  (choice buttons)
│  │ 𓊨  │ │ 𓁹  │      │
│  └────┘ └────┘      │
│  ┌────┐ ┌────┐      │
│  │ 𓄿  │ │ 𓂋  │      │
│  └────┘ └────┘      │
│                      │
│  ✅ Correct! The     │  (feedback after answer)
│  throne sign 𓊨...    │
│                      │
│  [← Prev] [Next →]  │
│                      │
│  Score: 12 · Glyphs  │
│  learned: 8          │
└──────────────────────┘
```

---

## S15: Dashboard

**Web equivalent**: `dashboard.html`

**Layout**:
```
┌──────────────────────┐
│   Dashboard    ⚙️    │  (settings gear)
├──────────────────────┤
│                      │
│  👤 Nader            │  (avatar + name)
│  nader@example.com   │
│  Member since Jan 26 │
│                      │
│  ┌─────┐ ┌─────┐    │
│  │  3  │ │ 42  │    │  (stat cards, 2x2 grid)
│  │Today│ │Total│    │
│  │Scans│ │Scans│    │
│  └─────┘ └─────┘    │
│  ┌─────┐ ┌─────┐    │
│  │  2  │ │ 35  │    │
│  │Story│ │Glyph│    │
│  │Done │ │Learn│    │
│  └─────┘ └─────┘    │
│                      │
│  ── Recent Scans ──  │
│  [thumb] [thumb] →   │  (horizontal scroll)
│                      │
│  ── Favorites ──     │
│  [Landmarks][Glyphs] │  (sub-tabs)
│  [Stories]           │
│  [fav card] [fav]→   │
│                      │
│  ── Story Progress ──│
│  Osiris: 60% ▓▓▓░░  │
│  Eye of Ra: 20% ▓░░ │
│                      │
├──────────────────────┤
│ 🏠  📷  🧭  📜  👤 │
└──────────────────────┘
```

---

## S16: Settings

**Web equivalent**: `settings.html`

**Layout**:
```
┌──────────────────────┐
│ ← Settings           │
├──────────────────────┤
│                      │
│  ── Profile ──       │
│  Display name: Nader │  (editable)
│  Email: n@ex.com     │  (read-only)
│  [Google] auth       │  (provider badge)
│                      │
│  ── Language ──      │
│  ○ English           │
│  ● العربية            │
│                      │
│  ── Password ──      │  (hidden if Google auth)
│  [Current password]  │
│  [New password]      │
│  [Change Password]   │
│                      │
│  ── TTS ──           │
│  Enabled: ✓          │
│  Speed: ─○──── 1.0x  │
│                      │
│  ── Storage ──       │
│  Cached: 45 MB       │
│  [Clear Cache]       │
│                      │
│  ── About ──         │
│  Version: 1.0.0      │
│  Built by Mr Robot   │
│  [Send Feedback]     │
│                      │
│  ── Account ──       │
│  [Sign Out]          │  (red)
│  [Delete Account]    │  (red outline)
│                      │
└──────────────────────┘
```

---

## S17: Feedback

**Layout**:
```
┌──────────────────────┐
│ ← Feedback           │
├──────────────────────┤
│                      │
│  Category:           │
│  [Bug] [Suggestion]  │  (selectable chips)
│  [Praise] [Other]    │
│                      │
│  Message:            │
│  ┌──────────────────┐│
│  │                  ││  (multiline input)
│  │                  ││
│  │                  ││
│  └──────────────────┘│
│  10/1000 chars       │
│                      │
│  Name (optional):    │
│  [──────────────]    │
│                      │
│  Email (optional):   │
│  [──────────────]    │
│                      │
│  [Submit Feedback]   │  (WadjetButton)
│                      │
└──────────────────────┘
```

---

## S18: Auth Bottom Sheets

**Used from**: Welcome (S02), any protected action when logged out.

### Login Bottom Sheet
```
┌──────────────────────┐
│  ──── handle ────    │
│                      │
│  Sign In             │  (Playfair, gold)
│                      │
│  [Email]             │  (WadjetTextField)
│  [Password]          │  (WadjetTextField, password toggle)
│                      │
│  [Sign In]           │  (WadjetButton, gold, full-width)
│                      │
│  [Forgot password?]  │  (text link, sand)
│                      │
│  ── or ──            │  (divider)
│                      │
│  [G Sign in w Google]│  (Google brand button)
│                      │
│  Don't have account? │
│  [Create one]        │  (text link → Register sheet)
│                      │
│  ⚠ Error: Invalid    │  (error text, red, hidden by default)
│  email or password   │
└──────────────────────┘
```

### Register Bottom Sheet
```
┌──────────────────────┐
│  ──── handle ────    │
│                      │
│  Create Account      │  (Playfair, gold)
│                      │
│  [Display Name]      │  (WadjetTextField, optional)
│  [Email]             │  (WadjetTextField)
│  [Password]          │  (WadjetTextField, strength indicator)
│  [Confirm Password]  │  (WadjetTextField)
│                      │
│  Password strength:  │
│  ▓▓▓▓▓▓▓░░░ Strong  │  (green bar)
│                      │
│  [Create Account]    │  (WadjetButton, gold, full-width)
│                      │
│  ── or ──            │
│                      │
│  [G Sign up w Google]│
│                      │
│  Already have one?   │
│  [Sign in]           │  (text link → Login sheet)
│                      │
│  ✓ 8+ characters     │  (password rules, check/cross)
│  ✓ 1 uppercase       │
│  ✓ 1 lowercase       │
│  ✗ 1 digit           │
└──────────────────────┘
```

### Forgot Password Bottom Sheet
```
┌──────────────────────┐
│  ──── handle ────    │
│                      │
│  Reset Password      │  (Playfair, gold)
│                      │
│  Enter your email    │
│  and we'll send a    │
│  reset link.         │
│                      │
│  [Email]             │  (WadjetTextField)
│                      │
│  [Send Reset Link]   │  (WadjetButton, gold, full-width)
│                      │
│  [← Back to Sign In] │  (text link → Login sheet)
│                      │
│  ── After Submit ──  │
│                      │
│  ✅ Check your inbox │
│  We sent a link to   │
│  n***@example.com    │  (masked email)
│                      │
│  [Open Email App]    │  (WadjetGhostButton)
│  [Resend] (30s)      │  (countdown timer)
└──────────────────────┘
```

---

## Navigation Flow Diagram

```
                    ┌──────┐
                    │Splash│
                    └──┬───┘
               ┌───────┴───────┐
          logged out      logged in
               │               │
          ┌────▼────┐   ┌─────▼─────┐
          │Welcome  │   │ Landing   │
          │(S02)    │   │ (S03)     │
          └────┬────┘   └─────┬─────┘
               │              │
          auth success   BottomNav tabs
               │         ┌───┼───┬───┐
               ▼         │   │   │   │
          Landing     Scan  Exp  Str  Prof
          (S03)      (S04) (S09)(S13)(S15)
                       │     │    │    │
                    Results Detail Reader Settings
                    (S05) (S10) (S14) (S16)
                             │           │
                          Chat(S12)  Feedback(S17)
```
