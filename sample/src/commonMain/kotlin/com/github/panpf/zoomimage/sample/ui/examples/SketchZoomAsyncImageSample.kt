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
import com.github.panpf.sketch.request.LoadState
import com.github.panpf.sketch.state.ThumbnailMemoryCacheStateImage
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.components.MyPageState
import com.github.panpf.zoomimage.sample.ui.components.PageState

@Composable
fun SketchZoomAsyncImageSample(
    sketchImageUri: String,
    photoPaletteState: MutableState<PhotoPalette>,
) {
    BaseZoomImageSample(
        sketchImageUri = sketchImageUri,
        photoPaletteState = photoPaletteState
    ) { contentScale, alignment, state, scrollBar, onLongClick ->
        val imageState = rememberAsyncImageState()
        SketchZoomAsyncImage(
            request = ComposableImageRequest(sketchImageUri) {
                placeholder(ThumbnailMemoryCacheStateImage())
                crossfade(fadeStart = false)
            },
            contentDescription = "view image",
            contentScale = contentScale,
            alignment = alignment,
            modifier = Modifier
                .fillMaxSize()
                .progressIndicator(imageState, rememberSectorProgressPainter()),
            state = imageState,
            zoomState = state,
            scrollBar = scrollBar,
            onLongPress = { onLongClick.invoke() }
        )

        val myPageState by remember {
            derivedStateOf {
                if (imageState.loadState is LoadState.Error) {
                    MyPageState.Error { imageState.restart() }
                } else MyPageState.None
            }
        }
        PageState(state = myPageState)
    }
}