# Stage 10 — Firebase and Auth Flow

**Date:** 2025-07-22  
**Auditor:** Automated (Copilot)  
**Scope:** Firebase Auth, FCM, TokenManager, AuthInterceptor, TokenAuthenticator, all auth flows, session persistence

---

## Summary

| Metric | Value |
|---|---|
| **Auth flows tested** | Login, Logout, Register (UI only), Forgot Password (UI only), Process Death |
| **Auth providers** | Email/Password + Google Sign-In (via CredentialManager) |
| **Token storage** | EncryptedSharedPreferences (AES-256-GCM keys, AES-256-GCM values) |
| **Token refresh** | OkHttp Authenticator with ReentrantLock |
| **Firebase services** | Auth (active), Messaging (partial), Firestore (provided but unused), Crashlytics, Analytics |
| **Critical bugs found** | 3 |
| **High bugs found** | 3 |

---

## 1. Auth Flow Matrix (Emulator-Tested)

| Flow | Works? | Error Handling | Notes |
|---|---|---|---|
| **Fresh install → Welcome** | ✅ | N/A | `pm clear` → Welcome screen after splash |
| **Email login** | ✅ | Shows error string | Firebase first, then backend POST `/api/auth/login` (478ms) |
| **Login → Landing** | ✅ | N/A | Bottom nav appears with all 5 tabs |
| **Register sheet** | ✅ UI only | Has validation | Fields: Display Name (optional), Email, Password, Confirm Password |
| **Forgot Password sheet** | ✅ UI only | N/A | Has email field + "Send Reset Link" button |
| **Settings → Sign Out** | ✅ | Confirmation dialog | Quick Settings → Full Settings → scroll → Sign Out |
| **Sign Out → Welcome** | ✅ | N/A | Back stack fully cleared |
| **Process death → restore** | ❌ **BROKEN** | N/A | See Section 5 |

---

## 2. Token Lifecycle

| Event | Expected | Actual | Issues |
|---|---|---|---|
| **Fresh login** | Token stored in EncryptedSharedPrefs | ✅ Two encrypted entries in `wadjet_secure_prefs.xml` | Working |
| **Token expired** | Auto-refresh via TokenAuthenticator | ✅ Architecture correct (ReentrantLock, stale-check, retry limit) | ⚠️ Bare OkHttpClient for refresh call |
| **Refresh failed** | Clear tokens → redirect to login | ✅ `tokenManager.clearAll()` + returns null | ⚠️ Firebase NOT signed out (ghost state) |
| **Concurrent refresh** | Queue/lock | ✅ `ReentrantLock` + stale token check | Working |
| **Logout** | Clear all tokens + Firebase | ✅ `clearAll()` + `firebaseAuth.signOut()` | ⚠️ Local data NOT cleared |
| **Process death** | Token persisted → auto-login | ❌ Shows Welcome despite persisted tokens | **CRITICAL** — see Section 5 |

---

## 3. Firebase Services Audit

| Service | Configured? | Working? | Issues |
|---|---|---|---|
| **FirebaseAuth** | ✅ Singleton via Hilt | ✅ Login/logout confirmed | `getIdToken()` is dead code |
| **FirebaseMessaging** | ✅ Service declared in manifest | ⚠️ Partial | FCM token **never sent to backend** — push targeting impossible |
| **FirebaseFirestore** | ✅ Provided in DI | ❌ Never injected/used | Dead DI binding |
| **Crashlytics** | ✅ Plugin configured | Not tested | — |
| **Analytics** | ✅ google-services.json present | Not tested | — |

### FirebaseAuthManager
- Wraps `FirebaseAuth` only
- Exposes reactive `authStateFlow` via `callbackFlow` + `AuthStateListener`
- All methods propagate exceptions (no internal catch)
- `getIdToken(false)` → never called → dead code
- Thread-safe (stateless wrapper over Firebase singleton)

### WadjetFirebaseMessaging
- `onNewToken()` logs first 10 chars of token but **never sends to backend**
- Comment says "sent when user authenticates" but no such code exists anywhere
- No topic subscriptions
- Uses placeholder icon (`android.R.drawable.ic_dialog_info`)
- Single notification channel (`wadjet_updates`) for all types
- No Hilt injection — cannot access repositories

---

## 4. Security Analysis

### 🔴 CRITICAL: Debug OkHttp Body Logging Exposes All Credentials

Confirmed via logcat capture during login:

| Logged Data | Log Tag | Risk |
|---|---|---|
| **Plaintext password** | `okhttp.OkHttpClient` | `{"email":"...","password":"12345mmmmmmmM"}` fully logged |
| **Access token (JWT)** | `okhttp.OkHttpClient` | Full `eyJ...` access token in response body |
| **Refresh token (JWT)** | `okhttp.OkHttpClient` | Full `eyJ...` refresh token in `Set-Cookie` header |
| **Bearer header** | `okhttp.OkHttpClient` | `Authorization: Bearer eyJ...` on subsequent requests |

**Mitigation**: `NetworkModule.kt` correctly sets `Level.NONE` for release builds. However:
- Debug builds on shared/CI devices expose all credentials in logcat
- Any crash reporter that captures logcat in debug could leak tokens
- ADB logcat accessible to other apps on rooted devices

### Token Storage Security

| Check | Status |
|---|---|
| MasterKey: AES256_GCM | ✅ |
| Key encryption: AES256_SIV | ✅ |
| Value encryption: AES256_GCM | ✅ |
| Dedicated file: `wadjet_secure_prefs` | ✅ |
| Lazy initialization | ✅ |
| No cleartext tokens in code | ✅ |
| No hardcoded API keys in source | ✅ |

### Network Security Config

| Rule | Status |
|---|---|
| Cleartext for `10.0.2.2` and `localhost` only | ✅ |
| All production traffic over HTTPS | ✅ |
| HSTS in response headers | ✅ (`max-age=31536000; includeSubDomains`) |

---

## 5. CRITICAL: Session Restoration Broken After Process Death

### Reproduction
1. Login successfully → Landing screen visible
2. `adb shell am force-stop com.wadjet.app`
3. `adb shell am start -n com.wadjet.app/.MainActivity`
4. **Result**: Welcome screen (not Landing)

### Evidence
- `wadjet_secure_prefs.xml` still has 2 encrypted entries (tokens persist)
- Firebase `FIREBASE_USER` SharedPrefs entry still present with full user data
- `isLoggedIn` check: `firebaseAuth.currentUser != null && tokenManager.isLoggedIn`

### Root Cause Analysis
The `isLoggedIn` property is evaluated **synchronously** in `MainActivity.onCreate()`:
```kotlin
val isLoggedIn = authRepository.isLoggedIn  // line 80
```
This calls:
```kotlin
override val isLoggedIn: Boolean
    get() = firebaseAuth.currentUser != null && tokenManager.isLoggedIn
```

After `force-stop`, `FirebaseAuth.getInstance().getCurrentUser()` may return `null` briefly while Firebase restores its cached state from SharedPreferences. The check happens before Firebase finishes its internal initialization, resulting in `isLoggedIn = false`.

### Impact
- Every app restart after process death (low memory kill, system kill, OOM) forces user to re-login
- Users on low-RAM devices will experience this frequently
- Destroys user experience for an app that requires authentication

---

## 6. All Issues

| # | Severity | Issue | Location |
|---|---|---|---|
| 1 | **Critical** | **Session restoration broken** — `isLoggedIn` evaluated before Firebase init completes → Welcome screen shown despite valid persisted tokens | `MainActivity.onCreate()` + `AuthRepositoryImpl.isLoggedIn` |
| 2 | **Critical** | **Bare OkHttpClient** for token refresh — no interceptors, no TLS config, no timeouts | `TokenAuthenticator.kt:78` |
| 3 | **Critical** | **Debug logging exposes credentials** — passwords, access tokens, refresh tokens all logged to logcat at BODY level | `NetworkModule.kt:55-60` |
| 4 | **High** | **FCM token never sent to backend** — push notifications cannot be targeted to specific users | `WadjetFirebaseMessaging.onNewToken()` |
| 5 | **High** | **Logout doesn't clear local data** — Room DB, DataStore, chat history survive logout. Next user sees stale data | `AuthRepositoryImpl.signOut()` |
| 6 | **High** | **Ghost auth state after refresh failure** — TokenAuthenticator clears tokens but doesn't sign out Firebase. `isLoggedIn` briefly reports true with no valid tokens | `TokenAuthenticator.authenticate()` |
| 7 | **Medium** | Auth endpoint matching duplicated — `AuthInterceptor.isAuthEndpoint()` (5 specific paths) vs `TokenAuthenticator` (substring `/auth/`) | `AuthInterceptor.kt` + `TokenAuthenticator.kt` |
| 8 | **Medium** | Response body leak risk — `refreshResponse` not closed if `parseAccessToken` throws | `TokenAuthenticator.refreshToken()` |
| 9 | **Low** | `FirebaseFirestore` provided in DI but never used | `FirebaseModule.kt` |
| 10 | **Low** | `FirebaseAuthManager.getIdToken()` never called — dead code | `FirebaseAuthManager.kt` |
| 11 | **Low** | FCM notification uses placeholder icon `android.R.drawable.ic_dialog_info` | `WadjetFirebaseMessaging.kt` |
| 12 | **Info** | `TokenManager` uses `apply()` (async write) for token storage — theoretically lossy on immediate crash | `TokenManager.kt` |

---

## 7. Auth UI Screens Mapped

### Welcome Screen (Unauthenticated)
- Title: "WADJET" + "Decode the Secrets of Ancient Egypt"
- 3 feature cards: Scan, Dictionary, Explore
- Buttons: "Sign in with Google", "Sign up with Email", "Already have an account? Sign in"
- Footer: "Built by Mr Robot"

### Login Sheet (Bottom Sheet)
- Fields: Email, Password (masked)
- Buttons: "Sign In", "Forgot password?"
- Alt auth: "Sign in with Google"
- Footer: "Don't have an account? Create one"

### Register Sheet (Bottom Sheet)
- Fields: Display Name (optional), Email, Password, Confirm Password
- Validation: 8+ chars, upper/lower/digit, match confirmation
- Buttons: "Create Account"
- Alt auth: "Sign up with Google"
- Footer: "Already have an account? Sign in"

### Forgot Password Sheet (Bottom Sheet)
- Title: "Reset Password" + "Enter your email and we'll send a reset link."
- Field: Email
- Button: "Send Reset Link"
- Footer: "← Back to Sign In"

### Sign Out Flow
- Settings icon (gear) at top-right of Landing → Quick Settings sheet → "Full Settings" → scroll down → "Sign Out" button
- Confirmation dialog: "Are you sure you want to sign out?" with Cancel/Sign Out

---

## 8. Google Sign-In Architecture

- Uses Android Credential Manager API (modern approach)
- `GetGoogleIdOption` with `webClientId` from `R.string.default_web_client_id`
- UUID nonce generated per request
- Called from 3 locations: main Welcome screen, LoginSheet, RegisterSheet
- Handles `GetCredentialCancellationException` (silent) and `NoCredentialException` (error message)
- Flow: `credentialManager.getCredential()` → extract `GoogleIdTokenCredential.idToken` → `authViewModel.signInWithGoogle(idToken)` → Firebase + Backend sync
- Split-brain protection: if backend sync fails, Firebase is signed out ✅
