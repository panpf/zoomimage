package com.github.panpf.zoomimage.core.test.subsampling

import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.core.test.internal.useApply
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileBitmapPoolHelper
import com.github.panpf.zoomimage.subsampling.TileDecoder
import com.github.panpf.zoomimage.subsampling.TileManager
import com.github.panpf.zoomimage.subsampling.TileMemoryCacheHelper
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.internal.calculateImageLoadRect
import com.github.panpf.zoomimage.subsampling.internal.calculateTileMaxSize
import com.github.panpf.zoomimage.subsampling.readImageInfo
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.io.Closeable

class TileManagerTest {

    @Test
    fun testCompanion() {
        Assert.assertEquals(
            ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE,
            TileManager.DefaultPausedContinuousTransformType
        )
    }

    @Test
    fun testPausedContinuousTransformType() {
        ManagerHolder().useApply {
            Assert.assertEquals(
                TileManager.DefaultPausedContinuousTransformType,
                tileManager.pausedContinuousTransformType
            )
            listOf(
                ContinuousTransformType.SCALE to -5,
                ContinuousTransformType.OFFSET to -5,
                ContinuousTransformType.LOCATE to -5,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTilesWithContinuousTransformType(it.first)
                )
            }

            tileManager.pausedContinuousTransformType = 0
            listOf(
                ContinuousTransformType.SCALE to 0,
                ContinuousTransformType.OFFSET to 0,
                ContinuousTransformType.LOCATE to 0,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTilesWithContinuousTransformType(it.first)
                )
            }

            tileManager.pausedContinuousTransformType = ContinuousTransformType.SCALE
            listOf(
                ContinuousTransformType.SCALE to -5,
                ContinuousTransformType.OFFSET to 0,
                ContinuousTransformType.LOCATE to 0,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTilesWithContinuousTransformType(it.first)
                )
            }

            tileManager.pausedContinuousTransformType = ContinuousTransformType.SCALE or
                    ContinuousTransformType.OFFSET
            listOf(
                ContinuousTransformType.SCALE to -5,
                ContinuousTransformType.OFFSET to -5,
                ContinuousTransformType.LOCATE to 0,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTilesWithContinuousTransformType(it.first)
                )
            }

            tileManager.pausedContinuousTransformType = ContinuousTransformType.SCALE or
                    ContinuousTransformType.OFFSET or
                    ContinuousTransformType.LOCATE
            listOf(
                ContinuousTransformType.SCALE to -5,
                ContinuousTransformType.OFFSET to -5,
                ContinuousTransformType.LOCATE to -5,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTilesWithContinuousTransformType(it.first)
                )
            }

            tileManager.pausedContinuousTransformType = ContinuousTransformType.SCALE or
                    ContinuousTransformType.OFFSET or
                    ContinuousTransformType.LOCATE or
                    ContinuousTransformType.GESTURE
            listOf(
                ContinuousTransformType.SCALE to -5,
                ContinuousTransformType.OFFSET to -5,
                ContinuousTransformType.LOCATE to -5,
                ContinuousTransformType.GESTURE to -5,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTilesWithContinuousTransformType(it.first)
                )
            }

            tileManager.pausedContinuousTransformType = ContinuousTransformType.SCALE or
                    ContinuousTransformType.OFFSET or
                    ContinuousTransformType.LOCATE or
                    ContinuousTransformType.GESTURE or
                    ContinuousTransformType.FLING
            listOf(
                ContinuousTransformType.SCALE to -5,
                ContinuousTransformType.OFFSET to -5,
                ContinuousTransformType.LOCATE to -5,
                ContinuousTransformType.GESTURE to -5,
                ContinuousTransformType.FLING to -5,
            ).forEach {
                Assert.assertEquals(
                    "continuousTransformType=${it.first}",
                    it.second,
                    refreshTilesWithContinuousTransformType(it.first)
                )
            }
        }
    }

    @Test
    fun testDisabledBackgroundTiles() {
        ManagerHolder().useApply {
            Assert.assertEquals(false, tileManager.disabledBackgroundTiles)
            Assert.assertEquals(0, tileManager.sampleSize)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)

            Assert.assertEquals(0, refreshTilesWithScale(3f))
            Assert.assertEquals(8, tileManager.sampleSize)
            Thread.sleep(1000)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)

            Assert.assertEquals(0, refreshTilesWithScale(6f))
            Assert.assertEquals(4, tileManager.sampleSize)
            Thread.sleep(50)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(100)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(1000)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)

            Assert.assertEquals(0, refreshTilesWithScale(3f))
            Assert.assertEquals(8, tileManager.sampleSize)
            Thread.sleep(50)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(100)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(1000)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)

            tileManager.disabledBackgroundTiles = true
            Assert.assertEquals(true, tileManager.disabledBackgroundTiles)

            Assert.assertEquals(0, refreshTilesWithScale(6f))
            Assert.assertEquals(4, tileManager.sampleSize)
            Thread.sleep(50)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(100)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(1000)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)

            Assert.assertEquals(0, refreshTilesWithScale(3f))
            Assert.assertEquals(8, tileManager.sampleSize)
            Thread.sleep(50)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(100)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
            Thread.sleep(1000)
            Assert.assertEquals(emptyList<TileSnapshot>(), tileManager.backgroundTiles)
            Assert.assertNotEquals(emptyList<TileSnapshot>(), tileManager.foregroundTiles)
        }
    }

    @Test
    fun testTileAnimationSpec() {
        ManagerHolder().useApply {
            Assert.assertEquals(TileAnimationSpec.Default, tileManager.tileAnimationSpec)

            Assert.assertTrue(foregroundTilesChangedList.size == 0)
            Assert.assertEquals(0, refreshTilesWithScale(3f))
            Thread.sleep(1000)
            Assert.assertTrue(foregroundTilesChangedList.any { it.any { tile -> tile.alpha < 255 } })

            foregroundTilesChangedList.clear()
            Assert.assertTrue(foregroundTilesChangedList.size == 0)
            Assert.assertEquals(0, refreshTilesWithScale(6f))
            Thread.sleep(1000)
            Assert.assertTrue(foregroundTilesChangedList.any { it.any { tile -> tile.alpha < 255 } })

            tileManager.tileAnimationSpec = TileAnimationSpec.None
            Assert.assertEquals(TileAnimationSpec.None, tileManager.tileAnimationSpec)

            foregroundTilesChangedList.clear()
            Assert.assertTrue(foregroundTilesChangedList.size == 0)
            Assert.assertEquals(0, refreshTilesWithScale(3f))
            Thread.sleep(1000)
            Assert.assertTrue(foregroundTilesChangedList.all { it.all { tile -> tile.alpha == 255 } })

            foregroundTilesChangedList.clear()
            Assert.assertTrue(foregroundTilesChangedList.size == 0)
            Assert.assertEquals(0, refreshTilesWithScale(6f))
            Thread.sleep(1000)
            Assert.assertTrue(foregroundTilesChangedList.all { it.all { tile -> tile.alpha == 255 } })
        }
    }

    @Test
    fun testTileMaxSize() {
        ManagerHolder().useApply {
            Assert.assertEquals(calculateTileMaxSize(containerSize), tileManager.tileMaxSize)
        }
    }

    @Test
    fun testSortedTileGridMap() {
        ManagerHolder().useApply {
            val tileGridMapInfo = tileManager.sortedTileGridMap.entries.map { entry ->
                val tableSize =
                    entry.value.last().coordinate.let { IntSizeCompat(it.x + 1, it.y + 1) }
                "${entry.key}:${entry.value.size}:${tableSize.toShortString()}"
            }.toString()
            Assert.assertEquals(
                "[16:1:1x1, 8:2:1x2, 4:4:1x4, 2:7:1x7, 1:26:2x13]",
                tileGridMapInfo
            )
        }
    }

    @Test
    fun testSampleSize() {
        ManagerHolder().useApply {
            listOf(
                1f to 0,
                3f to 8,
                6f to 4,
                12f to 2,
                24f to 1,
            ).forEach { (scale, expectedSampleSize) ->
                refreshTilesWithScale(scale)
                Assert.assertEquals("scale=$scale", expectedSampleSize, tileManager.sampleSize)
            }
        }
    }

    @Test
    fun testImageLoadRect() {
        ManagerHolder().useApply {
            val widthSpace = contentSize.width / 4
            val heightSpace = contentSize.height / 4
            val contentVisibleRect1 = IntRectCompat(
                left = widthSpace,
                top = heightSpace,
                right = contentSize.width - widthSpace,
                bottom = contentSize.height - heightSpace,
            )
            val contentVisibleRect2 = IntRectCompat(
                left = 0,
                top = 0,
                right = contentSize.width - widthSpace * 2,
                bottom = contentSize.height - heightSpace * 2,
            )
            val contentVisibleRect3 = IntRectCompat(
                left = contentSize.width - widthSpace * 2,
                top = contentSize.height - heightSpace * 2,
                right = contentSize.width,
                bottom = contentSize.height,
            )
            listOf(
                contentVisibleRect1 to calculateImageLoadRect(
                    imageSize = imageInfo.size,
                    contentSize = contentSize,
                    tileMaxSize = tileMaxSize,
                    contentVisibleRect = contentVisibleRect1
                ),
                contentVisibleRect2 to calculateImageLoadRect(
                    imageSize = imageInfo.size,
                    contentSize = contentSize,
                    tileMaxSize = tileMaxSize,
                    contentVisibleRect = contentVisibleRect2
                ),
                contentVisibleRect3 to calculateImageLoadRect(
                    imageSize = imageInfo.size,
                    contentSize = contentSize,
                    tileMaxSize = tileMaxSize,
                    contentVisibleRect = contentVisibleRect3
                ),
            ).forEach { (contentVisibleRect, expectedImageLoadRect) ->
                refreshTilesWithContentVisibleRect(contentVisibleRect)
                Assert.assertEquals(
                    "contentVisibleRect=$contentVisibleRect",
                    expectedImageLoadRect,
                    tileManager.imageLoadRect
                )
            }
        }
    }

    // todo refreshTiles

    @Test
    fun testClean() {
        ManagerHolder().useApply {
            Assert.assertEquals(0, refreshTilesWithScale(3f))
            Thread.sleep(1000)
            Assert.assertTrue(tileManager.foregroundTiles.all { it.state == Tile.STATE_LOADED })

            runBlocking(Dispatchers.Main) {
                tileManager.clean("testClean")
            }
            Thread.sleep(1000)
            Assert.assertTrue(tileManager.foregroundTiles.all { it.state == Tile.STATE_NONE })

            Assert.assertEquals(0, refreshTilesWithScale(6f))
            Thread.sleep(1000)
            Assert.assertTrue(tileManager.foregroundTiles.all { it.state == Tile.STATE_LOADED })

            runBlocking(Dispatchers.Main) {
                tileManager.clean("testClean")
            }
            Thread.sleep(1000)
            Assert.assertTrue(tileManager.foregroundTiles.all { it.state == Tile.STATE_NONE })
        }
    }

//    @Test
//    fun testConstructor() {
//        ManagerHolder().useApply {
//            tileManager.also {
//                Assert.assertEquals(false, it.disabledBackgroundTiles)
//                Assert.assertEquals(TileAnimationSpec.Default, it.tileAnimationSpec)
//                Assert.assertEquals(tileMaxSize, it.tileMaxSize)
//                Assert.assertEquals(
//                    initializeTileMap(imageInfo.size, tileMaxSize)
//                        .toSortedMap { o1, o2 -> (o1 - o2) * -1 },
//                    it.sortedTileMap,
//                )
//                Assert.assertEquals(0, it.sampleSize)
//                Assert.assertEquals(IntRectCompat.Zero, it.imageLoadRect)
//                Assert.assertEquals(emptyList<TileSnapshot>(), it.foregroundTiles)
//                Assert.assertEquals(emptyList<TileSnapshot>(), it.backgroundTiles)
//                Assert.assertEquals(emptyList<List<TileSnapshot>>(), backgroundTilesChangedList)
//                Assert.assertEquals(emptyList<List<TileSnapshot>>(), foregroundTilesChangedList)
//                Assert.assertEquals(emptyList<Int>(), sampleSizeChangedList)
//                Assert.assertEquals(emptyList<IntRectCompat>(), imageLoadChangedList)
//            }
//
//            var contentVisibleRect = IntRectCompat(
//                left = imageInfo.width / 2,
//                top = imageInfo.height / 2,
//                right = imageInfo.width - imageInfo.width / 2,
//                bottom = imageInfo.height - imageInfo.height / 2,
//            )
//            var scale = 2.5f
//            var rotation = 0
//            var continuousTransformType = ContinuousTransformType.GESTURE
//            val refreshTilesResult = runBlocking(Dispatchers.Main) {
//                tileManager.refreshTiles(
//                    contentVisibleRect,
//                    scale,
//                    rotation,
//                    continuousTransformType,
//                    "test"
//                )
//            }
//            Assert.assertEquals(0, refreshTilesResult)
//            Thread.sleep(2000)
//            tileManager.also {
//                Assert.assertEquals(2, it.sampleSize)
//                Assert.assertEquals(
//                    calculateImageLoadRect(
//                        imageSize = imageInfo.size,
//                        contentSize = contentSize,
//                        tileMaxSize = tileMaxSize,
//                        contentVisibleRect = contentVisibleRect
//                    ),
//                    it.imageLoadRect
//                )
//            }
//        }
//    }

    private class ManagerHolder : Closeable {
        private val context = InstrumentationRegistry.getInstrumentation().context
        private val logger = Logger("Test").apply {
            level = Logger.DEBUG
        }
        private val imageSource = ImageSource.fromAsset(context, "sample_long_comic.jpg")
        val imageInfo = imageSource.readImageInfo(false).getOrThrow()
        private val tileBitmapPoolHelper = TileBitmapPoolHelper(logger)
        private val tileMemoryCacheHelper = TileMemoryCacheHelper(logger)
        val containerSize = IntSizeCompat(1080, 1920)
        val tileMaxSize = calculateTileMaxSize(containerSize)
        val contentSize = imageInfo.size / 32
        val tileDecoder = TileDecoder(logger, imageSource, tileBitmapPoolHelper, imageInfo)
        val backgroundTilesChangedList = mutableListOf<List<TileSnapshot>>()
        val foregroundTilesChangedList = mutableListOf<List<TileSnapshot>>()
        val sampleSizeChangedList = mutableListOf<Int>()
        val imageLoadChangedList = mutableListOf<IntRectCompat>()
        val tileManager = TileManager(
            logger = logger,
            tileDecoder = tileDecoder,
            tileMemoryCacheHelper = tileMemoryCacheHelper,
            tileBitmapPoolHelper = tileBitmapPoolHelper,
            imageSource = imageSource,
            imageInfo = imageInfo,
            containerSize = containerSize,
            contentSize = contentSize,
            onTileChanged = {
                backgroundTilesChangedList.add(it.backgroundTiles)
                foregroundTilesChangedList.add(it.foregroundTiles)
            },
            onSampleSizeChanged = {
                sampleSizeChangedList.add(it.sampleSize)
            },
            onImageLoadRectChanged = {
                imageLoadChangedList.add(it.imageLoadRect)
            },
        )

        override fun close() {
            runBlocking(Dispatchers.Main) {
                tileDecoder.destroy("close")
            }
        }

        val contentVisibleRect = IntRectCompat(
            left = contentSize.width / 4,
            top = contentSize.height / 4,
            right = contentSize.width - contentSize.width / 4,
            bottom = contentSize.height - contentSize.height / 4,
        )
        val scale = 3f
        val rotation = 0
        val continuousTransformType = ContinuousTransformType.NONE

        fun refreshTilesWithContinuousTransformType(continuousTransformType: Int): Int {
            return runBlocking(Dispatchers.Main) {
                tileManager.refreshTiles(
                    scale = scale,
                    contentVisibleRect = contentVisibleRect,
                    rotation = rotation,
                    continuousTransformType = continuousTransformType,
                    caller = "test"
                )
            }
        }

        fun refreshTilesWithScale(scale: Float): Int {
            return runBlocking(Dispatchers.Main) {
                tileManager.refreshTiles(
                    scale = scale,
                    contentVisibleRect = contentVisibleRect,
                    rotation = rotation,
                    continuousTransformType = continuousTransformType,
                    caller = "test"
                )
            }
        }

        fun refreshTilesWithContentVisibleRect(contentVisibleRect: IntRectCompat): Int {
            return runBlocking(Dispatchers.Main) {
                tileManager.refreshTiles(
                    scale = scale,
                    contentVisibleRect = contentVisibleRect,
                    rotation = rotation,
                    continuousTransformType = continuousTransformType,
                    caller = "test"
                )
            }
        }
    }
}