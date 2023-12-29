package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.sample.compose.widget.ZoomImageMinimap
import com.github.panpf.zoomimage.sample.compose.widget.ZoomImageTool
import com.github.panpf.zoomimage.sample.compose.widget.rememberMyDialogState
import com.github.panpf.zoomimage.sample.settingsService
import com.github.panpf.zoomimage.sample.ui.util.compose.valueOf
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator

@Composable
fun BaseZoomImageSample(
    sketchImageUri: String,
    supportIgnoreExifOrientation: Boolean,
    content: @Composable BoxScope.(
        contentScale: ContentScale,
        alignment: Alignment,
        state: ZoomState,
        ignoreExifOrientation: Boolean,
        scrollBar: ScrollBarSpec?,
    ) -> Unit
) {
    val context = LocalContext.current
    val settingsService = remember { context.settingsService }
    val contentScaleName by settingsService.contentScale.collectAsState()
    val alignmentName by settingsService.alignment.collectAsState()
    val threeStepScale by settingsService.threeStepScale.collectAsState()
    val rubberBandScale by settingsService.rubberBandScale.collectAsState()
    val readModeEnabled by settingsService.readModeEnabled.collectAsState()
    val readModeAcceptedBoth by settingsService.readModeAcceptedBoth.collectAsState()
    val scrollBarEnabled by settingsService.scrollBarEnabled.collectAsState()
    val logLevelName by settingsService.logLevel.collectAsState()
    val animateScale by settingsService.animateScale.collectAsState()
    val slowerScaleAnimation by settingsService.slowerScaleAnimation.collectAsState()
    val limitOffsetWithinBaseVisibleRect by settingsService.limitOffsetWithinBaseVisibleRect.collectAsState()
    val scalesCalculatorName by settingsService.scalesCalculator.collectAsState()
    val scalesMultipleString by settingsService.scalesMultiple.collectAsState()
    val pausedContinuousTransformType by settingsService.pausedContinuousTransformType.collectAsState()
    val disabledGestureType by settingsService.disabledGestureType.collectAsState()
    val disabledBackgroundTiles by settingsService.disabledBackgroundTiles.collectAsState()
    val ignoreExifOrientation by settingsService.ignoreExifOrientation.collectAsState()
    val showTileBounds by settingsService.showTileBounds.collectAsState()
    val tileAnimation by settingsService.tileAnimation.collectAsState()
    val horizontalLayout by settingsService.horizontalPagerLayout.collectAsState(initial = true)

    val scalesCalculator by remember {
        derivedStateOf {
            val scalesMultiple = scalesMultipleString.toFloat()
            if (scalesCalculatorName == "Dynamic") {
                ScalesCalculator.dynamic(scalesMultiple)
            } else {
                ScalesCalculator.fixed(scalesMultiple)
            }
        }
    }
    val contentScale by remember { derivedStateOf { ContentScale.valueOf(contentScaleName) } }
    val alignment by remember { derivedStateOf { Alignment.valueOf(alignmentName) } }
    val zoomAnimationSpec by remember {
        derivedStateOf {
            val durationMillis = if (animateScale) (if (slowerScaleAnimation) 3000 else 300) else 0
            ZoomAnimationSpec.Default.copy(durationMillis = durationMillis)
        }
    }
    val readMode by remember {
        derivedStateOf {
            val sizeType = when {
                readModeAcceptedBoth -> ReadMode.SIZE_TYPE_HORIZONTAL or ReadMode.SIZE_TYPE_VERTICAL
                horizontalLayout -> ReadMode.SIZE_TYPE_VERTICAL
                else -> ReadMode.SIZE_TYPE_HORIZONTAL
            }
            if (readModeEnabled) ReadMode.Default.copy(sizeType = sizeType) else null
        }
    }
    val logLevel by remember { derivedStateOf { Logger.level(logLevelName) } }
    val zoomState = rememberZoomState(rememberZoomImageLogger(level = logLevel)).apply {
        LaunchedEffect(threeStepScale) {
            zoomable.threeStepScale = threeStepScale
        }
        LaunchedEffect(rubberBandScale) {
            zoomable.rubberBandScale = rubberBandScale
        }
        LaunchedEffect(zoomAnimationSpec) {
            zoomable.animationSpec = zoomAnimationSpec
        }
        LaunchedEffect(scalesCalculator) {
            zoomable.scalesCalculator = scalesCalculator
        }
        LaunchedEffect(limitOffsetWithinBaseVisibleRect) {
            zoomable.limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect
        }
        LaunchedEffect(readMode) {
            zoomable.readMode = readMode
        }
        LaunchedEffect(disabledGestureType) {
            zoomable.disabledGestureType = disabledGestureType.toInt()
        }
        LaunchedEffect(pausedContinuousTransformType) {
            subsampling.pausedContinuousTransformType = pausedContinuousTransformType.toInt()
        }
        LaunchedEffect(disabledBackgroundTiles) {
            subsampling.disabledBackgroundTiles = disabledBackgroundTiles
        }
        if (supportIgnoreExifOrientation) {
            LaunchedEffect(ignoreExifOrientation) {
                subsampling.ignoreExifOrientation = ignoreExifOrientation
            }
        }
        LaunchedEffect(showTileBounds) {
            subsampling.showTileBounds = showTileBounds
        }
        LaunchedEffect(tileAnimation) {
            subsampling.tileAnimationSpec =
                if (tileAnimation) TileAnimationSpec.Default else TileAnimationSpec.None
        }
    }
    val infoDialogState = rememberMyDialogState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        content(
            contentScale,
            alignment,
            zoomState,
            supportIgnoreExifOrientation && ignoreExifOrientation,
            if (scrollBarEnabled) ScrollBarSpec.Default else null
        )

        ZoomImageMinimap(
            imageUri = sketchImageUri,
            zoomableState = zoomState.zoomable,
            subsamplingState = zoomState.subsampling,
            ignoreExifOrientation = supportIgnoreExifOrientation && ignoreExifOrientation,
        )

        ZoomImageTool(
            zoomableState = zoomState.zoomable,
            subsamplingState = zoomState.subsampling,
            infoDialogState = infoDialogState,
            imageUri = sketchImageUri,
        )
    }
}