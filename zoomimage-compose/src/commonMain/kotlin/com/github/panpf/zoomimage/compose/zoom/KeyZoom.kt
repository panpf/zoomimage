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
import com.github.panpf.zoomimage.compose.util.BaseKeyHandler
import com.github.panpf.zoomimage.compose.util.KeyHandler
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.compose.util.platformAssistKey
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.TimeSource


/**
 * Add keyboard zoom support. Note: You must let the current component gain focus to use it
 *
 * The registered keys are as follows:
 * * scale in: Key.ZoomIn, Key.Equals + meta/ctrl
 * * scale out: Key.ZoomOut, Key.Minus + meta/ctrl
 * * move up: Key.DirectionUp + meta/ctrl
 * * move down: Key.DirectionDown + meta/ctrl
 * * move left: Key.DirectionLeft + meta/ctrl
 * * move right: Key.DirectionRight + meta/ctrl
 */
@Composable
fun Modifier.keyZoom(
    zoomable: ZoomableState,
    keyHandlers: ImmutableList<KeyHandler> = DefaultKeyZoomHandlers
): Modifier {
    val density = LocalDensity.current
    return this.then(KeyZoomElement(zoomable, keyHandlers, density))
}

/**
 * To handle key zoom, you just get the key event and pass it to this function
 *
 * The registered keys are as follows:
 * * scale in: Key.ZoomIn, Key.Equals + meta/ctrl
 * * scale out: Key.ZoomOut, Key.Minus + meta/ctrl
 * * move up: Key.DirectionUp + meta/ctrl
 * * move down: Key.DirectionDown + meta/ctrl
 * * move left: Key.DirectionLeft + meta/ctrl
 * * move right: Key.DirectionRight + meta/ctrl
 */
@Composable
fun keyZoom(
    zoomable: ZoomableState,
    eventFlow: SharedFlow<KeyEvent>,
    keyHandlers: ImmutableList<KeyHandler> = DefaultKeyZoomHandlers,
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    LaunchedEffect(zoomable, keyHandlers) {
        eventFlow.collect { event ->
            keyHandlers.any {
                it.handle(coroutineScope, zoomable, density, event)
            }
        }
    }
}


val DefaultScaleInKeyMatcher = listOf(
    KeyMatcher(key = Key.ZoomIn),
    KeyMatcher(key = Key.Equals, assistKey = platformAssistKey()),
).toImmutableList()

val DefaultScaleOutKeyMatcher = listOf(
    KeyMatcher(key = Key.ZoomOut),
    KeyMatcher(key = Key.Minus, assistKey = platformAssistKey()),
).toImmutableList()

val DefaultMoveUpKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionUp, assistKey = platformAssistKey()),
).toImmutableList()

val DefaultMoveDownKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionDown, assistKey = platformAssistKey())
).toImmutableList()

val DefaultMoveLeftKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionLeft, assistKey = platformAssistKey())
).toImmutableList()

val DefaultMoveRightKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionRight, assistKey = platformAssistKey())
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
) : BaseOperateKeyHandler(keyMatchers) {

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
        density: Density,
        event: KeyEvent
    ): Boolean = if (zoomableState.disabledGestureTypes and GestureType.KEYBOARD_SCALE == 0) {
        super.handle(coroutineScope, zoomableState, density, event)
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
) : BaseOperateKeyHandler(keyMatchers) {

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
        density: Density,
        event: KeyEvent
    ): Boolean = if (zoomableState.disabledGestureTypes and GestureType.KEYBOARD_DRAG == 0) {
        super.handle(coroutineScope, zoomableState, density, event)
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
abstract class BaseOperateKeyHandler(
    override val keyMatchers: ImmutableList<KeyMatcher>,
) : BaseKeyHandler(keyMatchers) {

    private var longPressJob: Job? = null

    /**
     * How many consecutive short presses are expected to be required to go from minimum to maximum?
     */
    abstract val shortPressReachedMaxValueNumber: Int

    /**
     * How long is expected to take to transition from the minimum to the maximum value on a long press
     */
    abstract val longPressReachedMaxValueDuration: Int

    abstract fun getValue(zoomableState: ZoomableState): Float

    abstract fun getValueRange(zoomableState: ZoomableState): ClosedRange<Float>

    abstract fun getShortStepMinValue(zoomableState: ZoomableState): Float?

    override fun onKey(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        density: Density,
        event: KeyEvent
    ) {
        if (event.type == KeyEventType.KeyDown) {
            if (longPressJob == null) {
                performShortPress(coroutineScope, zoomableState)
            }
            startLongPress(coroutineScope, zoomableState)
        } else if (event.type == KeyEventType.KeyUp) {
            cancelLongPress()
        }
    }

    private fun performShortPress(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
    ) {
        // TODO If you press continuously, the speed should become faster and faster. Record the number of short presses and accelerate according to the number of times.
        val motionRange = getValueRange(zoomableState)
        val step = (motionRange.endInclusive - motionRange.start) / shortPressReachedMaxValueNumber
        val shortStepMinValue = getShortStepMinValue(zoomableState) ?: 0f
        val addValue = step.coerceAtLeast(shortStepMinValue)
        zoomableState.logger.d {
            "BaseOperateKeyHandler. onKey. short press. addValue=$addValue. motionRange=$motionRange"
        }
        coroutineScope.launch {
            updateValue(
                zoomableState = zoomableState,
                animationSpec = ZoomAnimationSpec.Default.copy(durationMillis = 150),
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
                // TODO The speed should be getting faster and faster when long pressed, but now it is getting slower and slower.
                val elapsedTime = startTimeMark.elapsedNow().inWholeMilliseconds
                val progress =
                    (elapsedTime / longPressReachedMaxValueDuration.toFloat()).coerceAtMost(1f)
                val progressValue = progress * (motionRange.endInclusive - motionRange.start)
                val addValue = progressValue - lastProgressValue
                lastProgressValue = progressValue
                zoomableState.logger.d {
                    "BaseOperateKeyHandler. onKey. long press running. " +
                            "progress=$progress, " +
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

    private fun cancelLongPress() {
        this.longPressJob?.cancel()
        this.longPressJob = null
    }

    override fun onCanceled(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        density: Density,
        event: KeyEvent
    ) {
        cancelLongPress()
        zoomableState.logger.d {
            "BaseOperateKeyHandler. onCanceled"
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
    val keyHandlers: ImmutableList<KeyHandler>,
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
    var keyHandlers: ImmutableList<KeyHandler>,
    var density: Density
) : KeyInputModifierNode, Modifier.Node() {

    override fun onPreKeyEvent(event: KeyEvent): Boolean = false

    override fun onKeyEvent(event: KeyEvent): Boolean = keyHandlers.any {
        it.handle(coroutineScope, zoomable, density, event)
    }
}