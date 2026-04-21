plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
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

    signingConfigs {
        create("release") {
            // Load from signing.properties at project root (gitignored), fall back to env vars.
            val signingPropsFile = rootProject.file("signing.properties")
            val signingProps = mutableMapOf<String, String>()
            if (signingPropsFile.exists()) {
                signingPropsFile.readLines().forEach { raw ->
                    val line = raw.trim()
                    if (line.isNotEmpty() && !line.startsWith("#")) {
                        val idx = line.indexOf('=')
                        if (idx > 0) {
                            signingProps[line.substring(0, idx).trim()] =
                                line.substring(idx + 1).trim()
                        }
                    }
                }
            }
            fun prop(key: String, default: String = ""): String =
                signingProps[key] ?: System.getenv(key) ?: default

            val keystorePath = prop("KEYSTORE_PATH", "${rootProject.projectDir}/wadjet-release.jks")
            storeFile = file(keystorePath)
            storePassword = prop("KEYSTORE_PASSWORD")
            keyAlias = prop("KEY_ALIAS", "wadjet")
            keyPassword = prop("KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            // Use local.properties "debug.base.url" for emulator/local dev,
            // otherwise fall back to production backend
            val localBaseUrl = project.findProperty("debug.base.url") as? String
            val debugUrl = localBaseUrl ?: "https://nadercr7-wadjet-v2.hf.space"
            buildConfigField("String", "BASE_URL", "\"$debugUrl\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://nadercr7-wadjet-v2.hf.space\"")
            signingConfig = signingConfigs.getByName("release")
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

    composeCompiler {
        stabilityConfigurationFile = rootProject.layout.projectDirectory.file("compose_compiler_config.conf")
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
    // Project modules — Core
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:firebase"))
    implementation(project(":core:ml"))
    implementation(project(":core:common"))
    implementation(project(":core:ui"))

    // Project modules — Feature
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
    implementation(libs.compose.material3.window.size)
    implementation(libs.compose.material3.adaptive.navigation.suite)
    implementation(libs.compose.material.icons)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.kotlinx.serialization.json)

    // Coil (for SingletonImageLoader.Factory in Application)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.okhttp)

    // Timber
    implementation(libs.timber)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.hilt.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}