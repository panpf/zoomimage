package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.sample.ui.util.valueOf
import com.github.panpf.zoomimage.sample.ui.widget.MyDialog
import com.github.panpf.zoomimage.sample.ui.widget.ZoomImageInfo
import com.github.panpf.zoomimage.sample.ui.widget.ZoomImageTool
import com.github.panpf.zoomimage.sample.ui.widget.rememberMyDialogState
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import kotlin.math.roundToInt

@Composable
fun BaseZoomImageSample(
    sketchImageUri: String,
    content: @Composable BoxScope.(
        contentScale: ContentScale,
        alignment: Alignment,
        state: ZoomState,
        scrollBar: ScrollBarSpec?,
        onLongClick: () -> Unit
    ) -> Unit
) {
    val settingsService = LocalPlatformContext.current.appSettings
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
            if (scrollBarEnabled) ScrollBarSpec.Default else null
        ) { infoDialogState.show() }

        Row(Modifier.padding(20.dp).padding(top = 80.dp)) {
            val headerInfo = remember {
                """
                    scale: 
                    offset: 
                    rotation: 
                """.trimIndent()
            }
            Text(
                text = headerInfo,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(offset = Offset(0f, 0f), blurRadius = 10f),
                ),
                overflow = TextOverflow.Ellipsis,
            )
            val transformInfo = remember(zoomState.zoomable.transform) {
                val transform = zoomState.zoomable.transform
                """
                    ${transform.scale.toShortString()}
                    ${transform.offset.round().toShortString()}
                    ${transform.rotation.roundToInt()}
                """.trimIndent()
            }
            Text(
                text = transformInfo,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(offset = Offset(0f, 0f), blurRadius = 10f),
                ),
                overflow = TextOverflow.Ellipsis,
            )
        }

        ZoomImageTool(
            zoomableState = zoomState.zoomable,
            subsamplingState = zoomState.subsampling,
            infoDialogState = infoDialogState,
            imageUri = sketchImageUri,
        )

        MyDialog(state = infoDialogState) {
            ZoomImageInfo(
                imageUri = sketchImageUri,
                zoomable = zoomState.zoomable,
                subsampling = zoomState.subsampling
            )
        }
    }
}