package com.github.panpf.zoomimage.view.zoom

import android.content.res.Resources


/**
 * Used to configure the style of the scroll bar
 */
data class ScrollBarSpec(
    /**
     * Scroll bar color, which defaults to translucent gray
     */
    val color: Int = 0xB2888888.toInt(),

    /**
     * Scroll bar size, default to 3 dp
     */
    val size: Float = 3f * Resources.getSystem().displayMetrics.density,

    /**
     * The distance of the scroll bar from the edge of the container, which defaults to 6 dp
     */
    val margin: Float = 6f * Resources.getSystem().displayMetrics.density
) {
    companion object {
        val Default = ScrollBarSpec()
    }
}