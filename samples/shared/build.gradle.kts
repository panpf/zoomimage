import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
}

kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.sample.compose")

kotlin {
    applyMyHierarchyTemplate()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    js {
        browser()
        binaries.executable()
        binaries.library()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
        binaries.library()
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.internal.images)
            api(projects.internal.utilsCoil3)
            api(projects.zoomimageCompose)
            api(projects.zoomimageComposeCoil3)
            api(projects.zoomimageComposeResources)
            api(projects.zoomimageComposeSketch4Koin)
            api(libs.androidx.paging.compose)
            api(libs.jetbrains.compose.components.resources)
            api(libs.jetbrains.compose.material)    // pull refresh
            api(libs.jetbrains.compose.material3)
            api(
                libs.coil3.network.ktor.get().let { "${it.group}:${it.name}:${it.version}" }) {
                // See libs.versions.toml#ktor
                exclude(group = "io.ktor", module = "ktor-client-core")
            }
            api(libs.jetbrains.compose.ui.tooling.preview)
            api(libs.jetbrains.lifecycle.viewmodel)
            api(libs.ktor.http.cio)  // See libs.versions.toml#ktor
            api(libs.jetbrains.compose.material.icons.core)
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            api(libs.koin.compose.viewmodel.navigation)
            api(libs.ktor.client.contentNegotiation)
            api(libs.ktor.serialization.kotlinxJson)
            api(libs.kotlinx.collections.immutable)
            api(libs.multiplatform.settings)
            api(libs.panpf.sketch4.animated.gif)
            api(libs.panpf.sketch4.compose.resources)
            api(libs.panpf.sketch4.extensions.compose)
            api(libs.panpf.sketch4.http.ktor3)
            api(libs.panpf.sketch4.svg)
            api(libs.voyager.navigator)
            api(libs.voyager.screenModel)
            api(libs.voyager.transitions)
            api(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            api(projects.zoomimageComposeGlide)
            api(libs.kotlinx.serialization.json)
            api(libs.androidx.activity.compose)
            api(libs.androidx.appcompat)
            api(libs.androidx.constraintlayout.compose)
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.androidx.lifecycle.viewmodel.compose)
            api(libs.androidx.lifecycle.runtime)
            api(libs.androidx.navigation.compose)
            api(libs.androidx.recyclerview)
            api(libs.coil3.gif)
            api(libs.glide.compose)
            api(libs.google.material)
            api(libs.koin.android)
            api(libs.moko.permissions)
            api(libs.moko.permissions.storage)
            api(libs.panpf.tools4a.activity)
            api(libs.panpf.tools4a.device)
            api(libs.panpf.tools4a.dimen)
            api(libs.panpf.tools4a.display)
            api(libs.panpf.tools4a.fileprovider)
            api(libs.panpf.tools4a.network)
            api(libs.panpf.tools4a.toast)
            api(libs.panpf.tools4k)
            api(libs.telephoto.coil3)
        }
        desktopMain.dependencies {
            api(compose.desktop.currentOs)
        }
        iosMain {
            // It has been configured in the internal:images module, but it is still inaccessible in the sample module.
            // This may be a bug of kmp.
            resources.srcDirs("../internal/images/files")
            dependencies {
                api(libs.moko.permissions)
                api(libs.moko.permissions.storage)
            }
        }
        wasmJsMain.dependencies {
            // https://youtrack.jetbrains.com/issue/KTOR-7934/JS-WASM-fails-with-IllegalStateException-Content-Length-mismatch-on-requesting-gzipped-content
            api(libs.ktor31.client.wasmJs)
        }
    }
}

compose.resources {
    packageOfResClass = "com.github.panpf.zoomimage.sample"
}

dependencies {
    androidRuntimeClasspath(libs.jetbrains.compose.ui.tooling)  // For compose preview
}