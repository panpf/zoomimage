package com.github.panpf.zoomimage.images

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.MyPlatformContext
import com.github.panpf.zoomimage.util.appCacheDirectory
import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use

class LocalImageFiles private constructor(cacheDir: Path) {

    companion object {

        private var instance: LocalImageFiles? = null
        private val lock = Mutex()

        suspend fun with(context: MyPlatformContext): LocalImageFiles {
            return instance ?: lock.withLock {
                instance ?: run {
                    val cacheDir = context.appCacheDirectory()!!.resolve("images")
                    saveImageToExternalFilesDir(
                        context = context,
                        imageFiles = ComposeResImageFiles.values.toList(),
                        cacheDir = cacheDir
                    )
                    LocalImageFiles(cacheDir).also { instance = it }
                }
            }
        }
    }

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

class LocalImageFile(
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
        "LocalImageFile(name='$name', uri='$uri', size=$size, exifOrientation=$exifOrientation)"
}

fun ComposeResImageFile.toLocalImageFile(
    cacheDir: Path
): LocalImageFile = LocalImageFile(
    name = this.name,
    uri = cacheDir.resolve(this.name).toString(),
    size = this.size,
    length = this.length,
    mimeType = this.mimeType,
    animated = this.animated,
    exifOrientation = this.exifOrientation
)

suspend fun saveImageToExternalFilesDir(
    context: MyPlatformContext,
    imageFiles: List<ComposeResImageFile>,
    cacheDir: Path
): Unit = withContext(ioCoroutineDispatcher()) {
    val fileSystem = FileSystem.SYSTEM
    if (!fileSystem.exists(cacheDir)) {
        fileSystem.createDirectories(cacheDir)
    }
    imageFiles.forEach { imageFile ->
        val file = cacheDir.resolve(imageFile.name)
        if (!fileSystem.exists(file) || (fileSystem.metadataOrNull(file)?.size ?: 0L) <= 0L) {
            val tempFile = "${file}.temp".toPath()
            fileSystem.delete(tempFile)
            try {
                imageFile.toImageSource().openSource()
                    .buffer().use { input ->
                        fileSystem.sink(tempFile).buffer().use { output ->
                            output.writeAll(input)
                        }
                    }
                fileSystem.atomicMove(tempFile, file)
            } catch (e: Exception) {
                fileSystem.delete(tempFile)
                throw Exception("Failed to copy ${imageFile.name} to $file", e)
            }
        }
    }
}
