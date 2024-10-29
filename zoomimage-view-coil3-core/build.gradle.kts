plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.coil3.core") {
    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }
}

dependencies {
    api(projects.zoomimageView)
    api(projects.zoomimageCoreCoil3)

    androidTestImplementation(projects.internal.testView)
    androidTestImplementation(projects.internal.testCoil3)
}