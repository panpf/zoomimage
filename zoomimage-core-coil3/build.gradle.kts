plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlinx.kover")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.core.coil3")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCore)
            api(libs.coil3.core)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCore)
        }
        desktopTest.dependencies {
            implementation(libs.coil3.network.ktor)
            implementation(libs.ktor.client.java)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.testCore)
        }
    }
}