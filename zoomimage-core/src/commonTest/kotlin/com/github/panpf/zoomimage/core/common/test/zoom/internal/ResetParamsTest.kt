package com.github.panpf.zoomimage.core.common.test.zoom.internal

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.internal.ResetParams
import com.github.panpf.zoomimage.zoom.internal.diff
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ResetParamsTest {

    @Test
    fun testEqualsAndHashCode() {
        val params = ResetParams(
            containerSize = IntSizeCompat(1000, 2000),
            contentSize = IntSizeCompat(200, 100),
            contentOriginSize = IntSizeCompat(800, 400),
            rotation = 0,
            contentScale = ContentScaleCompat.Fit,
            alignment = AlignmentCompat.Center,
            readMode = null,
            scalesCalculator = ScalesCalculator.Fixed,
            limitOffsetWithinBaseVisibleRect = true,
            containerWhitespaceMultiple = 1f,
            containerWhitespace = ContainerWhitespace.Zero
        )
        val params1 = params.copy()
        val params2 = params.copy(containerSize = IntSizeCompat(2000, 5000))
        val params3 = params.copy(contentSize = IntSizeCompat(300, 200))
        val params4 = params.copy(contentOriginSize = IntSizeCompat(1600, 800))
        val params5 = params.copy(rotation = 90)
        val params6 = params.copy(contentScale = ContentScaleCompat.Crop)
        val params7 = params.copy(alignment = AlignmentCompat.TopStart)
        val params8 = params.copy(readMode = ReadMode.Default)
        val params9 = params.copy(scalesCalculator = ScalesCalculator.dynamic(10f))
        val params10 = params.copy(limitOffsetWithinBaseVisibleRect = false)
        val params11 = params.copy(containerWhitespaceMultiple = 2f)
        val params12 = params.copy(containerWhitespace = ContainerWhitespace(5f))

        assertEquals(expected = params, actual = params1)
        assertNotEquals(illegal = params, actual = params2)
        assertNotEquals(illegal = params, actual = params3)
        assertNotEquals(illegal = params, actual = params4)
        assertNotEquals(illegal = params, actual = params5)
        assertNotEquals(illegal = params, actual = params6)
        assertNotEquals(illegal = params, actual = params7)
        assertNotEquals(illegal = params, actual = params8)
        assertNotEquals(illegal = params, actual = params9)
        assertNotEquals(illegal = params, actual = params10)
        assertNotEquals(illegal = params, actual = params11)
        assertNotEquals(illegal = params, actual = params12)

        assertEquals(expected = params.hashCode(), actual = params1.hashCode())
        assertNotEquals(illegal = params.hashCode(), actual = params2.hashCode())
        assertNotEquals(illegal = params.hashCode(), actual = params3.hashCode())
        assertNotEquals(illegal = params.hashCode(), actual = params4.hashCode())
        assertNotEquals(illegal = params.hashCode(), actual = params5.hashCode())
        assertNotEquals(illegal = params.hashCode(), actual = params6.hashCode())
        assertNotEquals(illegal = params.hashCode(), actual = params7.hashCode())
        assertNotEquals(illegal = params.hashCode(), actual = params8.hashCode())
        assertNotEquals(illegal = params.hashCode(), actual = params9.hashCode())
        assertNotEquals(illegal = params.hashCode(), actual = params10.hashCode())
        assertNotEquals(illegal = params.hashCode(), actual = params11.hashCode())
        assertNotEquals(illegal = params.hashCode(), actual = params12.hashCode())
    }

    @Test
    fun testToString() {
        val params = ResetParams(
            containerSize = IntSizeCompat(1000, 2000),
            contentSize = IntSizeCompat(200, 100),
            contentOriginSize = IntSizeCompat(800, 400),
            rotation = 0,
            contentScale = ContentScaleCompat.Fit,
            alignment = AlignmentCompat.Center,
            readMode = null,
            scalesCalculator = ScalesCalculator.Fixed,
            limitOffsetWithinBaseVisibleRect = true,
            containerWhitespaceMultiple = 1f,
            containerWhitespace = ContainerWhitespace.Zero
        )
        assertEquals(
            expected = "ResetParams(containerSize=1000 x 2000, contentSize=200 x 100, contentOriginSize=800 x 400, rotation=0, contentScale=${ContentScaleCompat.Fit}, alignment=BiasAlignmentCompat(horizontalBias=0.0, verticalBias=0.0), readMode=null, scalesCalculator=FixedScalesCalculator(multiple=3.0), limitOffsetWithinBaseVisibleRect=true, containerWhitespaceMultiple=1.0, containerWhitespace=ContainerWhitespace(left=0.0, top=0.0, right=0.0, bottom=0.0))",
            actual = params.toString()
        )
    }

    @Test
    fun testDiffSingleAttributeChanged() {
        val params = ResetParams(
            containerSize = IntSizeCompat(1000, 2000),
            contentSize = IntSizeCompat(200, 100),
            contentOriginSize = IntSizeCompat(800, 400),
            rotation = 0,
            contentScale = ContentScaleCompat.Fit,
            alignment = AlignmentCompat.Center,
            readMode = null,
            scalesCalculator = ScalesCalculator.Fixed,
            limitOffsetWithinBaseVisibleRect = true,
            containerWhitespaceMultiple = 1f,
            containerWhitespace = ContainerWhitespace.Zero
        )
        val params1 = params.copy()
        val params2 = params.copy(containerSize = IntSizeCompat(2000, 5000))
        val params3 = params.copy(contentSize = IntSizeCompat(300, 200))
        val params4 = params.copy(contentOriginSize = IntSizeCompat(1600, 800))
        val params5 = params.copy(rotation = 90)
        val params6 = params.copy(contentScale = ContentScaleCompat.Crop)
        val params7 = params.copy(alignment = AlignmentCompat.TopStart)
        val params8 = params.copy(readMode = ReadMode.Default)
        val params9 = params.copy(scalesCalculator = ScalesCalculator.dynamic(10f))
        val params10 = params.copy(limitOffsetWithinBaseVisibleRect = false)
        val params11 = params.copy(containerWhitespaceMultiple = 2f)
        val params12 = params.copy(containerWhitespace = ContainerWhitespace(5f))

        params.diff(params1).apply {
            assertEquals(expected = true, actual = isNotChanged)
            assertEquals(expected = false, actual = isContainerSizeChanged)
            assertEquals(expected = false, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult()", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params2).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = true, actual = isContainerSizeChanged)
            assertEquals(expected = false, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(containerSize)", actual = toString())
            assertEquals(expected = true, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params3).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = false, actual = isContainerSizeChanged)
            assertEquals(expected = true, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(contentSize)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = true, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = true, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params4).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = false, actual = isContainerSizeChanged)
            assertEquals(expected = false, actual = isContentSizeChanged)
            assertEquals(expected = true, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(contentOriginSize)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = true, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = true, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params5).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = false, actual = isContainerSizeChanged)
            assertEquals(expected = false, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = true, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(rotation)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params6).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = false, actual = isContainerSizeChanged)
            assertEquals(expected = false, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = true, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(contentScale)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params7).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = false, actual = isContainerSizeChanged)
            assertEquals(expected = false, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = true, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(alignment)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params8).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = false, actual = isContainerSizeChanged)
            assertEquals(expected = false, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = true, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(readMode)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params9).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = false, actual = isContainerSizeChanged)
            assertEquals(expected = false, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = true, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(scalesCalculator)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params10).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = false, actual = isContainerSizeChanged)
            assertEquals(expected = false, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = true, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(
                expected = "ResetParamsDiffResult(limitOffsetWithinBaseVisibleRect)",
                actual = toString()
            )
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params11).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = false, actual = isContainerSizeChanged)
            assertEquals(expected = false, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = true, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(
                expected = "ResetParamsDiffResult(containerWhitespaceMultiple)",
                actual = toString()
            )
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params12).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = false, actual = isContainerSizeChanged)
            assertEquals(expected = false, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = true, actual = isContainerWhitespaceChanged)
            assertEquals(
                expected = "ResetParamsDiffResult(containerWhitespace)",
                actual = toString()
            )
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
    }

    @Test
    fun testDiffMultipleAttributeChanged() {
        val params = ResetParams(
            containerSize = IntSizeCompat(1000, 2000),
            contentSize = IntSizeCompat(200, 100),
            contentOriginSize = IntSizeCompat(800, 400),
            rotation = 0,
            contentScale = ContentScaleCompat.Fit,
            alignment = AlignmentCompat.Center,
            readMode = null,
            scalesCalculator = ScalesCalculator.Fixed,
            limitOffsetWithinBaseVisibleRect = true,
            containerWhitespaceMultiple = 1f,
            containerWhitespace = ContainerWhitespace.Zero
        )
        val params1 = params.copy()
        val params2 = params1.copy(containerSize = IntSizeCompat(2000, 5000))
        val params3 = params2.copy(contentSize = IntSizeCompat(300, 200))
        val params4 = params3.copy(contentOriginSize = IntSizeCompat(1600, 800))
        val params5 = params4.copy(rotation = 90)
        val params6 = params5.copy(contentScale = ContentScaleCompat.Crop)
        val params7 = params6.copy(alignment = AlignmentCompat.TopStart)
        val params8 = params7.copy(readMode = ReadMode.Default)
        val params9 = params8.copy(scalesCalculator = ScalesCalculator.dynamic(10f))
        val params10 = params9.copy(limitOffsetWithinBaseVisibleRect = false)
        val params11 = params10.copy(containerWhitespaceMultiple = 2f)
        val params12 = params11.copy(containerWhitespace = ContainerWhitespace(5f))
        params.diff(params1).apply {
            assertEquals(expected = true, actual = isNotChanged)
            assertEquals(expected = false, actual = isContainerSizeChanged)
            assertEquals(expected = false, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult()", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params2).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = true, actual = isContainerSizeChanged)
            assertEquals(expected = false, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(containerSize)", actual = toString())
            assertEquals(expected = true, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params3).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = true, actual = isContainerSizeChanged)
            assertEquals(expected = true, actual = isContentSizeChanged)
            assertEquals(expected = false, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(containerSize, contentSize)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params4).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = true, actual = isContainerSizeChanged)
            assertEquals(expected = true, actual = isContentSizeChanged)
            assertEquals(expected = true, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(containerSize, contentSize, contentOriginSize)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params5).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = true, actual = isContainerSizeChanged)
            assertEquals(expected = true, actual = isContentSizeChanged)
            assertEquals(expected = true, actual = isContentOriginSizeChanged)
            assertEquals(expected = true, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(containerSize, contentSize, contentOriginSize, rotation)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params6).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = true, actual = isContainerSizeChanged)
            assertEquals(expected = true, actual = isContentSizeChanged)
            assertEquals(expected = true, actual = isContentOriginSizeChanged)
            assertEquals(expected = true, actual = isRotationChanged)
            assertEquals(expected = true, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(containerSize, contentSize, contentOriginSize, rotation, contentScale)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params7).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = true, actual = isContainerSizeChanged)
            assertEquals(expected = true, actual = isContentSizeChanged)
            assertEquals(expected = true, actual = isContentOriginSizeChanged)
            assertEquals(expected = true, actual = isRotationChanged)
            assertEquals(expected = true, actual = isContentScaleChanged)
            assertEquals(expected = true, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(containerSize, contentSize, contentOriginSize, rotation, contentScale, alignment)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params8).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = true, actual = isContainerSizeChanged)
            assertEquals(expected = true, actual = isContentSizeChanged)
            assertEquals(expected = true, actual = isContentOriginSizeChanged)
            assertEquals(expected = true, actual = isRotationChanged)
            assertEquals(expected = true, actual = isContentScaleChanged)
            assertEquals(expected = true, actual = isAlignmentChanged)
            assertEquals(expected = true, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(containerSize, contentSize, contentOriginSize, rotation, contentScale, alignment, readMode)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params9).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = true, actual = isContainerSizeChanged)
            assertEquals(expected = true, actual = isContentSizeChanged)
            assertEquals(expected = true, actual = isContentOriginSizeChanged)
            assertEquals(expected = true, actual = isRotationChanged)
            assertEquals(expected = true, actual = isContentScaleChanged)
            assertEquals(expected = true, actual = isAlignmentChanged)
            assertEquals(expected = true, actual = isReadModeChanged)
            assertEquals(expected = true, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(containerSize, contentSize, contentOriginSize, rotation, contentScale, alignment, readMode, scalesCalculator)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params10).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = true, actual = isContainerSizeChanged)
            assertEquals(expected = true, actual = isContentSizeChanged)
            assertEquals(expected = true, actual = isContentOriginSizeChanged)
            assertEquals(expected = true, actual = isRotationChanged)
            assertEquals(expected = true, actual = isContentScaleChanged)
            assertEquals(expected = true, actual = isAlignmentChanged)
            assertEquals(expected = true, actual = isReadModeChanged)
            assertEquals(expected = true, actual = isScalesCalculatorChanged)
            assertEquals(expected = true, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(containerSize, contentSize, contentOriginSize, rotation, contentScale, alignment, readMode, scalesCalculator, limitOffsetWithinBaseVisibleRect)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params11).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = true, actual = isContainerSizeChanged)
            assertEquals(expected = true, actual = isContentSizeChanged)
            assertEquals(expected = true, actual = isContentOriginSizeChanged)
            assertEquals(expected = true, actual = isRotationChanged)
            assertEquals(expected = true, actual = isContentScaleChanged)
            assertEquals(expected = true, actual = isAlignmentChanged)
            assertEquals(expected = true, actual = isReadModeChanged)
            assertEquals(expected = true, actual = isScalesCalculatorChanged)
            assertEquals(expected = true, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = true, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(containerSize, contentSize, contentOriginSize, rotation, contentScale, alignment, readMode, scalesCalculator, limitOffsetWithinBaseVisibleRect, containerWhitespaceMultiple)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
        params.diff(params12).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = true, actual = isContainerSizeChanged)
            assertEquals(expected = true, actual = isContentSizeChanged)
            assertEquals(expected = true, actual = isContentOriginSizeChanged)
            assertEquals(expected = true, actual = isRotationChanged)
            assertEquals(expected = true, actual = isContentScaleChanged)
            assertEquals(expected = true, actual = isAlignmentChanged)
            assertEquals(expected = true, actual = isReadModeChanged)
            assertEquals(expected = true, actual = isScalesCalculatorChanged)
            assertEquals(expected = true, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = true, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = true, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(containerSize, contentSize, contentOriginSize, rotation, contentScale, alignment, readMode, scalesCalculator, limitOffsetWithinBaseVisibleRect, containerWhitespaceMultiple, containerWhitespace)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
    }

    @Test
    fun testOnlyContentSizeOrContentOriginSizeChanged() {
        val params = ResetParams(
            containerSize = IntSizeCompat(1000, 2000),
            contentSize = IntSizeCompat(200, 100),
            contentOriginSize = IntSizeCompat(800, 400),
            rotation = 0,
            contentScale = ContentScaleCompat.Fit,
            alignment = AlignmentCompat.Center,
            readMode = null,
            scalesCalculator = ScalesCalculator.Fixed,
            limitOffsetWithinBaseVisibleRect = true,
            containerWhitespaceMultiple = 1f,
            containerWhitespace = ContainerWhitespace.Zero
        )
        val params1 = params.copy(contentSize = IntSizeCompat(400, 300), contentOriginSize = IntSizeCompat(120, 900))
        params.diff(params1).apply {
            assertEquals(expected = false, actual = isNotChanged)
            assertEquals(expected = false, actual = isContainerSizeChanged)
            assertEquals(expected = true, actual = isContentSizeChanged)
            assertEquals(expected = true, actual = isContentOriginSizeChanged)
            assertEquals(expected = false, actual = isRotationChanged)
            assertEquals(expected = false, actual = isContentScaleChanged)
            assertEquals(expected = false, actual = isAlignmentChanged)
            assertEquals(expected = false, actual = isReadModeChanged)
            assertEquals(expected = false, actual = isScalesCalculatorChanged)
            assertEquals(expected = false, actual = isLimitOffsetWithinBaseVisibleRectChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceMultipleChanged)
            assertEquals(expected = false, actual = isContainerWhitespaceChanged)
            assertEquals(expected = "ResetParamsDiffResult(contentSize, contentOriginSize)", actual = toString())
            assertEquals(expected = false, actual = isOnlyContainerSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentSizeChanged)
            assertEquals(expected = false, actual = isOnlyContentOriginSizeChanged)
            assertEquals(expected = true, actual = isOnlyContentSizeOrContentOriginSizeChanged)
        }
    }
}