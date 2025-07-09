/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.sample.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.ui.common.menu.DropdownMenu
import com.github.panpf.zoomimage.sample.ui.common.menu.MenuDivider
import com.github.panpf.zoomimage.sample.ui.common.menu.MultiChooseMenu
import com.github.panpf.zoomimage.sample.ui.common.menu.SwitchMenuFlow
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import com.github.panpf.zoomimage.zoom.name
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

class ZoomImageSettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _data = MutableStateFlow<List<Any>>(emptyList())
    val data: StateFlow<List<Any>> = _data
    val appSettings: AppSettings = KoinPlatform.getKoin().get()

    init {
        viewModelScope.launch {
            appSettings.viewImageLoader.collect {
                _data.value = buildData()
            }
        }
    }

    private fun buildData(): List<Any> = buildList {
        add(MenuDivider("Arrange"))

        val contentScales = listOf(
            ContentScaleCompat.Fit,
            ContentScaleCompat.Crop,
            ContentScaleCompat.Inside,
            ContentScaleCompat.FillWidth,
            ContentScaleCompat.FillHeight,
            ContentScaleCompat.FillBounds,
            ContentScaleCompat.None,
        )
        add(
            DropdownMenu(
                title = "Content Scale",
                desc = null,
                values = contentScales.map { it.name },
                getValue = { appSettings.contentScaleName.value },
                onSelected = { _, value ->
                    appSettings.contentScaleName.value = value
                }
            )
        )

        val alignments = listOf(
            AlignmentCompat.TopStart,
            AlignmentCompat.TopCenter,
            AlignmentCompat.TopEnd,
            AlignmentCompat.CenterStart,
            AlignmentCompat.Center,
            AlignmentCompat.CenterEnd,
            AlignmentCompat.BottomStart,
            AlignmentCompat.BottomCenter,
            AlignmentCompat.BottomEnd,
        )
        add(
            DropdownMenu(
                title = "Alignment",
                desc = null,
                values = alignments.map { it.name },
                getValue = { appSettings.alignmentName.value },
                onSelected = { _, value ->
                    appSettings.alignmentName.value = value
                }
            )
        )
        add(
            SwitchMenuFlow(
                title = "RTL Layout Direction",
                desc = null,
                data = appSettings.rtlLayoutDirectionEnabled,
            )
        )

        add(MenuDivider("Gesture"))

        add(
            SwitchMenuFlow(
                title = "Animate Scale",
                desc = null,
                data = appSettings.animateScale,
            )
        )
        add(
            SwitchMenuFlow(
                title = "Rubber Band Scale",
                desc = null,
                data = appSettings.rubberBandScale,
            )
        )
        add(
            SwitchMenuFlow(
                title = "Three Step Scale",
                desc = null,
                data = appSettings.threeStepScale,
            )
        )
        add(
            SwitchMenuFlow(
                title = "Slower Scale Animation",
                desc = null,
                data = appSettings.slowerScaleAnimation,
            )
        )
        val scalesCalculators = listOf("Dynamic", "Fixed")
        add(
            DropdownMenu(
                title = "Scales Calculator",
                desc = null,
                values = scalesCalculators,
                getValue = { appSettings.scalesCalculatorName.value },
                onSelected = { _, value ->
                    appSettings.scalesCalculatorName.value = value
                }
            )
        )
        val scalesMultiples = listOf(
            2.0f.toString(),
            2.5f.toString(),
            3.0f.toString(),
            3.5f.toString(),
            4.0f.toString(),
        )
        add(
            DropdownMenu(
                title = "Scales Multiple",
                desc = null,
                values = scalesMultiples,
                getValue = { appSettings.scalesMultiple.value },
                onSelected = { _, value ->
                    appSettings.scalesMultiple.value = value
                }
            )
        )

        val gestureTypes = GestureType.values
        add(
            MultiChooseMenu(
                title = "Disabled Gesture Type",
                values = gestureTypes.map { GestureType.name(it) },
                getCheckedList = { gestureTypes.map { it and appSettings.disabledGestureTypes.value != 0 } },
                onSelected = { which, isChecked ->
                    val checkedList =
                        gestureTypes.map { it and appSettings.disabledGestureTypes.value != 0 }
                    val newCheckedList =
                        checkedList.toMutableList().apply { set(which, isChecked) }
                    val newDisabledGestureTypeType =
                        newCheckedList.asSequence().mapIndexedNotNull { index, checked ->
                            if (checked) gestureTypes[index] else null
                        }.fold(0) { acc, gestureType ->
                            acc or gestureType
                        }
                    appSettings.disabledGestureTypes.value = newDisabledGestureTypeType
                }
            )
        )

        add(MenuDivider("Offset Bounds"))

        add(
            SwitchMenuFlow(
                title = "Limit Offset Within Base Visible Rect",
                desc = null,
                data = appSettings.limitOffsetWithinBaseVisibleRect,
            )
        )

        add(
            DropdownMenu(
                title = "Container Whitespace Multiple",
                desc = null,
                values = listOf(0f, 0.5f, 1f, 2f).map { it.toString() },
                getValue = { appSettings.containerWhitespaceMultiple.value.toString() },
                onSelected = { _, value ->
                    appSettings.containerWhitespaceMultiple.value = value.toFloat()
                }
            )
        )

        add(
            SwitchMenuFlow(
                title = "Container Whitespace",
                desc = null,
                data = appSettings.containerWhitespace,
            )
        )

        add(MenuDivider("Read Mode"))

        add(
            SwitchMenuFlow(
                title = "Read Mode",
                desc = null,
                data = appSettings.readModeEnabled,
            )
        )
        add(
            SwitchMenuFlow(
                title = "Read Mode - Both",
                desc = null,
                data = appSettings.readModeAcceptedBoth,
            )
        )

        add(MenuDivider("Subsampling"))

        add(
            SwitchMenuFlow(
                title = "Subsampling",
                desc = null,
                data = appSettings.subsamplingEnabled,
            )
        )

        add(
            SwitchMenuFlow(
                title = "Auto Stop With Lifecycle",
                desc = null,
                data = appSettings.autoStopWithLifecycleEnabled,
            )
        )

        add(
            SwitchMenuFlow(
                title = "Tile Memory Cache",
                desc = null,
                data = appSettings.tileMemoryCache,
            )
        )
        val continuousTransformTypes = ContinuousTransformType.values
        add(
            MultiChooseMenu(
                title = "Paused Continuous Transform Type",
                values = continuousTransformTypes.map { ContinuousTransformType.name(it) },
                getCheckedList = { continuousTransformTypes.map { it and appSettings.pausedContinuousTransformTypes.value != 0 } },
                onSelected = { which, isChecked ->
                    val checkedList =
                        continuousTransformTypes.map { it and appSettings.pausedContinuousTransformTypes.value != 0 }
                    val newCheckedList =
                        checkedList.toMutableList().apply { set(which, isChecked) }
                    val newContinuousTransformType =
                        newCheckedList.asSequence().mapIndexedNotNull { index, checked ->
                            if (checked) continuousTransformTypes[index] else null
                        }.fold(0) { acc, continuousTransformType ->
                            acc or continuousTransformType
                        }
                    appSettings.pausedContinuousTransformTypes.value = newContinuousTransformType
                }
            )
        )
        add(
            SwitchMenuFlow(
                title = "Disabled Background Tiles",
                desc = null,
                data = appSettings.disabledBackgroundTiles,
            )
        )
        add(
            SwitchMenuFlow(
                title = "Show Tile Bounds",
                desc = null,
                data = appSettings.showTileBounds,
            )
        )
        add(
            SwitchMenuFlow(
                title = "Tile Animation",
                desc = null,
                data = appSettings.tileAnimation,
            )
        )

        add(MenuDivider("Other"))

        add(
            SwitchMenuFlow(
                title = "Scroll Bar",
                desc = null,
                data = appSettings.scrollBarEnabled,
            )
        )

        add(
            SwitchMenuFlow(
                title = "Keep Transform",
                desc = "Works only when switching images with the same aspect ratio",
                data = appSettings.keepTransformWhenSameAspectRatioContentSizeChangedEnabled,
            )
        )

        add(
            SwitchMenuFlow(
                title = "Delayed loading of images from local",
                desc = "Only for Sketch ImageLoader",
                data = appSettings.delayImageLoadEnabled,
            )
        )

        add(
            DropdownMenu(
                title = "Log Level",
                desc = null,
                values = listOf(
                    Logger.Level.Verbose,
                    Logger.Level.Debug,
                    Logger.Level.Info,
                    Logger.Level.Warn,
                    Logger.Level.Error,
                    Logger.Level.Assert,
                ).map { it.name },
                getValue = { appSettings.logLevelName.value },
                onSelected = { _, value ->
                    appSettings.logLevelName.value = value
                }
            )
        )
    }
}