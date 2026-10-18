plugins {
    id("compose7.kmp-library")
}

description = "Compose7 foundation - layout primitives, page/block scaffolding, card"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            api(compose.foundation)
            api(compose.animation)
        }
    }
}
