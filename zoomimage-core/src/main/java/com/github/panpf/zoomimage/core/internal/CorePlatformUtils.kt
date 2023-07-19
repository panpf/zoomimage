package com.github.panpf.zoomimage.core.internal

import android.graphics.Bitmap
import android.os.Looper


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

internal fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun Bitmap.toShortString(): String = "(${width}x${height},$config)"