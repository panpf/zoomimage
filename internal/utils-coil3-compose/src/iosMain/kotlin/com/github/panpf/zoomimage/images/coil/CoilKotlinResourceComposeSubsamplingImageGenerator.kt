package com.github.panpf.zoomimage.images.coil

import androidx.compose.ui.graphics.painter.Painter
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.pathSegments
import coil3.request.SuccessResult
import coil3.toUri
import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.subsampling.fromKotlinResource
import com.github.panpf.zoomimage.subsampling.toFactory

class CoilKotlinResourceComposeSubsamplingImageGenerator :
    CoilComposeSubsamplingImageGenerator {

    override suspend fun generateImage(
        context: PlatformContext,
        imageLoader: ImageLoader,
        result: SuccessResult,
        painter: Painter
    ): SubsamplingImageGenerateResult? {
        val uri = when (val model = result.request.data) {
            is String -> model.toUri()
            is coil3.Uri -> model.toString().toUri()
            is platform.Foundation.NSURL -> model.toString().toUri()
            else -> null
        }
        if (uri != null && isKotlinResourceUri(uri)) {
            val resourcePath = uri.pathSegments.drop(1).joinToString("/")
            val imageSource = ImageSource.fromKotlinResource(resourcePath).toFactory()
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
        return "CoilKotlinResourceComposeSubsamplingImageGenerator"
    }
}