package com.github.panpf.zoomimage.sample.ui.util

import android.graphics.Bitmap
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.internal.RequestContext
import com.github.panpf.sketch.transform.TransformResult
import com.github.panpf.sketch.transform.Transformation
import kotlinx.coroutines.delay

fun ImageRequest.Builder.forceDelay(delay: Long): ImageRequest.Builder {
    memoryCachePolicy(CachePolicy.DISABLED)
    resultCachePolicy(CachePolicy.DISABLED)
    addTransformations(DelayTransformation(delay))
    return this
}

private data class DelayTransformation(val delay: Long) : Transformation {

    override val key: String
        get() = "DelayTransformation($delay)"

    override suspend fun transform(
        sketch: Sketch,
        requestContext: RequestContext,
        input: Bitmap
    ): TransformResult? {
        delay(delay)
        return null
    }
}