package com.github.panpf.zoomimage.view.zoom.internal

import android.view.MotionEvent
import android.view.View
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.center
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.view.zoom.OnViewLongPressListener
import com.github.panpf.zoomimage.view.zoom.OnViewTapListener

class TouchEngine(view: View, zoomEngine: ZoomEngine) {

    private val gestureDetector: UnifiedGestureDetector
    private var onViewTapListenerList: MutableSet<OnViewTapListener>? = null
    private var onViewLongPressListenerList: MutableSet<OnViewLongPressListener>? = null

    init {
        gestureDetector = UnifiedGestureDetector(
            view = view,
            onDownCallback = { true },
            onSingleTapConfirmedCallback = { e: MotionEvent ->
                val onViewTapListenerList = onViewTapListenerList
                onViewTapListenerList?.forEach {
                    it.onViewTap(view, e.x, e.y)
                }
                onViewTapListenerList?.isNotEmpty() == true || view.performClick()
            },
            onLongPressCallback = { e: MotionEvent ->
                val onViewLongPressListenerList = onViewLongPressListenerList
                onViewLongPressListenerList?.forEach {
                    it.onViewLongPress(view, e.x, e.y)
                }
                onViewLongPressListenerList?.isNotEmpty() == true || view.performLongClick()
            },
            onDoubleTapCallback = { e: MotionEvent ->
                val touchPoint = OffsetCompat(x = e.x, y = e.y)
                val centroidContentPoint = zoomEngine.touchPointToContentPoint(touchPoint)
                zoomEngine.switchScale(centroidContentPoint = centroidContentPoint, animated = true)
                true
            },
            onDragCallback = { dx: Float, dy: Float ->
                zoomEngine.transform(
                    centroid = zoomEngine.containerSize.toSize().center,
                    panChange = OffsetCompat(dx, dy),
                    zoomChange = 1f,
                    rotationChange = 0f,
                )
            },
            onFlingCallback = { velocityX: Float, velocityY: Float ->
                zoomEngine.fling(velocityX, velocityY)
            },
            onScaleCallback = { scaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float ->
                zoomEngine.transform(
                    centroid = OffsetCompat(x = focusX, y = focusY),
                    panChange = OffsetCompat(x = dx, y = dy),
                    zoomChange = scaleFactor,
                    rotationChange = 0f,
                )
            },
            onScaleBeginCallback = {
                zoomEngine.transforming = true
                true
            },
            onScaleEndCallback = {
                zoomEngine.transforming = false
                zoomEngine.rollbackScale(it)
            },
            onActionDownCallback = {
                zoomEngine.stopAllAnimation("onActionDown")
            },
            onActionUpCallback = { },
            onActionCancelCallback = { },
            canDrag = { horizontal: Boolean, direction: Int ->
                zoomEngine.canScroll(horizontal, direction)
            }
        )
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    /**
     * Register a single click event listener
     */
    fun registerOnViewTapListener(listener: OnViewTapListener) {
        this.onViewTapListenerList = (onViewTapListenerList ?: LinkedHashSet())
            .apply { add(listener) }
    }

    /**
     * Unregister a single click event listener
     */
    fun unregisterOnViewTapListener(listener: OnViewTapListener): Boolean {
        return onViewTapListenerList?.remove(listener) == true
    }

    /**
     * Register a long press event listener
     */
    fun registerOnViewLongPressListener(listener: OnViewLongPressListener) {
        this.onViewLongPressListenerList = (onViewLongPressListenerList ?: LinkedHashSet())
            .apply { add(listener) }
    }

    /**
     * Unregister a long press event listener
     */
    fun unregisterOnViewLongPressListener(listener: OnViewLongPressListener): Boolean {
        return onViewLongPressListenerList?.remove(listener) == true
    }
}