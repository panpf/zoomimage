plugins {
    id("com.android.library")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view")

dependencies {
    api(projects.zoomimageCore)
    api(libs.androidx.appcompat)
    api(libs.androidx.lifecycle.runtime)

    androidTestImplementation(projects.internal.testView)
}