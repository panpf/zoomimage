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
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.sample.settingsService
import com.github.panpf.zoomimage.sample.ui.common.compose.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.util.compose.valueOf
import com.github.panpf.zoomimage.sample.ui.widget.compose.ZoomImageMinimap
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.vibration

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
    val contentScaleName by settingsService.contentScale.stateFlow.collectAsState()
    val alignmentName by settingsService.alignment.stateFlow.collectAsState()
    val threeStepScale by settingsService.threeStepScale.stateFlow.collectAsState()
    val oneFingerScale by settingsService.oneFingerScale.stateFlow.collectAsState()
    val rubberBandScale by settingsService.rubberBandScale.stateFlow.collectAsState()
    val readModeEnabled by settingsService.readModeEnabled.stateFlow.collectAsState()
    val readModeAcceptedBoth by settingsService.readModeAcceptedBoth.stateFlow.collectAsState()
    val scrollBarEnabled by settingsService.scrollBarEnabled.stateFlow.collectAsState()
    val logLevelName by settingsService.logLevel.stateFlow.collectAsState()
    val animateScale by settingsService.animateScale.stateFlow.collectAsState()
    val slowerScaleAnimation by settingsService.slowerScaleAnimation.stateFlow.collectAsState()
    val limitOffsetWithinBaseVisibleRect by settingsService.limitOffsetWithinBaseVisibleRect.stateFlow.collectAsState()
    val scalesCalculatorName by settingsService.scalesCalculator.stateFlow.collectAsState()
    val scalesMultipleString by settingsService.scalesMultiple.stateFlow.collectAsState()
    val pausedContinuousTransformType by settingsService.pausedContinuousTransformType.stateFlow.collectAsState()
    val disabledGestureType by settingsService.disabledGestureType.stateFlow.collectAsState()
    val disabledBackgroundTiles by settingsService.disabledBackgroundTiles.stateFlow.collectAsState()
    val ignoreExifOrientation by settingsService.ignoreExifOrientation.stateFlow.collectAsState()
    val showTileBounds by settingsService.showTileBounds.stateFlow.collectAsState()
    val tileAnimation by settingsService.tileAnimation.stateFlow.collectAsState()
    val horizontalLayout by settingsService.horizontalPagerLayout.stateFlow.collectAsState(initial = true)

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
    val zoomState = rememberZoomState().apply {
        LaunchedEffect(logLevel) {
            logger.level = logLevel
        }
        LaunchedEffect(threeStepScale) {
            zoomable.threeStepScale = threeStepScale
        }
        LaunchedEffect(oneFingerScale) {
            zoomable.oneFingerScaleSpec = if (oneFingerScale)
                OneFingerScaleSpec.vibration(context) else null
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
        LaunchedEffect(ignoreExifOrientation) {
            subsampling.ignoreExifOrientation = ignoreExifOrientation
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
            sketchImageUri = sketchImageUri,
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