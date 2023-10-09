package com.github.panpf.zoomimage.compose.test.internal

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.internal.TopStart
import com.github.panpf.zoomimage.compose.internal.div
import com.github.panpf.zoomimage.compose.internal.lerp
import com.github.panpf.zoomimage.compose.internal.times
import com.github.panpf.zoomimage.compose.internal.toShortString
import org.junit.Assert
import org.junit.Test

class ComposePlatformUtilsTransformOriginTest {

    @Test
    fun testToShortString() {
        Assert.assertEquals("0.34x0.57", TransformOrigin(0.342f, 0.567f).toShortString())
        Assert.assertEquals("0.57x0.34", TransformOrigin(0.567f, 0.342f).toShortString())
    }

    @Test
    fun testTopStart() {
        Assert.assertEquals(
            "0.0x0.0",
            TransformOrigin.TopStart.toShortString()
        )
        Assert.assertEquals(
            TransformOrigin.TopStart,
            TransformOrigin.TopStart
        )
    }

    @Test
    fun testTimes() {
        Assert.assertEquals(
            "1.13x1.87",
            (TransformOrigin(0.342f, 0.567f) * 3.3f).toShortString()
        )
        Assert.assertEquals(
            "1.81x3.01",
            (TransformOrigin(0.342f, 0.567f) * 5.3f).toShortString()
        )

        Assert.assertEquals(
            "251x221",
            (IntSize(735, 389) * TransformOrigin(0.342f, 0.567f)).toShortString()
        )
        Assert.assertEquals(
            "417x133",
            (IntSize(735, 389) * TransformOrigin(0.567f, 0.342f)).toShortString()
        )

        Assert.assertEquals(
            "251.54x220.96",
            (Size(735.5f, 389.7f) * TransformOrigin(0.342f, 0.567f)).toShortString()
        )
        Assert.assertEquals(
            "417.03x133.28",
            (Size(735.5f, 389.7f) * TransformOrigin(0.567f, 0.342f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        Assert.assertEquals(
            "0.34x0.57",
            (TransformOrigin(1.13f, 1.87f) / 3.3f).toShortString()
        )
        Assert.assertEquals(
            "0.21x0.35",
            (TransformOrigin(1.13f, 1.87f) / 5.3f).toShortString()
        )
    }

    @Test
    fun testLerp() {
        val origin1 = TransformOrigin(0.13f, 0.77f)
        val origin2 = TransformOrigin(0.77f, 0.13f)
        Assert.assertEquals(origin1, lerp(origin1, origin2, 0f))
        Assert.assertEquals(TransformOrigin(0.45f, 0.45f), lerp(origin1, origin2, 0.5f))
        Assert.assertEquals(origin2, lerp(origin1, origin2, 1f))
    }
}