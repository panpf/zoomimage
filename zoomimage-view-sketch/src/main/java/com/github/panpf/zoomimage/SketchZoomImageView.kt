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
package com.github.panpf.zoomimage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.ViewCompat
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.displayResult
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.ImageOptionsProvider
import com.github.panpf.sketch.request.Listener
import com.github.panpf.sketch.request.ProgressListener
import com.github.panpf.sketch.sketch
import com.github.panpf.sketch.stateimage.internal.SketchStateDrawable
import com.github.panpf.sketch.util.SketchUtils
import com.github.panpf.sketch.util.findLastSketchDrawable
import com.github.panpf.sketch.util.getLastChildDrawable
import com.github.panpf.sketch.viewability.ViewAbility
import com.github.panpf.sketch.viewability.ViewAbilityContainer
import com.github.panpf.sketch.viewability.ViewAbilityManager
import com.github.panpf.sketch.viewability.internal.RealViewAbilityManager
import com.github.panpf.zoomimage.sketch.internal.SketchImageSource
import com.github.panpf.zoomimage.sketch.internal.SketchTileBitmapPool
import com.github.panpf.zoomimage.sketch.internal.SketchTileMemoryCache
import com.github.panpf.zoomimage.subsampling.ImageSource

/**
 * An ImageView that integrates the Sketch image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * val sketchZoomImageView = SketchZoomImageView(context)
 * sketchZoomImageView.displayImage("http://sample.com/sample.jpg") {
 *     placeholder(R.drawable.placeholder)
 *     crossfade()
 * }
 * ```
 */
open class SketchZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle), ImageOptionsProvider, ViewAbilityContainer {

    private var viewAbilityManager: ViewAbilityManager? = null

    override var displayImageOptions: ImageOptions? = null

    override val viewAbilityList: List<ViewAbility>
        get() = viewAbilityManager?.viewAbilityList ?: emptyList()

    init {
        @Suppress("LeakingThis")
        viewAbilityManager = RealViewAbilityManager(this, this)
        _subsamplingAbility?.tileBitmapPool =
            SketchTileBitmapPool(context.sketch, "SketchZoomImageView")
        _subsamplingAbility?.tileMemoryCache =
            SketchTileMemoryCache(context.sketch, "SketchZoomImageView")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewAbilityManager?.onAttachedToWindow()
        if (drawable != null) {
            resetImageSource()
        }
    }

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        if (ViewCompat.isAttachedToWindow(this)) {
            resetImageSource()
        }
    }

    private fun resetImageSource() {
        post {
            if (!ViewCompat.isAttachedToWindow(this)) {
                return@post
            }
            val result = displayResult
            if (result == null) {
                logger.d { "SketchZoomImageView. Can't use Subsampling, result is null" }
                return@post
            }
            if (result !is DisplayResult.Success) {
                logger.d { "SketchZoomImageView. Can't use Subsampling, result is not Success" }
                return@post
            }
            _subsamplingAbility?.disableMemoryCache = isDisableMemoryCache(result.drawable)
            _subsamplingAbility?.disallowReuseBitmap = isDisallowReuseBitmap(result.drawable)
            _subsamplingAbility?.ignoreExifOrientation = isIgnoreExifOrientation(result.drawable)
            _subsamplingAbility?.setImageSource(newImageSource(result.drawable))
        }
    }

    private fun isDisableMemoryCache(drawable: Drawable?): Boolean {
        val sketchDrawable = drawable?.findLastSketchDrawable()
        val requestKey = sketchDrawable?.requestKey
        val displayResult = SketchUtils.getResult(this)
        return displayResult != null
                && displayResult is DisplayResult.Success
                && displayResult.requestKey == requestKey
                && displayResult.request.memoryCachePolicy != CachePolicy.ENABLED
    }

    private fun isDisallowReuseBitmap(drawable: Drawable?): Boolean {
        val sketchDrawable = drawable?.findLastSketchDrawable()
        val requestKey = sketchDrawable?.requestKey
        val displayResult = SketchUtils.getResult(this)
        return displayResult != null
                && displayResult is DisplayResult.Success
                && displayResult.requestKey == requestKey
                && displayResult.request.disallowReuseBitmap
    }

    private fun isIgnoreExifOrientation(drawable: Drawable?): Boolean {
        val sketchDrawable = drawable?.findLastSketchDrawable()
        val requestKey = sketchDrawable?.requestKey
        val displayResult = SketchUtils.getResult(this)
        return displayResult != null
                && displayResult is DisplayResult.Success
                && displayResult.requestKey == requestKey
                && displayResult.request.ignoreExifOrientation
    }

    private fun newImageSource(drawable: Drawable?): ImageSource? {
        drawable ?: return null
        if (drawable.getLastChildDrawable() is SketchStateDrawable) {
            logger.d { "SketchZoomImageView. Can't use Subsampling, drawable is SketchStateDrawable" }
            return null
        }
        val sketchDrawable = drawable.findLastSketchDrawable()
        if (sketchDrawable == null) {
            logger.d { "SketchZoomImageView. Can't use Subsampling, drawable is not SketchDrawable" }
            return null
        }
        if (sketchDrawable is Animatable) {
            logger.d { "SketchZoomImageView. Can't use Subsampling, drawable is Animatable" }
            return null
        }
        return SketchImageSource(
            context = context,
            sketch = context.sketch,
            imageUri = sketchDrawable.imageUri,
        )
    }

    final override fun addViewAbility(viewAbility: ViewAbility) {
        viewAbilityManager?.addViewAbility(viewAbility)
    }

    override fun removeViewAbility(viewAbility: ViewAbility) {
        viewAbilityManager?.removeViewAbility(viewAbility)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        viewAbilityManager?.onVisibilityChanged(changedView, visibility)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        viewAbilityManager?.onLayout(changed, left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewAbilityManager?.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        viewAbilityManager?.onDrawBefore(canvas)
        super.onDraw(canvas)
        viewAbilityManager?.onDraw(canvas)
    }

    override fun onDrawForeground(canvas: Canvas) {
        viewAbilityManager?.onDrawForegroundBefore(canvas)
        super.onDrawForeground(canvas)
        viewAbilityManager?.onDrawForeground(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return viewAbilityManager?.onTouchEvent(event) == true || super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewAbilityManager?.onDetachedFromWindow()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        val oldDrawable = this.drawable
        super.setImageDrawable(drawable)
        val newDrawable = this.drawable
        if (oldDrawable !== newDrawable) {
            viewAbilityManager?.onDrawableChanged(oldDrawable, newDrawable)
        }
    }

    override fun setImageURI(uri: Uri?) {
        val oldDrawable = this.drawable
        super.setImageURI(uri)
        val newDrawable = this.drawable
        if (oldDrawable !== newDrawable) {
            viewAbilityManager?.onDrawableChanged(oldDrawable, newDrawable)
        }
    }

    final override fun setOnClickListener(l: OnClickListener?) {
        viewAbilityManager?.setOnClickListener(l)
    }

    final override fun setOnLongClickListener(l: OnLongClickListener?) {
        viewAbilityManager?.setOnLongClickListener(l)
    }

    final override fun superSetOnClickListener(listener: OnClickListener?) {
        super.setOnClickListener(listener)
        if (listener == null) {
            isClickable = false
        }
    }

    final override fun superSetOnLongClickListener(listener: OnLongClickListener?) {
        super.setOnLongClickListener(listener)
        if (listener == null) {
            isLongClickable = false
        }
    }

//    final override fun superSetScaleType(scaleType: ScaleType) {
//        super.setScaleType(scaleType)
//    }
//
//    final override fun superGetScaleType(): ScaleType {
//        return super.getScaleType()
//    }

    final override fun setScaleType(scaleType: ScaleType) {
        if (viewAbilityManager?.setScaleType(scaleType) != true) {
            super.setScaleType(scaleType)
        }
    }

    final override fun getScaleType(): ScaleType {
        return viewAbilityManager?.getScaleType() ?: super.getScaleType()
    }

//    final override fun superSetImageMatrix(matrix: Matrix?) {
//        super.setImageMatrix(matrix)
//    }

    final override fun superGetImageMatrix(): Matrix {
        return super.getImageMatrix()
    }

    final override fun setImageMatrix(matrix: Matrix?) {
        if (viewAbilityManager?.setImageMatrix(matrix) != true) {
            super.setImageMatrix(matrix)
        }
    }

    final override fun getImageMatrix(): Matrix {
        return viewAbilityManager?.getImageMatrix() ?: super.getImageMatrix()
    }

    override fun getDisplayListener(): Listener<DisplayRequest, DisplayResult.Success, DisplayResult.Error>? {
        return viewAbilityManager?.getRequestListener()
    }

    override fun getDisplayProgressListener(): ProgressListener<DisplayRequest>? {
        return viewAbilityManager?.getRequestProgressListener()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superParcelable = super.onSaveInstanceState()
        val abilityListStateBundle1 =
            viewAbilityManager?.onSaveInstanceState() ?: return superParcelable
        return SavedState(superParcelable).apply {
            abilityListStateBundle = abilityListStateBundle1
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        viewAbilityManager?.onRestoreInstanceState(state.abilityListStateBundle)
    }

    override fun submitRequest(request: DisplayRequest) {
        context.sketch.enqueue(request)
    }

    class SavedState : BaseSavedState {
        var abilityListStateBundle: Bundle? = null

        internal constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeBundle(abilityListStateBundle)
        }

        override fun toString(): String = "AbsAbilityImageViewSavedState"

        private constructor(`in`: Parcel) : super(`in`) {
            abilityListStateBundle = `in`.readBundle(SavedState::class.java.classLoader)
        }

        companion object {
            @Keep
            @JvmField
            @Suppress("unused")
            val CREATOR: Creator<SavedState> = object : Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}