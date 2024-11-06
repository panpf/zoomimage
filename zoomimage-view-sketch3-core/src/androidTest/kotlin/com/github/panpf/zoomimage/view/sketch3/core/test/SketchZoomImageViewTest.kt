package com.github.panpf.zoomimage.view.sketch3.core.test

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.util.SketchUtils
import com.github.panpf.tools4a.test.ktx.getActivitySync
import com.github.panpf.tools4j.reflect.ktx.getFieldValue
import com.github.panpf.zoomimage.SketchZoomImageView
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.test.TestActivity
import com.github.panpf.zoomimage.test.suspendLaunchActivityWithUse
import com.github.panpf.zoomimage.view.sketch.SketchViewSubsamplingImageGenerator
import com.github.panpf.zoomimage.view.sketch.internal.AnimatableSketchViewSubsamplingImageGenerator
import com.github.panpf.zoomimage.view.sketch.internal.EngineSketchViewSubsamplingImageGenerator
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
    fun testSetSubsamplingImageGenerators() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val coilZoomImageView = withContext(Dispatchers.Main) {
                SketchZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertEquals(
                expected = listOf(
                    AnimatableSketchViewSubsamplingImageGenerator(),
                    EngineSketchViewSubsamplingImageGenerator()
                ),
                actual = coilZoomImageView.getFieldValue<List<SketchViewSubsamplingImageGenerator>>(
                    "subsamplingImageGenerators"
                )!!
            )

            val convertor1 = TestSketchViewSubsamplingImageGenerator()
            coilZoomImageView.setSubsamplingImageGenerators(convertor1)
            assertEquals(
                expected = listOf(
                    TestSketchViewSubsamplingImageGenerator(),
                    AnimatableSketchViewSubsamplingImageGenerator(),
                    EngineSketchViewSubsamplingImageGenerator()
                ),
                actual = coilZoomImageView.getFieldValue<List<SketchViewSubsamplingImageGenerator>>(
                    "subsamplingImageGenerators"
                )
            )

            coilZoomImageView.setSubsamplingImageGenerators(null)
            assertEquals(
                expected = listOf(
                    AnimatableSketchViewSubsamplingImageGenerator(),
                    EngineSketchViewSubsamplingImageGenerator()
                ),
                actual = coilZoomImageView.getFieldValue<List<SketchViewSubsamplingImageGenerator>>(
                    "subsamplingImageGenerators"
                )!!
            )
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
            assertNull(actual = sketchZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)

            sketchZoomImageView.displayImage(convertAssetUri(ResourceImages.hugeCard.uri))
            Thread.sleep(500)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNotNull(actual = sketchZoomImageView.drawable)
            assertTrue(actual = SketchUtils.getResult(sketchZoomImageView) is ImageResult.Success)
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileImageCacheState.value)
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
            assertNull(actual = sketchZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)

            sketchZoomImageView.displayImage(convertAssetUri(ResourceImages.hugeCard.uri))
            Thread.sleep(500)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNotNull(actual = sketchZoomImageView.drawable)
            assertNotNull(actual = SketchUtils.getResult(sketchZoomImageView))
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileImageCacheState.value)
            assertTrue(actual = sketchZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                sketchZoomImageView.setImageDrawable(null)
            }
            Thread.sleep(100)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNull(actual = sketchZoomImageView.drawable)
            assertTrue(actual = SketchUtils.getResult(sketchZoomImageView) is ImageResult.Success)
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileImageCacheState.value)
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
            assertNull(actual = sketchZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)

            sketchZoomImageView.displayImage(convertAssetUri(ResourceImages.hugeCard.uri) + "1") {
                error(ColorDrawable(Color.CYAN))
            }
            Thread.sleep(500)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNotNull(actual = sketchZoomImageView.drawable)
            assertTrue(actual = SketchUtils.getResult(sketchZoomImageView) is ImageResult.Error)
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileImageCacheState.value)
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
            assertNull(actual = sketchZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)

            sketchZoomImageView.displayImage(convertAssetUri(ResourceImages.hugeCard.uri))
            Thread.sleep(500)

            assertTrue(actual = sketchZoomImageView.isAttachedToWindow)
            assertNotNull(actual = sketchZoomImageView.drawable)
            assertNotNull(actual = SketchUtils.getResult(sketchZoomImageView))
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileImageCacheState.value)
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
            assertNotNull(actual = sketchZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                (sketchZoomImageView.parent as ViewGroup).removeView(sketchZoomImageView)
            }
            Thread.sleep(100)

            assertFalse(actual = sketchZoomImageView.isAttachedToWindow)
            assertNull(actual = sketchZoomImageView.drawable)
            assertTrue(actual = SketchUtils.getResult(sketchZoomImageView) is ImageResult.Success)
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = sketchZoomImageView.subsampling.readyState.value)


            withContext(Dispatchers.Main) {
                sketchZoomImageView.setImageDrawable(thumbnail)
            }
            Thread.sleep(100)

            assertFalse(actual = sketchZoomImageView.isAttachedToWindow)
            assertNotNull(actual = sketchZoomImageView.drawable)
            assertTrue(actual = SketchUtils.getResult(sketchZoomImageView) is ImageResult.Success)
            assertNotNull(actual = SketchUtils.getSketch(sketchZoomImageView))
            assertNotNull(actual = sketchZoomImageView.subsampling.tileImageCacheState.value)
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
            assertNotNull(actual = sketchZoomImageView.subsampling.tileImageCacheState.value)
            assertTrue(actual = sketchZoomImageView.subsampling.readyState.value)
        }
    }

    private fun convertAssetUri(uri: String): String {
        val uri1 = uri.toUri()
        if (isAssetUri4(uri1)) {
            val fileName = uri1.pathSegments.drop(1).joinToString("/")
            return newAssetUri(fileName)
        }
        return uri
    }

    /**
     * Check if the uri is a android asset uri
     *
     * Support 'file:///android_asset/test.png' uri
     */
    private fun isAssetUri4(uri: Uri): Boolean =
        "file".equals(uri.scheme, ignoreCase = true)
                && uri.authority?.takeIf { it.isNotEmpty() } == null
                && "android_asset".equals(
            uri.pathSegments.firstOrNull(),
            ignoreCase = true
        )

    class TestSketchViewSubsamplingImageGenerator : SketchViewSubsamplingImageGenerator {

        override suspend fun generateImage(
            sketch: Sketch,
            result: DisplayResult.Success,
            drawable: Drawable
        ): SubsamplingImageGenerateResult? {
            return null
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

        override fun toString(): String {
            return "TestSketchViewSubsamplingImageGenerator"
        }
    }
}