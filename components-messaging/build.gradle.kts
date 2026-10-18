plugins {
    id("compose7.kmp-library")
}

description = "Compose7 messaging - Messages chat list, MessageBar"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":foundation"))
        }
    }
}
