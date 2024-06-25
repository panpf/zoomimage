package com.github.panpf.zoomimage.sample.ui.photoalbum.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.resize.LongImageClipPrecisionDecider
import com.github.panpf.sketch.resize.LongImageStartCropScaleDecider
import com.github.panpf.sketch.resize.Precision.SAME_ASPECT_RATIO
import com.github.panpf.sketch.state.IconDrawableStateImage
import com.github.panpf.zoomimage.sample.R.color
import com.github.panpf.zoomimage.sample.R.drawable

@Composable
fun SketchListImage(sketchImageUri: String, modifier: Modifier) {
    val context = LocalContext.current
    AsyncImage(
        request = ImageRequest(context, sketchImageUri) {
            placeholder(
                IconDrawableStateImage(
                    icon = drawable.ic_image_outline,
                    background = color.placeholder_bg
                )
            )
            error(
                IconDrawableStateImage(
                    icon = drawable.ic_error_baseline,
                    background = color.placeholder_bg
                )
            )
            crossfade()
            resizeOnDraw()
            precision(LongImageClipPrecisionDecider(SAME_ASPECT_RATIO))
            scale(LongImageStartCropScaleDecider())
        },
        modifier = modifier,
        contentScale = ContentScale.Crop,
        contentDescription = "photo",
    )
}