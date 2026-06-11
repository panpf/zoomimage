package com.github.panpf.zoomimage.images

import android.content.Context
import android.os.Environment
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import java.io.File

class AndroidLocalImageFiles private constructor() {

    companion object {

        private var instance: AndroidLocalImageFiles? = null

        suspend fun with(context: Context): AndroidLocalImageFiles {
            saveToExternalFilesDir(context)
            return instance ?: synchronized(this) {
                instance ?: AndroidLocalImageFiles().also { instance = it }
            }
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

    private val cacheDir =
        File("${Environment.getExternalStorageDirectory()}/Android/data/com.github.panpf.zoomimage.sample/files/assets/")

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