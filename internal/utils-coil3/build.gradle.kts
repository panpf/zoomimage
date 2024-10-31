plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.utils.coil3")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCoreCoil3)
        }
    }
}