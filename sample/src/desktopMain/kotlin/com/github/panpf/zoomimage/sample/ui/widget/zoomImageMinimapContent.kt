package com.github.panpf.zoomimage.sample.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.panpf.sketch.AsyncImage

@Composable
actual fun ZoomImageMinimapContent(
    imageUri: String,
    modifier: Modifier,
) {
    AsyncImage(
        uri = imageUri,
        contentDescription = "Minimap",
        modifier = modifier,
    )
}