package com.github.panpf.zoomimage.compose.common.test.util

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.util.roundToCompat
import com.github.panpf.zoomimage.compose.util.roundToPlatform
import com.github.panpf.zoomimage.compose.util.toCompat
import com.github.panpf.zoomimage.compose.util.toCompatOffset
import com.github.panpf.zoomimage.compose.util.toCompatRect
import com.github.panpf.zoomimage.compose.util.toCompatSize
import com.github.panpf.zoomimage.compose.util.toPlatform
import com.github.panpf.zoomimage.compose.util.toPlatformOffset
import com.github.panpf.zoomimage.compose.util.toPlatformRect
import com.github.panpf.zoomimage.compose.util.toPlatformSize
import com.github.panpf.zoomimage.compose.zoom.Transform
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ComposeConvertUtilsTest {

    @Test
    fun testContentScaleToCompat() {
        assertEquals(
            expected = ContentScaleCompat.Fit,
            actual = ContentScale.Fit.toCompat()
        )
        assertEquals(
            expected = ContentScaleCompat.FillBounds,
            actual = ContentScale.FillBounds.toCompat()
        )
        assertEquals(
            expected = ContentScaleCompat.FillWidth,
            actual = ContentScale.FillWidth.toCompat()
        )
        assertEquals(
            expected = ContentScaleCompat.FillHeight,
            actual = ContentScale.FillHeight.toCompat()
        )
        assertEquals(
            expected = ContentScaleCompat.Crop,
            actual = ContentScale.Crop.toCompat()
        )
        assertEquals(
            expected = ContentScaleCompat.Inside,
            actual = ContentScale.Inside.toCompat()
        )
        assertEquals(
            expected = ContentScaleCompat.None,
            actual = ContentScale.None.toCompat()
        )
        assertFailsWith(IllegalArgumentException::class) {
            object : ContentScale {
                override fun computeScaleFactor(srcSize: Size, dstSize: Size): ScaleFactor {
                    return ScaleFactor(1f, 1f)
                }
            }.toCompat()
        }
    }

    @Test
    fun testContentScaleCompatToPlatform() {
        assertEquals(
            expected = ContentScale.Fit,
            actual = ContentScaleCompat.Fit.toPlatform()
        )
        assertEquals(
            expected = ContentScale.FillBounds,
            actual = ContentScaleCompat.FillBounds.toPlatform()
        )
        assertEquals(
            expected = ContentScale.FillWidth,
            actual = ContentScaleCompat.FillWidth.toPlatform()
        )
        assertEquals(
            expected = ContentScale.FillHeight,
            actual = ContentScaleCompat.FillHeight.toPlatform()
        )
        assertEquals(
            expected = ContentScale.Crop,
            actual = ContentScaleCompat.Crop.toPlatform()
        )
        assertEquals(
            expected = ContentScale.Inside,
            actual = ContentScaleCompat.Inside.toPlatform()
        )
        assertEquals(
            expected = ContentScale.None,
            actual = ContentScaleCompat.None.toPlatform()
        )
        assertFailsWith(IllegalArgumentException::class) {
            object : ContentScaleCompat {
                override fun computeScaleFactor(
                    srcSize: SizeCompat,
                    dstSize: SizeCompat
                ): ScaleFactorCompat {
                    return ScaleFactorCompat(1f, 1f)
                }
            }.toPlatform()
        }
    }

    @Test
    fun testAlignmentToCompat() {
        assertEquals(
            expected = AlignmentCompat.TopStart,
            actual = Alignment.TopStart.toCompat()
        )
        assertEquals(
            expected = AlignmentCompat.TopCenter,
            actual = Alignment.TopCenter.toCompat()
        )
        assertEquals(
            expected = AlignmentCompat.TopEnd,
            actual = Alignment.TopEnd.toCompat()
        )
        assertEquals(
            expected = AlignmentCompat.CenterStart,
            actual = Alignment.CenterStart.toCompat()
        )
        assertEquals(
            expected = AlignmentCompat.Center,
            actual = Alignment.Center.toCompat()
        )
        assertEquals(
            expected = AlignmentCompat.CenterEnd,
            actual = Alignment.CenterEnd.toCompat()
        )
        assertEquals(
            expected = AlignmentCompat.BottomStart,
            actual = Alignment.BottomStart.toCompat()
        )
        assertEquals(
            expected = AlignmentCompat.BottomCenter,
            actual = Alignment.BottomCenter.toCompat()
        )
        assertEquals(
            expected = AlignmentCompat.BottomEnd,
            actual = Alignment.BottomEnd.toCompat()
        )
        assertFailsWith(IllegalArgumentException::class) {
            Alignment { _, _, _ -> IntOffset(0, 0) }.toCompat()
        }
    }

    @Test
    fun testAlignmentCompatToPlatform() {
        assertEquals(
            expected = Alignment.TopStart,
            actual = AlignmentCompat.TopStart.toPlatform()
        )
        assertEquals(
            expected = Alignment.TopCenter,
            actual = AlignmentCompat.TopCenter.toPlatform()
        )
        assertEquals(
            expected = Alignment.TopEnd,
            actual = AlignmentCompat.TopEnd.toPlatform()
        )
        assertEquals(
            expected = Alignment.CenterStart,
            actual = AlignmentCompat.CenterStart.toPlatform()
        )
        assertEquals(
            expected = Alignment.Center,
            actual = AlignmentCompat.Center.toPlatform()
        )
        assertEquals(
            expected = Alignment.CenterEnd,
            actual = AlignmentCompat.CenterEnd.toPlatform()
        )
        assertEquals(
            expected = Alignment.BottomStart,
            actual = AlignmentCompat.BottomStart.toPlatform()
        )
        assertEquals(
            expected = Alignment.BottomCenter,
            actual = AlignmentCompat.BottomCenter.toPlatform()
        )
        assertEquals(
            expected = Alignment.BottomEnd,
            actual = AlignmentCompat.BottomEnd.toPlatform()
        )
        assertFailsWith(IllegalArgumentException::class) {
            AlignmentCompat { _, _, _ -> IntOffsetCompat(0, 0) }.toPlatform()
        }
    }

    @Test
    fun testSizeToCompat() {
        assertEquals(
            expected = SizeCompat(111.53f, 759.35f),
            actual = Size(111.53f, 759.35f).toCompat()
        )
        assertEquals(
            expected = SizeCompat.Unspecified,
            actual = Size.Unspecified.toCompat()
        )
    }

    @Test
    fun testSizeRoundToCompat() {
        assertEquals(
            expected = IntSizeCompat(112, 759),
            actual = Size(111.53f, 759.35f).roundToCompat()
        )
        assertEquals(
            expected = IntSizeCompat.Zero,
            actual = Size.Unspecified.roundToCompat()
        )
    }

    @Test
    fun testIntSizeToCompat() {
        assertEquals(
            expected = IntSizeCompat(111, 759),
            actual = IntSize(111, 759).toCompat()
        )
    }

    @Test
    fun testIntSizeToCompatSize() {
        assertEquals(
            expected = SizeCompat(111f, 759f),
            actual = IntSize(111, 759).toCompatSize()
        )
    }

    @Test
    fun testSizeCompatToPlatform() {
        assertEquals(
            expected = Size(111.53f, 759.35f),
            actual = SizeCompat(111.53f, 759.35f).toPlatform()
        )
        assertEquals(
            expected = Size.Unspecified,
            actual = SizeCompat.Unspecified.toPlatform()
        )
    }

    @Test
    fun testSizeCompatRoundToPlatform() {
        assertEquals(
            expected = IntSize(112, 759),
            actual = SizeCompat(111.53f, 759.35f).roundToPlatform()
        )
        assertEquals(
            expected = IntSize.Zero,
            actual = SizeCompat.Unspecified.roundToPlatform()
        )
    }

    @Test
    fun testIntSizeCompatToPlatform() {
        assertEquals(
            expected = IntSize(111, 759),
            actual = IntSizeCompat(111, 759).toPlatform()
        )
    }

    @Test
    fun testIntSizeCompatToPlatformSize() {
        assertEquals(
            expected = Size(111f, 759f),
            actual = IntSizeCompat(111, 759).toPlatformSize()
        )
    }

    @Test
    fun testRectToCompat() {
        assertEquals(
            expected = RectCompat(111.53f, 759.35f, 593.67f, 935.31f),
            actual = Rect(111.53f, 759.35f, 593.67f, 935.31f).toCompat()
        )
    }

    @Test
    fun testRectRoundToCompat() {
        assertEquals(
            expected = IntRectCompat(112, 759, 594, 935),
            actual = Rect(111.53f, 759.35f, 593.67f, 935.31f).roundToCompat()
        )
    }

    @Test
    fun testIntRectToCompat() {
        assertEquals(
            expected = IntRectCompat(111, 759, 593, 935),
            actual = IntRect(111, 759, 593, 935).toCompat()
        )
    }

    @Test
    fun testIntRectToCompatRect() {
        assertEquals(
            expected = RectCompat(111f, 759f, 593f, 935f),
            actual = IntRect(111, 759, 593, 935).toCompatRect()
        )
    }

    @Test
    fun testRectCompatToPlatform() {
        assertEquals(
            expected = Rect(111.53f, 759.35f, 593.67f, 935.31f),
            actual = RectCompat(111.53f, 759.35f, 593.67f, 935.31f).toPlatform()
        )
    }

    @Test
    fun testRectCompatRoundToPlatform() {
        assertEquals(
            expected = IntRect(112, 759, 594, 935),
            actual = RectCompat(111.53f, 759.35f, 593.67f, 935.31f).roundToPlatform()
        )
    }

    @Test
    fun testIntRectCompatToPlatform() {
        assertEquals(
            expected = IntRect(111, 759, 593, 935),
            actual = IntRectCompat(111, 759, 593, 935).toPlatform()
        )
    }

    @Test
    fun testIntRectCompatToPlatformRect() {
        assertEquals(
            expected = Rect(111f, 759f, 593f, 935f),
            actual = IntRectCompat(111, 759, 593, 935).toPlatformRect()
        )
    }

    @Test
    fun testOffsetToCompat() {
        assertEquals(
            expected = OffsetCompat(111.53f, 759.35f),
            actual = Offset(111.53f, 759.35f).toCompat()
        )
        assertEquals(
            expected = OffsetCompat.Unspecified,
            actual = Offset.Unspecified.toCompat()
        )
    }

    @Test
    fun testOffsetRoundToCompat() {
        assertEquals(
            expected = IntOffsetCompat(112, 759),
            actual = Offset(111.53f, 759.35f).roundToCompat()
        )
        assertEquals(
            expected = IntOffsetCompat.Zero,
            actual = Offset.Unspecified.roundToCompat()
        )
    }

    @Test
    fun testIntOffsetToCompat() {
        assertEquals(
            expected = IntOffsetCompat(111, 759),
            actual = IntOffset(111, 759).toCompat()
        )
    }

    @Test
    fun testIntOffsetToCompatOffset() {
        assertEquals(
            expected = OffsetCompat(111f, 759f),
            actual = IntOffset(111, 759).toCompatOffset()
        )
    }

    @Test
    fun testOffsetCompatToPlatform() {
        assertEquals(
            expected = Offset(111.53f, 759.35f),
            actual = OffsetCompat(111.53f, 759.35f).toPlatform()
        )
        assertEquals(
            expected = Offset.Unspecified,
            actual = OffsetCompat.Unspecified.toPlatform()
        )
    }

    @Test
    fun testOffsetCompatRoundToPlatform() {
        assertEquals(
            expected = IntOffset(112, 759),
            actual = OffsetCompat(111.53f, 759.35f).roundToPlatform()
        )
        assertEquals(
            expected = IntOffset.Zero,
            actual = OffsetCompat.Unspecified.roundToPlatform()
        )
    }

    @Test
    fun testIntOffsetCompatToPlatform() {
        assertEquals(
            expected = IntOffset(111, 759),
            actual = IntOffsetCompat(111, 759).toPlatform()
        )
    }

    @Test
    fun testIntOffsetCompatToPlatformOffset() {
        assertEquals(
            expected = Offset(111f, 759f),
            actual = IntOffsetCompat(111, 759).toPlatformOffset()
        )
    }

    @Test
    fun testTransformOriginToCompat() {
        assertEquals(
            expected = TransformOriginCompat(111.53f, 759.35f),
            actual = TransformOrigin(111.53f, 759.35f).toCompat()
        )
    }

    @Test
    fun testTransformOriginCompatToPlatform() {
        assertEquals(
            expected = TransformOrigin(111.53f, 759.35f),
            actual = TransformOriginCompat(111.53f, 759.35f).toPlatform()
        )
    }

    @Test
    fun testScaleFactorToCompat() {
        assertEquals(
            expected = ScaleFactorCompat(111.53f, 759.35f),
            actual = ScaleFactor(111.53f, 759.35f).toCompat()
        )
    }

    @Test
    fun testScaleFactorCompatToPlatform() {
        assertEquals(
            expected = ScaleFactor(111.53f, 759.35f),
            actual = ScaleFactorCompat(111.53f, 759.35f).toPlatform()
        )
    }

    @Test
    fun testTransformToCompat() {
        assertEquals(
            expected = TransformCompat(
                scale = ScaleFactorCompat(1.3f, 4.7f),
                offset = OffsetCompat(156f, 97f),
                rotation = 270f,
                scaleOrigin = TransformOriginCompat.Center,
                rotationOrigin = TransformOriginCompat.Center,
            ),
            actual = Transform(
                scale = ScaleFactor(scaleX = 1.3f, scaleY = 4.7f),
                offset = Offset(156f, 97f),
                rotation = 270f,
                scaleOrigin = TransformOrigin.Center,
                rotationOrigin = TransformOrigin.Center,
            ).toCompat()
        )
    }

    @Test
    fun testTransformCompatToPlatform() {
        assertEquals(
            expected = Transform(
                scale = ScaleFactor(1.3f, 4.7f),
                offset = Offset(156f, 97f),
                rotation = 270f,
                scaleOrigin = TransformOrigin.Center,
                rotationOrigin = TransformOrigin.Center,
            ),
            actual = TransformCompat(
                scale = ScaleFactorCompat(scaleX = 1.3f, scaleY = 4.7f),
                offset = OffsetCompat(156f, 97f),
                rotation = 270f,
                scaleOrigin = TransformOriginCompat.Center,
                rotationOrigin = TransformOriginCompat.Center,
            ).toPlatform()
        )
    }
}