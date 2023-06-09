package com.github.panpf.zoomimage.core

data class TransformCompat(
    val scale: ScaleFactorCompat,
    val offset: OffsetCompat,
    val rotation: Float = 0f
) {

    constructor(
        scaleX: Float,
        scaleY: Float,
        offsetX: Float,
        offsetY: Float,
        rotation: Float = 0f,
    ) : this(
        scale = ScaleFactorCompat(scaleX = scaleX, scaleY = scaleY),
        offset = OffsetCompat(x = offsetX, y = offsetY),
        rotation = rotation,
    )

    val scaleX: Float
        get() = scale.scaleX
    val scaleY: Float
        get() = scale.scaleY
    val offsetX: Float
        get() = offset.x
    val offsetY: Float
        get() = offset.y

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

/**
 * Linearly interpolate between two TransformCompat.
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start] (or something
 * equivalent to [start]), 1.0 meaning that the interpolation has finished,
 * returning [stop] (or something equivalent to [stop]), and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid (and can
 * easily be generated by curves).
 *
 * Values for [fraction] are usually obtained from an [Animation<Float>], such as
 * an `AnimationController`.
 */
fun lerp(start: TransformCompat, stop: TransformCompat, fraction: Float): TransformCompat {
    return TransformCompat(
        scale = lerp(start.scale, stop.scale, fraction),
        offset = lerp(start.offset, stop.offset, fraction),
        rotation = com.github.panpf.zoomimage.core.internal
            .lerp(start.rotation, stop.rotation, fraction),
    )
}

fun TransformCompat.toShortString(): String =
    "${scale.toShortString()},${offset.toShortString()},$rotation"

fun TransformCompat.times(scaleFactor: ScaleFactorCompat): TransformCompat {
    return this.copy(
        scale = ScaleFactorCompat(
            scaleX = scale.scaleX * scaleFactor.scaleX,
            scaleY = scale.scaleY * scaleFactor.scaleY,
        ),
        offset = OffsetCompat(
            x = offset.x * scaleFactor.scaleX,
            y = offset.y * scaleFactor.scaleY,
        ),
    )
}

fun TransformCompat.div(scaleFactor: ScaleFactorCompat): TransformCompat {
    return this.copy(
        scale = ScaleFactorCompat(
            scaleX = scale.scaleX / scaleFactor.scaleX,
            scaleY = scale.scaleY / scaleFactor.scaleY,
        ),
        offset = OffsetCompat(
            x = offset.x / scaleFactor.scaleX,
            y = offset.y / scaleFactor.scaleY,
        ),
    )
}

fun TransformCompat.concat(other: TransformCompat): TransformCompat {
    return TransformCompat(
        scale = scale.times(other.scale),
        offset = offset + other.offset,
        rotation = rotation + other.rotation,
    )
}