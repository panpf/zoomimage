package com.github.panpf.zoomimage.core

data class TransformCompat(
    val scale: ScaleFactorCompat,
    val offset: OffsetCompat,
    val rotation: Float = 0f
) {

    companion object {
        val Origin = TransformCompat(
            scale = ScaleFactorCompat(1f, 1f),
            offset = OffsetCompat.Zero,
            rotation = 0f
        )
    }

    override fun toString(): String {
        return "Transform(scale=${scale.toShortString()}, offset=${offset.toShortString()}, rotation=$rotation)"
    }
}

fun TransformCompat.toShortString(): String =
    "(${scale.toShortString()},${offset.toShortString()},$rotation)"