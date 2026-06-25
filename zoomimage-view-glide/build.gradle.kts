plugins {
    id("com.android.library")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.glide") {
    compileOptions {
        // Glide useds 'java.util.function.Supplier', for compatibility with Android 24-
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    api(projects.zoomimageView)
    api(projects.zoomimageCoreGlide)

    // Glide useds 'java.util.function.Supplier', for compatibility with Android 24-
    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    androidTestImplementation(projects.internal.testView)
}