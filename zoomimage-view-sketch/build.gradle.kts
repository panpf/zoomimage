plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.github.panpf.zoomimage.view.sketch"
    compileSdk = property("compileSdk").toString().toInt()

    defaultConfig {
        minSdk = property("minSdk").toString().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(projects.zoomimageView)
    api(projects.zoomimageCoreSketch)
    api(libs.panpf.sketch4.extensions.viewability)
    api(libs.panpf.sketch4.view.core)

    androidTestImplementation(projects.internal.testUtils)
}