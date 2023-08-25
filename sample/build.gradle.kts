plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
}

android {
    namespace = "com.github.panpf.zoomimage.sample"
    compileSdk = property("compileSdk").toString().toInt()

    defaultConfig {
        applicationId = "com.github.panpf.zoomimage.sample"

        minSdk = property("minSdk").toString().toInt()
        targetSdk = property("targetSdk").toString().toInt()
        versionCode = property("versionCode").toString().toInt()
        versionName = property("versionName").toString()

        buildConfigField("String", "VERSION_NAME", "\"${property("versionName").toString()}\"")
        buildConfigField("int", "VERSION_CODE", property("versionCode").toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
    val releaseSigningConfig = readReleaseSigningConfig()
    signingConfigs {
        if (releaseSigningConfig != null) {
            create("release") {
                storeFile = releaseSigningConfig.storeFile
                storePassword = releaseSigningConfig.storePassword
                keyAlias = releaseSigningConfig.keyAlias
                keyPassword = releaseSigningConfig.keyPassword
            }
        }
    }
    buildTypes {
        debug {
            multiDexEnabled = true
            if (releaseSigningConfig != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        release {
            multiDexEnabled = true
            isMinifyEnabled = true
//            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (releaseSigningConfig != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"

        // Enable Compose Compiler Report
        freeCompilerArgs = freeCompilerArgs.plus(
            arrayOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + project.buildDir.absolutePath + "/compose_metrics"
            )
        )
        // Enable Compose Compiler Metrics
        freeCompilerArgs = freeCompilerArgs.plus(
            arrayOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + project.buildDir.absolutePath + "/compose_metrics"
            )
        )
    }
}

dependencies {
    implementation(project(":zoomimage-compose-coil"))
    implementation(project(":zoomimage-compose-glide"))
    implementation(project(":zoomimage-compose-sketch"))
    implementation(project(":zoomimage-view-coil"))
    implementation(project(":zoomimage-view-glide"))
    implementation(project(":zoomimage-view-picasso"))
    implementation(project(":zoomimage-view-sketch"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.google.material)
    implementation(libs.panpf.assemblyadapter4)
    implementation(libs.panpf.tools4a)
    implementation(libs.panpf.tools4j)
    implementation(libs.panpf.tools4k)
    implementation(libs.panpf.sketch3.extensions)
    implementation(libs.mmkv)
    implementation(libs.subsamplingscaleimageview)
    implementation(libs.photoview)
    implementation(libs.androidx.constraintlayout.compose)

    /* compose */
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.paging.compose)
    implementation(libs.panpf.sketch3.compose)
    implementation(libs.telephoto)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
}

fun readReleaseSigningConfig(): ReleaseSigningConfig? {
    val localProperties = `java.util`.Properties().apply {
        project.file("local.properties")
            .takeIf { it.exists() }
            ?.inputStream()?.use { this@apply.load(it) }
    }
    val jksFile = project.file("release.jks")
    return if (
        localProperties.containsKey("signing.storePassword")
        && localProperties.containsKey("signing.keyAlias")
        && localProperties.containsKey("signing.keyPassword")
        && jksFile.exists()
    ) {
        println("hasReleaseSigningConfig: true")
        ReleaseSigningConfig(
            localProperties.getProperty("signing.storePassword"),
            localProperties.getProperty("signing.keyAlias"),
            localProperties.getProperty("signing.keyPassword"),
            jksFile
        )
    } else {
        println("hasReleaseSigningConfig: false")
        null
    }
}

class ReleaseSigningConfig(
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
    val storeFile: File,
)