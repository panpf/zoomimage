package com.github.panpf.zoomimage.internal

import android.view.View
import coil3.ImageLoader
import coil3.util.CoilUtils

fun CoilUtils.getImageLoader(view: View): ImageLoader? {
    val requestManager = view.getTag(coil3.core.R.id.coil3_request_manager)
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