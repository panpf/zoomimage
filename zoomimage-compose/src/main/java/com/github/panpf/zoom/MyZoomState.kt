package com.github.panpf.zoom

import android.util.Log
import androidx.annotation.FloatRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MyZoomState(
    @FloatRange(from = 0.0) val minScale: Float = 1f,
    @FloatRange(from = 0.0) val maxScale: Float = 4f,
    @FloatRange(from = 0.0) initialTranslateX: Float = 0f,
    @FloatRange(from = 0.0) initialTranslateY: Float = 0f,
    @FloatRange(from = 0.0) initialScale: Float = minScale,
    val debugMode: Boolean = false
) {

    private val _scale = Animatable(initialScale)
    private val _translationX = Animatable(initialTranslateX)
    private val _translationY = Animatable(initialTranslateY)
    private val velocityTracker = VelocityTracker()
    private val _containerSize = mutableStateOf(Size.Unspecified)
    private val _contentSize = mutableStateOf(Size.Unspecified)
    private val _contentScale = mutableStateOf(ContentScale.Fit)
    private val _contentAlignment = mutableStateOf(Alignment.Center)
    private val _translationBounds = mutableStateOf<Rect?>(null)
    // todo rotate

    val transformOrigin = TransformOrigin(0f, 0f)

    val containerSize: Size by _containerSize

    val contentSize: Size by _contentSize

    val contentScale: ContentScale by _contentScale

    val contentAlignment: Alignment by _contentAlignment

    /**
     * The current scale value for [MyZoomImage]
     */
    @get:FloatRange(from = 0.0)
    val scale: Float by derivedStateOf { _scale.value }

    /**
     * The current translation value for [MyZoomImage]
     */
    val translation: Offset by derivedStateOf { Offset(_translationX.value, _translationY.value) }

    val zooming: Boolean by derivedStateOf { scale > minScale }

    val containerVisibleRect: Rect by derivedStateOf {
        computeContainerVisibleRect(containerSize, scale, translation)
    }

    val contentVisibleRect: Rect by derivedStateOf {
        computeContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            scale = scale,
            translation = translation,
        )
    }

    val contentInContainerRect: Rect by derivedStateOf {
        computeContentInContainerRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
        )
    }

    val translationBounds: Rect? by _translationBounds

    val horizontalScrollEdge: Edge by derivedStateOf {
        computeScrollEdge(contentSize, contentVisibleRect, horizontal = true)
    }

    val verticalScrollEdge: Edge by derivedStateOf {
        computeScrollEdge(contentSize, contentVisibleRect, horizontal = false)
    }

    init {
        require(minScale < maxScale) { "minScale must be < maxScale" }
    }

    fun init(
        containerSize: Size,
        contentSize: Size,
        contentScale: ContentScale,
        contentAlignment: Alignment
    ) {
        if (containerSize != _containerSize.value || contentSize != _contentSize.value || contentScale != _contentScale.value || contentAlignment != _contentAlignment.value) {
            _containerSize.value = containerSize
            _contentSize.value = contentSize
            _contentScale.value = contentScale
            _contentAlignment.value = contentAlignment
            updateTranslationBounds("init")
        }
    }

    /**
     * Animates scale of [MyZoomImage] to given [newScale]
     */
    suspend fun animateScaleTo(
        newScale: Float,
        newScaleContentCentroid: Centroid = Centroid(0.5f, 0.5f),
        animationDurationMillis: Int = ScaleAnimationConfig.DefaultDurationMillis,
        animationEasing: Easing = ScaleAnimationConfig.DefaultEasing,
        initialVelocity: Float = ScaleAnimationConfig.DefaultInitialVelocity,
    ) {
        stopAllAnimation("animateScaleTo")
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val currentTranslation = translation

        val animationSpec = tween<Float>(
            durationMillis = animationDurationMillis,
            easing = animationEasing
        )
        val futureTranslationBounds = computeTranslationBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            scale = newScale
        )
        val containerCentroid = contentCentroidToContainerCentroid(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            contentCentroid = newScaleContentCentroid
        )
        val targetTranslation = computeScaleTargetTranslation(
            containerSize = containerSize,
            scale = newScale,
            containerCentroid = containerCentroid
        ).let {
            it.copy(
                x = it.x.coerceIn(futureTranslationBounds.left, futureTranslationBounds.right),
                y = it.y.coerceIn(futureTranslationBounds.top, futureTranslationBounds.bottom),
            )
        }
        logI {
            """animateScaleTo. size: containerSize=${containerSize.toShortString()}, contentSize=${contentSize.toShortString()}
                scale: $currentScale -> $newScale, contentCentroid=${newScaleContentCentroid.toShortString()}, containerCentroid=${containerCentroid.toShortString()}
                translation: ${currentTranslation.toShortString()} -> ${targetTranslation.toShortString()}, bounds=${futureTranslationBounds.toShortString()}
            """.trimIndent()
        }
        clearTranslationBounds("animateScaleTo. before")
        coroutineScope {
            launch {
                _scale.animateTo(
                    targetValue = newScale.coerceIn(minScale, maxScale),
                    animationSpec = animationSpec,
                    initialVelocity = initialVelocity,
                ) {
                    logD { "animateScaleTo. running. scale=${this.value}, translation=${translation.toShortString()}" }
                }
                updateTranslationBounds("animateScaleTo. end")
                logD { "animateScaleTo. end. scale=${scale}, translation=${translation.toShortString()}" }
            }
            launch {
                _translationX.animateTo(
                    targetValue = targetTranslation.x,
                    animationSpec = animationSpec,
                )
            }
            launch {
                _translationY.animateTo(
                    targetValue = targetTranslation.y,
                    animationSpec = animationSpec,
                )
            }
        }
    }

    /**
     * Animates scale of [MyZoomImage] to given [newScale]
     */
    suspend fun animateScaleTo(
        newScale: Float,
        touchPosition: Offset,
        animationDurationMillis: Int = ScaleAnimationConfig.DefaultDurationMillis,
        animationEasing: Easing = ScaleAnimationConfig.DefaultEasing,
        initialVelocity: Float = ScaleAnimationConfig.DefaultInitialVelocity,
    ) {
        stopAllAnimation("animateScaleTo")
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val currentTranslation = translation
        val containerCentroid = computeContainerCentroidByTouchPosition(
            containerSize = containerSize,
            scale = currentScale,
            translation = currentTranslation,
            touchPosition = touchPosition
        )
        val contentCentroid = containerCentroidToContentCentroid(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            containerCentroid = containerCentroid
        )
        logI { "animateScaleTo. newScale=$newScale, touchPosition=${touchPosition.toShortString()}, containerCentroid=${containerCentroid.toShortString()}, contentCentroid=${contentCentroid.toShortString()}" }
        animateScaleTo(
            newScale = newScale,
            newScaleContentCentroid = contentCentroid,
            animationDurationMillis = animationDurationMillis,
            animationEasing = animationEasing,
            initialVelocity = initialVelocity,
        )
    }

    /**
     * Instantly sets scale of [MyZoomImage] to given [newScale]
     */
    suspend fun snapScaleTo(
        newScale: Float,
        newScaleContentCentroid: Centroid = Centroid(0.5f, 0.5f)
    ) {
        stopAllAnimation("snapScaleTo")
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val currentTranslation = translation

        val futureTranslationBounds = computeTranslationBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            scale = newScale
        )
        val containerCentroid = contentCentroidToContainerCentroid(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            contentCentroid = newScaleContentCentroid
        )
        val targetTranslation = computeScaleTargetTranslation(
            containerSize = containerSize,
            scale = newScale,
            containerCentroid = containerCentroid
        ).let {
            it.copy(
                x = it.x.coerceIn(futureTranslationBounds.left, futureTranslationBounds.right),
                y = it.y.coerceIn(futureTranslationBounds.top, futureTranslationBounds.bottom),
            )
        }
        logI {
            """snapScaleTo. size: containerSize=${containerSize.toShortString()}, contentSize=${contentSize.toShortString()} 
                 scale: $currentScale -> $newScale, contentCentroid=${newScaleContentCentroid.toShortString()}, containerCentroid=${containerCentroid.toShortString()}
                translation: ${currentTranslation.toShortString()} -> ${targetTranslation.toShortString()}, bounds=${futureTranslationBounds.toShortString()}
            """.trimIndent()
        }
        coroutineScope {
            _scale.snapTo(newScale.coerceIn(minimumValue = minScale, maximumValue = maxScale))
            updateTranslationBounds("snapScaleTo")
            _translationX.snapTo(targetValue = targetTranslation.x)
            _translationY.snapTo(targetValue = targetTranslation.y)
        }
    }

    /**
     * Instantly sets scale of [MyZoomImage] to given [newScale]
     */
    suspend fun snapScaleTo(newScale: Float, touchPosition: Offset) {
        stopAllAnimation("snapScaleTo")
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val currentTranslation = translation
        val containerCentroid = computeContainerCentroidByTouchPosition(
            containerSize = containerSize,
            scale = currentScale,
            translation = currentTranslation,
            touchPosition = touchPosition
        )
        val contentCentroid = containerCentroidToContentCentroid(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            containerCentroid = containerCentroid
        )
        logI { "snapScaleTo. newScale=$newScale, touchPosition=${touchPosition.toShortString()}, contentCentroid=${contentCentroid.toShortString()}" }
        snapScaleTo(
            newScale = newScale,
            newScaleContentCentroid = contentCentroid
        )
    }

    fun nextScale(): Float {
        val scaleSteps = arrayOf(minScale, maxScale)
        val currentScale = scale
        val currentScaleIndex =
            scaleSteps.findLast { currentScale >= (it - 0.1f) }?.let { scaleSteps.indexOf(it) }
                ?: -1
        return if (currentScaleIndex != -1) {
            scaleSteps[(currentScaleIndex + 1) % scaleSteps.size]
        } else {
            scaleSteps.first()
        }
    }

    internal suspend fun dragStart() {
        stopAllAnimation("dragStart")
        logI { "drag. start. resetTracking" }
        velocityTracker.resetTracking()
    }

    internal suspend fun drag(change: PointerInputChange, dragAmount: Offset) {
        val newTranslation = Offset(
            x = _translationX.value + dragAmount.x,
            y = _translationY.value + dragAmount.y
        )
        logD { "drag. running. dragAmount=${dragAmount.toShortString()}, newTranslation=${newTranslation.toShortString()}" }
        velocityTracker.addPointerInputChange(change)
        coroutineScope {
            launch {
                _translationX.snapTo(newTranslation.x)
                _translationY.snapTo(newTranslation.y)
            }
        }
    }

    internal suspend fun dragEnd() {
        logI { "drag. end" }
        fling(velocityTracker.calculateVelocity())
    }

    internal fun dragCancel() {
        logI { "drag. cancel" }
    }

    internal suspend fun transform(zoomChange: Float, touchCentroid: Offset) {
        stopAllAnimation("transform")
        val currentScale = scale
        val newScale =
            (currentScale * zoomChange).coerceIn(minimumValue = minScale, maximumValue = maxScale)
        val addCentroidOffset = Offset(
            x = (newScale - currentScale) * touchCentroid.x * -1,
            y = (newScale - currentScale) * touchCentroid.y * -1
        )
        val targetTranslation = Offset(
            x = _translationX.value + addCentroidOffset.x,
            y = _translationY.value + addCentroidOffset.y
        )
        logD { "transform. zoomChange=$zoomChange, touchCentroid=${touchCentroid.toShortString()}, newScale=$newScale, addCentroidOffset=${addCentroidOffset.toShortString()}, targetTranslation=${targetTranslation.toShortString()}" }
        coroutineScope {
            _scale.snapTo(newScale)
            updateTranslationBounds("snapScaleTo")
            _translationX.snapTo(targetValue = targetTranslation.x)
            _translationY.snapTo(targetValue = targetTranslation.y)
        }
    }

    private suspend fun stopAllAnimation(caller: String) {
        if (_scale.isRunning) {
            _scale.stop()
            logI { "stopAllAnimation. stop scale. scale=$scale" }
            updateTranslationBounds(caller)
        }
        if (_translationX.isRunning || _translationY.isRunning) {
            _translationX.stop()
            _translationY.stop()
            logI { "stopAllAnimation. stop translation. translation=${translation.toShortString()}" }
        }
    }

    private suspend fun fling(velocity: Velocity) = coroutineScope {
        logI { "fling. velocity=$velocity, translation=${translation.toShortString()}" }
        launch {
            _translationX.animateDecay(velocity.x, exponentialDecay()) {
                logD { "fling. running. velocity=$velocity, translationX=${this.value}" }
            }
        }
        launch {
            _translationY.animateDecay(velocity.y, exponentialDecay()) {
                logD { "fling. running. velocity=$velocity, translationY=${this.value}" }
            }
        }
    }

    override fun toString(): String =
        "MyZoomState(minScale=$minScale, maxScale=$maxScale, scale=$scale, translation=${translation.toShortString()}"

    private fun updateTranslationBounds(caller: String) {
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val bounds = computeTranslationBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            scale = currentScale
        )
        this._translationBounds.value = bounds
        logD { "updateTranslationBounds. $caller. bounds=${bounds.toShortString()}, containerSize=${containerSize.toShortString()}, contentSize=${contentSize.toShortString()}, scale=$currentScale" }
        _translationX.updateBounds(lowerBound = bounds.left, upperBound = bounds.right)
        _translationY.updateBounds(lowerBound = bounds.top, upperBound = bounds.bottom)
    }

    private fun clearTranslationBounds(@Suppress("SameParameterValue") caller: String) {
        logD { "updateTranslationBounds. ${caller}. clear" }
        this._translationBounds.value = null
        _translationX.updateBounds(lowerBound = null, upperBound = null)
        _translationY.updateBounds(lowerBound = null, upperBound = null)
    }

    private fun logD(message: () -> String) {
        if (debugMode) {
            Log.d("MyZoomState", message())
        }
    }

    private fun logI(message: () -> String) {
        if (debugMode) {
            Log.i("MyZoomState", message())
        }
    }

    companion object {

        /**
         * The default [Saver] implementation for [MyZoomState].
         */
        val Saver: Saver<MyZoomState, *> = mapSaver(
            save = {
                mapOf(
                    "translationX" to it.translation.x,
                    "translationY" to it.translation.y,
                    "scale" to it.scale,
                    "minScale" to it.minScale,
                    "maxScale" to it.maxScale,
                    "debugMode" to it.debugMode,
                )
            },
            restore = {
                MyZoomState(
                    initialTranslateX = it["translationX"] as Float,
                    initialTranslateY = it["translationY"] as Float,
                    initialScale = it["scale"] as Float,
                    minScale = it["minScale"] as Float,
                    maxScale = it["maxScale"] as Float,
                    debugMode = it["debugMode"] as Boolean,
                )
            }
        )
    }
}