package com.github.panpf.zoom.sample.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.geometry.Size
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.scale

data class PhotoItem(val photo: Photo, val name: String, val big: Boolean) {

    private var cacheBitmap: Bitmap? = null
    private var cacheViewSize: Int? = null

    fun calculateTargetSize(viewSize: Int): Size {
        return photo.calculateTargetSize(viewSize, big)
    }

    fun getBitmap(context: Context, viewSize: Int): Bitmap {
        if (viewSize != cacheViewSize) {
            cacheBitmap = null
        }

        val cacheBitmap = cacheBitmap
        if (cacheBitmap != null) return cacheBitmap

        val targetSize = photo.calculateTargetSize(viewSize, big)
        val bitmap = ResourcesCompat.getDrawable(context.resources, photo.resId, null)
            .let { it as BitmapDrawable }.bitmap
        return bitmap.scale(targetSize.width.toInt(), targetSize.height.toInt()).apply {
            this@PhotoItem.cacheBitmap = this
            this@PhotoItem.cacheViewSize = viewSize
        }
    }
}
