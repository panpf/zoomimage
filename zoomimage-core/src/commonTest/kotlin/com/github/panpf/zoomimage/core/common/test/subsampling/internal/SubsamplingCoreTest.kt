package com.github.panpf.zoomimage.core.common.test.subsampling.internal

import com.githb.panpf.zoomimage.images.ResourceImageFile
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileState
import com.github.panpf.zoomimage.subsampling.internal.SubsamplingCore
import com.github.panpf.zoomimage.subsampling.internal.ZoomableBridge
import com.github.panpf.zoomimage.test.block
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.times
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SubsamplingCoreTest {

    @Test
    fun test() = runTest {
        val imageFile: ResourceImageFile = ResourceImages.hugeChina
        val contentSize: IntSizeCompat = imageFile.size / 8

        val zoomableBridge = TestZoomableBridge(imageFile, contentSize)
        val subsamplingCore = SubsamplingCore(
            module = "Test",
            logger = Logger("Test"),
            tileImageConvertor = null,
            zoomableBridge = zoomableBridge,
            onReadyChanged = {},
            onTileChanged = {}
        )
        assertEquals(expected = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = false, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(expected = null, actual = subsamplingCore.imageInfo)
        assertEquals(expected = 0, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.sampleSize)
        assertEquals(expected = IntRectCompat.Zero, actual = subsamplingCore.imageLoadRect)
        assertEquals(expected = emptyMap(), actual = subsamplingCore.tileGridSizeMap)

        withContext(Dispatchers.Main) {
            subsamplingCore.onAttached()
        }
        block(5000)
        assertEquals(expected = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = false, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(expected = null, actual = subsamplingCore.imageInfo)
        assertEquals(expected = 0, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.sampleSize)
        assertEquals(expected = IntRectCompat.Zero, actual = subsamplingCore.imageLoadRect)
        assertEquals(expected = emptyMap(), actual = subsamplingCore.tileGridSizeMap)

        zoomableBridge.containerSizeState.value = IntSizeCompat(1200, 700)
        block(5000)
        assertEquals(expected = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = false, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(expected = null, actual = subsamplingCore.imageInfo)
        assertEquals(expected = 0, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.sampleSize)
        assertEquals(expected = IntRectCompat.Zero, actual = subsamplingCore.imageLoadRect)
        assertEquals(expected = emptyMap(), actual = subsamplingCore.tileGridSizeMap)

        zoomableBridge.contentSizeState.value = contentSize
        block(5000)
        assertEquals(expected = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = false, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(expected = null, actual = subsamplingCore.imageInfo)
        assertEquals(expected = 0, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.sampleSize)
        assertEquals(expected = IntRectCompat.Zero, actual = subsamplingCore.imageLoadRect)
        assertEquals(expected = emptyMap(), actual = subsamplingCore.tileGridSizeMap)

        withContext(Dispatchers.Main) {
            subsamplingCore.setImage(imageFile.toImageSource())
        }
        block(5000)
        assertNotEquals(illegal = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = true, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(
            expected = ImageInfo(imageFile.size, "image/jpeg"),
            actual = subsamplingCore.imageInfo
        )
        assertEquals(expected = 168, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(
            expected = 72,
            actual = subsamplingCore.foregroundTiles.count { it.state == TileState.STATE_LOADED })
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 1, actual = subsamplingCore.sampleSize)
        assertEquals(
            expected = IntRectCompat(1397, 1049, 5402, 3841),
            actual = subsamplingCore.imageLoadRect
        )
        assertEquals(
            expected = "{8=(2, 2), 4=(3, 4), 2=(6, 7), 1=(12, 14)}",
            actual = subsamplingCore.tileGridSizeMap.toString()
        )
    }

    @Test
    fun testAttachedAndDetached() = runTest {
        val imageFile: ResourceImageFile = ResourceImages.hugeChina
        val contentSize: IntSizeCompat = imageFile.size / 8

        val zoomableBridge = TestZoomableBridge(imageFile, contentSize)
        val subsamplingCore = SubsamplingCore(
            module = "Test",
            logger = Logger("Test"),
            tileImageConvertor = null,
            zoomableBridge = zoomableBridge,
            onReadyChanged = {},
            onTileChanged = {}
        )
        assertEquals(expected = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = false, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(expected = null, actual = subsamplingCore.imageInfo)
        assertEquals(expected = 0, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.sampleSize)
        assertEquals(expected = IntRectCompat.Zero, actual = subsamplingCore.imageLoadRect)
        assertEquals(expected = emptyMap(), actual = subsamplingCore.tileGridSizeMap)

        withContext(Dispatchers.Main) {
            subsamplingCore.onAttached()
        }
        zoomableBridge.containerSizeState.value = IntSizeCompat(1200, 700)
        zoomableBridge.contentSizeState.value = contentSize
        withContext(Dispatchers.Main) {
            subsamplingCore.setImage(imageFile.toImageSource())
        }
        block(5000)
        assertNotEquals(illegal = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = true, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(
            expected = ImageInfo(imageFile.size, "image/jpeg"),
            actual = subsamplingCore.imageInfo
        )
        assertEquals(expected = 168, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(
            expected = 72,
            actual = subsamplingCore.foregroundTiles.count { it.state == TileState.STATE_LOADED })
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 1, actual = subsamplingCore.sampleSize)
        assertEquals(
            expected = IntRectCompat(1397, 1049, 5402, 3841),
            actual = subsamplingCore.imageLoadRect
        )
        assertEquals(
            expected = "{8=(2, 2), 4=(3, 4), 2=(6, 7), 1=(12, 14)}",
            actual = subsamplingCore.tileGridSizeMap.toString()
        )

        withContext(Dispatchers.Main) {
            subsamplingCore.onDetached()
        }
        block(5000)
        assertNotEquals(illegal = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = false, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(expected = null, actual = subsamplingCore.imageInfo)
        assertEquals(expected = 0, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.sampleSize)
        assertEquals(expected = IntRectCompat.Zero, actual = subsamplingCore.imageLoadRect)
        assertEquals(expected = emptyMap(), actual = subsamplingCore.tileGridSizeMap)

        withContext(Dispatchers.Main) {
            subsamplingCore.onAttached()
        }
        block(5000)
        assertNotEquals(illegal = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = true, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(
            expected = ImageInfo(imageFile.size, "image/jpeg"),
            actual = subsamplingCore.imageInfo
        )
        assertEquals(expected = 168, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(
            expected = 72,
            actual = subsamplingCore.foregroundTiles.count { it.state == TileState.STATE_LOADED })
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 1, actual = subsamplingCore.sampleSize)
        assertEquals(
            expected = IntRectCompat(1397, 1049, 5402, 3841),
            actual = subsamplingCore.imageLoadRect
        )
        assertEquals(
            expected = "{8=(2, 2), 4=(3, 4), 2=(6, 7), 1=(12, 14)}",
            actual = subsamplingCore.tileGridSizeMap.toString()
        )
    }

    @Test
    fun testUpdateContainerSize() = runTest {
        val imageFile: ResourceImageFile = ResourceImages.hugeChina
        val contentSize: IntSizeCompat = imageFile.size / 8

        val zoomableBridge = TestZoomableBridge(imageFile, contentSize)
        val subsamplingCore = SubsamplingCore(
            module = "Test",
            logger = Logger("Test"),
            tileImageConvertor = null,
            zoomableBridge = zoomableBridge,
            onReadyChanged = {},
            onTileChanged = {}
        )
        assertEquals(expected = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = false, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(expected = null, actual = subsamplingCore.imageInfo)
        assertEquals(expected = 0, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.sampleSize)
        assertEquals(expected = IntRectCompat.Zero, actual = subsamplingCore.imageLoadRect)
        assertEquals(expected = emptyMap(), actual = subsamplingCore.tileGridSizeMap)

        withContext(Dispatchers.Main) {
            subsamplingCore.onAttached()
        }
        zoomableBridge.containerSizeState.value = IntSizeCompat(1200, 700)
        zoomableBridge.contentSizeState.value = contentSize
        withContext(Dispatchers.Main) {
            subsamplingCore.setImage(imageFile.toImageSource())
        }
        block(5000)
        assertNotEquals(illegal = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = true, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(
            expected = ImageInfo(imageFile.size, "image/jpeg"),
            actual = subsamplingCore.imageInfo
        )
        assertEquals(expected = 168, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(
            expected = 72,
            actual = subsamplingCore.foregroundTiles.count { it.state == TileState.STATE_LOADED })
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 1, actual = subsamplingCore.sampleSize)
        assertEquals(
            expected = IntRectCompat(1397, 1049, 5402, 3841),
            actual = subsamplingCore.imageLoadRect
        )
        assertEquals(
            expected = "{8=(2, 2), 4=(3, 4), 2=(6, 7), 1=(12, 14)}",
            actual = subsamplingCore.tileGridSizeMap.toString()
        )

        zoomableBridge.containerSizeState.value = IntSizeCompat(1200, 700).times(1.5f)
        block(5000)
        assertNotEquals(illegal = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = true, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(
            expected = ImageInfo(imageFile.size, "image/jpeg"),
            actual = subsamplingCore.imageInfo
        )
        assertEquals(expected = 168, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(
            expected = 72,
            actual = subsamplingCore.foregroundTiles.count { it.state == TileState.STATE_LOADED })
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 1, actual = subsamplingCore.sampleSize)
        assertEquals(
            expected = IntRectCompat(1397, 1049, 5402, 3841),
            actual = subsamplingCore.imageLoadRect
        )
        assertEquals(
            expected = "{8=(2, 2), 4=(3, 4), 2=(6, 7), 1=(12, 14)}",
            actual = subsamplingCore.tileGridSizeMap.toString()
        )

        zoomableBridge.containerSizeState.value = IntSizeCompat(1200, 700).times(2f)
        block(5000)
        assertNotEquals(illegal = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = true, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(
            expected = ImageInfo(imageFile.size, "image/jpeg"),
            actual = subsamplingCore.imageInfo
        )
        assertEquals(expected = 42, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(
            expected = 30,
            actual = subsamplingCore.foregroundTiles.count { it.state == TileState.STATE_LOADED })
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 1, actual = subsamplingCore.sampleSize)
        assertEquals(
            expected = IntRectCompat(1097, 874, 5702, 4016),
            actual = subsamplingCore.imageLoadRect
        )
        assertEquals(
            expected = "{8=(1, 1), 4=(2, 2), 2=(3, 4), 1=(6, 7)}",
            actual = subsamplingCore.tileGridSizeMap.toString()
        )
    }

    @Test
    fun testUpdateContentSize() = runTest {
        val imageFile: ResourceImageFile = ResourceImages.hugeChina
        val contentSize: IntSizeCompat = imageFile.size / 8

        val zoomableBridge = TestZoomableBridge(imageFile, contentSize)
        val subsamplingCore = SubsamplingCore(
            module = "Test",
            logger = Logger("Test"),
            tileImageConvertor = null,
            zoomableBridge = zoomableBridge,
            onReadyChanged = {},
            onTileChanged = {}
        )
        assertEquals(expected = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = false, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(expected = null, actual = subsamplingCore.imageInfo)
        assertEquals(expected = 0, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.sampleSize)
        assertEquals(expected = IntRectCompat.Zero, actual = subsamplingCore.imageLoadRect)
        assertEquals(expected = emptyMap(), actual = subsamplingCore.tileGridSizeMap)

        withContext(Dispatchers.Main) {
            subsamplingCore.onAttached()
        }
        zoomableBridge.containerSizeState.value = IntSizeCompat(1200, 700)
        zoomableBridge.contentSizeState.value = contentSize
        withContext(Dispatchers.Main) {
            subsamplingCore.setImage(imageFile.toImageSource())
        }
        block(5000)
        assertNotEquals(illegal = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = true, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(
            expected = ImageInfo(imageFile.size, "image/jpeg"),
            actual = subsamplingCore.imageInfo
        )
        assertEquals(expected = 168, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(
            expected = 72,
            actual = subsamplingCore.foregroundTiles.count { it.state == TileState.STATE_LOADED })
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 1, actual = subsamplingCore.sampleSize)
        assertEquals(
            expected = IntRectCompat(1397, 1049, 5402, 3841),
            actual = subsamplingCore.imageLoadRect
        )
        assertEquals(
            expected = "{8=(2, 2), 4=(3, 4), 2=(6, 7), 1=(12, 14)}",
            actual = subsamplingCore.tileGridSizeMap.toString()
        )

        zoomableBridge.contentSizeState.value = IntSizeCompat.Zero
        block(5000)
        assertNotEquals(illegal = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = false, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(expected = null, actual = subsamplingCore.imageInfo)
        assertEquals(expected = 0, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 0, actual = subsamplingCore.sampleSize)
        assertEquals(expected = IntRectCompat.Zero, actual = subsamplingCore.imageLoadRect)
        assertEquals(expected = emptyMap(), actual = subsamplingCore.tileGridSizeMap)

        zoomableBridge.contentSizeState.value = contentSize
        block(5000)
        assertNotEquals(illegal = null, actual = subsamplingCore.subsamplingImage)
        assertEquals(expected = true, actual = subsamplingCore.ready)
        assertEquals(expected = false, actual = subsamplingCore.stopped)
        assertEquals(
            expected = ImageInfo(imageFile.size, "image/jpeg"),
            actual = subsamplingCore.imageInfo
        )
        assertEquals(expected = 168, actual = subsamplingCore.foregroundTiles.size)
        assertEquals(
            expected = 72,
            actual = subsamplingCore.foregroundTiles.count { it.state == TileState.STATE_LOADED })
        assertEquals(expected = 0, actual = subsamplingCore.backgroundTiles.size)
        assertEquals(expected = 1, actual = subsamplingCore.sampleSize)
        assertEquals(
            expected = IntRectCompat(1397, 1049, 5402, 3841),
            actual = subsamplingCore.imageLoadRect
        )
        assertEquals(
            expected = "{8=(2, 2), 4=(3, 4), 2=(6, 7), 1=(12, 14)}",
            actual = subsamplingCore.tileGridSizeMap.toString()
        )
    }

    // TODO test

    private class TestZoomableBridge(
        imageFile: ResourceImageFile,
        val contentSize: IntSizeCompat
    ) : ZoomableBridge {

        private val _transformFlow: MutableStateFlow<TransformCompat> =
            MutableStateFlow(
                TransformCompat(
                    scale = ScaleFactorCompat(8f),
                    offset = OffsetCompat(
                        x = imageFile.size.width / 4f * -1,
                        y = imageFile.size.height / 4f * -1
                    ),
                    rotation = 0f,
                )
            )
        private val _continuousTransformTypeFlow: MutableStateFlow<Int> = MutableStateFlow(0)

        var contentOriginSize: IntSizeCompat? = null
        val containerSizeState = MutableStateFlow(IntSizeCompat.Zero)
        val contentSizeState = MutableStateFlow(contentSize)

        override val contentVisibleRect: RectCompat
            get() {
                val widthOneQuarter = contentSize.width / 4f
                val heightOneQuarter = contentSize.height / 4f
                return RectCompat(
                    left = widthOneQuarter,
                    top = heightOneQuarter,
                    right = contentSize.width - widthOneQuarter,
                    bottom = contentSize.height - heightOneQuarter
                )
            }
        override val transform: TransformCompat
            get() = _transformFlow.value
        override val continuousTransformType: Int
            get() = _continuousTransformTypeFlow.value
        override val transformFlow: Flow<TransformCompat>
            get() = _transformFlow
        override val continuousTransformTypeFlow: Flow<Int>
            get() = _continuousTransformTypeFlow
        override val containerSizeFlow: Flow<IntSizeCompat>
            get() = containerSizeState
        override val contentSizeFlow: Flow<IntSizeCompat>
            get() = contentSizeState

        override fun setContentOriginSize(contentOriginSize: IntSizeCompat) {
            this.contentOriginSize = contentOriginSize
        }
    }
}