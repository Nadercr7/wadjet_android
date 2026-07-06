# Wadjet Android - DEPI Final Project Deck: Execution Plan

> This file is the single source of truth for the executing chat.
> Read it top to bottom. Follow every instruction. Don't improvise structure.

---

## Deliverables

| # | File | Description |
|---|------|-------------|
| 1 | `wadjet-android-deck.html` | Self-contained HTML slide deck (English). 14 slides. Zero external dependencies except Google Fonts CDN. Print-to-PDF ready. |
| 2 | `script-ar.md` | Egyptian Arabic presenter script. Slide-by-slide. What Nader says out loud. Includes Q&A prep section. |

Both files go in: `d:\Personal attachements\Projects\Wadjet-Android\`

---

## Mandatory Skills & References

The executing chat MUST use these resources during content generation. They live in the user's local repos at `D:\Personal attachements\Repos\`.

### Humanization (CRITICAL - applies to ALL English text)

**Source**: The humanizer instructions are already loaded in the Copilot environment at `vscode-userdata:/c%3A/Users/Nader/AppData/Roaming/Code/User/prompts/humanizer-default.instructions.md`

**Voice profile**: `technical` (precise terms, code-like clarity, deadpan humor, short sentences)

**Non-negotiable rules for all English text in the deck**:
- Zero em dashes. No en-dashes (except numeric ranges). No `--`.
- No banned words: passionate, leverage, robust, seamless, delve, elevate, empower, world-class, cutting-edge, innovative, groundbreaking, transformative, game-changing.
- Active voice by default. Specific nouns and numbers over abstract adjectives.
- Vary sentence length wildly. Three words. Then thirty.
- No closing summaries. Just stop.
- No significance inflation ("pivotal moment", "represents a broader trend"). State facts.
- No superficial -ing phrases ("highlighting the importance of", "ensuring that").
- No "It's not just X, it's Y" structures.
- No em dashes. (Yes, listed twice. It's that important.)
- Run the 3-pass humanizer process: Strip AI vocab > Break AI structures > Inject soul/burstiness.
- After writing each slide's text, ask: "What still makes this obviously AI-generated?" Fix those things.

### Presentation Design Principles (from ppt-agent-skills)

Read these files before starting slide design. They inform layout, rhythm, and composition decisions:

| Principle | Path | What it teaches |
|-----------|------|-----------------|
| Visual hierarchy & CRAP | `D:\Personal attachements\Repos\ppt-agent-skills\references\principles\visual-hierarchy.md` | Contrast, Repetition, Alignment, Proximity. One visual anchor per slide. |
| Cognitive load | `D:\Personal attachements\Repos\ppt-agent-skills\references\principles\cognitive-load.md` | Miller's Law (5-9 chunks max). One point per slide. 60-75% density for content pages. |
| Narrative arc | `D:\Personal attachements\Repos\ppt-agent-skills\references\principles\narrative-arc.md` | SCQA framework. Attention curve. Visual rhythm (density alternation). |
| Composition & whitespace | `D:\Personal attachements\Repos\ppt-agent-skills\references\principles\composition.md` | Gestalt principles. Three-tier whitespace. Three-thirds rule. |
| Color psychology | `D:\Personal attachements\Repos\ppt-agent-skills\references\principles\color-psychology.md` | 60-30-10 rule. Gold/black = luxury + precision (our palette). |
| Data visualization | `D:\Personal attachements\Repos\ppt-agent-skills\references\principles\data-visualization.md` | Stats need context. Trend > snapshot. Annotate anomalies. |
| Dark tech style | `D:\Personal attachements\Repos\ppt-agent-skills\references\styles\dark-tech.md` | Closest style reference to our black+gold. Grid dot patterns, glow rings, L-shape corner accents, pulse dots. |

### Animation & Transition References

Consult these repos for CSS animation patterns, transitions, and motion ideas:

| Repo | Path | What to steal |
|------|------|---------------|
| Hover.css | `D:\Personal attachements\Repos\21-Frontend-UI\Hover\css\hover.css` | Hover effects for cards and buttons. 2D transitions, border transitions, shadow transitions. |
| motion-primitives | `D:\Personal attachements\Repos\21-Frontend-UI\motion-primitives\` | Modern CSS/Framer motion patterns. Stagger reveals, fade-up, blur-in transitions. |
| animate-ui | `D:\Personal attachements\Repos\21-Frontend-UI\animate-ui\` | UI animation primitives. Entrance/exit animations, spring physics ideas. |
| magicui | `D:\Personal attachements\Repos\21-Frontend-UI\magicui\` | Animated counters, text reveals, shimmer effects, border beams. |
| react-bits | `D:\Personal attachements\Repos\21-Frontend-UI\react-bits\` | Creative UI animation ideas (translate to pure CSS). Blur text, split text, gradient text reveals. |

**Important**: These repos use React/Framer Motion. Extract the CONCEPTS and CSS keyframes, not the React code. The deck is pure HTML/CSS/JS with zero dependencies.

---

## Design System

### Brand palette (CSS variables, inherited from web deck)

```css
:root {
    --gold: #D4AF37;
    --gold-light: #E5C76B;
    --gold-dark: #B8962E;
    --gold-muted: #A08520;
    --night: #0A0A0A;
    --surface: #141414;
    --surface-alt: #1A1A1A;
    --ivory: #F5F0E8;
    --sand: #C4A265;
    --dust: #8B7355;
    --text: #F0F0F0;
    --text-muted: #8A8A8A;
    --border: #2A2A2A;
}
```

### Typography

- Headings: `Playfair Display` (600, 700, 800)
- Body: `Inter` (300-700)
- Hieroglyphs: `Noto Sans Egyptian Hieroglyphs`
- All loaded from Google Fonts CDN (single link tag)

### Color application (60-30-10)

- 60% `--night` / `--surface` (backgrounds)
- 30% `--ivory` / `--text` (content text, card surfaces)
- 10% `--gold` variants (accents, stats, highlights, CTAs)

---

## Animations & Transitions (DETAILED SPEC)

The web deck has basic reveal animations. The Android deck should go further. Here's exactly what to implement:

### Scroll-triggered reveals (enhanced from web deck)

```css
/* Base reveal - fade up */
.reveal {
    opacity: 0;
    transform: translateY(30px);
    transition: opacity 0.8s cubic-bezier(0.16, 1, 0.3, 1),
                transform 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

/* Blur-in variant - elements start blurred */
.reveal-blur {
    opacity: 0;
    filter: blur(8px);
    transform: translateY(20px);
    transition: opacity 0.8s ease, filter 0.8s ease, transform 0.8s ease;
}

/* Scale-in variant - elements grow from 95% */
.reveal-scale {
    opacity: 0;
    transform: scale(0.95);
    transition: opacity 0.6s ease, transform 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* Slide-in from left */
.reveal-left {
    opacity: 0;
    transform: translateX(-40px);
    transition: opacity 0.7s ease, transform 0.7s cubic-bezier(0.16, 1, 0.3, 1);
}

/* Slide-in from right */
.reveal-right {
    opacity: 0;
    transform: translateX(40px);
    transition: opacity 0.7s ease, transform 0.7s cubic-bezier(0.16, 1, 0.3, 1);
}
```

### Staggered delays (for card grids)

```css
.reveal-d1 { transition-delay: 0.1s; }
.reveal-d2 { transition-delay: 0.2s; }
.reveal-d3 { transition-delay: 0.3s; }
.reveal-d4 { transition-delay: 0.4s; }
.reveal-d5 { transition-delay: 0.5s; }
.reveal-d6 { transition-delay: 0.6s; }
.reveal-d7 { transition-delay: 0.7s; }
.reveal-d8 { transition-delay: 0.8s; }
```

### Animated counter for stats (JavaScript)

When the "By The Numbers" slide becomes visible, stat numbers should count up from 0 to their value over 1.5 seconds. Use `requestAnimationFrame` with easing. NOT a library. Pure JS.

```js
function animateCounter(element, target, duration = 1500) {
    const start = performance.now();
    const easeOutExpo = t => t === 1 ? 1 : 1 - Math.pow(2, -10 * t);
    // ... count from 0 to target with easing
}
```

### Card hover effects

Cards should have subtle hover transitions:
- Border color fades to `rgba(212,175,55,0.3)`
- Box shadow fades in: `0 0 30px rgba(212,175,55,0.08)`
- Transform: `translateY(-2px)` on hover
- Transition: 0.3s ease on all properties

### Gold shimmer effect (for hero slide title)

CSS-only shimmer that sweeps across the gold gradient text on the title slide. Uses a `@keyframes` animation with a linear-gradient moving left to right.

### Floating decorative elements

- **Meteors**: Same as web deck. Gold streaks that animate diagonally.
- **Glow rings**: Radial gradient circles (400-600px) with 5-8% gold opacity. Position absolute.
- **Dot patterns**: `radial-gradient(circle, rgba(212,175,55,0.15) 1px, transparent 1px)` at `32px 32px` size.
- **Pulse dots**: On the architecture slide, connection points between modules pulse. 6px gold dot + 14px 10%-opacity outer ring, animating scale 1 to 1.4 and opacity 0.6 to 0.

### Phone mockup animation

When a slide with a phone mockup enters, the phone should:
1. Fade up from below (reveal-blur, 0.6s)
2. The screen content inside fades in 0.2s after the frame (staggered)
3. Callout labels fade in 0.3s after the screen content

### Pipeline step animation

The 4-step pipeline on the Scan slide: each step fades in sequentially (0.15s stagger), then the arrows between them draw in (border animation from left to right).

### Slide transitions

Between slides (scroll-snap), add a subtle parallax effect where background decorative elements (dot patterns, glow rings) move at 60% of scroll speed using IntersectionObserver and transform.

---

## Phone Mockup Component (CSS-only)

Replace the web deck's browser-frame mockups with Android phone frames. Pure CSS, no images.

```
Structure:
┌─────────────────────────┐
│  ┌───────────────────┐  │  ← Status bar (time, battery, signal)
│  │   ● ● ● (notch)  │  │  ← Centered pill notch
│  ├───────────────────┤  │
│  │                   │  │
│  │   Screen content  │  │  ← App content goes here
│  │   (simulated)     │  │
│  │                   │  │
│  │                   │  │
│  ├───────────────────┤  │
│  │   ━━━ (gesture)   │  │  ← Bottom gesture indicator
│  └───────────────────┘  │
└─────────────────────────┘
```

CSS specs:
- Outer frame: `border-radius: 36px`, `background: #1A1A1A`, `border: 2px solid #333`, `padding: 8px`
- Notch: centered pill, `width: 80px, height: 22px, border-radius: 11px, background: #0A0A0A`
- Status bar: flex row, `font-size: 0.55rem`, time left, icons right
- Screen: `border-radius: 28px`, `overflow: hidden`, holds the simulated app content
- Gesture bar: centered line, `width: 100px, height: 4px, border-radius: 2px, background: #555`
- Aspect ratio: approximately 9:19.5 (modern Android phone)
- Size: max-height ~70vh per slide, auto width
- Shadow: `0 20px 60px rgba(0,0,0,0.5)` for floating effect

Content inside the phone uses same card/surface styles as the rest of the deck but scaled down (`font-size` based on phone frame size).

---

## Slide-by-Slide Specification (14 slides)

### SLIDE 01 — Title
**Layout**: Centered, immersive. Low density (25-35%). Breathing room.
**Background**: Gold dot pattern + 3 meteor animations + glow ring top-right.
**Content**:
- Wadjet logo (base64 embedded, reuse from web deck)
- "Wadjet" in gold gradient (h1, with shimmer animation)
- Subtitle: "Egyptian Heritage AI" (lead text, sand color)
- Badge: `DEPI SOFTWARE DEVELOPMENT TRACK · FINAL PROJECT · 2026`
- Name: Nader Mohamed
- Subtext: BSc Artificial Intelligence, Kafr El Sheikh University
- Decorative hieroglyph line at bottom
**Reveals**: Logo first (reveal-scale), then badge (reveal, d1), title (reveal, d2), subtitle (reveal-blur, d3), name (reveal, d4), hieroglyphs (reveal, d5)
**Footer**: `01 / 14`
**DEPI footer line**: Every slide gets a subtle bottom-left text: "DEPI · Software Development · 2026"

### SLIDE 02 — The Problem
**Layout**: Centered heading + 3-card grid below.
**Badge**: `THE PROBLEM`
**Heading**: "Ancient Egypt is [gold]fascinating[/gold] but [muted]inaccessible[/muted]"
**Divider**: Gold gradient line
**Three cards** (reveal staggered d3-d5):
1. 𓂀 "Can't read it" — You visit a temple. Photograph an inscription. Walk away with no idea what it says. 99.9% of visitors do exactly this.
2. 𓏛 "No mobile tool" — No single app combines scanning, dictionary, writing, and exploration. You'd need five different resources.
3. 𓁟 "Scattered knowledge" — Landmark details live in expensive textbooks. Mythology in fragmented websites. Sign references behind academic paywalls.
**Background**: Subtle dot pattern.
**Humanizer note**: These card descriptions must NOT read like marketing copy. State the problem as a person would describe it.

### SLIDE 03 — The Solution
**Layout**: Centered heading + 6-card grid (3x2).
**Badge**: `THE SOLUTION`
**Heading**: "One app. Six tools. [gold]Everything Egypt.[/gold]"
**Six feature cards** (icon + name + one line):
1. 𓂀 Scan — Point your camera at hieroglyphs. Get glyph-by-glyph translation.
2. 𓏛 Dictionary — 1,000+ Gardiner signs. Offline. Searchable.
3. 𓆣 Write — Type English, get hieroglyphs. Hear the ancient pronunciation.
4. 𓉐 Explore — 260+ Egyptian landmarks with images and history.
5. 𓃭 Thoth — AI Egyptology guide. Ask anything about ancient Egypt.
6. 𓏞 Stories — 12 mythology tales with illustrations and narration.
**Subtitle below cards**: "Native Android. Material 3. Bilingual EN/AR."
**Reveals**: Cards stagger in (d1 through d6), subtitle last (d7).

### SLIDE 04 — Scan & Translate (hero feature + tech behind it)
**Layout**: Two sections. Top: feature overview. Bottom: technical pipeline.
**Badge**: `CORE FEATURE`
**Heading**: "Scan & [gold]Translate[/gold]"
**Lead text**: "Point. Shoot. Read. Take a photo of any inscription and get readable text."
**Pipeline** (4 horizontal steps with arrows):
1. Camera — CameraX native capture
2. Detect — Locates individual glyphs in the image
3. Classify — Identifies each Gardiner code (171 classes)
4. Translate — Transliteration + EN/AR translation
**Three stat cards below pipeline**:
- `98.2%` Classification accuracy (171 Gardiner sign classes)
- `3 Models` Working together (cross-validate and correct)
- `< 5s` Per scan (detect + classify + translate)
**Phone mockup on right side**: Camera viewfinder with scan button, callout "CameraX native integration"
**Animation**: Pipeline steps fade in sequentially (0.15s stagger). Stats count up when visible.

### SLIDE 05 — Dictionary & Write
**Layout**: Split (grid-2). Left: Dictionary. Right: Write.
**Badge**: `LEARN & CREATE`
**Heading**: "Dictionary & [gold]Write[/gold]"
**Left side (Dictionary)**:
- 1,023 Gardiner Signs
- All 26 categories (A through Aa)
- Phonetic values, meanings, search by name/sound/description
- Offline via Room database
- Phone mockup: dictionary list view
**Right side (Write)**:
- Card showing: Type "Eternal life" → hieroglyph output 𓇳𓏤𓋹𓈖𓐍 → pronunciation "ankh djet"
- Smart transliteration (word-level, not letter substitution)
- TTS pronunciation: transliterationToSpeech() maps x→kh, S→sh, etc.
- Phone mockup: write screen with result

### SLIDE 06 — Explore + Thoth + Stories (compressed)
**Layout**: Three-column. Each feature gets a column.
**Badge**: `MORE FEATURES`
**Heading**: "Explore. Ask. [gold]Read.[/gold]"
**Column 1 — Explore**:
- 260+ heritage sites
- 93.8% landmark identification (52 sites)
- Smart categories, 1,220+ curated images
- Mini phone mockup: landmark card list
**Column 2 — Thoth AI Chat**:
- AI Egyptology guide named after the god of knowledge
- Multi-turn conversation with memory
- Edit messages (ChatGPT-style), retry on failures
- Mini phone mockup: chat bubble interface
**Column 3 — Stories**:
- 12 mythology tales
- Scene illustrations, voice narration, chapter progress
- Story badges (Eye of Horus, Isis & Osiris, etc.)
- Mini phone mockup: story reader view
**Animation**: Columns slide in from bottom, staggered (reveal, d2/d4/d6).

### SLIDE 07 — Live Demo
**Layout**: Minimal. This is a pause point.
**Background**: Gold dot pattern, glow ring centered, meteors.
**Center content**:
- Large hieroglyph: 𓂀 (2.5rem, gold)
- Heading: "[gold]Live Demo[/gold]"
- Lead text: "Let me show you."
- Three demo items as badges: "Scan a hieroglyph" / "Ask Thoth a question" / "Write your name in hieroglyphs"
- Subtle instruction: "(Switch to phone)"
**This slide exists so Nader picks up his phone and does a live demo.**
**Keep it sparse. The phone IS the content.**
**Animation**: Everything reveals from blur-in. Slow, dramatic.

### SLIDE 08 — System Architecture
**Layout**: Full-width diagram.
**Badge**: `ARCHITECTURE`
**Heading**: "How It [gold]Works[/gold]"
**Two-level architecture**:

**Level 1 — System overview (top section)**:
A horizontal flow diagram:
```
[Android App] ←→ [Backend API (FastAPI)] ←→ [ML Models (ONNX)]
                        ↕
                   [Database]
```
- Android App: Kotlin, Compose, Hilt
- Backend API: FastAPI on Hugging Face Spaces
- ML Models: 3 models (detector, classifier, landmark)
- Database: landmark data, dictionary, user data
- Show that Android and Web App share the same backend

**Level 2 — App module structure (bottom section)**:
Layered box diagram:
```
┌─────────────────── APP ───────────────────┐
├──────────── FEATURE MODULES ──────────────┤
│ auth │ chat │ scan │ dictionary │ explore │
│ feedback │ landing │ settings │ stories  │
│ dashboard                                 │
├──────────── CORE MODULES ─────────────────┤
│ common │ data │ domain │ network │        │
│ database │ designsystem │ ml │ ui │       │
│ firebase                                  │
└───────────────────────────────────────────┘
```

**Build these as CSS boxes with borders, not images.** Color-code: gold border for feature modules, sand border for core modules.
**Animation**: System overview fades in first. Module diagram reveals 0.4s later. Pulse dots on the connection arrows between system components.

### SLIDE 09 — Tech Stack
**Layout**: Single tech table (full width) with elegant styling.
**Badge**: `TECH STACK`
**Heading**: "Built [gold]With[/gold]"

**Table rows**:
| Layer | Technology | Why |
|-------|-----------|-----|
| Language | Kotlin 2.1 | Type-safe, concise, Jetpack-native |
| UI | Jetpack Compose + Material 3 | Declarative, Egyptian gold theme |
| DI | Hilt (Dagger) | Compile-time injection, module scoping |
| Database | Room | Offline dictionary, chat history |
| Network | Retrofit + kotlinx.serialization | Type-safe API calls, JSON parsing |
| Images | Coil 3 | Kotlin-first, shared OkHttp client |
| Camera | CameraX | Native camera scanning |
| ML | ONNX Runtime | On-device inference, privacy |
| Auth | Firebase + Google Sign-In | OAuth, analytics |
| Navigation | Type-safe Compose Nav 2.8+ | Compile-time route safety |
| Target | API 35 (min 26) | Android 8.0+ coverage |

**Animation**: Table rows reveal from top to bottom with stagger (0.06s per row).

### SLIDE 10 — ML Deep Dive
**Layout**: Left: training pipeline. Right: results table + ONNX flow.
**Badge**: `MACHINE LEARNING`
**Heading**: "AI On [gold]Device[/gold]"

**Left side — Three model cards**:
1. **Hieroglyph Classifier**: 98.2% accuracy, 171 Gardiner classes. CNN architecture. Transfer learning.
2. **Landmark Identifier**: 93.8% accuracy, 52 Egyptian sites. Trained on curated site images.
3. **Glyph Detector**: Locates individual signs in inscription photos. Bounding box output.

**Right side — Pipeline**:
```
Training (TensorFlow/Keras)
    ↓
Export (ONNX format)
    ↓
Android Integration (ONNX Runtime)
    ↓
On-device inference
```

**Privacy callout card** (gold border): "Images never leave the device. All inference runs on-phone via ONNX Runtime."

**Data points to include** (evaluators WILL ask):
- Training framework: TensorFlow / Keras
- Model format: ONNX
- Runtime: ONNX Runtime for Android
- Dataset: Gardiner sign images (171 classes), Egyptian site photos (52 sites)

**Animation**: Model cards stagger left, pipeline steps stagger right.

### SLIDE 11 — Challenges & Solutions
**Layout**: Three problem/solution card pairs.
**Badge**: `REAL ENGINEERING`
**Heading**: "What [gold]Broke[/gold] (and how I fixed it)"

**Three challenges** (each as a card with problem → solution):

1. **Auth token leak**
   - Problem: AuthInterceptor was sending Bearer tokens to external URLs (Wikipedia image CDN).
   - Fix: Scoped interceptor to API base URL only. External requests bypass auth.

2. **Chat crash on history load**
   - Problem: Duplicate message IDs caused crashes when loading conversation history. IDs used `sessionId_timestamp` but messages sent in the same millisecond collided.
   - Fix: Changed ID format to `sessionId_index_timestamp`. Array index guarantees uniqueness.

3. **18-module build complexity**
   - Problem: Feature modules need core modules but shouldn't know about each other. Cross-feature navigation without direct dependencies.
   - Fix: Clean architecture with domain layer as the contract. Type-safe Compose Navigation routes. Hilt module scoping.

**Humanizer note**: These must read like incident reports, not humble-brags. State what broke. State how you fixed it. Stop.
**Animation**: Cards reveal-left for problem, reveal-right for solution, paired.

### SLIDE 12 — Competition & Numbers (merged)
**Layout**: Top half: comparison table. Bottom half: stat blocks.
**Badge**: `WHAT EXISTS VS WADJET`
**Heading**: "By The [gold]Numbers[/gold]"

**Comparison table (top)**:
| Feature | Google Lens | Hieroglyphic Apps | Wikipedia | Wadjet |
|---------|-------------|-------------------|-----------|--------|
| Scan hieroglyphs | General OCR | Some | No | 98.2% accuracy, 171 classes |
| Dictionary | No | Limited | Text only | 1,023 signs, offline |
| Write hieroglyphs | No | Some | No | Smart transliteration + TTS |
| Explore landmarks | No | No | Scattered | 260+ sites, categorized |
| AI chat guide | No | No | No | Thoth (multi-turn, bilingual) |
| Mythology stories | No | No | Articles | 12 interactive tales |
| Bilingual EN/AR | No | Rare | Separate | Full RTL support |
| Offline support | Partial | Varies | No | Dictionary + ML inference |

**Stat blocks (bottom, 2 rows of 4)**:
- Row 1: `1,023` signs / `260+` sites / `12` stories / `1,220+` images
- Row 2: `98.2%` glyph accuracy / `93.8%` landmark accuracy / `EN/AR` bilingual / `~88 MB` APK

**Animation**: Table fades in first. Stat numbers count up (animated counter JS) when visible.

### SLIDE 13 — Future Vision
**Layout**: 6 cards in 3x2 grid.
**Badge**: `WHAT'S NEXT`
**Heading**: "Future [gold]Vision[/gold]"
**Six cards**:
1. 𓏲 Live Camera Scan — Real-time hieroglyph recognition. AR overlay on camera feed.
2. 𓂋 Deep Translation — Full grammatical parsing. Understanding sentences, not just individual signs.
3. 𓊹 Offline ML — Ship ONNX models with the APK. Scan works without internet.
4. 𓉐 Museum Partnerships — Scan museum exhibits for context, stories, and pronunciations.
5. 𓏛 More Stories — Expand to 50+ stories. Pharaohs' chronicles, daily-life tales.
6. 𓋔 Education Mode — Structured curriculum for schools. Lesson plans, quizzes, progress tracking.
**Animation**: Cards stagger in (reveal-scale, d1 through d6). Meteors in background.

### SLIDE 14 — Thank You
**Layout**: Centered, immersive. Same energy as title slide (bookend).
**Background**: Gold dot pattern + meteors + centered glow ring.
**Content**:
- Wadjet logo (base64)
- "Thank You" with gold gradient and hieroglyphs: 𓋹
- Nader Mohamed
- BSc Artificial Intelligence, Kafr El Sheikh University
- DEPI Software Development Track, 2026
- Two buttons: "Download APK" + "Try Web App"
- URLs shown as text below buttons (for PDF readability)
- GitHub: github.com/Nadercr7
- Decorative hieroglyph line
**Animation**: All content reveals from blur-in, slow and elegant. Final slide should feel like a curtain call.

---

## Narrative Arc & Visual Rhythm

Following the ppt-agent-skills narrative arc principle:

```
Attention curve mapped to slides:

HIGH   ▓▓▓▓
       ▓    ▓▓           ▓▓▓▓
       ▓      ▓▓       ▓▓    ▓▓
       ▓        ▓▓   ▓▓        ▓▓▓▓
       ▓          ▓▓▓▓
       └──┬──┬──┬──┬──┬──┬──┬──┬──→ slides
         01 02 03 04  07 08 10 12 14
         ↑  ↑     ↑   ↑     ↑  ↑  ↑
        Hook Problem Hero Demo ML Nums End
```

**Visual density alternation** (controls cognitive load):
| Slide | Density | Role |
|-------|---------|------|
| 01 Title | LOW (25%) | Breathing room, set tone |
| 02 Problem | MEDIUM (55%) | Build tension |
| 03 Solution | MEDIUM-HIGH (65%) | Payoff |
| 04 Scan | HIGH (75%) | Hero feature + pipeline |
| 05 Dict/Write | MEDIUM (60%) | Feature detail |
| 06 Explore/Thoth/Stories | HIGH (70%) | Compressed features |
| 07 Live Demo | LOW (20%) | BREATHING POINT. Reset attention. |
| 08 Architecture | HIGH (75%) | Technical credibility |
| 09 Tech Stack | MEDIUM (60%) | Table, scannable |
| 10 ML Deep Dive | HIGH (75%) | AI credibility |
| 11 Challenges | MEDIUM (60%) | Story, engaging |
| 12 Numbers | HIGH (70%) | Data wall, impressive |
| 13 Future | MEDIUM (55%) | Forward-looking |
| 14 Thank You | LOW (25%) | Closure, bookend |

---

## Arabic Script Specification (script-ar.md)

### Language rules
- Egyptian Arabic dialect (عامية مصرية), NOT Modern Standard Arabic (فصحى)
- Write as if Nader is talking to professors who are also Egyptian
- Casual but respectful. Not reading from a paper.
- Technical terms stay in English (Kotlin, ONNX, Compose, etc.) with Arabic explanation after first mention
- Hieroglyph Unicode characters can be included inline

### Structure per slide
```markdown
## Slide XX — [English title]

[3-8 sentences in Egyptian Arabic]

**Key points**:
- Point 1
- Point 2

**Transition**: [Line that bridges to the next slide]
```

### Special sections in the script
- `[PAUSE]` markers where Nader should stop and let the slide breathe
- `[POINT TO SCREEN]` markers where he should gesture
- `[PICK UP PHONE]` for the live demo slide
- `[WAIT FOR QUESTION]` after the challenges slide (evaluators always ask here)

### Q&A Prep section at the end
Common evaluator questions with suggested answers in Egyptian Arabic:
1. "ليه مش استخدمت Google Lens?" (Why not Google Lens?)
2. "الداتا جت منين؟" (Where did the training data come from?)
3. "ايه الموديل اللي استخدمته؟" (What model architecture?)
4. "ليه ONNX مش TFLite?" (Why ONNX not TFLite?)
5. "الأبلكيشن بيشتغل offline?" (Does the app work offline?)
6. "ايه اللي كان صعب؟" (What was the hardest part?)
7. "ايه الخطة الجاية؟" (What's the future plan?)

---

## JavaScript Requirements

### Reuse from web deck (adapt, don't rewrite)
- IntersectionObserver for scroll-triggered reveals
- Progress bar update on scroll
- Nav dots generation and active state
- Keyboard navigation (↑↓ Space)
- Slide number display

### New JS additions
1. **Animated counters**: Count up from 0 when stat elements enter viewport. Easing: easeOutExpo. Duration: 1.5s. Trigger once (don't re-animate on re-entry).
2. **Parallax backgrounds**: Background decorative elements (dots, glow rings) transform at 60% scroll speed. Use `transform: translateY()` on scroll, not `background-position` (better perf).
3. **Phone mockup screen transition**: When a phone mockup enters viewport, internal screen content fades in with 0.2s delay after the frame.

### Print-to-PDF compatibility
```css
@media print {
    html { scroll-snap-type: none; }
    .slide {
        height: 100vh !important;
        page-break-after: always;
        page-break-inside: avoid;
    }
    .slide:last-of-type { page-break-after: avoid; }
    .progress-bar, .nav-dots, .keyboard-hint { display: none !important; }
    .reveal, .reveal-blur, .reveal-scale, .reveal-left, .reveal-right {
        opacity: 1 !important;
        transform: none !important;
        filter: none !important;
    }
    .meteor, .glow-ring, .dot-pattern { display: none; }
    body { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
}
```

---

## Execution Checklist (for the implementing chat)

1. [ ] Read all ppt-agent-skills principle files listed above
2. [ ] Read the humanizer instructions (already in environment)
3. [ ] Scan Hover.css, motion-primitives, animate-ui, magicui for animation patterns
4. [ ] Build HTML file: CSS first (all variables, components, animations), then slides 01-14, then JS
5. [ ] Apply humanizer 3-pass process to ALL English text in the deck
6. [ ] Test: every slide has DEPI footer, slide number, at least one animation
7. [ ] Test: phone mockups render correctly (CSS-only, no images needed)
8. [ ] Test: print preview shows one slide per page, no broken layouts
9. [ ] Write Arabic script (script-ar.md) with all markers and Q&A section
10. [ ] Final review: check for banned words, em dashes, AI patterns in all text

---

## Key Facts for Content (verified numbers)

| Fact | Value | Source |
|------|-------|--------|
| Gardiner sign classes | 171 | ML model |
| Classification accuracy | 98.2% | ML model |
| Egyptian sites classified | 52 | ML model |
| Landmark accuracy | 93.8% | ML model |
| Total dictionary signs | 1,023 | Gardiner Sign List |
| Gardiner categories | 26 (A through Aa) | Gardiner Sign List |
| Heritage sites | 260+ | Curated database |
| Landmark images | 1,220+ | Curated from Wikimedia |
| Mythology stories | 12 | Content database |
| App modules | 18 (1 app + 10 feature + 7 core) | build.gradle.kts |
| APK size | ~88 MB | Release build |
| Min SDK | 26 (Android 8.0) | build.gradle.kts |
| Target SDK | 35 | build.gradle.kts |
| JVM target | 17 | build.gradle.kts |
| Kotlin version | 2.1 | libs.versions.toml |
| Backend | FastAPI on Hugging Face Spaces | Deployment |
| Backend URL | nadercr7-wadjet-v2.hf.space | Live |
| Languages | English + Arabic (RTL) | App |
| Compose Navigation | 2.8+ (type-safe) | libs.versions.toml |
