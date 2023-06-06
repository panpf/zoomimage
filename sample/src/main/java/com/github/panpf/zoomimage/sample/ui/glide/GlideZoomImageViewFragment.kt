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
package com.github.panpf.zoomimage.sample.ui.glide

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.decode.internal.exifOrientationName
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.tools4j.io.ktx.formatFileSize
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.format
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.databinding.GlideZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.ui.base.BindingFragment
import com.github.panpf.zoomimage.sample.ui.coil.CoilZoomImageViewFragmentArgs
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.sample.ui.util.toVeryShortString
import com.github.panpf.zoomimage.sample.ui.zoomimage.SettingsDialogFragment
import com.github.panpf.zoomimage.sample.util.lifecycleOwner
import kotlinx.coroutines.launch

class GlideZoomImageViewFragment : BindingFragment<GlideZoomImageViewFragmentBinding>() {

    private val args by navArgs<CoilZoomImageViewFragmentArgs>()

    override fun onViewCreated(
        binding: GlideZoomImageViewFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        binding.glideZoomImageViewImage.apply {
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

        binding.common.zoomImageViewTileMap.setZoomImageView(binding.glideZoomImageViewImage)

        binding.common.zoomImageViewRotate.setOnClickListener {
            binding.glideZoomImageViewImage.zoomAbility.rotateBy(90)
        }

        binding.common.zoomImageViewSettings.setOnClickListener {
            SettingsDialogFragment().show(childFragmentManager, null)
        }

        binding.common.zoomImageViewInfoText.apply {
            maxLines = 4
            setOnClickListener {
                maxLines = if (maxLines == 4) Int.MAX_VALUE else 4
            }
            binding.glideZoomImageViewImage.zoomAbility.addOnMatrixChangeListener {
                updateInfo(binding)
            }
            binding.glideZoomImageViewImage.zoomAbility.addOnScaleChangeListener { _, _, _ ->
                updateInfo(binding)
            }
        }

        loadImage(binding)
    }

    private fun loadImage(binding: GlideZoomImageViewFragmentBinding) {
        binding.common.zoomImageViewProgress.isVisible = true
        binding.common.zoomImageViewError.isVisible = false
        Glide.with(this@GlideZoomImageViewFragment)
            .load(args.imageUri.replace("asset://", "file:///android_asset/"))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.common.zoomImageViewProgress.isVisible = false
                    binding.common.zoomImageViewError.isVisible = true
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.common.zoomImageViewProgress.isVisible = false
                    binding.common.zoomImageViewError.isVisible = false
                    return false
                }
            })
            .into(binding.glideZoomImageViewImage)

        binding.common.zoomImageViewTileMap.displayImage(args.imageUri) {
            resizeSize(600, 600)
            resizePrecision(Precision.LESS_PIXELS)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateInfo(binding: GlideZoomImageViewFragmentBinding) {
        val zoomInfo = binding.glideZoomImageViewImage.zoomAbility.run {
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
        val imageInfo = binding.glideZoomImageViewImage.subsamplingAbility.run {
            val exifOrientationName = imageExifOrientation?.let { exifOrientationName(it) }
            """
                image: ${imageSize?.toShortString()}, '${imageMimeType}', $exifOrientationName
            """.trimIndent()
        }
        val subsamplingInfo = binding.glideZoomImageViewImage.subsamplingAbility.run {
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
        ): Fragment = GlideZoomImageViewFragment().apply {
            arguments = CoilZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}