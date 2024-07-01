/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.databinding.FragmentPhotoPagerBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BaseToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.examples.view.ZoomImageViewOptionsDialogFragment
import com.github.panpf.zoomimage.sample.ui.examples.view.ZoomImageViewOptionsDialogFragmentArgs
import com.github.panpf.zoomimage.sample.ui.examples.view.ZoomViewType
import com.github.panpf.zoomimage.sample.ui.util.collectWithLifecycle

class PhotoPagerViewFragment : BaseToolbarBindingFragment<FragmentPhotoPagerBinding>() {

    private val args by navArgs<PhotoPagerViewFragmentArgs>()
    private val zoomViewType by lazy { ZoomViewType.valueOf(args.zoomViewType) }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(
        toolbar: Toolbar,
        binding: FragmentPhotoPagerBinding,
        savedInstanceState: Bundle?
    ) {
        toolbar.title = zoomViewType.title

        val appSettings = requireContext().appSettings
        toolbar.menu.add("Layout").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                appSettings.horizontalPagerLayout.value =
                    !appSettings.horizontalPagerLayout.value
                true
            }
            appSettings.horizontalPagerLayout.collectWithLifecycle(viewLifecycleOwner) {
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
        binding.pager.apply {
            offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            appSettings.horizontalPagerLayout.collectWithLifecycle(viewLifecycleOwner) {
                orientation =
                    if (it) ViewPager2.ORIENTATION_HORIZONTAL else ViewPager2.ORIENTATION_VERTICAL
            }
            adapter = AssemblyFragmentStateAdapter(
                fragment = this@PhotoPagerViewFragment,
                itemFactoryList = listOf(zoomViewType.createPageItemFactory()),
                initDataList = imageUrlList
            )
            setCurrentItem(args.position - args.startPosition, false)
        }

        binding.pageNumberText.apply {
            val updateCurrentPageNumber: () -> Unit = {
                val pageNumber = args.startPosition + binding.pager.currentItem + 1
                text = "$pageNumber\n·\n${args.totalCount}"
            }
            binding.pager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateCurrentPageNumber()
                }
            })
            updateCurrentPageNumber()
        }
    }
}