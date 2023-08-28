package com.github.panpf.zoomimage.view.zoom.internal

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.view.zoom.internal.ScaleDragGestureDetector.OnActionListener
import com.github.panpf.zoomimage.view.zoom.internal.ScaleDragGestureDetector.OnGestureListener

class UnifiedGestureDetector(
    view: View,
    onDownCallback: (e: MotionEvent) -> Boolean,
    onSingleTapConfirmedCallback: (e: MotionEvent) -> Boolean,
    onLongPressCallback: (e: MotionEvent) -> Unit,
    onDoubleTapCallback: (e: MotionEvent) -> Boolean,
    onDragCallback: (dx: Float, dy: Float) -> Unit,
    onFlingCallback: (velocityX: Float, velocityY: Float) -> Unit,
    onScaleBeginCallback: () -> Boolean,
    onScaleCallback: (scaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float) -> Unit,
    onScaleEndCallback: (lastFocus: OffsetCompat?) -> Unit,
    onActionDownCallback: (ev: MotionEvent) -> Unit,
    onActionUpCallback: (ev: MotionEvent) -> Unit,
    onActionCancelCallback: (ev: MotionEvent) -> Unit,
    canDrag: (horizontal: Boolean, direction: Int) -> Boolean,
) {

    private val tapGestureDetector =
        GestureDetector(view.context, object : SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                return onDownCallback(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                return onSingleTapConfirmedCallback(e)
            }

            override fun onLongPress(e: MotionEvent) {
                onLongPressCallback(e)
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                return onDoubleTapCallback(e)
            }
        })

    private val scaleDragGestureDetector =
        ScaleDragGestureDetector(view, canDrag, object : OnGestureListener {
            override fun onDrag(dx: Float, dy: Float) =
                onDragCallback(dx, dy)

            override fun onFling(velocityX: Float, velocityY: Float) =
                onFlingCallback(velocityX, velocityY)

            override fun onScaleBegin(): Boolean = onScaleBeginCallback()

            override fun onScale(
                scaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float
            ) = onScaleCallback(scaleFactor, focusX, focusY, dx, dy)

            override fun onScaleEnd(lastFocus: OffsetCompat?) = onScaleEndCallback(lastFocus)
        }).apply {
            onActionListener = object : OnActionListener {
                override fun onActionDown(ev: MotionEvent) = onActionDownCallback(ev)
                override fun onActionUp(ev: MotionEvent) = onActionUpCallback(ev)
                override fun onActionCancel(ev: MotionEvent) = onActionCancelCallback(ev)
            }
        }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val scaleAndDragConsumed = scaleDragGestureDetector.onTouchEvent(event)
        val tapConsumed = tapGestureDetector.onTouchEvent(event)
        return scaleAndDragConsumed || tapConsumed
    }
}