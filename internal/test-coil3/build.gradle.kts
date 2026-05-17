plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.test.coil")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.internal.testCore)
            api(projects.internal.utilsCoil3)
            api(projects.zoomimageComposeCoil3Core)
            api(projects.zoomimageComposeResources)
            api(libs.coil3)
        }
    }
}