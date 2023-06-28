package com.github.panpf.zoomimage

import android.util.Log
import androidx.annotation.FloatRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Velocity
import com.github.panpf.zoomimage.internal.ScaleFactor
import com.github.panpf.zoomimage.internal.calculateNextStepScale
import com.github.panpf.zoomimage.internal.computeContainerCentroidByTouchPosition
import com.github.panpf.zoomimage.internal.computeContainerVisibleRect
import com.github.panpf.zoomimage.internal.computeContentInContainerRect
import com.github.panpf.zoomimage.internal.computeContentVisibleRect
import com.github.panpf.zoomimage.internal.computeScaleTargetTranslation
import com.github.panpf.zoomimage.internal.computeScrollEdge
import com.github.panpf.zoomimage.internal.computeSupportScales
import com.github.panpf.zoomimage.internal.computeTranslationBounds
import com.github.panpf.zoomimage.internal.containerCentroidToContentCentroid
import com.github.panpf.zoomimage.internal.contentCentroidToContainerCentroid
import com.github.panpf.zoomimage.internal.toScaleFactor
import com.github.panpf.zoomimage.internal.toScaleMode
import com.github.panpf.zoomimage.internal.toShortString
import com.github.panpf.zoomimage.internal.toSize
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberZoomableState(
    threeStepScaleEnabled: Boolean = false,
    debugMode: Boolean = false,
): ZoomableState {
    val state = rememberSaveable(saver = ZoomableState.Saver) {
        ZoomableState()
    }
    state.threeStepScaleEnabled = threeStepScaleEnabled
    state.debugMode = debugMode
    LaunchedEffect(
        state.containerSize,
        state.contentSize,
        state.contentOriginSize,
        state.contentScale,
        state.contentAlignment
    ) {
        if (state.contentSize.isUnspecified && state.containerSize.isSpecified) {
            state.contentSize = state.containerSize
        }
        state.reset()
    }
    return state
}

class ZoomableState(
    @FloatRange(from = 0.0) initialScale: Float = 1f,
    @FloatRange(from = 0.0) initialTranslateX: Float = 0f,
    @FloatRange(from = 0.0) initialTranslateY: Float = 0f,
) {

    // todo support click and long press
    // todo support rubber band effect
    // todo support read mode

    private val velocityTracker = VelocityTracker()
    private val scaleAnimatable = Animatable(initialScale)
    private val translationXAnimatable = Animatable(initialTranslateX)
    private val translationYAnimatable = Animatable(initialTranslateY)

    var containerSize: Size by mutableStateOf(Size.Unspecified)
    var contentSize: Size by mutableStateOf(Size.Unspecified)
    var contentOriginSize: Size by mutableStateOf(Size.Unspecified)
    var contentScale: ContentScale by mutableStateOf(ContentScale.Fit)
    var contentAlignment: Alignment by mutableStateOf(Alignment.Center)
    var threeStepScaleEnabled: Boolean = false
    var debugMode: Boolean = false

    var minScale: Float by mutableStateOf(1f)
        private set
    var mediumScale: Float by mutableStateOf(1f)
        private set
    var maxScale: Float by mutableStateOf(1f)
        private set

    /**
     * The current scale value for [ZoomImage]
     */
    @get:FloatRange(from = 0.0)
    val scale: Float by derivedStateOf { scaleAnimatable.value }
    val baseScale: ScaleFactor by derivedStateOf {
        val contentSize = contentSize
        val containerSize = containerSize
        if (containerSize.isUnspecified || containerSize.isEmpty()
            || contentSize.isUnspecified || contentSize.isEmpty()
        ) {
            ScaleFactor(1f, 1f)
        } else {
            contentScale.computeScaleFactor(contentSize, containerSize).toScaleFactor()
        }
    }
    val displayScale: ScaleFactor by derivedStateOf {
        baseScale.times(scale)
    }

    /**
     * The current translation value for [ZoomImage]
     */
    val translation: Offset by derivedStateOf {
        Offset(
            translationXAnimatable.value,
            translationYAnimatable.value
        )
    }
    val rotation: Float by mutableStateOf(0f)    // todo support rotation
    val transformOrigin = TransformOrigin(0f, 0f)

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

    var translationBounds: Rect? by mutableStateOf(null)
        private set
    val horizontalScrollEdge: Edge by derivedStateOf {
        computeScrollEdge(contentSize, contentVisibleRect, horizontal = true)
    }
    val verticalScrollEdge: Edge by derivedStateOf {
        computeScrollEdge(contentSize, contentVisibleRect, horizontal = false)
    }

    internal fun reset() {
        resetScales()
        updateTranslationBounds("reset")
    }

    /**
     * Animates scale of [ZoomImage] to given [newScale]
     */
    suspend fun animateScaleTo(
        newScale: Float,
        newScaleContentCentroid: Centroid = Centroid(0.5f, 0.5f),
        animationDurationMillis: Int = AnimationConfig.DefaultDurationMillis,
        animationEasing: Easing = AnimationConfig.DefaultEasing,
        initialVelocity: Float = AnimationConfig.DefaultInitialVelocity,
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
                scaleAnimatable.animateTo(
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
                translationXAnimatable.animateTo(
                    targetValue = targetTranslation.x,
                    animationSpec = animationSpec,
                )
            }
            launch {
                translationYAnimatable.animateTo(
                    targetValue = targetTranslation.y,
                    animationSpec = animationSpec,
                )
            }
        }
    }

    /**
     * Animates scale of [ZoomImage] to given [newScale]
     */
    suspend fun animateScaleTo(
        newScale: Float,
        touchPosition: Offset,
        animationDurationMillis: Int = AnimationConfig.DefaultDurationMillis,
        animationEasing: Easing = AnimationConfig.DefaultEasing,
        initialVelocity: Float = AnimationConfig.DefaultInitialVelocity,
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
     * Instantly sets scale of [ZoomImage] to given [newScale]
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
            scaleAnimatable.snapTo(
                newScale.coerceIn(
                    minimumValue = minScale,
                    maximumValue = maxScale
                )
            )
            updateTranslationBounds("snapScaleTo")
            translationXAnimatable.snapTo(targetValue = targetTranslation.x)
            translationYAnimatable.snapTo(targetValue = targetTranslation.y)
        }
    }

    /**
     * Instantly sets scale of [ZoomImage] to given [newScale]
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

    fun getNextStepScale(): Float {
        val stepScales = if (threeStepScaleEnabled) {
            floatArrayOf(minScale, mediumScale, maxScale)
        } else {
            floatArrayOf(minScale, mediumScale)
        }
        return calculateNextStepScale(stepScales, scale)
    }

    internal suspend fun dragStart() {
        stopAllAnimation("dragStart")
        logI { "drag. start. resetTracking" }
        velocityTracker.resetTracking()
    }

    internal suspend fun drag(change: PointerInputChange, dragAmount: Offset) {
        val newTranslation = Offset(
            x = translationXAnimatable.value + dragAmount.x,
            y = translationYAnimatable.value + dragAmount.y
        )
        logD { "drag. running. dragAmount=${dragAmount.toShortString()}, newTranslation=${newTranslation.toShortString()}" }
        velocityTracker.addPointerInputChange(change)
        coroutineScope {
            launch {
                translationXAnimatable.snapTo(newTranslation.x)
                translationYAnimatable.snapTo(newTranslation.y)
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
            x = translationXAnimatable.value + addCentroidOffset.x,
            y = translationYAnimatable.value + addCentroidOffset.y
        )
        logD { "transform. zoomChange=$zoomChange, touchCentroid=${touchCentroid.toShortString()}, newScale=$newScale, addCentroidOffset=${addCentroidOffset.toShortString()}, targetTranslation=${targetTranslation.toShortString()}" }
        coroutineScope {
            scaleAnimatable.snapTo(newScale)
            updateTranslationBounds("snapScaleTo")
            translationXAnimatable.snapTo(targetValue = targetTranslation.x)
            translationYAnimatable.snapTo(targetValue = targetTranslation.y)
        }
    }

    private suspend fun stopAllAnimation(caller: String) {
        if (scaleAnimatable.isRunning) {
            scaleAnimatable.stop()
            logI { "stopAllAnimation. stop scale. scale=$scale" }
            updateTranslationBounds(caller)
        }
        if (translationXAnimatable.isRunning || translationYAnimatable.isRunning) {
            translationXAnimatable.stop()
            translationYAnimatable.stop()
            logI { "stopAllAnimation. stop translation. translation=${translation.toShortString()}" }
        }
    }

    private suspend fun fling(velocity: Velocity) = coroutineScope {
        logI { "fling. velocity=$velocity, translation=${translation.toShortString()}" }
        launch {
            translationXAnimatable.animateDecay(velocity.x, exponentialDecay()) {
                logD { "fling. running. velocity=$velocity, translationX=${this.value}" }
            }
        }
        launch {
            translationYAnimatable.animateDecay(velocity.y, exponentialDecay()) {
                logD { "fling. running. velocity=$velocity, translationY=${this.value}" }
            }
        }
    }

    override fun toString(): String =
        "MyZoomState(minScale=$minScale, maxScale=$maxScale, scale=$scale, translation=${translation.toShortString()}"

    private fun resetScales() {
        val contentSize = contentSize
        val contentOriginSize = contentOriginSize
        val containerSize = containerSize
        val contentScale = contentScale
        if (containerSize.isUnspecified || containerSize.isEmpty()
            || contentSize.isUnspecified || contentSize.isEmpty()
        ) {
            minScale = 1.0f
            mediumScale = 1.0f
            maxScale = 1.0f
        } else {
            val scales = computeSupportScales(
                contentSize = contentSize.toSize(),
                contentOriginSize = contentOriginSize.toSize(),
                containerSize = containerSize.toSize(),
                scaleMode = contentScale.toScaleMode(),
                baseScale = contentScale.computeScaleFactor(contentSize, containerSize)
                    .toScaleFactor()
            )
            minScale = scales[0]
            mediumScale = scales[1]
            maxScale = scales[2]
        }
    }

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
        this.translationBounds = bounds
        logD { "updateTranslationBounds. $caller. bounds=${bounds.toShortString()}, containerSize=${containerSize.toShortString()}, contentSize=${contentSize.toShortString()}, scale=$currentScale" }
        translationXAnimatable.updateBounds(lowerBound = bounds.left, upperBound = bounds.right)
        translationYAnimatable.updateBounds(lowerBound = bounds.top, upperBound = bounds.bottom)
    }

    private fun clearTranslationBounds(@Suppress("SameParameterValue") caller: String) {
        logD { "updateTranslationBounds. ${caller}. clear" }
        this.translationBounds = null
        translationXAnimatable.updateBounds(lowerBound = null, upperBound = null)
        translationYAnimatable.updateBounds(lowerBound = null, upperBound = null)
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
         * The default [Saver] implementation for [ZoomableState].
         */
        val Saver: Saver<ZoomableState, *> = mapSaver(
            save = {
                mapOf(
                    "scale" to it.scale,
                    "translationX" to it.translation.x,
                    "translationY" to it.translation.y,
                )
            },
            restore = {
                ZoomableState(
                    initialScale = it["scale"] as Float,
                    initialTranslateX = it["translationX"] as Float,
                    initialTranslateY = it["translationY"] as Float,
                )
            }
        )
    }
}