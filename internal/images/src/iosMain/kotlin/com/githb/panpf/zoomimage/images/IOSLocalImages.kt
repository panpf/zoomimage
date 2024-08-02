package com.githb.panpf.zoomimage.images

import com.github.panpf.zoomimage.subsampling.KotlinResourceImageSource
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

class IOSLocalImages private constructor() {

    companion object {

        private var instance: IOSLocalImages? = null
        private val lock = SynchronizedObject()

        suspend fun with(): IOSLocalImages {
            saveToExternalFilesDir()
            return instance ?: synchronized(lock) {
                instance ?: IOSLocalImages().also { instance = it }
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

    private val path =
        "file://${appCacheDirectory()!!.resolve("zoomimage-files")}/"

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
    return getCacheDirectory().toPath()
}

private fun getCacheDirectory(): String {
    val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
    return paths.first() as String
}