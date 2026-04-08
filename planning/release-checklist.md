# Wadjet Android — Release Checklist

> Everything needed from code-complete to Google Play Store listing.
> Use this file during Phase P10.

---

## 1. App Signing & Keystore

### Generate Release Keystore
```bash
# Run in Android Studio terminal or standalone
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

### Play App Signing (Recommended)
- [ ] Enroll in Google Play App Signing in Play Console
- [ ] Upload app signing key OR let Google generate one
- [ ] Keep upload key locally; Google holds the signing key
- [ ] **NEVER** commit `wadjet-release.jks` to Git

### Keystore Backup
- Store `wadjet-release.jks` in:
  - Encrypted USB drive (physical backup)
  - Google Drive (encrypted zip)
  - Password manager vault
- Store passwords separately from keystore file

---

## 2. Versioning Strategy

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        versionCode = 1          // Increment each Play Store upload (integer)
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

- `versionCode` MUST always increase for Play Store acceptance
- `versionName` follows semver for user-facing display
- CI auto-increments `versionCode` via Git tag count or build number

---

## 3. CI/CD — GitHub Actions

### File: `.github/workflows/android.yml`

```yaml
name: Android CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        distribution: 'temurin'
        java-version: 17
    
    - name: Cache Gradle
      uses: actions/cache@v4
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
    
    - name: Run Lint
      run: ./gradlew lint
    
    - name: Run Unit Tests
      run: ./gradlew testDebugUnitTest
    
    - name: Build Debug APK
      run: ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v4
      with:
        name: debug-apk
        path: app/build/outputs/apk/debug/app-debug.apk

  release:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        distribution: 'temurin'
        java-version: 17
    
    - name: Decode Keystore
      run: echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > wadjet-release.jks
    
    - name: Build Release Bundle
      env:
        KEYSTORE_PATH: wadjet-release.jks
        KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      run: ./gradlew bundleRelease
    
    - name: Upload AAB
      uses: actions/upload-artifact@v4
      with:
        name: release-aab
        path: app/build/outputs/bundle/release/app-release.aab
```

### Required GitHub Secrets
| Secret | Description |
|--------|-------------|
| `KEYSTORE_BASE64` | Base64-encoded `wadjet-release.jks` |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | `wadjet-app` |
| `KEY_PASSWORD` | Key password |
| `GOOGLE_SERVICES_JSON` | Firebase config (if not in repo) |

---

## 4. Play Store Listing

### App Details
| Field | EN | AR |
|-------|----|----|
| App name | Wadjet — Ancient Egypt Explorer | وادجت — مستكشف مصر القديمة |
| Short description (80 chars) | Scan hieroglyphs, explore landmarks, chat with Thoth, read mythology stories. | امسح الهيروغليفية، استكشف المعالم، تحدث مع تحوت، اقرأ قصص الأساطير. |
| Full description | See below | See below |

### Full Description (EN)
```
Wadjet brings Ancient Egypt to life with AI-powered technology.

🔍 SCAN HIEROGLYPHS
Point your camera at hieroglyphic inscriptions. Wadjet detects individual signs, classifies them using the Gardiner system, and translates the text — all in seconds.

📖 LEARN 1,000+ SIGNS
Browse our comprehensive dictionary of Gardiner signs. Each entry includes transliteration, pronunciation, meaning, and fun facts. Take 5 progressive lessons from alphabet to reading practice.

✍️ WRITE IN HIEROGLYPHS
Type English text and see it rendered in authentic hieroglyphs. Three modes: alphabetic, smart transliteration, and Manuel de Codage.

🏛 EXPLORE 260+ LANDMARKS
Discover Egypt's pharaonic temples, Islamic mosques, Coptic churches, and natural wonders. Get directions, tips, and rich history for each site. Identify landmarks from photos.

🤖 CHAT WITH THOTH
Meet Thoth, the AI keeper of wisdom. Ask anything about Ancient Egypt and get knowledgeable, personality-rich answers in real time.

📚 12 MYTHOLOGY STORIES
Read interactive stories from the Contendings of Horus & Set to Cleopatra's Last Stand. Earn points, learn glyphs, and unlock narrated experiences.

Features:
• Offline dictionary and landmark browsing
• English & Arabic (العربية) interface
• Text-to-speech narration
• Save favorites and track progress
• Beautiful black & gold Egyptian design

Built by Mr Robot
```

### Screenshots (Required: 2–8 per device type)
| # | Screen | Device | Orientation |
|---|--------|--------|-------------|
| 1 | Landing (dual path) | Phone 6.7" | Portrait |
| 2 | Scan result (detected glyphs) | Phone 6.7" | Portrait |
| 3 | Dictionary sign detail | Phone 6.7" | Portrait |
| 4 | Explore landmark detail | Phone 6.7" | Portrait |
| 5 | Chat with Thoth | Phone 6.7" | Portrait |
| 6 | Story reader | Phone 6.7" | Portrait |
| 7 | Dashboard | Phone 6.7" | Portrait |
| 8 | Write in hieroglyphs | Phone 6.7" | Portrait |

### Feature Graphic
- 1024 x 500 px
- Black (#0A0A0A) background, gold (#D4AF37) Eye of Wadjet, app name
- No screenshots in feature graphic (Google recommendation)

### App Icon
- Adaptive icon: foreground = gold Eye of Wadjet SVG, background = #0A0A0A
- 512x512 PNG for Play Store listing
- Must look good at 48dp (launcher) and 512px (store)

---

## 5. Content Rating (IARC)

Complete the questionnaire in Play Console:
- **Violence**: None (mythology stories are textual, no graphic violence)
- **Sexual content**: None
- **Language**: None (mild mythological references)
- **Controlled substances**: None
- **User interaction**: Yes (chat with AI, text input)
- **Shares location**: No (directions link to Google Maps externally)
- **Data sharing**: Yes (see Data Safety below)

Expected rating: **PEGI 3 / Everyone**

---

## 6. Data Safety Form

Required disclosures for Google Play Data Safety section:

| Data Type | Collected | Shared | Purpose |
|-----------|-----------|--------|---------|
| Email address | Yes | No | Account management (Firebase Auth) |
| Name / Display name | Yes | No | Profile personalization |
| Photos (camera/gallery) | Yes (processed) | No | Hieroglyph scanning, landmark ID |
| App activity (scans, favorites) | Yes | No | User dashboard, progress tracking |
| Crash logs | Yes | No | Crashlytics (app stability) |
| App interactions | Yes | No | Firebase Analytics (usage patterns) |
| Device identifiers | Yes | No | Firebase (instance ID) |

### Data Handling
- [ ] Data encrypted in transit: **Yes** (HTTPS + Firebase TLS)
- [ ] Data encrypted at rest: **Yes** (EncryptedSharedPreferences for tokens)
- [ ] Users can request deletion: **Yes** (Settings → Delete Account)
- [ ] Data deletion mechanism: Firebase Auth delete + Firestore cascade

---

## 7. Privacy Policy

**Required before Play Store submission.** Host at a public URL.

### Minimum Content
1. What data is collected (email, name, usage, photos)
2. How data is used (account, features, analytics, crash reporting)
3. Third parties (Firebase/Google, Wadjet API server)
4. User rights (access, delete, export)
5. Data retention (account data until deletion, crash logs 90 days)
6. Children's privacy (not targeted at children under 13)
7. Contact information

### Hosting Options
- GitHub Pages (free): `https://nadercr7.github.io/wadjet-privacy/`
- Firebase Hosting (free): `https://wadjet-app.web.app/privacy`
- Static page in Wadjet web app: `https://nadercr7-wadjet-v2.hf.space/privacy`

---

## 8. Pre-Release Testing

### Internal Testing Track
1. Upload AAB to Play Console → Internal testing track
2. Add 1–5 testers (email list)
3. Test on multiple devices:
   - [ ] Small phone (5.5")
   - [ ] Large phone (6.7")
   - [ ] Tablet (10")
   - [ ] Old device (API 26, Android 8.0)
   - [ ] Latest device (API 35, Android 15)

### Pre-Launch Report
- Play Console automatically tests on Firebase Test Lab
- Review: crashes, accessibility, security alerts

### Testing Checklist
- [ ] All 18 screens render correctly
- [ ] Camera permission flow works (grant + deny + settings)
- [ ] Scan completes end-to-end with network
- [ ] Chat streaming displays incrementally
- [ ] Firebase Auth (Google + Email) works
- [ ] Offline mode (airplane mode) shows cached data
- [ ] RTL layout (Arabic) renders correctly
- [ ] TTS narration plays
- [ ] Deep links open correct screens
- [ ] No ANR (Application Not Responding) in Test Lab
- [ ] ProGuard/R8 doesn't break runtime reflection

---

## 9. Analytics Events Plan

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

## 10. Deep Linking (Future — P11)

### URL Scheme
```
wadjet://scan
wadjet://dictionary/{gardiner_code}
wadjet://explore/{landmark_slug}
wadjet://stories/{story_id}
wadjet://chat
```

### Android App Links (HTTPS)
```
https://wadjet.app/landmarks/{slug}
https://wadjet.app/stories/{story_id}
https://wadjet.app/dictionary/{code}
```

### AndroidManifest Intent Filter
```xml
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="https" android:host="wadjet.app" />
</intent-filter>
```

### Digital Asset Links
Host at `https://wadjet.app/.well-known/assetlinks.json`:
```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "com.wadjet.app",
    "sha256_cert_fingerprints": ["<SHA-256 from Play App Signing>"]
  }
}]
```

---

## Final Pre-Submission Checklist

- [ ] All features working (18 screens)
- [ ] Unit tests pass (80%+ coverage)
- [ ] UI tests pass (critical flows)
- [ ] Lint clean (0 errors)
- [ ] ProGuard/R8 tested (release build works)
- [ ] Release AAB under 150 MB
- [ ] App icon (adaptive) looks good
- [ ] Feature graphic ready
- [ ] 8 screenshots per locale
- [ ] Store listing EN + AR
- [ ] Privacy policy URL live
- [ ] Data Safety form completed
- [ ] Content rating questionnaire done
- [ ] Internal testing passed
- [ ] Pre-launch report reviewed
- [ ] Keystore backed up securely
- [ ] Firebase Crashlytics receiving events
- [ ] Firebase Analytics receiving events
- [ ] No hardcoded API keys in source
- [ ] `google-services.json` NOT in public repo
