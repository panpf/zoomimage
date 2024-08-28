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

import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.SizeCompat
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a rule to apply to scale a source rectangle to be inscribed into a destination
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ContentScaleCompatTest
 */
interface ContentScaleCompat {

    /**
     * Computes the scale factor to apply to the horizontal and vertical axes independently
     * of one another to fit the source appropriately with the given destination
     */
    fun computeScaleFactor(srcSize: SizeCompat, dstSize: SizeCompat): ScaleFactorCompat

    /**
     * Companion object containing commonly used [ContentScaleCompat] implementations
     */
    companion object {

        /**
         * Scale the source uniformly (maintaining the source's aspect ratio) so that both
         * dimensions (width and height) of the source will be equal to or larger than the
         * corresponding dimension of the destination.
         *
         * This [ContentScaleCompat] implementation in combination with usage of [AlignmentCompat.Center]
         * provides similar behavior to 'android.widget.ImageView.ScaleType.CENTER_CROP'
         */
        val Crop = object : ContentScaleCompat {
            override fun computeScaleFactor(
                srcSize: SizeCompat,
                dstSize: SizeCompat
            ): ScaleFactorCompat =
                computeFillMaxDimension(srcSize, dstSize).let {
                    ScaleFactorCompat(it, it)
                }
        }

        /**
         * Scale the source uniformly (maintaining the source's aspect ratio) so that both
         * dimensions (width and height) of the source will be equal to or less than the
         * corresponding dimension of the destination
         *
         * This [ContentScaleCompat] implementation in combination with usage of [AlignmentCompat.Center]
         * provides similar behavior to 'android.widget.ImageView.ScaleType.FIT_CENTER'
         */
        val Fit = object : ContentScaleCompat {
            override fun computeScaleFactor(
                srcSize: SizeCompat,
                dstSize: SizeCompat
            ): ScaleFactorCompat =
                computeFillMinDimension(srcSize, dstSize).let {
                    ScaleFactorCompat(it, it)
                }
        }

        /**
         * Scale the source maintaining the aspect ratio so that the bounds match the destination
         * height. This can cover a larger area than the destination if the height is larger than
         * the width.
         */
        val FillHeight = object : ContentScaleCompat {
            override fun computeScaleFactor(
                srcSize: SizeCompat,
                dstSize: SizeCompat
            ): ScaleFactorCompat =
                computeFillHeight(srcSize, dstSize).let {
                    ScaleFactorCompat(it, it)
                }
        }

        /**
         * Scale the source maintaining the aspect ratio so that the bounds match the
         * destination width. This can cover a larger area than the destination if the width is
         * larger than the height.
         */
        val FillWidth = object : ContentScaleCompat {
            override fun computeScaleFactor(
                srcSize: SizeCompat,
                dstSize: SizeCompat
            ): ScaleFactorCompat =
                computeFillWidth(srcSize, dstSize).let {
                    ScaleFactorCompat(it, it)
                }
        }

        /**
         * Scale the source to maintain the aspect ratio to be inside the destination bounds
         * if the source is larger than the destination. If the source is smaller than or equal
         * to the destination in both dimensions, this behaves similarly to [None]. This will
         * always be contained within the bounds of the destination.
         *
         * This [ContentScaleCompat] implementation in combination with usage of [AlignmentCompat.Center]
         * provides similar behavior to 'android.widget.ImageView.ScaleType.CENTER_INSIDE'
         */
        val Inside = object : ContentScaleCompat {

            override fun computeScaleFactor(
                srcSize: SizeCompat,
                dstSize: SizeCompat
            ): ScaleFactorCompat {
                return if (srcSize.width <= dstSize.width &&
                    srcSize.height <= dstSize.height
                ) {
                    ScaleFactorCompat(1.0f, 1.0f)
                } else {
                    computeFillMinDimension(srcSize, dstSize).let {
                        ScaleFactorCompat(it, it)
                    }
                }
            }
        }

        /**
         * Do not apply any scaling to the source
         */
        val None = FixedScale(1.0f)

        /**
         * Scale horizontal and vertically non-uniformly to fill the destination bounds.
         */
        val FillBounds = object : ContentScaleCompat {
            override fun computeScaleFactor(
                srcSize: SizeCompat,
                dstSize: SizeCompat
            ): ScaleFactorCompat =
                ScaleFactorCompat(
                    computeFillWidth(srcSize, dstSize),
                    computeFillHeight(srcSize, dstSize)
                )
        }
    }
}

/**
 * [ContentScaleCompat] implementation that always scales the dimension by the provided
 * fixed floating point value
 */
data class FixedScale(val value: Float) : ContentScaleCompat {
    override fun computeScaleFactor(srcSize: SizeCompat, dstSize: SizeCompat): ScaleFactorCompat =
        ScaleFactorCompat(value, value)
}

private fun computeFillMaxDimension(srcSize: SizeCompat, dstSize: SizeCompat): Float {
    val widthScale = computeFillWidth(srcSize, dstSize)
    val heightScale = computeFillHeight(srcSize, dstSize)
    return max(widthScale, heightScale)
}

private fun computeFillMinDimension(srcSize: SizeCompat, dstSize: SizeCompat): Float {
    val widthScale = computeFillWidth(srcSize, dstSize)
    val heightScale = computeFillHeight(srcSize, dstSize)
    return min(widthScale, heightScale)
}

private fun computeFillWidth(srcSize: SizeCompat, dstSize: SizeCompat): Float =
    dstSize.width / srcSize.width

private fun computeFillHeight(srcSize: SizeCompat, dstSize: SizeCompat): Float =
    dstSize.height / srcSize.height


/**
 * Returns the name of [ContentScaleCompat], which can also be converted back via the [valueOf] method
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ContentScaleCompatTest.testName
 */
val ContentScaleCompat.name: String
    get() = when (this) {
        ContentScaleCompat.FillWidth -> "FillWidth"
        ContentScaleCompat.FillHeight -> "FillHeight"
        ContentScaleCompat.FillBounds -> "FillBounds"
        ContentScaleCompat.Fit -> "Fit"
        ContentScaleCompat.Crop -> "Crop"
        ContentScaleCompat.Inside -> "Inside"
        ContentScaleCompat.None -> "None"
        else -> "Unknown ContentScaleCompat: $this"
    }

/**
 * Returns the [ContentScaleCompat] corresponding to the given [name], or throws [IllegalArgumentException]. see [name] property
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ContentScaleCompatTest.testValueOf
 */
fun ContentScaleCompat.Companion.valueOf(name: String): ContentScaleCompat {
    return when (name) {
        "FillWidth" -> FillWidth
        "FillHeight" -> FillHeight
        "FillBounds" -> FillBounds
        "Fit" -> Fit
        "Crop" -> Crop
        "Inside" -> Inside
        "None" -> None
        else -> throw IllegalArgumentException("Unknown ContentScaleCompat name: $name")
    }
}