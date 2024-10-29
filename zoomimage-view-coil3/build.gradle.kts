plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.coil3")

dependencies {
    api(projects.zoomimageViewCoil3Core)
    api(libs.coil3)

    androidTestImplementation(projects.internal.testView)
}