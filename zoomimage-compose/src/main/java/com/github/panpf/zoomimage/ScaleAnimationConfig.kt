package com.github.panpf.zoomimage

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing

data class ScaleAnimationConfig(
    val animateDoubleTapScale: Boolean = true,
    val animationDurationMillis: Int = DefaultDurationMillis,
    val animationEasing: Easing = DefaultEasing,
    val initialVelocity: Float = DefaultInitialVelocity,
) {
    companion object {
        const val DefaultDurationMillis: Int = 500
        val DefaultEasing: Easing = FastOutSlowInEasing
        const val DefaultInitialVelocity: Float = 0f
    }
}