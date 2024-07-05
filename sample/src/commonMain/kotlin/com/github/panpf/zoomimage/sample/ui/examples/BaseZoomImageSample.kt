package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_info
import com.github.panpf.zoomimage.sample.resources.ic_more_vert
import com.github.panpf.zoomimage.sample.resources.ic_rotate_right
import com.github.panpf.zoomimage.sample.resources.ic_zoom_in
import com.github.panpf.zoomimage.sample.resources.ic_zoom_out
import com.github.panpf.zoomimage.sample.ui.components.MoveKeyboard
import com.github.panpf.zoomimage.sample.ui.components.MyDialog
import com.github.panpf.zoomimage.sample.ui.components.MyDialogState
import com.github.panpf.zoomimage.sample.ui.components.ZoomImageInfo
import com.github.panpf.zoomimage.sample.ui.components.ZoomImageMinimap
import com.github.panpf.zoomimage.sample.ui.components.rememberMoveKeyboardState
import com.github.panpf.zoomimage.sample.ui.components.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.sample.ui.util.valueOf
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@Composable
fun BaseZoomImageSample(
    sketchImageUri: String,
    photoPaletteState: MutableState<PhotoPalette>,
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
            zoomable.disabledGestureType = disabledGestureType
        }
        LaunchedEffect(pausedContinuousTransformType) {
            subsampling.pausedContinuousTransformType = pausedContinuousTransformType
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
            if (scrollBarEnabled) ScrollBarSpec.Default.copy(color = photoPaletteState.value.containerColor) else null
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
            imageUri = sketchImageUri,
            zoomableState = zoomState.zoomable,
            subsamplingState = zoomState.subsampling,
            infoDialogState = infoDialogState,
            photoPaletteState = photoPaletteState,
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

@Composable
fun ZoomImageTool(
    imageUri: String,
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
    infoDialogState: MyDialogState,
    photoPaletteState: MutableState<PhotoPalette>,
) {
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(NavigationBarDefaults.windowInsets)) {
        ZoomImageMinimap(
            imageUri = imageUri,
            zoomableState = zoomableState,
            subsamplingState = subsamplingState,
        )

        Column(
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.BottomEnd)
                .wrapContentHeight()
                .width(164.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val inspectionMode = LocalInspectionMode.current
            var moreShow by remember { mutableStateOf(inspectionMode) }
            val photoPalette by photoPaletteState
            AnimatedVisibility(
                visible = moreShow,
                enter = slideInHorizontally(initialOffsetX = { it * 2 }),
                exit = slideOutHorizontally(targetOffsetX = { it * 2 }),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val moveKeyboardState = rememberMoveKeyboardState()
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomableState.containerSize }.collect { size ->
                            moveKeyboardState.maxStep = Offset(size.width / 20f, size.height / 20f)
                        }
                    }
                    LaunchedEffect(Unit) {
                        moveKeyboardState.moveFlow.collect {
                            zoomableState.offset(zoomableState.transform.offset + it * -1f)
                        }
                    }
                    MoveKeyboard(
                        state = moveKeyboardState,
                        iconTint = photoPalette.containerColor,
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(modifier = Modifier.size(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        FilledIconButton(
                            onClick = {
                                coroutineScope.launch {
                                    zoomableState.scale(
                                        targetScale = zoomableState.transform.scaleX - 0.5f,
                                        animated = true
                                    )
                                }
                            },
                            modifier = Modifier.size(30.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = photoPalette.containerColor,
                                contentColor = photoPalette.contentColor
                            ),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_zoom_out),
                                contentDescription = "zoom out",
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                        )

                        FilledIconButton(
                            onClick = {
                                coroutineScope.launch {
                                    zoomableState.scale(
                                        targetScale = zoomableState.transform.scaleX + 0.5f,
                                        animated = true
                                    )
                                }
                            },
                            modifier = Modifier.size(30.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = photoPalette.containerColor,
                                contentColor = photoPalette.contentColor
                            )
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_zoom_in),
                                contentDescription = "zoom in",
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(6.dp))

                    Slider(
                        value = zoomableState.transform.scaleX,
                        valueRange = zoomableState.minScale..zoomableState.maxScale,
                        onValueChange = {
                            coroutineScope.launch {
                                zoomableState.scale(targetScale = it, animated = true)
                            }
                        },
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = photoPalette.accentColor,
                            activeTrackColor = photoPalette.containerColor,
                        ),
                    )

                    Spacer(modifier = Modifier.size(6.dp))
                }
            }

            ButtonPad(infoDialogState, zoomableState, photoPaletteState) {
                moreShow = !moreShow
            }
        }
    }
}

@Composable
private fun ButtonPad(
    infoDialogState: MyDialogState,
    zoomableState: ZoomableState,
    photoPaletteState: MutableState<PhotoPalette>,
    onClickMore: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val photoPalette by photoPaletteState
    Row(Modifier.background(photoPalette.containerColor, RoundedCornerShape(50))) {
        IconButton(
            onClick = {
                coroutineScope.launch {
                    zoomableState.rotate((zoomableState.transform.rotation + 90).roundToInt())
                }
            },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_rotate_right),
                contentDescription = "Rotate",
                tint = photoPalette.contentColor
            )
        }

        IconButton(
            onClick = {
                coroutineScope.launch {
                    zoomableState.switchScale(animated = true)
                }
            },
            modifier = Modifier.size(40.dp)
        ) {
            val zoomIn by remember {
                derivedStateOf {
                    zoomableState.getNextStepScale() > zoomableState.transform.scaleX
                }
            }
            val description = if (zoomIn) {
                "zoom in"
            } else {
                "zoom out"
            }
            val icon = if (zoomIn) {
                painterResource(Res.drawable.ic_zoom_in)
            } else {
                painterResource(Res.drawable.ic_zoom_out)
            }
            Icon(
                painter = icon,
                contentDescription = description,
                tint = photoPalette.contentColor
            )
        }

        IconButton(
            onClick = { infoDialogState.showing = !infoDialogState.showing },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_info),
                contentDescription = "Info",
                tint = photoPalette.contentColor
            )
        }

        IconButton(
            onClick = { onClickMore() },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_more_vert),
                contentDescription = "More",
                tint = photoPalette.contentColor
            )
        }
    }
}