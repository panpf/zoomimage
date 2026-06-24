plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlinx.atomicfu")
    id("org.jetbrains.kotlinx.kover")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.coil3.core")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCompose)
            api(projects.zoomimageCoreCoil3)
            api(libs.coil3.compose.core)
            api(libs.kotlinx.collections.immutable)
        }
        androidMain.dependencies {
            api(libs.google.drawablepainter) // coil uses implementation dependency drawablepainer
            api(libs.androidx.vectordrawable.animated) // for coil CrossfadeDrawable
        }

        commonTest.dependencies {
            implementation(projects.internal.testCompose)
            implementation(projects.internal.testCoil3)
        }
        androidDeviceTest.dependencies {
            implementation(projects.internal.testCompose)
            implementation(projects.internal.testCoil3)
        }
    }
}