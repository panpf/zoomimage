plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlinx.kover")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.compose")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCore)
            api(libs.jetbrains.compose.foundation)
            api(libs.jetbrains.compose.runtime)
            api(libs.jetbrains.compose.ui)
//            api(compose.uiUtil)
            api(libs.kotlinx.collections.immutable)
            api(libs.jetbrains.lifecycle.runtime.compose)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCompose)
        }
        androidDeviceTest.dependencies {
            implementation(projects.internal.testCompose)
        }
    }
}