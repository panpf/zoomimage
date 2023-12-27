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

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.PicassoZoomImageView
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.widget.view.StateView
import com.github.panpf.zoomimage.sample.ui.widget.view.ZoomImageMinimapView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator

class PicassoZoomImageViewFragment :
    BaseZoomImageViewFragment<PicassoZoomImageView>() {

    private val args by navArgs<PicassoZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override fun createZoomImageView(context: Context): PicassoZoomImageView {
        return PicassoZoomImageView(context)
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
        val sketchImageUri = args.imageUri
        when {
            sketchImageUri.startsWith("asset://") ->
                zoomView.loadImage(
                    path = sketchImageUri.replace("asset://", "file:///android_asset/"),
                    callback = callback,
                    config = config
                )

            sketchImageUri.startsWith("android.resource://") -> {
                val resId =
                    sketchImageUri.toUri().getQueryParameters("resId").firstOrNull()
                        ?.toIntOrNull()
                        ?: throw IllegalArgumentException("Can't use Subsampling, invalid resource uri: '$sketchImageUri'")
                zoomView.loadImage(
                    resourceId = resId,
                    callback = callback,
                    config = config
                )
            }

            else ->
                zoomView.loadImage(
                    uri = Uri.parse(sketchImageUri),
                    callback = callback,
                    config = config
                )
        }
    }

    override fun loadMinimap(minimapView: ZoomImageMinimapView, sketchImageUri: String) {
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
            .resize(600, 600)
            .centerInside()
            .into(minimapView)
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = PicassoZoomImageViewFragment().apply {
            arguments = PicassoZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}