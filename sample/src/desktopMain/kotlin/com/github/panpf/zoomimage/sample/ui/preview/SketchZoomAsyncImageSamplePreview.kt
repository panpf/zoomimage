package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.github.panpf.sketch.fetch.newComposeResourceUri
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.ui.examples.SketchZoomAsyncImageSample
import com.github.panpf.zoomimage.sample.ui.model.Photo
import org.jetbrains.compose.resources.ExperimentalResourceApi


@OptIn(ExperimentalResourceApi::class)
@Preview
@Composable
fun SketchZoomAsyncImageSamplePreview() {
    val colorScheme = MaterialTheme.colorScheme
    val photo = remember {
        val sketchImageUri = newComposeResourceUri(Res.getUri("files/huge_china.jpg"))
        Photo(sketchImageUri)
    }
    SketchZoomAsyncImageSample(
        photo = photo,
        photoPaletteState = mutableStateOf(PhotoPalette(colorScheme)),
        pageSelected = true
    )
}