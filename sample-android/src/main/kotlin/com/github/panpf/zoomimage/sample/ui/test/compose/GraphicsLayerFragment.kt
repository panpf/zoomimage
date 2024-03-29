package com.github.panpf.zoomimage.sample.ui.test.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.constraintlayout.compose.ConstraintLayout
import com.github.panpf.sketch.compose.rememberAsyncImagePainter
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.zoomimage.compose.internal.toCompat
import com.github.panpf.zoomimage.compose.internal.toPlatform
import com.github.panpf.zoomimage.compose.zoom.Transform
import com.github.panpf.zoomimage.compose.zoom.plus
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.base.compose.BaseAppBarComposeFragment
import com.github.panpf.zoomimage.sample.ui.common.compose.AutoSizeText
import com.github.panpf.zoomimage.sample.ui.util.compose.ScaleFactor
import com.github.panpf.zoomimage.sample.ui.util.compose.name
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString
import com.github.panpf.zoomimage.sample.ui.util.computeImageViewSize
import com.github.panpf.zoomimage.sample.util.BitmapScaleTransformation
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.zoom.calculateBaseTransform
import com.github.panpf.zoomimage.zoom.calculateContentDisplayRect
import kotlin.math.min
import com.github.panpf.zoomimage.sample.common.R as CommonR

class GraphicsLayerFragment : BaseAppBarComposeFragment() {

    override fun getTitle(): String {
        return "graphicsLayer"
    }

    @Composable
    override fun DrawContent() {
        GraphicsLayerSample()
    }
}


@Composable
private fun GraphicsLayerSample() {
    val context = LocalContext.current

    var horImage by remember { mutableStateOf(true) }
    val imageUri = remember(horImage) {
        if (horImage) {
            newAssetUri("sample_elephant.jpg")
        } else {
            newAssetUri("sample_cat.jpg")
        }
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
    val painter = rememberAsyncImagePainter(request = DisplayRequest(context, imageUri) {
        val resources = context.resources
        val maxSize =
            min(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels) / 4
        addTransformations(BitmapScaleTransformation(maxSize))
        listener(onSuccess = { _, result ->
            contentSize = IntSize(result.drawable.intrinsicWidth, result.drawable.intrinsicHeight)
        })
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
    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.4286f)
                .padding(end = 20.dp)
        ) {
            val density = LocalDensity.current
            val size = remember {
                computeImageViewSize(context).let {
                    with(density) {
                        DpSize(it.width.toFloat().toDp(), it.height.toFloat().toDp())
                    }
                }
            }
            Image(
                painter = painter,
                contentDescription = "dog",
                contentScale = ContentScale.None,
                alignment = Alignment.TopStart,
                modifier = Modifier
                    .size(size)
                    .onSizeChanged { containerSize = it }
                    .align(Alignment.BottomEnd)
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
                modifier = Modifier
                    .size(size)
                    .align(Alignment.BottomEnd)
                    .border(0.5f.dp, Color.Red)
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f),
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
                ConstraintLayout(
                    Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    val (center, left, up, right, down) = createRefs()
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .constrainAs(center) {
                                centerTo(parent)
                            }
                    )

                    FilledIconButton(
                        onClick = {
                            val offset = userTransform.offset
                            userTransform =
                                userTransform.copy(offset = Offset(offset.x - offsetStep, offset.y))
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .constrainAs(left) {
                                circular(center, 270f, 40.dp)
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_left),
                            contentDescription = "arrow left"
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            val offset = userTransform.offset
                            userTransform =
                                userTransform.copy(offset = Offset(offset.x, offset.y - offsetStep))
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .constrainAs(up) {
                                circular(center, 0f, 40.dp)
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_up),
                            contentDescription = "arrow up"
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            val offset = userTransform.offset
                            userTransform =
                                userTransform.copy(offset = Offset(offset.x + offsetStep, offset.y))
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .constrainAs(right) {
                                circular(center, 90f, 40.dp)
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_right),
                            contentDescription = "arrow right"
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            val offset = userTransform.offset
                            userTransform =
                                userTransform.copy(offset = Offset(offset.x, offset.y + offsetStep))
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .constrainAs(down) {
                                circular(center, 180f, 40.dp)
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_down),
                            contentDescription = "arrow down"
                        )
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
                                painter = painterResource(id = CommonR.drawable.ic_zoom_out),
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
                                painter = painterResource(id = CommonR.drawable.ic_zoom_in),
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
                                painter = painterResource(id = R.drawable.ic_rotate_left),
                                contentDescription = "rotate left"
                            )
                        }

                        Spacer(Modifier.size(20.dp))

                        FilledIconButton(
                            onClick = { rotation = (rotation + rotateStep) % 360 },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = CommonR.drawable.ic_rotate_right),
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
                                Divider(
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
                                Divider(
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
}

@Preview(showBackground = true)
@Composable
private fun GraphicsLayerSamplePreview() {
    GraphicsLayerSample()
}