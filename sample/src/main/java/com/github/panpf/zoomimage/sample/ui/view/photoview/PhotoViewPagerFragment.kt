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
package com.github.panpf.zoomimage.sample.ui.view.photoview

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.zoomimage.sample.SampleImage
import com.github.panpf.zoomimage.sample.SampleImages
import com.github.panpf.zoomimage.sample.databinding.TabPagerFragmentBinding
import com.github.panpf.zoomimage.sample.ui.view.base.ToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.view.photoview.PhotoViewFragment.ItemFactory
import com.google.android.material.tabs.TabLayoutMediator

class PhotoViewPagerFragment : ToolbarBindingFragment<TabPagerFragmentBinding>() {

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: TabPagerFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        toolbar.title = "PhotoView"

        val sampleImages = SampleImages.FETCHERS
        binding.tabPagerPager.adapter = AssemblyFragmentStateAdapter(
            this,
            listOf(ItemFactory()),
            sampleImages.map { it.uri }
        )
        TabLayoutMediator(binding.tabPagerTabLayout, binding.tabPagerPager) { tab, position ->
            tab.text = sampleImages[position].name
        }.attach()
    }
}