plugins {
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
}

androidApplication(
    nameSpace = "com.github.panpf.zoomimage.sample",
    applicationId = "com.github.panpf.zoomimage.sample"
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
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val name = "zoomimage-sample-${variant.name}-${output.versionName.get()}.apk"
            output as com.android.build.api.variant.impl.VariantOutputImpl
            output.outputFileName = name
        }
    }
}
//// Another solution, you can only modify the prefix, AGP will automatically add the variant name
//base {
//    archivesName = "sketch-sample-${android.defaultConfig.versionName}"
//}

dependencies {
    implementation(projects.samples.shared)

    implementation(projects.zoomimageViewCoil3)
    implementation(projects.zoomimageViewGlide)
    implementation(projects.zoomimageViewPicasso)
    implementation(projects.zoomimageViewSketch4Koin)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.panpf.assemblyadapter4.pager2)
    implementation(libs.panpf.assemblyadapter4.recycler)
    implementation(libs.panpf.assemblyadapter4.recycler.paging)
    implementation(libs.panpf.sketch4.extensions.view)
    implementation(libs.panpf.tools4a.view)
    implementation(libs.photoview)
    implementation(libs.subsamplingscaleimageview)

    ksp(libs.glide.ksp)

    debugImplementation(libs.leakcanary)
}