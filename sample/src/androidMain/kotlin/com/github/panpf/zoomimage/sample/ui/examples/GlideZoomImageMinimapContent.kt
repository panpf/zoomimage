package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.panpf.zoomimage.compose.glide.internal.CrossFade
import com.github.panpf.zoomimage.compose.glide.internal.ExperimentalGlideComposeApi
import com.github.panpf.zoomimage.compose.glide.internal.GlideImage
import com.github.panpf.zoomimage.sample.util.sketchUri2GlideModel

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
fun GlideZoomImageMinimapContent(sketchImageUri: String) {
    val glideModel = remember(sketchImageUri) {
        sketchUri2GlideModel(sketchImageUri)
    }
    GlideImage(
        model = glideModel,
        modifier = Modifier.fillMaxSize(),
        contentDescription = "Minimap",
        transition = CrossFade
    )
}