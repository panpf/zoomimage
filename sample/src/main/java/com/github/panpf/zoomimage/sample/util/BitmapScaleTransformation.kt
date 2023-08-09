package com.github.panpf.zoomimage.sample.util

import android.graphics.Bitmap
import androidx.core.graphics.scale
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.internal.RequestContext
import com.github.panpf.sketch.transform.TransformResult
import com.github.panpf.sketch.transform.Transformation
import kotlin.math.min
import kotlin.math.roundToInt

class BitmapScaleTransformation(val maxSize: Int) : Transformation {

    override val key: String = "BitmapScaleTransformation($maxSize)"

    override suspend fun transform(
        sketch: Sketch,
        requestContext: RequestContext,
        input: Bitmap
    ): TransformResult {
        val scale = min(maxSize / input.width.toFloat(), maxSize / input.height.toFloat())
        val scaledBitmap = input.scale(
            (input.width * scale).roundToInt(),
            (input.height * scale).roundToInt(),
            true
        )
        return TransformResult(scaledBitmap, key)
    }
}