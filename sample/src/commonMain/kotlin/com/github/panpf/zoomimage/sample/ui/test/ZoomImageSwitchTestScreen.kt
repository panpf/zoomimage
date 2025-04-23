package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.sample.EventBus
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_rotate_right
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ReadMode
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

val imageSwitchTestResources = arrayOf(
    ResourceImages.hugeChinaThumbnail,
    ResourceImages.hugeChina,
    ResourceImages.hugeLongComicThumbnail,
    ResourceImages.hugeLongComic,
    ResourceImages.hugeLongQmshtThumbnail,
    ResourceImages.hugeLongQmsht,
    ResourceImages.cat,
    ResourceImages.dog,
    ResourceImages.longEnd,
    ResourceImages.longWhale,
)

class ZoomImageSwitchTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("ZoomImage (Switch)") {
            val zoomState = rememberSketchZoomState()
            LaunchedEffect(zoomState) {
                zoomState.zoomable.readMode = ReadMode.Default
                zoomState.logger.level = Logger.Level.Debug
            }
            Column(Modifier.fillMaxSize().background(Color.Black)) {
                val imageUris = remember { imageSwitchTestResources.map { it.uri } }
                var currentImageUri by remember { mutableStateOf(imageUris.first()) }
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    SketchZoomAsyncImage(
                        uri = currentImageUri,
                        contentDescription = "Image",
                        modifier = Modifier.fillMaxSize(),
                        zoomState = zoomState,
                    )

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
                        val transformInfo = remember(zoomState.zoomable.transform) {
                            val transform = zoomState.zoomable.transform
                            """
                                ${transform.scale.toShortString()}
                                ${transform.offset.round().toShortString()}
                                ${transform.rotation.roundToInt()}
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

                    val coroutineScope = rememberCoroutineScope()
                    Row(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Keep: ",
                            style = LocalTextStyle.current.copy(
                                shadow = Shadow(offset = Offset(0f, 0f), blurRadius = 10f),
                            ),
                            color = Color.White
                        )
                        Switch(
                            checked = zoomState.zoomable.keepTransformWhenSameAspectRatioContentSizeChanged,
                            onCheckedChange = { isChecked ->
                                zoomState.zoomable.keepTransformWhenSameAspectRatioContentSizeChanged =
                                    isChecked
                                if (isChecked) {
                                    coroutineScope.launch {
                                        EventBus.toastFlow.emit("Keep Transform only when pictures with the same aspect ratio switch")
                                    }
                                }
                            }
                        )

                        Spacer(Modifier.weight(1f))

                        FilledIconButton(onClick = {
                            coroutineScope.launch {
                                zoomState.zoomable.rotateBy(90)
                            }
                        }) {
                            androidx.compose.material3.Icon(
                                painter = painterResource(Res.drawable.ic_rotate_right),
                                contentDescription = "Rotate",
                            )
                        }
                    }
                }

                LazyRow(Modifier.fillMaxWidth().height(100.dp)) {
                    items(imageUris) { uri ->
                        AsyncImage(
                            uri = uri,
                            contentDescription = "ThumbnailImage",
                            contentScale = ContentScale.Inside,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(80.dp)
                                .clickable { currentImageUri = uri }
                        )
                    }
                }
            }
        }
    }
}