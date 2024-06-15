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

import com.github.panpf.zoomimage.subsampling.internal.TileBitmapReuseHelper
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.flip
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.rotateInSpace
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Image
import kotlin.math.abs

/**
 * @see [com.github.panpf.zoomimage.core.test.subsampling.DesktopExifOrientationTest]
 */
class DesktopExifOrientation(val exifOrientation: Int) : ExifOrientation {

    /**
     * Returns if the current image orientation is flipped.
     *
     * @see rotationDegrees
     */
    val isFlipped: Boolean =
        when (exifOrientation) {
            ExifOrientation.ORIENTATION_FLIP_HORIZONTAL,
            ExifOrientation.ORIENTATION_TRANSVERSE,
            ExifOrientation.ORIENTATION_FLIP_VERTICAL,
            ExifOrientation.ORIENTATION_TRANSPOSE -> true

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
            ExifOrientation.ORIENTATION_ROTATE_90,
            ExifOrientation.ORIENTATION_TRANSVERSE -> 90

            ExifOrientation.ORIENTATION_ROTATE_180,
            ExifOrientation.ORIENTATION_FLIP_VERTICAL -> 180

            ExifOrientation.ORIENTATION_ROTATE_270,
            ExifOrientation.ORIENTATION_TRANSPOSE -> 270

            ExifOrientation.ORIENTATION_UNDEFINED,
            ExifOrientation.ORIENTATION_NORMAL,
            ExifOrientation.ORIENTATION_FLIP_HORIZONTAL -> 0

            else -> 0
        }

//    @Suppress("unused")
//    val translation: Int =
//        when (exifOrientation) {
//            ExifOrientation.ORIENTATION_FLIP_HORIZONTAL,
//            ExifOrientation.ORIENTATION_FLIP_VERTICAL,
//            ExifOrientation.ORIENTATION_TRANSPOSE,
//            ExifOrientation.ORIENTATION_TRANSVERSE -> -1
//
//            else -> 1
//        }

    override fun applyToSize(size: IntSizeCompat, reverse: Boolean): IntSizeCompat {
        return size.rotate(if (!reverse) rotationDegrees else -rotationDegrees)
    }

    override fun applyToRect(
        srcRect: IntRectCompat,
        imageSize: IntSizeCompat,
        reverse: Boolean
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

    override fun applyToTileBitmap(
        tileBitmap: TileBitmap,
        reverse: Boolean,
        bitmapReuseHelper: TileBitmapReuseHelper?,
    ): TileBitmap {
        val bitmap = (tileBitmap as DesktopTileBitmap).bitmap

        val bitmap2: Bitmap
        val bitmap3: Bitmap
        val isRotated = abs(rotationDegrees % 360) != 0
        if (!reverse) {
            bitmap2 = if (isFlipped) {
                flipImage(bitmap, vertical = false)
            } else {
                bitmap
            }
            bitmap3 = if (isRotated) {
                rotateImage(bitmap2, rotationDegrees)
            } else {
                bitmap2
            }
        } else {
            bitmap2 = if (isRotated) {
                rotateImage(bitmap, -rotationDegrees)
            } else {
                bitmap
            }
            bitmap3 = if (isFlipped) {
                flipImage(bitmap2, vertical = false)
            } else {
                bitmap2
            }
        }
        return DesktopTileBitmap(bitmap3)
    }

    private fun flipImage(
        source: Bitmap,
        @Suppress("SameParameterValue") vertical: Boolean = false
    ): Bitmap {
        val flipped = Bitmap().apply {
            allocPixels(source.imageInfo)
        }
        Canvas(flipped).use { canvas ->
            if (!vertical) {
                canvas.translate(source.width.toFloat(), 0f)
                canvas.scale(-1f, 1f)
            } else {
                canvas.translate(0f, source.height.toFloat())
                canvas.scale(1f, -1f)
            }
            canvas.drawImage(Image.makeFromBitmap(source), 0f, 0f)
        }

        return flipped
    }

    private fun rotateImage(source: Bitmap, degree: Int): Bitmap {
        val sourceSize = IntSizeCompat(source.width, source.height)
        val newSize = sourceSize.rotate(degree)
        val newImage = Bitmap().apply {
            allocPixels(
                source.imageInfo.withWidthHeight(newSize.width, newSize.height)
            )
        }

        Canvas(newImage).use { canvas ->
            canvas.translate(
                /* tx = */ (newSize.width - sourceSize.width) / 2f,
                /* ty = */ (newSize.height - sourceSize.height) / 2f
            )
            canvas.rotate(
                /* degrees = */ degree.toFloat(),
                /* x = */ (sourceSize.width / 2).toFloat(),
                /* y = */ (sourceSize.height / 2).toFloat()
            )
            canvas.drawImage(Image.makeFromBitmap(source), 0f, 0f)
        }
        return newImage
    }

    override fun name(): String {
        return when (exifOrientation) {
            ExifOrientation.ORIENTATION_ROTATE_90 -> "ROTATE_90"
            ExifOrientation.ORIENTATION_TRANSPOSE -> "TRANSPOSE"
            ExifOrientation.ORIENTATION_ROTATE_180 -> "ROTATE_180"
            ExifOrientation.ORIENTATION_FLIP_VERTICAL -> "FLIP_VERTICAL"
            ExifOrientation.ORIENTATION_ROTATE_270 -> "ROTATE_270"
            ExifOrientation.ORIENTATION_TRANSVERSE -> "TRANSVERSE"
            ExifOrientation.ORIENTATION_FLIP_HORIZONTAL -> "FLIP_HORIZONTAL"
            ExifOrientation.ORIENTATION_UNDEFINED -> "UNDEFINED"
            ExifOrientation.ORIENTATION_NORMAL -> "NORMAL"
            else -> exifOrientation.toString()
        }
    }

    override fun toString(): String {
        return "DesktopExifOrientation(${name()})"
    }
}