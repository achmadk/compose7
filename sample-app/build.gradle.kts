plugins {
    id("compose7.kmp-sample")
}

description = "Compose7 Kitchen Sink sample - component gallery in every style"

kotlin {
    // Static framework for the Xcode app shell - see docs/IOS.md. Declaring a
    // binary is what makes `embedAndSignAppleFrameworkForXcode` available to
    // an Xcode Run Script build phase.
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "Compose7Sample"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            api(project(":foundation"))
            api(project(":components-buttons"))
            api(project(":components-indicators"))
            api(project(":components-navigation"))
            api(project(":components-lists"))
            api(project(":components-forms"))
            api(project(":components-overlays"))
            api(project(":components-media"))
            api(project(":components-data"))
            api(project(":components-messaging"))
            api(project(":templates"))
        }
    }
}
