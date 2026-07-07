# PRODUCT_UPLIFT.md — Premium design + capability catalog (PROPOSAL ONLY)

_Date: 2026-07-07. Author: design audit (Creative Director + Principal Product Designer + Android/web engineer)._
_Scope: Wadjet across Android (`Wadjet-Android`, Compose, v1.1.0) and Web (`Wadjet-v3-beta`, FastAPI + Jinja2 + Tailwind v4 + Alpine, deployed on the HF Space)._
_Status: BUILD NOTHING. This is an audit and a prioritized catalog. Execution happens later, on a new branch, one modular/reversible item at a time._

Quality bar applied: the `taste-skill` v2 framework (anti-slop, one locked accent, dark-mode parity, motivated motion only, real assets over fake ones, zero em-dashes in shipped copy, redesign-preserve = audit brand tokens and keep IA / copy voice / analytics events).

---

## 0. DESIGN READ (taste-skill Section 0.B)

Reading this as: a **premium heritage / culture-education product** for curious learners and travellers, with a **museum-grade, editorial-luxury** language, leaning toward the **existing gold-on-obsidian system, refined** (not a generic beige-brass premium-consumer palette, which it already correctly avoids, and not AI-purple). Dials: `VARIANCE 7 / MOTION 6 / DENSITY 3`. Mode: **Redesign-Preserve**. The brand mark, the black-and-gold tokens, the IA, and the Thoth persona are assets to sharpen, not replace.

### What is already good (do not "fix")
- **Real bespoke identity**, shared across platforms: gold cobra-"W" logomark (Android launcher + web `logo.png`) and an Eye-of-Wadjet mark (`core/firebase/.../ic_stat_wadjet.xml`). Named Egyptian-pigment accents (Lapis Lazuli `#26648B`, Carnelian `#A63A28`) tie difficulty tiers to real stones.
- **Bundled custom fonts** (Playfair Display display, Inter body, Cairo Arabic, Noto Egyptian Hieroglyphs) on both platforms. Not stock Roboto.
- **A genuine component + motion system**: Android `core/designsystem/animation/` (BorderBeam, MeteorShower, GoldGradientText, shared-element nav); web `input.css` (component layer, Ken Burns, border-beam, reduced-motion respected globally).
- **Two genuinely premium screens on each platform**: web `scan.html` and `story_reader.html`; Android `LandingScreen.kt` and `StoryReaderScreen.kt`.
- **Strong loading/empty/error discipline** on web and on the best Android screens.
- **The scribe/Thoth voice exists** on web error and chat pages (`error.not_found_message` "The ancient scrolls contain no record of this path.").

### The honest teardown (what reads generic / dated / unfinished)
1. **Both web heroes are flat**: a PNG logo on a blur, centered, no depth, no motion, no real imagery. The showpiece slot is empty (`landing.html:14-47`, `welcome.html:20-47`). Live headline is a generic "Decode the Secrets of Ancient Egypt" over a comma-list subhead.
2. **`#D4AF37` is the single most generic gold on the web** ("template gold"), used identically on both platforms (`WadjetColors.kt:7`, `input.css` `--color-gold`). The brand's own accent reads as a default.
3. **Muddy muted-text ramp**: `TextMuted #8A8A8A`, `TextDim #7E7E7E`, `Dust #8B7355`/web `#A89070` are near-indistinguishable yet all used, so the low-emphasis type hierarchy is effectively flat. Two spots self-admit WCAG-AA failures (the `K-02` comments in `LandingScreen.kt:273`, `ExploreScreen.kt:451`).
4. **The app voice is split**: the warm scribe voice is web-only. Android surfaces a family of ~10 bare dev-strings ("Failed to load stats", "Interaction failed", "Invalid scan ID") and a persona-breaking Thoth error ("Sorry, I encountered an error").
5. **Arabic has real defects**: no `<plurals>` anywhere, so counts render grammatically wrong ("%d رموز" reads "1 رموز"); web Arabic lurches between MSA and Egyptian colloquial ("إيه هو واجت؟"); the app name is spelled three ways (واجِت / واجت / واجيت).
6. **Loaded-but-idle capability**: web ships Atropos, GSAP, ScrollTrigger, and Lenis but uses them only for basic fade-ins; Atropos is loaded and never referenced. Android orphans `KenBurnsImage.kt` (imported, never called).
7. **Unfinished edges**: hardcoded English camera-permission strings in `ScanScreen.kt:305-332`, stray markup in `explore.html:349-350`, a dead `stopCamera()` in `scan.html:976`, a `font-mono` declared but never loaded, a duplicate OG image, `og-default.png == og-wadjet.png`.
8. **Android systemic gap**: no spacing/dimens token scale at all. Every `dp` is a hardcoded literal and the rhythm drifts (`24.dp` here, `20.dp` there, `72.dp` there).
9. **The shell's one un-branded surface**: stock Material bottom-nav icons (`Icons.Outlined.Home/Explore/MenuBook/Chat`) on an otherwise-bespoke app.
10. **Premium-undercut**: pervasive BETA chrome (pill + banner + FAB); an unbranded near-black splash with no animated icon; "Made By Mr Robot" as the Android footer credit; the "works offline" claim contradicted by Google-Fonts-CDN and onnxruntime-CDN loads.
11. **Weight**: the Android install is ~129 MB (mostly bundled ONNX models). That is a conversion tax on a heritage app whose audience includes travellers on mobile data.

---

## Catalog legend

Each item: **ID · Title** then fields: _Workstream · Platform · Observation (file) · Change · User value · Effort (S/M/L) · Risk · APK/size impact · Dependencies · Needs-user-taste_.

Effort: S ≈ up to 1 day, M ≈ 2-4 days, L ≈ 1 week+. APK impact is Android install-size delta; "n/a" = web-only or negligible.

---

## A. HUMANIZATION

### H1 · Give Android the scribe voice (retire the dev-string family)
- Workstream: Humanization. Platform: Android (web already has the voice).
- Observation: `feature/chat/.../strings.xml chat_error_generic_reply` "Sorry, I encountered an error"; the ~10-clone "Failed to load X" family across `feature/dashboard`, `feature/explore`, `feature/stories`; `scan_error_invalid_id` "Invalid scan ID"; `reader_error_interaction` "Interaction failed".
- Change: rewrite the ~30 worst functional strings (errors, empty states, upload prompts) into the warm, plain, in-character voice that web already ships. Keep Thoth in character even on failure. Preserve every placeholder (see §5 of the copy audit).
- User value: one coherent brand personality instead of two; errors that reassure and point to a next step.
- Effort: M. Risk: Low. APK: negligible. Deps: none. Needs-taste: partial (voice-sample sign-off).

### H2 · Arabic plurals + number agreement (systemic)
- Workstream: Humanization. Platform: both.
- Observation: no `<plurals>` / ICU plural anywhere; hardcoded 3-10 plural is wrong for 1/2/11+. Android `hub_glyph_count` "%d رموز", `time_minutes_ago` "منذ %1$d دقائق"; web `scan.share_text` "تم اكتشاف {count} رموز". ~6 count strings affected across both repos.
- Change: Android `<plurals>` quantity strings; web a small pluralization helper in `app/i18n`. English gets correct singular/plural too.
- User value: Arabic that reads as written by a person, not a string-formatter. This is the clearest machine-translation "tell" in the product.
- Effort: M. Risk: Low-Med (i18n plumbing, must not break placeholders). APK: negligible. Deps: none. Needs-taste: n.

### H3 · Standardize the Arabic register (web) + canonicalize the app name
- Workstream: Humanization. Platform: Web (name fix spans both).
- Observation: most of `ar.json` is MSA, but `welcome.*`, `notes.*`, `feedback.*`, `auth.gsi_slow`, `landing.android_title` switch to Egyptian colloquial ("ابدأ دلوقتي", "إيه هو واجت؟"). App name spelled واجِت / واجت / واجيت.
- Change: pick one register (recommend **MSA**, to match Android's uniform MSA) and rewrite the colloquial sections; lock one Arabic spelling of the name (recommend **واجِت**) across both repos.
- User value: a single, consistent Arabic voice; the brand name spelled the same everywhere.
- Effort: M. Risk: Low. APK: negligible. Deps: none. Needs-taste: y (register is a brand decision).

### H4 · Reconcile cross-platform wording + retire placeholder credit
- Workstream: Humanization. Platform: both.
- Observation: feedback title "Send Feedback" (Android) vs "Share Your Feedback" (web); nav label "Explore" (Android, AR "المعالم") vs "Landmarks" (web); author credit "Made By Mr Robot" (Android `footer_credit`) vs "Nader Mohamed" (web); landmark counts claimed as 260+, 190+, and 139+ across surfaces; transliteration term النقحرة (Android) vs النطق (web).
- Change: one wording per concept, one true landmark count (verify against the catalog), one real author/brand credit, one Arabic transliteration term.
- User value: the product stops contradicting itself; trust and polish.
- Effort: S-M. Risk: Low. APK: negligible. Deps: none. Needs-taste: partial (credit + count are facts to confirm).

### H5 · Warm the hero and empty-state microcopy (the 5 demo samples)
- Workstream: Humanization. Platform: both.
- Observation: `scan_upload_title` "Tap to select a hieroglyph image"; `browse_empty_*` "No signs found"; hub descriptions leaning on "powered by AI" filler; web `scan.no_glyphs_desc` "The AI didn't detect any hieroglyphs".
- Change: a targeted pass on the highest-traffic first-impression strings. Sample, do not mass-rewrite.
- User value: the first thing a new user reads sounds like a guide, not a form.
- Effort: M. Risk: Low. APK: negligible. Deps: H1 voice sign-off. Needs-taste: y.

**Five representative before -> after samples** (illustration for approval; placeholders preserved; no em-dashes):

| # | File · key | Before | After (proposed) |
|---|---|---|---|
| 1 | Android `chat` · `chat_error_generic_reply` | "Sorry, I encountered an error. Please try again." | "The papyrus tore before I could finish. Ask me once more." |
| 2 | Android `dashboard` · `dashboard_error_stats` (family of ~10) | "Failed to load stats" | "We couldn't gather your stats just now. Check your connection and try again." |
| 3 | Web `en.json` · `scan.no_glyphs_desc` | "The AI didn't detect any hieroglyphs in this image. Try a photo with clear, visible hieroglyphic inscriptions." | "No glyphs surfaced in this image. Try a sharper, well-lit shot of the inscription." |
| 4 | Android (AR) `app` · `hub_glyph_count` | "%d رموز" (wrong for 1, 2, 11+) | `<plurals>`: one "رمز واحد" · two "رمزان" · few "%d رموز" · many "%d رمزًا" · other "%d رمز" |
| 5 | Web (AR) `ar.json` · `welcome.features_title` | "إيه هو واجت؟" (colloquial + off-spelling) | "ما هو واجِت؟" (MSA + canonical spelling) |

---

## B. UI / UX DEPTH

### U1 · Introduce a spacing/dimens token scale (Android)
- Workstream: UI/UX. Platform: Android.
- Observation: no `Spacing`/`Dimens` object exists (grep = 0). Every screen hardcodes `dp` and drifts (`LandingScreen.kt:112-113` 20/24, `ScanScreen.kt:81` 72, `WelcomeScreen.kt:120` 80).
- Change: add a `WadjetSpacing` scale (4/8/12/16/24/32/48) in `core/designsystem`; migrate screens incrementally. One enforcement point for vertical rhythm.
- User value: consistent rhythm and alignment across every screen; faster future work.
- Effort: M-L (mechanical but broad). Risk: Low. APK: negligible. Deps: none. Needs-taste: n. Foundational.

### U2 · Fix the muted-text ramp and clear the K-02 contrast debt
- Workstream: UI/UX + accessibility. Platform: both.
- Observation: `TextMuted #8A8A8A` / `TextDim #7E7E7E` / `Dust` collapse into one visual weight; `K-02` comments admit WCAG-AA failures on Night.
- Change: collapse to a documented 2-step muted ramp with AA-passing values; retune `Dust`/small-badge text until it passes 4.5:1. Apply the same tokens on web (`--color-text-muted/-dim/-dust`).
- User value: readable secondary text, real hierarchy, accessibility compliance.
- Effort: S. Risk: Low. APK: negligible. Deps: none. Needs-taste: n.

### U3 · Bespoke Egyptian bottom-nav icons (Android)
- Workstream: UI/UX + identity. Platform: Android.
- Observation: `TopLevelDestination.kt:6-9` uses stock `Icons.Outlined.Home/HistoryEdu/Explore/MenuBook/Chat` on an otherwise-bespoke shell.
- Change: 5 custom line-glyph icons (eye/scan, papyrus/hub, obelisk/explore, scroll/stories, ibis-Thoth/chat) as vector drawables, with selected/unselected states.
- User value: the shell finally matches the brand; the last generic surface is gone.
- Effort: M. Risk: Low. APK: negligible (vectors). Deps: I1 (icon language). Needs-taste: partial.

### U4 · Consistent loading treatment (Android)
- Workstream: UI/UX. Platform: Android.
- Observation: Landing/Explore use `ShimmerCardList`; Dashboard cold-load shows only a pull-refresh spinner (`DashboardScreen.kt:107-108`), Settings/Dictionary-tabs show nothing.
- Change: shimmer skeletons matching final layout on Dashboard, Settings, and each Dictionary tab.
- User value: no blank-then-pop; perceived speed and polish parity across screens.
- Effort: S-M. Risk: Low. APK: negligible. Deps: none. Needs-taste: n.

### U5 · Branded splash + theme-color
- Workstream: UI/UX. Platform: both.
- Observation: Android sets only `windowSplashScreenBackground #0A0A0A`, no `windowSplashScreenAnimatedIcon` (`values-v31/themes.xml`); web has no `<meta name="theme-color">`, so mobile browser chrome doesn't match the black UI.
- Change: add the animated splash icon (the Wadjet mark reveal) on Android; add `theme-color` + a real maskable icon check on web.
- User value: a first frame that says "premium brand" instead of "default launcher".
- Effort: S. Risk: Low. APK: negligible. Deps: I1. Needs-taste: partial.

### U6 · Replace template-gold with a bespoke brand gold
- Workstream: UI/UX + identity. Platform: both.
- Observation: `#D4AF37` (the internet-default gold) is the whole accent, used identically on both platforms.
- Change: adopt a slightly warmer, lower-chroma "gilded" gold plus a real two-stop gold gradient token for hero/CTA surfaces (references actual gilding, not a flat fill). Single centralized token swap; audit every gold surface (Color-Consistency-Lock).
- User value: the accent stops reading as a default and starts reading as ours. Highest identity payoff per line changed.
- Effort: S (centralized) but wide blast radius. Risk: Low-Med (touch all gold surfaces; verify contrast). APK: negligible. Deps: I2 direction. Needs-taste: y.

### U7 · First-run value walkthrough
- Workstream: UI/UX. Platform: both (Android first).
- Observation: first-run hits a login wall; the three `FeatureCard`s on `WelcomeScreen.kt:290-309` are static decoration, not onboarding.
- Change: a 3-screen value tour (scan a glyph, explore a landmark, ask Thoth) before/around auth, skippable, analytics-instrumented.
- User value: users understand the value before committing to sign-in; better activation.
- Effort: M. Risk: Low. APK: negligible. Deps: none. Needs-taste: partial.

### U8 · Dead-code and unfinished-edge cleanup
- Workstream: UI/UX hygiene. Platform: both.
- Observation: web Atropos loaded/unused; Android `KenBurnsImage.kt` orphaned; `ScanScreen.kt:305-332` hardcoded English camera strings; `scan.html:976` dead `stopCamera()`; `explore.html:349-350` stray markup; `font-mono` declared not loaded; `og-default.png == og-wadjet.png`.
- Change: remove or wire up each. Either use Atropos on the mask card (see D1 fallback) or drop it.
- User value: no rendered stray markup, smaller/cleaner bundles, no latent bugs.
- Effort: S. Risk: Low. APK: slight reduction. Deps: none. Needs-taste: n.

### U9 · Make the loaded motion stack earn its weight (web hero)
- Workstream: UI/UX. Platform: Web.
- Observation: GSAP + ScrollTrigger + Lenis are fully wired but only run fade-ins; the hero has no scroll-driven moment.
- Change: one motivated scroll-scrubbed hero reveal (pairs with the 3D mask, D1). Motion must communicate hierarchy/story, not decorate. Reduced-motion fallback explicit.
- User value: the first genuinely "wow" moment on the site, using plumbing that already ships.
- Effort: M. Risk: Low. APK: n/a. Deps: D1. Needs-taste: y.

### U10 · Self-host web fonts (honor the offline claim)
- Workstream: UI/UX + performance. Platform: Web.
- Observation: Playfair + Inter load from Google Fonts CDN (`base.html:27`), render-blocking and contradicting the "works offline" copy; onnxruntime also CDN.
- Change: self-host Playfair/Inter woff2 with `font-display: swap` (Cairo already self-hosted); self-host onnxruntime-web.
- User value: faster first paint, true offline, no third-party font dependency.
- Effort: S. Risk: Low. APK: n/a. Deps: none. Needs-taste: n.

### U11 · Dial back BETA chrome for the premium impression
- Workstream: UI/UX. Platform: both.
- Observation: BETA pill in nav + typewriter beta banner + feedback FAB together read "unfinished" on a product being positioned as premium.
- Change: reduce to a single, quiet beta affordance (or remove for the public release); keep the feedback path but make it less shouty.
- User value: the product looks finished. This is a positioning decision as much as a design one.
- Effort: S. Risk: Low. APK: negligible. Deps: none. Needs-taste: y.

### U12 · Refine the type scale for showpiece hierarchy
- Workstream: UI/UX. Platform: both.
- Observation: web h1 caps at `3.5rem` (conservative for a hero); Android `headlineLarge == headlineMedium` (22sp, a redundant step, `WadjetTypography.kt:27-38`).
- Change: extend the display end of the scale (larger, tighter hero heading), de-duplicate the Android step, keep body sizes.
- User value: a hero that carries weight; a cleaner type ramp.
- Effort: S. Risk: Low. APK: negligible. Deps: I2. Needs-taste: partial.

### U13 · Polish the two weakest Android screens (Settings, Dashboard)
- Workstream: UI/UX. Platform: Android.
- Observation: Settings is plain stock list rows with zero motion; Dashboard has only 2 animation sites and no cold-load skeleton.
- Change: grouped setting sections with subtle transitions and iconography; Dashboard skeleton + a lightweight reveal; optional in-app text-size control.
- User value: the low points come up to the level of Landing/Explore.
- Effort: M. Risk: Low. APK: negligible. Deps: U1, U4. Needs-taste: n.

---

## C. IDENTITY / LOGO

**Context:** the identity is real and coherent, not placeholder-grade. This is refinement plus a taste-pick between evolution directions, never a from-scratch rebrand. Preserve recognition.

### I1 · Establish a single canonical master vector for the mark
- Workstream: Identity. Platform: both (production hygiene).
- Observation: the primary mark exists as a raster PNG launcher foreground (`ic_launcher_foreground.png`, all densities), a ~152 KB base64 blob (`logo-base64.txt`), web PNGs (`logo.png` and variants), and a separate, divergent, and unreferenced `favicon.svg`. No single source-of-truth vector.
- Change: redraw the cobra-"W" as one clean master SVG; regenerate all raster/adaptive/favicon/OG assets from it; wire `favicon.svg` in `base.html` or delete it.
- User value: crisp scaling everywhere, true themed-icon fidelity on Android 13+, one file to evolve.
- Effort: S-M. Risk: Low. APK: slight reduction (vector foreground). Deps: none. Needs-taste: n. Prerequisite for I2/U3/U5.

### I2 · Pick an identity evolution direction (taste-pick, supervisor decides)
- Workstream: Identity. Platform: both.
- Observation: the current mark is good but the wordmark (Playfair "safe" pairing) and the accent (`#D4AF37`) are the least distinctive parts.
- Change: choose ONE of three directions below, then apply consistently (mark, wordmark, gold per U6, icon set per U3).
- User value: a brand that looks deliberately designed, not competently defaulted.
- Effort: M (execution once chosen). Risk: Low-Med (recognition). APK: negligible. Deps: I1, U6. Needs-taste: **y (this is the primary taste pick)**.

**Direction A — "Refine the Uraeus" (Preserve, lowest risk).** Keep the cobra-"W". Redraw as crisp vector, tighten the geometry, add the bespoke gold gradient and a subtle engraved/embossed treatment for hero use. Keeps 100% recognition. Best if the brand is happy and just wants finish.

**Direction B — "The Wadjet Eye ascends" (Evolve, medium).** Promote the Eye of Wadjet (already the status icon and the product's namesake) to co-primary. The eye is more instantly readable to a general audience than a cobra-"W", and it is literally what "Wadjet" means. Pair eye + wordmark in a locked lockup; keep the cobra-"W" as the app-icon/monogram. Recommended if the goal is stronger recognition.

**Direction C — "Cartouche wordmark" (Bolder, higher risk).** Set the wordmark inside a cartouche (the oval that framed a royal name in hieroglyphic writing). Very on-theme, distinctive, ownable. Biggest departure from current recognition; strongest "designed" statement. Recommended only if a visible refresh is wanted.

Wordmark note (applies to all three): Playfair Display is acceptable for a heritage brand (serif is justified here), but it is a common choice. A more distinctive high-contrast display face is an optional differentiator, flagged as taste-pick, not a mandate.

---

## D. 3D SHOWPIECE (Tutankhamun golden mask)

**State today:** no 3D anywhere. No Three.js/WebGL/model-viewer, no `.glb`/`.gltf` in either repo. The only shipped "3D" is Atropos CSS tilt (loaded, unused). The web hero slot is empty and the smooth-scroll plumbing (GSAP + ScrollTrigger + Lenis) is already loaded. Android has no 3D engine dependency.

### D3 · Model sourcing + LICENSE (do this FIRST; it gates D1/D2)
- Workstream: 3D. Platform: both (asset).
- The artifact is ancient (public domain as an object), but **every specific 3D scan or model carries its own copyright and license.** Never scrape a model out of a web viewer. Legal paths, ranked:
  1. **Commission an original model** (contract 3D artist, or in-house Blender). Work-for-hire = full ownership, no attribution obligation, exact art-direction to the brand. Cost roughly USD 150-800, lead time 1-3 weeks. **Recommended and cleanest.**
  2. **Buy a royalty-free licensed model** (TurboSquid Standard/CGTrader Royalty-Free). Must confirm the license permits use inside a commercial app; reject anything marked **"Editorial Use Only"** (that forbids commercial products). Cost roughly USD 20-200. Keep the receipt/license in-repo.
  3. **CC0 / CC-BY from an open-access source** (Sketchfab CC0/CC-BY, museum open-access). Verify the specific model's license: CC0 = no attribution; CC-BY = credit required in-app (an "About the 3D model" line). Free.
  - **Avoid:** editorial-only models, unlicensed viewer rips, and AI-generated 3D with murky provenance.
  - **Cultural note:** present a funerary national treasure respectfully (context/credit line); Egypt's antiquities-replica law exists and is worth a legal glance, though a digital homage with a clean model license is the norm.
- User value: a legally clean, on-brand hero asset the product actually owns or is licensed to use.
- Effort: S (procurement) + external lead time. Risk: **the gating risk for the whole showpiece.** APK: the asset itself (target Draco-compressed `.glb` under 3-5 MB). Deps: none. Needs-taste: y (which mask treatment).

### D1 · Web hero showpiece: the mask (recommended first build)
- Workstream: 3D. Platform: Web.
- Observation: empty hero slot (`welcome.html:20-47`), motion stack already loaded.
- Change: drop the mask into the hero center column using **`<model-viewer>`** (Google, Apache-2.0, self-hostable) or **vanilla Three.js** (web is Jinja/Alpine, so react-three-fiber is off the table; port patterns from the local `Repos/react-bits` reference). Scroll-driven reveal/rotate via the already-loaded GSAP + Lenis. **Fallbacks:** a high-res static mask image (or the existing Atropos tilt card, which closes U8's unused-Atropos finding) for `prefers-reduced-transparency`/reduced-motion/no-WebGL/low-end. Lazy-load the `.glb` only on hero intersection, never on the critical path.
- User value: the first thing on the site that reads as genuinely premium, not "well-built template".
- Effort: L. Risk: Med (low-end mobile-web perf, must gate + fallback). APK: n/a (web). Deps: D3. Needs-taste: y.

### D2 · Android landing: a LIGHTER mask treatment (honest APK call)
- Workstream: 3D. Platform: Android.
- Observation: no 3D engine present; a real-time engine would be greenfield. The install is already ~129 MB, minSdk 26 includes low-end devices, and new native `.so` must be 16 KB-page-aligned (recall the ONNX 1.27 saga).
- Change (recommended): **do NOT ship a full 3D engine on Android by default.** Use a pre-rendered turntable of the mask (a short muted looping video or an image-sequence sprite) for the hero moment. It gives 90% of the "wow" at a fraction of the size and zero low-end perf cliff. If a real rotatable scene is wanted later, gate SceneView (`io.github.sceneview`, Apache-2.0, Filament-backed, Compose-friendly) behind an `isLowRamDevice` + build/remote flag, and deliver the model + native libs via **Play Asset Delivery** so the base APK does not grow (ties to C4/C-size).
- User value: a premium landing moment on Android without a 15 MB+ install tax or a low-end stutter.
- Effort: L. Risk: Med-High if a real engine is used (APK, low-end perf, 16 KB alignment); Low-Med for the pre-rendered route. APK: pre-rendered video/sprite roughly 1-4 MB; full engine roughly 10-20 MB (avoid in base). Deps: D3. Needs-taste: y.

### D4 · Optional reuse: rotatable artifact viewer in Landmark Detail (only if it earns its place)
- Workstream: 3D. Platform: both.
- Observation: Landmark detail currently uses Pexels stock photos; there is no interactive artifact.
- Change: reuse the D1/D2 renderer to let users "handle" a small set of licensed 3D artifacts on landmark pages. Restraint applies: one showpiece plus this one reuse, not 3D everywhere.
- User value: turns "view a landmark" into "handle the artifact"; deepens the museum feel.
- Effort: M (incremental on D1/D2). Risk: Med. APK: per-model asset via Asset Delivery. Deps: D1/D2, D3 (per artifact). Needs-taste: y.

---

## E. CAPABILITIES / PRODUCTIZATION

### C1 · In-app EN/AR language switcher (Android)
- Workstream: Capabilities. Platform: Android.
- Observation: the single biggest Arabic gap (PARITY.md). Strings exist (`quick_settings_language`, `arabic`) but are UNUSED; no `setApplicationLocales`/LocaleManager anywhere; the app follows system locale only. Web already has a toggle.
- Change: per-app locale via AppCompat `setApplicationLocales` (+ `locales_config`); wire the existing Settings strings.
- User value: Arabic users can switch in-app without changing the whole phone. High value, well-scoped.
- Effort: M. Risk: Low. APK: negligible. Deps: none. Needs-taste: n.

### C2 · Deep links for verify/reset + FCM content routing
- Workstream: Capabilities. Platform: Android.
- Observation: email verify/reset complete on web only (PARITY.md); FCM pushes cannot open specific content (no deep links). App Links infra already shipped (E-P8, assetlinks live).
- Change: extend the existing App Links to verify/reset/content URLs and route FCM payloads to the right screen.
- User value: email links and notifications open the app on the right page, not the browser or the home tab.
- Effort: M. Risk: Low. APK: negligible. Deps: E-P8 (done). Needs-taste: n.

### C3 · Real scan progress stages (retire the fake overlay)
- Workstream: Capabilities. Platform: Android (web already staged).
- Observation: E-P3 backlog. The scan progress overlay uses fixed delays, not real server stages; `detection_source` is available.
- Change: drive `ScanProgressOverlay` from server `timing`/`detection_source` (shows the honest "AI Vision" vs "on-device" source).
- User value: honest, informative progress instead of theater.
- Effort: M. Risk: Low. APK: negligible. Deps: none. Needs-taste: n.

### C4 · Slim the install: Play Asset Delivery for ML models
- Workstream: Productization + performance. Platform: Android.
- Observation: ~129 MB install, dominated by bundled ONNX models (`core/ml`).
- Change: move the on-device models to **Play Asset Delivery** (install-time or fast-follow) or a dynamic feature; the base APK/AAB drops toward a lean core. This is also the delivery channel for any 3D assets (D2/D4).
- User value: dramatically smaller first download on a heritage app whose users are often on mobile data. Biggest single conversion lever.
- Effort: L. Risk: Med (delivery plumbing, offline-first must still work). APK: large reduction to base install. Deps: none (enables D2/D4). Needs-taste: n.

### C5 · On-device landmark identify fallback
- Workstream: Capabilities. Platform: Android.
- Observation: E-P10 backlog. Landmark identify is server-only; scan already has an offline on-device path (C2 pattern from the prior phase).
- Change: bundle a small landmark classifier (via Asset Delivery per C4) for offline "what am I looking at", clearly lower-fidelity than the server pipeline.
- User value: the explore feature works at a heritage site with no signal.
- Effort: M. Risk: Low-Med. APK: model via Asset Delivery (not base). Deps: C4. Needs-taste: n.

### C6 · Store-readiness pass
- Workstream: Productization. Platform: both.
- Observation: the polished `scan`/`story_reader` pages are the natural screenshots; web OG images are duplicated; sitemap/robots already exist.
- Change: Play listing (feature graphic, 6-8 screenshots from the best screens, humanized short/full description, data-safety form, content rating); dedupe/refresh OG cards; verify structured data.
- User value: the store page sells the product as well as the product deserves.
- Effort: M. Risk: Low. APK: n/a. Deps: I2, H-pass (copy). Needs-taste: partial.

### C7 · Accessibility pass
- Workstream: Productization. Platform: both.
- Observation: K-02 contrast fails (overlaps U2); glyph-as-icon chrome (raw "→"/"●" in Landing) lacks content descriptions; forced-dark only.
- Change: clear K-02, add TalkBack/`aria` labels to decorative-glyph chrome, verify dynamic text scaling, confirm reduced-motion coverage end to end.
- User value: usable by more people; store and platform compliance.
- Effort: M. Risk: Low. APK: negligible. Deps: U2. Needs-taste: n.

### C8 · Landmark paging via `has_more` (contract-exact) + Room migration test rig
- Workstream: Productization (engineering hardening). Platform: Android.
- Observation: E-P2 (paging off a synthesized `totalPages` instead of backend `has_more`) and E-P5 (no automated Room migration test) backlog.
- Change: adopt `has_more`/Paging 3; add a `MigrationTestHelper` rig over the exported schemas.
- User value: exact backend contract match; migrations proven automatically, not by hand.
- Effort: M. Risk: Low (test-only for the rig). APK: negligible. Deps: none. Needs-taste: n.

### C9 · Content push on publish (finish E-P9 loop)
- Workstream: Capabilities. Platform: backend + Android.
- Observation: the push sender (E-P9) is built but real delivery is blocked only on `GOOGLE_APPLICATION_CREDENTIALS` on the Space (FIREBASE_RUNBOOK §7); no trigger wires content publishing to a push.
- Change: once the service-account env is set, hook `send_push` into new-story/new-landmark publishing (E-P11); route via C2 deep links.
- User value: the notification channel finally does something useful (new content), not just manual console campaigns.
- Effort: S (once env exists). Risk: Low. APK: negligible. Deps: env credential (user), C2. Needs-taste: n.

### C10 · Analytics-informed UX loop
- Workstream: Productization. Platform: both.
- Observation: Analytics is already wired (screen_view, login, scan_completed, story_completed). It is not yet feeding design.
- Change: a funnel review (activation, scan-completion, story-completion drop-off) that targets the empty-state CTAs and onboarding (U7) at the real leak points.
- User value: design effort aimed by data, not guesswork.
- Effort: M. Risk: Low. APK: negligible. Deps: U7. Needs-taste: n.

---

## ROADMAP (impact-to-effort, phased)

### Phase Q — Quick wins (about 1 engineering-week; low risk, high polish-per-effort)
U8 (dead-code purge), U2 (muted ramp + K-02), U5 (branded splash + theme-color), U10 (self-host web fonts), H3/H4 (Arabic register + name + cross-platform wording + retire "Mr Robot"), U6 (bespoke gold, centralized), I1 (canonical master vector), U11 (dial back BETA chrome), U12 (type scale).

### Phase S — Showpieces (about 2-3 weeks; the "premium" leap; 3D model lead time starts on day 1)
D3 (license/procure the mask, FIRST), D1 (web mask hero) + U9 (hero motion), I2 (logo direction pick), U3 (bespoke tab icons), U7 (onboarding), H1/H5 (Android scribe voice + hero copy), C1 (language switcher), U4/U13 (loading consistency + weak-screen polish).

### Phase D — Deep bets (about 3-4 weeks; structural)
U1 (spacing tokens), C4 (Play Asset Delivery, slim the install), D2 (Android mask treatment), H2 (Arabic plurals system), C2 (deep links), C5 (on-device landmark), C6 (store readiness), C7 (a11y pass), C8 (paging + migration rig), C9 (content push), C10 (analytics loop), D4 (optional artifact viewer).

**Rough total:** about 6-8 engineering-weeks for the full catalog, but it is modular and reversible: each item ships independently on the new branch, and the Quick-wins phase alone visibly raises the premium bar in a week. The 3D mask is the long pole because of external model lead time, so procurement (D3) should start before anything else in Phase S.

---

## Guardrail notes carried forward
- Build nothing here. Execution later, new branch, one labelled reversible commit per item, green build on both repos.
- Redesign-Preserve: do not change route slugs, primary nav labels, form-field names, existing analytics event names, or the legal/consent copy without explicit approval.
- 3D: no scraped or "editorial-only" model. State the license path per asset (D3).
- Copy: preserve every placeholder and RTL arrow-flip listed in the copy audit §5; zero em-dashes in shipped strings.
- Any item marked needs-user-taste = y waits for the supervisor's pick before build.
