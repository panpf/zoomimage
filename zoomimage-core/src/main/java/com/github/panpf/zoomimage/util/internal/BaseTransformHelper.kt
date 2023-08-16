package com.github.panpf.zoomimage.util.internal

import com.github.panpf.zoomimage.util.AlignmentCompat
import com.github.panpf.zoomimage.util.ContentScaleCompat
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.center
import com.github.panpf.zoomimage.util.computeContentRotateOrigin
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.minus
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toOffset
import com.github.panpf.zoomimage.util.toSize

internal class BaseTransformHelper(
    val containerSize: IntSizeCompat,
    val contentSize: IntSizeCompat,
    val contentScale: ContentScaleCompat,
    val alignment: AlignmentCompat,
    val rotation: Int,
    val ltrLayout: Boolean = true,
) {
    // todo Unit tests
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */

    val rotatedContentSize: IntSizeCompat by lazy { contentSize.rotate(rotation) }

    val scaleFactor: ScaleFactorCompat by lazy {
        contentScale.computeScaleFactor(
            srcSize = rotatedContentSize.toSize(),
            dstSize = containerSize.toSize()
        )
    }

    val scaledRotatedContentSize: SizeCompat by lazy {
        rotatedContentSize.toSize().times(scaleFactor)
    }

    val rotateOffset: OffsetCompat by lazy {
        val rotatedContentMoveToTopLeftOffset = computeRotatedContentMoveToTopLeftOffset(
            containerSize = containerSize,
            contentSize = contentSize,
            rotation = rotation,
        )
        rotatedContentMoveToTopLeftOffset * scaleFactor
    }

    val alignmentOffset: OffsetCompat by lazy {
        alignment.align(
            size = scaledRotatedContentSize.round(),
            space = containerSize,
            ltrLayout = ltrLayout
        ).toOffset()
    }

    val offset: OffsetCompat by lazy { rotateOffset + alignmentOffset }

    val rotationOrigin by lazy {
        computeContentRotateOrigin(
            containerSize = containerSize,
            contentSize = contentSize,
            rotation = rotation
        )
    }

    val displayRect: RectCompat by lazy {
        RectCompat(
            left = alignmentOffset.x,
            top = alignmentOffset.y,
            right = alignmentOffset.x + scaledRotatedContentSize.width,
            bottom = alignmentOffset.y + scaledRotatedContentSize.height,
        )
    }

    val insideDisplayRect: RectCompat by lazy {
        displayRect.limitTo(containerSize.toSize())
    }

    val transform: TransformCompat by lazy {
        TransformCompat(
            scale = scaleFactor,
            scaleOrigin = TransformOriginCompat.TopStart,
            offset = offset,
            rotation = rotation.toFloat(),
            rotationOrigin = rotationOrigin,
        )
    }

    private fun computeRotatedContentRect(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        rotation: Int,
    ): RectCompat {
        if (containerSize.isEmpty() || contentSize.isEmpty()) {
            return RectCompat.Zero
        }
        require(rotation % 90 == 0) { "rotation must be multiple of 90" }

        if (rotation % 180 == 0) {
            return RectCompat(
                left = 0f,
                top = 0f,
                right = containerSize.width.toFloat(),
                bottom = containerSize.height.toFloat(),
            )
        } else {
            val contentCenter = contentSize.toSize().center
            val left = contentCenter.x - contentCenter.y
            val top = contentCenter.y - contentCenter.x
            return RectCompat(
                left = left,
                top = top,
                right = left + contentSize.height,
                bottom = top + contentSize.width,
            )
        }
    }

    private fun computeRotatedContentMoveToTopLeftOffset(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        rotation: Int,
    ): OffsetCompat {
        val rotatedContentRect = computeRotatedContentRect(
            containerSize = containerSize,
            contentSize = contentSize,
            rotation = rotation,
        )
        return IntOffsetCompat.Zero - rotatedContentRect.topLeft
    }
}