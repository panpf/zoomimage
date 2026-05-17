plugins {
    id("com.android.library")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.picasso")

dependencies {
    api(projects.zoomimageView)
    api(projects.zoomimageCorePicasso)

    androidTestImplementation(projects.internal.testView)
}