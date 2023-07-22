package com.github.panpf.zoomimage.view.coil.internal

import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import coil.drawable.CrossfadeDrawable


internal fun Drawable.getLastChildDrawable(): Drawable? {
    return when (val drawable = this) {
        is CrossfadeDrawable -> {
            drawable.end?.getLastChildDrawable()
        }

        is LayerDrawable -> {
            val layerCount = drawable.numberOfLayers.takeIf { it > 0 } ?: return null
            drawable.getDrawable(layerCount - 1).getLastChildDrawable()
        }

        else -> drawable
    }
}

internal fun Context?.getLifecycle(): Lifecycle? {
    var context: Context? = this
    while (true) {
        when (context) {
            is LifecycleOwner -> return context.lifecycle
            is ContextWrapper -> context = context.baseContext
            else -> return null
        }
    }
}

internal fun Lifecycle.isCoilGlobalLifecycle() =
    this.toString() == "coil.request.GlobalLifecycle"