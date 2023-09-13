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

package com.github.panpf.zoomimage.sample.ui.examples.view

import android.os.Bundle
import android.widget.ImageView.ScaleType
import android.widget.ImageView.ScaleType.MATRIX
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.panpf.assemblyadapter.recycler.AssemblyRecyclerAdapter
import com.github.panpf.tools4a.display.ktx.getDisplayMetrics
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.databinding.RecyclerFragmentBinding
import com.github.panpf.zoomimage.sample.settingsService
import com.github.panpf.zoomimage.sample.ui.base.view.BindingDialogFragment
import com.github.panpf.zoomimage.sample.ui.common.view.list.ListSeparatorItemFactory
import com.github.panpf.zoomimage.sample.ui.common.view.menu.DropdownMenu
import com.github.panpf.zoomimage.sample.ui.common.view.menu.DropdownMenuItemFactory
import com.github.panpf.zoomimage.sample.ui.common.view.menu.MenuDivider
import com.github.panpf.zoomimage.sample.ui.common.view.menu.MenuDividerItemFactory
import com.github.panpf.zoomimage.sample.ui.common.view.menu.MultiChooseMenu
import com.github.panpf.zoomimage.sample.ui.common.view.menu.MultiChooseMenuItemFactory
import com.github.panpf.zoomimage.sample.ui.common.view.menu.SwitchMenuFlow
import com.github.panpf.zoomimage.sample.ui.common.view.menu.SwitchMenuItemFactory
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.name

class ZoomImageViewOptionsDialogFragment : BindingDialogFragment<RecyclerFragmentBinding>() {

    private val args by navArgs<ZoomImageViewOptionsDialogFragmentArgs>()
    private val zoomViewType by lazy { ZoomViewType.valueOf(args.zoomViewType) }

    override fun onViewCreated(binding: RecyclerFragmentBinding, savedInstanceState: Bundle?) {
        val dataList = buildList()
        binding.recyclerRecycler.apply {
            val screenHeightPixels = context.getDisplayMetrics().heightPixels
            val menuItemHeight = context.resources.getDimension(R.dimen.menu_item_height)
            val dialogMaxHeight = screenHeightPixels * 0.8f
            if (dataList.size * menuItemHeight > dialogMaxHeight) {
                updateLayoutParams {
                    height = dialogMaxHeight.toInt()
                }
            }

            layoutManager = LinearLayoutManager(context)
            adapter = AssemblyRecyclerAdapter(
                itemFactoryList = listOf(
                    SwitchMenuItemFactory(),
                    DropdownMenuItemFactory(requireActivity()),
                    MultiChooseMenuItemFactory(requireActivity()),
                    ListSeparatorItemFactory(),
                    MenuDividerItemFactory(),
                ),
                initDataList = dataList
            )
        }
    }

    private fun buildList(): List<Any> = buildList {
        if (zoomViewType.my) {
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
                    values = contentScales.map { it.name },
                    getValue = { settingsService.contentScale.value },
                    onSelected = { _, value ->
                        settingsService.contentScale.value = value
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
                    values = alignments.map { it.name },
                    getValue = { settingsService.alignment.value },
                    onSelected = { _, value ->
                        settingsService.alignment.value = value
                    }
                )
            )

            add(MenuDivider())

            add(
                SwitchMenuFlow(
                    title = "Animate Scale",
                    data = settingsService.animateScale,
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Rubber Band Scale",
                    data = settingsService.rubberBandScale,
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Three Step Scale",
                    data = settingsService.threeStepScale,
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Slower Scale Animation",
                    data = settingsService.slowerScaleAnimation,
                )
            )
            val scalesCalculators = listOf("Dynamic", "Fixed")
            add(
                DropdownMenu(
                    title = "Scales Calculator",
                    values = scalesCalculators,
                    getValue = { settingsService.scalesCalculator.value },
                    onSelected = { _, value ->
                        settingsService.scalesCalculator.value = value
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
                    values = scalesMultiples,
                    getValue = { settingsService.scalesMultiple.value },
                    onSelected = { _, value ->
                        settingsService.scalesMultiple.value = value
                    }
                )
            )

            add(MenuDivider())

            add(
                SwitchMenuFlow(
                    title = "Limit Offset Within Base Visible Rect",
                    data = settingsService.limitOffsetWithinBaseVisibleRect,
                )
            )

            add(MenuDivider())

            add(
                SwitchMenuFlow(
                    title = "Read Mode",
                    data = settingsService.readModeEnabled,
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Read Mode Accepted Both",
                    data = settingsService.readModeAcceptedBoth,
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
                    getCheckeds = { continuousTransformTypes.map { it and settingsService.pausedContinuousTransformType.value.toInt() != 0 } },
                    getCheckedNames = {
                        continuousTransformTypes.filter { it and settingsService.pausedContinuousTransformType.value.toInt() != 0 }
                            .joinToString(separator = ",") { ContinuousTransformType.name(it) }
                    },
                    onSelected = { which, isChecked ->
                        val checkeds =
                            continuousTransformTypes.map { it and settingsService.pausedContinuousTransformType.value.toInt() != 0 }
                        val newCheckeds = checkeds.toMutableList().apply { set(which, isChecked) }
                        val newContinuousTransformType =
                            newCheckeds.asSequence().mapIndexedNotNull { index, checked ->
                                if (checked) continuousTransformTypes[index] else null
                            }.fold(0) { acc, continuousTransformType ->
                                acc or continuousTransformType
                            }
                        settingsService.pausedContinuousTransformType.value =
                            newContinuousTransformType.toString()
                    }
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Disabled Background Tiles",
                    data = settingsService.disabledBackgroundTiles,
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Ignore Exif Orientation",
                    data = settingsService.ignoreExifOrientation,
                    disabled = !zoomViewType.supportIgnoreExifOrientation,
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Show Tile Bounds",
                    data = settingsService.showTileBounds,
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Tile Animation",
                    data = settingsService.tileAnimation,
                )
            )

            add(MenuDivider())

            add(
                SwitchMenuFlow(
                    title = "Scroll Bar",
                    data = settingsService.scrollBarEnabled,
                )
            )
        } else {
            add(
                DropdownMenu(
                    title = "Scale Type",
                    values = ScaleType.values()
                        .filter { it != MATRIX }.map { it.name },
                    getValue = { settingsService.scaleType.value },
                    onSelected = { _, value ->
                        settingsService.scaleType.value = value
                    }
                )
            )
        }
    }
}