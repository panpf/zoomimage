package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.util.toPlatform
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.bindKeyZoomWithKeyEventFlow
import com.github.panpf.zoomimage.sample.AppEvents
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.buildScalesCalculator
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_info
import com.github.panpf.zoomimage.sample.resources.ic_more_vert
import com.github.panpf.zoomimage.sample.resources.ic_photo_camera
import com.github.panpf.zoomimage.sample.resources.ic_rotate_right
import com.github.panpf.zoomimage.sample.resources.ic_zoom_in
import com.github.panpf.zoomimage.sample.resources.ic_zoom_out
import com.github.panpf.zoomimage.sample.ui.components.MoveKeyboard
import com.github.panpf.zoomimage.sample.ui.components.MyDialog
import com.github.panpf.zoomimage.sample.ui.components.MyDialogState
import com.github.panpf.zoomimage.sample.ui.components.ZoomImageMinimap
import com.github.panpf.zoomimage.sample.ui.components.rememberMoveKeyboardState
import com.github.panpf.zoomimage.sample.ui.components.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.gallery.photoPagerTopBarHeight
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.ui.util.CapturableState
import com.github.panpf.zoomimage.sample.ui.util.crop
import com.github.panpf.zoomimage.sample.ui.util.limitTo
import com.github.panpf.zoomimage.sample.ui.util.rememberCapturableState
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.ReadMode
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : ZoomState> BaseZoomImageSample(
    photo: Photo,
    photoPaletteState: MutableState<PhotoPalette>,
    pageSelected: Boolean,
    createZoomState: @Composable () -> T,
    content: @Composable ContentScope<T>.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val appSettings: AppSettings = koinInject()
        val infoDialogState = rememberMyDialogState()
        val capturableState = rememberCapturableState()
        val zoomState = createZoomState().apply { bindSettings(appSettings) }

        val rtlLayoutDirectionEnabled by appSettings.rtlLayoutDirectionEnabled.collectAsState()
        val layoutDirection =
            if (rtlLayoutDirectionEnabled) LayoutDirection.Rtl else LocalLayoutDirection.current
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            val contentScale by appSettings.contentScale.collectAsState()
            val alignment by appSettings.alignment.collectAsState()
            val scrollBarEnabled by appSettings.scrollBarEnabled.collectAsState()
            val contentScope =
                remember(zoomState, contentScale, alignment, capturableState, scrollBarEnabled) {
                    ContentScope(
                        zoomState = zoomState,
                        contentScale = contentScale.toPlatform(),
                        alignment = alignment.toPlatform(),
                        capturableState = capturableState,
                        scrollBar = if (scrollBarEnabled) ScrollBarSpec.Default.copy(color = photoPaletteState.value.containerColor) else null,
                        onLongClick = { infoDialogState.show() },
                    )
                }
            with(contentScope) {
                content()
            }
        }

        Row(
            Modifier
                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                .padding(top = photoPagerTopBarHeight)
                .padding(horizontal = 20.dp)
        ) {
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

        if (pageSelected) {
            val appEvents: AppEvents = koinInject()
            bindKeyZoomWithKeyEventFlow(appEvents.keyEvent, zoomState.zoomable)
        }

        ZoomImageTool(
            photo = photo,
            zoomableState = zoomState.zoomable,
            subsamplingState = zoomState.subsampling,
            capturableState = capturableState,
            infoDialogState = infoDialogState,
            photoPaletteState = photoPaletteState,
        )

        MyDialog(infoDialogState) {
            ZoomImageInfo(
                photo = photo,
                zoomState = zoomState,
            )
        }
    }
}

@Composable
private fun ZoomState.bindSettings(appSettings: AppSettings) {
    val logLevel by appSettings.logLevel.collectAsState()
    logger.level = logLevel

    val threeStepScale by appSettings.threeStepScaleEnabled.collectAsState()
    zoomable.threeStepScale = threeStepScale

    val rubberBandScale by appSettings.rubberBandScaleEnabled.collectAsState()
    zoomable.rubberBandScale = rubberBandScale

    val animateScale by appSettings.zoomAnimateEnabled.collectAsState()
    val slowerScaleAnimation by appSettings.zoomSlowerAnimationEnabled.collectAsState()
    val zoomAnimationSpec by remember {
        derivedStateOf {
            val durationMillis = if (animateScale) (if (slowerScaleAnimation) 3000 else 300) else 0
            ZoomAnimationSpec.Default.copy(durationMillis = durationMillis)
        }
    }
    zoomable.animationSpec = zoomAnimationSpec

    val reverseMouseWheelScale by appSettings.reverseMouseWheelScaleEnabled.collectAsState()
    zoomable.reverseMouseWheelScale = reverseMouseWheelScale

    val scalesCalculatorName by appSettings.scalesCalculatorName.collectAsState()
    val scalesMultiple by appSettings.fixedScalesCalculatorMultiple.collectAsState()
    val scalesCalculator by remember {
        derivedStateOf {
            buildScalesCalculator(scalesCalculatorName, scalesMultiple.toFloat())
        }
    }
    zoomable.scalesCalculator = scalesCalculator

    val rubberBandOffsetEnabled by appSettings.rubberBandOffsetEnabled.collectAsState()
    zoomable.rubberBandOffset = rubberBandOffsetEnabled

    val alwaysCanDragAtEdgeEnabled by appSettings.alwaysCanDragAtEdgeEnabled.collectAsState()
    zoomable.alwaysCanDragAtEdge = alwaysCanDragAtEdgeEnabled

    val limitOffsetWithinBaseVisibleRect by appSettings.limitOffsetWithinBaseVisibleRect.collectAsState()
    zoomable.limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect

    val containerWhitespaceMultiple by appSettings.containerWhitespaceMultiple.collectAsState()
    zoomable.containerWhitespaceMultiple = containerWhitespaceMultiple

    val containerWhitespaceEnabled by appSettings.containerWhitespaceEnabled.collectAsState()
    val containerWhitespace by remember {
        derivedStateOf {
            if (containerWhitespaceEnabled) {
                ContainerWhitespace(left = 100f, top = 200f, right = 300f, bottom = 400f)
            } else {
                ContainerWhitespace.Zero
            }
        }
    }
    zoomable.containerWhitespace = containerWhitespace

    val readModeEnabled by appSettings.readModeEnabled.collectAsState()
    val readModeAcceptedBoth by appSettings.readModeAcceptedBoth.collectAsState()
    val horizontalLayout by appSettings.horizontalPagerLayout.collectAsState()
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
    zoomable.readMode = readMode

    val disabledGestureTypes by appSettings.disabledGestureTypes.collectAsState()
    zoomable.disabledGestureTypes = disabledGestureTypes

    val keepTransformWhenSameAspectRatioContentSizeChangedEnabled by appSettings.keepTransformEnabled.collectAsState()
    zoomable.keepTransformWhenSameAspectRatioContentSizeChanged =
        keepTransformWhenSameAspectRatioContentSizeChangedEnabled

    val subsamplingEnabled by appSettings.subsamplingEnabled.collectAsState()
    subsampling.disabled = !subsamplingEnabled

    val autoStopWithLifecycleEnabled by appSettings.autoStopWithLifecycleEnabled.collectAsState()
    subsampling.disabledAutoStopWithLifecycle = !autoStopWithLifecycleEnabled

    val pausedContinuousTransformTypes by appSettings.pausedContinuousTransformTypes.collectAsState()
    subsampling.pausedContinuousTransformTypes = pausedContinuousTransformTypes

    val backgroundTilesEnabled by appSettings.backgroundTilesEnabled.collectAsState()
    subsampling.disabledBackgroundTiles = !backgroundTilesEnabled

    val tileBoundsEnabled by appSettings.tileBoundsEnabled.collectAsState()
    subsampling.showTileBounds = tileBoundsEnabled

    val tileAnimationEnabled by appSettings.tileAnimationEnabled.collectAsState()
    val tileAnimationSpec by remember {
        derivedStateOf {
            if (tileAnimationEnabled) TileAnimationSpec.Default else TileAnimationSpec.None
        }
    }
    subsampling.tileAnimationSpec = tileAnimationSpec

    val tileMemoryCache by appSettings.tileMemoryCacheEnabled.collectAsState()
    subsampling.disabledTileImageCache = !tileMemoryCache
}

@Stable
data class ContentScope<T : ZoomState>(
    val zoomState: T,
    val contentScale: ContentScale,
    val alignment: Alignment,
    val capturableState: CapturableState,
    val scrollBar: ScrollBarSpec?,
    val onLongClick: () -> Unit
)

@Composable
fun ZoomImageTool(
    photo: Photo,
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
    capturableState: CapturableState,
    infoDialogState: MyDialogState,
    photoPaletteState: MutableState<PhotoPalette>,
) {
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(NavigationBarDefaults.windowInsets)) {
        ZoomImageMinimap(
            imageUri = photo.listThumbnailUrl,
            zoomableState = zoomableState,
            subsamplingState = subsamplingState,
        )

        Column(
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.BottomEnd)
                .wrapContentHeight()
                .width(205.dp),
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
                            zoomableState.offsetBy(it * -1f)
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
                                    zoomableState.scaleBy(addScale = 0.67f, animated = true)
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
                                    zoomableState.scaleBy(addScale = 1.5f, animated = true)
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
                            thumbColor = photoPalette.containerColor,
                            activeTickColor = photoPalette.contentColor,
                            activeTrackColor = photoPalette.containerColor,
                            inactiveTickColor = photoPalette.contentColor,
                            inactiveTrackColor = photoPalette.containerColor,
                        ),
                    )

                    Spacer(modifier = Modifier.size(6.dp))
                }
            }

            ButtonPad(infoDialogState, zoomableState, capturableState, photoPaletteState) {
                moreShow = !moreShow
            }
        }
    }
}

@Composable
private fun ButtonPad(
    infoDialogState: MyDialogState,
    zoomableState: ZoomableState,
    capturableState: CapturableState,
    photoPaletteState: MutableState<PhotoPalette>,
    onClickMore: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val photoPalette by photoPaletteState
    val screenshotDialog = rememberMyDialogState(false)
    var screenshotBitmap by mutableStateOf<ImageBitmap?>(null)
    Row(
        Modifier
            .background(photoPalette.containerColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp)
    ) {
        IconButton(
            onClick = {
                coroutineScope.launch {
                    zoomableState.rotateBy(90)
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
            onClick = {
                coroutineScope.launch {
                    val imageBitmap = capturableState.capture()
                    val cropRect = zoomableState.contentDisplayRect
                        .limitTo(zoomableState.containerSize)
                    screenshotBitmap = imageBitmap.crop(cropRect)
                    screenshotDialog.show()
                }
            },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_photo_camera),
                contentDescription = "Capture",
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

        MyDialog(screenshotDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        screenshotDialog.dismiss()
                    }
                    .padding(40.dp)
            ) {
                Image(
                    bitmap = screenshotBitmap!!,
                    contentDescription = "screenshot",
                    modifier = Modifier
                        .wrapContentSize()
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        .align(Alignment.Center)
                )
            }
        }
    }
}