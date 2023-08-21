package com.github.panpf.zoomimage.view.subsampling

import com.github.panpf.zoomimage.util.IntRectCompat

fun interface OnImageLoadRectChangeListener {
    fun onImageLoadRectChanged(imageLoadRect: IntRectCompat)
}