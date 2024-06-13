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
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.github.panpf.sketch.drawable.internal.IconDrawable
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel

class CoilPhotoGridItemFactory : BasePhotoGridItemFactory() {

    override fun displayImage(imageView: ImageView, sketchImageUri: String) {
        imageView.load(sketchUri2CoilModel(imageView.context, sketchImageUri)) {
            placeholder(
                iconDrawable(
                    imageView.context,
                    R.drawable.ic_image_outline,
                    R.color.placeholder_bg
                )
            )
            this.error(R.drawable.im_error)
            crossfade(true)
        }
    }
}

fun iconDrawable(context: Context, @DrawableRes icon: Int, bg: Int): Drawable {
    return IconDrawable(
        ResourcesCompat.getDrawable(context.resources, icon, null)!!,
        ColorDrawable(ResourcesCompat.getColor(context.resources, bg, null)),
    )
}