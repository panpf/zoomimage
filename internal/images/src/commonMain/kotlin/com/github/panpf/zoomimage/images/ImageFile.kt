package com.github.panpf.zoomimage.images

import com.github.panpf.zoomimage.util.IntSizeCompat

interface ImageFile {
    val name: String
    val uri: String
    val size: IntSizeCompat
    val exifOrientation: Int
}