plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.core.coil")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCore)
            api(libs.coil.core)
        }

        commonTest.dependencies {
            implementation(libs.coil.network.ktor)
            implementation(projects.internal.testUtils)
        }
        desktopTest.dependencies {
            implementation(libs.coil.network.ktor)
            implementation(libs.ktor.client.java)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.testUtils)
        }
    }
}