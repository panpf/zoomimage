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
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
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
        val bufferedImage = (tileBitmap as DesktopTileBitmap).bufferedImage

        val bufferedImage2: BufferedImage
        val bufferedImage3: BufferedImage
        val isRotated = abs(rotationDegrees % 360) != 0
        if (!reverse) {
            bufferedImage2 = if (isFlipped) {
                flipImage(bufferedImage, vertical = false)
            } else {
                bufferedImage
            }
            bufferedImage3 = if (isRotated) {
                rotateImage(bufferedImage2, rotationDegrees)
            } else {
                bufferedImage2
            }
        } else {
            bufferedImage2 = if (isRotated) {
                rotateImage(bufferedImage, -rotationDegrees)
            } else {
                bufferedImage
            }
            bufferedImage3 = if (isFlipped) {
                flipImage(bufferedImage2, vertical = false)
            } else {
                bufferedImage2
            }
        }
        return DesktopTileBitmap(bufferedImage3)
    }

    private fun flipImage(
        source: BufferedImage,
        @Suppress("SameParameterValue") vertical: Boolean = false
    ): BufferedImage {
        val flipped = BufferedImage(source.width, source.height, source.type)
        val graphics = flipped.createGraphics()
        val transform = if (!vertical) {
            AffineTransform.getTranslateInstance(source.width.toDouble(), 0.0)
        } else {
            AffineTransform.getTranslateInstance(0.0, source.height.toDouble())
        }.apply {
            val flip = if (!vertical) {
                AffineTransform.getScaleInstance(-1.0, 1.0)
            } else {
                AffineTransform.getScaleInstance(1.0, -1.0)
            }
            concatenate(flip)
        }
        graphics.transform = transform
        graphics.drawImage(source, 0, 0, null)
        graphics.dispose()
        return flipped
    }

    private fun rotateImage(source: BufferedImage, degree: Int): BufferedImage {
        val sourceSize = IntSizeCompat(source.width, source.height)
        val newSize = sourceSize.rotate(degree)
        val type = source.colorModel.transparency
        val newImage = BufferedImage(newSize.width, newSize.height, type)
        val graphics: Graphics2D = newImage.createGraphics()
//        graphics.setRenderingHint(
//            RenderingHints.KEY_INTERPOLATION,
//            RenderingHints.VALUE_INTERPOLATION_BILINEAR
//        )
        graphics.translate(
            /* tx = */ (newSize.width - sourceSize.width) / 2.0,
            /* ty = */ (newSize.height - sourceSize.height) / 2.0
        )
        graphics.rotate(
            /* theta = */ Math.toRadians(degree.toDouble()),
            /* x = */ (sourceSize.width / 2).toDouble(),
            /* y = */ (sourceSize.height / 2).toDouble()
        )
        graphics.drawImage(source, 0, 0, null)
        graphics.dispose()
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