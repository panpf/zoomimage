package com.github.panpf.zoomimage.util

import com.github.panpf.zoomimage.SkiaRect


internal fun IntRectCompat.toSkiaRect(): SkiaRect = SkiaRect(
    left = left.toFloat(),
    top = top.toFloat(),
    right = right.toFloat(),
    bottom = bottom.toFloat(),
)