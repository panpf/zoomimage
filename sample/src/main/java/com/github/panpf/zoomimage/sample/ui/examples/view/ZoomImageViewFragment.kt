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

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.sketch
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewCommonFragmentBinding
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.ui.widget.view.ZoomImageMinimapView
import com.github.panpf.zoomimage.sample.util.collectWithLifecycle
import com.github.panpf.zoomimage.subsampling.ImageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ZoomImageViewFragment : BaseZoomImageViewFragment<ZoomImageViewFragmentBinding>() {

    private val args by navArgs<ZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override fun getCommonBinding(binding: ZoomImageViewFragmentBinding): ZoomImageViewCommonFragmentBinding {
        return binding.common
    }

    override fun getZoomImageView(binding: ZoomImageViewFragmentBinding): ZoomImageView {
        return binding.zoomImageViewImage
    }

    override fun onViewCreated(
        binding: ZoomImageViewFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(binding, savedInstanceState)

        prefsService.ignoreExifOrientation.sharedFlow.collectWithLifecycle(viewLifecycleOwner) {
            loadData(binding, binding.common, sketchImageUri)
        }
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
                    downloadCachePolicy(CachePolicy.ENABLED)
                    ignoreExifOrientation(prefsService.ignoreExifOrientation.value)
                }
                val result = requireContext().sketch.execute(request)
                if (result is DisplayResult.Success) {
                    setImageDrawable(result.drawable)
                    subsamplingAbility.ignoreExifOrientation =
                        prefsService.ignoreExifOrientation.value
                    subsamplingAbility.setImageSource(newImageSource(binding, sketchImageUri))
                    onCallSuccess()
                } else {
                    subsamplingAbility.setImageSource(null)
                    onCallError()
                }
            }
        }
    }

    override fun loadMinimap(zoomImageMinimapView: ZoomImageMinimapView, sketchImageUri: String) {
        zoomImageMinimapView.displayImage(sketchImageUri) {
            lifecycle(viewLifecycleOwner.lifecycle)
            crossfade()
            resizeSize(600, 600)
            resizePrecision(Precision.LESS_PIXELS)
            ignoreExifOrientation(prefsService.ignoreExifOrientation.value)
        }
    }

    private suspend fun newImageSource(
        binding: ZoomImageViewFragmentBinding,
        sketchImageUri: String
    ): ImageSource? = when {
        sketchImageUri.startsWith("http://") || sketchImageUri.startsWith("https://") -> {
            val cache = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    requireContext().sketch.downloadCache[sketchImageUri]
                }
            }.getOrNull()
            cache?.let { ImageSource.fromFile(it.file) }
        }

        sketchImageUri.startsWith("asset://") -> {
            val assetFileName = sketchImageUri.replace("asset://", "")
            ImageSource.fromAsset(requireContext(), assetFileName)
        }

        sketchImageUri.startsWith("android.resource://") -> {
            val resId =
                Uri.parse(sketchImageUri).getQueryParameters("resId").firstOrNull()
                    ?.toIntOrNull()
            if (resId != null) {
                ImageSource.fromResource(requireContext().resources, resId)
            } else {
                binding.zoomImageViewImage.logger.w {
                    "ZoomImageViewFragment. Can't use Subsampling, invalid resource uri: '$sketchImageUri'"
                }
                null
            }
        }

        sketchImageUri.startsWith("content://") -> {
            ImageSource.fromContent(requireContext(), Uri.parse(sketchImageUri))
        }

        sketchImageUri.startsWith("/") -> {
            ImageSource.fromFile(File(sketchImageUri))
        }

        sketchImageUri.startsWith("file://") -> {
            ImageSource.fromFile(File(sketchImageUri.replace("file://", "")))
        }

        else -> {
            binding.zoomImageViewImage.logger.w {
                "ZoomImageViewFragment. Can't use Subsampling, unsupported uri: '$sketchImageUri'"
            }
            null
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