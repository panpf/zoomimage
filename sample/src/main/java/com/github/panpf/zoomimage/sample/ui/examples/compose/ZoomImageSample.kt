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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.zoomimage.AnimationConfig
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.rememberZoomState
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.model.ResPhoto
import com.github.panpf.zoomimage.sample.ui.util.compose.toPx
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString
import com.github.panpf.zoomimage.sample.ui.widget.compose.ZoomImageMinimap
import kotlinx.coroutines.launch

@Composable
fun ZoomImageSample(sketchImageUri: String) {
    val coroutineScope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    val settingsDialogState = rememberZoomImageSettingsDialogState()
    val horSmall = remember { ResPhoto.dog.newFetcher(name = "横向图片 - 小", big = false) }
    val horBig = remember { ResPhoto.dog.newFetcher(name = "横向图片 - 大", big = true) }
    val verSmall = remember { ResPhoto.cat.newFetcher(name = "竖向图片 - 小", big = false) }
    val verBig = remember { ResPhoto.cat.newFetcher(name = "竖向图片 - 大", big = true) }
    val image by remember {
        derivedStateOf {
            if (settingsDialogState.horImageSelected) {
                if (settingsDialogState.smallImageSelected) horSmall else horBig
            } else {
                if (settingsDialogState.smallImageSelected) verSmall else verBig
            }
        }
    }
    val animationDurationMillisState = remember(settingsDialogState.slowerScaleAnimation) {
        mutableStateOf(if (settingsDialogState.slowerScaleAnimation) 3000 else AnimationConfig.DefaultDurationMillis)
    }
    var zoomOptionsDialogShow by remember { mutableStateOf(false) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val myZoomState = rememberZoomState(debugMode = BuildConfig.DEBUG)
        val zoomIn = remember {
            derivedStateOf {
                val nextScale = myZoomState.getNextStepScale()
                nextScale > myZoomState.minScale
            }
        }
        val context = LocalContext.current
        val viewSize = min(maxWidth, maxHeight).toPx().toInt()
        val painter = remember(viewSize, image) {
            image.getBitmap(context, viewSize).asImageBitmap().let { BitmapPainter(it) }
        }
        ZoomImage(
            painter = painter,
            contentDescription = "",
            contentScale = settingsDialogState.contentScale,
            alignment = settingsDialogState.alignment,
            modifier = Modifier.fillMaxSize(),
            state = myZoomState,
            animationConfig = AnimationConfig(
                doubleTapScaleEnabled = !settingsDialogState.closeScaleAnimation,
                durationMillis = animationDurationMillisState.value,
            ),
        )

        ZoomImageMinimap(
            painter = painter,
            state = myZoomState,
            animateScale = !settingsDialogState.closeScaleAnimation,
            animationDurationMillis = animationDurationMillisState.value,
        )

        Column {
            val expandedState = remember { mutableStateOf(false) }
            Text(
                text = """
                    scale: ${myZoomState.scale}, ${if (myZoomState.zooming) "zooming" else ""}
                    translation: ${myZoomState.translation.toShortString()}
                    translationBounds: ${myZoomState.translationBounds?.toShortString()}
                    contentVisibleRect: ${myZoomState.contentVisibleRect.toShortString()}
                    containerVisibleRect: ${myZoomState.containerVisibleRect.toShortString()}
                    scrollEdge: horizontal=${myZoomState.horizontalScrollEdge}, vertical=${myZoomState.verticalScrollEdge}
                    contentSize: ${myZoomState.contentSize}
                    containerSize: ${myZoomState.containerSize}
                    contentInContainerRect: ${myZoomState.contentInContainerRect.toShortString()}
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
                        val newScale = myZoomState.getNextStepScale()
                        if (!settingsDialogState.closeScaleAnimation) {
                            myZoomState.animateScaleTo(newScale = newScale)
                        } else {
                            myZoomState.snapScaleTo(newScale = newScale)
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