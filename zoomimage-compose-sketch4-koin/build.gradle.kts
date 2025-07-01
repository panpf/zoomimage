plugins {
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlinx.kover")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.compose.sketch4.koin")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.zoomimageComposeSketch4Core)
            api(libs.panpf.sketch4.compose.koin)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCompose)
            implementation(projects.internal.testKoin)
            implementation(projects.internal.testSketch4Core)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.testCompose)
            implementation(projects.internal.testKoin)
            implementation(projects.internal.testSketch4Core)
        }
    }
}