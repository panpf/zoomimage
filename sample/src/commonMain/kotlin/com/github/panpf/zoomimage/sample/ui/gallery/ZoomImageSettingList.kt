package com.github.panpf.zoomimage.sample.ui.gallery

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_expand_more
import com.github.panpf.zoomimage.sample.ui.util.name
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun ZoomImageSettingList() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val appSettings = LocalPlatformContext.current.appSettings

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
            state = appSettings.contentScaleName,
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
            state = appSettings.alignmentName,
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
        SwitchSettingItem(
            title = "Reverse Mouse Wheel Scale",
            desc = null,
            state = appSettings.reverseMouseWheelScale,
        )
        DropdownSettingItem(
            title = "Scales Calculator",
            desc = null,
            values = listOf("Dynamic", "Fixed"),
            state = appSettings.scalesCalculatorName,
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

        val gestureTypes = remember { GestureType.values }
        val gestureTypeStrings = remember {
            gestureTypes.map { GestureType.name(it) }
        }
        val disabledGestureTypes by appSettings.disabledGestureTypes.collectAsState()
        val disabledGestureTypeCheckedList = remember(disabledGestureTypes) {
            gestureTypes.map { it and disabledGestureTypes != 0 }
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
                    }.fold(0) { acc, gestureType ->
                        acc or gestureType
                    }
                appSettings.disabledGestureTypes.value = newDisabledGestureType
            }
        )

        DividerSettingItem()

        SwitchSettingItem(
            title = "Limit Offset Within Base Visible Rect",
            desc = null,
            state = appSettings.limitOffsetWithinBaseVisibleRect,
        )

        val containerWhitespaceMultiples = remember {
            listOf(0f, 0.5f, 1f, 2f)
        }
        DropdownSettingItem(
            title = "Container Whitespace Multiple",
            values = containerWhitespaceMultiples,
            state = appSettings.containerWhitespaceMultiple
        )

        SwitchSettingItem(
            title = "Container Whitespace",
            desc = null,
            state = appSettings.containerWhitespace,
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

        SwitchSettingItem(
            title = "Tile Memory Cache",
            desc = null,
            state = appSettings.tileMemoryCache,
        )

        val continuousTransformTypes = remember { ContinuousTransformType.values }
        val continuousTransformTypeStrings = remember {
            continuousTransformTypes.map { ContinuousTransformType.name(it) }
        }
        val pausedContinuousTransformTypes by appSettings.pausedContinuousTransformTypes.collectAsState()
        val pausedContinuousTransformTypeCheckedList =
            remember(pausedContinuousTransformTypes) {
                continuousTransformTypes.map { it and pausedContinuousTransformTypes != 0 }
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
                appSettings.pausedContinuousTransformTypes.value =
                    newContinuousTransformType
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
                Logger.Level.Verbose,
                Logger.Level.Debug,
                Logger.Level.Info,
                Logger.Level.Warn,
                Logger.Level.Error,
                Logger.Level.Assert,
            ).map { it.name }
        }
        DropdownSettingItem(
            title = "Log Level",
            desc = null,
            values = logLevelValues,
            state = appSettings.logLevelName,
        )
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