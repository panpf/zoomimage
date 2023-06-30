package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.zoomimage.AnimationConfig
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.rememberZoomableState
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.widget.compose.ZoomImageMinimap
import com.google.accompanist.drawablepainter.DrawablePainter

@Composable
fun ZoomImageSample(sketchImageUri: String) {
    val zoomImageOptionsDialogState = rememberZoomImageOptionsDialogState()
    val zoomableState = rememberZoomableState(threeStepScaleEnabled = zoomImageOptionsDialogState.threeStepScaleEnabled, debugMode = BuildConfig.DEBUG)
    val animationDurationMillisState = remember(zoomImageOptionsDialogState.slowerScaleAnimation) {
        mutableStateOf(if (zoomImageOptionsDialogState.slowerScaleAnimation) 3000 else AnimationConfig.DefaultDurationMillis)
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val context = LocalContext.current
        var drawablePainter: Painter? by remember { mutableStateOf(null) }
        LaunchedEffect(sketchImageUri) {
            val drawable = DisplayRequest(context, sketchImageUri) {
                crossfade()
            }.execute().drawable
            drawablePainter = drawable?.let { DrawablePainter(it) }
        }

        val drawablePainter1 = drawablePainter
        if (drawablePainter1 != null) {
            ZoomImage(
                painter = drawablePainter1,
                contentDescription = "",
                contentScale = zoomImageOptionsDialogState.contentScale,
                alignment = zoomImageOptionsDialogState.alignment,
                modifier = Modifier.fillMaxSize(),
                state = zoomableState,
                animationConfig = AnimationConfig(
                    doubleTapScaleEnabled = zoomImageOptionsDialogState.animateScale,
                    durationMillis = animationDurationMillisState.value,
                ),
                scrollBarEnabled = zoomImageOptionsDialogState.scrollBarEnabled,
            )
        }

        ZoomImageMinimap(
            sketchImageUri = sketchImageUri,
            state = zoomableState,
            animateScale = zoomImageOptionsDialogState.animateScale,
            animationDurationMillis = animationDurationMillisState.value,
        )

        ZoomImageTool(
            zoomableState = zoomableState,
            zoomImageOptionsDialogState = zoomImageOptionsDialogState
        )
    }
}

@Preview
@Composable
private fun ZoomImageSamplePreview() {
    ZoomImageSample(newResourceUri(R.drawable.im_placeholder))
}