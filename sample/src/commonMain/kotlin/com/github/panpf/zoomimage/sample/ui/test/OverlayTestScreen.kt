package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.util.toPx
import org.koin.compose.viewmodel.koinViewModel

class OverlayTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("Overlay") {
            val zoomState = rememberSketchZoomState()
            SketchZoomAsyncImage(
                uri = ResourceImages.woodpile.uri,
                contentDescription = "Woodpile",
                modifier = Modifier.fillMaxSize(),
                zoomState = zoomState,
            )
            val contentSize = zoomState.zoomable.contentSize
            if (contentSize.width > 0) {
                val viewModel: OverlayTestViewModel = koinViewModel()
                val marks by viewModel.marks.collectAsState()
                val transform = zoomState.zoomable.transform
                val scalePivot by remember {
                    derivedStateOf {
                        val scaleOrigin = transform.scaleOrigin
                        Offset(scaleOrigin.pivotFractionX, scaleOrigin.pivotFractionY)
                    }
                }
                val markScale = contentSize.width.toFloat() / 6010f
                val density = LocalDensity.current
                val style = remember { Stroke(2.dp.toPx(density)) }
                Canvas(Modifier.fillMaxSize()) {
                    translate(left = transform.offsetX, top = transform.offsetY) {
                        scale(
                            scaleX = transform.scaleX,
                            scaleY = transform.scaleY,
                            pivot = scalePivot
                        ) {
                            marks.forEach {
                                drawCircle(
                                    color = Color.Red,
                                    radius = it.radiusPx * markScale,
                                    center = Offset(
                                        x = it.cxPx * markScale,
                                        y = it.cyPx * markScale
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