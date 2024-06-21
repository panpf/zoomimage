plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}

addAllMultiplatformTargets(MultiplatformTargets.Android, MultiplatformTargets.Desktop)

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            api(libs.androidx.annotation)
            api(libs.androidx.exifinterface)
            api(libs.androidx.lifecycle.common)
            api(libs.kotlinx.coroutines.android)
        }

        desktopMain.dependencies {
            api(libs.kotlinx.coroutines.swing)
            api("com.drewnoakes:metadata-extractor:2.18.0")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmCommonTest.dependencies {
            implementation(libs.junit)
            implementation(libs.panpf.tools4j.test)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.test.ext.junit)
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.rules)
            implementation(projects.internal.images)
        }
        desktopTest.dependencies {
            implementation(projects.internal.images)
        }
    }
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.core") {
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        buildConfigField("String", "VERSION_NAME", "\"${project.versionName}\"")
        buildConfigField("int", "VERSION_CODE", project.versionCode.toString())
    }
}