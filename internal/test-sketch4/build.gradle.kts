plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.test.sketch4")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.internal.testCore)
            api(projects.internal.testSketch4Core)
            api(libs.panpf.sketch4.singleton)
            api(libs.panpf.sketch4.compose.resources)
        }
    }
}