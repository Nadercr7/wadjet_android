# UPLIFT_LOG.md — execution log for the premium uplift (branch `uplift/premium`)

Item -> change -> proof. Both repos on `uplift/premium`, branched from the shipped v1.1.0 baseline. Green-build invariant held before and after every commit (Android `:app:assembleDebug`; Web `pytest` + local boot). Shipped branch never touched. No deploy / APK rebuild by the agent (supervisor triggers those).

Bespoke gold locked for BOTH platforms and the 3D model: **`#C8A24B`** (light `#E2C97E`, dark `#9E7C33`, muted `#7E661F`), replacing the generic template-gold `#D4AF37`.

---

## Phase 8A — Quick wins

### Web (Wadjet-v3-beta)  — pytest 244 passed; welcome hero screenshotted EN + AR (RTL); 0 Google-Font network requests

| id | change | files | proof |
|----|--------|-------|-------|
| U10 | Self-host Playfair/Inter/JetBrains fonts (woff2 from the bundled Android ttf set); drop Google Fonts CDN | `app/static/fonts/*.woff2` (6, 461 KB), `input.css` @font-face, `base.html` (removed `<link>`) | Puppeteer network log: `GOOGLE_FONT_REQUESTS: 0`; hero renders Playfair + Inter |
| U6 | Bespoke gilded gold `#C8A24B` ramp + `--gradient-gold` sheen token; folded every scattered gold hex/rgb literal into tokens | `input.css`, `dictionary/landing/lesson_page/scan/welcome/sign_detail_modal/base` templates | compiled `styles.css`: 0 `d4af37`, new gold present; hero screenshot shows warmer gilded gold |
| U2 | Distinct AA-passing muted-text ramp (`text-muted #B4B4B4`, `text-dim #8C8C8C`) | `input.css` @theme | contrast: `#B4B4B4` ~8:1, `#8C8C8C` ~5.8:1 on Night |
| U5 (web half) | `<meta name="theme-color" content="#0A0A0A">` | `base.html` | present in head |
| U8 | Remove dead `this.stopCamera()` (method gone after camera removal) + stray orphaned `<img>` attributes in landmark detail; `font-mono` now actually loads | `scan.html`, `explore.html` | pytest green; no runtime TypeError in resetScan/destroy |
| H3 | Standardize Arabic to MSA (42 keys: welcome/feedback/notes/landing/auth/write/stories) + canonicalize app name to واجِت | `app/i18n/ar.json` | JSON valid; 0 residual colloquial markers; AR RTL screenshot shows MSA CTAs ("ابدأ الآن", "شاهد كيف يعمل") |

Commits: `3608408` [U10 fonts], `ce3b465` [U6/U2/U10/U5/U8 core], `46eff2d` [U8 explore], `f24c69c` [H3].

### Android (Wadjet-Android) — assembleDebug green; emulator screenshots pending

| id | change | files | proof |
|----|--------|-------|-------|
| U6 | Bespoke gold `#C8A24B` ramp in tokens + placeholder drawables + DifficultyBeginner | `WadjetColors.kt`, `ic_placeholder_{error,glyph,landmark,story}.xml` | 0 residual `D4AF37` in source; assembleDebug green |
| U2/K-02 | `TextMuted #B4B4B4`; `Dust #8B7355 -> #B39B76` to clear the self-admitted K-02 WCAG debt | `WadjetColors.kt` | AA-passing values on Night |
| U8 | Remove dead `PermissionDeniedContent` (unreachable, hardcoded English strings) + orphaned `KenBurnsImage` import | `ScanScreen.kt`, `LandmarkDetailScreen.kt` | assembleDebug green |
| H3 | Canonicalize Arabic `app_name` واجت -> واجِت | `app/.../values-ar/strings.xml` | only bare spelling in the repo, now canonical |

### Deferred out of 8A (with reason)
- **U11 BETA-chrome dial-back** -> held for the taste-signoff batch (positioning decision; reversible; no code change made yet).
- **U5 Android branded splash** and **I1 canonical master vector** -> moved into 8B, bundled with **Logo Direction B** (Eye-of-Wadjet primary). All three are the same brand-artwork workstream and are taste-gated; doing splash/vector art before the logo direction is finalized would be rework.

---

## Phase 8B — Showpieces (in progress / pending sign-off)

| id | change | status | proof |
|----|--------|--------|-------|
| I1 | Canonical Eye-of-Wadjet master vector (Direction B primary mark), gold `#C8A24B`: web `wadjet-eye.svg` + tile; Android `ic_logo_eye.xml`; recolored cobra-W `favicon.svg` to bespoke gold | DONE (web `86f0944`; android pending build) | rendered tile PNG reviewed — clean premium Eye of Horus |
| H1 | Android scribe voice: 19 content/Thoth error strings (EN+AR) rewritten warm + in-character | DONE, committed `d0c616c` | assembleDebug green; XML validated |
| C1 | In-app EN/AR switcher | ALREADY IMPLEMENTED (pre-dates this phase; catalog "missing" was stale from Phase-1 audit) | `LanguageSection.kt` wired in `SettingsScreen.kt:191` + quick dialog; `locales_config.xml`; `AppCompatDelegate.setApplicationLocales`; strings present; build green. RTL flip to be functionally confirmed in next signed-in emulator pass |

### 3D showpiece — direction change (supervisor)
- The from-scratch **Tutankhamun mask** (`mask.glb`) was abandoned: procedural primitive sculpting read cartoonish, would undercut premium. No commercially-licensed mask could be sourced (free3d is mixed-license and bot-blocked; Meshy is paid).
- **New direction (owned outright, zero licensing):** author ALL 3D originally as STYLIZED-PREMIUM gilded relief. **Hero = the approved Eye-of-Wadjet logo extruded into a 3D gilded relief** (gold PBR, procedural studio env, slow auto-rotate). Building now (`eye3d` sub-build). Web = `<model-viewer>`/vanilla Three.js lazy-loaded + fallbacks; Android = pre-rendered turntable. To be SHOWN for sign-off before wider rollout.
- After sign-off: a curated 2-4 original gold accents (ankh / cartouche-wordmark / scarab / sun-disk / extruded hieroglyph relief) only where they serve a surface.

### Phase 8B — DONE (all but U7)
Bespoke gold locked: **`#C8A24B`**. 3D palette locked: gold `#C8A24B` + deep-navy lapis `#14294A` + obsidian `#0C0C12`.

| id | change | commits (web / android) |
|----|--------|--------|
| I1 | Canonical Eye master vector + favicon recolor | web `86f0944` / android `dc51a9d` |
| I2 Direction B | Eye is the primary mark across nav/loaders/hero/footer; cobra-W stays the app icon | web `4d47256` / android `83e8f31` |
| D1/D2 3D Eye hero | Stela + lapis inset + gentle sway; web `<model-viewer>` (lazy, fallbacks) + Android 48-frame turntable; deep-navy color pass | web `e0d3872`,`2b346d9` / android `ebc863a`,`ab9ac2d` |
| D4 accents | Ankh + cartouche-"WADJET" + hieroglyph panel (color-matched). Web: footer seal, hub header, scan empty, dashboard. Android: settings seal, hub header | web `9cf37fe`,`5527525` / android `9f9de76` |
| U5 | Branded splash (gold Eye reveal) | android `7cb9e5d` |
| U3 | Bespoke Egyptian nav icons (ankh/papyrus/pyramid/book/ibis) | android `9120eb4` |
| U11 | Em-dash purge (178 web + 38 android) | web `054a01a` / android `a4d30fa` |
| H1 | Android scribe voice (19 strings) | android `d0c616c` |
| C1 | EN/AR switcher | already implemented (verified) |

3D authored 100% procedurally (no external/licensed assets): `eye3d.glb` 0.98 MB, accents 0.5-1.0 MB each, all round-trip verified. Turntable 354 KB in the Android base APK (no 3D engine).

**Remaining: U7 first-run onboarding** (a first-run pager + persisted seen-flag + entry wiring) — deferred for a focused, verified implementation.

## v1.2.0 finalization — the shippable release (uplift/premium → main / master)

Everything below is on `uplift/premium` in both repos, one reversible commit per concern,
green before and after each (Android `:app:assembleDebug`; Web `pytest` 244 passed + local
boot). "Made By Mr Robot" untouched. Dark-only by design.

### Stage 1 — U7 first-run onboarding (Android)  — EMULATOR-VERIFIED end-to-end
Three-page `HorizontalPager` (Read the walls / Walk through Egypt / Ask Thoth) shown once
before Welcome, gated on a persisted `UserPreferencesDataStore.onboarding_seen` flag read
off the splash thread. Bespoke glyph icons in a gold ring, `GoldGradientText` titles,
animated dot indicator, skippable, accessible (labelled icons, page-position announcement,
48dp targets). No auto-advance. Verified live: pages render premium, last page CTA swaps to
"Begin", finishing persists the flag, and relaunch skips straight to Welcome. Web mirror:
the existing public `/welcome` marketing page already serves the same first-visit role.
Commit `492ba20`.

### Stage 2 — Full humanization (H1/H3 completion, H2 plurals)  — BOTH platforms, EN + AR
No dedicated humanization skill exists locally (the `25-Humanization` repo is AI-framework
libraries), so this was a rigorous manual editorial pass. Warm in-character scribe voice on
empty/error/loading states, Thoth replies, onboarding, and tips; MSA Arabic standardized
(residual colloquialisms like "أكتر/زي ما/اتعلم/كمّل" removed); zero em-dashes and a
consistent middot separator replacing " - " hyphen-dashes; "..." → "…". H2 `<plurals>` for
real Arabic number agreement (zero/one/two/few/many/other), wired via `pluralStringResource`
/ `getQuantityString`: chat message-count + relative time, stories glyphs + chapters. STATE.md
"Humanization SKIPPED" line retired. Commits: web `035e8fb`, android `dc2158c`.

### Stage 3 — UI/UX completeness (taste)
Full-codebase audit: 0 em-dashes in user copy, 0 stray hyphen-dashes, all Android user text
localized — except the ScanResult "not found" empty state (the one hardcoded-English
literal), now localized + warmed ("This scan slipped away") EN+AR. Web: middot separator in
the site-wide `<title>`, every per-page title / OG title, and the dictionary tooltip.
Commits: android `af180d5`, web `2a00a07`.

### Stage 4 — 8C deep bets
`docs/audit/RELEASE_1.2.0.md`: humanized Play listing (title, short/full desc EN+AR),
What's-new, Data-safety summary, verified deep-link inventory, a11y notes, screenshot
shot-list. Deep links already implemented (wadjet://landmark, wadjet://story, https App
Links for /stories). a11y verified strong on both (AA contrast, ≥48dp targets, content
descriptions, reduced-motion, RTL `<html dir>` + skip-link + aria-live on web). Play Asset
Delivery real-time 3D recorded as a proposed follow-up, not shipped unreviewed. Commit
`1cd3787`.

### Stage 5 — Verification
Android `assembleDebug` green throughout; web `pytest` 244 passed (108s) post-humanization
and again (244 passed) post-template-changes. Web gallery captured live via headless Chrome
against a local boot: `/welcome` EN (3D Eye hero rendering via SwiftShader WebGL) + AR (RTL,
MSA copy), plus /scan /explore /chat /stories /dictionary. Android gallery: onboarding ×3 +
Welcome (deep-navy turntable) + branded splash. Authed Android screens are build-verified
(Compose exposes no uiautomator nodes for sign-in automation); their humanized copy is shown
live on the public web mirrors.

### Stage 6 — Release prep
`versionCode 3 / versionName 1.2.0`, production BASE_URL, R8 minify + resource-shrink, signed
with `wadjet-release.jks`. Web `landing.android_version` → 1.2.0. Signed APK path/size/sha256,
`static/downloads/wadjet.apk` LFS refresh, clean `uplift/premium → main` (Android) / `master`
(web) merges, and the exact push sequence are in the release report.

## Phase 8C / U1 spacing-dimens tokens — deferred (documented): a formal `dimens` scale is a
low-risk follow-up; current spacing is already consistent via Compose `.dp` literals and the
gold/text token ramps.
