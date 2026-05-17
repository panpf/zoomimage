plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.utils.coil3")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCoreCoil3)
        }
    }
}