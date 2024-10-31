package com.github.panpf.zoomimage.images.coil

import androidx.compose.ui.graphics.painter.Painter
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult

class TestCoilComposeSubsamplingImageGenerator : CoilComposeSubsamplingImageGenerator {

    override suspend fun generateImage(
        context: PlatformContext,
        imageLoader: ImageLoader,
        request: ImageRequest,
        result: SuccessResult,
        painter: Painter
    ): SubsamplingImageGenerateResult? {
        return null
    }
}