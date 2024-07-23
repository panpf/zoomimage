package com.github.panpf.zoomimage.subsampling.internal

import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import okio.buffer
import okio.use
import java.io.BufferedInputStream

/**
 * Not thread safe
 */
class BitmapRegionDecoderDecodeHelper(
    val imageSource: ImageSource,
    override val imageInfo: ImageInfo,
    override val supportRegion: Boolean,
    val exifOrientationHelper: ExifOrientationHelper,
) : DecodeHelper {

    private var _decoder: BitmapRegionDecoder? = null
    private var _inputStream: BufferedInputStream? = null

    override fun decodeRegion(
        key: String,
        region: IntRectCompat,
        sampleSize: Int
    ): TileBitmap {
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val originalRegion = exifOrientationHelper
            .applyToRect(region, imageInfo.size, reverse = true)
        val decoder = getOrCreateDecoder()
        val bitmap = decoder.decodeRegion(originalRegion.toAndroidRect(), options)
            ?: throw Exception("Invalid image. region decode return null")
        val tileBitmap = AndroidTileBitmap(bitmap, key, BitmapFrom.LOCAL)
        val correctedImage = exifOrientationHelper.applyToTileBitmap(tileBitmap)
        return correctedImage
    }

    private fun getOrCreateDecoder(): BitmapRegionDecoder {
        val decoder = _decoder
        if (decoder != null) {
            return decoder
        }
        val inputStream = imageSource.openSource().buffer().inputStream().buffered()
        val newDecoder = if (VERSION.SDK_INT >= VERSION_CODES.S) {
            BitmapRegionDecoder.newInstance(inputStream)!!
        } else {
            @Suppress("DEPRECATION")
            BitmapRegionDecoder.newInstance(inputStream, false)!!
        }
        this._decoder = newDecoder
        this._inputStream = inputStream
        return newDecoder
    }

    override fun close() {
        _decoder?.recycle()
        _inputStream?.close()
    }

    override fun copy(): DecodeHelper {
        return BitmapRegionDecoderDecodeHelper(
            imageSource = imageSource,
            imageInfo = imageInfo,
            supportRegion = supportRegion,
            exifOrientationHelper = exifOrientationHelper
        )
    }

    override fun toString(): String {
        return "BitmapFactoryDecodeHelper(imageSource=$imageSource, imageInfo=$imageInfo, supportRegion=$supportRegion, exifOrientationHelper=$exifOrientationHelper)"
    }

    private fun IntRectCompat.toAndroidRect(): Rect {
        return Rect(left, top, right, bottom)
    }

    class Factory : DecodeHelper.Factory {

        override fun create(imageSource: ImageSource): BitmapRegionDecoderDecodeHelper {
            val imageInfo = decodeImageInfo(imageSource)
            val exifOrientation1 = imageSource.decodeExifOrientation().getOrThrow()
            val exifOrientationHelper = ExifOrientationHelper(exifOrientation1)
            val correctedImageInfo = exifOrientationHelper.applyToImageInfo(imageInfo)
            val supportRegion = checkSupportSubsamplingByMimeType(imageInfo.mimeType)
            return BitmapRegionDecoderDecodeHelper(
                imageSource = imageSource,
                imageInfo = correctedImageInfo,
                supportRegion = supportRegion,
                exifOrientationHelper = exifOrientationHelper
            )
        }

        private fun decodeImageInfo(imageSource: ImageSource): ImageInfo {
            val boundOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            println("openSource: decodeImageInfo")
            imageSource.openSource().buffer().inputStream().use {
                BitmapFactory.decodeStream(it, null, boundOptions)
            }
            val mimeType = boundOptions.outMimeType.orEmpty()
            val imageSize = IntSizeCompat(
                width = boundOptions.outWidth,
                height = boundOptions.outHeight
            )
            return ImageInfo(size = imageSize, mimeType = mimeType)
        }
    }
}