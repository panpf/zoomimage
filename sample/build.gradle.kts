@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
    id("androidx.navigation.safeargs.kotlin")      // Must be after kotlin plugin
}

kotlin {
    applyMyHierarchyTemplate()

    androidTarget()

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    js {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
//                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
//                    static = (static ?: mutableListOf()).apply {
//                        // Serve sources to debug inside browser
//                        add(project.projectDir.path)
//                    }
//                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.internal.images)
            implementation(projects.internal.utilsCoil3)
            implementation(projects.zoomimageCompose)
            implementation(projects.zoomimageComposeCoil3)
            implementation(projects.zoomimageComposeResources)
            implementation(projects.zoomimageComposeSketch4)
            implementation(compose.components.resources)
            implementation(compose.material)    // pull refresh
            implementation(compose.material3)
            implementation(libs.coil3.network.ktor)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.panpf.sketch4.animated.gif)
            implementation(libs.panpf.sketch4.compose.resources)
            implementation(libs.panpf.sketch4.extensions.compose)
            implementation(libs.panpf.sketch4.http.ktor3)
            implementation(libs.panpf.sketch4.svg)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.transitions)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(projects.zoomimageComposeGlide)
            implementation(projects.zoomimageViewCoil3)
            implementation(projects.zoomimageViewGlide)
            implementation(projects.zoomimageViewPicasso)
            implementation(projects.zoomimageViewSketch4)
            implementation(compose.preview) // Only available on Android and desktop platforms
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.constraintlayout)
            implementation(libs.androidx.constraintlayout.compose)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.navigation.fragment)
            implementation(libs.androidx.navigation.ui)
            implementation(libs.androidx.recyclerview)
            implementation(libs.androidx.swiperefreshlayout)
            implementation(libs.coil3.gif)
            implementation(libs.google.material)
            implementation(libs.moko.permissions)
            implementation(libs.panpf.assemblyadapter4.pager2)
            implementation(libs.panpf.assemblyadapter4.recycler)
            implementation(libs.panpf.assemblyadapter4.recycler.paging)
            implementation(libs.panpf.sketch4.extensions.view)
            implementation(libs.panpf.tools4a.activity)
            implementation(libs.panpf.tools4a.device)
            implementation(libs.panpf.tools4a.dimen)
            implementation(libs.panpf.tools4a.display)
            implementation(libs.panpf.tools4a.fileprovider)
            implementation(libs.panpf.tools4a.network)
            implementation(libs.panpf.tools4a.toast)
            implementation(libs.panpf.tools4a.view)
            implementation(libs.panpf.tools4k)
            implementation(libs.photoview)
            implementation(libs.subsamplingscaleimageview)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.preview) // Only available on Android and desktop platforms
        }
        iosMain {
            // It has been configured in the internal:images module, but it is still inaccessible in the sample module.
            // This may be a bug of kmp.
            resources.srcDirs("../internal/images/files")
            dependencies {
                implementation(libs.moko.permissions)
            }
        }
        nonJsCommonMain.dependencies {
            implementation(libs.androidx.datastore.core.okio)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.cashapp.paging.compose.common)
        }
    }
}

compose.resources {
    packageOfResClass = "com.github.panpf.zoomimage.sample.resources"
}

val appId = "com.github.panpf.zoomimage.sample"
val appName = "ZoomImage Sample"
compose.desktop {
    application {
        mainClass = "com.github.panpf.zoomimage.sample.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = appName
            packageVersion = property("versionName").toString().let {
                if (it.contains("-")) {
                    it.substring(0, it.indexOf("-"))
                } else {
                    it
                }
            }
            vendor = "panpfpanpf@outlook.com"
            description = "Image Zoom Library Sample App"
            macOS {
                bundleID = appId
                iconFile.set(project.file("icons/icon-macos.icns"))
            }
            windows {
                iconFile.set(project.file("icons/icon-windows.ico"))
            }
            linux {
                iconFile.set(project.file("icons/icon-linux.png"))
            }
            modules(
                "jdk.unsupported",  // 'sun/misc/Unsafe' error
                "java.net.http",    // 'java/net/http/HttpClient$Version ' error
            )
        }
        buildTypes.release.proguard {
            obfuscate.set(true) // Obfuscate the code
            optimize.set(true) // proguard optimization, enabled by default
            configurationFiles.from(project.file("compose-desktop.pro"))
        }
    }
}

androidApplication(
    nameSpace = appId,
    applicationId = appId
) {
    defaultConfig {
        buildConfigField("String", "VERSION_NAME", "\"${property("versionName").toString()}\"")
        buildConfigField("int", "VERSION_CODE", property("versionCode").toString())
    }
    signingConfigs {
        create("sample") {
            storeFile = project.file("sample.keystore")
            storePassword = "B027HHiiqKOMYesQ"
            keyAlias = "panpf-sample"
            keyPassword = "B027HHiiqKOMYesQ"
        }
    }
    buildTypes {
        debug {
            multiDexEnabled = true
            signingConfig = signingConfigs.getByName("sample")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("sample")
        }
    }

    flavorDimensions.add("default")

    androidResources {
        noCompress.add("bmp")
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this
            if (output is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                output.outputFileName =
                    "zoomimage-sample-${variant.name}-${variant.versionName}.apk"
            }
        }
    }

    dependencies {
        debugImplementation(libs.leakcanary)
    }
}

// https://youtrack.jetbrains.com/issue/KT-56025
afterEvaluate {
    tasks {
        val configureJs: Task.() -> Unit = {
            dependsOn(named("jsDevelopmentExecutableCompileSync"))
            dependsOn(named("jsProductionExecutableCompileSync"))
            dependsOn(named("jsTestTestDevelopmentExecutableCompileSync"))
        }
        named("jsBrowserProductionWebpack").configure(configureJs)
    }
}
// https://youtrack.jetbrains.com/issue/KT-56025
afterEvaluate {
    tasks {
        val configureWasmJs: Task.() -> Unit = {
            dependsOn(named("wasmJsDevelopmentExecutableCompileSync"))
            dependsOn(named("wasmJsProductionExecutableCompileSync"))
            dependsOn(named("wasmJsTestTestDevelopmentExecutableCompileSync"))
        }
        named("wasmJsBrowserProductionWebpack").configure(configureWasmJs)
    }
}

// https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html
// The current 1.6.1 version only supports the use of compose resources in the commonMain source set of the Feiku module.
// The files of the images module can only be added to the js module in this way.
tasks.register<Copy>("copyImagesToJsProcessedResources") {
    from(project(":internal:images").file("files"))
    into(project(":sample").file("build/processedResources/js/main/files"))
}
tasks.named("jsProcessResources") {
    dependsOn("copyImagesToJsProcessedResources")
}
tasks.register<Copy>("copyImagesToWasmJsProcessedResources") {
    from(project(":internal:images").file("files"))
    into(project(":sample").file("build/processedResources/wasmJs/main/files"))
}
tasks.named("wasmJsProcessResources") {
    dependsOn("copyImagesToWasmJsProcessedResources")
}
