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
import com.github.panpf.zoomimage.sample.databinding.FragmentZoomViewBinding
import com.github.panpf.zoomimage.sample.settingsService
import com.github.panpf.zoomimage.sample.ui.util.collectWithLifecycle
import com.github.panpf.zoomimage.sample.ui.widget.view.StateView
import com.github.panpf.zoomimage.sample.ui.widget.view.ZoomImageMinimapView
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.fromContent
import com.github.panpf.zoomimage.subsampling.fromResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ZoomImageViewFragment : BaseZoomImageViewFragment<ZoomImageView>() {

    private val args by navArgs<ZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override fun createZoomImageView(context: Context): ZoomImageView {
        return ZoomImageView(context)
    }

    override fun onViewCreated(
        binding: FragmentZoomViewBinding,
        zoomView: ZoomImageView,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(binding, savedInstanceState)

        settingsService.ignoreExifOrientation.sharedFlow.collectWithLifecycle(viewLifecycleOwner) {
            loadData()
        }
    }

    override fun loadImage(zoomView: ZoomImageView, stateView: StateView) {
        stateView.loading()
        zoomView.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                val request = DisplayRequest(requireContext(), sketchImageUri) {
                    downloadCachePolicy(CachePolicy.ENABLED)
                    ignoreExifOrientation(settingsService.ignoreExifOrientation.value)
                }
                val result = requireContext().sketch.execute(request)
                if (result is DisplayResult.Success) {
                    setImageDrawable(result.drawable)
                    subsampling.ignoreExifOrientationState.value =
                        settingsService.ignoreExifOrientation.value
                    subsampling.setImageSource(newImageSource(zoomView, sketchImageUri))
                    stateView.gone()
                } else if (result is DisplayResult.Error) {
                    subsampling.setImageSource(null)
                    stateView.error {
                        message(result.throwable)
                        retryAction {
                            loadData()
                        }
                    }
                }
            }
        }
    }

    override fun loadMinimap(minimapView: ZoomImageMinimapView, sketchImageUri: String) {
        minimapView.displayImage(sketchImageUri) {
            crossfade()
            resizeSize(600, 600)
            resizePrecision(Precision.LESS_PIXELS)
            ignoreExifOrientation(settingsService.ignoreExifOrientation.value)
        }
    }

    private suspend fun newImageSource(
        zoomView: ZoomImageView,
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
                zoomView.logger.w {
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
            zoomView.logger.w {
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