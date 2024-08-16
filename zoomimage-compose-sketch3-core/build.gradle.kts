plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.sketch3.core") {
    buildFeatures {
        compose = true
    }
}

dependencies {
    api(projects.zoomimageCompose)
    api(projects.zoomimageCoreSketch3)
    api(libs.panpf.sketch3.compose.core)

    androidTestImplementation(projects.internal.testCompose)
    androidTestImplementation(projects.internal.testSketch3)
}