plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("com.android.library")
}

addAllMultiplatformTargets(MultiplatformTargets.Android, MultiplatformTargets.Desktop)

androidLibrary(nameSpace = "com.github.panpf.zoomimage.compose")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCore)
            api(compose.foundation)
            api(compose.ui)
            api(compose.uiTooling.replace("ui-tooling", "ui-util"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.junit)
            implementation(libs.panpf.tools4j.test)
        }
    }
}