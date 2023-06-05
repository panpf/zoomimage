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
package com.github.panpf.zoomimage.sample.ui.zoomimage

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.sketch
import com.github.panpf.zoomimage.ImageSource
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.format
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.ui.base.BindingFragment
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.sample.ui.util.toVeryShortString
import kotlinx.coroutines.launch

class ZoomImageViewFragment : BindingFragment<ZoomImageViewFragmentBinding>() {

    private val args by navArgs<ZoomImageViewFragmentArgs>()
    private val settingsEventViewModel by viewModels<SettingsEventViewModel>()

    override fun onViewCreated(binding: ZoomImageViewFragmentBinding, savedInstanceState: Bundle?) {
        binding.zoomImageViewImage.apply {
            zoomAbility.logger.level = if (BuildConfig.DEBUG)
                Logger.Level.DEBUG else Logger.Level.INFO
            // todo settings
            settingsEventViewModel.observeZoomSettings(this)

            subsamplingAbility.setLifecycle(viewLifecycleOwner.lifecycle)
        }

        binding.common.zoomImageViewErrorRetryButton.setOnClickListener {
            loadImage(binding)
        }

        binding.common.zoomImageViewTileMap.setZoomImageView(binding.zoomImageViewImage)

        binding.common.zoomImageViewRotate.setOnClickListener {
            binding.zoomImageViewImage.zoomAbility.rotateBy(90)
        }

        binding.common.zoomImageViewSettings.setOnClickListener {
            // todo settings
//            findNavController().navigate(
//                MainFragmentDirections.actionGlobalSettingsDialogFragment(Page.ZOOM.name)
//            )
        }

        binding.common.zoomImageViewInfoText.apply {
            maxLines = 4
            setOnClickListener {
                maxLines = if (maxLines == 4) Int.MAX_VALUE else 4
            }
            binding.zoomImageViewImage.zoomAbility.addOnMatrixChangeListener {
                updateInfo(binding)
            }
            binding.zoomImageViewImage.zoomAbility.addOnScaleChangeListener { _, _, _ ->
                updateInfo(binding)
            }
        }

        loadImage(binding)
    }

    private fun loadImage(binding: ZoomImageViewFragmentBinding) {
        binding.zoomImageViewImage.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                val request = DisplayRequest(requireContext(), args.imageUri) {
                    lifecycle(viewLifecycleOwner.lifecycle)
                }
                binding.common.zoomImageViewProgress.isVisible = true
                binding.common.zoomImageViewError.isVisible = false
                val result = requireContext().sketch.execute(request)
                if (result is DisplayResult.Success) {
                    setImageDrawable(result.drawable)
                    val assetFileName = args.imageUri.replace("asset://", "")
                    subsamplingAbility.setImageSource(
                        ImageSource.fromAsset(requireContext(), assetFileName)
                    )
                    binding.common.zoomImageViewProgress.isVisible = false
                    binding.common.zoomImageViewError.isVisible = false
                } else {
                    binding.common.zoomImageViewProgress.isVisible = false
                    binding.common.zoomImageViewError.isVisible = true
                }
            }
        }

        binding.common.zoomImageViewTileMap.displayImage(args.imageUri) {
            resizeSize(600, 600)
            resizePrecision(Precision.LESS_PIXELS)
        }
    }

    private fun updateInfo(binding: ZoomImageViewFragmentBinding) {
        val info = binding.zoomImageViewImage.zoomAbility.run {
            """
                scale: ${scale.format(2)}, range=[${minScale.format(2)}, ${maxScale.format(2)}], steps=(${
                stepScales.joinToString {
                    it.format(
                        2
                    )
                }
            })
                translation: ${translation.run { "($x, $y)" }}
                visibleRect: ${getVisibleRect().toVeryShortString()}
                drawRect: ${getDrawRect().toVeryShortString()}
                size: view=${viewSize.toShortString()}, drawable=${drawableSize.toShortString()}
                edge: hor=${horScrollEdge}, ver=${verScrollEdge}
            """.trimIndent()
            // todo bitmap, subsampling info
        }
        binding.common.zoomImageViewInfoText.text = info
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