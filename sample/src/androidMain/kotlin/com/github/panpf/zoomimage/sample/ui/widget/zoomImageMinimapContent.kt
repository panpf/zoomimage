package com.github.panpf.zoomimage.sample.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.request.ImageRequest

@Composable
actual fun ZoomImageMinimapContent(
    imageUri: String,
    modifier: Modifier,
) {
    AsyncImage(
        request = ImageRequest(LocalContext.current, imageUri) {
            crossfade()
        },
        contentDescription = "Minimap",
        modifier = modifier
    )
}