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
package com.github.panpf.zoomimage.sample.ui.picasso

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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
import com.github.panpf.zoomimage.sample.databinding.PicassoZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.ui.base.BindingFragment
import com.github.panpf.zoomimage.sample.ui.coil.CoilZoomImageViewFragmentArgs
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.sample.ui.util.toVeryShortString
import com.github.panpf.zoomimage.sample.ui.zoomimage.SettingsDialogFragment
import com.github.panpf.zoomimage.sample.util.lifecycleOwner
import com.squareup.picasso.Callback
import com.squareup.picasso.RequestCreator
import kotlinx.coroutines.launch
import java.io.File

class PicassoZoomImageViewFragment : BindingFragment<PicassoZoomImageViewFragmentBinding>() {

    private val args by navArgs<CoilZoomImageViewFragmentArgs>()

    override fun onViewCreated(
        binding: PicassoZoomImageViewFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        binding.picassoZoomImageViewImage.apply {
            zoomAbility.logger.level = if (BuildConfig.DEBUG)
                Logger.Level.DEBUG else Logger.Level.INFO

            subsamplingAbility.setLifecycle(viewLifecycleOwner.lifecycle)

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

        binding.common.zoomImageViewTileMap.setZoomImageView(binding.picassoZoomImageViewImage)

        binding.common.zoomImageViewRotate.setOnClickListener {
            binding.picassoZoomImageViewImage.zoomAbility.rotateBy(90)
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
            binding.picassoZoomImageViewImage.zoomAbility.addOnMatrixChangeListener {
                updateInfo(binding)
            }
            binding.picassoZoomImageViewImage.zoomAbility.addOnScaleChangeListener { _, _, _ ->
                updateInfo(binding)
            }
        }

        loadImage(binding)
    }

    private fun loadImage(binding: PicassoZoomImageViewFragmentBinding) {
        binding.common.zoomImageViewProgress.isVisible = true
        binding.common.zoomImageViewErrorLayout.isVisible = false
        val callback = object : Callback {
            override fun onSuccess() {
                binding.common.zoomImageViewProgress.isVisible = false
                binding.common.zoomImageViewErrorLayout.isVisible = false
            }

            override fun onError(e: Exception?) {
                binding.common.zoomImageViewProgress.isVisible = false
                binding.common.zoomImageViewErrorLayout.isVisible = true
            }
        }
        val config: RequestCreator.() -> Unit = {
            fit()
            centerInside()
        }
        val sketchImageUri = args.imageUri
        when {
            sketchImageUri.startsWith("asset://") ->
                binding.picassoZoomImageViewImage.loadImage(
                    path = sketchImageUri.replace("asset://", "file:///android_asset/"),
                    callback = callback,
                    config = config
                )

            sketchImageUri.startsWith("file://") ->
                binding.picassoZoomImageViewImage.loadImage(
                    file = File(sketchImageUri.replace("file://", "")),
                    callback = callback,
                    config = config
                )

            sketchImageUri.startsWith("android.resource://") -> {
                val resId =
                    sketchImageUri.toUri().getQueryParameters("resId").firstOrNull()?.toIntOrNull()
                if (resId != null) {
                    binding.picassoZoomImageViewImage.loadImage(
                        resourceId = resId,
                        callback = callback,
                        config = config
                    )
                } else {
                    binding.picassoZoomImageViewImage.zoomAbility.logger.w("ZoomImageViewFragment") {
                        "Can't use Subsampling, invalid resource uri: '$sketchImageUri'"
                    }
                    binding.picassoZoomImageViewImage.loadImage(
                        path = null,
                        callback = callback,
                        config = config
                    )
                }
            }

            else ->
                binding.picassoZoomImageViewImage.loadImage(
                    uri = Uri.parse(sketchImageUri),
                    callback = callback,
                    config = config
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
    private fun updateInfo(binding: PicassoZoomImageViewFragmentBinding) {
        binding.common.zoomImageViewUriText.text = "uri: ${args.imageUri}"
        val zoomInfo = binding.picassoZoomImageViewImage.zoomAbility.run {
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
        val imageInfo = binding.picassoZoomImageViewImage.subsamplingAbility.run {
            val exifOrientationName = imageExifOrientation?.let { exifOrientationName(it) }
            """
                image: ${imageSize?.toShortString()}, '${imageMimeType}', $exifOrientationName
            """.trimIndent()
        }
        val subsamplingInfo = binding.picassoZoomImageViewImage.subsamplingAbility.run {
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
        ): Fragment = PicassoZoomImageViewFragment().apply {
            arguments = CoilZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}