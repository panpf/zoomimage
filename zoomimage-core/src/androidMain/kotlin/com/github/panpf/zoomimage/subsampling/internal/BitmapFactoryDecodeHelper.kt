@file:Suppress("UnnecessaryVariable")

package com.github.panpf.zoomimage.subsampling.internal

import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.github.panpf.zoomimage.subsampling.AndroidExifOrientation
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import okio.buffer

/**
 * Not thread safe
 */
class BitmapFactoryDecodeHelper(
    private val imageSource: ImageSource,
    private val initialImageInfo: ImageInfo? = null
) : DecodeHelper {

    override val imageInfo: ImageInfo by lazy { initialImageInfo ?: decodeImageInfo() }
    override val supportRegion: Boolean by lazy { checkSupportSubsamplingByMimeType(imageInfo.mimeType) }

    private val exifOrientation: Int by lazy {
        imageSource.decodeExifOrientationValue().getOrThrow()
    }
    private val exifOrientationHelper by lazy { AndroidExifOrientation(exifOrientation) }
    private var decoder: BitmapRegionDecoder? = null

    override fun copy(): DecodeHelper {
        return BitmapFactoryDecodeHelper(imageSource, imageInfo)
    }

    override fun decodeRegion(region: IntRectCompat, sampleSize: Int): TileBitmap {
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val decoder = getOrCreateDecoder()
        val originalRegion = exifOrientationHelper
            .applyToRect(region, imageInfo.size, reverse = true)
        val bitmap = decoder.decodeRegion(originalRegion.toAndroidRect(), options)
            ?: throw Exception("Invalid image. region decode return null")
        val tileBitmap = AndroidTileBitmap(bitmap)
        val correctedImage = exifOrientationHelper.applyToTileBitmap(tileBitmap)
        return correctedImage
    }

    private fun getOrCreateDecoder(): BitmapRegionDecoder {
        return decoder ?: imageSource.openSource().getOrThrow().buffer().inputStream().buffered()
            .use {
                if (VERSION.SDK_INT >= VERSION_CODES.S) {
                    BitmapRegionDecoder.newInstance(it)!!
                } else {
                    @Suppress("DEPRECATION")
                    BitmapRegionDecoder.newInstance(it, false)!!
                }
            }.apply {
                this@BitmapFactoryDecodeHelper.decoder = this
            }
    }

    private fun decodeImageInfo(): ImageInfo {
        val boundOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        imageSource.openSource().getOrThrow().buffer().inputStream().use {
            BitmapFactory.decodeStream(it, null, boundOptions)
        }
        val mimeType = boundOptions.outMimeType.orEmpty()
        val imageSize =
            IntSizeCompat(width = boundOptions.outWidth, height = boundOptions.outHeight)
        val correctedImageSize = exifOrientationHelper.applyToSize(imageSize)
        return ImageInfo(size = correctedImageSize, mimeType = mimeType)
    }

    override fun close() {
        decoder?.recycle()
    }

    override fun toString(): String {
        return "BitmapFactoryDecodeHelper(imageSource=$imageSource)"
    }

    private fun IntRectCompat.toAndroidRect(): Rect {
        return Rect(left, top, right, bottom)
    }
}