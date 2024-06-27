plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}
// TODO multiplatform
android {
    namespace = "com.github.panpf.zoomimage.core.coil"
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
    api(projects.zoomimageCore)
    api(libs.coil.core)

    androidTestImplementation(projects.internal.testUtils)
}