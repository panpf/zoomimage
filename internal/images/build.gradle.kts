plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

addAllMultiplatformTargets(MultiplatformTargets.Android, MultiplatformTargets.Desktop)

androidLibrary(nameSpace = "com.github.panpf.zoomimage.images")