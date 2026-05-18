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
import androidx.fragment.app.FragmentManager
import com.github.panpf.sketch.imageResult
import com.github.panpf.sketch.loadImage
import com.github.panpf.sketch.resize.LongImagePrecisionDecider
import com.github.panpf.sketch.resize.LongImageScaleDecider
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.state.IconDrawableStateImage
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.databinding.GridItemPhotoBinding
import com.github.panpf.zoomimage.sample.ui.components.InfoItemsDialogFragment
import com.github.panpf.zoomimage.sample.ui.model.Photo

class SketchPhotoGridItemFactory(val fragmentManager: FragmentManager) :
    BasePhotoGridItemFactory() {

    override fun initItem(
        context: Context,
        binding: GridItemPhotoBinding,
        item: BindingItem<Photo, GridItemPhotoBinding>
    ) {
        super.initItem(context, binding, item)
        binding.image.setOnLongClickListener {
            val result = binding.image.imageResult
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
        imageView.loadImage(sketchImageUri) {
            placeholder(
                IconDrawableStateImage(
                    icon = R.drawable.ic_image_outline,
                    background = R.color.placeholder_bg
                )
            )
            error(
                IconDrawableStateImage(
                    icon = R.drawable.ic_error_baseline,
                    background = R.color.placeholder_bg
                )
            )
            crossfade()
            resizeOnDraw()
            precision(LongImagePrecisionDecider(Precision.SAME_ASPECT_RATIO))
            scale(LongImageScaleDecider())
        }
    }
}