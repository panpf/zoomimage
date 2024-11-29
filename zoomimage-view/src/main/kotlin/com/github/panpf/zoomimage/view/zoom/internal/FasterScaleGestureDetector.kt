/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.panpf.zoomimage.view.zoom.internal

import android.content.Context
import android.os.Build
import android.os.Handler
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.github.panpf.zoomimage.view.zoom.internal.FasterScaleGestureDetector.OnScaleGestureListener
import kotlin.math.abs
import kotlin.math.hypot

/**
 * Detects scaling transformation gestures using the supplied [MotionEvent]s.
 * The [OnScaleGestureListener] callback will notify users when a particular
 * gesture event has occurred.
 *
 * This class should only be used with [MotionEvent]s reported via touch.
 *
 * To use this class:
 *
 *  * Create an instance of the `ScaleGestureDetector` for your
 * [View]
 *  * In the [View.onTouchEvent] method ensure you call
 * [.onTouchEvent]. The methods defined in your
 * callback will be executed when the events occur.
 *
 *
 * Copied from API 34 android.view.ScaleGestureDetector, modified the value of the mMinSpan attribute so that the zoom gesture can start faster
 */
class FasterScaleGestureDetector @JvmOverloads constructor(
    private val mContext: Context,
    private val mListener: OnScaleGestureListener,
    handler: Handler? = null
) {

    companion object {
        private const val TAG = "FasterScaleGestureDetector"

        private const val TOUCH_STABILIZE_TIME: Long = 128 // ms
        private const val SCALE_FACTOR = .5f
        private const val ANCHORED_SCALE_MODE_NONE = 0
        private const val ANCHORED_SCALE_MODE_DOUBLE_TAP = 1
        private const val ANCHORED_SCALE_MODE_STYLUS = 2
    }

    /**
     * The listener for receiving notifications when gestures occur.
     * If you want to listen for all the different gestures then implement
     * this interface. If you only want to listen for a subset it might
     * be easier to extend [SimpleOnScaleGestureListener].
     *
     * An application will receive events in the following order:
     *
     *  * One [OnScaleGestureListener.onScaleBegin]
     *  * Zero or more [OnScaleGestureListener.onScale]
     *  * One [OnScaleGestureListener.onScaleEnd]
     *
     */
    interface OnScaleGestureListener {
        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        fun onScale(detector: FasterScaleGestureDetector): Boolean

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        fun onScaleBegin(detector: FasterScaleGestureDetector): Boolean

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         *
         * Once a scale has ended, [FasterScaleGestureDetector.getFocusX]
         * and [FasterScaleGestureDetector.getFocusY] will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         */
        fun onScaleEnd(detector: FasterScaleGestureDetector)
    }

    /**
     * A convenience class to extend when you only want to listen for a subset
     * of scaling-related events. This implements all methods in
     * [OnScaleGestureListener] but does nothing.
     * [OnScaleGestureListener.onScale] returns
     * `false` so that a subclass can retrieve the accumulated scale
     * factor in an overridden onScaleEnd.
     * [OnScaleGestureListener.onScaleBegin] returns
     * `true`.
     */
    class SimpleOnScaleGestureListener : OnScaleGestureListener {
        override fun onScale(detector: FasterScaleGestureDetector): Boolean {
            return false
        }

        override fun onScaleBegin(detector: FasterScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleEnd(detector: FasterScaleGestureDetector) {
            // Intentionally empty
        }
    }

    private val mSpanSlop: Int
    private val mMinSpan: Int
    private val mHandler: Handler?
    private var mInitialSpan = 0f
    private var mAnchoredScaleStartX = 0f
    private var mAnchoredScaleStartY = 0f
    private var mAnchoredScaleMode = ANCHORED_SCALE_MODE_NONE
    private var mGestureDetector: GestureDetector? = null
    private var mEventBeforeOrAboveStartingGestureEvent = false

    /**
     * Set whether the associated [OnScaleGestureListener] should receive onScale callbacks
     * when the user performs a doubleTap followed by a swipe. Note that this is enabled by default
     * if the app targets API 19 and newer.
     * scales true to enable quick scaling, false to disable
     */
    var isQuickScaleEnabled: Boolean = false
        set(scales) {
            field = scales
            if (field && mGestureDetector == null) {
                val gestureListener: SimpleOnGestureListener =
                    object : SimpleOnGestureListener() {
                        override fun onDoubleTap(e: MotionEvent): Boolean {
                            // Double tap: start watching for a swipe
                            mAnchoredScaleStartX = e.x
                            mAnchoredScaleStartY = e.y
                            mAnchoredScaleMode =
                                ANCHORED_SCALE_MODE_DOUBLE_TAP
                            return true
                        }
                    }
                mGestureDetector = GestureDetector(mContext, gestureListener, mHandler)
            }
        }

    /**
     * Return the scaling factor from the previous scale event to the current
     * event. This value is defined as
     * ([.getCurrentSpan] / [.getPreviousSpan]).
     *
     * @return The current scaling factor.
     */
    val scaleFactor: Float
        get() {
            if (inAnchoredScaleMode()) {
                // Drag is moving up; the further away from the gesture
                // start, the smaller the span should be, the closer,
                // the larger the span, and therefore the larger the scale
                val scaleUp =
                    (mEventBeforeOrAboveStartingGestureEvent && (currentSpan < previousSpan)) ||
                            (!mEventBeforeOrAboveStartingGestureEvent && (currentSpan > previousSpan))
                val spanDiff =
                    ((abs((1 - (currentSpan / previousSpan)).toDouble()) * SCALE_FACTOR).toFloat())
                return if (previousSpan <= mSpanSlop) 1f else if (scaleUp) (1 + spanDiff) else (1 - spanDiff)
            }
            return if (previousSpan > 0) currentSpan / previousSpan else 1f
        }

    /**
     * Return the time difference in milliseconds between the previous
     * accepted scaling event and the current scaling event.
     *
     * @return Time difference since the last scaling event in milliseconds.
     */
    val timeDelta: Long
        get() = eventTime - mPrevTime

    /**
     * Get the X coordinate of the current gesture's focal point.
     * If a gesture is in progress, the focal point is between
     * each of the pointers forming the gesture.
     *
     * If [.isInProgress] would return false, the result of this
     * function is undefined.
     *
     * @return X coordinate of the focal point in pixels.
     */
    var focusX: Float = 0f
        private set

    /**
     * Get the Y coordinate of the current gesture's focal point.
     * If a gesture is in progress, the focal point is between
     * each of the pointers forming the gesture.
     *
     * If [.isInProgress] would return false, the result of this
     * function is undefined.
     *
     * @return Y coordinate of the focal point in pixels.
     */
    var focusY: Float = 0f
        private set

    /**
     * Sets whether the associates [OnScaleGestureListener] should receive
     * onScale callbacks when the user uses a stylus and presses the button.
     * Note that this is enabled by default if the app targets API 23 and newer.
     *
     * @param scales true to enable stylus scaling, false to disable.
     */
    var isStylusScaleEnabled: Boolean = false

    /**
     * Return the average distance between each of the pointers forming the
     * gesture in progress through the focal point.
     *
     * @return Distance between pointers in pixels.
     */
    var currentSpan: Float = 0f
        private set

    /**
     * Return the previous average distance between each of the pointers forming the
     * gesture in progress through the focal point.
     *
     * @return Previous distance between pointers in pixels.
     */
    var previousSpan: Float = 0f
        private set

    /**
     * Return the average X distance between each of the pointers forming the
     * gesture in progress through the focal point.
     *
     * @return Distance between pointers in pixels.
     */
    var currentSpanX: Float = 0f
        private set

    /**
     * Return the average Y distance between each of the pointers forming the
     * gesture in progress through the focal point.
     *
     * @return Distance between pointers in pixels.
     */
    var currentSpanY: Float = 0f
        private set

    /**
     * Return the previous average X distance between each of the pointers forming the
     * gesture in progress through the focal point.
     *
     * @return Previous distance between pointers in pixels.
     */
    var previousSpanX: Float = 0f
        private set

    /**
     * Return the previous average Y distance between each of the pointers forming the
     * gesture in progress through the focal point.
     *
     * @return Previous distance between pointers in pixels.
     */
    var previousSpanY: Float = 0f
        private set

    /**
     * Return the event time of the current event being processed.
     *
     * @return Current event time in milliseconds.
     */
    var eventTime: Long = 0
        private set
    private var mPrevTime: Long = 0

    /**
     * Returns `true` if a scale gesture is in progress.
     */
    var isInProgress: Boolean = false
        private set

    /**
     * Creates a ScaleGestureDetector with the supplied listener.
     * You may only use this constructor from a [Looper][android.os.Looper] thread.
     *
     * @param context the application's context
     * @param listener the listener invoked for all the callbacks, this must
     * not be null.
     *
     * @throws NullPointerException if `listener` is null.
     */
    init {
        val viewConfiguration = ViewConfiguration.get(
            mContext
        )
        mSpanSlop = viewConfiguration.scaledTouchSlop * 2
//        mMinSpan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            viewConfiguration.getScaledMinimumScalingSpan()
//        } else {
//            val res = mContext.resources;
////            mMinSpan = res.getDimensionPixelSize(com.android.internal.R.dimen.config_minScalingSpan);
//            val id = res.getIdentifier("config_minScalingSpan", "dimen", "android");
//            res.getDimensionPixelSize(id)
//        }
        // Speed up the sensitivity of zoom start and keep it in sync with compose version
        mMinSpan = mSpanSlop
        mHandler = handler
        // Quick scale is enabled by default after JB_MR2
        val targetSdkVersion = mContext.applicationInfo.targetSdkVersion
        if (targetSdkVersion > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            isQuickScaleEnabled = true
        }
        // Stylus scale is enabled by default after LOLLIPOP_MR1
        if (targetSdkVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            isStylusScaleEnabled = true
        }
    }

    /**
     * Accepts MotionEvents and dispatches events to a [OnScaleGestureListener]
     * when appropriate.
     *
     *
     * Applications should pass a complete and consistent event stream to this method.
     * A complete and consistent event stream involves all MotionEvents from the initial
     * ACTION_DOWN to the final ACTION_UP or ACTION_CANCEL.
     *
     * @param event The event to process
     * @return true if the event was processed and the detector wants to receive the
     * rest of the MotionEvents in this event stream.
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
//        if (mInputEventConsistencyVerifier != null) {
//            mInputEventConsistencyVerifier.onTouchEvent(event, 0);
//        }

        eventTime = event.eventTime

        val action = event.actionMasked

        // Forward the event to check for double tap gesture
        if (isQuickScaleEnabled) {
            mGestureDetector!!.onTouchEvent(event)
        }

        val count = event.pointerCount
        val isStylusButtonDown =
            (event.buttonState and MotionEvent.BUTTON_STYLUS_PRIMARY) != 0

        val anchoredScaleCancelled =
            mAnchoredScaleMode == ANCHORED_SCALE_MODE_STYLUS && !isStylusButtonDown
        val streamComplete =
            action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || anchoredScaleCancelled

        if (action == MotionEvent.ACTION_DOWN || streamComplete) {
            // Reset any scale in progress with the listener.
            // If it's an ACTION_DOWN we're beginning a new event stream.
            // This means the app probably didn't give us all the events. Shame on it.
            if (isInProgress) {
                mListener.onScaleEnd(this)
                isInProgress = false
                mInitialSpan = 0f
                mAnchoredScaleMode = ANCHORED_SCALE_MODE_NONE
            } else if (inAnchoredScaleMode() && streamComplete) {
                isInProgress = false
                mInitialSpan = 0f
                mAnchoredScaleMode = ANCHORED_SCALE_MODE_NONE
            }

            if (streamComplete) {
                return true
            }
        }

        if (!isInProgress && isStylusScaleEnabled && !inAnchoredScaleMode()
            && !streamComplete && isStylusButtonDown
        ) {
            // Start of a button scale gesture
            mAnchoredScaleStartX = event.x
            mAnchoredScaleStartY = event.y
            mAnchoredScaleMode = ANCHORED_SCALE_MODE_STYLUS
            mInitialSpan = 0f
        }

        val configChanged =
            action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_POINTER_DOWN || anchoredScaleCancelled

        val pointerUp = action == MotionEvent.ACTION_POINTER_UP
        val skipIndex = if (pointerUp) event.actionIndex else -1

        // Determine focal point
        var sumX = 0f
        var sumY = 0f
        val div = if (pointerUp) count - 1 else count
        val focusX: Float
        val focusY: Float
        if (inAnchoredScaleMode()) {
            // In anchored scale mode, the focal pt is always where the double tap
            // or button down gesture started
            focusX = mAnchoredScaleStartX
            focusY = mAnchoredScaleStartY
            mEventBeforeOrAboveStartingGestureEvent = if (event.y < focusY) {
                true
            } else {
                false
            }
        } else {
            for (i in 0 until count) {
                if (skipIndex == i) continue
                sumX += event.getX(i)
                sumY += event.getY(i)
            }

            focusX = sumX / div
            focusY = sumY / div
        }

        // Determine average deviation from focal point
        var devSumX = 0f
        var devSumY = 0f
        for (i in 0 until count) {
            if (skipIndex == i) continue

            // Convert the resulting diameter into a radius.
            devSumX += abs((event.getX(i) - focusX))
            devSumY += abs((event.getY(i) - focusY))
        }
        val devX = devSumX / div
        val devY = devSumY / div

        // Span is the average distance between touch points through the focal point;
        // i.e. the diameter of the circle with a radius of the average deviation from
        // the focal point.
        val spanX = devX * 2
        val spanY = devY * 2
        val span = if (inAnchoredScaleMode()) {
            spanY
        } else {
            hypot(spanX.toDouble(), spanY.toDouble()).toFloat()
        }

        // Dispatch begin/end events as needed.
        // If the configuration changes, notify the app to reset its current state by beginning
        // a fresh scale event stream.
        val wasInProgress = isInProgress
        this.focusX = focusX
        this.focusY = focusY
        if (!inAnchoredScaleMode() && isInProgress && (span < mMinSpan || configChanged)) {
            mListener.onScaleEnd(this)
            isInProgress = false
            mInitialSpan = span
        }
        if (configChanged) {
            currentSpanX = spanX
            previousSpanX = currentSpanX
            currentSpanY = spanY
            previousSpanY = currentSpanY
            currentSpan = span
            previousSpan = currentSpan
            mInitialSpan = previousSpan
        }

        val minSpan = if (inAnchoredScaleMode()) mSpanSlop else mMinSpan
        if (!isInProgress && span >= minSpan &&
            (wasInProgress || abs((span - mInitialSpan).toDouble()) > mSpanSlop)
        ) {
            currentSpanX = spanX
            previousSpanX = currentSpanX
            currentSpanY = spanY
            previousSpanY = currentSpanY
            currentSpan = span
            previousSpan = currentSpan
            mPrevTime = eventTime
            isInProgress = mListener.onScaleBegin(this)
        }

        // Handle motion; focal point and span/scale factor are changing.
        if (action == MotionEvent.ACTION_MOVE) {
            currentSpanX = spanX
            currentSpanY = spanY
            currentSpan = span

            var updatePrev = true

            if (isInProgress) {
                updatePrev = mListener.onScale(this)
            }

            if (updatePrev) {
                previousSpanX = currentSpanX
                previousSpanY = currentSpanY
                previousSpan = currentSpan
                mPrevTime = eventTime
            }
        }

        return true
    }

    private fun inAnchoredScaleMode(): Boolean {
        return mAnchoredScaleMode != ANCHORED_SCALE_MODE_NONE
    }
}