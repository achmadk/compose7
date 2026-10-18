pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "compose7"

// Library modules (§10 of PRD.md — module layout, plus data/messaging split for P1/P2 groups)
include(":core")
include(":foundation")
include(":components-buttons")
include(":components-indicators")
include(":components-navigation")
include(":components-lists")
include(":components-forms")
include(":components-overlays")
include(":components-media")
include(":components-data")
include(":components-messaging")
include(":templates")

// Sample app: shared KMP UI + per-platform entry points
include(":sample-app")
include(":desktop-app")
include(":android-app")
