import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

// Load Pexels API keys from rootProject local.properties (kept out of git).
// A secondary key is used as fallback when the primary is rate-limited.
val pexelsProps: Properties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val pexelsApiKey: String = pexelsProps.getProperty("pexels.api.key", "")
val pexelsApiKey2: String = pexelsProps.getProperty("pexels.api.key.2", "")

android {
    namespace = "com.wadjet.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "APP_VERSION", "\"${findProperty("appVersionName") ?: "1.0.0"}\"")
        buildConfigField("String", "PEXELS_API_KEY", "\"$pexelsApiKey\"")
        buildConfigField("String", "PEXELS_API_KEY_2", "\"$pexelsApiKey2\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:common"))

    // Retrofit
    api(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    api(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.sse)

    // Serialization
    api(libs.kotlinx.serialization.json)

    // Security
    implementation(libs.security.crypto)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
