package com.github.panpf.zoomimage.view.coil2.core.test

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.test.platform.app.InstrumentationRegistry
import coil.ImageLoader
import coil.load
import coil.request.ErrorResult
import coil.request.SuccessResult
import coil.util.CoilUtils
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.tools4a.test.ktx.getActivitySync
import com.github.panpf.tools4j.reflect.ktx.getFieldValue
import com.github.panpf.zoomimage.CoilZoomImageView
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.test.TestActivity
import com.github.panpf.zoomimage.test.suspendLaunchActivityWithUse
import com.github.panpf.zoomimage.view.coil.CoilViewSubsamplingImageGenerator
import com.github.panpf.zoomimage.view.coil.internal.AnimatableCoilViewSubsamplingImageGenerator
import com.github.panpf.zoomimage.view.coil.internal.EngineCoilViewSubsamplingImageGenerator
import com.github.panpf.zoomimage.view.coil.internal.getImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
    fun testSetSubsamplingImageGenerators() = runTest {
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
                expected = listOf(
                    AnimatableCoilViewSubsamplingImageGenerator(),
                    EngineCoilViewSubsamplingImageGenerator()
                ),
                actual = coilZoomImageView.getFieldValue<List<CoilViewSubsamplingImageGenerator>>("subsamplingImageGenerators")!!
            )

            val convertor1 = TestCoilViewSubsamplingImageGenerator()
            coilZoomImageView.setSubsamplingImageGenerators(convertor1)
            assertEquals(
                expected = listOf(
                    TestCoilViewSubsamplingImageGenerator(),
                    AnimatableCoilViewSubsamplingImageGenerator(),
                    EngineCoilViewSubsamplingImageGenerator()
                ),
                actual = coilZoomImageView.getFieldValue<List<CoilViewSubsamplingImageGenerator>>("subsamplingImageGenerators")
            )

            coilZoomImageView.setSubsamplingImageGenerators(null)
            assertEquals(
                expected = listOf(
                    AnimatableCoilViewSubsamplingImageGenerator(),
                    EngineCoilViewSubsamplingImageGenerator()
                ),
                actual = coilZoomImageView.getFieldValue<List<CoilViewSubsamplingImageGenerator>>("subsamplingImageGenerators")!!
            )
        }
    }

    @Test
    fun testResetImageSource() = runTest {
        // success
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val coilZoomImageView = withContext(Dispatchers.Main) {
                CoilZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = coilZoomImageView.isAttachedToWindow)
            assertNull(actual = coilZoomImageView.drawable)
            assertNull(actual = CoilUtils.result(coilZoomImageView))
            assertNull(actual = CoilUtils.getImageLoader(coilZoomImageView))
            assertNull(actual = coilZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = coilZoomImageView.subsampling.readyState.value)

            coilZoomImageView.load(ResourceImages.hugeCard.uri)
            Thread.sleep(500)

            assertTrue(actual = coilZoomImageView.isAttachedToWindow)
            assertNotNull(actual = coilZoomImageView.drawable)
            assertTrue(actual = CoilUtils.result(coilZoomImageView) is SuccessResult)
            assertNotNull(actual = CoilUtils.getImageLoader(coilZoomImageView))
            assertNotNull(actual = coilZoomImageView.subsampling.tileImageCacheState.value)
            assertTrue(actual = coilZoomImageView.subsampling.readyState.value)
        }

        // drawable null
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val coilZoomImageView = withContext(Dispatchers.Main) {
                CoilZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = coilZoomImageView.isAttachedToWindow)
            assertNull(actual = coilZoomImageView.drawable)
            assertNull(actual = CoilUtils.result(coilZoomImageView))
            assertNull(actual = CoilUtils.getImageLoader(coilZoomImageView))
            assertNull(actual = coilZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = coilZoomImageView.subsampling.readyState.value)

            coilZoomImageView.load(ResourceImages.hugeCard.uri)
            Thread.sleep(500)

            assertTrue(actual = coilZoomImageView.isAttachedToWindow)
            assertNotNull(actual = coilZoomImageView.drawable)
            assertNotNull(actual = CoilUtils.result(coilZoomImageView))
            assertNotNull(actual = CoilUtils.getImageLoader(coilZoomImageView))
            assertNotNull(actual = coilZoomImageView.subsampling.tileImageCacheState.value)
            assertTrue(actual = coilZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                coilZoomImageView.setImageDrawable(null)
            }
            Thread.sleep(100)

            assertTrue(actual = coilZoomImageView.isAttachedToWindow)
            assertNull(actual = coilZoomImageView.drawable)
            assertTrue(actual = CoilUtils.result(coilZoomImageView) is SuccessResult)
            assertNotNull(actual = CoilUtils.getImageLoader(coilZoomImageView))
            assertNotNull(actual = coilZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = coilZoomImageView.subsampling.readyState.value)
        }

        // result error
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val coilZoomImageView = withContext(Dispatchers.Main) {
                CoilZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = coilZoomImageView.isAttachedToWindow)
            assertNull(actual = coilZoomImageView.drawable)
            assertNull(actual = CoilUtils.result(coilZoomImageView))
            assertNull(actual = CoilUtils.getImageLoader(coilZoomImageView))
            assertNull(actual = coilZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = coilZoomImageView.subsampling.readyState.value)

            coilZoomImageView.load(ResourceImages.hugeCard.uri + "1") {
                error(ColorDrawable(Color.CYAN))
            }
            Thread.sleep(500)

            assertTrue(actual = coilZoomImageView.isAttachedToWindow)
            assertNotNull(actual = coilZoomImageView.drawable)
            assertTrue(actual = CoilUtils.result(coilZoomImageView) is ErrorResult)
            assertNotNull(actual = CoilUtils.getImageLoader(coilZoomImageView))
            assertNotNull(actual = coilZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = coilZoomImageView.subsampling.readyState.value)
        }

        // resetImageSourceOnAttachedToWindow
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val coilZoomImageView = withContext(Dispatchers.Main) {
                CoilZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = coilZoomImageView.isAttachedToWindow)
            assertNull(actual = coilZoomImageView.drawable)
            assertNull(actual = CoilUtils.result(coilZoomImageView))
            assertNull(actual = CoilUtils.getImageLoader(coilZoomImageView))
            assertNull(actual = coilZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = coilZoomImageView.subsampling.readyState.value)

            coilZoomImageView.load(ResourceImages.hugeCard.uri)
            Thread.sleep(500)

            assertTrue(actual = coilZoomImageView.isAttachedToWindow)
            assertNotNull(actual = coilZoomImageView.drawable)
            assertNotNull(actual = CoilUtils.result(coilZoomImageView))
            assertNotNull(actual = CoilUtils.getImageLoader(coilZoomImageView))
            assertNotNull(actual = coilZoomImageView.subsampling.tileImageCacheState.value)
            assertTrue(actual = coilZoomImageView.subsampling.readyState.value)

            val thumbnail = coilZoomImageView.drawable
            withContext(Dispatchers.Main) {
                coilZoomImageView.setImageDrawable(null)
            }
            Thread.sleep(100)

            assertTrue(actual = coilZoomImageView.isAttachedToWindow)
            assertNull(actual = coilZoomImageView.drawable)
            assertTrue(actual = CoilUtils.result(coilZoomImageView) is SuccessResult)
            assertNotNull(actual = CoilUtils.getImageLoader(coilZoomImageView))
            assertNotNull(actual = coilZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = coilZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                (coilZoomImageView.parent as ViewGroup).removeView(coilZoomImageView)
            }
            Thread.sleep(100)

            assertFalse(actual = coilZoomImageView.isAttachedToWindow)
            assertNull(actual = coilZoomImageView.drawable)
            assertTrue(actual = CoilUtils.result(coilZoomImageView) is SuccessResult)
            assertNotNull(actual = CoilUtils.getImageLoader(coilZoomImageView))
            assertNotNull(actual = coilZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = coilZoomImageView.subsampling.readyState.value)


            withContext(Dispatchers.Main) {
                coilZoomImageView.setImageDrawable(thumbnail)
            }
            Thread.sleep(100)

            assertFalse(actual = coilZoomImageView.isAttachedToWindow)
            assertNotNull(actual = coilZoomImageView.drawable)
            assertTrue(actual = CoilUtils.result(coilZoomImageView) is SuccessResult)
            assertNotNull(actual = CoilUtils.getImageLoader(coilZoomImageView))
            assertNotNull(actual = coilZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = coilZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                activity.findViewById<ViewGroup>(android.R.id.content)
                    .addView(coilZoomImageView, ViewGroup.LayoutParams(516, 516))
            }
            Thread.sleep(100)

            assertTrue(actual = coilZoomImageView.isAttachedToWindow)
            assertNotNull(actual = coilZoomImageView.drawable)
            assertTrue(actual = CoilUtils.result(coilZoomImageView) is SuccessResult)
            assertNotNull(actual = CoilUtils.getImageLoader(coilZoomImageView))
            assertNotNull(actual = coilZoomImageView.subsampling.tileImageCacheState.value)
            assertTrue(actual = coilZoomImageView.subsampling.readyState.value)
        }
    }

    class TestCoilViewSubsamplingImageGenerator : CoilViewSubsamplingImageGenerator {

        override suspend fun generateImage(
            context: Context,
            imageLoader: ImageLoader,
            result: SuccessResult,
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
            return "TestCoilViewSubsamplingImageGenerator"
        }
    }
}