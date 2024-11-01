package com.github.panpf.zoomimage.view.glide.test

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import com.bumptech.glide.getRequestFromView
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.tools4a.test.ktx.getActivitySync
import com.github.panpf.tools4j.reflect.ktx.getFieldValue
import com.github.panpf.zoomimage.GlideZoomImageView
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.glide.GlideSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
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

class GlideZoomImageViewTest {

    @Test
    fun testParent() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val glideZoomImageView = GlideZoomImageView(context)

            @Suppress("USELESS_IS_CHECK")
            assertTrue(
                actual = glideZoomImageView is ZoomImageView,
                message = "Expected ZoomImageView, actual $glideZoomImageView"
            )
        }
    }

    @Test
    fun testLogger() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val glideZoomImageView = GlideZoomImageView(context)
            assertEquals(expected = "GlideZoomImageView", actual = glideZoomImageView.logger.tag)
        }
    }

    @Test
    fun testRegisterSubsamplingImageGenerator() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val glideZoomImageView = withContext(Dispatchers.Main) {
                GlideZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertEquals(
                expected = 0,
                actual = glideZoomImageView.getFieldValue<List<GlideSubsamplingImageGenerator>>("subsamplingImageGenerators")!!.size
            )

            val convertor1 = TestGlideSubsamplingImageGenerator
            glideZoomImageView.registerSubsamplingImageGenerator(convertor1)
            assertEquals(
                expected = 1,
                actual = glideZoomImageView.getFieldValue<List<GlideSubsamplingImageGenerator>>("subsamplingImageGenerators")!!.size
            )

            val convertor2 = TestGlideSubsamplingImageGenerator
            glideZoomImageView.registerSubsamplingImageGenerator(convertor2)
            assertEquals(
                expected = 2,
                actual = glideZoomImageView.getFieldValue<List<GlideSubsamplingImageGenerator>>("subsamplingImageGenerators")!!.size
            )

            glideZoomImageView.unregisterSubsamplingImageGenerator(convertor2)
            assertEquals(
                expected = 1,
                actual = glideZoomImageView.getFieldValue<List<GlideSubsamplingImageGenerator>>("subsamplingImageGenerators")!!.size
            )

            glideZoomImageView.unregisterSubsamplingImageGenerator(convertor1)
            assertEquals(
                expected = 0,
                actual = glideZoomImageView.getFieldValue<List<GlideSubsamplingImageGenerator>>("subsamplingImageGenerators")!!.size
            )
        }
    }

    @Test
    fun testResetImageSource() = runTest {
        // success
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val glideZoomImageView = withContext(Dispatchers.Main) {
                GlideZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = glideZoomImageView.isAttachedToWindow)
            assertNull(actual = glideZoomImageView.drawable)
            assertNull(actual = getRequestFromView(glideZoomImageView))
            assertNull(actual = glideZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = glideZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                Glide.with(glideZoomImageView)
                    .load(ResourceImages.hugeCard.uri)
                    .into(glideZoomImageView)
            }
            Thread.sleep(500)

            assertTrue(actual = glideZoomImageView.isAttachedToWindow)
            assertNotNull(actual = glideZoomImageView.drawable)
            assertTrue(actual = getRequestFromView(glideZoomImageView)?.isComplete == true)
            assertNotNull(actual = glideZoomImageView.subsampling.tileImageCacheState.value)
            assertTrue(actual = glideZoomImageView.subsampling.readyState.value)
        }

        // drawable null
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val glideZoomImageView = withContext(Dispatchers.Main) {
                GlideZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = glideZoomImageView.isAttachedToWindow)
            assertNull(actual = glideZoomImageView.drawable)
            assertNull(actual = getRequestFromView(glideZoomImageView))
            assertNull(actual = glideZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = glideZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                Glide.with(glideZoomImageView)
                    .load(ResourceImages.hugeCard.uri)
                    .into(glideZoomImageView)
            }
            Thread.sleep(500)

            assertTrue(actual = glideZoomImageView.isAttachedToWindow)
            assertNotNull(actual = glideZoomImageView.drawable)
            assertNotNull(actual = getRequestFromView(glideZoomImageView))
            assertNotNull(actual = glideZoomImageView.subsampling.tileImageCacheState.value)
            assertTrue(actual = glideZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                glideZoomImageView.setImageDrawable(null)
            }
            Thread.sleep(100)

            assertTrue(actual = glideZoomImageView.isAttachedToWindow)
            assertNull(actual = glideZoomImageView.drawable)
            assertTrue(actual = getRequestFromView(glideZoomImageView)?.isComplete == true)
            assertNotNull(actual = glideZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = glideZoomImageView.subsampling.readyState.value)
        }

        // result error
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val glideZoomImageView = withContext(Dispatchers.Main) {
                GlideZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = glideZoomImageView.isAttachedToWindow)
            assertNull(actual = glideZoomImageView.drawable)
            assertNull(actual = getRequestFromView(glideZoomImageView))
            assertNull(actual = glideZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = glideZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                Glide.with(glideZoomImageView)
                    .load(ResourceImages.hugeCard.uri + "1")
                    .error(ColorDrawable(Color.CYAN))
                    .into(glideZoomImageView)
            }
            Thread.sleep(500)

            assertTrue(actual = glideZoomImageView.isAttachedToWindow)
            assertNotNull(actual = glideZoomImageView.drawable)
            assertTrue(actual = getRequestFromView(glideZoomImageView)?.isComplete != true)
            assertNotNull(actual = glideZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = glideZoomImageView.subsampling.readyState.value)
        }

        // resetImageSourceOnAttachedToWindow
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val glideZoomImageView = withContext(Dispatchers.Main) {
                GlideZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = glideZoomImageView.isAttachedToWindow)
            assertNull(actual = glideZoomImageView.drawable)
            assertNull(actual = getRequestFromView(glideZoomImageView))
            assertNull(actual = glideZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = glideZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                Glide.with(glideZoomImageView)
                    .load(ResourceImages.hugeCard.uri)
                    .into(glideZoomImageView)
            }
            Thread.sleep(500)

            assertTrue(actual = glideZoomImageView.isAttachedToWindow)
            assertNotNull(actual = glideZoomImageView.drawable)
            assertNotNull(actual = getRequestFromView(glideZoomImageView))
            assertNotNull(actual = glideZoomImageView.subsampling.tileImageCacheState.value)
            assertTrue(actual = glideZoomImageView.subsampling.readyState.value)

            val thumbnail = glideZoomImageView.drawable
            withContext(Dispatchers.Main) {
                glideZoomImageView.setImageDrawable(null)
            }
            Thread.sleep(100)

            assertTrue(actual = glideZoomImageView.isAttachedToWindow)
            assertNull(actual = glideZoomImageView.drawable)
            assertTrue(actual = getRequestFromView(glideZoomImageView)?.isComplete == true)
            assertNotNull(actual = glideZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = glideZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                (glideZoomImageView.parent as ViewGroup).removeView(glideZoomImageView)
            }
            Thread.sleep(100)

            assertFalse(actual = glideZoomImageView.isAttachedToWindow)
            assertNull(actual = glideZoomImageView.drawable)
            assertTrue(actual = getRequestFromView(glideZoomImageView)?.isComplete == true)
            assertNotNull(actual = glideZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = glideZoomImageView.subsampling.readyState.value)


            withContext(Dispatchers.Main) {
                glideZoomImageView.setImageDrawable(thumbnail)
            }
            Thread.sleep(100)

            assertFalse(actual = glideZoomImageView.isAttachedToWindow)
            assertNotNull(actual = glideZoomImageView.drawable)
            assertTrue(actual = getRequestFromView(glideZoomImageView)?.isComplete == true)
            assertNotNull(actual = glideZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = glideZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                activity.findViewById<ViewGroup>(android.R.id.content)
                    .addView(glideZoomImageView, ViewGroup.LayoutParams(516, 516))
            }
            Thread.sleep(100)

            assertTrue(actual = glideZoomImageView.isAttachedToWindow)
            assertNotNull(actual = glideZoomImageView.drawable)
            assertTrue(actual = getRequestFromView(glideZoomImageView)?.isComplete == true)
            assertNotNull(actual = glideZoomImageView.subsampling.tileImageCacheState.value)
            assertTrue(actual = glideZoomImageView.subsampling.readyState.value)
        }
    }

    data object TestGlideSubsamplingImageGenerator : GlideSubsamplingImageGenerator {

        override suspend fun generateImage(
            context: Context,
            glide: Glide,
            model: Any,
            drawable: Drawable
        ): SubsamplingImageGenerateResult? {
            return null
        }
    }
}