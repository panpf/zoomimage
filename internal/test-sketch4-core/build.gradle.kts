plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.test.sketch4.core")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.internal.testCore)
            api(libs.panpf.sketch4.core)
        }
    }
}