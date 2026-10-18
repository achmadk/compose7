// Plugins come from the buildSrc classpath — no versions here.
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":sample-app"))
}

compose.desktop {
    application {
        mainClass = "io.github.compose7.sample.desktop.MainKt"

        nativeDistributions {
            packageName = "Compose7 Kitchen Sink"
            packageVersion = "0.1.0"
            description = "Compose7 component gallery"
        }
    }
}
