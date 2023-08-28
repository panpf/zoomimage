package com.github.panpf.zoomimage.subsampling

import com.github.panpf.zoomimage.subsampling.internal.exifOrientationName
import com.github.panpf.zoomimage.util.IntSizeCompat

/**
 * Image information
 */
data class ImageInfo constructor(
    /**
     * Image size
     */
    val size: IntSizeCompat,

    /**
     * Image mime type
     */
    val mimeType: String,

    /**
     * Image exif orientation
     */
    val exifOrientation: Int,
) {

    /**
     * Create a new [ImageInfo] based on the specified parameters
     *
     * @param width Image width
     * @param height Image height
     * @param mimeType Image mime type
     * @param exifOrientation Image exif orientation
     */
    constructor(
        width: Int,
        height: Int,
        mimeType: String,
        exifOrientation: Int,
    ) : this(IntSizeCompat(width, height), mimeType, exifOrientation)

    /**
     * Image width
     */
    val width: Int = size.width

    /**
     * Image height
     */
    val height: Int = size.height

    /**
     * Create a new [ImageInfo] based on the current [ImageInfo]
     */
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