plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-atomicfu")
}

addAllMultiplatformTargets()
androidLibrary(nameSpace = "com.github.panpf.zoomimage.test.utils")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCore)
            api(libs.kotlin.test)
            api(libs.kotlinx.coroutines.test)
        }
        jvmCommonMain.dependencies {
            api(libs.junit)
            api(libs.kotlin.test.junit)
//            api(libs.panpf.tools4j.reflect)
//            api(libs.panpf.tools4j.security)
            api(libs.panpf.tools4j.test)
        }
        androidMain.dependencies {
            api(projects.internal.images)
//            api(libs.androidx.fragment)
            api(libs.androidx.test.runner)
            api(libs.androidx.test.rules)
            api(libs.androidx.test.ext.junit)
//            api(libs.panpf.tools4a.device)
//            api(libs.panpf.tools4a.dimen)
//            api(libs.panpf.tools4a.display)
//            api(libs.panpf.tools4a.network)
//            api(libs.panpf.tools4a.run)
//            api(libs.panpf.tools4a.test)
        }
        desktopMain.dependencies {
            api(projects.internal.images)
            api(skikoAwtRuntimeDependency(libs.versions.skiko.get()))
        }
    }
}