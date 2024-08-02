package com.githb.panpf.zoomimage.images

import com.github.panpf.zoomimage.subsampling.KotlinResourceImageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.harawata.appdirs.AppDirsFactory
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import java.io.File
import java.net.URI
import java.nio.file.Paths

class DesktopLocalImages private constructor() {

    companion object {

        private var instance: DesktopLocalImages? = null

        suspend fun with(): DesktopLocalImages {
            saveToExternalFilesDir()
            return instance ?: synchronized(this) {
                instance ?: DesktopLocalImages().also { instance = it }
            }
        }

        suspend fun saveToExternalFilesDir() = withContext(Dispatchers.IO) {
            val fileSystem = FileSystem.SYSTEM
            val outDir = appCacheDirectory()!!.resolve("zoomimage-files")
            if (!fileSystem.exists(outDir)) {
                fileSystem.createDirectories(outDir)
            }
            ResourceImages.values.forEach {
                val file = outDir.resolve(it.resourceName)
                if (!fileSystem.exists(file)) {
                    try {
                        KotlinResourceImageSource(it.resourceName).openSource()
                            .buffer().use { input ->
                                fileSystem.sink(file).buffer().use { output ->
                                    output.writeAll(input)
                                }
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        fileSystem.delete(file)
                    }
                }
            }
        }
    }

    private val path = "file://${appCacheDirectory()!!.resolve("zoomimage-files")}/"

    val cat = ResourceImages.cat.let { it.copy(uri = it.uri.replace("file:///kotlin_resource/", path)) }
    val dog = ResourceImages.dog.let { it.copy(uri = it.uri.replace("file:///kotlin_resource/", path)) }
    val anim = ResourceImages.anim.let { it.copy(uri = it.uri.replace("file:///kotlin_resource/", path)) }
    val longEnd =
        ResourceImages.longEnd.let { it.copy(uri = it.uri.replace("file:///kotlin_resource/", path)) }
    val longWhale =
        ResourceImages.longWhale.let { it.copy(uri = it.uri.replace("file:///kotlin_resource/", path)) }
    val hugeChina =
        ResourceImages.hugeChina.let { it.copy(uri = it.uri.replace("file:///kotlin_resource/", path)) }
    val hugeCard =
        ResourceImages.hugeCard.let { it.copy(uri = it.uri.replace("file:///kotlin_resource/", path)) }
    val hugeLongQmsht =
        ResourceImages.hugeLongQmsht.let {
            it.copy(
                uri = it.uri.replace(
                    "file:///kotlin_resource/",
                    path
                )
            )
        }
    val hugeLongComic =
        ResourceImages.hugeLongComic.let {
            it.copy(
                uri = it.uri.replace(
                    "file:///kotlin_resource/",
                    path
                )
            )
        }

    val exifFlipHorizontal =
        ResourceImages.exifFlipHorizontal.let {
            it.copy(
                uri = it.uri.replace(
                    "file:///kotlin_resource/",
                    path
                )
            )
        }
    val exifFlipVertical =
        ResourceImages.exifFlipVertical.let {
            it.copy(
                uri = it.uri.replace(
                    "file:///kotlin_resource/",
                    path
                )
            )
        }
    val exifNormal =
        ResourceImages.exifNormal.let { it.copy(uri = it.uri.replace("file:///kotlin_resource/", path)) }
    val exifRotate90 =
        ResourceImages.exifRotate90.let {
            it.copy(
                uri = it.uri.replace(
                    "file:///kotlin_resource/",
                    path
                )
            )
        }
    val exifRotate180 =
        ResourceImages.exifRotate180.let {
            it.copy(
                uri = it.uri.replace(
                    "file:///kotlin_resource/",
                    path
                )
            )
        }
    val exifRotate270 =
        ResourceImages.exifRotate270.let {
            it.copy(
                uri = it.uri.replace(
                    "file:///kotlin_resource/",
                    path
                )
            )
        }
    val exifTranspose =
        ResourceImages.exifTranspose.let {
            it.copy(
                uri = it.uri.replace(
                    "file:///kotlin_resource/",
                    path
                )
            )
        }
    val exifTransverse =
        ResourceImages.exifTransverse.let {
            it.copy(
                uri = it.uri.replace(
                    "file:///kotlin_resource/",
                    path
                )
            )
        }

    val exifs = arrayOf(
        exifFlipHorizontal,
        exifFlipVertical,
        exifNormal,
        exifRotate90,
        exifRotate180,
        exifRotate270,
        exifTranspose,
        exifTransverse,
    )

    val all = listOf(
        cat,
        dog,
        anim,
        longEnd,
        longWhale,
        hugeChina,
        hugeCard,
        hugeLongQmsht,
        hugeLongComic,

        exifFlipHorizontal,
        exifFlipVertical,
        exifNormal,
        exifRotate90,
        exifRotate180,
        exifRotate270,
        exifTranspose,
        exifTransverse,
    )
}

fun appCacheDirectory(): Path? {
    val appName = (getComposeResourcesPath() ?: getJarPath(DesktopLocalImages::class.java))
        ?.md5()
        ?: throw UnsupportedOperationException(
            "Unable to generate application aliases to automatically initialize downloadCache and resultCache, " +
                    "please configure them manually. Documentation address 'https://github.com/panpf/sketch/blob/main/docs/wiki/getting_started.md'"
        )
    return requireNotNull(
        AppDirsFactory.getInstance().getUserCacheDir(
            /* appName = */ "SketchImageLoader${File.separator}${appName}",
            /* appVersion = */ null,
            /* appAuthor = */ null,
        )
    ) { "Failed to get the cache directory of the App" }.toPath()
}

/**
 * Returns the path to the app's compose resources directory. Only works in release mode
 *
 * Example:
 * macOs: '/Applications/hellokmp.app/Contents/app/resources'
 * Windows: 'C:\Program Files\hellokmp\app\resources'
 * Linux: '/opt/hellokmp/lib/app/resources'
 * dev: null
 */
fun getComposeResourcesPath(): String? {
    return System.getProperty("compose.application.resources.dir")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}

/**
 * Returns the path to the jar file where the specified class is located
 *
 * Example:
 * macOs: '/Applications/hellokmp.app/Contents/app/composeApp-desktop-f6c789dab561fea8ab3a9533b659d11a.jar'
 * Windows: 'C:\Program Files\hellokmp\app\composeApp-desktop-55c1f2d3ee1433be9f95b1912fbd.jar'
 * Linux: '/opt/hellokmp/lib/app/composeApp-desktop-e1e452276759301f909baa97e6a11ff4.jar'
 * dev: '/Users/panpf/Workspace/KotlinProjectDesktop/composeApp/build/classes/kotlin/desktop/main'
 */
fun getJarPath(aclass: Class<*>): String? {
    try {
        val codeSource = aclass.protectionDomain.codeSource
        if (codeSource != null) {
            val location: URI = codeSource.location.toURI()
            return Paths.get(location).toString()
        } else {
            return null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

internal fun String.md5() = encodeUtf8().md5().hex()