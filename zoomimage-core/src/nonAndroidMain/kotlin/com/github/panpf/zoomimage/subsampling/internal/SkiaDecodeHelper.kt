package com.github.panpf.zoomimage.subsampling.internal

import com.github.panpf.zoomimage.SkiaImage
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SkiaTileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.buffer
import okio.use
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.impl.use

/**
 * Not thread safe
 */
internal class SkiaDecodeHelper(
    private val imageSource: ImageSource,
    initialBytes: ByteArray? = null,
    initialImageInfo: ImageInfo? = null
) : DecodeHelper {

    private var _bytes: ByteArray? = initialBytes
    private var _imageInfo: ImageInfo? = initialImageInfo
    private var _skiaImage: SkiaImage? = null

    override suspend fun decodeRegion(
        key: String,
        region: IntRectCompat,
        sampleSize: Int
    ): TileBitmap =
        withContext(ioCoroutineDispatcher()) {
            val skiaImage = getSkiaImage()
            val skiaBitmap = skiaImage.decodeRegion(region, sampleSize)
            SkiaTileBitmap(skiaBitmap, key, BitmapFrom.LOCAL)
        }

    private suspend fun getBytes(): ByteArray {
        val bytes = _bytes
        if (bytes != null) {
            return bytes
        }
        return withContext(ioCoroutineDispatcher()) {
            imageSource.openSource().getOrThrow().buffer().use { it.readByteArray() }
                .apply {
                    _bytes = this
                }
        }
    }

    override suspend fun getImageInfo(): ImageInfo {
        return _imageInfo ?: readImageInfo().apply {
            _imageInfo = this
        }
    }

    override suspend fun supportRegion(): Boolean = true

    private suspend fun getSkiaImage(): SkiaImage {
        val skiaImage = _skiaImage
        if (skiaImage != null) {
            return skiaImage
        }
        val bytes = getBytes()
        // SkiaImage.makeFromEncoded(bytes) will parse exif orientation and does not support closing
        return withContext(ioCoroutineDispatcher()) {
            SkiaImage.makeFromEncoded(bytes).apply {
                this@SkiaDecodeHelper._skiaImage = this
            }
        }
    }

    private suspend fun readImageInfo(): ImageInfo = withContext(ioCoroutineDispatcher()) {
        val bytes = getBytes()
        val encodedImageFormat = Codec.makeFromData(Data.makeFromBytes(bytes)).use {
            it.encodedImageFormat
        }
        val mimeType = "image/${encodedImageFormat.name.lowercase()}"
        val skiaImage = getSkiaImage()
        ImageInfo(
            width = skiaImage.width,
            height = skiaImage.height,
            mimeType = mimeType,
        )
    }

    override fun close() {
        _skiaImage?.close()
    }

    override fun copy(): DecodeHelper {
        return SkiaDecodeHelper(imageSource, _bytes, _imageInfo)
    }

    override fun toString(): String {
        return "SkiaDecodeHelper(imageSource=$imageSource)"
    }
}