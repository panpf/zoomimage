package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.model.Photo

@Composable
expect fun PhotoDetail(
    photo: Photo,
    photoPaletteState: MutableState<PhotoPalette>,
    pageSelected: Boolean,
)