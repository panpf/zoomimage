package com.github.panpf.zoomimage.subsampling

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.WorkerThread
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.subsampling.internal.safeConfig
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlin.math.abs

class AndroidExifOrientation constructor(val exifOrientation: Int) : ExifOrientation {

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

    @Suppress("unused")
    val translation: Int =
        when (exifOrientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            ExifInterface.ORIENTATION_FLIP_VERTICAL,
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_TRANSVERSE -> -1

            else -> 1
        }

    override fun applyToSize(size: IntSizeCompat): IntSizeCompat {
        val matrix = Matrix().apply {
            applyFlipAndRotationToMatrix(this, isFlipped, rotationDegrees, true)
        }
        val newRect = RectF(0f, 0f, size.width.toFloat(), size.height.toFloat())
        matrix.mapRect(newRect)
        return IntSizeCompat(newRect.width().toInt(), newRect.height().toInt())
    }

    override fun addToSize(size: IntSizeCompat): IntSizeCompat {
        val matrix = Matrix().apply {
            applyFlipAndRotationToMatrix(this, isFlipped, -rotationDegrees, false)
        }
        val newRect = RectF(0f, 0f, size.width.toFloat(), size.height.toFloat())
        matrix.mapRect(newRect)
        return IntSizeCompat(newRect.width().toInt(), newRect.height().toInt())
    }

    override fun addToRect(srcRect: IntRectCompat, imageSize: IntSizeCompat): IntRectCompat =
        when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> IntRectCompat(
                srcRect.top,
                imageSize.width - srcRect.right,
                srcRect.bottom,
                imageSize.width - srcRect.left,
            )

            ExifInterface.ORIENTATION_TRANSVERSE -> IntRectCompat(
                imageSize.height - srcRect.bottom,
                imageSize.width - srcRect.right,
                imageSize.height - srcRect.top,
                imageSize.width - srcRect.left,
            )

            ExifInterface.ORIENTATION_ROTATE_180 -> IntRectCompat(
                imageSize.width - srcRect.right,
                imageSize.height - srcRect.bottom,
                imageSize.width - srcRect.left,
                imageSize.height - srcRect.top
            )

            ExifInterface.ORIENTATION_FLIP_VERTICAL -> IntRectCompat(
                srcRect.left,
                imageSize.height - srcRect.bottom,
                srcRect.right,
                imageSize.height - srcRect.top,
            )

            ExifInterface.ORIENTATION_ROTATE_270 -> IntRectCompat(
                imageSize.height - srcRect.bottom,
                srcRect.left,
                imageSize.height - srcRect.top,
                srcRect.right
            )

            ExifInterface.ORIENTATION_TRANSPOSE -> IntRectCompat(
                srcRect.top,
                srcRect.left,
                srcRect.bottom,
                srcRect.right
            )

            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> IntRectCompat(
                imageSize.width - srcRect.right,
                srcRect.top,
                imageSize.width - srcRect.left,
                srcRect.bottom,
            )

            else -> srcRect
        }

    override fun applyToTileBitmap(
        bitmapReuseHelper: TileBitmapReuseHelper?,
        tileBitmap: TileBitmap,
    ): TileBitmap {
        val bitmap = (tileBitmap as AndroidTileBitmap).bitmap!!
        val androidBitmapReuseHelper =
            if (bitmapReuseHelper != null && bitmapReuseHelper is AndroidTileBitmapReuseHelper)
                bitmapReuseHelper else null
        val newBitmap = applyToBitmap(bitmap, androidBitmapReuseHelper)
            ?: return tileBitmap
        return DefaultAndroidTileBitmap(newBitmap)
    }

    @WorkerThread
    fun applyToBitmap(
        inBitmap: Bitmap,
        bitmapReuseHelper: AndroidTileBitmapReuseHelper? = null,
    ): Bitmap? {
        return applyFlipAndRotation(
            inBitmap = inBitmap,
            isFlipped = isFlipped,
            rotationDegrees = rotationDegrees,
            apply = true,
            bitmapReuseHelper = bitmapReuseHelper,
        )
    }

    @Suppress("unused")
    @WorkerThread
    fun addToBitmap(
        inBitmap: Bitmap,
        bitmapReuseHelper: AndroidTileBitmapReuseHelper? = null,
    ): Bitmap? = applyFlipAndRotation(
        inBitmap = inBitmap,
        isFlipped = isFlipped,
        rotationDegrees = -rotationDegrees,
        apply = false,
        bitmapReuseHelper = bitmapReuseHelper,
    )

    private fun applyFlipAndRotationToMatrix(
        matrix: Matrix,
        isFlipped: Boolean,
        rotationDegrees: Int,
        apply: Boolean
    ) {
        val isRotated = abs(rotationDegrees % 360) != 0
        if (apply) {
            if (isFlipped) {
                matrix.postScale(-1f, 1f)
            }
            if (isRotated) {
                matrix.postRotate(rotationDegrees.toFloat())
            }
        } else {
            if (isRotated) {
                matrix.postRotate(rotationDegrees.toFloat())
            }
            if (isFlipped) {
                matrix.postScale(-1f, 1f)
            }
        }
    }

    @WorkerThread
    private fun applyFlipAndRotation(
        inBitmap: Bitmap,
        isFlipped: Boolean,
        rotationDegrees: Int,
        apply: Boolean,
        bitmapReuseHelper: AndroidTileBitmapReuseHelper?,
    ): Bitmap? {
        val isRotated = abs(rotationDegrees % 360) != 0
        if (!isFlipped && !isRotated) {
            return null
        }

        val matrix = Matrix().apply {
            applyFlipAndRotationToMatrix(this, isFlipped, rotationDegrees, apply)
        }
        val newRect = RectF(0f, 0f, inBitmap.width.toFloat(), inBitmap.height.toFloat())
        matrix.mapRect(newRect)
        matrix.postTranslate(-newRect.left, -newRect.top)

        val config = inBitmap.safeConfig
        val newWidth = newRect.width().toInt()
        val newHeight = newRect.height().toInt()
        val outBitmap = bitmapReuseHelper
            ?.getOrCreate(newWidth, newHeight, config, "applyFlipAndRotation")
            ?: Bitmap.createBitmap(newWidth, newHeight, config)

        val canvas = Canvas(outBitmap)
        val paint = Paint(Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(inBitmap, matrix, paint)
        return outBitmap
    }

    override fun name(): String {
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
}