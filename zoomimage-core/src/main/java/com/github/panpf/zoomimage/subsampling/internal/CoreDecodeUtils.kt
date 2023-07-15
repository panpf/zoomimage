package com.github.panpf.zoomimage.subsampling.internal

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.internal.toHexString
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor

internal fun Bitmap.Config.isAndSupportHardware(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this == Bitmap.Config.HARDWARE

/**
 * If true, indicates that the given mimeType and sampleSize combination can be using 'inBitmap' in BitmapFactory
 *
 * Test results based on the BitmapFactoryTest.testInBitmapAndInSampleSize() method
 */
@SuppressLint("ObsoleteSdkInt")
internal fun isSupportInBitmap(mimeType: String?, sampleSize: Int): Boolean =
    when {
        "image/jpeg".equals(mimeType, true) ->
            if (sampleSize == 1) Build.VERSION.SDK_INT >= 16 else Build.VERSION.SDK_INT >= 19

        "image/png".equals(mimeType, true) ->
            if (sampleSize == 1) Build.VERSION.SDK_INT >= 16 else Build.VERSION.SDK_INT >= 19

        "image/gif".equals(mimeType, true) ->
            if (sampleSize == 1) Build.VERSION.SDK_INT >= 19 else Build.VERSION.SDK_INT >= 21

        "image/webp".equals(mimeType, true) -> Build.VERSION.SDK_INT >= 19
//        "image/webp".equals(mimeType, true) -> VERSION.SDK_INT >= 26 animated
        "image/bmp".equals(mimeType, true) -> Build.VERSION.SDK_INT >= 19
        "image/heic".equals(mimeType, true) -> false
        "image/heif".equals(mimeType, true) -> Build.VERSION.SDK_INT >= 28
        else -> Build.VERSION.SDK_INT >= 32   // Compatible with new image types supported in the future
    }

/**
 * If true, indicates that the given mimeType can be using 'inBitmap' in BitmapRegionDecoder
 *
 * Test results based on the BitmapRegionDecoderTest.testInBitmapAndInSampleSize() method
 */
@SuppressLint("ObsoleteSdkInt")
internal fun isSupportInBitmapForRegion(mimeType: String?): Boolean =
    when {
        "image/jpeg".equals(mimeType, true) -> Build.VERSION.SDK_INT >= 16
        "image/png".equals(mimeType, true) -> Build.VERSION.SDK_INT >= 16
        "image/gif".equals(mimeType, true) -> false
        "image/webp".equals(mimeType, true) -> Build.VERSION.SDK_INT >= 16
//        "image/webp".equals(mimeType, true) -> VERSION.SDK_INT >= 26 animated
        "image/bmp".equals(mimeType, true) -> false
        "image/heic".equals(mimeType, true) -> Build.VERSION.SDK_INT >= 28
        "image/heif".equals(mimeType, true) -> Build.VERSION.SDK_INT >= 28
        else -> Build.VERSION.SDK_INT >= 32   // Compatible with new image types supported in the future
    }

/**
 * Calculate the size of the sampled Bitmap, support for BitmapFactory or ImageDecoder
 */
internal fun calculateSampledBitmapSize(
    imageSize: IntSizeCompat, sampleSize: Int, mimeType: String? = null
): IntSizeCompat {
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
    return IntSizeCompat(width, height)
}


/**
 * Calculate the size of the sampled Bitmap, support for BitmapRegionDecoder
 */
internal fun calculateSampledBitmapSizeForRegion(
    regionSize: IntSizeCompat,
    sampleSize: Int,
    mimeType: String? = null,
    imageSize: IntSizeCompat? = null
): IntSizeCompat {
    val widthValue = regionSize.width / sampleSize.toDouble()
    val heightValue = regionSize.height / sampleSize.toDouble()
    val width: Int
    val height: Int
    val isPNGFormat = "image/png".equals(mimeType, true)
    if (!isPNGFormat && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && regionSize == imageSize) {
        width = ceil(widthValue).toInt()
        height = ceil(heightValue).toInt()
    } else {
        width = floor(widthValue).toInt()
        height = floor(heightValue).toInt()
    }
    return IntSizeCompat(width, height)
}

// todo chang to toShortString
internal val Bitmap.logString: String
    get() = "Bitmap(${width}x${height},$config,@${toHexString()})"

internal val Bitmap.safeConfig: Bitmap.Config
    get() = config ?: Bitmap.Config.ARGB_8888

suspend fun ImageSource.readImageBounds(): Result<BitmapFactory.Options?> {
    return withContext(Dispatchers.IO) {
        openInputStream()
            .let { it.getOrNull() ?: return@withContext Result.failure(it.exceptionOrNull()!!) }
            .use { inputStream ->
                kotlin.runCatching {
                    BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                        BitmapFactory.decodeStream(inputStream, null, this)
                    }.takeIf { it.outWidth > 0 && it.outHeight > 0 }
                }
            }
    }
}

suspend fun ImageSource.readExifOrientation(): Result<Int> {
    val orientationUndefined = ExifInterface.ORIENTATION_UNDEFINED
    return withContext(Dispatchers.IO) {
        openInputStream()
            .let { it.getOrNull() ?: return@withContext Result.failure(it.exceptionOrNull()!!) }
            .use { inputStream ->
                kotlin.runCatching {
                    ExifInterface(inputStream)
                        .getAttributeInt(ExifInterface.TAG_ORIENTATION, orientationUndefined)
                }
            }
    }
}

suspend fun ImageSource.readImageInfo(): ImageInfo? {
    val options = readImageBounds().getOrNull() ?: return null
    val exifOrientation = readExifOrientation().getOrNull() ?: return null
    return ImageInfo(
        size = IntSizeCompat(options.outWidth, options.outHeight),
        mimeType = options.outMimeType,
        exifOrientation = exifOrientation,
    )
}