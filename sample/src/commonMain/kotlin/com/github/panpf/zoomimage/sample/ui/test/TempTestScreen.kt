package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.asPainter
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.execute
import com.github.panpf.sketch.util.windowContainerSize
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold

class TempTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("Mouse") {
            val context = LocalPlatformContext.current
            var painter by remember { mutableStateOf<Painter?>(null) }
            LaunchedEffect(Unit) {
                val imageResult = ImageRequest(context, ResourceImages.dog.uri).execute()
                painter = imageResult.image?.asPainter()
            }
            val painter1 = painter
            if (painter1 != null) {
                val sketchPainter = remember(painter1) {
                    com.github.panpf.sketch.painter.CrossfadePainter(
                        null,
                        painter1,
                        contentScale = ContentScale.FillBounds
                    )
                }
                val coilPainter = remember(painter1) {
                    coil3.compose.CrossfadePainter(
                        null,
                        painter1,
                        contentScale = ContentScale.FillBounds
                    )
                }
                val windowSize = windowContainerSize()
                if (windowSize.width > windowSize.height) {
                    Row {
                        Image(
                            painter = painter1,
                            contentDescription = "view image",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize().weight(1f),
                        )
                        Spacer(Modifier.size(10.dp))
                        Image(
                            painter = sketchPainter,
                            contentDescription = "view image",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize().weight(1f),
                        )
                        Spacer(Modifier.size(10.dp))
                        Image(
                            painter = coilPainter,
                            contentDescription = "view image",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize().weight(1f),
                        )
                    }
                } else {
                    Column {
                        Image(
                            painter = painter1,
                            contentDescription = "view image",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize().weight(1f),
                        )
                        Spacer(Modifier.size(10.dp))
                        Image(
                            painter = sketchPainter,
                            contentDescription = "view image",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize().weight(1f),
                        )
                        Spacer(Modifier.size(10.dp))
                        Image(
                            painter = coilPainter,
                            contentDescription = "view image",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize().weight(1f),
                        )
                    }
                }
            }
        }
    }
}