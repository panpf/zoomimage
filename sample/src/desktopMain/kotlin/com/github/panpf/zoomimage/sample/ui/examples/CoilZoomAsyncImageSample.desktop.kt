package com.github.panpf.zoomimage.sample.ui.examples

import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.sample.image.CoilKotlinResourceToImageSource

actual fun platformCoilModelToImageSource(): List<CoilModelToImageSource>? =
    listOf(CoilKotlinResourceToImageSource())