plugins {
    id("com.android.library")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.coil2")

dependencies {
    api(projects.zoomimageViewCoil2Core)
    api(libs.coil2)

    androidTestImplementation(projects.internal.testView)
}