package com.github.panpf.zoomimage.view.test.zoom.internal

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.github.panpf.tools4a.test.ktx.getActivitySync
import com.github.panpf.tools4a.test.ktx.launchActivity
import com.github.panpf.zoomimage.test.TestActivity
import com.github.panpf.zoomimage.view.zoom.internal.FloatAnimatable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FloatAnimatableTest {

    @Test
    fun test() {
        val activity = TestActivity::class.launchActivity().getActivitySync()
        val view = activity.findViewById<View>(android.R.id.content)

        val start = 50f
        val end = 1000f
        val values1 = mutableListOf<Float>()
        val runnings = mutableListOf<Boolean?>()
        runBlocking {
            withContext(Dispatchers.Main) {
                suspendCoroutine { continuation ->
                    var floatAnimatable: FloatAnimatable? = null
                    floatAnimatable = FloatAnimatable(
                        view = view,
                        startValue = start,
                        endValue = end,
                        durationMillis = 1000,
                        interpolator = AccelerateDecelerateInterpolator(),
                        onUpdateValue = { value ->
                            values1.add(value)
                        },
                        onEnd = {
                            runnings.add(floatAnimatable?.running)
                            continuation.resume(Unit)
                        }
                    )
                    runnings.add(floatAnimatable.running)
                    floatAnimatable.start()
                    runnings.add(floatAnimatable.running)
                }
            }
        }
        assertEquals(expected = true, actual = values1.size in 50..70)
        assertEquals(expected = start.roundToInt(), actual = values1.first().roundToInt())
        assertEquals(expected = end.roundToInt(), actual = values1.last().roundToInt())
        values1.forEachIndexed { index, it ->
            if (index > 0) {
                val lastIt = values1[index - 1]
                assertEquals(expected = true, actual = it > lastIt)
            }
        }
        assertEquals(
            expected = listOf(false, true, false).joinToString(),
            actual = runnings.joinToString()
        )

        val start2 = 1000f
        val end2 = 50f
        val values2 = mutableListOf<Float>()
        runBlocking {
            withContext(Dispatchers.Main) {
                suspendCoroutine { continuation ->
                    var floatAnimatable: FloatAnimatable? = null
                    floatAnimatable = FloatAnimatable(
                        view = view,
                        startValue = start2,
                        endValue = end2,
                        durationMillis = 1000,
                        interpolator = AccelerateDecelerateInterpolator(),
                        onUpdateValue = { value ->
                            values2.add(value)
                            if (values2.size == 30) {
                                floatAnimatable?.stop()
                            }
                        },
                        onEnd = {
                            continuation.resume(Unit)
                        }
                    )
                    floatAnimatable?.start()
                }
            }
        }
        assertEquals(expected = 30, actual = values2.size)
        assertEquals(expected = start2.roundToInt(), actual = values2.first().roundToInt())
        assertNotEquals(illegal = end2.roundToInt(), actual = values2.last().roundToInt())
        values2.forEachIndexed { index, it ->
            if (index > 0) {
                val lastIt = values2[index - 1]
                assertEquals(expected = true, actual = it < lastIt)
            }
        }
    }
}