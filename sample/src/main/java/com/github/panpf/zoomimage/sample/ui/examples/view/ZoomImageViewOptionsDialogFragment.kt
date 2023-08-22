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
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.ui.base.view.BindingDialogFragment
import com.github.panpf.zoomimage.sample.ui.common.view.list.ListSeparatorItemFactory
import com.github.panpf.zoomimage.sample.ui.common.view.menu.MenuDivider
import com.github.panpf.zoomimage.sample.ui.common.view.menu.MenuDividerItemFactory
import com.github.panpf.zoomimage.sample.ui.common.view.menu.MultiSelectMenu
import com.github.panpf.zoomimage.sample.ui.common.view.menu.MultiSelectMenuItemFactory
import com.github.panpf.zoomimage.sample.ui.common.view.menu.SwitchMenuFlow
import com.github.panpf.zoomimage.sample.ui.common.view.menu.SwitchMenuItemFactory
import com.github.panpf.zoomimage.util.AlignmentCompat
import com.github.panpf.zoomimage.util.ContentScaleCompat
import com.github.panpf.zoomimage.util.name

class ZoomImageViewOptionsDialogFragment : BindingDialogFragment<RecyclerFragmentBinding>() {

    private val args by navArgs<ZoomImageViewOptionsDialogFragmentArgs>()
    private val zoomViewType by lazy { ZoomViewType.valueOf(args.zoomViewType) }

    override fun onViewCreated(binding: RecyclerFragmentBinding, savedInstanceState: Bundle?) {
        binding.recyclerRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AssemblyRecyclerAdapter(
                itemFactoryList = listOf(
                    SwitchMenuItemFactory(),
                    MultiSelectMenuItemFactory(requireActivity()),
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
                MultiSelectMenu(
                    title = "Content Scale",
                    values = contentScales.map { it.name },
                    getValue = { prefsService.contentScale.value },
                    onSelect = { _, value ->
                        prefsService.contentScale.value = value
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
                MultiSelectMenu(
                    title = "Alignment",
                    values = alignments.map { it.name },
                    getValue = { prefsService.alignment.value },
                    onSelect = { _, value ->
                        prefsService.alignment.value = value
                    }
                )
            )

            add(MenuDivider())

            add(
                SwitchMenuFlow(
                    title = "Animate Scale",
                    data = prefsService.animateScale,
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Rubber Band Scale",
                    data = prefsService.rubberBandScale,
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Three Step Scale",
                    data = prefsService.threeStepScale,
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Slower Scale Animation",
                    data = prefsService.slowerScaleAnimation,
                )
            )
            val mediumScaleMinMultiples = listOf(
                2.0f.toString(),
                2.5f.toString(),
                3.0f.toString(),
                3.5f.toString(),
                4.0f.toString(),
            )
            add(
                MultiSelectMenu(
                    title = "Medium Scale Min Multiple",
                    values = mediumScaleMinMultiples,
                    getValue = { prefsService.mediumScaleMinMultiple.value },
                    onSelect = { _, value ->
                        prefsService.mediumScaleMinMultiple.value = value
                    }
                )
            )

            add(MenuDivider())

            add(
                SwitchMenuFlow(
                    title = "Read Mode",
                    data = prefsService.readModeEnabled,
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Read Mode Direction Both",
                    data = prefsService.readModeDirectionBoth,
                )
            )

            add(MenuDivider())

            add(
                SwitchMenuFlow(
                    title = "Show Tile Bounds",
                    data = prefsService.showTileBounds,
                )
            )
            add(
                SwitchMenuFlow(
                    title = "Ignore Exif Orientation",
                    data = prefsService.ignoreExifOrientation,
                    disabled = !zoomViewType.supportIgnoreExifOrientation,
                )
            )

            add(MenuDivider())

            add(
                SwitchMenuFlow(
                    title = "Scroll Bar",
                    data = prefsService.scrollBarEnabled,
                )
            )
        } else {
            add(
                MultiSelectMenu(
                    title = "Scale Type",
                    values = ScaleType.values()
                        .filter { it != MATRIX }.map { it.name },
                    getValue = { prefsService.scaleType.value },
                    onSelect = { _, value ->
                        prefsService.scaleType.value = value
                    }
                )
            )
        }
    }
}