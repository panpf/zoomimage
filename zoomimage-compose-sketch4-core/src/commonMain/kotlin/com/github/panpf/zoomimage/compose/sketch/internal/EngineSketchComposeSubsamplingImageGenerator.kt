package com.github.panpf.zoomimage.compose.sketch.internal

import androidx.compose.ui.graphics.painter.Painter
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.zoomimage.compose.sketch.SketchComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.sketch.SketchImageSource
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult

class EngineSketchComposeSubsamplingImageGenerator : SketchComposeSubsamplingImageGenerator {

    override fun generateImage(
        sketch: Sketch,
        request: ImageRequest,
        result: ImageResult.Success,
        painter: Painter
    ): SubsamplingImageGenerateResult {
        val imageSource = SketchImageSource.Factory(sketch, request.uri.toString())
        val imageInfo = ImageInfo(
            width = result.imageInfo.width,
            height = result.imageInfo.height,
            mimeType = result.imageInfo.mimeType
        )
        return SubsamplingImageGenerateResult.Success(SubsamplingImage(imageSource, imageInfo))
    }
}