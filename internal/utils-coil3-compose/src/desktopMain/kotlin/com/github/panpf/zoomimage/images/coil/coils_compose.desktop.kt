package com.github.panpf.zoomimage.images.coil

import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

actual fun platformComposeSubsamplingImageGenerators(): ImmutableList<CoilComposeSubsamplingImageGenerator> {
    return persistentListOf(CoilKotlinResourceComposeSubsamplingImageGenerator())
}