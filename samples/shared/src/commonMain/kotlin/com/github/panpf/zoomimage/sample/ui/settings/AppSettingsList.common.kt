package com.github.panpf.zoomimage.sample.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.ui.components.DividerSettingItem
import com.github.panpf.zoomimage.sample.ui.components.DropdownSettingItem
import com.github.panpf.zoomimage.sample.ui.components.MultiChooseSettingItem
import com.github.panpf.zoomimage.sample.ui.components.SwitchSettingItem
import com.github.panpf.zoomimage.sample.ui.util.name
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import org.koin.compose.koinInject

@Composable
fun AppSettingsList(page: AppSettingsPage) {
    val appSettings: AppSettings = koinInject()
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        if (page == AppSettingsPage.VIEWER) {
            ArrangeSettingsList(appSettings)
            ZoomSettingsList(appSettings)
            ScaleSettingsList(appSettings)
            OffsetSettingsList(appSettings)
            SubsamplingSettingsList(appSettings)
        }

        OtherSettingsList(appSettings, page)
    }
}

@Composable
fun ArrangeSettingsList(appSettings: AppSettings) {
    DividerSettingItem("Arrange")

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

    SwitchSettingItem(
        title = "RTL Layout Direction",
        desc = null,
        state = appSettings.rtlLayoutDirectionEnabled,
    )
}

@Composable
fun ZoomSettingsList(appSettings: AppSettings) {
    DividerSettingItem("Zoom")

    SwitchSettingItem(
        title = "Animate",
        desc = null,
        state = appSettings.zoomAnimateEnabled,
    )

    SwitchSettingItem(
        title = "Slower Animation",
        desc = null,
        state = appSettings.zoomSlowerAnimationEnabled,
    )

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

    SwitchSettingItem(
        title = "Scroll Bar",
        desc = null,
        state = appSettings.scrollBarEnabled,
    )

    SwitchSettingItem(
        title = "Keep Transform",
        desc = "Works only when switching images with the same aspect ratio",
        state = appSettings.keepTransformEnabled,
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
}

@Composable
fun ScaleSettingsList(appSettings: AppSettings) {
    DividerSettingItem("Scale")

    SwitchSettingItem(
        title = "Rubber Band Scale",
        desc = null,
        state = appSettings.rubberBandScaleEnabled,
    )

    SwitchSettingItem(
        title = "Three Step Scale",
        desc = null,
        state = appSettings.threeStepScaleEnabled,
    )

    SwitchSettingItem(
        title = "Reverse Mouse Wheel Scale",
        desc = null,
        state = appSettings.reverseMouseWheelScaleEnabled,
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
        title = "Fixed Scales Calculator Multiple",
        desc = null,
        values = scalesMultipleValues,
        state = appSettings.fixedScalesCalculatorMultiple,
    )
}

@Composable
fun OffsetSettingsList(appSettings: AppSettings) {
    DividerSettingItem("Offset")

    SwitchSettingItem(
        title = "Limit Offset Within Base Visible Rect",
        desc = null,
        state = appSettings.limitOffsetWithinBaseVisibleRect,
    )

    SwitchSettingItem(
        title = "Container Whitespace",
        desc = null,
        state = appSettings.containerWhitespaceEnabled,
    )

    val containerWhitespaceMultiples = remember {
        listOf(0f, 0.5f, 1f, 2f)
    }
    DropdownSettingItem(
        title = "Container Whitespace Multiple",
        values = containerWhitespaceMultiples,
        state = appSettings.containerWhitespaceMultiple
    )
}

@Composable
fun SubsamplingSettingsList(appSettings: AppSettings) {
    DividerSettingItem("Subsampling")

    SwitchSettingItem(
        title = "Subsampling",
        desc = null,
        state = appSettings.subsamplingEnabled,
    )

    SwitchSettingItem(
        title = "Tile Animation",
        desc = null,
        state = appSettings.tileAnimationEnabled,
    )

    SwitchSettingItem(
        title = "Tile Bounds",
        desc = null,
        state = appSettings.tileBoundsEnabled,
    )

    SwitchSettingItem(
        title = "Background Tiles",
        desc = null,
        state = appSettings.backgroundTilesEnabled,
    )

    SwitchSettingItem(
        title = "Tile Memory Cache",
        desc = null,
        state = appSettings.tileMemoryCacheEnabled,
    )

    SwitchSettingItem(
        title = "Auto Stop With Lifecycle",
        desc = null,
        state = appSettings.autoStopWithLifecycleEnabled,
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
}

@Composable
fun OtherSettingsList(appSettings: AppSettings, page: AppSettingsPage) {
    DividerSettingItem("Other")

    val imageLoaderLogLevelValues = remember {
        listOf(
            com.github.panpf.sketch.util.Logger.Level.Verbose,
            com.github.panpf.sketch.util.Logger.Level.Debug,
            com.github.panpf.sketch.util.Logger.Level.Info,
            com.github.panpf.sketch.util.Logger.Level.Warn,
            com.github.panpf.sketch.util.Logger.Level.Error,
            com.github.panpf.sketch.util.Logger.Level.Assert,
        ).map { it.name }
    }
    DropdownSettingItem(
        title = "ImageLoader Log Level",
        desc = null,
        values = imageLoaderLogLevelValues,
        state = appSettings.imageLoaderLogLevelName,
    )

    if (page == AppSettingsPage.VIEWER) {
        SwitchSettingItem(
            title = "Delayed loading of images from local",
            desc = "Only for Sketch ImageLoader",
            state = appSettings.delayImageLoadEnabled,
        )

        val zoomImageLogLevelValues = remember {
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
            title = "Zoom Log Level",
            desc = null,
            values = zoomImageLogLevelValues,
            state = appSettings.zoomImageLogLevelName,
        )
    }

    PlatformOtherSettingsList(appSettings, page)
}

@Composable
expect fun PlatformOtherSettingsList(appSettings: AppSettings, page: AppSettingsPage)