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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.ui.util.compose.name
import com.github.panpf.zoomimage.sample.util.BaseMmkvData
import com.github.panpf.zoomimage.util.DefaultMediumScaleMinMultiple
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun rememberZoomImageOptionsState(): ZoomImageOptionsState {
    val state = remember { ZoomImageOptionsState() }

    if (!LocalInspectionMode.current) {
        val prefsService = LocalContext.current.prefsService
        BindStateAndFlow(state.contentScaleName, prefsService.contentScale)
        BindStateAndFlow(state.alignmentName, prefsService.alignment)

        BindStateAndFlow(state.animateScale, prefsService.animateScale)
        BindStateAndFlow(state.rubberBandScale, prefsService.rubberBandScale)
        BindStateAndFlow(state.threeStepScale, prefsService.threeStepScale)
        BindStateAndFlow(state.slowerScaleAnimation, prefsService.slowerScaleAnimation)
        BindStateAndFlow(state.mediumScaleMinMultiple, prefsService.mediumScaleMinMultiple)

        BindStateAndFlow(state.readModeEnabled, prefsService.readModeEnabled)
        BindStateAndFlow(state.readModeDirectionBoth, prefsService.readModeDirectionBoth)

        BindStateAndFlow(state.showTileBounds, prefsService.showTileBounds)
        BindStateAndFlow(state.ignoreExifOrientation, prefsService.ignoreExifOrientation)

        BindStateAndFlow(state.scrollBarEnabled, prefsService.scrollBarEnabled)
    }

    return state
}

@Composable
private fun <T> BindStateAndFlow(state: MutableStateFlow<T>, mmkvData: BaseMmkvData<T>) {
    LaunchedEffect(state) {
        state.value = mmkvData.value
        state.collect {
            mmkvData.value = it
        }
    }
}

class ZoomImageOptionsState {
    val contentScaleName = MutableStateFlow(ContentScale.Fit.name)
    val alignmentName = MutableStateFlow(Alignment.Center.name)

    val animateScale = MutableStateFlow(true)
    val rubberBandScale = MutableStateFlow(true)
    val threeStepScale = MutableStateFlow(false)
    val slowerScaleAnimation = MutableStateFlow(false)
    val mediumScaleMinMultiple = MutableStateFlow(DefaultMediumScaleMinMultiple.toString())

    val readModeEnabled = MutableStateFlow(true)
    val readModeDirectionBoth = MutableStateFlow(true)

    val showTileBounds = MutableStateFlow(false)
    val ignoreExifOrientation = MutableStateFlow(false)

    val scrollBarEnabled = MutableStateFlow(true)
}

@Composable
fun ZoomImageOptionsDialog(
    my: Boolean,
    state: ZoomImageOptionsState = rememberZoomImageOptionsState(),
    onDismissRequest: () -> Unit
) {
    val contentScaleName by state.contentScaleName.collectAsState()
    val alignmentName by state.alignmentName.collectAsState()

    val animateScale by state.animateScale.collectAsState()
    val rubberBandScale by state.rubberBandScale.collectAsState()
    val threeStepScale by state.threeStepScale.collectAsState()
    val slowerScaleAnimation by state.slowerScaleAnimation.collectAsState()
    val mediumScaleMinMultiple by state.mediumScaleMinMultiple.collectAsState()

    val readModeEnabled by state.readModeEnabled.collectAsState()
    val readModeDirectionBoth by state.readModeDirectionBoth.collectAsState()

    val showTileBounds by state.showTileBounds.collectAsState()
    val ignoreExifOrientation by state.ignoreExifOrientation.collectAsState()

    val scrollBarEnabled by state.scrollBarEnabled.collectAsState()

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
    var mediumScaleMinMultipleExpanded by remember { mutableStateOf(false) }
    val mediumScaleMinMultiples = remember {
        listOf(
            2.0f.toString(),
            2.5f.toString(),
            3.0f.toString(),
            3.5f.toString(),
            4.0f.toString(),
        )
    }
    Dialog(onDismissRequest) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .clickable {
                        contentScaleMenuExpanded = !contentScaleMenuExpanded
                    }
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "ContentScale", modifier = Modifier.weight(1f))
                Text(text = contentScaleName)
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
                                state.contentScaleName.value = contentScale.name
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
                    .height(45.dp)
                    .clickable {
                        alignmentMenuExpanded = !alignmentMenuExpanded
                    }
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Alignment", modifier = Modifier.weight(1f))
                Text(text = alignmentName)
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
                                state.alignmentName.value = alignment.name
                                alignmentMenuExpanded = !alignmentMenuExpanded
                                onDismissRequest()
                            }
                        )
                    }
                }
            }

            if (my) {
                Divider(Modifier.padding(horizontal = 20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .clickable {
                            state.animateScale.value = !state.animateScale.value
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
                        .height(45.dp)
                        .clickable {
                            state.rubberBandScale.value = !state.rubberBandScale.value
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
                        .height(45.dp)
                        .clickable {
                            state.threeStepScale.value = !state.threeStepScale.value
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
                        .height(45.dp)
                        .clickable {
                            state.slowerScaleAnimation.value = !state.slowerScaleAnimation.value
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
                        .height(45.dp)
                        .clickable {
                            mediumScaleMinMultipleExpanded = !mediumScaleMinMultipleExpanded
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Medium Scale Min Multiple", modifier = Modifier.weight(1f))
                    Text(text = mediumScaleMinMultiple)
                    Icon(
                        painter = painterResource(id = R.drawable.ic_expand_more),
                        contentDescription = "more"
                    )
                    DropdownMenu(
                        expanded = mediumScaleMinMultipleExpanded,
                        onDismissRequest = {
                            mediumScaleMinMultipleExpanded = !mediumScaleMinMultipleExpanded
                        },
                    ) {
                        mediumScaleMinMultiples.forEachIndexed { index, mediumScaleMinMultiple ->
                            if (index > 0) {
                                Divider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp)
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(text = mediumScaleMinMultiple)
                                },
                                onClick = {
                                    state.mediumScaleMinMultiple.value = mediumScaleMinMultiple
                                    alignmentMenuExpanded = !alignmentMenuExpanded
                                    onDismissRequest()
                                }
                            )
                        }
                    }
                }

                Divider(Modifier.padding(horizontal = 20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .clickable {
                            state.readModeEnabled.value = !state.readModeEnabled.value
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
                        .height(45.dp)
                        .clickable {
                            state.readModeDirectionBoth.value = !state.readModeDirectionBoth.value
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

                Divider(Modifier.padding(horizontal = 20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .clickable {
                            state.showTileBounds.value = !state.showTileBounds.value
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
                        .height(45.dp)
                        .clickable {
                            state.ignoreExifOrientation.value = !state.ignoreExifOrientation.value
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

                Divider(Modifier.padding(horizontal = 20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .clickable {
                            state.scrollBarEnabled.value = !state.scrollBarEnabled.value
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