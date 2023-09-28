package com.github.panpf.zoomimage.compose.glide.internal

import com.bumptech.glide.RequestBuilder

internal val RequestBuilder<*>.internalModel: Any?
    //  get() = model
    get() {
        return try {
            this.javaClass.getDeclaredField("model").apply {
                isAccessible = true
            }.get(this)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }