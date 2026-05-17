package com.github.panpf.zoomimage.sample.ui.util

import coil3.Image
import coil3.ImageLoader

fun findPlaceholderFromMemoryCache(imageLoader: ImageLoader, url: String): Image? {
    val memoryCache = imageLoader.memoryCache ?: return null
    val keys = memoryCache.keys
    keys.forEach {
        if (it.key != url) return@forEach
        val value = memoryCache[it] ?: return@forEach
        return value.image
    }
    return null
}