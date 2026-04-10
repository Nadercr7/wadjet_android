# Wadjet Android — Release Checklist

> Everything needed from code-complete to APK distribution.
> Use this file during Phase P10.

---

## 1. App Signing & Keystore

### Generate Release Keystore
```bash
keytool -genkeypair -v \
  -keystore wadjet-release.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias wadjet-app \
  -dname "CN=Wadjet App, OU=Mobile, O=Mr Robot, L=Cairo, ST=Cairo, C=EG"
```

### Configure Signing in `build.gradle.kts`
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "../wadjet-release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: "wadjet-app"
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
```

### Keystore Backup
- Store `wadjet-release.jks` in:
  - Encrypted USB drive (physical backup)
  - Google Drive (encrypted zip)
  - Password manager vault
- Store passwords separately from keystore file
- **NEVER** commit `wadjet-release.jks` to Git

---

## 2. Versioning Strategy

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        versionCode = 1          // Increment each release (integer)
        versionName = "1.0.0"    // Semantic versioning (MAJOR.MINOR.PATCH)
    }
}
```

### Rules
| Change Type | versionCode | versionName |
|-------------|-------------|-------------|
| Bug fix | +1 | x.x.+1 (1.0.1) |
| New feature | +1 | x.+1.0 (1.1.0) |
| Breaking change | +1 | +1.0.0 (2.0.0) |
| Internal build | +1 | Same name + "-beta.N" |

### Git Tag Workflow
```bash
# Tag a release
git tag -a v1.0.0 -m "Wadjet v1.0.0 — Initial release"
git push origin v1.0.0
# CI automatically builds signed APK and creates GitHub Release
```

---

## 3. CI/CD — GitHub Actions

### File: `.github/workflows/android.yml`

**Build job** (every push/PR):
- Lint → Unit tests → Debug APK → Upload artifact

**Release job** (on `v*` tag push):
- Decode keystore → Build signed release APK → Create GitHub Release with APK attached

### Required GitHub Secrets
| Secret | Description |
|--------|-------------|
| `KEYSTORE_BASE64` | Base64-encoded `wadjet-release.jks` |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | `wadjet-app` |
| `KEY_PASSWORD` | Key password |

### Encode Keystore for CI
```bash
base64 -w 0 wadjet-release.jks > keystore-base64.txt
# Copy contents into GitHub repo secret KEYSTORE_BASE64
```

---

## 4. APK Distribution

### Direct Share
- Build locally: `./gradlew assembleRelease`
- APK location: `app/build/outputs/apk/release/app-release.apk`
- Share via Telegram, WhatsApp, Google Drive, USB

### GitHub Releases (Recommended)
- Push a `v*` tag → CI builds and publishes to GitHub Releases
- Users download from: `https://github.com/<user>/Wadjet-Android/releases/latest`
- Each release auto-generates changelog from commits

### Install Instructions (for users)
1. Download `wadjet-vX.Y.Z.apk`
2. Open the file on your Android device
3. If prompted, enable "Install from unknown sources" for your browser/file manager
4. Tap Install → Open
5. Requires Android 8.0 (API 26) or higher

---

## 5. Pre-Release Testing

### Testing Checklist
- [ ] All 18 screens render correctly
- [ ] Camera permission flow works (grant + deny + settings)
- [ ] Scan completes end-to-end with network
- [ ] Chat streaming displays incrementally
- [ ] Firebase Auth (Google + Email) works
- [ ] Offline mode (airplane mode) shows cached data
- [ ] RTL layout (Arabic) renders correctly
- [ ] TTS narration plays
- [ ] Deep links open correct screens (`wadjet://scan`, etc.)
- [ ] No ANR (Application Not Responding)
- [ ] ProGuard/R8 doesn't break runtime reflection
- [ ] Release APK installs cleanly on a fresh device

### Device Coverage
- [ ] Small phone (5.5")
- [ ] Large phone (6.7")
- [ ] Old device (API 26, Android 8.0)
- [ ] Latest device (API 35, Android 15)

---

## 6. Analytics Events Plan

Log these events via `FirebaseAnalytics.logEvent()`:

| Event | Parameters | When |
|-------|-----------|------|
| `scan_started` | `mode` (auto/ai/onnx), `source` (camera/gallery) | User initiates scan |
| `scan_completed` | `glyph_count`, `confidence_avg`, `duration_ms` | Scan pipeline finishes |
| `scan_saved` | `scan_id` | User saves scan to history |
| `dictionary_search` | `query`, `results_count` | User searches dictionary |
| `dictionary_sign_viewed` | `gardiner_code` | User opens sign detail |
| `lesson_started` | `level` | User starts a lesson |
| `lesson_completed` | `level`, `score` | User finishes a lesson |
| `landmark_viewed` | `slug`, `category` | User opens landmark detail |
| `landmark_identified` | `top_match`, `confidence` | Identify camera result |
| `landmark_favorited` | `slug` | User adds landmark to favorites |
| `chat_message_sent` | `char_count` | User sends message to Thoth |
| `chat_voice_used` | `lang` | User uses voice input |
| `story_started` | `story_id` | User opens a story |
| `story_chapter_read` | `story_id`, `chapter_index` | Chapter completed |
| `story_completed` | `story_id`, `score`, `glyphs_learned` | All chapters done |
| `tts_played` | `text_length`, `lang` | TTS narration triggered |
| `language_changed` | `from`, `to` | Language switch |
| `feedback_submitted` | `category` | Feedback sent |
| `auth_sign_up` | `method` (google/email) | New account created |
| `auth_sign_in` | `method` | Login |

---

## 7. Deep Linking (Custom Scheme)

### URL Scheme (works without Play Store)
```
wadjet://scan
wadjet://dictionary/{gardiner_code}
wadjet://explore/{landmark_slug}
wadjet://stories/{story_id}
wadjet://chat
```

### AndroidManifest Intent Filter
```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="wadjet" />
</intent-filter>
```

> Note: HTTPS App Links (`https://wadjet.app/...`) require Play Store verification
> via Digital Asset Links. Use `wadjet://` scheme for APK distribution.

---

## Final Pre-Release Checklist

- [ ] All features working (18 screens)
- [ ] Unit tests pass
- [ ] Lint clean (0 errors)
- [ ] ProGuard/R8 tested (release build installs and runs)
- [ ] Release APK size reasonable (< 100 MB)
- [ ] App icon (adaptive) looks good
- [ ] Keystore generated and backed up securely
- [ ] GitHub Secrets configured for CI
- [ ] Tag pushed → GitHub Release created with APK
- [ ] Firebase Crashlytics receiving events
- [ ] Tested on 2+ real devices
- [ ] Firebase Analytics receiving events
- [ ] No hardcoded API keys in source
- [ ] `google-services.json` NOT in public repo
