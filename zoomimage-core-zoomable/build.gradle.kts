plugins {
    id("java-library")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(project(":zoomimage-core-util"))

    testImplementation(libs.junit)
}

//plugins {
//    alias(libs.plugins.com.android.library)
//    alias(libs.plugins.org.jetbrains.kotlin.android)
//}
//
//android {
//    namespace = "com.github.panpf.zoomimage.core.zoomable"
//    compileSdk = property("compileSdk").toString().toInt()
//
//    defaultConfig {
//        minSdk = property("minSdk").toString().toInt()
//
//        buildConfigField("String", "VERSION_NAME", "\"${property("versionName").toString()}\"")
//        buildConfigField("int", "VERSION_CODE", property("versionCode").toString())
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//    buildFeatures {
//        buildConfig = true
//    }
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
//}
//
//dependencies {
//    api(libs.kotlin.stdlib)
//    api(libs.androidx.annotation)
//    api(libs.kotlinx.coroutines.android)
//    api(project(":zoomimage-core-util"))
////    api(libs.androidx.exifinterface)
////    api(libs.androidx.lifecycle.common)
////    api(libs.androidx.appcompat)
//
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.panpf.tools4j.test)
//    androidTestImplementation(libs.androidx.core.ktx)
//    androidTestImplementation(libs.androidx.test.ext.junit)
//    androidTestImplementation(libs.androidx.test.runner)
//    androidTestImplementation(libs.androidx.test.rules)
//}