plugins {
    id("compose7.kmp-library")
}

description = "Compose7 navigation chrome - Navbar, Tabbar, Tabs, SegmentedControl, Panel, Searchbar"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":foundation"))
        }
    }
}
