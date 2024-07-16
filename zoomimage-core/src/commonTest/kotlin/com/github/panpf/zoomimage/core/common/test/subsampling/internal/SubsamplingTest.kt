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
import kotlin.test.assertTrue

class SubsamplingTest {

    @Test
    fun testCanUseSubsamplingByAspectRatio() {
        // TODO fix
        val imageSize = IntSizeCompat(1000, 2000)

        assertTrue(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17f, 17f))
        )
        assertTrue(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17f, 16.5f))
        )
        assertTrue(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17.3f, 17f))
        )
        assertFalse(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17f, 16.4f))
        )
        assertFalse(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17.6f, 17f))
        )
        assertTrue(
            canUseSubsamplingByAspectRatio(
                imageSize,
                imageSize / ScaleFactorCompat(17f, 16.4f),
                minDifference = 0.8f
            )
        )
        assertTrue(
            canUseSubsamplingByAspectRatio(
                imageSize,
                imageSize / ScaleFactorCompat(17.6f, 17f),
                minDifference = 0.8f
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