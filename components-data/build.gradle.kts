plugins {
    id("compose7.kmp-library")
}

description = "Compose7 data display - Accordion, Timeline, Breadcrumbs, grid helpers"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":foundation"))
        }
    }
}
