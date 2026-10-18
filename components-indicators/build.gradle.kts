plugins {
    id("compose7.kmp-library")
}

description = "Compose7 indicators - ProgressBar, Preloader, Gauge, Skeleton"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":foundation"))
        }
    }
}
