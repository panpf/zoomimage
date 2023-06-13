package com.github.panpf.zoomimage.internal

import android.graphics.Rect
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat


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

internal val View.isAttachedToWindowCompat: Boolean
    get() = ViewCompat.isAttachedToWindow(this)

internal fun getPointerIndex(action: Int): Int {
    return action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
}

internal fun Rect.toVeryShortString(): String =
    "(${left},${top}-${right},${bottom})"