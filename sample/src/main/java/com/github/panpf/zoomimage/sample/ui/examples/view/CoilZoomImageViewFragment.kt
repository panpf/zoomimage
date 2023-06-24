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
package com.github.panpf.zoomimage.sample.ui.examples.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.load
import coil.request.CachePolicy
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.databinding.CoilZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewCommonFragmentBinding
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.util.collectWithLifecycle
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel
import kotlinx.coroutines.flow.merge

class CoilZoomImageViewFragment : BaseZoomImageViewFragment<CoilZoomImageViewFragmentBinding>() {

    private val args by navArgs<CoilZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override val supportDisabledMemoryCache: Boolean
        get() = true

    override val supportIgnoreExifOrientation: Boolean
        get() = false

    override val supportDisallowReuseBitmap: Boolean
        get() = false

    override fun getCommonBinding(binding: CoilZoomImageViewFragmentBinding): ZoomImageViewCommonFragmentBinding {
        return binding.common
    }

    override fun getZoomImageView(binding: CoilZoomImageViewFragmentBinding): ZoomImageView {
        return binding.coilZoomImageViewImage
    }

    override fun onViewCreated(
        binding: CoilZoomImageViewFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(binding, savedInstanceState)

        binding.coilZoomImageViewImage.apply {
//            listOf(
//                prefsService.disableMemoryCache.stateFlow,
//                prefsService.disallowReuseBitmap.stateFlow,
//                prefsService.ignoreExifOrientation.stateFlow,
//            ).merge().collect(lifecycleOwner) {
//                subsamplingAbility.disableMemoryCache = prefsService.disableMemoryCache.value
//                subsamplingAbility.disallowReuseBitmap = prefsService.disallowReuseBitmap.value
//                subsamplingAbility.ignoreExifOrientation = prefsService.ignoreExifOrientation.value
//            }
            listOf(
                prefsService.disableMemoryCache.sharedFlow,
//                prefsService.disallowReuseBitmap.sharedFlow,
//                prefsService.ignoreExifOrientation.sharedFlow,
            ).merge().collectWithLifecycle(viewLifecycleOwner) {
                loadData(binding, binding.common, sketchImageUri)
            }
        }
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
            memoryCachePolicy(
                if (prefsService.disableMemoryCache.value)
                    CachePolicy.DISABLED else CachePolicy.ENABLED
            )
            listener(
                onStart = { onCallStart() },
                onSuccess = { _, _ -> onCallSuccess() },
                onError = { _, _ -> onCallError() },
            )
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