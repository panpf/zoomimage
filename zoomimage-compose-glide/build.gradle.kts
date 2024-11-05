plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.glide") {
    buildFeatures {
        compose = true
    }
}

dependencies {
    api(projects.zoomimageCompose)
    api(projects.zoomimageCoreGlide)
    api(libs.glide.ktx)
    api(libs.google.drawablepainter)
    api(libs.kotlinx.collections.immutable)

    androidTestImplementation(projects.internal.testCompose)
}