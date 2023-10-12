package com.github.panpf.zoomimage.core.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.subsampling.internal.calculateGridSize
import com.github.panpf.zoomimage.subsampling.internal.calculateImageLoadRect
import com.github.panpf.zoomimage.subsampling.internal.calculatePreferredTileSize
import com.github.panpf.zoomimage.subsampling.internal.calculateTileGridMap
import com.github.panpf.zoomimage.subsampling.internal.closestPowerOfTwo
import com.github.panpf.zoomimage.subsampling.internal.findSampleSize
import com.github.panpf.zoomimage.subsampling.toIntroString
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.toShortString
import org.junit.Assert
import org.junit.Test
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class TileManageUtilsTest {

    @Test
    fun testCalculatePreferredTileSize() {
        Assert.assertEquals(
            /* expected = */ IntSizeCompat(1080, 1920) / 2,
            /* actual = */ calculatePreferredTileSize(IntSizeCompat(1080, 1920))
        )

        Assert.assertEquals(
            /* expected = */ IntSizeCompat(1000, 2000) / 2,
            /* actual = */ calculatePreferredTileSize(IntSizeCompat(1000, 2000))
        )
    }

    @Test
    fun testClosestPowerOfTwo() {
        val result = (1..17)
            .flatMap { number -> buildList { repeat(10) { index -> add(number + (index * 0.1f)) } } }
            .map { scale -> scale to closestPowerOfTwo(scale) }
            .distinctBy { it.second }
            .map { "${it.first}:${it.second}" }
            .toString()
        Assert.assertEquals("[1.0:1, 1.5:2, 2.9:4, 5.7:8, 11.4:16]", result)
    }

    @Test
    fun testFindSampleSize() {
        val imageSize = IntSizeCompat(690, 12176)

        Assert.assertEquals(
            /* expected = */ 0,
            /* actual = */ findSampleSize(IntSizeCompat.Zero, imageSize / 16, 4f)
        )
        Assert.assertEquals(
            /* expected = */ 0,
            /* actual = */ findSampleSize(imageSize, IntSizeCompat.Zero, 4f)
        )
        Assert.assertEquals(
            /* expected = */ 0,
            /* actual = */ findSampleSize(imageSize, imageSize / 16, 0f)
        )
        Assert.assertEquals(
            /* expected = */ 128,
            /* actual = */ findSampleSize(imageSize, imageSize / 16, 0.1f)
        )
        Assert.assertEquals(
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
            Assert.assertEquals(
                /* message = */ "index=$index, thumbnailSize=${thumbnailSize.toShortString()}",
                /* expected = */ excepted,
                /* actual = */ result
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
            Assert.assertEquals(
                "index=$index, preferredTileSize=${item.preferredTileSize.toShortString()}, sampleSize=${item.sampleSize}",
                item.excepted,
                result
            )
        }

        Assert.assertEquals(
            /* expected = */ IntOffsetCompat(3266, 999),
            /* actual = */ calculateGridSize(
                imageSize = IntSizeCompat(9798, 6988),
                preferredTileSize = IntSizeCompat(3, 7),
                sampleSize = 1
            )
        )

        Assert.assertEquals(
            /* expected = */ IntOffsetCompat(200, 150),
            /* actual = */ calculateGridSize(
                imageSize = IntSizeCompat(9798, 6988),
                preferredTileSize = IntSizeCompat(3, 7),
                sampleSize = 1,
                maxGridSize = IntOffsetCompat(200, 150)
            )
        )
    }

    @Test
    fun testCalculateTileGridMap() {
        val checkTiles: (List<Tile>, IntSizeCompat) -> Unit =
            { tileList, imageSize ->
                var minLeft = 0
                var minTop = 0
                var maxRight = 0
                var maxBottom = 0
                var lastTop = 0
                var lastRight = 0
                tileList.forEachIndexed { index, tile ->
                    if (index == 0) {
                        Assert.assertEquals(0, tile.srcRect.left)
                        Assert.assertEquals(0, tile.srcRect.top)
                    } else if (index == tileList.lastIndex) {
                        Assert.assertEquals(imageSize.width, tile.srcRect.right)
                        Assert.assertEquals(imageSize.height, tile.srcRect.bottom)
                    }

                    Assert.assertEquals(lastRight, tile.srcRect.left)
                    Assert.assertEquals(lastTop, tile.srcRect.top)
                    if (tile.srcRect.right >= imageSize.width) {
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
                Assert.assertEquals(0, minLeft)
                Assert.assertEquals(0, minTop)
                Assert.assertEquals(imageSize.width, maxRight)
                Assert.assertEquals(imageSize.height, maxBottom)
            }

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
                excepted = "[64:1:1x1,32:2:2x1,16:4:4x1,8:7:7x1,4:14:14x1,2:28:28x1,1:56:56x1]"
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
            Assert.assertEquals(
                "index=$index, imageSize=${item.imageSize.toShortString()}, preferredTileSize=${item.preferredTileSize.toShortString()}",
                item.excepted,
                result.toIntroString()
            )
            result.values.forEach { checkTiles(it, item.imageSize) }
        }

        Assert.assertEquals(
            /* expected = */ "[4096:1:1x1,2048:2:2x1,1024:4:4x1,512:14:7x2,256:52:13x4,128:208:26x8,64:832:52x16,32:3296:103x32,16:9450:150x63,8:16050:150x107,4:16050:150x107,2:16050:150x107,1:16050:150x107]",
            /* actual = */ calculateTileGridMap(
                imageSize = IntSizeCompat(9798, 6988),
                preferredTileSize = IntSizeCompat(3, 7)
            ).toIntroString()
        )
        Assert.assertEquals(
            /* expected = */ "[4096:1:1x1,2048:2:1x2,1024:4:1x4,512:14:2x7,256:52:4x13,128:208:8x26,64:832:16x52,32:3296:32x103,16:9450:63x150,8:16050:107x150,4:16050:107x150,2:16050:107x150,1:16050:107x150]",
            /* actual = */ calculateTileGridMap(
                imageSize = IntSizeCompat(6988, 9798),
                preferredTileSize = IntSizeCompat(7, 3)
            ).toIntroString()
        )
    }

    @Test
    fun testCalculateImageLoadRect() {
        val imageSize = IntSizeCompat(1241, 3073)
        val preferredTileSize = IntSizeCompat(333, 111)

        val contentSize = imageSize / 8
        Assert.assertEquals(
            IntSizeCompat(155, 384),
            contentSize
        )

        val contentVisibleSize = IntSizeCompat(
            width = contentSize.width / 4,
            height = contentSize.width / 8,
        )
        Assert.assertEquals(
            IntSizeCompat(38, 19),
            contentVisibleSize
        )

        var contentVisibleRect =
            IntRectCompat(0, 0, contentVisibleSize.width, contentVisibleSize.height)
        Assert.assertEquals(
            IntRectCompat(0, 0, 472, 209),
            calculateImageLoadRect(imageSize, contentSize, preferredTileSize, contentVisibleRect)
        )

        Assert.assertEquals(
            IntRectCompat.Zero,
            calculateImageLoadRect(IntSizeCompat.Zero, contentSize, preferredTileSize, contentVisibleRect)
        )
        Assert.assertEquals(
            IntRectCompat.Zero,
            calculateImageLoadRect(imageSize, IntSizeCompat.Zero, preferredTileSize, contentVisibleRect)
        )
        Assert.assertEquals(
            IntRectCompat.Zero,
            calculateImageLoadRect(imageSize, contentSize, preferredTileSize, IntRectCompat.Zero)
        )

        contentVisibleRect = IntRectCompat(
            left = 0,
            top = contentSize.height - contentVisibleSize.height,
            right = contentVisibleSize.width,
            bottom = contentSize.height
        )
        Assert.assertEquals(
            IntRectCompat(0, 2864, 472, 3073),
            calculateImageLoadRect(imageSize, contentSize, preferredTileSize, contentVisibleRect)
        )

        contentVisibleRect = IntRectCompat(
            left = contentSize.width - contentVisibleSize.width,
            top = 0,
            right = contentSize.width,
            bottom = contentVisibleSize.height
        )
        Assert.assertEquals(
            IntRectCompat(769, 0, 1241, 209),
            calculateImageLoadRect(imageSize, contentSize, preferredTileSize, contentVisibleRect)
        )

        contentVisibleRect = IntRectCompat(
            left = contentSize.width - contentVisibleSize.width,
            top = contentSize.height - contentVisibleSize.height,
            right = contentSize.width,
            bottom = contentSize.height
        )
        Assert.assertEquals(
            IntRectCompat(769, 2864, 1241, 3073),
            calculateImageLoadRect(imageSize, contentSize, preferredTileSize, contentVisibleRect)
        )

        contentVisibleRect = IntRectCompat(
            left = (contentSize.width - contentVisibleSize.width) / 2,
            top = (contentSize.height - contentVisibleSize.height) / 2,
            right = contentSize.width - ((contentSize.width - contentVisibleSize.width) / 2),
            bottom = contentSize.height - (contentSize.height - contentVisibleSize.height) / 2
        )
        Assert.assertEquals(
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