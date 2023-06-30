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
                    SwitchMenuItemFactory(compactModel = true),
                    MultiSelectMenuItemFactory(requireActivity(), compactModel = true),
                    ListSeparatorItemFactory(),
                ),
                buildList {
                    add(
                        MultiSelectMenu(
                            title = "Scale Type",
                            desc = null,
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
                            title = "Three Step Scale",
                            data = prefsService.threeStepScaleEnabled,
                            desc = "Double-click zoom in three steps"
                        )
                    )
                    add(
                        SwitchMenuFlow(
                            title = "Read Mode",
                            data = prefsService.readModeEnabled,
                            desc = "Long images are displayed in full screen by default"
                        )
                    )
                    add(
                        SwitchMenuFlow(
                            title = "Scroll Bar",
                            desc = null,
                            data = prefsService.scrollBarEnabled,
                        )
                    )

                    add(
                        SwitchMenuFlow(
                            title = "Show Tile Bounds",
                            desc = "Overlay the state and area of the tile on the View",
                            data = prefsService.showTileBounds,
                        )
                    )
                    add(
                        SwitchMenuFlow(
                            title = "Disable Memory Cache",
                            desc = if (args.supportMemoryCache) null else "Current *ZoomImageView not supported",
                            data = prefsService.disableMemoryCache,
                            disabled = !args.supportMemoryCache,
                        )
                    )
                    add(
                        SwitchMenuFlow(
                            title = "Disallow Reuse Bitmap",
                            desc = if (args.supportReuseBitmap) null else "Current *ZoomImageView not supported",
                            data = prefsService.disallowReuseBitmap,
                            disabled = !args.supportReuseBitmap,
                        )
                    )
                    add(
                        SwitchMenuFlow(
                            title = "Ignore Exif Orientation",
                            desc = if (args.supportReuseBitmap) null else "Current *ZoomImageView not supported",
                            data = prefsService.ignoreExifOrientation,
                            disabled = !args.supportIgnoreExifOrientation,
                        )
                    )
                }
            )
        }
    }
}