package com.github.panpf.zoomimage.internal

import android.os.Looper
import java.math.BigDecimal
import java.math.RoundingMode


internal fun requiredWorkThread() {
    check(Looper.myLooper() != Looper.getMainLooper()) {
        "This method must be executed in the work thread"
    }
}

internal fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()

internal fun Any.toHexString(): String = Integer.toHexString(this.hashCode())

internal fun Float.format(newScale: Int): Float {
    return if (this.isNaN()) {
        this
    } else {
        BigDecimal(toDouble()).setScale(newScale, RoundingMode.HALF_UP).toFloat()
    }
}