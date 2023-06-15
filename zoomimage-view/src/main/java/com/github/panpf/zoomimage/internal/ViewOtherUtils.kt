package com.github.panpf.zoomimage.internal

import java.math.BigDecimal
import java.math.RoundingMode

internal fun Float.format(newScale: Int): Float {
    return if (this.isNaN()) {
        this
    } else {
        BigDecimal(toDouble()).setScale(newScale, RoundingMode.HALF_UP).toFloat()
    }
}

internal fun Any.toHexString(): String = Integer.toHexString(this.hashCode())