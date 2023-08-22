package com.github.panpf.zoomimage.util.internal

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

internal fun Bitmap.toShortString(): String = "(${width}x${height},$config)"

internal fun Bitmap.toHexString(): String =
    "Bitmap(${width}x${height},$config,@${(this as Any).toHexString()})"

internal val Bitmap.safeConfig: Bitmap.Config
    get() = config ?: Bitmap.Config.ARGB_8888