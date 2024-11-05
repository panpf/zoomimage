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
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.PicassoZoomImageView
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.image.PicassoResourceSubsamplingImageGenerator
import com.github.panpf.zoomimage.sample.image.sketchUri2PicassoData
import com.github.panpf.zoomimage.sample.ui.components.StateView
import com.github.panpf.zoomimage.sample.ui.components.ZoomImageMinimapView
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator

class PicassoZoomImageViewFragment : BaseZoomImageViewFragment<PicassoZoomImageView>() {

    private val args by navArgs<PicassoZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override fun createZoomImageView(context: Context): PicassoZoomImageView {
        return PicassoZoomImageView(context).apply {
            setSubsamplingImageGenerators(PicassoResourceSubsamplingImageGenerator())
        }
    }

    override fun loadImage(zoomView: PicassoZoomImageView, stateView: StateView) {
        stateView.loading()
        val callback = object : Callback {
            override fun onSuccess() {
                stateView.gone()
            }

            override fun onError(e: Exception?) {
                stateView.error {
                    message(e)
                    retryAction {
                        loadData()
                    }
                }
            }
        }
        val config: RequestCreator.() -> Unit = {
            fit()
            centerInside()
        }
        val picassoImageUri = sketchUri2PicassoData(zoomView.context,  args.imageUri)
        zoomView.loadImage(
            uri = Uri.parse(picassoImageUri),
            callback = callback,
            config = config
        )
    }

    override fun loadMinimap(minimapView: ZoomImageMinimapView, sketchImageUri: String) {
        val picassoImageUri = sketchUri2PicassoData(minimapView.context, sketchImageUri)
        Picasso.get()
            .load(Uri.parse(picassoImageUri))
            .placeholder(R.drawable.im_placeholder)
            .error(R.drawable.im_error)
            .resize(600, 600)
            .centerInside()
            .into(minimapView)
    }

    class ItemFactory : FragmentItemFactory<Photo>(Photo::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: Photo
        ): Fragment = PicassoZoomImageViewFragment().apply {
            arguments = PicassoZoomImageViewFragmentArgs(data.originalUrl).toBundle()
        }
    }
}