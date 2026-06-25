package com.github.panpf.zoomimage.images

import android.content.Context
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.buffer
import java.io.File

class ContentImageFiles private constructor() {

    companion object {

        private var _instance: ContentImageFiles? = null
        private val lock = Mutex()

        suspend fun getInstance(context: Context): ContentImageFiles {
            val instance = _instance
            if (instance != null) {
                return instance
            }
            lock.withLock {
                val instance1 = _instance
                if (instance1 != null) {
                    return instance1
                }

                val assetsDir =
                    File((context.getExternalFilesDir(null) ?: context.filesDir), "assets")
                if (!assetsDir.exists()) {
                    assetsDir.mkdirs()
                }
                ComposeResImageFiles.values.forEach {
                    val file = File(assetsDir, it.name)
                    if (!file.exists()) {
                        try {
                            it.toImageSource().openSource().buffer().inputStream()
                                .use { inputStream ->
                                    file.outputStream().use { outputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }
                        } catch (e: Exception) {
                            file.delete()
                            throw Exception("Failed to copy ${it.name} to ${file.absolutePath}", e)
                        }
                    }
                }
                val newInstance = ContentImageFiles()
                _instance = newInstance
                return newInstance
            }
        }
    }

    val cat = ComposeResImageFiles.cat.toContentImageFile()
    val dog = ComposeResImageFiles.dog.toContentImageFile()
    val anim = ComposeResImageFiles.anim.toContentImageFile()
    val longEnd = ComposeResImageFiles.longEnd.toContentImageFile()
    val longWhale = ComposeResImageFiles.longWhale.toContentImageFile()
    val hugeChina = ComposeResImageFiles.hugeChina.toContentImageFile()
    val hugeCard = ComposeResImageFiles.hugeCard.toContentImageFile()
    val hugeLongQmsht = ComposeResImageFiles.hugeLongQmsht.toContentImageFile()
    val hugeLongComic = ComposeResImageFiles.hugeLongComic.toContentImageFile()

    val all = listOf(
        cat,
        dog,
        anim,
        longEnd,
        longWhale,
        hugeChina,
        hugeCard,
        hugeLongQmsht,
        hugeLongComic
    )
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