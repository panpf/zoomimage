package com.github.panpf.zoomimage.zoom

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.zoom.ScalesCalculator.Companion.Multiple
import kotlin.math.max

interface ScalesCalculator {

    fun calculate(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentOriginSize: IntSizeCompat,
        contentScale: ContentScaleCompat,
        minScale: Float,
    ): Result

    companion object {
        const val Multiple = 3f

        val Dynamic = DynamicScalesCalculator()

        val Fixed = FixedScalesCalculator()

        fun dynamic(multiple: Float = Multiple): ScalesCalculator =
            DynamicScalesCalculator(multiple)

        fun fixed(multiple: Float = Multiple): ScalesCalculator =
            FixedScalesCalculator(multiple)
    }

    data class Result(val mediumScale: Float, val maxScale: Float)
}

data class DynamicScalesCalculator(
    private val multiple: Float = Multiple
) : ScalesCalculator {

    override fun calculate(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentOriginSize: IntSizeCompat,
        contentScale: ContentScaleCompat,
        minScale: Float
    ): ScalesCalculator.Result {
        val minMediumScale = minScale * multiple
        val mediumScale = if (contentScale != ContentScaleCompat.FillBounds) {
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
        val maxScale = mediumScale * multiple
        return ScalesCalculator.Result(mediumScale = mediumScale, maxScale = maxScale)
    }

    override fun toString(): String {
        return "DynamicScalesCalculator($multiple)"
    }
}

data class FixedScalesCalculator(
    private val multiple: Float = Multiple
) : ScalesCalculator {

    override fun calculate(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentOriginSize: IntSizeCompat,
        contentScale: ContentScaleCompat,
        minScale: Float
    ): ScalesCalculator.Result {
        val mediumScale = minScale * multiple
        val maxScale = mediumScale * multiple
        return ScalesCalculator.Result(mediumScale = mediumScale, maxScale = maxScale)
    }

    override fun toString(): String {
        return "FixedScalesCalculator($multiple)"
    }
}