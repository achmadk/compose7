plugins {
    id("compose7.kmp-library")
}

description = "Compose7 templates - full-screen starting points (login)"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":foundation"))
            api(project(":components-forms"))
            api(project(":components-buttons"))
        }
    }
}
