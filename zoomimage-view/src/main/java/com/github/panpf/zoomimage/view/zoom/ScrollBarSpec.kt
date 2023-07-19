package com.github.panpf.zoomimage.view.zoom

import android.content.res.Resources
import android.graphics.Color

data class ScrollBarSpec(
    val color: Int = Color.parseColor("#B2888888"),
    val size: Float = 3f * Resources.getSystem().displayMetrics.density,
    val margin: Float = 6f * Resources.getSystem().displayMetrics.density
) {
    companion object {
        val Default = ScrollBarSpec()
    }
}