# Wadjet Android — Feature Specification

> Complete feature spec for the Wadjet Android app.
> Every feature maps 1:1 to the web app. No gaps. No extras.

---

## F1: Authentication (Priority: P1)

### Overview
Firebase Auth with Google Sign-In (primary) and Email/Password (secondary). On successful auth, the app gets a Firebase ID token and also authenticates with the Wadjet backend API (using Firebase token as credential or by using the existing `/api/auth/google` endpoint with the Google ID token).

### User Journeys

**F1.1 — First Launch (No Account)**
1. App opens → Splash screen (Wadjet eye logo, gold pulse animation, night background)
2. Splash → Welcome screen (hero image, Wadjet branding, two CTAs)
3. User taps "Sign in with Google" → Google One Tap / Sign-In flow → Firebase Auth creates account
4. On success → App syncs Firebase ID token with Wadjet backend (`POST /api/auth/google` with Google credential)
5. Store Wadjet backend access token in EncryptedSharedPreferences
6. Navigate to Landing screen (dual-path)

**F1.2 — Email Registration**
1. From Welcome → "Sign up with email" → Registration form (email, password 8+ chars with upper/lower/digit, display name optional)
2. Firebase Auth `createUserWithEmailAndPassword` → Send verification email
3. On success → Create Wadjet backend account (`POST /api/auth/register`)
4. Navigate to Landing

**F1.3 — Returning User**
1. App opens → Splash → Check Firebase Auth `currentUser`
2. If logged in → Auto-refresh Firebase token → Validate Wadjet backend token
3. If Wadjet token expired → Refresh via `POST /api/auth/refresh` (using stored refresh token)
4. Navigate to Landing

**F1.4 — Logout**
1. Settings → Sign Out → Firebase Auth `signOut()` + Wadjet `POST /api/auth/logout`
2. Clear local tokens → Navigate to Welcome

### Acceptance Criteria
- [ ] Google Sign-In works on real device with One Tap
- [ ] Email registration validates password strength (8+ chars, 1 upper, 1 lower, 1 digit)
- [ ] Account lockout after 10 failed email logins (15 min cooldown)
- [ ] Tokens stored in EncryptedSharedPreferences, NEVER in plain SharedPreferences
- [ ] Auto token refresh on 401 responses (single shared coroutine, no race conditions)
- [ ] Firebase user → Wadjet backend user sync is atomic (both succeed or rollback)

---

## F2: Landing — Dual Path Hub (Priority: P1)

### Overview
The home screen after auth. Shows the Wadjet brand, then two large cards: "Hieroglyphs" path and "Landmarks" path. Bottom navigation provides quick access to all features.

### UI Structure
- **Top**: Wadjet eye logo + "Wadjet" animated gold gradient text
- **Body**: Two large tappable cards with Atropos-style parallax tilt:
  - **Hieroglyphs Card**: Eye of Horus icon, "Decode Ancient Egypt", description, gold CTA "Start Scanning"
  - **Landmarks Card**: Pyramid icon, "Explore Egypt", description, gold CTA "Start Exploring"
- **Quick Actions Row**: Scan (camera icon), Dictionary (book), Explore (compass), Stories (scroll), Chat (message)
- **Bottom Nav**: 5 tabs — Home, Scan, Explore, Stories, Profile

### Bottom Navigation (5 Tabs)
| Tab | Icon | Destination |
|-----|------|-------------|
| Home | `home` | Landing / Dual-path hub |
| Scan | `scan` | Hieroglyph scanner (camera) |
| Explore | `compass` | Landmark explorer |
| Stories | `book-open` | Story listing |
| Profile | `user` | Dashboard / Settings |

---

## F3: Hieroglyph Scanner (Priority: P1)

### Overview
The core feature. User captures/selects an image of hieroglyphs → ONNX YOLOv8s detects individual glyphs → ONNX MobileNetV3 classifies each → reading order determined → MdC transliteration → AI translation (EN + AR).

### User Journeys

**F3.1 — Camera Scan**
1. Scan tab → CameraX preview (full screen, gold frame overlay)
2. User taps gold capture button → Image captured
3. Pipeline runs with animated progress steps:
   - Step 1: "Detecting glyphs..." (shimmer animation)
   - Step 2: "Classifying signs..." 
   - Step 3: "Transliterating..."
   - Step 4: "Translating..."
4. Results screen:
   - Annotated image (bounding boxes on original)
   - Detected glyphs list (Gardiner code, Unicode glyph, confidence %)
   - Transliteration (MdC text)
   - Translation (EN + AR, switchable)
   - Timing info (total ms)
   - Actions: Save to history, Share, Scan again

**F3.2 — Gallery Upload**
1. Scan tab → Gallery icon (top-right) → Photo picker
2. Image selected → Same pipeline as F3.1

**F3.3 — Scan History**
1. Scan tab → History icon (top-left) → List of past scans
2. Each entry: thumbnail, date, glyph count, confidence
3. Tap → Full results view (same as F3.1 step 4)
4. Swipe to delete

### API Flow
```
POST /api/scan (multipart: file=image, mode="auto")
→ { num_detections, glyphs[], transliteration, gardiner_sequence,
    translation_en, translation_ar, annotated_image (base64),
    detection_ms, classification_ms, translation_ms, total_ms }
```

### Alternative: On-Device ONNX
For offline mode, the app CAN run ONNX models locally:
- `glyph_detector_uint8.onnx` (YOLOv8s, ~25MB)
- `hieroglyph_classifier_uint8.onnx` (MobileNetV3-Small, ~5MB)
- Translation falls back to a simple lookup table (no AI)

### Acceptance Criteria
- [ ] Camera preview at 30fps, no lag
- [ ] Image auto-resized to max 1024px before upload (bandwidth optimization)
- [ ] Progress steps animate sequentially with timing
- [ ] Results show annotated image with colored bounding boxes
- [ ] Glyph cards show: Unicode glyph (large), Gardiner code, meaning, confidence bar
- [ ] Translation toggleable between EN and AR
- [ ] Save to scan history (Firestore + local Room cache)
- [ ] Share as image (annotated) or text (translation)
- [ ] Handles: no glyphs found, low confidence warning, network error, large file

---

## F4: Dictionary (Priority: P1)

### Overview
Browse and search 1,000+ Gardiner signs. Three tabs: Browse (full catalog), Learn (5 lessons), Write (text → hieroglyphs).

### Tabs

**F4.1 — Browse Tab**
- Horizontal category chips (A-Z Gardiner categories + "All")
- Type filter chips: Uniliteral, Biliteral, Triliteral, Logogram, Determinative
- Search bar (filters by code, meaning, transliteration)
- Grid of sign cards (gold-bordered on dark):
  - Large Unicode glyph
  - Gardiner code (monospace)
  - Meaning (body text)
  - Type badge
- Tap sign → Sign Detail bottom sheet

**F4.2 — Sign Detail (Bottom Sheet)**
- Large glyph display (80dp, gold, hieroglyph font)
- Gardiner code + category name
- Transliteration + phonetic value
- Type badge (Uniliteral/Biliteral/etc.)
- Pronunciation section:
  - "How to say it" text
  - Play pronunciation button (TTS)
  - Pronunciation guide (sound + description)
- Meaning + examples
- Fun fact card
- Actions: Add to favorites, Copy glyph, Share

**F4.3 — Learn Tab (5 Lessons)**
- 5 lesson cards with progress indicators:
  1. The Alphabet (25 uniliterals)
  2. Common Words (biliterals + triliterals)
  3. Royal Names (cartouche reading)
  4. Determinatives (silent classifiers)
  5. Reading Practice (full inscriptions)
- Tap lesson → Lesson screen with:
  - Teaching section (sign + explanation + pronunciation)
  - Exercise section (multiple choice / matching)
  - Progress bar + score

**F4.4 — Write Tab**
- Text input field ("Type English text...")
- Mode selector: Alpha (letter-by-letter) | Smart (AI translation) | MdC (expert)
- Output display: hieroglyph string (large, gold, hieroglyph font)
- Individual glyph breakdown cards
- Glyph palette (tap to insert):
  - Category tabs: Uniliteral, Biliteral, Common, Numbers
  - Grid of tappable glyphs
- Actions: Copy, Share, Save to history

### API Endpoints
```
GET /api/dictionary?category=G&type=uniliteral&search=bird&page=1&per_page=50&lang=en
GET /api/dictionary/categories?lang=en
GET /api/dictionary/alphabet?lang=en
GET /api/dictionary/{code}?lang=en   (e.g., /api/dictionary/G1)
GET /api/dictionary/lesson/{n}?lang=en (n=1..5)
POST /api/write { text, mode: "alpha"|"mdc"|"smart" }
GET /api/write/palette
```

### Offline Strategy
- Cache all dictionary signs in Room on first load (~500KB JSON)
- Cache lessons in Room
- Write Alpha + MdC modes work offline (lookup tables)
- Write Smart mode requires network (AI translation)

---

## F5: Landmark Explorer (Priority: P1)

### Overview
Browse 260+ Egyptian landmarks with rich detail pages. Filter by category, city, search. Identify landmarks from photos.

### User Journeys

**F5.1 — Browse Landmarks**
1. Explore tab → Grid/List of landmark cards
2. Filter bar: Category chips (Pharaonic, Islamic, Museum, etc.) + City dropdown + Search
3. Each card: thumbnail image, name, category badge, city, era
4. Tap card → Landmark Detail screen

**F5.2 — Landmark Detail**
- Hero image carousel (swipeable)
- Name (EN + AR), category badge, era, city
- Google Maps link button
- Tabs: Overview | History | Tips | Gallery
  - Overview: description, highlights, historical significance
  - History: sections (title + content paragraphs)
  - Tips: visiting tips, opening hours
  - Gallery: image grid (tap to fullscreen)
- Recommendations section (similar landmarks cards)
- Actions: Favorite, Share, Get directions, Chat about this

**F5.3 — Identify Landmark**
1. Explore tab → Camera FAB (or top-right camera icon)
2. Camera/gallery → Upload image
3. API returns: top-3 matches with confidence
4. Display: ranked list of matches, tap to see detail
5. Best match auto-expands

**F5.4 — Landmark on Map**
- Optional: Map view of landmarks with gold pin markers
- Tap pin → preview card → tap to detail

### API Endpoints
```
GET /api/landmarks?category=Pharaonic&city=Luxor&search=temple&page=1&per_page=24&lang=en
GET /api/landmarks/categories
GET /api/landmarks/{slug}?lang=en
GET /api/landmarks/{slug}/children
POST /api/explore/identify (multipart: file=image)
```

### Offline Strategy
- Cache landmark list in Room (metadata only, ~200KB)
- Cache visited landmark details in Room (full JSON + image URLs)
- Landmark identification requires network

---

## F6: Thoth AI Chat (Priority: P2)

### Overview
Streaming AI chatbot with Egyptian mythology personality. Supports text + voice input. Context-aware (can discuss specific landmarks). TTS for reading responses.

### User Journeys

**F6.1 — Text Chat**
1. Chat screen → Message input bar + send button
2. User types message → Send
3. SSE stream: response appears word-by-word (animated typing effect)
4. Response rendered as Markdown (bold, lists, hieroglyph Unicode)
5. Below each response: TTS play button, copy button

**F6.2 — Voice Chat**
1. Tap microphone icon → Android SpeechRecognizer activates
2. Voice transcribed to text → Auto-sent as message
3. Response arrives → Auto-plays TTS

**F6.3 — Landmark Context Chat**
1. From Landmark Detail → "Chat about this" button
2. Opens chat with landmark context pre-loaded
3. Thoth knows about this specific landmark (injected as system context)

### SSE Stream Parsing
```
POST /api/chat/stream { message, session_id, landmark? }
Response: text/event-stream
→ data: {"text": "Ah, "}
→ data: {"text": "the Eye "}
→ data: {"text": "of Horus..."}
→ data: [DONE]
```

### Acceptance Criteria
- [ ] SSE stream parsed correctly, text appears incrementally
- [ ] Markdown rendered (bold, lists, code blocks)
- [ ] Session persists during app lifetime (session_id in ViewModel)
- [ ] Clear chat option works
- [ ] TTS plays response with Orus voice (server TTS) or device TTS
- [ ] STT works in noisy environments gracefully
- [ ] Network error → retry button
- [ ] Message limit warning (20/day for free tier)

---

## F7: Interactive Stories (Priority: P2)

### Overview
12 Egyptian mythology stories with interactive chapters. 4 interaction types. AI-generated scene images. TTS narration per chapter.

### Story List Screen
- Grid of story cards (cover glyph emoji, title, subtitle, difficulty badge, chapter count)
- Difficulty filter: Beginner / Intermediate / Advanced
- Progress indicator on each card (chapters completed / total)
- Free tier limit: 3 stories accessible

### Story Reader Screen
- Chapter progress bar (top)
- Scene image (AI-generated, Ken Burns animation)
- Chapter title
- Story text paragraphs with inline glyph annotations:
  - Tappable glyph words → tooltip showing: glyph, code, meaning, transliteration
- Interaction zone (between paragraphs):
  - `choose_glyph`: Multiple choice — pick correct hieroglyph
  - `write_word`: Text input — type Gardiner code
  - `glyph_discovery`: Info reveal — tap to learn (always "correct")
  - `story_decision`: Branching choice — tap an outcome
- Chapter navigation: Previous / Next buttons
- Narration: Play button → TTS reads chapter (Aoede voice)
- Score display + glyphs learned counter

### API Endpoints
```
GET /api/stories → list all 12 stories
GET /api/stories/{id} → full story JSON
GET /api/stories/{id}/chapters/{index} → single chapter
POST /api/stories/{id}/interact { chapter_index, interaction_index, answer }
POST /api/stories/{id}/chapters/{index}/image → generate AI scene image
```

### Offline Strategy
- Cache story content in Room on first view
- Interaction validation: cache correct answers locally for offline play
- Scene images cached via Coil disk cache
- TTS requires network (server voices) or falls back to device TTS

---

## F8: Text-to-Speech (Priority: P2)

### Overview
Dual TTS: Server-side Gemini voices (high quality, contextual) → Android TTS (fallback). Voice varies by context.

### Voice Context Mapping
| Context | Voice | Usage |
|---------|-------|-------|
| `thoth_chat` | Orus | Chat responses |
| `story_narration` | Aoede | Story chapters |
| `dictionary` / `pronunciation` | Rasalgethi | Sign pronunciation |
| `landing` / `explore` / `scan` | Charon | UI narration |

### Flow
1. App calls `POST /api/audio/speak { text, lang, context }`
2. If 200 → WAV audio blob → Play with MediaPlayer
3. If 204 → Use Android TextToSpeech engine (device voices)
4. Cache server audio responses (SHA256 hash key) in app-specific storage

### Settings
- TTS speed control (stored in preferences, sent as parameter)
- Language preference (en/ar) affects voice selection
- Mute toggle

---

## F9: Dashboard (Priority: P2)

### Overview
User stats, scan history, favorites, story progress. All data from Firestore with local Room cache.

### Sections
1. **Stats Cards**: Scans today / Total scans / Stories completed / Glyphs learned
2. **Recent Scans**: Horizontal scroll of scan thumbnails → tap to view
3. **Favorites**: Tabs — Landmarks | Glyphs | Stories → grid of favorited items
4. **Story Progress**: List of in-progress stories with progress bars

### Data Sources
- Stats: Firestore aggregation or local count
- Scan history: Firestore `scan_history` collection + Room cache
- Favorites: Firestore `favorites` subcollection
- Story progress: Firestore `story_progress` subcollection

---

## F10: Settings (Priority: P3)

### Sections
1. **Profile**: Avatar, display name (editable), email (read-only), auth provider badge
2. **Language**: EN / AR toggle (changes entire UI including RTL)
3. **Password**: Change password — ONLY shown for email auth users (not Google)
4. **TTS Settings**: Speed slider, enable/disable
5. **Offline Data**: Manage cached data, clear cache, download offline pack
6. **About**: Version, "Built by Mr Robot", licenses, GitHub link
7. **Danger Zone**: Delete account, Sign out

---

## F11: Feedback (Priority: P3)

### Flow
1. Settings → "Send Feedback" or shake gesture
2. Form: Category (Bug/Suggestion/Praise/Other), Message (10-1000 chars), optional name/email
3. Submit → `POST /api/feedback` → Success toast

---

## F12: Bilingual EN/AR (Priority: P2)

### Implementation
- All strings externalized to `strings.xml` (EN) and `strings-ar/strings.xml` (AR)
- Language toggle in Settings (not system language — user preference)
- Arabic activates RTL layout direction
- String keys match web app i18n keys (en.json / ar.json)
- Numbers in AR locale
- Hieroglyph text always LTR (hieroglyphs don't have RTL)

---

## F13: Push Notifications (Priority: P3)

### Channels
1. **Story Reminder**: "Continue your story — The Eye of Ra awaits"
2. **Daily Glyph**: "Today's hieroglyph: 𓄿 (A) — the Egyptian vulture"
3. **New Content**: When new stories or features are added

---

## Edge Cases

| Scenario | Handling |
|----------|----------|
| No internet | Cached data shows, AI features show "offline" state |
| Camera denied | Graceful message + settings link, gallery still works |
| Large image (>10MB) | Compress before upload, show progress |
| API rate limit (429) | Show "Too many requests" with cooldown timer |
| API server down | Show cached data, queue actions for retry |
| Free tier limit | Show upgrade prompt (future), explain limits |
| Very long hieroglyph text | Horizontal scroll or wrap in results |
| Arabic RTL + hieroglyphs LTR | Hieroglyph sections force LTR within RTL layout |
| Background → foreground | Refresh stale data (>5 min old) |
| Low memory | Release image caches, reduce animation |
| Slow network | Show loading shimmer, timeout after 30s |
