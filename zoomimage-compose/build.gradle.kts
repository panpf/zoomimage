plugins {
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlinx.kover")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.compose")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCore)
            api(compose.foundation)
            api(compose.runtime)
            api(compose.ui)
            api(compose.uiUtil)
            api(libs.kotlinx.collections.immutable)
            api(libs.jetbrains.lifecycle.runtime.compose)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCompose)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.testCompose)
        }
    }
}