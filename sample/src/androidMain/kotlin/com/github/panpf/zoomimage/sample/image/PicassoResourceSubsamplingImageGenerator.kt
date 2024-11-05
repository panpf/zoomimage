package com.github.panpf.zoomimage.sample.image

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import com.github.panpf.sketch.fetch.isComposeResourceUri
import com.github.panpf.sketch.util.toUri
import com.github.panpf.zoomimage.picasso.PicassoSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.ComposeResourceImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.squareup.picasso.Picasso

class PicassoResourceSubsamplingImageGenerator : PicassoSubsamplingImageGenerator {

    override suspend fun generateImage(
        context: Context,
        picasso: Picasso,
        data: Any,
        drawable: Drawable
    ): SubsamplingImageGenerateResult? {
        if (data is Uri && isComposeResourceUri(data.toString().toUri())) {
            val resourcePath = data.pathSegments.drop(1).joinToString("/")
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
        return "PicassoResourceSubsamplingImageGenerator"
    }
}