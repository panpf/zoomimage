package com.githb.panpf.zoomimage.images

import com.github.panpf.zoomimage.util.IntSizeCompat
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
            val outDir = appCacheDirectory().resolve("zoomimage-files")
            if (!fileSystem.exists(outDir)) {
                fileSystem.createDirectories(outDir)
            }
            ComposeResImageFiles.values.forEach {
                val file = outDir.resolve(it.name)
                if (!fileSystem.exists(file)) {
                    try {
                        it.toImageSource().openSource()
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

    private val cacheDir = "${appCacheDirectory().resolve("zoomimage-files")}".toPath()

    val cat = ComposeResImageFiles.cat.toLocalImageFile(cacheDir)
    val dog = ComposeResImageFiles.dog.toLocalImageFile(cacheDir)
    val anim = ComposeResImageFiles.anim.toLocalImageFile(cacheDir)
    val longEnd = ComposeResImageFiles.longEnd.toLocalImageFile(cacheDir)
    val longWhale = ComposeResImageFiles.longWhale.toLocalImageFile(cacheDir)
    val hugeChina = ComposeResImageFiles.hugeChina.toLocalImageFile(cacheDir)
    val hugeCard = ComposeResImageFiles.hugeCard.toLocalImageFile(cacheDir)
    val hugeLongQmsht = ComposeResImageFiles.hugeLongQmsht.toLocalImageFile(cacheDir)
    val hugeLongComic = ComposeResImageFiles.hugeLongComic.toLocalImageFile(cacheDir)

    val exifFlipHorizontal = ComposeResImageFiles.exifFlipHorizontal.toLocalImageFile(cacheDir)
    val exifFlipVertical = ComposeResImageFiles.exifFlipVertical.toLocalImageFile(cacheDir)
    val exifNormal = ComposeResImageFiles.exifNormal.toLocalImageFile(cacheDir)
    val exifRotate90 = ComposeResImageFiles.exifRotate90.toLocalImageFile(cacheDir)
    val exifRotate180 = ComposeResImageFiles.exifRotate180.toLocalImageFile(cacheDir)
    val exifRotate270 = ComposeResImageFiles.exifRotate270.toLocalImageFile(cacheDir)
    val exifTranspose = ComposeResImageFiles.exifTranspose.toLocalImageFile(cacheDir)
    val exifTransverse = ComposeResImageFiles.exifTransverse.toLocalImageFile(cacheDir)

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

class IosLocalImageFile(
    override val name: String,
    override val uri: String,
    override val size: IntSizeCompat,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED,
) : ImageFile {

    override fun toString(): String =
        "IosLocalImageFile(name='$name', uri='$uri', size=$size, exifOrientation=$exifOrientation)"
}

fun ComposeResImageFile.toLocalImageFile(
    cacheDir: Path
): IosLocalImageFile = IosLocalImageFile(
    name = this.name,
    uri = cacheDir.resolve(this.name).toString(),
    size = this.size,
    exifOrientation = this.exifOrientation
)

fun appCacheDirectory(): Path {
    return getCacheDirectory().toPath()
}

private fun getCacheDirectory(): String {
    val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
    return paths.first() as String
}