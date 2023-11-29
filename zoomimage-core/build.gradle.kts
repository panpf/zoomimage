plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.com.android.library)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations.configureEach {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    jvm("desktop") {
        compilations.configureEach {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        named("androidMain") {
            dependencies {
                api(libs.kotlinx.coroutines.android)
                api(libs.androidx.exifinterface)
                api(libs.androidx.lifecycle.common)
            }
        }
        named("androidInstrumentedTest") {
            dependencies {
                implementation(libs.junit)
                implementation(libs.panpf.tools4j.test)
                implementation(libs.androidx.test.ext.junit)
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.test.rules)
                implementation(project(":zoomimage-resources"))
            }
        }

        named("commonMain") {
            dependencies {
                api(libs.androidx.annotation)
                api(libs.kotlinx.coroutines.core.jvm)
            }
        }
        named("commonTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.junit)
                implementation(libs.panpf.tools4j.test)
            }
        }

        named("desktopMain") {
            dependencies {
                api(libs.kotlinx.coroutines.swing)
                api("com.drewnoakes:metadata-extractor:2.18.0")
            }
        }

        named("desktopTest") {
            dependencies {
                implementation(project(":zoomimage-resources"))
            }
        }
    }
}

android {
    namespace = "com.github.panpf.zoomimage.core"
    compileSdk = property("compileSdk").toString().toInt()

    defaultConfig {
        minSdk = property("minSdk").toString().toInt()

        buildConfigField("String", "VERSION_NAME", "\"${property("versionName").toString()}\"")
        buildConfigField("int", "VERSION_CODE", property("versionCode").toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}