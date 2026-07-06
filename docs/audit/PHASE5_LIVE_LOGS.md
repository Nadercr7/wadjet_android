# Phase 5 — live production verification logs (2026-07-07)

Production: https://nadercr7-wadjet-v2.hf.space. Emulator: Pixel_8 Play-Store image, debug-signed build pointed at the production BASE_URL (default). Test account wadjetaudit11803@web-library.net.

## Prod endpoint sanity (curl)
- `GET /api/health` → 200
- `GET /.well-known/assetlinks.json` → 200, package `com.wadjet.app`, sha256 = RELEASE cert `1C:94:2D:…:D9:02` (production-only, debug fp dropped as advised)
- `POST /api/auth/firebase {}` → 422 (endpoint live)
- `GET /api/images/pexels-search?query=giza` → 401 (live, auth-gated)
- `POST /api/push/send {...}` → 401 (live, auth-gated)

## 1. AUTH (email/password → Firebase-primary exchange)
```
FA-SVC: Logging event: name=login, params={method=password, ga_screen=Welcome}
(app reached Landing "Welcome back"; POST /api/auth/firebase 200 against prod)
```
Cold restart (force-stop + relaunch) → app resumed logged-in on Landing (re-prompted notifications on launch = the isLoggedIn launch path), session persisted.

## 2. Firestore progress mirror (rules now deployed)
Advanced a chapter; NO `PERMISSION_DENIED`, NO `Failed to save story progress to Firestore` (the catch logs on failure; silence = success). Room row (WAL-aware pull) confirms the triple-write landed:
```
story_progress: [('creation-from-nun', 1, 1, 0)]
```
`story_completed` analytics also fired:
```
FA-SVC: name=story_completed, params={story_id=creation-from-nun}
```

## 3. FCM token registry (rules now deployed)
```
WadjetFirebaseMessaging: FCM token refreshed: dVOEB5BOSd
FcmTokenRegistrar: FCM token registered for user 0nYKZA: dVOEB5BOSd…
```
No `FCM token write failed`, no `PERMISSION_DENIED` (the log line prints only AFTER `.set().await()` succeeds → users/{uid}/fcm_tokens write allowed).

## 4. App Links
Prod assetlinks carries the RELEASE cert only; the emulator build is DEBUG-signed (`7A:83:…`):
```
pm get-app-links: nadercr7-wadjet-v2.hf.space: 1024   (no verified state — correct: debug cert not in statement)
```
This is the security model working, not a defect — a release-signed (Play) build WOULD auto-verify. Functional deep-link path proven with the domain approved:
```
am start -a VIEW -d https://nadercr7-wadjet-v2.hf.space/stories/osiris-myth
→ in-app StoryReader "The Golden Age" (analytics screen_class=StoryReader, not a browser)
```
(First attempt used a wrong slug `the-osiris-myth`; the real slug is `osiris-myth` — routing was correct, the URL was mine.)

## 5. Pexels proxy (key now set in prod)
```
ThumbnailResolver: Pexels resolved 'great-pyramids-of-giza' -> https://images.pexels.com/photos/31133003/…
ThumbnailResolver: Pexels resolved 'abu-simbel'            -> https://images.pexels.com/photos/5488754/…
```
APK contains ZERO Pexels key (grep of classes*.dex + strings for the key literal = 0). The only path to these URLs is the backend proxy `/api/images/pexels-search`.

## 6. Analytics (FA verbose)
All emitted: `login` (method=password), `screen_view` (Landing→Stories→StoryReader→Scan…), `story_completed` (story_id=creation-from-nun), `scan_completed` (detection_source=on_device_onnx, num_detections=6).

## 7. Regression
- Offline on-device scan (airplane): `Server scan unreachable — running on-device ONNX fallback` → `On-device scan: 6 glyphs in 4341+751 ms` → UI "Detected (6)", offline banner, source on_device_onnx.
- Offline story read (airplane): Osiris reader rendered from cache with "No internet connection".
- Full tab tour (online): Home/Hieroglyphs/Explore/Stories/Thoth all rendered. **0 FATAL** across the whole session (crash buffer clean).

## Google Sign-In
Flow launches correctly end-to-end on the app side:
```
CredentialManager: starting executeGetCredential with callingPackage: com.wadjet.app
CredentialManager: Option TYPE_GOOGLE_ID_TOKEN_CREDENTIAL meets all filtering conditions
Auth.Api.Credentials: [GetGoogleIdOperation] Operation started.
```
Cannot COMPLETE on this emulator: no Google account is provisioned, and interactively adding one (Google login + 2FA) can't be automated or faked. Production also requires the release-signed build for the OAuth cert audience. **NEEDS-PHYSICAL-DEVICE** (Play + a signed-in Google account + release signing). No crash — the flow degrades cleanly.
