package com.github.panpf.zoomimage.core

import com.github.panpf.zoomimage.core.internal.format

data class Transform(val scale: ScaleFactorCompat, val offset: OffsetCompat) {

    constructor(
        scaleX: Float,
        scaleY: Float,
        offsetX: Float,
        offsetY: Float
    ) : this(
        scale = ScaleFactorCompat(scaleX = scaleX, scaleY = scaleY),
        offset = OffsetCompat(x = offsetX, y = offsetY),
    )

    val scaleX: Float = scale.scaleX
    val scaleY: Float = scale.scaleY
    val offsetX: Float = offset.x
    val offsetY: Float = offset.y

    companion object {
        val Empty = Transform(scaleX = 1f, scaleY = 1f, offsetX = 0f, offsetY = 0f)
    }

    override fun toString(): String {
        return "Transform(scale=${scaleX.format(2)}x${scaleY.format(2)}, " +
                "offset=${offsetX.format(1)}x${offsetY.format(1)}))"
    }
}

fun Transform.toShortString(): String =
    "(${scaleX.format(2)}x${scaleY.format(2)}),${offsetX.format(1)}x${offsetY.format(1)})"