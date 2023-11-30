package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.tools4a.toast.ktx.showShortToast
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString

@Composable
fun SketchZoomAsyncImageSample(sketchImageUri: String) {
    BaseZoomImageSample(
        sketchImageUri = sketchImageUri,
        supportIgnoreExifOrientation = true
    ) { contentScale, alignment, state, ignoreExifOrientation, scrollBar ->
        val context = LocalContext.current
        SketchZoomAsyncImage(
            request = DisplayRequest(context, sketchImageUri) {
//                placeholder(IconStateImage(drawable.ic_image_outline, IntColor(Color.BLACK)))
//                placeholder(R.mipmap.ic_launcher)
//                placeholder(ThumbnailMemoryCacheStateImage())
                // todo Test placeholder
                ignoreExifOrientation(ignoreExifOrientation)
                crossfade()
//                addTransformations(object : Transformation {
//                    override val key: String
//                        get() = "DelayTransformation"
//
//                    override suspend fun transform(
//                        sketch: Sketch,
//                        requestContext: RequestContext,
//                        input: Bitmap
//                    ): TransformResult? {
//                        delay(10000)
//                        return null
//                    }
//                })
            },
            contentDescription = "view image",
            contentScale = contentScale,
            alignment = alignment,
            modifier = Modifier.fillMaxSize(),
            state = state,
            scrollBar = scrollBar,
            onTap = {
                context.showShortToast("Click (${it.toShortString()})")
            },
            onLongPress = {
                context.showShortToast("Long click (${it.toShortString()})")
            }
        )
    }
}

@Preview
@Composable
private fun SketchZoomAsyncImageSamplePreview() {
    SketchZoomAsyncImageSample(newResourceUri(R.drawable.im_placeholder))
}