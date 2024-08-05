plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.sketch3")

dependencies {
    api(projects.zoomimageViewSketch3Core)
    api(libs.panpf.sketch3)

    androidTestImplementation(projects.internal.testView)
}