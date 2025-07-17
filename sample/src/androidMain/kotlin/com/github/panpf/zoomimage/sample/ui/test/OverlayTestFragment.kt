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
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.withMatrix
import androidx.lifecycle.coroutineScope
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.loadImage
import com.github.panpf.tools4a.dimen.ktx.dp2pxF
import com.github.panpf.zoomimage.sample.databinding.FragmentOverlayTestBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseToolbarBindingFragment
import com.github.panpf.zoomimage.subsampling.internal.calculateThumbnailToOriginScaleFactor
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.view.util.applyOriginToThumbnailScale
import com.github.panpf.zoomimage.view.util.applyTransform
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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

        viewLifecycleOwner.lifecycle.coroutineScope.launch {
            overlayTestViewModel.marks.collect {
                binding.overlayView.setMarkList(it)
            }
        }
    }
}

class OverlayView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val reusableMatrix = Matrix()
    private val reusableMatrix2 = Matrix()
    private var zoomableEngine: ZoomableEngine? = null
    private var coroutineScope: CoroutineScope? = null
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        this.strokeWidth = 2.dp2pxF
        isAntiAlias = true
        alpha = 125
    }
    private var markList: List<Mark>? = null

    fun setZoomableEngine(zoomableEngine: ZoomableEngine) {
        this.zoomableEngine = zoomableEngine
        bindZoomableEngine()
    }

    fun setMarkList(markList: List<Mark>?) {
        this.markList = markList
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
            zoomableEngine.transformState.collect { transform ->
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        val zoomableEngine = zoomableEngine ?: return
        val markList = this.markList?.takeIf { it.isNotEmpty() } ?: return
        val contentSize = zoomableEngine.contentSizeState.value
            .takeIf { it.isNotEmpty() } ?: return
        val containerSize = zoomableEngine.containerSizeState.value
            .takeIf { it.isNotEmpty() } ?: return
        val contentOriginSize = zoomableEngine.contentOriginSizeState.value
            .takeIf { it.isNotEmpty() } ?: return
        val transform = zoomableEngine.transformState.value
        val contentVisibleRect = zoomableEngine.contentVisibleRectState.value

        val thumbnailToOriginScaleFactor = calculateThumbnailToOriginScaleFactor(
            originImageSize = contentOriginSize,
            thumbnailImageSize = contentSize
        )
        val originVisibleRect = contentVisibleRect.times(thumbnailToOriginScaleFactor)

        canvas.withMatrix(
            matrix = reusableMatrix.applyTransform(transform, containerSize)
        ) {
            canvas.withMatrix(
                matrix = reusableMatrix2.applyOriginToThumbnailScale(
                    originImageSize = contentOriginSize,
                    thumbnailImageSize = contentSize,
                )
            ) {
                markList.forEach { mark ->
                    val left = mark.radiusPx - mark.cxPx
                    val top = mark.radiusPx - mark.cyPx
                    val right = mark.radiusPx + mark.cxPx
                    val bottom = mark.radiusPx + mark.cyPx
                    if (left < originVisibleRect.right
                        && top < originVisibleRect.bottom
                        && right > originVisibleRect.left
                        && bottom > originVisibleRect.top
                    ) {
                        canvas.drawCircle(
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
}