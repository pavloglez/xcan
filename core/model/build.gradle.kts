plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.pavloglez.xcan.core.model"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Pure data classes, minimal dependencies.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
