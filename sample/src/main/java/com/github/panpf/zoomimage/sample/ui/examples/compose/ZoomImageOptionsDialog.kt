package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.ui.util.compose.alignment
import com.github.panpf.zoomimage.sample.ui.util.compose.contentScale
import com.github.panpf.zoomimage.sample.ui.util.compose.name

@Composable
fun ZoomImageOptionsDialog(my: Boolean, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val prefsService = remember { context.prefsService }
    val contentScaleName by prefsService.contentScale.stateFlow.collectAsState()
    val alignmentName by prefsService.alignment.stateFlow.collectAsState()
    val contentScale = remember(contentScaleName) { contentScale(contentScaleName) }
    val alignment = remember(alignmentName) { alignment(alignmentName) }
    val threeStepScale by prefsService.threeStepScale.stateFlow.collectAsState()
    val rubberBandScale by prefsService.rubberBandScale.stateFlow.collectAsState()
    val readModeEnabled by prefsService.readModeEnabled.stateFlow.collectAsState()
    val readModeDirectionBoth by prefsService.readModeDirectionBoth.stateFlow.collectAsState()
    val scrollBarEnabled by prefsService.scrollBarEnabled.stateFlow.collectAsState()
    val animateScale by prefsService.animateScale.stateFlow.collectAsState()
    val slowerScaleAnimation by prefsService.slowerScaleAnimation.stateFlow.collectAsState()
    val showTileBounds by prefsService.showTileBounds.stateFlow.collectAsState()
    val ignoreExifOrientation by prefsService.ignoreExifOrientation.stateFlow.collectAsState()
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
    Dialog(onDismissRequest) {
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
                Text(text = contentScale.name)
                Icon(
                    painter = painterResource(id = R.drawable.ic_expand_more),
                    contentDescription = "more"
                )
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
                                prefsService.contentScale.value = contentScale.name
                                contentScaleMenuExpanded = !contentScaleMenuExpanded
                                onDismissRequest()
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
                Text(text = alignment.name)
                Icon(
                    painter = painterResource(id = R.drawable.ic_expand_more),
                    contentDescription = "more"
                )
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
                                prefsService.alignment.value = alignment.name
                                alignmentMenuExpanded = !alignmentMenuExpanded
                                onDismissRequest()
                            }
                        )
                    }
                }
            }

            if (my) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            prefsService.scrollBarEnabled.value = !scrollBarEnabled
                            onDismissRequest()
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Scroll Bar", modifier = Modifier.weight(1f))
                    Switch(
                        checked = scrollBarEnabled,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            prefsService.readModeEnabled.value = !readModeEnabled
                            onDismissRequest()
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Read Mode", modifier = Modifier.weight(1f))
                    Switch(
                        checked = readModeEnabled,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            prefsService.readModeDirectionBoth.value = !readModeDirectionBoth
                            onDismissRequest()
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Read Mode Direction Both", modifier = Modifier.weight(1f))
                    Switch(
                        checked = readModeDirectionBoth,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            prefsService.animateScale.value = !animateScale
                            onDismissRequest()
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Animate Scale", modifier = Modifier.weight(1f))
                    Switch(
                        checked = animateScale,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            prefsService.threeStepScale.value = !threeStepScale
                            onDismissRequest()
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Three Step Scale", modifier = Modifier.weight(1f))
                    Switch(
                        checked = threeStepScale,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            prefsService.rubberBandScale.value = !rubberBandScale
                            onDismissRequest()
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Rubber Band Scale", modifier = Modifier.weight(1f))
                    Switch(
                        checked = rubberBandScale,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            prefsService.slowerScaleAnimation.value = !slowerScaleAnimation
                            onDismissRequest()
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Slower Scale Animation", modifier = Modifier.weight(1f))
                    Switch(
                        checked = slowerScaleAnimation,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            prefsService.showTileBounds.value = !showTileBounds
                            onDismissRequest()
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Show Tile Bounds", modifier = Modifier.weight(1f))
                    Switch(
                        checked = showTileBounds,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            prefsService.ignoreExifOrientation.value = !ignoreExifOrientation
                            onDismissRequest()
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Ignore Exif Orientation", modifier = Modifier.weight(1f))
                    Switch(
                        checked = ignoreExifOrientation,
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
    ZoomImageOptionsDialog(true) {

    }
}