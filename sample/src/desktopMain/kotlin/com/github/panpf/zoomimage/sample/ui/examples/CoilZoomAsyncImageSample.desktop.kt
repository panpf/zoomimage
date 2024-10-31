package com.github.panpf.zoomimage.sample.ui.examples

import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.images.coil.CoilKotlinResourceComposeSubsamplingImageGenerator

actual fun platformCoilComposeSubsamplingImageGenerator(): List<CoilComposeSubsamplingImageGenerator>? =
    listOf(CoilKotlinResourceComposeSubsamplingImageGenerator())