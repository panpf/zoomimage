package com.github.panpf.zoomimage.test

import com.github.panpf.sketch.util.Size
import com.github.panpf.zoomimage.util.IntSizeCompat

fun Size.toIntSizeCompat(): IntSizeCompat {
    return IntSizeCompat(width, height)
}