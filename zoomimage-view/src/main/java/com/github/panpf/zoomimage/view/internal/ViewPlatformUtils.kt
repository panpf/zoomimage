package com.github.panpf.zoomimage.view.internal

import android.graphics.Rect
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView.ScaleType
import androidx.core.view.ViewCompat
import com.github.panpf.zoomimage.core.IntRectCompat
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.ScaleFactorCompat
import com.github.panpf.zoomimage.core.times
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


internal fun requiredMainThread() {
    check(Looper.myLooper() == Looper.getMainLooper()) {
        "This method must be executed in the UI thread"
    }
}

internal fun requiredWorkThread() {
    check(Looper.myLooper() != Looper.getMainLooper()) {
        "This method must be executed in the work thread"
    }
}

internal val View.isAttachedToWindowCompat: Boolean
    get() = ViewCompat.isAttachedToWindow(this)

internal fun getPointerIndex(action: Int): Int {
    return action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
}

val ZeroRect = Rect(0, 0, 0, 0)

internal fun IntSizeCompat.times(scale: Float): IntSizeCompat =
    IntSizeCompat(
        (this.width * scale).roundToInt(),
        (this.height * scale).roundToInt()
    )

internal fun Rect.scale(scale: Float): Rect {
    return Rect(
        left = (left * scale).roundToInt(),
        top = (top * scale).roundToInt(),
        right = (right * scale).roundToInt(),
        bottom = (bottom * scale).roundToInt()
    )
}

fun Rect(left: Int, top: Int, right: Int, bottom: Int): Rect {
    return Rect(left, top, right, bottom)
}

internal fun Rect.toIntRectCompat(): IntRectCompat {
    return IntRectCompat(left, top, right, bottom)
}


internal fun ScaleType.computeScaleFactor(
    srcSize: IntSizeCompat,
    dstSize: IntSizeCompat
): ScaleFactorCompat {
    val widthScale = dstSize.width / srcSize.width.toFloat()
    val heightScale = dstSize.height / srcSize.height.toFloat()
    val fillMaxDimension = max(widthScale, heightScale)
    val fillMinDimension = min(widthScale, heightScale)
    return when (this) {
        ScaleType.CENTER -> ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)

        ScaleType.CENTER_CROP -> {
            ScaleFactorCompat(scaleX = fillMaxDimension, scaleY = fillMaxDimension)
        }

        ScaleType.CENTER_INSIDE -> {
            if (srcSize.width <= dstSize.width && srcSize.height <= dstSize.height) {
                ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)
            } else {
                ScaleFactorCompat(scaleX = fillMinDimension, scaleY = fillMinDimension)
            }
        }

        ScaleType.FIT_START,
        ScaleType.FIT_CENTER,
        ScaleType.FIT_END -> {
            ScaleFactorCompat(scaleX = fillMinDimension, scaleY = fillMinDimension)
        }

        ScaleType.FIT_XY -> {
            ScaleFactorCompat(scaleX = widthScale, scaleY = heightScale)
        }

        ScaleType.MATRIX -> ScaleFactorCompat(1.0f, 1.0f)
        else -> ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)
    }
}

internal fun ScaleType.isStart(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.MATRIX
            || this == ScaleType.FIT_XY
            || (this == ScaleType.FIT_START && scaledSrcSize.width < dstSize.width)
}

internal fun ScaleType.isHorizontalCenter(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.CENTER
            || this == ScaleType.CENTER_CROP
            || this == ScaleType.CENTER_INSIDE
            || this == ScaleType.FIT_CENTER
            || (this == ScaleType.FIT_START && scaledSrcSize.width >= dstSize.width)
            || (this == ScaleType.FIT_END && scaledSrcSize.width >= dstSize.width)
}

internal fun ScaleType.isCenter(
    @Suppress("UNUSED_PARAMETER") srcSize: IntSizeCompat,
    @Suppress("UNUSED_PARAMETER") dstSize: IntSizeCompat
): Boolean =
    this == ScaleType.CENTER
            || this == ScaleType.CENTER_CROP
            || this == ScaleType.CENTER_INSIDE
            || this == ScaleType.FIT_CENTER

internal fun ScaleType.isEnd(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.FIT_END && scaledSrcSize.width < dstSize.width
}

internal fun ScaleType.isTop(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.MATRIX
            || this == ScaleType.FIT_XY
            || (this == ScaleType.FIT_START && scaledSrcSize.height < dstSize.height)
}

internal fun ScaleType.isVerticalCenter(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.CENTER
            || this == ScaleType.CENTER_CROP
            || this == ScaleType.CENTER_INSIDE
            || this == ScaleType.FIT_CENTER
            || (this == ScaleType.FIT_START && scaledSrcSize.height >= dstSize.height)
            || (this == ScaleType.FIT_END && scaledSrcSize.height >= dstSize.height)
}

internal fun ScaleType.isBottom(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.FIT_END && scaledSrcSize.height < dstSize.height
}