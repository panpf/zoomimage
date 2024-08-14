plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlinx.kover")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.core.sketch")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCore)
            api(libs.panpf.sketch4.core)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCore)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.testCore)
        }
    }
}