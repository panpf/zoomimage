package com.github.panpf.zoomimage.core.coil3.ios.test.internal

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.CachePolicy
import coil3.toUri
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.coil.internal.dataToImageSource
import com.github.panpf.zoomimage.subsampling.ByteArrayImageSource
import com.github.panpf.zoomimage.subsampling.FileImageSource
import com.github.panpf.zoomimage.subsampling.toFactory
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import platform.Foundation.NSURL
import kotlin.test.Test
import kotlin.test.assertEquals

class CoilCoreUtilsIosTest {

    private fun buildRequest(data: Any): coil3.request.ImageRequest {
        val context = PlatformContext.INSTANCE
        return coil3.request.ImageRequest.Builder(context)
            .data(data)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    @Test
    fun testDataToImageSource() = runTest {
        val context = PlatformContext.INSTANCE
        val imageLoader = ImageLoader.Builder(context).build()
        try {
            val httpUri = "http://www.example.com/image.jpg"
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, buildRequest(httpUri)),
                actual = dataToImageSource(context, imageLoader, buildRequest(httpUri))
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(
                    context,
                    imageLoader,
                    buildRequest(httpUri.toUri())
                ),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(httpUri.toUri())
                )
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(
                    context,
                    imageLoader,
                    buildRequest(NSURL(string = httpUri))
                ),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(NSURL(string = httpUri))
                )
            )

            val httpsUri = "https://www.example.com/image.jpg"
            assertEquals(
                expected = CoilHttpImageSource.Factory(
                    context,
                    imageLoader,
                    buildRequest(httpsUri)
                ),
                actual = dataToImageSource(context, imageLoader, buildRequest(httpsUri))
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(
                    context,
                    imageLoader,
                    buildRequest(httpsUri.toUri())
                ),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(httpsUri.toUri())
                )
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(
                    context,
                    imageLoader,
                    buildRequest(NSURL(string = httpsUri))
                ),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(NSURL(string = httpsUri))
                )
            )

            val pathUri = "/sdcard/image.jpg"
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(pathUri))
            )
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(NSURL(string = pathUri))
                )
            )
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(pathUri.toUri())
                )
            )

            val fileUri = "file:///sdcard/image.jpg"
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(fileUri))
            )
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(NSURL(string = fileUri))
                )
            )
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(fileUri.toUri())
                )
            )

            val path = "/sdcard/image.jpg".toPath()
            assertEquals(
                expected = FileImageSource(path).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(path))
            )

            val byteArray = "Hello".encodeToByteArray()
            assertEquals(
                expected = ByteArrayImageSource(byteArray).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(byteArray))
            )
        } finally {
            imageLoader.shutdown()
        }
    }
}