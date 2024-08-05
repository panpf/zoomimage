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
        }
    }
}