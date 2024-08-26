package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.panpf.zoomimage.compose.glide.CrossFade
import com.github.panpf.zoomimage.compose.glide.ExperimentalGlideComposeApi
import com.github.panpf.zoomimage.compose.glide.GlideImage
import com.github.panpf.zoomimage.sample.image.sketchUri2GlideModel

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
fun GlideZoomImageMinimapContent(sketchImageUri: String) {
    val context = LocalContext.current
    val glideModel = remember(sketchImageUri) {
        sketchUri2GlideModel(context, sketchImageUri)
    }
    GlideImage(
        model = glideModel,
        modifier = Modifier.fillMaxSize(),
        contentDescription = "Minimap",
        transition = CrossFade
    )
}