package com.github.panpf.zoomimage.sample.ui.photoalbum.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.github.panpf.zoomimage.compose.glide.internal.CrossFade
import com.github.panpf.zoomimage.compose.glide.internal.ExperimentalGlideComposeApi
import com.github.panpf.zoomimage.compose.glide.internal.GlideImage
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.util.sketchUri2GlideModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlideListImage(sketchImageUri: String, modifier: Modifier) {
    val glideModel = remember(sketchImageUri) {
        sketchUri2GlideModel(sketchImageUri)
    }
    GlideImage(
        model = glideModel,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        contentDescription = "photo",
        transition = CrossFade
    ) {
        it.placeholder(R.drawable.im_placeholder)
            .error(R.drawable.im_error)
    }
}