package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.copy
import com.github.panpf.zoomimage.util.plus
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.calculateContentVisibleRect
import com.github.panpf.zoomimage.zoom.calculateInitialZoom
import com.github.panpf.zoomimage.zoom.calculateRestoreContentVisibleCenterUserTransform
import com.github.panpf.zoomimage.zoom.checkParamsChanges
import com.github.panpf.zoomimage.zoom.transformAboutEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ZoomsTest5 {

    @Test
    fun testCheckParamsChanges() {
        val containerSize = IntSizeCompat(800, 572)
        val contentSize = IntSizeCompat(6799, 4882)
        val contentOriginSize = IntSizeCompat.Zero
        val contentScale = ContentScaleCompat.Fit
        val alignment = AlignmentCompat.Center
        val rotation = 0
        val readMode: ReadMode? = null
        val scalesCalculator = ScalesCalculator.Dynamic
        val limitOffsetWithinBaseVisibleRect = false
        val containerWhitespace = ContainerWhitespace.Zero

        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize,
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(0, this)
        }
        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize.copy(),
            lastContentSize = contentSize.copy(),
            lastContentOriginSize = contentOriginSize.copy(),
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(0, this)
        }

        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize.copy(width = containerSize.width + 1),
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(1, this)
        }
        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize.copy(height = containerSize.height + 1),
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(1, this)
        }

        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize.copy(width = containerSize.width + 1),
            lastContentSize = contentSize.copy(width = contentSize.width + 1),
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(-1, this)
        }
        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize.copy(width = containerSize.width + 1),
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize.copy(width = contentOriginSize.width + 1),
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(-1, this)
        }
        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize.copy(width = containerSize.width + 1),
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = ContentScaleCompat.Inside,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(-1, this)
        }
        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize.copy(width = containerSize.width + 1),
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = AlignmentCompat.BottomEnd,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(-1, this)
        }
        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize.copy(width = containerSize.width + 1),
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation + 90,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(-1, this)
        }
        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize.copy(width = containerSize.width + 1),
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = ReadMode(ReadMode.SIZE_TYPE_HORIZONTAL),
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(-1, this)
        }

        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize.copy(width = containerSize.width + 1),
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = ScalesCalculator.Fixed,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(-1, this)
        }

        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize.copy(width = containerSize.width + 1),
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = true,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(-1, this)
        }

        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize.copy(width = containerSize.width + 1),
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = ContainerWhitespace(
                horizontal = containerSize.width * 0.5f,
                vertical = containerSize.height * 0.5f
            ),
        ).apply {
            assertEquals(-1, this)
        }

        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = IntSizeCompat.Zero,
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(-1, this)
        }

        checkParamsChanges(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize,
            lastContentSize = IntSizeCompat.Zero,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(-1, this)
        }

        checkParamsChanges(
            containerSize = IntSizeCompat.Zero,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize,
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(-1, this)
        }

        checkParamsChanges(
            containerSize = containerSize,
            contentSize = IntSizeCompat.Zero,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator,
            limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            containerWhitespace = containerWhitespace,
            lastContainerSize = containerSize,
            lastContentSize = contentSize,
            lastContentOriginSize = contentOriginSize,
            lastContentScale = contentScale,
            lastAlignment = alignment,
            lastRotation = rotation,
            lastReadMode = readMode,
            lastScalesCalculator = scalesCalculator,
            lastLimitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
            lastContainerWhitespace = containerWhitespace,
        ).apply {
            assertEquals(-1, this)
        }
    }

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
                lastContentVisibleCenter = contentVisibleCenterPoint.round(),
            )

            val newContentVisibleCenterPoint = calculateContentVisibleRect(
                containerSize = newContainerSize,
                contentSize = contentSize,
                contentScale = contentScale,
                alignment = alignment,
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
}