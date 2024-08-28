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
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.panpf.assemblyadapter.recycler.AssemblyRecyclerAdapter
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.databinding.FragmentRecyclerBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingDialogFragment
import com.github.panpf.zoomimage.sample.ui.common.menu.ImageLoaderItemFactory
import com.github.panpf.zoomimage.sample.viewImageLoaders

class SwitchImageLoaderDialogFragment : BaseBindingDialogFragment<FragmentRecyclerBinding>() {

    override fun onViewCreated(binding: FragmentRecyclerBinding, savedInstanceState: Bundle?) {
        val recyclerAdapter = AssemblyRecyclerAdapter(
            itemFactoryList = listOf(ImageLoaderItemFactory().setOnItemClickListener { context, _, _, _, data ->
                context.appSettings.viewImageLoader.value = data.name
                dismiss()
            }),
            initDataList = viewImageLoaders
        )

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerAdapter
        }
    }
}