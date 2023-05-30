package com.github.panpf.zoom.ui

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
import com.github.panpf.zoom.MyZoomImage
import com.github.panpf.zoom.MyZoomVisibleRectImage
import com.github.panpf.zoom.ScaleAnimationConfig
import com.github.panpf.zoom.rememberMyZoomState
import com.github.panpf.zoom.sample.BuildConfig
import com.github.panpf.zoom.sample.R
import com.github.panpf.zoom.toPx
import com.github.panpf.zoom.toShortString
import com.github.panpf.zoom.ui.base.compose.HorizontalTabPager
import com.github.panpf.zoom.ui.base.compose.Material3ComposeAppBarFragment
import com.github.panpf.zoom.ui.base.compose.PageItem
import com.github.panpf.zoom.ui.common.PhotoItem
import com.github.panpf.zoom.ui.common.horPhoto
import com.github.panpf.zoom.ui.common.verPhoto
import kotlinx.coroutines.launch

class ZoomImageFragment : Material3ComposeAppBarFragment() {

    override fun getTitle(): String {
        return "ZoomImage"
    }

    @Composable
    override fun DrawContent() {
        HorizontalTabPager(
            PageItem(
                data = "My",
                titleFactory = { it },
                contentFactory = { data, _ ->
                    MyZoomImageFullSample()
                }
            )
        )
    }
}

@Composable
private fun MyZoomImageFullSample() {
    val coroutineScope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    val zoomOptionsDialogState = rememberZoomOptionsDialogState()
    val horSmall = remember { PhotoItem(horPhoto, "横向图片 - 小", false) }
    val verSmall = remember { PhotoItem(verPhoto, "竖向图片 - 小", false) }
    val horBig = remember { PhotoItem(horPhoto, "横向图片 - 大", true) }
    val verBig = remember { PhotoItem(verPhoto, "竖向图片 - 大", true) }
    val image by remember {
        derivedStateOf {
            if (zoomOptionsDialogState.horImageSelected) {
                if (zoomOptionsDialogState.smallImageSelected) horSmall else horBig
            } else {
                if (zoomOptionsDialogState.smallImageSelected) verSmall else verBig
            }
        }
    }
    val animationDurationMillisState = remember(zoomOptionsDialogState.slowerScaleAnimation) {
        mutableStateOf(if (zoomOptionsDialogState.slowerScaleAnimation) 3000 else ScaleAnimationConfig.DefaultDurationMillis)
    }
    var zoomOptionsDialogShow by remember { mutableStateOf(false) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val myZoomState = rememberMyZoomState(debugMode = BuildConfig.DEBUG)
        val zoomIn = remember {
            derivedStateOf {
                val nextScale = myZoomState.nextScale()
                nextScale > myZoomState.minScale
            }
        }
        val context = LocalContext.current
        val viewSize = min(maxWidth, maxHeight).toPx().toInt()
        val painter = remember(viewSize, image) {
            image.getBitmap(context, viewSize).asImageBitmap().let { BitmapPainter(it) }
        }
        MyZoomImage(
            painter = painter,
            contentDescription = "",
            contentScale = zoomOptionsDialogState.contentScale,
            alignment = zoomOptionsDialogState.alignment,
            modifier = Modifier.fillMaxSize(),
            state = myZoomState,
            scaleAnimationConfig = ScaleAnimationConfig(
                animateDoubleTapScale = !zoomOptionsDialogState.closeScaleAnimation,
                animationDurationMillis = animationDurationMillisState.value,
            ),
        )

        MyZoomVisibleRectImage(
            painter = painter,
            state = myZoomState,
            animateScale = !zoomOptionsDialogState.closeScaleAnimation,
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
                        val newScale = myZoomState.nextScale()
                        if (!zoomOptionsDialogState.closeScaleAnimation) {
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
            ZoomOptionsDialog(state = zoomOptionsDialogState) {
                zoomOptionsDialogShow = false
            }
        }
    }
}

@Preview
@Composable
private fun MyZoomImageFullSamplePreview() {
    MyZoomImageFullSample()
}