@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

task("clean", Delete::class) {
    delete = setOf(rootProject.buildDir)
}