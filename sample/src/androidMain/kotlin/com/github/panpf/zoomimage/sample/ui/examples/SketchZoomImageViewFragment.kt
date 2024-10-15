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
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.ability.showSectorProgressIndicator
import com.github.panpf.sketch.loadImage
import com.github.panpf.sketch.request.LoadState
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.state.ThumbnailMemoryCacheStateImage
import com.github.panpf.zoomimage.SketchZoomImageView
import com.github.panpf.zoomimage.sample.databinding.FragmentZoomViewBinding
import com.github.panpf.zoomimage.sample.ui.components.StateView
import com.github.panpf.zoomimage.sample.ui.components.ZoomImageMinimapView
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.util.repeatCollectWithLifecycle

class SketchZoomImageViewFragment : BaseZoomImageViewFragment<SketchZoomImageView>() {

    private val args by navArgs<SketchZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override fun createZoomImageView(context: Context): SketchZoomImageView {
        return SketchZoomImageView(context)
    }

    override fun onViewCreated(
        binding: FragmentZoomViewBinding,
        zoomView: SketchZoomImageView,
        savedInstanceState: Bundle?
    ) {
        zoomView.showSectorProgressIndicator()

        zoomView.requestState.loadState
            .repeatCollectWithLifecycle(
                owner = viewLifecycleOwner,
                state = Lifecycle.State.CREATED
            ) {
                if (it is LoadState.Error) {
                    binding.stateView.error {
                        message(it.result.throwable)
                        retryAction {
                            loadData()
                        }
                    }
                } else {
                    binding.stateView.gone()
                }
            }

        super.onViewCreated(binding, savedInstanceState)
    }

    override fun loadImage(zoomView: SketchZoomImageView, stateView: StateView) {
        zoomView.loadImage(args.imageUri) {
            placeholder(ThumbnailMemoryCacheStateImage(args.placeholderImageUri))
            crossfade(fadeStart = false)
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
        ): Fragment = SketchZoomImageViewFragment().apply {
            arguments = SketchZoomImageViewFragmentArgs(
                imageUri = data.originalUrl,
                placeholderImageUri = data.listThumbnailUrl
            ).toBundle()
        }
    }
}