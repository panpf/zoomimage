/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.panpf.assemblyadapter.recycler.AssemblyRecyclerAdapter
import com.github.panpf.zoomimage.sample.databinding.RecyclerFragmentBinding
import com.github.panpf.zoomimage.sample.settingsService
import com.github.panpf.zoomimage.sample.ui.base.view.BindingDialogFragment
import com.github.panpf.zoomimage.sample.ui.common.view.list.ListSeparatorItemFactory
import com.github.panpf.zoomimage.sample.ui.common.view.menu.DropdownMenu
import com.github.panpf.zoomimage.sample.ui.common.view.menu.DropdownMenuItemFactory
import com.github.panpf.zoomimage.sample.ui.common.view.menu.MenuDivider
import com.github.panpf.zoomimage.sample.ui.common.view.menu.MenuDividerItemFactory
import com.github.panpf.zoomimage.sample.ui.common.view.menu.SwitchMenuFlow
import com.github.panpf.zoomimage.sample.ui.common.view.menu.SwitchMenuItemFactory
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.name

class ZoomImageViewOptionsDialogFragment : BindingDialogFragment<RecyclerFragmentBinding>() {

    private val args by navArgs<ZoomImageViewOptionsDialogFragmentArgs>()
    private val zoomViewType by lazy { ZoomViewType.valueOf(args.zoomViewType) }

    override fun onViewCreated(binding: RecyclerFragmentBinding, savedInstanceState: Bundle?) {
        binding.recyclerRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AssemblyRecyclerAdapter(
                itemFactoryList = listOf(
                    SwitchMenuItemFactory(),
                    DropdownMenuItemFactory(requireActivity()),
                    ListSeparatorItemFactory(),
                    MenuDividerItemFactory(),
                ),
                initDataList = buildList()
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
            val stepScaleMinMultiples = listOf(
                2.0f.toString(),
                2.5f.toString(),
                3.0f.toString(),
                3.5f.toString(),
                4.0f.toString(),
            )
            add(
                DropdownMenu(
                    title = "Step Scale Min Multiple",
                    values = stepScaleMinMultiples,
                    getValue = { settingsService.stepScaleMinMultiple.value },
                    onSelected = { _, value ->
                        settingsService.stepScaleMinMultiple.value = value
                    }
                )
            )
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
                    title = "Read Mode Direction Both",
                    data = settingsService.readModeDirectionBoth,
                )
            )

            add(MenuDivider())

            add(
                SwitchMenuFlow(
                    title = "Show Tile Bounds",
                    data = settingsService.showTileBounds,
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Ignore Exif Orientation",
                    data = settingsService.ignoreExifOrientation,
                    disabled = !zoomViewType.supportIgnoreExifOrientation,
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