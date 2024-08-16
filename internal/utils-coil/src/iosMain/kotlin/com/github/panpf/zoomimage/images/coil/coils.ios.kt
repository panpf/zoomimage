package com.github.panpf.zoomimage.images.coil

import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

actual fun platformModeToImageSources(): ImmutableList<CoilModelToImageSource> {
    return persistentListOf(CoilKotlinResourceToImageSource())
}