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

import android.widget.ImageView
import coil3.load
import coil3.request.error
import coil3.request.placeholder
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.photoalbum.compose.iconDrawable
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel

class CoilPhotoGridItemFactory : BasePhotoGridItemFactory() {

    override fun loadImage(imageView: ImageView, sketchImageUri: String) {
        imageView.load(sketchUri2CoilModel(imageView.context, sketchImageUri)) {
            placeholder(
                iconDrawable(
                    imageView.context,
                    R.drawable.ic_image_outline,
                    R.color.placeholder_bg
                )
            )
            error(
                iconDrawable(
                    imageView.context,
                    R.drawable.ic_error_baseline,
                    R.color.placeholder_bg
                )
            )
//            crossfade(true) // TODO There is a bug
        }
    }
}