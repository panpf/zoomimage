package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.github.panpf.zoomimage.sample.image.PhotoPalette

@Composable
expect fun PhotoPagerBackground(
    sketchImageUri: String,
    photoPaletteState: MutableState<PhotoPalette>
)