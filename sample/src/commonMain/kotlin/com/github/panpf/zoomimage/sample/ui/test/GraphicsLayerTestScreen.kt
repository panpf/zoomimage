package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.size
import com.github.panpf.sketch.util.toIntSize
import com.github.panpf.zoomimage.compose.util.toCompat
import com.github.panpf.zoomimage.compose.util.toPlatform
import com.github.panpf.zoomimage.compose.zoom.Transform
import com.github.panpf.zoomimage.compose.zoom.plus
import com.github.panpf.zoomimage.sample.image.BitmapScaleTransformation
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_arrow_down
import com.github.panpf.zoomimage.sample.resources.ic_arrow_left
import com.github.panpf.zoomimage.sample.resources.ic_arrow_right
import com.github.panpf.zoomimage.sample.resources.ic_arrow_up
import com.github.panpf.zoomimage.sample.resources.ic_rotate_left
import com.github.panpf.zoomimage.sample.resources.ic_rotate_right
import com.github.panpf.zoomimage.sample.resources.ic_zoom_in
import com.github.panpf.zoomimage.sample.resources.ic_zoom_out
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.components.AutoSizeText
import com.github.panpf.zoomimage.sample.ui.util.ScaleFactor
import com.github.panpf.zoomimage.sample.ui.util.name
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.sample.ui.util.windowSize
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.zoom.calculateBaseTransform
import com.github.panpf.zoomimage.zoom.calculateContentDisplayRect
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.painterResource
import kotlin.math.min

class GraphicsLayerTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("Graphics Layer") {
            val context = LocalPlatformContext.current

            var horImage by remember { mutableStateOf(true) }
            val imageUri = remember(horImage) {
                if (horImage) ResourceImages.longEnd.uri else ResourceImages.longWhale.uri
            }

            var containerSize by remember { mutableStateOf(IntSize.Zero) }
            var contentSize by remember { mutableStateOf(IntSize.Zero) }
            val rotationOrigin by remember {
                derivedStateOf {
                    contentSize.toSize().center.let {
                        TransformOrigin(
                            it.x / containerSize.width,
                            it.y / containerSize.height
                        )
                    }
                }
            }
            val state = rememberAsyncImageState()
            LaunchedEffect(Unit) {
                snapshotFlow { state.result }.filter { it is ImageResult.Success }.collect {
                    contentSize = (it as ImageResult.Success).image.size.toIntSize()
                }
            }
            val painter =
                rememberAsyncImagePainter(request = ComposableImageRequest(context, imageUri) {
                    memoryCachePolicy(CachePolicy.DISABLED)
                    val windowSize = windowSize()
                    val maxSize = min(windowSize.width, windowSize.height) / 4
                    addTransformations(BitmapScaleTransformation(maxSize))
                })

            var contentScale by remember { mutableStateOf(ContentScale.Fit) }
            var alignment by remember { mutableStateOf(Alignment.Center) }
            var rotation by remember { mutableIntStateOf(0) }

            val baseTransform by remember {
                derivedStateOf {
                    calculateBaseTransform(
                        containerSize = containerSize.toCompat(),
                        contentSize = contentSize.toCompat(),
                        contentScale = contentScale.toCompat(),
                        alignment = alignment.toCompat(),
                        rotation = rotation,
                    ).toPlatform()
                }
            }
            var userTransform by remember { mutableStateOf(Transform.Origin) }
            val displayTransform by remember {
                derivedStateOf { baseTransform + userTransform }
            }

            var contentScaleMenuExpanded by remember { mutableStateOf(false) }
            val contentScales = remember {
                listOf(
                    ContentScale.Fit,
                    ContentScale.Crop,
                    ContentScale.Inside,
                    ContentScale.FillWidth,
                    ContentScale.FillHeight,
                    ContentScale.FillBounds,
                    ContentScale.None,
                )
            }
            var alignmentMenuExpanded by remember { mutableStateOf(false) }
            val alignments = remember {
                listOf(
                    Alignment.TopStart,
                    Alignment.TopCenter,
                    Alignment.TopEnd,
                    Alignment.CenterStart,
                    Alignment.Center,
                    Alignment.CenterEnd,
                    Alignment.BottomStart,
                    Alignment.BottomCenter,
                    Alignment.BottomEnd,
                )
            }

            val transformValue by remember {
                derivedStateOf {
                    val scaleString = displayTransform.scaleX.format(2).toString()
                    val translationString = displayTransform.offset.round().toShortString()
                    val rotationString = rotation.toString()
                    "scale: ${scaleString}, offset: ${translationString}, rotation: $rotationString"
                }
            }
            val displayValue by remember {
                derivedStateOf {
                    val rect = calculateContentDisplayRect(
                        contentSize = contentSize.toCompat(),
                        containerSize = containerSize.toCompat(),
                        contentScale = contentScale.toCompat(),
                        alignment = alignment.toCompat(),
                        rotation = rotation,
                        userScale = userTransform.scaleX,
                        userOffset = userTransform.offset.toCompat(),
                    )
                    "display: ${rect.toShortString()}"
                }
            }
            val sizeValue by remember {
                derivedStateOf {
                    "container: ${containerSize.toShortString()}, content: ${contentSize.toShortString()}"
                }
            }

            val scaleStep = 0.2f
            val offsetStep = 50
            val rotateStep = 90
            Container(
                modifier = Modifier.fillMaxSize(),
                imageContent = { column: Boolean ->
                    val modifier = if (column) {
                        Modifier
                            .fillMaxHeight(0.6f)
                            .aspectRatio(1f)
                    } else {
                        Modifier
                            .fillMaxWidth(0.6f)
                            .aspectRatio(1f)
                    }
                    Image(
                        painter = painter,
                        contentDescription = "dog",
                        contentScale = ContentScale.None,
                        alignment = Alignment.TopStart,
                        modifier = modifier
                            .onSizeChanged { containerSize = it }
                            .align(Alignment.Center)
                            .graphicsLayer {
                                scaleX = displayTransform.scaleX
                                scaleY = displayTransform.scaleY
                                translationX = displayTransform.offsetX
                                translationY = displayTransform.offsetY
                                transformOrigin = displayTransform.scaleOrigin
                            }
                            .graphicsLayer {
                                rotationZ = displayTransform.rotation
                                transformOrigin = rotationOrigin
                            }
                    )
                    Box(
                        modifier = modifier
                            .align(Alignment.Center)
                            .border(0.5f.dp, Color.Red)
                    )
                },
                operationContent = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = displayValue,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            fontSize = 14.sp,
                        )
                        Text(
                            text = transformValue,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            fontSize = 14.sp,
                        )
                        Text(
                            text = sizeValue,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            fontSize = 14.sp,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                        .align(Alignment.Center)
                                ) {
                                    FilledIconButton(
                                        onClick = {
                                            val offset = userTransform.offset
                                            userTransform =
                                                userTransform.copy(
                                                    offset = Offset(
                                                        offset.x - offsetStep,
                                                        offset.y
                                                    )
                                                )
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .align(Alignment.CenterStart)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_arrow_left),
                                            contentDescription = "arrow left"
                                        )
                                    }

                                    FilledIconButton(
                                        onClick = {
                                            val offset = userTransform.offset
                                            userTransform =
                                                userTransform.copy(
                                                    offset = Offset(
                                                        offset.x,
                                                        offset.y - offsetStep
                                                    )
                                                )
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .align(Alignment.TopCenter)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_arrow_up),
                                            contentDescription = "arrow up"
                                        )
                                    }

                                    FilledIconButton(
                                        onClick = {
                                            val offset = userTransform.offset
                                            userTransform =
                                                userTransform.copy(
                                                    offset = Offset(
                                                        offset.x + offsetStep,
                                                        offset.y
                                                    )
                                                )
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .align(Alignment.CenterEnd)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_arrow_right),
                                            contentDescription = "arrow right"
                                        )
                                    }

                                    FilledIconButton(
                                        onClick = {
                                            val offset = userTransform.offset
                                            userTransform =
                                                userTransform.copy(
                                                    offset = Offset(
                                                        offset.x,
                                                        offset.y + offsetStep
                                                    )
                                                )
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .align(Alignment.BottomCenter)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_arrow_down),
                                            contentDescription = "arrow down"
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.size(12.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Row(Modifier.padding(end = 20.dp)) {
                                    FilledIconButton(
                                        onClick = {
                                            val scale = userTransform.scale.scaleX
                                            userTransform =
                                                userTransform.copy(scale = ScaleFactor(scale - scaleStep))
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_zoom_out),
                                            contentDescription = "zoom out"
                                        )
                                    }

                                    Spacer(Modifier.size(20.dp))

                                    FilledIconButton(
                                        onClick = {
                                            val scale = userTransform.scale.scaleX
                                            userTransform =
                                                userTransform.copy(scale = ScaleFactor(scale + scaleStep))
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_zoom_in),
                                            contentDescription = "zoom in"
                                        )
                                    }
                                }

                                Spacer(Modifier.size(12.dp))

                                Row(Modifier.padding(start = 20.dp)) {
                                    FilledIconButton(
                                        onClick = { rotation = (rotation - rotateStep) % 360 },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_rotate_left),
                                            contentDescription = "rotate left"
                                        )
                                    }

                                    Spacer(Modifier.size(20.dp))

                                    FilledIconButton(
                                        onClick = { rotation = (rotation + rotateStep) % 360 },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_rotate_right),
                                            contentDescription = "rotate right"
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.size(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Spacer(modifier = Modifier.size(12.dp))

                            Button(
                                onClick = { horImage = !horImage },
                                contentPadding = PaddingValues(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            ) {
                                AutoSizeText(text = if (horImage) "Ver" else "Hor", maxLines = 1)
                            }

                            Spacer(modifier = Modifier.size(12.dp))

                            Box(Modifier.weight(1f)) {
                                Button(
                                    onClick = { contentScaleMenuExpanded = true },
                                    contentPadding = PaddingValues(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    AutoSizeText(text = contentScale.name, maxLines = 1)
                                }

                                DropdownMenu(
                                    expanded = contentScaleMenuExpanded,
                                    onDismissRequest = {
                                        contentScaleMenuExpanded = false
                                    },
                                ) {
                                    contentScales.forEachIndexed { index, newContentScale ->
                                        if (index > 0) {
                                            HorizontalDivider(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp)
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = {
                                                Text(text = newContentScale.name)
                                            },
                                            onClick = {
                                                contentScale = newContentScale
                                                contentScaleMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.size(12.dp))

                            Box(Modifier.weight(1f)) {
                                Button(
                                    onClick = { alignmentMenuExpanded = true },
                                    contentPadding = PaddingValues(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    AutoSizeText(text = alignment.name, maxLines = 1)
                                }

                                DropdownMenu(
                                    expanded = alignmentMenuExpanded,
                                    onDismissRequest = {
                                        alignmentMenuExpanded = false
                                    },
                                ) {
                                    alignments.forEachIndexed { index, newAlignment ->
                                        if (index > 0) {
                                            HorizontalDivider(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp)
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = {
                                                Text(text = newAlignment.name)
                                            },
                                            onClick = {
                                                alignment = newAlignment
                                                alignmentMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.size(12.dp))

                            Button(
                                onClick = {
                                    userTransform = Transform.Origin
                                    rotation = 0
                                },
                                contentPadding = PaddingValues(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                AutoSizeText(text = "Reset", maxLines = 1)
                            }

                            Spacer(modifier = Modifier.size(12.dp))
                        }
                    }
                }
            )
        }
    }

    @Composable
    inline fun Container(
        modifier: Modifier,
        imageContent: @Composable BoxScope.(column: Boolean) -> Unit,
        operationContent: @Composable BoxScope.(column: Boolean) -> Unit
    ) {
        val windowSize = windowSize()
        if (windowSize.width < windowSize.height) {
            Column(modifier) {
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    imageContent(true)
                }
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    operationContent(true)
                }
            }
        } else {
            Row(modifier) {
                Box(Modifier.fillMaxHeight().weight(1f)) {
                    imageContent(false)
                }
                Box(Modifier.fillMaxHeight().weight(1f)) {
                    operationContent(false)
                }
            }
        }
    }
}