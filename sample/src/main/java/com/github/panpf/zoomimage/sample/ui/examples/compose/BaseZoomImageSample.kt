package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.ui.common.compose.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.util.compose.alignment
import com.github.panpf.zoomimage.sample.ui.util.compose.contentScale
import com.github.panpf.zoomimage.sample.ui.widget.compose.ZoomImageMinimap

@Composable
fun BaseZoomImageSample(
    logger: Logger,
    sketchImageUri: String,
    supportIgnoreExifOrientation: Boolean,
    content: @Composable BoxScope.(
        contentScale: ContentScale,
        alignment: Alignment,
        zoomableState: ZoomableState,
        subsamplingState: SubsamplingState,
        ignoreExifOrientation: Boolean,
        scrollBarSpec: ScrollBarSpec?,
        onLongPress: ((Offset) -> Unit),
    ) -> Unit
) {
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
    val zoomableState = rememberZoomableState(
        logger = logger,
        threeStepScale = threeStepScale,
        rubberBandScale = rubberBandScale,
        animationSpec = zoomAnimationSpec.value,
        readMode = readMode,
    )
    val infoDialogState = rememberMyDialogState()
    val subsamplingState = rememberSubsamplingState(logger)
    LaunchedEffect(ignoreExifOrientation) {
        subsamplingState.ignoreExifOrientation = ignoreExifOrientation
    }
    logger.level = if (BuildConfig.DEBUG) Logger.DEBUG else Logger.INFO
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        content(
            contentScale = contentScale,
            alignment = alignment,
            zoomableState = zoomableState,
            subsamplingState = subsamplingState,
            ignoreExifOrientation = supportIgnoreExifOrientation && ignoreExifOrientation,
            scrollBarSpec = if (scrollBarEnabled) ScrollBarSpec.Default else null,
            onLongPress = { infoDialogState.showing = true }
        )

        ZoomImageMinimap(
            sketchImageUri = sketchImageUri,
            zoomableState = zoomableState,
            subsamplingState = subsamplingState,
            ignoreExifOrientation = supportIgnoreExifOrientation && ignoreExifOrientation,
        )

        ZoomImageTool(
            zoomableState = zoomableState,
            subsamplingState = subsamplingState,
            infoDialogState = infoDialogState,
            imageUri = sketchImageUri,
        )
    }
}