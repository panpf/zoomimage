package com.github.panpf.zoomimage.util

import kotlin.math.max
import kotlin.math.min

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
         * provides similar behavior to [android.widget.ImageView.ScaleType.CENTER_CROP]
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
         * provides similar behavior to [android.widget.ImageView.ScaleType.FIT_CENTER]
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
         * provides similar behavior to [android.widget.ImageView.ScaleType.CENTER_INSIDE]
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

fun contentScaleCompat(name: String): ContentScaleCompat {
    return when (name) {
        "FillWidth" -> ContentScaleCompat.FillWidth
        "FillHeight" -> ContentScaleCompat.FillHeight
        "FillBounds" -> ContentScaleCompat.FillBounds
        "Fit" -> ContentScaleCompat.Fit
        "Crop" -> ContentScaleCompat.Crop
        "Inside" -> ContentScaleCompat.Inside
        "None" -> ContentScaleCompat.None
        else -> throw IllegalArgumentException("Unknown ContentScaleCompat name: $name")
    }
}