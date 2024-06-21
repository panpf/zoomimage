package com.github.panpf.zoomimage.sample.ui.photoalbum.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.resize.LongImageClipPrecisionDecider
import com.github.panpf.sketch.resize.LongImageScaleDecider
import com.github.panpf.sketch.resize.Precision.SAME_ASPECT_RATIO
import com.github.panpf.sketch.stateimage.IconStateImage
import com.github.panpf.zoomimage.sample.R.color
import com.github.panpf.zoomimage.sample.R.drawable

@Composable
fun SketchListImage(sketchImageUri: String, modifier: Modifier) {
    val context = LocalContext.current
    AsyncImage(
        request = DisplayRequest(context, sketchImageUri) {
            placeholder(IconStateImage(drawable.ic_image_outline) {
                resColorBackground(color.placeholder_bg)
            })
            error(IconStateImage(drawable.ic_error_baseline) {
                resColorBackground(color.placeholder_bg)
            })
            crossfade()
            resizeApplyToDrawable()
            resizePrecision(LongImageClipPrecisionDecider(SAME_ASPECT_RATIO))
            resizeScale(LongImageScaleDecider())
        },
        modifier = modifier,
        contentScale = ContentScale.Crop,
        contentDescription = "photo",
    )
}