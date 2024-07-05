package com.github.panpf.zoomimage.sample.image

import com.github.panpf.sketch.Image
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.internal.RequestContext
import com.github.panpf.sketch.transform.TransformResult
import com.github.panpf.sketch.transform.Transformation
import kotlin.math.min

data class BitmapScaleTransformation(val maxSize: Int) : Transformation {

    override val key: String = "BitmapScaleTransformation($maxSize)"

    override suspend fun transform(
        sketch: Sketch,
        requestContext: RequestContext,
        input: Image
    ): TransformResult {
        val scale = min(maxSize / input.width.toFloat(), maxSize / input.height.toFloat())
        val scaledBitmap = input.transformer()!!.scale(input, scale)
        return TransformResult(scaledBitmap, key)
    }
}