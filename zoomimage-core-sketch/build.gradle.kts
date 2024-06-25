plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
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
            implementation(projects.internal.testUtils)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.testUtils)
        }
    }
}