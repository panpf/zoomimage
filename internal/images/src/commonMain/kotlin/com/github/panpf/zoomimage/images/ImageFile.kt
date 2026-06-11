package com.github.panpf.zoomimage.images

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat

interface ImageFile {
    val name: String
    val uri: String
    val size: IntSizeCompat
    val length: Long
    val mimeType: String
    val animated: Boolean
    val exifOrientation: Int
    val imageInfo: ImageInfo
}