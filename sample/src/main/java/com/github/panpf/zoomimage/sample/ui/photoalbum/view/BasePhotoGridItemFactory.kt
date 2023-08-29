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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.panpf.tools4a.display.ktx.getScreenWidth
import com.github.panpf.tools4k.lang.asOrNull
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.databinding.PhotoItemBinding
import com.github.panpf.zoomimage.sample.ui.base.view.MyBindingItemFactory
import com.github.panpf.zoomimage.sample.ui.photoalbum.Photo

abstract class BasePhotoGridItemFactory :
    MyBindingItemFactory<Photo, PhotoItemBinding>(Photo::class) {

    private var itemSize: Int? = null

    final override fun createItemViewBinding(
        context: Context,
        inflater: LayoutInflater,
        parent: ViewGroup
    ): PhotoItemBinding {
        if (itemSize == null && parent is RecyclerView) {
            val screenWidth = context.getScreenWidth()
            val gridDivider = context.resources.getDimensionPixelSize(R.dimen.grid_divider)
            val spanCount = parent.layoutManager?.asOrNull<GridLayoutManager>()?.spanCount ?: 1
            itemSize = (screenWidth - (gridDivider * (spanCount - 1))) / spanCount
        }
        return super.createItemViewBinding(context, inflater, parent)
    }

    final override fun initItem(
        context: Context,
        binding: PhotoItemBinding,
        item: BindingItem<Photo, PhotoItemBinding>
    ) {

    }

    final override fun bindItemData(
        context: Context,
        binding: PhotoItemBinding,
        item: BindingItem<Photo, PhotoItemBinding>,
        bindingAdapterPosition: Int,
        absoluteAdapterPosition: Int,
        data: Photo
    ) {
        binding.photoItemImage.apply {
            updateLayoutParams<LayoutParams> {
                val itemSize = itemSize!!
                width = itemSize
                height = itemSize
            }
            displayImage(binding.photoItemImage, data.uri)
        }
    }

    abstract fun displayImage(imageView: ImageView, sketchImageUri: String)
}