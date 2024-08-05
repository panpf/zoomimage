plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.coil")

dependencies {
    api(projects.zoomimageViewCoilCore)
    api(libs.coil)

    androidTestImplementation(projects.internal.testView)
}