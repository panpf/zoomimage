package com.github.panpf.zoomimage.test.coil

import androidx.compose.ui.graphics.painter.Painter
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.Uri
import coil3.pathSegments
import coil3.request.SuccessResult
import coil3.toUri
import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.ComposeResourceImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import platform.Foundation.NSURL

actual class CoilComposeResourceSubsamplingImageGenerator :
    CoilComposeSubsamplingImageGenerator {

    actual override suspend fun generateImage(
        context: PlatformContext,
        imageLoader: ImageLoader,
        result: SuccessResult,
        painter: Painter
    ): SubsamplingImageGenerateResult? {
        val uri = when (val model = result.request.data) {
            is String -> model.toUri()
            is Uri -> model.toString().toUri()
            is NSURL -> model.toString().toUri()
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
        return "CoilComposeResourceSubsamplingImageGenerator"
    }
}