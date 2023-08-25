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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.settingsService
import com.github.panpf.zoomimage.sample.ui.common.compose.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.util.compose.valueOf
import com.github.panpf.zoomimage.sample.ui.widget.compose.ZoomImageMinimap
import com.github.panpf.zoomimage.zoom.StepScalesComputer

@Composable
fun BaseZoomImageSample(
    sketchImageUri: String,
    supportIgnoreExifOrientation: Boolean,
    content: @Composable BoxScope.(
        contentScale: ContentScale,
        alignment: Alignment,
        state: ZoomState,
        ignoreExifOrientation: Boolean,
        scrollBarSpec: ScrollBarSpec?,
        onLongPress: ((Offset) -> Unit),
    ) -> Unit
) {
    val context = LocalContext.current
    val settingsService = remember { context.settingsService }
    val contentScaleName by settingsService.contentScale.stateFlow.collectAsState()
    val alignmentName by settingsService.alignment.stateFlow.collectAsState()
    val threeStepScale by settingsService.threeStepScale.stateFlow.collectAsState()
    val rubberBandScale by settingsService.rubberBandScale.stateFlow.collectAsState()
    val readModeEnabled by settingsService.readModeEnabled.stateFlow.collectAsState()
    val readModeDirectionBoth by settingsService.readModeDirectionBoth.stateFlow.collectAsState()
    val scrollBarEnabled by settingsService.scrollBarEnabled.stateFlow.collectAsState()
    val animateScale by settingsService.animateScale.stateFlow.collectAsState()
    val slowerScaleAnimation by settingsService.slowerScaleAnimation.stateFlow.collectAsState()
    val limitOffsetWithinBaseVisibleRect by settingsService.limitOffsetWithinBaseVisibleRect.stateFlow.collectAsState()
    val stepScalesComputerName by settingsService.stepScalesComputer.stateFlow.collectAsState()
    val stepScaleMultipleString by settingsService.stepScaleMultiple.stateFlow.collectAsState()
    val ignoreExifOrientation by settingsService.ignoreExifOrientation.stateFlow.collectAsState()
    val showTileBounds by settingsService.showTileBounds.stateFlow.collectAsState()
    val horizontalLayout by settingsService.horizontalPagerLayout.stateFlow.collectAsState(initial = true)

    val stepScalesComputer by remember {
        derivedStateOf {
            val stepScaleMultiple = stepScaleMultipleString.toFloat()
            if (stepScalesComputerName == "Dynamic") {
                StepScalesComputer.dynamic(stepScaleMultiple)
            } else {
                StepScalesComputer.fixed(stepScaleMultiple)
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
            val readModeDirection = when {
                readModeDirectionBoth -> ReadMode.Direction.Both
                horizontalLayout -> ReadMode.Direction.OnlyVertical
                else -> ReadMode.Direction.OnlyHorizontal
            }
            if (readModeEnabled) ReadMode.Default.copy(direction = readModeDirection) else null
        }
    }
    val logLevel by remember { mutableStateOf(if (BuildConfig.DEBUG) Logger.DEBUG else Logger.INFO) }
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
        LaunchedEffect(stepScalesComputer) {
            zoomable.stepScalesComputer = stepScalesComputer
        }
        LaunchedEffect(limitOffsetWithinBaseVisibleRect) {
            zoomable.limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect
        }
        LaunchedEffect(readMode) {
            zoomable.readMode = readMode
        }
        LaunchedEffect(ignoreExifOrientation) {
            subsampling.ignoreExifOrientation = ignoreExifOrientation
        }
        LaunchedEffect(showTileBounds) {
            subsampling.showTileBounds = showTileBounds
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
        ) { infoDialogState.showing = true }

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