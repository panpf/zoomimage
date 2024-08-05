plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.core.picasso") {
    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }
}

dependencies {
    api(projects.zoomimageCore)
    api(libs.picasso)

    androidTestImplementation(projects.internal.testCore)
}