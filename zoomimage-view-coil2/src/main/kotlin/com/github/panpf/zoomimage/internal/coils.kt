package com.github.panpf.zoomimage.internal

import android.view.View
import coil.ImageLoader
import coil.util.CoilUtils

fun CoilUtils.getImageLoader(view: View): ImageLoader? {
    val requestManager = view.getTag(coil.base.R.id.coil_request_manager)
    if (requestManager != null) {
        try {
            val requestDelegate = requestManager.javaClass.getDeclaredField("currentRequest")
                .apply { isAccessible = true }
                .get(requestManager)
            if (requestDelegate != null) {
                val imageLoader = requestDelegate.javaClass.getDeclaredField("imageLoader")
                    .apply { isAccessible = true }
                    .get(requestDelegate)
                return imageLoader as? ImageLoader
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return null
}