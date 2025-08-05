package com.github.panpf.zoomimage.core.desktop.test.subsampling.internal

import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.internal.createTileDecoder
import com.github.panpf.zoomimage.test.TestRegionDecoder
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.div
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubsamplingDesktopTest {

    @Test
    fun testCreateTileDecoder() = runTest {
        val logger = Logger("MyTest")

        // success
        val hugeLongQmshtImageFile = ResourceImages.hugeLongQmsht
        val hugeLongQmshtImageSource = hugeLongQmshtImageFile.toImageSource()
        createTileDecoder(
            logger = logger,
            subsamplingImage = SubsamplingImage(hugeLongQmshtImageSource),
            contentSize = hugeLongQmshtImageFile.size / 32f,
            regionDecoders = emptyList(),
        ).getOrThrow().apply {
            assertEquals(
                expected = hugeLongQmshtImageFile.size,
                actual = imageInfo.size
            )
            assertEquals(
                expected = "image/jpeg",
                actual = imageInfo.mimeType
            )
        }

        // error: imageSize empty
        createTileDecoder(
            logger = logger,
            subsamplingImage = SubsamplingImage(
                imageSource = hugeLongQmshtImageSource,
                imageInfo = ImageInfo(0, 100, "image/jpeg")
            ),
            contentSize = hugeLongQmshtImageFile.size / 32f,
            regionDecoders = emptyList(),
        ).exceptionOrNull()!!
        createTileDecoder(
            logger = logger,
            subsamplingImage = SubsamplingImage(
                imageSource = hugeLongQmshtImageSource,
                imageInfo = ImageInfo(100, 0, "image/jpeg")
            ),
            contentSize = hugeLongQmshtImageFile.size / 32f,
            regionDecoders = emptyList(),
        ).exceptionOrNull()!!

        // error: contentSize >= imageSize
        createTileDecoder(
            logger = logger,
            subsamplingImage = SubsamplingImage(
                imageSource = hugeLongQmshtImageSource,
                imageInfo = ImageInfo(hugeLongQmshtImageFile.size, "image/jpeg")
            ),
            contentSize = hugeLongQmshtImageFile.size,
            regionDecoders = emptyList(),
        ).exceptionOrNull()!!

        // error: aspect ratio too different
        createTileDecoder(
            logger = logger,
            subsamplingImage = SubsamplingImage(
                imageSource = hugeLongQmshtImageSource,
                imageInfo = ImageInfo(hugeLongQmshtImageFile.size, "image/jpeg")
            ),
            contentSize = IntSizeCompat(
                hugeLongQmshtImageFile.size.width / 32,
                hugeLongQmshtImageFile.size.height / 35
            ),
            regionDecoders = emptyList(),
        ).exceptionOrNull()!!

        // error: unsupported mimeTypes
        createTileDecoder(
            logger = logger,
            subsamplingImage = SubsamplingImage(
                imageSource = hugeLongQmshtImageSource,
                imageInfo = ImageInfo(hugeLongQmshtImageFile.size, "image/jpeg")
            ),
            contentSize = hugeLongQmshtImageFile.size / 32,
            regionDecoders = listOf(
                TestRegionDecoder.Factory(
                    imageInfo = ImageInfo(100, 100, "image/jpeg"),
                    unsupportedMimeTypes = listOf("image/jpeg")
                )
            ),
        ).exceptionOrNull()!!

        // onImageInfoPassed
        val actions = mutableListOf<String>()
        createTileDecoder(
            logger = logger,
            subsamplingImage = SubsamplingImage(hugeLongQmshtImageSource),
            contentSize = hugeLongQmshtImageFile.size / 32f,
            regionDecoders = listOf(
                TestRegionDecoder.Factory(
                    imageInfo = ImageInfo(hugeLongQmshtImageFile.size, "image/jpeg"),
                    actions = actions
                )
            ),
        ).getOrThrow()
        assertEquals("[accept, create, checkSupport]", actions.toString())

        actions.clear()
        createTileDecoder(
            logger = logger,
            subsamplingImage = SubsamplingImage(
                imageSource = hugeLongQmshtImageSource,
                imageInfo = ImageInfo(hugeLongQmshtImageFile.size, "image/jpeg")
            ),
            contentSize = hugeLongQmshtImageFile.size / 32f,
            regionDecoders = listOf(
                TestRegionDecoder.Factory(
                    imageInfo = ImageInfo(hugeLongQmshtImageFile.size, "image/jpeg"),
                    actions = actions
                )
            ),
        ).getOrThrow()
        assertEquals("[accept, checkSupport, create]", actions.toString())

        actions.clear()
        createTileDecoder(
            logger = logger,
            subsamplingImage = SubsamplingImage(
                imageSource = hugeLongQmshtImageSource,
                imageInfo = ImageInfo(hugeLongQmshtImageFile.size, "image/png")
            ),
            contentSize = hugeLongQmshtImageFile.size / 32f,
            regionDecoders = listOf(
                TestRegionDecoder.Factory(
                    imageInfo = ImageInfo(hugeLongQmshtImageFile.size, "image/jpeg"),
                    actions = actions
                )
            ),
        ).getOrThrow()
        assertEquals("[accept, checkSupport, create, checkSupport]", actions.toString())

        // imageInfo
        createTileDecoder(
            logger = logger,
            subsamplingImage = SubsamplingImage(hugeLongQmshtImageSource),
            contentSize = hugeLongQmshtImageFile.size / 32f,
            regionDecoders = emptyList(),
        ).getOrThrow().apply {
            assertEquals(hugeLongQmshtImageFile.size, imageInfo.size)
            assertEquals("image/jpeg", imageInfo.mimeType)
        }
        createTileDecoder(
            logger = logger,
            subsamplingImage = SubsamplingImage(
                imageSource = hugeLongQmshtImageSource,
                imageInfo = ImageInfo(hugeLongQmshtImageFile.size * 2, "image/png")
            ),
            contentSize = hugeLongQmshtImageFile.size / 32f,
            regionDecoders = emptyList(),
        ).getOrThrow().apply {
            assertEquals(hugeLongQmshtImageFile.size * 2, imageInfo.size)
            assertEquals("image/png", imageInfo.mimeType)
        }
    }
}