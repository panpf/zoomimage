plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.core.coil2")

dependencies {
    api(projects.zoomimageCore)
    api(libs.coil2.base)

    androidTestImplementation(projects.internal.testCore)
}