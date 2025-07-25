package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.compose.util.toCompat
import com.github.panpf.zoomimage.compose.util.toPlatform
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.util.isNotEmpty
import com.github.panpf.zoomimage.sample.ui.util.times
import com.github.panpf.zoomimage.sample.ui.util.toPx
import com.github.panpf.zoomimage.subsampling.internal.calculateOriginToThumbnailScaleFactor
import com.github.panpf.zoomimage.subsampling.internal.calculateThumbnailToOriginScaleFactor
import org.koin.compose.viewmodel.koinViewModel

class OverlayTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("Overlay") {
            val zoomState = rememberSketchZoomState()
            SketchZoomAsyncImage(
                request = ComposableImageRequest(ResourceImages.woodpile.uri) {
                    size(500, 500)
                },
                contentDescription = "Woodpile",
                modifier = Modifier.fillMaxSize(),
                zoomState = zoomState,
            )

            Overlay(zoomState.zoomable)
        }
    }

    @Composable
    fun Overlay(zoomable: ZoomableState) {
        val contentSize = zoomable.contentSize.takeIf { it.isNotEmpty() } ?: return
        val contentOriginSize = zoomable.contentOriginSize.takeIf { it.isNotEmpty() } ?: return
        val transform = zoomable.transform
        val contentVisibleRect = zoomable.contentVisibleRect

        val originVisibleRect: IntRect by remember {
            derivedStateOf {
                val thumbnailToOriginScaleFactor = calculateThumbnailToOriginScaleFactor(
                    originImageSize = contentOriginSize.toCompat(),
                    thumbnailImageSize = contentSize.toCompat(),
                )
                contentVisibleRect.times(thumbnailToOriginScaleFactor.toPlatform())
            }
        }

        val originToThumbnailScaleFactor by remember {
            derivedStateOf {
                calculateOriginToThumbnailScaleFactor(
                    originImageSize = (contentOriginSize.takeIf { it.isNotEmpty() }
                        ?: contentSize).toCompat(),
                    thumbnailImageSize = contentSize.toCompat(),
                )
            }
        }

        val viewModel: OverlayTestViewModel = koinViewModel()
        val markList by viewModel.marks.collectAsState()

        val density = LocalDensity.current
        val style = remember { Stroke(2.dp.toPx(density)) }

        Canvas(Modifier.fillMaxSize().clipToBounds()) {
            translate(left = transform.offsetX, top = transform.offsetY) {
                val scalePivot = Offset(
                    x = transform.scaleOrigin.pivotFractionX,
                    y = transform.scaleOrigin.pivotFractionY
                )
                scale(
                    scaleX = transform.scaleX,
                    scaleY = transform.scaleY,
                    pivot = scalePivot
                ) {
                    scale(
                        scaleX = originToThumbnailScaleFactor.scaleX,
                        scaleY = originToThumbnailScaleFactor.scaleY,
                        pivot = scalePivot
                    ) {
                        markList.forEach { mark ->
                            val left = mark.radiusPx - mark.cxPx
                            val top = mark.radiusPx - mark.cyPx
                            val right = mark.radiusPx + mark.cxPx
                            val bottom = mark.radiusPx + mark.cyPx
                            if (left < originVisibleRect.right
                                && top < originVisibleRect.bottom
                                && right > originVisibleRect.left
                                && bottom > originVisibleRect.top
                            ) {
                                drawCircle(
                                    color = Color.Red,
                                    radius = mark.radiusPx,
                                    center = Offset(
                                        x = mark.cxPx,
                                        y = mark.cyPx
                                    ),
                                    alpha = 0.5f,
                                    style = style
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}