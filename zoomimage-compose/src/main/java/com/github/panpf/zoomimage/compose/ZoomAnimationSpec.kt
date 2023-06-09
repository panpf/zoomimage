package com.github.panpf.zoomimage.compose

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing

data class ZoomAnimationSpec(
    val durationMillis: Int,
    val easing: Easing,
    val initialVelocity: Float,
) {
    companion object {
        val Default = ZoomAnimationSpec(
            durationMillis = 300,
            easing = FastOutSlowInEasing,
            initialVelocity = 0f
        )
    }
}