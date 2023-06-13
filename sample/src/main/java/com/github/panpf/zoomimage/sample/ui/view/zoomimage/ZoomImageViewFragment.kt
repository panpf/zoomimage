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

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.sketch
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewCommonFragmentBinding
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewFragmentBinding
import kotlinx.coroutines.launch

class ZoomImageViewFragment : BaseZoomImageViewFragment<ZoomImageViewFragmentBinding>() {

    private val args by navArgs<ZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override val supportDisabledMemoryCache: Boolean
        get() = false

    override val supportIgnoreExifOrientation: Boolean
        get() = false

    override val supportDisallowReuseBitmap: Boolean
        get() = false

    override fun getCommonBinding(binding: ZoomImageViewFragmentBinding): ZoomImageViewCommonFragmentBinding {
        return binding.common
    }

    override fun getZoomImageView(binding: ZoomImageViewFragmentBinding): ZoomImageView {
        return binding.zoomImageViewImage
    }

    override fun loadImage(
        binding: ZoomImageViewFragmentBinding,
        onCallStart: () -> Unit,
        onCallSuccess: () -> Unit,
        onCallError: () -> Unit
    ) {
        onCallStart()
        binding.zoomImageViewImage.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                val request = DisplayRequest(requireContext(), sketchImageUri) {
                    lifecycle(viewLifecycleOwner.lifecycle)
                }
                val result = requireContext().sketch.execute(request)
                if (result is DisplayResult.Success) {
                    setImageDrawable(result.drawable)
                    onCallSuccess()
                } else {
                    onCallError()
                }
            }
        }
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = ZoomImageViewFragment().apply {
            arguments = ZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}