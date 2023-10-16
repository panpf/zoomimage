package com.github.panpf.zoomimage.sample.ui.photoalbum.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.photoalbum.view.iconDrawable
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel

@Composable
fun CoilListImage(sketchImageUri: String, modifier: Modifier) {
    val context = LocalContext.current
    val coilModel = remember(sketchImageUri) {
        sketchUri2CoilModel(context, sketchImageUri)
    }
    AsyncImage(
        model = ImageRequest.Builder(context).apply {
            data(coilModel)
            placeholder(iconDrawable(context, R.drawable.ic_image_outline, R.color.placeholder_bg))
            error(R.drawable.im_error)
            crossfade(true)
        }.build(),
        modifier = modifier,
        contentScale = ContentScale.Crop,
        contentDescription = "photo",
    )
}