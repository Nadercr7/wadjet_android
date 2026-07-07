# Wadjet v1.2.0 — Release readiness (uplift/premium)

Version: **1.2.0** (versionCode **3**), min SDK 26, target SDK 35. Release build is R8
minified + resource-shrunk, signed with `wadjet-release.jks`, production `BASE_URL =
https://nadercr7-wadjet-v2.hf.space`. Dark-only by design. "Made By Mr Robot" attribution
preserved.

This release is the **premium uplift**: Direction-B Eye-of-Wadjet identity, the 3D
golden-Eye-on-lapis showpiece (web `<model-viewer>` + Android turntable), the cartouche /
ankh / hieroglyph accents, first-run onboarding, and a full bilingual humanization pass.

---

## What's new — v1.2.0 (store "What's new", ≤500 chars)

**English**
> A whole new look. Meet the Eye of Wadjet: a hand-built 3D golden relief, a refreshed
> gilded palette, and bespoke Egyptian icons throughout. A first-run tour now walks you in,
> Thoth speaks with more warmth, and the Arabic reads in clean Modern Standard Arabic with
> proper number agreement. Faster, calmer, and more at home in 5,000 years of heritage.

**العربية**
> حُلّة جديدة تمامًا. تعرّف على عين واجِت: نقش ذهبي ثلاثي الأبعاد مصنوع يدويًا، ولوحة ألوان
> مذهّبة، وأيقونات مصرية خاصة في كل مكان. جولة ترحيبية تأخذ بيدك عند أول تشغيل، وتحوت يتحدث
> بدفء أكبر، والعربية الآن بفصحى سليمة مع مطابقة صحيحة للأعداد. أسرع، وأهدأ، وأقرب إلى روح
> خمسة آلاف عام من التراث.

---

## Play Store listing (humanized)

**App title (≤30 chars):** `Wadjet: Egypt & Hieroglyphs`

**Short description (≤80 chars, EN):**
> Scan hieroglyphs, explore Egypt's landmarks, and ask Thoth anything, with AI.

**Short description (AR):**
> امسح الهيروغليفية، واستكشف معالم مصر، واسأل تحوت أي شيء، بالذكاء الاصطناعي.

**Full description (EN):**
> Point your camera at any hieroglyphs and Wadjet reads them back to you, sign by sign, then
> transliterates and translates the whole inscription in English or Arabic.
>
> • **Scan & translate** — an on-device model detects each glyph and gives its Gardiner code,
>   sound, and meaning. Core scanning even works offline.
> • **Explore Egypt** — wander 260+ temples, tombs, and treasures, each with its story, its
>   map, and its meaning. Snap a photo and let AI name the monument.
> • **Ask Thoth** — the scribe of the gods answers your questions about any glyph, ruler, or
>   place you meet.
> • **Learn & write** — a 1,000+ sign Gardiner dictionary, step-by-step lessons, and a writer
>   that turns your words into hieroglyphs.
> • **Stories of the Nile** — interactive mythology with narration and glyph lessons woven in.
>
> Bilingual throughout (English + Modern Standard Arabic, full right-to-left). No account
> needed to start exploring.

**Full description (AR):** mirror of the above in MSA (source of truth: in-app strings, all
humanized this release).

---

## Data safety (Play Data-safety form)

- **Account** (email, display name): collected for sign-in and saving progress. Encrypted in
  transit. Deletable by the user (sign-out + account deletion path).
- **Photos** (scan/identify): processed for hieroglyph/landmark recognition. Sent to the
  backend only when server mode is used; on-device scanning keeps the image local. Not stored
  for advertising.
- **App activity / analytics**: Firebase Analytics screen-views + Crashlytics crash reports
  (no advertising ID resale).
- **No data sold to third parties. No ads.**
- Data is encrypted in transit (HTTPS). Users can request deletion.

---

## Deep links (verified in NavGraph)

- `wadjet://landmark/{slug}` → Landmark detail
- `wadjet://story/{storyId}` → Story reader
- `https://nadercr7-wadjet-v2.hf.space/stories` → Stories list (App Link, autoVerify)
- `https://nadercr7-wadjet-v2.hf.space/stories/{id}` → Story reader (App Link)

App Links auto-verify depends on the backend serving `/.well-known/assetlinks.json` with the
**release** SHA-256 (FIREBASE_RUNBOOK §9 / ANDROID_CERT_SHA256 env). Until deployed, links open
via the chooser; the `wadjet://` scheme always works.

---

## Accessibility

- Dark-only, high-contrast: body text `#B4B4B4` (~8:1), dim `#8C8C8C` (~5.8:1) on Night.
- Touch targets ≥48dp on buttons and nav; onboarding controls included.
- Content descriptions on icon buttons, nav items, the 3D turntable, and accent art.
- Reduced-motion honored (turntable/sway freeze when `ANIMATOR_DURATION_SCALE == 0`).
- Font-scaling safe: feature cards use `heightIn(min=…)` to grow at large scale.
- RTL: no baked-in directional arrows; layouts mirror.

## Screenshots (capture for the listing — 6–8)

1. Onboarding "Read the walls"  2. Welcome (3D Eye hero)  3. Hieroglyphs hub
4. Scan result (glyphs + translation)  5. Explore landmarks grid  6. Landmark detail
7. Ask Thoth chat  8. Story reader.  (Captured in the emulator tour; add EN + one AR/RTL.)

## Not built this release (proposed, needs sign-off)

- **Play Asset Delivery** for an optional real-time 3D model (asset pack) instead of the
  pre-rendered turntable — keeps the base APK small while offering interactive 3D on capable
  devices. Sizable; proposed for a follow-up.
