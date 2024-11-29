package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest.Builder
import coil3.request.crossfade
import coil3.size.Precision
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.rememberCoilZoomState
import com.github.panpf.zoomimage.sample.image.CoilComposeResourceSubsamplingImageGenerator
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.image.sketchUri2CoilModel
import com.github.panpf.zoomimage.sample.ui.components.PageState
import com.github.panpf.zoomimage.sample.ui.model.Photo
import kotlinx.collections.immutable.toImmutableList

expect fun platformCoilComposeSubsamplingImageGenerator(): List<CoilComposeSubsamplingImageGenerator>?

@Composable
fun CoilZoomAsyncImageSample(
    photo: Photo,
    photoPaletteState: MutableState<PhotoPalette>,
    pageSelected: Boolean,
) {
    BaseZoomImageSample(
        photo = photo,
        photoPaletteState = photoPaletteState,
        createZoomState = {
            val extensionsModelToImageSources = remember {
                platformCoilComposeSubsamplingImageGenerator().orEmpty()
                    .plus(CoilComposeResourceSubsamplingImageGenerator())
                    .toImmutableList()
            }
            rememberCoilZoomState(subsamplingImageGenerators = extensionsModelToImageSources)
        },
        pageSelected = pageSelected,
    ) { contentScale, alignment, zoomState, scrollBar, onLongClick, onTapClick ->
        var pageState by remember { mutableStateOf<PageState?>(null) }
        val context = LocalPlatformContext.current
        val request = remember(key1 = photo) {
            val model = sketchUri2CoilModel(context, photo.originalUrl)
            Builder(context).apply {
                data(model)
                crossfade(true)
                precision(Precision.EXACT)
                listener(
                    onStart = {
                        pageState = PageState.Loading
                    },
                    onError = { _, _ ->
                        pageState = PageState.Error()
                    },
                    onSuccess = { _, _ ->
                        pageState = null
                    }
                )
            }.build()
        }
        CoilZoomAsyncImage(
            model = request,
            contentDescription = "view image",
            contentScale = contentScale,
            alignment = alignment,
            modifier = Modifier.fillMaxSize(),
            zoomState = zoomState,
            scrollBar = scrollBar,
            onLongPress = {
                onLongClick.invoke()
            },
            onTap = {
                onTapClick.invoke(it)
            }
        )

        PageState(pageState = pageState)
    }
}