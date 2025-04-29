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

import kotlin.math.absoluteValue

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
 * Calculate the scroll wheel scaling according to the step principle. The [stepScrollDelta] distance increases or decreases the [stepScaleFactor] scaling ratio for each scroll.
 */
data class DefaultMouseWheelScaleCalculator(
    val stepScrollDelta: Float = platformMouseWheelStepScrollDelta(),
    val stepScaleFactor: Float = 0.3f,
) : MouseWheelScaleCalculator {

    override fun calculateScale(currentScale: Float, scrollDelta: Float): Float {
        val multiple = scrollDelta.absoluteValue / stepScrollDelta
        val absAddScale = 1f + (multiple * stepScaleFactor)
        val finalAddScale = if (scrollDelta < 0) absAddScale else 1f / absAddScale
        return currentScale * finalAddScale
    }
}

/**
 * The distance of the mouse wheel scroll step, which is used to calculate the scaling ratio of the mouse wheel scroll.
 */
expect fun platformMouseWheelStepScrollDelta(): Float