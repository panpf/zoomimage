plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.sketch3.core")

dependencies {
    api(projects.zoomimageView)
    api(projects.zoomimageCoreSketch3)
    api(libs.panpf.sketch3.viewability)

    androidTestImplementation(projects.internal.testView)
    androidTestImplementation(libs.panpf.sketch3)
}