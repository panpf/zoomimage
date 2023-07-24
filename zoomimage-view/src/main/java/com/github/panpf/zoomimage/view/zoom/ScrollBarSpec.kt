package com.github.panpf.zoomimage.view.zoom

import android.content.res.Resources
import android.graphics.Color

data class ScrollBarSpec(
    val color: Int = 0xB2888888.toInt(),
    val size: Float = 3f * Resources.getSystem().displayMetrics.density,
    val margin: Float = 6f * Resources.getSystem().displayMetrics.density
) {
    companion object {
        val Default = ScrollBarSpec()
    }
}