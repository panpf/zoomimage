plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.core.sketch3")

dependencies {
    api(projects.zoomimageCore)
    api(libs.panpf.sketch3.core)

    androidTestImplementation(projects.internal.testCore)
}