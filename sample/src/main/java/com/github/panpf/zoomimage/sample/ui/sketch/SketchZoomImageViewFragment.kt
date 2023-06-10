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
package com.github.panpf.zoomimage.sample.ui.sketch

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.decode.internal.exifOrientationName
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.tools4j.io.ktx.formatFileSize
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.format
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.databinding.SketchZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.ui.base.BindingFragment
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.sample.ui.util.toVeryShortString
import com.github.panpf.zoomimage.sample.ui.zoomimage.SettingsDialogFragment
import com.github.panpf.zoomimage.sample.util.lifecycleOwner
import kotlinx.coroutines.launch

class SketchZoomImageViewFragment : BindingFragment<SketchZoomImageViewFragmentBinding>() {

    private val args by navArgs<SketchZoomImageViewFragmentArgs>()

    override fun onViewCreated(
        binding: SketchZoomImageViewFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        binding.sketchZoomImageViewImage.apply {
            zoomAbility.logger.level = if (BuildConfig.DEBUG)
                Logger.Level.DEBUG else Logger.Level.INFO

            lifecycleOwner.lifecycleScope.launch {
                prefsService.scaleType.stateFlow.collect {
                    scaleType = ImageView.ScaleType.valueOf(it)
                }
            }
            lifecycleOwner.lifecycleScope.launch {
                prefsService.scrollBarEnabled.stateFlow.collect {
                    zoomAbility.scrollBarEnabled = it
                }
            }
            lifecycleOwner.lifecycleScope.launch {
                prefsService.readModeEnabled.stateFlow.collect {
                    zoomAbility.readModeEnabled = it
                }
            }
            lifecycleOwner.lifecycleScope.launch {
                prefsService.showTileBounds.stateFlow.collect {
                    subsamplingAbility.showTileBounds = it
                }
            }
        }

        binding.common.zoomImageViewErrorRetryButton.setOnClickListener {
            loadImage(binding)
        }

        binding.common.zoomImageViewTileMap.setZoomImageView(binding.sketchZoomImageViewImage)

        binding.common.zoomImageViewRotate.setOnClickListener {
            binding.sketchZoomImageViewImage.zoomAbility.rotateBy(90)
        }

        binding.common.zoomImageViewSettings.setOnClickListener {
            SettingsDialogFragment().show(childFragmentManager, null)
        }

        binding.common.zoomImageViewInfoText.apply {
            var isSingleLine = true
            binding.common.zoomImageViewUriText.isSingleLine = isSingleLine
            binding.common.zoomImageViewInfoText.maxLines = 4
            setOnClickListener {
                isSingleLine = !isSingleLine
                binding.common.zoomImageViewUriText.isSingleLine = isSingleLine
                binding.common.zoomImageViewInfoText.maxLines =
                    if (binding.common.zoomImageViewInfoText.maxLines == 4) Int.MAX_VALUE else 4
            }
            binding.sketchZoomImageViewImage.zoomAbility.addOnMatrixChangeListener {
                updateInfo(binding)
            }
            binding.sketchZoomImageViewImage.zoomAbility.addOnScaleChangeListener { _, _, _ ->
                updateInfo(binding)
            }
        }

        loadImage(binding)
        updateInfo(binding)
    }

    private fun loadImage(binding: SketchZoomImageViewFragmentBinding) {
        binding.sketchZoomImageViewImage.displayImage(args.imageUri) {
            lifecycle(viewLifecycleOwner.lifecycle)
            crossfade()
            listener(
                onStart = {
                    binding.common.zoomImageViewProgress.isVisible = true
                    binding.common.zoomImageViewErrorLayout.isVisible = false
                },
                onSuccess = { _, _ ->
                    binding.common.zoomImageViewProgress.isVisible = false
                    binding.common.zoomImageViewErrorLayout.isVisible = false
                },
                onError = { _, _ ->
                    binding.common.zoomImageViewProgress.isVisible = false
                    binding.common.zoomImageViewErrorLayout.isVisible = true
                },
            )
        }

        binding.common.zoomImageViewTileMap.displayImage(args.imageUri) {
            lifecycle(viewLifecycleOwner.lifecycle)
            crossfade()
            resizeSize(600, 600)
            resizePrecision(Precision.LESS_PIXELS)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateInfo(binding: SketchZoomImageViewFragmentBinding) {
        binding.common.zoomImageViewUriText.text = "uri: ${args.imageUri}"
        val zoomInfo = binding.sketchZoomImageViewImage.zoomAbility.run {
            val stepScalesString = stepScales.joinToString { it.format(2) }
            """
                scale: ${scale.format(2)}, range=[${minScale.format(2)}, ${maxScale.format(2)}], steps=($stepScalesString)
                translation: ${translation.run { "($x, $y)" }}
                drawRect: ${getDrawRect().toVeryShortString()}
                visibleRect: ${getVisibleRect().toVeryShortString()}
                edge: hor=${horScrollEdge}, ver=${verScrollEdge}
                size: view=${viewSize.toShortString()}, drawable=${drawableSize.toShortString()}
            """.trimIndent()
        }
        val imageInfo = binding.sketchZoomImageViewImage.subsamplingAbility.run {
            val exifOrientationName = imageExifOrientation?.let { exifOrientationName(it) }
            """
                image: ${imageSize?.toShortString()}, '${imageMimeType}', $exifOrientationName
            """.trimIndent()
        }
        val subsamplingInfo = binding.sketchZoomImageViewImage.subsamplingAbility.run {
            val tileList = tileList ?: emptyList()
            val tilesByteCount = tileList.sumOf { it.bitmap?.byteCount ?: 0 }
                .toLong().formatFileSize()
            """
                tileCount=${tileList.size}
                validTileCount=${tileList.count { it.bitmap != null }}
                tilesByteCount=${tilesByteCount}
            """.trimIndent()
        }
        binding.common.zoomImageViewInfoText.text = "$zoomInfo\n$imageInfo\n$subsamplingInfo"
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