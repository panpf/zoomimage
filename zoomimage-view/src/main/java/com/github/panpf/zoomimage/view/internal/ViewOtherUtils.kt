package com.github.panpf.zoomimage.view.internal

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal fun Float.format(newScale: Int): Float {
    return if (this.isNaN()) {
        this
    } else {
        BigDecimal(toDouble()).setScale(newScale, RoundingMode.HALF_UP).toFloat()
    }
}

internal fun Any.toHexString(): String = Integer.toHexString(this.hashCode())

internal fun Float.isSafe(): Boolean {
    return !isNaN() && !isInfinite()
}

/**
 * Linearly interpolate between [start] and [stop] with [fraction] fraction between them.
 */
internal fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}

/**
 * Linearly interpolate between [start] and [stop] with [fraction] fraction between them.
 */
internal fun lerp(start: Int, stop: Int, fraction: Float): Int {
    return start + ((stop - start) * fraction.toDouble()).roundToInt()
}

/**
 * Linearly interpolate between [start] and [stop] with [fraction] fraction between them.
 */
internal fun lerp(start: Long, stop: Long, fraction: Float): Long {
    return start + ((stop - start) * fraction.toDouble()).roundToLong()
}