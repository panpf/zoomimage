package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.panpf.tools4a.display.ktx.getDisplayMetrics
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.SettingsService
import com.github.panpf.zoomimage.sample.settingsService
import com.github.panpf.zoomimage.sample.ui.util.compose.name
import com.github.panpf.zoomimage.sample.ui.util.compose.toDp
import com.github.panpf.zoomimage.sample.util.BaseMmkvData
import com.github.panpf.zoomimage.subsampling.TileManager
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import com.github.panpf.zoomimage.zoom.ScalesCalculator
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
        BindStateAndFlow(state.oneFingerScale, settingsService.oneFingerScale)
        BindStateAndFlow(state.slowerScaleAnimation, settingsService.slowerScaleAnimation)
        BindStateAndFlow(state.scalesCalculator, settingsService.scalesCalculator)
        BindStateAndFlow(state.scalesMultiple, settingsService.scalesMultiple)
        BindStateAndFlow(
            state.limitOffsetWithinBaseVisibleRect,
            settingsService.limitOffsetWithinBaseVisibleRect
        )

        BindStateAndFlow(state.readModeEnabled, settingsService.readModeEnabled)
        BindStateAndFlow(state.readModeAcceptedBoth, settingsService.readModeAcceptedBoth)

        BindStateAndFlow(
            state.disabledGestureType,
            settingsService.disabledGestureType
        )
        BindStateAndFlow(
            state.pausedContinuousTransformType,
            settingsService.pausedContinuousTransformType
        )
        BindStateAndFlow(state.disabledBackgroundTiles, settingsService.disabledBackgroundTiles)
        BindStateAndFlow(state.ignoreExifOrientation, settingsService.ignoreExifOrientation)
        BindStateAndFlow(state.showTileBounds, settingsService.showTileBounds)
        BindStateAndFlow(state.tileAnimation, settingsService.tileAnimation)

        BindStateAndFlow(state.scrollBarEnabled, settingsService.scrollBarEnabled)
        BindStateAndFlow(state.logLevel, settingsService.logLevel)
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
    val oneFingerScale = MutableStateFlow(false)
    val slowerScaleAnimation = MutableStateFlow(false)
    val scalesCalculator = MutableStateFlow("Dynamic")
    val scalesMultiple = MutableStateFlow(ScalesCalculator.Multiple.toString())
    val limitOffsetWithinBaseVisibleRect = MutableStateFlow(false)

    val readModeEnabled = MutableStateFlow(true)
    val readModeAcceptedBoth = MutableStateFlow(true)

    val showTileBounds = MutableStateFlow(false)
    val tileAnimation = MutableStateFlow(true)
    val pausedContinuousTransformType =
        MutableStateFlow(TileManager.DefaultPausedContinuousTransformType.toString())
    val disabledGestureType = MutableStateFlow(0.toString())
    val disabledBackgroundTiles = MutableStateFlow(false)
    val ignoreExifOrientation = MutableStateFlow(false)

    val scrollBarEnabled = MutableStateFlow(true)
    val logLevel = MutableStateFlow(SettingsService.defaultLogLevel())
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
    val oneFingerScale by state.oneFingerScale.collectAsState()
    val slowerScaleAnimation by state.slowerScaleAnimation.collectAsState()
    val scalesCalculator by state.scalesCalculator.collectAsState()
    val scalesMultiple by state.scalesMultiple.collectAsState()
    val limitOffsetWithinBaseVisibleRect by state.limitOffsetWithinBaseVisibleRect.collectAsState()

    val readModeEnabled by state.readModeEnabled.collectAsState()
    val readModeAcceptedBoth by state.readModeAcceptedBoth.collectAsState()

    val showTileBounds by state.showTileBounds.collectAsState()
    val tileAnimation by state.tileAnimation.collectAsState()
    val disabledGestureTypeString by state.disabledGestureType.collectAsState()
    val disabledGestureType = remember(disabledGestureTypeString) {
        disabledGestureTypeString.toInt()
    }
    val pausedContinuousTransformTypeString by state.pausedContinuousTransformType.collectAsState()
    val pausedContinuousTransformType = remember(pausedContinuousTransformTypeString) {
        pausedContinuousTransformTypeString.toInt()
    }
    val disabledBackgroundTiles by state.disabledBackgroundTiles.collectAsState()
    val ignoreExifOrientation by state.ignoreExifOrientation.collectAsState()

    val scrollBarEnabled by state.scrollBarEnabled.collectAsState()
    val logLevel by state.logLevel.collectAsState()

    val menuCount = 16
    Dialog(onDismissRequest) {
        val screenHeightPixels = LocalContext.current.getDisplayMetrics().heightPixels
        val menuItemHeight = LocalContext.current.resources.getDimension(R.dimen.menu_item_height)
        val dialogMaxHeight = screenHeightPixels * 0.8f
        Column(
            Modifier
                .fillMaxWidth()
                .let {
                    if (menuCount * menuItemHeight > dialogMaxHeight) {
                        it
                            .height(dialogMaxHeight.toDp())
                            .background(Color.White, shape = RoundedCornerShape(20.dp))
                            .verticalScroll(rememberScrollState())
                    } else {
                        it
                            .background(Color.White, shape = RoundedCornerShape(20.dp))
                    }
                }
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
//                onDismissRequest()
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
//                onDismissRequest()
            }

            if (my) {
                Divider(Modifier.padding(horizontal = 20.dp))

                SwitchMenu("Animate Scale", animateScale) {
                    state.animateScale.value = !state.animateScale.value
//                    onDismissRequest()
                }
                SwitchMenu("Rubber Band Scale", rubberBandScale) {
                    state.rubberBandScale.value = !state.rubberBandScale.value
//                    onDismissRequest()
                }
                SwitchMenu("Three Step Scale", threeStepScale) {
                    state.threeStepScale.value = !state.threeStepScale.value
//                    onDismissRequest()
                }
                SwitchMenu("One Finger Scale", oneFingerScale) {
                    state.oneFingerScale.value = !state.oneFingerScale.value
//                    onDismissRequest()
                }
                SwitchMenu("Slower Scale Animation", slowerScaleAnimation) {
                    state.slowerScaleAnimation.value = !state.slowerScaleAnimation.value
//                    onDismissRequest()
                }

                val scalesCalculators = remember {
                    listOf("Dynamic", "Fixed")
                }
                MyDropdownMenu("Scales Calculator", scalesCalculator, scalesCalculators) {
                    state.scalesCalculator.value = it
//                    onDismissRequest()
                }

                val scalesMultiples = remember {
                    listOf(
                        2.0f.toString(),
                        2.5f.toString(),
                        3.0f.toString(),
                        3.5f.toString(),
                        4.0f.toString(),
                    )
                }
                MyDropdownMenu("Scales Multiple", scalesMultiple, scalesMultiples) {
                    state.scalesMultiple.value = it
//                    onDismissRequest()
                }
                val gestureTypes = remember {
                    listOf(
                        GestureType.DRAG,
                        GestureType.TWO_FINGER_SCALE,
                        GestureType.ONE_FINGER_SCALE,
                        GestureType.DOUBLE_TAP_SCALE,
                    )
                }
                val gestureTypeStrings = remember {
                    gestureTypes.map { GestureType.name(it) }
                }
                val disabledGestureTypeCheckedList = remember(disabledGestureType) {
                    gestureTypes.map { it and disabledGestureType != 0 }
                }
                MyMultiChooseMenu(
                    name = "Disabled Gesture Type",
                    values = gestureTypeStrings,
                    checkedList = disabledGestureTypeCheckedList,
                ) { which, isChecked ->
                    val newCheckedList = disabledGestureTypeCheckedList.toMutableList().apply { set(which, isChecked) }
                    val newDisabledGestureType =
                        newCheckedList.asSequence().mapIndexedNotNull { index, checked ->
                            if (checked) gestureTypes[index] else null
                        }.fold(0) { acc, disabledGestureType ->
                            acc or disabledGestureType
                        }
                    state.disabledGestureType.value =
                        newDisabledGestureType.toString()
//                    onDismissRequest()
                }

                Divider(Modifier.padding(horizontal = 20.dp))

                SwitchMenu(
                    "Limit Offset Within Base Visible Rect",
                    limitOffsetWithinBaseVisibleRect
                ) {
                    state.limitOffsetWithinBaseVisibleRect.value =
                        !state.limitOffsetWithinBaseVisibleRect.value
//                    onDismissRequest()
                }

                Divider(Modifier.padding(horizontal = 20.dp))

                SwitchMenu("Read Mode", readModeEnabled) {
                    state.readModeEnabled.value = !state.readModeEnabled.value
//                    onDismissRequest()
                }
                SwitchMenu("Read Mode - Both", readModeAcceptedBoth) {
                    state.readModeAcceptedBoth.value = !state.readModeAcceptedBoth.value
//                    onDismissRequest()
                }

                Divider(Modifier.padding(horizontal = 20.dp))

                val continuousTransformTypes = remember {
                    listOf(
                        ContinuousTransformType.SCALE,
                        ContinuousTransformType.OFFSET,
                        ContinuousTransformType.LOCATE,
                        ContinuousTransformType.GESTURE,
                        ContinuousTransformType.FLING,
                    )
                }
                val continuousTransformTypeStrings = remember {
                    continuousTransformTypes.map { ContinuousTransformType.name(it) }
                }
                val pausedContinuousTransformTypeCheckedList = remember(pausedContinuousTransformType) {
                    continuousTransformTypes.map { it and pausedContinuousTransformType != 0 }
                }
                MyMultiChooseMenu(
                    name = "Paused Continuous Transform Type",
                    values = continuousTransformTypeStrings,
                    checkedList = pausedContinuousTransformTypeCheckedList,
                ) { which, isChecked ->
                    val newCheckedList = pausedContinuousTransformTypeCheckedList.toMutableList().apply { set(which, isChecked) }
                    val newContinuousTransformType =
                        newCheckedList.asSequence().mapIndexedNotNull { index, checked ->
                            if (checked) continuousTransformTypes[index] else null
                        }.fold(0) { acc, continuousTransformType ->
                            acc or continuousTransformType
                        }
                    state.pausedContinuousTransformType.value =
                        newContinuousTransformType.toString()
//                    onDismissRequest()
                }
                SwitchMenu("Disabled Background Tiles", disabledBackgroundTiles) {
                    state.disabledBackgroundTiles.value = !state.disabledBackgroundTiles.value
//                    onDismissRequest()
                }
                SwitchMenu("Ignore Exif Orientation", ignoreExifOrientation) {
                    state.ignoreExifOrientation.value = !state.ignoreExifOrientation.value
//                    onDismissRequest()
                }
                SwitchMenu("Show Tile Bounds", showTileBounds) {
                    state.showTileBounds.value = !state.showTileBounds.value
//                    onDismissRequest()
                }
                SwitchMenu("Tile Animation", tileAnimation) {
                    state.tileAnimation.value = !state.tileAnimation.value
//                    onDismissRequest()
                }

                Divider(Modifier.padding(horizontal = 20.dp))

                SwitchMenu("Scroll Bar", scrollBarEnabled) {
                    state.scrollBarEnabled.value = !state.scrollBarEnabled.value
//                    onDismissRequest()
                }

                Divider(Modifier.padding(horizontal = 20.dp))

                val logLevelNames = remember {
                    listOf(
                        Logger.VERBOSE,
                        Logger.DEBUG,
                        Logger.INFO,
                        Logger.WARN,
                        Logger.ERROR,
                        Logger.ASSERT,
                    ).map { Logger.levelName(it) }
                }
                MyDropdownMenu("Log Level", logLevel, logLevelNames) {
                    state.logLevel.value = it
//                    onDismissRequest()
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
            .height(dimensionResource(id = R.dimen.menu_item_height))
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
                .height(dimensionResource(id = R.dimen.menu_item_height))
                .clickable {
                    expanded = !expanded
                }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = name, modifier = Modifier.weight(1f), fontSize = 12.sp)
            Text(text = value, fontSize = 10.sp)
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

@Composable
private fun MyMultiChooseMenu(
    name: String,
    values: List<String>,
    checkedList: List<Boolean>,
    onSelected: (which: Int, isChecked: Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val checkedCount = remember(key1 = checkedList) {
        checkedList.count { it }.toString()
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.menu_item_height))
                .clickable {
                    expanded = !expanded
                }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = checkedCount,
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
            )
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
                        Text(text = value, modifier = Modifier.width(150.dp))
                    },
                    trailingIcon = {
                        Checkbox(checked = checkedList[index], onCheckedChange = {
//                            expanded = !expanded
                            onSelected(index, !checkedList[index])
                        })
                    },
                    onClick = {
//                        expanded = !expanded
                        onSelected(index, !checkedList[index])
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyMultiChooseMenuPreview() {
    val values = remember {
        listOf("A", "B", "C", "D")
    }
    MyMultiChooseMenu(
        name = "Animate Scale",
        values = values,
        checkedList = listOf(true, false, true, false),
    ) { _, _ ->

    }
}