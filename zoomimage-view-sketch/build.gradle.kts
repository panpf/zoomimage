plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.sketch")

dependencies {
    api(projects.zoomimageViewSketchCore)
    api(libs.panpf.sketch4.view)

    androidTestImplementation(projects.internal.testUtils)
}