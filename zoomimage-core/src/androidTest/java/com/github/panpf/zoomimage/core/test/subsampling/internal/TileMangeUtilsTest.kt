package com.github.panpf.zoomimage.core.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.subsampling.internal.calculateImageLoadRect
import com.github.panpf.zoomimage.subsampling.internal.calculateTileGridMap
import com.github.panpf.zoomimage.subsampling.internal.calculateTileMaxSize
import com.github.panpf.zoomimage.subsampling.toIntroString
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import org.junit.Assert
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class TileMangeUtilsTest {

    @Test
    fun testCalculateTileMaxSize() {
        Assert.assertEquals(
            /* expected = */ IntSizeCompat(1080, 1920) / 2,
            /* actual = */ calculateTileMaxSize(IntSizeCompat(1080, 1920))
        )

        Assert.assertEquals(
            /* expected = */ IntSizeCompat(1000, 2000) / 2,
            /* actual = */ calculateTileMaxSize(IntSizeCompat(1000, 2000))
        )
    }

    @Test
    fun testFindSampleSize() {
        // todo testFindSampleSize
//        val imageSize = IntSizeCompat(690, 12176)
//
//        Assert.assertEquals(
//            /* expected = */ 0,
//            /* actual = */ findSampleSize(IntSizeCompat.Zero, imageSize / 16, 4f)
//        )
//        Assert.assertEquals(
//            /* expected = */ 0,
//            /* actual = */ findSampleSize(imageSize, IntSizeCompat.Zero, 4f)
//        )
//        Assert.assertEquals(
//            /* expected = */ 0,
//            /* actual = */ findSampleSize(imageSize, imageSize / 16, 0f)
//        )
//
//        var thumbnailSize = imageSize / 16
////        listOf(
////            0 to 0f,
////            128 to 0.1f,
////            32 to 0.5f,
////            16 to 0.9f,
////            16 to 1.0f,
////
////            8 to 1.1f,
////            8 to 2.0f,
////
////            4 to 2.1f,
////            4 to 4.0f,
////
////            2 to 4.1f,
////            2 to 8.0f,
////
////            1 to 8.1f,
////            1 to 100.0f,
////        ).forEach { (expectSampleSize, scale) ->
////            Assert.assertEquals(
////                /* message = */ "scale=$scale",
////                /* expected = */ expectSampleSize,
////                /* actual = */ findSampleSize(imageSize, thumbnailSize, scale)
////            )
////        }
//
//        thumbnailSize =
//            IntSizeCompat(ceil(imageSize.width / 4f).toInt(), ceil(imageSize.height / 4f).toInt())
//
//        listOf(
//            0.1f, 0.5f, 0.9f,
//            1.0f, 1.1f, 1.9f,
//            2.0f, 2.1f, 2.9f,
//            3.0f, 3.1f, 3.9f,
//            4.0f, 4.1f, 4.9f,
//            6.0f, 6.1f, 6.9f,
//            8.0f, 8.1f, 8.9f,
//            12.0f, 12.1f, 12.9f,
//            16.0f, 16.1f, 16.9f,
//            17.0f, 17.1f, 17.9f,
//        ).map { scale ->
//            val sampleSize = findSampleSize(imageSize, thumbnailSize, scale)
//            "$scale:$sampleSize"
//        }.also { list ->
//            val excepted = """
//                0.1:32, 0.5:8, 0.9:4,
//                1.0:4, 1.1:4, 1.9:2,
//                2.0:2, 2.1:2, 2.9:1,
//                3.0:1, 3.1:1, 3.9:1,
//                4.0:1, 4.1:1, 4.9:1,
//                6.0:1, 6.1:1, 6.9:1,
//                8.0:1, 8.1:1, 8.9:1,
//                12.0:1, 12.1:1, 12.9:1,
//                16.0:1, 16.1:1, 16.9:1,
//                17.0:1, 17.1:1, 17.9:1,
//            """.trimIndent()
//            val result = buildString {
//                list.forEachIndexed { index, chunkedList ->
//                    if (index > 0) {
//                        append("\n")
//                    }
////                    append(chunkedList.joinToString(separator = ", "));append(",")
//                }
//            }
//            Assert.assertEquals(excepted, result)
////            Assert.assertEquals("", result)
//        }
//
////        listOf(
////            0 to 0f,
////            32 to 0.1f,
////            8 to 0.5f,
////            4 to 0.9f,
////            4 to 1.0f,
////
////            2 to 1.1f,
////            2 to 2.0f,
////
////            1 to 2.1f,
////            1 to 4.0f,
////        ).forEach { (expectSampleSize, scale) ->
////            Assert.assertEquals(
////                /* message = */ "scale=$scale",
////                /* expected = */ expectSampleSize,
////                /* actual = */ findSampleSize(imageSize, thumbnailSize, scale)
////            )
////        }
    }

    @Test
    fun testInitializeTileMap() {
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
        val tileMaxSize = containerSize / 2

        var imageSize = IntSizeCompat(8000, 8000)
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 16,
        ).apply {
            Assert.assertEquals("[16:1:1x1,8:4:2x2,4:12:4x3,2:40:8x5,1:135:15x9]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 32,
        ).apply {
            Assert.assertEquals("[16:1:1x1,8:4:2x2,4:12:4x3,2:40:8x5,1:135:15x9]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 8,
        ).apply {
            Assert.assertEquals("[8:4:2x2,4:12:4x3,2:40:8x5,1:135:15x9]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 2,
        ).apply {
            Assert.assertEquals("[2:40:8x5,1:135:15x9]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }

        imageSize = IntSizeCompat(8000, 3000)
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 16,
        ).apply {
            Assert.assertEquals("[16:1:1x1,8:2:2x1,4:4:4x1,2:16:8x2,1:60:15x4]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }

        imageSize = IntSizeCompat(3000, 8000)
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 16,
        ).apply {
            Assert.assertEquals("[16:1:1x1,8:2:1x2,4:6:2x3,2:15:3x5,1:54:6x9]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }


        imageSize = IntSizeCompat(1500, 1500)
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 16,
        ).apply {
            Assert.assertEquals("[4:1:1x1,2:2:2x1,1:6:3x2]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }

        imageSize = IntSizeCompat(1000, 1500)
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 16,
        ).apply {
            Assert.assertEquals("[2:1:1x1,1:4:2x2]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }

        imageSize = IntSizeCompat(1500, 1000)
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 16,
        ).apply {
            Assert.assertEquals("[4:1:1x1,2:2:2x1,1:6:3x2]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }

        imageSize = IntSizeCompat(1000, 1000)
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 16,
        ).apply {
            Assert.assertEquals("[2:1:1x1,1:4:2x2]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }


        imageSize = IntSizeCompat(30000, 926)
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 16,
        ).apply {
            Assert.assertEquals("[16:4:4x1,8:7:7x1,4:14:14x1,2:28:28x1,1:56:56x1]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }

        imageSize = IntSizeCompat(690, 12176)
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 16,
        ).apply {
            Assert.assertEquals("[16:1:1x1,8:2:1x2,4:4:1x4,2:7:1x7,1:26:2x13]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }

        imageSize = IntSizeCompat(7557, 5669)
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 16,
        ).apply {
            Assert.assertEquals("[16:1:1x1,8:2:2x1,4:8:4x2,2:21:7x3,1:84:14x6]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }

        imageSize = IntSizeCompat(9798, 6988)
        calculateTileGridMap(
            imageSize = imageSize,
            tileMaxSize = tileMaxSize,
            thumbnailSize = imageSize / 16,
        ).apply {
            Assert.assertEquals("[16:2:2x1,8:3:3x1,4:10:5x2,2:40:10x4,1:152:19x8]", toIntroString())
            values.forEach { checkTiles(it, imageSize) }
        }
    }

    @Test
    fun testCalculateImageLoadRect() {
        val imageSize = IntSizeCompat(1241, 3073)
        val tileMaxSize = IntSizeCompat(333, 111)

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
            calculateImageLoadRect(imageSize, contentSize, tileMaxSize, contentVisibleRect)
        )

        contentVisibleRect = IntRectCompat(
            left = 0,
            top = contentSize.height - contentVisibleSize.height,
            right = contentVisibleSize.width,
            bottom = contentSize.height
        )
        Assert.assertEquals(
            IntRectCompat(0, 2864, 472, 3073),
            calculateImageLoadRect(imageSize, contentSize, tileMaxSize, contentVisibleRect)
        )

        contentVisibleRect = IntRectCompat(
            left = contentSize.width - contentVisibleSize.width,
            top = 0,
            right = contentSize.width,
            bottom = contentVisibleSize.height
        )
        Assert.assertEquals(
            IntRectCompat(769, 0, 1241, 209),
            calculateImageLoadRect(imageSize, contentSize, tileMaxSize, contentVisibleRect)
        )

        contentVisibleRect = IntRectCompat(
            left = contentSize.width - contentVisibleSize.width,
            top = contentSize.height - contentVisibleSize.height,
            right = contentSize.width,
            bottom = contentSize.height
        )
        Assert.assertEquals(
            IntRectCompat(769, 2864, 1241, 3073),
            calculateImageLoadRect(imageSize, contentSize, tileMaxSize, contentVisibleRect)
        )

        contentVisibleRect = IntRectCompat(
            left = (contentSize.width - contentVisibleSize.width) / 2,
            top = (contentSize.height - contentVisibleSize.height) / 2,
            right = contentSize.width - ((contentSize.width - contentVisibleSize.width) / 2),
            bottom = contentSize.height - (contentSize.height - contentVisibleSize.height) / 2
        )
        Assert.assertEquals(
            IntRectCompat(297, 1400, 944, 1673),
            calculateImageLoadRect(imageSize, contentSize, tileMaxSize, contentVisibleRect)
        )
    }
}