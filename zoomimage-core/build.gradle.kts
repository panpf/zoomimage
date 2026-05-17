plugins {
    id("com.android.kotlin.multiplatform.library")
    id("com.codingfeline.buildkonfig")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlinx.atomicfu")
    id("org.jetbrains.kotlinx.kover")
}

addMultiplatformTargets(KmpTarget.entries.toTypedArray())
kmpAndroidLibrary(nameSpace = "com.github.panpf.zoomimage.core")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.jetbrains.lifecycle.common)
            api(libs.kotlin.stdlib)
            api(libs.kotlinx.coroutines.core)
            api(libs.okio)
        }
        androidMain.dependencies {
            api(libs.androidx.exifinterface)
            api(libs.kotlinx.coroutines.android)
        }
        desktopMain.dependencies {
            api(libs.kotlinx.coroutines.swing)
        }
        nonAndroidMain.dependencies {
            api(libs.skiko)
        }

        commonTest.dependencies {
            implementation(projects.internal.testCore)
        }
        androidDeviceTest.dependencies {
            implementation(projects.internal.testCore)
        }
    }
}

buildkonfig {
    packageName = "com.github.panpf.zoomimage.core"
    defaultConfigs {
        buildConfigField(
            type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            name = "VERSION_NAME",
            value = project.versionName,
            const = true
        )
        buildConfigField(
            type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            name = "VERSION_CODE",
            value = project.versionCode.toString(),
            const = true
        )
        buildConfigField(
            type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            name = "SKIKO_VERSION_NAME",
            value = libs.versions.skiko.get(),
            const = true
        )
    }
}