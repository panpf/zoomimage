plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.picasso")

dependencies {
    api(projects.zoomimageView)
    api(projects.zoomimageCorePicasso)

    androidTestImplementation(projects.internal.testView)
}