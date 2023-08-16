package com.github.panpf.zoomimage.util

data class InitialZoom(
    val minScale: Float,
    val mediumScale: Float,
    val maxScale: Float,
    val baseTransform: TransformCompat,
    val userTransform: TransformCompat,
) {
    companion object {
        // todo Unit tests
        val Origin = InitialZoom(
            minScale = 1.0f,
            mediumScale = 1.0f,
            maxScale = 1.0f,
            baseTransform = TransformCompat.Origin,
            userTransform = TransformCompat.Origin,
        )
    }
}