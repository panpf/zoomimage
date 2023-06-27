package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.sp
import com.github.panpf.sketch.compose.rememberAsyncImagePainter
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.zoomimage.AnimationConfig
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.rememberZoomableState
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString
import com.github.panpf.zoomimage.sample.ui.widget.compose.ZoomImageMinimap
import com.github.panpf.zoomimage.sample.util.format
import kotlinx.coroutines.launch

@Composable
fun ZoomImageSample(sketchImageUri: String) {
    val coroutineScope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    val settingsDialogState = rememberZoomImageSettingsDialogState()
    val animationDurationMillisState = remember(settingsDialogState.slowerScaleAnimation) {
        mutableStateOf(if (settingsDialogState.slowerScaleAnimation) 3000 else AnimationConfig.DefaultDurationMillis)
    }
    var zoomOptionsDialogShow by remember { mutableStateOf(false) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val zoomableState = rememberZoomableState(debugMode = BuildConfig.DEBUG)
        val zoomIn = remember {
            derivedStateOf {
                val nextScale = zoomableState.getNextStepScale()
                nextScale > zoomableState.minScale
            }
        }
        ZoomImage(
            painter = rememberAsyncImagePainter(imageUri = sketchImageUri),
            contentDescription = "",
            contentScale = settingsDialogState.contentScale,
            alignment = settingsDialogState.alignment,
            modifier = Modifier.fillMaxSize(),
            state = zoomableState,
            animationConfig = AnimationConfig(
                doubleTapScaleEnabled = !settingsDialogState.closeScaleAnimation,
                durationMillis = animationDurationMillisState.value,
            ),
        )

        ZoomImageMinimap(
            painter = rememberAsyncImagePainter(imageUri = sketchImageUri),
            state = zoomableState,
            animateScale = !settingsDialogState.closeScaleAnimation,
            animationDurationMillis = animationDurationMillisState.value,
        )

        Column {
            val expandedState = remember { mutableStateOf(false) }
            Text(
                text = """
                    scale: ${zoomableState.scale.format(2)}
                    translation: ${zoomableState.translation.toShortString()}
                    translationBounds: ${zoomableState.translationBounds?.toShortString()}
                    contentVisibleRect: ${zoomableState.contentVisibleRect.toShortString()}
                    containerVisibleRect: ${zoomableState.containerVisibleRect.toShortString()}
                    scrollEdge: horizontal=${zoomableState.horizontalScrollEdge}, vertical=${zoomableState.verticalScrollEdge}
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
                .padding(20.dp)
                .background(colors.tertiary.copy(alpha = 0.8f), RoundedCornerShape(50)),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        val newScale = zoomableState.getNextStepScale()
                        if (!settingsDialogState.closeScaleAnimation) {
                            zoomableState.animateScaleTo(newScale = newScale)
                        } else {
                            zoomableState.snapScaleTo(newScale = newScale)
                        }
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
            IconButton(onClick = { zoomOptionsDialogShow = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    tint = colors.onTertiary
                )
            }
        }

        if (zoomOptionsDialogShow) {
            ZoomImageSettingsDialog(state = settingsDialogState) {
                zoomOptionsDialogShow = false
            }
        }
    }
}

@Preview
@Composable
private fun ZoomImageSamplePreview() {
    ZoomImageSample(newResourceUri(R.drawable.im_placeholder))
}