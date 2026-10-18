plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

description = "Compose7 desktop microbenchmarks (not published)"

kotlin {
    jvm("desktop")
    jvmToolchain(21)

    sourceSets {
        getByName("desktopMain").dependencies {
            implementation(project(":foundation"))
            implementation(project(":components-buttons"))
            implementation(project(":components-lists"))
            implementation(compose.desktop.currentOs)
        }
    }
}

// Keep benchmark bytecode at the same optimization level as library code.
val benchmark = tasks.register<JavaExec>("benchmark") {
    group = "verification"
    description = "Runs the Compose7 desktop microbenchmarks and prints a report."
    dependsOn("desktopMainClasses")

    val compilation = kotlin.targets.getByName("desktop").compilations.getByName("main")
    mainClass.set("io.github.compose7.benchmarks.BenchmarksKt")
    classpath = compilation.runtimeDependencyFiles?.let { runtime ->
        files(compilation.output.allOutputs, runtime)
    } ?: files(compilation.output.allOutputs)
    jvmArgs("-Dskiko.renderApi=SOFTWARE", "-Xmx2g")
}
