plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.images") {
    // Android does not support resources folders, so you can only use assets folders
    sourceSets["main"].assets.srcDirs("files")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageCore)
        }
        desktopMain {
            resources.srcDirs("files")
            dependencies {
                api(libs.appdirs)
            }
        }
        iosMain {
            resources.srcDirs("files")
        }
        // js and wasmJs are configured in sample's build.gradle.kts
    }
}