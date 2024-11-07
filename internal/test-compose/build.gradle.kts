plugins {
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.test.compose")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.internal.testCore)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            api(compose.uiTest)
            api(libs.jetbrains.lifecycle.runtime.compose)
        }
        androidMain.dependencies {
            api(libs.androidx.compose.ui.test.junit4.android)
            api(libs.androidx.compose.ui.test.manifest)
        }
    }
}

compose.resources {
    packageOfResClass = "com.github.panpf.zoomimage.sample.test.compose"
}