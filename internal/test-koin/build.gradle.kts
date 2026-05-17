plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlinx.atomicfu")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.test.koin")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.internal.testCore)
            api(libs.koin.test)
        }
    }
}