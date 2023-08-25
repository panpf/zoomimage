package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.settingsService
import com.github.panpf.zoomimage.sample.ui.util.compose.name
import com.github.panpf.zoomimage.sample.util.BaseMmkvData
import com.github.panpf.zoomimage.zoom.DefaultStepScaleMinMultiple
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun rememberZoomImageOptionsState(): ZoomImageOptionsState {
    val state = remember { ZoomImageOptionsState() }

    if (!LocalInspectionMode.current) {
        val settingsService = LocalContext.current.settingsService
        BindStateAndFlow(state.contentScaleName, settingsService.contentScale)
        BindStateAndFlow(state.alignmentName, settingsService.alignment)

        BindStateAndFlow(state.animateScale, settingsService.animateScale)
        BindStateAndFlow(state.rubberBandScale, settingsService.rubberBandScale)
        BindStateAndFlow(state.threeStepScale, settingsService.threeStepScale)
        BindStateAndFlow(state.slowerScaleAnimation, settingsService.slowerScaleAnimation)
        BindStateAndFlow(state.stepScaleMinMultiple, settingsService.stepScaleMinMultiple)
        BindStateAndFlow(
            state.limitOffsetWithinBaseVisibleRect,
            settingsService.limitOffsetWithinBaseVisibleRect
        )

        BindStateAndFlow(state.readModeEnabled, settingsService.readModeEnabled)
        BindStateAndFlow(state.readModeDirectionBoth, settingsService.readModeDirectionBoth)

        BindStateAndFlow(state.showTileBounds, settingsService.showTileBounds)
        BindStateAndFlow(state.ignoreExifOrientation, settingsService.ignoreExifOrientation)

        BindStateAndFlow(state.scrollBarEnabled, settingsService.scrollBarEnabled)
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
    val stepScaleMinMultiple = MutableStateFlow(DefaultStepScaleMinMultiple.toString())
    val limitOffsetWithinBaseVisibleRect = MutableStateFlow(false)

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
    val stepScaleMinMultiple by state.stepScaleMinMultiple.collectAsState()
    val limitOffsetWithinBaseVisibleRect by state.limitOffsetWithinBaseVisibleRect.collectAsState()

    val readModeEnabled by state.readModeEnabled.collectAsState()
    val readModeDirectionBoth by state.readModeDirectionBoth.collectAsState()

    val showTileBounds by state.showTileBounds.collectAsState()
    val ignoreExifOrientation by state.ignoreExifOrientation.collectAsState()

    val scrollBarEnabled by state.scrollBarEnabled.collectAsState()

    Dialog(onDismissRequest) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(20.dp))
        ) {
            val contentScales = remember {
                listOf(
                    ContentScale.Fit,
                    ContentScale.Crop,
                    ContentScale.Inside,
                    ContentScale.FillWidth,
                    ContentScale.FillHeight,
                    ContentScale.FillBounds,
                    ContentScale.None,
                ).map { it.name }
            }
            MyDropdownMenu("ContentScale", contentScaleName, contentScales) {
                state.contentScaleName.value = it
                onDismissRequest()
            }

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
                ).map { it.name }
            }
            MyDropdownMenu("Alignment", alignmentName, alignments) {
                state.alignmentName.value = it
                onDismissRequest()
            }

            if (my) {
                Divider(Modifier.padding(horizontal = 20.dp))

                SwitchMenu("Animate Scale", animateScale) {
                    state.animateScale.value = !state.animateScale.value
                    onDismissRequest()
                }
                SwitchMenu("Rubber Band Scale", rubberBandScale) {
                    state.rubberBandScale.value = !state.rubberBandScale.value
                    onDismissRequest()
                }
                SwitchMenu("Three Step Scale", threeStepScale) {
                    state.threeStepScale.value = !state.threeStepScale.value
                    onDismissRequest()
                }
                SwitchMenu("Slower Scale Animation", slowerScaleAnimation) {
                    state.slowerScaleAnimation.value = !state.slowerScaleAnimation.value
                    onDismissRequest()
                }

                val stepScaleMinMultiples = remember {
                    listOf(
                        2.0f.toString(),
                        2.5f.toString(),
                        3.0f.toString(),
                        3.5f.toString(),
                        4.0f.toString(),
                    )
                }
                MyDropdownMenu(
                    "Step Scale Min Multiple",
                    stepScaleMinMultiple,
                    stepScaleMinMultiples
                ) {
                    state.stepScaleMinMultiple.value = it
                    onDismissRequest()
                }

                SwitchMenu(
                    "Limit Offset Within Base Visible Rect",
                    limitOffsetWithinBaseVisibleRect
                ) {
                    state.limitOffsetWithinBaseVisibleRect.value =
                        !state.limitOffsetWithinBaseVisibleRect.value
                    onDismissRequest()
                }

                Divider(Modifier.padding(horizontal = 20.dp))

                SwitchMenu("Read Mode", readModeEnabled) {
                    state.readModeEnabled.value = !state.readModeEnabled.value
                    onDismissRequest()
                }
                SwitchMenu("Read Mode Direction Both", readModeDirectionBoth) {
                    state.readModeDirectionBoth.value = !state.readModeDirectionBoth.value
                    onDismissRequest()
                }

                Divider(Modifier.padding(horizontal = 20.dp))

                SwitchMenu("Show Tile Bounds", showTileBounds) {
                    state.showTileBounds.value = !state.showTileBounds.value
                    onDismissRequest()
                }
                SwitchMenu("Ignore Exif Orientation", ignoreExifOrientation) {
                    state.ignoreExifOrientation.value = !state.ignoreExifOrientation.value
                    onDismissRequest()
                }

                Divider(Modifier.padding(horizontal = 20.dp))

                SwitchMenu("Scroll Bar", scrollBarEnabled) {
                    state.scrollBarEnabled.value = !state.scrollBarEnabled.value
                    onDismissRequest()
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

@Composable
private fun SwitchMenu(
    name: String,
    value: Boolean,
    onToggled: (value: Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable {
                onToggled(!value)
            }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, modifier = Modifier.weight(1f), fontSize = 12.sp)
        val colorScheme = MaterialTheme.colorScheme
        Switch(
            checked = value,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorScheme.primary,
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwitchMenuPreview() {
    SwitchMenu("Animate Scale", false) {

    }
}

@Composable
private fun MyDropdownMenu(
    name: String,
    value: String,
    values: List<String>,
    onSelected: (value: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clickable {
                    expanded = !expanded
                }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = name, modifier = Modifier.weight(1f), fontSize = 12.sp)
            Text(text = value, fontSize = 12.sp)
            Icon(
                painter = painterResource(id = R.drawable.ic_expand_more),
                contentDescription = "more"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = !expanded
            },
        ) {
            values.forEachIndexed { index, value ->
                if (index > 0) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                    )
                }
                DropdownMenuItem(
                    text = {
                        Text(text = value)
                    },
                    onClick = {
                        expanded = !expanded
                        onSelected(value)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyDropdownMenuPreview() {
    val values = remember {
        listOf("A", "B", "C", "D")
    }
    MyDropdownMenu("Animate Scale", "A", values) {

    }
}