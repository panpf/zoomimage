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
package com.github.panpf.zoomimage.sample.ui.photoalbum.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.databinding.PhotoSlideshowFragmentBinding
import com.github.panpf.zoomimage.sample.settingsService
import com.github.panpf.zoomimage.sample.ui.base.view.ToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.examples.view.ZoomImageViewOptionsDialogFragment
import com.github.panpf.zoomimage.sample.ui.examples.view.ZoomImageViewOptionsDialogFragmentArgs
import com.github.panpf.zoomimage.sample.ui.examples.view.ZoomViewType
import com.github.panpf.zoomimage.sample.util.collectWithLifecycle

class PhotoSlideshowViewFragment : ToolbarBindingFragment<PhotoSlideshowFragmentBinding>() {

    private val args by navArgs<PhotoSlideshowViewFragmentArgs>()
    private val zoomViewType by lazy { ZoomViewType.valueOf(args.zoomViewType) }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(
        toolbar: Toolbar,
        binding: PhotoSlideshowFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        toolbar.title = zoomViewType.title

        toolbar.menu.add("Layout").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                settingsService.horizontalPagerLayout.value =
                    !settingsService.horizontalPagerLayout.value
                true
            }
            settingsService.horizontalPagerLayout.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                val meuIcon = if (it) R.drawable.ic_swap_vert else R.drawable.ic_swap_horiz
                setIcon(meuIcon)
            }
        }

        if (zoomViewType != ZoomViewType.SubsamplingScaleImageView) {
            toolbar.menu.add("Options").apply {
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                setOnMenuItemClickListener {
                    ZoomImageViewOptionsDialogFragment().apply {
                        arguments = ZoomImageViewOptionsDialogFragmentArgs(
                            zoomViewType = args.zoomViewType,
                        ).toBundle()
                    }.show(childFragmentManager, null)
                    true
                }
                setIcon(R.drawable.ic_settings)
            }
        }

        val imageUrlList = args.imageUris.split(",")
        binding.photoSlideshowPager.apply {
            offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            settingsService.horizontalPagerLayout.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                orientation =
                    if (it) ViewPager2.ORIENTATION_HORIZONTAL else ViewPager2.ORIENTATION_VERTICAL
            }
            adapter = AssemblyFragmentStateAdapter(
                fragment = this@PhotoSlideshowViewFragment,
                itemFactoryList = listOf(zoomViewType.createPageItemFactory()),
                initDataList = imageUrlList
            )
            setCurrentItem(args.position - args.startPosition, false)
        }

        binding.photoSlideshowCurrentPage.apply {
            val updateCurrentPageNumber: () -> Unit = {
                val pageNumber = args.startPosition + binding.photoSlideshowPager.currentItem + 1
                text = "$pageNumber\n·\n${args.totalCount}"
            }
            binding.photoSlideshowPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateCurrentPageNumber()
                }
            })
            updateCurrentPageNumber()
        }
    }
}