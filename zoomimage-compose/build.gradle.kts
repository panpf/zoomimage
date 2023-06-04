plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

android {
    namespace = "com.github.panpf.zoomimage.compose"
    compileSdk = property("compileSdk").toString().toInt()

    defaultConfig {
        minSdk = property("minSdk.compose").toString().toInt()

        buildConfigField("String", "VERSION_NAME", "\"${property("versionName").toString()}\"")
        buildConfigField("int", "VERSION_CODE", property("versionCode").toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    @Suppress("UnstableApiUsage")
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            @Suppress("UnstableApiUsage")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    api(project(":zoomimage-core"))
    api(libs.kotlin.stdlib)
    api(libs.kotlinx.coroutines.android)
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)

    /* compose */
    api(platform(libs.androidx.compose.bom))
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.foundation)
//    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.util)
    api(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
}