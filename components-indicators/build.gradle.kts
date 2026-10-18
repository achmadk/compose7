plugins {
    id("compose7.kmp-library")
}

description = "Compose7 indicators - ProgressBar, Preloader, Gauge, Skeleton"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":foundation"))
        }
        // ImageComposeScene (golden tests) needs Skiko's host runtime, which
        // the library's own variants do not drag onto the test classpath.
        desktopTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
