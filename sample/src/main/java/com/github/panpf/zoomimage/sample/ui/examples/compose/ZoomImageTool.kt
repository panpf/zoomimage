package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
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
import kotlin.math.roundToInt

@Composable
fun ZoomImageTool(
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
    infoDialogState: MyDialogState,
    imageUri: String,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val transformInfo = remember(zoomableState.transform) {
            val transform = zoomableState.transform
            """
                scale: ${transform.scale.toShortString()}
                offset: ${transform.offset.round().toShortString()}
                rotation: ${transform.rotation.roundToInt()}
            """.trimIndent()
        }
        Text(
            text = transformInfo,
            color = Color.White,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            style = LocalTextStyle.current.copy(
                shadow = Shadow(offset = Offset(1f, 1f), blurRadius = 4f),
            ),
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(20.dp)
        )

        var content by remember { mutableStateOf(ToolContent.BUTTON) }
        Crossfade(
            targetState = content,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.5f)
                .aspectRatio(1f), label = ""
        ) {
            when (it) {
                ToolContent.SCALE -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        ScalePad(zoomableState) { content = ToolContent.BUTTON }
                    }
                }

                ToolContent.MOVE -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        MovePad(zoomableState) { content = ToolContent.BUTTON }
                    }
                }

                ToolContent.BUTTON -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        ButtonPad(infoDialogState, zoomableState) { newContent ->
                            content = newContent
                        }
                    }
                }
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

@Preview
@Composable
fun ZoomImageToolPreview() {
    val logger = rememberZoomImageLogger()
    ZoomImageTool(
        zoomableState = rememberZoomableState(logger),
        subsamplingState = rememberSubsamplingState(logger),
        infoDialogState = rememberMyDialogState(),
        imageUri = SampleImages.Asset.DOG.uri
    )
}

enum class ToolContent {
    SCALE, MOVE, BUTTON
}

@Composable
private fun ButtonPad(
    infoDialogState: MyDialogState,
    zoomableState: ZoomableState,
    onSetToolContent: (ToolContent) -> Unit
) {
    ConstraintLayout(Modifier.wrapContentSize()) {
        val (button1, button2, button3, button4, button5) = createRefs()

        FilledIconButton(
            onClick = {
                onSetToolContent(ToolContent.MOVE)
            },
            modifier = Modifier
                .size(34.dp)
                .constrainAs(button1) {
                    centerTo(parent)
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_gamepad),
                contentDescription = "GamePad",
                modifier = Modifier.fillMaxSize(0.6f)
            )
        }

        FilledIconButton(
            onClick = {
                zoomableState.rotate((zoomableState.transform.rotation + 90).roundToInt())
            },
            modifier = Modifier
                .size(34.dp)
                .constrainAs(button2) {
                    circular(button1, 315f, 44.dp)
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_rotate_right),
                contentDescription = "Rotate",
                modifier = Modifier.fillMaxSize(0.6f)
            )
        }

        FilledIconButton(
            onClick = { zoomableState.switchScale(animated = true) },
            modifier = Modifier
                .size(34.dp)
                .constrainAs(button3) {
                    circular(button1, 45f, 44.dp)
                }
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
                modifier = Modifier.fillMaxSize(0.6f)
            )
        }

        FilledIconButton(
            onClick = {
                onSetToolContent(ToolContent.SCALE)
            },
            modifier = Modifier
                .size(34.dp)
                .constrainAs(button4) {
                    circular(button1, 135f, 44.dp)
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_linear_scale),
                contentDescription = "LinearScale",
                modifier = Modifier.fillMaxSize(0.6f)
            )
        }

        FilledIconButton(
            onClick = { infoDialogState.showing = !infoDialogState.showing },
            modifier = Modifier
                .size(34.dp)
                .constrainAs(button5) {
                    circular(button1, 225f, 44.dp)
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_info),
                contentDescription = "Options",
                modifier = Modifier.fillMaxSize(0.6f)
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
        onSetToolContent = {}
    )
}

@Composable
private fun MovePad(zoomableState: ZoomableState, onClosed: () -> Unit = {}) {
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

    Column {
        FilledIconButton(
            onClick = { onClosed() },
            modifier = Modifier
                .size(34.dp)
                .align(Alignment.End)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "GamePad",
                modifier = Modifier.fillMaxSize(0.6f)
            )
        }

        Spacer(modifier = Modifier.size(20.dp))

        MoveKeyboard(
            state = moveKeyboardState,
            modifier = Modifier.size(100.dp)
        )
    }
}

@Preview
@Composable
private fun MovePadPreview() {
    MovePad(zoomableState = rememberZoomableState(logger = rememberZoomImageLogger()))
}

@Composable
private fun ScalePad(zoomableState: ZoomableState, onClosed: () -> Unit = {}) {
    var value by remember { mutableStateOf(zoomableState.transform.scaleX) }
    Column(Modifier.fillMaxWidth()) {
        FilledIconButton(
            onClick = { onClosed() },
            modifier = Modifier
                .size(34.dp)
                .align(Alignment.End)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "GamePad",
                modifier = Modifier.fillMaxSize(0.6f)
            )
        }

        Spacer(modifier = Modifier.size(20.dp))

        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            valueRange = zoomableState.minScale..zoomableState.maxScale,
            onValueChange = {
                value = it
                zoomableState.scale(targetScale = it, animated = true)
            },
            steps = 8,
        )
    }
}

@Preview
@Composable
private fun ScalePadPreview() {
    ScalePad(zoomableState = rememberZoomableState(logger = rememberZoomImageLogger()))
}