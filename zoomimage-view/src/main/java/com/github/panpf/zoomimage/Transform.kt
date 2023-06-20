package com.github.panpf.zoomimage

import com.github.panpf.zoomimage.internal.ScaleFactor
import com.github.panpf.zoomimage.internal.Translation

internal data class Transform constructor(
    val scaleX: Float,
    val scaleY: Float,
    val translationX: Float,
    val translationY: Float
) {

    constructor(scaleFactor: ScaleFactor, translation: Translation) :
            this(
                scaleX = scaleFactor.scaleX,
                scaleY = scaleFactor.scaleY,
                translationX = translation.translationX,
                translationY = translation.translationY,
            )

    companion object {
        val EMPTY = Transform(scaleX = 1f, scaleY = 1f, translationX = 0f, translationY = 0f)
    }
}