/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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
import com.github.panpf.zoomimage.util.internal.format
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.zoom.ScalesCalculator.Companion.DifferencePercentage
import com.github.panpf.zoomimage.zoom.ScalesCalculator.Companion.Multiple
import kotlin.math.abs
import kotlin.math.max

/**
 * Used to calculate mediumScale and maxScale
 *
 * @see [com.github.panpf.zoomimage.core.test.zoom.ScalesCalculatorTest]
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
        const val Multiple = 3f

        /**
         * Participate in calculating mediumScale. If initialScale is greater than minScale and the difference between initialScale and mediumScale is less than mediumScale multiplied by differencePercentage, initialScale is used as the mediumScale
         */
        const val DifferencePercentage = 0.3f

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
        fun dynamic(
            multiple: Float = Multiple,
            differencePercentage: Float = DifferencePercentage
        ): DynamicScalesCalculator = DynamicScalesCalculator(multiple, differencePercentage)

        /**
         * Creates a [FixedScalesCalculator] and specified [multiple]
         */
        fun fixed(multiple: Float = Multiple): FixedScalesCalculator =
            FixedScalesCalculator(multiple)
    }

    data class Result(val mediumScale: Float, val maxScale: Float)
}

/**
 * Dynamic scales calculator based on content size, content raw size, and container size
 */
data class DynamicScalesCalculator(
    /**
     * The multiplier between the scales, because by default `mediumScale = minScale * multiple`,
     * `maxScale = mediumScale * multiple`
     */
    val multiple: Float = Multiple,

    /**
     * Participate in calculating mediumScale. If initialScale is greater than minScale and the difference between initialScale and mediumScale is less than mediumScale multiplied by differencePercentage, initialScale is used as the mediumScale
     */
    val differencePercentage: Float = DifferencePercentage,
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
        val coarseMediumScale = if (contentScale != ContentScaleCompat.FillBounds) {
            // The width and height of content fill the container at the same time
            val fillContainerScale = max(
                containerSize.width / contentSize.width.toFloat(),
                containerSize.height / contentSize.height.toFloat()
            )
            // Enlarge content to the same size as its original
            val contentOriginScale = if (contentOriginSize.isNotEmpty()) {
                // Sometimes there will be a slight difference in the original scaling ratio of width and height, so take the larger one
                val widthScale = contentOriginSize.width / contentSize.width.toFloat()
                val heightScale = contentOriginSize.height / contentSize.height.toFloat()
                max(widthScale, heightScale)
            } else {
                1.0f
            }
            floatArrayOf(minMediumScale, fillContainerScale, contentOriginScale).maxOrNull()!!
        } else {
            minMediumScale
        }

        // initialScale is usually determined by the ReadMode, so initialScale takes precedence
        val mediumScale = if (
            initialScale > minScale
            && abs(initialScale - coarseMediumScale) <= coarseMediumScale * differencePercentage
        ) {
            initialScale
        } else {
            coarseMediumScale
        }

        val maxScale = mediumScale * multiple
        return ScalesCalculator.Result(mediumScale = mediumScale, maxScale = maxScale)
    }

    override fun toString(): String {
        return "DynamicScalesCalculator(" +
                "multiple=${multiple.format(2)},differencePercentage=${differencePercentage.format(2)})"
    }
}


/**
 * Fixed scales calculator, always 'mediumScale = minScale * multiple', 'maxScale = mediumScale * multiple'
 */
data class FixedScalesCalculator(
    /**
     * The multiplier between the scales, because by default `mediumScale = minScale * multiple`,
     * `maxScale = mediumScale * multiple`
     */
    val multiple: Float = Multiple
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
        val mediumScale = if (initialScale > minScale) {
            initialScale
        } else {
            minScale * multiple
        }
        val maxScale = mediumScale * multiple
        return ScalesCalculator.Result(mediumScale = mediumScale, maxScale = maxScale)
    }

    override fun toString(): String {
        return "FixedScalesCalculator(multiple=${multiple.format(2)})"
    }
}