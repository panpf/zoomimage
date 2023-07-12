package com.github.panpf.zoomimage.view.internal

import android.graphics.Rect
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import com.github.panpf.zoomimage.core.IntSizeCompat
import kotlin.math.roundToInt


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

internal fun Rect.crossWith(other: Rect): Boolean {
    return this.left < other.right
            && this.right > other.left
            && this.top < other.bottom
            && this.bottom > other.top
}

val ZeroRect = Rect(0, 0, 0, 0)

internal fun IntSizeCompat.times(scale: Float): IntSizeCompat =
    IntSizeCompat(
        (this.width * scale).roundToInt(),
        (this.height * scale).roundToInt()
    )

internal fun Rect.scale(scale: Float): Rect {
    return Rect(
        left = (left * scale).roundToInt(),
        top = (top * scale).roundToInt(),
        right = (right * scale).roundToInt(),
        bottom = (bottom * scale).roundToInt()
    )
}

fun Rect(left: Int, top: Int, right: Int, bottom: Int): Rect {
    return Rect(left, top, right, bottom)
}