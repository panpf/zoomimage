package com.github.panpf.zoomimage.sample

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat

fun getViewImageLoaderIcon(context: Context, viewImageLoader: String): Drawable {
    val resources = context.resources
    return when (viewImageLoader) {
        "Sketch" -> ResourcesCompat.getDrawable(resources, R.drawable.logo_sketch, null)!!
        "Coil" -> ResourcesCompat.getDrawable(resources, R.drawable.logo_coil, null)!!
        "Glide" -> ResourcesCompat.getDrawable(resources, R.drawable.logo_glide, null)!!
        "Picasso" -> ResourcesCompat.getDrawable(resources, R.drawable.logo_square, null)!!
        "Basic" -> ResourcesCompat.getDrawable(resources, R.drawable.logo_basic, null)!!
        else -> throw IllegalArgumentException("Unsupported composeImageLoader: $viewImageLoader")
    }
}