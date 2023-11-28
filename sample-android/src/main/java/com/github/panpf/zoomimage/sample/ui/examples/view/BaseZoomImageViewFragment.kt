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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.github.panpf.tools4a.toast.ktx.showShortToast
import com.github.panpf.tools4a.view.ktx.animTranslate
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.databinding.ZoomImageViewCommonFragmentBinding
import com.github.panpf.zoomimage.sample.settingsService
import com.github.panpf.zoomimage.sample.ui.base.view.BindingFragment
import com.github.panpf.zoomimage.sample.ui.util.collectWithLifecycle
import com.github.panpf.zoomimage.sample.ui.util.repeatCollectWithLifecycle
import com.github.panpf.zoomimage.sample.ui.widget.view.ZoomImageMinimapView
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.view.zoom.OnViewLongPressListener
import com.github.panpf.zoomimage.view.zoom.OnViewTapListener
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.valueOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.github.panpf.zoomimage.sample.common.R as CommonR

abstract class BaseZoomImageViewFragment<VIEW_BINDING : ViewBinding> :
    BindingFragment<VIEW_BINDING>() {

    abstract val sketchImageUri: String

    abstract fun getZoomImageView(binding: VIEW_BINDING): ZoomImageView

    abstract fun getCommonBinding(binding: VIEW_BINDING): ZoomImageViewCommonFragmentBinding

    override fun onViewCreated(binding: VIEW_BINDING, savedInstanceState: Bundle?) {
        val zoomImageView = getZoomImageView(binding)
        val common = getCommonBinding(binding)
        zoomImageView.apply {
            onViewTapListener = OnViewTapListener { _, offset ->
                showShortToast("Click (${offset.toShortString()})")
            }
            onViewLongPressListener = OnViewLongPressListener { _, offset ->
                showShortToast("Long click (${offset.toShortString()})")
            }
            settingsService.logLevel.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                logger.level = Logger.level(it)
            }
            settingsService.scrollBarEnabled.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                scrollBar = if (it) ScrollBarSpec.Default else null
            }
            zoomable.apply {
                settingsService.contentScale.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    contentScaleState.value = ContentScaleCompat.valueOf(it)
                }
                settingsService.alignment.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    alignmentState.value = AlignmentCompat.valueOf(it)
                }
                settingsService.threeStepScale.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    threeStepScaleState.value = it
                }
                settingsService.rubberBandScale.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    rubberBandScaleState.value = it
                }
                settingsService.scalesMultiple.stateFlow
                    .collectWithLifecycle(viewLifecycleOwner) {
                        val scalesMultiple = settingsService.scalesMultiple.value.toFloat()
                        val scalesCalculatorName = settingsService.scalesCalculator.value
                        scalesCalculatorState.value = if (scalesCalculatorName == "Dynamic") {
                            ScalesCalculator.dynamic(scalesMultiple)
                        } else {
                            ScalesCalculator.fixed(scalesMultiple)
                        }
                    }
                settingsService.scalesCalculator.stateFlow
                    .collectWithLifecycle(viewLifecycleOwner) {
                        val scalesMultiple = settingsService.scalesMultiple.value.toFloat()
                        val scalesCalculatorName = settingsService.scalesCalculator.value
                        scalesCalculatorState.value = if (scalesCalculatorName == "Dynamic") {
                            ScalesCalculator.dynamic(scalesMultiple)
                        } else {
                            ScalesCalculator.fixed(scalesMultiple)
                        }
                    }
                settingsService.limitOffsetWithinBaseVisibleRect.stateFlow
                    .collectWithLifecycle(viewLifecycleOwner) {
                        limitOffsetWithinBaseVisibleRectState.value = it
                    }
                settingsService.readModeEnabled.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    val sizeType = if (settingsService.readModeAcceptedBoth.value) {
                        ReadMode.SIZE_TYPE_HORIZONTAL or ReadMode.SIZE_TYPE_VERTICAL
                    } else if (settingsService.horizontalPagerLayout.value) {
                        ReadMode.SIZE_TYPE_HORIZONTAL
                    } else {
                        ReadMode.SIZE_TYPE_VERTICAL
                    }
                    readModeState.value =
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
                    readModeState.value =
                        if (settingsService.readModeEnabled.value) ReadMode(sizeType = sizeType) else null
                }
                settingsService.animateScale.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    val durationMillis = if (settingsService.animateScale.value) {
                        (if (settingsService.slowerScaleAnimation.value) 3000 else 300)
                    } else {
                        0
                    }
                    animationSpecState.value =
                        ZoomAnimationSpec.Default.copy(durationMillis = durationMillis)
                }
                settingsService.slowerScaleAnimation.stateFlow.collectWithLifecycle(
                    viewLifecycleOwner
                ) {
                    val durationMillis = if (settingsService.animateScale.value) {
                        (if (settingsService.slowerScaleAnimation.value) 3000 else 300)
                    } else {
                        0
                    }
                    animationSpecState.value =
                        ZoomAnimationSpec.Default.copy(durationMillis = durationMillis)
                }
                settingsService.disabledGestureType.stateFlow
                    .collectWithLifecycle(viewLifecycleOwner) {
                        disabledGestureTypeState.value = it.toInt()
                    }
            }
            subsampling.apply {
                settingsService.showTileBounds.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    showTileBoundsState.value = it
                }
                settingsService.tileAnimation.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    tileAnimationSpecState.value =
                        if (it) TileAnimationSpec.Default else TileAnimationSpec.None
                }
                settingsService.pausedContinuousTransformType.stateFlow.collectWithLifecycle(
                    viewLifecycleOwner
                ) {
                    pausedContinuousTransformTypeState.value = it.toInt()
                }
                settingsService.disabledBackgroundTiles.stateFlow.collectWithLifecycle(
                    viewLifecycleOwner
                ) {
                    disabledBackgroundTilesState.value = it
                }
            }
        }

        common.zoomImageViewErrorRetryButton.setOnClickListener {
            loadData(binding, common, sketchImageUri)
        }

        common.zoomImageViewTileMap.setZoomImageView(zoomImageView)

        common.zoomImageViewRotate.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                zoomImageView.zoomable.rotate(zoomImageView.zoomable.transformState.value.rotation.roundToInt() + 90)
            }
        }

        common.zoomImageViewZoom.apply {
            setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val nextStepScale = zoomImageView.zoomable.getNextStepScale()
                    zoomImageView.zoomable.scale(nextStepScale, animated = true)
                }
            }
            zoomImageView.zoomable.transformState
                .repeatCollectWithLifecycle(viewLifecycleOwner, Lifecycle.State.STARTED) {
                    val zoomIn =
                        zoomImageView.zoomable.getNextStepScale() > zoomImageView.zoomable.transformState.value.scaleX
                    if (zoomIn) {
                        setImageResource(CommonR.drawable.ic_zoom_in)
                    } else {
                        setImageResource(CommonR.drawable.ic_zoom_out)
                    }
                }
        }

        common.zoomImageViewInfo.setOnClickListener {
            ZoomImageViewInfoDialogFragment().apply {
                arguments = ZoomImageViewInfoDialogFragment
                    .buildArgs(zoomImageView, sketchImageUri)
                    .toBundle()
            }.show(childFragmentManager, null)
        }

        common.zoomImageViewLinearScaleSlider.apply {
            var changing = false
            listOf(
                zoomImageView.zoomable.minScaleState,
                zoomImageView.zoomable.maxScaleState
            ).merge()
                .repeatCollectWithLifecycle(viewLifecycleOwner, Lifecycle.State.STARTED) {
                    val minScale = zoomImageView.zoomable.minScaleState.value
                    val maxScale = zoomImageView.zoomable.maxScaleState.value
                    val scale = zoomImageView.zoomable.transformState.value.scaleX
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
            zoomImageView.zoomable.transformState
                .repeatCollectWithLifecycle(viewLifecycleOwner, Lifecycle.State.STARTED) {
                    val minScale = zoomImageView.zoomable.minScaleState.value
                    val maxScale = zoomImageView.zoomable.maxScaleState.value
                    val scale = it.scaleX
                    if (!changing && scale in minScale..maxScale && minScale < maxScale) {
                        val step = (valueTo - valueFrom) / 9
                        changing = true
                        value = valueFrom + ((scale - valueFrom) / step).toInt() * step
                        changing = false
                    }
                }
            addOnChangeListener { _, value, _ ->
                if (!changing) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        zoomImageView.zoomable.scale(targetScale = value, animated = true)
                    }
                }
            }
        }

        common.zoomImageViewMoveKeyboard.moveFlow.collectWithLifecycle(viewLifecycleOwner) {
            val offset = zoomImageView.zoomable.transformState.value.offset
            zoomImageView.zoomable.offset(offset + it * -1f)
        }

        common.zoomImageViewMore.apply {
            common.zoomImageViewExtraLayout.isVisible = false

            setOnClickListener {
                if (zoomImageView.zoomable.minScaleState.value >= zoomImageView.zoomable.maxScaleState.value) {
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
            viewLifecycleOwner.lifecycleScope.launch {
                val targetScale = zoomImageView.zoomable.transformState.value.scaleX - 0.5f
                zoomImageView.zoomable.scale(targetScale = targetScale, animated = true)
            }
        }

        common.zoomImageViewZoomIn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val targetScale = zoomImageView.zoomable.transformState.value.scaleX + 0.5f
                zoomImageView.zoomable.scale(targetScale = targetScale, animated = true)
            }
        }

        zoomImageView.zoomable.transformState
            .repeatCollectWithLifecycle(viewLifecycleOwner, Lifecycle.State.STARTED) {
                updateInfo(zoomImageView, common)
            }

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
        common.zoomImageViewInfoContentText.text = zoomImageView.zoomable.transformState.value.run {
            """
                ${scale.toShortString()}
                ${offset.toShortString()}
                ${rotation.roundToInt()}
            """.trimIndent()
        }
    }
}