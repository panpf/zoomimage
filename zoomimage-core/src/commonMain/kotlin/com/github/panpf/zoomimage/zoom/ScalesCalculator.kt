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

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.zoom.ScalesCalculator.Companion.MULTIPLE

/**
 * Used to calculate mediumScale and maxScale
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ScalesCalculatorTest
 */
interface ScalesCalculator {

    fun calculate(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentOriginSize: IntSizeCompat,
        contentScale: ContentScaleCompat,
        minScale: Float,
        initialScale: Float,
    ): Result

    companion object {
        /**
         * The default multiplier between the scales, because by default `mediumScale = minScale * multiple`,
         * `maxScale = mediumScale * multiple`
         */
        const val MULTIPLE = 3f

        /**
         * Dynamic scales calculator based on content size, content raw size, and container size
         */
        val Dynamic = DynamicScalesCalculator()

        /**
         * Fixed scales calculator, always 'mediumScale = minScale * multiple', 'maxScale = mediumScale * multiple'
         */
        val Fixed = FixedScalesCalculator()

        /**
         * Creates a [DynamicScalesCalculator] and specified [multiple]
         */
        fun dynamic(multiple: Float = MULTIPLE): DynamicScalesCalculator =
            DynamicScalesCalculator(multiple)

        /**
         * Creates a [FixedScalesCalculator] and specified [multiple]
         */
        fun fixed(multiple: Float = MULTIPLE): FixedScalesCalculator =
            FixedScalesCalculator(multiple)
    }

    data class Result(val minScale: Float, val mediumScale: Float, val maxScale: Float) {
        override fun toString(): String {
            return "Result(" +
                    "minScale=${minScale.format(2)}, " +
                    "mediumScale=${mediumScale.format(2)}, " +
                    "maxScale=${maxScale.format(2)})"
        }
    }
}

/**
 * Dynamic scales calculator based on content size, content raw size, and container size
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ScalesCalculatorTest.testDynamic
 */
data class DynamicScalesCalculator(
    /**
     * The multiplier between the scales, because by default `mediumScale = minScale * multiple`,
     * `maxScale = mediumScale * multiple`
     */
    val multiple: Float = MULTIPLE,
) : ScalesCalculator {

    override fun calculate(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentOriginSize: IntSizeCompat,
        contentScale: ContentScaleCompat,
        minScale: Float,
        initialScale: Float,
    ): ScalesCalculator.Result {
        val minMediumScale = minScale * multiple
        val mediumScale: Float
        val maxScale: Float
        if (contentScale == ContentScaleCompat.FillBounds) {
            mediumScale = minMediumScale
            maxScale = mediumScale * multiple
        } else {
            // Enlarge content to the same proportions as original size
            val contentOriginScale = if (contentOriginSize.isNotEmpty()) {
                // Sometimes there will be a slight difference in the original scaling ratio of width and height, so take the larger one
                val widthScale = contentOriginSize.width / contentSize.width.toFloat()
                val heightScale = contentOriginSize.height / contentSize.height.toFloat()
                maxOf(widthScale, heightScale)
            } else {
                1.0f
            }
            mediumScale = if (initialScale > minScale) {
                initialScale    // initialScale is usually determined by the ReadMode, so initialScale takes precedence
            } else {
                // The width and height of content fill the container at the same time
                val widthScale = containerSize.width / contentSize.width.toFloat()
                val heightScale = containerSize.height / contentSize.height.toFloat()
                val fillContainerScale = maxOf(widthScale, heightScale)
                maxOf(minMediumScale, fillContainerScale, contentOriginScale)
            }
            maxScale = maxOf(mediumScale * multiple, contentOriginScale)
        }
        return ScalesCalculator.Result(
            minScale = minScale,
            mediumScale = mediumScale,
            maxScale = maxScale
        )
    }

    override fun toString(): String {
        return "DynamicScalesCalculator(multiple=${multiple.format(2)})"
    }
}


/**
 * Fixed scales calculator, always 'mediumScale = minScale * multiple', 'maxScale = mediumScale * multiple'
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ScalesCalculatorTest.testFixed
 */
data class FixedScalesCalculator(
    /**
     * The multiplier between the scales, because by default `mediumScale = minScale * multiple`,
     * `maxScale = mediumScale * multiple`
     */
    val multiple: Float = MULTIPLE
) : ScalesCalculator {

    override fun calculate(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentOriginSize: IntSizeCompat,
        contentScale: ContentScaleCompat,
        minScale: Float,
        initialScale: Float,
    ): ScalesCalculator.Result {
        // initialScale is usually determined by the ReadMode, so initialScale takes precedence
        val mediumScale = if (contentScale == ContentScaleCompat.FillBounds) {
            minScale * multiple
        } else if (initialScale > minScale) {
            initialScale
        } else {
            minScale * multiple
        }
        val maxScale = mediumScale * multiple
        return ScalesCalculator.Result(
            minScale = minScale,
            mediumScale = mediumScale,
            maxScale = maxScale
        )
    }

    override fun toString(): String {
        return "FixedScalesCalculator(multiple=${multiple.format(2)})"
    }
}