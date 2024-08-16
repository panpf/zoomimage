plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.test.sketch3")

dependencies {
    api(projects.internal.testCore)
    api(libs.panpf.sketch3)
}