package com.github.panpf.zoomimage.subsampling.internal

import com.drew.imaging.ImageMetadataReader
import com.github.panpf.zoomimage.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.DesktopExifOrientation
import com.github.panpf.zoomimage.subsampling.ExifOrientation
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseSpec
import com.github.panpf.zoomimage.util.Logger
import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream


@WorkerThread
internal actual fun ImageSource.decodeExifOrientation(): Result<ExifOrientation> {
    val inputStreamResult = openInputStream()
    if (inputStreamResult.isFailure) {
        return Result.failure(inputStreamResult.exceptionOrNull()!!)
    }
    val inputStream = inputStreamResult.getOrNull()!!
    val metadata = try {
        inputStream.use { ImageMetadataReader.readMetadata(it) }
    } catch (e: Exception) {
        return Result.failure(e)
    }
    val directory = metadata.directories
        .find { it.tags.find { tag -> tag.tagName == "Orientation" } != null }
    if (directory == null) {
        return Result.success(DesktopExifOrientation(ExifOrientation.ORIENTATION_UNDEFINED))
    }
    val orientationTag = directory
        .tags?.find { it.tagName == "Orientation" }
    if (orientationTag == null) {
        return Result.success(DesktopExifOrientation(ExifOrientation.ORIENTATION_UNDEFINED))
    }
    val exifOrientationInt = directory.getInt(orientationTag.tagType)
    return Result.success(DesktopExifOrientation(exifOrientationInt))
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

actual fun createTileDecoder(
    logger: Logger,
    imageSource: ImageSource,
    imageInfo: ImageInfo,
    exifOrientation: ExifOrientation?,
    tileBitmapReuseHelper: TileBitmapReuseHelper?,
): Result<TileDecoder> =
    Result.success(DesktopTileDecoder(logger, imageSource, imageInfo, exifOrientation))