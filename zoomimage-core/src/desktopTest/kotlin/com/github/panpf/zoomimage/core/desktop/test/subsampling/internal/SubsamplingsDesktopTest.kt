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
            onImageInfoPassed = {}
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
            onImageInfoPassed = {}
        ).exceptionOrNull()!!
        createTileDecoder(
            logger = logger,
            subsamplingImage = SubsamplingImage(
                imageSource = hugeLongQmshtImageSource,
                imageInfo = ImageInfo(100, 0, "image/jpeg")
            ),
            contentSize = hugeLongQmshtImageFile.size / 32f,
            regionDecoders = emptyList(),
            onImageInfoPassed = {}
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
            onImageInfoPassed = {}
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
                hugeLongQmshtImageFile.size.height / 34
            ),
            regionDecoders = emptyList(),
            onImageInfoPassed = {}
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
            onImageInfoPassed = {}
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
            onImageInfoPassed = { actions.add("onImageInfoPassed") }
        ).getOrThrow()
        assertEquals("[accept, create, checkSupport, onImageInfoPassed]", actions.toString())

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
            onImageInfoPassed = { actions.add("onImageInfoPassed") }
        ).getOrThrow()
        assertEquals("[accept, checkSupport, onImageInfoPassed, create]", actions.toString())

        // imageInfo
        createTileDecoder(
            logger = logger,
            subsamplingImage = SubsamplingImage(hugeLongQmshtImageSource),
            contentSize = hugeLongQmshtImageFile.size / 32f,
            regionDecoders = emptyList(),
            onImageInfoPassed = { actions.add("onImageInfoPassed") }
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
            onImageInfoPassed = { actions.add("onImageInfoPassed") }
        ).getOrThrow().apply {
            assertEquals(hugeLongQmshtImageFile.size * 2, imageInfo.size)
            assertEquals("image/png", imageInfo.mimeType)
        }
    }
}