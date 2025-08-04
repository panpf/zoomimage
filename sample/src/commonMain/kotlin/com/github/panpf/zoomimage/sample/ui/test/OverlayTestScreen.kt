package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.zooming
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_rotate_right
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.util.isEmpty
import com.github.panpf.zoomimage.sample.ui.util.toPx
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

class OverlayTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold(title = "Overlay") {
            val zoomState = rememberSketchZoomState()
            SketchZoomAsyncImage(
                request = ComposableImageRequest(ResourceImages.woodpile.uri) {
                    size(500, 500)
                },
                contentDescription = "Woodpile",
                modifier = Modifier.fillMaxSize(),
                zoomState = zoomState,
            )

            val viewModel: OverlayTestViewModel = koinViewModel()
            Overlay(
                zoomable = zoomState.zoomable,
                viewModel = viewModel,
            )

            val coroutineScope = rememberCoroutineScope()
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .alpha(0.8f)
                    .clip(RoundedCornerShape(50))
                    .background(colorScheme.tertiaryContainer)
                    .size(50.dp)
                    .clickable {
                        coroutineScope.launch {
                            zoomState.zoomable.rotateBy(90)
                        }
                    },
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_rotate_right),
                    contentDescription = null,
                    tint = colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(30.dp).align(Alignment.Center)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
                    .alpha(0.8f)
            ) {
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colorScheme.tertiaryContainer)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val rectMode by viewModel.rectMode.collectAsState()
                    Text("Point")
                    Spacer(Modifier.size(4.dp))
                    Switch(checked = rectMode, onCheckedChange = { viewModel.setRectMode(it) })
                    Spacer(Modifier.size(4.dp))
                    Text("Rect")
                }

                Spacer(Modifier.size(10.dp))

                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colorScheme.tertiaryContainer)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val partitionMode by viewModel.partitionMode.collectAsState()
                    Text("Overall")
                    Spacer(Modifier.size(4.dp))
                    Switch(
                        checked = partitionMode,
                        onCheckedChange = { viewModel.setPartitionMode(it) })
                    Spacer(Modifier.size(4.dp))
                    Text("Partition")
                }
            }
        }
    }

    @Composable
    fun Overlay(zoomable: ZoomableState, viewModel: OverlayTestViewModel) {
        if (zoomable.containerSize.isEmpty()) return
        if (zoomable.contentSize.isEmpty()) return
        if (zoomable.contentOriginSize.isEmpty()) return

        val markList: ImmutableList<Mark> by viewModel.marks.collectAsState()
        val rectMode by viewModel.rectMode.collectAsState()
        val partitionMode by viewModel.partitionMode.collectAsState()
        if (partitionMode) {
            DrawMarksWithPartitionMapping(zoomable, rectMode, markList)
        } else {
            DrawMarksWithOverallMapping(zoomable, rectMode, markList)
        }
    }

    @Composable
    fun DrawMarksWithOverallMapping(
        zoomable: ZoomableState,
        rectMode: Boolean,
        markList: ImmutableList<Mark>
    ) {
        val sourceVisibleRect = zoomable.sourceVisibleRectF.takeIf { !it.isEmpty } ?: return
        val density = LocalDensity.current
        val style by remember {
            derivedStateOf {
                // Always keep the border looking width 2dp
                Stroke(2.dp.toPx(density) / zoomable.sourceScaleFactor.scaleX)
            }
        }
        Canvas(
            Modifier
                .fillMaxSize()
                .zooming(zoomable, firstScaleByContentSize = true)
        ) {
            markList.forEach { mark ->
                val markRect = Rect(
                    center = Offset(x = mark.cxPx, y = mark.cyPx),
                    radius = mark.radiusPx
                )
                if (sourceVisibleRect.overlaps(other = markRect)) {
                    if (rectMode) {
                        drawRect(
                            color = Color.Red,
                            topLeft = markRect.topLeft,
                            size = markRect.size,
                            alpha = 0.5f,
                            style = style
                        )
                    } else {
                        drawCircle(
                            color = Color.Red,
                            radius = mark.radiusPx,
                            center = Offset(x = mark.cxPx, y = mark.cyPx),
                            alpha = 0.5f,
                            style = style
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun DrawMarksWithPartitionMapping(
        zoomable: ZoomableState,
        rectMode: Boolean,
        markList: ImmutableList<Mark>
    ) {
        val sourceVisibleRect = zoomable.sourceVisibleRectF.takeIf { !it.isEmpty } ?: return
        val sourceScaleFactor = zoomable.sourceScaleFactor
        val density = LocalDensity.current
        val style = remember { Stroke(2.dp.toPx(density)) }
        Canvas(
            Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            markList.forEach { mark ->
                val markRect = Rect(
                    center = Offset(x = mark.cxPx, y = mark.cyPx),
                    radius = mark.radiusPx
                )
                if (sourceVisibleRect.overlaps(other = markRect)) {
                    if (rectMode) {
                        val drawRect = zoomable.sourceToDraw(markRect)
                        drawRect(
                            color = Color.Red,
                            topLeft = drawRect.topLeft,
                            size = drawRect.size,
                            alpha = 0.5f,
                            style = style
                        )
                    } else {
                        val drawPoint = zoomable.sourceToDraw(Offset(mark.cxPx, mark.cyPx))
                        val drawRadius = mark.radiusPx * sourceScaleFactor.scaleX
                        drawCircle(
                            color = Color.Red,
                            radius = drawRadius,
                            center = drawPoint,
                            alpha = 0.5f,
                            style = style
                        )
                    }
                }
            }
        }
    }
}