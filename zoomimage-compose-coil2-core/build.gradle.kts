plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.coil2.core") {
    buildFeatures {
        compose = true
    }
}

dependencies {
    api(projects.zoomimageCompose)
    api(projects.zoomimageCoreCoil2)
    api(libs.coil2.compose.base)
    api(libs.kotlinx.collections.immutable)

    androidTestImplementation(projects.internal.testUtils)
}