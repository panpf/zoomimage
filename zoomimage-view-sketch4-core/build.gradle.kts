plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.sketch4.core")

dependencies {
    api(projects.zoomimageView)
    api(projects.zoomimageCoreSketch4)
    api(libs.panpf.sketch4.extensions.viewability)
    api(libs.panpf.sketch4.view.core)

    androidTestImplementation(projects.internal.testView)
    androidTestImplementation(libs.panpf.sketch4.view)
}