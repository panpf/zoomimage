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

package com.github.panpf.zoomimage.view.util

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Looper
import android.view.View
import android.widget.ImageView.ScaleType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import kotlin.math.roundToInt

/**
 * Check if the current thread is the main thread, otherwise throw an exception.
 *
 * @see com.github.panpf.zoomimage.view.test.util.ViewPlatformUtilsTest.testRequiredMainThread
 */
internal fun requiredMainThread() {
    check(Looper.myLooper() == Looper.getMainLooper()) {
        "This method must be executed in the UI thread"
    }
}

internal fun requiredWorkThread() {
    check(Looper.myLooper() != Looper.getMainLooper()) {
        "This method must be executed in the work thread"
    }
}

/**
 * Scale the [Rect] by [scale].
 *
 * @see com.github.panpf.zoomimage.view.test.util.ViewPlatformUtilsTest.testRectScale
 */
internal fun Rect.scale(scale: Float): Rect = Rect(
    /* left = */ (left * scale).roundToInt(),
    /* top = */ (top * scale).roundToInt(),
    /* right = */ (right * scale).roundToInt(),
    /* bottom = */ (bottom * scale).roundToInt()
)

/**
 * Find [Lifecycle] from [Context].
 *
 * @see com.github.panpf.zoomimage.view.test.util.ViewPlatformUtilsTest.testContextFindLifecycle
 */
internal fun Context.findLifecycle(): Lifecycle? = when (this) {
    is LifecycleOwner -> this.lifecycle
    is ContextWrapper -> this.baseContext.findLifecycle()
    else -> null
}

/**
 * Get the intrinsic size of the [Drawable].
 *
 * @see com.github.panpf.zoomimage.view.test.util.ViewPlatformUtilsTest.testDrawableIntrinsicSize
 */
internal fun Drawable.intrinsicSize(): IntSizeCompat =
    IntSizeCompat(intrinsicWidth, intrinsicHeight)

/**
 * Convert [ScaleType] to [ContentScaleCompat].
 *
 * @see com.github.panpf.zoomimage.view.test.util.ViewPlatformUtilsTest.testScaleTypeToContentScale
 */
internal fun ScaleType.toContentScale(): ContentScaleCompat = when (this) {
    ScaleType.MATRIX -> ContentScaleCompat.None
    ScaleType.FIT_XY -> ContentScaleCompat.FillBounds
    ScaleType.FIT_START -> ContentScaleCompat.Fit
    ScaleType.FIT_CENTER -> ContentScaleCompat.Fit
    ScaleType.FIT_END -> ContentScaleCompat.Fit
    ScaleType.CENTER -> ContentScaleCompat.None
    ScaleType.CENTER_CROP -> ContentScaleCompat.Crop
    ScaleType.CENTER_INSIDE -> ContentScaleCompat.Inside
}

/**
 * Convert [ScaleType] to [AlignmentCompat].
 *
 * @see com.github.panpf.zoomimage.view.test.util.ViewPlatformUtilsTest.testScaleTypeToAlignment
 */
internal fun ScaleType.toAlignment(): AlignmentCompat = when (this) {
    ScaleType.MATRIX -> AlignmentCompat.TopStart
    ScaleType.FIT_XY -> AlignmentCompat.TopStart
    ScaleType.FIT_START -> AlignmentCompat.TopStart
    ScaleType.FIT_CENTER -> AlignmentCompat.Center
    ScaleType.FIT_END -> AlignmentCompat.BottomEnd
    ScaleType.CENTER -> AlignmentCompat.Center
    ScaleType.CENTER_CROP -> AlignmentCompat.Center
    ScaleType.CENTER_INSIDE -> AlignmentCompat.Center
}

/**
 * If [layoutDirection] is [View.LAYOUT_DIRECTION_RTL], returns the horizontally flipped [AlignmentCompat], otherwise returns itself
 *
 * @see com.github.panpf.zoomimage.view.test.util.ViewPlatformUtilsTest.testRtlFlipped
 */
fun AlignmentCompat.rtlFlipped(layoutDirection: Int? = null): AlignmentCompat {
    return if (layoutDirection == null || layoutDirection == View.LAYOUT_DIRECTION_RTL) {
        when (this) {
            AlignmentCompat.TopStart -> AlignmentCompat.TopEnd
            AlignmentCompat.TopCenter -> AlignmentCompat.TopCenter
            AlignmentCompat.TopEnd -> AlignmentCompat.TopStart
            AlignmentCompat.CenterStart -> AlignmentCompat.CenterEnd
            AlignmentCompat.Center -> AlignmentCompat.Center
            AlignmentCompat.CenterEnd -> AlignmentCompat.CenterStart
            AlignmentCompat.BottomStart -> AlignmentCompat.BottomEnd
            AlignmentCompat.BottomCenter -> AlignmentCompat.BottomCenter
            AlignmentCompat.BottomEnd -> AlignmentCompat.BottomStart
            else -> this
        }
    } else {
        this
    }
}

//internal fun ScaleType.computeScaleFactor(
//    srcSize: IntSizeCompat,
//    dstSize: IntSizeCompat
//): ScaleFactorCompat {
//    val widthScale = dstSize.width / srcSize.width.toFloat()
//    val heightScale = dstSize.height / srcSize.height.toFloat()
//    val fillMaxDimension = max(widthScale, heightScale)
//    val fillMinDimension = min(widthScale, heightScale)
//    return when (this) {
//        ScaleType.CENTER -> ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)
//
//        ScaleType.CENTER_CROP -> {
//            ScaleFactorCompat(scaleX = fillMaxDimension, scaleY = fillMaxDimension)
//        }
//
//        ScaleType.CENTER_INSIDE -> {
//            if (srcSize.width <= dstSize.width && srcSize.height <= dstSize.height) {
//                ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)
//            } else {
//                ScaleFactorCompat(scaleX = fillMinDimension, scaleY = fillMinDimension)
//            }
//        }
//
//        ScaleType.FIT_START,
//        ScaleType.FIT_CENTER,
//        ScaleType.FIT_END -> {
//            ScaleFactorCompat(scaleX = fillMinDimension, scaleY = fillMinDimension)
//        }
//
//        ScaleType.FIT_XY -> {
//            ScaleFactorCompat(scaleX = widthScale, scaleY = heightScale)
//        }
//
//        ScaleType.MATRIX -> ScaleFactorCompat(1.0f, 1.0f)
//        else -> ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)
//    }
//}
//
//internal fun ScaleType.isStart(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
//    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
//    return this == ScaleType.MATRIX
//            || this == ScaleType.FIT_XY
//            || (this == ScaleType.FIT_START && scaledSrcSize.width < dstSize.width)
//}
//
//internal fun ScaleType.isHorizontalCenter(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
//    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
//    return this == ScaleType.CENTER
//            || this == ScaleType.CENTER_CROP
//            || this == ScaleType.CENTER_INSIDE
//            || this == ScaleType.FIT_CENTER
//            || (this == ScaleType.FIT_START && scaledSrcSize.width >= dstSize.width)
//            || (this == ScaleType.FIT_END && scaledSrcSize.width >= dstSize.width)
//}
//
//internal fun ScaleType.isCenter(): Boolean =
//    this == ScaleType.CENTER
//            || this == ScaleType.CENTER_CROP
//            || this == ScaleType.CENTER_INSIDE
//            || this == ScaleType.FIT_CENTER
//
//internal fun ScaleType.isEnd(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
//    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
//    return this == ScaleType.FIT_END && scaledSrcSize.width < dstSize.width
//}
//
//internal fun ScaleType.isTop(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
//    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
//    return this == ScaleType.MATRIX
//            || this == ScaleType.FIT_XY
//            || (this == ScaleType.FIT_START && scaledSrcSize.height < dstSize.height)
//}
//
//internal fun ScaleType.isVerticalCenter(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
//    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
//    return this == ScaleType.CENTER
//            || this == ScaleType.CENTER_CROP
//            || this == ScaleType.CENTER_INSIDE
//            || this == ScaleType.FIT_CENTER
//            || (this == ScaleType.FIT_START && scaledSrcSize.height >= dstSize.height)
//            || (this == ScaleType.FIT_END && scaledSrcSize.height >= dstSize.height)
//}
//
//internal fun ScaleType.isBottom(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
//    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
//    return this == ScaleType.FIT_END && scaledSrcSize.height < dstSize.height
//}

/**
 * Apply [TransformCompat] to the matrix.
 *
 * @see com.github.panpf.zoomimage.view.test.util.ViewPlatformUtilsTest.testMatrixApplyTransform
 */
internal fun Matrix.applyTransform(
    transform: TransformCompat,
    containerSize: IntSizeCompat
): Matrix {
    reset()
    postRotate(
        /* degrees = */ transform.rotation,
        /* px = */ transform.rotationOriginX * containerSize.width,
        /* py = */ transform.rotationOriginY * containerSize.height
    )
    postScale(transform.scale.scaleX, transform.scale.scaleY)
    postTranslate(transform.offset.x, transform.offset.y)
    return this
}

//private val matrixValuesLocal = ThreadLocal<FloatArray>()
//private val Matrix.localValues: FloatArray
//    get() {
//        val values = matrixValuesLocal.get()
//            ?: FloatArray(9).apply { matrixValuesLocal.set(this) }
//        getValues(values)
//        return values
//    }
//
//internal fun Matrix.getScale(): ScaleFactorCompat {
//    val values = localValues
//
//    val scaleX: Float = values[Matrix.MSCALE_X]
//    val skewY: Float = values[Matrix.MSKEW_Y]
//    val scaleX1 = sqrt(scaleX.toDouble().pow(2.0) + skewY.toDouble().pow(2.0)).toFloat()
//    val scaleY: Float = values[Matrix.MSCALE_Y]
//    val skewX: Float = values[Matrix.MSKEW_X]
//    val scaleY1 = sqrt(scaleY.toDouble().pow(2.0) + skewX.toDouble().pow(2.0)).toFloat()
//    val scaleFactorCompat = ScaleFactorCompat(scaleX = scaleX1, scaleY = scaleY1)
//    return scaleFactorCompat
//}
//
//internal fun Matrix.getTranslation(): OffsetCompat {
//    val values = localValues
//    val offsetCompat = OffsetCompat(
//        x = values[Matrix.MTRANS_X],
//        y = values[Matrix.MTRANS_Y]
//    )
//    return offsetCompat
//}
//
//internal fun Matrix.getRotation(): Int {
//    val values = localValues
//    val skewX: Float = values[Matrix.MSKEW_X]
//    val scaleX: Float = values[Matrix.MSCALE_X]
//    val degrees = (atan2(skewX.toDouble(), scaleX.toDouble()) * (180 / Math.PI)).roundToInt()
//    val rotation = when {
//        degrees < 0 -> abs(degrees)
//        degrees > 0 -> 360 - degrees
//        else -> 0
//    }
//    return rotation
//}