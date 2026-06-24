plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlinx.kover")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.sketch4.koin")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageComposeSketch4Core)
            api(libs.panpf.sketch4.compose.koin)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCompose)
            implementation(projects.internal.testKoin)
            implementation(projects.internal.testSketch4)
        }
        androidDeviceTest.dependencies {
            implementation(projects.internal.testCompose)
            implementation(projects.internal.testKoin)
            implementation(projects.internal.testSketch4)
        }
    }
}