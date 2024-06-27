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

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.OnStateChangedListener
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.sketch
import com.github.panpf.sketch.util.DownloadData
import com.github.panpf.tools4a.toast.ktx.showShortToast
import com.github.panpf.zoomimage.sample.databinding.FragmentSubsamplingViewBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingFragment
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.sample.util.toShortString
import com.github.panpf.zoomimage.sample.util.toVeryShortString
import kotlinx.coroutines.launch

class SubsamplingViewFragment : BaseBindingFragment<FragmentSubsamplingViewBinding>() {

    private val args by navArgs<SubsamplingViewFragmentArgs>()

    override fun onViewCreated(
        binding: FragmentSubsamplingViewBinding,
        savedInstanceState: Bundle?
    ) {
        binding.subsamplingView.apply {
            setOnClickListener {
                showShortToast("Click")
            }
            setOnLongClickListener {
                showShortToast("Long click")
                true
            }
            setOnStateChangedListener(object : OnStateChangedListener {
                override fun onScaleChanged(newScale: Float, origin: Int) {
                    updateInfo(binding)
                }

                override fun onCenterChanged(newCenter: PointF?, origin: Int) {
                    updateInfo(binding)
                }
            })
        }

        updateInfo(binding)
        setImage(binding)
    }

    private fun setImage(binding: FragmentSubsamplingViewBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            val imageSource = newImageSource(binding, args.imageUri)
            if (imageSource != null) {
                binding.subsamplingView.setImage(imageSource)
            }
        }
    }

    private suspend fun newImageSource(
        binding: FragmentSubsamplingViewBinding,
        sketchImageUri: String
    ): ImageSource? {
        return when {
            sketchImageUri.startsWith("asset://") -> {
                ImageSource.asset(sketchImageUri.replace("asset://", ""))
            }

            sketchImageUri.startsWith("android.resource://") -> {
                val resId =
                    sketchImageUri.toUri().getQueryParameters("resId").firstOrNull()?.toIntOrNull()
                if (resId != null) {
                    ImageSource.resource(resId)
                } else {
                    Log.e(
                        "ZoomImageViewFragment",
                        "newImageSource failed, invalid resource uri: '$sketchImageUri'"
                    )
                    null
                }
            }

            sketchImageUri.startsWith("http://") || sketchImageUri.startsWith("https://") -> {
                binding.stateView.loading()
                val context = binding.subsamplingView.context
                val result = context.sketch.executeDownload(ImageRequest(context, args.imageUri))
                val data = result.getOrNull()
                if (data != null) {
                    binding.stateView.gone()
                    when (data) {
                        is DownloadData.Cache -> {
                            ImageSource.uri(data.path.toFile().toUri())
                        }

                        is DownloadData.Bytes -> {
                            val bitmap =
                                BitmapFactory.decodeByteArray(data.bytes, 0, data.bytes.size)
                            ImageSource.bitmap(bitmap)
                        }

                        else -> {
                            throw IllegalArgumentException("Unsupported DownloadData type: ${data::class}")
                        }
                    }
                } else {
                    binding.stateView.error {
                        message(result.exceptionOrNull())
                        retryAction {
                            setImage(binding)
                        }
                    }
                    null
                }
            }

            else -> {
                ImageSource.uri(sketchImageUri)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateInfo(binding: FragmentSubsamplingViewBinding) {
        binding.infoHeaderText.text = """
                scale: 
                space: 
                center: 
            """.trimIndent()
        binding.infoText.text = binding.subsamplingView.run {
            """
                ${scale.format(2)} in (${minScale.format(2)},${maxScale.format(2)})
                ${RectF().apply { getPanRemaining(this) }.toVeryShortString()}
                ${center?.toShortString()}
            """.trimIndent()
        }
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = SubsamplingViewFragment().apply {
            arguments = SubsamplingViewFragmentArgs(data).toBundle()
        }
    }
}