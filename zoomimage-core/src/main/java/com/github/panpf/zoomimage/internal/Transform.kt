package com.github.panpf.zoomimage.internal

data class Transform(val scale: ScaleFactor, val translation: Translation) {

    constructor(
        scaleX: Float,
        scaleY: Float,
        translationX: Float,
        translationY: Float
    ) : this(
        scale = ScaleFactor(scaleX = scaleX, scaleY = scaleY),
        translation = Translation(translationX = translationX, translationY = translationY),
    )

    val scaleX: Float = scale.scaleX
    val scaleY: Float = scale.scaleY
    val translationX: Float = translation.translationX
    val translationY: Float = translation.translationY

    companion object {
        val EMPTY = Transform(scaleX = 1f, scaleY = 1f, translationX = 0f, translationY = 0f)
    }

    override fun toString(): String {
        return "Transform(scale=($scaleX,$scaleY),translation=($translationX,$translationY))"
    }
}