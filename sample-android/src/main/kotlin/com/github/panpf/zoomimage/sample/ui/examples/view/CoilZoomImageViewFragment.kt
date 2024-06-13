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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil3.load
import coil3.request.crossfade
import coil3.request.lifecycle
import coil3.size.Precision.INEXACT
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.CoilZoomImageView
import com.github.panpf.zoomimage.sample.ui.widget.view.StateView
import com.github.panpf.zoomimage.sample.ui.widget.view.ZoomImageMinimapView
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel

class CoilZoomImageViewFragment : BaseZoomImageViewFragment<CoilZoomImageView>() {

    private val args by navArgs<CoilZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override fun createZoomImageView(context: Context): CoilZoomImageView {
        return CoilZoomImageView(context)
    }

    override fun loadImage(zoomView: CoilZoomImageView, stateView: StateView) {
        val model = sketchUri2CoilModel(requireContext(), args.imageUri)
        zoomView.load(model) {
            lifecycle(viewLifecycleOwner.lifecycle)
            precision(coil3.size.Precision.INEXACT)
            crossfade(true)
//            val imageLoader = Coil.imageLoader(context)
//            if (coilData != null) {
//                val key = imageLoader.components.key(coilData, Options(context))
//                placeholderMemoryCacheKey(key)
//            }
            listener(
                onStart = { stateView.loading() },
                onSuccess = { _, _ -> stateView.gone() },
                onError = { _, result ->
                    stateView.error {
                        message(result.throwable)
                        retryAction {
                            loadData()
                        }
                    }
                },
            )
        }
    }

    override fun loadMinimap(minimapView: ZoomImageMinimapView, sketchImageUri: String) {
        val model = sketchUri2CoilModel(requireContext(), args.imageUri)
        minimapView.load(model) {
            lifecycle(viewLifecycleOwner.lifecycle)
            crossfade(true)
            size(600, 600)
            precision(INEXACT)
        }
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = CoilZoomImageViewFragment().apply {
            arguments = CoilZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}