package com.github.panpf.zoomimage.view.picasso.test

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.test.platform.app.InstrumentationRegistry
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.tools4a.test.ktx.getActivitySync
import com.github.panpf.tools4j.reflect.ktx.getFieldValue
import com.github.panpf.zoomimage.PicassoZoomImageView
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.picasso.PicassoSubsamplingImageGenerator
import com.github.panpf.zoomimage.picasso.internal.EnginePicassoSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.test.TestActivity
import com.github.panpf.zoomimage.test.suspendLaunchActivityWithUse
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PicassoZoomImageViewTest {

    @Test
    fun testParent() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val picassoZoomImageView = PicassoZoomImageView(context)

            @Suppress("USELESS_IS_CHECK")
            assertTrue(
                actual = picassoZoomImageView is ZoomImageView,
                message = "Expected ZoomImageView, actual $picassoZoomImageView"
            )
        }
    }

    @Test
    fun testLogger() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val picassoZoomImageView = PicassoZoomImageView(context)
            assertEquals(
                expected = "PicassoZoomImageView",
                actual = picassoZoomImageView.logger.tag
            )
        }
    }

    @Test
    fun testSetSubsamplingImageGenerators() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val picassoZoomImageView = withContext(Dispatchers.Main) {
                PicassoZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertEquals(
                expected = listOf(
                    EnginePicassoSubsamplingImageGenerator()
                ),
                actual = picassoZoomImageView.getFieldValue<List<PicassoSubsamplingImageGenerator>>(
                    "subsamplingImageGenerators"
                )!!
            )

            val convertor1 = TestPicassoSubsamplingImageGenerator()
            picassoZoomImageView.setSubsamplingImageGenerators(convertor1)
            assertEquals(
                expected = listOf(
                    TestPicassoSubsamplingImageGenerator(),
                    EnginePicassoSubsamplingImageGenerator()
                ),
                actual = picassoZoomImageView.getFieldValue<List<PicassoSubsamplingImageGenerator>>(
                    "subsamplingImageGenerators"
                )
            )

            picassoZoomImageView.setSubsamplingImageGenerators(null)
            assertEquals(
                expected = listOf(
                    EnginePicassoSubsamplingImageGenerator()
                ),
                actual = picassoZoomImageView.getFieldValue<List<PicassoSubsamplingImageGenerator>>(
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
            val picassoZoomImageView = withContext(Dispatchers.Main) {
                PicassoZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = picassoZoomImageView.isAttachedToWindow)
            assertNull(actual = picassoZoomImageView.drawable)
            assertNull(actual = picassoZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = picassoZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                picassoZoomImageView.loadImage(ResourceImages.hugeCard.uri) {
                    fit()
                    centerInside()
                }
            }
            Thread.sleep(1000)

            assertTrue(actual = picassoZoomImageView.isAttachedToWindow)
            assertNotNull(actual = picassoZoomImageView.drawable)
            assertNotNull(actual = picassoZoomImageView.subsampling.tileImageCacheState.value)
            assertTrue(actual = picassoZoomImageView.subsampling.readyState.value)
        }

        // drawable null
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val picassoZoomImageView = withContext(Dispatchers.Main) {
                PicassoZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = picassoZoomImageView.isAttachedToWindow)
            assertNull(actual = picassoZoomImageView.drawable)
            assertNull(actual = picassoZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = picassoZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                picassoZoomImageView.loadImage(ResourceImages.hugeCard.uri) {
                    fit()
                    centerInside()
                }
            }
            Thread.sleep(1000)

            assertTrue(actual = picassoZoomImageView.isAttachedToWindow)
            assertNotNull(actual = picassoZoomImageView.drawable)
            assertNotNull(actual = picassoZoomImageView.subsampling.tileImageCacheState.value)
            assertTrue(actual = picassoZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                picassoZoomImageView.setImageDrawable(null)
            }
            Thread.sleep(100)

            assertTrue(actual = picassoZoomImageView.isAttachedToWindow)
            assertNull(actual = picassoZoomImageView.drawable)
            assertNotNull(actual = picassoZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = picassoZoomImageView.subsampling.readyState.value)
        }

        // result error
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val picassoZoomImageView = withContext(Dispatchers.Main) {
                PicassoZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertTrue(actual = picassoZoomImageView.isAttachedToWindow)
            assertNull(actual = picassoZoomImageView.drawable)
            assertNull(actual = picassoZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = picassoZoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                picassoZoomImageView.loadImage(ResourceImages.hugeCard.uri + "1") {
                    fit()
                    centerInside()
                    error(ColorDrawable(Color.CYAN))
                }
            }
            Thread.sleep(1000)

            assertTrue(actual = picassoZoomImageView.isAttachedToWindow)
            assertNotNull(actual = picassoZoomImageView.drawable)
            assertNotNull(actual = picassoZoomImageView.subsampling.tileImageCacheState.value)
            assertFalse(actual = picassoZoomImageView.subsampling.readyState.value)
        }
    }

    class TestPicassoSubsamplingImageGenerator : PicassoSubsamplingImageGenerator {

        override suspend fun generateImage(
            context: Context,
            picasso: Picasso,
            data: Any,
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
            return "TestPicassoSubsamplingImageGenerator"
        }
    }
}