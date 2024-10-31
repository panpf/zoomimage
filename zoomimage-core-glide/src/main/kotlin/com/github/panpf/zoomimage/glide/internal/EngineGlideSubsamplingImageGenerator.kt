package com.github.panpf.zoomimage.glide.internal

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.github.panpf.zoomimage.glide.GlideSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult

/**
 * Default implementation of [GlideSubsamplingImageGenerator]
 */
class EngineGlideSubsamplingImageGenerator : GlideSubsamplingImageGenerator {

    override suspend fun generateImage(
        context: Context,
        glide: Glide,
        model: Any,
        drawable: Drawable
    ): SubsamplingImageGenerateResult {
        val imageSource = modelToImageSource(context, glide, model)
            ?: return SubsamplingImageGenerateResult.Error("Unsupported model")
        return SubsamplingImageGenerateResult.Success(SubsamplingImage(imageSource, null))
    }
}