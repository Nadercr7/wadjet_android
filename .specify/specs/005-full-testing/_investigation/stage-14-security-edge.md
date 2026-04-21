# Stage 14: Security and Edge Cases

## Security Audit

### Code Review Findings

| Area | Status | Issue | Severity |
|------|--------|-------|----------|
| Token storage | ✅ OK | EncryptedSharedPreferences with AES256-GCM for access/refresh tokens | — |
| Certificate pinning | ❌ Missing | No `CertificatePinner` on OkHttpClient — MITM possible on compromised networks | MEDIUM |
| Cleartext traffic | ✅ OK | `network_security_config.xml` blocks cleartext; all traffic is HTTPS | — |
| ProGuard rules | ⚠️ Issue | `-renamesourcefileattribute SourceFile` is **commented out** — stack traces expose real filenames in release | LOW |
| Permissions | ✅ OK | Camera, internet, network state — no over-requesting | — |
| Input sanitization | ✅ OK | Room uses parameterized queries; no raw SQL injection vectors | — |
| Deep link validation | ✅ OK | No deep links registered in manifest — no attack surface | — |
| WebView | ✅ OK | No WebView usage anywhere | — |
| Exported components | ✅ OK | Only `MainActivity` exported (required for launcher) | — |
| Backup rules | ⚠️ Issue | `backup_rules.xml` / `data_extraction_rules.xml` don't exclude `encrypted_prefs` — tokens could leak via ADB backup or cloud backup | MEDIUM |
| SharedPreferences mix | ⚠️ Issue | `UserPreferencesDataStore` uses **plain** DataStore alongside EncryptedSharedPreferences for tokens — if non-sensitive prefs (ttsEnabled, theme) are fine, but worth auditing what goes where | LOW |
| Debug logging | ❌ CRITICAL | `HttpLoggingInterceptor.Level.BODY` in debug builds — **logs plaintext passwords, access tokens, refresh tokens** to logcat. Any app on device with READ_LOGS can capture these. | CRITICAL (debug) |

### Detailed Findings

#### 1. No Certificate Pinning (MEDIUM)
- **File**: `core/network/src/main/java/com/wadjet/core/network/di/NetworkModule.kt`
- `OkHttpClient.Builder()` has no `.certificatePinner()` call
- Allows MITM interception on compromised WiFi/corporate networks
- Fix: Add SHA-256 pins for `nadercr7-wadjet-v2.hf.space`

#### 2. Backup Rules Don't Exclude Encrypted Prefs (MEDIUM)
- **File**: `app/src/main/res/xml/backup_rules.xml` and `data_extraction_rules.xml`
- EncryptedSharedPreferences file not in exclude list
- Android Auto Backup or `adb backup` could extract encrypted pref file
- Fix: Add `<exclude domain="sharedpref" path="encrypted_prefs.xml"/>` to both files

#### 3. ProGuard SourceFile Attribute (LOW)
- **File**: `app/proguard-rules.pro`
- `-renamesourcefileattribute SourceFile` line is commented out
- Release crash stack traces in Crashlytics will show real filenames
- Fix: Uncomment the line

#### 4. Plain SharedPreferences Alongside Encrypted (LOW)
- `UserPreferencesDataStore` uses Jetpack DataStore (unencrypted) for settings like `ttsEnabled`, `ttsSpeed`, `theme`
- Tokens are correctly in EncryptedSharedPreferences via `TokenManager`
- Low risk: settings leaking is not security-critical, but worth documentation

#### 5. Debug Body Logging (CRITICAL — debug builds only)
- **File**: `core/network/src/main/java/com/wadjet/core/network/di/NetworkModule.kt`
- `HttpLoggingInterceptor` set to `Level.BODY` in debug
- Logs full request/response bodies including: login request (email + password), auth responses (access_token, refresh_token), all Bearer token headers
- Not a production risk (stripped in release), but dangerous during development on shared devices
- Fix: Use `Level.HEADERS` or `Level.BASIC` for debug; `Level.NONE` for release

---

## Edge Case Testing

### Monkey Test (Random UI Interactions)

| Run | Events | Seed | Result |
|-----|--------|------|--------|
| 1 | 500 (attempted) | 1776354960614 | **System OOM at event 5** — emulator only has 2GB RAM; `Process exited due to signal 9 (Killed)` + `mem-pressure-event` in logcat. **NOT an app crash** — system killed process under memory pressure. |
| 2 | 100 | default | **✅ Completed successfully** — `Events injected: 100`, `Monkey finished`. No fatal exceptions, no ANRs. |

- **Conclusion**: App survived 100 random UI interactions without crash. The event-5 kill was an emulator resource constraint (2GB RAM), not an app defect.

### Memory Usage

```
adb shell dumpsys meminfo com.wadjet.app

** MEMINFO in pid 31014 [com.wadjet.app] **
Total PSS:     174,261 KB  (~170 MB)
  Java Heap:    23,820 KB  (~23 MB)
  Native Heap:  13,444 KB  (~13 MB)
  Code:          9,052 KB
  Stack:           928 KB
  Graphics:     63,756 KB  (~62 MB — Compose/Skia rendering)
  System:       23,604 KB
Total RSS:     278,256 KB  (~272 MB)
```

- **Assessment**: 170MB PSS is reasonable for a Compose app with image loading. Java heap (23MB) well under default 256MB limit. Graphics (62MB) expected for Skia-backed Compose. No OOM risk under normal usage.

### Rotation (Landscape ↔ Portrait)

| Test | Result |
|------|--------|
| Switch to landscape via `user_rotation 1` | ✅ UI rendered correctly — "WADJET / Decode the Secrets of Ancient Egypt" visible in uiautomator dump |
| Switch back to portrait via `user_rotation 0` | ✅ UI restored correctly |
| Any crash during rotation | ✅ No errors in logcat |

### Don't-Keep-Activities (Developer Option)

| Test | Result |
|------|--------|
| `always_finish_activities = 1` | App shows **Welcome screen** with "Sign in with Google" instead of restoring session |
| Root cause | Same as Stage 10 — `isLoggedIn` evaluated synchronously in `MainActivity.onCreate()` before Firebase init completes |
| Severity | HIGH — confirms session restoration is fundamentally broken under activity recreation |

**Connection to Stage 10**: This is the same underlying bug. Whether triggered by force-stop (Stage 10), process death (`am kill`), or don't-keep-activities — the app loses the authenticated state and shows Welcome instead of restoring to the correct screen.

### Split-Screen / Multi-Window

Not tested — emulator API 37 freeform mode requires additional setup. Low priority given Compose's adaptive layout.

---

## Summary of Security Issues (Ordered by Severity)

| # | Issue | Severity | Production Impact | Fix Effort |
|---|-------|----------|-------------------|------------|
| 1 | Debug body logging exposes passwords + tokens | CRITICAL (debug) | None (stripped in release) | LOW — change to Level.HEADERS |
| 2 | No certificate pinning | MEDIUM | MITM on compromised networks | MEDIUM — add CertificatePinner |
| 3 | Backup rules don't exclude encrypted prefs | MEDIUM | Token extraction via ADB backup | LOW — add exclude rules |
| 4 | ProGuard `-renamesourcefileattribute` commented out | LOW | Real filenames in crash logs | LOW — uncomment line |
| 5 | Plain SharedPreferences for settings | LOW | Settings visible (not sensitive) | LOW — document as acceptable |

## Summary of Edge Case Issues

| # | Scenario | Result | Issue |
|---|----------|--------|-------|
| 1 | Monkey 100 events | ✅ No crash | — |
| 2 | Monkey 500 events | System OOM (not app) | Emulator 2GB limit |
| 3 | Landscape rotation | ✅ Works | — |
| 4 | Don't-keep-activities | ❌ Shows Welcome | Session restoration broken (='Stage 10 bug) |
| 5 | Memory usage | 174MB PSS | Reasonable for Compose |
| 6 | Split-screen | Not tested | Low priority |
