package com.github.panpf.zoomimage.subsampling.internal

import com.github.panpf.zoomimage.SkiaBitmap
import com.github.panpf.zoomimage.SkiaCanvas
import com.github.panpf.zoomimage.SkiaImage
import com.github.panpf.zoomimage.SkiaRect
import com.github.panpf.zoomimage.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.ExifOrientation
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseSpec
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.toSkiaRect
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream
import kotlin.math.ceil

internal actual fun createDecodeHelper(imageSource: ImageSource): DecodeHelper? {
    return SkiaDecodeHelper(imageSource)
}

@WorkerThread
internal actual fun ImageSource.decodeExifOrientation(): Result<ExifOrientation> {
    return Result.success(
        com.github.panpf.zoomimage.subsampling.EmptyExifOrientation
    )
}

@WorkerThread
internal actual fun ImageSource.decodeImageInfo(): Result<ImageInfo> {
    val inputStream: InputStream = openInputStream()
        .let { it.getOrNull() ?: return Result.failure(it.exceptionOrNull()!!) }
    var imageStream: ImageInputStream? = null
    var reader: ImageReader? = null
    try {
        imageStream = ImageIO.createImageInputStream(inputStream)
        reader = ImageIO.getImageReaders(imageStream).next().apply {
            setInput(imageStream, true, true)
        }
        val width = reader.getWidth(0)
        val height = reader.getHeight(0)
        val mimeType = "image/${reader.formatName.lowercase()}"
        return Result.success(ImageInfo(width = width, height = height, mimeType = mimeType))
    } catch (e: Throwable) {
        return Result.failure(e)
    } finally {
        reader?.dispose()
        imageStream?.close()
        inputStream.close()
    }
}

internal actual fun checkSupportSubsamplingByMimeType(mimeType: String): Boolean =
    !"image/gif".equals(mimeType, true)

actual fun createTileBitmapReuseHelper(
    logger: Logger,
    tileBitmapReuseSpec: TileBitmapReuseSpec,
): TileBitmapReuseHelper? = null

internal fun SkiaImage.decodeRegion(srcRect: IntRectCompat, sampleSize: Int): SkiaBitmap {
    val bitmapSize =
        calculateSampledBitmapSize(IntSizeCompat(srcRect.width, srcRect.height), sampleSize)
    val bitmap = SkiaBitmap().apply {
        allocN32Pixels(bitmapSize.width, bitmapSize.height)
    }
    val canvas = SkiaCanvas(bitmap)
    canvas.drawImageRect(
        image = this,
        src = srcRect.toSkiaRect(),
        dst = SkiaRect.makeWH(bitmapSize.width.toFloat(), bitmapSize.height.toFloat())
    )
    return bitmap
}

fun ImageSource.readImageInfo(): ImageInfo {
    val bytes = openInputStream().getOrThrow().use { it.readBytes() }
    val image = SkiaImage.makeFromEncoded(bytes)
    val codec = Codec.makeFromData(Data.makeFromBytes(bytes))
    val mimeType = "image/${codec.encodedImageFormat.name.lowercase()}"
    return ImageInfo(
        width = image.width,
        height = image.height,
        mimeType = mimeType,
    )
}

/**
 * Calculate the size of the sampled Bitmap, support for BitmapFactory or ImageDecoder
 */
fun calculateSampledBitmapSize(
    imageSize: IntSizeCompat,
    sampleSize: Int,
    mimeType: String? = null
): IntSizeCompat {
    val widthValue = imageSize.width / sampleSize.toDouble()
    val heightValue = imageSize.height / sampleSize.toDouble()
    val width: Int = ceil(widthValue).toInt()
    val height: Int = ceil(heightValue).toInt()
    return IntSizeCompat(width, height)
}