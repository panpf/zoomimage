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

import android.os.Bundle
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.panpf.assemblyadapter.recycler.AssemblyRecyclerAdapter
import com.github.panpf.tools4a.display.ktx.getDisplayMetrics
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.databinding.FragmentRecyclerBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingDialogFragment
import com.github.panpf.zoomimage.sample.ui.common.list.ListSeparatorItemFactory
import com.github.panpf.zoomimage.sample.ui.common.menu.DropdownMenuItemFactory
import com.github.panpf.zoomimage.sample.ui.common.menu.MenuDividerItemFactory
import com.github.panpf.zoomimage.sample.ui.common.menu.MultiChooseMenuItemFactory
import com.github.panpf.zoomimage.sample.ui.common.menu.SwitchMenuItemFactory
import com.github.panpf.zoomimage.sample.util.repeatCollectWithLifecycle

class ZoomImageSettingsDialogFragment : BaseBindingDialogFragment<FragmentRecyclerBinding>() {

    private val zoomImageSettingsViewModel by viewModels<ZoomImageSettingsViewModel>()

    override fun onViewCreated(binding: FragmentRecyclerBinding, savedInstanceState: Bundle?) {
        val recyclerAdapter = AssemblyRecyclerAdapter(
            itemFactoryList = listOf(
                SwitchMenuItemFactory(),
                DropdownMenuItemFactory(requireActivity()),
                MultiChooseMenuItemFactory(requireActivity()),
                ListSeparatorItemFactory(),
                MenuDividerItemFactory(),
            ),
            initDataList = emptyList<Any>()
        )

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerAdapter
        }

        zoomImageSettingsViewModel.data.repeatCollectWithLifecycle(
            viewLifecycleOwner,
            State.CREATED
        ) { dataList ->
            recyclerAdapter.submitList(dataList)

            val screenHeightPixels = requireContext().getDisplayMetrics().heightPixels
            val menuItemHeight = requireContext().resources.getDimension(R.dimen.menu_item_height)
            val dialogMaxHeight = screenHeightPixels * 0.8f
            if (dataList.size * menuItemHeight > dialogMaxHeight) {
                binding.recycler.updateLayoutParams {
                    height = dialogMaxHeight.toInt()
                }
            }
        }
    }
}