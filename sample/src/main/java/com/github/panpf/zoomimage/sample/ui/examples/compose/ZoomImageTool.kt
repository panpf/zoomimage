package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.rememberZoomImageLogger
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.SampleImages
import com.github.panpf.zoomimage.sample.ui.common.compose.MoveKeyboard
import com.github.panpf.zoomimage.sample.ui.common.compose.MyDialog
import com.github.panpf.zoomimage.sample.ui.common.compose.MyDialogState
import com.github.panpf.zoomimage.sample.ui.common.compose.rememberMoveKeyboardState
import com.github.panpf.zoomimage.sample.ui.common.compose.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString
import com.github.panpf.zoomimage.sample.util.format
import kotlin.math.roundToInt

@Composable
fun ZoomImageTool(
    zoomableState: ZoomableState,
    infoDialogState: MyDialogState,
    imageUri: String,
) {
    val colors = MaterialTheme.colorScheme
    val info = remember(zoomableState.transform) {
        val transform = zoomableState.transform
        """
            scale: ${transform.scale.toShortString()}
            offset: ${transform.offset.round().toShortString()}
            rotation: ${transform.rotation.roundToInt()}
        """.trimIndent()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Text(
                text = info,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(offset = Offset(1f, 1f), blurRadius = 4f),
                ),
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(10.dp)
            )
        }

        val inspectionMode = LocalInspectionMode.current
        var show by remember { mutableStateOf(inspectionMode) }
        var content by remember { mutableStateOf(0) }
        Column(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = show,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Crossfade(targetState = content, label = "") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (it == 1) {
                            var value by remember { mutableStateOf(zoomableState.transform.scaleX) }
                            Slider(
                                modifier = Modifier.fillMaxWidth(),
                                value = value,
                                valueRange = zoomableState.minScale..zoomableState.maxScale,
                                onValueChange = {
                                    value = it
                                    zoomableState.scale(it, animated = true)
                                },
                                steps = 8,
                            )
                        } else {
                            Box(
                                modifier = Modifier.padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val maxStep by remember {
                                    derivedStateOf {
                                        (zoomableState.containerSize / 20)
                                            .let { Offset(it.width.toFloat(), it.height.toFloat()) }
                                    }
                                }
                                val moveKeyboardState = rememberMoveKeyboardState(maxStep = maxStep, stepInterval = 8)
                                LaunchedEffect(Unit) {
                                    moveKeyboardState.moveFlow.collect {
                                        zoomableState.offset(zoomableState.transform.offset + it * -1f)
                                    }
                                }
                                MoveKeyboard(moveKeyboardState)
                            }
                        }
                    }
                }
            }

            Row(
                Modifier.background(colors.tertiary.copy(alpha = 0.7f), RoundedCornerShape(50)),
            ) {
                IconButton(
                    onClick = {
                        zoomableState.rotate((zoomableState.transform.rotation + 90).roundToInt())
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_rotate_right),
                        contentDescription = "Rotate",
                        tint = colors.onTertiary
                    )
                }

                IconButton(
                    onClick = { zoomableState.switchScale(animated = true) },
                    modifier = Modifier.size(40.dp)
                ) {
                    val zoomIn = remember {
                        derivedStateOf {
                            val scale = zoomableState.transform.scaleX  // trigger refresh
                            scale >= 0 && zoomableState.getNextStepScale() > zoomableState.minScale
                        }
                    }
                    val icon = if (zoomIn.value)
                        R.drawable.ic_zoom_in to "zoom in" else R.drawable.ic_zoom_out to "zoom out"
                    Icon(
                        painter = painterResource(id = icon.first),
                        contentDescription = icon.second,
                        tint = colors.onTertiary
                    )
                }

                IconButton(
                    onClick = {
                        if (show) {
                            if (content == 1) {
                                show = false
                            } else {
                                content = 1
                            }
                        } else {
                            show = true
                            content = 1
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_linear_scale),
                        contentDescription = "LinearScale",
                        tint = colors.onTertiary
                    )
                }

                IconButton(
                    onClick = {
                        if (show) {
                            if (content == 0) {
                                show = false
                            } else {
                                content = 0
                            }
                        } else {
                            show = true
                            content = 0
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_gamepad),
                        contentDescription = "GamePad",
                        tint = colors.onTertiary
                    )
                }

                IconButton(
                    onClick = { infoDialogState.showing = !infoDialogState.showing },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = "Options",
                        tint = colors.onTertiary
                    )
                }
            }
        }

        MyDialog(state = infoDialogState) {
            ZoomImageInfo(imageUri = imageUri, zoomableState = zoomableState)
        }
    }
}

@Preview
@Composable
fun ZoomImageToolPreview() {
    ZoomImageTool(
        zoomableState = rememberZoomableState(rememberZoomImageLogger()),
        infoDialogState = rememberMyDialogState(),
        imageUri = SampleImages.Asset.DOG.uri
    )
}