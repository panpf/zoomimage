package com.github.panpf.zoomimage.sample.image

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.github.panpf.sketch.fetch.isComposeResourceUri
import com.github.panpf.sketch.util.toUri
import com.github.panpf.zoomimage.glide.GlideSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.ComposeResourceImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult

class ComposeResourceGlideSubsamplingImageGenerator : GlideSubsamplingImageGenerator {

    override suspend fun generateImage(
        context: Context,
        glide: Glide,
        model: Any,
        drawable: Drawable
    ): SubsamplingImageGenerateResult? {
        val uri = when (model) {
            is String -> model.toUri()
            is Uri -> model.toString().toUri()
            else -> null
        }
        if (uri != null && isComposeResourceUri(uri)) {
            val resourcePath = uri.pathSegments.drop(1).joinToString("/")
            val imageSource = ComposeResourceImageSource.Factory(resourcePath)
            return SubsamplingImageGenerateResult.Success(SubsamplingImage(imageSource, null))
        }
        return null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other != null && this::class == other::class
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }

    override fun toString(): String {
        return "EngineGlideSubsamplingImageGenerator"
    }
}