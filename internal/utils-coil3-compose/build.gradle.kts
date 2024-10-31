plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.utils.coil3.compose")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageComposeCoil3Core)
            api(projects.internal.utilsCoil3)
            api(libs.kotlinx.collections.immutable)
        }
    }
}