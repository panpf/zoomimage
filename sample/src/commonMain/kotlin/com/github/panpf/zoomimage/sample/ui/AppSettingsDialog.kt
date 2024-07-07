package com.github.panpf.zoomimage.sample.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.composeImageLoaders
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_expand_more
import com.github.panpf.zoomimage.sample.ui.util.name
import com.github.panpf.zoomimage.sample.ui.util.windowSize
import com.github.panpf.zoomimage.sample.util.RuntimePlatform
import com.github.panpf.zoomimage.sample.util.runtimePlatformInstance
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppSettingsDialog(
    my: Boolean,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties()) {
        Surface(
            Modifier
                .fillMaxWidth()
                .height(getSettingsDialogHeight())
                .clip(RoundedCornerShape(20.dp))
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                val appSettings = LocalPlatformContext.current.appSettings

                // TODO Differentiate sources and display different setting items

                val imageLoaderValues = remember { composeImageLoaders.map { it.name } }
                val imageLoaderName by appSettings.composeImageLoader.collectAsState()
                val imageLoaderDesc by remember {
                    derivedStateOf {
                        composeImageLoaders.find { it.name == imageLoaderName }?.desc
                    }
                }
                DropdownSettingItem(
                    title = "Image Loader",
                    desc = imageLoaderDesc,
                    values = imageLoaderValues,
                    state = appSettings.composeImageLoader,
                )

                DividerSettingItem()

                val contentScaleValues = remember {
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
                DropdownSettingItem(
                    title = "Content Scale",
                    desc = null,
                    values = contentScaleValues,
                    state = appSettings.contentScale,
                )
                val alignmentValues = remember {
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
                DropdownSettingItem(
                    title = "Alignment",
                    desc = null,
                    values = alignmentValues,
                    state = appSettings.alignment,
                )

                DividerSettingItem()

                SwitchSettingItem(
                    title = "Animate Scale",
                    desc = null,
                    state = appSettings.animateScale,
                )
                SwitchSettingItem(
                    title = "Rubber Band Scale",
                    desc = null,
                    state = appSettings.rubberBandScale,
                )
                SwitchSettingItem(
                    title = "Three Step Scale",
                    desc = null,
                    state = appSettings.threeStepScale,
                )
                SwitchSettingItem(
                    title = "Slower Scale Animation",
                    desc = null,
                    state = appSettings.slowerScaleAnimation,
                )
                DropdownSettingItem(
                    title = "Scales Calculator",
                    desc = null,
                    values = listOf("Dynamic", "Fixed"),
                    state = appSettings.scalesCalculator,
                )
                val scalesMultipleValues = remember {
                    listOf(
                        2.0f.toString(),
                        2.5f.toString(),
                        3.0f.toString(),
                        3.5f.toString(),
                        4.0f.toString(),
                    )
                }
                DropdownSettingItem(
                    title = "Scales Multiple",
                    desc = null,
                    values = scalesMultipleValues,
                    state = appSettings.scalesMultiple,
                )

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
                val disabledGestureType by appSettings.disabledGestureType.collectAsState()
                val disabledGestureTypeCheckedList = remember(disabledGestureType) {
                    gestureTypes.map { it and disabledGestureType != 0 }
                }
                MultiChooseSettingItem(
                    title = "Disabled Gesture Type",
                    values = gestureTypeStrings,
                    checkedList = disabledGestureTypeCheckedList,
                    onSelected = { which, isChecked ->
                        val newCheckedList = disabledGestureTypeCheckedList.toMutableList()
                            .apply { set(which, isChecked) }
                        val newDisabledGestureType =
                            newCheckedList.asSequence().mapIndexedNotNull { index, checked ->
                                if (checked) gestureTypes[index] else null
                            }.fold(0) { acc, disabledGestureType ->
                                acc or disabledGestureType
                            }
                        appSettings.disabledGestureType.value = newDisabledGestureType
                    }
                )

                DividerSettingItem()

                SwitchSettingItem(
                    title = "Limit Offset Within Base Visible Rect",
                    desc = null,
                    state = appSettings.limitOffsetWithinBaseVisibleRect,
                )

                DividerSettingItem()

                SwitchSettingItem(
                    title = "Read Mode",
                    desc = null,
                    state = appSettings.readModeEnabled,
                )
                SwitchSettingItem(
                    title = "Read Mode - Both",
                    desc = null,
                    state = appSettings.readModeAcceptedBoth,
                )

                DividerSettingItem()

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
                val pausedContinuousTransformType by appSettings.pausedContinuousTransformType.collectAsState()
                val pausedContinuousTransformTypeCheckedList =
                    remember(pausedContinuousTransformType) {
                        continuousTransformTypes.map { it and pausedContinuousTransformType != 0 }
                    }
                MultiChooseSettingItem(
                    title = "Paused Continuous Transform Type",
                    values = continuousTransformTypeStrings,
                    checkedList = pausedContinuousTransformTypeCheckedList,
                    onSelected = { which, isChecked ->
                        val newCheckedList =
                            pausedContinuousTransformTypeCheckedList.toMutableList()
                                .apply { set(which, isChecked) }
                        val newContinuousTransformType =
                            newCheckedList.asSequence().mapIndexedNotNull { index, checked ->
                                if (checked) continuousTransformTypes[index] else null
                            }.fold(0) { acc, continuousTransformType ->
                                acc or continuousTransformType
                            }
                        appSettings.pausedContinuousTransformType.value = newContinuousTransformType
                    }
                )

                SwitchSettingItem(
                    title = "Disabled Background Tiles",
                    desc = null,
                    state = appSettings.disabledBackgroundTiles,
                )
                SwitchSettingItem(
                    title = "Show Tile Bounds",
                    desc = null,
                    state = appSettings.showTileBounds,
                )
                SwitchSettingItem(
                    title = "Tile Animation",
                    desc = null,
                    state = appSettings.tileAnimation,
                )

                DividerSettingItem()

                SwitchSettingItem(
                    title = "Scroll Bar",
                    desc = null,
                    state = appSettings.scrollBarEnabled,
                )

                DividerSettingItem()

                val logLevelValues = remember {
                    listOf(
                        Logger.VERBOSE,
                        Logger.DEBUG,
                        Logger.INFO,
                        Logger.WARN,
                        Logger.ERROR,
                        Logger.ASSERT,
                    ).map { Logger.levelName(it) }
                }
                DropdownSettingItem(
                    title = "Log Level",
                    desc = null,
                    values = logLevelValues,
                    state = appSettings.logLevel,
                )
            }
        }
    }
}

val menuItemHeight = 50.dp

@Composable
fun DividerSettingItem(
    title: String? = null,
    enabledState: Flow<Boolean>? = null,
) {
    if (enabledState != null) {
        val enabled by enabledState.collectAsState(false)
        if (enabled) return
    }

    Column(Modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    top = 20.dp,
                    bottom = 10.dp,
                    start = 20.dp,
                    end = 20.dp
                )
            )
        }
        HorizontalDivider(
            Modifier.fillMaxWidth()
                .height(0.5.dp)
                .padding(horizontal = 20.dp)
        )
    }
}

@Composable
fun SwitchSettingItem(
    title: String,
    state: MutableStateFlow<Boolean>,
    desc: String? = null,
    onLongClick: (() -> Unit)? = null,
    enabledState: Flow<Boolean>? = null,
) {
    if (enabledState != null) {
        val enabled by enabledState.collectAsState(false)
        if (enabled) return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = menuItemHeight)
            .pointerInput(state) {
                detectTapGestures(
                    onTap = { state.value = !state.value },
                    onLongPress = { onLongClick?.invoke() },
                )
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
            )
            if (desc != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        val checked by state.collectAsState()
        Switch(
            checked = checked,
            onCheckedChange = null,
        )
    }
}

@Composable
fun <T> DropdownSettingItem(
    title: String,
    values: List<T>,
    state: MutableStateFlow<T>,
    desc: String? = null,
    enabledState: Flow<Boolean>? = null,
    onItemClick: (suspend (T) -> Unit)? = null,
) {
    if (enabledState != null) {
        val enabled by enabledState.collectAsState(false)
        if (enabled) return
    }

    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxWidth()) {
        var expanded by remember { mutableStateOf(false) }
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = menuItemHeight)
                .clickable { expanded = true }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 16.sp,
                )
                if (desc != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = desc,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))
            val value by state.collectAsState()
            Text(text = value.toString(), fontSize = 10.sp)
            Icon(
                painter = painterResource(Res.drawable.ic_expand_more),
                contentDescription = "more"
            )
        }

        DropdownMenu(
            expanded = expanded,
            modifier = Modifier.align(Alignment.CenterEnd),
            onDismissRequest = { expanded = false },
        ) {
            values.forEachIndexed { index, value ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                    )
                }
                DropdownMenuItem(
                    text = { Text(text = value.toString()) },
                    onClick = {
                        state.value = value
                        expanded = false
                        coroutineScope.launch {
                            onItemClick?.invoke(value)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MultiChooseSettingItem(
    title: String,
    values: List<String>,
    checkedList: List<Boolean>,
    onSelected: (which: Int, isChecked: Boolean) -> Unit,
    enabledState: Flow<Boolean>? = null,
) {
    if (enabledState != null) {
        val enabled by enabledState.collectAsState(false)
        if (enabled) return
    }

    var expanded by remember { mutableStateOf(false) }
    val checkedCount = remember(key1 = checkedList) {
        checkedList.count { it }.toString()
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(menuItemHeight)
                .clickable {
                    expanded = !expanded
                }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = checkedCount,
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
            )
            Icon(
                painter = painterResource(Res.drawable.ic_expand_more),
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
                    HorizontalDivider(
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

@Composable
fun getSettingsDialogHeight(): Dp {
    return if (runtimePlatformInstance == RuntimePlatform.Js) {
        600.dp
    } else {
        val density = LocalDensity.current
        val windowSize = windowSize()
        remember {
            with(density) {
                (windowSize.height * 0.8f).toInt().toDp()
            }
        }
    }
}