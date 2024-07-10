/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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
import com.github.panpf.zoomimage.sample.appSettings
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

class ZoomImageSettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _data = MutableStateFlow<List<Any>>(emptyList())
    val data: StateFlow<List<Any>> = _data
    private val appSettings = application.appSettings

    init {
        viewModelScope.launch {
            appSettings.viewImageLoader.collect {
                _data.value = buildData()
            }
        }
    }

    private fun buildData(): List<Any> = buildList {
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
                getValue = { appSettings.contentScale.value },
                onSelected = { _, value ->
                    appSettings.contentScale.value = value
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
                getValue = { appSettings.alignment.value },
                onSelected = { _, value ->
                    appSettings.alignment.value = value
                }
            )
        )

        add(MenuDivider())

        add(
            SwitchMenuFlow(
                title = "Animate Scale",
                data = appSettings.animateScale,
            )
        )
        add(
            SwitchMenuFlow(
                title = "Rubber Band Scale",
                data = appSettings.rubberBandScale,
            )
        )
        add(
            SwitchMenuFlow(
                title = "Three Step Scale",
                data = appSettings.threeStepScale,
            )
        )
        add(
            SwitchMenuFlow(
                title = "Slower Scale Animation",
                data = appSettings.slowerScaleAnimation,
            )
        )
        val scalesCalculators = listOf("Dynamic", "Fixed")
        add(
            DropdownMenu(
                title = "Scales Calculator",
                desc = null,
                values = scalesCalculators,
                getValue = { appSettings.scalesCalculator.value },
                onSelected = { _, value ->
                    appSettings.scalesCalculator.value = value
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

        val gestureTypes = listOf(
            GestureType.DRAG,
            GestureType.TWO_FINGER_SCALE,
            GestureType.ONE_FINGER_SCALE,
            GestureType.DOUBLE_TAP_SCALE,
        )
        add(
            MultiChooseMenu(
                title = "Disabled Gesture Type",
                values = gestureTypes.map { GestureType.name(it) },
                getCheckedList = { gestureTypes.map { it and appSettings.disabledGestureType.value != 0 } },
                onSelected = { which, isChecked ->
                    val checkedList =
                        gestureTypes.map { it and appSettings.disabledGestureType.value != 0 }
                    val newCheckedList =
                        checkedList.toMutableList().apply { set(which, isChecked) }
                    val newDisabledGestureTypeType =
                        newCheckedList.asSequence().mapIndexedNotNull { index, checked ->
                            if (checked) gestureTypes[index] else null
                        }.fold(0) { acc, gestureType ->
                            acc or gestureType
                        }
                    appSettings.disabledGestureType.value = newDisabledGestureTypeType
                }
            )
        )

        add(MenuDivider())

        add(
            SwitchMenuFlow(
                title = "Limit Offset Within Base Visible Rect",
                data = appSettings.limitOffsetWithinBaseVisibleRect,
            )
        )

        add(MenuDivider())

        add(
            SwitchMenuFlow(
                title = "Read Mode",
                data = appSettings.readModeEnabled,
            )
        )
        add(
            SwitchMenuFlow(
                title = "Read Mode - Both",
                data = appSettings.readModeAcceptedBoth,
            )
        )

        add(MenuDivider())

        val continuousTransformTypes = listOf(
            ContinuousTransformType.SCALE,
            ContinuousTransformType.OFFSET,
            ContinuousTransformType.LOCATE,
            ContinuousTransformType.GESTURE,
            ContinuousTransformType.FLING,
        )
        add(
            MultiChooseMenu(
                title = "Paused Continuous Transform Type",
                values = continuousTransformTypes.map { ContinuousTransformType.name(it) },
                getCheckedList = { continuousTransformTypes.map { it and appSettings.pausedContinuousTransformType.value != 0 } },
                onSelected = { which, isChecked ->
                    val checkedList =
                        continuousTransformTypes.map { it and appSettings.pausedContinuousTransformType.value != 0 }
                    val newCheckedList =
                        checkedList.toMutableList().apply { set(which, isChecked) }
                    val newContinuousTransformType =
                        newCheckedList.asSequence().mapIndexedNotNull { index, checked ->
                            if (checked) continuousTransformTypes[index] else null
                        }.fold(0) { acc, continuousTransformType ->
                            acc or continuousTransformType
                        }
                    appSettings.pausedContinuousTransformType.value = newContinuousTransformType
                }
            )
        )
        add(
            SwitchMenuFlow(
                title = "Disabled Background Tiles",
                data = appSettings.disabledBackgroundTiles,
            )
        )
        add(
            SwitchMenuFlow(
                title = "Show Tile Bounds",
                data = appSettings.showTileBounds,
            )
        )
        add(
            SwitchMenuFlow(
                title = "Tile Animation",
                data = appSettings.tileAnimation,
            )
        )

        add(MenuDivider())

        add(
            SwitchMenuFlow(
                title = "Scroll Bar",
                data = appSettings.scrollBarEnabled,
            )
        )

        add(MenuDivider())

        add(
            DropdownMenu(
                title = "Log Level",
                desc = null,
                values = listOf(
                    Logger.VERBOSE,
                    Logger.DEBUG,
                    Logger.INFO,
                    Logger.WARN,
                    Logger.ERROR,
                    Logger.ASSERT,
                ).map { Logger.levelName(it) },
                getValue = { appSettings.logLevel.value },
                onSelected = { _, value ->
                    appSettings.logLevel.value = value
                }
            )
        )
    }
}