package com.github.panpf.zoomimage.view.zoom

import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator

data class ZoomAnimationSpec(
    var durationMillis: Int = 300,
    var interpolator: Interpolator = AccelerateDecelerateInterpolator()
) {
    companion object {
        val Default = ZoomAnimationSpec(
            durationMillis = 300,
            interpolator = AccelerateDecelerateInterpolator(),
        )
    }
}