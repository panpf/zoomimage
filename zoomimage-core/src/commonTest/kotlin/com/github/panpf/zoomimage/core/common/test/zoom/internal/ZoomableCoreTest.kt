package com.github.panpf.zoomimage.core.common.test.zoom.internal

import com.github.panpf.zoomimage.test.TestAnimationAdapter
import com.github.panpf.zoomimage.test.allFold
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.internal.ZoomableCore
import com.github.panpf.zoomimage.zoom.name
import com.github.panpf.zoomimage.zoom.toShortString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ZoomableCoreTest {

    @Test
    fun testConstructor() {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            rtlLayoutDirection = false,
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
            rtlLayoutDirection = false,
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
            rtlLayoutDirection = false,
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
        val bigScale9 = ScaleFactorCompat(3.3f, 3.32f)
        withContext(Dispatchers.Main) {
            zoomableCore.setKeepTransformWhenSameAspectRatioContentSizeChanged(true)
            zoomableCore.setContentSize(contentSize.toSize().times(bigScale9).round())
        }
        val properties9 = zoomableCore.toProperties()
        assertNotEquals(properties4, properties9)
        assertEquals(expected = "1650x996", actual = properties9.contentSize)
        assertEquals(true, properties9.keepTransformWhenSameAspectRatioContentSizeChanged)
        assertEquals("(0.65x0.65,0.0x634.0,0.0,0.0x0.0,0.76x0.26)", properties9.baseTransform)
        assertEquals("(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)", properties9.userTransform)
        assertEquals("(0.65x0.65,0.0x634.0,0.0,0.0x0.0,0.76x0.26)", properties9.transform)
        assertEquals(expected = "0.65", actual = properties9.minScale)
        assertEquals(expected = "1.96", actual = properties9.mediumScale)
        assertEquals(expected = "5.89", actual = properties9.maxScale)
        assertEquals("[0.0x634.0,1080.0x1285.93]", properties9.contentBaseDisplayRect)
        assertEquals("[0.0x0.0,1650.0x996.0]", properties9.contentBaseVisibleRect)
        assertEquals("[0.0x634.0,1080.0x1286.0]", properties9.contentDisplayRect)
        assertEquals("[-0.0x0.0,1650.0x996.0]", properties9.contentVisibleRect)
        assertEquals("(BOTH,BOTH)", properties9.scrollEdge)
        assertEquals("[0.0x0.04,0.0x0.04]", properties9.userOffsetBoundsRect)
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
        assertEquals("825.0x498.0", contentVisibleCenter9.toShortString())
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
            rtlLayoutDirection = false,
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
            rtlLayoutDirection = false,
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
            rtlLayoutDirection = false,
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

    @Test
    fun testSetReadMode() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            rtlLayoutDirection = false,
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
            rtlLayoutDirection = false,
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
    fun testSetThreeStepScale() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            rtlLayoutDirection = false,
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
            zoomableCore.setThreeStepScale(false)
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
            assertEquals(false, this.threeStepScale)
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
        assertEquals(
            expected = "0.22, 6.4, 0.22, 6.4",
            actual = listOf(
                withContext(Dispatchers.Main) {
                    zoomableCore.switchScale()
                    zoomableCore.transform.scaleX.format(2)
                },
                withContext(Dispatchers.Main) {
                    zoomableCore.switchScale()
                    zoomableCore.transform.scaleX.format(2)
                },
                withContext(Dispatchers.Main) {
                    zoomableCore.switchScale()
                    zoomableCore.transform.scaleX.format(2)
                },
                withContext(Dispatchers.Main) {
                    zoomableCore.switchScale()
                    zoomableCore.transform.scaleX.format(2)
                },
            ).joinToString()
        )

        withContext(Dispatchers.Main) {
            zoomableCore.setThreeStepScale(true)
        }
        zoomableCore.toProperties().apply {
//            assertEquals("", this.toFormattedString())
            assertEquals(true, this.threeStepScale)
            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
            assertEquals("(29.63x29.63,-15460.0x-27496.0,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
            assertEquals("[-15460.0x0.29,16540.0x1926.22]", this.contentDisplayRect)
            assertEquals("[2415.62x0.0,2584.38x299.95]", this.contentVisibleRect)
            assertEquals("0.22", this.minScale)
            assertEquals("6.4", this.mediumScale)
            assertEquals("19.2", this.maxScale)
        }
        assertEquals(
            expected = "19.2, 0.22, 6.4, 19.2",
            actual = listOf(
                withContext(Dispatchers.Main) {
                    zoomableCore.switchScale()
                    zoomableCore.transform.scaleX.format(2)
                },
                withContext(Dispatchers.Main) {
                    zoomableCore.switchScale()
                    zoomableCore.transform.scaleX.format(2)
                },
                withContext(Dispatchers.Main) {
                    zoomableCore.switchScale()
                    zoomableCore.transform.scaleX.format(2)
                },
                withContext(Dispatchers.Main) {
                    zoomableCore.switchScale()
                    zoomableCore.transform.scaleX.format(2)
                },
            ).joinToString()
        )
    }

    @Test
    fun testSetRubberBandScale() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            rtlLayoutDirection = false,
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
            rtlLayoutDirection = false,
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
        // TODO 没必要单独测，融合在 scale、offset、locate 等方法中测试
//        val zoomableCore = ZoomableCore(
//            logger = Logger(tag = "Test"),
//            module = "ZoomableCoreTest",
//            rtlLayoutDirection = false,
//            animationAdapter = TestAnimationAdapter(),
//            onTransformChanged = { }
//        )
//        assertEquals("null", zoomableCore.animationSpec.toString())
//
//        withContext(Dispatchers.Main) {
//            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
//            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
//        }
//
//        val scaleStartTime = TimeSource.Monotonic.markNow()
//        withContext(Dispatchers.Main) {
//            zoomableCore.scale(
//                targetScale = zoomableCore.maxScale,
//                animationSpec = null,
//                animated = true
//            )
//        }
//        val scaleElapsedTime = scaleStartTime.elapsedNow().inWholeMilliseconds
//        assertTrue(scaleElapsedTime in 300L..350L, message = "elapsedTime=$scaleElapsedTime")
//
//        val switchScaleStartTime = TimeSource.Monotonic.markNow()
//        withContext(Dispatchers.Main) {
//            zoomableCore.scale(
//                targetScale = zoomableCore.maxScale,
//                animationSpec = null,
//                animated = true
//            )
//        }
//        val switchScaleElapsedTime = switchScaleStartTime.elapsedNow().inWholeMilliseconds
//        assertTrue(switchScaleElapsedTime in 300L..350L, message = "elapsedTime=$switchScaleElapsedTime")
//
//        withContext(Dispatchers.Main) {
//            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
//            zoomableCore.setContentSize(IntSizeCompat(5000, 300))
//            zoomableCore.locate(
//                contentPoint = OffsetCompat(
//                    x = zoomableCore.contentSize.width * 0.7f,
//                    y = zoomableCore.contentSize.height * 0.7f
//                ),
//                targetScale = zoomableCore.maxScale,
//                animated = false
//            )
//        }
//
//        val startTime = TimeSource.Monotonic.markNow()
//        withContext(Dispatchers.Main) {
//            zoomableCore.locate(
//                contentPoint = OffsetCompat(
//                    x = zoomableCore.contentSize.width * 0.2f,
//                    y = zoomableCore.contentSize.height * 0.2f
//                ),
//                targetScale = zoomableCore.maxScale,
//                animated = true
//            )
//        }
//        zoomableCore.toProperties().apply {
//            assertNotEquals("", this.toFormattedString())
//            assertEquals("null", this.animationSpec)
//            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
//            assertEquals("(88.89x88.89,-18660.0x-82680.9,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
//            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
//            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
//            assertEquals("[-18660.0x-192.01,77340.01x5585.77]", this.contentDisplayRect)
//            assertEquals("[971.87x10.0,1028.12x110.0]", this.contentVisibleRect)
//            assertEquals("0.22", this.minScale)
//            assertEquals("6.4", this.mediumScale)
//            assertEquals("19.2", this.maxScale)
//        }
//        val elapsedTime = startTime.elapsedNow().inWholeMilliseconds
//        assertTrue(elapsedTime in 300L..350L, message = "elapsedTime=$elapsedTime")
//
//        val startTime2 = TimeSource.Monotonic.markNow()
//        withContext(Dispatchers.Main) {
//            zoomableCore.setAnimationSpec(TestZoomAnimationSpec(500))
//            zoomableCore.locate(
//                contentPoint = OffsetCompat(
//                    x = zoomableCore.contentSize.width * 0.7f,
//                    y = zoomableCore.contentSize.height * 0.7f
//                ),
//                targetScale = zoomableCore.maxScale,
//                animated = true
//            )
//        }
//        assertEquals(TestZoomAnimationSpec(500).toString(), zoomableCore.animationSpec.toString())
//        zoomableCore.toProperties().apply {
////            assertEquals("", this.toFormattedString())
//            assertEquals(TestZoomAnimationSpec(500).toString(), this.animationSpec)
//            assertEquals("(0.22x0.22,0.0x928.0,0.0,0.0x0.0,2.31x0.08)", this.baseTransform)
//            assertEquals("(88.89x88.89,-66660.0x-85560.89,0.0,0.0x0.0,0.0x0.0)", this.userTransform)
//            assertEquals("[0.0x928.0,1080.0x992.8]", this.contentBaseDisplayRect)
//            assertEquals("[0.0x0.0,5000.0x300.0]", this.contentBaseVisibleRect)
//            assertEquals("[-66660.0x-3072.0,29340.01x2705.78]", this.contentDisplayRect)
//            assertEquals("[3471.87x160.0,3528.12x260.0]", this.contentVisibleRect)
//            assertEquals("0.22", this.minScale)
//            assertEquals("6.4", this.mediumScale)
//            assertEquals("19.2", this.maxScale)
//        }
//        val elapsedTime2 = startTime2.elapsedNow().inWholeMilliseconds
//        assertTrue(elapsedTime2 in 500L..550L, message = "elapsedTime=$elapsedTime2")
    }

    @Test
    fun testSetLimitOffsetWithinBaseVisibleRect() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            rtlLayoutDirection = false,
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
        // TODO test setContainerWhitespaceMultiple
    }

    @Test
    fun testSetContainerWhitespace() = runTest {
        // TODO test setContainerWhitespace
    }

    @Test
    fun testSetKeepTransformWhenSameAspectRatioContentSizeChanged() = runTest {
        // TODO test setKeepTransformWhenSameAspectRatioContentSizeChanged
    }

    @Test
    fun testScale() = runTest {
        // TODO test scale
    }

    @Test
    fun testSwitchScale() = runTest {
        // TODO test switchScale
    }

    @Test
    fun testOffset() = runTest {
        // TODO test offset
    }

    @Test
    fun testLocate() = runTest {
        // TODO test locate
    }

    @Test
    fun testRotate() = runTest {
        // TODO test rotate
    }

    @Test
    fun testGetNextStepScale() = runTest {
        // TODO test getNextStepScale
    }

    @Test
    fun testTouchPointToContentPoint() = runTest {
        // TODO test touchPointToContentPoint
    }

    @Test
    fun testCanScroll() = runTest {
        // TODO test canScroll
    }

    @Test
    fun testReset() = runTest {
        val zoomableCore = ZoomableCore(
            logger = Logger(tag = "Test"),
            module = "ZoomableCoreTest",
            rtlLayoutDirection = false,
            animationAdapter = TestAnimationAdapter(),
            onTransformChanged = {}
        )
        val properties = zoomableCore.toProperties()

        // TODO Initial state
        // TODO Full state
        // TODO isNotChanged

        // Reset without any changes
        withContext(Dispatchers.Main) {
            zoomableCore.reset("testReset")
        }
        val properties2 = zoomableCore.toProperties()
        assertEquals(expected = properties, actual = properties2)

        // Set containerSize value
        withContext(Dispatchers.Main) {
            zoomableCore.setContainerSize(IntSizeCompat(1080, 1920))
        }
        val properties3 = zoomableCore.toProperties()
        assertNotEquals(illegal = properties2, actual = properties3)
        assertEquals(expected = "1080x1920", actual = properties3.containerSize)
        assertEquals(
            expected = properties2,
            actual = properties3.copy(containerSize = properties2.containerSize)
        )

        // Set contentSize value
        withContext(Dispatchers.Main) {
            zoomableCore.setContentSize(IntSizeCompat(500, 300))
        }
        val properties4 = zoomableCore.toProperties()
        assertNotEquals(illegal = properties3, actual = properties4)
        assertEquals(expected = "500x300", actual = properties4.contentSize)
        assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", properties4.baseTransform)
        assertEquals("(2.16x2.16,0.0x636.0,0.0,0.0x0.0,0.23x0.08)", properties4.transform)
        assertEquals(expected = "2.16", actual = properties4.minScale)
        assertEquals(expected = "6.48", actual = properties4.mediumScale)
        assertEquals(expected = "19.44", actual = properties4.maxScale)
        assertEquals("[0.0x636.0,1080.0x1284.0]", properties4.contentBaseDisplayRect)
        assertEquals("[0.0x0.0,500.0x300.0]", properties4.contentBaseVisibleRect)
        assertEquals("[0.0x636.0,1080.0x1284.0]", properties4.contentDisplayRect)
        assertEquals("[-0.0x0.0,500.0x300.0]", properties4.contentVisibleRect)
        assertEquals(
            expected = properties3,
            actual = properties4.copy(
                contentSize = properties3.contentSize,
                baseTransform = properties3.baseTransform,
                transform = properties3.transform,
                minScale = properties3.minScale,
                mediumScale = properties3.mediumScale,
                maxScale = properties3.maxScale,
                contentBaseDisplayRect = properties3.contentBaseDisplayRect,
                contentBaseVisibleRect = properties3.contentBaseVisibleRect,
                contentDisplayRect = properties3.contentDisplayRect,
                contentVisibleRect = properties3.contentVisibleRect,
            )
        )

        // Set contentOriginSize value
        withContext(Dispatchers.Main) {
            zoomableCore.setContentOriginSize(IntSizeCompat(40000, 24000))
        }
        val properties5 = zoomableCore.toProperties()
        assertNotEquals(illegal = properties4, actual = properties5)
        assertEquals(expected = "40000x24000", actual = properties5.contentOriginSize)
        assertEquals(expected = "2.16", actual = properties5.minScale)
        assertEquals(expected = "80.0", actual = properties5.mediumScale)
        assertEquals(expected = "240.0", actual = properties5.maxScale)
        assertEquals(
            expected = properties4,
            actual = properties5.copy(
                contentOriginSize = properties4.contentOriginSize,
                minScale = properties4.minScale,
                mediumScale = properties4.mediumScale,
                maxScale = properties4.maxScale,
            )
        )
    }

    @Test
    fun testStopAllAnimation() = runTest {
        // TODO test stopAllAnimation
    }

    @Test
    fun testRollbackScale() = runTest {
        // TODO test rollbackScale
    }

    @Test
    fun testGestureTransform() = runTest {
        // TODO test gestureTransform
    }

    @Test
    fun testFling() = runTest {
        // TODO test fling
    }

    @Test
    fun testSetContinuousTransformType() = runTest {
        // TODO test setContinuousTransformType
    }

    @Test
    fun testCheckSupportGestureType() = runTest {
        // TODO test checkSupportGestureType
    }

    @Test
    fun testRtlLayoutDirection() = runTest {
        // TODO test rtlLayoutDirection
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