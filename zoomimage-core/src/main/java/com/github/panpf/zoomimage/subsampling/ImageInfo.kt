package com.github.panpf.zoomimage.subsampling

import com.github.panpf.zoomimage.subsampling.internal.exifOrientationName
import com.github.panpf.zoomimage.util.IntSizeCompat

data class ImageInfo constructor(
    val size: IntSizeCompat,
    val mimeType: String,
    val exifOrientation: Int,
) {

    constructor(
        width: Int,
        height: Int,
        mimeType: String,
        exifOrientation: Int,
    ) : this(IntSizeCompat(width, height), mimeType, exifOrientation)

    val width: Int = size.width
    val height: Int = size.height

    fun newImageInfo(
        width: Int = this.width,
        height: Int = this.height,
        mimeType: String = this.mimeType,
        exifOrientation: Int = this.exifOrientation,
    ): ImageInfo = ImageInfo(width, height, mimeType, exifOrientation)

    override fun toString(): String {
        val exifOrientationName = exifOrientationName(exifOrientation)
        return "ImageInfo(width=$width, height=$height, mimeType='$mimeType', exifOrientation=$exifOrientationName)"
    }

    fun toShortString(): String =
        "(${width}x$height,'$mimeType',${exifOrientationName(exifOrientation)})"
}