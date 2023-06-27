package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.panpf.zoomimage.sample.ui.util.compose.name

class ZoomImageOptionsDialogState(initialShow: Boolean = false) {

    var showing: Boolean by mutableStateOf(initialShow)

    var contentScale: ContentScale by mutableStateOf(ContentScale.Fit)
        internal set
    var alignment: Alignment by mutableStateOf(Alignment.Center)
        internal set
    var closeScaleAnimation: Boolean by mutableStateOf(false)
        internal set
    var slowerScaleAnimation: Boolean by mutableStateOf(false)
        internal set
}

@Composable
fun rememberZoomImageOptionsDialogState(initialShow: Boolean = false): ZoomImageOptionsDialogState =
    remember { ZoomImageOptionsDialogState(initialShow) }

@Composable
fun ZoomImageOptionsDialog(
    state: ZoomImageOptionsDialogState = rememberZoomImageOptionsDialogState(),
) {
    if (state.showing) {
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
        Dialog(onDismissRequest = {
            state.showing = false
        }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(20.dp))
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            contentScaleMenuExpanded = !contentScaleMenuExpanded
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ContentScale", modifier = Modifier.weight(1f))
                    Text(text = state.contentScale.name)
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
                                    state.contentScale = contentScale
                                    contentScaleMenuExpanded = !contentScaleMenuExpanded
                                    state.showing = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            alignmentMenuExpanded = !alignmentMenuExpanded
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Alignment", modifier = Modifier.weight(1f))
                    Text(text = state.alignment.name)
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
                                    state.alignment = alignment
                                    alignmentMenuExpanded = !alignmentMenuExpanded
                                    state.showing = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            state.closeScaleAnimation = !state.closeScaleAnimation
                            state.showing = false
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "关闭缩放动画", modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = state.closeScaleAnimation,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            state.slowerScaleAnimation = !state.slowerScaleAnimation
                            state.showing = false
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "更慢的缩放动画", modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = state.slowerScaleAnimation,
                        onCheckedChange = null
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun ZoomImageOptionsDialogPreview() {
    ZoomImageOptionsDialog(rememberZoomImageOptionsDialogState(initialShow = true))
}