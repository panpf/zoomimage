plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.images") {
    // Android does not support resources folders, so you can only use assets folders
    sourceSets["main"].assets.srcDirs("files")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.panpf.sketch4.core)
            api(projects.zoomimageCore)
        }
        desktopMain {
            resources.srcDirs("files")
        }
        iosMain {
            resources.srcDirs("files")
        }
        // js and wasmJs are configured in sample's build.gradle.kts
    }
}