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

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.load
import coil.size.Precision.INEXACT
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.databinding.CoilZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewCommonFragmentBinding
import com.github.panpf.zoomimage.sample.ui.widget.view.ZoomImageMinimapView
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel

class CoilZoomImageViewFragment : BaseZoomImageViewFragment<CoilZoomImageViewFragmentBinding>() {

    private val args by navArgs<CoilZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override fun getCommonBinding(binding: CoilZoomImageViewFragmentBinding): ZoomImageViewCommonFragmentBinding {
        return binding.common
    }

    override fun getZoomImageView(binding: CoilZoomImageViewFragmentBinding): ZoomImageView {
        return binding.coilZoomImageViewImage
    }

    override fun loadImage(
        binding: CoilZoomImageViewFragmentBinding,
        onCallStart: () -> Unit,
        onCallSuccess: () -> Unit,
        onCallError: () -> Unit
    ) {
        val model = sketchUri2CoilModel(requireContext(), args.imageUri)
        binding.coilZoomImageViewImage.load(model) {
            lifecycle(viewLifecycleOwner.lifecycle)
            precision(coil.size.Precision.INEXACT)
            crossfade(true)
            listener(
                onStart = { onCallStart() },
                onSuccess = { _, _ -> onCallSuccess() },
                onError = { _, _ -> onCallError() },
            )
        }
    }

    override fun loadMinimap(zoomImageMinimapView: ZoomImageMinimapView, sketchImageUri: String) {
        val model = sketchUri2CoilModel(requireContext(), args.imageUri)
        zoomImageMinimapView.load(model) {
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