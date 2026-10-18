plugins {
    id("compose7.kmp-library")
}

description = "Compose7 overlays - Dialog, ActionSheet, SheetModal, Popover, Popup, Toast"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":foundation"))
        }
    }
}
