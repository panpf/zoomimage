package com.github.panpf.zoomimage.core.internal

import android.graphics.Rect
import com.github.panpf.zoomimage.Size
import com.github.panpf.zoomimage.internal.ScaleFactor
import com.github.panpf.zoomimage.internal.ScaleMode
import com.github.panpf.zoomimage.internal.Transform
import com.github.panpf.zoomimage.internal.format
import com.github.panpf.zoomimage.isNotEmpty
import kotlin.math.max

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

fun computeSupportScales(
    contentSize: Size,
    contentOriginSize: Size,
    containerSize: Size,
    scaleMode: ScaleMode,
    baseScale: ScaleFactor,
): FloatArray {
    if (contentSize.isEmpty || containerSize.isEmpty) {
        return floatArrayOf(1.0f, 1.0f, 1.0f)
    } else if (scaleMode == ScaleMode.FILL_BOUNDS
        || baseScale.scaleX.format(2) != baseScale.scaleY.format(2)
    ) {
        return floatArrayOf(1.0f, 2.0f, 4.0f)
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
        val mediumScale = floatArrayOf(contentOriginScale, fillContainerScale, minScale * 2f)
            .maxOrNull()!!
        val maxScale = mediumScale * 2f
        return floatArrayOf(minScale, mediumScale, maxScale)
            .map { it / baseScale.scaleX }
            .toFloatArray()
    }
}

fun computeReadModeTransform(
    srcSize: Size,
    dstSize: Size,
    baseTransform: Transform,
): Transform {
    val widthScale = dstSize.width / srcSize.width.toFloat()
    val heightScale = dstSize.height / srcSize.height.toFloat()
    val fillMaxDimension = max(widthScale, heightScale)
    @Suppress("UnnecessaryVariable") val scaleX = fillMaxDimension
    @Suppress("UnnecessaryVariable") val scaleY = fillMaxDimension
    val translateX =
        if (baseTransform.translationX < 0) baseTransform.translationX * -1 * scaleX else 0.0f
    val translateY =
        if (baseTransform.translationY < 0) baseTransform.translationY * -1 * scaleY else 0.0f
    return Transform(
        scaleX = scaleX,
        scaleY = scaleY,
        translationX = translateX,
        translationY = translateY
    )
}

fun computeCanDrag(
    contentSize: Size,
    contentVisibleRect: Rect,
    horizontal: Boolean,
    direction: Int
): Boolean {
    if (contentSize.isEmpty || contentVisibleRect.isEmpty) return false
    return if (horizontal) {
        (direction > 0 && contentVisibleRect.left > 0) || (direction < 0 && contentVisibleRect.right < contentSize.width)
    } else {
        (direction > 0 && contentVisibleRect.top > 0) || (direction < 0 && contentVisibleRect.bottom < contentSize.height)
    }
}