plugins {
    id("com.android.library")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.sketch4.koin")

dependencies {
    api(projects.zoomimageViewSketch4Core)
    api(libs.panpf.sketch4.view.koin)

    androidTestImplementation(projects.internal.testView)
}