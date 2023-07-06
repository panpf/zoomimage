package com.github.panpf.zoomimage.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ScaleFactor
import com.github.panpf.zoomimage.compose.internal.times
import com.github.panpf.zoomimage.compose.internal.toShortString

data class Transform(
    val scale: ScaleFactor,
    val offset: Offset,
    val rotation: Float = 0f,
) {

    companion object {
        val Origin = Transform(scale = ScaleFactor(1f, 1f), offset = Offset.Zero, rotation = 0f)
    }

    override fun toString(): String {
        return "Transform(scale=${scale.toShortString()}, offset=${offset.toShortString()}, rotation=$rotation)"
    }
}

fun Transform.toShortString(): String =
    "(${scale.toShortString()},${offset.toShortString()},$rotation)"

fun Transform.times(scaleFactor: ScaleFactor): Transform {
    return this.copy(
        scale = ScaleFactor(
            scaleX = scale.scaleX * scaleFactor.scaleX,
            scaleY = scale.scaleY * scaleFactor.scaleY,
        ),
        offset = Offset(
            x = offset.x * scaleFactor.scaleX,
            y = offset.y * scaleFactor.scaleY,
        ),
    )
}

fun Transform.div(scaleFactor: ScaleFactor): Transform {
    return this.copy(
        scale = ScaleFactor(
            scaleX = scale.scaleX / scaleFactor.scaleX,
            scaleY = scale.scaleY / scaleFactor.scaleY,
        ),
        offset = Offset(
            x = offset.x / scaleFactor.scaleX,
            y = offset.y / scaleFactor.scaleY,
        ),
    )
}

fun Transform.concat(other: Transform): Transform {
    return Transform(
        scale = scale.times(other.scale),
        offset = offset + other.offset,
        rotation = rotation + other.rotation,
    )
}