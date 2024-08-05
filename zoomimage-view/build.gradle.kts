plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view")

dependencies {
    api(projects.zoomimageCore)
    api(libs.androidx.appcompat)
    api(libs.androidx.lifecycle.runtime)

    androidTestImplementation(projects.internal.testView)
}