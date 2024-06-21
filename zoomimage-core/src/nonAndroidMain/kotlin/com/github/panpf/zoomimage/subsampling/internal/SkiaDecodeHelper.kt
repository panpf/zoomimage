package com.github.panpf.zoomimage.subsampling.internal

import com.github.panpf.zoomimage.SkiaImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SkiaTileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat
import okio.use
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.impl.use

/**
 * Not thread safe
 */
class SkiaDecodeHelper(
    private val imageSource: ImageSource,
    private val bytesLazy: Lazy<ByteArray> = lazy {
        imageSource.openInputStream().getOrThrow().use { it.readBytes() }
    },
    private val initialImageInfo: ImageInfo? = null
) : DecodeHelper {

    override val imageInfo: ImageInfo by lazy { initialImageInfo ?: readImageInfo() }
    override val supportRegion: Boolean = true

    private val bytes by bytesLazy
    private var skiaImage: SkiaImage? = null

    override fun copy(): DecodeHelper {
        return SkiaDecodeHelper(imageSource, bytesLazy, imageInfo)
    }

    override fun decodeRegion(region: IntRectCompat, sampleSize: Int): TileBitmap {
        val skiaImage = getOrCreateDecoder()
        val skiaBitmap = skiaImage.decodeRegion(region, sampleSize)
        return SkiaTileBitmap(skiaBitmap)
    }

    private fun getOrCreateDecoder(): SkiaImage {
        // SkiaImage.makeFromEncoded(bytes) will parse exif orientation and does not support closing
        return skiaImage ?: SkiaImage.makeFromEncoded(bytes).apply {
            this@SkiaDecodeHelper.skiaImage = this
        }
    }

    private fun readImageInfo(): ImageInfo {
        val encodedImageFormat = Codec.makeFromData(Data.makeFromBytes(bytes)).use {
            it.encodedImageFormat
        }
        val mimeType = "image/${encodedImageFormat.name.lowercase()}"
        val skiaImage = getOrCreateDecoder()
        return ImageInfo(
            width = skiaImage.width,
            height = skiaImage.height,
            mimeType = mimeType,
        )
    }

    override fun close() {
        skiaImage?.close()
    }

    override fun toString(): String {
        return "SkiaDecodeHelper(imageSource=$imageSource, imageInfo=$imageInfo, supportRegion=$supportRegion)"
    }
}