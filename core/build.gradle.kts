plugins {
    id("compose7.kmp-library")
}

description = "Compose7 core - theme engine, tokens, platform detection, native bridge API"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
            api(compose.animation)
        }
    }
}
