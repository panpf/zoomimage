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
import androidx.compose.runtime.LaunchedEffect
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
fun rememberZoomImageOptionsDialogState(initialShow: Boolean = false): ZoomImageOptionsDialogState {
    val context = LocalContext.current
    val state = remember {
        val prefsService = context.prefsService
        ZoomImageOptionsDialogState(initialShow).apply {
            contentScale = contentScale(prefsService.contentScale.value)
            alignment = alignment(prefsService.alignment.value)
            threeStepScale = prefsService.threeStepScale.value
            rubberBandScale = prefsService.rubberBandScale.value
            readModeEnabled = prefsService.readModeEnabled.value
            readModeDirectionBoth = prefsService.readModeDirectionBoth.value
            scrollBarEnabled = prefsService.scrollBarEnabled.value
            animateScale = prefsService.animateScale.value
            slowerScaleAnimation = prefsService.slowerScaleAnimation.value
        }
    }
    LaunchedEffect(
        state.contentScale,
        state.alignment,
        state.threeStepScale,
        state.rubberBandScale,
        state.readModeEnabled,
        state.readModeDirectionBoth,
        state.scrollBarEnabled,
        state.animateScale,
        state.slowerScaleAnimation,
    ) {
        val prefsService = context.prefsService
        prefsService.contentScale.value = state.contentScale.name
        prefsService.alignment.value = state.alignment.name
        prefsService.threeStepScale.value = state.threeStepScale
        prefsService.rubberBandScale.value = state.rubberBandScale
        prefsService.readModeEnabled.value = state.readModeEnabled
        prefsService.readModeDirectionBoth.value = state.readModeDirectionBoth
        prefsService.scrollBarEnabled.value = state.scrollBarEnabled
        prefsService.animateScale.value = state.animateScale
        prefsService.slowerScaleAnimation.value = state.slowerScaleAnimation
    }
    return state
}

class ZoomImageOptionsDialogState(initialShow: Boolean = false) {

    var showing: Boolean by mutableStateOf(initialShow)

    var contentScale: ContentScale by mutableStateOf(ContentScale.Fit)
        internal set
    var alignment: Alignment by mutableStateOf(Alignment.Center)
        internal set
    var threeStepScale: Boolean by mutableStateOf(false)
        internal set
    var rubberBandScale: Boolean by mutableStateOf(true)
        internal set
    var readModeEnabled: Boolean by mutableStateOf(true)
        internal set
    var readModeDirectionBoth: Boolean by mutableStateOf(true)
        internal set
    var scrollBarEnabled: Boolean by mutableStateOf(true)
        internal set
    var animateScale: Boolean by mutableStateOf(true)
        internal set
    var slowerScaleAnimation: Boolean by mutableStateOf(false)
        internal set
}

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
                            state.scrollBarEnabled = !state.scrollBarEnabled
                            state.showing = false
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Scroll Bar", modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.scrollBarEnabled,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            state.readModeEnabled = !state.readModeEnabled
                            state.showing = false
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Read Mode", modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.readModeEnabled,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            state.readModeDirectionBoth = !state.readModeDirectionBoth
                            state.showing = false
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Read Mode Direction Both", modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.readModeDirectionBoth,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            state.animateScale = !state.animateScale
                            state.showing = false
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Animate Scale", modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.animateScale,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            state.threeStepScale = !state.threeStepScale
                            state.showing = false
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Three Step Scale", modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.threeStepScale,
                        onCheckedChange = null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            state.rubberBandScale = !state.rubberBandScale
                            state.showing = false
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Rubber Band Scale", modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.rubberBandScale,
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
                    Text(text = "Slower Scale Animation", modifier = Modifier.weight(1f))
                    Switch(
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