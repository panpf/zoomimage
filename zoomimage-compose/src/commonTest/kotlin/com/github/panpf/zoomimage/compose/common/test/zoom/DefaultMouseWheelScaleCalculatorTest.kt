package com.github.panpf.zoomimage.compose.common.test.zoom

import com.github.panpf.zoomimage.zoom.DefaultMouseWheelScaleCalculator
import com.github.panpf.zoomimage.zoom.platformMouseWheelStepScrollDelta
import kotlin.math.absoluteValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DefaultMouseWheelScaleCalculatorTest {

    @Test
    fun testConstructor() {
        DefaultMouseWheelScaleCalculator().apply {
            assertEquals(expected = platformMouseWheelStepScrollDelta(), actual = stepScrollDelta)
            assertEquals(expected = 0.3f, actual = stepScaleFactor)
        }
    }

    @Test
    fun testCalculateScale() {
        val calculator = DefaultMouseWheelScaleCalculator()
        assertEquals(
            expected = 1f / ((5f).absoluteValue / calculator.stepScrollDelta).times(calculator.stepScaleFactor)
                .plus(1f),
            actual = calculator.calculateScale(currentScale = 1f, scrollDelta = 5f)
        )
        assertEquals(
            expected = ((-5f).absoluteValue / calculator.stepScrollDelta).times(calculator.stepScaleFactor)
                .plus(1f),
            actual = calculator.calculateScale(currentScale = 1f, scrollDelta = -5f)
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = DefaultMouseWheelScaleCalculator()
        val element11 = element1.copy()
        val element2 = element1.copy(stepScrollDelta = 1f)
        val element3 = element1.copy(stepScaleFactor = 2f)
        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())
        assertNotEquals(illegal = element1, actual = element2)
        assertNotEquals(illegal = element1, actual = element3)
        assertNotEquals(illegal = element2, actual = element3)

        assertEquals(expected = element1.hashCode(), actual = element1.hashCode())
        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element2.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element3.hashCode())
        assertNotEquals(illegal = element2.hashCode(), actual = element3.hashCode())
    }

    @Test
    fun testToString() {
        val element = DefaultMouseWheelScaleCalculator()
        assertEquals(
            expected = "DefaultMouseWheelScaleCalculator(stepScrollDelta=${element.stepScrollDelta}, stepScaleFactor=${element.stepScaleFactor})",
            actual = element.toString()
        )
    }
}