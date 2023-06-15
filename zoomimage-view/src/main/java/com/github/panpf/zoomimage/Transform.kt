package com.github.panpf.zoomimage

import com.github.panpf.zoomimage.internal.ScaleFactor
import com.github.panpf.zoomimage.internal.Translation

internal data class Transform constructor(
    val scaleX: Float,
    val scaleY: Float,
    val translateX: Float,
    val translateY: Float
) {

    constructor(scaleFactor: ScaleFactor, translation: Translation) :
            this(
                scaleX = scaleFactor.scaleX,
                scaleY = scaleFactor.scaleY,
                translateX = translation.translationX,
                translateY = translation.translationY,
            )

    companion object {
        val EMPTY = Transform(scaleX = 1f, scaleY = 1f, translateX = 0f, translateY = 0f)
    }
}