plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.images")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCore)
            api(libs.jetbrains.compose.runtime)
            api(libs.jetbrains.compose.components.resources)
        }
        androidMain.dependencies {
            api(libs.androidx.core)
        }
        desktopMain {
            dependencies {
                api(libs.appdirs)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "com.github.panpf.zoomimage.images"
    publicResClass = true
}