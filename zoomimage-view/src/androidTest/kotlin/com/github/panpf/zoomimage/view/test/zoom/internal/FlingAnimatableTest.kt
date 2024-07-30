package com.github.panpf.zoomimage.view.test.zoom.internal

import android.graphics.Rect
import android.view.View
import com.github.panpf.tools4a.test.ktx.getActivitySync
import com.github.panpf.tools4a.test.ktx.launchActivity
import com.github.panpf.zoomimage.test.TestActivity
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.view.zoom.internal.FlingAnimatable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals

class FlingAnimatableTest {

    @Test
    fun test() {
        val activity = TestActivity::class.launchActivity().getActivitySync()
        val view = activity.findViewById<View>(android.R.id.content)
        val start = IntOffsetCompat(-3362, -5260)
        val bounds = Rect(-5719, -6570, 0, -3601)
        val velocity = IntOffsetCompat(-7773, -591)

        val values1 = mutableListOf<IntOffsetCompat>()
        val runnings = mutableListOf<Boolean?>()
        runBlocking {
            withContext(Dispatchers.Main) {
                suspendCoroutine { continuation ->
                    var flingAnimatable: FlingAnimatable? = null
                    flingAnimatable = FlingAnimatable(
                        view = view,
                        start = start,
                        bounds = bounds,
                        velocity = velocity,
                        onUpdateValue = { value ->
                            values1.add(value)
                        },
                        onEnd = {
                            runnings.add(flingAnimatable?.running)
                            continuation.resume(Unit)
                        }
                    )
                    runnings.add(flingAnimatable.running)
                    flingAnimatable.start()
                    runnings.add(flingAnimatable.running)
                }
            }
        }
        assertEquals(
            expected = true,
            actual = values1.size in 20..40
        )
        values1.forEachIndexed { index, it ->
            assertEquals(
                expected = true,
                actual = it.x in bounds.left..bounds.right
            )
            assertEquals(
                expected = true,
                actual = it.y in bounds.top..bounds.bottom
            )
            if (index > 0) {
                val lastIt = values1[index - 1]
                assertEquals(expected = true, actual = it.x <= lastIt.x)
                assertEquals(expected = true, actual = it.y <= lastIt.y)
            }
        }
        assertEquals(
            expected = listOf(false, true, false).joinToString(),
            actual = runnings.joinToString()
        )

        val values2 = mutableListOf<IntOffsetCompat>()
        runBlocking {
            withContext(Dispatchers.Main) {
                suspendCoroutine { continuation ->
                    var flingAnimatable: FlingAnimatable? = null
                    flingAnimatable = FlingAnimatable(
                        view = view,
                        start = start,
                        bounds = bounds,
                        velocity = velocity,
                        onUpdateValue = { value ->
                            values2.add(value)
                            if(values2.size == 10) {
                                flingAnimatable?.stop()
                            }
                        },
                        onEnd = {
                            continuation.resume(Unit)
                        }
                    )
                    flingAnimatable?.start()
                }
            }
        }
        assertEquals(
            expected = 10,
            actual = values2.size
        )
        values2.forEachIndexed { index, it ->
            assertEquals(
                expected = true,
                actual = it.x in bounds.left..bounds.right
            )
            assertEquals(
                expected = true,
                actual = it.y in bounds.top..bounds.bottom
            )
            if (index > 0) {
                val lastIt = values2[index - 1]
                assertEquals(expected = true, actual = it.x <= lastIt.x)
                assertEquals(expected = true, actual = it.y <= lastIt.y)
            }
        }
    }
}