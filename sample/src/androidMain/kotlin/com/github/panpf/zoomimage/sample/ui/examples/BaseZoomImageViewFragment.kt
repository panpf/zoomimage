/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage.sample.ui.examples

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.github.panpf.tools4a.view.ktx.animTranslate
import com.github.panpf.tools4k.lang.asOrThrow
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.sample.R as CommonR
import com.github.panpf.zoomimage.sample.buildScalesCalculator
import com.github.panpf.zoomimage.sample.databinding.FragmentZoomViewBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment
import com.github.panpf.zoomimage.sample.ui.components.CaptureDialogFragment
import com.github.panpf.zoomimage.sample.ui.components.InfoItemsDialogFragment
import com.github.panpf.zoomimage.sample.ui.components.StateView
import com.github.panpf.zoomimage.sample.ui.components.ZoomImageMinimapView
import com.github.panpf.zoomimage.sample.ui.components.buildZoomImageViewInfos
import com.github.panpf.zoomimage.sample.ui.gallery.PhotoPaletteViewModel
import com.github.panpf.zoomimage.sample.ui.util.capture
import com.github.panpf.zoomimage.sample.ui.util.crop
import com.github.panpf.zoomimage.sample.ui.util.parentViewModel
import com.github.panpf.zoomimage.sample.ui.util.toAndroidRect
import com.github.panpf.zoomimage.sample.util.collectWithLifecycle
import com.github.panpf.zoomimage.sample.util.repeatCollectWithLifecycle
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.view.zoom.OnViewLongPressListener
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.ReadMode
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.math.roundToInt

abstract class BaseZoomImageViewFragment<ZOOM_VIEW : ZoomImageView> :
    BaseBindingFragment<FragmentZoomViewBinding>() {

    abstract val sketchImageUri: String

    private var zoomView: ZOOM_VIEW? = null
    private val photoPaletteViewModel by parentViewModel<PhotoPaletteViewModel>()
    private val captureViewModel by activityViewModel<CaptureViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screenMode = false
    }

    abstract fun createZoomImageView(context: Context): ZOOM_VIEW

    open fun onViewCreated(
        binding: FragmentZoomViewBinding,
        zoomView: ZOOM_VIEW,
        savedInstanceState: Bundle?
    ) {

    }

    override fun getStatusBarInsetsView(binding: FragmentZoomViewBinding): View {
        return binding.topBarInsetsLayout
    }

    override fun getNavigationBarInsetsView(binding: FragmentZoomViewBinding): View {
        return binding.bottomBarInsetsLayout
    }

    final override fun onViewCreated(
        binding: FragmentZoomViewBinding,
        savedInstanceState: Bundle?
    ) {
        val zoomImageView = createZoomImageView(binding.root.context)
        this.zoomView = zoomImageView
        binding.contentLayout.addView(
            zoomImageView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        zoomImageView.apply {
            appSettings.rtlLayoutDirectionEnabled.collectWithLifecycle(viewLifecycleOwner) {
                zoomable.setRtlLayoutDirection(it)
            }
            onViewLongPressListener = OnViewLongPressListener { _, _ ->
                InfoItemsDialogFragment().apply {
                    val infoItems = buildZoomImageViewInfos(zoomImageView, sketchImageUri)
                    arguments = InfoItemsDialogFragment.buildArgs(infoItems).toBundle()
                }.show(childFragmentManager, null)
            }
            appSettings.logLevel.collectWithLifecycle(viewLifecycleOwner) {
                logger.level = it
            }
            appSettings.scrollBarEnabled.collectWithLifecycle(viewLifecycleOwner) {
                scrollBar = if (it) ScrollBarSpec.Default else null
            }
            zoomable.apply {
                appSettings.contentScale.collectWithLifecycle(viewLifecycleOwner) {
                    setContentScale(it)
                }
                appSettings.alignment.collectWithLifecycle(viewLifecycleOwner) {
                    setAlignment(it)
                }
                appSettings.threeStepScaleEnabled.collectWithLifecycle(viewLifecycleOwner) {
                    setThreeStepScale(it)
                }
                appSettings.rubberBandScaleEnabled.collectWithLifecycle(viewLifecycleOwner) {
                    setRubberBandScale(it)
                }
//                appSettings.scalesCalculator
//                    .collectWithLifecycle(viewLifecycleOwner) {
//                        setScalesCalculator(it)
//                    }
                appSettings.scalesCalculatorName
                    .collectWithLifecycle(viewLifecycleOwner) {
                        val scalesCalculator = buildScalesCalculator(
                            appSettings.scalesCalculatorName.value,
                            appSettings.fixedScalesCalculatorMultiple.value.toFloat()
                        )
                        setScalesCalculator(scalesCalculator)
                    }
                appSettings.fixedScalesCalculatorMultiple
                    .collectWithLifecycle(viewLifecycleOwner) {
                        val scalesCalculator = buildScalesCalculator(
                            appSettings.scalesCalculatorName.value,
                            appSettings.fixedScalesCalculatorMultiple.value.toFloat()
                        )
                        setScalesCalculator(scalesCalculator)
                    }
                appSettings.limitOffsetWithinBaseVisibleRect
                    .collectWithLifecycle(viewLifecycleOwner) {
                        setLimitOffsetWithinBaseVisibleRect(it)
                    }
                appSettings.containerWhitespaceMultiple
                    .collectWithLifecycle(viewLifecycleOwner) {
                        setContainerWhitespaceMultiple(it)
                    }
                appSettings.containerWhitespaceEnabled
                    .collectWithLifecycle(viewLifecycleOwner) {
                        val containerWhitespace = if (it) {
                            ContainerWhitespace(
                                left = 100f,
                                top = 200f,
                                right = 300f,
                                bottom = 400f
                            )
                        } else {
                            ContainerWhitespace.Zero
                        }
                        setContainerWhitespace(containerWhitespace)
                    }
                appSettings.readModeEnabled.collectWithLifecycle(viewLifecycleOwner) {
                    val sizeType = if (appSettings.readModeAcceptedBoth.value) {
                        ReadMode.SIZE_TYPE_HORIZONTAL or ReadMode.SIZE_TYPE_VERTICAL
                    } else if (appSettings.horizontalPagerLayout.value) {
                        ReadMode.SIZE_TYPE_HORIZONTAL
                    } else {
                        ReadMode.SIZE_TYPE_VERTICAL
                    }
                    val readMode =
                        if (appSettings.readModeEnabled.value) ReadMode(sizeType = sizeType) else null
                    setReadMode(readMode)
                }
                appSettings.readModeAcceptedBoth.collectWithLifecycle(
                    viewLifecycleOwner
                ) {
                    val sizeType = if (appSettings.readModeAcceptedBoth.value) {
                        ReadMode.SIZE_TYPE_HORIZONTAL or ReadMode.SIZE_TYPE_VERTICAL
                    } else if (appSettings.horizontalPagerLayout.value) {
                        ReadMode.SIZE_TYPE_VERTICAL
                    } else {
                        ReadMode.SIZE_TYPE_HORIZONTAL
                    }
                    val readMode =
                        if (appSettings.readModeEnabled.value) ReadMode(sizeType = sizeType) else null
                    setReadMode(readMode)
                }
                appSettings.zoomAnimateEnabled.collectWithLifecycle(viewLifecycleOwner) {
                    val durationMillis = if (appSettings.zoomAnimateEnabled.value) {
                        (if (appSettings.zoomSlowerAnimationEnabled.value) 3000 else 300)
                    } else {
                        0
                    }
                    setAnimationSpec(ZoomAnimationSpec.Default.copy(durationMillis = durationMillis))
                }
                appSettings.zoomSlowerAnimationEnabled.collectWithLifecycle(
                    viewLifecycleOwner
                ) {
                    val durationMillis = if (appSettings.zoomAnimateEnabled.value) {
                        (if (appSettings.zoomSlowerAnimationEnabled.value) 3000 else 300)
                    } else {
                        0
                    }
                    setAnimationSpec(ZoomAnimationSpec.Default.copy(durationMillis = durationMillis))
                }
                appSettings.disabledGestureTypes.collectWithLifecycle(viewLifecycleOwner) {
                    setDisabledGestureTypes(it)
                }
                appSettings.keepTransformEnabled.collectWithLifecycle(
                    viewLifecycleOwner
                ) {
                    setKeepTransformWhenSameAspectRatioContentSizeChanged(it)
                }
            }
            subsampling.apply {
                appSettings.tileBoundsEnabled.collectWithLifecycle(viewLifecycleOwner) {
                    setShowTileBounds(it)
                }
                appSettings.tileAnimationEnabled.collectWithLifecycle(viewLifecycleOwner) {
                    val tileAnimationSpec =
                        if (it) TileAnimationSpec.Default else TileAnimationSpec.None
                    setTileAnimationSpec(tileAnimationSpec)
                }
                appSettings.tileMemoryCacheEnabled.collectWithLifecycle(viewLifecycleOwner) {
                    setDisabledTileImageCache(!it)
                }
                appSettings.pausedContinuousTransformTypes.collectWithLifecycle(viewLifecycleOwner) {
                    setPausedContinuousTransformTypes(it)
                }
                appSettings.backgroundTilesEnabled.collectWithLifecycle(viewLifecycleOwner) {
                    setDisabledBackgroundTiles(!it)
                }
                appSettings.subsamplingEnabled.collectWithLifecycle(viewLifecycleOwner) {
                    setDisabled(!it)
                }
                appSettings.autoStopWithLifecycleEnabled.collectWithLifecycle(viewLifecycleOwner) {
                    setDisabledAutoStopWithLifecycle(!it)
                }
            }
        }

        binding.minimapView.setZoomImageView(zoomImageView)

        binding.rotate.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                zoomImageView.zoomable.rotateBy(90)
            }
        }

        binding.zoom.apply {
            setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val nextStepScale = zoomImageView.zoomable.getNextStepScale()
                    zoomImageView.zoomable.scale(nextStepScale, animated = true)
                }
            }
            zoomImageView.zoomable.transformState
                .repeatCollectWithLifecycle(viewLifecycleOwner, Lifecycle.State.CREATED) {
                    val zoomIn =
                        zoomImageView.zoomable.getNextStepScale() > zoomImageView.zoomable.transformState.value.scaleX
                    if (zoomIn) {
                        setImageResource(CommonR.drawable.ic_zoom_in)
                    } else {
                        setImageResource(CommonR.drawable.ic_zoom_out)
                    }
                }
        }

        binding.capture.setOnClickListener {
            val bitmap = zoomImageView.capture()
            val cropRect = zoomImageView.zoomable.contentDisplayRectState.value
                .limitTo(zoomImageView.zoomable.containerSizeState.value)
            val croppedBitmap = bitmap.crop(cropRect.toAndroidRect())
            captureViewModel.capturedBitmap = croppedBitmap
            CaptureDialogFragment().show(childFragmentManager, null)
        }

        binding.info.setOnClickListener {
            InfoItemsDialogFragment().apply {
                val infoItems = buildZoomImageViewInfos(zoomImageView, sketchImageUri)
                arguments = InfoItemsDialogFragment.buildArgs(infoItems).toBundle()
            }.show(childFragmentManager, null)
        }

        binding.linearScaleSlider.apply {
            var changing = false
            listOf(
                zoomImageView.zoomable.minScaleState,
                zoomImageView.zoomable.maxScaleState
            ).merge()
                .repeatCollectWithLifecycle(viewLifecycleOwner, Lifecycle.State.CREATED) {
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
                .repeatCollectWithLifecycle(viewLifecycleOwner, Lifecycle.State.CREATED) {
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

        binding.moveKeyboard.moveFlow.collectWithLifecycle(viewLifecycleOwner) {
            zoomImageView.zoomable.offsetBy(it * -1f)
        }

        binding.more.apply {
            binding.extraLayout.isVisible = false

            setOnClickListener {
                if (zoomImageView.zoomable.minScaleState.value >= zoomImageView.zoomable.maxScaleState.value) {
                    return@setOnClickListener
                }
                if (binding.extraLayout.isVisible) {
                    binding.extraLayout.animTranslate(
                        fromXDelta = 0f,
                        toXDelta = binding.extraLayout.width.toFloat(),
                        fromYDelta = 0f,
                        toYDelta = 0f,
                        listener = object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {
                            }

                            override fun onAnimationEnd(animation: Animation?) {
                                binding.extraLayout.isVisible = false
                            }

                            override fun onAnimationRepeat(animation: Animation?) {
                            }
                        }
                    )
                } else {
                    binding.extraLayout.isVisible = true
                    binding.extraLayout.animTranslate(
                        fromXDelta = binding.extraLayout.width.toFloat(),
                        toXDelta = 0f,
                        fromYDelta = 0f,
                        toYDelta = 0f,
                    )
                }
            }
        }

        binding.zoomOut.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                zoomImageView.zoomable.scaleBy(addScale = 0.67f, animated = true)
            }
        }

        binding.zoomIn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                zoomImageView.zoomable.scaleBy(addScale = 1.5f, animated = true)
            }
        }

        zoomImageView.zoomable.transformState
            .repeatCollectWithLifecycle(viewLifecycleOwner, Lifecycle.State.CREATED) {
                updateInfo(zoomImageView, binding)
            }

        photoPaletteViewModel.photoPaletteState.repeatCollectWithLifecycle(
            owner = viewLifecycleOwner,
            state = Lifecycle.State.CREATED
        ) { photoPalette ->
            listOf(
                binding.zoomOut,
                binding.zoomIn,
                binding.bottomToolbar
            ).forEach {
                it.background.asOrThrow<GradientDrawable>().setColor(photoPalette.containerColorInt)
            }
            zoomImageView.scrollBar = if (appSettings.scrollBarEnabled.value) {
                ScrollBarSpec.Default.copy(photoPalette.containerColorInt)
            } else {
                null
            }
            binding.linearScaleSlider.thumbTintList =
                ColorStateList.valueOf(photoPalette.accentColorInt)
            binding.linearScaleSlider.trackTintList =
                ColorStateList.valueOf(photoPalette.containerColorInt)
            binding.linearScaleSlider.tickTintList =
                ColorStateList.valueOf(photoPalette.contentColorInt)
            binding.moveKeyboard.thumbView.drawable.setTint(photoPalette.containerColorInt)
        }

        loadData()
    }

    protected fun loadData() {
        loadData(binding!!, zoomView!!, sketchImageUri)
    }

    private fun loadData(
        binding: FragmentZoomViewBinding,
        zoomView: ZOOM_VIEW,
        sketchImageUri: String
    ) {
        loadImage(zoomView, binding.stateView)
        loadMinimap(binding.minimapView, sketchImageUri)
    }

    abstract fun loadImage(zoomView: ZOOM_VIEW, stateView: StateView)

    abstract fun loadMinimap(
        minimapView: ZoomImageMinimapView,
        sketchImageUri: String
    )

    @SuppressLint("SetTextI18n")
    private fun updateInfo(
        zoomImageView: ZoomImageView,
        binding: FragmentZoomViewBinding
    ) {
        binding.infoHeaderText.text = """
                scale: 
                offset: 
                rotation: 
            """.trimIndent()
        binding.infoContentText.text =
            zoomImageView.zoomable.transformState.value.run {
                """
                ${scale.toShortString()}
                ${offset.toShortString()}
                ${rotation.roundToInt()}
            """.trimIndent()
            }
    }

    override fun onDestroy() {
        captureViewModel.capturedBitmap = null
        super.onDestroy()
    }
}