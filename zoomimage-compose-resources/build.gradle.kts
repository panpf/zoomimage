plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlinx.kover")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.resources")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCompose)
            api(libs.jetbrains.compose.components.resources)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCompose)
        }
        androidDeviceTest.dependencies {
            implementation(projects.internal.testCompose)
        }
    }
}