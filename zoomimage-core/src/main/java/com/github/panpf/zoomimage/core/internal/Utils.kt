package com.github.panpf.zoomimage.core.internal

import android.os.Looper
import java.math.BigDecimal


internal fun requiredWorkThread() {
    check(Looper.myLooper() != Looper.getMainLooper()) {
        "This method must be executed in the work thread"
    }
}

internal fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()

internal fun Any.toHexString(): String = Integer.toHexString(this.hashCode())

internal fun Float.format(newScale: Int): Float =
    BigDecimal(toDouble()).setScale(newScale, BigDecimal.ROUND_HALF_UP).toFloat()