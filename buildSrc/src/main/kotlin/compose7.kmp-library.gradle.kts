/**
 * Compose7 library convention.
 *
 * Every published module (core, foundation, components-*) gets:
 *  - the standard target set (Android, iOS trio, Desktop/JVM),
 *  - Compose compiler wiring,
 *  - Dokka,
 *  - Maven Central publishing (guarded so local builds need no signing keys).
 *
 * Modules only declare their namespace/description and dependencies.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

val moduleNamespace = "io.github.compose7." + project.name.split("-").joinToString(".")

kotlin {
    androidLibrary {
        namespace = moduleNamespace
        // Compose Multiplatform 1.12 ships AndroidX Compose AARs whose metadata
        // requires compileSdk 37; consumers inherit that requirement.
        compileSdk = 37
        minSdk = 24
    }
    // iosX64 omitted: Compose Multiplatform 1.12 no longer publishes x86_64
    // iOS artifacts (Apple Silicon covers simulators via iosSimulatorArm64).
    iosArm64()
    iosSimulatorArm64()
    jvm("desktop")
    jvmToolchain(21)

    sourceSets {
        commonTest.dependencies {
            val libs = project.extensions
                .getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
                .named("libs")
            implementation(libs.findLibrary("kotlin-test").get().get())
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    // Sign only when a key is available, so `publishToMavenLocal` works on dev machines.
    val hasSigningKey = System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey") != null ||
        System.getenv("SIGNING_KEY") != null ||
        providers.gradleProperty("signingInMemoryKey").isPresent
    if (hasSigningKey) {
        signAllPublications()
    }
    pom {
        name.set(project.name)
        // Lazy: module build scripts set `description` after this convention runs.
        description.set(provider { project.description ?: "Compose7 — adaptive Compose Multiplatform UI components" })
        inceptionYear.set("2026")
        url.set(providers.gradleProperty("POM_URL").orElse("https://github.com/compose7/compose7"))
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("achmadk")
                name.set("achmadk")
            }
        }
        scm {
            url.set(providers.gradleProperty("POM_SCM_URL").orElse("https://github.com/compose7/compose7"))
            connection.set(providers.gradleProperty("POM_SCM_CONNECTION").orElse("scm:git:git://github.com/compose7/compose7.git"))
            developerConnection.set(providers.gradleProperty("POM_SCM_DEV_CONNECTION").orElse("scm:git:ssh://git@github.com/compose7/compose7.git"))
        }
    }
}
