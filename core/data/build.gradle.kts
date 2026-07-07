plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.wadjet.core.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:firebase"))
    implementation(project(":core:common"))
    implementation(project(":core:ml"))

    // OkHttp SSE (for chat streaming)
    implementation(libs.okhttp.sse)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // WorkManager (E-P1 story prefetch)
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.ext.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
