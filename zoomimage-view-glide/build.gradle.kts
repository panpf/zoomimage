plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.glide")

dependencies {
    api(projects.zoomimageView)
    api(projects.zoomimageCoreGlide)

    androidTestImplementation(projects.internal.testView)
}