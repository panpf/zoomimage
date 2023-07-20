package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.github.panpf.sketch.sketch
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ZoomImage
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
import com.github.panpf.zoomimage.sketch.internal.SketchImageSource
import com.github.panpf.zoomimage.sketch.internal.SketchTileBitmapPool
import com.github.panpf.zoomimage.sketch.internal.SketchTileMemoryCache
import com.github.panpf.zoomimage.subsampling.rememberSubsamplingState
import com.google.accompanist.drawablepainter.DrawablePainter

@Composable
fun ZoomImageSample(sketchImageUri: String) {
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
    val showTileBounds by prefsService.showTileBounds.stateFlow.collectAsState()
    val ignoreExifOrientation by prefsService.ignoreExifOrientation.stateFlow.collectAsState()
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
    val subsamplingState = rememberSubsamplingState(
        logger = logger,
        showTileBounds = showTileBounds,
    )
    LaunchedEffect(Unit) {
        subsamplingState.tileBitmapPool = SketchTileBitmapPool(context.sketch)
        subsamplingState.tileMemoryCache = SketchTileMemoryCache(context.sketch)
    }
    LaunchedEffect(ignoreExifOrientation) {
        subsamplingState.ignoreExifOrientation = ignoreExifOrientation
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        var drawablePainter: Painter? by remember { mutableStateOf(null) }
        LaunchedEffect(sketchImageUri, ignoreExifOrientation) {
            val drawable = DisplayRequest(context, sketchImageUri) {
                crossfade()
                ignoreExifOrientation(ignoreExifOrientation)
            }.execute().drawable
            drawablePainter = drawable?.let { DrawablePainter(it) }

            val imageSource = SketchImageSource(context, context.sketch, sketchImageUri)
            subsamplingState.setImageSource(imageSource)
        }

        val drawablePainter1 = drawablePainter
        if (drawablePainter1 != null) {
            ZoomImage(
                painter = drawablePainter1,
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
        }

        ZoomImageMinimap(
            sketchImageUri = sketchImageUri,
            zoomableState = zoomableState,
            subsamplingState = subsamplingState,
            ignoreExifOrientation = ignoreExifOrientation,
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
private fun ZoomImageSamplePreview() {
    ZoomImageSample(newResourceUri(R.drawable.im_placeholder))
}
