plugins {
    id("com.android.library")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.test.view")

dependencies {
    api(projects.internal.testCore)
}
