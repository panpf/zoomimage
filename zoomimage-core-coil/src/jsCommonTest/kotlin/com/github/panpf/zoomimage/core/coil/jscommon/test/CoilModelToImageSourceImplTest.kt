package com.github.panpf.zoomimage.core.coil.jscommon.test

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.toUri
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.coil.CoilModelToImageSourceImpl
import com.github.panpf.zoomimage.subsampling.ByteArrayImageSource
import com.github.panpf.zoomimage.subsampling.FileImageSource
import com.github.panpf.zoomimage.subsampling.toFactory
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

class CoilModelToImageSourceImplTest {

    @Test
    fun test() {
        val context = PlatformContext.INSTANCE
        val imageLoader = ImageLoader.Builder(context).build()
        try {
            val modelToImageSource = CoilModelToImageSourceImpl()

            val httpUri = "http://www.example.com/image.jpg"
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpUri),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, httpUri)
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpUri),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, httpUri.toUri())
            )

            val httpsUri = "https://www.example.com/image.jpg"
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpsUri),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, httpsUri)
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpsUri),
                actual = modelToImageSource.dataToImageSource(
                    context,
                    imageLoader,
                    httpsUri.toUri()
                )
            )

            val pathUri = "/sdcard/image.jpg"
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, pathUri)
            )
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, pathUri.toUri())
            )

            val fileUri = "file:///sdcard/image.jpg"
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, fileUri)
            )
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, fileUri.toUri())
            )

            val path = "/sdcard/image.jpg".toPath()
            assertEquals(
                expected = FileImageSource(path).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, path)
            )

            val byteArray = "Hello".encodeToByteArray()
            assertEquals(
                expected = ByteArrayImageSource(byteArray).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, byteArray)
            )
        } finally {
            imageLoader.shutdown()
        }
    }
}