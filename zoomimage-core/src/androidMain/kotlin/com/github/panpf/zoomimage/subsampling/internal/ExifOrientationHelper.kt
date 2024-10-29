/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.subsampling.internal

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.flip
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.rotateInSpace
import com.github.panpf.zoomimage.util.safeConfig
import kotlin.math.abs

fun ExifOrientationHelper.applyToImageInfo(imageInfo: ImageInfo): ImageInfo {
    val newSize = applyToSize(imageInfo.size)
    return imageInfo.copy(size = newSize)
}

/**
 * Helper class to handle image orientation based on Exif tags.
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.internal.ExifOrientationHelperTest
 */
class ExifOrientationHelper(val exifOrientation: Int) {

    /**
     * Returns if the current image orientation is flipped.
     *
     * @see rotationDegrees
     */
    val isFlipped: Boolean =
        when (exifOrientation) {
            ORIENTATION_FLIP_HORIZONTAL,
            ORIENTATION_TRANSVERSE,
            ORIENTATION_FLIP_VERTICAL,
            ORIENTATION_TRANSPOSE -> true

            else -> false
        }

    /**
     * Returns the rotation degrees for the current image orientation. If the image is flipped,
     * i.e., [.isFlipped] returns `true`, the rotation degrees will be base on
     * the assumption that the image is first flipped horizontally (along Y-axis), and then do
     * the rotation. For example, [.ORIENTATION_TRANSPOSE] will be interpreted as flipped
     * horizontally first, and then rotate 270 degrees clockwise.
     *
     * @return The rotation degrees of the image after the horizontal flipping is applied, if any.
     *
     * @see isFlipped
     */
    val rotationDegrees: Int =
        when (exifOrientation) {
            ORIENTATION_ROTATE_90,
            ORIENTATION_TRANSVERSE -> 90

            ORIENTATION_ROTATE_180,
            ORIENTATION_FLIP_VERTICAL -> 180

            ORIENTATION_ROTATE_270,
            ORIENTATION_TRANSPOSE -> 270

            ORIENTATION_UNDEFINED,
            ORIENTATION_NORMAL,
            ORIENTATION_FLIP_HORIZONTAL -> 0

            else -> 0
        }

    val translation: Int =
        when (exifOrientation) {
            ORIENTATION_FLIP_HORIZONTAL,
            ORIENTATION_FLIP_VERTICAL,
            ORIENTATION_TRANSPOSE,
            ORIENTATION_TRANSVERSE -> -1

            else -> 1
        }

    fun applyToSize(size: IntSizeCompat, reverse: Boolean = false): IntSizeCompat {
        return size.rotate(if (!reverse) rotationDegrees else -rotationDegrees)
    }

    fun applyToRect(
        srcRect: IntRectCompat,
        imageSize: IntSizeCompat,
        reverse: Boolean = false
    ): IntRectCompat {
        val isRotated = abs(rotationDegrees % 360) != 0
        return if (!reverse) {
            srcRect
                .let { if (isFlipped) it.flip(imageSize, vertical = false) else it }
                .let { if (isRotated) it.rotateInSpace(imageSize, rotationDegrees) else it }
        } else {
            val rotatedImageSize = imageSize.rotate(-rotationDegrees)
            srcRect
                .let { if (isRotated) it.rotateInSpace(imageSize, -rotationDegrees) else it }
                .let { if (isFlipped) it.flip(rotatedImageSize, vertical = false) else it }
        }
    }

    fun applyToTileImage(
        tileImage: BitmapTileImage,
        reverse: Boolean = false,
    ): BitmapTileImage {
        val isRotated = abs(rotationDegrees % 360) != 0
        if (!isFlipped && !isRotated) {
            return tileImage
        }

        val bitmap = tileImage.bitmap

        val matrix = Matrix().apply {
            if (!reverse) {
                if (isFlipped) {
                    this.postScale(-1f, 1f)
                }
                if (isRotated) {
                    this.postRotate(rotationDegrees.toFloat())
                }
            } else {
                if (isRotated) {
                    this.postRotate(-rotationDegrees.toFloat())
                }
                if (isFlipped) {
                    this.postScale(-1f, 1f)
                }
            }
        }
        val newRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        matrix.mapRect(newRect)
        matrix.postTranslate(-newRect.left, -newRect.top)

        val config = bitmap.safeConfig
        val newWidth = newRect.width().toInt()
        val newHeight = newRect.height().toInt()
        val outBitmap = Bitmap.createBitmap(newWidth, newHeight, config)

        val canvas = Canvas(outBitmap)
        val paint = Paint(Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmap, matrix, paint)
        return BitmapTileImage(outBitmap, tileImage.key, tileImage.fromCache)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ExifOrientationHelper
        return exifOrientation == other.exifOrientation
    }

    override fun hashCode(): Int {
        return exifOrientation
    }

    override fun toString(): String {
        return "ExifOrientationHelper(${name(exifOrientation)})"
    }

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

        fun name(exifOrientation: Int): String = when (exifOrientation) {
            ORIENTATION_ROTATE_90 -> "ROTATE_90"
            ORIENTATION_TRANSPOSE -> "TRANSPOSE"
            ORIENTATION_ROTATE_180 -> "ROTATE_180"
            ORIENTATION_FLIP_VERTICAL -> "FLIP_VERTICAL"
            ORIENTATION_ROTATE_270 -> "ROTATE_270"
            ORIENTATION_TRANSVERSE -> "TRANSVERSE"
            ORIENTATION_FLIP_HORIZONTAL -> "FLIP_HORIZONTAL"
            ORIENTATION_UNDEFINED -> "UNDEFINED"
            ORIENTATION_NORMAL -> "NORMAL"
            else -> exifOrientation.toString()
        }
    }
}