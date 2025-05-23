package com.github.panpf.zoomimage.core.common.test.zoom.internal

import com.github.panpf.zoomimage.test.TestAnimationAdapter
import com.github.panpf.zoomimage.test.TestZoomAnimationSpec
import com.github.panpf.zoomimage.test.allFold
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toIntOffset
import com.github.panpf.zoomimage.util.toOffset
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.GestureType
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.internal.ZoomableCore
import com.github.panpf.zoomimage.zoom.isEnd
import com.github.panpf.zoomimage.zoom.isStart
import com.github.panpf.zoomimage.zoom.name
import com.github.panpf.zoomimage.zoom.toShortString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.measureTime

class ZoomableCoreTest {

    @Test
    fun testConstructor() {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )

        val properties = zoomableCore.toProperties()
        assertEquals(expected = 0, actual = properties.rotation)
        assertEquals(expected = "0x0", actual = properties.containerSize)
        assertEquals(expected = "0x0", actual = properties.contentSize)
        assertEquals(expected = "0x0", actual = properties.contentOriginSize)
        assertEquals(expected = "Fit", actual = properties.contentScale)
        assertEquals(expected = "Center", actual = properties.alignment)
        assertEquals(expected = "null", actual = properties.readMode)
        assertEquals(
            expected = "DynamicScalesCalculator(multiple=3.0)",
            actual = properties.scalesCalculator
        )
        assertEquals(expected = false, actual = properties.threeStepScale)
        assertEquals(expected = true, actual = properties.rubberBandScale)
        assertEquals(
            expected = "OneFingerScaleSpec(panToScaleTransformer=DefaultPanToScaleTransformer(reference=200))",
            actual = properties.oneFingerScaleSpec
        )
        assertEquals(expected = "null", actual = properties.animationSpec)
        assertEquals(expected = false, actual = properties.limitOffsetWithinBaseVisibleRect)
        assertEquals(expected = "0.0", actual = properties.containerWhitespaceMultiple)
        assertEquals(expected = "[0.0x0.0,0.0x0.0]", actual = properties.containerWhitespace)
        assertEquals(false, properties.keepTransformWhenSameAspectRatioContentSizeChanged)
        assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", properties.baseTransform)
        assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", properties.userTransform)
        assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", properties.transform)
        assertEquals(expected = "1.0", actual = properties.minScale)
        assertEquals(expected = "1.0", actual = properties.mediumScale)
        assertEquals(expected = "1.0", actual = properties.maxScale)
        assertEquals(expected = "[0.0x0.0,0.0x0.0]", actual = properties.contentBaseDisplayRect)
        assertEquals(expected = "[0.0x0.0,0.0x0.0]", actual = properties.contentBaseVisibleRect)
        assertEquals(expected = "[0.0x0.0,0.0x0.0]", actual = properties.contentDisplayRect)
        assertEquals(expected = "[0.0x0.0,0.0x0.0]", actual = properties.contentVisibleRect)
        assertEquals(expected = "(BOTH,BOTH)", actual = properties.scrollEdge)
        assertEquals(expected = "[0.0x0.0,0.0x0.0]", actual = properties.userOffsetBoundsRect)
        assertEquals(expected = 0, actual = properties.continuousTransformType)
    }

    @Test
    fun testSetContainerSize() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )
        val properties = zoomableCore.toProperties()

        // Initial
        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(500, 300))
        }
        val properties2 = zoomableCore.toProperties()
        assertNotEquals(illegal = properties, actual = properties2)
        assertEquals(expected = "1080x1920", actual = properties2.containerSize)
        assertEquals(expected = "500x300", actual = properties2.contentSize)
        assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", properties2.baseTransform)
        assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", properties2.transform)
        assertEquals(expected = "2.16", actual = properties2.minScale)
        assertEquals(expected = "6.48", actual = properties2.mediumScale)
        assertEquals(expected = "19.44", actual = properties2.maxScale)
        assertEquals("[0.0x636.0,1080.0x1284.0]", properties2.contentBaseDisplayRect)
        assertEquals("[0.0x0.0,500.0x300.0]", properties2.contentBaseVisibleRect)
        assertEquals("[0.0x636.0,1080.0x1284.0]", properties2.contentDisplayRect)
        assertEquals("[-0.0x0.0,500.0x300.0]", properties2.contentVisibleRect)
        assertNotEquals(properties.containerSize, properties2.containerSize)
        assertNotEquals(properties.contentSize, properties2.contentSize)
        assertNotEquals(properties.baseTransform, properties2.baseTransform)
        assertNotEquals(properties.transform, properties2.transform)
        assertNotEquals(properties.minScale, properties2.minScale)
        assertNotEquals(properties.mediumScale, properties2.mediumScale)
        assertNotEquals(properties.maxScale, properties2.maxScale)
        assertNotEquals(properties.contentBaseDisplayRect, properties2.contentBaseDisplayRect)
        assertNotEquals(properties.contentBaseVisibleRect, properties2.contentBaseVisibleRect)
        assertNotEquals(properties.contentDisplayRect, properties2.contentDisplayRect)
        assertNotEquals(properties.contentVisibleRect, properties2.contentVisibleRect)
        assertEquals(
            expected = properties,
            actual = properties2.copy(
                containerSize = properties.containerSize,
                contentSize = properties.contentSize,
                baseTransform = properties.baseTransform,
                transform = properties.transform,
                minScale = properties.minScale,
                mediumScale = properties.mediumScale,
                maxScale = properties.maxScale,
                contentBaseDisplayRect = properties.contentBaseDisplayRect,
                contentBaseVisibleRect = properties.contentBaseVisibleRect,
                contentDisplayRect = properties.contentDisplayRect,
                contentVisibleRect = properties.contentVisibleRect,
            )
        )
        val contentVisibleCenter2 = zoomableCore.contentVisibleRect.center
        assertEquals("250.0x150.0", contentVisibleCenter2.toShortString())

        // Set containerSize. big. no user actions
        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1380, 2220))
        }
        val properties3 = zoomableCore.toProperties()
        assertNotEquals(illegal = properties2, actual = properties3)
        assertEquals(expected = "1380x2220", actual = properties3.containerSize)
        assertEquals("(2.76x2.76,0.0x696.0,0.0,0.0x0.0,0.18x0.07)", properties3.baseTransform)
        assertEquals("(2.76x2.76,0.0x696.0,0.0,0.0x0.0,0.18x0.07)", properties3.transform)
        assertEquals(expected = "2.76", actual = properties3.minScale)
        assertEquals(expected = "8.28", actual = properties3.mediumScale)
        assertEquals(expected = "24.84", actual = properties3.maxScale)
        assertEquals("[0.0x696.0,1380.0x1524.0]", properties3.contentBaseDisplayRect)
        assertEquals("[0.0x696.0,1380.0x1524.0]", properties3.contentDisplayRect)
        assertNotEquals(properties2.containerSize, properties3.containerSize)
        assertNotEquals(properties2.baseTransform, properties3.baseTransform)
        assertNotEquals(properties2.transform, properties3.transform)
        assertNotEquals(properties2.minScale, properties3.minScale)
        assertNotEquals(properties2.mediumScale, properties3.mediumScale)
        assertNotEquals(properties2.maxScale, properties3.maxScale)
        assertNotEquals(properties2.contentBaseDisplayRect, properties3.contentBaseDisplayRect)
        assertNotEquals(properties2.contentDisplayRect, properties3.contentDisplayRect)
        assertEquals(
            expected = properties2,
            actual = properties3.copy(
                containerSize = properties2.containerSize,
                baseTransform = properties2.baseTransform,
                transform = properties2.transform,
                minScale = properties2.minScale,
                mediumScale = properties2.mediumScale,
                maxScale = properties2.maxScale,
                contentBaseDisplayRect = properties2.contentBaseDisplayRect,
                contentDisplayRect = properties2.contentDisplayRect,
            )
        )
        val contentVisibleCenter3 = zoomableCore.contentVisibleRect.center
        assertEquals("250.0x150.0", contentVisibleCenter3.toShortString())
        assertEquals(contentVisibleCenter2.toShortString(), contentVisibleCenter3.toShortString())

        // Change visible center
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = OffsetCompat(350f, 200f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        val properties4 = zoomableCore.toProperties()
        assertNotEquals(properties3, properties4)
        assertEquals("(9.0x9.0,-8004.0x-10122.0,0.0,0.0x0.0,0.0x0.0)", properties4.userTransform)
        assertEquals("(24.84x24.84,-8004.0x-3858.0,0.0,0.0x0.0,0.18x0.07)", properties4.transform)
        assertEquals("[-8004.0x-3858.0,4416.0x3594.0]", properties4.contentDisplayRect)
        assertEquals("[322.22x155.31,377.78x244.69]", properties4.contentVisibleRect)
        assertEquals("(NONE,NONE)", properties4.scrollEdge)
        assertEquals("[-11040.0x-11496.0,0.0x-6264.0]", properties4.userOffsetBoundsRect)
        assertNotEquals(properties3.userTransform, properties4.userTransform)
        assertNotEquals(properties3.transform, properties4.transform)
        assertNotEquals(properties3.contentDisplayRect, properties4.contentDisplayRect)
        assertNotEquals(properties3.contentVisibleRect, properties4.contentVisibleRect)
        assertNotEquals(properties3.scrollEdge, properties4.scrollEdge)
        assertNotEquals(properties3.userOffsetBoundsRect, properties4.userOffsetBoundsRect)
        assertEquals(
            expected = properties3,
            actual = properties4.copy(
                userTransform = properties3.userTransform,
                transform = properties3.transform,
                contentDisplayRect = properties3.contentDisplayRect,
                contentVisibleRect = properties3.contentVisibleRect,
                scrollEdge = properties3.scrollEdge,
                userOffsetBoundsRect = properties3.userOffsetBoundsRect,
            )
        )
        val contentVisibleRect4 = zoomableCore.contentVisibleRect
        val contentVisibleCenter4 = zoomableCore.contentVisibleRect.center
        assertEquals("350.0x200.0", contentVisibleCenter4.toShortString())
        assertNotEquals(
            contentVisibleCenter3.toShortString(),
            contentVisibleCenter4.toShortString()
        )

        // Set containerSize. small, keep visible center
        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(580, 820))
        }
        val properties5 = zoomableCore.toProperties()
        assertNotEquals(properties4, properties5)
        assertEquals(expected = "580x820", actual = properties5.containerSize)
        assertEquals("(1.16x1.16,0.0x236.0,0.0,0.0x0.0,0.43x0.18)", properties5.baseTransform)
        assertEquals(
            "(21.41x21.41,-8404.0x-9611.66,0.0,0.0x0.0,0.43x0.18)",
            properties5.userTransform
        )
        assertEquals("(24.84x24.84,-8404.0x-4558.0,0.0,0.0x0.0,0.43x0.18)", properties5.transform)
        assertEquals(expected = "1.16", actual = properties5.minScale)
        assertEquals(expected = "3.48", actual = properties5.mediumScale)
        assertEquals(expected = "10.44", actual = properties5.maxScale)
        assertEquals("[0.0x236.0,580.0x584.0]", properties5.contentBaseDisplayRect)
        assertEquals("[-8404.0x-4558.0,4016.0x2894.0]", properties5.contentDisplayRect)
        assertEquals("[338.33x183.49,361.67x216.51]", properties5.contentVisibleRect)
        assertEquals("[-11840.0x-11685.66,0.0x-5053.66]", properties5.userOffsetBoundsRect)
        assertNotEquals(properties4.containerSize, properties5.containerSize)
        assertNotEquals(properties4.baseTransform, properties5.baseTransform)
        assertNotEquals(properties4.userTransform, properties5.userTransform)
        assertNotEquals(properties4.transform, properties5.transform)
        assertNotEquals(properties4.minScale, properties5.minScale)
        assertNotEquals(properties4.mediumScale, properties5.mediumScale)
        assertNotEquals(properties4.maxScale, properties5.maxScale)
        assertNotEquals(properties4.contentBaseDisplayRect, properties5.contentBaseDisplayRect)
        assertNotEquals(properties4.contentDisplayRect, properties5.contentDisplayRect)
        assertNotEquals(properties4.contentVisibleRect, properties5.contentVisibleRect)
        assertNotEquals(properties4.userOffsetBoundsRect, properties5.userOffsetBoundsRect)
        assertEquals(
            expected = properties4,
            actual = properties5.copy(
                containerSize = properties4.containerSize,
                baseTransform = properties4.baseTransform,
                userTransform = properties4.userTransform,
                transform = properties4.transform,
                minScale = properties4.minScale,
                mediumScale = properties4.mediumScale,
                maxScale = properties4.maxScale,
                contentBaseDisplayRect = properties4.contentBaseDisplayRect,
                contentDisplayRect = properties4.contentDisplayRect,
                contentVisibleRect = properties4.contentVisibleRect,
                userOffsetBoundsRect = properties4.userOffsetBoundsRect,
            )
        )
        val contentVisibleRect5 = zoomableCore.contentVisibleRect
        assertTrue(contentVisibleRect5.width < contentVisibleRect4.width)
        assertTrue(contentVisibleRect5.height < contentVisibleRect4.height)
        val contentVisibleCenter5 = zoomableCore.contentVisibleRect.center
        assertEquals("350.0x200.0", contentVisibleCenter5.toShortString())
        assertEquals(contentVisibleCenter4.toShortString(), contentVisibleCenter5.toShortString())

        // Set containerSize. big, keep visible center
        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
        }
        val properties6 = zoomableCore.toProperties()
        assertNotEquals(properties5, properties6)
        assertEquals(expected = "1080x1920", actual = properties6.containerSize)
        assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", properties6.baseTransform)
        assertEquals(
            "(11.5x11.5,-8154.0x-11322.0,0.0,0.0x0.0,0.23x0.08)",
            properties6.userTransform
        )
        assertEquals("(24.84x24.84,-8154.0x-4008.0,0.0,0.0x0.0,0.23x0.08)", properties6.transform)
        assertEquals(expected = "2.16", actual = properties6.minScale)
        assertEquals(expected = "6.48", actual = properties6.mediumScale)
        assertEquals(expected = "19.44", actual = properties6.maxScale)
        assertEquals("[0.0x636.0,1080.0x1284.0]", properties6.contentBaseDisplayRect)
        assertEquals("[-8154.0x-4008.0,4266.0x3444.0]", properties6.contentDisplayRect)
        assertEquals("[328.26x161.35,371.74x238.65]", properties6.contentVisibleRect)
        assertEquals("[-11340.0x-12846.0,0.0x-7314.0]", properties6.userOffsetBoundsRect)
        assertNotEquals(properties5.containerSize, properties6.containerSize)
        assertNotEquals(properties5.baseTransform, properties6.baseTransform)
        assertNotEquals(properties5.userTransform, properties6.userTransform)
        assertNotEquals(properties5.transform, properties6.transform)
        assertNotEquals(properties5.minScale, properties6.minScale)
        assertNotEquals(properties5.mediumScale, properties6.mediumScale)
        assertNotEquals(properties5.maxScale, properties6.maxScale)
        assertNotEquals(properties5.contentBaseDisplayRect, properties6.contentBaseDisplayRect)
        assertNotEquals(properties5.contentDisplayRect, properties6.contentDisplayRect)
        assertNotEquals(properties5.contentVisibleRect, properties6.contentVisibleRect)
        assertNotEquals(properties5.userOffsetBoundsRect, properties6.userOffsetBoundsRect)
        assertEquals(
            expected = properties5,
            actual = properties6.copy(
                containerSize = properties5.containerSize,
                baseTransform = properties5.baseTransform,
                userTransform = properties5.userTransform,
                transform = properties5.transform,
                minScale = properties5.minScale,
                mediumScale = properties5.mediumScale,
                maxScale = properties5.maxScale,
                contentBaseDisplayRect = properties5.contentBaseDisplayRect,
                contentDisplayRect = properties5.contentDisplayRect,
                contentVisibleRect = properties5.contentVisibleRect,
                userOffsetBoundsRect = properties5.userOffsetBoundsRect,
            )
        )
        val contentVisibleRect6 = zoomableCore.contentVisibleRect
        assertTrue(contentVisibleRect6.width > contentVisibleRect5.width)
        assertTrue(contentVisibleRect6.height > contentVisibleRect5.height)
        val contentVisibleCenter6 = zoomableCore.contentVisibleRect.center
        assertEquals("350.0x200.0", contentVisibleCenter6.toShortString())
        assertEquals(contentVisibleCenter5.toShortString(), contentVisibleCenter6.toShortString())
    }

    @Test
    fun testSetContentSize() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )
        val properties = zoomableCore.toProperties()

        // Initial
        val contentSize = IntSizeCompat(500, 300)
        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(contentSize)
        }
        val properties2 = zoomableCore.toProperties()
        assertNotEquals(illegal = properties, actual = properties2)
        assertEquals(expected = "1080x1920", actual = properties2.containerSize)
        assertEquals(expected = "500x300", actual = properties2.contentSize)
        assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", properties2.baseTransform)
        assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", properties2.transform)
        assertEquals(expected = "2.16", actual = properties2.minScale)
        assertEquals(expected = "6.48", actual = properties2.mediumScale)
        assertEquals(expected = "19.44", actual = properties2.maxScale)
        assertEquals("[0.0x636.0,1080.0x1284.0]", properties2.contentBaseDisplayRect)
        assertEquals("[0.0x0.0,500.0x300.0]", properties2.contentBaseVisibleRect)
        assertEquals("[0.0x636.0,1080.0x1284.0]", properties2.contentDisplayRect)
        assertEquals("[-0.0x0.0,500.0x300.0]", properties2.contentVisibleRect)
        assertNotEquals(properties.containerSize, properties2.containerSize)
        assertNotEquals(properties.contentSize, properties2.contentSize)
        assertNotEquals(properties.baseTransform, properties2.baseTransform)
        assertNotEquals(properties.transform, properties2.transform)
        assertNotEquals(properties.minScale, properties2.minScale)
        assertNotEquals(properties.mediumScale, properties2.mediumScale)
        assertNotEquals(properties.maxScale, properties2.maxScale)
        assertNotEquals(properties.contentBaseDisplayRect, properties2.contentBaseDisplayRect)
        assertNotEquals(properties.contentBaseVisibleRect, properties2.contentBaseVisibleRect)
        assertNotEquals(properties.contentDisplayRect, properties2.contentDisplayRect)
        assertNotEquals(properties.contentVisibleRect, properties2.contentVisibleRect)
        assertEquals(
            expected = properties,
            actual = properties2.copy(
                containerSize = properties.containerSize,
                contentSize = properties.contentSize,
                baseTransform = properties.baseTransform,
                transform = properties.transform,
                minScale = properties.minScale,
                mediumScale = properties.mediumScale,
                maxScale = properties.maxScale,
                contentBaseDisplayRect = properties.contentBaseDisplayRect,
                contentBaseVisibleRect = properties.contentBaseVisibleRect,
                contentDisplayRect = properties.contentDisplayRect,
                contentVisibleRect = properties.contentVisibleRect,
            )
        )
        val contentVisibleRect2 = zoomableCore.contentVisibleRect
        val contentVisibleCenter2 = zoomableCore.contentVisibleRect.center
        assertEquals("250.0x150.0", contentVisibleCenter2.toShortString())

        // Set contentSize. big. no user actions
        val bigScale3 = 3.3f
        withContext(Dispatchers.Main) {
            zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(true)
            zoomableCore.setContentSize(contentSize.toSize().times(bigScale3).round())
        }
        val properties3 = zoomableCore.toProperties()
        assertNotEquals(properties2, properties3)
        assertEquals(expected = "1650x990", actual = properties3.contentSize)
        assertEquals(true, properties3.keepTransformWhenSameAspectRatioContentSizeChanged)
        assertEquals("(0.65x0.65,0.0x636.0,0.0,0.0x0.0,0.76x0.26)", properties3.baseTransform)
        assertEquals("(0.65x0.65,0.0x636.0,0.0,0.0x0.0,0.76x0.26)", properties3.transform)
        assertEquals(expected = "0.65", actual = properties3.minScale)
        assertEquals(expected = "1.96", actual = properties3.mediumScale)
        assertEquals(expected = "5.89", actual = properties3.maxScale)
        assertEquals("[0.0x0.0,1650.0x990.0]", properties3.contentBaseVisibleRect)
        assertEquals("[-0.0x0.0,1650.0x990.0]", properties3.contentVisibleRect)
        assertNotEquals(properties2.contentSize, properties3.contentSize)
        assertNotEquals(
            properties2.keepTransformWhenSameAspectRatioContentSizeChanged,
            properties3.keepTransformWhenSameAspectRatioContentSizeChanged
        )
        assertNotEquals(properties2.baseTransform, properties3.baseTransform)
        assertNotEquals(properties2.transform, properties3.transform)
        assertNotEquals(properties2.minScale, properties3.minScale)
        assertNotEquals(properties2.mediumScale, properties3.mediumScale)
        assertNotEquals(properties2.maxScale, properties3.maxScale)
        assertNotEquals(properties2.contentBaseVisibleRect, properties3.contentBaseVisibleRect)
        assertNotEquals(properties2.contentVisibleRect, properties3.contentVisibleRect)
        assertEquals(
            expected = properties2,
            actual = properties3.copy(
                contentSize = properties2.contentSize,
                keepTransformWhenSameAspectRatioContentSizeChanged = properties2.keepTransformWhenSameAspectRatioContentSizeChanged,
                baseTransform = properties2.baseTransform,
                transform = properties2.transform,
                minScale = properties2.minScale,
                mediumScale = properties2.mediumScale,
                maxScale = properties2.maxScale,
                contentBaseVisibleRect = properties2.contentBaseVisibleRect,
                contentVisibleRect = properties2.contentVisibleRect,
            )
        )
        val contentVisibleRect3 = zoomableCore.contentVisibleRect
        assertEquals(
            contentVisibleRect2.times(bigScale3).toShortString(),
            contentVisibleRect3.toShortString()
        )
        val contentVisibleCenter3 = zoomableCore.contentVisibleRect.center
        assertEquals("825.0x495.0", contentVisibleCenter3.toShortString())
        assertEquals(
            contentVisibleCenter2.times(bigScale3).toShortString(),
            contentVisibleCenter3.toShortString()
        )

        // Change visible center
        withContext(Dispatchers.Main) {
            zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(false)
            zoomableCore.setContentSize(contentSize)
            zoomableCore.locate(
                contentPoint = OffsetCompat(350f, 200f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        val properties4 = zoomableCore.toProperties()
        assertNotEquals(properties2, properties4)
        assertEquals("(9.0x9.0,-6264.0x-8652.0,0.0,0.0x0.0,0.0x0.0)", properties4.userTransform)
        assertEquals("(19.44x19.44,-6264.0x-2928.0,0.0,0.0x0.0,0.23x0.08)", properties4.transform)
        assertEquals("[-6264.0x-2928.0,3456.0x2904.0]", properties4.contentDisplayRect)
        assertEquals("[322.22x150.62,377.78x249.38]", properties4.contentVisibleRect)
        assertEquals("(NONE,NONE)", properties4.scrollEdge)
        assertEquals("[-8640.0x-9636.0,0.0x-5724.0]", properties4.userOffsetBoundsRect)
        assertNotEquals(properties2.userTransform, properties4.userTransform)
        assertNotEquals(properties2.transform, properties4.transform)
        assertNotEquals(properties2.contentDisplayRect, properties4.contentDisplayRect)
        assertNotEquals(properties2.contentVisibleRect, properties4.contentVisibleRect)
        assertNotEquals(properties2.scrollEdge, properties4.scrollEdge)
        assertNotEquals(properties2.userOffsetBoundsRect, properties4.userOffsetBoundsRect)
        assertEquals(
            expected = properties2,
            actual = properties4.copy(
                userTransform = properties2.userTransform,
                transform = properties2.transform,
                contentDisplayRect = properties2.contentDisplayRect,
                contentVisibleRect = properties2.contentVisibleRect,
                scrollEdge = properties2.scrollEdge,
                userOffsetBoundsRect = properties2.userOffsetBoundsRect,
            )
        )
        val contentVisibleRect4 = zoomableCore.contentVisibleRect
        val contentVisibleCenter4 = zoomableCore.contentVisibleRect.center
        assertEquals("350.0x200.0", contentVisibleCenter4.toShortString())
        assertNotEquals(
            contentVisibleCenter2.toShortString(),
            contentVisibleCenter4.toShortString()
        )

        // Set contentSize. big. keep visible rect
        val bigScale5 = 3.3f
        withContext(Dispatchers.Main) {
            zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(true)
            zoomableCore.setContentSize(contentSize.toSize().times(bigScale5).round())
        }
        val properties5 = zoomableCore.toProperties()
        assertNotEquals(properties4, properties5)
        assertEquals(expected = "1650x990", actual = properties5.contentSize)
        assertEquals(true, properties5.keepTransformWhenSameAspectRatioContentSizeChanged)
        assertEquals("(0.65x0.65,0.0x636.0,0.0,0.0x0.0,0.76x0.26)", properties5.baseTransform)
        assertEquals("(9.0x9.0,-6264.0x-8652.0,0.0,0.0x0.0,0.76x0.26)", properties5.userTransform)
        assertEquals("(5.89x5.89,-6264.0x-2928.0,0.0,0.0x0.0,0.76x0.26)", properties5.transform)
        assertEquals(expected = "0.65", actual = properties5.minScale)
        assertEquals(expected = "1.96", actual = properties5.mediumScale)
        assertEquals(expected = "5.89", actual = properties5.maxScale)
        assertEquals("[0.0x0.0,1650.0x990.0]", properties5.contentBaseVisibleRect)
        assertEquals("[1063.33x497.04,1246.67x822.96]", properties5.contentVisibleRect)
        assertNotEquals(properties4.contentSize, properties5.contentSize)
        assertNotEquals(
            properties4.keepTransformWhenSameAspectRatioContentSizeChanged,
            properties5.keepTransformWhenSameAspectRatioContentSizeChanged
        )
        assertNotEquals(properties4.baseTransform, properties5.baseTransform)
        assertNotEquals(properties4.userTransform, properties5.userTransform)
        assertNotEquals(properties4.transform, properties5.transform)
        assertNotEquals(properties4.minScale, properties5.minScale)
        assertNotEquals(properties4.mediumScale, properties5.mediumScale)
        assertNotEquals(properties4.maxScale, properties5.maxScale)
        assertNotEquals(properties4.contentBaseVisibleRect, properties5.contentBaseVisibleRect)
        assertNotEquals(properties4.contentVisibleRect, properties5.contentVisibleRect)
        assertEquals(
            expected = properties4,
            actual = properties5.copy(
                contentSize = properties4.contentSize,
                keepTransformWhenSameAspectRatioContentSizeChanged = properties4.keepTransformWhenSameAspectRatioContentSizeChanged,
                baseTransform = properties4.baseTransform,
                userTransform = properties4.userTransform,
                transform = properties4.transform,
                minScale = properties4.minScale,
                mediumScale = properties4.mediumScale,
                maxScale = properties4.maxScale,
                contentBaseVisibleRect = properties4.contentBaseVisibleRect,
                contentVisibleRect = properties4.contentVisibleRect,
            )
        )
        val contentVisibleRect5 = zoomableCore.contentVisibleRect
        assertEquals(
            contentVisibleRect4.times(bigScale5).toShortString(),
            contentVisibleRect5.toShortString()
        )
        val contentVisibleCenter5 = zoomableCore.contentVisibleRect.center
        assertEquals("1155.0x660.0", contentVisibleCenter5.toShortString())
        assertEquals(
            contentVisibleCenter4.times(bigScale5).toShortString(),
            contentVisibleCenter5.toShortString()
        )

        // Set contentSize. small. keep visible rect
        val smallScale = 0.67f
        withContext(Dispatchers.Main) {
            zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(true)
            zoomableCore.setContentSize(contentSize.toSize().times(smallScale).round())
        }
        val properties6 = zoomableCore.toProperties()
        assertNotEquals(properties5, properties6)
        assertEquals(expected = "335x201", actual = properties6.contentSize)
        assertEquals("(3.22x3.22,0.0x636.0,0.0,0.0x0.0,0.16x0.05)", properties6.baseTransform)
        assertEquals("(9.0x9.0,-6264.0x-8652.0,0.0,0.0x0.0,0.16x0.05)", properties6.userTransform)
        assertEquals("(29.01x29.01,-6264.0x-2928.0,0.0,0.0x0.0,0.16x0.05)", properties6.transform)
        assertEquals(expected = "3.22", actual = properties6.minScale)
        assertEquals(expected = "9.67", actual = properties6.mediumScale)
        assertEquals(expected = "29.01", actual = properties6.maxScale)
        assertEquals("[0.0x0.0,335.0x201.0]", properties6.contentBaseVisibleRect)
        assertEquals("[215.89x100.91,253.11x167.09]", properties6.contentVisibleRect)
        assertNotEquals(properties5.contentSize, properties6.contentSize)
        assertNotEquals(properties5.baseTransform, properties6.baseTransform)
        assertNotEquals(properties5.userTransform, properties6.userTransform)
        assertNotEquals(properties5.transform, properties6.transform)
        assertNotEquals(properties5.minScale, properties6.minScale)
        assertNotEquals(properties5.mediumScale, properties6.mediumScale)
        assertNotEquals(properties5.maxScale, properties6.maxScale)
        assertNotEquals(properties5.contentBaseVisibleRect, properties6.contentBaseVisibleRect)
        assertNotEquals(properties5.contentVisibleRect, properties6.contentVisibleRect)
        assertEquals(
            expected = properties5,
            actual = properties6.copy(
                contentSize = properties5.contentSize,
                baseTransform = properties5.baseTransform,
                userTransform = properties5.userTransform,
                transform = properties5.transform,
                minScale = properties5.minScale,
                mediumScale = properties5.mediumScale,
                maxScale = properties5.maxScale,
                contentBaseVisibleRect = properties5.contentBaseVisibleRect,
                contentVisibleRect = properties5.contentVisibleRect,
            )
        )
        val contentVisibleRect6 = zoomableCore.contentVisibleRect
        assertEquals(
            contentVisibleRect4.times(smallScale).toShortString(),
            contentVisibleRect6.toShortString()
        )
        val contentVisibleCenter6 = zoomableCore.contentVisibleRect.center
        assertEquals("234.5x134.0", contentVisibleCenter6.toShortString())
        assertEquals(
            contentVisibleCenter4.times(smallScale).toShortString(),
            contentVisibleCenter6.toShortString()
        )

        // Set contentSize. big. keep visible rect. keepTransformWhenSameAspectRatioContentSizeChanged false
        withContext(Dispatchers.Main) {
            zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(false)
            zoomableCore.setContentSize(contentSize)
        }
        val properties7 = zoomableCore.toProperties()
        assertNotEquals(properties6, properties7)
        assertEquals(expected = "500x300", actual = properties7.contentSize)
        assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", properties2.baseTransform)
        assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", properties2.userTransform)
        assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", properties2.transform)
        assertEquals(expected = "2.16", actual = properties2.minScale)
        assertEquals(expected = "6.48", actual = properties2.mediumScale)
        assertEquals(expected = "19.44", actual = properties2.maxScale)
        assertEquals("[0.0x0.0,500.0x300.0]", properties2.contentBaseVisibleRect)
        assertEquals("[0.0x636.0,1080.0x1284.0]", properties2.contentDisplayRect)
        assertEquals("[-0.0x0.0,500.0x300.0]", properties2.contentVisibleRect)
        assertEquals("(BOTH,BOTH)", properties2.scrollEdge)
        assertEquals("[0.0x0.0,0.0x0.0]", properties2.userOffsetBoundsRect)
        assertEquals(false, properties2.keepTransformWhenSameAspectRatioContentSizeChanged)
        assertNotEquals(properties6.contentSize, properties7.contentSize)
        assertNotEquals(properties6.baseTransform, properties7.baseTransform)
        assertNotEquals(properties6.userTransform, properties7.userTransform)
        assertNotEquals(properties6.transform, properties7.transform)
        assertNotEquals(properties6.minScale, properties7.minScale)
        assertNotEquals(properties6.mediumScale, properties7.mediumScale)
        assertNotEquals(properties6.maxScale, properties7.maxScale)
        assertNotEquals(properties6.contentBaseVisibleRect, properties7.contentBaseVisibleRect)
        assertNotEquals(properties6.contentDisplayRect, properties7.contentDisplayRect)
        assertNotEquals(properties6.contentVisibleRect, properties7.contentVisibleRect)
        assertNotEquals(properties6.scrollEdge, properties7.scrollEdge)
        assertNotEquals(properties6.userOffsetBoundsRect, properties7.userOffsetBoundsRect)
        assertNotEquals(
            properties6.keepTransformWhenSameAspectRatioContentSizeChanged,
            properties7.keepTransformWhenSameAspectRatioContentSizeChanged
        )
        assertEquals(
            expected = properties6,
            actual = properties7.copy(
                contentSize = properties6.contentSize,
                baseTransform = properties6.baseTransform,
                userTransform = properties6.userTransform,
                transform = properties6.transform,
                minScale = properties6.minScale,
                mediumScale = properties6.mediumScale,
                maxScale = properties6.maxScale,
                contentBaseVisibleRect = properties6.contentBaseVisibleRect,
                contentDisplayRect = properties6.contentDisplayRect,
                contentVisibleRect = properties6.contentVisibleRect,
                scrollEdge = properties6.scrollEdge,
                userOffsetBoundsRect = properties6.userOffsetBoundsRect,
                keepTransformWhenSameAspectRatioContentSizeChanged = properties6.keepTransformWhenSameAspectRatioContentSizeChanged,
            )
        )
        val contentVisibleCenter7 = zoomableCore.contentVisibleRect.center
        assertEquals("250.0x150.0", contentVisibleCenter7.toShortString())

        // Change visible center
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = OffsetCompat(350f, 200f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        val properties8 = zoomableCore.toProperties()
        assertNotEquals(properties7, properties8)
        assertEquals("(9.0x9.0,-6264.0x-8652.0,0.0,0.0x0.0,0.0x0.0)", properties8.userTransform)
        assertEquals("(19.44x19.44,-6264.0x-2928.0,0.0,0.0x0.0,0.23x0.08)", properties8.transform)
        assertEquals("[-6264.0x-2928.0,3456.0x2904.0]", properties8.contentDisplayRect)
        assertEquals("[322.22x150.62,377.78x249.38]", properties8.contentVisibleRect)
        assertEquals("(NONE,NONE)", properties8.scrollEdge)
        assertEquals("[-8640.0x-9636.0,0.0x-5724.0]", properties8.userOffsetBoundsRect)
        assertNotEquals(properties7.userTransform, properties8.userTransform)
        assertNotEquals(properties7.transform, properties8.transform)
        assertNotEquals(properties7.contentDisplayRect, properties8.contentDisplayRect)
        assertNotEquals(properties7.contentVisibleRect, properties8.contentVisibleRect)
        assertNotEquals(properties7.scrollEdge, properties8.scrollEdge)
        assertNotEquals(properties7.userOffsetBoundsRect, properties8.userOffsetBoundsRect)
        assertEquals(
            expected = properties7,
            actual = properties8.copy(
                userTransform = properties7.userTransform,
                transform = properties7.transform,
                contentDisplayRect = properties7.contentDisplayRect,
                contentVisibleRect = properties7.contentVisibleRect,
                scrollEdge = properties7.scrollEdge,
                userOffsetBoundsRect = properties7.userOffsetBoundsRect,
            )
        )
        val contentVisibleRect8 = zoomableCore.contentVisibleRect
        val contentVisibleCenter8 = zoomableCore.contentVisibleRect.center
        assertEquals("350.0x200.0", contentVisibleCenter8.toShortString())
        assertNotEquals(
            contentVisibleCenter7.toShortString(),
            contentVisibleCenter8.toShortString()
        )

        // Set contentSize. big. Inconsistent aspect ratio
        val bigScale9 = ScaleFactorCompat(3.3f, 3.33f)
        withContext(Dispatchers.Main) {
            zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(true)
            zoomableCore.setContentSize(contentSize.toSize().times(bigScale9).round())
        }
        val properties9 = zoomableCore.toProperties()
        assertNotEquals(properties4, properties9)
        assertEquals(expected = "1650x999", actual = properties9.contentSize)
        assertEquals(true, properties9.keepTransformWhenSameAspectRatioContentSizeChanged)
        assertEquals("(0.65x0.65,0.0x633.0,0.0,0.0x0.0,0.76x0.26)", properties9.baseTransform)
        assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", properties9.userTransform)
        assertEquals("(0.65x0.65,0.0x633.0,0.0,0.0x0.0,0.76x0.26)", properties9.transform)
        assertEquals(expected = "0.65", actual = properties9.minScale)
        assertEquals(expected = "1.96", actual = properties9.mediumScale)
        assertEquals(expected = "5.89", actual = properties9.maxScale)
        assertEquals("[0.0x633.0,1080.0x1286.89]", properties9.contentBaseDisplayRect)
        assertEquals("[0.0x0.0,1650.0x999.0]", properties9.contentBaseVisibleRect)
        assertEquals("[0.0x633.0,1080.0x1287.0]", properties9.contentDisplayRect)
        assertEquals("[-0.0x0.0,1650.0x999.0]", properties9.contentVisibleRect)
        assertEquals("(BOTH,BOTH)", properties9.scrollEdge)
        assertEquals("[0.0x0.05,0.0x0.05]", properties9.userOffsetBoundsRect)
        assertNotEquals(properties4.contentSize, properties9.contentSize)
        assertNotEquals(
            properties4.keepTransformWhenSameAspectRatioContentSizeChanged,
            properties9.keepTransformWhenSameAspectRatioContentSizeChanged
        )
        assertNotEquals(properties4.baseTransform, properties9.baseTransform)
        assertNotEquals(properties4.userTransform, properties9.userTransform)
        assertNotEquals(properties4.transform, properties9.transform)
        assertNotEquals(properties4.minScale, properties9.minScale)
        assertNotEquals(properties4.mediumScale, properties9.mediumScale)
        assertNotEquals(properties4.maxScale, properties9.maxScale)
        assertNotEquals(properties4.contentBaseDisplayRect, properties9.contentBaseDisplayRect)
        assertNotEquals(properties4.contentBaseVisibleRect, properties9.contentBaseVisibleRect)
        assertNotEquals(properties4.contentDisplayRect, properties9.contentDisplayRect)
        assertNotEquals(properties4.contentVisibleRect, properties9.contentVisibleRect)
        assertNotEquals(properties4.scrollEdge, properties9.scrollEdge)
        assertNotEquals(properties4.userOffsetBoundsRect, properties9.userOffsetBoundsRect)
        assertEquals(
            expected = properties4,
            actual = properties9.copy(
                contentSize = properties4.contentSize,
                keepTransformWhenSameAspectRatioContentSizeChanged = properties4.keepTransformWhenSameAspectRatioContentSizeChanged,
                baseTransform = properties4.baseTransform,
                userTransform = properties4.userTransform,
                transform = properties4.transform,
                minScale = properties4.minScale,
                mediumScale = properties4.mediumScale,
                maxScale = properties4.maxScale,
                contentBaseDisplayRect = properties4.contentBaseDisplayRect,
                contentBaseVisibleRect = properties4.contentBaseVisibleRect,
                contentDisplayRect = properties4.contentDisplayRect,
                contentVisibleRect = properties4.contentVisibleRect,
                scrollEdge = properties4.scrollEdge,
                userOffsetBoundsRect = properties4.userOffsetBoundsRect,
            )
        )
        val contentVisibleRect9 = zoomableCore.contentVisibleRect
        assertNotEquals(
            contentVisibleRect8.times(bigScale9).toShortString(),
            contentVisibleRect9.toShortString()
        )
        val contentVisibleCenter9 = zoomableCore.contentVisibleRect.center
        assertEquals("825.0x499.5", contentVisibleCenter9.toShortString())
        assertNotEquals(
            contentVisibleCenter8.times(bigScale9).toShortString(),
            contentVisibleCenter9.toShortString()
        )
    }

    @Test
    fun testSetContentOriginSize() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )
        val properties = zoomableCore.toProperties()

        // Initial
        val contentSize = IntSizeCompat(500, 300)
        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(contentSize)
        }
        val properties2 = zoomableCore.toProperties()
        assertNotEquals(illegal = properties, actual = properties2)
        assertEquals(expected = "1080x1920", actual = properties2.containerSize)
        assertEquals(expected = "500x300", actual = properties2.contentSize)
        assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", properties2.baseTransform)
        assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", properties2.transform)
        assertEquals(expected = "2.16", actual = properties2.minScale)
        assertEquals(expected = "6.48", actual = properties2.mediumScale)
        assertEquals(expected = "19.44", actual = properties2.maxScale)
        assertEquals("[0.0x636.0,1080.0x1284.0]", properties2.contentBaseDisplayRect)
        assertEquals("[0.0x0.0,500.0x300.0]", properties2.contentBaseVisibleRect)
        assertEquals("[0.0x636.0,1080.0x1284.0]", properties2.contentDisplayRect)
        assertEquals("[-0.0x0.0,500.0x300.0]", properties2.contentVisibleRect)
        assertNotEquals(properties.containerSize, properties2.containerSize)
        assertNotEquals(properties.contentSize, properties2.contentSize)
        assertNotEquals(properties.baseTransform, properties2.baseTransform)
        assertNotEquals(properties.transform, properties2.transform)
        assertNotEquals(properties.minScale, properties2.minScale)
        assertNotEquals(properties.mediumScale, properties2.mediumScale)
        assertNotEquals(properties.maxScale, properties2.maxScale)
        assertNotEquals(properties.contentBaseDisplayRect, properties2.contentBaseDisplayRect)
        assertNotEquals(properties.contentBaseVisibleRect, properties2.contentBaseVisibleRect)
        assertNotEquals(properties.contentDisplayRect, properties2.contentDisplayRect)
        assertNotEquals(properties.contentVisibleRect, properties2.contentVisibleRect)
        assertEquals(
            expected = properties,
            actual = properties2.copy(
                containerSize = properties.containerSize,
                contentSize = properties.contentSize,
                baseTransform = properties.baseTransform,
                transform = properties.transform,
                minScale = properties.minScale,
                mediumScale = properties.mediumScale,
                maxScale = properties.maxScale,
                contentBaseDisplayRect = properties.contentBaseDisplayRect,
                contentBaseVisibleRect = properties.contentBaseVisibleRect,
                contentDisplayRect = properties.contentDisplayRect,
                contentVisibleRect = properties.contentVisibleRect,
            )
        )
        val contentVisibleRect2 = zoomableCore.contentVisibleRect
        val contentVisibleCenter2 = zoomableCore.contentVisibleRect.center
        assertEquals("250.0x150.0", contentVisibleCenter2.toShortString())

        // Set contentOriginSize. big. no user actions
        withContext(Dispatchers.Main) {
            zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(true)
            zoomableCore.setContentOriginSize(contentSize.toSize().times(8f).round())
        }
        val properties3 = zoomableCore.toProperties()
        assertNotEquals(properties2, properties3)
        assertEquals(expected = "4000x2400", actual = properties3.contentOriginSize)
        assertEquals(true, properties3.keepTransformWhenSameAspectRatioContentSizeChanged)
        assertEquals(expected = "8.0", actual = properties3.mediumScale)
        assertEquals(expected = "24.0", actual = properties3.maxScale)
        assertNotEquals(properties2.contentOriginSize, properties3.contentOriginSize)
        assertNotEquals(
            properties2.keepTransformWhenSameAspectRatioContentSizeChanged,
            properties3.keepTransformWhenSameAspectRatioContentSizeChanged
        )
        assertNotEquals(properties2.mediumScale, properties3.mediumScale)
        assertNotEquals(properties2.maxScale, properties3.maxScale)
        assertEquals(
            expected = properties2,
            actual = properties3.copy(
                contentOriginSize = properties2.contentOriginSize,
                keepTransformWhenSameAspectRatioContentSizeChanged = properties2.keepTransformWhenSameAspectRatioContentSizeChanged,
                mediumScale = properties2.mediumScale,
                maxScale = properties2.maxScale,
            )
        )
        val contentVisibleRect3 = zoomableCore.contentVisibleRect
        assertEquals(contentVisibleRect2.toShortString(), contentVisibleRect3.toShortString())
        val contentVisibleCenter3 = zoomableCore.contentVisibleRect.center
        assertEquals("250.0x150.0", contentVisibleCenter3.toShortString())
        assertEquals(contentVisibleCenter2.toShortString(), contentVisibleCenter3.toShortString())

        // Change visible center
        withContext(Dispatchers.Main) {
            zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(false)
            zoomableCore.setContentOriginSize(IntSizeCompat(0, 0))
            zoomableCore.locate(
                contentPoint = OffsetCompat(350f, 200f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        val properties4 = zoomableCore.toProperties()
        assertNotEquals(properties2, properties4)
        assertEquals("(9.0x9.0,-6264.0x-8652.0,0.0,0.0x0.0,0.0x0.0)", properties4.userTransform)
        assertEquals("(19.44x19.44,-6264.0x-2928.0,0.0,0.0x0.0,0.23x0.08)", properties4.transform)
        assertEquals("[-6264.0x-2928.0,3456.0x2904.0]", properties4.contentDisplayRect)
        assertEquals("[322.22x150.62,377.78x249.38]", properties4.contentVisibleRect)
        assertEquals("(NONE,NONE)", properties4.scrollEdge)
        assertEquals("[-8640.0x-9636.0,0.0x-5724.0]", properties4.userOffsetBoundsRect)
        assertNotEquals(properties2.userTransform, properties4.userTransform)
        assertNotEquals(properties2.transform, properties4.transform)
        assertNotEquals(properties2.contentDisplayRect, properties4.contentDisplayRect)
        assertNotEquals(properties2.contentVisibleRect, properties4.contentVisibleRect)
        assertNotEquals(properties2.scrollEdge, properties4.scrollEdge)
        assertNotEquals(properties2.userOffsetBoundsRect, properties4.userOffsetBoundsRect)
        assertEquals(
            expected = properties2,
            actual = properties4.copy(
                userTransform = properties2.userTransform,
                transform = properties2.transform,
                contentDisplayRect = properties2.contentDisplayRect,
                contentVisibleRect = properties2.contentVisibleRect,
                scrollEdge = properties2.scrollEdge,
                userOffsetBoundsRect = properties2.userOffsetBoundsRect,
            )
        )
        val contentVisibleRect4 = zoomableCore.contentVisibleRect
        val contentVisibleCenter4 = zoomableCore.contentVisibleRect.center
        assertEquals("350.0x200.0", contentVisibleCenter4.toShortString())
        assertNotEquals(
            contentVisibleCenter2.toShortString(),
            contentVisibleCenter4.toShortString()
        )

        // Set contentOriginSize. big. keep visible rect
        withContext(Dispatchers.Main) {
            zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(true)
            zoomableCore.setContentOriginSize(contentSize.toSize().times(8f).round())
        }
        val properties5 = zoomableCore.toProperties()
        assertNotEquals(properties4, properties5)
        assertEquals(expected = "4000x2400", actual = properties5.contentOriginSize)
        assertEquals(true, properties5.keepTransformWhenSameAspectRatioContentSizeChanged)
        assertEquals("(9.0x9.0,-6264.0x-8652.0,0.0,0.0x0.0,0.23x0.08)", properties5.userTransform)
        assertEquals(expected = "8.0", actual = properties5.mediumScale)
        assertEquals(expected = "24.0", actual = properties5.maxScale)
        assertNotEquals(properties4.contentOriginSize, properties5.contentOriginSize)
        assertNotEquals(
            properties4.keepTransformWhenSameAspectRatioContentSizeChanged,
            properties5.keepTransformWhenSameAspectRatioContentSizeChanged
        )
        assertNotEquals(properties4.userTransform, properties5.userTransform)
        assertNotEquals(properties4.mediumScale, properties5.mediumScale)
        assertNotEquals(properties4.maxScale, properties5.maxScale)
        assertEquals(
            expected = properties4,
            actual = properties5.copy(
                contentOriginSize = properties4.contentOriginSize,
                keepTransformWhenSameAspectRatioContentSizeChanged = properties4.keepTransformWhenSameAspectRatioContentSizeChanged,
                userTransform = properties4.userTransform,
                mediumScale = properties4.mediumScale,
                maxScale = properties4.maxScale,
            )
        )
        val contentVisibleRect5 = zoomableCore.contentVisibleRect
        assertEquals(contentVisibleRect4.toShortString(), contentVisibleRect5.toShortString())
        val contentVisibleCenter5 = zoomableCore.contentVisibleRect.center
        assertEquals("350.0x200.0", contentVisibleCenter5.toShortString())
        assertEquals(contentVisibleCenter4.toShortString(), contentVisibleCenter5.toShortString())

        // Set contentOriginSize. small. keep visible rect
        withContext(Dispatchers.Main) {
            zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(true)
            zoomableCore.setContentOriginSize(contentSize.toSize().times(4f).round())
        }
        val properties6 = zoomableCore.toProperties()
        assertNotEquals(properties5, properties6)
        assertEquals(expected = "2000x1200", actual = properties6.contentOriginSize)
        assertEquals(expected = "6.48", actual = properties6.mediumScale)
        assertEquals(expected = "19.44", actual = properties6.maxScale)
        assertNotEquals(properties5.contentOriginSize, properties6.contentOriginSize)
        assertNotEquals(properties5.mediumScale, properties6.mediumScale)
        assertNotEquals(properties5.maxScale, properties6.maxScale)
        assertEquals(
            expected = properties5,
            actual = properties6.copy(
                contentOriginSize = properties5.contentOriginSize,
                mediumScale = properties5.mediumScale,
                maxScale = properties5.maxScale,
            )
        )
        val contentVisibleRect6 = zoomableCore.contentVisibleRect
        assertEquals(contentVisibleRect4.toShortString(), contentVisibleRect6.toShortString())
        val contentVisibleCenter6 = zoomableCore.contentVisibleRect.center
        assertEquals("350.0x200.0", contentVisibleCenter6.toShortString())
        assertEquals(contentVisibleCenter4.toShortString(), contentVisibleCenter6.toShortString())

        // Set contentOriginSize. big. keep visible rect. keepTransformWhenSameAspectRatioContentSizeChanged false
        withContext(Dispatchers.Main) {
            zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(false)
            zoomableCore.setContentOriginSize(contentSize.toSize().times(8f).round())
        }
        val properties7 = zoomableCore.toProperties()
        assertNotEquals(properties6, properties7)
        assertEquals(expected = "4000x2400", actual = properties7.contentOriginSize)
        assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", properties2.userTransform)
        assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", properties2.transform)
        assertEquals(expected = "6.48", actual = properties2.mediumScale)
        assertEquals(expected = "19.44", actual = properties2.maxScale)
        assertEquals("[0.0x636.0,1080.0x1284.0]", properties2.contentDisplayRect)
        assertEquals("[-0.0x0.0,500.0x300.0]", properties2.contentVisibleRect)
        assertEquals("(BOTH,BOTH)", properties2.scrollEdge)
        assertEquals("[0.0x0.0,0.0x0.0]", properties2.userOffsetBoundsRect)
        assertEquals(false, properties2.keepTransformWhenSameAspectRatioContentSizeChanged)
        assertNotEquals(properties6.contentSize, properties7.contentOriginSize)
        assertNotEquals(properties6.userTransform, properties7.userTransform)
        assertNotEquals(properties6.transform, properties7.transform)
        assertNotEquals(properties6.mediumScale, properties7.mediumScale)
        assertNotEquals(properties6.maxScale, properties7.maxScale)
        assertNotEquals(properties6.contentDisplayRect, properties7.contentDisplayRect)
        assertNotEquals(properties6.contentVisibleRect, properties7.contentVisibleRect)
        assertNotEquals(properties6.scrollEdge, properties7.scrollEdge)
        assertNotEquals(properties6.userOffsetBoundsRect, properties7.userOffsetBoundsRect)
        assertNotEquals(
            properties6.keepTransformWhenSameAspectRatioContentSizeChanged,
            properties7.keepTransformWhenSameAspectRatioContentSizeChanged
        )
        assertEquals(
            expected = properties6,
            actual = properties7.copy(
                contentOriginSize = properties6.contentOriginSize,
                userTransform = properties6.userTransform,
                transform = properties6.transform,
                mediumScale = properties6.mediumScale,
                maxScale = properties6.maxScale,
                contentDisplayRect = properties6.contentDisplayRect,
                contentVisibleRect = properties6.contentVisibleRect,
                scrollEdge = properties6.scrollEdge,
                userOffsetBoundsRect = properties6.userOffsetBoundsRect,
                keepTransformWhenSameAspectRatioContentSizeChanged = properties6.keepTransformWhenSameAspectRatioContentSizeChanged,
            )
        )
        val contentVisibleCenter7 = zoomableCore.contentVisibleRect.center
        assertEquals("250.0x150.0", contentVisibleCenter7.toShortString())
    }

    @Test
    fun testSetContentScale() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals("Fit", this.contentScale)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x0.0,0.0x0.0]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,0.0x0.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x0.0,0.0x0.0]", this.contentDisplayRect)
            assertEquals("[0.0x0.0,0.0x0.0]", this.contentVisibleRect)
            assertEquals("1.0", this.minScale)
            assertEquals("1.0", this.mediumScale)
            assertEquals("1.0", this.maxScale)
        }

        // Fit
        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 3000))
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals("Fit", this.contentScale)
            assertEquals("(0.22x0.22,0.0x636.0,0.0,0.0x0.0,2.31x0.78)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x636.0,1080.0x1284.0]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x3000.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x636.0,1080.0x1284.0]", this.contentDisplayRect)
            assertEquals("[-0.0x0.0,5000.0x3000.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("1.0", this.mediumScale)
            assertEquals("3.0", this.maxScale)
        }

        // FillBounds
        withContext(Dispatchers.Main) {
            zoomableCore.setContentScale(ContentScaleCompat.FillBounds)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toString().toFormattedString())
            assertEquals("FillBounds", this.contentScale)
            assertEquals("(0.22x0.64,0.0x0.0,0.0,0.0x0.0,2.31x0.78)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x0.0,1080.0x1920.0]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x3000.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x0.0,1080.0x1920.0]", this.contentDisplayRect)
            assertEquals("[-0.0x-0.0,5000.0x3000.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("0.65", this.mediumScale)
            assertEquals("1.94", this.maxScale)
        }

        // FillHeight
        withContext(Dispatchers.Main) {
            zoomableCore.setContentScale(ContentScaleCompat.FillHeight)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toString().toFormattedString())
            assertEquals("FillHeight", this.contentScale)
            assertEquals("(0.64x0.64,-1060.0x0.0,0.0,0.0x0.0,2.31x0.78)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[-1060.0x0.0,2140.0x1920.0]", this.contentBaseDisplayRect)
            assertEquals("[1656.25x0.0,3343.75x3000.0]", this.contentBaseVisibleRect)
            assertEquals("[-1060.0x0.0,2140.0x1920.0]", this.contentDisplayRect)
            assertEquals("[1656.25x-0.0,3343.75x3000.0]", this.contentVisibleRect)
            assertEquals("0.64", this.minScale)
            assertEquals("1.92", this.mediumScale)
            assertEquals("5.76", this.maxScale)
        }

        // FillBounds
        withContext(Dispatchers.Main) {
            zoomableCore.setContentScale(ContentScaleCompat.FillBounds)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toString().toFormattedString())
            assertEquals("FillBounds", this.contentScale)
            assertEquals("(0.22x0.64,0.0x0.0,0.0,0.0x0.0,2.31x0.78)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x0.0,1080.0x1920.0]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x3000.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x0.0,1080.0x1920.0]", this.contentDisplayRect)
            assertEquals("[-0.0x-0.0,5000.0x3000.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("0.65", this.mediumScale)
            assertEquals("1.94", this.maxScale)
        }

        // Crop
        withContext(Dispatchers.Main) {
            zoomableCore.setContentScale(ContentScaleCompat.Crop)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toString().toFormattedString())
            assertEquals("Crop", this.contentScale)
            assertEquals("(0.64x0.64,-1060.0x0.0,0.0,0.0x0.0,2.31x0.78)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[-1060.0x0.0,2140.0x1920.0]", this.contentBaseDisplayRect)
            assertEquals("[1656.25x0.0,3343.75x3000.0]", this.contentBaseVisibleRect)
            assertEquals("[-1060.0x0.0,2140.0x1920.0]", this.contentDisplayRect)
            assertEquals("[1656.25x-0.0,3343.75x3000.0]", this.contentVisibleRect)
            assertEquals("0.64", this.minScale)
            assertEquals("1.92", this.mediumScale)
            assertEquals("5.76", this.maxScale)
        }

        // Inside
        withContext(Dispatchers.Main) {
            zoomableCore.setContentScale(ContentScaleCompat.Inside)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toString().toFormattedString())
            assertEquals("Inside", this.contentScale)
            assertEquals("(0.22x0.22,0.0x636.0,0.0,0.0x0.0,2.31x0.78)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x636.0,1080.0x1284.0]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x3000.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x636.0,1080.0x1284.0]", this.contentDisplayRect)
            assertEquals("[-0.0x0.0,5000.0x3000.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("1.0", this.mediumScale)
            assertEquals("3.0", this.maxScale)
        }

        // None
        withContext(Dispatchers.Main) {
            zoomableCore.setContentScale(ContentScaleCompat.None)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toString().toFormattedString())
            assertEquals("None", this.contentScale)
            assertEquals("(1.0x1.0,-1960.0x-540.0,0.0,0.0x0.0,2.31x0.78)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[-1960.0x-540.0,3040.0x2460.0]", this.contentBaseDisplayRect)
            assertEquals("[1960.0x540.0,3040.0x2460.0]", this.contentBaseVisibleRect)
            assertEquals("[-1960.0x-540.0,3040.0x2460.0]", this.contentDisplayRect)
            assertEquals("[1960.0x540.0,3040.0x2460.0]", this.contentVisibleRect)
            assertEquals("1.0", this.minScale)
            assertEquals("3.0", this.mediumScale)
            assertEquals("9.0", this.maxScale)
        }

        // Change visible center
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toString().toFormattedString())
            assertEquals("None", this.contentScale)
            assertEquals("(1.0x1.0,-1960.0x-540.0,0.0,0.0x0.0,2.31x0.78)", this.baseTransform)
            assertEquals("(9.0x9.0,-13320.0x-13080.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[-1960.0x-540.0,3040.0x2460.0]", this.contentBaseDisplayRect)
            assertEquals("[1960.0x540.0,3040.0x2460.0]", this.contentBaseVisibleRect)
            assertEquals("[-30960.0x-17940.0,14040.0x9060.0]", this.contentDisplayRect)
            assertEquals("[3440.0x1993.33,3560.0x2206.67]", this.contentVisibleRect)
            assertEquals("1.0", this.minScale)
            assertEquals("3.0", this.mediumScale)
            assertEquals("9.0", this.maxScale)
        }

        // Switch contentScale. reset
        withContext(Dispatchers.Main) {
            zoomableCore.setContentScale(ContentScaleCompat.Fit)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toString().toFormattedString())
            assertEquals("Fit", this.contentScale)
            assertEquals("(0.22x0.22,0.0x636.0,0.0,0.0x0.0,2.31x0.78)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x636.0,1080.0x1284.0]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x3000.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x636.0,1080.0x1284.0]", this.contentDisplayRect)
            assertEquals("[-0.0x0.0,5000.0x3000.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("1.0", this.mediumScale)
            assertEquals("3.0", this.maxScale)
        }
    }

    @Test
    fun testSetAlignment() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )
        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(500, 300))
            zoomableCore.setContentScale(ContentScaleCompat.None)
        }

        val propertiesList = listOf(
            AlignmentCompat.TopStart,
            AlignmentCompat.TopCenter,
            AlignmentCompat.TopEnd,
            AlignmentCompat.CenterStart,
            AlignmentCompat.Center,
            AlignmentCompat.CenterEnd,
            AlignmentCompat.BottomStart,
            AlignmentCompat.BottomCenter,
            AlignmentCompat.BottomEnd,
        ).map { alignment ->
            withContext(Dispatchers.Main) {
                zoomableCore.setAlignment(alignment)
            }
            zoomableCore.toProperties()
        }

        val firstProperties = propertiesList.first()
        assertTrue(
            actual = propertiesList.allFold { t1, t2 ->
                t1.baseTransform != t2.baseTransform && firstProperties.baseTransform != t2.baseTransform
            },
            message = propertiesList.joinToString("\n")
        )
        assertTrue(
            actual = propertiesList.allFold { t1, t2 ->
                t1.userTransform == t2.userTransform && firstProperties.userTransform == t2.userTransform
            },
            message = propertiesList.joinToString("\n")
        )
        assertTrue(
            actual = propertiesList.allFold { t1, t2 ->
                t1.minScale == t2.minScale && firstProperties.minScale == t2.minScale
            },
            message = propertiesList.joinToString("\n")
        )
        assertTrue(
            actual = propertiesList.allFold { t1, t2 ->
                t1.mediumScale == t2.mediumScale && firstProperties.mediumScale == t2.mediumScale
            },
            message = propertiesList.joinToString("\n")
        )
        assertTrue(
            actual = propertiesList.allFold { t1, t2 ->
                t1.maxScale == t2.maxScale && firstProperties.maxScale == t2.maxScale
            },
            message = propertiesList.joinToString("\n")
        )

        // Change visible center
        withContext(Dispatchers.Main) {
            zoomableCore.setContentScale(ContentScaleCompat.Fit)
            zoomableCore.setAlignment(AlignmentCompat.Center)
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toString().toFormattedString())
            assertEquals("Fit", this.contentScale)
            assertEquals("Center", this.alignment)
            assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", this.baseTransform)
            assertEquals("(9.0x9.0,-6264.0x-8846.4,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x636.0,1080.0x1284.0]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,500.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-6264.0x-3122.4,3456.0x2709.6]", this.contentDisplayRect)
            assertEquals("[322.22x160.62,377.78x259.38]", this.contentVisibleRect)
            assertEquals("2.16", this.minScale)
            assertEquals("6.48", this.mediumScale)
            assertEquals("19.44", this.maxScale)
        }

        // Change alignment. reset
        withContext(Dispatchers.Main) {
            zoomableCore.setAlignment(AlignmentCompat.TopCenter)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toString().toFormattedString())
            assertEquals("Fit", this.contentScale)
            assertEquals("TopCenter", this.alignment)
            assertEquals("(2.16x2.16,0.0x0.0,0.0,0.0x0.0,0.23x0.08)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x0.0,1080.0x648.0]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,500.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x0.0,1080.0x648.0]", this.contentDisplayRect)
            assertEquals("[-0.0x-0.0,500.0x300.0]", this.contentVisibleRect)
            assertEquals("2.16", this.minScale)
            assertEquals("6.48", this.mediumScale)
            assertEquals("19.44", this.maxScale)
        }
    }

    // TODO test rtlLayoutDirection

    @Test
    fun testSetReadMode() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(500, 300))
            zoomableCore.setReadMode(null)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals("null", this.readMode)
            assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x636.0,1080.0x1284.0]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,500.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x636.0,1080.0x1284.0]", this.contentDisplayRect)
            assertEquals("[-0.0x0.0,500.0x300.0]", this.contentVisibleRect)
            assertEquals("2.16", this.minScale)
            assertEquals("6.48", this.mediumScale)
            assertEquals("19.44", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(500, 300))
            zoomableCore.setReadMode(ReadMode.Default)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(ReadMode.Default.toString(), this.readMode)
            assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x636.0,1080.0x1284.0]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,500.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x636.0,1080.0x1284.0]", this.contentDisplayRect)
            assertEquals("[-0.0x0.0,500.0x300.0]", this.contentVisibleRect)
            assertEquals("2.16", this.minScale)
            assertEquals("6.48", this.mediumScale)
            assertEquals("19.44", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
            zoomableCore.setReadMode(null)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals("null", this.readMode)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x928.0,1080.0x993.0]", this.contentDisplayRect)
            assertEquals("[-0.0x0.0,5000.0x300.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
            zoomableCore.setReadMode(ReadMode.Default)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(ReadMode.Default.toString(), this.readMode)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(29.63x29.63,0.0x-27496.29,0.0,0.0x0.0,2.31x0.08)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x0.0,32000.0x1925.93]", this.contentDisplayRect)
            assertEquals("[-0.0x0.0,168.75x300.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
            zoomableCore.setReadMode(null)
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals("null", this.readMode)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(88.89x88.89,-66660.0x-85560.89,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-66660.0x-3072.0,29340.01x2705.78]", this.contentDisplayRect)
            assertEquals("[3471.87x160.0,3528.12x260.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.setReadMode(ReadMode.Default)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(ReadMode.Default.toString(), this.readMode)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(29.63x29.63,0.0x-27496.29,0.0,0.0x0.0,2.31x0.08)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x0.0,32000.0x1925.93]", this.contentDisplayRect)
            assertEquals("[-0.0x0.0,168.75x300.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }
    }

    @Test
    fun testSetScalesCalculator() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(ScalesCalculator.Dynamic.toString(), this.scalesCalculator)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(88.89x88.89,-66660.0x-85560.89,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-66660.0x-3072.0,29340.01x2705.78]", this.contentDisplayRect)
            assertEquals("[3471.87x160.0,3528.12x260.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.setScalesCalculator(ScalesCalculator.Fixed)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(ScalesCalculator.Fixed.toString(), this.scalesCalculator)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x928.0,1080.0x993.0]", this.contentDisplayRect)
            assertEquals("[-0.0x0.0,5000.0x300.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("0.65", this.mediumScale)
            assertEquals("1.94", this.maxScale)
        }
    }

    @Test
    fun testSetThreeStepScale() {
        // See testGetNextStepScale()
    }

    @Test
    fun testSetRubberBandScale() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
            zoomableCore.setRubberBandScale(false)
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(false, this.rubberBandScale)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(88.89x88.89,-66660.0x-85560.89,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-66660.0x-3072.0,29340.01x2705.78]", this.contentDisplayRect)
            assertEquals("[3471.87x160.0,3528.12x260.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.setRubberBandScale(false)
            zoomableCore.gestureTransform(
                centroid = OffsetCompat(
                    x = zoomableCore.containerSize.width / 2f,
                    y = zoomableCore.containerSize.height / 2f
                ),
                panChange = OffsetCompat.Zero,
                zoomChange = 1.2f,
                rotationChange = 0f
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(false, this.rubberBandScale)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(88.89x88.89,-66660.0x-85560.89,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-66660.0x-3072.0,29340.01x2705.78]", this.contentDisplayRect)
            assertEquals("[3471.87x160.0,3528.12x260.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.setRubberBandScale(true)
            zoomableCore.gestureTransform(
                centroid = OffsetCompat(
                    x = zoomableCore.containerSize.width / 2f,
                    y = zoomableCore.containerSize.height / 2f
                ),
                panChange = OffsetCompat.Zero,
                zoomChange = 1.2f,
                rotationChange = 0f
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(true, this.rubberBandScale)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(96.0x96.0,-72036.01x-92482.56,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-72036.01x-3394.55,31644.0x2845.45]", this.contentDisplayRect)
            assertEquals("[3473.96x163.7,3526.04x256.3]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }
    }

    @Test
    fun testSetOneFingerScaleSpec() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(OneFingerScaleSpec.Default.toString(), this.oneFingerScaleSpec)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(88.89x88.89,-66660.0x-85560.89,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-66660.0x-3072.0,29340.01x2705.78]", this.contentDisplayRect)
            assertEquals("[3471.87x160.0,3528.12x260.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }

        val newOneFingerScaleSpec =
            OneFingerScaleSpec(com.github.panpf.zoomimage.zoom.DefaultPanToScaleTransformer(300))
        withContext(Dispatchers.Main) {
            zoomableCore.setOneFingerScaleSpec(newOneFingerScaleSpec)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(newOneFingerScaleSpec.toString(), this.oneFingerScaleSpec)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(88.89x88.89,-66660.0x-85560.89,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-66660.0x-3072.0,29340.01x2705.78]", this.contentDisplayRect)
            assertEquals("[3471.87x160.0,3528.12x260.0]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }
    }

    @Test
    fun testSetAnimationSpec() = runTest {
        // See testScale(), testSwitchScale(), testOffset(), testLocate(), testRollbackScale()
    }

    @Test
    fun testSetLimitOffsetWithinBaseVisibleRect() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = { }
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
            zoomableCore.setContentScale(ContentScaleCompat.Crop)
            zoomableCore.setLimitOffsetWithinBaseVisibleRect(false)
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(false, this.limitOffsetWithinBaseVisibleRect)
            assertEquals("(6.4x6.4,-15460.0x0.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(9.0x9.0,-61920.0x-11136.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[-15460.0x0.0,16540.0x1920.0]", this.contentBaseDisplayRect)
            assertEquals("[2415.62x0.0,2584.38x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-201060.0x-11136.0,86940.0x6144.0]", this.contentDisplayRect)
            assertEquals("[3490.62x193.33,3509.38x226.67]", this.contentVisibleRect)
            assertEquals("[-147780.0x-15360.0,139140.0x0.0]", this.userOffsetBoundsRect)
            assertEquals("6.4", this.minScale)
            assertEquals("19.2", this.mediumScale)
            assertEquals("57.6", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.setLimitOffsetWithinBaseVisibleRect(true)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(true, this.limitOffsetWithinBaseVisibleRect)
            assertEquals("(6.4x6.4,-15460.0x0.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[-15460.0x0.0,16540.0x1920.0]", this.contentBaseDisplayRect)
            assertEquals("[2415.62x0.0,2584.38x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-15460.0x0.0,16540.0x1920.0]", this.contentDisplayRect)
            assertEquals("[2415.62x-0.0,2584.38x300.0]", this.contentVisibleRect)
            assertEquals("[0.0x0.0,0.0x0.0]", this.userOffsetBoundsRect)
            assertEquals("6.4", this.minScale)
            assertEquals("19.2", this.mediumScale)
            assertEquals("57.6", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(true, this.limitOffsetWithinBaseVisibleRect)
            assertEquals("(6.4x6.4,-15460.0x0.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(9.0x9.0,-8640.0x-11136.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[-15460.0x0.0,16540.0x1920.0]", this.contentBaseDisplayRect)
            assertEquals("[2415.62x0.0,2584.38x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-147780.0x-11136.0,140220.0x6144.0]", this.contentDisplayRect)
            assertEquals("[2565.62x193.33,2584.38x226.67]", this.contentVisibleRect)
            assertEquals("[-8640.0x-15360.0,0.0x0.0]", this.userOffsetBoundsRect)
            assertEquals("6.4", this.minScale)
            assertEquals("19.2", this.mediumScale)
            assertEquals("57.6", this.maxScale)
        }
    }

    @Test
    fun testSetContainerWhitespaceMultiple() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = { }
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals("[0.0x0.0,0.0x0.0]", this.containerWhitespace)
            assertEquals("0.0", this.containerWhitespaceMultiple)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x928.0,1080.0x993.0]", this.contentDisplayRect)
            assertEquals("[-0.0x0.0,5000.0x300.0]", this.contentVisibleRect)
            assertEquals("[0.0x-0.4,0.0x-0.4]", this.userOffsetBoundsRect)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals("[0.0x0.0,0.0x0.0]", this.containerWhitespace)
            assertEquals("0.0", this.containerWhitespaceMultiple)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(88.89x88.89,-66660.0x-85560.89,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-66660.0x-3072.0,29340.01x2705.78]", this.contentDisplayRect)
            assertEquals("[3471.87x160.0,3528.12x260.0]", this.contentVisibleRect)
            assertEquals("[-94920.01x-86328.89,0.0x-82488.89]", this.userOffsetBoundsRect)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerWhitespaceMultiple(0.5f)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals("[0.0x0.0,0.0x0.0]", this.containerWhitespace)
            assertEquals("0.5", this.containerWhitespaceMultiple)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x928.0,1080.0x993.0]", this.contentDisplayRect)
            assertEquals("[-0.0x0.0,5000.0x300.0]", this.contentVisibleRect)
            assertEquals("[-540.0x-32.8,540.0x32.0]", this.userOffsetBoundsRect)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals("[0.0x0.0,0.0x0.0]", this.containerWhitespace)
            assertEquals("0.5", this.containerWhitespaceMultiple)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(88.89x88.89,-66660.0x-85560.89,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-66660.0x-3072.0,29340.01x2705.78]", this.contentDisplayRect)
            assertEquals("[3471.87x160.0,3528.12x260.0]", this.contentVisibleRect)
            assertEquals("[-95460.01x-87288.89,540.0x-81528.89]", this.userOffsetBoundsRect)
        }
    }

    @Test
    fun testSetContainerWhitespace() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = { }
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals("[0.0x0.0,0.0x0.0]", this.containerWhitespace)
            assertEquals("0.0", this.containerWhitespaceMultiple)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x928.0,1080.0x993.0]", this.contentDisplayRect)
            assertEquals("[-0.0x0.0,5000.0x300.0]", this.contentVisibleRect)
            assertEquals("[0.0x-0.4,0.0x-0.4]", this.userOffsetBoundsRect)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals("[0.0x0.0,0.0x0.0]", this.containerWhitespace)
            assertEquals("0.0", this.containerWhitespaceMultiple)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(88.89x88.89,-66660.0x-85560.89,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-66660.0x-3072.0,29340.01x2705.78]", this.contentDisplayRect)
            assertEquals("[3471.87x160.0,3528.12x260.0]", this.contentVisibleRect)
            assertEquals("[-94920.01x-86328.89,0.0x-82488.89]", this.userOffsetBoundsRect)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerWhitespace(
                ContainerWhitespace(
                    left = 100f,
                    top = 200f,
                    right = 300f,
                    bottom = 400f
                )
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals("[100.0x200.0,300.0x400.0]", this.containerWhitespace)
            assertEquals("0.0", this.containerWhitespaceMultiple)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[0.0x928.0,1080.0x993.0]", this.contentDisplayRect)
            assertEquals("[-0.0x0.0,5000.0x300.0]", this.contentVisibleRect)
            assertEquals("[-300.0x-0.4,100.0x-0.4]", this.userOffsetBoundsRect)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals("[100.0x200.0,300.0x400.0]", this.containerWhitespace)
            assertEquals("0.0", this.containerWhitespaceMultiple)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(88.89x88.89,-66660.0x-85560.89,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-66660.0x-3072.0,29340.01x2705.78]", this.contentDisplayRect)
            assertEquals("[3471.87x160.0,3528.12x260.0]", this.contentVisibleRect)
            assertEquals("[-95220.01x-86728.89,100.0x-82288.89]", this.userOffsetBoundsRect)
        }
    }

    @Test
    fun testSetKeepTransformWhenSameAspectRatioContentSizeChanged() = runTest {
        // See testSetContentSize()、 testSetContentOriginSize()

        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = { }
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(false, this.keepTransformWhenSameAspectRatioContentSizeChanged)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(88.89x88.89,-66660.0x-85560.89,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-66660.0x-3072.0,29340.01x2705.78]", this.contentDisplayRect)
            assertEquals("[3471.87x160.0,3528.12x260.0]", this.contentVisibleRect)
            assertEquals("[-94920.01x-86328.89,0.0x-82488.89]", this.userOffsetBoundsRect)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(true)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(true, this.keepTransformWhenSameAspectRatioContentSizeChanged)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(88.89x88.89,-66660.0x-85560.89,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-66660.0x-3072.0,29340.01x2705.78]", this.contentDisplayRect)
            assertEquals("[3471.87x160.0,3528.12x260.0]", this.contentVisibleRect)
            assertEquals("[-94920.01x-86328.89,0.0x-82488.89]", this.userOffsetBoundsRect)
        }
    }

    @Test
    fun testScale() = runTest {
        var transformChangedCount = 0
        val continuousTransformTypes = mutableListOf<Int>()
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {
                transformChangedCount += 1
                continuousTransformTypes.add(it.continuousTransformType)
            }
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
        }
        assertEquals("0.22", zoomableCore.minScale.format(2).toString())
        assertEquals("6.4", zoomableCore.mediumScale.format(2).toString())
        assertEquals("19.2", zoomableCore.maxScale.format(2).toString())
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())

        assertFailsWith(IllegalStateException::class) {
            zoomableCore.scale(
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }

        // targetScale. Beyond the maximum
        withContext(Dispatchers.Main) {
            zoomableCore.scale(
                targetScale = zoomableCore.maxScale + 3f,
                animated = false
            )
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())

        // targetScale. Beyond the minimum
        withContext(Dispatchers.Main) {
            zoomableCore.scale(
                targetScale = zoomableCore.minScale - 3f,
                animated = false
            )
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())

        // centroidContentPoint. default visible center
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        withContext(Dispatchers.Main) {
            zoomableCore.scale(
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals(
            expected = "2500.0x149.98",
            actual = zoomableCore.contentVisibleRect.center.toShortString()
        )

        // centroidContentPoint. setup
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        withContext(Dispatchers.Main) {
            zoomableCore.scale(
                targetScale = zoomableCore.maxScale,
                centroidContentPoint = zoomableCore.contentSize.toIntOffset().toOffset()
                    .times(0.7f),
                animated = false
            )
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals(
            expected = "3488.75x209.3",
            actual = zoomableCore.contentVisibleRect.center.toShortString()
        )

        // animated. false
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        transformChangedCount = 0
        continuousTransformTypes.clear()
        val duration1 = measureTime {
            withContext(Dispatchers.Main) {
                zoomableCore.scale(
                    targetScale = zoomableCore.maxScale,
                    animated = false
                )
            }
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())
        assertTrue(actual = duration1.inWholeMilliseconds <= 50, message = "duration=$duration1")
        assertEquals(expected = 1, actual = transformChangedCount)
        assertTrue(
            continuousTransformTypes.size == 1,
            message = continuousTransformTypes.joinToString()
        )
        assertTrue(
            continuousTransformTypes.first() == 0,
            message = continuousTransformTypes.joinToString()
        )

        // animated. true, global animationSpec
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        transformChangedCount = 0
        continuousTransformTypes.clear()
        val duration2 = measureTime {
            withContext(Dispatchers.Main) {
                zoomableCore.scale(
                    targetScale = zoomableCore.maxScale,
                    animated = true
                )
            }
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())
        assertTrue(duration2.inWholeMilliseconds in 300..350, message = "duration=$duration2")
        assertTrue(
            transformChangedCount in 10..50,
            message = "transformChangedCount=$transformChangedCount"
        )
        assertTrue(
            continuousTransformTypes.size >= 10,
            message = continuousTransformTypes.joinToString()
        )
        assertEquals(
            expected = continuousTransformTypes.size - 1,
            actual = continuousTransformTypes.count { it == ContinuousTransformType.SCALE },
            message = continuousTransformTypes.joinToString()
        )
        assertTrue(
            continuousTransformTypes.last() == 0,
            message = continuousTransformTypes.joinToString()
        )

        // animated. true, change global animationSpec
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        transformChangedCount = 0
        val duration3 = measureTime {
            zoomableCore.setAnimationSpec(TestZoomAnimationSpec(durationMillis = 500))
            withContext(Dispatchers.Main) {
                zoomableCore.scale(
                    targetScale = zoomableCore.maxScale,
                    animated = true
                )
            }
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())
        assertTrue(duration3.inWholeMilliseconds in 500..550, message = "duration=$duration3")
        assertTrue(
            transformChangedCount in 10..50,
            message = "transformChangedCount=$transformChangedCount"
        )

        // animated. true, setup animationSpec
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        transformChangedCount = 0
        val duration4 = measureTime {
            zoomableCore.setAnimationSpec(TestZoomAnimationSpec(durationMillis = 500))
            withContext(Dispatchers.Main) {
                zoomableCore.scale(
                    targetScale = zoomableCore.maxScale,
                    animated = true,
                    animationSpec = TestZoomAnimationSpec(durationMillis = 150)
                )
            }
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())
        assertTrue(duration4.inWholeMilliseconds in 150..200, message = "duration=$duration4")
        assertTrue(
            transformChangedCount in 10..50,
            message = "transformChangedCount=$transformChangedCount"
        )
    }

    @Test
    fun testSwitchScale() = runTest {
        var transformChangedCount = 0
        val continuousTransformTypes = mutableListOf<Int>()
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {
                transformChangedCount += 1
                continuousTransformTypes.add(it.continuousTransformType)
            }
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
        }
        assertEquals(false, zoomableCore.threeStepScale)
        assertEquals("0.22", zoomableCore.minScale.format(2).toString())
        assertEquals("6.4", zoomableCore.mediumScale.format(2).toString())
        assertEquals("19.2", zoomableCore.maxScale.format(2).toString())
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())

        assertFailsWith(IllegalStateException::class) {
            zoomableCore.switchScale(animated = false)
        }

        // targetScale
        withContext(Dispatchers.Main) {
            zoomableCore.switchScale(animated = false)
        }
        assertEquals("6.4", zoomableCore.transform.scaleX.format(2).toString())

        withContext(Dispatchers.Main) {
            zoomableCore.switchScale(animated = false)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())

        zoomableCore.setThreeStepScale(true)

        withContext(Dispatchers.Main) {
            zoomableCore.switchScale(animated = false)
        }
        assertEquals("6.4", zoomableCore.transform.scaleX.format(2).toString())

        withContext(Dispatchers.Main) {
            zoomableCore.switchScale(animated = false)
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())

        withContext(Dispatchers.Main) {
            zoomableCore.switchScale(animated = false)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())

        zoomableCore.setThreeStepScale(false)

        // centroidContentPoint. default visible center
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        withContext(Dispatchers.Main) {
            zoomableCore.switchScale(
                animated = false
            )
        }
        assertEquals("6.4", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals(
            expected = "2500.0x149.98",
            actual = zoomableCore.contentVisibleRect.center.toShortString()
        )

        // centroidContentPoint. setup
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        withContext(Dispatchers.Main) {
            zoomableCore.switchScale(
                centroidContentPoint = zoomableCore.contentSize.toIntOffset().toOffset()
                    .times(0.7f),
                animated = false
            )
        }
        assertEquals("6.4", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals(
            expected = "3466.25x149.98",
            actual = zoomableCore.contentVisibleRect.center.toShortString()
        )

        // animated. false
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        transformChangedCount = 0
        continuousTransformTypes.clear()
        val duration1 = measureTime {
            withContext(Dispatchers.Main) {
                zoomableCore.switchScale(
                    animated = false
                )
            }
        }
        assertEquals("6.4", zoomableCore.transform.scaleX.format(2).toString())
        assertTrue(actual = duration1.inWholeMilliseconds <= 50, message = "duration=$duration1")
        assertEquals(expected = 1, actual = transformChangedCount)
        assertTrue(
            continuousTransformTypes.size == 1,
            message = continuousTransformTypes.joinToString()
        )
        assertTrue(
            continuousTransformTypes.first() == 0,
            message = continuousTransformTypes.joinToString()
        )

        // animated. true, global animationSpec
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        transformChangedCount = 0
        continuousTransformTypes.clear()
        val duration2 = measureTime {
            withContext(Dispatchers.Main) {
                zoomableCore.switchScale(animated = true)
            }
        }
        assertEquals("6.4", zoomableCore.transform.scaleX.format(2).toString())
        assertTrue(duration2.inWholeMilliseconds in 300..350, message = "duration=$duration2")
        assertTrue(
            transformChangedCount in 10..50,
            message = "transformChangedCount=$transformChangedCount"
        )
        assertTrue(
            continuousTransformTypes.size >= 10,
            message = continuousTransformTypes.joinToString()
        )
        assertEquals(
            expected = continuousTransformTypes.size - 1,
            actual = continuousTransformTypes.count { it == ContinuousTransformType.SCALE },
            message = continuousTransformTypes.joinToString()
        )
        assertTrue(
            continuousTransformTypes.last() == 0,
            message = continuousTransformTypes.joinToString()
        )

        // animated. true, change global animationSpec
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        transformChangedCount = 0
        val duration3 = measureTime {
            zoomableCore.setAnimationSpec(TestZoomAnimationSpec(durationMillis = 500))
            withContext(Dispatchers.Main) {
                zoomableCore.switchScale(animated = true)
            }
        }
        assertEquals("6.4", zoomableCore.transform.scaleX.format(2).toString())
        assertTrue(duration3.inWholeMilliseconds in 500..550, message = "duration=$duration3")
        assertTrue(
            transformChangedCount in 10..50,
            message = "transformChangedCount=$transformChangedCount"
        )

        // animated. true, setup animationSpec
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        transformChangedCount = 0
        val duration4 = measureTime {
            zoomableCore.setAnimationSpec(TestZoomAnimationSpec(durationMillis = 500))
            withContext(Dispatchers.Main) {
                zoomableCore.switchScale(
                    animated = true,
                    animationSpec = TestZoomAnimationSpec(durationMillis = 150)
                )
            }
        }
        assertEquals("6.4", zoomableCore.transform.scaleX.format(2).toString())
        assertTrue(duration4.inWholeMilliseconds in 150..200, message = "duration=$duration4")
        assertTrue(
            transformChangedCount in 10..50,
            message = "transformChangedCount=$transformChangedCount"
        )
    }

    @Test
    fun testOffset() = runTest {
        var transformChangedCount = 0
        val continuousTransformTypes = mutableListOf<Int>()
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {
                transformChangedCount += 1
                continuousTransformTypes.add(it.continuousTransformType)
            }
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
            zoomableCore.scale(
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        assertEquals("-47460.0x-1919.6", zoomableCore.transform.offset.toShortString())

        assertFailsWith(IllegalStateException::class) {
            zoomableCore.offset(
                targetOffset = OffsetCompat(-500000f, -500000f),
                animated = false
            )
        }

        // targetOffset. Beyond the maximum
        withContext(Dispatchers.Main) {
            zoomableCore.offset(
                targetOffset = OffsetCompat(-500000f, -500000f),
                animated = false
            )
        }
        assertEquals("-94920.0x-3840.11", zoomableCore.transform.offset.toShortString())

        // targetOffset. Beyond the minimum
        withContext(Dispatchers.Main) {
            zoomableCore.offset(
                targetOffset = OffsetCompat(500000f, 500000f),
                animated = false
            )
        }
        assertEquals("0.0x-0.11", zoomableCore.transform.offset.toShortString())

        // animated. false
        transformChangedCount = 0
        continuousTransformTypes.clear()
        val duration1 = measureTime {
            withContext(Dispatchers.Main) {
                zoomableCore.offset(
                    targetOffset = OffsetCompat(-30000f, -5000f),
                    animated = false
                )
            }
        }
        assertEquals("-30000.0x-3840.11", zoomableCore.transform.offset.toShortString())
        assertTrue(actual = duration1.inWholeMilliseconds <= 50, message = "duration=$duration1")
        assertEquals(expected = 1, actual = transformChangedCount)
        assertTrue(
            continuousTransformTypes.size == 1,
            message = continuousTransformTypes.joinToString()
        )
        assertTrue(
            continuousTransformTypes.first() == 0,
            message = continuousTransformTypes.joinToString()
        )

        // animated. true, global animationSpec
        transformChangedCount = 0
        continuousTransformTypes.clear()
        val duration2 = measureTime {
            withContext(Dispatchers.Main) {
                zoomableCore.offset(
                    targetOffset = OffsetCompat(30000f, 5000f),
                    animated = true
                )
            }
        }
        assertEquals("0.0x-0.11", zoomableCore.transform.offset.toShortString())
        assertTrue(duration2.inWholeMilliseconds in 300..350, message = "duration=$duration2")
        assertTrue(
            transformChangedCount in 10..50,
            message = "transformChangedCount=$transformChangedCount"
        )
        assertTrue(
            continuousTransformTypes.size >= 10,
            message = continuousTransformTypes.joinToString()
        )
        assertEquals(
            expected = continuousTransformTypes.size - 1,
            actual = continuousTransformTypes.count { it == ContinuousTransformType.OFFSET },
            message = continuousTransformTypes.joinToString()
        )
        assertTrue(
            continuousTransformTypes.last() == 0,
            message = continuousTransformTypes.joinToString()
        )

        // animated. true, change global animationSpec
        transformChangedCount = 0
        val duration3 = measureTime {
            zoomableCore.setAnimationSpec(TestZoomAnimationSpec(durationMillis = 500))
            withContext(Dispatchers.Main) {
                zoomableCore.offset(
                    targetOffset = OffsetCompat(-30000f, -5000f),
                    animated = true
                )
            }
        }
        assertEquals("-30000.0x-3840.11", zoomableCore.transform.offset.toShortString())
        assertTrue(duration3.inWholeMilliseconds in 500..550, message = "duration=$duration3")
        assertTrue(
            transformChangedCount in 10..50,
            message = "transformChangedCount=$transformChangedCount"
        )

        // animated. true, setup animationSpec
        transformChangedCount = 0
        val duration4 = measureTime {
            zoomableCore.setAnimationSpec(TestZoomAnimationSpec(durationMillis = 500))
            withContext(Dispatchers.Main) {
                zoomableCore.offset(
                    targetOffset = OffsetCompat(30000f, 5000f),
                    animated = true,
                    animationSpec = TestZoomAnimationSpec(durationMillis = 150)
                )
            }
        }
        assertEquals("0.0x-0.11", zoomableCore.transform.offset.toShortString())
        assertTrue(duration4.inWholeMilliseconds in 150..200, message = "duration=$duration4")
        assertTrue(
            transformChangedCount in 10..50,
            message = "transformChangedCount=$transformChangedCount"
        )
    }

    @Test
    fun testLocate() = runTest {
        var transformChangedCount = 0
        val continuousTransformTypes = mutableListOf<Int>()
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {
                transformChangedCount += 1
                continuousTransformTypes.add(it.continuousTransformType)
            }
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
        }
        assertEquals("0.22", zoomableCore.minScale.format(2).toString())
        assertEquals("6.4", zoomableCore.mediumScale.format(2).toString())
        assertEquals("19.2", zoomableCore.maxScale.format(2).toString())
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals("0.0x928.0", zoomableCore.transform.offset.toShortString())

        assertFailsWith(IllegalStateException::class) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() + OffsetCompat(100f, 100f),
                animated = false
            )
        }

        // contentPoint and targetScale.
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() + OffsetCompat(100f, 100f),
                animated = false
            )
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals("0.0x928.0", zoomableCore.transform.offset.toShortString())

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = OffsetCompat.Zero - OffsetCompat(100f, 100f),
                animated = false
            )
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals("0.0x928.0", zoomableCore.transform.offset.toShortString())

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() + OffsetCompat(100f, 100f),
                targetScale = zoomableCore.maxScale + 3f,
                animated = false
            )
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals("-94920.0x-3840.11", zoomableCore.transform.offset.toShortString())

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = OffsetCompat.Zero - OffsetCompat(100f, 100f),
                animated = false
            )
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals("0.0x-0.11", zoomableCore.transform.offset.toShortString())

        // animated. false
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals("0.0x928.0", zoomableCore.transform.offset.toShortString())
        transformChangedCount = 0
        continuousTransformTypes.clear()
        val duration1 = measureTime {
            withContext(Dispatchers.Main) {
                zoomableCore.locate(
                    contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                    targetScale = zoomableCore.maxScale,
                    animated = false
                )
            }
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals("-47460.0x-1920.01", zoomableCore.transform.offset.toShortString())
        assertTrue(actual = duration1.inWholeMilliseconds <= 50, message = "duration=$duration1")
        assertEquals(expected = 1, actual = transformChangedCount)
        assertTrue(
            continuousTransformTypes.size == 1,
            message = continuousTransformTypes.joinToString()
        )
        assertTrue(
            continuousTransformTypes.first() == 0,
            message = continuousTransformTypes.joinToString()
        )

        // animated. true, global animationSpec
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals("0.0x928.0", zoomableCore.transform.offset.toShortString())
        transformChangedCount = 0
        continuousTransformTypes.clear()
        val duration2 = measureTime {
            withContext(Dispatchers.Main) {
                zoomableCore.locate(
                    contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                    targetScale = zoomableCore.maxScale,
                    animated = true
                )
            }
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals("-47460.0x-1920.01", zoomableCore.transform.offset.toShortString())
        assertTrue(duration2.inWholeMilliseconds in 300..350, message = "duration=$duration2")
        assertTrue(
            transformChangedCount in 10..50,
            message = "transformChangedCount=$transformChangedCount"
        )
        assertTrue(
            continuousTransformTypes.size >= 10,
            message = continuousTransformTypes.joinToString()
        )
        assertEquals(
            expected = continuousTransformTypes.size - 1,
            actual = continuousTransformTypes.count { it == ContinuousTransformType.LOCATE },
            message = continuousTransformTypes.joinToString()
        )
        assertTrue(
            continuousTransformTypes.last() == 0,
            message = continuousTransformTypes.joinToString()
        )

        // animated. true, change global animationSpec
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals("0.0x928.0", zoomableCore.transform.offset.toShortString())
        transformChangedCount = 0
        val duration3 = measureTime {
            zoomableCore.setAnimationSpec(TestZoomAnimationSpec(durationMillis = 500))
            withContext(Dispatchers.Main) {
                zoomableCore.locate(
                    contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                    targetScale = zoomableCore.maxScale,
                    animated = true
                )
            }
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals("-47460.0x-1920.01", zoomableCore.transform.offset.toShortString())
        assertTrue(duration3.inWholeMilliseconds in 500..550, message = "duration=$duration3")
        assertTrue(
            transformChangedCount in 10..50,
            message = "transformChangedCount=$transformChangedCount"
        )

        // animated. true, setup animationSpec
        withContext(Dispatchers.Main) {
            zoomableCore.reset(caller = "test", force = true)
        }
        assertEquals("0.22", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals("0.0x928.0", zoomableCore.transform.offset.toShortString())
        transformChangedCount = 0
        val duration4 = measureTime {
            zoomableCore.setAnimationSpec(TestZoomAnimationSpec(durationMillis = 500))
            withContext(Dispatchers.Main) {
                zoomableCore.locate(
                    contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                    targetScale = zoomableCore.maxScale,
                    animated = true,
                    animationSpec = TestZoomAnimationSpec(durationMillis = 150)
                )
            }
        }
        assertEquals("19.2", zoomableCore.transform.scaleX.format(2).toString())
        assertEquals("-47460.0x-1920.01", zoomableCore.transform.offset.toShortString())
        assertTrue(duration4.inWholeMilliseconds in 150..200, message = "duration=$duration4")
        assertTrue(
            transformChangedCount in 10..50,
            message = "transformChangedCount=$transformChangedCount"
        )
    }

    @Test
    fun testRotate() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(0, this.rotation)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.transform)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }

        assertFailsWith(IllegalStateException::class) {
            zoomableCore.rotate(90)
        }
        (0..361).forEach { rotation ->
            if (rotation % 90 == 0) {
                withContext(Dispatchers.Main) {
                    zoomableCore.rotate(rotation)
                }
            } else {
                assertFailsWith(IllegalArgumentException::class) {
                    withContext(Dispatchers.Main) {
                        zoomableCore.rotate(rotation)
                    }
                }
            }
        }
        withContext(Dispatchers.Main) {
            zoomableCore.rotate(0)
        }
        assertEquals(0, zoomableCore.rotation)

        withContext(Dispatchers.Main) {
            zoomableCore.rotate(90)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(90, this.rotation)
            assertEquals("(0.38x0.38,-419.4x902.4,90.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(0.38x0.38,-419.4x902.4,90.0,0.0x0.0,2.31x0.08)", this.transform)
            assertEquals("0.38", this.minScale)
            assertEquals("3.6", this.mediumScale)
            assertEquals("10.8", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.rotate(180)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(180, this.rotation)
            assertEquals("(0.22x0.22,0.0x928.0,180.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(0.22x0.22,0.0x928.0,180.0,0.0x0.0,2.31x0.08)", this.transform)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.rotate(270)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(270, this.rotation)
            assertEquals("(0.38x0.38,-419.4x902.4,270.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(0.38x0.38,-419.4x902.4,270.0,0.0x0.0,2.31x0.08)", this.transform)
            assertEquals("0.38", this.minScale)
            assertEquals("3.6", this.mediumScale)
            assertEquals("10.8", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.rotate(360)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(0, this.rotation)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.transform)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }

        // test reset
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.7f,
                targetScale = zoomableCore.maxScale,
                animated = false,
            )
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(0, this.rotation)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(19.2x19.2,-66660.0x-3072.0,0.0,0.0x0.0,2.31x0.08)", this.transform)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.rotate(90)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(90, this.rotation)
            assertEquals("(0.38x0.38,-419.4x902.4,90.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(0.38x0.38,-419.4x902.4,90.0,0.0x0.0,2.31x0.08)", this.transform)
            assertEquals("0.38", this.minScale)
            assertEquals("3.6", this.mediumScale)
            assertEquals("10.8", this.maxScale)
        }

        // test touch point
        val touchPoint = zoomableCore.containerSize.toIntOffset().toOffset() * 0.7f

        withContext(Dispatchers.Main) {
            zoomableCore.rotate(90)
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.maxScale,
                animated = false,
            )
        }
        assertEquals(
            expected = "2535.56x130.0",
            actual = zoomableCore.touchPointToContentPoint(touchPoint).toShortString()
        )

        withContext(Dispatchers.Main) {
            zoomableCore.rotate(180)
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.maxScale,
                animated = false,
            )
        }
        assertEquals(
            expected = "2488.75x130.0",
            actual = zoomableCore.touchPointToContentPoint(touchPoint).toShortString()
        )

        withContext(Dispatchers.Main) {
            zoomableCore.rotate(270)
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.maxScale,
                animated = false,
            )
        }
        assertEquals(
            expected = "2464.44x170.0",
            actual = zoomableCore.touchPointToContentPoint(touchPoint).toShortString()
        )

        withContext(Dispatchers.Main) {
            zoomableCore.rotate(360)
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.maxScale,
                animated = false,
            )
        }
        assertEquals(
            expected = "2511.25x170.0",
            actual = zoomableCore.touchPointToContentPoint(touchPoint).toShortString()
        )
    }

    @Test
    fun testGetNextStepScale() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(false, this.threeStepScale)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.transform)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }
        assertEquals(
            expected = "6.4, 0.22, 6.4",
            actual = listOf(
                withContext(Dispatchers.Main) {
                    zoomableCore.getNextStepScale().apply {
                        zoomableCore.scale(this, animated = false)
                    }.format(2)
                },
                withContext(Dispatchers.Main) {
                    zoomableCore.getNextStepScale().apply {
                        zoomableCore.scale(this, animated = false)
                    }.format(2)
                },
                withContext(Dispatchers.Main) {
                    zoomableCore.getNextStepScale().apply {
                        zoomableCore.scale(this, animated = false)
                    }.format(2)
                },
            ).joinToString()
        )
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(false, this.threeStepScale)
            assertEquals("(6.4x6.4,-15460.0x0.29,0.0,0.0x0.0,2.31x0.08)", this.transform)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }

        withContext(Dispatchers.Main) {
            zoomableCore.setThreeStepScale(true)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(true, this.threeStepScale)
            assertEquals("(6.4x6.4,-15460.0x0.29,0.0,0.0x0.0,2.31x0.08)", this.transform)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }
        assertEquals(
            expected = "19.2, 0.22, 6.4, 19.2",
            actual = listOf(
                withContext(Dispatchers.Main) {
                    zoomableCore.getNextStepScale().apply {
                        zoomableCore.scale(this, animated = false)
                    }.format(2)
                },
                withContext(Dispatchers.Main) {
                    zoomableCore.getNextStepScale().apply {
                        zoomableCore.scale(this, animated = false)
                    }.format(2)
                },
                withContext(Dispatchers.Main) {
                    zoomableCore.getNextStepScale().apply {
                        zoomableCore.scale(this, animated = false)
                    }.format(2)
                },
                withContext(Dispatchers.Main) {
                    zoomableCore.getNextStepScale().apply {
                        zoomableCore.scale(this, animated = false)
                    }.format(2)
                },
            ).joinToString()
        )
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(true, this.threeStepScale)
            assertEquals("(19.2x19.2,-47460.0x-1919.41,0.0,0.0x0.0,2.31x0.08)", this.transform)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }
    }

    @Test
    fun testTouchPointToContentPoint() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )
        val containerSize = IntSizeCompat(1080, 1920)
        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(containerSize)
            zoomableCore.setContentSize(IntSizeCompat(500, 300))
        }
        assertEquals(
            expected = "(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)",
            actual = zoomableCore.toProperties().transform
        )
        assertEquals("Fit", zoomableCore.toProperties().contentScale)
        assertEquals("Center", zoomableCore.toProperties().alignment)

        // top left corner
        assertEquals(
            expected = "100.0x0.0",
            actual = zoomableCore.touchPointToContentPoint(
                containerSize.toIntOffset().toOffset().times(ScaleFactorCompat(0.2f, 0.2f))
            ).toShortString()
        )

        // top right corner
        assertEquals(
            expected = "100.0x300.0",
            actual = zoomableCore.touchPointToContentPoint(
                containerSize.toIntOffset().toOffset().times(ScaleFactorCompat(0.2f, 0.8f))
            ).toShortString()
        )

        // center center
        assertEquals(
            expected = "250.0x150.0",
            actual = zoomableCore.touchPointToContentPoint(
                containerSize.toIntOffset().toOffset().times(ScaleFactorCompat(0.5f, 0.5f))
            ).toShortString()
        )

        // bottom left corner
        assertEquals(
            expected = "400.0x0.0",
            actual = zoomableCore.touchPointToContentPoint(
                containerSize.toIntOffset().toOffset().times(ScaleFactorCompat(0.8f, 0.2f))
            ).toShortString()
        )

        // bottom right corner
        assertEquals(
            expected = "400.0x300.0",
            actual = zoomableCore.touchPointToContentPoint(
                containerSize.toIntOffset().toOffset().times(ScaleFactorCompat(0.8f, 0.8f))
            ).toShortString()
        )

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = OffsetCompat(
                    x = zoomableCore.contentSize.width * 0.7f,
                    y = zoomableCore.contentSize.height * 0.7f
                ),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        assertEquals(
            expected = "(19.44x19.44,-6264.0x-3122.4,0.0,0.0x0.0,0.23x0.08)",
            actual = zoomableCore.toProperties().transform
        )
        assertEquals("Fit", zoomableCore.toProperties().contentScale)
        assertEquals("Center", zoomableCore.toProperties().alignment)

        // top left corner
        assertEquals(
            expected = "333.33x180.37",
            actual = zoomableCore.touchPointToContentPoint(
                containerSize.toIntOffset().toOffset().times(ScaleFactorCompat(0.2f, 0.2f))
            ).toShortString()
        )

        // top right corner
        assertEquals(
            expected = "333.33x239.63",
            actual = zoomableCore.touchPointToContentPoint(
                containerSize.toIntOffset().toOffset().times(ScaleFactorCompat(0.2f, 0.8f))
            ).toShortString()
        )

        // center center
        assertEquals(
            expected = "350.0x210.0",
            actual = zoomableCore.touchPointToContentPoint(
                containerSize.toIntOffset().toOffset().times(ScaleFactorCompat(0.5f, 0.5f))
            ).toShortString()
        )

        // bottom left corner
        assertEquals(
            expected = "366.67x180.37",
            actual = zoomableCore.touchPointToContentPoint(
                containerSize.toIntOffset().toOffset().times(ScaleFactorCompat(0.8f, 0.2f))
            ).toShortString()
        )

        // bottom right corner
        assertEquals(
            expected = "366.67x239.63",
            actual = zoomableCore.touchPointToContentPoint(
                containerSize.toIntOffset().toOffset().times(ScaleFactorCompat(0.8f, 0.8f))
            ).toShortString()
        )
    }

    @Test
    fun testCanScroll() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )
        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(500, 300))
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() * ScaleFactorCompat(0f, 0f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        assertEquals("(START,START)", zoomableCore.toProperties().scrollEdge)
        assertEquals(
            expected = listOf(true, false, true, false),
            actual = listOf(
                zoomableCore.canScroll(horizontal = true, direction = 1),
                zoomableCore.canScroll(horizontal = true, direction = -1),
                zoomableCore.canScroll(horizontal = false, direction = 1),
                zoomableCore.canScroll(horizontal = false, direction = -1),
            )
        )

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() * ScaleFactorCompat(0.5f, 0f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        assertEquals("(NONE,START)", zoomableCore.toProperties().scrollEdge)
        assertEquals(
            expected = listOf(true, true, true, false),
            actual = listOf(
                zoomableCore.canScroll(horizontal = true, direction = 1),
                zoomableCore.canScroll(horizontal = true, direction = -1),
                zoomableCore.canScroll(horizontal = false, direction = 1),
                zoomableCore.canScroll(horizontal = false, direction = -1),
            )
        )

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() * ScaleFactorCompat(1f, 0f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        assertEquals("(END,START)", zoomableCore.toProperties().scrollEdge)
        assertEquals(
            expected = listOf(false, true, true, false),
            actual = listOf(
                zoomableCore.canScroll(horizontal = true, direction = 1),
                zoomableCore.canScroll(horizontal = true, direction = -1),
                zoomableCore.canScroll(horizontal = false, direction = 1),
                zoomableCore.canScroll(horizontal = false, direction = -1),
            )
        )

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() * ScaleFactorCompat(0f, 0.5f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        assertEquals("(START,NONE)", zoomableCore.toProperties().scrollEdge)
        assertEquals(
            expected = listOf(true, false, true, true),
            actual = listOf(
                zoomableCore.canScroll(horizontal = true, direction = 1),
                zoomableCore.canScroll(horizontal = true, direction = -1),
                zoomableCore.canScroll(horizontal = false, direction = 1),
                zoomableCore.canScroll(horizontal = false, direction = -1),
            )
        )

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() * ScaleFactorCompat(0.5f, 0.5f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        assertEquals("(NONE,NONE)", zoomableCore.toProperties().scrollEdge)
        assertEquals(
            expected = listOf(true, true, true, true),
            actual = listOf(
                zoomableCore.canScroll(horizontal = true, direction = 1),
                zoomableCore.canScroll(horizontal = true, direction = -1),
                zoomableCore.canScroll(horizontal = false, direction = 1),
                zoomableCore.canScroll(horizontal = false, direction = -1),
            )
        )

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() * ScaleFactorCompat(1f, 0.5f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        assertEquals("(END,NONE)", zoomableCore.toProperties().scrollEdge)
        assertEquals(
            expected = listOf(false, true, true, true),
            actual = listOf(
                zoomableCore.canScroll(horizontal = true, direction = 1),
                zoomableCore.canScroll(horizontal = true, direction = -1),
                zoomableCore.canScroll(horizontal = false, direction = 1),
                zoomableCore.canScroll(horizontal = false, direction = -1),
            )
        )

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() * ScaleFactorCompat(0f, 1f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        assertEquals("(START,END)", zoomableCore.toProperties().scrollEdge)
        assertEquals(
            expected = listOf(true, false, false, true),
            actual = listOf(
                zoomableCore.canScroll(horizontal = true, direction = 1),
                zoomableCore.canScroll(horizontal = true, direction = -1),
                zoomableCore.canScroll(horizontal = false, direction = 1),
                zoomableCore.canScroll(horizontal = false, direction = -1),
            )
        )

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() * ScaleFactorCompat(0.5f, 1f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        assertEquals("(NONE,END)", zoomableCore.toProperties().scrollEdge)
        assertEquals(
            expected = listOf(true, true, false, true),
            actual = listOf(
                zoomableCore.canScroll(horizontal = true, direction = 1),
                zoomableCore.canScroll(horizontal = true, direction = -1),
                zoomableCore.canScroll(horizontal = false, direction = 1),
                zoomableCore.canScroll(horizontal = false, direction = -1),
            )
        )

        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() * ScaleFactorCompat(1f, 1f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        assertEquals("(END,END)", zoomableCore.toProperties().scrollEdge)
        assertEquals(
            expected = listOf(false, true, false, true),
            actual = listOf(
                zoomableCore.canScroll(horizontal = true, direction = 1),
                zoomableCore.canScroll(horizontal = true, direction = -1),
                zoomableCore.canScroll(horizontal = false, direction = 1),
                zoomableCore.canScroll(horizontal = false, direction = -1),
            )
        )
    }

    @Test
    fun testReset() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )

        // requiredMainThread
        assertFailsWith(IllegalStateException::class) {
            zoomableCore.reset("testReset")
        }

        // isNotChanged
        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(500, 300))
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() * ScaleFactorCompat(0.5f, 0.5f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        assertEquals(
            "(9.0x9.0,-4320.0x-7680.0,0.0,0.0x0.0,0.0x0.0)",
            zoomableCore.toProperties().userTransform
        )
        withContext(Dispatchers.Main) {
            zoomableCore.reset("testReset")
        }
        assertEquals(
            "(9.0x9.0,-4320.0x-7680.0,0.0,0.0x0.0,0.0x0.0)",
            zoomableCore.toProperties().userTransform
        )

        // force
        withContext(Dispatchers.Main) {
            zoomableCore.reset("testReset", force = true)
        }
        assertEquals(
            "(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)",
            zoomableCore.toProperties().userTransform
        )
    }

    @Test
    fun testStopAllAnimation() = runTest {
        var stopHalfway = false
        var onTransformChangedCount = 0
        val scope = this
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = { zoomable ->
                onTransformChangedCount++
                if (onTransformChangedCount >= 5 && stopHalfway) {
                    stopHalfway = false
                    scope.launch {
                        zoomable.stopAllAnimation("Test")
                    }
                }
            }
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
        }

        /*
         * stopAnimation
         */

        stopHalfway = false
        onTransformChangedCount = 0
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() * ScaleFactorCompat(0.5f, 0.5f),
                targetScale = zoomableCore.maxScale,
                animated = true
            )
        }
        assertEquals("2500.0x150.0", zoomableCore.contentVisibleRect.center.toShortString())
        assertTrue(
            onTransformChangedCount >= 10,
            message = "onTransformChangedCount=$onTransformChangedCount"
        )

        stopHalfway = true
        onTransformChangedCount = 0
        withContext(Dispatchers.Main) {
            zoomableCore.reset("Test", force = true)
        }
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() * ScaleFactorCompat(0.5f, 0.5f),
                targetScale = zoomableCore.maxScale,
                animated = true
            )
        }
        assertEquals("2500.0x150.0", zoomableCore.contentVisibleRect.center.toShortString())
        assertTrue(
            onTransformChangedCount <= 10,
            message = "onTransformChangedCount=$onTransformChangedCount"
        )

        /*
         * stopFlingAnimation
         */
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset()
                    .toOffset() * ScaleFactorCompat(0.5f, 0.5f),
                targetScale = zoomableCore.maxScale,
                animated = false
            )
        }
        stopHalfway = false
        onTransformChangedCount = 0
        withContext(Dispatchers.Main) {
            zoomableCore.fling(
                velocity = OffsetCompat(10000f, 10000f),
                extras = emptyMap(),
            )
        }
        assertTrue(
            onTransformChangedCount >= 10,
            message = "onTransformChangedCount=$onTransformChangedCount"
        )

        stopHalfway = true
        onTransformChangedCount = 0
        withContext(Dispatchers.Main) {
            zoomableCore.fling(
                velocity = OffsetCompat(-10000f, -10000f),
                extras = emptyMap(),
            )
        }
        assertTrue(
            onTransformChangedCount < 10,
            message = "onTransformChangedCount=$onTransformChangedCount"
        )
    }

    @Test
    fun testRollbackScale() = runTest {
        val continuousTransformTypes = mutableListOf<Int>()
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {
                continuousTransformTypes.add(it.continuousTransformType)
            }
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
        }
        zoomableCore.toProperties().apply {
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }

        assertEquals(0.22f, zoomableCore.transform.scaleX.format(2))
        assertFalse(withContext(Dispatchers.Main) { zoomableCore.rollbackScale() })

        withContext(Dispatchers.Main) {
            zoomableCore.scale(zoomableCore.mediumScale)
        }
        assertEquals(6.4f, zoomableCore.transform.scaleX.format(2))
        assertFalse(withContext(Dispatchers.Main) { zoomableCore.rollbackScale() })

        withContext(Dispatchers.Main) {
            zoomableCore.scale(zoomableCore.maxScale)
        }
        assertEquals(19.2f, zoomableCore.transform.scaleX.format(2))
        assertFalse(withContext(Dispatchers.Main) { zoomableCore.rollbackScale() })

        withContext(Dispatchers.Main) {
            zoomableCore.gestureTransform(
                centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
                panChange = OffsetCompat.Zero,
                zoomChange = 2f,
                rotationChange = 0f
            )
        }
        assertEquals(38.4f, zoomableCore.transform.scaleX.format(2))

        continuousTransformTypes.clear()
        var result: Boolean
        val duration = measureTime {
            withContext(Dispatchers.Main) {
                result = zoomableCore.rollbackScale()
            }
        }
        assertTrue(result)
        assertEquals(19.2f, zoomableCore.transform.scaleX.format(2))
        assertTrue(duration.inWholeMilliseconds in 300..350, message = "duration=$duration")
        assertTrue(
            continuousTransformTypes.size >= 10,
            message = continuousTransformTypes.joinToString()
        )
        assertEquals(
            expected = continuousTransformTypes.size - 1,
            actual = continuousTransformTypes.count { it == ContinuousTransformType.SCALE },
            message = continuousTransformTypes.joinToString()
        )
        assertTrue(
            continuousTransformTypes.last() == 0,
            message = continuousTransformTypes.joinToString()
        )

        withContext(Dispatchers.Main) {
            zoomableCore.scale(zoomableCore.minScale)
        }
        assertEquals(0.22f, zoomableCore.transform.scaleX.format(2))

        withContext(Dispatchers.Main) {
            zoomableCore.gestureTransform(
                centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
                panChange = OffsetCompat.Zero,
                zoomChange = 0.4f,
                rotationChange = 0f
            )
        }
        assertEquals(0.11f, zoomableCore.transform.scaleX.format(2))

        var result2: Boolean
        val duration2 = measureTime {
            withContext(Dispatchers.Main) {
                zoomableCore.setAnimationSpec(TestZoomAnimationSpec(500))
                result2 = zoomableCore.rollbackScale()
            }
        }
        assertTrue(result2)
        assertTrue(
            actual = zoomableCore.transform.scaleX.format(2) in 0.21f..0.22f,
            message = zoomableCore.transform.scaleX.toString()
        )
        assertTrue(duration2.inWholeMilliseconds in 500..550, message = "duration=$duration2")
    }

    @Test
    fun testGestureTransform() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
        }

        // panChange. +
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.maxScale
            )
        }
        assertEquals(
            "(19.2x19.2,-47460.0x-1920.01,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )
        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat(10f, 10f),
            zoomChange = 1f,
            rotationChange = 0f
        )
        assertEquals(
            "(19.2x19.2,-47450.01x-1910.01,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        // panChange. -
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.maxScale
            )
        }
        assertEquals(
            "(19.2x19.2,-47460.0x-1920.01,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )
        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat(-10f, -10f),
            zoomChange = 1f,
            rotationChange = 0f
        )
        assertEquals(
            "(19.2x19.2,-47470.0x-1930.01,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        // panChange. -. bounds
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset(),
                targetScale = zoomableCore.maxScale
            )
        }
        assertEquals(
            "(19.2x19.2,-94920.0x-3840.11,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )
        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat(-10f, -10f),
            zoomChange = 1f,
            rotationChange = 0f
        )
        assertEquals(
            "(19.2x19.2,-94920.0x-3840.11,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        // panChange. +. bounds
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = OffsetCompat.Zero,
                targetScale = zoomableCore.maxScale
            )
        }
        assertEquals(
            "(19.2x19.2,0.0x-0.11,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )
        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat(10f, 10f),
            zoomChange = 1f,
            rotationChange = 0f
        )
        assertEquals(
            "(19.2x19.2,0.0x-0.11,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        // zoomChange. +
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.mediumScale,
            )
        }
        assertEquals(
            "(6.4x6.4,-15460.0x0.29,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )
        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat.Zero,
            zoomChange = 1.2f,
            rotationChange = 0f
        )
        assertEquals(
            "(7.68x7.68,-18660.0x-191.64,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        // zoomChange. -
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.mediumScale,
            )
        }
        assertEquals(
            "(6.4x6.4,-15460.0x0.29,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )
        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat.Zero,
            zoomChange = 0.8f,
            rotationChange = 0f
        )
        assertEquals(
            "(5.12x5.12,-12260.0x192.04,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        // zoomChange. +. bounds. rubberBandScale true
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.maxScale,
            )
        }
        zoomableCore.setRubberBandScale(true)
        assertEquals(
            "(19.2x19.2,-47460.0x-1920.01,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )
        assertEquals(true, zoomableCore.rubberBandScale)
        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat.Zero,
            zoomChange = 1.2f,
            rotationChange = 0f
        )
        assertEquals(
            "(20.74x20.74,-51300.01x-2150.4,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        // zoomChange. -. bounds. rubberBandScale true
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.minScale,
            )
        }
        zoomableCore.setRubberBandScale(true)
        assertEquals(
            "(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )
        assertEquals(true, zoomableCore.rubberBandScale)
        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat.Zero,
            zoomChange = 0.4f,
            rotationChange = 0f
        )
        assertEquals(
            "(0.11x0.11,270.0x944.0,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        // zoomChange. +. bounds. rubberBandScale false
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.maxScale,
            )
        }
        zoomableCore.setRubberBandScale(false)
        assertEquals(
            "(19.2x19.2,-47460.0x-1920.01,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )
        assertEquals(false, zoomableCore.rubberBandScale)
        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat.Zero,
            zoomChange = 1.2f,
            rotationChange = 0f
        )
        assertEquals(
            "(19.2x19.2,-47460.01x-1920.01,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        // zoomChange. -. bounds. rubberBandScale false
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.minScale,
            )
        }
        zoomableCore.setRubberBandScale(false)
        assertEquals(
            "(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )
        assertEquals(false, zoomableCore.rubberBandScale)
        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat.Zero,
            zoomChange = 0.4f,
            rotationChange = 0f
        )
        assertEquals(
            "(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        // rotationChange
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.mediumScale,
            )
        }
        assertEquals(
            "(6.4x6.4,-15460.0x0.29,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat.Zero,
            zoomChange = 1f,
            rotationChange = 90f
        )
        assertEquals(
            "(6.4x6.4,-15460.0x0.29,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat.Zero,
            zoomChange = 1f,
            rotationChange = 180f
        )
        assertEquals(
            "(6.4x6.4,-15460.0x0.29,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat.Zero,
            zoomChange = 1f,
            rotationChange = 270f
        )
        assertEquals(
            "(6.4x6.4,-15460.0x0.29,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        zoomableCore.gestureTransform(
            centroid = zoomableCore.containerSize.toIntOffset().toOffset() * 0.5f,
            panChange = OffsetCompat.Zero,
            zoomChange = 1f,
            rotationChange = 360f
        )
        assertEquals(
            "(6.4x6.4,-15460.0x0.29,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )
    }

    @Test
    fun testFling() = runTest {
        var transformChangedCount = 0
        val continuousTransformTypes = mutableListOf<Int>()
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {
                transformChangedCount += 1
                continuousTransformTypes.add(it.continuousTransformType)
            }
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
        }
        withContext(Dispatchers.Main) {
            zoomableCore.locate(
                contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                targetScale = zoomableCore.maxScale
            )
        }
        assertEquals(
            "(19.2x19.2,-47460.0x-1920.01,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )

        transformChangedCount = 0
        continuousTransformTypes.clear()
        zoomableCore.fling(
            velocity = OffsetCompat(10000f, 10000f),
            extras = emptyMap(),
        )
        assertEquals(
            "(19.2x19.2,0.0x-0.11,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )
        assertTrue(
            transformChangedCount >= 10,
            message = "transformChangedCount=$transformChangedCount"
        )
        assertTrue(
            continuousTransformTypes.size >= 10,
            message = continuousTransformTypes.joinToString()
        )
        assertEquals(
            expected = continuousTransformTypes.size - 1,
            actual = continuousTransformTypes.count { it == ContinuousTransformType.FLING },
            message = continuousTransformTypes.joinToString()
        )
        assertTrue(
            continuousTransformTypes.last() == 0,
            message = continuousTransformTypes.joinToString()
        )

        transformChangedCount = 0
        continuousTransformTypes.clear()
        zoomableCore.fling(
            velocity = OffsetCompat(-10000f, -10000f),
            extras = emptyMap(),
        )
        assertEquals(
            "(19.2x19.2,-94920.0x-3840.0,0.0,0.0x0.0,2.31x0.08)",
            zoomableCore.transform.toShortString()
        )
        assertTrue(
            transformChangedCount >= 10,
            message = "transformChangedCount=$transformChangedCount"
        )
        assertTrue(
            continuousTransformTypes.size >= 10,
            message = continuousTransformTypes.joinToString()
        )
        assertEquals(
            expected = continuousTransformTypes.size - 1,
            actual = continuousTransformTypes.count { it == ContinuousTransformType.FLING },
            message = continuousTransformTypes.joinToString()
        )
        assertTrue(
            continuousTransformTypes.last() == 0,
            message = continuousTransformTypes.joinToString()
        )
    }

    @Test
    fun testSetContinuousTransformType() = runTest {
        // See testScale(), testSwitchScale(), testOffset(), testLocate(), testFling()

        var count = 0
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = { count++ }
        )
        assertEquals(0, count)
        assertEquals(0, zoomableCore.continuousTransformType)

        zoomableCore.setContinuousTransformType(ContinuousTransformType.LOCATE)
        assertEquals(1, count)
        assertEquals(ContinuousTransformType.LOCATE, zoomableCore.continuousTransformType)

        zoomableCore.setContinuousTransformType(ContinuousTransformType.OFFSET)
        assertEquals(2, count)
        assertEquals(ContinuousTransformType.OFFSET, zoomableCore.continuousTransformType)

        zoomableCore.setContinuousTransformType(ContinuousTransformType.SCALE)
        assertEquals(3, count)
        assertEquals(ContinuousTransformType.SCALE, zoomableCore.continuousTransformType)

        zoomableCore.setContinuousTransformType(ContinuousTransformType.FLING)
        assertEquals(4, count)
        assertEquals(ContinuousTransformType.FLING, zoomableCore.continuousTransformType)

        zoomableCore.setContinuousTransformType(ContinuousTransformType.GESTURE)
        assertEquals(5, count)
        assertEquals(ContinuousTransformType.GESTURE, zoomableCore.continuousTransformType)

        zoomableCore.setContinuousTransformType(-1000000)
        assertEquals(6, count)
        assertEquals(-1000000, zoomableCore.continuousTransformType)
    }

    @Test
    fun testCheckSupportGestureType() = runTest {
        val flags = GestureType.KEYBOARD_SCALE or GestureType.DOUBLE_TAP_SCALE
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )
        assertFalse(
            zoomableCore.checkSupportGestureType(
                disabledGestureTypes = flags,
                gestureType = GestureType.KEYBOARD_SCALE
            )
        )
        assertFalse(
            zoomableCore.checkSupportGestureType(
                disabledGestureTypes = flags,
                gestureType = GestureType.DOUBLE_TAP_SCALE
            )
        )
        assertTrue(
            zoomableCore.checkSupportGestureType(
                disabledGestureTypes = flags,
                gestureType = GestureType.ONE_FINGER_DRAG
            )
        )
        assertTrue(
            zoomableCore.checkSupportGestureType(
                disabledGestureTypes = flags,
                gestureType = GestureType.ONE_FINGER_SCALE
            )
        )
        assertTrue(
            zoomableCore.checkSupportGestureType(
                disabledGestureTypes = flags,
                gestureType = GestureType.KEYBOARD_DRAG
            )
        )
        assertTrue(
            zoomableCore.checkSupportGestureType(
                disabledGestureTypes = flags,
                gestureType = GestureType.TWO_FINGER_SCALE
            )
        )
        assertTrue(
            zoomableCore.checkSupportGestureType(
                disabledGestureTypes = flags,
                gestureType = GestureType.MOUSE_WHEEL_SCALE
            )
        )
    }

    @Test
    fun testRtlLayoutDirection() = runTest {
        // alignment
        val getBaseTransform: suspend (AlignmentCompat, Boolean, IntSizeCompat) -> TransformCompat =
            { alignment, rtlLayoutDirection, contentSize ->
                val zoomableCore = ZoomableCore(
                    logger = Logger(tag = "Test"),
                    module = "ZoomableCoreTest",
                    animationAdapter = TestAnimationAdapter(),
                    onTransformChanged = {}
                )

                withContext(Dispatchers.Main) {
                    zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
                    zoomableCore.setContentSize(contentSize)
                    zoomableCore.setAlignment(alignment)
                    zoomableCore.setRtlLayoutDirection(rtlLayoutDirection)
                }
                zoomableCore.baseTransform
            }

        val horContent = IntSizeCompat(5000, 300)
        val verContent = IntSizeCompat(300, 5000)
        listOf(
            AlignmentCompat.TopStart, AlignmentCompat.TopCenter, AlignmentCompat.TopEnd,
            AlignmentCompat.CenterStart, AlignmentCompat.Center, AlignmentCompat.CenterEnd,
            AlignmentCompat.BottomStart, AlignmentCompat.BottomCenter, AlignmentCompat.BottomEnd,
        ).forEach { alignment ->
            if (alignment.isStart || alignment.isEnd) {
                assertEquals(
                    expected = getBaseTransform(alignment, false, horContent).toShortString(),
                    actual = getBaseTransform(alignment, true, horContent).toShortString(),
                    message = "alignment=${alignment.name}"
                )
                assertNotEquals(
                    illegal = getBaseTransform(alignment, false, verContent).toShortString(),
                    actual = getBaseTransform(alignment, true, verContent).toShortString(),
                    message = "alignment=${alignment.name}"
                )
            } else {
                assertEquals(
                    expected = getBaseTransform(alignment, false, horContent).toShortString(),
                    actual = getBaseTransform(alignment, true, horContent).toShortString(),
                    message = "alignment=$alignment"
                )
                assertEquals(
                    expected = getBaseTransform(alignment, false, verContent).toShortString(),
                    actual = getBaseTransform(alignment, true, verContent).toShortString(),
                    message = "alignment=$alignment"
                )
            }
        }

        // containerWhitespace
        val getUserOffsetBounds: suspend (ContainerWhitespace, Boolean) -> RectCompat =
            { containerWhitespace, rtlLayoutDirection ->
                val zoomableCore = ZoomableCore(
                    logger = Logger(tag = "Test"),
                    module = "ZoomableCoreTest",
                    animationAdapter = TestAnimationAdapter(),
                    onTransformChanged = {}
                )

                withContext(Dispatchers.Main) {
                    zoomableCore.setRtlLayoutDirection(rtlLayoutDirection)
                    zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
                    zoomableCore.setContentSize(IntSizeCompat(500, 300))
                    zoomableCore.setContainerWhitespace(containerWhitespace)
                    zoomableCore.locate(
                        contentPoint = zoomableCore.contentSize.toIntOffset().toOffset() * 0.5f,
                        targetScale = zoomableCore.maxScale
                    )
                }
                zoomableCore.userOffsetBoundsRect
            }

        assertNotEquals(
            illegal = getUserOffsetBounds(ContainerWhitespace(100f, 200f, 300f, 400f), false),
            actual = getUserOffsetBounds(ContainerWhitespace(100f, 200f, 300f, 400f), true),
        )
        assertEquals(
            expected = getUserOffsetBounds(ContainerWhitespace(100f, 200f, 300f, 400f), false),
            actual = getUserOffsetBounds(ContainerWhitespace(300f, 200f, 100f, 400f), true),
        )
    }

    data class ZoomableCoreProperties(
        val rotation: Int,
        val containerSize: String,
        val contentSize: String,
        val contentOriginSize: String,
        val contentScale: String,
        val alignment: String,
        val readMode: String,
        val scalesCalculator: String,
        val threeStepScale: Boolean,
        val rubberBandScale: Boolean,
        val oneFingerScaleSpec: String,
        val animationSpec: String,
        val limitOffsetWithinBaseVisibleRect: Boolean,
        val containerWhitespaceMultiple: String,
        val containerWhitespace: String,
        val keepTransformWhenSameAspectRatioContentSizeChanged: Boolean,
        val baseTransform: String,
        val userTransform: String,
        val transform: String,
        val minScale: String,
        val mediumScale: String,
        val maxScale: String,
        val contentBaseDisplayRect: String,
        val contentBaseVisibleRect: String,
        val contentDisplayRect: String,
        val contentVisibleRect: String,
        val scrollEdge: String,
        val userOffsetBoundsRect: String,
        val continuousTransformType: Int,
    )

    private fun ZoomableCore.toProperties(): ZoomableCoreProperties = ZoomableCoreProperties(
        rotation = rotation,
        containerSize = containerSize.toShortString(),
        contentSize = contentSize.toShortString(),
        contentOriginSize = contentOriginSize.toShortString(),
        contentScale = contentScale.name,
        alignment = alignment.name,
        readMode = readMode.toString(),
        scalesCalculator = scalesCalculator.toString(),
        threeStepScale = threeStepScale,
        rubberBandScale = rubberBandScale,
        oneFingerScaleSpec = oneFingerScaleSpec.toString(),
        animationSpec = animationSpec.toString(),
        limitOffsetWithinBaseVisibleRect = limitOffsetWithinBaseVisibleRect,
        containerWhitespaceMultiple = containerWhitespaceMultiple.format(2).toString(),
        containerWhitespace = containerWhitespace.toShortString(),
        keepTransformWhenSameAspectRatioContentSizeChanged = keepTransformWhenSameAspectRatioContentSizeChanged,
        baseTransform = baseTransform.toShortString(),
        userTransform = userTransform.toShortString(),
        transform = transform.toShortString(),
        minScale = minScale.format(2).toString(),
        mediumScale = mediumScale.format(2).toString(),
        maxScale = maxScale.format(2).toString(),
        contentBaseDisplayRect = contentBaseDisplayRect.toShortString(),
        contentBaseVisibleRect = contentBaseVisibleRect.toShortString(),
        contentDisplayRect = contentDisplayRect.toShortString(),
        contentVisibleRect = contentVisibleRect.toShortString(),
        scrollEdge = scrollEdge.toShortString(),
        userOffsetBoundsRect = userOffsetBoundsRect.toShortString(),
        continuousTransformType = continuousTransformType
    )
}