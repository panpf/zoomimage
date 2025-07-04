package com.github.panpf.zoomimage.core.coil3.android.test.internal

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import coil3.ImageLoader
import coil3.pathSegments
import coil3.request.CachePolicy
import coil3.toUri
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.coil.internal.dataToImageSource
import com.github.panpf.zoomimage.images.R
import com.github.panpf.zoomimage.subsampling.AssetImageSource
import com.github.panpf.zoomimage.subsampling.ByteArrayImageSource
import com.github.panpf.zoomimage.subsampling.ContentImageSource
import com.github.panpf.zoomimage.subsampling.FileImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.ResourceImageSource
import com.github.panpf.zoomimage.subsampling.toFactory
import kotlinx.coroutines.test.runTest
import okio.Buffer
import okio.Path.Companion.toPath
import okio.Source
import okio.Timeout
import okio.buffer
import java.io.File
import java.net.URL
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class CoilCoreUtilsAndroidTest {

    private fun buildRequest(data: Any): coil3.request.ImageRequest {
        val context = InstrumentationRegistry.getInstrumentation().context
        return coil3.request.ImageRequest.Builder(context)
            .data(data)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    @Test
    fun testDataToImageSource() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
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
                    buildRequest(Uri.parse(httpUri))
                ),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(Uri.parse(httpUri))
                )
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
                expected = null,
                actual = dataToImageSource(context, imageLoader, buildRequest(URL(httpUri)))
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
                    buildRequest(Uri.parse(httpsUri))
                ),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(Uri.parse(httpsUri))
                )
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
                expected = null,
                actual = dataToImageSource(context, imageLoader, buildRequest(URL(httpsUri)))
            )

            val contentUri = "content://myapp/image.jpg"
            assertEquals(
                expected = ContentImageSource(
                    context,
                    Uri.parse(contentUri)
                ).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(contentUri))
            )
            assertEquals(
                expected = ContentImageSource(
                    context,
                    Uri.parse(contentUri)
                ).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(Uri.parse(contentUri))
                )
            )
            assertEquals(
                expected = ContentImageSource(
                    context,
                    Uri.parse(contentUri)
                ).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(contentUri.toUri())
                )
            )

            val assetUri = "file:///android_asset/image.jpg"
            val assetFileName = assetUri.toUri().pathSegments.drop(1).joinToString("/")
            assertEquals(
                expected = AssetImageSource(context, assetFileName).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(assetUri))
            )
            assertEquals(
                expected = AssetImageSource(context, assetFileName).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(Uri.parse(assetUri))
                )
            )
            assertEquals(
                expected = AssetImageSource(context, assetFileName).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(assetUri.toUri())
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
                    buildRequest(Uri.parse(pathUri))
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
                    buildRequest(Uri.parse(fileUri))
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

            val file = File("/sdcard/image.jpg")
            assertEquals(
                expected = FileImageSource(file).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(file))
            )

            val resourceId = R.raw.huge_card
            assertEquals(
                expected = ResourceImageSource(context, resourceId).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(resourceId))
            )

            val resourceNameUri = "android.resource://${context.packageName}/raw/huge_card"
            assertEquals(
                expected = resourceId,
                actual = ((dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(resourceNameUri)
                ) as ImageSource.WrapperFactory).imageSource as ResourceImageSource).resId
            )
            assertEquals(
                expected = resourceId,
                actual = ((dataToImageSource(
                    context, imageLoader,
                    buildRequest(Uri.parse(resourceNameUri))
                ) as ImageSource.WrapperFactory).imageSource as ResourceImageSource).resId
            )
            assertEquals(
                expected = resourceId,
                actual = ((dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(resourceNameUri.toUri())
                ) as ImageSource.WrapperFactory).imageSource as ResourceImageSource).resId
            )

            val resourceIntUri = "android.resource://${context.packageName}/${resourceId}"
            assertEquals(
                expected = resourceId,
                actual = ((dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(resourceIntUri)
                ) as ImageSource.WrapperFactory).imageSource as ResourceImageSource).resId
            )
            assertEquals(
                expected = resourceId,
                actual = ((dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(Uri.parse(resourceIntUri))
                ) as ImageSource.WrapperFactory).imageSource as ResourceImageSource).resId
            )
            assertEquals(
                expected = resourceId,
                actual = ((dataToImageSource(
                    context,
                    imageLoader,
                    buildRequest(resourceIntUri.toUri())
                ) as ImageSource.WrapperFactory).imageSource as ResourceImageSource).resId
            )

            val byteArray = "Hello".toByteArray()
            assertEquals(
                expected = ByteArrayImageSource(byteArray).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(byteArray))
            )

            val byteBuffer = ByteBuffer.wrap("Hello".toByteArray())
            assertEquals(
                expected = ByteArrayImageSource(
                    byteBuffer.asSource().buffer().use { it.readByteArray() }).toFactory(),
                actual = dataToImageSource(context, imageLoader, buildRequest(byteBuffer))
            )
        } finally {
            imageLoader.shutdown()
        }
    }

    internal fun ByteBuffer.asSource() = object : Source {
        private val buffer = this@asSource.slice()
        private val len = buffer.capacity()

        override fun close() = Unit

        override fun read(sink: Buffer, byteCount: Long): Long {
            if (buffer.position() == len) return -1
            val pos = buffer.position()
            val newLimit = (pos + byteCount).toInt().coerceAtMost(len)
            buffer.limit(newLimit)
            return sink.write(buffer).toLong()
        }

        override fun timeout() = Timeout.NONE
    }
}