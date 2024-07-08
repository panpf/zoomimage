package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.examples.BasicZoomImageSample
import com.github.panpf.zoomimage.sample.ui.examples.CoilZoomAsyncImageSample
import com.github.panpf.zoomimage.sample.ui.examples.SketchZoomAsyncImageSample
import com.github.panpf.zoomimage.sample.ui.photoalbum.compose.CoilListImage
import com.github.panpf.zoomimage.sample.ui.photoalbum.compose.GlideListImage
import com.github.panpf.zoomimage.sample.ui.photoalbum.compose.SketchListImage

enum class ZoomImageType(
    val title: String,
    val subtitle: String?,
    val drawListContent: @Composable (sketchImageUri: String, modifier: Modifier) -> Unit,
    val drawContent: @Composable (sketchImageUri: String) -> Unit,
    val my: Boolean,
) {
    MyZoomImage(
        title = "ZoomImage",
        subtitle = null,
        drawListContent = { sketchImageUri, modifier ->
            SketchListImage(sketchImageUri, modifier)
        },
        drawContent = { sketchImageUri ->
            val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
            BasicZoomImageSample(
                sketchImageUri = sketchImageUri,
                photoPaletteState = remember { mutableStateOf(PhotoPalette(colorScheme)) }
            )
        },
        my = true,
    ),

    SketchZoomAsyncImage(
        title = "SketchZoomAsyncImage",
        subtitle = null,
        drawListContent = { sketchImageUri, modifier ->
            SketchListImage(sketchImageUri, modifier)
        },
        drawContent = { sketchImageUri ->
            val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
            SketchZoomAsyncImageSample(
                sketchImageUri = sketchImageUri,
                photoPaletteState = mutableStateOf(PhotoPalette(colorScheme))
            )
        },
        my = true,
    ),

    CoilZoomAsyncImage(
        title = "CoilZoomAsyncImage",
        subtitle = null,
        drawListContent = { sketchImageUri, modifier ->
            CoilListImage(sketchImageUri, modifier)
        },
        drawContent = { sketchImageUri ->
            val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
            CoilZoomAsyncImageSample(
                sketchImageUri = sketchImageUri,
                photoPaletteState = remember { mutableStateOf(PhotoPalette(colorScheme)) }
            )
        },
        my = true,
    ),

    GlideZoomAsyncImage(
        title = "GlideZoomAsyncImage",
        subtitle = null,
        drawListContent = { sketchImageUri, modifier ->
            GlideListImage(sketchImageUri, modifier)
        },
        drawContent = { sketchImageUri ->
            val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
            GlideZoomAsyncImageSample(
                sketchImageUri = sketchImageUri,
                photoPaletteState = remember { mutableStateOf(PhotoPalette(colorScheme)) }
            )
        },
        my = true,
    ),

    TelephotoZoomableAsyncImage(
        title = "ZoomableAsyncImage",
        subtitle = "Telephoto",
        drawListContent = { sketchImageUri, modifier ->
            CoilListImage(sketchImageUri, modifier)
        },
        drawContent = { sketchImageUri ->
            TelephotoZoomableAsyncImageSample(sketchImageUri)
        },
        my = false,
    ),
}