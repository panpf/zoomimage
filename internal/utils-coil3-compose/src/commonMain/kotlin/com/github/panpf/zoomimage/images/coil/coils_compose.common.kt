package com.github.panpf.zoomimage.images.coil

import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import kotlinx.collections.immutable.ImmutableList

expect fun platformComposeSubsamplingImageGenerators(): ImmutableList<CoilComposeSubsamplingImageGenerator>