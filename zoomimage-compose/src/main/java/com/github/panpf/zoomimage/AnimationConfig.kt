package com.github.panpf.zoomimage

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing

data class AnimationConfig(
    val doubleTapScaleEnabled: Boolean = true,
    val durationMillis: Int = DefaultDurationMillis,
    val easing: Easing = DefaultEasing,
    val initialVelocity: Float = DefaultInitialVelocity,
) {
    companion object {
        const val DefaultDurationMillis: Int = 300
        val DefaultEasing: Easing = FastOutSlowInEasing
        const val DefaultInitialVelocity: Float = 0f
    }
}