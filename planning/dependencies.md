# Wadjet Android — Dependencies

> Complete dependency list with versions, organized by module.
> All versions verified against developer.android.com as of 2026-04-08.

---

## Version Catalog (`gradle/libs.versions.toml`)

```toml
[versions]
# Core Android
agp = "8.7.3"
kotlin = "2.1.0"
ksp = "2.1.0-1.0.29"

# Compose BOM (maps to Compose 1.10.x, Material3 1.4.x)
composeBom = "2026.03.00"
# NOTE: composeCompiler removed — bundled with Kotlin plugin since Kotlin 2.0

# Lifecycle & Navigation
lifecycle = "2.10.0"
navigation = "2.9.7"

# Hilt
hilt = "2.53.1"
hiltNavigation = "1.2.0"

# Network
retrofit = "2.11.0"
okhttp = "4.12.0"
kotlinxSerialization = "1.7.3"
retrofitSerializationConverter = "1.0.0"

# Firebase
firebaseBom = "33.7.0"

# Room (2.8+ raised minSdk to 23 — fine for our minSdk 26)
room = "2.8.4"

# DataStore
datastore = "1.1.1"

# Image Loading
coil = "3.0.4"

# Camera
cameraX = "1.4.1"

# ML
onnxRuntime = "1.20.0"

# Coroutines
coroutines = "1.9.0"

# Testing
junit = "4.13.2"
junitExt = "1.2.1"
espresso = "3.6.1"
mockk = "1.13.13"
turbine = "1.2.0"

# Other
timber = "5.0.1"
splashscreen = "1.0.1"
lottie = "6.6.2"
mapsCompose = "6.2.1"
composeMarkdown = "0.5.4"
playServicesAuth = "21.3.0"
credentials = "1.5.0-alpha07"
googleid = "1.1.1"
```

---

## Dependencies by Module

### `:app` (Application module)
```toml
[dependencies]
# Hilt
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }

# Splash Screen
core-splashscreen = { module = "androidx.core:core-splashscreen", version.ref = "splashscreen" }
```

### `:core:designsystem`
```toml
# Compose
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-material-icons = { module = "androidx.compose.material:material-icons-extended" }
compose-animation = { module = "androidx.compose.animation:animation" }
compose-foundation = { module = "androidx.compose.foundation:foundation" }

# Lottie (for shimmer / gold pulse animations)
lottie-compose = { module = "com.airbnb.android:lottie-compose", version.ref = "lottie" }
```

### `:core:network`
```toml
# Retrofit
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
retrofit-kotlinx-serialization = { module = "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter", version.ref = "retrofitSerializationConverter" }
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
okhttp-logging = { module = "com.squareup.okhttp3:logging-interceptor", version.ref = "okhttp" }
okhttp-sse = { module = "com.squareup.okhttp3:okhttp-sse", version.ref = "okhttp" }

# Serialization
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
```

### `:core:data`
```toml
# DataStore
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }

# Depends on :core:database, :core:firebase, :core:network (aggregation layer)
```

### `:core:database`
```toml
# Room
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
```

### `:core:firebase`
```toml
# Firebase
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { module = "com.google.firebase:firebase-auth-ktx" }
firebase-firestore = { module = "com.google.firebase:firebase-firestore-ktx" }
# firebase-storage removed — requires Blaze plan (not free on Spark)
firebase-messaging = { module = "com.google.firebase:firebase-messaging-ktx" }
firebase-analytics = { module = "com.google.firebase:firebase-analytics-ktx" }
firebase-crashlytics = { module = "com.google.firebase:firebase-crashlytics-ktx" }

# Google Sign-In (for Firebase Auth)
play-services-auth = { module = "com.google.android.gms:play-services-auth", version.ref = "playServicesAuth" }
credentials = { module = "androidx.credentials:credentials", version.ref = "credentials" }
credentials-play = { module = "androidx.credentials:credentials-play-services-auth", version.ref = "credentials" }
googleid = { module = "com.google.android.libraries.identity.googleid:googleid", version.ref = "googleid" }
```

### `:core:domain`
```toml
# Coroutines (interface definitions, use cases)
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
```

### `:core:network` (continued — serialization for DTOs)
```toml
# Serialization already listed above in :core:network
# DTO @Serializable annotations use kotlinx-serialization-json from :core:network
```

### `:core:common`
```toml
# Timber logging
timber = { module = "com.jakewharton.timber:timber", version.ref = "timber" }

# Coroutines
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
```

### `:core:ml`
```toml
# ONNX Runtime
onnxruntime-android = { module = "com.microsoft.onnxruntime:onnxruntime-android", version.ref = "onnxRuntime" }
```

### `:core:ui`
```toml
# Compose (shared UI utilities for feature modules)
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
compose-material3 = { module = "androidx.compose.material3:material3" }

# Image loading (shared across features)
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }

# Depends on :core:designsystem
```

### `:feature:scan`
```toml
# CameraX
camerax-core = { module = "androidx.camera:camera-core", version.ref = "cameraX" }
camerax-camera2 = { module = "androidx.camera:camera-camera2", version.ref = "cameraX" }
camerax-lifecycle = { module = "androidx.camera:camera-lifecycle", version.ref = "cameraX" }
camerax-view = { module = "androidx.camera:camera-view", version.ref = "cameraX" }

# Depends on :core:ml (ONNX inference) and :core:ui (Coil image loading)
```

### `:feature:explore`
```toml
# Coil (landmark images)
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }

# Google Maps (for landmark map view — spec F5.4)
maps-compose = { module = "com.google.maps.android:maps-compose", version.ref = "mapsCompose" }
```

### `:feature:chat`
```toml
# SSE streaming (via OkHttp SSE)
okhttp-sse = { module = "com.squareup.okhttp3:okhttp-sse", version.ref = "okhttp" }

# Markdown rendering (for chat messages — spec F6.1)
compose-markdown = { module = "com.github.jeziellago:compose-markdown", version.ref = "composeMarkdown" }
```

### `:feature:stories`
```toml
# Coil (story illustrations)
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
```

### All Feature Modules (shared)
```toml
# Lifecycle
lifecycle-runtime = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
lifecycle-viewmodel = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }

# Hilt
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hiltNavigation" }

# Navigation
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }

# Compose
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
```

---

## Testing Dependencies

```toml
# Unit Testing
junit = { module = "junit:junit", version.ref = "junit" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }

# Room testing
room-testing = { module = "androidx.room:room-testing", version.ref = "room" }

# Instrumentation Testing
junit-ext = { module = "androidx.test.ext:junit", version.ref = "junitExt" }
espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "espresso" }
compose-ui-test = { module = "androidx.compose.ui:ui-test-junit4" }
compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
hilt-testing = { module = "com.google.dagger:hilt-android-testing", version.ref = "hilt" }
```

---

## Gradle Plugins

```toml
[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
google-services = { id = "com.google.gms.google-services", version = "4.4.2" }
firebase-crashlytics = { id = "com.google.firebase.crashlytics", version = "3.0.2" }
```

---

## ProGuard / R8 Rules (Highlights)

```proguard
# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.wadjet.app.core.network.dto.** { *; }
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Kotlinx Serialization
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.wadjet.app.**$$serializer { *; }
-keepclassmembers class com.wadjet.app.** {
    *** Companion;
}

# ONNX Runtime
-keep class ai.onnxruntime.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
```

---

## Dependency Notes

| Decision | Rationale |
|----------|-----------|
| Coil 3 over Glide | Compose-first, coroutine-native, smaller APK |
| kotlinx.serialization over Moshi/Gson | Multiplatform, compile-time, no reflection |
| Retrofit over Ktor | Simpler API definition, mature OkHttp ecosystem |
| Hilt over Koin | Compile-time DI, Android-first, Google recommended |
| Room over raw SQLite | Type-safe queries, coroutine Flow observation |
| DataStore over SharedPreferences | Async, coroutine-based, type-safe |
| Timber over Log | Tree-based, auto-tag, zero-cost in release |
| Turbine for Flow testing | Idiomatic Flow assertion library |
| Material 3 via BOM | BOM manages M3 version automatically — no separate pinning needed |
| Firebase BOM | Single version management for all Firebase libs |
| CameraX over Camera2 | Simpler lifecycle management, backward compat |
| OkHttp SSE over Ktor | Consistent with Retrofit stack |
| Lottie | Complex animations (shimmer, gold pulse) as JSON |

---

## Size Budget

| Component | Estimated Size |
|-----------|---------------|
| Base APK (Compose + Hilt + Nav) | ~8 MB |
| Firebase (Auth + Firestore + Storage + FCM) | ~4 MB |
| Retrofit + OkHttp + Serialization | ~2 MB |
| CameraX | ~3 MB |
| ONNX Runtime Android | ~8 MB |
| ML Models (ONNX quantized) | ~45 MB |
| Coil + Room + DataStore | ~2 MB |
| Lottie | ~1 MB |
| Resources (fonts, strings, drawables) | ~3 MB |
| **Total estimated** | **~76 MB** |

> Target: < 100 MB installed. Use Android App Bundle (AAB) for Play Store — ONNX models can be delivered via Play Asset Delivery for on-demand download.
