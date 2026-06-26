package com.github.panpf.zoomimage.images

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

class IOSLocalImages private constructor(cacheDir: Path) {

    companion object {

        private var instance: IOSLocalImages? = null
        private val lock = Mutex()

        suspend fun with(): IOSLocalImages {
            return instance ?: lock.withLock {
                instance ?: run {
                    val cacheDir = appCacheDirectory().resolve("zoomimage-files")
                    saveImageToExternalFilesDir(
//                        imageFiles = ComposeResImageFiles.values.toList(),
                        imageFiles = listOf(element = ComposeResImageFiles.cat),
                        cacheDir = cacheDir
                    )
                    IOSLocalImages(cacheDir).also { instance = it }
                }
            }
        }
    }

    val cat = ComposeResImageFiles.cat.toLocalImageFile(cacheDir)
//    val dog = ComposeResImageFiles.dog.toLocalImageFile(cacheDir)
//    val anim = ComposeResImageFiles.anim.toLocalImageFile(cacheDir)
//    val longEnd = ComposeResImageFiles.longEnd.toLocalImageFile(cacheDir)
//    val longWhale = ComposeResImageFiles.longWhale.toLocalImageFile(cacheDir)
//    val hugeChina = ComposeResImageFiles.hugeChina.toLocalImageFile(cacheDir)
//    val hugeCard = ComposeResImageFiles.hugeCard.toLocalImageFile(cacheDir)
//    val hugeLongQmsht = ComposeResImageFiles.hugeLongQmsht.toLocalImageFile(cacheDir)
//    val hugeLongComic = ComposeResImageFiles.hugeLongComic.toLocalImageFile(cacheDir)
//
//    val exifFlipHorizontal = ComposeResImageFiles.exifFlipHorizontal.toLocalImageFile(cacheDir)
//    val exifFlipVertical = ComposeResImageFiles.exifFlipVertical.toLocalImageFile(cacheDir)
//    val exifNormal = ComposeResImageFiles.exifNormal.toLocalImageFile(cacheDir)
//    val exifRotate90 = ComposeResImageFiles.exifRotate90.toLocalImageFile(cacheDir)
//    val exifRotate180 = ComposeResImageFiles.exifRotate180.toLocalImageFile(cacheDir)
//    val exifRotate270 = ComposeResImageFiles.exifRotate270.toLocalImageFile(cacheDir)
//    val exifTranspose = ComposeResImageFiles.exifTranspose.toLocalImageFile(cacheDir)
//    val exifTransverse = ComposeResImageFiles.exifTransverse.toLocalImageFile(cacheDir)
//
//    val exifs = arrayOf(
//        exifFlipHorizontal,
//        exifFlipVertical,
//        exifNormal,
//        exifRotate90,
//        exifRotate180,
//        exifRotate270,
//        exifTranspose,
//        exifTransverse,
//    )
//
//    val all = listOf(
//        cat,
//        dog,
//        anim,
//        longEnd,
//        longWhale,
//        hugeChina,
//        hugeCard,
//        hugeLongQmsht,
//        hugeLongComic,
//
//        exifFlipHorizontal,
//        exifFlipVertical,
//        exifNormal,
//        exifRotate90,
//        exifRotate180,
//        exifRotate270,
//        exifTranspose,
//        exifTransverse,
//    )
}

class IosLocalImageFile(
    override val name: String,
    override val uri: String,
    override val size: IntSizeCompat,
    override val length: Long,
    override val mimeType: String,
    override val animated: Boolean = false,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED,
) : ImageFile {

    override val imageInfo: ImageInfo = ImageInfo(size = size, mimeType = mimeType)

    override fun toString(): String =
        "IosLocalImageFile(name='$name', uri='$uri', size=$size, exifOrientation=$exifOrientation)"
}

fun ComposeResImageFile.toLocalImageFile(
    cacheDir: Path
): IosLocalImageFile = IosLocalImageFile(
    name = this.name,
    uri = cacheDir.resolve(this.name).toString(),
    size = this.size,
    length = this.length,
    mimeType = this.mimeType,
    animated = this.animated,
    exifOrientation = this.exifOrientation
)

fun appCacheDirectory(): Path {
    return getCacheDirectory().toPath()
}

private fun getCacheDirectory(): String {
    val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
    return paths.first() as String
}