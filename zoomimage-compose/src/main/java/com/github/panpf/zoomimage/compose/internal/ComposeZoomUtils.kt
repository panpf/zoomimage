package com.github.panpf.zoomimage.compose.internal

import androidx.compose.ui.layout.ScaleFactor as ComposeScaleFactor
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.times
import com.github.panpf.zoomimage.core.Origin
import com.github.panpf.zoomimage.core.TransformCompat
import kotlin.math.absoluteValue

internal fun computeContentScaleOffset(
    srcSize: Size,
    dstSize: Size,
    scale: ContentScale,
    alignment: Alignment,
): Offset {
    if (dstSize.isNotAvailable() || dstSize.isEmpty()
        || srcSize.isNotAvailable() || srcSize.isEmpty()
    ) {
        return Offset.Zero
    }
    val contentScaleFactor =
        scale.computeScaleFactor(srcSize = srcSize, dstSize = dstSize)
    val contentScaledContentSize = srcSize.times(contentScaleFactor)
    return when (alignment) {
        Alignment.TopStart -> Offset(
            x = 0f,
            y = 0f,
        )

        Alignment.TopCenter -> Offset(
            x = (dstSize.width - contentScaledContentSize.width) / 2,
            y = 0f,
        )

        Alignment.TopEnd -> Offset(
            x = dstSize.width - contentScaledContentSize.width,
            y = 0f,
        )

        Alignment.CenterStart -> Offset(
            x = 0f,
            y = (dstSize.height - contentScaledContentSize.height) / 2,
        )

        Alignment.Center -> Offset(
            x = (dstSize.width - contentScaledContentSize.width) / 2,
            y = (dstSize.height - contentScaledContentSize.height) / 2,
        )

        Alignment.CenterEnd -> Offset(
            x = dstSize.width - contentScaledContentSize.width,
            y = (dstSize.height - contentScaledContentSize.height) / 2,
        )

        Alignment.BottomStart -> Offset(
            x = 0f,
            y = dstSize.height - contentScaledContentSize.height,
        )

        Alignment.BottomCenter -> Offset(
            x = (dstSize.width - contentScaledContentSize.width) / 2,
            y = dstSize.height - contentScaledContentSize.height,
        )

        Alignment.BottomEnd -> Offset(
            x = dstSize.width - contentScaledContentSize.width,
            y = dstSize.height - contentScaledContentSize.height,
        )

        else -> Offset(
            x = 0f,
            y = 0f,
        )
    }
}

internal fun computeContentInContainerRect(
    containerSize: Size,
    contentSize: Size,
    contentScale: ContentScale,
    contentAlignment: Alignment,
): Rect {
    if (containerSize.isNotAvailable() || contentSize.isNotAvailable()) return Rect.Zero
    val contentScaleFactor =
        contentScale.computeScaleFactor(srcSize = contentSize, dstSize = containerSize)
    val contentScaledContentSize = contentSize.times(contentScaleFactor)
    val offset = computeContentScaleOffset(
        srcSize = contentSize,
        dstSize = containerSize,
        scale = contentScale,
        alignment = contentAlignment
    )
    return Rect(
        left = offset.x.coerceAtLeast(0f),
        top = offset.y.coerceAtLeast(0f),
        right = (offset.x + contentScaledContentSize.width)
            .coerceAtMost(containerSize.width),
        bottom = (offset.y + contentScaledContentSize.height)
            .coerceAtMost(containerSize.height),
    )
}

internal fun computeContentInContainerVisibleRect(
    containerSize: Size,
    contentSize: Size,
    contentScale: ContentScale,
    contentAlignment: Alignment,
): Rect {
    val contentScaleFactor =
        contentScale.computeScaleFactor(srcSize = contentSize, dstSize = containerSize)
    val contentScaledContentSize = contentSize.times(contentScaleFactor)

    val left: Float
    val right: Float
    val horizontalSpace = (contentScaledContentSize.width - containerSize.width) / 2
    if (contentScaledContentSize.width <= containerSize.width) {
        left = 0f
        right = contentScaledContentSize.width
    } else if (contentAlignment.isStart) {
        left = 0f
        right = containerSize.width
    } else if (contentAlignment.isHorizontalCenter) {
        left = horizontalSpace
        right = horizontalSpace + containerSize.width
    } else {   // contentAlignment.isEnd
        left = contentScaledContentSize.width - containerSize.width
        right = contentScaledContentSize.width
    }

    val top: Float
    val bottom: Float
    val verticalSpace = (contentScaledContentSize.height - containerSize.height) / 2
    if (contentScaledContentSize.height <= containerSize.height) {
        top = 0f
        bottom = contentScaledContentSize.height
    } else if (contentAlignment.isTop) {
        top = 0f
        bottom = containerSize.height
    } else if (contentAlignment.isVerticalCenter) {
        top = verticalSpace
        bottom = verticalSpace + containerSize.height
    } else {   // contentAlignment.isBottom
        top = contentScaledContentSize.height - containerSize.height
        bottom = contentScaledContentSize.height
    }

    return Rect(left = left, top = top, right = right, bottom = bottom)
        .restoreScale(contentScaleFactor)
}

internal fun computeLocationOffset(
    containerOrigin: Origin,
    newScale: Float,
    containerSize: Size,
): Offset {
    if (containerSize.isNotAvailable() || containerSize.isEmpty()) return Offset.Zero
    val scaledContainerSize = containerSize.times(newScale)
    val scaledContainerOffset = Offset(
        x = scaledContainerSize.width * containerOrigin.x,
        y = scaledContainerSize.height * containerOrigin.y,
    )
    return Offset(
        x = scaledContainerOffset.x - (containerSize.width / 2),
        y = scaledContainerOffset.y - (containerSize.height / 2),
    ).run {
        Offset(
            x = (x * -1).coerceIn(-scaledContainerSize.width, 0f),
            y = (y * -1).coerceIn(-scaledContainerSize.height, 0f)
        )
    }
}

internal fun computeOffsetBounds(
    containerSize: Size,
    contentSize: Size,
    contentScale: ContentScale,
    contentAlignment: Alignment,
    scale: Float
): Rect {
    // based on the top left zoom
    if (scale <= 1.0f || containerSize.isNotAvailable() || contentSize.isNotAvailable()) {
        return Rect.Zero
    }
    val scaledContainerSize = containerSize.times(scale)
    val scaledContentInContainerRect = computeContentInContainerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        contentAlignment = contentAlignment
    ).scale(scale)

    val horizontalBounds = if (scaledContentInContainerRect.width > containerSize.width) {
        ((scaledContentInContainerRect.right - containerSize.width) * -1)..(scaledContentInContainerRect.left * -1)
    } else if (contentAlignment.isStart) {
        0f..0f
    } else if (contentAlignment.isHorizontalCenter) {
        val horizontalSpace = (scaledContainerSize.width - containerSize.width) / 2 * -1
        horizontalSpace..horizontalSpace
    } else {   // contentAlignment.isEnd
        val horizontalSpace = (scaledContainerSize.width - containerSize.width) * -1
        horizontalSpace..horizontalSpace
    }

    val verticalBounds = if (scaledContentInContainerRect.height > containerSize.height) {
        ((scaledContentInContainerRect.bottom - containerSize.height) * -1)..(scaledContentInContainerRect.top * -1)
    } else if (contentAlignment.isTop) {
        0f..0f
    } else if (contentAlignment.isVerticalCenter) {
        val verticalSpace = (scaledContainerSize.height - containerSize.height) / 2 * -1
        verticalSpace..verticalSpace
    } else {   // contentAlignment.isBottom
        val verticalSpace = (scaledContainerSize.height - containerSize.height) * -1
        verticalSpace..verticalSpace
    }

    return Rect(
        left = horizontalBounds.start,
        top = verticalBounds.start,
        right = horizontalBounds.endInclusive,
        bottom = verticalBounds.endInclusive
    )
}


/* ******************************************* VisibleRect ***************************************** */

internal fun computeContainerVisibleRect(
    containerSize: Size,
    scale: Float,
    offset: Offset
): Rect {
    if (containerSize.isNotAvailable()) return Rect.Zero
    val scaledContainerSize = containerSize.times(scale)
    val left: Float
    val right: Float
    if (offset.x >= scaledContainerSize.width || offset.x <= -scaledContainerSize.width) {
        left = 0f
        right = 0f
    } else if (offset.x > 0) {
        left = 0f
        right = (containerSize.width - offset.x).coerceIn(0f..scaledContainerSize.width)
    } else { // offset.x < 0
        left = offset.x.absoluteValue
        right = (offset.x.absoluteValue + containerSize.width)
            .coerceAtMost(scaledContainerSize.width)
    }
    val top: Float
    val bottom: Float
    if (offset.y >= scaledContainerSize.height || offset.y <= -scaledContainerSize.height) {
        top = 0f
        bottom = 0f
    } else if (offset.y > 0) {
        top = 0f
        bottom = (containerSize.height - offset.y).coerceAtMost(scaledContainerSize.height)
    } else { // offset.y < 0
        top = offset.y.absoluteValue
        bottom = (offset.y.absoluteValue + containerSize.height)
            .coerceAtMost(scaledContainerSize.height)
    }
    return Rect(left = left, top = top, right = right, bottom = bottom)
        .restoreScale(scale)
}

internal fun computeContentVisibleRect(
    containerSize: Size,
    contentSize: Size,
    contentScale: ContentScale,
    contentAlignment: Alignment,
    scale: Float,
    offset: Offset,
): Rect {
    if (containerSize.isNotAvailable() || contentSize.isNotAvailable()) return Rect.Zero
    val containerVisibleRect = computeContainerVisibleRect(containerSize, scale, offset)
    val contentInContainerRect = computeContentInContainerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        contentAlignment = contentAlignment,
    )
    if (containerVisibleRect.overlaps(contentInContainerRect)) {
        val contentScaleFactor =
            contentScale.computeScaleFactor(srcSize = contentSize, dstSize = containerSize)
        val contentInContainerVisibleRect = computeContentInContainerVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
        )
        return Rect(
            left = (containerVisibleRect.left - contentInContainerRect.left).coerceAtLeast(0f),
            top = (containerVisibleRect.top - contentInContainerRect.top).coerceAtLeast(0f),
            right = (containerVisibleRect.right - contentInContainerRect.left)
                .coerceIn(0f, contentInContainerRect.width),
            bottom = (containerVisibleRect.bottom - contentInContainerRect.top)
                .coerceIn(0f, contentInContainerRect.height)
        ).restoreScale(contentScaleFactor)
            .translate(
                translateX = contentInContainerVisibleRect.left,
                translateY = contentInContainerVisibleRect.top
            )
    } else {
        return Rect(0f, 0f, 0f, 0f)
    }
}


/* ******************************************* Origin ***************************************** */

internal fun computeContainerOriginByTouchPosition(
    containerSize: Size,
    scale: Float,
    offset: Offset,
    touch: Offset
): Origin {
    if (containerSize.isNotAvailable()) return Origin.Zero
    val touchOfContainer = touch - offset
    return Origin(
        x = ((touchOfContainer.x / scale) / containerSize.width).coerceIn(0f, 1f),
        y = ((touchOfContainer.y / scale) / containerSize.height).coerceIn(0f, 1f),
    )
}

internal fun containerOriginToContentOrigin(
    containerSize: Size,
    contentSize: Size,
    contentScale: ContentScale,
    contentAlignment: Alignment,
    containerOrigin: Origin
): Origin {
    if (containerSize.isNotAvailable() || contentSize.isNotAvailable()) return Origin.Zero
    val contentInContainerRect = computeContentInContainerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        contentAlignment = contentAlignment
    )
    val contentInContainerVisibleRect = computeContentInContainerVisibleRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        contentAlignment = contentAlignment
    )
    val contentScaleFactor =
        contentScale.computeScaleFactor(srcSize = contentSize, dstSize = containerSize)
    val containerOriginOffset = Offset(
        x = containerSize.width * containerOrigin.x,
        y = containerSize.height * containerOrigin.y
    )
    val contentScaledContentOriginOffset = Offset(
        x = containerOriginOffset.x - contentInContainerRect.left,
        y = containerOriginOffset.y - contentInContainerRect.top,
    )
    val contentOriginOffset = Offset(
        x = contentScaledContentOriginOffset.x / contentScaleFactor.scaleX,
        y = contentScaledContentOriginOffset.y / contentScaleFactor.scaleY,
    ).let {
        Offset(
            x = it.x + contentInContainerVisibleRect.left,
            y = it.y + contentInContainerVisibleRect.top
        )
    }.let {
        Offset(
            x = it.x.coerceIn(0f, contentSize.width),
            y = it.y.coerceIn(0f, contentSize.height)
        )
    }
    return Origin(
        x = contentOriginOffset.x / contentSize.width,
        y = contentOriginOffset.y / contentSize.height
    )
}

internal fun contentOriginToContainerOrigin(
    containerSize: Size,
    contentSize: Size,
    contentScale: ContentScale,
    contentAlignment: Alignment,
    contentOrigin: Origin
): Origin {
    if (containerSize.isNotAvailable() || contentSize.isNotAvailable()) return Origin.Zero
    val contentInContainerRect = computeContentInContainerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        contentAlignment = contentAlignment
    )
    val contentInContainerVisibleRect = computeContentInContainerVisibleRect(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = contentScale,
        contentAlignment = contentAlignment
    )
    val contentScaleFactor =
        contentScale.computeScaleFactor(srcSize = contentSize, dstSize = containerSize)
    val contentOriginOffset = Offset(
        x = contentSize.width * contentOrigin.x,
        y = contentSize.height * contentOrigin.y,
    ).let {
        Offset(
            x = it.x - contentInContainerVisibleRect.left,
            y = it.y - contentInContainerVisibleRect.top,
        )
    }
    val contentScaledContentOriginOffset = Offset(
        x = contentOriginOffset.x * contentScaleFactor.scaleX,
        y = contentOriginOffset.y * contentScaleFactor.scaleY,
    )
    val containerOriginOffset = Offset(
        x = contentInContainerRect.left + contentScaledContentOriginOffset.x,
        y = contentInContainerRect.top + contentScaledContentOriginOffset.y,
    ).let {
        Offset(
            x = it.x.coerceIn(0f, containerSize.width),
            y = it.y.coerceIn(0f, containerSize.height),
        )
    }
    return Origin(
        x = containerOriginOffset.x / containerSize.width,
        y = containerOriginOffset.y / containerSize.height
    )
}


/* ******************************************* Other ***************************************** */

//internal fun computeScrollEdge(
//    contentSize: Size,
//    contentVisibleRect: Rect,
//    horizontal: Boolean
//): Edge {
//    if (contentSize.isNotAvailable() || contentVisibleRect.isEmpty) return Edge.BOTH
//    if (horizontal) {
//        return if (contentVisibleRect.left <= 0f && contentVisibleRect.right.roundToInt() >= contentSize.width.roundToInt()) {
//            Edge.BOTH
//        } else if (contentVisibleRect.left > 0f && contentVisibleRect.right.roundToInt() < contentSize.width.roundToInt()) {
//            Edge.NONE
//        } else if (contentVisibleRect.left <= 0f) {
//            Edge.START
//        } else {
//            // contentVisibleRect.right >= contentSize.width
//            Edge.END
//        }
//    } else {
//        // vertical
//        return if (contentVisibleRect.top <= 0f && contentVisibleRect.bottom.roundToInt() >= contentSize.height.roundToInt()) {
//            Edge.BOTH
//        } else if (contentVisibleRect.top > 0f && contentVisibleRect.bottom.roundToInt() < contentSize.height.roundToInt()) {
//            Edge.NONE
//        } else if (contentVisibleRect.top <= 0f) {
//            Edge.START
//        } else {
//            // contentVisibleRect.bottom >= contentSize.height
//            Edge.END
//        }
//    }
//}

internal fun computeTransform(
    srcSize: Size,
    dstSize: Size,
    scale: ContentScale,
    alignment: Alignment,
): TransformCompat {
    val scaleFactor = scale.computeScaleFactor(srcSize, dstSize)
    val offset = computeContentScaleOffset(
        srcSize = srcSize,
        dstSize = dstSize,
        scale = scale,
        alignment = alignment
    )
    return TransformCompat(scaleFactor.toCompatScaleFactor(), offset.toCompatOffset())
}

internal fun computeReadModeTransform(
    srcSize: Size,
    dstSize: Size,
    scale: ContentScale,
    alignment: Alignment,
): TransformCompat {
    return com.github.panpf.zoomimage.core.internal.computeReadModeTransform(
        srcSize = srcSize.toCompatSize(),
        dstSize = dstSize.toCompatSize(),
        baseTransform = computeTransform(
            srcSize = srcSize,
            dstSize = dstSize,
            scale = scale,
            alignment = alignment
        )
    )
}

internal fun computeScaleFactor(
    srcSize: Size,
    dstSize: Size,
    contentScale: ContentScale
): ComposeScaleFactor {
    return if (dstSize.isNotAvailable() || dstSize.isEmpty() || srcSize.isNotAvailable() || srcSize.isEmpty()) {
        ComposeScaleFactor(1f, 1f)
    } else {
        contentScale.computeScaleFactor(srcSize = srcSize, dstSize = dstSize)
    }
}

internal fun ContentScale.supportReadMode(): Boolean = this != ContentScale.FillBounds

internal fun computeScaleOffsetByCentroid(
    currentScale: Float,
    currentOffset: Offset,
    targetScale: Float,
    centroid: Offset,
    gestureRotate: Float,
): Offset {
    // copied https://github.com/androidx/androidx/blob/643b1cfdd7dfbc5ccce1ad951b6999df049678b3/compose/foundation/foundation/samples/src/main/java/androidx/compose/foundation/samples/TransformGestureSamples.kt
    @Suppress("UnnecessaryVariable") val oldScale = currentScale
    @Suppress("UnnecessaryVariable") val newScale = targetScale
    var contentOffset = currentOffset / currentScale * -1f
    // For natural zooming and rotating, the centroid of the gesture should
    // be the fixed point where zooming and rotating occurs.
    // We compute where the centroid was (in the pre-transformed coordinate
    // space), and then compute where it will be after this delta.
    // We then compute what the new offset should be to keep the centroid
    // visually stationary for rotating and zooming, and also apply the pan.
    contentOffset = (contentOffset + centroid / oldScale).rotateBy(gestureRotate) - (centroid / newScale)
    return contentOffset * newScale * -1f
}