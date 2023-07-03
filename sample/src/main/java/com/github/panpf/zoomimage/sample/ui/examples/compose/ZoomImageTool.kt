package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.zoomimage.ZoomableState
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString
import com.github.panpf.zoomimage.sample.util.format
import kotlinx.coroutines.launch

@Composable
fun ZoomImageTool(
    zoomableState: ZoomableState,
    zoomImageOptionsDialogState: ZoomImageOptionsDialogState
) {
    val coroutineScope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    val zoomIn = remember {
        derivedStateOf { zoomableState.getNextStepScale() > zoomableState.minScale }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            val expandedState = remember { mutableStateOf(false) }
            val scales = floatArrayOf(zoomableState.minScale, zoomableState.mediumScale, zoomableState.maxScale)
                .joinToString(prefix = "[", postfix = "]") { it.format(2).toString() }
            Text(
                text = """
                    scale: ${zoomableState.scale.format(2)}(${zoomableState.displayScale.scaleX.format(2)}/${zoomableState.baseScale.scaleX.format(2)}) in $scales
                    offset: ${zoomableState.offset.toShortString()}
                    offsetBounds: ${zoomableState.offsetBounds?.toShortString()}
                    contentVisibleRect: ${zoomableState.contentVisibleRect.toShortString()}
                    containerVisibleRect: ${zoomableState.containerVisibleRect.toShortString()}
                    contentSize: ${zoomableState.contentSize.toShortString()}
                    containerSize: ${zoomableState.containerSize.toShortString()}
                    contentInContainerRect: ${zoomableState.contentInContainerRect.toShortString()}
                """.trimIndent(),
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(offset = Offset(1f, 1f), blurRadius = 4f),
                ),
                maxLines = if (expandedState.value) Int.MAX_VALUE else 6,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable { expandedState.value = !expandedState.value }
                    .padding(10.dp)
            )
        }

        Row(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(colors.tertiary.copy(alpha = 0.7f), RoundedCornerShape(50)),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        zoomableState.switchScale()
                    }
                }
            ) {
                val icon = if (zoomIn.value)
                    R.drawable.ic_zoom_in to "zoom in" else R.drawable.ic_zoom_out to "zoom out"
                Icon(
                    painter = painterResource(id = icon.first),
                    contentDescription = icon.second,
                    tint = colors.onTertiary
                )
            }
            IconButton(onClick = {
                zoomImageOptionsDialogState.showing = !zoomImageOptionsDialogState.showing
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Options",
                    tint = colors.onTertiary
                )
            }
        }

        ZoomImageOptionsDialog(state = zoomImageOptionsDialogState)
    }
}