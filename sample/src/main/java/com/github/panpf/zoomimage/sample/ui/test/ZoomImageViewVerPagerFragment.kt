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
package com.github.panpf.zoomimage.sample.ui.test

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.zoomimage.sample.SampleImage
import com.github.panpf.zoomimage.sample.SampleImages
import com.github.panpf.zoomimage.sample.databinding.TabPagerVerFragmentBinding
import com.github.panpf.zoomimage.sample.ui.base.BindingFragment
import com.github.panpf.zoomimage.sample.ui.widget.VerTabLayoutMediator
import com.github.panpf.zoomimage.sample.ui.zoomimage.ZoomImageViewFragment

class ZoomImageViewVerPagerFragment : BindingFragment<TabPagerVerFragmentBinding>() {

    override fun onViewCreated(binding: TabPagerVerFragmentBinding, savedInstanceState: Bundle?) {
        val sampleImages = SampleImages.FETCHERS
        binding.tabPagerVerPager.apply {
            orientation = ViewPager2.ORIENTATION_VERTICAL
            adapter = AssemblyFragmentStateAdapter(
                fragment = this@ZoomImageViewVerPagerFragment,
                itemFactoryList = listOf(ZoomImageViewFragment.ItemFactory()),
                initDataList = sampleImages.map { it.uri }
            )
        }
        VerTabLayoutMediator(
            binding.tabPagerVerTabLayout,
            binding.tabPagerVerPager
        ) { tab, position ->
            tab.text = sampleImages[position].name
        }.attach()
    }
}