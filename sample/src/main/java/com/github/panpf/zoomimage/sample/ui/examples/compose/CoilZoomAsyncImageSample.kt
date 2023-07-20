package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import coil.request.ImageRequest
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.compose.ScrollBarSpec
import com.github.panpf.zoomimage.compose.ZoomAnimationSpec
import com.github.panpf.zoomimage.rememberLogger
import com.github.panpf.zoomimage.rememberZoomableState
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.ui.util.compose.alignment
import com.github.panpf.zoomimage.sample.ui.util.compose.contentScale
import com.github.panpf.zoomimage.sample.ui.widget.compose.ZoomImageMinimap
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel
import com.github.panpf.zoomimage.subsampling.rememberSubsamplingState

@Composable
fun CoilZoomAsyncImageSample(sketchImageUri: String) {
    val context = LocalContext.current
    val prefsService = remember { context.prefsService }
    val contentScaleName by prefsService.contentScale.stateFlow.collectAsState()
    val alignmentName by prefsService.alignment.stateFlow.collectAsState()
    val contentScale = remember(contentScaleName) { contentScale(contentScaleName) }
    val alignment = remember(alignmentName) { alignment(alignmentName) }
    val threeStepScale by prefsService.threeStepScale.stateFlow.collectAsState()
    val rubberBandScale by prefsService.rubberBandScale.stateFlow.collectAsState()
    val readModeEnabled by prefsService.readModeEnabled.stateFlow.collectAsState()
    val readModeDirectionBoth by prefsService.readModeDirectionBoth.stateFlow.collectAsState()
    val scrollBarEnabled by prefsService.scrollBarEnabled.stateFlow.collectAsState()
    val animateScale by prefsService.animateScale.stateFlow.collectAsState()
    val slowerScaleAnimation by prefsService.slowerScaleAnimation.stateFlow.collectAsState()
    val zoomAnimationSpec = remember(animateScale, slowerScaleAnimation) {
        val durationMillis = if (animateScale) (if (slowerScaleAnimation) 3000 else 300) else 0
        mutableStateOf(ZoomAnimationSpec.Default.copy(durationMillis = durationMillis))
    }
    val horizontalLayout by prefsService.horizontalPagerLayout.stateFlow.collectAsState(initial = true)
    val readModeDirection = remember(horizontalLayout, readModeDirectionBoth) {
        if (readModeDirectionBoth) {
            ReadMode.Direction.Both
        } else if (horizontalLayout) {
            ReadMode.Direction.OnlyVertical
        } else {
            ReadMode.Direction.OnlyHorizontal
        }
    }
    val readMode =
        if (readModeEnabled) ReadMode.Default.copy(direction = readModeDirection) else null
    val logger = rememberLogger(level = if (BuildConfig.DEBUG) Logger.DEBUG else Logger.INFO)
    val zoomableState = rememberZoomableState(
        logger = logger,
        threeStepScale = threeStepScale,
        rubberBandScale = rubberBandScale,
        animationSpec = zoomAnimationSpec.value,
        readMode = readMode,
    )
    val infoDialogState = rememberZoomImageInfoDialogState()
    val subsamplingState = rememberSubsamplingState(logger)
    val coilData =
        remember(key1 = sketchImageUri) { sketchUri2CoilModel(context, sketchImageUri) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        CoilZoomAsyncImage(
            model = ImageRequest.Builder(LocalContext.current).apply {
                data(coilData)
                crossfade(true)
            }.build(),
            contentDescription = "",
            contentScale = contentScale,
            alignment = alignment,
            modifier = Modifier.fillMaxSize(),
            zoomableState = zoomableState,
            subsamplingState = subsamplingState,
            scrollBarSpec = if (scrollBarEnabled) ScrollBarSpec.Default else null,
            onLongPress = {
                infoDialogState.showing = true
            }
        )

        ZoomImageMinimap(
            sketchImageUri = sketchImageUri,
            zoomableState = zoomableState,
            subsamplingState = subsamplingState,
        )

        ZoomImageTool(
            zoomableState = zoomableState,
            infoDialogState = infoDialogState,
            imageUri = sketchImageUri,
        )
    }
}

@Preview
@Composable
private fun CoilZoomAsyncImageSamplePreview() {
    CoilZoomAsyncImageSample(newResourceUri(R.drawable.im_placeholder))
}