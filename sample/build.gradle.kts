import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("androidx.navigation.safeargs.kotlin")   // Must be after kotlin plugin
}

kotlin {
    applyMyHierarchyTemplate()

    androidTarget()

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(projects.internal.images)
            implementation(projects.zoomimageCompose)
            implementation(projects.zoomimageComposeResources)
            implementation(projects.zoomimageComposeSketch)
            implementation(compose.components.resources)
            implementation(compose.material)    // pull refresh
            implementation(compose.material3)
            implementation(libs.panpf.sketch4.animated)
            implementation(libs.panpf.sketch4.compose.resources)
            implementation(libs.panpf.sketch4.extensions.compose)
            implementation(libs.telephoto.subsampling)
        }
        androidMain.dependencies {
            implementation(projects.zoomimageComposeCoil)
            implementation(projects.zoomimageComposeGlide)
            implementation(projects.zoomimageViewCoil)
            implementation(projects.zoomimageViewGlide)
            implementation(projects.zoomimageViewPicasso)
            implementation(projects.zoomimageViewSketch)
            implementation(compose.preview) // Only available on Android and desktop platforms
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.constraintlayout)
            implementation(libs.androidx.constraintlayout.compose)
            implementation(libs.androidx.core)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.navigation.fragment)
            implementation(libs.androidx.navigation.ui)
            implementation(libs.androidx.paging.compose)
            implementation(libs.androidx.recyclerview)
            implementation(libs.androidx.swiperefreshlayout)
            implementation(libs.google.material)
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
//        iosMain {
//            // It has been configured in the internal:images module, but it is still inaccessible in the sample module.
//            // This may be a bug of kmp.
//            resources.srcDirs("../internal/images/files")
//        }

        commonTest.dependencies {
            implementation(projects.internal.testUtils)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.testUtils)
        }
    }
}

compose.resources {
    packageOfResClass = "com.github.panpf.zoomimage.sample.resources"
}

compose.desktop {
    application {
        mainClass = "com.github.panpf.zoomimage.sample.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.github.panpf.zoomimage.sample"
            packageVersion = property("versionName").toString().let {
                if (it.contains("-")) {
                    it.substring(0, it.indexOf("-"))
                } else {
                    it
                }
            }
        }
    }
}

androidApplication(nameSpace = "com.github.panpf.zoomimage.sample") {
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
            isShrinkResources = true
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

// https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html
// The current 1.6.1 version only supports the use of compose resources in the commonMain source set of the Feiku module.
// The files of the images module can only be added to the js module in this way.
// TODO After supporting js and wasmJs, open this configuration
//tasks.register<Copy>("copyImagesToJsProcessedResources") {
//    from(project(":internal:images").file("files"))
//    into(project(":sample").file("build/processedResources/js/main/files"))
//}
//tasks.named("jsProcessResources") {
//    dependsOn("copyImagesToJsProcessedResources")
//}
//tasks.register<Copy>("copyImagesToWasmJsProcessedResources") {
//    from(project(":internal:images").file("files"))
//    into(project(":sample").file("build/processedResources/wasmJs/main/files"))
//}
//tasks.named("wasmJsProcessResources") {
//    dependsOn("copyImagesToWasmJsProcessedResources")
//}