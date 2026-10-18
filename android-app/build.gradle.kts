// Plugins come from the buildSrc classpath — no versions here.
// AGP 9 has built-in Kotlin support: no org.jetbrains.kotlin.android plugin.
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "io.github.compose7.sample.android"
    // Compose 1.12's AndroidX artifacts require compiling against API 37+.
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.compose7.sample"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-alpha01"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":sample-app"))
    implementation(libs.activity.compose)
}
