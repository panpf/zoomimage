package com.github.panpf.zoomimage.images

import android.content.Context
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Path.Companion.toOkioPath
import java.io.File

class ContentImageFiles private constructor() {

    companion object {

        private var instance: ContentImageFiles? = null
        private val lock = Mutex()

        suspend fun with(context: Context): ContentImageFiles {
            return instance ?: lock.withLock {
                instance ?: run {
                    val cacheDir =
                        File((context.getExternalFilesDir(null) ?: context.filesDir), "assets")
                    saveImageToExternalFilesDir(
//                        imageFiles = ComposeResImageFiles.values.toList(),
                        imageFiles = listOf(element = ComposeResImageFiles.cat),
                        cacheDir = cacheDir.toOkioPath()
                    )
                    ContentImageFiles().also { instance = it }
                }
            }
        }
    }

    val cat = ComposeResImageFiles.cat.toContentImageFile()
//    val dog = ComposeResImageFiles.dog.toContentImageFile()
//    val anim = ComposeResImageFiles.anim.toContentImageFile()
//    val longEnd = ComposeResImageFiles.longEnd.toContentImageFile()
//    val longWhale = ComposeResImageFiles.longWhale.toContentImageFile()
//    val hugeChina = ComposeResImageFiles.hugeChina.toContentImageFile()
//    val hugeCard = ComposeResImageFiles.hugeCard.toContentImageFile()
//    val hugeLongQmsht = ComposeResImageFiles.hugeLongQmsht.toContentImageFile()
//    val hugeLongComic = ComposeResImageFiles.hugeLongComic.toContentImageFile()
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
//        hugeLongComic
//    )
}

class ContentImageFile(
    override val name: String,
    override val size: IntSizeCompat,
    override val length: Long,
    override val mimeType: String,
    override val animated: Boolean = false,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED,
) : ImageFile {

    override val uri =
        "content://com.github.panpf.zoomimage.images.fileprovider/asset_images/${this.name}"

    override val imageInfo: ImageInfo = ImageInfo(size = size, mimeType = mimeType)

    override fun toString(): String =
        "ContentImageFile(name='$name', uri='$uri', size=$size, exifOrientation=$exifOrientation)"
}

fun ComposeResImageFile.toContentImageFile(): ContentImageFile = ContentImageFile(
    name = this.name,
    size = this.size,
    length = this.length,
    mimeType = this.mimeType,
    animated = this.animated,
    exifOrientation = this.exifOrientation
)