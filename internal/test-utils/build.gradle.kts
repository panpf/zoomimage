plugins {
    id("com.android.library")
    id("kotlinx-atomicfu")
    id("org.jetbrains.kotlin.multiplatform")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.test.utils")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.internal.images)
            api(projects.zoomimageCore)
            api(libs.kotlin.test)
            api(libs.kotlinx.coroutines.test)
        }
        jvmCommonMain.dependencies {
            api(libs.junit)
            api(libs.kotlin.test.junit)
            api(libs.panpf.tools4j.test)
        }
        androidMain.dependencies {
            api(libs.androidx.test.runner)
            api(libs.androidx.test.rules)
            api(libs.androidx.test.ext.junit)
        }
        desktopMain.dependencies {
            api(skikoAwtRuntimeDependency(libs.versions.skiko.get()))
        }
    }
}