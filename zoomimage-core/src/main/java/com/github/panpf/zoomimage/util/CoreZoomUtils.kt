package com.github.panpf.zoomimage.util

import com.github.panpf.zoomimage.Edge
import com.github.panpf.zoomimage.ScrollEdge
import com.github.panpf.zoomimage.util.internal.format
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

const val DefaultMediumScaleMinMultiple: Float = 3f

fun computeUserScales(
    contentSize: IntSizeCompat,
    contentOriginSize: IntSizeCompat,
    containerSize: IntSizeCompat,
    scaleMode: ScaleMode,
    baseScale: ScaleFactorCompat,
    mediumScaleMinMultiple: Float
): FloatArray {
    if (contentSize.isEmpty() || containerSize.isEmpty()) {
        return floatArrayOf(1.0f, 1.0f, 1.0f)
    } else if (scaleMode == ScaleMode.FILL_BOUNDS
        || baseScale.scaleX.format(2) != baseScale.scaleY.format(2)
    ) {
        val minScale = 1.0f
        val mediumScale = minScale * mediumScaleMinMultiple
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
            minScale * mediumScaleMinMultiple
        ).maxOrNull()!!
        val maxScale = mediumScale * 2f
        return floatArrayOf(minScale, mediumScale, maxScale)
            .map { it / baseScale.scaleX }
            .toFloatArray()
    }
}

fun isSameDirection(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val srcAspectRatio = srcSize.width.toFloat().div(srcSize.height).format(2)
    val dstAspectRatio = dstSize.width.toFloat().div(dstSize.height).format(2)
    return (srcAspectRatio == 1.0f || dstAspectRatio == 1.0f)
            || (srcAspectRatio > 1.0f && dstAspectRatio > 1.0f)
            || (srcAspectRatio < 1.0f && dstAspectRatio < 1.0f)
}


fun computeScrollEdge(
    contentInContainerVisibleRect: IntRectCompat,
    contentVisibleRect: IntRectCompat,
): ScrollEdge {
    if (contentInContainerVisibleRect.isEmpty || contentVisibleRect.isEmpty)
        return ScrollEdge(horizontal = Edge.BOTH, vertical = Edge.BOTH)
    return ScrollEdge(
        horizontal = when {
            contentVisibleRect.width >= contentInContainerVisibleRect.width -> Edge.BOTH
            contentVisibleRect.left <= contentInContainerVisibleRect.left -> Edge.START
            contentVisibleRect.right >= contentInContainerVisibleRect.right -> Edge.END
            else -> Edge.NONE
        },
        vertical = when {
            contentVisibleRect.height >= contentInContainerVisibleRect.height -> Edge.BOTH
            contentVisibleRect.top <= contentInContainerVisibleRect.top -> Edge.START
            contentVisibleRect.bottom >= contentInContainerVisibleRect.bottom -> Edge.END
            else -> Edge.NONE
        },
    )
}

/**
 * Whether you can scroll horizontally or vertical in the specified direction
 *
 * @param direction Negative to check scrolling left, positive to check scrolling right.
 */
fun canScroll(horizontal: Boolean, direction: Int, scrollEdge: ScrollEdge): Boolean {
    return if (horizontal) {
        if (direction < 0) {
            scrollEdge.horizontal != Edge.START && scrollEdge.horizontal != Edge.BOTH
        } else {
            scrollEdge.horizontal != Edge.END && scrollEdge.horizontal != Edge.BOTH
        }
    } else {
        if (direction < 0) {
            scrollEdge.vertical != Edge.START && scrollEdge.vertical != Edge.BOTH
        } else {
            scrollEdge.vertical != Edge.END && scrollEdge.vertical != Edge.BOTH
        }
    }
}

fun limitScaleWithRubberBand(
    currentScale: Float,
    targetScale: Float,
    minScale: Float,
    maxScale: Float,
    rubberBandRatio: Float = 2f
): Float {
    return when {
        targetScale > maxScale -> {
            val addScale = targetScale - currentScale
            val rubberBandMaxScale = maxScale * rubberBandRatio
            val overScale = targetScale - maxScale
            val overMaxScale = rubberBandMaxScale - maxScale
            val progress = overScale / overMaxScale
            // Multiplying by 0.5f is to be a little slower
            val limitedAddScale = addScale * (1 - progress) * 0.5f
            currentScale + limitedAddScale
        }

        targetScale < minScale -> {
            val addScale = targetScale - currentScale
            val rubberBandMinScale = minScale / rubberBandRatio
            val overScale = targetScale - minScale
            val overMinScale = rubberBandMinScale - minScale
            val progress = overScale / overMinScale
            // Multiplying by 0.5f is to be a little slower
            val limitedAddScale = addScale * (1 - progress) * 0.5f
            currentScale + limitedAddScale
        }

        else -> targetScale
    }
}

enum class ScaleMode {
    CROP,
    FIT,
    FILL_UNILATERAL,
    FILL_BOUNDS,
    INSIDE,
    NONE,
}