# Wadjet Android — Phase 0: Setup Instructions

> Step-by-step from zero to running project.
> Prerequisite: Android Studio installed.

---

## Step 1: Verify Your Environment

Open a terminal and check:

```bash
# Java (should be 17+, bundled with Android Studio)
java -version

# Android Studio → Help → About → Check build version
# Should be Ladybug (2024.2) or newer
```

### Install Android SDK 35
1. Open Android Studio → **Settings** → **Languages & Frameworks** → **Android SDK**
2. **SDK Platforms** tab → check **Android 15 (VanillaIceCream) — API 35**
3. **SDK Tools** tab → check:
   - Android SDK Build-Tools 35
   - Android SDK Command-line Tools
   - Android Emulator
   - Android SDK Platform-Tools
   - Google Play services (for Google Sign-In)
4. Click **Apply** → download

### Create an Emulator (Optional — you can use a real device)
1. **Tools** → **Device Manager** → **Create Virtual Device**
2. Pick **Pixel 8** (or any phone with Play Store icon)
3. System image: **API 35** (download if needed)
4. Finish

---

## Step 2: Create the Android Project

> **IMPORTANT**: The `planning/` folder already exists at the project path.
> Android Studio will create its files alongside it — this is fine.
> Do NOT delete or move the planning/ folder.

1. **File** → **New** → **New Project**
2. Template: **Empty Activity** (Compose)
3. Configure:
   - Name: **Wadjet**
   - Package name: **com.wadjet.app**
   - Save location: `D:\Personal attachements\Projects\Wadjet-Android`
   - Language: **Kotlin**
   - Minimum SDK: **API 26 (Android 8.0)**
   - Build configuration language: **Kotlin DSL (build.gradle.kts)**
4. Click **Finish**

> Android Studio will generate the project and do initial Gradle sync.
> The `planning/` folder will be visible in the Project view — this is your documentation.

---

## Step 3: Initialize Git Repository

The Android project gets its **own** Git repo (separate from the Wadjet v3 web project).

```bash
cd "D:\Personal attachements\Projects\Wadjet-Android"
git init
git add .
git commit -m "P0.1: Initial Android Studio project + planning docs"
```

Create `.gitignore` at project root (Android Studio generates one, but verify it has):
```
*.iml
.gradle/
build/
local.properties
.idea/
*.apk
*.aab
app/google-services.json
wadjet-release.jks
```

---

## Step 4: Create Multi-Module Structure

In Android Studio, for each module:
**File** → **New** → **New Module** → **Android Library**

### Core modules (9 libraries):
| Module name | Package |
|------------|---------|
| core:designsystem | com.wadjet.core.designsystem |
| core:domain | com.wadjet.core.domain |
| core:data | com.wadjet.core.data |
| core:network | com.wadjet.core.network |
| core:database | com.wadjet.core.database |
| core:firebase | com.wadjet.core.firebase |
| core:ml | com.wadjet.core.ml |
| core:common | com.wadjet.core.common |
| core:ui | com.wadjet.core.ui |

### Feature modules (10 libraries):
| Module name | Package |
|------------|---------|
| feature:auth | com.wadjet.feature.auth |
| feature:landing | com.wadjet.feature.landing |
| feature:scan | com.wadjet.feature.scan |
| feature:dictionary | com.wadjet.feature.dictionary |
| feature:explore | com.wadjet.feature.explore |
| feature:chat | com.wadjet.feature.chat |
| feature:stories | com.wadjet.feature.stories |
| feature:dashboard | com.wadjet.feature.dashboard |
| feature:settings | com.wadjet.feature.settings |
| feature:feedback | com.wadjet.feature.feedback |

> **Tip**: You can also create modules manually by creating directories + `build.gradle.kts` files, then adding `include()` to `settings.gradle.kts`. The AI prompt for P0 can generate all of this.

After creating all modules, your `settings.gradle.kts` should have:
```kotlin
include(":app")
include(":core:designsystem")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:database")
include(":core:firebase")
include(":core:ml")
include(":core:common")
include(":core:ui")
include(":feature:auth")
include(":feature:landing")
include(":feature:scan")
include(":feature:dictionary")
include(":feature:explore")
include(":feature:chat")
include(":feature:stories")
include(":feature:dashboard")
include(":feature:settings")
include(":feature:feedback")
```

---

## Step 5: Setup Version Catalog

Create/edit `gradle/libs.versions.toml` with the full catalog from [dependencies.md](dependencies.md).

This is the single source of truth for ALL dependency versions. Copy the entire `[versions]`, `[libraries]`, and `[plugins]` sections.

---

## Step 6: Configure Root build.gradle.kts

```kotlin
// Root build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
```

---

## Step 7: Configure App Module build.gradle.kts

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.wadjet.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.wadjet.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://nadercr7-wadjet-v2.hf.space\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // See dependencies.md for complete :app module dependencies
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:firebase"))
    implementation(project(":core:ml"))
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:landing"))
    implementation(project(":feature:scan"))
    implementation(project(":feature:dictionary"))
    implementation(project(":feature:explore"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:stories"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:feedback"))

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.compose.ui.test)
}
```

---

## Step 8: Create WadjetApplication

```kotlin
// app/src/main/java/com/wadjet/app/WadjetApplication.kt
package com.wadjet.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WadjetApplication : Application()
```

---

## Step 9: Firebase Setup

### 9a. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add project** → Name: **wadjet-android**
3. Enable Google Analytics → Create project

### 9b. Add Android App
1. In Firebase Console → **Add app** → Android
2. Package name: `com.wadjet.app`
3. App nickname: Wadjet
4. **SHA-1 fingerprint** (required for Google Sign-In):
   ```powershell
   # In Android Studio terminal (or PowerShell):
   cd "D:\Personal attachements\Projects\Wadjet-Android"
   
   # Debug SHA-1 (PowerShell — use $env:USERPROFILE, not %USERPROFILE%):
   keytool -list -v -alias androiddebugkey -keystore "$env:USERPROFILE\.android\debug.keystore" -storepass android
   
   # Copy the SHA-1 line and paste in Firebase Console
   ```
   
   **Already retrieved**: `8A:2D:0F:56:90:75:38:23:9D:88:C4:90:25:7C:58:72:6F:B0:46:67`

5. Click **Register app**
6. ~~Download `google-services.json` → place in `app/` directory~~ ✅ Already done
7. Click **Next** through the remaining steps

### 9c. Enable Firebase Services
In Firebase Console → your project:

| Service | Location | Setup |
|---------|----------|-------|
| **Authentication** | Build → Authentication → Sign-in method | Enable: Google, Email/Password |
| **Cloud Firestore** | Build → Firestore Database | Create database → Start in **test mode** (we'll deploy rules later) |
| **Storage** | Build → Storage | Get started → default rules |
| **Analytics** | Already enabled from project creation | — |
| **Crashlytics** | Release & Monitor → Crashlytics | Will activate on first app run |
| **Cloud Messaging** | Engage → Messaging | Enabled by default |

### 9d. Deploy Firestore Security Rules
Copy rules from [firebase-schema.md](firebase-schema.md) → Firebase Console → Firestore → Rules → paste → **Publish**.

### 9e. Google Sign-In Config
1. Firebase Console → Authentication → Sign-in method → Google → Enable
2. Note the **Web client ID** (NOT Android client ID) — you'll need this for Credential Manager API
3. Firebase auto-generates OAuth client IDs using your SHA-1

---

## Step 10: Download Font Files

Download from Google Fonts and place in `core/designsystem/src/main/res/font/`:

| Font | Download URL | Files Needed |
|------|-------------|--------------|
| Playfair Display | fonts.google.com/specimen/Playfair+Display | SemiBold (600), Bold (700) |
| Inter | fonts.google.com/specimen/Inter | Regular (400), Medium (500), SemiBold (600) |
| JetBrains Mono | fonts.google.com/specimen/JetBrains+Mono | Regular (400) |
| Cairo | fonts.google.com/specimen/Cairo | Regular, Medium, SemiBold, Bold |
| Noto Sans Egyptian Hieroglyphs | fonts.google.com/noto/specimen/Noto+Sans+Egyptian+Hieroglyphs | Regular |

**Rename files** to lowercase with underscores (Android resource naming):
```
playfair_display_semibold.ttf
playfair_display_bold.ttf
inter_regular.ttf
inter_medium.ttf
inter_semibold.ttf
jetbrains_mono_regular.ttf
cairo_regular.ttf
cairo_medium.ttf
cairo_semibold.ttf
cairo_bold.ttf
noto_sans_egyptian_hieroglyphs.ttf
```

---

## Step 11: Copy ONNX Models

```bash
# Create assets directory
mkdir "D:\Personal attachements\Projects\Wadjet-Android\app\src\main\assets\models\hieroglyph"
mkdir "D:\Personal attachements\Projects\Wadjet-Android\app\src\main\assets\models\landmark"

# Copy from web project
copy "D:\Personal attachements\Projects\Wadjet-v3-beta\models\hieroglyph\*" "D:\Personal attachements\Projects\Wadjet-Android\app\src\main\assets\models\hieroglyph\"
copy "D:\Personal attachements\Projects\Wadjet-v3-beta\models\landmark\*" "D:\Personal attachements\Projects\Wadjet-Android\app\src\main\assets\models\landmark\"
```

> These are large files (~45 MB total). Consider Git LFS for version control.

---

## Step 12: Sync & Build

1. In Android Studio: **File** → **Sync Project with Gradle Files**
2. Wait for sync to complete (first time downloads all dependencies)
3. **Build** → **Make Project** (Ctrl+F9)
4. Fix any compilation errors

### Common first-build issues:
| Issue | Fix |
|-------|-----|
| "Could not resolve..." | Check internet, re-sync Gradle |
| KSP version mismatch | Ensure KSP version matches Kotlin version in libs.versions.toml |
| Missing google-services.json | Complete Step 9b |
| "Namespace not specified" | Add `namespace` to each module's build.gradle.kts |

---

## Step 13: Run on Device/Emulator

1. Select your emulator or connected device from the toolbar
2. Click **Run** (Shift+F10)
3. Should see a blank screen with Night (#0A0A0A) background

### Verify Firebase Connection
- Open Firebase Console → Analytics → DebugView
- In Android Studio terminal:
  ```bash
  adb shell setprop debug.firebase.analytics.app com.wadjet.app
  ```
- Run app → check DebugView for events arriving

---

## Step 14: Create GitHub Repository

The Android project gets its own GitHub repo — completely separate from Wadjet v3 web.

```bash
cd "D:\Personal attachements\Projects\Wadjet-Android"

# 1. Create repo on GitHub: github.com/new → "Wadjet-Android" → Private → Create
# 2. Then link and push:
git remote add origin https://github.com/nadercr7/Wadjet-Android.git
git branch -M main
git push -u origin main
```

> **Remember**: `google-services.json` and `wadjet-release.jks` are in `.gitignore` — never push these.
> The `planning/` folder WILL be pushed — it's your project documentation.
> After each phase, commit with: `git add . ; git commit -m "P[N]: description" ; git push`

---

## Step 15: Commit Phase 0

```bash
git add .
git commit -m "P0: Project setup — 20 modules, Gradle, Firebase, fonts, models"
git push
```

---

## Phase 0 Completion Checklist

- [ ] Android Studio project created (`com.wadjet.app`)
- [ ] 20 modules created and listed in settings.gradle.kts
- [ ] Version catalog (`libs.versions.toml`) with all dependencies
- [ ] App module build.gradle.kts with BuildConfig, signing, dependencies
- [ ] `WadjetApplication.kt` with `@HiltAndroidApp`
- [ ] Firebase project created in Console
- [ ] `google-services.json` in `app/` directory
- [ ] Firebase Auth enabled (Google + Email)
- [ ] Firestore database created
- [ ] SHA-1 fingerprint registered
- [ ] Font files downloaded and placed
- [ ] ONNX models copied to assets
- [ ] Project compiles (Ctrl+F9)
- [ ] App runs on emulator (blank Night screen)
- [ ] Firebase Analytics events arriving
- [ ] Git repo initialized and pushed
- [ ] `.gitignore` excludes sensitive files

---

## What's Next

**Phase 1: Design System** — Use the P1 prompt from [prompts.md](prompts.md) to build:
- WadjetTheme, Colors, Typography
- All reusable components (Button, Card, TextField, etc.)
- Animations (GoldPulse, FadeUp, KenBurns)
- Design system showcase screen
