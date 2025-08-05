package com.github.panpf.zoomimage.core.common.test.zoom.internal

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.Origin
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.minus
import com.github.panpf.zoomimage.util.plus
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.internal.calculateBaseTransform
import com.github.panpf.zoomimage.zoom.internal.calculateContentVisibleRect
import com.github.panpf.zoomimage.zoom.internal.calculateInitialZoom
import com.github.panpf.zoomimage.zoom.internal.calculateRestoreContentVisibleCenterUserTransform
import com.github.panpf.zoomimage.zoom.internal.calculateRestoreVisibleCenterTransformWhenOnlyContainerSizeChanged
import com.github.panpf.zoomimage.zoom.internal.calculateRestoreVisibleRectTransformWhenOnlyContentSizeChanged
import com.github.panpf.zoomimage.zoom.internal.calculateSourceScaleFactor
import com.github.panpf.zoomimage.zoom.internal.calculateSourceVisibleRect
import com.github.panpf.zoomimage.zoom.internal.sourceToDraw
import com.github.panpf.zoomimage.zoom.internal.transformAboutEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ZoomsTest5 {

    @Test
    fun testCalculateRestoreContentVisibleCenterUserTransform() {
        val containerSize = IntSizeCompat(800, 572)
        val contentSize = IntSizeCompat(6799, 4882)
        val contentOriginSize = IntSizeCompat.Zero
        val contentScale = ContentScaleCompat.Fit
        val alignment = AlignmentCompat.Center
        val rotation = 0
        val readMode: ReadMode? = null
        val scalesCalculator = ScalesCalculator.Dynamic
        val lastBaseTransform = TransformCompat(
            scale = ScaleFactorCompat(0.117165096f, 0.117165096f),
            offset = OffsetCompat(2.0f, 0.0f),
            rotationOrigin = TransformOriginCompat(4.249375f, 4.2674823f)
        )
        val lastUserTransform = TransformCompat(
            scale = ScaleFactorCompat(8.5349655f, 8.5349655f),
            offset = OffsetCompat(-3948.0999f, -2968.974f),
            rotationOrigin = TransformOriginCompat(4.249375f, 4.2674823f)
        )
        val lastTransform = (lastBaseTransform + lastUserTransform).apply {
            assertEquals(
                "TransformCompat(scale=1.0x1.0, " +
                        "offset=-3931.03x-2968.97, " +
                        "rotation=0.0, " +
                        "scaleOrigin=0.0x0.0, " +
                        "rotationOrigin=4.25x4.27)",
                this.toString()
            )
        }
        val contentVisibleCenterPoint = calculateContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = false,
            rotation = rotation,
            userScale = lastUserTransform.scaleX,
            userOffset = lastUserTransform.offset,
        ).center.apply {
            assertEquals("OffsetCompat(4331.0, 3255.0)", this.toString())
        }

        listOf(
            IntSizeCompat(1900, 1072),
            IntSizeCompat(1072, 1900),
            IntSizeCompat(880, 433),
            IntSizeCompat(433, 880),
        ).forEach { newContainerSize ->
            val newInitialZoom = calculateInitialZoom(
                containerSize = newContainerSize,
                contentSize = contentSize,
                contentOriginSize = contentOriginSize,
                contentScale = contentScale,
                alignment = alignment,
                rtlLayoutDirection = false,
                rotation = rotation,
                readMode = readMode,
                scalesCalculator = scalesCalculator
            )
            val newBaseTransform = newInitialZoom.baseTransform
            val newUserTransform = calculateRestoreContentVisibleCenterUserTransform(
                containerSize = newContainerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rotation = rotation,
                newBaseTransform = newBaseTransform,
                lastTransform = lastTransform,
                lastContentVisibleCenter = contentVisibleCenterPoint,
            )

            val newContentVisibleCenterPoint = calculateContentVisibleRect(
                containerSize = newContainerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rtlLayoutDirection = false,
                rotation = rotation,
                userScale = newUserTransform.scaleX,
                userOffset = newUserTransform.offset,
            ).center

            assertEquals(
                expected = contentVisibleCenterPoint.x,
                actual = newContentVisibleCenterPoint.x,
                absoluteTolerance = 1f,
                message = "newContainerSize: $newContainerSize. assert x",
            )
            assertEquals(
                expected = contentVisibleCenterPoint.y,
                actual = newContentVisibleCenterPoint.y,
                absoluteTolerance = 1f,
                message = "newContainerSize: $newContainerSize. assert y",
            )
        }
    }

    @Test
    fun testCalculateRestoreVisibleCenterTransformWhenOnlyContainerSizeChanged() {
        val containerSize = IntSizeCompat(800, 572)
        val contentSize = IntSizeCompat(6799, 4882)
        val contentScale = ContentScaleCompat.Fit
        val alignment = AlignmentCompat.Center
        val rtlLayoutDirection = false
        val rotation = 0
        val lastBaseTransform = TransformCompat(
            scale = ScaleFactorCompat(0.117165096f, 0.117165096f),
            offset = OffsetCompat(2.0f, 0.0f),
            rotationOrigin = TransformOriginCompat(4.249375f, 4.2674823f)
        )
        val lastUserTransform = TransformCompat(
            scale = ScaleFactorCompat(8.5349655f, 8.5349655f),
            offset = OffsetCompat(-3948.0999f, -2968.974f),
            rotationOrigin = TransformOriginCompat(4.249375f, 4.2674823f)
        )
        val lastTransform = (lastBaseTransform + lastUserTransform).apply {
            assertEquals(
                "TransformCompat(scale=1.0x1.0, " +
                        "offset=-3931.03x-2968.97, " +
                        "rotation=0.0, " +
                        "scaleOrigin=0.0x0.0, " +
                        "rotationOrigin=4.25x4.27)",
                this.toString()
            )
        }
        val contentVisibleCenterPoint = calculateContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
            userScale = lastUserTransform.scaleX,
            userOffset = lastUserTransform.offset,
        ).center.apply {
            assertEquals("OffsetCompat(4331.0, 3255.0)", this.toString())
        }

        listOf(
            IntSizeCompat(1900, 1072),
            IntSizeCompat(1072, 1900),
            IntSizeCompat(880, 433),
            IntSizeCompat(433, 880),
        ).forEach { newContainerSize ->
            val newBaseTransform = calculateBaseTransform(
                containerSize = newContainerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rtlLayoutDirection = rtlLayoutDirection,
                rotation = rotation,
            )
            val newTransform = calculateRestoreVisibleCenterTransformWhenOnlyContainerSizeChanged(
                oldContainerSize = containerSize,
                newContainerSize = newContainerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rtlLayoutDirection = rtlLayoutDirection,
                rotation = rotation,
                transform = lastTransform,
            )
            val newUserTransform = newTransform - newBaseTransform

            val newContentVisibleCenterPoint = calculateContentVisibleRect(
                containerSize = newContainerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rtlLayoutDirection = rtlLayoutDirection,
                rotation = rotation,
                userScale = newUserTransform.scaleX,
                userOffset = newUserTransform.offset,
            ).center

            assertEquals(
                expected = contentVisibleCenterPoint.x,
                actual = newContentVisibleCenterPoint.x,
                absoluteTolerance = 1f,
                message = "newContainerSize: $newContainerSize. assert x",
            )
            assertEquals(
                expected = contentVisibleCenterPoint.y,
                actual = newContentVisibleCenterPoint.y,
                absoluteTolerance = 1f,
                message = "newContainerSize: $newContainerSize. assert y",
            )
        }
    }
    @Test
    fun testCalculateRestoreVisibleCenterTransformWhenOnlyContainerSizeChangedWithRTL() {
        val containerSize = IntSizeCompat(800, 572)
        val contentSize = IntSizeCompat(6799, 4882)
        val contentScale = ContentScaleCompat.Fit
        val alignment = AlignmentCompat.Center
        val rtlLayoutDirection = true
        val rotation = 0
        val lastBaseTransform = TransformCompat(
            scale = ScaleFactorCompat(0.117165096f, 0.117165096f),
            offset = OffsetCompat(2.0f, 0.0f),
            rotationOrigin = TransformOriginCompat(4.249375f, 4.2674823f)
        )
        val lastUserTransform = TransformCompat(
            scale = ScaleFactorCompat(8.5349655f, 8.5349655f),
            offset = OffsetCompat(-3948.0999f, -2968.974f),
            rotationOrigin = TransformOriginCompat(4.249375f, 4.2674823f)
        )
        val lastTransform = (lastBaseTransform + lastUserTransform).apply {
            assertEquals(
                "TransformCompat(scale=1.0x1.0, " +
                        "offset=-3931.03x-2968.97, " +
                        "rotation=0.0, " +
                        "scaleOrigin=0.0x0.0, " +
                        "rotationOrigin=4.25x4.27)",
                this.toString()
            )
        }
        val contentVisibleCenterPoint = calculateContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
            userScale = lastUserTransform.scaleX,
            userOffset = lastUserTransform.offset,
        ).center.apply {
            assertEquals("OffsetCompat(4331.0, 3255.0)", this.toString())
        }

        listOf(
            IntSizeCompat(1900, 1072),
            IntSizeCompat(1072, 1900),
            IntSizeCompat(880, 433),
            IntSizeCompat(433, 880),
        ).forEach { newContainerSize ->
            val newBaseTransform = calculateBaseTransform(
                containerSize = newContainerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rtlLayoutDirection = rtlLayoutDirection,
                rotation = rotation,
            )
            val newTransform = calculateRestoreVisibleCenterTransformWhenOnlyContainerSizeChanged(
                oldContainerSize = containerSize,
                newContainerSize = newContainerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rtlLayoutDirection = rtlLayoutDirection,
                rotation = rotation,
                transform = lastTransform,
            )
            val newUserTransform = newTransform - newBaseTransform

            val newContentVisibleCenterPoint = calculateContentVisibleRect(
                containerSize = newContainerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
                rtlLayoutDirection = rtlLayoutDirection,
                rotation = rotation,
                userScale = newUserTransform.scaleX,
                userOffset = newUserTransform.offset,
            ).center

            assertEquals(
                expected = contentVisibleCenterPoint.x,
                actual = newContentVisibleCenterPoint.x,
                absoluteTolerance = 1f,
                message = "newContainerSize: $newContainerSize. assert x",
            )
            assertEquals(
                expected = contentVisibleCenterPoint.y,
                actual = newContentVisibleCenterPoint.y,
                absoluteTolerance = 1f,
                message = "newContainerSize: $newContainerSize. assert y",
            )
        }
    }

    @Test
    fun testCalculateRestoreVisibleCenterTransformWhenOnlyContainerSizeChanged3() {
        val containerSize = IntSizeCompat(800, 572)
        val contentSize = IntSizeCompat(6799, 4882)
        val contentScale = ContentScaleCompat.Fit
        val alignment = AlignmentCompat.Center
        val rtlLayoutDirection = false
        val rotation = 0
        val lastBaseTransform = calculateBaseTransform(
            containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
        )
        val lastUserTransform = TransformCompat.Origin
        val lastTransform = (lastBaseTransform + lastUserTransform)

        val newTransform = calculateRestoreVisibleCenterTransformWhenOnlyContainerSizeChanged(
            oldContainerSize = containerSize,
            newContainerSize = IntSizeCompat(900, 600),
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rtlLayoutDirection = rtlLayoutDirection,
            rotation = rotation,
            transform = lastTransform,
        )
        assertEquals(
            expected = "TransformCompat(scale=0.12x0.12, offset=32.2x0.0, rotation=0.0, scaleOrigin=0.0x0.0, rotationOrigin=3.78x4.07)",
            actual = newTransform.toString()
        )
    }

    @Test
    fun testCalculateRestoreVisibleRectTransformWhenOnlyContentSizeChanged() {
        val containerSize = IntSizeCompat(1080, 1920)
        val contentSize = IntSizeCompat(679, 488)
        val baseTransform = calculateBaseTransform(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = ContentScaleCompat.Fit,
            alignment = AlignmentCompat.Center,
            rtlLayoutDirection = false,
            rotation = 90
        )
        val userTransform =
            TransformCompat(scale = ScaleFactorCompat(8f), offset = OffsetCompat(300f, 500f))
        val transform = baseTransform + userTransform

        val newContentSize = contentSize * 3
        calculateRestoreVisibleRectTransformWhenOnlyContentSizeChanged(
            oldContentSize = contentSize,
            newContentSize = newContentSize,
            transform = transform
        ).apply {
            assertEquals(
                expected = ScaleFactorCompat((contentSize.width * transform.scaleX) / newContentSize.width),
                actual = scale
            )
            assertEquals(expected = transform.offset, actual = offset)
            assertEquals(expected = transform.rotation, actual = rotation)
            assertEquals(expected = transform.rotationOrigin, actual = rotationOrigin)
            assertEquals(expected = transform.scaleOrigin, actual = scaleOrigin)
        }
    }

    @Test
    fun testCalculateRestoreVisibleRectTransformWhenOnlyContentSizeChanged2() {
        val containerSize = IntSizeCompat(1080, 1920)
        val contentSize = IntSizeCompat(488, 679)
        val baseTransform = calculateBaseTransform(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = ContentScaleCompat.Fit,
            alignment = AlignmentCompat.Center,
            rtlLayoutDirection = false,
            rotation = 90
        )
        val userTransform =
            TransformCompat(scale = ScaleFactorCompat(8f), offset = OffsetCompat(300f, 500f))
        val transform = baseTransform + userTransform

        val newContentSize = contentSize * 3
        calculateRestoreVisibleRectTransformWhenOnlyContentSizeChanged(
            oldContentSize = contentSize,
            newContentSize = newContentSize,
            transform = transform
        ).apply {
            assertEquals(
                expected = ScaleFactorCompat((contentSize.width * transform.scaleX) / newContentSize.width),
                actual = scale
            )
            assertEquals(expected = transform.offset, actual = offset)
            assertEquals(expected = transform.rotation, actual = rotation)
            assertEquals(expected = transform.rotationOrigin, actual = rotationOrigin)
            assertEquals(expected = transform.scaleOrigin, actual = scaleOrigin)
        }
    }

    @Test
    fun testTransformAboutEquals() {
        val transform = TransformCompat(
            scale = ScaleFactorCompat(1.3487f, 8.44322f),
            offset = OffsetCompat(199.9872f, 80.232f),
        )

        assertTrue(
            actual = transformAboutEquals(one = transform, two = transform.copy())
        )

        assertTrue(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(scale = transform.scale.copy(scaleX = transform.scaleX + 0.1f))
            )
        )
        assertTrue(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(scale = transform.scale.copy(scaleX = transform.scaleX - 0.1f))
            )
        )
        assertFalse(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(scale = transform.scale.copy(scaleX = transform.scaleX + 0.11f))
            )
        )
        assertFalse(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(scale = transform.scale.copy(scaleX = transform.scaleX - 0.11f))
            )
        )

        assertTrue(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(scale = transform.scale.copy(scaleY = transform.scaleY + 0.1f))
            )
        )
        assertTrue(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(scale = transform.scale.copy(scaleY = transform.scaleY - 0.1f))
            )
        )
        assertFalse(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(scale = transform.scale.copy(scaleY = transform.scaleY + 0.11f))
            )
        )
        assertFalse(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(scale = transform.scale.copy(scaleY = transform.scaleY - 0.11f))
            )
        )

        assertTrue(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(offset = transform.offset.copy(x = transform.offsetX + 1.0f))
            )
        )
        assertTrue(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(offset = transform.offset.copy(x = transform.offsetX - 1.0f))
            )
        )
        assertFalse(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(offset = transform.offset.copy(x = transform.offsetX + 1.1f))
            )
        )
        assertFalse(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(offset = transform.offset.copy(x = transform.offsetX - 1.1f))
            )
        )

        assertTrue(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(offset = transform.offset.copy(y = transform.offsetY + 1.0f))
            )
        )
        assertTrue(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(offset = transform.offset.copy(y = transform.offsetY - 1.0f))
            )
        )
        assertFalse(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(offset = transform.offset.copy(y = transform.offsetY + 1.1f))
            )
        )
        assertFalse(
            actual = transformAboutEquals(
                one = transform,
                two = transform.copy(offset = transform.offset.copy(y = transform.offsetY - 1.1f))
            )
        )
    }

    @Test
    fun testCalculateSourceScaleFactor() {
        assertEquals(
            expected = ScaleFactorCompat.Origin,
            actual = calculateSourceScaleFactor(
                contentSize = IntSizeCompat(0, 1000),
                contentOriginSize = IntSizeCompat(1080, 1920),
                scale = ScaleFactorCompat.Origin
            )
        )
        assertEquals(
            expected = ScaleFactorCompat.Origin,
            actual = calculateSourceScaleFactor(
                contentSize = IntSizeCompat(1000, 0),
                contentOriginSize = IntSizeCompat(1080, 1920),
                scale = ScaleFactorCompat.Origin
            )
        )
        assertEquals(
            expected = ScaleFactorCompat.Origin,
            actual = calculateSourceScaleFactor(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(0, 1000),
                scale = ScaleFactorCompat.Origin
            )
        )
        assertEquals(
            expected = ScaleFactorCompat.Origin,
            actual = calculateSourceScaleFactor(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1000, 0),
                scale = ScaleFactorCompat.Origin
            )
        )

        assertEquals(
            expected = ScaleFactorCompat.Origin,
            actual = calculateSourceScaleFactor(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                scale = ScaleFactorCompat.Origin
            )
        )

        assertEquals(
            expected = ScaleFactorCompat(0.75f, 0.5f),
            actual = calculateSourceScaleFactor(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                scale = ScaleFactorCompat.Origin
            )
        )

        assertEquals(
            expected = ScaleFactorCompat(0.375f, 0.25f),
            actual = calculateSourceScaleFactor(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                scale = ScaleFactorCompat(0.5f, 0.5f)
            )
        )
    }

    @Test
    fun testCalculateSourceVisibleRect() {
        assertEquals(
            expected = RectCompat.Zero,
            actual = calculateSourceVisibleRect(
                contentSize = IntSizeCompat(0, 1000),
                contentOriginSize = IntSizeCompat(1080, 1920),
                contentVisibleRect = RectCompat(21f, 33f, 45f, 67f)
            )
        )
        assertEquals(
            expected = RectCompat.Zero,
            actual = calculateSourceVisibleRect(
                contentSize = IntSizeCompat(1000, 0),
                contentOriginSize = IntSizeCompat(1080, 1920),
                contentVisibleRect = RectCompat(21f, 33f, 45f, 67f)
            )
        )
        assertEquals(
            expected = RectCompat.Zero,
            actual = calculateSourceVisibleRect(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(0, 1000),
                contentVisibleRect = RectCompat(21f, 33f, 45f, 67f)
            )
        )
        assertEquals(
            expected = RectCompat.Zero,
            actual = calculateSourceVisibleRect(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1000, 0),
                contentVisibleRect = RectCompat(21f, 33f, 45f, 67f)
            )
        )

        assertEquals(
            expected = RectCompat(21f, 33f, 45f, 67f),
            actual = calculateSourceVisibleRect(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                contentVisibleRect = RectCompat(21f, 33f, 45f, 67f)
            )
        )

        assertEquals(
            expected = RectCompat(28f, 66f, 60f, 134f),
            actual = calculateSourceVisibleRect(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                contentVisibleRect = RectCompat(21f, 33f, 45f, 67f)
            )
        )
    }

    @Test
    fun testPointSourceToDraw() {
        assertEquals(
            expected = "505.0x333.0",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(0, 1000),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 0,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )
        assertEquals(
            expected = "505.0x333.0",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1000, 0),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 0,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )
        assertEquals(
            expected = "505.0x333.0",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(0, 1000),
                rotation = 0,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )
        assertEquals(
            expected = "505.0x333.0",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1000, 0),
                rotation = 0,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )

        // rotation 0
        assertEquals(
            expected = "505.0x333.0",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 0,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )
        assertEquals(
            expected = "378.75x166.5",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 0,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )
        assertEquals(
            expected = "189.38x83.25",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 0,
                scale = ScaleFactorCompat(0.5f, 0.5f),
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )
        assertEquals(
            expected = "289.38x283.25",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 0,
                scale = ScaleFactorCompat(0.5f, 0.5f),
                offset = OffsetCompat(100f, 200f),
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )

        // rotation 90
        assertEquals(
            expected = "1167.0x925.0",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 90,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )
        assertEquals(
            expected = "718.5x453.75",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 90,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )
        assertEquals(
            expected = "359.25x226.88",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 90,
                scale = ScaleFactorCompat(0.5f, 0.5f),
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )
        assertEquals(
            expected = "459.25x426.88",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 90,
                scale = ScaleFactorCompat(0.5f, 0.5f),
                offset = OffsetCompat(100f, 200f),
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )

        // rotation 180
        assertEquals(
            expected = "575.0x1587.0",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 180,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )

        // rotation 270
        assertEquals(
            expected = "-87.0x995.0",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 270,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )

        // rotation 360
        assertEquals(
            expected = "505.0x333.0",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 360,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            ).toShortString()
        )

        assertFailsWith(IllegalArgumentException::class) {
            sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 1,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            )
        }
        assertFailsWith(IllegalArgumentException::class) {
            sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 89,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            )
        }
        assertFailsWith(IllegalArgumentException::class) {
            sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 91,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            )
        }
        assertFailsWith(IllegalArgumentException::class) {
            sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 269,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            )
        }
        assertFailsWith(IllegalArgumentException::class) {
            sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 271,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                point = OffsetCompat(505f, 333f)
            )
        }
    }

    @Test
    fun testRectSourceToDraw() {
        assertEquals(
            expected = "[505.0x333.0,707.0x444.0]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(0, 1000),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 0,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )
        assertEquals(
            expected = "[505.0x333.0,707.0x444.0]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1000, 0),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 0,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )
        assertEquals(
            expected = "[505.0x333.0,707.0x444.0]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(0, 1000),
                rotation = 0,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )
        assertEquals(
            expected = "[505.0x333.0,707.0x444.0]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1000, 0),
                rotation = 0,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )

        // rotation 0
        assertEquals(
            expected = "[505.0x333.0,707.0x444.0]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 0,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )
        assertEquals(
            expected = "[378.75x166.5,530.25x222.0]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 0,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )
        assertEquals(
            expected = "[189.38x83.25,265.12x111.0]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 0,
                scale = ScaleFactorCompat(0.5f, 0.5f),
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )
        assertEquals(
            expected = "[289.38x283.25,365.12x311.0]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 0,
                scale = ScaleFactorCompat(0.5f, 0.5f),
                offset = OffsetCompat(100f, 200f),
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )

        // rotation 90
        assertEquals(
            expected = "[1167.0x925.0,1056.0x1127.0]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 90,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )
        assertEquals(
            expected = "[718.5x453.75,663.0x605.25]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 90,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )
        assertEquals(
            expected = "[359.25x226.88,331.5x302.62]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 90,
                scale = ScaleFactorCompat(0.5f, 0.5f),
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )
        assertEquals(
            expected = "[459.25x426.88,431.5x502.62]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920).times(ScaleFactorCompat(0.75f, 0.5f)),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 90,
                scale = ScaleFactorCompat(0.5f, 0.5f),
                offset = OffsetCompat(100f, 200f),
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )

        // rotation 180
        assertEquals(
            expected = "[575.0x1587.0,373.0x1476.0]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 180,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )

        // rotation 270
        assertEquals(
            expected = "[-87.0x995.0,24.0x793.0]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 270,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )

        // rotation 360
        assertEquals(
            expected = "[505.0x333.0,707.0x444.0]",
            actual = sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 360,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            ).toShortString()
        )

        assertFailsWith(IllegalArgumentException::class) {
            sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 1,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            )
        }
        assertFailsWith(IllegalArgumentException::class) {
            sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 89,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            )
        }
        assertFailsWith(IllegalArgumentException::class) {
            sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 91,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            )
        }
        assertFailsWith(IllegalArgumentException::class) {
            sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 269,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            )
        }
        assertFailsWith(IllegalArgumentException::class) {
            sourceToDraw(
                contentSize = IntSizeCompat(1080, 1920),
                contentOriginSize = IntSizeCompat(1080, 1920),
                rotation = 271,
                scale = ScaleFactorCompat.Origin,
                offset = OffsetCompat.Zero,
                rect = RectCompat(505f, 333f, 707f, 444f)
            )
        }
    }
}