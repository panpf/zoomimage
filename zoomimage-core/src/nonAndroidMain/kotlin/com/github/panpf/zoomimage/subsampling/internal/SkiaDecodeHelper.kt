package com.github.panpf.zoomimage.subsampling.internal

import com.github.panpf.zoomimage.SkiaImage
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SkiaTileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat
import okio.buffer
import okio.use
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.impl.use

/**
 * Not thread safe
 */
class SkiaDecodeHelper(
    val imageSource: ImageSource,
    override val imageInfo: ImageInfo,
    override val supportRegion: Boolean,
    val bytes: ByteArray,
    val skiaImage: SkiaImage,
) : DecodeHelper {

    override fun decodeRegion(
        key: String,
        region: IntRectCompat,
        sampleSize: Int
    ): TileBitmap {
        // SkiaImage will parse exif orientation and does not support closing
        val skiaBitmap = skiaImage.decodeRegion(region, sampleSize)
        return SkiaTileBitmap(skiaBitmap, key, BitmapFrom.LOCAL)
    }

    override fun close() {
        skiaImage.close()
    }

    override fun copy(): DecodeHelper {
        return SkiaDecodeHelper(
            imageSource = imageSource,
            imageInfo = imageInfo,
            supportRegion = supportRegion,
            bytes = bytes,
            skiaImage = SkiaImage.makeFromEncoded(bytes)
        )
    }

    override fun toString(): String {
        return "SkiaDecodeHelper(imageSource=$imageSource, imageInfo=$imageInfo,)"
    }

    class Factory : DecodeHelper.Factory {

        override fun create(imageSource: ImageSource): SkiaDecodeHelper {
            val bytes = imageSource.openSource().buffer().use { it.readByteArray() }
            val skiaImage = SkiaImage.makeFromEncoded(bytes)
            val imageInfo = readImageInfo(bytes, skiaImage)
            val supportRegion = checkSupportSubsamplingByMimeType(imageInfo.mimeType)
            return SkiaDecodeHelper(
                imageSource = imageSource,
                imageInfo = imageInfo,
                supportRegion = supportRegion,
                bytes = bytes,
                skiaImage = skiaImage
            )
        }

        private fun readImageInfo(bytes: ByteArray, skiaImage: SkiaImage): ImageInfo {
            val encodedImageFormat = Codec.makeFromData(Data.makeFromBytes(bytes)).use {
                it.encodedImageFormat
            }
            val mimeType = "image/${encodedImageFormat.name.lowercase()}"
            return ImageInfo(
                width = skiaImage.width,
                height = skiaImage.height,
                mimeType = mimeType
            )
        }
    }
}