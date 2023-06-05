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

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.format
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.databinding.SketchZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.ui.base.BindingFragment
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.sample.ui.util.toVeryShortString
import com.github.panpf.zoomimage.sample.ui.zoomimage.SettingsEventViewModel

class SketchZoomImageViewFragment : BindingFragment<SketchZoomImageViewFragmentBinding>() {

    private val args by navArgs<SketchZoomImageViewFragmentArgs>()
    private val settingsEventViewModel by viewModels<SettingsEventViewModel>()

    override fun onViewCreated(
        binding: SketchZoomImageViewFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        binding.sketchZoomImageViewImage.apply {
            zoomAbility.logger.level = if (BuildConfig.DEBUG)
                Logger.Level.DEBUG else Logger.Level.INFO
            // todo settings
            settingsEventViewModel.observeZoomSettings(this)
        }

        binding.common.zoomImageViewErrorRetryButton.setOnClickListener {
            loadImage(binding)
        }

        binding.common.zoomImageViewTileMap.setZoomImageView(binding.sketchZoomImageViewImage)

        binding.common.zoomImageViewRotate.setOnClickListener {
            binding.sketchZoomImageViewImage.zoomAbility.rotateBy(90)
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
                    binding.common.zoomImageViewError.isVisible = false
                },
                onSuccess = { _, _ ->
                    binding.common.zoomImageViewProgress.isVisible = false
                    binding.common.zoomImageViewError.isVisible = false
                },
                onError = { _, _ ->
                    binding.common.zoomImageViewProgress.isVisible = false
                    binding.common.zoomImageViewError.isVisible = true
                },
            )
        }

        binding.common.zoomImageViewTileMap.displayImage(args.imageUri) {
            resizeSize(600, 600)
            resizePrecision(Precision.LESS_PIXELS)
        }
    }

    private fun updateInfo(binding: SketchZoomImageViewFragmentBinding) {
        val info = binding.sketchZoomImageViewImage.zoomAbility.run {
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

//            fun createDirectionsFromImageView(
//                imageView: ImageView,
//                uri: String?,
//            ): NavDirections {
//                var uri1: String? = uri
//                var optionsInfo: String? = null
//                var imageInfo: String? = null
//                var bitmapInfo: String? = null
//                var drawableInfo: String? = null
//                var dataFromInfo: String? = null
//                var transformedInfo: String? = null
//                var zoomInfo: String? = null
//                var tilesInfo: String? = null
//                var throwableString: String? = null
//                val displayResult = imageView.displayResult
//                if (displayResult is DisplayResult.Success) {
//                    val sketchDrawable = displayResult.drawable.findLastSketchDrawable()!!
//                    uri1 = sketchDrawable.imageUri
//                    imageInfo = sketchDrawable.imageInfo.run {
//                        "${width}x${height}, ${mimeType}, ${exifOrientationName(exifOrientation)}"
//                    }
//
//                    optionsInfo = sketchDrawable.requestKey
//                        .replace(sketchDrawable.imageUri, "")
//                        .let { if (it.startsWith("?")) it.substring(1) else it }
//                        .split("&")
//                        .joinToString(separator = "\n")
//
//                    bitmapInfo = displayResult.drawable.let {
//                        if (it is ResizeDrawable) it.drawable!! else it
//                    }.let {
//                        if (it is SketchCountBitmapDrawable) {
//                            "${it.bitmap.width}x${it.bitmap.height}, ${it.bitmap.config}, ${
//                                it.bitmap.byteCount.toLong().formatFileSize()
//                            }"
//                        } else {
//                            "${it.intrinsicWidth}x${it.intrinsicHeight}, $ARGB_8888, ${
//                                calculateBitmapByteCount(
//                                    it.intrinsicWidth,
//                                    it.intrinsicHeight,
//                                    ARGB_8888
//                                ).toLong().formatFileSize()
//                            }"
//                        }
//                    }
//
//                    drawableInfo = displayResult.drawable.let {
//                        "${it.intrinsicWidth}x${it.intrinsicHeight}"
//                    }
//
//                    dataFromInfo = sketchDrawable.dataFrom.name
//
//                    transformedInfo = sketchDrawable.transformedList
//                        ?.joinToString(separator = "\n") { transformed ->
//                            transformed.replace("Transformed", "")
//                        }
//                } else if (displayResult is DisplayResult.Error) {
//                    uri1 = displayResult.request.uriString
//
//                    throwableString = displayResult.throwable.toString()
//                }
//
//                if (imageView is ZoomImageView) {
//                    zoomInfo = buildList {
//                        add("view=${imageView.width}x${imageView.height}")
//                        add(
//                            "draw=${
//                                imageView.zoomAbility.getDrawRect().toRect()
//                            }"
//                        )
//                        add("visible=${imageView.zoomAbility.getVisibleRect() }")
//                        add(
//                            "nowScale=${imageView.zoomAbility.scale.format(2)}(${
//                                imageView.zoomAbility.baseScale.format(
//                                    2
//                                )
//                            },${
//                                imageView.zoomAbility.supportScale.format(2)
//                            })"
//                        )
//                        add("minScale=${imageView.zoomAbility.minScale.format(2)}")
//                        add("maxScale=${imageView.zoomAbility.maxScale.format(2)}")
//                        val stepScales = imageView.zoomAbility.stepScales
//                            .joinToString(prefix = "[", postfix = "]") { it.format(2) }
//                        add("stepScales=${stepScales}")
//                        add("rotateDegrees=${imageView.zoomAbility.rotateDegrees}")
//                        add(
//                            "horScroll(left/right)=${imageView.canScrollHorizontally(-1)},${
//                                imageView.canScrollHorizontally(1)
//                            }"
//                        )
//                        add(
//                            "verScroll(up/down)=${imageView.canScrollVertically(-1)},${
//                                imageView.canScrollVertically(1)
//                            }"
//                        )
//                        add("ScrollEdge(hor/ver)=${imageView.zoomAbility.horScrollEdge},${imageView.zoomAbility.verScrollEdge}")
//                    }.joinToString(separator = "\n")
//
//                    tilesInfo = imageView.subsamplingAbility.tileList?.takeIf { it.isNotEmpty() }?.let {
//                        buildList {
//                            add("tileCount=${it.size}")
//                            add("validTileCount=${it.count { it.bitmap != null }}")
//                            val tilesByteCount = it.sumOf { it.bitmap?.byteCount ?: 0 }
//                                .toLong().formatFileSize()
//                            add("tilesByteCount=${tilesByteCount}")
//                        }.joinToString(separator = "\n")
//                    }
//                }
//
//                return NavMainDirections.actionGlobalImageInfoDialogFragment(
//                    uri = uri1,
//                    imageInfo = imageInfo,
//                    bitmapInfo = bitmapInfo,
//                    drawableInfo = drawableInfo,
//                    optionsInfo = optionsInfo,
//                    dataFromInfo = dataFromInfo,
//                    transformedInfo = transformedInfo,
//                    zoomInfo = zoomInfo,
//                    tilesInfo = tilesInfo,
//                    throwableString = throwableString
//                )
//            }

        }
        binding.common.zoomImageViewInfoText.text = info
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