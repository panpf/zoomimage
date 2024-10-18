package com.github.panpf.zoomimage.sample.image

import com.github.panpf.sketch.BitmapImage
import com.github.panpf.sketch.Image
import com.github.panpf.sketch.asImage
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.transform.TransformResult
import com.github.panpf.sketch.transform.Transformation
import com.github.panpf.sketch.util.scale
import kotlin.math.min

data class BitmapScaleTransformation(val maxSize: Int) : Transformation {

    override val key: String = "BitmapScaleTransformation($maxSize)"

    override fun transform(requestContext: RequestContext, input: Image): TransformResult? {
        val inputBitmap = (input as? BitmapImage)?.bitmap ?: return null
        val scale = min(maxSize / input.width.toFloat(), maxSize / input.height.toFloat())
        val scaledBitmap = inputBitmap.scale(scale)
        return TransformResult(scaledBitmap.asImage(), key)
    }
}