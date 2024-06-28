plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.coil2") {
    buildFeatures {
        compose = true
    }
}

dependencies {
    api(projects.zoomimageCompose)
    api(projects.zoomimageComposeCoil2Core)
    api(libs.coil2.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(projects.internal.testUtils)
}