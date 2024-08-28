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

package com.github.panpf.zoomimage.compose.zoom.internal

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlin.math.PI
import kotlin.math.abs


/**
 * Copied from androidx.compose.foundation.gestures.detectTransformGestures(),
 * adds the canDrag parameter to determine whether it can be dragged to be compatible with Pager,
 * adds the onEnd parameter to call back Velocity used for fling at the end of the gesture.
 *
 * A gesture detector for zoom. Once touch slop has been reached, the
 * user can use zoom gestures. [onGesture] will be called when any of the zoom occurs, passing the zoom in scale factor and
 * pan as an offset in pixels. Each of these changes is a difference between the previous call
 * and the current gesture. This will consume all position changes after touch slop has
 * been reached. [onGesture] will also provide centroid of all the pointers that are down.
 *
 * If [panZoomLock] is `true`, rotation is allowed only if touch slop is detected for rotation
 * before pan or zoom motions. If not, pan and zoom gestures will be detected, but rotation
 * gestures will not be. If [panZoomLock] is `false`, once touch slop is reached, all three
 * gestures are detected.
 */
internal suspend fun PointerInputScope.detectPowerfulTransformGestures(
    panZoomLock: Boolean = false,
    canDrag: (horizontal: Boolean, direction: Int) -> Boolean,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float, pointCount: Int) -> Unit,
    onEnd: (centroid: Offset, velocity: Velocity) -> Unit = { _, _ -> },
) {
    awaitEachGesture {
        var rotation = 0f
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        var lockedToPanZoom = false
        var lastCentroid: Offset? = null
        val velocityTracker = VelocityTracker()
        var notInterceptEventsUntilUp: Boolean
        awaitFirstDown(requireUnconsumed = false)
        notInterceptEventsUntilUp = false
        var maxPointCount = 0
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.fastAny { it.isConsumed }
            if (!canceled) {
                val zoomChange = event.calculateZoom()
                val rotationChange = event.calculateRotation()
                val panChange = event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    rotation += rotationChange
                    pan += panChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                    val panMotion = pan.getDistance()

                    val canDragged = if (abs(panChange.x) > abs(panChange.y)) {
                        panChange.x != 0f && canDrag(true, if (panChange.x > 0f) -1 else 1)
                    } else {
                        panChange.y != 0f && canDrag(false, if (panChange.y > 0f) -1 else 1)
                    }
                    if (zoomMotion > touchSlop ||
                        rotationMotion > touchSlop ||
                        (!notInterceptEventsUntilUp && panMotion > touchSlop && canDragged) ||
                        event.changes.size > 1 // Avoid triggering Pager's swipe at the smallest zoom factor and multi-finger touch
                    ) {
                        pastTouchSlop = true
                        lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                    }

                    // Once you decide to give up the event, you can no longer consume the event until the end. This is very necessary when compatibility with Pager
                    if (!pastTouchSlop && !notInterceptEventsUntilUp && panMotion > touchSlop && !canDragged) {
                        notInterceptEventsUntilUp = true
                    }
                }

                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                    if (effectiveRotation != 0f ||
                        zoomChange != 1f ||
                        panChange != Offset.Zero
                    ) {
                        velocityTracker.addPointerInputChange(event.changes.first())
                        val pointCount = event.changes.size
                        maxPointCount = maxOf(maxPointCount, pointCount)
                        lastCentroid = centroid
                        onGesture(centroid, panChange, zoomChange, effectiveRotation, pointCount)
                    }
                    event.changes.fastForEach {
                        if (it.positionChanged()) {
                            it.consume()
                        }
                    }
                }
            }
        } while (!canceled && event.changes.fastAny { it.pressed })

        if (lastCentroid != null) {
            // Only allows one-finger dragging and flinging
            val velocity = if (maxPointCount == 1) {
                velocityTracker.calculateVelocity()
            } else {
                Velocity.Zero
            }
            onEnd(lastCentroid, velocity)
        }
    }
}