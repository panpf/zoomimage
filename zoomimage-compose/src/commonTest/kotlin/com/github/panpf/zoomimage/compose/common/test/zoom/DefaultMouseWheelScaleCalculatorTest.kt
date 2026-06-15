package com.github.panpf.zoomimage.compose.common.test.zoom

import com.github.panpf.zoomimage.zoom.DefaultMouseWheelScaleCalculator
import com.github.panpf.zoomimage.zoom.platformMouseWheelScrollMultiplier
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DefaultMouseWheelScaleCalculatorTest {

    @Test
    fun testConstructor() {
        DefaultMouseWheelScaleCalculator().apply {
            assertEquals(
                expected = platformMouseWheelScrollMultiplier(),
                actual = mouseWheelScrollMultiplier
            )
            assertEquals(expected = 0.4f, actual = scalingSensitivity)
        }

        DefaultMouseWheelScaleCalculator(
            mouseWheelScrollMultiplier = 4f,
            scalingSensitivity = 8f
        ).apply {
            assertEquals(expected = 4f, actual = mouseWheelScrollMultiplier)
            assertEquals(expected = 8f, actual = scalingSensitivity)
        }

        DefaultMouseWheelScaleCalculator(2f, 6f).apply {
            assertEquals(expected = 2f, actual = mouseWheelScrollMultiplier)
            assertEquals(expected = 6f, actual = scalingSensitivity)
        }
    }

    @Test
    fun testCalculateScale() {
        val calculator = DefaultMouseWheelScaleCalculator()
        assertEquals(
            expected = 1f * calculateScaleMultiplier(
                scrollDelta = 5f / calculator.mouseWheelScrollMultiplier,
                scalingSensitivity = calculator.scalingSensitivity
            ),
            actual = calculator.calculateScale(currentScale = 1f, scrollDelta = 5f)
        )
        assertEquals(
            expected = 1f * calculateScaleMultiplier(
                scrollDelta = -5f / calculator.mouseWheelScrollMultiplier,
                scalingSensitivity = calculator.scalingSensitivity
            ),
            actual = calculator.calculateScale(currentScale = 1f, scrollDelta = -5f)
        )

        val calculator2 = DefaultMouseWheelScaleCalculator(15f, 0.8f)
        assertEquals(
            expected = 1f * calculateScaleMultiplier(
                scrollDelta = 5f / calculator2.mouseWheelScrollMultiplier,
                scalingSensitivity = calculator2.scalingSensitivity
            ),
            actual = calculator2.calculateScale(currentScale = 1f, scrollDelta = 5f)
        )
        assertEquals(
            expected = 1f * calculateScaleMultiplier(
                scrollDelta = -5f / calculator2.mouseWheelScrollMultiplier,
                scalingSensitivity = calculator2.scalingSensitivity
            ),
            actual = calculator2.calculateScale(currentScale = 1f, scrollDelta = -5f)
        )
        assertNotEquals(
            illegal = calculator.calculateScale(currentScale = 1f, scrollDelta = 5f),
            actual = calculator2.calculateScale(currentScale = 1f, scrollDelta = 5f)
        )
        assertNotEquals(
            illegal = calculator.calculateScale(currentScale = 1f, scrollDelta = -5f),
            actual = calculator2.calculateScale(currentScale = 1f, scrollDelta = -5f)
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = DefaultMouseWheelScaleCalculator()
        val element11 = element1.copy()
        val element2 = element1.copy(mouseWheelScrollMultiplier = 8f)
        val element3 = element1.copy(scalingSensitivity = 2f)
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
            expected = "DefaultMouseWheelScaleCalculator(mouseWheelScrollMultiplier=${element.mouseWheelScrollMultiplier}, scalingSensitivity=${element.scalingSensitivity})",
            actual = element.toString()
        )
    }

    private fun calculateScaleMultiplier(scrollDelta: Float, scalingSensitivity: Float): Float {
        if (scrollDelta == 0f) return 1.0f

        // Scroll up to zoom in, scroll down to zoom out
        val direction = -scrollDelta.sign
        val absDelta = abs(scrollDelta)

        // Segment dynamic weight
        val adjustedDelta = if (absDelta <= 1.0f) {
            // When scrolling smoothly (e.g. 0.1, 0.4) keep it as is, ensuring silky smooth scaling for small increments.
            absDelta
        } else {
            // When the default scroll wheel or smooth scrolling has a large value (such as 4.9, 8.1), we give it a basic weight (1.0), plus reasonable compensation for the part exceeding 1.0, which not only retains the power of the "large value" but also prevents exponential explosion.
            1.0f + (absDelta - 1.0f) * 0.5f
        }

        // Basic scaling base (use 1.15 to provide a more obvious single-step scaling feeling)
        val base = 1.15f

        // Calculation of powers
        val exponent = direction * adjustedDelta * scalingSensitivity

        // Appropriately relax the single-step limit and allow the default scroll wheel to have a larger single-step span
        val scaleMultiplier = base.pow(exponent).coerceIn(0.5f, 2.0f)
        return scaleMultiplier
    }

}