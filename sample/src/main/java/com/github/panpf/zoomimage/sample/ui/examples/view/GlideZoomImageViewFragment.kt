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

package com.github.panpf.zoomimage.sample.ui.examples.view

import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.databinding.GlideZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewCommonFragmentBinding
import com.github.panpf.zoomimage.sample.ui.widget.view.ZoomImageMinimapView
import com.github.panpf.zoomimage.sample.util.sketchUri2GlideModel

class GlideZoomImageViewFragment : BaseZoomImageViewFragment<GlideZoomImageViewFragmentBinding>() {

    private val args by navArgs<CoilZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override fun getCommonBinding(binding: GlideZoomImageViewFragmentBinding): ZoomImageViewCommonFragmentBinding {
        return binding.common
    }

    override fun getZoomImageView(binding: GlideZoomImageViewFragmentBinding): ZoomImageView {
        return binding.glideZoomImageViewImage
    }

    override fun loadImage(
        binding: GlideZoomImageViewFragmentBinding,
        onCallStart: () -> Unit,
        onCallSuccess: () -> Unit,
        onCallError: () -> Unit
    ) {
        onCallStart()
        Glide.with(this@GlideZoomImageViewFragment)
            .load(sketchUri2GlideModel(args.imageUri))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    onCallError()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    onCallSuccess()
                    return false
                }
            })
            .into(binding.glideZoomImageViewImage)
    }

    override fun loadMinimap(zoomImageMinimapView: ZoomImageMinimapView, sketchImageUri: String) {
        Glide.with(zoomImageMinimapView.context)
            .load(sketchUri2GlideModel(sketchImageUri))
            .placeholder(R.drawable.im_placeholder)
            .error(R.drawable.im_error)
            .override(600, 600)
            .into(zoomImageMinimapView)
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = GlideZoomImageViewFragment().apply {
            arguments = GlideZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}