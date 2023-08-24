package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.SampleImages
import com.github.panpf.zoomimage.sample.ui.common.compose.MoveKeyboard
import com.github.panpf.zoomimage.sample.ui.common.compose.MyDialog
import com.github.panpf.zoomimage.sample.ui.common.compose.MyDialogState
import com.github.panpf.zoomimage.sample.ui.common.compose.rememberMoveKeyboardState
import com.github.panpf.zoomimage.sample.ui.common.compose.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString
import kotlin.math.roundToInt

@Composable
fun ZoomImageTool(
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
    infoDialogState: MyDialogState,
    imageUri: String,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(Modifier.padding(20.dp)) {
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
            val transformInfo = remember(zoomableState.transform) {
                val transform = zoomableState.transform
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
            AnimatedVisibility(
                visible = moreShow,
                enter = slideInHorizontally(initialOffsetX = { it * 2 }),
                exit = slideOutHorizontally(targetOffsetX = { it * 2 }),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val maxStep by remember {
                        derivedStateOf {
                            (zoomableState.containerSize / 20)
                                .let { Offset(it.width.toFloat(), it.height.toFloat()) }
                        }
                    }
                    val moveKeyboardState =
                        rememberMoveKeyboardState(maxStep = maxStep, stepInterval = 8)
                    LaunchedEffect(Unit) {
                        moveKeyboardState.moveFlow.collect {
                            zoomableState.offset(zoomableState.transform.offset + it * -1f)
                        }
                    }
                    MoveKeyboard(
                        state = moveKeyboardState,
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(modifier = Modifier.size(6.dp))

                    var value by remember { mutableStateOf(zoomableState.transform.scaleX) }
                    Slider(
                        value = value,
                        valueRange = zoomableState.minScale..zoomableState.maxScale,
                        onValueChange = {
                            value = it
                            zoomableState.scale(targetScale = it, animated = true)
                        },
                        steps = 8,
                    )

                    Spacer(modifier = Modifier.size(6.dp))
                }
            }

            ButtonPad(infoDialogState, zoomableState) {
                moreShow = !moreShow
            }
        }

        MyDialog(state = infoDialogState) {
            ZoomImageInfo(
                imageUri = imageUri,
                zoomableState = zoomableState,
                subsamplingState = subsamplingState
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ZoomImageToolPreview() {
    ZoomImageTool(
        zoomableState = rememberZoomableState(),
        subsamplingState = rememberSubsamplingState(),
        infoDialogState = rememberMyDialogState(),
        imageUri = SampleImages.Asset.DOG.uri
    )
}

@Composable
private fun ButtonPad(
    infoDialogState: MyDialogState,
    zoomableState: ZoomableState,
    onClickMore: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(Modifier.background(colorScheme.tertiary.copy(alpha = 0.8f), RoundedCornerShape(50))) {
        IconButton(
            onClick = {
                zoomableState.rotate((zoomableState.transform.rotation + 90).roundToInt())
            },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_rotate_right),
                contentDescription = "Rotate",
                tint = colorScheme.onTertiary
            )
        }

        IconButton(
            onClick = { zoomableState.switchScale(animated = true) },
            modifier = Modifier.size(40.dp)
        ) {
            val zoomIn by remember {
                derivedStateOf {
                    zoomableState.getNextStepScale() > zoomableState.transform.scaleX
                }
            }
            val icon = if (zoomIn)
                R.drawable.ic_zoom_in to "zoom in" else R.drawable.ic_zoom_out to "zoom out"
            Icon(
                painter = painterResource(id = icon.first),
                contentDescription = icon.second,
                tint = colorScheme.onTertiary
            )
        }

        IconButton(
            onClick = { infoDialogState.showing = !infoDialogState.showing },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_info),
                contentDescription = "Info",
                tint = colorScheme.onTertiary
            )
        }

        IconButton(
            onClick = { onClickMore() },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more_vert),
                contentDescription = "More",
                tint = colorScheme.onTertiary
            )
        }
    }
}

@Preview
@Composable
private fun ButtonPadPreview() {
    ButtonPad(
        infoDialogState = rememberMyDialogState(),
        zoomableState = rememberZoomableState(logger = rememberZoomImageLogger()),
        onClickMore = {}
    )
}