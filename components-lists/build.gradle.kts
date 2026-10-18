plugins {
    id("compose7.kmp-library")
}

description = "Compose7 lists - List, ListItem, Swipeout, SortableList, ListIndex"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":foundation"))
        }
    }
}
