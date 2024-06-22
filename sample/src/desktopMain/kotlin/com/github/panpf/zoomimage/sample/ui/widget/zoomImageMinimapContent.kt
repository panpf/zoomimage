package com.github.panpf.zoomimage.sample.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
actual fun ZoomImageMinimapContent(
    imageUri: String,
    modifier: Modifier,
) {
    Image(
        painter = painterResource(imageUri),
        contentDescription = "Minimap",
        modifier = modifier,
    )
}