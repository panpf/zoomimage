plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
}

android {
    namespace = "com.github.panpf.zoom.sample"
    compileSdk = property("compileSdk").toString().toInt()

    defaultConfig {
        applicationId = "com.github.panpf.zoom.sample"

        minSdk = property("minSdk").toString().toInt()
        targetSdk = property("targetSdk").toString().toInt()
        versionCode = property("versionCode").toString().toInt()
        versionName = property("versionName").toString()

        buildConfigField("String", "VERSION_NAME", "\"${property("versionName").toString()}\"")
        buildConfigField("int", "VERSION_CODE", property("versionCode").toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    @Suppress("UnstableApiUsage")
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
    buildTypes {
        debug {
            multiDexEnabled = true
        }
        release {
            multiDexEnabled = true
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
    implementation(project(":zoomimage-compose"))
    implementation(project(":zoomimage-view"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.google.material)
    implementation(libs.panpf.assemblyadapter4)
    implementation(libs.panpf.liveevent)
    implementation(libs.panpf.sketch3)
    implementation(libs.panpf.sketch3.extensions)
    implementation(libs.panpf.tools4a)
    implementation(libs.panpf.tools4j)
    implementation(libs.panpf.tools4k)
    implementation(libs.telephoto)
    implementation(libs.mmkv)
    implementation(libs.subsamplingscaleimageview)
    implementation(libs.photoview)

    /* compose */
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
}