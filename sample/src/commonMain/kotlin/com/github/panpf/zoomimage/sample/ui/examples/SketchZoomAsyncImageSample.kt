package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.panpf.sketch.ability.progressIndicator
import com.github.panpf.sketch.painter.rememberSectorProgressPainter
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.LoadState
import com.github.panpf.sketch.state.ThumbnailMemoryCacheStateImage
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.components.PageState
import com.github.panpf.zoomimage.sample.ui.model.Photo

expect fun getPlatformSketchZoomAsyncImageSampleImageOptions(): ImageOptions

@Composable
fun SketchZoomAsyncImageSample(
    photo: Photo,
    photoPaletteState: MutableState<PhotoPalette>,
    pageSelected: Boolean,
) {
    BaseZoomImageSample(
        photo = photo,
        photoPaletteState = photoPaletteState,
        createZoomState = { rememberSketchZoomState() },
        pageSelected = pageSelected,
    ) { contentScale, alignment, zoomState, scrollBar, onLongClick, onTapClick ->
        val imageState = rememberAsyncImageState()
        SketchZoomAsyncImage(
            request = ComposableImageRequest(photo.originalUrl) {
                placeholder(ThumbnailMemoryCacheStateImage(photo.listThumbnailUrl))
                crossfade(fadeStart = false)
                merge(getPlatformSketchZoomAsyncImageSampleImageOptions())
            },
            contentDescription = "view image",
            contentScale = contentScale,
            alignment = alignment,
            modifier = Modifier
                .fillMaxSize()
                .progressIndicator(imageState, rememberSectorProgressPainter()),
            state = imageState,
            zoomState = zoomState,
            scrollBar = scrollBar,
            onLongPress = { onLongClick.invoke() },
            onTap = {
                onTapClick.invoke(it)
            }
        )

        val pageState by remember {
            derivedStateOf {
                if (imageState.loadState is LoadState.Error) {
                    PageState.Error { imageState.restart() }
                } else null
            }
        }
        PageState(pageState = pageState)
    }
}