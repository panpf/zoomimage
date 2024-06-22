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
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.request.LoadState
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.stateimage.ThumbnailMemoryCacheStateImage
import com.github.panpf.sketch.viewability.showSectorProgressIndicator
import com.github.panpf.zoomimage.SketchZoomImageView
import com.github.panpf.zoomimage.sample.databinding.FragmentZoomViewBinding
import com.github.panpf.zoomimage.sample.ui.util.repeatCollectWithLifecycle
import com.github.panpf.zoomimage.sample.ui.widget.view.StateView
import com.github.panpf.zoomimage.sample.ui.widget.view.ZoomImageMinimapView

class SketchZoomImageViewFragment :
    BaseZoomImageViewFragment<SketchZoomImageView>() {

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
                state = Lifecycle.State.STARTED
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
        zoomView.displayImage(args.imageUri) {
            placeholder(ThumbnailMemoryCacheStateImage())
            crossfade(fadeStart = false)
        }
    }

    override fun loadMinimap(minimapView: ZoomImageMinimapView, sketchImageUri: String) {
        minimapView.displayImage(sketchImageUri) {
            crossfade()
            resizeSize(600, 600)
            resizePrecision(Precision.LESS_PIXELS)
        }
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = SketchZoomImageViewFragment().apply {
            arguments = SketchZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}