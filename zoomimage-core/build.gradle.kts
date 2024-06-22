plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-atomicfu")
}

addAllMultiplatformTargets(MultiplatformTargets.Android, MultiplatformTargets.Desktop)

androidLibrary(nameSpace = "com.github.panpf.zoomimage.core") {
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        buildConfigField("String", "VERSION_NAME", "\"${project.versionName}\"")
        buildConfigField("int", "VERSION_CODE", project.versionCode.toString())
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.jetbrains.lifecycle.common)
            api(libs.kotlinx.coroutines.core)
            api(libs.okio)
        }
        androidMain.dependencies {
            api(libs.androidx.annotation)
            api(libs.androidx.exifinterface)
            api(libs.kotlinx.coroutines.android)
        }
        desktopMain.dependencies {
            api(libs.kotlinx.coroutines.swing)
            api("com.drewnoakes:metadata-extractor:2.18.0") // TODO delete
        }
        nonAndroidMain.dependencies {
            api(libs.skiko)
        }

        commonTest.dependencies {
            implementation(projects.internal.testUtils)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.testUtils)
        }
    }
}