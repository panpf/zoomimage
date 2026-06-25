plugins {
    id("com.android.library")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.core.glide") {
    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
        compileOptions {
            // Glide useds 'java.util.function.Supplier', for compatibility with Android 24-
            isCoreLibraryDesugaringEnabled = true
        }
    }
}

dependencies {
    api(projects.zoomimageCore)
    api(libs.glide)

    // Glide useds 'java.util.function.Supplier', for compatibility with Android 24-
    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    androidTestImplementation(projects.internal.testCore)
}