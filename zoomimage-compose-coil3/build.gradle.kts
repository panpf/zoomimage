plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlinx.kover")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.coil3")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageComposeCoil3Core)
            api(libs.coil3.compose)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCompose)
            implementation(projects.internal.testCoil3)
            implementation(projects.internal.utilsCoil3Compose)
        }
        androidDeviceTest.dependencies {
            implementation(projects.internal.testCompose)
            implementation(projects.internal.testCoil3)
            implementation(projects.internal.utilsCoil3Compose)
        }
    }
}