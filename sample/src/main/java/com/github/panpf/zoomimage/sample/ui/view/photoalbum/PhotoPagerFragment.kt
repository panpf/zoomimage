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
package com.github.panpf.zoomimage.sample.ui.view.photoalbum

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.zoomimage.sample.databinding.PhotoPagerFragmentBinding
import com.github.panpf.zoomimage.sample.ui.view.ZoomViewType
import com.github.panpf.zoomimage.sample.ui.view.base.ToolbarBindingFragment

class PhotoPagerFragment : ToolbarBindingFragment<PhotoPagerFragmentBinding>() {

    private val args by navArgs<PhotoPagerFragmentArgs>()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(
        toolbar: Toolbar,
        binding: PhotoPagerFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        toolbar.isVisible = false
        toolbar.title = args.zoomViewType
        val imageUrlList = args.imageUris.split(",")

        binding.photoPagerPager.apply {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = AssemblyFragmentStateAdapter(
                fragment = this@PhotoPagerFragment,
                itemFactoryList = listOf(
                    ZoomViewType.valueOf(args.zoomViewType).createItemFactory()
                ),
                initDataList = imageUrlList
            )
            setCurrentItem(args.position - args.startPosition, false)
        }

        binding.photoPagerTotalPage.text = args.totalCount.toString()

        binding.photoPagerCurrentPage.apply {
            val updateCurrentPageNumber: () -> Unit = {
                text = (args.startPosition + binding.photoPagerPager.currentItem + 1).toString()
            }
            binding.photoPagerPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateCurrentPageNumber()
                }
            })
            updateCurrentPageNumber()
        }
    }
}