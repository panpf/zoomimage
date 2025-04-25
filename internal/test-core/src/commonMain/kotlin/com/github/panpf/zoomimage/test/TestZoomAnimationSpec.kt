package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.zoom.BaseZoomAnimationSpec

data class TestZoomAnimationSpec(
    override val durationMillis: Int = DEFAULT_DURATION_MILLIS
) : BaseZoomAnimationSpec {

    companion object {
        val DEFAULT_DURATION_MILLIS = BaseZoomAnimationSpec.DEFAULT_DURATION_MILLIS
        val Default = TestZoomAnimationSpec()
        val None = TestZoomAnimationSpec(durationMillis = 0)
    }
}