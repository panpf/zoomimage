package com.github.panpf.zoomimage.core.desktop.test.subsampling.internal

import com.githb.panpf.zoomimage.images.ResourceImageFile
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileImageCacheSpec
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.TileState
import com.github.panpf.zoomimage.subsampling.internal.TileDecoder
import com.github.panpf.zoomimage.subsampling.internal.TileImageCacheHelper
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.subsampling.internal.calculateImageLoadRect
import com.github.panpf.zoomimage.subsampling.internal.calculatePreferredTileSize
import com.github.panpf.zoomimage.subsampling.internal.defaultRegionDecoder
import com.github.panpf.zoomimage.subsampling.internal.toIntroString
import com.github.panpf.zoomimage.subsampling.toFactory
import com.github.panpf.zoomimage.test.decodeImageInfo
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.test.useApply
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import java.io.Closeable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TileManagerTest {

    @Test
    fun testDefaultPausedContinuousTransformTypes() {
        assertEquals(
            ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE,
            TileManager.DefaultPausedContinuousTransformTypes
        )
    }

    @Test
    fun testPausedContinuousTransformType() = runTest {
        createTileManagerHolder(ResourceImages.hugeLongQmsht).useApply {
            assertEquals(
                TileManager.DefaultPausedContinuousTransformTypes,
                tileManager.pausedContinuousTransformTypes
            )
            listOf(
                ContinuousTransformType.SCALE to -2,
                ContinuousTransformType.OFFSET to -2,
                ContinuousTransformType.LOCATE to -2,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                assertEquals(
                    expected = it.second,
                    actual = refreshTiles(continuousTransformType = it.first),
                    message = "continuousTransformType=${it.first}",
                )
            }

            tileManager.pausedContinuousTransformTypes = 0
            listOf(
                ContinuousTransformType.SCALE to 0,
                ContinuousTransformType.OFFSET to 0,
                ContinuousTransformType.LOCATE to 0,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                assertEquals(
                    expected = it.second,
                    actual = refreshTiles(continuousTransformType = it.first),
                    message = "continuousTransformType=${it.first}",
                )
            }

            tileManager.pausedContinuousTransformTypes = ContinuousTransformType.SCALE
            listOf(
                ContinuousTransformType.SCALE to -2,
                ContinuousTransformType.OFFSET to 0,
                ContinuousTransformType.LOCATE to 0,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                assertEquals(
                    expected = it.second,
                    actual = refreshTiles(continuousTransformType = it.first),
                    message = "continuousTransformType=${it.first}",
                )
            }

            tileManager.pausedContinuousTransformTypes = ContinuousTransformType.SCALE or
                    ContinuousTransformType.OFFSET
            listOf(
                ContinuousTransformType.SCALE to -2,
                ContinuousTransformType.OFFSET to -2,
                ContinuousTransformType.LOCATE to 0,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                assertEquals(
                    expected = it.second,
                    actual = refreshTiles(continuousTransformType = it.first),
                    message = "continuousTransformType=${it.first}",
                )
            }

            tileManager.pausedContinuousTransformTypes = ContinuousTransformType.SCALE or
                    ContinuousTransformType.OFFSET or
                    ContinuousTransformType.LOCATE
            listOf(
                ContinuousTransformType.SCALE to -2,
                ContinuousTransformType.OFFSET to -2,
                ContinuousTransformType.LOCATE to -2,
                ContinuousTransformType.GESTURE to 0,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                assertEquals(
                    expected = it.second,
                    actual = refreshTiles(continuousTransformType = it.first),
                    message = "continuousTransformType=${it.first}",
                )
            }

            tileManager.pausedContinuousTransformTypes = ContinuousTransformType.SCALE or
                    ContinuousTransformType.OFFSET or
                    ContinuousTransformType.LOCATE or
                    ContinuousTransformType.GESTURE
            listOf(
                ContinuousTransformType.SCALE to -2,
                ContinuousTransformType.OFFSET to -2,
                ContinuousTransformType.LOCATE to -2,
                ContinuousTransformType.GESTURE to -2,
                ContinuousTransformType.FLING to 0,
            ).forEach {
                assertEquals(
                    expected = it.second,
                    actual = refreshTiles(continuousTransformType = it.first),
                    message = "continuousTransformType=${it.first}",
                )
            }

            tileManager.pausedContinuousTransformTypes = ContinuousTransformType.SCALE or
                    ContinuousTransformType.OFFSET or
                    ContinuousTransformType.LOCATE or
                    ContinuousTransformType.GESTURE or
                    ContinuousTransformType.FLING
            listOf(
                ContinuousTransformType.SCALE to -2,
                ContinuousTransformType.OFFSET to -2,
                ContinuousTransformType.LOCATE to -2,
                ContinuousTransformType.GESTURE to -2,
                ContinuousTransformType.FLING to -2,
            ).forEach {
                assertEquals(
                    expected = it.second,
                    actual = refreshTiles(continuousTransformType = it.first),
                    message = "continuousTransformType=${it.first}",
                )
            }
        }
    }

    @Test
    fun testDisabledBackgroundTiles() = runTest {
        createTileManagerHolder(ResourceImages.hugeLongQmsht).useApply {
            assertEquals(false, tileManager.disabledBackgroundTiles)
            assertEquals(0, tileManager.sampleSize)
            assertEquals(emptyList(), tileManager.backgroundTiles)
            assertEquals(emptyList(), tileManager.foregroundTiles)

            assertEquals(0, refreshTiles(scale = 3f))
            assertEquals(8, tileManager.sampleSize)
            Thread.sleep(1000)
            assertEquals(emptyList(), tileManager.backgroundTiles)
            assertNotEquals(emptyList(), tileManager.foregroundTiles)

            assertEquals(0, refreshTiles(scale = 6f))
            assertEquals(4, tileManager.sampleSize)
            Thread.sleep(50)
            assertNotEquals(emptyList(), tileManager.backgroundTiles)
            assertNotEquals(emptyList(), tileManager.foregroundTiles)
            Thread.sleep(100)
            assertNotEquals(emptyList(), tileManager.backgroundTiles)
            assertNotEquals(emptyList(), tileManager.foregroundTiles)
            Thread.sleep(1000)
            assertEquals(emptyList(), tileManager.backgroundTiles)
            assertNotEquals(emptyList(), tileManager.foregroundTiles)

            assertEquals(0, refreshTiles(scale = 3f))
            assertEquals(8, tileManager.sampleSize)
            Thread.sleep(50)
            assertNotEquals(emptyList(), tileManager.backgroundTiles)
            assertNotEquals(emptyList(), tileManager.foregroundTiles)
            Thread.sleep(100)
            assertNotEquals(emptyList(), tileManager.backgroundTiles)
            assertNotEquals(emptyList(), tileManager.foregroundTiles)
            Thread.sleep(1000)
            assertEquals(emptyList(), tileManager.backgroundTiles)
            assertNotEquals(emptyList(), tileManager.foregroundTiles)

            tileManager.disabledBackgroundTiles = true
            assertEquals(true, tileManager.disabledBackgroundTiles)

            assertEquals(0, refreshTiles(scale = 6f))
            assertEquals(4, tileManager.sampleSize)
            Thread.sleep(50)
            assertEquals(emptyList(), tileManager.backgroundTiles)
            assertNotEquals(emptyList(), tileManager.foregroundTiles)
            Thread.sleep(100)
            assertEquals(emptyList(), tileManager.backgroundTiles)
            assertNotEquals(emptyList(), tileManager.foregroundTiles)
            Thread.sleep(1000)
            assertEquals(emptyList(), tileManager.backgroundTiles)
            assertNotEquals(emptyList(), tileManager.foregroundTiles)

            assertEquals(0, refreshTiles(scale = 3f))
            assertEquals(8, tileManager.sampleSize)
            Thread.sleep(50)
            assertEquals(emptyList(), tileManager.backgroundTiles)
            assertNotEquals(emptyList(), tileManager.foregroundTiles)
            Thread.sleep(100)
            assertEquals(emptyList(), tileManager.backgroundTiles)
            assertNotEquals(emptyList(), tileManager.foregroundTiles)
            Thread.sleep(1000)
            assertEquals(emptyList(), tileManager.backgroundTiles)
            assertNotEquals(emptyList(), tileManager.foregroundTiles)

            tileManager.disabledBackgroundTiles = true
            assertEquals(true, tileManager.disabledBackgroundTiles)
        }
    }

    @Test
    fun testTileAnimationSpec() = runTest {
        createTileManagerHolder(ResourceImages.hugeLongQmsht).useApply {
            assertEquals(TileAnimationSpec.Default, tileManager.tileAnimationSpec)

            assertTrue(foregroundTilesChangedList.size == 0)
            assertEquals(0, refreshTiles(scale = 3f))
            Thread.sleep(2000)
            assertTrue(foregroundTilesChangedList.any { it.any { tile -> tile.alpha < 255 } })

            foregroundTilesChangedList.clear()
            assertTrue(foregroundTilesChangedList.size == 0)
            assertEquals(0, refreshTiles(scale = 6f))
            Thread.sleep(2000)
            assertTrue(foregroundTilesChangedList.any { it.any { tile -> tile.alpha < 255 } })

            tileManager.tileAnimationSpec = TileAnimationSpec.None
            assertEquals(TileAnimationSpec.None, tileManager.tileAnimationSpec)

            foregroundTilesChangedList.clear()
            assertTrue(foregroundTilesChangedList.size == 0)
            assertEquals(0, refreshTiles(scale = 3f))
            Thread.sleep(2000)
            assertTrue(foregroundTilesChangedList.all { it.all { tile -> tile.alpha == 255 } })

            foregroundTilesChangedList.clear()
            assertTrue(foregroundTilesChangedList.size == 0)
            assertEquals(0, refreshTiles(scale = 6f))
            Thread.sleep(2000)
            assertTrue(foregroundTilesChangedList.all { it.all { tile -> tile.alpha == 255 } })
        }
    }

    @Test
    fun testSortedTileGridMap() = runTest {
        createTileManagerHolder(ResourceImages.hugeLongQmsht).useApply {
            assertEquals(
                "[32:2:2x1,16:4:4x1,8:7:7x1,4:14:14x1,2:28:28x1,1:50:50x1]",
                tileManager.sortedTileGridMap.toIntroString()
            )
        }
    }

    @Test
    fun testSampleSize() = runTest {
        createTileManagerHolder(ResourceImages.hugeLongQmsht).useApply {
            listOf(
                1f to 0,
                3f to 8,
                6f to 4,
                12f to 2,
                24f to 1,
            ).forEach { (scale, expectedSampleSize) ->
                refreshTiles(scale = scale)
                assertEquals(expectedSampleSize, tileManager.sampleSize, "scale=$scale")
            }
        }
    }

    @Test
    fun testImageLoadRect() = runTest {
        createTileManagerHolder(ResourceImages.hugeLongQmsht).useApply {
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
                    preferredTileSize = preferredTileSize,
                    contentVisibleRect = contentVisibleRect1
                ),
                contentVisibleRect2 to calculateImageLoadRect(
                    imageSize = imageInfo.size,
                    contentSize = contentSize,
                    preferredTileSize = preferredTileSize,
                    contentVisibleRect = contentVisibleRect2
                ),
                contentVisibleRect3 to calculateImageLoadRect(
                    imageSize = imageInfo.size,
                    contentSize = contentSize,
                    preferredTileSize = preferredTileSize,
                    contentVisibleRect = contentVisibleRect3
                ),
            ).forEach { (contentVisibleRect, expectedImageLoadRect) ->
                refreshTiles(contentVisibleRect = contentVisibleRect)
                assertEquals(
                    expected = expectedImageLoadRect,
                    actual = tileManager.imageLoadRect,
                    message = "contentVisibleRect=$contentVisibleRect",
                )
            }
        }
    }

    @Test
    fun testRefreshTiles() = runTest {
        // rotation =
        createTileManagerHolder(ResourceImages.hugeLongQmsht).useApply {
            listOf(-90, 0, 90, 180, 270, 360, 450)
                .forEach { rotation ->
                    assertEquals(
                        expected = 0,
                        actual = refreshTiles(rotation = rotation),
                        message = "rotation=$rotation",
                    )
                }
            listOf(-89, -91, -1, 1, 89, 91, 179, 181, 269, 271, 359, 361, 449, 451)
                .forEach { rotation ->
                    assertEquals(
                        expected = -1,
                        actual = refreshTiles(rotation = rotation),
                        message = "rotation=$rotation",
                    )
                }
        }

        // scale and contentVisibleRect
        createTileManagerHolder(ResourceImages.hugeCard).useApply {
            val tileManager = tileManager
            val contentSize = contentSize
            assertEquals(0, tileManager.sampleSize)
            assertEquals(IntRectCompat.Zero, tileManager.imageLoadRect)
            assertEquals(emptyList(), tileManager.backgroundTiles)
            assertEquals(emptyList(), tileManager.foregroundTiles)

            val contentVisibleSize = IntSizeCompat(
                width = contentSize.width / 2,
                height = contentSize.height / 3,
            )
            val widthSpace = (contentSize.width - contentVisibleSize.width) / 2
            val heightSpace = (contentSize.height - contentVisibleSize.height) / 2
            val contentVisibleRect1 = IntRectCompat(
                left = 0,
                top = 0,
                right = contentVisibleSize.width,
                bottom = contentVisibleSize.height,
            )
            val contentVisibleRect2 = IntRectCompat(
                left = widthSpace,
                top = heightSpace,
                right = widthSpace + contentVisibleSize.width,
                bottom = heightSpace + contentVisibleSize.height,
            )
            val contentVisibleRect3 = IntRectCompat(
                left = contentVisibleSize.width - widthSpace,
                top = contentVisibleSize.height - heightSpace,
                right = contentVisibleSize.width - widthSpace + contentVisibleSize.width,
                bottom = contentVisibleSize.height - heightSpace + contentVisibleSize.height,
            )

            refreshTiles(scale = 8f, contentVisibleRect = contentVisibleRect1)
            Thread.sleep(2000)
            assertEquals(4, tileManager.sampleSize)
            assertEquals(IntRectCompat(0, 0, 4049, 2370), tileManager.imageLoadRect)
            assertEquals(
                "[0x0, 1x0, 2x0]",
                tileManager.foregroundTiles
                    .filter { it.state == TileState.STATE_LOADED }
                    .map { it.coordinate.toShortString() }
                    .toString()
            )

            refreshTiles(scale = 8f, contentVisibleRect = contentVisibleRect2)
            Thread.sleep(2000)
            assertEquals(4, tileManager.sampleSize)
            assertEquals(IntRectCompat(1619, 1409, 5938, 4260), tileManager.imageLoadRect)
            assertEquals(
                "[0x0, 1x0, 2x0, 3x0, 0x1, 1x1, 2x1, 3x1]",
                tileManager.foregroundTiles
                    .filter { it.state == TileState.STATE_LOADED }
                    .map { it.coordinate.toShortString() }
                    .toString()
            )

            refreshTiles(scale = 8f, contentVisibleRect = contentVisibleRect3)
            Thread.sleep(2000)
            assertEquals(4, tileManager.sampleSize)
            assertEquals(IntRectCompat(1619, 0, 5938, 2370), tileManager.imageLoadRect)
            assertEquals(
                "[0x0, 1x0, 2x0, 3x0]",
                tileManager.foregroundTiles
                    .filter { it.state == TileState.STATE_LOADED }
                    .map { it.coordinate.toShortString() }
                    .toString()
            )

            refreshTiles(scale = 14f, contentVisibleRect = contentVisibleRect1)
            Thread.sleep(2000)
            assertEquals(2, tileManager.sampleSize)
            assertEquals(IntRectCompat(0, 0, 4049, 2370), tileManager.imageLoadRect)
            assertEquals(
                "[0x0, 1x0, 2x0, 3x0, 0x1, 1x1, 2x1, 3x1]",
                tileManager.foregroundTiles
                    .filter { it.state == TileState.STATE_LOADED }
                    .map { it.coordinate.toShortString() }
                    .toString()
            )

            refreshTiles(scale = 14f, contentVisibleRect = contentVisibleRect2)
            Thread.sleep(2000)
            assertEquals(2, tileManager.sampleSize)
            assertEquals(IntRectCompat(1619, 1409, 5938, 4260), tileManager.imageLoadRect)
            assertEquals(
                "[1x0, 2x0, 3x0, 4x0, 5x0, 1x1, 2x1, 3x1, 4x1, 5x1, 1x2, 2x2, 3x2, 4x2, 5x2]",
                tileManager.foregroundTiles
                    .filter { it.state == TileState.STATE_LOADED }
                    .map { it.coordinate.toShortString() }
                    .toString()
            )

            refreshTiles(scale = 14f, contentVisibleRect = contentVisibleRect3)
            Thread.sleep(2000)
            assertEquals(2, tileManager.sampleSize)
            assertEquals(IntRectCompat(1619, 0, 5938, 2370), tileManager.imageLoadRect)
            assertEquals(
                "[1x0, 2x0, 3x0, 4x0, 5x0, 1x1, 2x1, 3x1, 4x1, 5x1]",
                tileManager.foregroundTiles
                    .filter { it.state == TileState.STATE_LOADED }
                    .map { it.coordinate.toShortString() }
                    .toString()
            )
        }
    }

    @Test
    fun testClean() = runTest {
        createTileManagerHolder(ResourceImages.hugeLongQmsht).useApply {
            assertEquals(0, refreshTiles(scale = 3f))
            Thread.sleep(1000)
            assertEquals(
                5,
                tileManager.foregroundTiles.count { it.state == TileState.STATE_LOADED })

            runBlocking(Dispatchers.Main) {
                tileManager.clean("testClean")
            }
            Thread.sleep(1000)
            assertEquals(
                7,
                tileManager.foregroundTiles.count { it.state == TileState.STATE_NONE })

            assertEquals(0, refreshTiles(scale = 6f))
            Thread.sleep(1000)
            assertEquals(
                8,
                tileManager.foregroundTiles.count { it.state == TileState.STATE_LOADED })

            runBlocking(Dispatchers.Main) {
                tileManager.clean("testClean")
            }
            Thread.sleep(1000)
            assertEquals(
                14,
                tileManager.foregroundTiles.count { it.state == TileState.STATE_NONE })
        }
    }

    private fun createTileManagerHolder(
        resourceImageFile: ResourceImageFile
    ): TileManagerHolder {
        val imageSource = resourceImageFile.toImageSource()
        val imageInfo = imageSource.decodeImageInfo()
        return TileManagerHolder(imageSource, imageInfo)
    }

    private class TileManagerHolder(imageSource: ImageSource, val imageInfo: ImageInfo) :
        Closeable {
        private val logger = Logger("Test").apply {
            level = Logger.Level.Debug
        }
        private val tileImageCacheHelper = TileImageCacheHelper(TileImageCacheSpec())
        val containerSize = IntSizeCompat(1080, 1920)
        val preferredTileSize = calculatePreferredTileSize(containerSize)
        val contentSize = imageInfo.size / 32
        val tileDecoder = TileDecoder(
            logger,
            defaultRegionDecoder().create(SubsamplingImage(imageSource.toFactory()), imageSource)
        )
        val backgroundTilesChangedList = mutableListOf<List<TileSnapshot>>()
        val foregroundTilesChangedList = mutableListOf<List<TileSnapshot>>()
        val sampleSizeChangedList = mutableListOf<Int>()
        val imageLoadChangedList = mutableListOf<IntRectCompat>()
        val tileManager = TileManager(
            logger = logger,
            subsamplingImage = SubsamplingImage(imageSource.toFactory()),
            tileDecoder = tileDecoder,
            tileImageConvertor = null,
            tileImageCacheHelper = tileImageCacheHelper,
            imageInfo = imageInfo,
            preferredTileSize = preferredTileSize,
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
            runBlocking(Dispatchers.IO) {
                tileDecoder.close()
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
        val continuousTransformType = 0

        fun refreshTiles(
            scale: Float = this.scale,
            contentVisibleRect: IntRectCompat = this.contentVisibleRect,
            rotation: Int = this.rotation,
            continuousTransformType: Int = this.continuousTransformType
        ): Int {
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