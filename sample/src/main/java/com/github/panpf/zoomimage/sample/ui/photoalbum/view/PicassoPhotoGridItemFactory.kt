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

import android.net.Uri
import android.widget.ImageView
import androidx.core.net.toUri
import com.github.panpf.zoomimage.sample.R
import com.squareup.picasso.Picasso

class PicassoPhotoGridItemFactory : BasePhotoGridItemFactory() {

    override fun displayImage(imageView: ImageView, sketchImageUri: String) {
        Picasso.get()
            .let {
                when {
                    sketchImageUri.startsWith("asset://") ->
                        it.load(sketchImageUri.replace("asset://", "file:///android_asset/"))

                    sketchImageUri.startsWith("android.resource://") -> {
                        val resId =
                            sketchImageUri.toUri().getQueryParameters("resId").firstOrNull()
                                ?.toIntOrNull()
                                ?: throw IllegalArgumentException("Can't use Subsampling, invalid resource uri: '$sketchImageUri'")
                        it.load(resId)
                    }

                    else -> it.load(Uri.parse(sketchImageUri))
                }
            }
            .placeholder(R.drawable.im_placeholder)
            .error(R.drawable.im_error)
            .fit()
            .centerInside()
            .into(imageView)
    }
}