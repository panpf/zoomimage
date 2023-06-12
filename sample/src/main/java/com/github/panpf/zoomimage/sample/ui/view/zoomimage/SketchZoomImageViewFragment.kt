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
package com.github.panpf.zoomimage.sample.ui.view.zoomimage

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.displayImage
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.databinding.CommonZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.databinding.SketchZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.util.collect
import com.github.panpf.zoomimage.sample.util.lifecycleOwner
import kotlinx.coroutines.flow.merge

class SketchZoomImageViewFragment :
    BaseZoomImageViewFragment<SketchZoomImageViewFragmentBinding>() {

    private val args by navArgs<SketchZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override val supportDisabledMemoryCache: Boolean
        get() = true

    override val supportIgnoreExifOrientation: Boolean
        get() = true

    override val supportDisallowReuseBitmap: Boolean
        get() = true

    override fun getCommonBinding(binding: SketchZoomImageViewFragmentBinding): CommonZoomImageViewFragmentBinding {
        return binding.common
    }

    override fun getZoomImageView(binding: SketchZoomImageViewFragmentBinding): ZoomImageView {
        return binding.sketchZoomImageViewImage
    }

    override fun onViewCreated(
        binding: SketchZoomImageViewFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(binding, savedInstanceState)

        binding.sketchZoomImageViewImage.apply {
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
                prefsService.disallowReuseBitmap.sharedFlow,
                prefsService.ignoreExifOrientation.sharedFlow,
            ).merge().collect(lifecycleOwner) {
                loadData(binding, binding.common, sketchImageUri)
            }
        }
    }

    override fun loadImage(
        binding: SketchZoomImageViewFragmentBinding,
        onCallStart: () -> Unit,
        onCallSuccess: () -> Unit,
        onCallError: () -> Unit
    ) {
        binding.sketchZoomImageViewImage.displayImage(args.imageUri) {
            lifecycle(viewLifecycleOwner.lifecycle)
            crossfade()
            memoryCachePolicy(if (prefsService.disableMemoryCache.value) CachePolicy.DISABLED else CachePolicy.ENABLED)
            disallowReuseBitmap(prefsService.disallowReuseBitmap.value)
            ignoreExifOrientation(prefsService.ignoreExifOrientation.value)
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
        ): Fragment = SketchZoomImageViewFragment().apply {
            arguments = SketchZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}