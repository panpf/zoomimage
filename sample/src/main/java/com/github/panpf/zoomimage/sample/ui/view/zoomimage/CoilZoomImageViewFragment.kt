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

import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.load
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.databinding.CoilZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.databinding.CommonZoomImageViewFragmentBinding
import java.io.File

class CoilZoomImageViewFragment : BaseZoomImageViewFragment<CoilZoomImageViewFragmentBinding>() {

    private val args by navArgs<CoilZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override fun getCommonBinding(binding: CoilZoomImageViewFragmentBinding): CommonZoomImageViewFragmentBinding {
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
        binding.coilZoomImageViewImage.load(sketchUri2CoilModel(binding, args.imageUri)) {
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

    private fun sketchUri2CoilModel(
        binding: CoilZoomImageViewFragmentBinding,
        @Suppress("SameParameterValue") sketchImageUri: String
    ): Any? {
        return when {
            sketchImageUri.startsWith("asset://") -> {
                sketchImageUri.replace("asset://", "file://filled/android_asset/").toUri()
            }

            sketchImageUri.startsWith("file://") -> {
                File(sketchImageUri.substring("file://".length))
            }

            sketchImageUri.startsWith("android.resource://") -> {
                val resId =
                    sketchImageUri.toUri().getQueryParameters("resId").firstOrNull()?.toIntOrNull()
                if (resId != null) {
                    "android.resource://${requireContext().packageName}/$resId".toUri()
                } else {
                    binding.coilZoomImageViewImage.zoomAbility.logger.w("ZoomImageViewFragment") {
                        "Can't use Subsampling, invalid resource uri: '$sketchImageUri'"
                    }
                    null
                }
            }

            else -> {
                sketchImageUri.toUri()
            }
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