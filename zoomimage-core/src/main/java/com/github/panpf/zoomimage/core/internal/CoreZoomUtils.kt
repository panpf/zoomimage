package com.github.panpf.zoomimage.core.internal

import com.github.panpf.zoomimage.core.OffsetCompat
import com.github.panpf.zoomimage.core.RectFCompat
import com.github.panpf.zoomimage.core.ScaleFactorCompat
import com.github.panpf.zoomimage.core.SizeCompat
import com.github.panpf.zoomimage.core.TransformCompat
import com.github.panpf.zoomimage.core.isNotEmpty
import kotlin.math.max
import kotlin.math.roundToInt

fun calculateNextStepScale(
    stepScales: FloatArray,
    currentScale: Float,
    rangeOfError: Float = 0.1f
): Float {
    if (stepScales.isEmpty()) return currentScale
    val formattedCurrentScale = currentScale.format(1)
    return stepScales
        .find { it.format(1) > formattedCurrentScale + rangeOfError }
        ?: stepScales.first()
}

val DEFAULT_MEDIUM_SCALE_MULTIPLE: Float = 3f

fun computeSupportScales(
    contentSize: SizeCompat,
    contentOriginSize: SizeCompat,
    containerSize: SizeCompat,
    scaleMode: ScaleMode,
    baseScale: ScaleFactorCompat,
    defaultMediumScaleMultiple: Float
): FloatArray {
    if (contentSize.isEmpty || containerSize.isEmpty) {
        return floatArrayOf(1.0f, 1.0f, 1.0f)
    } else if (scaleMode == ScaleMode.FILL_BOUNDS
        || baseScale.scaleX.format(2) != baseScale.scaleY.format(2)
    ) {
        val minScale = 1.0f
        val mediumScale = minScale * defaultMediumScaleMultiple
        return floatArrayOf(minScale, mediumScale, mediumScale * 2f)
    } else {
        // The width and height of content fill the container at the same time
        val fillContainerScale = max(
            containerSize.width / contentSize.width.toFloat(),
            containerSize.height / contentSize.height.toFloat()
        )
        // Enlarge content to the same size as its original
        val contentOriginScale = if (contentOriginSize.isNotEmpty) {
            val widthScale = contentOriginSize.width / contentSize.width.toFloat()
            val heightScale = contentOriginSize.height / contentSize.height.toFloat()
            max(widthScale, heightScale)
        } else {
            1.0f
        }
        val minScale = baseScale.scaleX
        val mediumScale = floatArrayOf(
            contentOriginScale,
            fillContainerScale,
            minScale * defaultMediumScaleMultiple
        ).maxOrNull()!!
        val maxScale = mediumScale * 2f
        return floatArrayOf(minScale, mediumScale, maxScale)
            .map { it / baseScale.scaleX }
            .toFloatArray()
    }
}

fun computeReadModeTransform(
    srcSize: SizeCompat,
    dstSize: SizeCompat,
    baseTransform: TransformCompat,
): TransformCompat {
    val widthScale = dstSize.width / srcSize.width.toFloat()
    val heightScale = dstSize.height / srcSize.height.toFloat()
    val fillMaxDimension = max(widthScale, heightScale)
    @Suppress("UnnecessaryVariable") val scaleX = fillMaxDimension
    @Suppress("UnnecessaryVariable") val scaleY = fillMaxDimension
    val translateX =
        if (baseTransform.offset.x < 0) baseTransform.offset.x * -1 * scaleX else 0.0f
    val translateY =
        if (baseTransform.offset.y < 0) baseTransform.offset.y * -1 * scaleY else 0.0f
    return TransformCompat(
        scale = ScaleFactorCompat(scaleX = scaleX, scaleY = scaleY),
        offset = OffsetCompat(x = translateX, y = translateY)
    )
}

fun computeCanDrag(
    contentSize: SizeCompat,
    contentVisibleRect: RectFCompat,
    horizontal: Boolean,
    direction: Int
): Boolean {
    if (contentSize.isEmpty || contentVisibleRect.isEmpty) return false
    return if (horizontal) {
        (direction > 0 && contentVisibleRect.left.roundToInt() > 0)
                || (direction < 0 && contentVisibleRect.right.roundToInt() < contentSize.width)
    } else {
        (direction > 0 && contentVisibleRect.top.roundToInt() > 0)
                || (direction < 0 && contentVisibleRect.bottom.roundToInt() < contentSize.height)
    }
}