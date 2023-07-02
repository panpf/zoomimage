package com.github.panpf.zoomimage.sample.util

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.core.net.toUri
import com.github.panpf.zoomimage.Size
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode


internal fun Size.toShortString(): String = "(${width}x$height)"

internal fun PointF.toShortString(): String = "(${x}x$y)"

internal fun Size.toVeryShortString(): String = "${width}x$height"

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