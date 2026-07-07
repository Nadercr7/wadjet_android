# FIREBASE_RUNBOOK — exact console steps only you can do

Phase 3 · project **wadjet-android** (number 3523727700) · app `com.wadjet.app`
Everything below is ordered; each step says what breaks while it's not done.
Steps marked ✅ appear to already be done (verified from `app/google-services.json` / live behavior) — re-check, don't redo blindly.

---

## 1. Authentication providers (app sign-in stops without this)

Firebase console → **Build → Authentication → Sign-in method**:

1. **Email/Password** → Enable. ✅ (already working — Phase 2 test account signs in)
2. **Google** → Enable. Set the support email. ✅ (Google Sign-In web client exists)

## 2. SHA fingerprints for Google Sign-In (Google button fails with DEVELOPER_ERROR without this)

Console → **Project settings → Your apps → Android app `com.wadjet.app` → Add fingerprint**.

Two SHA-1s are already registered ✅ (`59f5e443…` and `8a2d0f56…` — debug + one release cert).
If you sign with any other keystore (e.g. Play App Signing key), add its SHA-1 AND SHA-256:

```bash
# from Wadjet-Android repo root — prints SHA-1/SHA-256 for every signing config
./gradlew signingReport
# or for a specific keystore:
keytool -list -v -keystore <path-to-keystore> -alias <alias>
```

Play Console users: **Play Console → Setup → App integrity → App signing** shows the
Play-managed certificate; copy its SHA-1/SHA-256 into Firebase too.

After adding fingerprints: **download the refreshed `google-services.json`** and replace
`app/google-services.json`, then rebuild.

## 3. Deploy Firestore rules (progress mirror + FCM token registry stay PERMISSION_DENIED without this)

The rules now live in the repo: [`firestore.rules`](../../firestore.rules) (+ `firebase.json`, `.firebaserc`).

```bash
npm install -g firebase-tools     # once
firebase login                    # once
cd <Wadjet-Android repo root>
firebase deploy --only firestore:rules
```

Pre-req: the Firestore database must exist — console → **Build → Firestore Database →
Create database** (production mode; the deployed rules take over). If it already exists, just deploy.

Verify: sign into the app, finish a story chapter, then console → Firestore →
`users/<uid>/story_progress/<storyId>` should appear (no more PERMISSION_DENIED in logcat).
After the app requests notification permission you should also see `users/<uid>/fcm_tokens/<token>`.

## 4. Backend deployment env vars (Firebase sign-in exchange + Pexels proxy are 401/503 without this)

On the HF Space (or wherever `Wadjet-v3-beta` branch `feat/firebase-integration` gets deployed) add:

| Secret | Value | Enables |
|---|---|---|
| `FIREBASE_PROJECT_ID` | `wadjet-android` | `POST /api/auth/firebase` (Android session exchange) |
| `PEXELS_API_KEYS` | your Pexels key(s), comma-separated | `GET /api/images/pexels-search` (Android thumbnail fallback) |

No service-account JSON is needed — token verification uses Google's public certs.
(Optional: set `GOOGLE_APPLICATION_CREDENTIALS` to a service-account file to switch
verification to the Firebase Admin SDK, which adds token-revocation checking.)
`firebase-admin` installs from the updated `requirements.txt` automatically.
**You deploy; I have not.** The Android build in this branch requires the deployed
`/api/auth/firebase` endpoint to sign in against production.

## 5. Crashlytics (crash reports never appear without first enabling)

Console → **Release & Monitor → Crashlytics → Enable/Get started** for `com.wadjet.app`.
The SDK+plugin+release Timber tree are already wired app-side. To see the first report:
run a release-ish build, force a crash (or wait for a real `Timber.e`), reopen the app so
the report uploads, then check the dashboard (first report can take a few minutes).

## 6. Analytics (nothing to enable — verify only)

Analytics is on by default for the project. Verify: console → Analytics → **DebugView**,
then on a device/emulator:

```bash
adb shell setprop debug.firebase.analytics.app com.wadjet.app
```

Use the app; you should see `screen_view`, `login`, `sign_up`, `scan_completed`,
`story_completed` events arriving live. Disable with `...analytics.app .none.`.

## 7. Send a real push notification (end-to-end FCM check)

**Option A — your own API (E-P9, preferred).** Needs step 3 done + the backend deployed
with `GOOGLE_APPLICATION_CREDENTIALS` pointing at a service-account JSON
(console → Project settings → Service accounts → Generate new private key; on HF Spaces
upload the file as a secret file and set the env var to its path). Then, signed in as
the admin account (`ADMIN_EMAIL`):

```bash
curl -X POST https://<backend>/api/push/send \
  -H "Authorization: Bearer <admin access_token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"New story!","body":"The Eye of Ra awaits","broadcast":true,"story_id":"eye-of-ra"}'
# → {"tokens":N,"sent":n,"failed":m,"pruned":p}  (dead tokens are auto-deleted)
```

Target a single user instead with `"uid":"<firebase uid>"` in place of `broadcast`.

**Option B — Firebase console.** **Engage → Messaging → New campaign → Firebase
Notification messages**: any title/body; under **Additional options → Custom data** add
`story_id` = `creation-from-nun` (or `landmark_slug` = `great-pyramids-of-giza`); send a
test message to the device token (visible in Firestore `users/<uid>/fcm_tokens/` after
step 3, or logcat `FCM token`).

Expected either way: notification shows with the Wadjet eye status icon; tapping opens
the story reader / landmark detail directly (deep links verified in Phase 2).

## 8. OPTIONAL — let web-created accounts sign in on Android

Backend-only accounts (registered on the website) don't exist in Firebase Auth, so
Android email/password sign-in won't find them. Two options:
- **Zero-effort**: they tap "Sign in with Google" (auto-links by email), or use
  Firebase "Forgot password" once.
- **Bulk import** (one-off script, run with a service account that has the
  `firebaseauth.users.create` permission):

```python
# pip install firebase-admin bcrypt
import firebase_admin
from firebase_admin import auth, credentials

cred = credentials.Certificate("service-account.json")
firebase_admin.initialize_app(cred)

# pull users + bcrypt hashes from the backend DB (users.email, users.password_hash)
users = [
    auth.ImportUserRecord(uid=row.id, email=row.email,
                          password_hash=row.password_hash.encode(),
                          email_verified=row.email_verified)
    for row in backend_rows if row.password_hash
]
result = auth.import_users(users, hash_alg=auth.UserImportHash.bcrypt())
print(result.success_count, result.failure_count, result.errors)
```

Their existing passwords keep working (Firebase verifies against the imported bcrypt hash).

## 9. App Links — https story URLs open the app (E-P8)

The backend branch serves `/.well-known/assetlinks.json` once you set this env var
on the deployment (alongside step 4's vars):

```bash
# both certs: RELEASE (wadjet-release.jks, extracted 2026-07-06) + DEBUG (this machine)
ANDROID_CERT_SHA256=1C:94:2D:17:2F:AF:6A:0A:C1:D1:65:2E:05:4F:9A:2A:97:5F:95:C8:C1:B4:29:57:0A:0B:41:24:B5:FB:D9:02,7A:83:F2:2E:45:FC:71:45:8F:BD:35:ED:C2:92:E9:A4:B9:27:48:CE:42:D0:75:5C:AF:F4:47:05:30:F0:63:11
```

- The first fingerprint is your **release** cert (`keytool -list -v -keystore wadjet-release.jks -alias wadjet`).
- If Play App Signing re-signs your app, ALSO append the Play cert's SHA-256
  (Play Console → Setup → App integrity → App signing).
- Drop the debug fingerprint (second value) for a production-only statement.

After deploy, sanity-check `https://<backend>/.well-known/assetlinks.json` returns the
statement, then on a device: reinstall the app (or
`adb shell pm verify-app-links --re-verify com.wadjet.app`), wait ~30s, and
`adb shell pm get-app-links com.wadjet.app` should show
`nadercr7-wadjet-v2.hf.space: verified`. From then on, story links
(`https://…/stories/<id>`) shared anywhere open the app's reader directly.

---

### Quick status checklist

| Step | Blocks | Status |
|---|---|---|
| 1 Auth providers | all sign-in | ✅ done |
| 2 SHA fingerprints | Google Sign-In | ✅ for the 2 registered certs; add Play cert if used |
| 3 firestore.rules deploy | progress mirror, FCM tokens | ⬜ **PENDING-USER-CONSOLE** |
| 4 Backend env + deploy | Android sign-in vs prod, Pexels proxy | ⬜ **PENDING-USER-DEPLOY** |
| 5 Crashlytics enable | crash reports | ⬜ **PENDING-USER-CONSOLE** |
| 6 Analytics DebugView | verification only | ⬜ optional |
| 7 Test push | FCM E2E vs prod | ⬜ after 3 (+ service account for the E-P9 API) |
| 8 User import | web accounts on Android | ⬜ optional |
| 9 App Links env + deploy | https story links open the app | ⬜ **PENDING-USER-DEPLOY** (value ready to paste above) |
