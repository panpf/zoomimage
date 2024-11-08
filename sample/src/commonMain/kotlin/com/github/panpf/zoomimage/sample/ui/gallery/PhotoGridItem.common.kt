package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.runtime.Composable
import com.github.panpf.zoomimage.sample.ui.model.Photo

@Composable
expect fun PhotoGridItem(
    index: Int,
    photo: Photo,
    staggeredGridMode: Boolean = false,
    onClick: (photo: Photo, index: Int) -> Unit,
)