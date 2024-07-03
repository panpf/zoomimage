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

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.github.panpf.assemblyadapter.recycler.divider.Divider
import com.github.panpf.assemblyadapter.recycler.divider.newAssemblyGridDividerItemDecoration
import com.github.panpf.assemblyadapter.recycler.newAssemblyGridLayoutManager
import com.github.panpf.assemblyadapter.recycler.paging.AssemblyPagingDataAdapter
import com.github.panpf.tools4k.lang.asOrThrow
import com.github.panpf.zoomimage.sample.NavMainDirections
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.databinding.FragmentRecyclerRefreshBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BaseToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.examples.view.ZoomViewType
import com.github.panpf.zoomimage.sample.ui.photoalbum.Photo2
import com.github.panpf.zoomimage.sample.ui.photoalbum.PhotoAlbumViewModel
import kotlinx.coroutines.launch

class PhotoAlbumViewFragment : BaseToolbarBindingFragment<FragmentRecyclerRefreshBinding>() {

    private val photoAlbumViewModel by viewModels<PhotoAlbumViewModel>()

    //    private val args by navArgs<PhotoAlbumViewFragmentArgs>()
//    private val zoomViewType by lazy { ZoomViewType.valueOf(args.zoomViewType) }
    private val zoomViewType = ZoomViewType.SketchZoomImageView

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: FragmentRecyclerRefreshBinding,
        savedInstanceState: Bundle?
    ) {
        toolbar.apply {
            title = "View"
            subtitle = zoomViewType.title
        }

        val pagingAdapter = AssemblyPagingDataAdapter<Photo2>(
            listOf(
            zoomViewType.createListItemFactory()
                .setOnItemClickListener { _, _, _, absoluteAdapterPosition, _ ->
                    startImageDetail(binding, absoluteAdapterPosition)
                }
        )).apply {
            viewLifecycleOwner.lifecycleScope.launch {
                photoAlbumViewModel.pagingFlow.collect {
                    submitData(it)
                }
            }
        }

        binding.refresh.apply {
            setOnRefreshListener {
                pagingAdapter.refresh()
            }
            viewLifecycleOwner.lifecycleScope.launch {
                pagingAdapter.loadStateFlow.collect { loadStates ->
                    isRefreshing = loadStates.refresh is LoadState.Loading
                }
            }
        }

        binding.recycler.apply {
            layoutManager =
                requireContext().newAssemblyGridLayoutManager(3, GridLayoutManager.VERTICAL)
            val itemDecoration = requireContext().newAssemblyGridDividerItemDecoration {
                val gridDivider = context.resources.getDimensionPixelSize(R.dimen.grid_divider)
                divider(Divider.space(gridDivider))
                sideDivider(Divider.space(gridDivider))
            }
            addItemDecoration(itemDecoration)
            adapter = pagingAdapter
        }
    }

    private fun startImageDetail(binding: FragmentRecyclerRefreshBinding, position: Int) {
        val currentList = binding.recycler
            .adapter!!.asOrThrow<AssemblyPagingDataAdapter<Photo2>>()
            .currentList
        val startPosition = (position - 50).coerceAtLeast(0)
        val totalCount = currentList.size
        val endPosition = (position + 50).coerceAtMost(totalCount - 1)
        val imageList = (startPosition..endPosition).map {
            currentList[it]?.uri
        }
        findNavController().navigate(
            NavMainDirections.actionGlobalPhotoPagerViewFragment(
//                zoomViewType = args.zoomViewType,
                zoomViewType = ZoomViewType.SketchZoomImageView.name,
                imageUris = imageList.joinToString(separator = ","),
                position = position,
                startPosition = startPosition,
                totalCount = totalCount
            ),
        )
    }
}
