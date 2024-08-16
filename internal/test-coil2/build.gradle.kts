plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.test.coil")

dependencies {
    api(projects.internal.testCore)
    api(libs.coil2)
}