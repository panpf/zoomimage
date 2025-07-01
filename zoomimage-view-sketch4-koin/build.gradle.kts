plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.sketch4.koin")

dependencies {
    api(projects.zoomimageViewSketch4Core)
    api(libs.panpf.sketch4.view.koin)

    androidTestImplementation(projects.internal.testView)
}