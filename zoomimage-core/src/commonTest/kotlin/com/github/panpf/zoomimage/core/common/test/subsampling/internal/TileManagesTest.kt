package com.github.panpf.zoomimage.core.common.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.subsampling.internal.calculateGridSize
import com.github.panpf.zoomimage.subsampling.internal.calculateImageLoadRect
import com.github.panpf.zoomimage.subsampling.internal.calculateMaxGridSize
import com.github.panpf.zoomimage.subsampling.internal.calculateTileGridMap
import com.github.panpf.zoomimage.subsampling.internal.calculateTiles
import com.github.panpf.zoomimage.subsampling.internal.closestPowerOfTwo
import com.github.panpf.zoomimage.subsampling.internal.findSampleSize
import com.github.panpf.zoomimage.subsampling.internal.toIntroString
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.toShortString
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TileManagesTest {

    @Test
    fun testClosestPowerOfTwo() {
        val result = (1..17)
            .flatMap { number -> buildList { repeat(10) { index -> add(number + (index * 0.1f)) } } }
            .map { scale -> scale to closestPowerOfTwo(scale) }
            .distinctBy { it.second }
            .map { "${it.first}:${it.second}" }
            .toString()
        assertEquals("[1.0:1, 1.5:2, 2.9:4, 5.7:8, 11.4:16]", result)
    }

    @Test
    fun testFindSampleSize() {
        val imageSize = IntSizeCompat(690, 12176)

        assertEquals(
            /* expected = */ 0,
            /* actual = */ findSampleSize(IntSizeCompat.Zero, imageSize / 16, 4f)
        )
        assertEquals(
            /* expected = */ 0,
            /* actual = */ findSampleSize(imageSize, IntSizeCompat.Zero, 4f)
        )
        assertEquals(
            /* expected = */ 0,
            /* actual = */ findSampleSize(imageSize, imageSize / 16, 0f)
        )
        assertEquals(
            /* expected = */ 128,
            /* actual = */ findSampleSize(imageSize, imageSize / 16, 0.1f)
        )
        assertEquals(
            /* expected = */ 16,
            /* actual = */ findSampleSize(imageSize, imageSize / 16, 0.9f)
        )

        /* 计算从 1.0，1.1，1.2 一直到 17.9 等缩放比例的 sampleSize，然后保留变化开始时的缩放比例 */
        listOf(
            imageSize.roundDiv(16f) to "[1.0:16, 1.5:8, 2.9:4, 5.7:2, 11.1:1]",
            imageSize.ceilDiv(16f) to "[1.0:16, 1.4:8, 2.8:4, 5.6:2, 10.9:1]",
            imageSize.floorDiv(16f) to "[1.0:16, 1.5:8, 2.9:4, 5.7:2, 11.1:1]",
        ).forEachIndexed { index, (thumbnailSize, excepted) ->
            val result = (1..17)
                .flatMap { number -> buildList { repeat(10) { index -> add(number + (index * 0.1f)) } } }
                .map { scale -> scale to findSampleSize(imageSize, thumbnailSize, scale) }
                .distinctBy { it.second }
                .map { "${it.first}:${it.second}" }
                .toString()
            assertEquals(
                expected = excepted,
                actual = result,
                message = "index=$index, thumbnailSize=${thumbnailSize.toShortString()}",
            )
        }
    }

    @Test
    fun testCalculateGridSize() {
        val containerSize = IntSizeCompat(1080, 1920)
        val imageSize = IntSizeCompat(9798, 6988)
        listOf(
            Item(
                imageSize = imageSize,
                preferredTileSize = containerSize / 2,
                sampleSize = 1,
                excepted = IntOffsetCompat(19, 8)
            ),
            Item(
                imageSize = imageSize,
                preferredTileSize = containerSize / 4,
                sampleSize = 1,
                excepted = IntOffsetCompat(37, 15)
            ),
            Item(
                imageSize = imageSize,
                preferredTileSize = containerSize / 6,
                sampleSize = 1,
                excepted = IntOffsetCompat(55, 22)
            ),
            Item(
                imageSize = imageSize,
                preferredTileSize = containerSize / 8,
                sampleSize = 1,
                excepted = IntOffsetCompat(73, 30)
            ),
            Item(
                imageSize = imageSize,
                preferredTileSize = containerSize / 2,
                sampleSize = 2,
                excepted = IntOffsetCompat(10, 4)
            ),
            Item(
                imageSize = imageSize,
                preferredTileSize = containerSize / 2,
                sampleSize = 4,
                excepted = IntOffsetCompat(5, 2)
            ),
            Item(
                imageSize = imageSize,
                preferredTileSize = containerSize / 2,
                sampleSize = 8,
                excepted = IntOffsetCompat(3, 1)
            ),
        ).forEachIndexed { index, item ->
            val result = calculateGridSize(item.imageSize, item.preferredTileSize, item.sampleSize)
            assertEquals(
                expected = item.excepted,
                actual = result,
                message = "index=$index, preferredTileSize=${item.preferredTileSize.toShortString()}, sampleSize=${item.sampleSize}",
            )
        }

        assertEquals(
            /* expected = */ IntOffsetCompat(3266, 999),
            /* actual = */ calculateGridSize(
                imageSize = IntSizeCompat(9798, 6988),
                preferredTileSize = IntSizeCompat(3, 7),
                sampleSize = 1
            )
        )

        assertEquals(
            /* expected = */ IntOffsetCompat(200, 150),
            /* actual = */ calculateGridSize(
                imageSize = IntSizeCompat(9798, 6988),
                preferredTileSize = IntSizeCompat(3, 7),
                sampleSize = 1,
                maxGridSize = IntOffsetCompat(200, 150)
            )
        )
    }

    private fun checkTiles(tileList: List<Tile>, imageSize: IntSizeCompat) {
        var minLeft = 0
        var minTop = 0
        var maxRight = 0
        var maxBottom = 0
        var lastTop = 0
        var lastRight = 0
        tileList.forEachIndexed { index, tile ->
            if (index == 0) {
                assertEquals(expected = 0, actual = tile.srcRect.left, message = "tile=$tile")
                assertEquals(expected = 0, actual = tile.srcRect.top, message = "tile=$tile")
            } else if (index == tileList.lastIndex) {
                assertEquals(
                    expected = imageSize.width - 1,
                    actual = tile.srcRect.right,
                    message = "index=$index, tile=$tile",
                )
                assertEquals(
                    expected = imageSize.height - 1,
                    actual = tile.srcRect.bottom,
                    message = "index=$index, tile=$tile",
                )
            }

            assertEquals(
                expected = lastRight,
                actual = tile.srcRect.left,
                message = "index=$index, tile=$tile"
            )
            assertEquals(
                expected = lastTop,
                actual = tile.srcRect.top,
                message = "index=$index, tile=$tile"
            )
            if (tile.srcRect.right >= imageSize.width - 1) {
                lastTop = tile.srcRect.bottom
                lastRight = 0
            } else {
                lastRight = tile.srcRect.right
            }

            minLeft = min(minLeft, tile.srcRect.left)
            minTop = min(minTop, tile.srcRect.top)
            maxRight = max(maxRight, tile.srcRect.right)
            maxBottom = max(maxBottom, tile.srcRect.bottom)
        }
        assertEquals(0, minLeft)
        assertEquals(0, minTop)
        assertEquals(imageSize.width - 1, maxRight)
        assertEquals(imageSize.height - 1, maxBottom)
    }

    @Test
    fun testCalculateTileGridMap() {
        val containerSize = IntSizeCompat(1080, 1920)
        listOf(
            Item(
                imageSize = IntSizeCompat(8000, 8000),
                preferredTileSize = containerSize / 2,
                excepted = "[16:1:1x1,8:4:2x2,4:12:4x3,2:40:8x5,1:135:15x9]"
            ),
            Item(
                imageSize = IntSizeCompat(8000, 8000),
                preferredTileSize = containerSize / 4,
                excepted = "[32:1:1x1,16:4:2x2,8:12:4x3,4:40:8x5,2:135:15x9,1:510:30x17]"
            ),
            Item(
                imageSize = IntSizeCompat(8000, 8000),
                preferredTileSize = containerSize / 6,
                excepted = "[64:1:1x1,32:2:2x1,16:6:3x2,8:24:6x4,4:84:12x7,2:299:23x13,1:1125:45x25]"
            ),

            Item(
                imageSize = IntSizeCompat(8000, 3000),
                preferredTileSize = containerSize / 2,
                excepted = "[16:1:1x1,8:2:2x1,4:4:4x1,2:16:8x2,1:60:15x4]"
            ),
            Item(
                imageSize = IntSizeCompat(3000, 8000),
                preferredTileSize = containerSize / 2,
                excepted = "[16:1:1x1,8:2:1x2,4:6:2x3,2:15:3x5,1:54:6x9]"
            ),
            Item(
                imageSize = IntSizeCompat(1500, 1500),
                preferredTileSize = containerSize / 2,
                excepted = "[4:1:1x1,2:2:2x1,1:6:3x2]"
            ),
            Item(
                imageSize = IntSizeCompat(1000, 1500),
                preferredTileSize = containerSize / 2,
                excepted = "[2:1:1x1,1:4:2x2]"
            ),
            Item(
                imageSize = IntSizeCompat(1500, 1000),
                preferredTileSize = containerSize / 2,
                excepted = "[4:1:1x1,2:2:2x1,1:6:3x2]"
            ),
            Item(
                imageSize = IntSizeCompat(1000, 1000),
                preferredTileSize = containerSize / 2,
                excepted = "[2:1:1x1,1:4:2x2]"
            ),
            Item(
                imageSize = IntSizeCompat(30000, 926),
                preferredTileSize = containerSize / 2,
                excepted = "[64:1:1x1,32:2:2x1,16:4:4x1,8:7:7x1,4:14:14x1,2:28:28x1,1:50:50x1]"
            ),
            Item(
                imageSize = IntSizeCompat(690, 12176),
                preferredTileSize = containerSize / 2,
                excepted = "[16:1:1x1,8:2:1x2,4:4:1x4,2:7:1x7,1:26:2x13]"
            ),
            Item(
                imageSize = IntSizeCompat(7557, 5669),
                preferredTileSize = containerSize / 2,
                excepted = "[16:1:1x1,8:2:2x1,4:8:4x2,2:21:7x3,1:84:14x6]"
            ),
            Item(
                imageSize = IntSizeCompat(9798, 6988),
                preferredTileSize = containerSize / 2,
                excepted = "[32:1:1x1,16:2:2x1,8:3:3x1,4:10:5x2,2:40:10x4,1:152:19x8]"
            ),
        ).forEachIndexed { index, item ->
            val result = calculateTileGridMap(
                imageSize = item.imageSize,
                preferredTileSize = item.preferredTileSize
            )
            assertEquals(
                expected = item.excepted,
                actual = result.toIntroString(),
                message = "index=$index, imageSize=${item.imageSize.toShortString()}, preferredTileSize=${item.preferredTileSize.toShortString()}",
            )
            result.forEach { checkTiles(it.tiles, item.imageSize) }
        }

        assertEquals(
            expected = "[4096:1:1x1,2048:2:2x1,1024:4:4x1,512:14:7x2,256:52:13x4,128:208:26x8,64:800:50x16,32:1600:50x32,16:1800:50x36,8:1800:50x36,4:1800:50x36,2:1800:50x36,1:1800:50x36]",
            actual = calculateTileGridMap(
                imageSize = IntSizeCompat(9798, 6988),
                preferredTileSize = IntSizeCompat(3, 7)
            ).toIntroString()
        )
        assertEquals(
            expected = "[4096:1:1x1,2048:2:1x2,1024:4:1x4,512:14:2x7,256:52:4x13,128:208:8x26,64:800:16x50,32:1600:32x50,16:1800:36x50,8:1800:36x50,4:1800:36x50,2:1800:36x50,1:1800:36x50]",
            actual = calculateTileGridMap(
                imageSize = IntSizeCompat(6988, 9798),
                preferredTileSize = IntSizeCompat(7, 3)
            ).toIntroString()
        )
    }

    @Test
    fun testCalculateMaxGridSize() {
        assertEquals(
            expected = IntOffsetCompat(x = 10, y = 8),
            actual = calculateMaxGridSize(IntSizeCompat(800, 600), singleDirectionMaxTiles = 10)
        )

        assertEquals(
            expected = IntOffsetCompat(x = 8, y = 10),
            actual = calculateMaxGridSize(IntSizeCompat(600, 800), singleDirectionMaxTiles = 10)
        )

        assertEquals(
            expected = IntOffsetCompat(x = 10, y = 10),
            actual = calculateMaxGridSize(IntSizeCompat(500, 500), singleDirectionMaxTiles = 10)
        )

        assertEquals(
            expected = IntOffsetCompat(x = 50, y = 1),
            actual = calculateMaxGridSize(IntSizeCompat(2561, 19), singleDirectionMaxTiles = 50)
        )

        assertEquals(
            expected = IntOffsetCompat(x = 1, y = 50),
            actual = calculateMaxGridSize(IntSizeCompat(19, 2561), singleDirectionMaxTiles = 50)
        )

        assertFailsWith(IllegalArgumentException::class) {
            calculateMaxGridSize(IntSizeCompat(800, 600), singleDirectionMaxTiles = 0)
        }

        assertFailsWith(IllegalArgumentException::class) {
            calculateMaxGridSize(IntSizeCompat(800, 600), singleDirectionMaxTiles = -10)
        }
    }

    @Test
    fun testCalculateTiles() {
        val imageSize = IntSizeCompat(999, 801)
        val gridSize = IntOffsetCompat(10, 8)
        val sampleSize = 2
        calculateTiles(imageSize, gridSize, sampleSize).apply {
            assertEquals(80, size)
            checkTiles(this, imageSize)
            assertTrue(this.all { it.sampleSize == sampleSize })
        }

        val imageSize2 = IntSizeCompat(999, 801)
        val gridSize2 = IntOffsetCompat(8, 10)
        val sampleSize2 = 2
        calculateTiles(imageSize2, gridSize2, sampleSize2).apply {
            assertEquals(80, size)
            checkTiles(this, imageSize2)
            assertTrue(this.all { it.sampleSize == sampleSize2 })
        }

        val imageSize3 = IntSizeCompat(999, 801)
        val gridSize3 = IntOffsetCompat(7, 7)
        val sampleSize3 = 4
        calculateTiles(imageSize3, gridSize3, sampleSize3).apply {
            assertEquals(49, size)
            checkTiles(this, imageSize3)
            assertTrue(this.all { it.sampleSize == sampleSize3 })
        }
    }

    @Test
    fun testCalculateImageLoadRect() {
        val imageSize = IntSizeCompat(1241, 3073)
        val preferredTileSize = IntSizeCompat(333, 111)

        val contentSize = imageSize / 8
        assertEquals(
            IntSizeCompat(155, 384),
            contentSize
        )

        val contentVisibleSize = IntSizeCompat(
            width = contentSize.width / 4,
            height = contentSize.width / 8,
        )
        assertEquals(
            IntSizeCompat(38, 19),
            contentVisibleSize
        )

        var contentVisibleRect =
            IntRectCompat(0, 0, contentVisibleSize.width, contentVisibleSize.height)
        assertEquals(
            IntRectCompat(0, 0, 472, 209),
            calculateImageLoadRect(imageSize, contentSize, preferredTileSize, contentVisibleRect)
        )

        assertEquals(
            IntRectCompat.Zero,
            calculateImageLoadRect(
                IntSizeCompat.Zero,
                contentSize,
                preferredTileSize,
                contentVisibleRect
            )
        )
        assertEquals(
            IntRectCompat.Zero,
            calculateImageLoadRect(
                imageSize,
                IntSizeCompat.Zero,
                preferredTileSize,
                contentVisibleRect
            )
        )
        assertEquals(
            IntRectCompat.Zero,
            calculateImageLoadRect(imageSize, contentSize, preferredTileSize, IntRectCompat.Zero)
        )

        contentVisibleRect = IntRectCompat(
            left = 0,
            top = contentSize.height - contentVisibleSize.height,
            right = contentVisibleSize.width,
            bottom = contentSize.height
        )
        assertEquals(
            IntRectCompat(0, 2864, 472, 3073),
            calculateImageLoadRect(imageSize, contentSize, preferredTileSize, contentVisibleRect)
        )

        contentVisibleRect = IntRectCompat(
            left = contentSize.width - contentVisibleSize.width,
            top = 0,
            right = contentSize.width,
            bottom = contentVisibleSize.height
        )
        assertEquals(
            IntRectCompat(769, 0, 1241, 209),
            calculateImageLoadRect(imageSize, contentSize, preferredTileSize, contentVisibleRect)
        )

        contentVisibleRect = IntRectCompat(
            left = contentSize.width - contentVisibleSize.width,
            top = contentSize.height - contentVisibleSize.height,
            right = contentSize.width,
            bottom = contentSize.height
        )
        assertEquals(
            IntRectCompat(769, 2864, 1241, 3073),
            calculateImageLoadRect(imageSize, contentSize, preferredTileSize, contentVisibleRect)
        )

        contentVisibleRect = IntRectCompat(
            left = (contentSize.width - contentVisibleSize.width) / 2,
            top = (contentSize.height - contentVisibleSize.height) / 2,
            right = contentSize.width - ((contentSize.width - contentVisibleSize.width) / 2),
            bottom = contentSize.height - (contentSize.height - contentVisibleSize.height) / 2
        )
        assertEquals(
            IntRectCompat(297, 1400, 944, 1673),
            calculateImageLoadRect(imageSize, contentSize, preferredTileSize, contentVisibleRect)
        )
    }


    /**
     * Returns an IntSizeCompat scaled by dividing [this] by [scale]
     */
    private fun IntSizeCompat.roundDiv(scale: Float): IntSizeCompat =
        IntSizeCompat(
            width = (this.width / scale).roundToInt(),
            height = (this.height / scale).roundToInt()
        )

    /**
     * Returns an IntSizeCompat scaled by dividing [this] by [scale]
     */
    private fun IntSizeCompat.ceilDiv(scale: Float): IntSizeCompat =
        IntSizeCompat(
            width = ceil(this.width / scale).toInt(),
            height = ceil(this.height / scale).toInt()
        )

    /**
     * Returns an IntSizeCompat scaled by dividing [this] by [scale]
     */
    private fun IntSizeCompat.floorDiv(scale: Float): IntSizeCompat =
        IntSizeCompat(
            width = floor(this.width / scale).toInt(),
            height = floor(this.height / scale).toInt()
        )

    private data class Item<T>(
        val imageSize: IntSizeCompat,
        val preferredTileSize: IntSizeCompat,
        val sampleSize: Int = 1,
        val excepted: T
    )
}