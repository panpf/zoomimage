package com.github.panpf.zoomimage.util.coil

import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator

actual fun platformCoilComposeSubsamplingImageGenerators(): List<CoilComposeSubsamplingImageGenerator> {
    return listOf(
        KotlinResourceCoilComposeSubsamplingImageGenerator(),
        ComposeResourceCoilComposeSubsamplingImageGenerator(),
        PhotoAssetCoilComposeSubsamplingImageGenerator(),
    )
}