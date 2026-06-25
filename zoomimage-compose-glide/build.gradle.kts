plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.glide") {
    compileOptions {
        // Glide useds 'java.util.function.Supplier', for compatibility with Android 24-
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    api(projects.zoomimageCompose)
    api(projects.zoomimageCoreGlide)
    api(libs.glide.ktx)
    api(libs.google.drawablepainter)
    api(libs.kotlinx.collections.immutable)

    // Glide useds 'java.util.function.Supplier', for compatibility with Android 24-
    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    androidTestImplementation(projects.internal.testCompose)
}