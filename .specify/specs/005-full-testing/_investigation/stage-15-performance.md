# Stage 15: Performance and Recomposition Profiling

## Cold Startup

| Metric | Value | Assessment |
|--------|-------|------------|
| LaunchState | COLD | Force-stopped before measurement |
| TotalTime | **6,872ms** | **VERY SLOW** — target is <1,000ms for cold start |
| WaitTime | 6,903ms | Includes system overhead |

Root causes (from code analysis):
- Hilt DI initialization of all singletons at startup
- Firebase init (Auth + Crashlytics + Messaging + Analytics)
- Room database init (6 tables + FTS5 + schema validation)
- OkHttpClient + Retrofit service creation (11 services)
- Coil image loader factory
- EncryptedSharedPreferences creation (expensive crypto init)
- No `@EarlyEntryPoint` or lazy initialization patterns

## Frame Rendering Stats

### After General Usage (scrolling Explore list)

| Metric | Value | Assessment |
|--------|-------|------------|
| Total frames | 7,755 | — |
| **Janky frames** | **5,139 (66.27%)** | **CRITICAL** — target is <5% |
| 50th percentile | **57ms** | **4x over budget** (should be <16ms) |
| 90th percentile | 89ms | |
| 95th percentile | 105ms | |
| 99th percentile | 150ms | |
| Missed Vsync | 1,399 | |
| High input latency | 5,172 | |
| Slow UI thread | 4,766 | |
| Slow bitmap uploads | 320 | Image loading related |
| Slow draw commands | 5,063 | |
| GPU 50th percentile | 20ms | Over 16ms budget |

### After Rapid Tab Switching (5 cycles × 5 tabs = 25 switches)

| Metric | Value | Assessment |
|--------|-------|------------|
| Total frames | 521 | — |
| **Janky frames** | **507 (97.31%)** | **CATASTROPHIC** |
| 50th percentile | **57ms** | |
| 90th percentile | **121ms** | |
| 95th percentile | 150ms | |
| 99th percentile | **500ms** | |
| GPU 90th percentile | **4,950ms** | GPU bottleneck extreme |

**NOTE**: These numbers are from emulator (x86_64, 2GB RAM, software GPU). Real device performance will be significantly better. However, the code-level issues below are real and will cause perceptible jank on physical devices too.

## Memory Profile

| State | PSS (KB) | Assessment |
|-------|----------|------------|
| After cold start (idle) | 172,068 (~168MB) | Moderate for Compose app |
| After Explore scrolling | 248,227 (~242MB) | +76MB from image loading |
| After tab switching stress | 259,475 (~253MB) | +11MB from stress (no major leak) |

Java Heap: 23–30MB (well under default 256MB limit)
Graphics: 62–76MB (Compose/Skia rendering buffers)

## Compose Compiler Config

**Missing from `compose_compiler_config.conf`:**
- `LandingUiState` — Landing screen state changes cause full recomposition
- `AlphabetUiState` — Dictionary alphabet tab
- `ChatMessage`, `ConversationSummary` — High churn during streaming
- `Interaction`, `InteractionResult`, `Paragraph`, `GlyphAnnotation` — StoryReader items

## Recomposition Safety Audit

### P0 — Performance-Breaking Issues

| # | Issue | File | Impact |
|---|-------|------|--------|
| 1 | **Chat streaming: `.map{}` over entire message list per chunk** | ChatViewModel.kt:~L195 | For 50 messages + 100 streaming chunks = 5,000 list copies + 5,000 recompositions. Most impactful single issue. |
| 2 | **ScanViewModel bitmap ops on Main thread** | ScanViewModel.kt:~L173 | `BitmapFactory.decodeFile()` + `compress()` without `Dispatchers.IO`. Will cause ANR on slow devices. |
| 3 | **DotPattern: ~4,000 drawCircle calls per frame** | DotPattern.kt:~L43 | Infinite animation drawing individual circles on every frame. Should pre-render to ImageBitmap. |

### P1 — Significant Issues

| # | Issue | File | Impact |
|---|-------|------|--------|
| 4 | **`shineSweep()` on every list item** | ShineSweep.kt | 10+ concurrent infinite `drawWithContent` per visible item in Explore/Stories/Dashboard. Multiplied animation overhead. |
| 5 | **StoriesUiState.filteredStories computed property** | StoriesViewModel.kt:~L30 | Sorts + filters on every recomposition access instead of being a `derivedStateOf` or separate StateFlow. |
| 6 | **DashboardUiState.filteredFavorites computed property** | DashboardViewModel.kt:~L29 | Same issue as #5. |
| 7 | **LandingUiState not in compiler config** | compose_compiler_config.conf | State changes cause full recomposition of Landing screen. |
| 8 | **ZERO animations respect reduced motion** | All animation files | None of the 9 infinite animations check `AccessibilityManager.isEnabled` or `LocalReducedMotionEnabled`. |

### P2 — Moderate Issues

| # | Issue | File | Impact |
|---|-------|------|--------|
| 9 | Chat auto-scroll on every streaming chunk | ChatScreen.kt:~L152 | `animateScrollToItem` fires per chunk — should debounce |
| 10 | MediaPlayer.prepare() on wrong thread | DictionaryVM, ChatVM, ScanVM | Blocking I/O on coroutine default dispatcher |
| 11 | ExploreScreen redundant snapshotFlow | ExploreScreen.kt:~L96 | Double-wrapping derivedState in LaunchedEffect + snapshotFlow |
| 12 | Disk cache percent-based | WadjetApplication.kt:~L57 | 5% of disk = 3-6GB on modern devices. Use fixed 250MB. |
| 13 | No `key` on LazyRow chip items | BrowseTab, ExploreScreen | Category/city filter chips lack keys |
| 14 | Unstable lambdas in LazyColumn items | ExploreScreen, BrowseTab, StoriesScreen | `onSignClick(sign)`, `onToggleFavorite(landmark)` create new lambda per item per recomposition |
| 15 | LandingScreen LazyColumn items lack `key` | LandingScreen.kt | `item {}` blocks without keys — insertions cause recomp of all items below |
| 16 | StoriesScreen stagger animation | StoriesScreen.kt:~L139 | N recompositions (one per story) as `visibleCount` increments |

## Animation Audit

| Animation | File | Infinite? | Battery Impact | Reduced Motion? |
|-----------|------|-----------|----------------|-----------------|
| BorderBeam | BorderBeam.kt | YES (4s loop) | Medium — PathMeasure every frame | **NO** |
| ButtonShimmer | ButtonShimmer.kt | YES (2s loop) | Low | **NO** |
| DotPattern | DotPattern.kt | YES (3s loop) | **HIGH — ~4000 drawCircle/frame** | **NO** |
| FadeUp | FadeUp.kt | No (finite) | None | OK |
| GoldGradientSweep | GoldGradientSweep.kt | YES (4s loop) | Low | **NO** |
| GoldGradientText | GoldGradientText.kt | YES (3s loop) | Low | **NO** |
| GoldPulse | GoldPulse.kt | YES (2s loop) | Low | **NO** |
| KenBurnsImage | KenBurnsImage.kt | YES (20s loop) | Medium — 2 animations driving scale + translation | **NO** |
| MeteorShower | MeteorShower.kt | YES (3s loop) | Medium — 5 meteors with trig calculations | **NO** |
| ShineSweep | ShineSweep.kt | YES (3s loop) | **Medium per card × 10+ visible = HIGH** | **NO** |

**Key concern**: `shineSweep()` applied to cards in LazyColumn/LazyRow means 10+ concurrent infinite transitions when scrolling Explore, Stories, or Dashboard.

## Image Loading

| Config | Value | Assessment |
|--------|-------|------------|
| Coil disk cache | `maxSizePercent(0.05)` of total storage | **Too large** — 3-6GB on 64-128GB devices |
| Coil memory cache | `maxSizePercent(0.25)` of app memory | Standard, OK |
| Image downsampling | **NOT configured** | Full-resolution images decoded into memory |
| OkHttp HTTP cache | **NOT configured** | API JSON responses not cached |
| Crossfade | Enabled globally | OK |

## Summary of Performance Issues (Priority Order)

| Priority | Issue | Category | Effort |
|----------|-------|----------|--------|
| P0 | Chat streaming full-list copy per chunk | Recomposition | Medium |
| P0 | ScanViewModel bitmap ops on Main thread | Threading | Low |
| P0 | DotPattern ~4K drawCircle/frame | Animation | Medium |
| P1 | shineSweep on all list items | Animation | Low |
| P1 | Computed properties on every recomposition (Stories, Dashboard) | Recomposition | Low |
| P1 | LandingUiState missing from compiler config | Recomposition | Trivial |
| P1 | Zero reduced motion support (9 animations) | Accessibility | Medium |
| P1 | Cold start 6.8s | Startup | High |
| P2 | Chat auto-scroll per chunk | Performance | Low |
| P2 | MediaPlayer.prepare on wrong thread | Threading | Low |
| P2 | Disk cache unbounded (percent-based) | Storage | Trivial |
| P2 | Missing LazyColumn/LazyRow keys | Recomposition | Low |
| P2 | Unstable lambdas in list items | Recomposition | Medium |
