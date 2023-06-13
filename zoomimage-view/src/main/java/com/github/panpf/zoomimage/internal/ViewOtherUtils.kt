package com.github.panpf.zoomimage.internal

import java.math.BigDecimal

internal fun Float.format(newScale: Int): Float =
    BigDecimal(toDouble()).setScale(newScale, BigDecimal.ROUND_HALF_UP).toFloat()

internal fun Any.toHexString(): String = Integer.toHexString(this.hashCode())