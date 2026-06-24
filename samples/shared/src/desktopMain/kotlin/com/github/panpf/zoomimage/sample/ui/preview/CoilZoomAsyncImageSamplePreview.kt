package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.examples.CoilZoomAsyncImageSample
import com.github.panpf.zoomimage.sample.ui.model.Photo
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Preview
@Composable
private fun CoilZoomAsyncImageSamplePreview() {
    val colorScheme = MaterialTheme.colorScheme
    val photo = remember { Photo(ComposeResImageFiles.hugeCard.uri) }
    CoilZoomAsyncImageSample(
        photo = photo,
        photoPaletteState = remember { mutableStateOf(PhotoPalette(colorScheme)) },
        pageSelected = true
    )
}