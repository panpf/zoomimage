package com.github.panpf.zoomimage.sample.image

import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.util.Size
import com.github.panpf.sketch.util.ioCoroutineDispatcher
import com.github.panpf.zoomimage.sample.ui.model.Photo
import kotlinx.coroutines.withContext

//suspend fun ImageRequest.decode(decoder: Decoder.Factory): ImageData? =
//    withContext(ioCoroutineDispatcher()) {
//        val sketch: Sketch = KoinPlatform.getKoin().get<Sketch>()
//        val requestContext = RequestContext(sketch, this@decode)
//        val fetchResult = sketch.components.newFetcherOrThrow(requestContext).fetch().getOrThrow()
//        decoder.create(requestContext, fetchResult)?.decode()
//    }

suspend fun photoUri2PhotoInfo(sketch: Sketch, uri: String): Photo {
    val imageInfo = withContext(ioCoroutineDispatcher()) {
        runCatching {
            val request = ImageRequest(sketch.context, uri = uri)
            val requestContext =
                RequestContext(sketch = sketch, initialRequest = request, size = Size.Empty)
            val fetcher = sketch.components.newFetcherOrThrow(requestContext)
            val fetchResult = fetcher.fetch().getOrThrow()
            val decoder = sketch.components.newDecoderOrThrow(requestContext, fetchResult)
            decoder.getImageInfo()
        }.apply {
            if (isFailure) {
                Exception("uri='$uri'", exceptionOrNull()).printStackTrace()
            }
        }.getOrNull()
    }
    return Photo(
        originalUrl = uri,
        mediumUrl = null,
        thumbnailUrl = null,
        width = imageInfo?.width,
        height = imageInfo?.height,
    )
}