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
package com.github.panpf.zoomimage.sample.ui.view.zoomimage

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView.ScaleType
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.github.panpf.sketch.decode.internal.exifOrientationName
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.tools4j.io.ktx.formatFileSize
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.format
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.databinding.CommonZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.ui.view.base.BindingFragment
import com.github.panpf.zoomimage.sample.ui.view.util.toShortString
import com.github.panpf.zoomimage.sample.ui.view.util.toVeryShortString
import com.github.panpf.zoomimage.sample.ui.view.widget.TilesMapImageView
import com.github.panpf.zoomimage.sample.util.lifecycleOwner
import kotlinx.coroutines.launch

abstract class BaseZoomImageViewFragment<VIEW_BINDING : ViewBinding> :
    BindingFragment<VIEW_BINDING>() {

    abstract val sketchImageUri: String

    abstract fun getZoomImageView(binding: VIEW_BINDING): ZoomImageView

    abstract fun getCommonBinding(binding: VIEW_BINDING): CommonZoomImageViewFragmentBinding

    abstract val supportDisabledMemoryCache: Boolean

    abstract val supportIgnoreExifOrientation: Boolean

    abstract val supportDisallowReuseBitmap: Boolean

    override fun onViewCreated(binding: VIEW_BINDING, savedInstanceState: Bundle?) {
        val zoomImageView = getZoomImageView(binding)
        val common = getCommonBinding(binding)
        zoomImageView.apply {
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

        common.zoomImageViewErrorRetryButton.setOnClickListener {
            loadData(binding, common, sketchImageUri)
        }

        common.zoomImageViewTileMap.setZoomImageView(zoomImageView)

        common.zoomImageViewRotate.setOnClickListener {
            zoomImageView.zoomAbility.rotateBy(90)
        }

        common.zoomImageViewSettings.setOnClickListener {
            SettingsDialogFragment().apply {
                arguments = SettingsDialogFragmentArgs(
                    supportMemoryCache = supportDisabledMemoryCache,
                    supportIgnoreExifOrientation = supportIgnoreExifOrientation,
                    supportReuseBitmap = supportDisallowReuseBitmap
                ).toBundle()
            }.show(childFragmentManager, null)
        }

        common.zoomImageViewInfoLayout.apply {
            var isSingleLine = true
            common.zoomImageViewUriText.isSingleLine = isSingleLine
            common.zoomImageViewInfoText.maxLines = 4
            setOnClickListener {
                isSingleLine = !isSingleLine
                common.zoomImageViewUriText.isSingleLine = isSingleLine
                common.zoomImageViewInfoText.maxLines =
                    if (common.zoomImageViewInfoText.maxLines == 4) Int.MAX_VALUE else 4
            }
            zoomImageView.zoomAbility.addOnMatrixChangeListener {
                updateInfo(zoomImageView, common, sketchImageUri)
            }
            zoomImageView.zoomAbility.addOnScaleChangeListener { _, _, _ ->
                updateInfo(zoomImageView, common, sketchImageUri)
            }
        }

        loadData(binding, common, sketchImageUri)
        updateInfo(zoomImageView, common, sketchImageUri)
    }

    protected fun loadData(
        binding: VIEW_BINDING,
        common: CommonZoomImageViewFragmentBinding,
        sketchImageUri: String
    ) {
        loadImage(
            binding = binding,
            onCallStart = {
                common.zoomImageViewProgress.isVisible = true
                common.zoomImageViewErrorLayout.isVisible = false
            },
            onCallSuccess = {
                common.zoomImageViewProgress.isVisible = false
                common.zoomImageViewErrorLayout.isVisible = false
            },
            onCallError = {
                common.zoomImageViewProgress.isVisible = false
                common.zoomImageViewErrorLayout.isVisible = true
            },
        )
        loadTilesMapImage(common.zoomImageViewTileMap, sketchImageUri)
    }

    private fun loadTilesMapImage(tilesMapImageView: TilesMapImageView, sketchImageUri: String) {
        tilesMapImageView.displayImage(sketchImageUri) {
            lifecycle(viewLifecycleOwner.lifecycle)
            crossfade()
            resizeSize(600, 600)
            resizePrecision(Precision.LESS_PIXELS)
            if (supportIgnoreExifOrientation) {
                ignoreExifOrientation(prefsService.ignoreExifOrientation.value)
            }
        }
    }

    abstract fun loadImage(
        binding: VIEW_BINDING,
        onCallStart: () -> Unit,
        onCallSuccess: () -> Unit,
        onCallError: () -> Unit
    )

    @SuppressLint("SetTextI18n")
    private fun updateInfo(
        zoomImageView: ZoomImageView,
        common: CommonZoomImageViewFragmentBinding,
        sketchImageUri: String
    ) {
        common.zoomImageViewUriText.text = "uri: $sketchImageUri"
        val zoomInfo = zoomImageView.zoomAbility.run {
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
        val imageInfo = zoomImageView.subsamplingAbility.run {
            val exifOrientationName = imageExifOrientation?.let { exifOrientationName(it) }
            """
                image: ${imageSize?.toShortString()}, '${imageMimeType}', $exifOrientationName
            """.trimIndent()
        }
        val subsamplingInfo = zoomImageView.subsamplingAbility.run {
            val tileList = tileList ?: emptyList()
            val tilesByteCount = tileList.sumOf { it.bitmap?.byteCount ?: 0 }
                .toLong().formatFileSize()
            """
                tileCount=${tileList.size}
                validTileCount=${tileList.count { it.bitmap != null }}
                tilesByteCount=${tilesByteCount}
            """.trimIndent()
        }
        common.zoomImageViewInfoText.text = "$zoomInfo\n$imageInfo\n$subsamplingInfo"
    }
}