package com.github.panpf.zoomimage.images

import android.content.Context
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import java.io.File

class ContentImageFiles private constructor(val context: Context) {

    companion object {

        suspend fun create(context: Context): ContentImageFiles {
            saveToExternalFilesDir(context)
            return ContentImageFiles(context)
        }

        suspend fun saveToExternalFilesDir(context: Context) = withContext(Dispatchers.IO) {
            val assetsDir = File((context.getExternalFilesDir(null) ?: context.filesDir), "assets")
            if (!assetsDir.exists()) {
                assetsDir.mkdirs()
            }
            ComposeResImageFiles.values.forEach {
                val file = File(assetsDir, it.name)
                if (!file.exists()) {
                    try {
                        it.toImageSource().openSource().buffer().inputStream().use { inputStream ->
                            file.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        file.delete()
                    }
                }
            }
        }
    }

    val cat = ComposeResImageFiles.cat.toContentImageFile(context)
    val dog = ComposeResImageFiles.dog.toContentImageFile(context)
    val anim = ComposeResImageFiles.anim.toContentImageFile(context)
    val longEnd = ComposeResImageFiles.longEnd.toContentImageFile(context)
    val longWhale = ComposeResImageFiles.longWhale.toContentImageFile(context)
    val hugeChina = ComposeResImageFiles.hugeChina.toContentImageFile(context)
    val hugeCard = ComposeResImageFiles.hugeCard.toContentImageFile(context)
    val hugeLongQmsht = ComposeResImageFiles.hugeLongQmsht.toContentImageFile(context)
    val hugeLongComic = ComposeResImageFiles.hugeLongComic.toContentImageFile(context)

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
    override val uri: String,
    override val size: IntSizeCompat,
    override val length: Long,
    override val mimeType: String,
    override val animated: Boolean = false,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED,
) : ImageFile {

    override val imageInfo: ImageInfo = ImageInfo(size = size, mimeType = mimeType)

    override fun toString(): String =
        "ContentImageFile(name='$name', uri='$uri', size=$size, exifOrientation=$exifOrientation)"
}

fun ComposeResImageFile.toContentImageFile(
    context: Context,
): ContentImageFile = ContentImageFile(
    name = this.name,
    uri = "content://${context.packageName}.fileprovider/asset_images/${this.name}",
    size = this.size,
    length = this.length,
    mimeType = this.mimeType,
    animated = this.animated,
    exifOrientation = this.exifOrientation
)