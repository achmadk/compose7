plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

// `-PcomposeVersion=...` lets CI exercise the previous stable Compose
// Multiplatform release without editing the catalog (PRD §10).
val composeVersion = providers.gradleProperty("composeVersion")
    .orElse(libs.versions.composeMultiplatform)
    .get()

println("compose7: building with Compose Multiplatform $composeVersion")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.compose:compose-gradle-plugin:$composeVersion")
    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:${libs.versions.dokka.get()}")
    implementation("com.vanniktech:gradle-maven-publish-plugin:${libs.versions.mavenPublish.get()}")
}
