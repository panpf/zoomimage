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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
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
import com.github.panpf.zoomimage.util.AlignmentCompat
import com.github.panpf.zoomimage.util.ContentScaleCompat
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.util.valueOf
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
            zoomAbility.apply {
                prefsService.contentScale.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    contentScale = ContentScaleCompat.valueOf(it)
                }
                prefsService.alignment.stateFlow.collectWithLifecycle(viewLifecycleOwner) {
                    alignment = AlignmentCompat.valueOf(it)
                }
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
            zoomImageView.zoomAbility.rotate(zoomImageView.zoomAbility.transform.rotation.roundToInt() + 90)
        }

        common.zoomImageViewZoom.apply {
            setOnClickListener {
                val nextStepScale = zoomImageView.zoomAbility.getNextStepScale()
                zoomImageView.zoomAbility.scale(nextStepScale, animated = true)
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
            zoomImageView.zoomAbility.registerOnTransformChangeListener {
                resetIcon()
            }
            resetIcon()
        }

        common.zoomImageViewInfo.setOnClickListener {
            ZoomImageViewInfoDialogFragment().apply {
                arguments = ZoomImageViewInfoDialogFragment.buildArgs(zoomImageView, sketchImageUri)
                    .toBundle()
            }.show(childFragmentManager, null)
        }
        zoomImageView.zoomAbility.registerOnViewLongPressListener { _, _, _ ->
            ZoomImageViewInfoDialogFragment().apply {
                arguments = ZoomImageViewInfoDialogFragment.buildArgs(zoomImageView, sketchImageUri)
                    .toBundle()
            }.show(childFragmentManager, null)
        }

        common.zoomImageViewLinearScale.apply {
            setOnClickListener {
                ZoomImageViewLinearScaleDialogFragment().apply {
                    arguments = ZoomImageViewLinearScaleDialogFragmentArgs(
                        valueFrom = zoomImageView.zoomAbility.minScale,
                        valueTo = zoomImageView.zoomAbility.maxScale,
                        value = zoomImageView.zoomAbility.transform.scaleX,
                    ).toBundle()
                }.show(childFragmentManager, null)
            }
            viewLifecycleOwner.lifecycleScope.launch {
                linearScaleViewModel.changeFlow.collectLatest {
                    zoomImageView.zoomAbility.scale(it, animated = true)
                }
            }
        }

        zoomImageView.zoomAbility.registerOnTransformChangeListener {
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
        common.zoomImageViewInfoContentText.text = zoomImageView.zoomAbility.run {
            """
                ${transform.scale.toShortString()}
                ${transform.offset.toShortString()}
                ${transform.rotation.roundToInt()}
            """.trimIndent()
        }
    }
}