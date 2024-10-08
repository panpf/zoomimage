plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.utils.coil")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCoreCoil)
            api(libs.kotlinx.collections.immutable)
        }
    }
}