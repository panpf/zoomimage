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
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.isNotEmpty
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
        binding.sketchZoomImageView.loadImage(ResourceImages.woodpile.uri)
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
    private var zoomableEngine: ZoomableEngine? = null
    private var coroutineScope: CoroutineScope? = null
    private var markScale: Float? = null
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
                with(this@OverlayView.reusableMatrix) {
                    reset()

                    postScale(
                        /* sx = */ transform.scaleX,
                        /* sy = */ transform.scaleY,
                        /* px = */ 0f,
                        /* py = */ 0f
                    )
                    postTranslate(
                        /* dx = */ transform.offsetX,
                        /* dy = */ transform.offsetY
                    )
                }
                invalidate()
            }
        }
        coroutineScope.launch {
            zoomableEngine.contentSizeState.collect { contentSize ->
                markScale = if (contentSize.isNotEmpty()) {
                    contentSize.width.toFloat() / 6010f
                } else {
                    null
                }
            }
        }
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        val markScale = this.markScale ?: return
        val markList = this.markList ?: return
        canvas.withMatrix(reusableMatrix) {
            markList.forEach { detection ->
                canvas.drawCircle(
                    detection.cxPx * markScale,
                    detection.cyPx * markScale,
                    detection.radiusPx * markScale,
                    paint
                )
            }
        }
    }
}