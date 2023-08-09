package com.github.panpf.zoomimage.sample.ui.test.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.github.panpf.sketch.compose.rememberAsyncImagePainter
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.zoomimage.sample.ui.util.compose.round
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.base.compose.AppBarFragment
import com.github.panpf.zoomimage.sample.ui.util.compose.computeContentInContainerRect
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString
import com.github.panpf.zoomimage.sample.util.BitmapScaleTransformation
import com.github.panpf.zoomimage.sample.util.format
import kotlin.math.min

class GraphicsLayerFragment : AppBarFragment() {

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
    var horImage by remember { mutableStateOf(true) }
    val imageUri = remember(horImage) {
        if (horImage) {
            newAssetUri("sample_elephant.jpg")
        } else {
            newAssetUri("sample_cat.jpg")
        }
    }
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(request = DisplayRequest(context, imageUri) {
        val resources = context.resources
        val maxSize =
            min(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels) / 4
        addTransformations(BitmapScaleTransformation(maxSize))
    })
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableStateOf(0) }

    val transformValue by remember {
        derivedStateOf {
            "缩放：${scale.format(2)}；位移：${offset.toShortString()}；旋转：${rotation}"
        }
    }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val displayValue by remember {
        derivedStateOf {
//            val rect = computeContentInContainerRect(
//                contentSize = painter.intrinsicSize.takeIf { it.isSpecified }?.round() ?: IntSize.Zero,
//                containerSize = containerSize,
//                contentScale = ContentScale.None,
//                alignment = Alignment.TopStart,
//                rotation = rotation
//            )
            val rect = computeContentInContainerRect(
                contentSize = painter.intrinsicSize.round(),
                containerSize = containerSize,
                contentScale = ContentScale.None,
                alignment = Alignment.TopStart,
                scale = scale,
                offset = offset,
                rotation = rotation
            )
            "display: ${rect.toShortString()}"
        }
    }
    val scaleStep = 0.2f
    val offsetStep = 50
    val rotateStep = 90
    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val brush = remember {
                Brush.linearGradient(
                    listOf(Color(0x88FF0000), Color(0x8800FF00), Color(0x880000FF)),
                )
            }
            Image(
                painter = painter,
                contentDescription = "dog",
                contentScale = ContentScale.None,
                alignment = Alignment.TopStart,
                modifier = Modifier
                    .fillMaxSize(0.6f)
                    .onSizeChanged { containerSize = it }
                    .align(Alignment.BottomEnd)
                    .background(brush)
                    .graphicsLayer {
                        rotationZ = rotation.toFloat()
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = transformValue,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = displayValue,
                modifier = Modifier.align(Alignment.CenterHorizontally)
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
                        onClick = { offset = Offset(offset.x - offsetStep, offset.y) },
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
                        onClick = { offset = Offset(offset.x, offset.y - offsetStep) },
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
                        onClick = { offset = Offset(offset.x + offsetStep, offset.y) },
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
                        onClick = { offset = Offset(offset.x, offset.y + offsetStep) },
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
                    Row {
                        FilledIconButton(
                            onClick = { scale -= scaleStep },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_zoom_out),
                                contentDescription = "zoom out"
                            )
                        }

                        Spacer(Modifier.size(20.dp))

                        FilledIconButton(
                            onClick = { scale += scaleStep },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_zoom_in),
                                contentDescription = "zoom in"
                            )
                        }

                        Spacer(Modifier.size(10.dp))
                    }

                    Spacer(Modifier.size(12.dp))

                    Row {
                        Spacer(Modifier.size(10.dp))

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
                                painter = painterResource(id = R.drawable.ic_rotate_right),
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
                Button(onClick = { horImage = true }) {
                    Text(text = "横图")
                }

                Spacer(modifier = Modifier.size(12.dp))

                Button(onClick = { horImage = false }) {
                    Text(text = "竖图")
                }

                Spacer(modifier = Modifier.size(12.dp))

                Button(onClick = {
                    scale = 1f
                    offset = Offset.Zero
                    rotation = 0
                }) {
                    Text(text = "重置")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GraphicsLayerSamplePreview() {
    GraphicsLayerSample()
}