package com.github.panpf.zoomimage.sample.ui.util.view

import android.content.Context


fun Context.getWindowBackground(): Int {
    val array = theme.obtainStyledAttributes(
        intArrayOf(android.R.attr.windowBackground)
    )
    val windowBackground = array.getColor(0, 0xFF00FF)
    array.recycle()
    return windowBackground
}