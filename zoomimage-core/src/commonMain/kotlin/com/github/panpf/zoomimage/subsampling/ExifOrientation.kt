package com.github.panpf.zoomimage.subsampling

import com.github.panpf.zoomimage.subsampling.internal.TileBitmapReuseHelper
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat

interface ExifOrientation {

    fun name(): String

    fun applyToSize(size: IntSizeCompat, reverse: Boolean = false): IntSizeCompat

    fun applyToRect(
        srcRect: IntRectCompat,
        imageSize: IntSizeCompat,
        reverse: Boolean = false
    ): IntRectCompat

    fun applyToTileBitmap(
        tileBitmap: TileBitmap,
        reverse: Boolean = false,
        bitmapReuseHelper: TileBitmapReuseHelper? = null
    ): TileBitmap

    companion object {
        // Constants used for the Orientation Exif tag.
        const val ORIENTATION_UNDEFINED = 0
        const val ORIENTATION_NORMAL = 1

        /**
         * Indicates the image is left right reversed mirror.
         */
        const val ORIENTATION_FLIP_HORIZONTAL = 2

        /**
         * Indicates the image is rotated by 180 degree clockwise.
         */
        const val ORIENTATION_ROTATE_180 = 3

        /**
         * Indicates the image is upside down mirror, it can also be represented by flip
         * horizontally firstly and rotate 180 degree clockwise.
         */
        const val ORIENTATION_FLIP_VERTICAL = 4

        /**
         * Indicates the image is flipped about top-left <--> bottom-right axis, it can also be
         * represented by flip horizontally firstly and rotate 270 degree clockwise.
         */
        const val ORIENTATION_TRANSPOSE = 5

        /**
         * Indicates the image is rotated by 90 degree clockwise.
         */
        const val ORIENTATION_ROTATE_90 = 6

        /**
         * Indicates the image is flipped about top-right <--> bottom-left axis, it can also be
         * represented by flip horizontally firstly and rotate 90 degree clockwise.
         */
        const val ORIENTATION_TRANSVERSE = 7

        /**
         * Indicates the image is rotated by 270 degree clockwise.
         */
        const val ORIENTATION_ROTATE_270 = 8
    }
}

fun ExifOrientation.applyToImageInfo(imageInfo: ImageInfo): ImageInfo {
    val newSize = applyToSize(imageInfo.size)
    return imageInfo.copy(size = newSize)
}

data object EmptyExifOrientation : ExifOrientation {

    override fun applyToSize(size: IntSizeCompat, reverse: Boolean): IntSizeCompat = size

    override fun applyToRect(
        srcRect: IntRectCompat,
        imageSize: IntSizeCompat,
        reverse: Boolean
    ): IntRectCompat = srcRect

    override fun applyToTileBitmap(
        tileBitmap: TileBitmap,
        reverse: Boolean,
        bitmapReuseHelper: TileBitmapReuseHelper?,
    ): TileBitmap = tileBitmap

    override fun name(): String = "UNDEFINED"

    override fun toString(): String = "EmptyExifOrientation"
}