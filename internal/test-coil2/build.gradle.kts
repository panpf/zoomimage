plugins {
    id("com.android.library")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.test.coil2")

dependencies {
    api(projects.internal.testCore)
    api(projects.zoomimageCoreCoil2)
    api(libs.coil2)
}