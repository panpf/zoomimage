plugins {
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.coil")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageComposeCoilCore)
            api(libs.coil.compose)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCompose)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.testCompose)
        }
    }
}