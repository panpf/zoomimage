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

package com.github.panpf.zoomimage.sample.ui.test

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.coroutineScope
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.loadImage
import com.github.panpf.tools4a.dimen.ktx.dp2pxF
import com.github.panpf.zoomimage.sample.databinding.FragmentOverlayTestBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseToolbarBindingFragment
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.view.subsampling.internal.withZooming
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OverlayTestFragment : BaseToolbarBindingFragment<FragmentOverlayTestBinding>() {

    private val overlayTestViewModel: OverlayTestViewModel by viewModel()

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: FragmentOverlayTestBinding,
        savedInstanceState: Bundle?
    ) {
        toolbar.title = "Overlay"

        binding.sketchZoomImageView.logger.level = Logger.Level.Verbose
        binding.sketchZoomImageView.loadImage(ResourceImages.woodpile.uri) {
            size(500, 500)
        }
        binding.overlayView.setZoomableEngine(binding.sketchZoomImageView.zoomable)

        binding.rotate.setOnClickListener {
            viewLifecycleOwner.lifecycle.coroutineScope.launch {
                binding.sketchZoomImageView.zoomable.rotateBy(90)
            }
        }

        viewLifecycleOwner.lifecycle.coroutineScope.launch {
            overlayTestViewModel.rectMode.collect { rectMode ->
                binding.rectModeSwitch.isChecked = rectMode
                binding.overlayView.setRectMode(rectMode)
            }
        }

        viewLifecycleOwner.lifecycle.coroutineScope.launch {
            overlayTestViewModel.partitionMode.collect { rectMode ->
                binding.partitionModeSwitch.isChecked = rectMode
                binding.overlayView.setPartitionMode(rectMode)
            }
        }

        viewLifecycleOwner.lifecycle.coroutineScope.launch {
            overlayTestViewModel.marks.collect {
                binding.overlayView.setMarkList(it)
            }
        }

        binding.rectModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            overlayTestViewModel.setRectMode(isChecked)
        }

        binding.partitionModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            overlayTestViewModel.setPartitionMode(isChecked)
        }
    }
}

class OverlayView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val cacheMatrix = Matrix()
    private var zoomableEngine: ZoomableEngine? = null
    private var coroutineScope: CoroutineScope? = null
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        isAntiAlias = true
        alpha = 125
    }
    private var markList: List<Mark>? = null
    private var rectMode: Boolean = false
    private var partitionMode: Boolean = false

    fun setZoomableEngine(zoomableEngine: ZoomableEngine) {
        this.zoomableEngine = zoomableEngine
        bindZoomableEngine()
    }

    fun setMarkList(markList: List<Mark>?) {
        this.markList = markList
        invalidate()
    }

    fun setRectMode(rectMode: Boolean) {
        this.rectMode = rectMode
        invalidate()
    }

    fun setPartitionMode(partitionMode: Boolean) {
        this.partitionMode = partitionMode
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.coroutineScope = CoroutineScope(Dispatchers.Main)
        bindZoomableEngine()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope?.cancel()
        coroutineScope = null
    }

    private fun bindZoomableEngine() {
        val coroutineScope = coroutineScope ?: return
        val zoomableEngine = zoomableEngine ?: return
        coroutineScope.launch {
            combine(
                flows = listOf(
                    zoomableEngine.transformState,
                    zoomableEngine.contentOriginSizeState
                ),
                transform = { it }
            ).collect {
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val zoomableEngine = zoomableEngine ?: return
        if (zoomableEngine.containerSizeState.value.isEmpty()) return
        if (zoomableEngine.contentSizeState.value.isEmpty()) return
        if (zoomableEngine.contentOriginSizeState.value.isEmpty()) return
        val markList = this.markList?.takeIf { it.isNotEmpty() } ?: return

        if (partitionMode) {
            drawMarksWithPartitionMapping(canvas, zoomableEngine, markList)
        } else {
            drawMarksWithOverallMapping(canvas, zoomableEngine, markList)
        }
    }

    fun drawMarksWithOverallMapping(
        canvas: Canvas,
        zoomableEngine: ZoomableEngine,
        markList: List<Mark>
    ) {
        val sourceVisibleRect = zoomableEngine.sourceVisibleRectFState.value
            .takeIf { !it.isEmpty } ?: return
        canvas.withZooming(
            zoomableEngine = zoomableEngine,
            cacheMatrix = cacheMatrix,
            firstScaleByContentSize = true,
        ) {
            // Always keep the border looking width 2dp
            paint.strokeWidth = 2.dp2pxF / zoomableEngine.sourceScaleFactorState.value.scaleX
            markList.forEach { mark ->
                if (rectMode) {
                    val markRect = RectCompat(
                        center = OffsetCompat(x = mark.cxPx, y = mark.cyPx),
                        radius = mark.radiusPx
                    )
                    if (sourceVisibleRect.overlaps(other = markRect)) {
                        drawRect(
                            /* left = */ markRect.left,
                            /* top = */ markRect.top,
                            /* right = */ markRect.right,
                            /* bottom = */ markRect.bottom,
                            /* paint = */ paint,
                        )
                    }
                } else {
                    val markRect = RectCompat(
                        center = OffsetCompat(x = mark.cxPx, y = mark.cyPx),
                        radius = mark.radiusPx
                    )
                    if (sourceVisibleRect.overlaps(other = markRect)) {
                        drawCircle(
                            /* cx = */ mark.cxPx,
                            /* cy = */ mark.cyPx,
                            /* radius = */ mark.radiusPx,
                            /* paint = */ paint
                        )
                    }
                }
            }
        }
    }

    fun drawMarksWithPartitionMapping(
        canvas: Canvas,
        zoomableEngine: ZoomableEngine,
        markList: List<Mark>
    ) {
        val sourceVisibleRect = zoomableEngine.sourceVisibleRectFState.value
            .takeIf { !it.isEmpty } ?: return
        val sourceScaleFactor = zoomableEngine.sourceScaleFactorState.value
        paint.strokeWidth = 2.dp2pxF
        markList.forEach { mark ->
            val markRect = RectCompat(
                center = OffsetCompat(x = mark.cxPx, y = mark.cyPx),
                radius = mark.radiusPx
            )
            if (sourceVisibleRect.overlaps(other = markRect)) {
                if (rectMode) {
                    val drawRect: RectCompat = zoomableEngine.sourceToDraw(markRect)
                    canvas.drawRect(
                        /* left = */ drawRect.left,
                        /* top = */ drawRect.top,
                        /* right = */ drawRect.right,
                        /* bottom = */ drawRect.bottom,
                        /* paint = */ paint,
                    )
                } else {
                    val drawPoint: OffsetCompat = zoomableEngine.sourceToDraw(
                        point = OffsetCompat(x = mark.cxPx, y = mark.cyPx)
                    )
                    val drawRadius: Float = mark.radiusPx * sourceScaleFactor.scaleX
                    canvas.drawCircle(
                        /* cx = */ drawPoint.x,
                        /* cy = */ drawPoint.y,
                        /* radius = */ drawRadius,
                        /* paint = */ paint
                    )
                }
            }
        }
    }
}