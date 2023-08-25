package com.github.panpf.zoomimage.zoom

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.zoom.StepScalesComputer.Companion.Multiple
import kotlin.math.max

interface StepScalesComputer {

    fun compute(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentOriginSize: IntSizeCompat,
        contentScale: ContentScaleCompat,
        minScale: Float,
    ): FloatArray

    companion object {
        const val Multiple = 3f

        val Dynamic = DynamicStepScalesComputer()

        val Fixed = FixedStepScalesComputer()

        fun dynamic(minStepScaleMultiple: Float = Multiple): StepScalesComputer =
            DynamicStepScalesComputer(minStepScaleMultiple)

        fun fixed(stepScaleMultiple: Float = Multiple): StepScalesComputer =
            FixedStepScalesComputer(stepScaleMultiple)
    }
}

data class DynamicStepScalesComputer(
    private val minStepScaleMultiple: Float = Multiple
) : StepScalesComputer {

    override fun compute(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentOriginSize: IntSizeCompat,
        contentScale: ContentScaleCompat,
        minScale: Float
    ): FloatArray {
        val minMediumScale = minScale * minStepScaleMultiple
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
        val maxScale = mediumScale * minStepScaleMultiple
        return floatArrayOf(mediumScale, maxScale)
    }

    override fun toString(): String {
        return "DynamicStepScalesComputer($minStepScaleMultiple)"
    }
}

data class FixedStepScalesComputer(
    private val stepScaleMultiple: Float = Multiple
) : StepScalesComputer {

    override fun compute(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentOriginSize: IntSizeCompat,
        contentScale: ContentScaleCompat,
        minScale: Float
    ): FloatArray {
        val mediumScale = minScale * stepScaleMultiple
        val maxScale = mediumScale * stepScaleMultiple
        return floatArrayOf(mediumScale, maxScale)
    }

    override fun toString(): String {
        return "FixedStepScalesComputer($stepScaleMultiple)"
    }
}