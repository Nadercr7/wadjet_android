# Wadjet Android — Project Structure

> Complete file tree for the Android project.
> Everything the project needs, organized by module.

---

## Root Structure

```
Wadjet-Android/
├── planning/                          # THIS FOLDER — do not ship
│   ├── CONSTITUTION.md
│   ├── spec.md
│   ├── architecture.md
│   ├── api-mapping.md
│   ├── design-system.md
│   ├── screens.md
│   ├── firebase-schema.md
│   ├── implementation-plan.md
│   ├── prompts.md
│   ├── project-structure.md           # THIS FILE
│   ├── i18n-strings.md
│   ├── dependencies.md
│   ├── release-checklist.md
│   ├── pre-flight-checklist.md
│   └── phase0-instructions.md
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   ├── google-services.json           # Firebase config (not in VCS)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/wadjet/app/
│       │   ├── WadjetApplication.kt   # Hilt Application
│       │   ├── MainActivity.kt        # Single Activity
│       │   └── navigation/
│       │       ├── WadjetNavGraph.kt   # NavHost + all routes
│       │       ├── Routes.kt          # Sealed class Route definitions
│       │       └── BottomNavItem.kt   # Bottom nav tab definitions
│       └── res/
│           ├── values/
│           │   ├── strings.xml         # English strings
│           │   ├── colors.xml          # Color resources (backup)
│           │   └── themes.xml          # Splash screen theme
│           ├── values-ar/
│           │   └── strings.xml         # Arabic strings
│           ├── mipmap-*/               # App icon (adaptive)
│           ├── drawable/
│           │   ├── ic_launcher_foreground.xml  # Eye of Wadjet
│           │   └── splash_logo.xml     # Splash screen logo
│           └── xml/
│               ├── backup_rules.xml
│               └── network_security_config.xml
├── core/
│   ├── designsystem/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── test/java/com/wadjet/core/designsystem/   # Unit tests
│   │       └── main/
│   │       ├── java/com/wadjet/core/designsystem/
│   │       │   ├── theme/
│   │       │   │   ├── WadjetTheme.kt
│   │       │   │   ├── WadjetColors.kt
│   │       │   │   ├── WadjetTypography.kt
│   │       │   │   ├── WadjetShapes.kt
│   │       │   │   └── WadjetFonts.kt
│   │       │   ├── component/
│   │       │   │   ├── WadjetButton.kt
│   │       │   │   ├── WadjetCard.kt
│   │       │   │   ├── WadjetTextField.kt
│   │       │   │   ├── WadjetBadge.kt
│   │       │   │   ├── WadjetTopBar.kt
│   │       │   │   ├── WadjetBottomBar.kt
│   │       │   │   ├── ShimmerEffect.kt
│   │       │   │   ├── ErrorState.kt
│   │       │   │   ├── LoadingOverlay.kt
│   │       │   │   └── WadjetToast.kt
│   │       │   └── animation/
│   │       │       ├── GoldPulse.kt
│   │       │       ├── FadeUp.kt
│   │       │       ├── KenBurnsImage.kt
│   │       │       └── GoldGradientText.kt
│   │       └── res/font/
│   │           ├── playfair_display_semibold.ttf
│   │           ├── playfair_display_bold.ttf
│   │           ├── inter_regular.ttf
│   │           ├── inter_medium.ttf
│   │           ├── inter_semibold.ttf
│   │           ├── jetbrains_mono_regular.ttf
│   │           ├── noto_sans_egyptian_hieroglyphs.ttf
│   │           ├── cairo_regular.ttf
│   │           ├── cairo_medium.ttf
│   │           ├── cairo_semibold.ttf
│   │           └── cairo_bold.ttf
│   ├── domain/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── test/java/com/wadjet/core/domain/         # Unit tests
│   │       └── main/java/com/wadjet/core/domain/
│   │       ├── model/
│   │       │   ├── User.kt
│   │       │   ├── ScanResult.kt
│   │       │   ├── DetectedGlyph.kt
│   │       │   ├── Sign.kt
│   │       │   ├── Lesson.kt
│   │       │   ├── WriteResult.kt
│   │       │   ├── Landmark.kt
│   │       │   ├── LandmarkDetail.kt
│   │       │   ├── IdentifyResult.kt
│   │       │   ├── ChatMessage.kt
│   │       │   ├── Story.kt
│   │       │   ├── Chapter.kt
│   │       │   ├── Interaction.kt
│   │       │   ├── Favorite.kt
│   │       │   ├── StoryProgress.kt
│   │       │   └── UserStats.kt
│   │       ├── repository/
│   │       │   ├── AuthRepository.kt          # Interface
│   │       │   ├── ScanRepository.kt
│   │       │   ├── DictionaryRepository.kt
│   │       │   ├── WriteRepository.kt
│   │       │   ├── LandmarkRepository.kt
│   │       │   ├── ChatRepository.kt
│   │       │   ├── StoryRepository.kt
│   │       │   ├── UserRepository.kt
│   │       │   ├── AudioRepository.kt
│   │       │   └── FeedbackRepository.kt
│   │       └── util/
│   │           └── WadjetResult.kt            # Result wrapper
│   ├── data/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── test/java/com/wadjet/core/data/           # Unit tests (MockK)
│   │       └── main/java/com/wadjet/core/data/
│   │       ├── repository/
│   │       │   ├── AuthRepositoryImpl.kt
│   │       │   ├── ScanRepositoryImpl.kt
│   │       │   ├── DictionaryRepositoryImpl.kt
│   │       │   ├── WriteRepositoryImpl.kt
│   │       │   ├── LandmarkRepositoryImpl.kt
│   │       │   ├── ChatRepositoryImpl.kt
│   │       │   ├── StoryRepositoryImpl.kt
│   │       │   ├── UserRepositoryImpl.kt
│   │       │   ├── AudioRepositoryImpl.kt
│   │       │   └── FeedbackRepositoryImpl.kt
│   │       ├── mapper/
│   │       │   ├── UserMapper.kt
│   │       │   ├── ScanMapper.kt
│   │       │   ├── DictionaryMapper.kt
│   │       │   ├── LandmarkMapper.kt
│   │       │   ├── StoryMapper.kt
│   │       │   └── ChatMapper.kt
│   │       └── di/
│   │           └── RepositoryModule.kt        # Hilt bindings
│   ├── network/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── test/java/com/wadjet/core/network/        # MockWebServer tests
│   │       └── main/java/com/wadjet/core/network/
│   │       ├── api/
│   │       │   ├── AuthApiService.kt
│   │       │   ├── UserApiService.kt
│   │       │   ├── ScanApiService.kt
│   │       │   ├── TranslateApiService.kt
│   │       │   ├── DictionaryApiService.kt
│   │       │   ├── WriteApiService.kt
│   │       │   ├── LandmarkApiService.kt
│   │       │   ├── ChatApiService.kt
│   │       │   ├── StoriesApiService.kt
│   │       │   ├── AudioApiService.kt
│   │       │   ├── FeedbackApiService.kt
│   │       │   └── HealthApiService.kt
│   │       ├── model/
│   │       │   ├── AuthDto.kt                 # All auth request/response DTOs
│   │       │   ├── UserDto.kt
│   │       │   ├── ScanDto.kt
│   │       │   ├── DictionaryDto.kt
│   │       │   ├── WriteDto.kt
│   │       │   ├── LandmarkDto.kt
│   │       │   ├── ChatDto.kt
│   │       │   ├── StoryDto.kt
│   │       │   ├── AudioDto.kt
│   │       │   ├── FeedbackDto.kt
│   │       │   └── HealthDto.kt
│   │       ├── interceptor/
│   │       │   ├── AuthInterceptor.kt
│   │       │   └── UserAgentInterceptor.kt
│   │       ├── sse/
│   │       │   └── SseClient.kt              # OkHttp SSE parser
│   │       └── di/
│   │           └── NetworkModule.kt           # OkHttp, Retrofit, API services
│   ├── database/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── androidTest/java/com/wadjet/core/database/ # Instrumented DAO tests
│   │       └── main/java/com/wadjet/core/database/
│   │       ├── WadjetDatabase.kt              # Room database
│   │       ├── entity/
│   │       │   ├── SignEntity.kt
│   │       │   ├── LandmarkEntity.kt
│   │       │   ├── StoryEntity.kt
│   │       │   └── ScanResultEntity.kt
│   │       ├── dao/
│   │       │   ├── SignDao.kt
│   │       │   ├── LandmarkDao.kt
│   │       │   ├── StoryDao.kt
│   │       │   └── ScanResultDao.kt
│   │       ├── converter/
│   │       │   └── Converters.kt              # Type converters
│   │       └── di/
│   │           └── DatabaseModule.kt
│   ├── firebase/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── test/java/com/wadjet/core/firebase/       # Unit tests
│   │       └── main/java/com/wadjet/core/firebase/
│   │       ├── auth/
│   │       │   ├── FirebaseAuthManager.kt
│   │       │   └── GoogleSignInHelper.kt
│   │       ├── firestore/
│   │       │   ├── FirestoreUserService.kt
│   │       │   ├── FirestoreFavoritesService.kt
│   │       │   ├── FirestoreProgressService.kt
│   │       │   └── FirestoreHistoryService.kt
│   │       ├── messaging/
│   │       │   └── WadjetFirebaseMessaging.kt
│   │       └── di/
│   │           └── FirebaseModule.kt
│   ├── ml/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/wadjet/core/ml/
│   │       ├── OnnxModelManager.kt            # Model loading/lifecycle
│   │       ├── HieroglyphDetector.kt          # YOLOv8s inference
│   │       ├── HieroglyphClassifier.kt        # MobileNetV3 inference
│   │       ├── LandmarkClassifier.kt          # EfficientNet-B0 inference
│   │       └── di/
│   │           └── MLModule.kt
│   ├── common/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/wadjet/core/common/
│   │       ├── Constants.kt
│   │       ├── Extensions.kt
│   │       ├── ImageUtils.kt                  # Compression, resize
│   │       ├── LanguageManager.kt             # EN/AR, RTL
│   │       └── TokenManager.kt               # EncryptedSharedPreferences
│   └── ui/
│       ├── build.gradle.kts
│       └── src/main/java/com/wadjet/core/ui/
│           ├── HieroglyphText.kt              # Composable for glyph text
│           ├── ZoomableImage.kt               # Pinch-to-zoom
│           ├── PullToRefresh.kt
│           ├── InfiniteScrollHandler.kt
│           └── MarkdownText.kt                # Markdown renderer
├── feature/
│   ├── auth/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/wadjet/feature/auth/
│   │       ├── SplashScreen.kt
│   │       ├── WelcomeScreen.kt
│   │       ├── LoginBottomSheet.kt
│   │       ├── RegisterBottomSheet.kt
│   │       ├── ForgotPasswordBottomSheet.kt
│   │       └── AuthViewModel.kt
│   ├── landing/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/wadjet/feature/landing/
│   │       ├── LandingScreen.kt
│   │       └── LandingViewModel.kt
│   ├── scan/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/wadjet/feature/scan/
│   │       ├── ScanScreen.kt                 # Camera + capture
│   │       ├── ScanResultScreen.kt           # Detection results
│   │       ├── ScanHistoryScreen.kt
│   │       ├── ScanViewModel.kt
│   │       ├── ScanUiState.kt
│   │       └── component/
│   │           ├── CameraPreview.kt
│   │           ├── ScanStepIndicator.kt
│   │           ├── GlyphResultCard.kt
│   │           └── AnnotatedImageViewer.kt
│   ├── dictionary/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/wadjet/feature/dictionary/
│   │       ├── DictionaryScreen.kt           # 3-tab container
│   │       ├── DictionaryViewModel.kt
│   │       ├── browse/
│   │       │   ├── BrowseTab.kt
│   │       │   ├── SignGrid.kt
│   │       │   ├── CategoryChips.kt
│   │       │   └── TypeFilterChips.kt
│   │       ├── learn/
│   │       │   ├── LearnTab.kt
│   │       │   ├── LessonScreen.kt
│   │       │   ├── LessonViewModel.kt
│   │       │   └── ExerciseCard.kt
│   │       ├── write/
│   │       │   ├── WriteTab.kt
│   │       │   ├── WriteViewModel.kt
│   │       │   ├── GlyphPalette.kt
│   │       │   └── ModeSelector.kt
│   │       └── component/
│   │           ├── SignDetailBottomSheet.kt
│   │           └── SignCard.kt
│   ├── explore/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/wadjet/feature/explore/
│   │       ├── ExploreScreen.kt
│   │       ├── ExploreViewModel.kt
│   │       ├── LandmarkDetailScreen.kt
│   │       ├── LandmarkDetailViewModel.kt
│   │       ├── IdentifyScreen.kt
│   │       ├── IdentifyViewModel.kt
│   │       └── component/
│   │           ├── LandmarkCard.kt
│   │           ├── ImageCarousel.kt
│   │           ├── RecommendationRow.kt
│   │           └── CategoryFilterBar.kt
│   ├── chat/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/wadjet/feature/chat/
│   │       ├── ChatScreen.kt
│   │       ├── ChatViewModel.kt
│   │       └── component/
│   │           ├── MessageBubble.kt
│   │           ├── ChatInputBar.kt
│   │           ├── StreamingIndicator.kt
│   │           └── VoiceInputButton.kt
│   ├── stories/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/wadjet/feature/stories/
│   │       ├── StoriesScreen.kt
│   │       ├── StoriesViewModel.kt
│   │       ├── StoryReaderScreen.kt
│   │       ├── StoryReaderViewModel.kt
│   │       └── component/
│   │           ├── StoryCard.kt
│   │           ├── ChapterContent.kt
│   │           ├── GlyphAnnotation.kt
│   │           ├── InteractionCard.kt
│   │           ├── ChooseGlyphInteraction.kt
│   │           ├── WriteWordInteraction.kt
│   │           ├── GlyphDiscoveryInteraction.kt
│   │           ├── StoryDecisionInteraction.kt
│   │           └── NarrationControls.kt
│   ├── dashboard/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/wadjet/feature/dashboard/
│   │       ├── DashboardScreen.kt
│   │       ├── DashboardViewModel.kt
│   │       └── component/
│   │           ├── StatCard.kt
│   │           ├── RecentScansRow.kt
│   │           ├── FavoritesSection.kt
│   │           └── ProgressSection.kt
│   ├── settings/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/wadjet/feature/settings/
│   │       ├── SettingsScreen.kt
│   │       ├── SettingsViewModel.kt
│   │       └── component/
│   │           ├── ProfileSection.kt
│   │           ├── LanguageSelector.kt
│   │           ├── TtsSettings.kt
│   │           └── AboutSection.kt
│   └── feedback/
│       ├── build.gradle.kts
│       └── src/main/java/com/wadjet/feature/feedback/
│           ├── FeedbackScreen.kt
│           └── FeedbackViewModel.kt
├── .github/
│   └── workflows/
│       └── android.yml                       # CI: build → lint → test → release
├── gradle/
│   ├── libs.versions.toml                    # Version catalog
│   └── wrapper/
├── build.gradle.kts                          # Root build file
├── settings.gradle.kts                       # Module includes
├── gradle.properties
├── local.properties                          # SDK path (not in VCS)
├── .gitignore
└── README.md
```

---

## Key Resource Files

### `app/src/main/AndroidManifest.xml`
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- Camera feature (not required — gallery still works) -->
    <uses-feature android:name="android.hardware.camera" android:required="false" />
    
    <application
        android:name=".WadjetApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.Wadjet.Splash"
        android:networkSecurityConfig="@xml/network_security_config">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- Firebase Messaging Service -->
        <service
            android:name="com.wadjet.core.firebase.messaging.WadjetFirebaseMessaging"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
        
    </application>
</manifest>
```

### `settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Wadjet"

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

## ONNX Model Files

Place these in `app/src/main/assets/models/`:
```
models/
├── hieroglyph/
│   ├── glyph_detector_uint8.onnx        (~25 MB)
│   ├── hieroglyph_classifier_uint8.onnx  (~5 MB)
│   └── label_mapping.json
└── landmark/
    ├── landmark_classifier_uint8.onnx    (~15 MB)
    └── landmark_label_mapping.json
```

Copy from web project: `D:\Personal attachements\Projects\Wadjet-v3-beta\models\`

---

## .gitignore Additions

```
# Firebase
app/google-services.json

# Local properties
local.properties

# ONNX models (large binary files — use Git LFS or download on build)  
# app/src/main/assets/models/

# Build outputs
build/
.gradle/
*.apk
*.aab

# IDE
.idea/
*.iml
```
