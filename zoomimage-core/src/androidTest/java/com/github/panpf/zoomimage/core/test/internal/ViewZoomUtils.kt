package com.github.panpf.zoomimage.core.test.internal

import android.widget.ImageView
import com.github.panpf.zoomimage.core.IntOffsetCompat
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.TransformCompat
import com.github.panpf.zoomimage.core.times
import com.github.panpf.zoomimage.core.toCompatOffset
import kotlin.math.roundToInt


internal fun ImageView.ScaleType.computeTransform(
    srcSize: IntSizeCompat,
    dstSize: IntSizeCompat
): TransformCompat {
    val scaleFactor = this.computeScaleFactor(srcSize, dstSize)
    val offset = computeContentScaleOffset(srcSize, dstSize, this)
    return TransformCompat(scale = scaleFactor, offset = offset.toCompatOffset())
}

internal fun computeContentScaleOffset(
    srcSize: IntSizeCompat,
    dstSize: IntSizeCompat,
    scaleType: ImageView.ScaleType
): IntOffsetCompat {
    val scaleFactor = scaleType.computeScaleFactor(srcSize = srcSize, dstSize = dstSize)
    val scaledSrcSize = srcSize.times(scaleFactor)
    val horSpace = ((dstSize.width - scaledSrcSize.width) / 2.0f).roundToInt()
    val verSpace = ((dstSize.height - scaledSrcSize.height) / 2.0f).roundToInt()
    return when (scaleType) {
        ImageView.ScaleType.CENTER -> IntOffsetCompat(x = horSpace, y = verSpace)
        ImageView.ScaleType.CENTER_CROP -> IntOffsetCompat(x = horSpace, y = verSpace)
        ImageView.ScaleType.CENTER_INSIDE -> IntOffsetCompat(x = horSpace, y = verSpace)
        ImageView.ScaleType.FIT_START -> IntOffsetCompat(x = 0, y = 0)
        ImageView.ScaleType.FIT_CENTER -> IntOffsetCompat(x = horSpace, y = verSpace)
        ImageView.ScaleType.FIT_END -> IntOffsetCompat(
            x = dstSize.width - scaledSrcSize.width,
            y = dstSize.height - scaledSrcSize.height
        )

        ImageView.ScaleType.FIT_XY -> IntOffsetCompat(x = 0, y = 0)
        ImageView.ScaleType.MATRIX -> IntOffsetCompat(x = 0, y = 0)
        else -> IntOffsetCompat(
            x = 0,
            y = 0
        )
    }
}