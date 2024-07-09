package com.github.panpf.zoomimage.sample.image

import coil3.BitmapImage
import coil3.Extras
import coil3.getExtra
import coil3.intercept.Interceptor
import coil3.request.ImageResult
import coil3.request.SuccessResult
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.internal.RequestContext
import com.github.panpf.sketch.transform.BlurTransformation

class CoilBlurInterceptor(
    val sketch: Sketch,
    /** Blur radius. range from = 0, to = 100 */
    val radius: Int = 15,

    /** If the Bitmap has transparent pixels, it will force the Bitmap to add an opaque background color and then blur it */
    val hasAlphaBitmapBgColor: Int? = 0xFF000000L.toInt(),

    /** Overlay the blurred image with a layer of color, often useful when using images as a background */
    val maskColor: Int? = null,
) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val result = chain.proceed()
        if (result is SuccessResult) {
            val request = chain.request
            val executeBlur = request.getExtra(blurKey)
            if (executeBlur) {
                val image = result.image
                if (image is BitmapImage) {
                    val image = image.asSketchImage()
                    val requestContext = RequestContext(sketch, ImageRequest(sketch.context, ""))
                    val blurTransformation =
                        BlurTransformation(radius, hasAlphaBitmapBgColor, maskColor)
                    val blurResult = blurTransformation.transform(sketch, requestContext, image)
                    val newBitmapImage = blurResult.image.asCoilBitmapImage()
                    return SuccessResult(
                        image = newBitmapImage,
                        request = result.request,
                        dataSource = result.dataSource,
                        memoryCacheKey = result.memoryCacheKey,
                        isSampled = result.isSampled,
                        diskCacheKey = result.diskCacheKey,
                        isPlaceholderCached = result.isPlaceholderCached,
                    )
                }
            }
        }
        return result
    }

    companion object {
        val blurKey = Extras.Key(false)
    }
}

expect fun BitmapImage.asSketchImage(): com.github.panpf.sketch.Image

expect fun com.github.panpf.sketch.Image.asCoilBitmapImage(): BitmapImage