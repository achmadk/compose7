// No root plugins block: buildSrc (convention plugins) puts the Kotlin, Compose,
// AGP, Dokka and Maven-publish plugins on the build classpath, and re-declaring
// them here with versions fails with "plugin is already on the classpath".
// Library modules use the `compose7.kmp-library` convention; apps apply plugins
// by id (no version) in their own build scripts.
