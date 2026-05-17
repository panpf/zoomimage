plugins {
    id("com.android.library")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.coil3")

dependencies {
    api(projects.zoomimageViewCoil3Core)
    api(libs.coil3)

    androidTestImplementation(projects.internal.testView)
}