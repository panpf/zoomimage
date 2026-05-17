plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlinx.kover")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.core.coil3")

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
        androidDeviceTest.dependencies {
            implementation(projects.internal.testCore)
        }
    }
}