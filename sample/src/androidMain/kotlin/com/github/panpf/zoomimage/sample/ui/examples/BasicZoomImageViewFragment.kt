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
import android.graphics.drawable.Animatable
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.asDrawableOrThrow
import com.github.panpf.sketch.drawable.startWithLifecycle
import com.github.panpf.sketch.loadImage
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.sketch
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.ui.components.StateView
import com.github.panpf.zoomimage.sample.ui.components.ZoomImageMinimapView
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.ui.test.sketchImageUriToZoomImageImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import kotlinx.coroutines.launch

class BasicZoomImageViewFragment : BaseZoomImageViewFragment<ZoomImageView>() {

    private val args by navArgs<BasicZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override fun createZoomImageView(context: Context): ZoomImageView {
        return ZoomImageView(context)
    }

    override fun loadImage(zoomView: ZoomImageView, stateView: StateView) {
        stateView.loading()

        viewLifecycleOwner.lifecycleScope.launch {
            val request = ImageRequest(requireContext(), sketchImageUri)
            val sketch = requireContext().sketch
            val result = sketch.execute(request)
            if (result is ImageResult.Success) {
                val drawable = result.image.asDrawableOrThrow()
                if (drawable is Animatable) {
                    drawable.startWithLifecycle(viewLifecycleOwner.lifecycle)
                }
                zoomView.setImageDrawable(drawable)
                val imageSource = sketchImageUriToZoomImageImageSource(
                    sketch = sketch,
                    imageUri = sketchImageUri,
                    http2ByteArray = false
                )
                zoomView.setImageSource(imageSource)
                stateView.gone()
            } else if (result is ImageResult.Error) {
                zoomView.setImageSource(null as ImageSource?)
                stateView.error {
                    message(result.throwable)
                    retryAction {
                        loadData()
                    }
                }
            }
        }
    }

    override fun loadMinimap(minimapView: ZoomImageMinimapView, sketchImageUri: String) {
        minimapView.loadImage(sketchImageUri) {
            crossfade()
            size(600, 600)
            precision(Precision.LESS_PIXELS)
        }
    }

    class ItemFactory : FragmentItemFactory<Photo>(Photo::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: Photo
        ): Fragment = BasicZoomImageViewFragment().apply {
            arguments = BasicZoomImageViewFragmentArgs(data.originalUrl).toBundle()
        }
    }
}