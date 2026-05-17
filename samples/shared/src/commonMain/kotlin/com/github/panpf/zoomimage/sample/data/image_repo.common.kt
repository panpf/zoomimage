package com.github.panpf.zoomimage.sample.data

import com.githb.panpf.zoomimage.images.ImageFile
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.util.Size
import com.github.panpf.sketch.util.ioCoroutineDispatcher
import kotlinx.coroutines.withContext


expect suspend fun builtinImages(context: PlatformContext): List<ImageFile>

expect suspend fun localImages(
    context: PlatformContext,
    startPosition: Int,
    pageSize: Int
): List<String>

suspend fun readImageInfoOrNull(
    context: PlatformContext,
    sketch: Sketch,
    uri: String,
): ImageInfo? = withContext(ioCoroutineDispatcher()) {
    runCatching {
        val request = ImageRequest(context, uri)
        val requestContext = RequestContext(sketch, request, Size.Empty)
        val fetcher = sketch.components.newFetcherOrThrow(requestContext)
        val fetchResult = fetcher.fetch().getOrThrow()
        val decoder = sketch.components.newDecoderOrThrow(requestContext, fetchResult)
        decoder.imageInfo
    }.apply {
        if (isFailure) {
            exceptionOrNull()?.printStackTrace()
        }
    }.getOrNull()
}
