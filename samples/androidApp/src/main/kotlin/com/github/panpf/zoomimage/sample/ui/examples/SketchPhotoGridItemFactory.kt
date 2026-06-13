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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.panpf.sketch.imageResult
import com.github.panpf.sketch.loadImage
import com.github.panpf.sketch.resize.LongImagePrecisionDecider
import com.github.panpf.sketch.resize.LongImageScaleDecider
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.state.IconDrawableStateImage
import com.github.panpf.zoomimage.sample.NavMainDirections
import com.github.panpf.zoomimage.sample.databinding.GridItemPhotoBinding
import com.github.panpf.zoomimage.sample.ui.model.Photo
import kotlinx.serialization.json.Json

class SketchPhotoGridItemFactory(val fragment: Fragment) :
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
                val infoItems = buildImageInfos(result)
                val jsonInfoItems = Json.encodeToString(infoItems)
                fragment.findNavController()
                    .navigate(NavMainDirections.actionInfoItemsDialogFragment(jsonInfoItems))
            }
            true
        }
    }

    override fun loadImage(imageView: ImageView, sketchImageUri: String) {
        imageView.loadImage(sketchImageUri) {
            placeholder(
                IconDrawableStateImage(
                    icon = com.github.panpf.zoomimage.sample.compose.R.drawable.ic_image_outline,
                    background = com.github.panpf.zoomimage.sample.compose.R.color.placeholder_bg
                )
            )
            error(
                IconDrawableStateImage(
                    icon = com.github.panpf.zoomimage.sample.compose.R.drawable.ic_error_baseline,
                    background = com.github.panpf.zoomimage.sample.compose.R.color.placeholder_bg
                )
            )
            crossfade()
            resizeOnDraw()
            precision(LongImagePrecisionDecider(Precision.SAME_ASPECT_RATIO))
            scale(LongImageScaleDecider())
        }
    }
}