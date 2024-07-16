package com.github.panpf.zoomimage.core.common.test.zoom.internal

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TopStart
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toOffset
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.calculateRotatedContentMoveToTopLeftOffset
import com.github.panpf.zoomimage.zoom.internal.BaseTransformHelper
import com.github.panpf.zoomimage.zoom.name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class BaseTransformHelperTest {

    @Test
    fun testRotatedContentSize() {
        val containerSize = IntSizeCompat(1000, 1000)
        val contentSize = IntSizeCompat(500, 400)
        val contentScale = ContentScaleCompat.Fit
        val alignment = AlignmentCompat.TopStart

        listOf(
            0 to IntSizeCompat(500, 400),
            90 to IntSizeCompat(400, 500),
            180 to IntSizeCompat(500, 400),
            270 to IntSizeCompat(400, 500),
            360 to IntSizeCompat(500, 400),
        ).forEach { (rotation, exceptedRotatedContentSize) ->
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                assertEquals(
                    expected = exceptedRotatedContentSize,
                    actual = it.rotatedContentSize,
                    message = "rotation=$rotation",
                )
            }
        }
    }

    @Test
    fun testScaleFactor() {
        val containerSize = IntSizeCompat(1000, 1000)
        val alignment = AlignmentCompat.TopStart

        var contentSize = IntSizeCompat(500, 400)
        listOf(
            ContentScaleCompat.Fit to 0 to ScaleFactorCompat(2.0f, 2.0f),
            ContentScaleCompat.FillWidth to 0 to ScaleFactorCompat(2.0f, 2.0f),
            ContentScaleCompat.FillHeight to 0 to ScaleFactorCompat(2.5f, 2.5f),
            ContentScaleCompat.FillBounds to 0 to ScaleFactorCompat(2.0f, 2.5f),
            ContentScaleCompat.Crop to 0 to ScaleFactorCompat(2.5f, 2.5f),
            ContentScaleCompat.Inside to 0 to ScaleFactorCompat(1.0f, 1.0f),
            ContentScaleCompat.None to 0 to ScaleFactorCompat(1.0f, 1.0f),
            ContentScaleCompat.Fit to 90 to ScaleFactorCompat(2.0f, 2.0f),
            ContentScaleCompat.FillWidth to 90 to ScaleFactorCompat(2.5f, 2.5f),
            ContentScaleCompat.FillHeight to 90 to ScaleFactorCompat(2.0f, 2.0f),
            ContentScaleCompat.FillBounds to 90 to ScaleFactorCompat(2.5f, 2.0f),
            ContentScaleCompat.Crop to 90 to ScaleFactorCompat(2.5f, 2.5f),
            ContentScaleCompat.Inside to 90 to ScaleFactorCompat(1.0f, 1.0f),
            ContentScaleCompat.None to 90 to ScaleFactorCompat(1.0f, 1.0f),
        ).forEach { item ->
            val contentScale = item.first.first
            val rotation = item.first.second
            val exceptedRotatedContentSize = item.second
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                assertEquals(
                    expected = exceptedRotatedContentSize,
                    actual = it.scaleFactor,
                    message = "contentSize=$contentSize, contentScale=${contentScale.name}, rotation=$rotation",
                )
            }
        }

        contentSize = IntSizeCompat(2000, 1600)
        listOf(
            ContentScaleCompat.Fit to 0 to ScaleFactorCompat(0.5f, 0.5f),
            ContentScaleCompat.FillWidth to 0 to ScaleFactorCompat(0.5f, 0.5f),
            ContentScaleCompat.FillHeight to 0 to ScaleFactorCompat(0.625f, 0.625f),
            ContentScaleCompat.FillBounds to 0 to ScaleFactorCompat(0.5f, 0.625f),
            ContentScaleCompat.Crop to 0 to ScaleFactorCompat(0.625f, 0.625f),
            ContentScaleCompat.Inside to 0 to ScaleFactorCompat(0.5f, 0.5f),
            ContentScaleCompat.None to 0 to ScaleFactorCompat(1.0f, 1.0f),
            ContentScaleCompat.Fit to 90 to ScaleFactorCompat(0.5f, 0.5f),
            ContentScaleCompat.FillWidth to 90 to ScaleFactorCompat(0.625f, 0.625f),
            ContentScaleCompat.FillHeight to 90 to ScaleFactorCompat(0.5f, 0.5f),
            ContentScaleCompat.FillBounds to 90 to ScaleFactorCompat(0.625f, 0.5f),
            ContentScaleCompat.Crop to 90 to ScaleFactorCompat(0.625f, 0.625f),
            ContentScaleCompat.Inside to 90 to ScaleFactorCompat(0.5f, 0.5f),
            ContentScaleCompat.None to 90 to ScaleFactorCompat(1.0f, 1.0f),
        ).forEach { item ->
            val contentScale = item.first.first
            val rotation = item.first.second
            val exceptedRotatedContentSize = item.second
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                assertEquals(
                    expected = exceptedRotatedContentSize,
                    actual = it.scaleFactor,
                    message = "contentSize=$contentSize, contentScale=${contentScale.name}, rotation=$rotation",
                )
            }
        }
    }

    @Test
    fun testScaledRotatedContentSize() {
        val containerSize = IntSizeCompat(1000, 1000)
        val alignment = AlignmentCompat.TopStart

        var contentSize = IntSizeCompat(500, 400)
        listOf(
            ContentScaleCompat.Fit to 0,
            ContentScaleCompat.FillWidth to 0,
            ContentScaleCompat.FillHeight to 0,
            ContentScaleCompat.FillBounds to 0,
            ContentScaleCompat.Crop to 0,
            ContentScaleCompat.Inside to 0,
            ContentScaleCompat.None to 0,
            ContentScaleCompat.Fit to 90,
            ContentScaleCompat.FillWidth to 90,
            ContentScaleCompat.FillHeight to 90,
            ContentScaleCompat.FillBounds to 90,
            ContentScaleCompat.Crop to 90,
            ContentScaleCompat.Inside to 90,
            ContentScaleCompat.None to 90,
        ).forEach { (contentScale, rotation) ->
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                assertEquals(
                    expected = it.rotatedContentSize.toSize().times(it.scaleFactor),
                    actual = it.scaledRotatedContentSize,
                    message = "contentSize=$contentSize, contentScale=${contentScale.name}, rotation=$rotation",
                )
            }
        }

        contentSize = IntSizeCompat(2000, 1600)
        listOf(
            ContentScaleCompat.Fit to 0,
            ContentScaleCompat.FillWidth to 0,
            ContentScaleCompat.FillHeight to 0,
            ContentScaleCompat.FillBounds to 0,
            ContentScaleCompat.Crop to 0,
            ContentScaleCompat.Inside to 0,
            ContentScaleCompat.None to 0,
            ContentScaleCompat.Fit to 90,
            ContentScaleCompat.FillWidth to 90,
            ContentScaleCompat.FillHeight to 90,
            ContentScaleCompat.FillBounds to 90,
            ContentScaleCompat.Crop to 90,
            ContentScaleCompat.Inside to 90,
            ContentScaleCompat.None to 90,
        ).forEach { (contentScale, rotation) ->
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                assertEquals(
                    expected = it.rotatedContentSize.toSize().times(it.scaleFactor),
                    actual = it.scaledRotatedContentSize,
                    message = "contentSize=$contentSize, contentScale=${contentScale.name}, rotation=$rotation",
                )
            }
        }
    }

    @Test
    fun testRotateRectifyOffset() {
        val containerSize = IntSizeCompat(1000, 1000)
        val alignment = AlignmentCompat.TopStart

        var contentSize = IntSizeCompat(500, 400)
        listOf(
            ContentScaleCompat.Fit to 0,
            ContentScaleCompat.FillWidth to 0,
            ContentScaleCompat.FillHeight to 0,
            ContentScaleCompat.FillBounds to 0,
            ContentScaleCompat.Crop to 0,
            ContentScaleCompat.Inside to 0,
            ContentScaleCompat.None to 0,
            ContentScaleCompat.Fit to 90,
            ContentScaleCompat.FillWidth to 90,
            ContentScaleCompat.FillHeight to 90,
            ContentScaleCompat.FillBounds to 90,
            ContentScaleCompat.Crop to 90,
            ContentScaleCompat.Inside to 90,
            ContentScaleCompat.None to 90,
        ).forEach { (contentScale, rotation) ->
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                val rotatedContentMoveToTopLeftOffset = calculateRotatedContentMoveToTopLeftOffset(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    rotation = rotation,
                )
                val result = rotatedContentMoveToTopLeftOffset * it.scaleFactor
                assertEquals(
                    expected = result,
                    actual = it.rotateRectifyOffset,
                    message = "contentSize=$contentSize, contentScale=${contentScale.name}, rotation=$rotation",
                )
            }
        }

        contentSize = IntSizeCompat(2000, 1600)
        listOf(
            ContentScaleCompat.Fit to 0,
            ContentScaleCompat.FillWidth to 0,
            ContentScaleCompat.FillHeight to 0,
            ContentScaleCompat.FillBounds to 0,
            ContentScaleCompat.Crop to 0,
            ContentScaleCompat.Inside to 0,
            ContentScaleCompat.None to 0,
            ContentScaleCompat.Fit to 90,
            ContentScaleCompat.FillWidth to 90,
            ContentScaleCompat.FillHeight to 90,
            ContentScaleCompat.FillBounds to 90,
            ContentScaleCompat.Crop to 90,
            ContentScaleCompat.Inside to 90,
            ContentScaleCompat.None to 90,
        ).forEach { (contentScale, rotation) ->
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                val rotatedContentMoveToTopLeftOffset = calculateRotatedContentMoveToTopLeftOffset(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    rotation = rotation,
                )
                val result = rotatedContentMoveToTopLeftOffset * it.scaleFactor
                assertEquals(
                    expected = result,
                    actual = it.rotateRectifyOffset,
                    message = "contentSize=$contentSize, contentScale=${contentScale.name}, rotation=$rotation",
                )
            }
        }
    }

    @Test
    fun testAlignmentOffset() {
        val containerSize = IntSizeCompat(1000, 1000)
        val contentSize = IntSizeCompat(500, 400)
        val contentScale = ContentScaleCompat.None
        val rotation = 0
        listOf(
            AlignmentCompat.TopStart,
            AlignmentCompat.TopCenter,
            AlignmentCompat.TopEnd,
            AlignmentCompat.CenterStart,
            AlignmentCompat.Center,
            AlignmentCompat.CenterEnd,
            AlignmentCompat.BottomStart,
            AlignmentCompat.BottomCenter,
            AlignmentCompat.BottomEnd,
        ).forEach { alignment ->
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                val result = alignment.align(
                    size = it.scaledRotatedContentSize.round(),
                    space = containerSize,
                    ltrLayout = it.ltrLayout
                ).toOffset()
                assertEquals(
                    expected = result,
                    actual = it.alignmentOffset,
                    message = "alignment=${alignment.name}",
                )
            }
        }
    }

    @Test
    fun testOffset() {
        val containerSize = IntSizeCompat(1000, 1000)
        val contentSize = IntSizeCompat(500, 400)
        val contentScale = ContentScaleCompat.None
        listOf(
            AlignmentCompat.TopStart to 0,
            AlignmentCompat.TopCenter to 0,
            AlignmentCompat.TopEnd to 0,
            AlignmentCompat.CenterStart to 0,
            AlignmentCompat.Center to 0,
            AlignmentCompat.CenterEnd to 0,
            AlignmentCompat.BottomStart to 0,
            AlignmentCompat.BottomCenter to 0,
            AlignmentCompat.BottomEnd to 0,
            AlignmentCompat.TopStart to 90,
            AlignmentCompat.TopCenter to 90,
            AlignmentCompat.TopEnd to 90,
            AlignmentCompat.CenterStart to 90,
            AlignmentCompat.Center to 90,
            AlignmentCompat.CenterEnd to 90,
            AlignmentCompat.BottomStart to 90,
            AlignmentCompat.BottomCenter to 90,
            AlignmentCompat.BottomEnd to 90,
        ).forEach { (alignment, rotation) ->
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                val result = it.rotateRectifyOffset + it.alignmentOffset
                assertEquals(
                    expected = result,
                    actual = it.offset,
                    message = "alignment=${alignment.name}, rotation=$rotation",
                )
            }
        }
    }

    @Test
    fun testRotationOrigin() {
        val containerSize = IntSizeCompat(1080, 1656)

        listOf(
            IntSizeCompat(7500, 232) to TransformOriginCompat(3.47f, 0.07f),
            IntSizeCompat(173, 3044) to TransformOriginCompat(0.08f, 0.92f),
            IntSizeCompat(575, 427) to TransformOriginCompat(0.27f, 0.13f),
            IntSizeCompat(551, 1038) to TransformOriginCompat(0.26f, 0.31f),
        ).forEach { (contentSize, expected) ->
            val baseTransformHelper = BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = ContentScaleCompat.Fit,
                alignment = AlignmentCompat.Center,
                rotation = 0,
            )
            assertEquals(
                expected = expected.toShortString(),
                actual = baseTransformHelper.rotationOrigin.toShortString(),
                message = "contentSize=$contentSize",
            )
        }
    }

    @Test
    fun testDisplayRect() {
        val containerSize = IntSizeCompat(1000, 1000)
        val contentScale = ContentScaleCompat.None
        val rotation = 0

        var contentSize = IntSizeCompat(500, 400)
        listOf(
            AlignmentCompat.TopStart,
            AlignmentCompat.TopCenter,
            AlignmentCompat.TopEnd,
            AlignmentCompat.CenterStart,
            AlignmentCompat.Center,
            AlignmentCompat.CenterEnd,
            AlignmentCompat.BottomStart,
            AlignmentCompat.BottomCenter,
            AlignmentCompat.BottomEnd,
        ).forEach { alignment ->
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                val result = RectCompat(
                    left = it.alignmentOffset.x,
                    top = it.alignmentOffset.y,
                    right = it.alignmentOffset.x + it.scaledRotatedContentSize.width,
                    bottom = it.alignmentOffset.y + it.scaledRotatedContentSize.height,
                )
                assertEquals(
                    expected = result,
                    actual = it.displayRect,
                    message = "alignment=${alignment.name}",
                )
            }
        }

        contentSize = IntSizeCompat(2000, 1600)
        listOf(
            AlignmentCompat.TopStart,
            AlignmentCompat.TopCenter,
            AlignmentCompat.TopEnd,
            AlignmentCompat.CenterStart,
            AlignmentCompat.Center,
            AlignmentCompat.CenterEnd,
            AlignmentCompat.BottomStart,
            AlignmentCompat.BottomCenter,
            AlignmentCompat.BottomEnd,
        ).forEach { alignment ->
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                val result = RectCompat(
                    left = it.alignmentOffset.x,
                    top = it.alignmentOffset.y,
                    right = it.alignmentOffset.x + it.scaledRotatedContentSize.width,
                    bottom = it.alignmentOffset.y + it.scaledRotatedContentSize.height,
                )
                assertEquals(
                    expected = result,
                    actual = it.displayRect,
                    message = "alignment=${alignment.name}",
                )
            }
        }
    }

    @Test
    fun testInsideDisplayRect() {
        val containerSize = IntSizeCompat(1000, 1000)
        val contentScale = ContentScaleCompat.None
        val rotation = 0

        var contentSize = IntSizeCompat(500, 400)
        listOf(
            AlignmentCompat.TopStart,
            AlignmentCompat.TopCenter,
            AlignmentCompat.TopEnd,
            AlignmentCompat.CenterStart,
            AlignmentCompat.Center,
            AlignmentCompat.CenterEnd,
            AlignmentCompat.BottomStart,
            AlignmentCompat.BottomCenter,
            AlignmentCompat.BottomEnd,
        ).forEach { alignment ->
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                val result = it.displayRect.limitTo(containerSize.toSize())
                assertEquals(
                    expected = result,
                    actual = it.insideDisplayRect,
                    message = "alignment=${alignment.name}",
                )
            }
        }

        contentSize = IntSizeCompat(2000, 1600)
        listOf(
            AlignmentCompat.TopStart,
            AlignmentCompat.TopCenter,
            AlignmentCompat.TopEnd,
            AlignmentCompat.CenterStart,
            AlignmentCompat.Center,
            AlignmentCompat.CenterEnd,
            AlignmentCompat.BottomStart,
            AlignmentCompat.BottomCenter,
            AlignmentCompat.BottomEnd,
        ).forEach { alignment ->
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                val result = it.displayRect.limitTo(containerSize.toSize())
                assertEquals(
                    expected = result,
                    actual = it.insideDisplayRect,
                    message = "alignment=${alignment.name}",
                )
                assertNotEquals(
                    illegal = it.displayRect,
                    actual = it.insideDisplayRect,
                    message = "alignment=${alignment.name}",
                )
            }
        }
    }

    @Test
    fun testTransform() {
        val containerSize = IntSizeCompat(1000, 1000)
        val contentScale = ContentScaleCompat.None
        val rotation = 0

        var contentSize = IntSizeCompat(500, 400)
        listOf(
            AlignmentCompat.TopStart,
            AlignmentCompat.TopCenter,
            AlignmentCompat.TopEnd,
            AlignmentCompat.CenterStart,
            AlignmentCompat.Center,
            AlignmentCompat.CenterEnd,
            AlignmentCompat.BottomStart,
            AlignmentCompat.BottomCenter,
            AlignmentCompat.BottomEnd,
        ).forEach { alignment ->
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                val result = TransformCompat(
                    scale = it.scaleFactor,
                    scaleOrigin = TransformOriginCompat.TopStart,
                    offset = it.offset,
                    rotation = rotation.toFloat(),
                    rotationOrigin = it.rotationOrigin,
                )
                assertEquals(
                    expected = result,
                    actual = it.transform,
                    message = "alignment=${alignment.name}",
                )
            }
        }

        contentSize = IntSizeCompat(2000, 1600)
        listOf(
            AlignmentCompat.TopStart,
            AlignmentCompat.TopCenter,
            AlignmentCompat.TopEnd,
            AlignmentCompat.CenterStart,
            AlignmentCompat.Center,
            AlignmentCompat.CenterEnd,
            AlignmentCompat.BottomStart,
            AlignmentCompat.BottomCenter,
            AlignmentCompat.BottomEnd,
        ).forEach { alignment ->
            BaseTransformHelper(
                containerSize = containerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
            ).also {
                val result = TransformCompat(
                    scale = it.scaleFactor,
                    scaleOrigin = TransformOriginCompat.TopStart,
                    offset = it.offset,
                    rotation = rotation.toFloat(),
                    rotationOrigin = it.rotationOrigin,
                )
                assertEquals(
                    expected = result,
                    actual = it.transform,
                    message = "alignment=${alignment.name}",
                )
            }
        }
    }
}