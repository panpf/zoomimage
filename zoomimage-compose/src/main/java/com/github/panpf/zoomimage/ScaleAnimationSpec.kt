package com.github.panpf.zoomimage

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing

data class ScaleAnimationSpec(
    val durationMillis: Int,
    val easing: Easing,
    val initialVelocity: Float,
) {
    companion object {
        val Default = ScaleAnimationSpec(
            durationMillis = 300,
            easing = FastOutSlowInEasing,
            initialVelocity = 0f
        )
    }
}