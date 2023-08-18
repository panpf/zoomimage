@file:Suppress("UnnecessaryVariable")

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.splineBasedDecay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.toRect
import androidx.compose.ui.unit.toSize
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ScrollEdge
import com.github.panpf.zoomimage.compose.internal.ScaleFactor
import com.github.panpf.zoomimage.compose.internal.format
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.compose.internal.name
import com.github.panpf.zoomimage.compose.internal.roundToPlatform
import com.github.panpf.zoomimage.compose.internal.times
import com.github.panpf.zoomimage.compose.internal.toCompat
import com.github.panpf.zoomimage.compose.internal.toCompatOffset
import com.github.panpf.zoomimage.compose.internal.toPlatform
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.util.DefaultMediumScaleMinMultiple
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.calculateNextStepScale
import com.github.panpf.zoomimage.util.canScrollByEdge
import com.github.panpf.zoomimage.util.computeContainerVisibleRect
import com.github.panpf.zoomimage.util.computeContentBaseDisplayRect
import com.github.panpf.zoomimage.util.computeContentBaseVisibleRect
import com.github.panpf.zoomimage.util.computeContentDisplayRect
import com.github.panpf.zoomimage.util.computeContentVisibleRect
import com.github.panpf.zoomimage.util.computeInitialZoom
import com.github.panpf.zoomimage.util.computeLocationUserOffset
import com.github.panpf.zoomimage.util.computeScrollEdge
import com.github.panpf.zoomimage.util.computeTransformOffset
import com.github.panpf.zoomimage.util.computeUserOffsetBounds
import com.github.panpf.zoomimage.util.containerPointToContentPoint
import com.github.panpf.zoomimage.util.contentPointToContainerPoint
import com.github.panpf.zoomimage.util.limitScaleWithRubberBand
import com.github.panpf.zoomimage.util.plus
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.util.touchPointToContainerPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun rememberZoomableState(
    logger: Logger,
    mediumScaleMinMultiple: Float = DefaultMediumScaleMinMultiple,
    threeStepScale: Boolean = false,
    rubberBandScale: Boolean = true,
    animationSpec: ZoomAnimationSpec = ZoomAnimationSpec.Default,
    readMode: ReadMode? = null,
): ZoomableState {
    val coroutineScope = rememberCoroutineScope()
    val zoomableState = remember { ZoomableState(logger, coroutineScope) }
    zoomableState.mediumScaleMinMultiple = mediumScaleMinMultiple
    zoomableState.threeStepScale = threeStepScale
    zoomableState.rubberBandScale = rubberBandScale
    zoomableState.animationSpec = animationSpec
    zoomableState.readMode = readMode
    LaunchedEffect(Unit) {
        snapshotFlow { zoomableState.containerSize }.collect {
            if (!it.isEmpty() && zoomableState.contentSize.isEmpty()) {
                zoomableState.contentSize = it
            }
            zoomableState.reset("containerSizeChanged")
        }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { zoomableState.contentSize }.collect {
            zoomableState.reset("contentSizeChanged")
        }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { zoomableState.contentOriginSize }.collect {
            zoomableState.reset("contentOriginSizeChanged")
        }
    }
    return zoomableState
}

class ZoomableState(
    logger: Logger,
    private val coroutineScope: CoroutineScope
) : RememberObserver {

    private val logger: Logger = logger.newLogger(module = "ZoomableState")

    private var lastScaleAnimatable: Animatable<*, *>? = null
    private var lastFlingAnimatable: Animatable<*, *>? = null
    private var rotation = 0

    var baseTransform: Transform by mutableStateOf(Transform.Origin)
        private set
    var userTransform: Transform by mutableStateOf(Transform.Origin)
        private set
    val transform: Transform by derivedStateOf {
        baseTransform + userTransform
    }
    var minScale: Float by mutableStateOf(1f)
        private set
    var mediumScale: Float by mutableStateOf(1f)
        private set
    var maxScale: Float by mutableStateOf(1f)
        private set
    var scaling: Boolean by mutableStateOf(false)
    var fling: Boolean by mutableStateOf(false)

    var containerSize: IntSize by mutableStateOf(IntSize.Zero)
    var contentSize: IntSize by mutableStateOf(IntSize.Zero)
    var contentOriginSize: IntSize by mutableStateOf(IntSize.Zero)
    var contentScale: ContentScale = ContentScale.Fit
        set(value) {
            if (field != value) {
                field = value
                reset("contentScaleChanged")
            }
        }
    var contentAlignment: Alignment = Alignment.Center
        set(value) {
            if (field != value) {
                field = value
                reset("contentAlignmentChanged")
            }
        }
    var readMode: ReadMode? = null
        set(value) {
            if (field != value) {
                field = value
                reset("readModeChanged")
            }
        }
    var mediumScaleMinMultiple: Float = DefaultMediumScaleMinMultiple
        set(value) {
            if (field != value) {
                field = value
                reset("mediumScaleMinMultipleChanged")
            }
        }
    var threeStepScale: Boolean = false
    var rubberBandScale: Boolean = true
    var animationSpec: ZoomAnimationSpec = ZoomAnimationSpec.Default

    val containerVisibleRect: IntRect by derivedStateOf {
        computeContainerVisibleRect(
            containerSize = containerSize.toCompat(),
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset.toCompat()
        ).roundToPlatform()
    }
    val contentBaseDisplayRect: IntRect by derivedStateOf {
        computeContentBaseDisplayRect(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = contentAlignment.toCompat(),
            rotation = rotation,
        ).roundToPlatform()
    }
    val contentBaseVisibleRect: IntRect by derivedStateOf {
        computeContentBaseVisibleRect(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = contentAlignment.toCompat(),
            rotation = rotation,
        ).roundToPlatform()
    }
    val contentDisplayRect: IntRect by derivedStateOf {
        computeContentDisplayRect(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = contentAlignment.toCompat(),
            rotation = rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset.toCompat(),
        ).roundToPlatform()
    }
    val contentVisibleRect: IntRect by derivedStateOf {
        computeContentVisibleRect(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = contentAlignment.toCompat(),
            rotation = rotation,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset.toCompat(),
        ).roundToPlatform()
    }

    val scrollEdge: ScrollEdge by derivedStateOf {
        val userOffsetBounds = computeUserOffsetBounds(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = contentAlignment.toCompat(),
            rotation = rotation,
            userScale = userTransform.scaleX,
        )
        computeScrollEdge(
            userOffsetBounds = userOffsetBounds,
            userOffset = userTransform.offset.toCompat(),
        )
    }
    val userOffsetBounds: IntRect by derivedStateOf {
        computeUserOffsetBounds(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = contentAlignment.toCompat(),
            rotation = rotation,
            userScale = userTransform.scaleX,
        ).roundToPlatform()
    }

    fun reset(
        caller: String,
        immediate: Boolean = false
    ) = coroutineScope.launch(getCoroutineContext(immediate)) {
        stopAllAnimationInternal("reset:$caller")

        val containerSize = containerSize
        val contentSize = contentSize
        val contentOriginSize = contentOriginSize
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val readMode = readMode
        val rotation = rotation
        val mediumScaleMinMultiple = mediumScaleMinMultiple

        val initialZoom = computeInitialZoom(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentOriginSize = contentOriginSize.toCompat(),
            contentScale = contentScale.toCompat(),
            contentAlignment = contentAlignment.toCompat(),
            rotation = rotation,
            readMode = readMode,
            mediumScaleMinMultiple = mediumScaleMinMultiple
        )
        logger.d {
            val transform = initialZoom.baseTransform + initialZoom.userTransform
            "reset:$caller. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentOriginSize=${contentOriginSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "contentAlignment=${contentAlignment.name}, " +
                    "rotation=${rotation}, " +
                    "mediumScaleMinMultiple=${mediumScaleMinMultiple.format(4)}, " +
                    "readMode=${readMode}. " +
                    "minScale=${initialZoom.minScale.format(4)}, " +
                    "mediumScale=${initialZoom.mediumScale.format(4)}, " +
                    "maxScale=${initialZoom.maxScale.format(4)}, " +
                    "baseTransform=${initialZoom.baseTransform.toShortString()}, " +
                    "initialUserTransform=${initialZoom.userTransform.toShortString()}, " +
                    "transform=${transform.toShortString()}"
        }

        minScale = initialZoom.minScale
        mediumScale = initialZoom.mediumScale
        maxScale = initialZoom.maxScale
        baseTransform = initialZoom.baseTransform.toPlatform()
        userTransform = initialZoom.userTransform.toPlatform()
    }

    fun scale(
        targetScale: Float,
        contentPoint: IntOffset? = null,
        animated: Boolean = false
    ) = coroutineScope.launch {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return@launch
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return@launch

        stopAllAnimationInternal("scale")

        val targetUserScale = targetScale / baseTransform.scaleX
        val limitedTargetUserScale = limitUserScale(targetUserScale)
        val currentUserTransform = userTransform
        val currentUserScale = currentUserTransform.scaleX
        val currentUserOffset = currentUserTransform.offset

        val containerPoint = if (contentPoint != null) {
            contentPointToContainerPoint(
                containerSize = containerSize.toCompat(),
                contentSize = contentSize.toCompat(),
                contentScale = contentScale.toCompat(),
                contentAlignment = contentAlignment.toCompat(),
                rotation = rotation,
                contentPoint = contentPoint.toCompat(),
            ).toPlatform()
        } else {
            containerSize.center
        }

        val targetUserOffset = computeTransformOffset(
            currentScale = currentUserScale,
            currentOffset = currentUserOffset.toCompat(),
            targetScale = limitedTargetUserScale,
            centroid = containerPoint.toCompatOffset(),
            pan = OffsetCompat.Zero,
            gestureRotate = 0f,
        ).toPlatform()
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, limitedTargetUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(
            scale = ScaleFactor(limitedTargetUserScale),
            offset = limitedTargetUserOffset
        )
        logger.d {
            val targetAddUserScale = targetUserScale - currentUserScale
            val limitedAddUserScale = limitedTargetUserScale - currentUserScale
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddOffset = limitedTargetUserOffset - currentUserOffset
            "scale. " +
                    "targetScale=${targetScale.format(4)}, " +
                    "contentPoint=${contentPoint?.toShortString()}, " +
                    "animated=${animated}. " +
                    "containerPoint=${containerPoint.toShortString()}, " +
                    "targetUserScale=${targetUserScale.format(4)}, " +
                    "addUserScale=${targetAddUserScale.format(4)} -> ${limitedAddUserScale.format(4)}, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddOffset.toShortString()}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedTargetUserTransform,
            animated = animated,
            caller = "scale"
        )
    }

    fun offset(
        targetOffset: Offset,
        animated: Boolean = false
    ) = coroutineScope.launch {
        containerSize.takeIf { it.isNotEmpty() } ?: return@launch
        contentSize.takeIf { it.isNotEmpty() } ?: return@launch

        stopAllAnimationInternal("offset")

        val targetUserOffset = targetOffset - (baseTransform.offset.times(userTransform.scale))
        val currentUserTransform = userTransform
        val currentUserScale = currentUserTransform.scaleX
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, currentUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(offset = limitedTargetUserOffset)
        logger.d {
            val currentUserOffset = currentUserTransform.offset
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddUserOffset = limitedTargetUserOffset - currentUserOffset
            "offset. " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "animated=${animated}. " +
                    "targetUserOffset=${targetUserOffset.toShortString()}, " +
                    "currentUserScale=${currentUserScale.format(4)}, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddUserOffset}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedTargetUserTransform,
            animated = animated,
            caller = "offset"
        )
    }

    fun location(
        contentPoint: IntOffset,
        targetScale: Float = transform.scaleX,
        animated: Boolean = false,
    ) = coroutineScope.launch {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return@launch
        val contentSize =
            contentSize.takeIf { it.isNotEmpty() } ?: return@launch
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentUserTransform = userTransform
        val rotation = rotation

        stopAllAnimationInternal("location")

        val containerPoint = contentPointToContainerPoint(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            contentAlignment = contentAlignment.toCompat(),
            rotation = rotation,
            contentPoint = contentPoint.toCompat(),
        )

        val targetUserScale = targetScale / baseTransform.scaleX
        val limitedTargetUserScale = limitUserScale(targetUserScale)

        val targetUserOffset = computeLocationUserOffset(
            containerSize = containerSize.toCompat(),
            containerPoint = containerPoint,
            userScale = limitedTargetUserScale,
        ).toPlatform()
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, limitedTargetUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(
            scale = ScaleFactor(limitedTargetUserScale),
            offset = limitedTargetUserOffset
        )
        logger.d {
            val currentUserScale = currentUserTransform.scaleX
            val currentUserOffset = currentUserTransform.offset
            val targetAddUserScale = targetUserScale - currentUserScale
            val limitedTargetAddUserScale = limitedTargetUserScale - currentUserScale
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddUserOffset = limitedTargetUserOffset - currentUserOffset
            val limitedTargetAddUserScaleFormatted = limitedTargetAddUserScale.format(4)
            "location. " +
                    "contentPoint=${contentPoint.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "animated=${animated}. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerPoint=${containerPoint.toShortString()}, " +
                    "addUserScale=${targetAddUserScale.format(4)} -> $limitedTargetAddUserScaleFormatted, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddUserOffset.toShortString()}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedTargetUserTransform,
            animated = animated,
            caller = "location"
        )
    }

    fun rotate(targetRotation: Int) = coroutineScope.launch {
        require(targetRotation >= 0) { "rotation must be greater than or equal to 0: $targetRotation" }
        require(targetRotation % 90 == 0) { "rotation must be in multiples of 90: $targetRotation" }
        val limitedTargetRotation = targetRotation % 360
        val currentRotation = rotation
        if (currentRotation == limitedTargetRotation) return@launch

        stopAllAnimationInternal("rotate")

        rotation = limitedTargetRotation
        reset("rotate")
    }

    fun transform(
        centroid: Offset,
        panChange: Offset,
        zoomChange: Float,
        rotationChange: Float
    ) = coroutineScope.launch {
        containerSize.takeIf { it.isNotEmpty() } ?: return@launch
        contentSize.takeIf { it.isNotEmpty() } ?: return@launch

        val targetScale = transform.scaleX * zoomChange
        val targetUserScale = targetScale / baseTransform.scaleX
        val limitedTargetUserScale = if (rubberBandScale) {
            limitUserScaleWithRubberBand(targetUserScale)
        } else {
            limitUserScale(targetUserScale)
        }
        val currentUserTransform = userTransform
        val currentUserScale = currentUserTransform.scaleX
        val currentUserOffset = currentUserTransform.offset
        val targetUserOffset = computeTransformOffset(
            currentScale = currentUserScale,
            currentOffset = currentUserOffset.toCompat(),
            targetScale = limitedTargetUserScale,
            centroid = centroid.toCompat(),
            pan = panChange.toCompat(),
            gestureRotate = 0f,
        ).toPlatform()
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, limitedTargetUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(
            scale = ScaleFactor(limitedTargetUserScale),
            offset = limitedTargetUserOffset
        )
        logger.d {
            val targetAddUserScale = targetUserScale - currentUserScale
            val limitedAddUserScale = limitedTargetUserScale - currentUserScale
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddOffset = limitedTargetUserOffset - currentUserOffset
            "transform. " +
                    "centroid=${centroid.toShortString()}, " +
                    "panChange=${panChange.toShortString()}, " +
                    "zoomChange=${zoomChange.format(4)}, " +
                    "rotationChange=${rotationChange.format(4)}. " +
                    "targetScale=${targetScale.format(4)}, " +
                    "targetUserScale=${targetUserScale.format(4)}, " +
                    "addUserScale=${targetAddUserScale.format(4)} -> ${limitedAddUserScale.format(4)}, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddOffset.toShortString()}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedTargetUserTransform,
            animated = false,
            caller = "transform"
        )
    }

    fun fling(velocity: Velocity, density: Density) = coroutineScope.launch {
        stopAllAnimationInternal("fling")

        val currentUserTransform = userTransform
        val startUserOffset = currentUserTransform.offset
        val flingAnimatable = Animatable(
            initialValue = startUserOffset,
            typeConverter = Offset.VectorConverter,
        )
        this@ZoomableState.lastFlingAnimatable = flingAnimatable
        var job: Job? = null
        job = coroutineScope {
            launch {
                fling = true
                try {
                    val initialVelocity = Offset.VectorConverter
                        .convertFromVector(AnimationVector(velocity.x, velocity.y))
                    flingAnimatable.animateDecay(
                        initialVelocity = initialVelocity,
                        animationSpec = splineBasedDecay(density)
                    ) {
                        val currentUserTransform2 = userTransform
                        val targetUserOffset = this.value
                        val limitedTargetUserOffset =
                            limitUserOffset(targetUserOffset, currentUserTransform2.scaleX)
                        if (limitedTargetUserOffset != currentUserTransform2.offset) {
                            val distance = limitedTargetUserOffset - startUserOffset
                            logger.d {
                                "fling. running. " +
                                        "velocity=$velocity. " +
                                        "startUserOffset=${startUserOffset.toShortString()}, " +
                                        "currentUserOffset=${limitedTargetUserOffset.toShortString()}, " +
                                        "distance=$distance"
                            }
                            userTransform =
                                currentUserTransform2.copy(offset = limitedTargetUserOffset)
                        } else {
                            // SubsamplingState(line 87) relies on the fling state to refresh tiles,
                            // so you need to end the fling animation as soon as possible
                            job?.cancel("reachBounds")
                            fling = false
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } finally {
                    fling = false
                }
            }
        }
    }

    fun rollbackScale(centroid: Offset? = null): Boolean {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return false
        contentSize.takeIf { it.isNotEmpty() } ?: return false

        val minScale = minScale
        val maxScale = maxScale
        val currentScale = transform.scaleX
        val targetScale = when {
            currentScale.format(2) > maxScale.format(2) -> maxScale
            currentScale.format(2) < minScale.format(2) -> minScale
            else -> null
        }
        if (targetScale != null) {
            val startScale = currentScale
            val endScale = targetScale
            logger.d {
                "rollbackScale. " +
                        "centroid=${centroid?.toShortString()}. " +
                        "startScale=${startScale.format(4)}, " +
                        "endScale=${endScale.format(4)}"
            }
            val finalCentroid = centroid ?: containerSize.toSize().center
            coroutineScope.launch {
                val updateAnimatable = Animatable(0f)
                this@ZoomableState.lastScaleAnimatable = updateAnimatable
                scaling = true
                try {
                    updateAnimatable.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = animationSpec.durationMillis,
                            easing = animationSpec.easing
                        ),
                        initialVelocity = animationSpec.initialVelocity,
                    ) {
                        val frameScale = androidx.compose.ui.util.lerp(
                            start = startScale,
                            stop = endScale,
                            fraction = value
                        )
                        val nowScale = transform.scaleX
                        val addScale = frameScale / nowScale
                        transform(
                            centroid = finalCentroid,
                            panChange = Offset.Zero,
                            zoomChange = addScale,
                            rotationChange = 0f
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } finally {
                    scaling = false
                }
            }
        }
        return targetScale != null
    }

    fun switchScale(
        contentPoint: IntOffset? = null,
        animated: Boolean = true
    ): Float {
        val finalContentPoint = contentPoint
            ?: contentVisibleRect.takeIf { !it.isEmpty }?.center
            ?: contentSize.takeIf { it.isNotEmpty() }?.center
            ?: return transform.scaleX
        val nextScale = getNextStepScale()
        location(
            contentPoint = finalContentPoint,
            targetScale = nextScale,
            animated = animated
        )
        return nextScale
    }

    fun getNextStepScale(): Float {
        val stepScales = if (threeStepScale) {
            floatArrayOf(minScale, mediumScale, maxScale)
        } else {
            floatArrayOf(minScale, mediumScale)
        }
        return calculateNextStepScale(stepScales, transform.scaleX)
    }

    fun stopAllAnimation(caller: String) = coroutineScope.launch {
        stopAllAnimationInternal(caller)
    }

    fun canScroll(horizontal: Boolean, direction: Int): Boolean =
        canScrollByEdge(scrollEdge, horizontal, direction)

    fun touchPointToContentPoint(touchPoint: Offset): IntOffset {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return IntOffset.Zero
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return IntOffset.Zero
        val currentUserTransform = userTransform
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val rotation = rotation
        val containerPoint = touchPointToContainerPoint(
            containerSize = containerSize.toCompat(),
            userScale = currentUserTransform.scaleX,
            userOffset = currentUserTransform.offset.toCompat(),
            touchPoint = touchPoint.toCompat()
        )
        val contentPoint = containerPointToContentPoint(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            contentAlignment = contentAlignment.toCompat(),
            rotation = rotation,
            containerPoint = containerPoint
        ).toPlatform()
        return contentPoint
    }

    private fun limitUserScale(targetUserScale: Float): Float {
        val minUserScale = minScale / baseTransform.scaleX
        val maxUserScale = maxScale / baseTransform.scaleX
        return targetUserScale.coerceIn(minimumValue = minUserScale, maximumValue = maxUserScale)
    }

    private fun limitUserScaleWithRubberBand(targetUserScale: Float): Float {
        val minUserScale = minScale / baseTransform.scaleX
        val maxUserScale = maxScale / baseTransform.scaleX
        return limitScaleWithRubberBand(
            currentScale = userTransform.scaleX,
            targetScale = targetUserScale,
            minScale = minUserScale,
            maxScale = maxUserScale
        )
    }

    private fun limitUserOffset(userOffset: Offset, userScale: Float): Offset {
        val userOffsetBounds = computeUserOffsetBounds(
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            contentScale = contentScale.toCompat(),
            alignment = contentAlignment.toCompat(),
            rotation = rotation,
            userScale = userScale,
        ).roundToPlatform().toRect()
        if (userOffset.x >= userOffsetBounds.left
            && userOffset.x <= userOffsetBounds.right
            && userOffset.y >= userOffsetBounds.top
            && userOffset.y <= userOffsetBounds.bottom
        ) {
            return userOffset
        }
        return Offset(
            x = userOffset.x.coerceIn(userOffsetBounds.left, userOffsetBounds.right),
            y = userOffset.y.coerceIn(userOffsetBounds.top, userOffsetBounds.bottom),
        )
    }

    private suspend fun stopAllAnimationInternal(caller: String) {
        val lastScaleAnimatable = lastScaleAnimatable
        if (lastScaleAnimatable?.isRunning == true) {
            lastScaleAnimatable.stop()
            scaling = false
            logger.d { "stopScaleAnimation:$caller" }
        }

        val lastFlingAnimatable = lastFlingAnimatable
        if (lastFlingAnimatable?.isRunning == true) {
            lastFlingAnimatable.stop()
            fling = false
            logger.d { "stopFlingAnimation:$caller" }
        }
    }

    private suspend fun updateUserTransform(
        targetUserTransform: Transform,
        animated: Boolean,
        caller: String
    ) {
        if (animated) {
            val currentUserTransform = userTransform
            val scaleChange = currentUserTransform.scale != targetUserTransform.scale
            val updateAnimatable = Animatable(0f)
            this.lastScaleAnimatable = updateAnimatable
            if (scaleChange) {
                scaling = true
            }
            try {
                updateAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = animationSpec.durationMillis,
                        easing = animationSpec.easing
                    ),
                    initialVelocity = animationSpec.initialVelocity,
                ) {
                    val userTransform = lerp(
                        start = currentUserTransform,
                        stop = targetUserTransform,
                        fraction = value
                    )
                    logger.d {
                        "$caller. animated running. transform=${userTransform.toShortString()}"
                    }
                    this@ZoomableState.userTransform = userTransform
                }
            } catch (e: CancellationException) {
                throw e
            } finally {
                if (scaleChange) {
                    scaling = false
                }
            }
        } else {
            this.userTransform = targetUserTransform
        }
    }

    private fun getCoroutineContext(immediate: Boolean) =
        if (immediate) Dispatchers.Main.immediate else EmptyCoroutineContext

    override fun onRemembered() {

    }

    override fun onAbandoned() {
        coroutineScope.cancel("onAbandoned")
    }

    override fun onForgotten() {
        coroutineScope.cancel("onForgotten")
    }

    override fun toString(): String =
        "ZoomableState(" +
                "containerSize=${containerSize.toShortString()}, " +
                "contentSize=${contentSize.toShortString()}, " +
                "contentOriginSize=${contentOriginSize.toShortString()}, " +
                "contentScale=${contentScale.name}, " +
                "contentAlignment=${contentAlignment.name}, " +
                "minScale=${minScale.format(4)}, " +
                "mediumScale=${mediumScale.format(4)}, " +
                "maxScale=${maxScale.format(4)}, " +
                "transform=${transform.toShortString()}" +
                ")"
}