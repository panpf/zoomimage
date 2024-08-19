package com.github.panpf.zoomimage.view.coil.core.test

import android.view.ViewGroup
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.tools4a.test.ktx.getActivitySync
import com.github.panpf.tools4j.reflect.ktx.getFieldValue
import com.github.panpf.zoomimage.CoilZoomImageView
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.images.coil.TestCoilModelToImageSource
import com.github.panpf.zoomimage.test.TestActivity
import com.github.panpf.zoomimage.test.suspendLaunchActivityWithUse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoilZoomImageViewTest {

    @Test
    fun testParent() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val coilZoomImageView = CoilZoomImageView(context)

            @Suppress("USELESS_IS_CHECK")
            assertTrue(
                actual = coilZoomImageView is ZoomImageView,
                message = "Expected ZoomImageView, actual $coilZoomImageView"
            )
        }
    }

    @Test
    fun testLogger() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val coilZoomImageView = CoilZoomImageView(context)
            assertEquals(expected = "CoilZoomImageView", actual = coilZoomImageView.logger.tag)
        }
    }

    @Test
    fun testRegisterModelToImageSource() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val coilZoomImageView = withContext(Dispatchers.Main) {
                CoilZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertEquals(
                expected = 0,
                actual = (coilZoomImageView.getFieldValue("convertors")!! as List<CoilModelToImageSource>).size
            )

            val convertor1 = TestCoilModelToImageSource()
            coilZoomImageView.registerModelToImageSource(convertor1)
            assertEquals(
                expected = 1,
                actual = (coilZoomImageView.getFieldValue("convertors")!! as List<CoilModelToImageSource>).size
            )

            val convertor2 = TestCoilModelToImageSource()
            coilZoomImageView.registerModelToImageSource(convertor2)
            assertEquals(
                expected = 2,
                actual = (coilZoomImageView.getFieldValue("convertors")!! as List<CoilModelToImageSource>).size
            )

            coilZoomImageView.unregisterModelToImageSource(convertor2)
            assertEquals(
                expected = 1,
                actual = (coilZoomImageView.getFieldValue("convertors")!! as List<CoilModelToImageSource>).size
            )

            coilZoomImageView.unregisterModelToImageSource(convertor1)
            assertEquals(
                expected = 0,
                actual = (coilZoomImageView.getFieldValue("convertors")!! as List<CoilModelToImageSource>).size
            )
        }
    }

    @Test
    fun testModelToImageSource() {
        // TODO test
    }

    @Test
    fun testNewImageSource() {
        // TODO test
    }

    @Test
    fun testResetImageSource() {
        // TODO test
    }

    @Test
    fun testOnDrawableChanged() {
        // TODO test
    }

    @Test
    fun testOnAttachedToWindow() {
        // TODO test
    }
}