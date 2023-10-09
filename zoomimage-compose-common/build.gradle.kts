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
    api(project(":zoomimage-core-zoomable"))
    api(project(":zoomimage-core-subsampling"))
    implementation("org.jetbrains.compose.foundation:foundation:1.5.0")
    implementation("org.jetbrains.compose.ui:ui:1.5.0")
    implementation("org.jetbrains.compose.ui:ui-util:1.5.0")

    testImplementation(libs.junit)
}

//plugins {
//    alias(libs.plugins.com.android.library)
//    alias(libs.plugins.org.jetbrains.kotlin.android)
//}
//
//android {
//    namespace = "com.github.panpf.zoomimage.compose"
//    compileSdk = property("compileSdk").toString().toInt()
//
//    defaultConfig {
//        minSdk = property("minSdk21").toString().toInt()
//
//        buildConfigField("String", "VERSION_NAME", "\"${property("versionName").toString()}\"")
//        buildConfigField("int", "VERSION_CODE", property("versionCode").toString())
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//    buildFeatures {
//        compose = true
//        buildConfig = true
//    }
//    composeOptions {
//        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
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
//
//        // Enable Compose Compiler Report
//        freeCompilerArgs = freeCompilerArgs.plus(
//            arrayOf(
//                "-P",
//                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + project.buildDir.absolutePath + "/compose_metrics"
//            )
//        )
//        // Enable Compose Compiler Metrics
//        freeCompilerArgs = freeCompilerArgs.plus(
//            arrayOf(
//                "-P",
//                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + project.buildDir.absolutePath + "/compose_metrics"
//            )
//        )
//    }
//}
//
//dependencies {
//    api(project(":zoomimage-core-zoomable"))
//    api(project(":zoomimage-core-subsampling"))
//
//    /* compose */
//    api(libs.androidx.compose.foundation)
//    api(libs.androidx.compose.ui)
//    api(libs.androidx.compose.ui.util)
//    implementation(libs.androidx.compose.ui.tooling.preview)
//    debugImplementation(libs.androidx.compose.ui.tooling)
//    debugImplementation(libs.androidx.compose.ui.test.manifest)
//
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.panpf.tools4j.test)
//    androidTestImplementation(libs.androidx.test.ext.junit)
//    androidTestImplementation(libs.androidx.test.runner)
//    androidTestImplementation(libs.androidx.test.rules)
//}