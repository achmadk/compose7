plugins {
    id("compose7.kmp-library")
}

description = "Compose7 buttons - Button, FAB, Chip, Badge"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":foundation"))
        }
    }
}
