package com.github.panpf.zoomimage.sample.ui.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.ui.geometry.Size
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.scale
import com.github.panpf.zoomimage.sample.R

data class ResPhoto(@DrawableRes @RawRes val resId: Int, val aspectRatio: Float) {

    companion object {
        val dog = ResPhoto(R.raw.sample_dog, 575.toFloat() / 427)
        val cat = ResPhoto(R.raw.sample_cat, 150.toFloat() / 266)
    }

    fun calculateTargetSize(viewSize: Int, toBig: Boolean): Size {
        return if (aspectRatio >= 1f) {
            val targetWidth = if (toBig) viewSize * 3f else viewSize / 2f
            val targetHeight = (targetWidth / aspectRatio)
            Size(targetWidth, targetHeight)
        } else {
            val targetHeight = if (toBig) viewSize * 3f else viewSize / 2f
            val targetWidth = (targetHeight * aspectRatio)
            Size(targetWidth, targetHeight)
        }
    }

    fun newFetcher(name: String, big: Boolean): Fetcher = Fetcher(this, name, big)

    data class Fetcher(val resPhoto: ResPhoto, val name: String, val big: Boolean) {

        private var cacheBitmap: Bitmap? = null
        private var cacheViewSize: Int? = null

        fun calculateTargetSize(viewSize: Int): Size {
            return resPhoto.calculateTargetSize(viewSize, big)
        }

        fun getBitmap(context: Context, viewSize: Int): Bitmap {
            if (viewSize != cacheViewSize) {
                cacheBitmap = null
            }

            val cacheBitmap = cacheBitmap
            if (cacheBitmap != null) return cacheBitmap

            val targetSize = resPhoto.calculateTargetSize(viewSize, big)
            val bitmap = ResourcesCompat.getDrawable(context.resources, resPhoto.resId, null)
                .let { it as BitmapDrawable }.bitmap
            return bitmap.scale(targetSize.width.toInt(), targetSize.height.toInt()).apply {
                this@Fetcher.cacheBitmap = this
                this@Fetcher.cacheViewSize = viewSize
            }
        }
    }
}