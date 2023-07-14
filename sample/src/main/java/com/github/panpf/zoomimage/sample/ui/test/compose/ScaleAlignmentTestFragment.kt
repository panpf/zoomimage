package com.github.panpf.zoomimage.sample.ui.test.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.zoomimage.rememberZoomableState
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.SampleImages
import com.github.panpf.zoomimage.sample.ui.base.compose.AppBarFragment
import com.github.panpf.zoomimage.sample.ui.util.compose.name
import com.github.panpf.zoomimage.sample.ui.widget.compose.ZoomImageMinimap
import com.github.panpf.zoomimage.sketch.ZoomAsyncImage

class ScaleAlignmentTestFragment : AppBarFragment() {

    override fun getTitle(): String = "ScaleAlignmentTest"

    @Composable
    override fun DrawContent() {
        ScaleAlignmentTestScreen()
    }
}

@Composable
private fun ScaleAlignmentTestScreen() {
    val colors = MaterialTheme.colorScheme
    val zoomableState = rememberZoomableState(debugMode = BuildConfig.DEBUG)
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
    val contentScaleState = remember { mutableStateOf(ContentScale.Fit) }
    val alignmentState = remember { mutableStateOf(Alignment.Center) }
    val smallImage = remember { mutableStateOf(true) }
    val horImage = remember { mutableStateOf(true) }
    val imageUri = remember(smallImage.value, horImage.value) {
        if (smallImage.value) {
            if (horImage.value) {
                SampleImages.Asset.DOG.uri
            } else {
                SampleImages.Asset.CAT.uri
            }
        } else {
            if (horImage.value) {
                SampleImages.Asset.QMSHT.uri
            } else {
                SampleImages.Asset.COMIC.uri
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        ZoomAsyncImage(
            imageUri = imageUri,
            contentDescription = "ZoomImage",
            modifier = Modifier.fillMaxSize(),
            state = zoomableState,
            contentScale = contentScaleState.value,
            alignment = alignmentState.value,
        )

        ZoomImageMinimap(
            sketchImageUri = imageUri,
            state = zoomableState,
            alignment = Alignment.TopStart,
        )

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
                .height(70.dp)
                .clip(RoundedCornerShape(50))
                .background(colors.tertiary.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        smallImage.value = !smallImage.value
                    }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Text(text = "小图", fontSize = 12.sp)
                Spacer(modifier = Modifier.size(4.dp))
                Switch(
                    checked = smallImage.value,
                    onCheckedChange = null
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        horImage.value = !horImage.value
                    }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Text(text = "横图", fontSize = 12.sp)
                Spacer(modifier = Modifier.size(4.dp))
                Switch(
                    checked = horImage.value,
                    onCheckedChange = null,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        contentScaleMenuExpanded = !contentScaleMenuExpanded
                    }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Text(text = "Scale", fontSize = 12.sp)
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = "Crop", fontSize = 12.sp)
                DropdownMenu(
                    expanded = contentScaleMenuExpanded,
                    onDismissRequest = {
                        contentScaleMenuExpanded = !contentScaleMenuExpanded
                    },
                ) {
                    contentScales.forEachIndexed { index, contentScale ->
                        if (index > 0) {
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(text = contentScale.name)
                            },
                            onClick = {
                                contentScaleState.value = contentScale
                                contentScaleMenuExpanded = !contentScaleMenuExpanded
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        alignmentMenuExpanded = !alignmentMenuExpanded
                    }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Text(text = "Alignment", fontSize = 12.sp)
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = "Center", fontSize = 12.sp)
                DropdownMenu(
                    expanded = alignmentMenuExpanded,
                    onDismissRequest = {
                        alignmentMenuExpanded = !alignmentMenuExpanded
                    },
                ) {
                    alignments.forEachIndexed { index, alignment ->
                        if (index > 0) {
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(text = alignment.name)
                            },
                            onClick = {
                                alignmentState.value = alignment
                                alignmentMenuExpanded = !alignmentMenuExpanded
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ScaleAlignmentTestScreenPreview() {
    ScaleAlignmentTestScreen()
}