plugins {
    id("com.android.library")
    id("kotlinx-atomicfu")
    id("org.jetbrains.kotlin.multiplatform")
}

addAllMultiplatformTargets()

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
            api(libs.kotlin.stdlib)
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
        }
        nonAndroidMain.dependencies {
            api(libs.skiko)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCore)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.testCore)
        }
    }
}