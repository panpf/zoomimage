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
import com.github.panpf.zoomimage.sample.ui.common.view.menu.MultiSelectMenu
import com.github.panpf.zoomimage.sample.ui.common.view.menu.MultiSelectMenuItemFactory
import com.github.panpf.zoomimage.sample.ui.common.view.menu.SwitchMenuFlow
import com.github.panpf.zoomimage.sample.ui.common.view.menu.SwitchMenuItemFactory

class ZoomImageViewOptionsDialogFragment : BindingDialogFragment<RecyclerFragmentBinding>() {

    private val args by navArgs<ZoomImageViewOptionsDialogFragmentArgs>()

    override fun onViewCreated(binding: RecyclerFragmentBinding, savedInstanceState: Bundle?) {
        binding.recyclerRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AssemblyRecyclerAdapter(
                listOf(
                    SwitchMenuItemFactory(),
                    MultiSelectMenuItemFactory(requireActivity()),
                    ListSeparatorItemFactory(),
                ),
                buildList {
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

                    add(
                        SwitchMenuFlow(
                            title = "Scroll Bar",
                            data = prefsService.scrollBarEnabled,
                        )
                    )
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
                    add(
                        SwitchMenuFlow(
                            title = "Animate Scale",
                            data = prefsService.animateScale,
                        )
                    )
                    add(
                        SwitchMenuFlow(
                            title = "Three Step Scale",
                            data = prefsService.threeStepScaleEnabled,
                        )
                    )
                    add(
                        SwitchMenuFlow(
                            title = "Slower Scale Animation",
                            data = prefsService.slowerScaleAnimation,
                        )
                    )

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
                            disabled = !args.supportIgnoreExifOrientation,
                        )
                    )
                }
            )
        }
    }
}