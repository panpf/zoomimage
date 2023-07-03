package com.github.panpf.zoomimage.sample.util

import android.graphics.PointF
import android.graphics.Rect
import com.github.panpf.zoomimage.core.SizeCompat
import java.math.BigDecimal
import java.math.RoundingMode


internal fun SizeCompat.toShortString(): String = "(${width}x$height)"

internal fun PointF.toShortString(): String = "(${x}x$y)"

internal fun SizeCompat.toVeryShortString(): String = "${width}x$height"

internal fun android.graphics.Rect.toVeryShortString(): String =
    "(${left},${top}-${right},${bottom})"

internal fun android.graphics.RectF.toVeryShortString(): String =
    "(${left.format(2)},${top.format(2)}-${right.format(2)},${bottom.format(2)})"

internal fun Rect.crossWith(other: Rect): Boolean {
    return this.left < other.right
            && this.right > other.left
            && this.top < other.bottom
            && this.bottom > other.top
}

internal fun Float.format(newScale: Int): Float {
    return if (this.isNaN()) {
        this
    } else {
        BigDecimal(toDouble()).setScale(newScale, RoundingMode.HALF_UP).toFloat()
    }
}

internal fun String.formatLength(targetLength: Int, padChar: Char = ' '): String {
    return if (this.length >= targetLength) {
        this.substring(0, targetLength)
    } else {
        this.padEnd(targetLength, padChar)
    }
}