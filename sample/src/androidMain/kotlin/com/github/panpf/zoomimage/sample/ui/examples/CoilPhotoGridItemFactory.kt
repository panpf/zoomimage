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

package com.github.panpf.zoomimage.sample.ui.examples

import android.content.Context
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.FragmentManager
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.result
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.databinding.GridItemPhotoBinding
import com.github.panpf.zoomimage.sample.image.sketchUri2CoilModel
import com.github.panpf.zoomimage.sample.ui.components.InfoItemsDialogFragment
import com.github.panpf.zoomimage.sample.ui.model.Photo

class CoilPhotoGridItemFactory(val fragmentManager: FragmentManager) : BasePhotoGridItemFactory() {

    override fun initItem(
        context: Context,
        binding: GridItemPhotoBinding,
        item: BindingItem<Photo, GridItemPhotoBinding>
    ) {
        super.initItem(context, binding, item)
        binding.image.setOnLongClickListener {
            val result = binding.image.result
            if (result != null) {
                InfoItemsDialogFragment().apply {
                    val infoItems = buildImageInfos(result)
                    arguments = InfoItemsDialogFragment.buildArgs(infoItems).toBundle()
                }.show(fragmentManager, null)
            }
            true
        }
    }

    override fun loadImage(imageView: ImageView, sketchImageUri: String) {
        imageView.load(sketchUri2CoilModel(imageView.context, sketchImageUri)) {
            placeholder(
                drawable = ResourcesCompat.getColor(
                    /* res = */ imageView.context.resources,
                    /* id = */ R.color.md_theme_primaryContainer,
                    /* theme = */ null
                ).toDrawable()
            )
            error(
                drawable = ResourcesCompat.getColor(
                    /* res = */ imageView.context.resources,
                    /* id = */ R.color.md_theme_errorContainer,
                    /* theme = */ null
                ).toDrawable()
            )
            crossfade(true)
        }
    }
}