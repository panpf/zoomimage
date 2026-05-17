plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlinx.kover")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.sketch4.core")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCompose)
            api(projects.zoomimageCoreSketch4)
            api(libs.panpf.sketch4.compose.core)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCompose)
            implementation(projects.internal.testSketch4)
        }
        androidDeviceTest.dependencies {
            implementation(projects.internal.testCompose)
            implementation(projects.internal.testSketch4)
        }
    }
}