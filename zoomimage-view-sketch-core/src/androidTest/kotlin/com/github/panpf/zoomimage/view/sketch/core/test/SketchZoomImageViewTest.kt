package com.github.panpf.zoomimage.view.sketch.core.test

import android.graphics.Color
import android.view.ViewGroup
import androidx.test.platform.app.InstrumentationRegistry
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.loadImage
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.error
import com.github.panpf.sketch.util.IntColor
import com.github.panpf.sketch.util.SketchUtils
import com.github.panpf.tools4a.test.ktx.getActivitySync
import com.github.panpf.zoomimage.SketchZoomImageView
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.test.TestActivity
import com.github.panpf.zoomimage.test.suspendLaunchActivityWithUse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SketchZoomImageViewTest {

    @Test
    fun testParent() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val sketchZoomImageView = SketchZoomImageView(context)

            @Suppress("USELESS_IS_CHECK")
            assertTrue(
                actual = sketchZoomImageView is ZoomImageView,
                message = "Expected ZoomImageView, actual $sketchZoomImageView"
            )
        }
    }

    @Test
    fun testLogger() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val sketchZoomImageView = SketchZoomImageView(context)
            assertEquals(expected = "SketchZoomImageView", actual = sketchZoomImageView.logger.tag)
        }
    }

    @Test
    fun testResetImageSource() = runTest {
        // success
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val sketchZoomImageView = withContext(Dispatchers.Main) {
                SketchZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNull(actual = sketchZoomImageView.drawable)
            assertNull(actual = SketchUtils.getResult(sketchZoomImageView))
            assertNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNull(actual = sketchZoomImageView.subsampling.tileBitmapCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)

            sketchZoomImageView.loadImage(ResourceImages.hugeCard.uri)
            Thread.sleep(500)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNotNull(actual = sketchZoomImageView.drawable)
            assertTrue(actual = SketchUtils.getResult(sketchZoomImageView) is ImageResult.Success)
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileBitmapCacheState.value)
            assertTrue(actual = sketchZoomImageView.subsampling.readyState.value)
        }

        // drawable null
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val sketchZoomImageView = withContext(Dispatchers.Main) {
                SketchZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNull(actual = sketchZoomImageView.drawable)
            assertNull(actual = SketchUtils.getResult(sketchZoomImageView))
            assertNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNull(actual = sketchZoomImageView.subsampling.tileBitmapCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)

            sketchZoomImageView.loadImage(ResourceImages.hugeCard.uri)
            Thread.sleep(500)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNotNull(actual = sketchZoomImageView.drawable)
            assertNotNull(actual = SketchUtils.getResult(sketchZoomImageView))
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileBitmapCacheState.value)
            assertTrue(actual = sketchZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                sketchZoomImageView.setImageDrawable(null)
            }
            Thread.sleep(100)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNull(actual = sketchZoomImageView.drawable)
            assertTrue(actual = SketchUtils.getResult(sketchZoomImageView) is ImageResult.Success)
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileBitmapCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)
        }

        // result error
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val sketchZoomImageView = withContext(Dispatchers.Main) {
                SketchZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNull(actual = sketchZoomImageView.drawable)
            assertNull(actual = SketchUtils.getResult(sketchZoomImageView))
            assertNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNull(actual = sketchZoomImageView.subsampling.tileBitmapCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)

            sketchZoomImageView.loadImage(ResourceImages.hugeCard.uri + "1") {
                error(IntColor(Color.CYAN))
            }
            Thread.sleep(500)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNotNull(actual = sketchZoomImageView.drawable)
            assertTrue(actual = SketchUtils.getResult(sketchZoomImageView) is ImageResult.Error)
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileBitmapCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)
        }

        // resetImageSourceOnAttachedToWindow
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val sketchZoomImageView = withContext(Dispatchers.Main) {
                SketchZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNull(actual = sketchZoomImageView.drawable)
            assertNull(actual = SketchUtils.getResult(sketchZoomImageView))
            assertNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNull(actual = sketchZoomImageView.subsampling.tileBitmapCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)

            sketchZoomImageView.loadImage(ResourceImages.hugeCard.uri)
            Thread.sleep(500)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNotNull(actual = sketchZoomImageView.drawable)
            assertNotNull(actual = SketchUtils.getResult(sketchZoomImageView))
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileBitmapCacheState.value)
            assertTrue(actual = sketchZoomImageView.subsampling.readyState.value)

            val thumbnail = sketchZoomImageView.drawable
            withContext(Dispatchers.Main) {
                sketchZoomImageView.setImageDrawable(null)
            }
            Thread.sleep(100)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNull(actual = sketchZoomImageView.drawable)
            assertTrue(actual = SketchUtils.getResult(sketchZoomImageView) is ImageResult.Success)
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileBitmapCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                (sketchZoomImageView.parent as ViewGroup).removeView(sketchZoomImageView)
            }
            Thread.sleep(100)

            assertFalse(actual = sketchZoomImageView.isAttachedToWindow)
            assertNull(actual = sketchZoomImageView.drawable)
            assertTrue(actual = SketchUtils.getResult(sketchZoomImageView) is ImageResult.Success)
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileBitmapCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)


            withContext(Dispatchers.Main) {
                sketchZoomImageView.setImageDrawable(thumbnail)
            }
            Thread.sleep(100)

            assertFalse(actual = sketchZoomImageView.isAttachedToWindow)
            assertNotNull(actual = sketchZoomImageView.drawable)
            assertTrue(actual = SketchUtils.getResult(sketchZoomImageView) is ImageResult.Success)
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileBitmapCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                activity.findViewById<ViewGroup>(android.R.id.content)
                    .addView(sketchZoomImageView, ViewGroup.LayoutParams(516, 516))
            }
            Thread.sleep(100)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNotNull(actual = sketchZoomImageView.drawable)
            assertTrue(actual = SketchUtils.getResult(sketchZoomImageView) is ImageResult.Success)
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileBitmapCacheState.value)
            assertTrue(actual = sketchZoomImageView.subsampling.readyState.value)
        }
    }
}