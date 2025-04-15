package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import kotlin.math.roundToInt

class TelephotoSwitchTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("Telephoto (Switch)") {
            val zoomState = rememberZoomableImageState(rememberZoomableState())
            Column(Modifier.fillMaxSize().background(Color.Black)) {
                val imageUris = remember { ResourceImages.snalls.map { it.uri } }
                var currentImageUri by remember { mutableStateOf(imageUris.first()) }
                ZoomableAsyncImage(
                    model = currentImageUri,
                    contentDescription = "Image",
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    state = zoomState,
                )

                LazyRow(Modifier.fillMaxWidth().height(100.dp)) {
                    items(imageUris) { uri ->
                        AsyncImage(
                            uri = uri,
                            contentDescription = "ThumbnailImage",
                            modifier = Modifier
                                .padding(10.dp)
                                .size(80.dp)
                                .clickable { currentImageUri = uri }
                        )
                    }
                }
            }

            Row(Modifier.padding(20.dp)) {
                val headerInfo = remember {
                    """
                    scale:
                    offset:
                    rotation:
                """.trimIndent()
                }
                Text(
                    text = headerInfo,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(offset = Offset(0f, 0f), blurRadius = 10f),
                    ),
                    overflow = TextOverflow.Ellipsis,
                )
                val transformInfo = remember(zoomState.zoomableState.contentTransformation) {
                    val transform = zoomState.zoomableState.contentTransformation
                    """
                    ${transform.scale.toShortString()}
                    ${transform.offset.round().toShortString()}
                    ${transform.rotationZ.roundToInt()}
                """.trimIndent()
                }
                Text(
                    text = transformInfo,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(offset = Offset(0f, 0f), blurRadius = 10f),
                    ),
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}