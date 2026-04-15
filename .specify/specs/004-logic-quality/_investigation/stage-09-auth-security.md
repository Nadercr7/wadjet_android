# Stage 9 — Auth & Security Audit

> **Auditor**: Copilot · **Date**: 2026-04-15  
> **Scope**: Auth flow, token management, session handling, deep links, rate limiting, input validation

---

## Files Reviewed

| # | File | Lines |
|---|------|-------|
| 1 | `feature/auth/…/AuthViewModel.kt` | 1–155 |
| 2 | `feature/auth/…/AuthUiState.kt` | 1–21 |
| 3 | `feature/auth/…/screen/WelcomeScreen.kt` | 1–330 |
| 4 | `feature/auth/…/sheet/LoginSheet.kt` | 1–138 |
| 5 | `feature/auth/…/sheet/RegisterSheet.kt` | 1–190 |
| 6 | `feature/auth/…/sheet/ForgotPasswordSheet.kt` | 1–120 |
| 7 | `core/network/…/TokenManager.kt` | 1–46 |
| 8 | `core/network/…/AuthInterceptor.kt` | 1–141 |
| 9 | `core/network/…/RateLimitInterceptor.kt` | 1–52 |
| 10 | `core/network/…/model/AuthModels.kt` | 1–52 |
| 11 | `core/network/…/api/AuthApiService.kt` | 1–30 |
| 12 | `core/network/…/di/NetworkModule.kt` | 1–120 |
| 13 | `core/data/…/repository/AuthRepositoryImpl.kt` | 1–137 |
| 14 | `core/data/…/repository/UserRepositoryImpl.kt` | 1–160 |
| 15 | `core/data/…/datastore/UserPreferencesDataStore.kt` | 1–34 |
| 16 | `core/domain/…/model/User.kt` | 1–22 |
| 17 | `core/domain/…/repository/AuthRepository.kt` | 1–15 |
| 18 | `core/firebase/…/FirebaseAuthManager.kt` | 1–60 |
| 19 | `app/…/navigation/WadjetNavGraph.kt` | 1–350 |
| 20 | `app/…/navigation/Route.kt` | 1–27 |
| 21 | `app/…/MainActivity.kt` | 1–200 |
| 22 | `app/…/di/AppModule.kt` | 1–35 |
| 23 | `app/src/main/AndroidManifest.xml` | 1–48 |

---

## Issues

### S9-01 | CRITICAL — Token refresh uses `Thread.sleep` on interceptor thread (blocks OkHttp thread pool)

**File**: [RateLimitInterceptor.kt](core/network/src/main/java/com/wadjet/core/network/RateLimitInterceptor.kt#L34-L36)

Both the 429 and 503 retry paths call `Thread.sleep(waitMs)` inside OkHttp's `Interceptor.intercept()`. OkHttp dispatches requests on a shared thread pool; sleeping here blocks an entire dispatcher thread. Under heavy concurrent requests (e.g. offline→online sync), this can deadlock the pool.

```kotlin
Thread.sleep(waitMs)       // L36 — blocks OkHttp dispatcher thread
response = chain.proceed(request)
```

**Recommendation**: Return the error response to the caller and implement retry at the repository/ViewModel layer with `delay()` on a coroutine, or use OkHttp's `Authenticator` interface which is designed for this.

---

### S9-02 | CRITICAL — Token refresh inside `runBlocking` blocks the OkHttp thread

**File**: [AuthInterceptor.kt](core/network/src/main/java/com/wadjet/core/network/AuthInterceptor.kt#L72)

`handleTokenRefresh` wraps the mutex-guarded refresh in `runBlocking`. Since OkHttp interceptors run on the OkHttp dispatcher thread (not a coroutine), `runBlocking` blocks the thread. If multiple 401s arrive simultaneously, only one thread proceeds; the rest park on the mutex, consuming all OkHttp threads.

```kotlin
val newToken = runBlocking {
    mutex.withLock {
        // blocks OkHttp thread until refresh completes
```

**Recommendation**: Use OkHttp's `Authenticator` interface which is specifically designed for transparent token refresh, or move to a coroutine-based HTTP client.

---

### S9-03 | CRITICAL — Dual auth system creates split-brain state (Firebase + backend tokens)

**File**: [AuthRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/AuthRepositoryImpl.kt#L47-L66)

The app authenticates to **both** Firebase Auth AND a custom Wadjet backend. If backend auth fails but Firebase succeeds, the user appears logged in (Firebase session exists, `currentUser` emits non-null) but has no backend token. All API calls that require `Authorization: Bearer` will fail with 401.

```kotlin
// signInWithGoogle:
if (response.isSuccessful) {
    ...
} else {
    Timber.w("Backend google auth failed: ${response.code()}")
    // Still signed into Firebase, just no backend token  ← SPLIT STATE
    firebaseUser.toDomain()
}
```

The same pattern exists in `register()` (line ~97): backend failure is silently ignored.

**Recommendation**: If backend auth fails, either sign out of Firebase or surface this as an error state to the user. The current code silently creates a partially authenticated session.

---

### S9-04 | MAJOR — Missing API endpoints: send-verification, verify-email, reset-password (confirm)

**File**: [AuthApiService.kt](core/network/src/main/java/com/wadjet/core/network/api/AuthApiService.kt)

The API service defines only 6 endpoints:
- `register`, `login`, `googleAuth`, `refresh`, `logout`, `forgotPassword`

Missing from the backend API surface:
1. **Send verification email** — `UserResponse.emailVerified` exists, but there's no endpoint to trigger a verification email
2. **Verify email callback** — no deep link handling for email verification tokens
3. **Reset password (confirm)** — `forgotPassword` sends a reset email, but there's no endpoint to submit the new password with a reset token (relies entirely on Firebase's built-in flow)

The app shows `emailVerified` in the `User` model but never gates any functionality on it.

---

### S9-05 | MAJOR — `forgotPasswordSent` state never resets

**File**: [AuthViewModel.kt](feature/auth/src/main/java/com/wadjet/feature/auth/AuthViewModel.kt#L108-L112)

When `forgotPassword()` succeeds, `forgotPasswordSent` is set to `true`. But it's **never reset to false** — not when the user dismisses the sheet, opens a different sheet, or navigates away. If the user re-opens the Forgot Password sheet, they'll immediately see the "Check your email" success state instead of the input form.

```kotlin
.onSuccess {
    _state.update { it.copy(isLoading = false, forgotPasswordSent = true) }
    // ← never reset anywhere
}
```

`showSheet()` and `dismissSheet()` only clear `error`, not `forgotPasswordSent`.

---

### S9-06 | MAJOR — No email verification enforcement

**Files**: [AuthRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/AuthRepositoryImpl.kt) · [WadjetNavGraph.kt](app/src/main/java/com/wadjet/app/navigation/WadjetNavGraph.kt)

`User.emailVerified` is carried through the model but **never checked anywhere**. The nav graph determines the start destination based solely on `authRepository.isLoggedIn` (Firebase user exists + access token present):

```kotlin
// MainActivity.kt ~L78
val startDestination: Route = if (isLoggedIn) Route.Landing else Route.Welcome
```

Unverified email users have full access to all features.

---

### S9-07 | MAJOR — `isLoggedIn` check at start is a one-shot, non-reactive snapshot

**File**: [MainActivity.kt](app/src/main/java/com/wadjet/app/MainActivity.kt#L78)

`authRepository.isLoggedIn` is read once during `setContent {}`. It's a synchronous `get()` that checks `firebaseAuth.currentUser != null && tokenManager.isLoggedIn`. This value is captured at composition time and **never re-observed**. If:
- The backend token expires and refresh fails → user sees authenticated UI but all API calls fail
- The user signs out from another device → no reactive observation of auth state change

```kotlin
val startDestination: Route = if (isLoggedIn) Route.Landing else Route.Welcome
// ← one-shot boolean, not a Flow/State
```

---

### S9-08 | MAJOR — Global sign-out has no navigation reset

**File**: [AuthRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/AuthRepositoryImpl.kt#L107-L111)

`signOut()` clears the backend token, calls Firebase sign-out, and best-effort calls the backend logout endpoint. But **nothing navigates the user back to the Welcome screen**. The user remains on whatever screen they were on, and subsequent API calls will fail with 401 (triggering refresh, which also fails → `tokenManager.clearAll()`, creating a loop).

```kotlin
override suspend fun signOut() {
    try { authApi.logout() } catch (e: Exception) { Timber.w(e, "Backend logout failed") }
    tokenManager.clearAll()
    firebaseAuth.signOut()
    // ← no navigation event, no auth-state-changed event to UI
}
```

---

### S9-09 | MAJOR — Refresh token sent as plain Cookie header, not in HttpOnly cookie

**File**: [AuthInterceptor.kt](core/network/src/main/java/com/wadjet/core/network/AuthInterceptor.kt#L56-L61)

The refresh token is stored in `EncryptedSharedPreferences` (good), but when sent to the server it's manually attached as a `Cookie` header string. If the server ever returns a `Set-Cookie: wadjet_refresh=…; HttpOnly; Secure`, OkHttp won't store it in a proper cookie jar — the interceptor extracts it manually but can't enforce `Secure` or `HttpOnly` semantics client-side.

```kotlin
request.newBuilder()
    .header("Cookie", "wadjet_refresh=$refreshToken")
    .build()
```

Additionally, no `SameSite` or path validation is done.

---

### S9-10 | MAJOR — JSON parsing via regex in hot path (fragile, exploitable)

**File**: [AuthInterceptor.kt](core/network/src/main/java/com/wadjet/core/network/AuthInterceptor.kt#L126-L129)

The access token is extracted from the refresh response body using a regex instead of proper JSON parsing:

```kotlin
private fun parseAccessToken(json: String): String? {
    val regex = """"access_token"\s*:\s*"([^"]+)"""".toRegex()
    return regex.find(json)?.groupValues?.get(1)
}
```

This will fail if the JSON contains escaped quotes within the token value, or if the server changes field ordering to embed `access_token` in a nested object. Same regex pattern is used in `AuthRepositoryImpl.parseError()`.

---

### S9-11 | MEDIUM — No brute-force protection on email/password login (client side)

**File**: [AuthViewModel.kt](feature/auth/src/main/java/com/wadjet/feature/auth/AuthViewModel.kt#L64-L79)

`signInWithEmail()` can be called unlimited times rapidly. There's no client-side rate limiting, exponential backoff, or attempt counter. The server's 429 detection exists in `RateLimitInterceptor` for login lockouts, but the client doesn't surface the lockout duration to the user:

```kotlin
if (path.contains("/auth/login")) {
    Timber.w("Login lockout detected (429). Locked for ${retryAfter}s")
    return response  // ← returns raw 429, no user-facing message about lockout time
}
```

The ViewModel treats any failure generically: `"Login failed"`.

---

### S9-12 | MEDIUM — Password validation allows weak passwords (no special characters)

**File**: [AuthViewModel.kt](feature/auth/src/main/java/com/wadjet/feature/auth/AuthViewModel.kt#L125-L131)

Password validation requires: 8+ chars, uppercase, lowercase, digit. But **no special character requirement**, and no check against common passwords or breached password lists:

```kotlin
fun validatePassword(password: String): String? {
    if (password.length < 8) return "Password must be at least 8 characters"
    if (!password.any { it.isUpperCase() }) return "Password must contain an uppercase letter"
    if (!password.any { it.isLowerCase() }) return "Password must contain a lowercase letter"
    if (!password.any { it.isDigit() }) return "Password must contain a digit"
    return null
}
```

`Aaaaaaaa1` would pass this validation.

---

### S9-13 | MEDIUM — Email validation regex is overly permissive

**File**: [AuthViewModel.kt](feature/auth/src/main/java/com/wadjet/feature/auth/AuthViewModel.kt#L121-L123)

```kotlin
fun validateEmail(email: String): Boolean {
    return email.trim().matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
}
```

This regex accepts `user@-invalid..com` and `user@.example.com` (leading dots/hyphens in domain). It also won't match internationalized email addresses. Consider using `android.util.Patterns.EMAIL_ADDRESS`.

---

### S9-14 | MEDIUM — `signInWithEmail` sends password to both Firebase AND Wadjet backend

**File**: [AuthRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/AuthRepositoryImpl.kt#L58-L70)

The plain-text password is sent to Firebase (`signInWithEmailAndPassword`) AND then again to the Wadjet backend API (`LoginRequest(email, password)`). The backend receives the raw password over HTTPS. If the backend is compromised or logs request bodies, the password is exposed.

```kotlin
val firebaseUser = firebaseAuth.signInWithEmail(email, password)
val response = authApi.login(LoginRequest(email = email, password = password))
```

**Recommendation**: Use Firebase as the single auth source and send only a Firebase ID token to the backend (like the Google flow does).

---

### S9-15 | MEDIUM — No deep link handling — but also no deep link attack surface

**File**: [AndroidManifest.xml](app/src/main/AndroidManifest.xml)

The manifest declares no `<intent-filter>` with `<data>` scheme/host for deep links. The `Route` sealed interface uses type-safe navigation only. This means:
- **No deep link attack surface** (good)  
- **No email verification deep link** (can't handle `wadjet://verify-email?token=xxx`)
- **No password reset deep link** (relies entirely on Firebase's built-in WebView flow)

---

### S9-16 | MEDIUM — Deprecated `MasterKeys` API for EncryptedSharedPreferences

**File**: [TokenManager.kt](core/network/src/main/java/com/wadjet/core/network/TokenManager.kt#L18-L19)

```kotlin
val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
```

`MasterKeys` is deprecated in favor of `MasterKey.Builder`. The new API provides better error handling and supports user-authentication-required keys.

---

### S9-17 | MEDIUM — Login sends password before email validation completes

**File**: [AuthViewModel.kt](feature/auth/src/main/java/com/wadjet/feature/auth/AuthViewModel.kt#L64-L66)

```kotlin
fun signInWithEmail(email: String, password: String) {
    if (!validateEmail(email) || password.isBlank()) {
```

The check is `password.isBlank()` — but there's no length or strength validation for login passwords. While login shouldn't enforce creation rules, a 1-character password will still be sent to both Firebase and the backend, wasting network calls.

---

### S9-18 | LOW — `HttpLoggingInterceptor.Level.BODY` logs tokens in debug builds

**File**: [NetworkModule.kt](core/network/src/main/java/com/wadjet/core/network/di/NetworkModule.kt#L54-L59)

In debug builds, the full HTTP request/response body (including `access_token`, `Cookie: wadjet_refresh=…`) is logged to Logcat:

```kotlin
level = if (com.wadjet.core.network.BuildConfig.DEBUG) {
    HttpLoggingInterceptor.Level.BODY
} else {
    HttpLoggingInterceptor.Level.NONE
}
```

Anyone with USB debugging or a rooted debug device can read tokens from logs.

---

### S9-19 | LOW — No PKCE or nonce for Google Sign-In

**File**: [WelcomeScreen.kt](feature/auth/src/main/java/com/wadjet/feature/auth/screen/WelcomeScreen.kt#L302-L309)

The Google ID option is built without `setNonce()`:

```kotlin
val googleIdOption = GetGoogleIdOption.Builder()
    .setServerClientId(webClientId)
    .setFilterByAuthorizedAccounts(false)
    .build()
```

While Google's Credential Manager handles PKCE internally, setting a server-side nonce (`setNonce()`) provides replay attack protection.

---

### S9-20 | LOW — `UserPreferencesDataStore` stores TTS prefs in plain text DataStore

**File**: [UserPreferencesDataStore.kt](core/data/src/main/java/com/wadjet/core/data/datastore/UserPreferencesDataStore.kt)

The preferences DataStore (`"user_preferences"`) is unencrypted. Currently it only stores `tts_enabled` and `tts_speed` which are non-sensitive. No issue now, but if any sensitive preferences are added later, they'll be in plaintext.

---

### S9-21 | INFO — Auth state uses two separate sources of truth

**Files**: [AuthRepositoryImpl.kt](core/data/src/main/java/com/wadjet/core/data/repository/AuthRepositoryImpl.kt#L29-L39)

`currentUser` flow combines `firebaseAuth.authStateFlow` (Firebase auth state) with `tokenManager.isLoggedIn` (backend token exists). These two sources can go out of sync:
- Firebase token expires → user is null in flow → UI shows logged out
- Backend token expires → `tokenManager.isLoggedIn` is false → user is null in flow
- But `isLoggedIn` property is a one-shot check, not a flow

---

## Summary

| Severity | Count |
|----------|-------|
| CRITICAL | 3 |
| MAJOR | 7 |
| MEDIUM | 6 |
| LOW | 3 |
| INFO | 2 |
| **Total** | **21** |

**Most impactful issues**: Split-brain Firebase+backend auth (S9-03), thread-blocking token refresh (S9-01/02), and no reactive auth state observation (S9-07/08).
