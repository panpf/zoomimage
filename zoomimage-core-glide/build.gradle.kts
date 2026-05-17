plugins {
    id("com.android.library")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.core.glide") {
    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }
}

dependencies {
    api(projects.zoomimageCore)
    api(libs.glide)

    androidTestImplementation(projects.internal.testCore)
}