package com.github.panpf.zoomimage.internal

import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.widget.ImageView

interface ImageViewBridge {

    /**
     * Get Drawable
     */
    fun getDrawable(): Drawable?

    /**
     * Call the parent class's setScaleType() method
     */
    fun superSetScaleType(scaleType: ImageView.ScaleType)

    /**
     * Call the parent class's getScaleType() method
     */
    fun superGetScaleType(): ImageView.ScaleType

    /**
     * Call the parent class's setImageMatrix() method
     */
    fun superSetImageMatrix(matrix: Matrix?)
}