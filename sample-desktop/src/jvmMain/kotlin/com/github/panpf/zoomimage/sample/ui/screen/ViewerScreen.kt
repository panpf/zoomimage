package com.github.panpf.zoomimage.sample.ui.screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.ResourceLoader
import androidx.compose.ui.res.painterResource
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.subsampling.fromResource
import com.github.panpf.zoomimage.sample.compose.widget.ZoomImageMinimap
import com.github.panpf.zoomimage.sample.compose.widget.ZoomImageTool
import com.github.panpf.zoomimage.sample.compose.widget.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.model.ImageResource
import com.github.panpf.zoomimage.sample.ui.navigation.Navigation
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun ViewerScreen(navigation: Navigation, imageResource: ImageResource) {
    Box(Modifier.fillMaxSize()) {
        val zoomState = rememberZoomState()
        LaunchedEffect(Unit) {
            zoomState.logger.level = Logger.DEBUG
            val imageSource =
                ImageSource.fromResource(ResourceLoader.Default, imageResource.resourcePath)
            zoomState.subsampling.setImageSource(imageSource)
            zoomState.zoomable.oneFingerScaleSpec = OneFingerScaleSpec.Default
        }

        ZoomImage(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(imageResource.thumbnailResourcePath),
            contentDescription = "Viewer",
            state = zoomState,
        )

        ZoomImageMinimap(
            imageUri = imageResource.thumbnailResourcePath,
            zoomableState = zoomState.zoomable,
            subsamplingState = zoomState.subsampling,
        )

        val infoDialogState = rememberMyDialogState()
        ZoomImageTool(
            imageUri = imageResource.thumbnailResourcePath,
            zoomableState = zoomState.zoomable,
            subsamplingState = zoomState.subsampling,
            infoDialogState = infoDialogState,
        )
    }
}