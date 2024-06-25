plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.sketch.core")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCompose)
            api(projects.zoomimageCoreSketch)
            api(libs.panpf.sketch4.compose.core)
        }

        commonTest.dependencies {
            implementation(projects.internal.testUtils)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.testUtils)
        }
    }
}

android {
    dependencies {
        debugImplementation(libs.androidx.compose.ui.tooling)
        debugImplementation(libs.androidx.compose.ui.test.manifest)
    }
}