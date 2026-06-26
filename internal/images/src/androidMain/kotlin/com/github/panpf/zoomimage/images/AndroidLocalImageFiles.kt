package com.github.panpf.zoomimage.images

import android.content.Context
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Path.Companion.toOkioPath
import java.io.File

class AndroidLocalImageFiles private constructor(cacheDir: File) {

    companion object {

        private var instance: AndroidLocalImageFiles? = null
        private val lock = Mutex()

        suspend fun with(context: Context): AndroidLocalImageFiles {
            return instance ?: lock.withLock {
                instance ?: run {
                    val cacheDir =
                        File((context.getExternalFilesDir(null) ?: context.filesDir), "assets")
                    saveImageToExternalFilesDir(
//                        imageFiles = ComposeResImageFiles.values.toList(),
                        imageFiles = listOf(element = ComposeResImageFiles.hugeLongQmsht),
                        cacheDir = cacheDir.toOkioPath()
                    )
                    AndroidLocalImageFiles(cacheDir).also { instance = it }
                }
            }
        }
    }

    //    val cat = ComposeResImageFiles.cat.toLocalImageFile(cacheDir)
//    val dog = ComposeResImageFiles.dog.toLocalImageFile(cacheDir)
//    val anim = ComposeResImageFiles.anim.toLocalImageFile(cacheDir)
//    val longEnd = ComposeResImageFiles.longEnd.toLocalImageFile(cacheDir)
//    val longWhale = ComposeResImageFiles.longWhale.toLocalImageFile(cacheDir)
//    val hugeChina = ComposeResImageFiles.hugeChina.toLocalImageFile(cacheDir)
//    val hugeCard = ComposeResImageFiles.hugeCard.toLocalImageFile(cacheDir)
    val hugeLongQmsht = ComposeResImageFiles.hugeLongQmsht.toLocalImageFile(cacheDir)
//    val hugeLongComic = ComposeResImageFiles.hugeLongComic.toLocalImageFile(cacheDir)

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

class AndroidLocalImageFile(
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
        "AndroidLocalImageFile(name='$name', uri='$uri', size=$size, exifOrientation=$exifOrientation)"
}

fun ComposeResImageFile.toLocalImageFile(
    cacheDir: File
): AndroidLocalImageFile = AndroidLocalImageFile(
    name = this.name,
    uri = File(cacheDir, this.name).path,
    size = this.size,
    length = this.length,
    mimeType = this.mimeType,
    animated = this.animated,
    exifOrientation = this.exifOrientation
)