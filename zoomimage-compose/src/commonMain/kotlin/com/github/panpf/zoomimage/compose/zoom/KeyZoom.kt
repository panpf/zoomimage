/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyInputModifierNode
import androidx.compose.ui.input.key.type
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.github.panpf.zoomimage.compose.util.AssistKey
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.compose.util.platformAssistKey
import com.github.panpf.zoomimage.compose.zoom.internal.MatcherZoomKeyHandler
import com.github.panpf.zoomimage.compose.zoom.internal.ZoomKeyHandler
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.ValueTimeMark


/**
 * Add keyboard zoom support. Note: You must let the current component gain focus to use it
 *
 * The registered keys are as follows:
 * * scale in: Key.ZoomIn, Key.Equals + (meta/ctrl)/alt, Key.DirectionUp + (meta/ctrl)/alt
 * * scale out: Key.ZoomOut, Key.Minus + (meta/ctrl)/alt, Key.DirectionDown + (meta/ctrl)/alt
 * * move up: Key.DirectionUp
 * * move down: Key.DirectionDown
 * * move left: Key.DirectionLeft
 * * move right: Key.DirectionRight
 */
@Composable
fun Modifier.keyZoom(
    zoomable: ZoomableState,
    keyHandlers: ImmutableList<ZoomKeyHandler> = DefaultKeyZoomHandlers
): Modifier {
    val density = LocalDensity.current
    return this.then(KeyZoomElement(zoomable, keyHandlers, density))
}

/**
 * To handle key zoom, you just get the key event and pass it to this function
 *
 * The registered keys are as follows:
 * * scale in: Key.ZoomIn, Key.Equals + (meta/ctrl)/alt, Key.DirectionUp + (meta/ctrl)/alt
 * * scale out: Key.ZoomOut, Key.Minus + (meta/ctrl)/alt, Key.DirectionDown + (meta/ctrl)/alt
 * * move up: Key.DirectionUp
 * * move down: Key.DirectionDown
 * * move left: Key.DirectionLeft
 * * move right: Key.DirectionRight
 */
@Composable
fun keyZoom(
    zoomable: ZoomableState,
    eventFlow: SharedFlow<KeyEvent>,
    keyHandlers: ImmutableList<ZoomKeyHandler> = DefaultKeyZoomHandlers,
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(zoomable, keyHandlers) {
        eventFlow.collect { event ->
            keyHandlers.any {
                it.handle(coroutineScope, zoomable, event)
            }
        }
    }
}


val DefaultScaleInKeyMatcher = listOf(
    KeyMatcher(key = Key.ZoomIn),
    KeyMatcher(key = Key.Equals, assistKey = platformAssistKey()),
    KeyMatcher(key = Key.Equals, assistKey = AssistKey.Alt),
    KeyMatcher(key = Key.DirectionUp, assistKey = AssistKey.Alt),
    KeyMatcher(key = Key.DirectionUp, assistKey = platformAssistKey()),
).toImmutableList()

val DefaultScaleOutKeyMatcher = listOf(
    KeyMatcher(key = Key.ZoomOut),
    KeyMatcher(key = Key.Minus, assistKey = platformAssistKey()),
    KeyMatcher(key = Key.Minus, assistKey = AssistKey.Alt),
    KeyMatcher(key = Key.DirectionDown, assistKey = AssistKey.Alt),
    KeyMatcher(key = Key.DirectionDown, assistKey = platformAssistKey()),
).toImmutableList()

val DefaultMoveUpKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionUp, assistKey = AssistKey.None),
).toImmutableList()

val DefaultMoveDownKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionDown, assistKey = AssistKey.None)
).toImmutableList()

val DefaultMoveLeftKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionLeft, assistKey = AssistKey.None)
).toImmutableList()

val DefaultMoveRightKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionRight, assistKey = AssistKey.None)
).toImmutableList()

val DefaultKeyZoomHandlers = listOf(
    ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatcher, scaleIn = true),
    ScaleKeyHandler(keyMatchers = DefaultScaleOutKeyMatcher, scaleIn = false),
    MoveKeyHandler(keyMatchers = DefaultMoveUpKeyMatchers, arrow = MoveKeyHandler.Arrow.Up),
    MoveKeyHandler(keyMatchers = DefaultMoveDownKeyMatchers, arrow = MoveKeyHandler.Arrow.Down),
    MoveKeyHandler(keyMatchers = DefaultMoveLeftKeyMatchers, arrow = MoveKeyHandler.Arrow.Left),
    MoveKeyHandler(keyMatchers = DefaultMoveRightKeyMatchers, arrow = MoveKeyHandler.Arrow.Right),
).toImmutableList()


@Stable
data class ScaleKeyHandler(
    override val keyMatchers: ImmutableList<KeyMatcher>,
    val scaleIn: Boolean,
    override val shortPressReachedMaxValueNumber: Int = 5,
    override val longPressReachedMaxValueDuration: Int = 3000,
    override val longPressAccelerate: Boolean = true,
) : ZoomMatcherKeyHandler(keyMatchers) {

    override fun getValue(zoomableState: ZoomableState): Float {
        return zoomableState.transform.scaleX
    }

    override fun getValueRange(zoomableState: ZoomableState): ClosedRange<Float> {
        return zoomableState.minScale..zoomableState.maxScale.coerceAtLeast(0f)
    }

    override fun getShortStepMinValue(zoomableState: ZoomableState): Float? = null

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean = if (zoomableState.disabledGestureTypes and GestureType.KEYBOARD_SCALE == 0) {
        super.handle(coroutineScope, zoomableState, event)
    } else {
        false
    }

    override suspend fun updateValue(
        zoomableState: ZoomableState,
        animationSpec: ZoomAnimationSpec?,
        add: Float
    ) {
        zoomableState.scale(
            targetScale = zoomableState.transform.scaleX + addScale(add),
            centroidContentPoint = zoomableState.contentVisibleRect.center,
            animated = animationSpec != null,
            animationSpec = animationSpec
        )
    }

    private fun addScale(scaleStep: Float): Float {
        return if (scaleIn) scaleStep else -scaleStep
    }
}

@Stable
data class MoveKeyHandler(
    override val keyMatchers: ImmutableList<KeyMatcher>,
    val arrow: Arrow,
    override val shortPressReachedMaxValueNumber: Int = 10,
    val shortPressMinStepWithContainerPercentage: Float = 0.25f,
    override val longPressReachedMaxValueDuration: Int = 3000,
    override val longPressAccelerate: Boolean = true,
) : ZoomMatcherKeyHandler(keyMatchers) {

    override fun getValue(zoomableState: ZoomableState): Float {
        return if (arrow == Arrow.Left || arrow == Arrow.Right) {
            zoomableState.transform.offset.x
        } else {
            zoomableState.transform.offset.y
        }
    }

    override fun getValueRange(zoomableState: ZoomableState): ClosedRange<Float> {
        return if (arrow == Arrow.Left || arrow == Arrow.Right) {
            zoomableState.userOffsetBounds.left.toFloat()..zoomableState.userOffsetBounds.right.toFloat()
        } else {
            zoomableState.userOffsetBounds.top.toFloat()..zoomableState.userOffsetBounds.bottom.toFloat()
        }
    }

    override fun getShortStepMinValue(zoomableState: ZoomableState): Float {
        return if (arrow == Arrow.Left || arrow == Arrow.Right) {
            zoomableState.containerSize.width * shortPressMinStepWithContainerPercentage
        } else {
            zoomableState.containerSize.height * shortPressMinStepWithContainerPercentage
        }
    }

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean = if (zoomableState.disabledGestureTypes and GestureType.KEYBOARD_DRAG == 0) {
        super.handle(coroutineScope, zoomableState, event)
    } else {
        false
    }

    override suspend fun updateValue(
        zoomableState: ZoomableState,
        animationSpec: ZoomAnimationSpec?,
        add: Float
    ) {
        zoomableState.offset(
            targetOffset = zoomableState.transform.offset + addOffset(add),
            animated = animationSpec != null,
            animationSpec = animationSpec
        )
    }

    private fun addOffset(add: Float): Offset = when (arrow) {
        Arrow.Up -> Offset(0f, add)
        Arrow.Down -> Offset(0f, -add)
        Arrow.Left -> Offset(add, 0f)
        Arrow.Right -> Offset(-add, 0f)
    }

    enum class Arrow {
        Up, Down, Left, Right
    }
}

@Stable
abstract class ZoomMatcherKeyHandler(
    override val keyMatchers: ImmutableList<KeyMatcher>,
) : MatcherZoomKeyHandler(keyMatchers) {

    private var longPressJob: Job? = null
    private var lastShortPressTimeMark: ValueTimeMark? = null

    /**
     * How many consecutive short presses are expected to be required to go from minimum to maximum?
     */
    abstract val shortPressReachedMaxValueNumber: Int

    /**
     * How long is expected to take to transition from the minimum to the maximum value on a long press
     *
     * Note: [longPressAccelerate] will accelerate changes and only takes half the time of [longPressReachedMaxValueDuration] to reach the maximum value
     */
    abstract val longPressReachedMaxValueDuration: Int

    /**
     * If true, the long press will be accelerated. After acceleration, the long press will only take half the original time to reach the maximum value.
     */
    abstract val longPressAccelerate: Boolean

    abstract fun getValue(zoomableState: ZoomableState): Float

    abstract fun getValueRange(zoomableState: ZoomableState): ClosedRange<Float>

    abstract fun getShortStepMinValue(zoomableState: ZoomableState): Float?

    override fun onKey(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ) {
        if (event.type == KeyEventType.KeyDown) {
            if (longPressJob == null) {
                performShortPress(coroutineScope, zoomableState)
            }
            startLongPress(coroutineScope, zoomableState)
        } else {
            // KeyEventType.KeyUp
            cancelLongPress()
        }
    }

    private fun performShortPress(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
    ) {
        val motionRange = getValueRange(zoomableState)
        val twoShortPressInterval = lastShortPressTimeMark?.elapsedNow()?.inWholeMilliseconds ?: -1
        lastShortPressTimeMark = TimeSource.Monotonic.markNow()
        val step = (motionRange.endInclusive - motionRange.start) / shortPressReachedMaxValueNumber
        val shortStepMinValue = getShortStepMinValue(zoomableState) ?: 0f
        val addValue = step.coerceAtLeast(shortStepMinValue)
        zoomableState.logger.d {
            "ZoomMatcherKeyHandler. onKey. short press. " +
                    "addValue=$addValue, " +
                    "twoShortPressInterval=$twoShortPressInterval. " +
                    "motionRange=$motionRange"
        }
        val animationDurationMillis = if (twoShortPressInterval > 0) {
            min(twoShortPressInterval.toInt(), zoomableState.animationSpec.durationMillis)
        } else {
            zoomableState.animationSpec.durationMillis
        }
        coroutineScope.launch {
            updateValue(
                zoomableState = zoomableState,
                animationSpec = ZoomAnimationSpec.Default.copy(durationMillis = animationDurationMillis),
                add = addValue
            )
        }
    }

    private fun startLongPress(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
    ) {
        if (longPressJob != null) {
            return
        }

        this.longPressJob = coroutineScope.launch {
            // Normally this is 500, but here for fast response, it is set to 200
            delay(200)

            val motionRange = getValueRange(zoomableState)
            val startTimeMark = TimeSource.Monotonic.markNow()
            var lastProgressValue = 0f
            while (isActive) {
                val elapsedTime = startTimeMark.elapsedNow().inWholeMilliseconds
                val progress =
                    (elapsedTime / longPressReachedMaxValueDuration.toFloat()).coerceAtMost(1f)
                val acceleratedProgress = accelerateProgress(progress)
                val progressValue =
                    acceleratedProgress * (motionRange.endInclusive - motionRange.start)
                val addValue = progressValue - lastProgressValue
                lastProgressValue = progressValue
                zoomableState.logger.d {
                    "ZoomMatcherKeyHandler. onKey. long press running. " +
                            "progress=$progress, " +
                            "acceleratedProgress=$acceleratedProgress, " +
                            "progressValue=$progressValue, " +
                            "addValue=$addValue, " +
                            "elapsedTime=$elapsedTime, " +
                            "motionRange=$motionRange"
                }
                updateValue(zoomableState, animationSpec = null, addValue)

                // Usually, on a device with a refresh rate of 60 frames, it can be refreshed once every 16 milliseconds,
                // but considering that most mobile devices already have a refresh rate of 120 frames, so 8
                delay(8)
            }
        }
    }

    private fun accelerateProgress(fraction: Float): Float {
        return if (longPressAccelerate) {
            fraction * ((fraction * 2) + 1)
        } else {
            fraction
        }
    }

    private fun cancelLongPress() {
        this.longPressJob?.cancel()
        this.longPressJob = null
    }

    override fun onCanceled(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ) {
        cancelLongPress()
        zoomableState.logger.d {
            "ZoomMatcherKeyHandler. onCanceled"
        }
    }

    abstract suspend fun updateValue(
        zoomableState: ZoomableState,
        animationSpec: ZoomAnimationSpec? = null,
        add: Float
    )
}


internal data class KeyZoomElement(
    val zoomable: ZoomableState,
    val keyHandlers: ImmutableList<ZoomKeyHandler>,
    val density: Density
) : ModifierNodeElement<KeyZoomNode>() {

    override fun create(): KeyZoomNode {
        return KeyZoomNode(zoomable, keyHandlers, density)
    }

    override fun update(node: KeyZoomNode) {
        node.zoomable = zoomable
        node.keyHandlers = keyHandlers
        node.density = density
    }
}

internal data class KeyZoomNode(
    var zoomable: ZoomableState,
    var keyHandlers: ImmutableList<ZoomKeyHandler>,
    var density: Density
) : KeyInputModifierNode, Modifier.Node() {

    override fun onPreKeyEvent(event: KeyEvent): Boolean = false

    override fun onKeyEvent(event: KeyEvent): Boolean = keyHandlers.any {
        it.handle(coroutineScope, zoomable, event)
    }
}