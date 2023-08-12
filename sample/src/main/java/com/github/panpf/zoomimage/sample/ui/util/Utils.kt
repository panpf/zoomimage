package com.github.panpf.zoomimage.sample.ui.util

import android.content.Context
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlin.math.roundToInt

fun computeImageViewSize(context: Context): IntSizeCompat {
    val displayMetrics = context.resources.displayMetrics
    val width = (displayMetrics.widthPixels * 0.7f).roundToInt()
    val height = (displayMetrics.widthPixels * 0.7f * 0.7f).roundToInt()
    return IntSizeCompat(width, height)
}