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
import android.os.Bundle
import android.view.animation.Animation
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.github.panpf.tools4a.view.ktx.animTranslate
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewCommonFragmentBinding
import com.github.panpf.zoomimage.sample.settingsService
import com.github.panpf.zoomimage.sample.ui.base.view.BindingFragment
import com.github.panpf.zoomimage.sample.ui.widget.view.ZoomImageMinimapView
import com.github.panpf.zoomimage.sample.util.collectWithLifecycle
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.valueOf
import kotlin.math.roundToInt

abstract class BaseZoomImageViewFragment<VIEW_BINDING : ViewBinding> :
    BindingFragment<VIEW_BINDING>() {

    abstract val sketchImageUri: String

    abstract fun getZoomImageView(binding: VIEW_BINDING): ZoomImageView

    abstract fun getCommonBinding(binding: VIEW_BINDING): ZoomImageViewCommonFragmentBinding

    override fun onViewCreated(binding: VIEW_BINDING, savedInstanceState: Bundle?) {
        val zoomImageView = getZoomImageView(binding)
        val common = getCommonBinding(binding)
        zoomImageView.apply {
            logger.level = if (BuildConfig.DEBUG) Logger.DEBUG else Logger.INFO
            settingsService.scrollBarEnabled.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                scrollBar = if (it) ScrollBarSpec.Default else null
            }
            zoomable.apply {
                settingsService.contentScale.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    contentScale = ContentScaleCompat.valueOf(it)
                }
                settingsService.alignment.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    alignment = AlignmentCompat.valueOf(it)
                }
                settingsService.threeStepScale.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    threeStepScale = it
                }
                settingsService.rubberBandScale.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    rubberBandScale = it
                }
                settingsService.scalesMultiple.stateFlow
                    .collectWithLifecycle(viewLifecycleOwner) {
                        val scalesMultiple = settingsService.scalesMultiple.value.toFloat()
                        val scalesCalculatorName = settingsService.scalesCalculator.value
                        scalesCalculator = if (scalesCalculatorName == "Dynamic") {
                            ScalesCalculator.dynamic(scalesMultiple)
                        } else {
                            ScalesCalculator.fixed(scalesMultiple)
                        }
                    }
                settingsService.scalesCalculator.stateFlow
                    .collectWithLifecycle(viewLifecycleOwner) {
                        val scalesMultiple = settingsService.scalesMultiple.value.toFloat()
                        val scalesCalculatorName = settingsService.scalesCalculator.value
                        scalesCalculator = if (scalesCalculatorName == "Dynamic") {
                            ScalesCalculator.dynamic(scalesMultiple)
                        } else {
                            ScalesCalculator.fixed(scalesMultiple)
                        }
                    }
                settingsService.limitOffsetWithinBaseVisibleRect.stateFlow
                    .collectWithLifecycle(viewLifecycleOwner) {
                        limitOffsetWithinBaseVisibleRect = it
                    }
                settingsService.readModeEnabled.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    val sizeType = if (settingsService.readModeAcceptedBoth.value) {
                        ReadMode.SIZE_TYPE_HORIZONTAL or ReadMode.SIZE_TYPE_VERTICAL
                    } else if (settingsService.horizontalPagerLayout.value) {
                        ReadMode.SIZE_TYPE_HORIZONTAL
                    } else {
                        ReadMode.SIZE_TYPE_VERTICAL
                    }
                    readMode =
                        if (settingsService.readModeEnabled.value) ReadMode(sizeType = sizeType) else null
                }
                settingsService.readModeAcceptedBoth.stateFlow.collectWithLifecycle(
                    viewLifecycleOwner
                ) {
                    val sizeType = if (settingsService.readModeAcceptedBoth.value) {
                        ReadMode.SIZE_TYPE_HORIZONTAL or ReadMode.SIZE_TYPE_VERTICAL
                    } else if (settingsService.horizontalPagerLayout.value) {
                        ReadMode.SIZE_TYPE_VERTICAL
                    } else {
                        ReadMode.SIZE_TYPE_HORIZONTAL
                    }
                    readMode =
                        if (settingsService.readModeEnabled.value) ReadMode(sizeType = sizeType) else null
                }
                settingsService.animateScale.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    val durationMillis = if (settingsService.animateScale.value) {
                        (if (settingsService.slowerScaleAnimation.value) 3000 else 300)
                    } else {
                        0
                    }
                    animationSpec = ZoomAnimationSpec.Default.copy(durationMillis = durationMillis)
                }
                settingsService.slowerScaleAnimation.stateFlow.collectWithLifecycle(
                    viewLifecycleOwner
                ) {
                    val durationMillis = if (settingsService.animateScale.value) {
                        (if (settingsService.slowerScaleAnimation.value) 3000 else 300)
                    } else {
                        0
                    }
                    animationSpec = ZoomAnimationSpec.Default.copy(durationMillis = durationMillis)
                }
            }
            subsampling.apply {
                settingsService.showTileBounds.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    showTileBounds = it
                }
                settingsService.tileAnimation.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    tileAnimationSpec =
                        if (it) TileAnimationSpec.Default else TileAnimationSpec.None
                }
                settingsService.pausedContinuousTransformType.stateFlow.collectWithLifecycle(
                    viewLifecycleOwner
                ) {
                    pausedContinuousTransformType = it.toInt()
                }
                settingsService.disabledBackgroundTiles.stateFlow.collectWithLifecycle(
                    viewLifecycleOwner
                ) {
                    disabledBackgroundTiles = it
                }
            }
        }

        common.zoomImageViewErrorRetryButton.setOnClickListener {
            loadData(binding, common, sketchImageUri)
        }

        common.zoomImageViewTileMap.setZoomImageView(zoomImageView)

        common.zoomImageViewRotate.setOnClickListener {
            zoomImageView.zoomable.rotate(zoomImageView.zoomable.transform.rotation.roundToInt() + 90)
        }

        common.zoomImageViewZoom.apply {
            setOnClickListener {
                val nextStepScale = zoomImageView.zoomable.getNextStepScale()
                zoomImageView.zoomable.scale(nextStepScale, animated = true)
            }
            val resetIcon = {
                val zoomIn =
                    zoomImageView.zoomable.getNextStepScale() > zoomImageView.zoomable.transform.scaleX
                if (zoomIn) {
                    setImageResource(R.drawable.ic_zoom_in)
                } else {
                    setImageResource(R.drawable.ic_zoom_out)
                }
            }
            zoomImageView.zoomable.registerOnTransformChangeListener {
                resetIcon()
            }
            resetIcon()
        }

        common.zoomImageViewInfo.setOnClickListener {
            ZoomImageViewInfoDialogFragment().apply {
                arguments = ZoomImageViewInfoDialogFragment
                    .buildArgs(zoomImageView, sketchImageUri)
                    .toBundle()
            }.show(childFragmentManager, null)
        }
        zoomImageView.setOnLongClickListener {
            ZoomImageViewInfoDialogFragment().apply {
                arguments = ZoomImageViewInfoDialogFragment
                    .buildArgs(zoomImageView, sketchImageUri)
                    .toBundle()
            }.show(childFragmentManager, null)
            true
        }

        common.zoomImageViewLinearScaleSlider.apply {
            var changing = false
            val updateRange: () -> Unit = {
                val minScale = zoomImageView.zoomable.minScale
                val maxScale = zoomImageView.zoomable.maxScale
                val scale = zoomImageView.zoomable.transform.scaleX
                if (minScale < maxScale) {
                    valueFrom = minScale
                    valueTo = maxScale
                    val step = (valueTo - valueFrom) / 9
                    stepSize = step
                    changing = true
                    value = valueFrom + ((scale - valueFrom) / step).toInt() * step
                    changing = false
                }
            }
            zoomImageView.zoomable.registerOnResetListener {
                updateRange()
            }
            updateRange()

            val updateValue: () -> Unit = {
                val minScale = zoomImageView.zoomable.minScale
                val maxScale = zoomImageView.zoomable.maxScale
                val scale = zoomImageView.zoomable.transform.scaleX
                if (!changing && scale in minScale..maxScale && minScale < maxScale) {
                    val step = (valueTo - valueFrom) / 9
                    changing = true
                    value = valueFrom + ((scale - valueFrom) / step).toInt() * step
                    changing = false
                }
            }
            zoomImageView.zoomable.registerOnTransformChangeListener {
                updateValue()
            }

            updateValue()
            addOnChangeListener { _, value, _ ->
                if (!changing) {
                    zoomImageView.zoomable.scale(targetScale = value, animated = true)
                }
            }
        }

        common.zoomImageViewMoveKeyboard.moveFlow.collectWithLifecycle(viewLifecycleOwner) {
            val offset = zoomImageView.zoomable.transform.offset
            zoomImageView.zoomable.offset(offset + it * -1f)
        }

        common.zoomImageViewMore.apply {
            common.zoomImageViewExtraLayout.isVisible = false

            setOnClickListener {
                if (zoomImageView.zoomable.minScale >= zoomImageView.zoomable.maxScale) {
                    return@setOnClickListener
                }
                if (common.zoomImageViewExtraLayout.isVisible) {
                    common.zoomImageViewExtraLayout.animTranslate(
                        fromXDelta = 0f,
                        toXDelta = common.zoomImageViewExtraLayout.width.toFloat(),
                        fromYDelta = 0f,
                        toYDelta = 0f,
                        listener = object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {
                            }

                            override fun onAnimationEnd(animation: Animation?) {
                                common.zoomImageViewExtraLayout.isVisible = false
                            }

                            override fun onAnimationRepeat(animation: Animation?) {
                            }
                        }
                    )
                } else {
                    common.zoomImageViewExtraLayout.isVisible = true
                    common.zoomImageViewExtraLayout.animTranslate(
                        fromXDelta = common.zoomImageViewExtraLayout.width.toFloat(),
                        toXDelta = 0f,
                        fromYDelta = 0f,
                        toYDelta = 0f,
                    )
                }
            }
        }

        common.zoomImageViewZoomOut.setOnClickListener {
            zoomImageView.zoomable.scale(
                targetScale = zoomImageView.zoomable.transform.scaleX - 0.5f,
                animated = true
            )
        }

        common.zoomImageViewZoomIn.setOnClickListener {
            zoomImageView.zoomable.scale(
                targetScale = zoomImageView.zoomable.transform.scaleX + 0.5f,
                animated = true
            )
        }

        zoomImageView.zoomable.registerOnTransformChangeListener {
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
                rotation: 
            """.trimIndent()
        common.zoomImageViewInfoContentText.text = zoomImageView.zoomable.run {
            """
                ${transform.scale.toShortString()}
                ${transform.offset.toShortString()}
                ${transform.rotation.roundToInt()}
            """.trimIndent()
        }
    }
}