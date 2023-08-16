package com.github.panpf.zoomimage.util.internal

import android.graphics.Bitmap
import android.os.Looper


// todo Unit tests
internal fun requiredMainThread() {
    check(Looper.myLooper() == Looper.getMainLooper()) {
        "This method must be executed in the UI thread"
    }
}

// todo Unit tests
internal fun requiredWorkThread() {
    check(Looper.myLooper() != Looper.getMainLooper()) {
        "This method must be executed in the work thread"
    }
}

// todo Unit tests
internal fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()

// todo Unit tests
internal fun Bitmap.toShortString(): String = "(${width}x${height},$config)"

// todo Unit tests
internal fun Bitmap.toHexString(): String = "Bitmap(${width}x${height},$config,@${(this as Any).toHexString()})"

// todo Unit tests
internal val Bitmap.safeConfig: Bitmap.Config
    get() = config ?: Bitmap.Config.ARGB_8888