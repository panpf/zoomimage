package com.github.panpf.zoomimage.view.zoom.internal

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import com.github.panpf.zoomimage.view.zoom.internal.ScaleDragGestureDetector.OnActionListener
import com.github.panpf.zoomimage.view.zoom.internal.ScaleDragGestureDetector.OnGestureListener

class UnifiedGestureDetector(
    context: Context,
    onDownCallback: (e: MotionEvent) -> Boolean,
    onSingleTapConfirmedCallback: (e: MotionEvent) -> Boolean,
    onLongPressCallback: (e: MotionEvent) -> Unit,
    onDoubleTapCallback: (e: MotionEvent) -> Boolean,
    onDragCallback: (dx: Float, dy: Float, scaling: Boolean) -> Unit,
    onFlingCallback: (velocityX: Float, velocityY: Float) -> Unit,
    onScaleBeginCallback: () -> Boolean,
    onScaleCallback: (scaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float) -> Unit,
    onScaleEndCallback: () -> Unit,
    onActionDownCallback: (ev: MotionEvent) -> Unit,
    onActionUpCallback: (ev: MotionEvent) -> Unit,
    onActionCancelCallback: (ev: MotionEvent) -> Unit,
) {

    private val tapGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {

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
        ScaleDragGestureDetector(context, object : OnGestureListener {
            override fun onDrag(dx: Float, dy: Float, scaling: Boolean) =
                onDragCallback(dx, dy, scaling)

            override fun onFling(velocityX: Float, velocityY: Float) =
                onFlingCallback(velocityX, velocityY)

            override fun onScaleBegin(): Boolean = onScaleBeginCallback()

            override fun onScale(
                scaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float
            ) = onScaleCallback(scaleFactor, focusX, focusY, dx, dy)

            override fun onScaleEnd() = onScaleEndCallback()
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