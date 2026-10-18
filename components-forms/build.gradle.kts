plugins {
    id("compose7.kmp-library")
}

description = "Compose7 forms - inputs, checkboxes, switches, steppers, sliders, pickers"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":foundation"))
            api(project(":components-overlays"))
        }
    }
}
