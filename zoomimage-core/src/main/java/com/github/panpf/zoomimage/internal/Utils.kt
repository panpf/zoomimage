package com.github.panpf.zoomimage.internal

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Looper
import com.github.panpf.zoomimage.Size
import kotlin.math.ceil
import kotlin.math.floor


internal fun requiredWorkThread() {
    check(Looper.myLooper() != Looper.getMainLooper()) {
        "This method must be executed in the work thread"
    }
}

internal fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()

internal fun Bitmap.Config.isAndSupportHardware(): Boolean =
    VERSION.SDK_INT >= VERSION_CODES.O && this == Bitmap.Config.HARDWARE

/**
 * If true, indicates that the given mimeType and sampleSize combination can be using 'inBitmap' in BitmapFactory
 *
 * Test results based on the BitmapFactoryTest.testInBitmapAndInSampleSize() method
 */
@SuppressLint("ObsoleteSdkInt")
internal fun isSupportInBitmap(mimeType: String?, sampleSize: Int): Boolean =
    when {
        "image/jpeg".equals(mimeType, true) ->
            if (sampleSize == 1) VERSION.SDK_INT >= 16 else VERSION.SDK_INT >= 19
        "image/png".equals(mimeType, true) ->
            if (sampleSize == 1) VERSION.SDK_INT >= 16 else VERSION.SDK_INT >= 19
        "image/gif".equals(mimeType, true) ->
            if (sampleSize == 1) VERSION.SDK_INT >= 19 else VERSION.SDK_INT >= 21
        "image/webp".equals(mimeType, true) -> VERSION.SDK_INT >= 19
//        "image/webp".equals(mimeType, true) -> VERSION.SDK_INT >= 26 animated
        "image/bmp".equals(mimeType, true) -> VERSION.SDK_INT >= 19
        "image/heic".equals(mimeType, true) -> false
        "image/heif".equals(mimeType, true) -> VERSION.SDK_INT >= 28
        else -> VERSION.SDK_INT >= 32   // Compatible with new image types supported in the future
    }

/**
 * If true, indicates that the given mimeType can be using 'inBitmap' in BitmapRegionDecoder
 *
 * Test results based on the BitmapRegionDecoderTest.testInBitmapAndInSampleSize() method
 */
@SuppressLint("ObsoleteSdkInt")
fun isSupportInBitmapForRegion(mimeType: String?): Boolean =
    when {
        "image/jpeg".equals(mimeType, true) ->  VERSION.SDK_INT >= 16
        "image/png".equals(mimeType, true) ->  VERSION.SDK_INT >= 16
        "image/gif".equals(mimeType, true) -> false
        "image/webp".equals(mimeType, true) -> VERSION.SDK_INT >= 16
//        "image/webp".equals(mimeType, true) -> VERSION.SDK_INT >= 26 animated
        "image/bmp".equals(mimeType, true) -> false
        "image/heic".equals(mimeType, true) -> VERSION.SDK_INT >= 28
        "image/heif".equals(mimeType, true) -> VERSION.SDK_INT >= 28
        else -> VERSION.SDK_INT >= 32   // Compatible with new image types supported in the future
    }

/**
 * Calculate the size of the sampled Bitmap, support for BitmapFactory or ImageDecoder
 */
internal fun calculateSampledBitmapSize(
    imageSize: Size, sampleSize: Int, mimeType: String? = null
): Size {
    val widthValue = imageSize.width / sampleSize.toDouble()
    val heightValue = imageSize.height / sampleSize.toDouble()
    val isPNGFormat = "image/png".equals(mimeType, true)
    val width: Int
    val height: Int
    if (isPNGFormat) {
        width = floor(widthValue).toInt()
        height = floor(heightValue).toInt()
    } else {
        width = ceil(widthValue).toInt()
        height = ceil(heightValue).toInt()
    }
    return Size(width, height)
}


/**
 * Calculate the size of the sampled Bitmap, support for BitmapRegionDecoder
 */
fun calculateSampledBitmapSizeForRegion(
    regionSize: Size, sampleSize: Int, mimeType: String? = null, imageSize: Size? = null
): Size {
    val widthValue = regionSize.width / sampleSize.toDouble()
    val heightValue = regionSize.height / sampleSize.toDouble()
    val width: Int
    val height: Int
    val isPNGFormat = "image/png".equals(mimeType, true)
    if (!isPNGFormat && VERSION.SDK_INT >= VERSION_CODES.N && regionSize == imageSize) {
        width = ceil(widthValue).toInt()
        height = ceil(heightValue).toInt()
    } else {
        width = floor(widthValue).toInt()
        height = floor(heightValue).toInt()
    }
    return Size(width, height)
}

internal val Bitmap.logString: String
    get() = "Bitmap(${width}x${height},$config,@${toHexString()})"

internal fun Any.toHexString(): String = Integer.toHexString(this.hashCode())

internal val Bitmap.safeConfig: Bitmap.Config
    get() = config ?: Bitmap.Config.ARGB_8888

@SuppressLint("ObsoleteSdkInt")
fun isSupportBitmapRegionDecoder(mimeType: String): Boolean =
    "image/jpeg".equals(mimeType, true)
            || "image/png".equals(mimeType, true)
            || "image/webp".equals(mimeType, true)
            || ("image/heic".equals(mimeType, true) && VERSION.SDK_INT >= VERSION_CODES.P)
            || ("image/heif".equals(mimeType, true) && VERSION.SDK_INT >= VERSION_CODES.P)