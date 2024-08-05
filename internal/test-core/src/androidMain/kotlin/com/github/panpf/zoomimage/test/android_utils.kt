package com.github.panpf.zoomimage.test

import android.graphics.Bitmap
import android.os.Build
import android.widget.ImageView
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.min


internal fun ImageView.ScaleType.computeScaleFactor(
    srcSize: IntSizeCompat,
    dstSize: IntSizeCompat
): ScaleFactorCompat {
    val widthScale = dstSize.width / srcSize.width.toFloat()
    val heightScale = dstSize.height / srcSize.height.toFloat()
    val fillMaxDimension = max(widthScale, heightScale)
    val fillMinDimension = min(widthScale, heightScale)
    return when (this) {
        ImageView.ScaleType.CENTER -> ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)

        ImageView.ScaleType.CENTER_CROP -> {
            ScaleFactorCompat(scaleX = fillMaxDimension, scaleY = fillMaxDimension)
        }

        ImageView.ScaleType.CENTER_INSIDE -> {
            if (srcSize.width <= dstSize.width && srcSize.height <= dstSize.height) {
                ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)
            } else {
                ScaleFactorCompat(scaleX = fillMinDimension, scaleY = fillMinDimension)
            }
        }

        ImageView.ScaleType.FIT_START,
        ImageView.ScaleType.FIT_CENTER,
        ImageView.ScaleType.FIT_END -> {
            ScaleFactorCompat(scaleX = fillMinDimension, scaleY = fillMinDimension)
        }

        ImageView.ScaleType.FIT_XY -> {
            ScaleFactorCompat(scaleX = widthScale, scaleY = heightScale)
        }

        ImageView.ScaleType.MATRIX -> ScaleFactorCompat(1.0f, 1.0f)
        else -> ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)
    }
}


internal val Bitmap.allocationByteCountCompat: Int
    get() = when {
        this.isRecycled -> 0
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> this.allocationByteCount
        else -> this.byteCount
    }



/**
 * The number of bytes required for calculation based on width, height, and configuration
 */
internal fun calculateBitmapByteCount(width: Int, height: Int, config: Bitmap.Config?): Int {
    return width * height * config.getBytesPerPixel()
}


/**
 * Gets the number of bytes occupied by a single pixel in a specified configuration
 */
internal fun Bitmap.Config?.getBytesPerPixel(): Int {
    // A bitmap by decoding a gif has null "config" in certain environments.
    val config = this ?: Bitmap.Config.ARGB_8888
    @Suppress("DEPRECATION")
    return when {
        config == Bitmap.Config.ALPHA_8 -> 1
        config == Bitmap.Config.RGB_565 || config == Bitmap.Config.ARGB_4444 -> 2
        config == Bitmap.Config.ARGB_8888 -> 4
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && config == Bitmap.Config.RGBA_F16 -> 8
        else -> 4
    }
}

val Bitmap.logString: String
    get() = "Bitmap(${width}x${height},$config,@${Integer.toHexString(this.hashCode())})"

/**
 * Returns the formatted file length that can be displayed, up to EB
 *
 * @receiver              File size
 * @param decimalPlacesLength   Keep a few decimal places
 * @param decimalPlacesFillZero Use 0 instead when the number of decimal places is insufficient
 * @param compact               If true, returns 150KB, otherwise returns 150 KB
 * @return For example: 300 B, 150.25 KB, 500.46 MB, 300 GB
 */
internal fun Long.formatFileSize(
    decimalPlacesLength: Int = 2,
    decimalPlacesFillZero: Boolean = false,
    compact: Boolean = true,
): String {
    // Multiplied by 999 to avoid 1000 KB, 1000 MB
    // Why is appendSuffix required to be true when calling the format method, because DecimalFormat encounters '#.##EB' and throws an exception 'IllegalArgumentException: Malformed exponential pattern "#.##EB"'
    @Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
    val finalFileSize = Math.max(this, 0)
    return if (finalFileSize <= 999) {
        finalFileSize.toString() + if (compact) "B" else " B"
    } else {
        val value: Double
        val suffix: String
        if (finalFileSize <= 1024L * 999) {
            value = (finalFileSize / 1024f).toDouble()
            suffix = if (compact) "KB" else " KB"
        } else if (finalFileSize <= 1024L * 1024 * 999) {
            value = (finalFileSize / 1024f / 1024f).toDouble()
            suffix = if (compact) "MB" else " MB"
        } else if (finalFileSize <= 1024L * 1024 * 1024 * 999) {
            value = (finalFileSize / 1024f / 1024f / 1024f).toDouble()
            suffix = if (compact) "GB" else " GB"
        } else if (finalFileSize <= 1024L * 1024 * 1024 * 1024 * 999) {
            value = (finalFileSize / 1024f / 1024f / 1024f / 1024f).toDouble()
            suffix = if (compact) "TB" else " TB"
        } else if (finalFileSize <= 1024L * 1024 * 1024 * 1024 * 1024 * 999) {
            value = (finalFileSize / 1024f / 1024f / 1024f / 1024f / 1024f).toDouble()
            suffix = if (compact) "PB" else " PB"
        } else {
            value = (finalFileSize / 1024f / 1024f / 1024f / 1024f / 1024f / 1024f).toDouble()
            suffix = if (compact) "EB" else " EB"
        }
        val buffString = StringBuilder()
        buffString.append("#")
        if (decimalPlacesLength > 0) {
            buffString.append(".")
            for (w in 0 until decimalPlacesLength) {
                buffString.append(if (decimalPlacesFillZero) "0" else "#")
            }
        }
        val format = DecimalFormat(buffString.toString())
        format.roundingMode = RoundingMode.HALF_UP
        format.format(value).toString() + suffix
    }
}

@Suppress("USELESS_ELVIS")
internal val Bitmap.configOrNull: Bitmap.Config?
    get() = config ?: null

/**
 * Convert the object to a hexadecimal string
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.CoreUtilsTest.testToHexString
 */
fun Any.toHexString(): String = this.hashCode().toString(16)

/**
 * Get the log string description of Bitmap, it additionally contains the hexadecimal string representation of the Bitmap memory address.
 *
 * @see com.github.panpf.zoomimage.core.android.test.util.CoreUtilsAndroidTest.testToLogString
 */
fun Bitmap.toLogString(): String = "Bitmap@${toHexString()}(${width}x${height},$config)"