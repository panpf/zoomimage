package com.github.panpf.zoomimage.core.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.subsampling.internal.findSampleSize
import com.github.panpf.zoomimage.subsampling.internal.initializeTileMap
import com.github.panpf.zoomimage.util.IntSizeCompat
import org.junit.Assert
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class TileMangeUtilsTest {

    @Test
    fun testInitializeTileMap() {
        val checkTiles: (List<Tile>, Int, IntSizeCompat) -> Unit =
            { tileList, expectedSize, imageSize ->
                Assert.assertEquals(expectedSize, tileList.size)
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

        initializeTileMap(
            imageSize = IntSizeCompat(8000, 8000),
            tileMaxSize = IntSizeCompat(1080, 1920)
        ).apply {
            Assert.assertEquals(4, size)
            checkTiles(get(1)!!, 40, IntSizeCompat(8000, 8000))
            checkTiles(get(2)!!, 12, IntSizeCompat(8000, 8000))
            checkTiles(get(4)!!, 4, IntSizeCompat(8000, 8000))
            checkTiles(get(8)!!, 1, IntSizeCompat(8000, 8000))
        }

        initializeTileMap(
            imageSize = IntSizeCompat(8000, 3000),
            tileMaxSize = IntSizeCompat(1080, 1920)
        ).apply {
            Assert.assertEquals(4, size)
            checkTiles(get(1)!!, 16, IntSizeCompat(8000, 3000))
            checkTiles(get(2)!!, 4, IntSizeCompat(8000, 3000))
            checkTiles(get(4)!!, 2, IntSizeCompat(8000, 3000))
            checkTiles(get(8)!!, 1, IntSizeCompat(8000, 3000))
        }

        initializeTileMap(
            imageSize = IntSizeCompat(3000, 8000),
            tileMaxSize = IntSizeCompat(1080, 1920)
        ).apply {
            Assert.assertEquals(4, size)
            checkTiles(get(1)!!, 15, IntSizeCompat(3000, 8000))
            checkTiles(get(2)!!, 6, IntSizeCompat(3000, 8000))
            checkTiles(get(4)!!, 2, IntSizeCompat(3000, 8000))
            checkTiles(get(8)!!, 1, IntSizeCompat(3000, 8000))
        }


        initializeTileMap(
            imageSize = IntSizeCompat(1500, 1500),
            tileMaxSize = IntSizeCompat(1080, 1920)
        ).apply {
            Assert.assertEquals(2, size)
            checkTiles(get(1)!!, 2, IntSizeCompat(1500, 1500))
            checkTiles(get(2)!!, 1, IntSizeCompat(1500, 1500))
        }

        initializeTileMap(
            imageSize = IntSizeCompat(1000, 1500),
            tileMaxSize = IntSizeCompat(1080, 1920)
        ).apply {
            Assert.assertEquals(1, size)
            checkTiles(get(1)!!, 1, IntSizeCompat(1000, 1500))
        }

        initializeTileMap(
            imageSize = IntSizeCompat(1500, 1000),
            tileMaxSize = IntSizeCompat(1080, 1920)
        ).apply {
            Assert.assertEquals(2, size)
            checkTiles(get(1)!!, 2, IntSizeCompat(1500, 1000))
            checkTiles(get(2)!!, 1, IntSizeCompat(1500, 1000))
        }

        initializeTileMap(
            imageSize = IntSizeCompat(1000, 1000),
            tileMaxSize = IntSizeCompat(1080, 1920)
        ).apply {
            Assert.assertEquals(1, size)
            checkTiles(get(1)!!, 1, IntSizeCompat(1000, 1000))
        }


        initializeTileMap(
            imageSize = IntSizeCompat(30000, 926),
            tileMaxSize = IntSizeCompat(1080, 1920)
        ).apply {
            Assert.assertEquals(6, size)
            checkTiles(get(1)!!, 28, IntSizeCompat(30000, 926))
            checkTiles(get(2)!!, 14, IntSizeCompat(30000, 926))
            checkTiles(get(4)!!, 7, IntSizeCompat(30000, 926))
            checkTiles(get(8)!!, 4, IntSizeCompat(30000, 926))
            checkTiles(get(16)!!, 2, IntSizeCompat(30000, 926))
            checkTiles(get(32)!!, 1, IntSizeCompat(30000, 926))
        }

        initializeTileMap(
            imageSize = IntSizeCompat(690, 12176),
            tileMaxSize = IntSizeCompat(1080, 1920)
        ).apply {
            Assert.assertEquals(4, size)
            checkTiles(get(1)!!, 7, IntSizeCompat(690, 12176))
            checkTiles(get(2)!!, 4, IntSizeCompat(690, 12176))
            checkTiles(get(4)!!, 2, IntSizeCompat(690, 12176))
            checkTiles(get(8)!!, 1, IntSizeCompat(690, 12176))
        }

        initializeTileMap(
            imageSize = IntSizeCompat(7557, 5669),
            tileMaxSize = IntSizeCompat(1080, 1920)
        ).apply {
            Assert.assertEquals(4, size)
            checkTiles(get(1)!!, 21, IntSizeCompat(7557, 5669))
            checkTiles(get(2)!!, 8, IntSizeCompat(7557, 5669))
            checkTiles(get(4)!!, 2, IntSizeCompat(7557, 5669))
            checkTiles(get(8)!!, 1, IntSizeCompat(7557, 5669))
        }

        initializeTileMap(
            IntSizeCompat(9798, 6988),
            tileMaxSize = IntSizeCompat(1080, 1920)
        ).apply {
            Assert.assertEquals(5, size)
            checkTiles(get(1)!!, 40, IntSizeCompat(9798, 6988))
            checkTiles(get(2)!!, 10, IntSizeCompat(9798, 6988))
            checkTiles(get(4)!!, 3, IntSizeCompat(9798, 6988))
            checkTiles(get(8)!!, 2, IntSizeCompat(9798, 6988))
            checkTiles(get(16)!!, 1, IntSizeCompat(9798, 6988))
        }
    }

    @Test
    fun testFindSampleSize() {
        Assert.assertEquals(16, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(50, 50), 1f))
        Assert.assertEquals(8, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(51, 51), 1f))
        Assert.assertEquals(8, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(99, 99), 1f))
        Assert.assertEquals(8, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(100, 100), 1f))
        Assert.assertEquals(4, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(101, 101), 1f))
        Assert.assertEquals(4, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(199, 199), 1f))
        Assert.assertEquals(4, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(200, 200), 1f))
        Assert.assertEquals(2, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(201, 201), 1f))
        Assert.assertEquals(2, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(399, 399), 1f))
        Assert.assertEquals(2, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(400, 400), 1f))
        Assert.assertEquals(1, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(401, 401), 1f))
        Assert.assertEquals(1, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(799, 799), 1f))
        Assert.assertEquals(1, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(800, 800), 1f))
        Assert.assertEquals(1, findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(801, 801), 1f))
        Assert.assertEquals(
            1,
            findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(10000, 10000), 1f)
        )

        Assert.assertEquals(
            findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(200, 200), 1f),
            findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(100, 100), 2f)
        )
        Assert.assertEquals(
            findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(300, 300), 1f),
            findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(100, 100), 3f)
        )
        Assert.assertEquals(
            findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(400, 400), 1f),
            findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(100, 100), 4f)
        )
        Assert.assertEquals(
            findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(700, 700), 1f),
            findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(100, 100), 7f)
        )
        Assert.assertEquals(
            findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(800, 800), 1f),
            findSampleSize(IntSizeCompat(800, 800), IntSizeCompat(100, 100), 8f)
        )
    }
}