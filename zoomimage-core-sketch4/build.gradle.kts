plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlinx.kover")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.core.sketch4")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCore)
            api(libs.panpf.sketch4.core)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCore)
            implementation(libs.panpf.sketch4.http.ktor3)
            implementation(libs.panpf.sketch4.compose.resources)
        }
        androidDeviceTest.dependencies {
            implementation(projects.internal.testCore)
            implementation(libs.panpf.sketch4.http.ktor3)
            implementation(libs.panpf.sketch4.compose.resources)
        }
    }
}