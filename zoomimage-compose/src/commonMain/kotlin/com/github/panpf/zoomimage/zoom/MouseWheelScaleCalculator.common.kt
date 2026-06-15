/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.zoom

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign

/**
 * Calculate the scaling factor based on the increment of the mouse wheel scroll
 */
fun interface MouseWheelScaleCalculator {

    /**
     * Calculate the new scale based on the scroll delta and current scale.
     */
    fun calculateScale(
        currentScale: Float,
        scrollDelta: Float,
    ): Float

    companion object {
        /**
         * Default implementation of [MouseWheelScaleCalculator].
         *
         * @see com.github.panpf.zoomimage.compose.common.test.zoom.MouseWheelScaleCalculatorTest.testDefault
         */
        val Default: MouseWheelScaleCalculator = DefaultMouseWheelScaleCalculator()
    }
}

/**
 * Default implementation of [MouseWheelScaleCalculator] that provides a smooth and intuitive
 * zooming experience based on the mouse wheel scroll delta.
 * It normalizes the scroll delta using a platform-specific multiplier and applies a
 * scaling sensitivity to calculate the new scale factor.
 */
data class DefaultMouseWheelScaleCalculator(
    val mouseWheelScrollMultiplier: Float = platformMouseWheelScrollMultiplier(),
    val scalingSensitivity: Float = 0.4f,
) : MouseWheelScaleCalculator {

    override fun calculateScale(currentScale: Float, scrollDelta: Float): Float {
        val normalizedScrollDelta = scrollDelta / mouseWheelScrollMultiplier
        val scaleMultiplier = calculateScaleMultiplier(normalizedScrollDelta, scalingSensitivity)
        val newScale = currentScale * scaleMultiplier
        return newScale
    }

    /**
     * Convert the mouse wheel's scrollDelta to a smooth zoom increment factor
     * @param scrollDelta System scrolling increment
     * @param scalingSensitivity Scaling sensitivity, the larger the value, the more sensitive it is
     * @return Scaling factor (e.g., returns 1.05 when zoomed in, and 0.95 when zoomed out)
     */
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

/**
 * Generally, the js platform scrolls faster than the desktop platform, about 30 times that of the desktop platform, so this function is used to provide a platform-related scroll increment multiplier to make the scroll increment of the js platform more reasonable.
 */
expect fun platformMouseWheelScrollMultiplier(): Float