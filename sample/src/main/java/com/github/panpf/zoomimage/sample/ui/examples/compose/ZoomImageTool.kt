package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.window.Dialog
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.rememberZoomImageLogger
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.SampleImages
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.toShortString
import kotlin.math.roundToInt

@Composable
fun ZoomImageTool(
    zoomableState: ZoomableState,
    infoDialogState: ZoomImageInfoDialogState,
    imageUri: String,
) {
    val colors = MaterialTheme.colorScheme
    val info = remember(
        zoomableState.minScale,
        zoomableState.mediumScale,
        zoomableState.maxScale,
        zoomableState.userTransform,
        zoomableState.transform,
        zoomableState.baseTransform,
        zoomableState.contentVisibleRect
    ) {
        val scales = floatArrayOf(
            zoomableState.minScale,
            zoomableState.mediumScale,
            zoomableState.maxScale
        ).joinToString(prefix = "[", postfix = "]") { it.format(2).toString() }
        val transform = zoomableState.transform
        val userTransform = zoomableState.userTransform
        val baseTransform = zoomableState.baseTransform
        val userScaleFormatted = userTransform.scaleX.format(2)
        val scaleFormatted = transform.scaleX.format(2)
        val baseScaleFormatted = baseTransform.scaleX.format(2)
        val offset = transform.offset.round()
        val contentVisibleRect = zoomableState.contentVisibleRect
        val scrollEdge = zoomableState.scrollEdge
        """
            scale: $scaleFormatted(${baseScaleFormatted}*${userScaleFormatted}) in $scales
            offset: ${offset.toShortString()}; edge=${scrollEdge.toShortString()}
            visible: ${contentVisibleRect.toShortString()}
        """.trimIndent()
    }
    val linearScaleDialogState = rememberLinearScaleDialogState()
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

        Row(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(colors.tertiary.copy(alpha = 0.7f), RoundedCornerShape(50)),
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
                    linearScaleDialogState.showing = !linearScaleDialogState.showing
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

        ZoomImageInfoDialog(
            state = infoDialogState,
            imageUri = imageUri,
            zoomableState = zoomableState
        )

        LinearScaleDialog(
            zoomableState = zoomableState,
            linearScaleDialogState = linearScaleDialogState
        )
    }
}

@Preview
@Composable
fun ZoomImageToolPreview() {
    val sketchImageUri = SampleImages.Asset.DOG.uri
    ZoomImageTool(
        zoomableState = rememberZoomableState(rememberZoomImageLogger()),
        infoDialogState = rememberZoomImageInfoDialogState(),
        imageUri = sketchImageUri
    )
}

@Composable
fun rememberLinearScaleDialogState(showing: Boolean = false): LinearScaleDialogState =
    remember { LinearScaleDialogState(showing) }

class LinearScaleDialogState(showing: Boolean = false) {
    var showing by mutableStateOf(showing)
}

@Composable
fun LinearScaleDialog(
    zoomableState: ZoomableState,
    linearScaleDialogState: LinearScaleDialogState
) {
    if (linearScaleDialogState.showing) {
        var value by remember { mutableStateOf(zoomableState.transform.scaleX) }
        val valueRange by remember { derivedStateOf { zoomableState.minScale..zoomableState.maxScale } }
        Dialog(onDismissRequest = { linearScaleDialogState.showing = false }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${zoomableState.minScale.format(1)}")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "${zoomableState.maxScale.format(1)}")
                }

                Spacer(modifier = Modifier.size(4.dp))

                Slider(
                    value = value,
                    valueRange = valueRange,
                    onValueChange = {
                        value = it
                        zoomableState.scale(it, animated = true)
                    },
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
fun LinearScaleDialogPreview() {
    LinearScaleDialog(
        zoomableState = rememberZoomableState(logger = rememberZoomImageLogger()),
        linearScaleDialogState = rememberLinearScaleDialogState()
    )
}