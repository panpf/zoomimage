@file:Suppress("UnnecessaryVariable")

package com.github.panpf.zoomimage.subsampling.internal

import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.github.panpf.zoomimage.subsampling.AndroidExifOrientation
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.buffer

/**
 * Not thread safe
 */
class BitmapFactoryDecodeHelper(
    private val imageSource: ImageSource,
    initialImageInfo: ImageInfo? = null
) : DecodeHelper {

    private var _imageInfo: ImageInfo? = initialImageInfo
    private var _exifOrientationHelper: AndroidExifOrientation? = null
    private var _decoder: BitmapRegionDecoder? = null

    override suspend fun decodeRegion(
        key: String,
        region: IntRectCompat,
        sampleSize: Int
    ): TileBitmap =
        withContext(ioCoroutineDispatcher()) {
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val imageInfo = getImageInfo()
            val exifOrientationHelper = getExifOrientation()
            val originalRegion = exifOrientationHelper
                .applyToRect(region, imageInfo.size, reverse = true)
            val decoder = getOrCreateDecoder()
            val bitmap = decoder.decodeRegion(originalRegion.toAndroidRect(), options)
                ?: throw Exception("Invalid image. region decode return null")
            val tileBitmap = AndroidTileBitmap(bitmap, key, BitmapFrom.LOCAL)
            val correctedImage = exifOrientationHelper.applyToTileBitmap(tileBitmap)
            correctedImage
        }

    override suspend fun getImageInfo(): ImageInfo {
        return _imageInfo ?: decodeImageInfo().apply {
            _imageInfo = this
        }
    }

    private suspend fun getExifOrientation(): AndroidExifOrientation {
        return _exifOrientationHelper ?: AndroidExifOrientation(
            imageSource.decodeExifOrientation().getOrThrow()
        ).apply {
            _exifOrientationHelper = this
        }
    }

    override suspend fun supportRegion(): Boolean {
        val imageInfo = getImageInfo()
        return checkSupportSubsamplingByMimeType(imageInfo.mimeType)
    }

    private suspend fun getOrCreateDecoder(): BitmapRegionDecoder {
        val decoder = _decoder
        if (decoder != null) {
            return decoder
        }
        return withContext(ioCoroutineDispatcher()) {
            imageSource.openSource().getOrThrow().buffer().inputStream().buffered()
                .use {
                    if (VERSION.SDK_INT >= VERSION_CODES.S) {
                        BitmapRegionDecoder.newInstance(it)!!
                    } else {
                        @Suppress("DEPRECATION")
                        BitmapRegionDecoder.newInstance(it, false)!!
                    }
                }.apply {
                    this@BitmapFactoryDecodeHelper._decoder = this
                }
        }
    }

    private suspend fun decodeImageInfo(): ImageInfo = withContext(ioCoroutineDispatcher()) {
        val boundOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        imageSource.openSource().getOrThrow().buffer().inputStream().use {
            BitmapFactory.decodeStream(it, null, boundOptions)
        }
        val mimeType = boundOptions.outMimeType.orEmpty()
        val imageSize =
            IntSizeCompat(width = boundOptions.outWidth, height = boundOptions.outHeight)
        val exifOrientationHelper = getExifOrientation()
        val correctedImageSize = exifOrientationHelper.applyToSize(imageSize)
        ImageInfo(size = correctedImageSize, mimeType = mimeType)
    }

    override fun close() {
        _decoder?.recycle()
    }

    override fun copy(): DecodeHelper {
        return BitmapFactoryDecodeHelper(imageSource, _imageInfo)
    }

    override fun toString(): String {
        return "BitmapFactoryDecodeHelper(imageSource=$imageSource)"
    }

    private fun IntRectCompat.toAndroidRect(): Rect {
        return Rect(left, top, right, bottom)
    }
}