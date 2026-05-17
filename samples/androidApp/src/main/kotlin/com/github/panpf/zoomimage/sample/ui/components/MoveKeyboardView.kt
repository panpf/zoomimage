package com.github.panpf.zoomimage.sample.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.customview.widget.ViewDragHelper
import com.github.panpf.tools4k.lang.asOrThrow
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.util.OffsetCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MoveKeyboardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    val thumbView: ImageView
    private val dragHelper: ViewDragHelper
    private var gamePadOffset = OffsetCompat(0f, 0f)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var maxStep: OffsetCompat = OffsetCompat(0f, 0f)
    private val stepInterval = 8L
    private val _moveFlow = MutableSharedFlow<OffsetCompat>()

    val moveFlow: SharedFlow<OffsetCompat> = _moveFlow

    init {
        LayoutInflater.from(context).inflate(R.layout.view_move_keyboard, this, true)
        thumbView = findViewById(R.id.thumb)
        dragHelper = ViewDragHelper.create(thumbView.parent as ViewGroup, MyCallback(this))
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return dragHelper.shouldInterceptTouchEvent(ev!!)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        dragHelper.processTouchEvent(event!!)
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (dragHelper.continueSettling(true)) {
            this.postInvalidateOnAnimation()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        maxStep = OffsetCompat(width / 20f, height / 20f)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lastJob?.cancel("onDetachedFromWindow")
    }

    private fun notifyOnMove(offset: OffsetCompat) {
        if (this.gamePadOffset != offset) {
            this.gamePadOffset = offset
            startMove()
        }
    }

    private var lastJob: Job? = null
    private fun startMove() {
        lastJob?.cancel("startMove")
        val gamePadOffset = gamePadOffset.takeIf { it.x != 0f && it.y != 0f } ?: return
        val parent = thumbView.parent.asOrThrow<ViewGroup>()
        val xSpace = (parent.width - thumbView.width) / 2f
        val ySpace = (parent.height - thumbView.height) / 2f
        val move = OffsetCompat(
            x = (gamePadOffset.x / xSpace) * maxStep.x,
            y = (gamePadOffset.y / ySpace) * maxStep.y
        )
        lastJob = coroutineScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val time = currentTime - startTime
                val scale = when {
                    time < 2000 -> 1f
                    time < 4000 -> 2f
                    time < 8000 -> 4f
                    else -> 8f
                }
                _moveFlow.emit(move * scale)
                delay(stepInterval)
            }
        }
    }

    private class MyCallback(val view: MoveKeyboardView) : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child.id == R.id.thumb
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            val parent = child.parent.asOrThrow<ViewGroup>()
            return left.coerceIn(0, parent.width - child.width)
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val parent = child.parent.asOrThrow<ViewGroup>()
            return top.coerceIn(0, parent.height - child.height)
        }

        override fun onViewDragStateChanged(state: Int) {
            if (state == ViewDragHelper.STATE_DRAGGING) {
                view.parent.requestDisallowInterceptTouchEvent(true)
            } else if (state == ViewDragHelper.STATE_IDLE) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val parent = releasedChild.parent.asOrThrow<ViewGroup>()
            val toLeft = (parent.width - releasedChild.width) / 2
            val toTop = (parent.height - releasedChild.height) / 2
            view.dragHelper.smoothSlideViewTo(releasedChild, toLeft, toTop)
            view.postInvalidateOnAnimation()
        }

        override fun onViewPositionChanged(
            changedView: View, left: Int, top: Int, dx: Int, dy: Int
        ) {
            val parent = changedView.parent.asOrThrow<ViewGroup>()
            val toLeft = (parent.width - changedView.width) / 2
            val toTop = (parent.height - changedView.height) / 2
            val offset =
                OffsetCompat(changedView.left - toLeft.toFloat(), changedView.top - toTop.toFloat())
            view.notifyOnMove(offset)
        }
    }
}