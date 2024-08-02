plugins {
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.coil.core")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCompose)
            api(projects.zoomimageCoreCoil)
            api(libs.coil.compose.core)
            api(libs.kotlinx.collections.immutable)
        }

        commonTest.dependencies {
            implementation(projects.internal.testUtils)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.testUtils)
        }
    }
}