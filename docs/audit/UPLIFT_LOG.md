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
- 3D mask: original model built (`mask.glb` 1.41 MB, 48-frame turntable 0.78 MB, hero fallback, gold `#C8A24B`, ~67k tris, 100% procedural, we own it). First render read too cartoonish for a premium hero; art direction being iterated before sign-off. NOT yet integrated.
- Logo Direction B, U3 nav icons, U7 onboarding, H1 Android scribe voice, C1 EN/AR switcher: pending.

## Phase 8C — Deep bets: pending.
