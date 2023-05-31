package com.github.panpf.zoom.sample.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Size
import com.github.panpf.zoom.sample.R


val horPhoto = Photo(R.drawable.dog_hor, 640.toFloat() / 427)
val verPhoto = Photo(R.drawable.dog_ver, 150.toFloat() / 266)

data class Photo(@DrawableRes val resId: Int, val aspectRatio: Float) {

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
}