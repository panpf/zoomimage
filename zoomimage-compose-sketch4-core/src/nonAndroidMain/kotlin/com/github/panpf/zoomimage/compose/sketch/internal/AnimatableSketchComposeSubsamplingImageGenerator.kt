package com.github.panpf.zoomimage.compose.sketch.internal

import androidx.compose.ui.graphics.painter.Painter
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.zoomimage.compose.sketch.SketchComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult

actual class AnimatableSketchComposeSubsamplingImageGenerator :
    SketchComposeSubsamplingImageGenerator {

    actual override fun generateImage(
        sketch: Sketch,
        request: ImageRequest,
        result: ImageResult.Success,
        painter: Painter
    ): SubsamplingImageGenerateResult? {
        return null
    }
}