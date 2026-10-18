plugins {
    id("compose7.kmp-library")
}

description = "Compose7 media - Swiper, PhotoBrowser"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":foundation"))
        }
    }
}
