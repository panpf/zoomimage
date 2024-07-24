package com.github.panpf.zoomimage.core.common.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.internal.calculatePreferredTileSize
import com.github.panpf.zoomimage.subsampling.internal.calculateTileGridMap
import com.github.panpf.zoomimage.subsampling.internal.canUseSubsamplingByAspectRatio
import com.github.panpf.zoomimage.subsampling.internal.toIntroString
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.div
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SubsamplingsCommonTest {

    @Test
    fun testCanUseSubsamplingByAspectRatio() {
        val imageSize = IntSizeCompat(1000, 2000)

        listOf(
            (ScaleFactorCompat(17f, 17f) to null as Float?) to true,
            (ScaleFactorCompat(17f, 17.5f) to null as Float?) to true,
            (ScaleFactorCompat(17f, 17.93f) to null as Float?) to true,
            (ScaleFactorCompat(17f, 17.94f) to null as Float?) to false,
            (ScaleFactorCompat(17f, 17.94f) to 1.1f) to true,
            (ScaleFactorCompat(17.5f, 17f) to null as Float?) to true,
            (ScaleFactorCompat(18.01f, 17f) to null as Float?) to true,
            (ScaleFactorCompat(18.02f, 17f) to null as Float?) to false,
            (ScaleFactorCompat(18.02f, 17f) to 1.2f) to true,
        ).forEachIndexed { index, it ->
            val (scaleFactor, minDifference) = it.first
            val expected = it.second
            if (minDifference != null) {
                assertEquals(
                    expected = expected,
                    actual = canUseSubsamplingByAspectRatio(
                        imageSize = imageSize,
                        thumbnailSize = imageSize / scaleFactor,
                        maxDifference = minDifference
                    ),
                    message = "item=${index + 1}, scaleFactor=$scaleFactor, minDifference=$minDifference"
                )
            } else {
                assertEquals(
                    expected = expected,
                    actual = canUseSubsamplingByAspectRatio(
                        imageSize = imageSize,
                        thumbnailSize = imageSize / scaleFactor
                    ),
                    message = "item=${index + 1}, scaleFactor=$scaleFactor, minDifference=$minDifference"
                )
            }
        }

        assertFalse(
            canUseSubsamplingByAspectRatio(
                imageSize = IntSizeCompat(0, 2000),
                thumbnailSize = IntSizeCompat(500, 1000),
            )
        )
        assertFalse(
            canUseSubsamplingByAspectRatio(
                imageSize = IntSizeCompat(1000, 0),
                thumbnailSize = IntSizeCompat(500, 1000),
            )
        )
        assertFalse(
            canUseSubsamplingByAspectRatio(
                imageSize = IntSizeCompat(1000, 2000),
                thumbnailSize = IntSizeCompat(0, 1000),
            )
        )
        assertFalse(
            canUseSubsamplingByAspectRatio(
                imageSize = IntSizeCompat(1000, 2000),
                thumbnailSize = IntSizeCompat(500, 0),
            )
        )

        assertFalse(
            canUseSubsamplingByAspectRatio(
                imageSize = IntSizeCompat(1000, 2000),
                thumbnailSize = IntSizeCompat(1001, 1000),
            )
        )
        assertFalse(
            canUseSubsamplingByAspectRatio(
                imageSize = IntSizeCompat(1000, 2000),
                thumbnailSize = IntSizeCompat(500, 20001),
            )
        )
    }

    @Test
    fun testToIntroString() {
        val imageSize = IntSizeCompat(8000, 8000)
        val containerSize = IntSizeCompat(1080, 1920)
        val preferredTileSize = containerSize / 2
        calculateTileGridMap(
            imageSize = imageSize,
            preferredTileSize = preferredTileSize,
        ).apply {
            assertEquals("[16:1:1x1,8:4:2x2,4:12:4x3,2:40:8x5,1:135:15x9]", toIntroString())
        }
    }

    @Test
    fun testCalculatePreferredTileSize() {
        assertEquals(
            /* expected = */ IntSizeCompat(1080, 1920) / 2,
            /* actual = */ calculatePreferredTileSize(IntSizeCompat(1080, 1920))
        )

        assertEquals(
            /* expected = */ IntSizeCompat(1000, 2000) / 2,
            /* actual = */ calculatePreferredTileSize(IntSizeCompat(1000, 2000))
        )
    }
}