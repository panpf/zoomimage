package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.splineBasedDecay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.unit.toSize
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.ScrollEdge
import com.github.panpf.zoomimage.compose.internal.ScaleFactor
import com.github.panpf.zoomimage.compose.internal.format
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.compose.internal.name
import com.github.panpf.zoomimage.compose.internal.rotate
import com.github.panpf.zoomimage.compose.internal.times
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.compose.zoom.internal.computeContainerVisibleRect
import com.github.panpf.zoomimage.compose.zoom.internal.computeContentInContainerRect
import com.github.panpf.zoomimage.compose.zoom.internal.computeContentInContainerVisibleRect
import com.github.panpf.zoomimage.compose.zoom.internal.computeContentVisibleRect
import com.github.panpf.zoomimage.compose.zoom.internal.computeLocationUserOffset
import com.github.panpf.zoomimage.compose.zoom.internal.computeReadModeTransform
import com.github.panpf.zoomimage.compose.zoom.internal.computeScaleOffsetByCentroid
import com.github.panpf.zoomimage.compose.zoom.internal.computeTransform
import com.github.panpf.zoomimage.compose.zoom.internal.computeUserOffsetBounds
import com.github.panpf.zoomimage.compose.zoom.internal.containerPointToContentPoint
import com.github.panpf.zoomimage.compose.zoom.internal.contentPointToContainerPoint
import com.github.panpf.zoomimage.compose.zoom.internal.rotateInContainer
import com.github.panpf.zoomimage.compose.zoom.internal.supportReadMode
import com.github.panpf.zoomimage.compose.zoom.internal.toCompatIntRect
import com.github.panpf.zoomimage.compose.zoom.internal.toCompatIntSize
import com.github.panpf.zoomimage.compose.zoom.internal.toCompatScaleFactor
import com.github.panpf.zoomimage.compose.zoom.internal.toScaleMode
import com.github.panpf.zoomimage.compose.zoom.internal.touchPointToContainerPoint
import com.github.panpf.zoomimage.core.internal.DEFAULT_MEDIUM_SCALE_MULTIPLE
import com.github.panpf.zoomimage.core.internal.calculateNextStepScale
import com.github.panpf.zoomimage.core.internal.canScroll
import com.github.panpf.zoomimage.core.internal.computeScrollEdge
import com.github.panpf.zoomimage.core.internal.computeUserScales
import com.github.panpf.zoomimage.core.internal.limitScaleWithRubberBand
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun rememberZoomableState(
    logger: Logger,
    defaultMediumScaleMultiple: Float = DEFAULT_MEDIUM_SCALE_MULTIPLE,
    threeStepScale: Boolean = false,
    rubberBandScale: Boolean = true,
    animationSpec: ZoomAnimationSpec = ZoomAnimationSpec.Default,
    readMode: ReadMode? = null,
): ZoomableState {
    val zoomableState = remember { ZoomableState(logger) }
    zoomableState.defaultMediumScaleMultiple = defaultMediumScaleMultiple
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

// todo support rotation
class ZoomableState(logger: Logger) {

    private val logger: Logger = logger.newLogger(module = "ZoomableState")
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var lastScaleAnimatable: Animatable<*, *>? = null
    private var lastFlingAnimatable: Animatable<*, *>? = null

    var containerSize: IntSize by mutableStateOf(IntSize.Zero)
    var contentSize: IntSize by mutableStateOf(IntSize.Zero)
    var contentOriginSize: IntSize by mutableStateOf(IntSize.Zero)
    var contentScale: ContentScale = ContentScale.Fit
        set(value) {
            if (field != value) {
                field = value
                coroutineScope.launch { reset("contentScaleChanged") }
            }
        }
    var contentAlignment: Alignment = Alignment.Center
        set(value) {
            if (field != value) {
                field = value
                coroutineScope.launch { reset("contentAlignmentChanged") }
            }
        }
    var readMode: ReadMode? = null
        set(value) {
            if (field != value) {
                field = value
                coroutineScope.launch { reset("readModeChanged") }
            }
        }
    var defaultMediumScaleMultiple: Float = DEFAULT_MEDIUM_SCALE_MULTIPLE
        set(value) {
            if (field != value) {
                field = value
                coroutineScope.launch { reset("defaultMediumScaleMultipleChanged") }
            }
        }
    var threeStepScale: Boolean = false
    var rubberBandScale: Boolean = true
    var animationSpec: ZoomAnimationSpec = ZoomAnimationSpec.Default

    var minScale: Float by mutableStateOf(1f)
        private set
    var mediumScale: Float by mutableStateOf(1f)
        private set
    var maxScale: Float by mutableStateOf(1f)
        private set
    var baseTransform: Transform by mutableStateOf(Transform.Origin)
        private set
    var userTransform: Transform by mutableStateOf(Transform.Origin)
        private set
    val transform: Transform by derivedStateOf {
        baseTransform.concat(userTransform)
    }
    var scaling: Boolean by mutableStateOf(false)
    var fling: Boolean by mutableStateOf(false)

    val userOffsetBounds: IntRect by derivedStateOf {
        computeUserOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
            userScale = userTransform.scaleX,
        ).roundToIntRect()
    }

    val contentInContainerRect: IntRect by derivedStateOf {
        computeContentInContainerRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
        ).roundToIntRect()
    }
    val contentInContainerVisibleRect: IntRect by derivedStateOf {
        computeContentInContainerVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
        ).roundToIntRect()
    }

    val containerVisibleRect: IntRect by derivedStateOf {
        computeContainerVisibleRect(
            containerSize = containerSize,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset
        ).roundToIntRect()
    }
    val contentVisibleRect: IntRect by derivedStateOf {
        computeContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset,
        ).roundToIntRect()
    }
    val scrollEdge: ScrollEdge by derivedStateOf {
        computeScrollEdge(
            contentInContainerVisibleRect = contentInContainerVisibleRect.toCompatIntRect(),
            contentVisibleRect = contentVisibleRect.toCompatIntRect(),
        )
    }

    internal suspend fun reset(caller: String) {
        stopAllAnimation("reset:$caller")

        val contentSize = contentSize
        val contentOriginSize = contentOriginSize
        val containerSize = containerSize
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val initialUserTransform: Transform
        if (containerSize.isEmpty() || contentSize.isEmpty()) {
            minScale = 1.0f
            mediumScale = 1.0f
            maxScale = 1.0f
            baseTransform = Transform.Origin
            initialUserTransform = Transform.Origin
        } else {
            val rotatedContentSize = contentSize.rotate(userTransform.rotation.roundToInt())
            val rotatedContentOriginSize =
                contentOriginSize.rotate(userTransform.rotation.roundToInt())
            val userScales = computeUserScales(
                contentSize = rotatedContentSize.toCompatIntSize(),
                contentOriginSize = rotatedContentOriginSize.toCompatIntSize(),
                containerSize = containerSize.toCompatIntSize(),
                scaleMode = contentScale.toScaleMode(),
                baseScale = contentScale.computeScaleFactor(
                    srcSize = rotatedContentSize.toSize(),
                    dstSize = containerSize.toSize()
                ).toCompatScaleFactor(),
                defaultMediumScaleMultiple = defaultMediumScaleMultiple,
            )
            baseTransform = computeTransform(
                contentSize = rotatedContentSize,
                containerSize = containerSize,
                contentScale = contentScale,
                alignment = contentAlignment,
            )
            minScale = userScales[0] * baseTransform.scaleX
            // todo 清明上河图图片示例，垂直方向上，没有充满屏幕，貌似是基础 Image 的缩放比例跟预想的不一样，导致计算出来的 mediumScale 应用后图片显示没有充满屏幕
            mediumScale = userScales[1] * baseTransform.scaleX
            maxScale = userScales[2] * baseTransform.scaleX
            val readModeResult = contentScale.supportReadMode() &&
                    readMode?.should(
                        srcSize = rotatedContentSize.toCompatIntSize(),
                        dstSize = containerSize.toCompatIntSize()
                    ) == true
            initialUserTransform = if (readModeResult) {
                val readModeTransform = computeReadModeTransform(
                    contentSize = rotatedContentSize,
                    containerSize = containerSize,
                    contentScale = contentScale,
                    alignment = contentAlignment,
                )
                readModeTransform.div(baseTransform.scale)
            } else {
                Transform.Origin
            }
        }
        val limitedInitialUserTransform = limitUserTransform(initialUserTransform)
        logger.d {
            "reset:$caller. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentOriginSize=${contentOriginSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "contentAlignment=${contentAlignment.name}, " +
                    "readMode=${readMode}, " +
                    "minScale=${minScale.format(4)}, " +
                    "mediumScale=${mediumScale.format(4)}, " +
                    "maxScale=${maxScale.format(4)}, " +
                    "baseTransform=${baseTransform.toShortString()}, " +
                    "initialUserTransform=${initialUserTransform.toShortString()}, " +
                    "limitedInitialUserTransform=${limitedInitialUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedInitialUserTransform,
            animated = false,
            caller = "reset"
        )
    }


    suspend fun scale(
        targetScale: Float,
        centroid: Offset = Offset(x = containerSize.width / 2f, y = containerSize.height / 2f),
        animated: Boolean = false,
        rubberBandScale: Boolean = false,
    ) {
        stopAllAnimation("scale")

        val targetUserScale = targetScale / baseTransform.scaleX
        val limitedTargetUserScale = if (rubberBandScale && this@ZoomableState.rubberBandScale) {
            limitUserScaleWithRubberBand(targetUserScale)
        } else {
            limitUserScale(targetUserScale)
        }
        val currentUserTransform = userTransform
        val currentUserScale = currentUserTransform.scaleX
        val currentUserOffset = currentUserTransform.offset
        val targetUserOffset = computeScaleOffsetByCentroid(
            currentScale = currentUserScale,
            currentOffset = currentUserOffset,
            targetScale = limitedTargetUserScale,
            centroid = centroid,
            gestureRotate = 0f,
        )
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
                    "targetUserScale=${targetUserScale.format(4)}, " +
                    "centroid=${centroid.toShortString()}, " +
                    "animated=${animated}, " +
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

    suspend fun offset(targetOffset: Offset, animated: Boolean = false) {
        stopAllAnimation("offset")

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
                    "targetUserOffset=${targetUserOffset.toShortString()}, " +
                    "animated=${animated}, " +
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

    suspend fun location(
        contentPoint: IntOffset,
        targetScale: Float = transform.scaleX,
        animated: Boolean = false,
    ) {
        stopAllAnimation("location")

        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentUserTransform = userTransform

        val rotatedContentPoint =
            contentPoint.rotateInContainer(contentSize, transform.rotation.roundToInt())
        val containerPoint = contentPointToContainerPoint(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            contentPoint = rotatedContentPoint
        )

        val targetUserScale = targetScale / baseTransform.scaleX
        val limitedTargetUserScale = limitUserScale(targetUserScale)

        val targetUserOffset = computeLocationUserOffset(
            containerSize = containerSize,
            containerPoint = containerPoint,
            userScale = limitedTargetUserScale,
        )
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

    suspend fun fling(velocity: Velocity, density: Density) {
        stopAllAnimation("fling")

        val currentUserTransform = userTransform
        val startUserOffset = currentUserTransform.offset
        val flingAnimatable = Animatable(
            initialValue = startUserOffset,
            typeConverter = Offset.VectorConverter,
        )
        this.lastFlingAnimatable = flingAnimatable
        coroutineScope {
            var job: Job? = null
            job = launch {
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
                                        "velocity=$velocity, " +
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

    suspend fun switchScale(
        contentCentroid: IntOffset? = null,
        animated: Boolean = true
    ): Float {
        val contentPoint = contentCentroid
            ?: contentVisibleRect.takeIf { !it.isEmpty }?.center
            ?: contentSize.takeIf { it.isNotEmpty() }?.center
            ?: return transform.scaleX
        val nextScale = getNextStepScale()
        location(
            contentPoint = contentPoint,
            targetScale = nextScale,
            animated = animated
        )
        return nextScale
    }

    suspend fun reboundUserScale(centroid: Offset) {
        val minScale = minScale
        val maxScale = maxScale
        val currentScale = transform.scaleX
        val targetScale = when {
            currentScale.format(2) > maxScale.format(2) -> maxScale
            currentScale.format(2) < minScale.format(2) -> minScale
            else -> null
        }
        if (targetScale != null) {
            scale(
                targetScale = targetScale,
                centroid = centroid,
                animated = true,
                rubberBandScale = false
            )
        }
    }

    fun getNextStepScale(): Float {
        val stepScales = if (threeStepScale) {
            floatArrayOf(minScale, mediumScale, maxScale)
        } else {
            floatArrayOf(minScale, mediumScale)
        }
        return calculateNextStepScale(stepScales, transform.scaleX)
    }

    suspend fun stopAllAnimation(caller: String) {
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

    fun canDrag(horizontal: Boolean, direction: Int): Boolean =
        canScroll(horizontal, direction * -1, scrollEdge)

    @Suppress("UnnecessaryVariable")
    fun touchPointToContentPoint(touchPoint: Offset): IntOffset {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return IntOffset.Zero
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return IntOffset.Zero
        val currentUserTransform = userTransform
        val containerPoint = touchPointToContainerPoint(
            containerSize = containerSize,
            userScale = currentUserTransform.scaleX,
            userOffset = currentUserTransform.offset,
            touchPoint = touchPoint
        )
        val contentPoint = containerPointToContentPoint(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            containerPoint = containerPoint
        )
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
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
            userScale = userScale
        )
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

    private fun limitUserTransform(userTransform: Transform): Transform {
        val limitedUserScale = limitUserScale(userTransform.scaleX)
        val limitedUserOffset = limitUserOffset(userTransform.offset, limitedUserScale)
        return if (limitedUserScale != userTransform.scaleX || limitedUserOffset != userTransform.offset) {
            userTransform.copy(
                scale = ScaleFactor(limitedUserScale),
                offset = limitedUserOffset,
            )
        } else {
            userTransform
        }
    }

    private suspend fun updateUserTransform(
        targetUserTransform: Transform,
        animated: Boolean,
        caller: String
    ) {
        stopAllAnimation(caller)

        if (animated) {
            val currentUserTransform = userTransform
            val scaleChange = currentUserTransform.scale != targetUserTransform.scale
            val updateAnimatable = Animatable(0f)
            this.lastScaleAnimatable = updateAnimatable
            coroutineScope {
                launch {
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
                }
            }
        } else {
            this.userTransform = targetUserTransform
        }
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
                "userTransform=${userTransform.toShortString()}" +
                ")"
}