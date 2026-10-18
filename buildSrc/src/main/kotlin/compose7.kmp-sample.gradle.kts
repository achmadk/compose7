/**
 * Sample-app convention: same targets as a library module, but not published.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    androidLibrary {
        namespace = "io.github.compose7.sample.app"
        // Matches the library modules (Compose 1.12 requires compileSdk 37).
        compileSdk = 37
        minSdk = 24
    }
    // iosX64 omitted: Compose Multiplatform 1.12 no longer publishes x86_64
    // iOS artifacts (Apple Silicon covers simulators via iosSimulatorArm64).
    iosArm64()
    iosSimulatorArm64()
    jvm("desktop")
    jvmToolchain(21)
}
