plugins {
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlinx.kover")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.sketch")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageComposeSketchCore)
            api(libs.panpf.sketch4.compose)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCompose)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.testCompose)
        }
    }
}