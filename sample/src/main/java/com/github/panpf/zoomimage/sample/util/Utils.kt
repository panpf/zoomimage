package com.github.panpf.zoomimage.sample.util

import java.math.BigDecimal
import java.math.RoundingMode

internal fun android.graphics.PointF.toShortString(): String =
    "${x.format(2)}x${y.format(2)}"

internal fun android.graphics.Rect.toVeryShortString(): String =
    "${left}x${top},${right}x${bottom}"

internal fun android.graphics.RectF.toVeryShortString(): String =
    "${left.format(2)}x${top.format(2)},${right.format(2)}x${bottom.format(2)}"

internal fun android.graphics.Rect.crossWith(other: android.graphics.Rect): Boolean {
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