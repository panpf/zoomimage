package com.github.panpf.zoomimage.view.test

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView.ScaleType
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.tools4a.test.ktx.getActivitySync
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.internal.TileManager.Companion.DefaultPausedContinuousTransformTypes
import com.github.panpf.zoomimage.subsampling.toFactory
import com.github.panpf.zoomimage.test.TestActivity
import com.github.panpf.zoomimage.test.suspendLaunchActivityWithUse
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.view.util.format
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.Edge
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScrollEdge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ZoomImageViewTest {

    @Test
    fun testLogger() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val zoomImageView = ZoomImageView(context)
            assertEquals(expected = "ZoomImageView", actual = zoomImageView.logger.tag)
        }
    }

    @Test
    fun testMatrix() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val zoomImageView = withContext(Dispatchers.Main) {
                ZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            val zoomable = zoomImageView.zoomable
            assertEquals(
                expected = "TransformCompat(scale=1.0x1.0, offset=0.0x0.0, rotation=0.0, scaleOrigin=0.0x0.0, rotationOrigin=0.0x0.0)",
                actual = zoomable.transformState.value.toString()
            )
            assertEquals(
                expected = Matrix(Matrix.IDENTITY_MATRIX),
                actual = zoomImageView.imageMatrix
            )

            zoomable.contentOriginSizeState.value = IntSizeCompat(690, 12176)
            withContext(Dispatchers.Main) {
                zoomImageView.setImageBitmap(Bitmap.createBitmap(86, 1522, Bitmap.Config.ARGB_8888))
            }
            Thread.sleep(100)
            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = "0.34, 6.0, 18.0",
                actual = arrayOf(
                    zoomable.minScaleState.value.format(2),
                    zoomable.mediumScaleState.value.format(2),
                    zoomable.maxScaleState.value.format(2)
                ).joinToString()
            )
            assertEquals(
                expected = "TransformCompat(scale=0.34x0.34, offset=244.0x0.0, rotation=0.0, scaleOrigin=0.0x0.0, rotationOrigin=0.08x1.47)",
                actual = zoomable.transformState.value.toString()
            )
            assertEquals(
                expected = "Matrix{[0.33902758, 0.0, 244.0][0.0, 0.33902758, 0.0][0.0, 0.0, 1.0]}",
                actual = zoomImageView.imageMatrix.toString()
            )

            withContext(Dispatchers.Main) {
                zoomable.scale(
                    targetScale = zoomable.maxScaleState.value,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = "TransformCompat(scale=18.0x18.0, offset=-515.42x-13440.0, rotation=0.0, scaleOrigin=0.0x0.0, rotationOrigin=0.08x1.47)",
                actual = zoomable.transformState.value.toString()
            )
            assertEquals(
                expected = "Matrix{[18.0, 0.0, -515.4219][0.0, 18.0, -13440.001][0.0, 0.0, 1.0]}",
                actual = zoomImageView.imageMatrix.toString()
            )

            zoomImageView.imageMatrix = Matrix().apply {
                setScale(2f, 2f)
            }
            assertEquals(
                expected = "Matrix{[18.0, 0.0, -515.4219][0.0, 18.0, -13440.001][0.0, 0.0, 1.0]}",
                actual = zoomImageView.imageMatrix.toString()
            )
        }
    }

    @Test
    fun testScaleType() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val zoomImageView = ZoomImageView(context)
            assertEquals(
                expected = ScaleType.FIT_CENTER,
                actual = zoomImageView.scaleType
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomImageView.zoomable.contentScaleState.value
            )
            assertEquals(
                expected = AlignmentCompat.Center,
                actual = zoomImageView.zoomable.alignmentState.value
            )

            zoomImageView.scaleType = ScaleType.MATRIX
            assertEquals(
                expected = ScaleType.MATRIX,
                actual = zoomImageView.scaleType
            )
            assertEquals(
                expected = ContentScaleCompat.None,
                actual = zoomImageView.zoomable.contentScaleState.value
            )
            assertEquals(
                expected = AlignmentCompat.TopStart,
                actual = zoomImageView.zoomable.alignmentState.value
            )

            zoomImageView.scaleType = ScaleType.CENTER_CROP
            assertEquals(
                expected = ScaleType.CENTER_CROP,
                actual = zoomImageView.scaleType
            )
            assertEquals(
                expected = ContentScaleCompat.Crop,
                actual = zoomImageView.zoomable.contentScaleState.value
            )
            assertEquals(
                expected = AlignmentCompat.Center,
                actual = zoomImageView.zoomable.alignmentState.value
            )
        }
    }

    @Test
    fun testScrollBar() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val zoomImageView = ZoomImageView(context)
            assertSame(
                expected = ScrollBarSpec.Default,
                actual = zoomImageView.scrollBar
            )

            zoomImageView.scrollBar = ScrollBarSpec.Default.copy(
                color = Color.BLUE
            )
            assertNotEquals(
                illegal = ScrollBarSpec.Default,
                actual = zoomImageView.scrollBar
            )
            assertEquals(
                expected = ScrollBarSpec.Default.copy(
                    color = Color.BLUE
                ),
                actual = zoomImageView.scrollBar
            )
        }
    }

    @Test
    fun testSetSubsamplingImageSource() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val zoomImageView = withContext(Dispatchers.Main) {
                ZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertEquals(
                expected = "516 x 516",
                actual = zoomImageView.zoomable.containerSizeState.value.toString()
            )
            assertEquals(
                expected = IntSizeCompat.Zero.toString(),
                actual = zoomImageView.zoomable.contentSizeState.value.toString()
            )
            assertFalse(actual = zoomImageView.subsampling.readyState.value)

            val bitmapSize = IntSizeCompat(7557, 5669).div(16)
            withContext(Dispatchers.Main) {
                val bitmap = Bitmap
                    .createBitmap(bitmapSize.width, bitmapSize.height, Bitmap.Config.ARGB_8888)
                zoomImageView.setImageDrawable(BitmapDrawable(zoomImageView.resources, bitmap))
            }
            Thread.sleep(100)

            withContext(Dispatchers.Main) {
                val imageSource = ImageSource
                    .fromAsset(zoomImageView.context, ResourceImages.hugeCard.resourceName)
                zoomImageView.setSubsamplingImage(imageSource)
            }
            Thread.sleep(500)

            assertEquals(
                expected = "516 x 516",
                actual = zoomImageView.zoomable.containerSizeState.value.toString()
            )
            assertEquals(
                expected = bitmapSize.toString(),
                actual = zoomImageView.zoomable.contentSizeState.value.toString()
            )
            assertTrue(actual = zoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                zoomImageView.setSubsamplingImage(null as ImageSource?)
            }
            Thread.sleep(500)
            assertEquals(
                expected = "516 x 516",
                actual = zoomImageView.zoomable.containerSizeState.value.toString()
            )
            assertEquals(
                expected = bitmapSize.toString(),
                actual = zoomImageView.zoomable.contentSizeState.value.toString()
            )
            assertFalse(actual = zoomImageView.subsampling.readyState.value)

            withContext(Dispatchers.Main) {
                val imageSource = ImageSource
                    .fromAsset(zoomImageView.context, ResourceImages.hugeCard.resourceName)
                zoomImageView.setSubsamplingImage(imageSource.toFactory())
            }
            Thread.sleep(500)

            assertEquals(
                expected = "516 x 516",
                actual = zoomImageView.zoomable.containerSizeState.value.toString()
            )
            assertEquals(
                expected = bitmapSize.toString(),
                actual = zoomImageView.zoomable.contentSizeState.value.toString()
            )
            assertTrue(actual = zoomImageView.subsampling.readyState.value)
        }
    }

    @Test
    fun testOnSizeChanged() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val zoomImageView = withContext(Dispatchers.Main) {
                ZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertEquals(
                expected = "516 x 516",
                actual = zoomImageView.zoomable.containerSizeState.value.toString()
            )

            withContext(Dispatchers.Main) {
                zoomImageView.layoutParams = FrameLayout.LayoutParams(1000, 511)
            }
            Thread.sleep(100)

            assertEquals(
                expected = "1000 x 511",
                actual = zoomImageView.zoomable.containerSizeState.value.toString()
            )
        }
    }

    @Test
    fun testOnDrawableChanged() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val zoomImageView = withContext(Dispatchers.Main) {
                ZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            Thread.sleep(100)

            assertEquals(
                expected = "0 x 0",
                actual = zoomImageView.zoomable.contentSizeState.value.toString()
            )

            withContext(Dispatchers.Main) {
                zoomImageView.setImageDrawable(
                    BitmapDrawable(
                        zoomImageView.resources,
                        Bitmap.createBitmap(300, 500, Bitmap.Config.ARGB_8888)
                    )
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = "300 x 500",
                actual = zoomImageView.zoomable.contentSizeState.value.toString()
            )

            withContext(Dispatchers.Main) {
                zoomImageView.setImageURI("android.resource://com.github.panpf.zoomimage.view.test/raw/dog".toUri())
            }
            Thread.sleep(100)

            assertEquals(
                expected = (IntSizeCompat(1100, 733)
                    .times(zoomImageView.resources.displayMetrics.density)).toString(),
                actual = zoomImageView.zoomable.contentSizeState.value.toString()
            )
        }
    }

    @Test
    fun testCanScrollHorizontallyOrVertical() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val zoomImageView = withContext(Dispatchers.Main) {
                ZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = zoomImageView.zoomable
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-1, 0, -1, 0).toString(),
                actual = zoomable.userOffsetBoundsState.value.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.BOTH, vertical = Edge.BOTH),
                actual = zoomable.scrollEdgeState.value
            )
            assertEquals(
                expected = listOf(false, false, false, false),
                actual = listOf(
                    zoomImageView.canScrollHorizontally(direction = 1),
                    zoomImageView.canScrollHorizontally(direction = -1),
                    zoomImageView.canScrollVertically(direction = 1),
                    zoomImageView.canScrollVertically(direction = -1),
                )
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val zoomImageView = withContext(Dispatchers.Main) {
                ZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = zoomImageView.zoomable
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            withContext(Dispatchers.Main) {
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 20f,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 20f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBoundsState.value.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.NONE, vertical = Edge.NONE),
                actual = zoomable.scrollEdgeState.value,
            )
            assertEquals(
                expected = listOf(true, true, true, true),
                actual = listOf(
                    zoomImageView.canScrollHorizontally(direction = 1),
                    zoomImageView.canScrollHorizontally(direction = -1),
                    zoomImageView.canScrollVertically(direction = 1),
                    zoomImageView.canScrollVertically(direction = -1),
                )
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val zoomImageView = withContext(Dispatchers.Main) {
                ZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = zoomImageView.zoomable
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            withContext(Dispatchers.Main) {
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 20f,
                    animated = false
                )
                val targetOffsetX = zoomable.userOffsetBoundsState.value.right + 1f
                val addOffset =
                    OffsetCompat(targetOffsetX - zoomable.userTransformState.value.offsetX, 0f)
                zoomable.offset(
                    targetOffset = zoomable.transformState.value.offset + addOffset,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 20f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBoundsState.value.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.START, vertical = Edge.NONE),
                actual = zoomable.scrollEdgeState.value,
            )
            assertEquals(
                expected = listOf(true, false, true, true),
                actual = listOf(
                    zoomImageView.canScrollHorizontally(direction = 1),
                    zoomImageView.canScrollHorizontally(direction = -1),
                    zoomImageView.canScrollVertically(direction = 1),
                    zoomImageView.canScrollVertically(direction = -1),
                )
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val zoomImageView = withContext(Dispatchers.Main) {
                ZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = zoomImageView.zoomable
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            withContext(Dispatchers.Main) {
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 20f,
                    animated = false
                )
                val targetOffsetX = zoomable.userOffsetBoundsState.value.left - 1f
                val addOffset =
                    OffsetCompat(targetOffsetX - zoomable.userTransformState.value.offsetX, 0f)
                zoomable.offset(
                    targetOffset = zoomable.transformState.value.offset + addOffset,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 20f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBoundsState.value.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.END, vertical = Edge.NONE),
                actual = zoomable.scrollEdgeState.value,
            )
            assertEquals(
                expected = listOf(false, true, true, true),
                actual = listOf(
                    zoomImageView.canScrollHorizontally(direction = 1),
                    zoomImageView.canScrollHorizontally(direction = -1),
                    zoomImageView.canScrollVertically(direction = 1),
                    zoomImageView.canScrollVertically(direction = -1),
                )
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val zoomImageView = withContext(Dispatchers.Main) {
                ZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = zoomImageView.zoomable
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            withContext(Dispatchers.Main) {
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 20f,
                    animated = false
                )
                val targetOffsetY = zoomable.userOffsetBoundsState.value.bottom + 1f
                val addOffset =
                    OffsetCompat(0f, targetOffsetY - zoomable.userTransformState.value.offsetY)
                zoomable.offset(
                    targetOffset = zoomable.transformState.value.offset + addOffset,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 20f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBoundsState.value.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.NONE, vertical = Edge.START),
                actual = zoomable.scrollEdgeState.value,
            )
            assertEquals(
                expected = listOf(true, true, true, false),
                actual = listOf(
                    zoomImageView.canScrollHorizontally(direction = 1),
                    zoomImageView.canScrollHorizontally(direction = -1),
                    zoomImageView.canScrollVertically(direction = 1),
                    zoomImageView.canScrollVertically(direction = -1),
                )
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val zoomImageView = withContext(Dispatchers.Main) {
                ZoomImageView(activity).apply {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = zoomImageView.zoomable
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            withContext(Dispatchers.Main) {
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 20f,
                    animated = false
                )
                val targetOffsetY = zoomable.userOffsetBoundsState.value.top - 1f
                val addOffset =
                    OffsetCompat(0f, targetOffsetY - zoomable.userTransformState.value.offsetY)
                zoomable.offset(
                    targetOffset = zoomable.transformState.value.offset + addOffset,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 20f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBoundsState.value.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.NONE, vertical = Edge.END),
                actual = zoomable.scrollEdgeState.value,
            )
            assertEquals(
                expected = listOf(true, true, false, true),
                actual = listOf(
                    zoomImageView.canScrollHorizontally(direction = 1),
                    zoomImageView.canScrollHorizontally(direction = -1),
                    zoomImageView.canScrollVertically(direction = 1),
                    zoomImageView.canScrollVertically(direction = -1),
                )
            )
        }
    }

    @Test
    fun testXmlAttrsDefault() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val zoomImageView =
                LayoutInflater.from(context).inflate(R.layout.zoom_image, null) as ZoomImageView

            assertEquals(ContentScaleCompat.Fit, zoomImageView.zoomable.contentScaleState.value)
            assertEquals(AlignmentCompat.Center, zoomImageView.zoomable.alignmentState.value)
            assertEquals(ZoomAnimationSpec.Default, zoomImageView.zoomable.animationSpecState.value)
            assertEquals(true, zoomImageView.zoomable.rubberBandScaleState.value)
            assertEquals(false, zoomImageView.zoomable.threeStepScaleState.value)
            assertEquals(false, zoomImageView.zoomable.limitOffsetWithinBaseVisibleRectState.value)
            assertEquals(null, zoomImageView.zoomable.readModeState.value)

            assertEquals(false, zoomImageView.subsampling.showTileBoundsState.value)
            assertEquals(
                DefaultPausedContinuousTransformTypes,
                zoomImageView.subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(false, zoomImageView.subsampling.disabledBackgroundTilesState.value)
            assertEquals(
                TileAnimationSpec.Default,
                zoomImageView.subsampling.tileAnimationSpecState.value
            )

            assertEquals(ScrollBarSpec.Default, zoomImageView.scrollBar)
        }
    }

    @Test
    fun testXmlAttrsContentScale() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = ContentScaleCompat.Crop,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_crop, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
            assertEquals(
                expected = ContentScaleCompat.FillBounds,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_fill_bounds, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
            assertEquals(
                expected = ContentScaleCompat.FillHeight,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_fill_height, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
            assertEquals(
                expected = ContentScaleCompat.FillWidth,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_fill_width, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_fit, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Inside,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_inside, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
            assertEquals(
                expected = ContentScaleCompat.None,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_none, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
        }
    }

    @Test
    fun testXmlAttrsAlignment() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = AlignmentCompat.BottomCenter,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_bottomcenter, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.BottomEnd,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_bottomend, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.BottomStart,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_bottomstart, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.Center,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_center, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.CenterEnd,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_centerend, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.CenterStart,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_centerstart, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.TopCenter,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_topcenter, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.TopEnd,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_topend, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.TopStart,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_topstart, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
        }
    }

    @Test
    fun testXmlAttrsAnimateScale() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = ZoomAnimationSpec.Default,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_animate_scale_true, null)
                    .let { it as ZoomImageView }
                    .zoomable.animationSpecState.value
            )
            assertEquals(
                expected = ZoomAnimationSpec.None,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_animate_scale_false, null)
                    .let { it as ZoomImageView }
                    .zoomable.animationSpecState.value
            )
        }
    }

    @Test
    fun testXmlAttrsRubberBandScale() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = true,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_rubber_band_scale_true, null)
                    .let { it as ZoomImageView }
                    .zoomable.rubberBandScaleState.value
            )
            assertEquals(
                expected = false,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_rubber_band_scale_false, null)
                    .let { it as ZoomImageView }
                    .zoomable.rubberBandScaleState.value
            )
        }
    }

    @Test
    fun testXmlAttrsThreeStepScale() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = true,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_three_step_scale_true, null)
                    .let { it as ZoomImageView }
                    .zoomable.threeStepScaleState.value
            )
            assertEquals(
                expected = false,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_three_step_scale_false, null)
                    .let { it as ZoomImageView }
                    .zoomable.threeStepScaleState.value
            )
        }
    }

    @Test
    fun testXmlAttrsLimitOffsetWithinBaseVisibleRect() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = true,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_limit_offset_within_base_visible_rect_true, null)
                    .let { it as ZoomImageView }
                    .zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = false,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_limit_offset_within_base_visible_rect_false, null)
                    .let { it as ZoomImageView }
                    .zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
        }
    }

    @Test
    fun testXmlAttrsReadMode() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = ReadMode.Default,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_read_mode_both, null)
                    .let { it as ZoomImageView }
                    .zoomable.readModeState.value
            )
            assertEquals(
                expected = null,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_read_mode_none, null)
                    .let { it as ZoomImageView }
                    .zoomable.readModeState.value
            )

            assertEquals(
                expected = ReadMode.Default.copy(sizeType = ReadMode.SIZE_TYPE_HORIZONTAL),
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_read_mode_horizontal, null)
                    .let { it as ZoomImageView }
                    .zoomable.readModeState.value
            )
            assertEquals(
                expected = ReadMode.Default.copy(sizeType = ReadMode.SIZE_TYPE_VERTICAL),
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_read_mode_vertical, null)
                    .let { it as ZoomImageView }
                    .zoomable.readModeState.value
            )
        }
    }

    @Test
    fun testXmlAttrsShowTileBounds() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = true,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_show_tile_bounds_true, null)
                    .let { it as ZoomImageView }
                    .subsampling.showTileBoundsState.value
            )
            assertEquals(
                expected = false,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_show_tile_bounds_false, null)
                    .let { it as ZoomImageView }
                    .subsampling.showTileBoundsState.value
            )
        }
    }

    @Test
    fun testXmlAttrsDisabledBackgroundTiles() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = true,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_background_tiles_true, null)
                    .let { it as ZoomImageView }
                    .subsampling.disabledBackgroundTilesState.value
            )
            assertEquals(
                expected = false,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_background_tiles_false, null)
                    .let { it as ZoomImageView }
                    .subsampling.disabledBackgroundTilesState.value
            )
        }
    }

    @Test
    fun testXmlAttrsTileAnimation() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = TileAnimationSpec.Default,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_tile_animation_true, null)
                    .let { it as ZoomImageView }
                    .subsampling.tileAnimationSpecState.value
            )
            assertEquals(
                expected = TileAnimationSpec.None,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_tile_animation_false, null)
                    .let { it as ZoomImageView }
                    .subsampling.tileAnimationSpecState.value
            )
        }
    }

    @Test
    fun testXmlAttrsPausedContinuousTransformTypes() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = 0,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_none, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(
                expected = ContinuousTransformType.SCALE,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_scale, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(
                expected = ContinuousTransformType.OFFSET,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_offset, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(
                expected = ContinuousTransformType.LOCATE,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_locate, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(
                expected = ContinuousTransformType.GESTURE,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_gesture, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(
                expected = ContinuousTransformType.FLING,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_fling, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(
                expected = ContinuousTransformType.values.fold(0) { last, curr -> last or curr },
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_all, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
        }
    }

    @Test
    fun testXmlAttrsDisabledScrollBar() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = null,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_scroll_bar_true, null)
                    .let { it as ZoomImageView }
                    .scrollBar
            )
            assertEquals(
                expected = ScrollBarSpec.Default,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_scroll_bar_false, null)
                    .let { it as ZoomImageView }
                    .scrollBar
            )
        }
    }

    @Test
    fun testXmlAttrsDisabledScrollBarStyle() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = ScrollBarSpec(
                    color = Color.parseColor("#FF0000"),
                    size = 55f,
                    margin = 17f,
                ),
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_scroll_bar_style, null)
                    .let { it as ZoomImageView }
                    .scrollBar
            )
            assertEquals(
                expected = null,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_scroll_bar_true_style, null)
                    .let { it as ZoomImageView }
                    .scrollBar
            )
            assertEquals(
                expected = ScrollBarSpec(
                    color = Color.parseColor("#FF0000"),
                    size = 55f,
                    margin = 17f,
                ),
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_scroll_bar_false_style, null)
                    .let { it as ZoomImageView }
                    .scrollBar
            )
        }
    }
}