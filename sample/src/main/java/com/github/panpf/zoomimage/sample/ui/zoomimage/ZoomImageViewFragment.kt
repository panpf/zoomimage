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

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView.ScaleType
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.decode.internal.exifOrientationName
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.sketch
import com.github.panpf.tools4j.io.ktx.formatFileSize
import com.github.panpf.zoomimage.ImageSource
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.format
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.ui.base.BindingFragment
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.sample.ui.util.toVeryShortString
import com.github.panpf.zoomimage.sample.util.lifecycleOwner
import kotlinx.coroutines.launch
import java.io.File

class ZoomImageViewFragment : BindingFragment<ZoomImageViewFragmentBinding>() {

    private val args by navArgs<ZoomImageViewFragmentArgs>()

    override fun onViewCreated(binding: ZoomImageViewFragmentBinding, savedInstanceState: Bundle?) {
        binding.zoomImageViewImage.apply {
            zoomAbility.logger.level = if (BuildConfig.DEBUG)
                Logger.Level.DEBUG else Logger.Level.INFO

            subsamplingAbility.setLifecycle(viewLifecycleOwner.lifecycle)

            lifecycleOwner.lifecycleScope.launch {
                prefsService.scaleType.stateFlow.collect {
                    scaleType = ScaleType.valueOf(it)
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

        binding.common.zoomImageViewTileMap.setZoomImageView(binding.zoomImageViewImage)

        binding.common.zoomImageViewRotate.setOnClickListener {
            binding.zoomImageViewImage.zoomAbility.rotateBy(90)
        }

        binding.common.zoomImageViewSettings.setOnClickListener {
            SettingsDialogFragment().show(childFragmentManager, null)
        }

        binding.common.zoomImageViewInfoLayout.apply {
            var isSingleLine = true
            binding.common.zoomImageViewUriText.isSingleLine = isSingleLine
            binding.common.zoomImageViewInfoText.maxLines = 4
            setOnClickListener {
                isSingleLine = !isSingleLine
                binding.common.zoomImageViewUriText.isSingleLine = isSingleLine
                binding.common.zoomImageViewInfoText.maxLines =
                    if (binding.common.zoomImageViewInfoText.maxLines == 4) Int.MAX_VALUE else 4
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
                binding.common.zoomImageViewErrorLayout.isVisible = false
                val result = requireContext().sketch.execute(request)
                if (result is DisplayResult.Success) {
                    setImageDrawable(result.drawable)
                    subsamplingAbility.setImageSource(newImageSource(binding, args.imageUri))
                    binding.common.zoomImageViewProgress.isVisible = false
                    binding.common.zoomImageViewErrorLayout.isVisible = false
                } else {
                    subsamplingAbility.setImageSource(null)
                    binding.common.zoomImageViewProgress.isVisible = false
                    binding.common.zoomImageViewErrorLayout.isVisible = true
                }
            }
        }

        binding.common.zoomImageViewTileMap.displayImage(args.imageUri) {
            lifecycle(viewLifecycleOwner.lifecycle)
            crossfade()
            resizeSize(600, 600)
            resizePrecision(Precision.LESS_PIXELS)
        }
    }

    private fun newImageSource(
        binding: ZoomImageViewFragmentBinding,
        sketchImageUri: String
    ): ImageSource? {
        return when {
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
                    binding.zoomImageViewImage.zoomAbility.logger.w("ZoomImageViewFragment") {
                        "Can't use Subsampling, invalid resource uri: '$sketchImageUri'"
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
                binding.zoomImageViewImage.zoomAbility.logger.w("ZoomImageViewFragment") {
                    "Can't use Subsampling, unsupported uri: '$sketchImageUri'"
                }
                null
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateInfo(binding: ZoomImageViewFragmentBinding) {
        binding.common.zoomImageViewUriText.text = "uri: ${args.imageUri}"
        val zoomInfo = binding.zoomImageViewImage.zoomAbility.run {
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
        val imageInfo = binding.zoomImageViewImage.subsamplingAbility.run {
            val exifOrientationName = imageExifOrientation?.let { exifOrientationName(it) }
            """
                image: ${imageSize?.toShortString()}, '${imageMimeType}', $exifOrientationName
            """.trimIndent()
        }
        val subsamplingInfo = binding.zoomImageViewImage.subsamplingAbility.run {
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
        ): Fragment = ZoomImageViewFragment().apply {
            arguments = ZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}