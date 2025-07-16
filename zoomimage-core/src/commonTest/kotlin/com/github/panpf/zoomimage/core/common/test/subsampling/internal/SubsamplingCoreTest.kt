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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SubsamplingCoreTest {

    @Test
    fun test() {
        val imageFile: ResourceImageFile = ResourceImages.hugeChina
        val contentSize: IntSizeCompat = imageFile.size / 8

        val subsamplingCore = SubsamplingCore(
            module = "Test",
            logger = Logger("Test"),
            tileImageConvertor = null,
            zoomableBridge = TestZoomableBridge(imageFile, contentSize),
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

        subsamplingCore.onAttached()
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

        subsamplingCore.setContainerSize(IntSizeCompat(1200, 700))
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

        subsamplingCore.setContentSize(contentSize)
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

        subsamplingCore.setImage(imageFile.toImageSource())
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
    fun testAttachedAndDetached() {
        val imageFile: ResourceImageFile = ResourceImages.hugeChina
        val contentSize: IntSizeCompat = imageFile.size / 8

        val subsamplingCore = SubsamplingCore(
            module = "Test",
            logger = Logger("Test"),
            tileImageConvertor = null,
            zoomableBridge = TestZoomableBridge(imageFile, contentSize),
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

        subsamplingCore.onAttached()
        subsamplingCore.setContainerSize(IntSizeCompat(1200, 700))
        subsamplingCore.setContentSize(contentSize)
        subsamplingCore.setImage(imageFile.toImageSource())
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

        subsamplingCore.onDetached()
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

        subsamplingCore.onAttached()
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
    fun testSetContainerSize() {
        val imageFile: ResourceImageFile = ResourceImages.hugeChina
        val contentSize: IntSizeCompat = imageFile.size / 8

        val subsamplingCore = SubsamplingCore(
            module = "Test",
            logger = Logger("Test"),
            tileImageConvertor = null,
            zoomableBridge = TestZoomableBridge(imageFile, contentSize),
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

        subsamplingCore.onAttached()
        subsamplingCore.setContainerSize(IntSizeCompat(1200, 700))
        subsamplingCore.setContentSize(contentSize)
        subsamplingCore.setImage(imageFile.toImageSource())
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

        subsamplingCore.setContainerSize(IntSizeCompat(1200, 700).times(1.5f))
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

        subsamplingCore.setContainerSize(IntSizeCompat(1200, 700).times(2f))
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
    fun testSetContentSize() {
        val imageFile: ResourceImageFile = ResourceImages.hugeChina
        val contentSize: IntSizeCompat = imageFile.size / 8

        val subsamplingCore = SubsamplingCore(
            module = "Test",
            logger = Logger("Test"),
            tileImageConvertor = null,
            zoomableBridge = TestZoomableBridge(imageFile, contentSize),
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

        subsamplingCore.onAttached()
        subsamplingCore.setContainerSize(IntSizeCompat(1200, 700))
        subsamplingCore.setContentSize(contentSize)
        subsamplingCore.setImage(imageFile.toImageSource())
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

        subsamplingCore.setContentSize(IntSizeCompat.Zero)
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

        subsamplingCore.setContentSize(contentSize)
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

        override fun setContentOriginSize(contentOriginSize: IntSizeCompat) {
            this.contentOriginSize = contentOriginSize
        }
    }
}