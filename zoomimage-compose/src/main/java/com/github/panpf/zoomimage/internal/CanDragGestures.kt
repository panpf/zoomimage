package com.github.panpf.zoomimage.internal

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import kotlin.math.abs
import kotlin.math.sign

/**
 * Copied from androidx.compose.foundation.gestures.detectDragGestures() extends the [canDrag]
 * parameter to achieve the effect that you can drag to the boundary, and is suitable for nesting in Pager
 *
 * Gesture detector that waits for pointer down and touch slop in any direction and then
 * calls [onDrag] for each drag event. It follows the touch slop detection of
 * [awaitTouchSlopOrCancellation] but will consume the position change automatically
 * once the touch slop has been crossed.
 *
 * [onDragStart] called when the touch slop has been passed and includes an [Offset] representing
 * the last known pointer position relative to the containing element. The [Offset] can be outside
 * the actual bounds of the element itself meaning the numbers can be negative or larger than the
 * element bounds if the touch target is smaller than the
 * [ViewConfiguration.minimumTouchTargetSize].
 *
 * [onDragEnd] is called after all pointers are up and [onDragCancel] is called if another gesture
 * has consumed pointer input, canceling this gesture.
 */
internal suspend fun PointerInputScope.detectCanDragGestures(
    canDrag: (horizontally: Boolean, direction: Int) -> Boolean,
    onDragStart: (Offset) -> Unit = { },
    onDragEnd: (velocity: Velocity) -> Unit = { },
    onDragCancel: () -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var drag: PointerInputChange?
        var overSlop = Offset.Zero
        val velocityTracker = VelocityTracker()
        do {
            drag = awaitPointerSlopOrCancellation(
                pointerId = down.id,
                pointerType = down.type,
                velocityTracker = velocityTracker,
                canDrag = canDrag,
                triggerOnMainAxisSlop = false
            ) { change, over ->
                change.consume()
                overSlop = over
            }
        } while (drag != null && !drag.isConsumed && overSlop != Offset.Zero)
        if (drag != null) {
            onDragStart.invoke(drag.position)
            onDrag(drag, overSlop)
            if (
                !drag(drag.id) {
                    velocityTracker.addPointerInputChange(it)
                    onDrag(it, it.positionChange())
                    it.consume()
                }
            ) {
                onDragCancel()
            } else {
                onDragEnd(velocityTracker.calculateVelocity())
            }
        }
    }
}

/**
 * Waits for drag motion along one axis based on [PointerDirectionConfig.mainAxisDelta] to pass
 * pointer slop, using [pointerId] as the pointer to examine if [triggerOnMainAxisSlop] is true.
 * Otherwise, it will wait slop to be crossed on any axis. If [pointerId] is raised, another pointer
 * from those that are down will be chosen to lead the gesture, and if none are down,
 * `null` is returned. If [pointerId] is not down when [awaitPointerSlopOrCancellation] is called,
 * then `null` is returned.
 *
 * When pointer slop is detected, [onPointerSlopReached] is called with the change and the distance
 * beyond the pointer slop. [PointerDirectionConfig.mainAxisDelta] should return the position
 * change in the direction of the drag axis. If [onPointerSlopReached] does not consume the
 * position change, pointer slop will not have been considered detected and the detection will
 * continue or, if it is consumed, the [PointerInputChange] that was consumed will be returned.
 *
 * This works with [awaitTouchSlopOrCancellation] for the other axis to ensure that only horizontal
 * or vertical dragging is done, but not both.
 *
 * [PointerDirectionConfig.offsetFromChanges] should return the offset considering x/y coordinates
 * positioning and main/cross axis nomenclature. This means if the main axis is Y, we should add
 * mainChange to the Y position of the resulting offset and vice versa.
 *
 * @return The [PointerInputChange] of the event that was consumed in [onPointerSlopReached] or
 * `null` if all pointers are raised or the position change was consumed by another gesture
 * detector.
 */
internal suspend inline fun AwaitPointerEventScope.awaitPointerSlopOrCancellation(
    pointerId: PointerId,
    pointerType: PointerType,
    velocityTracker: VelocityTracker,
    pointerDirectionConfig: PointerDirectionConfig = HorizontalPointerDirectionConfig,
    triggerOnMainAxisSlop: Boolean = true,
    canDrag: (horizontally: Boolean, direction: Int) -> Boolean,
    onPointerSlopReached: (PointerInputChange, Offset) -> Unit,
): PointerInputChange? {
    if (currentEvent.isPointerUp(pointerId)) {
        return null // The pointer has already been lifted, so the gesture is canceled
    }
    val touchSlop = viewConfiguration.pointerSlop(pointerType)
    var pointer: PointerId = pointerId
    var totalMainPositionChange = 0f
    var totalCrossPositionChange = 0f

    while (true) {
        val event = awaitPointerEvent()
        val dragEvent = event.changes.fastFirstOrNull { it.id == pointer } ?: return null
        velocityTracker.addPointerInputChange(dragEvent)
        if (dragEvent.isConsumed) {
            return null
        } else if (dragEvent.changedToUpIgnoreConsumed()) {
            val otherDown = event.changes.fastFirstOrNull { it.pressed }
            if (otherDown == null) {
                // This is the last "up"
                return null
            } else {
                pointer = otherDown.id
            }
        } else {
            val currentPosition = dragEvent.position
            val previousPosition = dragEvent.previousPosition

            val mainPositionChange = pointerDirectionConfig.mainAxisDelta(currentPosition) -
                    pointerDirectionConfig.mainAxisDelta(previousPosition)

            val crossPositionChange = pointerDirectionConfig.crossAxisDelta(currentPosition) -
                    pointerDirectionConfig.crossAxisDelta(previousPosition)
            totalMainPositionChange += mainPositionChange
            totalCrossPositionChange += crossPositionChange

            val inDirection = if (triggerOnMainAxisSlop) {
                abs(totalMainPositionChange)
            } else {
                pointerDirectionConfig.offsetFromChanges(
                    totalMainPositionChange,
                    totalCrossPositionChange
                ).getDistance()
            }
            if (inDirection < touchSlop) {
                // verify that nothing else consumed the drag event
                awaitPointerEvent(PointerEventPass.Final)
                if (dragEvent.isConsumed) {
                    return null
                }
            } else {
                val postSlopOffset = if (triggerOnMainAxisSlop) {
                    val finalMainPositionChange = totalMainPositionChange -
                            (sign(totalMainPositionChange) * touchSlop)
                    pointerDirectionConfig.offsetFromChanges(
                        finalMainPositionChange,
                        totalCrossPositionChange
                    )
                } else {
                    val offset = pointerDirectionConfig.offsetFromChanges(
                        totalMainPositionChange,
                        totalCrossPositionChange
                    )
                    val touchSlopOffset = offset / inDirection * touchSlop
                    offset - touchSlopOffset
                }

                val canDragged = if (abs(postSlopOffset.x) > abs(postSlopOffset.y)) {
                    postSlopOffset.x != 0f && canDrag(true, if (postSlopOffset.x > 0f) 1 else -1)
                } else {
                    postSlopOffset.y != 0f && canDrag(false, if (postSlopOffset.y > 0f) 1 else -1)
                }
                if (!canDragged) {
                    return null
                }

                onPointerSlopReached(dragEvent, postSlopOffset)
                if (dragEvent.isConsumed) {
                    return dragEvent
                } else {
                    totalMainPositionChange = 0f
                    totalCrossPositionChange = 0f
                }
            }
        }
    }
}

/**
 * Configures the calculations to convert offset to deltas in the Main and Cross Axis.
 * [offsetFromChanges] will also change depending on implementation.
 */
internal interface PointerDirectionConfig {
    fun mainAxisDelta(offset: Offset): Float
    fun crossAxisDelta(offset: Offset): Float
    fun offsetFromChanges(mainChange: Float, crossChange: Float): Offset
}

/**
 * Used for monitoring changes on X axis.
 */
internal val HorizontalPointerDirectionConfig = object :
    PointerDirectionConfig {
    override fun mainAxisDelta(offset: Offset): Float = offset.x
    override fun crossAxisDelta(offset: Offset): Float = offset.y
    override fun offsetFromChanges(mainChange: Float, crossChange: Float): Offset =
        Offset(mainChange, crossChange)
}

///**
// * Used for monitoring changes on Y axis.
// */
//internal val VerticalPointerDirectionConfig = object :
//    PointerDirectionConfig {
//    override fun mainAxisDelta(offset: Offset): Float = offset.y
//
//    override fun crossAxisDelta(offset: Offset): Float = offset.x
//
//    override fun offsetFromChanges(mainChange: Float, crossChange: Float): Offset =
//        Offset(crossChange, mainChange)
//}

private fun PointerEvent.isPointerUp(pointerId: PointerId): Boolean =
    changes.fastFirstOrNull { it.id == pointerId }?.pressed != true

// This value was determined using experiments and common sense.
// We can't use zero slop, because some hypothetical desktop/mobile devices can send
// pointer events with a very high precision (but I haven't encountered any that send
// events with less than 1px precision)
private val mouseSlop = 0.125.dp
private val defaultTouchSlop = 18.dp // The default touch slop on Android devices
private val mouseToTouchSlopRatio = mouseSlop / defaultTouchSlop

// TODO(demin): consider this as part of ViewConfiguration class after we make *PointerSlop*
//  functions public (see the comment at the top of the file).
//  After it will be a public API, we should get rid of `touchSlop / 144` and return absolute
//  value 0.125.dp.toPx(). It is not possible right now, because we can't access density.
internal fun ViewConfiguration.pointerSlop(pointerType: PointerType): Float {
    return when (pointerType) {
        PointerType.Mouse -> touchSlop * mouseToTouchSlopRatio
        else -> touchSlop
    }
}
