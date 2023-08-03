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
package com.github.panpf.zoomimage.sample.ui.examples.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView.ScaleType
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.github.panpf.sketch.decode.internal.exifOrientationName
import com.github.panpf.tools4j.io.ktx.formatFileSize
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewCommonFragmentBinding
import com.github.panpf.zoomimage.sample.prefsService
import com.github.panpf.zoomimage.sample.ui.base.view.BindingFragment
import com.github.panpf.zoomimage.sample.ui.widget.view.ZoomImageMinimapView
import com.github.panpf.zoomimage.sample.util.collectWithLifecycle
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.sample.util.toVeryShortString
import com.github.panpf.zoomimage.toShortString
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseZoomImageViewFragment<VIEW_BINDING : ViewBinding> :
    BindingFragment<VIEW_BINDING>() {

    abstract val sketchImageUri: String

    private val linearScaleViewModel by viewModels<LinearScaleViewModel>()

    abstract fun getZoomImageView(binding: VIEW_BINDING): ZoomImageView

    abstract fun getCommonBinding(binding: VIEW_BINDING): ZoomImageViewCommonFragmentBinding

    override fun onViewCreated(binding: VIEW_BINDING, savedInstanceState: Bundle?) {
        val zoomImageView = getZoomImageView(binding)
        val common = getCommonBinding(binding)
        zoomImageView.apply {
            logger.level = if (BuildConfig.DEBUG) Logger.DEBUG else Logger.INFO
            prefsService.scaleType.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                scaleType = ScaleType.valueOf(it)
            }
            zoomAbility.apply {
                prefsService.threeStepScale.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    threeStepScale = it
                }
                prefsService.rubberBandScale.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    rubberBandScale = it
                }
                prefsService.scrollBarEnabled.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    scrollBarSpec = if (it) ScrollBarSpec.Default else null
                }
                prefsService.readModeEnabled.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    val direction = if (prefsService.readModeDirectionBoth.value) {
                        ReadMode.Direction.Both
                    } else if (prefsService.horizontalPagerLayout.value) {
                        ReadMode.Direction.OnlyVertical
                    } else {
                        ReadMode.Direction.OnlyHorizontal
                    }
                    readMode =
                        if (prefsService.readModeEnabled.value) ReadMode(direction = direction) else null
                }
                prefsService.readModeDirectionBoth.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    val direction = if (prefsService.readModeDirectionBoth.value) {
                        ReadMode.Direction.Both
                    } else if (prefsService.horizontalPagerLayout.value) {
                        ReadMode.Direction.OnlyVertical
                    } else {
                        ReadMode.Direction.OnlyHorizontal
                    }
                    readMode =
                        if (prefsService.readModeEnabled.value) ReadMode(direction = direction) else null
                }
                prefsService.animateScale.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    val durationMillis = if (prefsService.animateScale.value) {
                        (if (prefsService.slowerScaleAnimation.value) 3000 else 300)
                    } else {
                        0
                    }
                    animationSpec = ZoomAnimationSpec.Default.copy(durationMillis = durationMillis)
                }
                prefsService.slowerScaleAnimation.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    val durationMillis = if (prefsService.animateScale.value) {
                        (if (prefsService.slowerScaleAnimation.value) 3000 else 300)
                    } else {
                        0
                    }
                    animationSpec = ZoomAnimationSpec.Default.copy(durationMillis = durationMillis)
                }
            }
            subsamplingAbility.apply {
                setLifecycle(viewLifecycleOwner.lifecycle)
                prefsService.showTileBounds.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    showTileBounds = it
                }
            }
        }

        common.zoomImageViewErrorRetryButton.setOnClickListener {
            loadData(binding, common, sketchImageUri)
        }

        common.zoomImageViewTileMap.setZoomImageView(zoomImageView)

        common.zoomImageViewRotate.setOnClickListener {
            zoomImageView.zoomAbility.rotation(zoomImageView.zoomAbility.rotation + 90)
        }

        common.zoomImageViewZoom.apply {
            setOnClickListener {
                // todo 这里应该用 stitchScale，参考 ZoomableState
                val nextStepScale = zoomImageView.zoomAbility.getNextStepScale()
                zoomImageView.zoomAbility.scale(nextStepScale, animate = true)
            }
            val resetIcon = {
                val zoomIn =
                    zoomImageView.zoomAbility.getNextStepScale() > zoomImageView.zoomAbility.minScale
                if (zoomIn) {
                    setImageResource(R.drawable.ic_zoom_in)
                } else {
                    setImageResource(R.drawable.ic_zoom_out)
                }
            }
            zoomImageView.zoomAbility.addOnMatrixChangeListener {
                resetIcon()
            }
            resetIcon()
        }

        common.zoomImageViewInfo.setOnClickListener {
            ZoomImageViewInfoDialogFragment().apply {
                arguments = buildOtherInfo(zoomImageView, sketchImageUri).toBundle()
            }.show(childFragmentManager, null)
        }
        zoomImageView.zoomAbility.addOnViewLongPressListener { _, _, _ ->
            ZoomImageViewInfoDialogFragment().apply {
                arguments = buildOtherInfo(zoomImageView, sketchImageUri).toBundle()
            }.show(childFragmentManager, null)
        }

        common.zoomImageViewLinearScale.apply {
            setOnClickListener {
                ZoomImageViewLinearScaleDialogFragment().apply {
                    arguments = ZoomImageViewLinearScaleDialogFragmentArgs(
                        valueFrom = zoomImageView.zoomAbility.minScale,
                        valueTo = zoomImageView.zoomAbility.maxScale,
                        value = zoomImageView.zoomAbility.scale.scaleX,
                    ).toBundle()
                }.show(childFragmentManager, null)
            }
            viewLifecycleOwner.lifecycleScope.launch {
                linearScaleViewModel.changeFlow.collectLatest {
                    zoomImageView.zoomAbility.scale(it, animate = true)
                }
            }
        }

        zoomImageView.zoomAbility.addOnMatrixChangeListener {
            updateInfo(zoomImageView, common)
        }
        updateInfo(zoomImageView, common)

        loadData(binding, common, sketchImageUri)
    }

    protected fun loadData(
        binding: VIEW_BINDING,
        common: ZoomImageViewCommonFragmentBinding,
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
        loadMinimap(common.zoomImageViewTileMap, sketchImageUri)
    }

    abstract fun loadImage(
        binding: VIEW_BINDING,
        onCallStart: () -> Unit,
        onCallSuccess: () -> Unit,
        onCallError: () -> Unit
    )

    abstract fun loadMinimap(
        zoomImageMinimapView: ZoomImageMinimapView,
        sketchImageUri: String
    )

    @SuppressLint("SetTextI18n")
    private fun updateInfo(
        zoomImageView: ZoomImageView,
        common: ZoomImageViewCommonFragmentBinding
    ) {
        common.zoomImageViewInfoHeaderText.text = """
                scale: 
                offset: 
                visible: 
            """.trimIndent()
        common.zoomImageViewInfoContentText.text = zoomImageView.zoomAbility.run {
            val scales = floatArrayOf(minScale, mediumScale, maxScale)
                .joinToString(prefix = "[", postfix = "]") { it.format(2).toString() }
            val offsetCompat = offset.round()
            """
                ${scale.scaleX.format(2)}(${baseScale.scaleX.format(2)}*${userScale.format(2)}) in $scales
                ${offsetCompat.toShortString()}; edge=${scrollEdge.toShortString()}
                ${getVisibleRect().toVeryShortString()}
            """.trimIndent()
        }
    }

    private fun buildOtherInfo(
        zoomImageView: ZoomImageView,
        sketchImageUri: String
    ): ZoomImageViewInfoDialogFragmentArgs {
        val zoomAbility = zoomImageView.zoomAbility
        val subsamplingAbility = zoomImageView.subsamplingAbility
        val tileList = subsamplingAbility.tileList ?: emptyList()
        val tilesByteCount = tileList.sumOf { it.bitmap?.byteCount ?: 0 }.toLong().formatFileSize()
        val exifOrientationName =
            subsamplingAbility.imageInfo?.exifOrientation?.let { exifOrientationName(it) }
        return ZoomImageViewInfoDialogFragmentArgs(
            imageUri = sketchImageUri,
            imageInfo = """
                size=${subsamplingAbility.imageInfo?.size?.toShortString()}
                mimeType=${subsamplingAbility.imageInfo?.mimeType}
                exifOrientation=$exifOrientationName
            """.trimIndent(),
            sizeInfo = """
                view=${zoomAbility.viewSize.toShortString()}
                drawable=${zoomAbility.drawableSize.toShortString()}
            """.trimIndent(),
            tilesInfo = """
                tileCount=${tileList.size}
                tilesBytes=${tilesByteCount}
                loadedTileCount=${tileList.count { it.bitmap != null }}
            """.trimIndent()
        )
    }
}

