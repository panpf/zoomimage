/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage.subsampling

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.subsampling.internal.safeConfig
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.flip
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.rotateInSpace
import kotlin.math.abs

class AndroidExifOrientation constructor(val exifOrientation: Int) {

    /**
     * Returns if the current image orientation is flipped.
     *
     * @see rotationDegrees
     */
    val isFlipped: Boolean =
        when (exifOrientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            ExifInterface.ORIENTATION_TRANSVERSE,
            ExifInterface.ORIENTATION_FLIP_VERTICAL,
            ExifInterface.ORIENTATION_TRANSPOSE -> true

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
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_TRANSVERSE -> 90

            ExifInterface.ORIENTATION_ROTATE_180,
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180

            ExifInterface.ORIENTATION_ROTATE_270,
            ExifInterface.ORIENTATION_TRANSPOSE -> 270

            ExifInterface.ORIENTATION_UNDEFINED,
            ExifInterface.ORIENTATION_NORMAL,
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> 0

            else -> 0
        }

    val translation: Int =
        when (exifOrientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            ExifInterface.ORIENTATION_FLIP_VERTICAL,
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_TRANSVERSE -> -1

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

    fun applyToTileBitmap(
        tileBitmap: TileBitmap,
        reverse: Boolean = false,
    ): TileBitmap {
        val isRotated = abs(rotationDegrees % 360) != 0
        if (!isFlipped && !isRotated) {
            return tileBitmap
        }

        val bitmap = (tileBitmap as AndroidTileBitmap).bitmap!!

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
        return AndroidTileBitmap(outBitmap, tileBitmap.bitmapFrom)
    }

    fun name(): String {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> "ROTATE_90"
            ExifInterface.ORIENTATION_TRANSPOSE -> "TRANSPOSE"
            ExifInterface.ORIENTATION_ROTATE_180 -> "ROTATE_180"
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> "FLIP_VERTICAL"
            ExifInterface.ORIENTATION_ROTATE_270 -> "ROTATE_270"
            ExifInterface.ORIENTATION_TRANSVERSE -> "TRANSVERSE"
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> "FLIP_HORIZONTAL"
            ExifInterface.ORIENTATION_UNDEFINED -> "UNDEFINED"
            ExifInterface.ORIENTATION_NORMAL -> "NORMAL"
            else -> exifOrientation.toString()
        }
    }

    override fun toString(): String {
        return "AndroidExifOrientation(${name()})"
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
    }
}

fun AndroidExifOrientation.applyToImageInfo(imageInfo: ImageInfo): ImageInfo {
    val newSize = applyToSize(imageInfo.size)
    return imageInfo.copy(size = newSize)
}