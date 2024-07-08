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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.asDrawableOrThrow
import com.github.panpf.sketch.loadImage
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.sketch
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.ui.widget.view.StateView
import com.github.panpf.zoomimage.sample.ui.widget.view.ZoomImageMinimapView
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.fromContent
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.fromResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class BasicZoomImageViewFragment : BaseZoomImageViewFragment<ZoomImageView>() {

    private val args by navArgs<BasicZoomImageViewFragmentArgs>()

    override val sketchImageUri: String
        get() = args.imageUri

    override fun createZoomImageView(context: Context): ZoomImageView {
        return ZoomImageView(context)
    }

    override fun loadImage(zoomView: ZoomImageView, stateView: StateView) {
        stateView.loading()
        zoomView.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                val request = ImageRequest(requireContext(), sketchImageUri)
                val result = requireContext().sketch.execute(request)
                if (result is ImageResult.Success) {
                    setImageDrawable(result.image.asDrawableOrThrow())
                    subsampling.setImageSource(newImageSource(zoomView, sketchImageUri))
                    stateView.gone()
                } else if (result is ImageResult.Error) {
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
        minimapView.loadImage(sketchImageUri) {
            crossfade()
            size(600, 600)
            precision(Precision.LESS_PIXELS)
        }
    }

    private suspend fun newImageSource(
        zoomView: ZoomImageView,
        sketchImageUri: String
    ): ImageSource? = when {
        sketchImageUri.startsWith("http://") || sketchImageUri.startsWith("https://") -> {
            val cache = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    requireContext().sketch.downloadCache.openSnapshot(sketchImageUri)
                        ?.use { it.data }
                }
            }.getOrNull()
            cache?.let { ImageSource.fromFile(it) }
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
        ): Fragment = BasicZoomImageViewFragment().apply {
            arguments = BasicZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}