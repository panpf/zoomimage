plugins {
    id("com.android.library")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.test.sketch3")

dependencies {
    api(projects.internal.testCore)
    api(libs.panpf.sketch3)
}