package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.compose.ScrollBar
import com.github.panpf.zoomimage.compose.ZoomAnimationSpec
import com.github.panpf.zoomimage.rememberZoomableState
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.prefsService
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
    val context = LocalContext.current
    val horizontalLayout by context.prefsService.horizontalPagerLayout.stateFlow
        .collectAsState(initial = true)
    val readModeDirection = remember(
        horizontalLayout,
        zoomImageOptionsDialogState.readModeDirectionBoth
    ) {
        if (zoomImageOptionsDialogState.readModeDirectionBoth) {
            ReadMode.Direction.Both
        } else if (horizontalLayout) {
            ReadMode.Direction.OnlyVertical
        } else {
            ReadMode.Direction.OnlyHorizontal
        }
    }
    val readMode =
        if (zoomImageOptionsDialogState.readModeEnabled) ReadMode.Default.copy(direction = readModeDirection) else null
    val zoomableState = rememberZoomableState(
        threeStepScale = zoomImageOptionsDialogState.threeStepScale,
        rubberBandScale = zoomImageOptionsDialogState.rubberBandScale,
        animationSpec = zoomAnimationSpec.value,
        readMode = readMode,
        debugMode = BuildConfig.DEBUG
    )
    val infoDialogState = rememberZoomImageInfoDialogState()
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
            scrollBar = if(zoomImageOptionsDialogState.scrollBarEnabled) ScrollBar.Default else null,
            onLongPress = {
                infoDialogState.showing = true
            }
        )

        ZoomImageMinimap(
            sketchImageUri = sketchImageUri,
            state = zoomableState,
        )

        ZoomImageTool(
            zoomableState = zoomableState,
            optionsDialogState = zoomImageOptionsDialogState,
            infoDialogState = infoDialogState,
            imageUri = sketchImageUri,
        )
    }
}

@Preview
@Composable
private fun SketchZoomAsyncImageSamplePreview() {
    SketchZoomAsyncImageSample(newResourceUri(R.drawable.im_placeholder))
}