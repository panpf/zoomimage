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
import com.bumptech.glide.Glide
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.util.sketchUri2GlideModel

class GlidePhotoGridItemFactory : BasePhotoGridItemFactory() {

    override fun displayImage(imageView: ImageView, sketchImageUri: String) {
        Glide.with(imageView.context)
            .load(sketchUri2GlideModel(sketchImageUri))
            .placeholder(R.drawable.im_placeholder)
            .error(R.drawable.im_error)
            .into(imageView)
    }
}