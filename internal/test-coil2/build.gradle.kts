plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.test.coil2")

dependencies {
    api(projects.internal.testCore)
    api(projects.zoomimageCoreCoil2)
    api(libs.coil2)
}