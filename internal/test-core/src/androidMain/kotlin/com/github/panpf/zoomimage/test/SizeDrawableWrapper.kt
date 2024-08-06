package com.github.panpf.zoomimage.test

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.util.Size
import java.lang.Deprecated

class SizeDrawableWrapper(
    val drawable: Drawable,
    val size: Size
) : Drawable() {

    override fun getIntrinsicWidth(): Int {
        return size.width
    }

    override fun getIntrinsicHeight(): Int {
        return size.height
    }

    override fun draw(canvas: Canvas) {
        drawable.draw(canvas)
    }

    override fun setAlpha(alpha: Int) {
        drawable.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        drawable.colorFilter = colorFilter
    }

    @Deprecated
    @Suppress("DEPRECATED_JAVA_ANNOTATION")
    override fun getOpacity(): Int {
        @Suppress("DEPRECATION")
        return drawable.opacity
    }
}