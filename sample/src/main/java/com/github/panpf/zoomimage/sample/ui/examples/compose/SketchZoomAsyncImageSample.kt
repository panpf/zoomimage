package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ZoomAnimationSpec
import com.github.panpf.zoomimage.rememberZoomableState
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.widget.compose.ZoomImageMinimap
import com.github.panpf.zoomimage.sketch.ZoomAsyncImage

@Composable
fun SketchZoomAsyncImageSample(sketchImageUri: String) {
    val zoomImageOptionsDialogState = rememberZoomImageOptionsDialogState()
    val zoomAnimationSpec = remember(
        zoomImageOptionsDialogState.animateScale,
        zoomImageOptionsDialogState.slowerScaleAnimation
    ) {
        val durationMillis = zoomImageOptionsDialogState.let {
            if (it.animateScale) (if (it.slowerScaleAnimation) 3000 else 300) else 0
        }
        mutableStateOf(ZoomAnimationSpec.Default.copy(durationMillis = durationMillis))
    }
    val zoomableState = rememberZoomableState(
        threeStepScaleEnabled = zoomImageOptionsDialogState.threeStepScaleEnabled,
        animationSpec = zoomAnimationSpec.value,
        readMode = ReadMode.Default.copy(enabled = zoomImageOptionsDialogState.readModeEnabled),
        debugMode = BuildConfig.DEBUG
    )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ZoomAsyncImage(
            request = DisplayRequest(LocalContext.current, sketchImageUri) {
                crossfade()
            },
            contentDescription = "",
            contentScale = zoomImageOptionsDialogState.contentScale,
            alignment = zoomImageOptionsDialogState.alignment,
            modifier = Modifier.fillMaxSize(),
            state = zoomableState,
            scrollBarEnabled = zoomImageOptionsDialogState.scrollBarEnabled,
        )

        ZoomImageMinimap(
            sketchImageUri = sketchImageUri,
            state = zoomableState,
        )

        ZoomImageTool(
            zoomableState = zoomableState,
            optionsDialogState = zoomImageOptionsDialogState,
            imageUri = sketchImageUri,
        )
    }
}

@Preview
@Composable
private fun SketchZoomAsyncImageSamplePreview() {
    SketchZoomAsyncImageSample(newResourceUri(R.drawable.im_placeholder))
}